package com.huyvuong.udacity.popularmovies.ui.activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.huyvuong.udacity.popularmovies.R;
import com.huyvuong.udacity.popularmovies.data.MovieContract;
import com.huyvuong.udacity.popularmovies.gateway.TmdbGateway;
import com.huyvuong.udacity.popularmovies.model.business.Movie;
import com.huyvuong.udacity.popularmovies.model.business.Review;
import com.huyvuong.udacity.popularmovies.model.business.Video;
import com.huyvuong.udacity.popularmovies.model.transport.GetReviewsResponse;
import com.huyvuong.udacity.popularmovies.model.transport.GetVideosResponse;
import com.huyvuong.udacity.popularmovies.util.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

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

    // Constants for use in constructing a YouTube URL.
    private static final String YOUTUBE_URL_START = "https://www.youtube.com/watch";
    private static final String QUERY_PARAMETER_VIDEO = "v";

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

    private MenuItem menuFavorite;

    private Unbinder unbinder;
    private Movie movie;

    public MovieDetailFragment()
    {
        // Mark that this fragment has menu items to add.
        setHasOptionsMenu(true);
    }

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
            movie = intent.getParcelableExtra(KEY_MOVIE);

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
                   .load(String.format(MOVIE_POSTER_URL_FORMAT, movie.getPosterPath()))
                   .placeholder(
                           new ColorDrawable(getContext().getResources()
                                                         .getColor(R.color.loadingPosterColor)))
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_movie_detail_fragment, menu);
        menuFavorite = menu.findItem(R.id.action_favorite);

        // Change the icon for the favorite button based on whether or not the current movie is
        // marked as a favorite. If it is, show the filled icon. Otherwise, show the outline icon.
        Uri movieUri = MovieContract.MovieEntry.buildMovieUriWithTmdbId(movie.getId());
        int favoriteIconDrawable = isFavorite(getContext().getContentResolver(), movieUri) ?
                                   R.drawable.ic_action_action_favorite :
                                   R.drawable.ic_action_action_favorite_outline;
        menuFavorite.setIcon(favoriteIconDrawable);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_favorite:
                // Toggle whether or not to mark this movie as a favorite.
                toggleFavorite();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        unbinder.unbind();
    }


    /**
     * Toggles whether or not the current movie is marked as a favorite and updates the favorite
     * icon to indicate the new favorite status.
     *
     * If the current movie is currently marked as a favorite, it unmarks it as a favorite.
     * Otherwise, it marks it as a favorite.
     */
    private void toggleFavorite()
    {
        ContentResolver contentResolver = getContext().getContentResolver();
        Uri movieUri = MovieContract.MovieEntry.buildMovieUriWithTmdbId(movie.getId());

        // Check if the movie is currently marked as a favorite.
        Observable<Boolean> favoriteObservable = Observable.create(
                subscriber -> subscriber.onNext(isFavorite(contentResolver, movieUri)));
        favoriteObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(isFavorite ->
                           {
                               if (isFavorite)
                               {
                                   unmarkFavorite(movieUri);
                               }
                               else
                               {
                                   markFavorite();
                               }
                           });
    }

    /**
     * Unmarks the currently display movie as a favorite and updates the favorite icon to the
     * outlined version.
     *
     * @param movieUri
     *     URI for the movie to unmark as a favorite
     */
    private void unmarkFavorite(Uri movieUri)
    {
        Observable<Integer> deleteObservable = Observable.create(
                subscriber -> subscriber.onNext(
                        getContext().getContentResolver().delete(
                                movieUri,
                                MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                                new String[] {String.valueOf(movie.getId())})));
        deleteObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rowsDeleted ->
                                   menuFavorite.setIcon(
                                           R.drawable.ic_action_action_favorite_outline));
    }

    /**
     * Marks the current movie as a favorite movie and updates the favorite icon to the
     * filled version.
     */
    private void markFavorite()
    {
        Observable<Uri> insertObservable = Observable.create(
                subscriber -> subscriber.onNext(
                        getContext()
                                .getContentResolver()
                                .insert(MovieContract.MovieEntry.CONTENT_URI,
                                        movie.toContentValues())));
        insertObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(uri -> menuFavorite.setIcon(R.drawable.ic_action_action_favorite));
    }

    /**
     * Returns true if the movie shown is marked as a favorite by the user. Returns false otherwise.
     *
     * @param contentResolver
     *     content resolver to query against
     * @param movieUri
     *     URI referring to the movie to check favorite status for
     * @return
     *     true if the given movie URI corresponds to a favorite movie; false otherwise
     */
    private boolean isFavorite(ContentResolver contentResolver, Uri movieUri)
    {
        Cursor cursor = contentResolver.query(movieUri, null, null, null, null);
        boolean isFavorite = false;
        if (cursor != null)
        {
            isFavorite = cursor.moveToFirst();
            cursor.close();
        }
        return isFavorite;
    }

    /**
     * Shows a TextView indicating why there are no trailers shown in the UI and hides the
     * LinearLayout containing each of the trailer entries.
     *
     * Call this method when there are no trailers from TMDb to show, either because there are none
     * or if an error occurred.
     */
    private void showEmptyTrailerView(String message)
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
        Uri youtubeUrl = Uri.parse(YOUTUBE_URL_START)
                            .buildUpon()
                            .appendQueryParameter(QUERY_PARAMETER_VIDEO, trailer.getKey())
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
            showEmptyTrailerView(getString(R.string.message_trailers_empty));
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
            getVideosObservable.flatMap(response -> Observable.from(response.getVideos()))
                               .filter(video -> "Trailer".equals(video.getType()))
                               .toList()
                               .subscribe(
                                       this::populateTrailersWith,
                                       error -> showEmptyTrailerView(
                                               getString(R.string.message_trailers_error_loading)));
            getVideosObservable.connect();
        }
        else
        {
            // Notify the user that they're currently offline.
            showEmptyTrailerView(getString(R.string.message_trailers_offline));
        }
    }

    /**
     * Shows a TextView indicating why there are no reviews shown in the UI and hides the
     * LinearLayout containing each of the review entries.
     *
     * Call this method when there are no reviews from TMDb to show, either because there are none
     * or if an error occurred.
     */
    private void showEmptyReviewView(String message)
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
            showEmptyReviewView(getString(R.string.message_reviews_empty));
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
                    error -> showEmptyReviewView(
                            getString(R.string.message_reviews_error_loading)));
            getReviewsObservable.connect();
        }
        else
        {
            // Notify the user that they're currently offline.
            showEmptyReviewView(getString(R.string.message_reviews_offline));
        }
    }
}
