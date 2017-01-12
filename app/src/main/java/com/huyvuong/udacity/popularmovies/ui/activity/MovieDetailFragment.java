package com.huyvuong.udacity.popularmovies.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import com.huyvuong.udacity.popularmovies.gateway.response.GetVideosResponse;
import com.huyvuong.udacity.popularmovies.model.Movie;
import com.huyvuong.udacity.popularmovies.model.Review;
import com.huyvuong.udacity.popularmovies.model.Video;
import com.huyvuong.udacity.popularmovies.util.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
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

    @BindView(R.id.linear_trailers)
    LinearLayout trailersLinearLayout;

    @BindView(R.id.text_empty_trailers)
    TextView emptyTrailerTextView;

    @BindView(R.id.linear_reviews)
    LinearLayout reviewsLinearLayout;

    @BindView(R.id.text_empty_reviews)
    TextView emptyReviewTextView;

    private Unbinder unbinder;

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

            // Populate the trailers listings.
            getTrailersFor(movie);

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
     * Shows a TextView indicating why there are no trailers shown in the UI and hides the
     * LinearLayout containing each of the trailer entries.
     *
     * Call this method when there are no trailers from TMDb to show, either because there are none
     * or if an error occurred.
     */
    private void showEmptyTrailersView(String message)
    {
        trailersLinearLayout.setVisibility(View.GONE);
        emptyTrailerTextView.setText(message);
        emptyTrailerTextView.setVisibility(View.VISIBLE);
    }

    /**
     * Shows the LinearLayout containing each of the trailer entries pulled from TMDb and hides the
     * TextView indicating that there were no trailers for the movie.
     *
     * Call this method when the response from TMDb contains trailers.
     */
    private void showTrailersLinearLayoutView()
    {
        trailersLinearLayout.setVisibility(View.VISIBLE);
        emptyTrailerTextView.setVisibility(View.GONE);
    }

    /**
     * Binds the given video metadata to a View object for rendering to the user.
     *
     * @param trailer
     *     trailer link to display to user
     * @param viewGroup
     *     containing view for the view being created
     * @return
     *     View object displaying a view linking the user to YouTube from the given video metadata
     */
    private View bindTrailerView(Video trailer, ViewGroup viewGroup)
    {
        View trailerView = LayoutInflater.from(getActivity())
                                         .inflate(R.layout.list_item_trailer, viewGroup, false);

        TextView nameTextView = (TextView) trailerView.findViewById(R.id.text_trailer_name);
        nameTextView.setText(trailer.getName());

        // Set up an OnClickListener to open YouTube to show the trailer to the user.
        Uri youtubeUrl = Uri.parse("https://www.youtube.com/watch")
                            .buildUpon()
                            .appendQueryParameter("v", trailer.getKey())
                            .build();
        trailerView.setOnClickListener(
                view -> startActivity(new Intent(Intent.ACTION_VIEW, youtubeUrl)));

        return trailerView;
    }

    /**
     * Clears out the trailers on the UI and repopulates the UI with the given list of videos.
     *
     * @param trailers
     *     list of trailers to display to the user
     */
    private void populateTrailersWith(List<Video> trailers)
    {
        if (trailers.isEmpty())
        {
            showEmptyTrailersView(getString(R.string.message_trailers_empty));
        }
        else
        {
            trailersLinearLayout.removeAllViews();
            Stream.of(trailers)
                  .map(video -> bindTrailerView(video, trailersLinearLayout))
                  .forEach(trailerView -> trailersLinearLayout.addView(trailerView));
            showTrailersLinearLayoutView();
        }
    }

    /**
     * Calls TMDb to populate the trailers LinearLayout with trailers for the given movie.
     *
     * @param movie
     *     movie to retrieve reviews for
     */
    private void getTrailersFor(Movie movie)
    {
        if (NetworkUtils.isOnline(getActivity()))
        {
            int movieId = movie.getId();

            // Populate the LinearLayout with trailer videos as retrieved from TMDb.
            ConnectableObservable<GetVideosResponse> getVideosObservable =
                    new TmdbGateway().getVideos(movieId);
            getVideosObservable
                    .flatMap(response -> Observable.from(response.getVideos()))
                    .filter(video -> "Trailer".equals(video.getType()))
                    .toList()
                    .subscribe(
                            this::populateTrailersWith,
                            error -> showEmptyTrailersView(
                                    getString(R.string.message_trailers_error_loading)));
            getVideosObservable.connect();
        }
        else
        {
            // Notify the user that they're currently offline.
            showEmptyTrailersView(getString(R.string.message_trailers_offline));
        }
    }

    /**
     * Shows a TextView indicating why there are no reviews shown in the UI and hides the
     * LinearLayout containing each of the review entries.
     *
     * Call this method when there are no reviews from TMDb to show, either because there are none
     * or if an error occurred.
     */
    private void showEmptyReviewsView(String message)
    {
        reviewsLinearLayout.setVisibility(View.GONE);
        emptyReviewTextView.setText(message);
        emptyReviewTextView.setVisibility(View.VISIBLE);
    }

    /**
     * Shows the LinearLayout containing each of the review entries pulled from TMDb and hides the
     * TextView indicating that there were no reviews for the movie.
     *
     * Call this method when the response from TMDb contains reviews.
     */
    private void showReviewsLinearLayoutView()
    {
        reviewsLinearLayout.setVisibility(View.VISIBLE);
        emptyReviewTextView.setVisibility(View.GONE);
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
            showEmptyReviewsView(getString(R.string.message_reviews_empty));
        }
        else
        {
            reviewsLinearLayout.removeAllViews();
            Stream.of(reviews)
                  .map(review -> bindReviewView(review, reviewsLinearLayout))
                  .forEach(reviewView -> reviewsLinearLayout.addView(reviewView));
            showReviewsLinearLayoutView();
        }
    }

    /**
     * Calls TMDb to populate the reviews LinearLayout with reviews for the given movie.
     *
     * @param movie
     *     movie to retrieve reviews for
     */
    private void getReviewsFor(Movie movie)
    {
        if (NetworkUtils.isOnline(getActivity()))
        {
            int movieId = movie.getId();

            // Populate the LinearLayout with movie reviews as retrieved from TMDb.
            ConnectableObservable<GetReviewsResponse> getReviewsObservable =
                    new TmdbGateway().getReviews(movieId);
            getReviewsObservable.subscribe(
                    response -> populateReviewsWith(response.getReviews()),
                    error -> showEmptyReviewsView(
                            getString(R.string.message_reviews_error_loading)));
            getReviewsObservable.connect();
        }
        else
        {
            // Notify the user that they're currently offline.
            showEmptyReviewsView(getString(R.string.message_reviews_offline));
        }
    }
}
