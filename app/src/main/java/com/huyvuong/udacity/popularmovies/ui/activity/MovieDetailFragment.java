package com.huyvuong.udacity.popularmovies.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.huyvuong.udacity.popularmovies.R;
import com.huyvuong.udacity.popularmovies.gateway.TmdbGateway;
import com.huyvuong.udacity.popularmovies.gateway.response.GetReviewsResponse;
import com.huyvuong.udacity.popularmovies.model.Movie;
import com.huyvuong.udacity.popularmovies.model.Review;
import com.huyvuong.udacity.popularmovies.util.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.observables.ConnectableObservable;

/**
 * Fragment containing the detail view of a movie retrieved from TMDb, which shows more detailed
 * metadata about a given movie.
 */
public class MovieDetailFragment
        extends Fragment
{
    // Intent Extra Keys
    public static final String KEY_MOVIE = "movie";

    // Used for loading the poster image.
    private static final String MOVIE_POSTER_URL_FORMAT = "http://image.tmdb.org/t/p/w342/%s";

    // Indicates that no average rating for a movie was found.
    private static final double NOT_FOUND = -1.0;

    @BindView(R.id.activity_movie_detail)
    ScrollView rootView;

    @BindView(R.id.text_original_title)
    TextView originalTitleText;

    @BindView(R.id.image_poster)
    ImageView posterImage;

    @BindView(R.id.text_rating)
    TextView ratingText;

    @BindView(R.id.text_release_date)
    TextView releaseDateText;

    @BindView(R.id.text_plot_synopsis)
    TextView plotSynopsisText;

    @BindView(R.id.linear_reviews)
    LinearLayout reviewsLinearLayout;

    @BindView(R.id.text_empty_reviews)
    TextView emptyTextView;

    private Unbinder unbinder;
    private Snackbar offlineSnackbar;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        // If an intent was passed to this activity, get movie data from that intent's extra data
        // and populate the views for this fragment with that data.
        Intent intent = getActivity().getIntent();
        if (intent != null &&
                intent.hasExtra(KEY_MOVIE) &&
                intent.getParcelableExtra(KEY_MOVIE) instanceof Movie)
        {
            // Obtain the Movie object from the intent.
            Movie movie = intent.getParcelableExtra(KEY_MOVIE);

            // Populate the original title.
            String title = movie.getOriginalTitle();
            originalTitleText.setText(title);
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null)
            {
                actionBar.setTitle(title);
            }

            // Load the poster image.
            Picasso.with(getActivity())
                    .load(String.format(
                            MOVIE_POSTER_URL_FORMAT,
                            movie.getPosterPath()))
                    .into(posterImage);

            // Populate the average rating.
            double rating = movie.getRating();
            ratingText.setText((rating > NOT_FOUND) ? String.valueOf(rating) : "--");

            // Populate the release date.
            releaseDateText.setText(movie.getReleaseDate());

            // Populate the plot synopsis.
            plotSynopsisText.setText(movie.getPlotSynopsis());

            // Populate the reviews listings.
            getReviewsFor(movie);
        }

        return rootView;
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        unbinder.unbind();
    }

    /**
     * Shows a TextView indicating that there were no reviews for the movie on TMDb and hides the
     * LinearLayout containing each of the review entries.
     *
     * Call this method when the response from TMDb contains no reviews.
     */
    private void showEmptyView()
    {
        reviewsLinearLayout.setVisibility(View.GONE);
        emptyTextView.setVisibility(View.VISIBLE);
    }

    /**
     * Shows the LinearLayout containing each the review entries pulled from TMDb and hides the
     * TextView indicating that there were no reviews for the movie.
     *
     * Call this method when the response from TMDb contains reviews.
     */
    private void showReviewsRecyclerView()
    {
        reviewsLinearLayout.setVisibility(View.VISIBLE);
        emptyTextView.setVisibility(View.GONE);
    }

    /**
     * Binds the given review data to a View object for rendering to the user.
     *
     * @param review
     *     review to display data from
     * @param viewGroup
     *     containing view for the view being created
     * @return
     *     View object displaying review data from the given review
     */
    private View bindReviewView(Review review, ViewGroup viewGroup)
    {
        View reviewView = LayoutInflater.from(getActivity())
                                        .inflate(R.layout.list_item_review, viewGroup, false);

        TextView letterTextView = (TextView) reviewView.findViewById(R.id.text_review_letter);
        letterTextView.setText(String.valueOf(review.getAuthor().charAt(0)).toUpperCase());

        TextView authorTextView = (TextView) reviewView.findViewById(R.id.text_review_author);
        authorTextView.setText(review.getAuthor());

        TextView contentTextView = (TextView) reviewView.findViewById(R.id.text_review_content);
        contentTextView.setText(review.getContent());

        return reviewView;
    }

    /**
     * Clears out the reviews on the UI and repopulates the UI with the given list of reviews.
     *
     * @param reviews
     *     list of reviews to display to the user
     */
    private void populateReviewsWith(List<Review> reviews)
    {
        if (reviews.isEmpty())
        {
            showEmptyView();
        }
        else
        {
            reviewsLinearLayout.removeAllViews();
            Stream.of(reviews)
                  .map(review -> bindReviewView(review, reviewsLinearLayout))
                  .forEach(reviewView -> reviewsLinearLayout.addView(reviewView));
            showReviewsRecyclerView();
        }
    }

    /**
     * Calls TMDb to populate the RecyclerView with reviews for the given movie.
     *
     * If the device is currently offline, shows a Snackbar instead that indicates that the device
     * is offline and provides the user a way to retry.
     *
     * @param movie
     *     movie to retrieve reviews for
     */
    private void getReviewsFor(Movie movie)
    {
        if (NetworkUtils.isOnline(getActivity()))
        {
            int movieId = movie.getId();

            // Populate the RecyclerView with movie reviews as retrieved from TMDb.
            ConnectableObservable<GetReviewsResponse> getReviewsObservable =
                    new TmdbGateway().getReviews(movieId);
            getReviewsObservable.subscribe(response -> populateReviewsWith(response.getReviews()));
            getReviewsObservable.connect();

            // If the device is no longer offline, then no point showing the Snackbar notifying the
            // user that their device is offline.
            if (offlineSnackbar != null)
            {
                offlineSnackbar.dismiss();
            }
        }
        else
        {
            // Show a Snackbar that allows the user to retry the request for the same movie.
            offlineSnackbar = Snackbar
                    .make(
                            rootView,
                            R.string.snackbar_offline_message,
                            Snackbar.LENGTH_INDEFINITE)
                    .setAction(
                            R.string.snackbar_offline_action_retry,
                            view -> getReviewsFor(movie));
            offlineSnackbar.show();
        }
    }
}
