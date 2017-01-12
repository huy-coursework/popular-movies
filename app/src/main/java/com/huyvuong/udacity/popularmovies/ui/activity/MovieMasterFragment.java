package com.huyvuong.udacity.popularmovies.ui.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.huyvuong.udacity.popularmovies.R;
import com.huyvuong.udacity.popularmovies.data.MovieContract;
import com.huyvuong.udacity.popularmovies.gateway.TmdbGateway;
import com.huyvuong.udacity.popularmovies.model.transport.GetMoviesResponse;
import com.huyvuong.udacity.popularmovies.model.business.Movie;
import com.huyvuong.udacity.popularmovies.ui.PosterAdapter;
import com.huyvuong.udacity.popularmovies.util.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.observables.ConnectableObservable;

import static com.huyvuong.udacity.popularmovies.ui.activity.MovieMasterFragment
        .MovieDisplayCriteria.POPULAR;

/**
 * Fragment containing the master view of the movies retrieved from TMDb, represented as movie
 * poster images that the user can click on.
 */
public class MovieMasterFragment
        extends DialogFragment
{
    private static final String KEY_CRITERIA = "movieDisplayCriteria";
    private static final String[] MOVIE_PROJECTION = new String[]
            {
                    MovieContract.MovieEntry.COLUMN_MOVIE_ID,
                    MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE,
                    MovieContract.MovieEntry.COLUMN_POSTER_PATH,
                    MovieContract.MovieEntry.COLUMN_PLOT_SYNOPSIS,
                    MovieContract.MovieEntry.COLUMN_RATING,
                    MovieContract.MovieEntry.COLUMN_RELEASE_DATE
            };
    private static final int INDEX_MOVIE_ID = 0;
    private static final int INDEX_ORIGINAL_TITLE = 1;
    private static final int INDEX_POSTER_PATH = 2;
    private static final int INDEX_PLOT_SYNOPSIS = 3;
    private static final int INDEX_RATING = 4;
    private static final int INDEX_RELEASE_DATE = 5;

    @BindView(R.id.grid_movies)
    GridView moviesGridView;

    @BindView(R.id.text_empty_movies)
    TextView emptyMovieTextView;

    private PosterAdapter posterAdapter;
    private Snackbar offlineSnackbar;
    private Unbinder unbinder;
    private MovieDisplayCriteria movieDisplayCriteria;

    public MovieMasterFragment()
    {
        // Mark that this fragment has menu items to add.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Initialize the GridView to show movie posters.
        View rootView = inflater.inflate(R.layout.fragment_movie_master, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        // Populate the grid view with movie posters that when clicked, open the detail view for
        // that movie.
        posterAdapter = new PosterAdapter(
                getActivity(),
                R.layout.grid_item_poster,
                new ArrayList<>());
        moviesGridView.setAdapter(posterAdapter);
        moviesGridView.setOnItemClickListener(startDetailActivity());

        // Determine the previous criteria used to display movies. If there were none, default to
        // showing popular movies.
        if (savedInstanceState != null &&
            savedInstanceState.getSerializable(KEY_CRITERIA) != null &&
            savedInstanceState.getSerializable(KEY_CRITERIA) instanceof MovieDisplayCriteria)
        {
            movieDisplayCriteria =
                    (MovieDisplayCriteria) savedInstanceState.getSerializable(KEY_CRITERIA);
        }
        else
        {
            movieDisplayCriteria = POPULAR;
        }

        // Return the root view to display for this fragment.
        return rootView;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (POPULAR.equals(movieDisplayCriteria))
        {
            // Show popular movies.
            getMoviesBy(TmdbGateway.MovieSortingCriteria.POPULAR);
        }
        else if (MovieDisplayCriteria.TOP_RATED.equals(movieDisplayCriteria))
        {
            // Show top rated movies.
            getMoviesBy(TmdbGateway.MovieSortingCriteria.TOP_RATED);
        }
        else if (MovieDisplayCriteria.FAVORITE.equals(movieDisplayCriteria))
        {
            // Show favorite movies.
            populateMoviesWith(queryForFavoriteMovies());
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_CRITERIA, movieDisplayCriteria);
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_movie_master_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_set_criteria_popular:
                // Show popular movies.
                movieDisplayCriteria = POPULAR;
                getMoviesBy(TmdbGateway.MovieSortingCriteria.POPULAR);
                return true;
            case R.id.action_set_criteria_top_rated:
                // Show top rated movies.
                movieDisplayCriteria = MovieDisplayCriteria.TOP_RATED;
                getMoviesBy(TmdbGateway.MovieSortingCriteria.TOP_RATED);
                return true;
            case R.id.action_set_criteria_favorite:
                // Show favorite movies.
                movieDisplayCriteria = MovieDisplayCriteria.FAVORITE;
                populateMoviesWith(queryForFavoriteMovies());
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Shows a TextView indicating why there are no movies shown in the UI and hides the GridView
     * containing each of the movie posters.
     *
     * Call this method when there are no movies to show, either because there are none or if an
     * error occurred.
     */
    private void showEmptyMovieView(String message)
    {
        moviesGridView.setVisibility(View.GONE);
        emptyMovieTextView.setText(message);
        emptyMovieTextView.setVisibility(View.VISIBLE);
    }

    /**
     * Shows the GridView containing the movies posters and hides the TextView indicating that there
     * were no movies to show.
     *
     * Call this method when there exist movies to show.
     */
    private void showMoviesGridView()
    {
        moviesGridView.setVisibility(View.VISIBLE);
        moviesGridView.invalidate();
        emptyMovieTextView.setVisibility(View.GONE);
    }

    /**
     * Queries for and returns the list of the user's favorite movies.
     *
     * @return
     *     list of favorite movies marked by the user
     */
    @NonNull
    private List<Movie> queryForFavoriteMovies()
    {
        // Query for all of the movies that the user has marked as favorites.
        Cursor cursor = getContext().getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                MOVIE_PROJECTION,
                null,
                null,
                null);

        // Convert the returned database rows to Movie objects.
        List<Movie> movies = new ArrayList<>();
        if (cursor != null)
        {
            while (cursor.moveToNext())
            {
                Movie movie = new Movie.Builder()
                        .withId(cursor.getInt(INDEX_MOVIE_ID))
                        .withOriginalTitle(cursor.getString(INDEX_ORIGINAL_TITLE))
                        .withPosterPath(cursor.getString(INDEX_POSTER_PATH))
                        .withPlotSynopsis(cursor.getString(INDEX_PLOT_SYNOPSIS))
                        .withRating(cursor.getDouble(INDEX_RATING))
                        .withReleaseDate(cursor.getString(INDEX_RELEASE_DATE))
                        .build();
                movies.add(movie);
            }
            cursor.close();
        }
        return movies;
    }

    /**
     * Populates the grid view of movies with the given list of movies.
     *
     * @param movies
     *     movies whose posted to populate the grid view with
     */
    private void populateMoviesWith(List<Movie> movies)
    {
        if (movies.isEmpty())
        {
            showEmptyMovieView(getString(R.string.message_movies_empty));
        }
        else
        {
            posterAdapter.clear();
            Stream.of(movies).forEach(movie -> posterAdapter.add(movie));
            showMoviesGridView();
        }
    }

    /**
     * Calls TMDb to populate the GridView with movies fulfilling the given search criteria.
     *
     * If the device is currently offline, shows a Snackbar instead that indicates that the device
     * is offline and provides the user a way to retry.
     *
     * @param movieSortingCriteria
     *     sorting criteria to sort movies by
     */
    private void getMoviesBy(String movieSortingCriteria)
    {
        if (NetworkUtils.isOnline(getActivity()))
        {
            // Populate the GridView with movie posters as retrieved from TMDb.
            ConnectableObservable<GetMoviesResponse> getMoviesObservable =
                    new TmdbGateway().getMovies(movieSortingCriteria);
            getMoviesObservable.flatMap(response -> Observable.from(response.getMovies()))
                               .toList()
                               .subscribe(this::populateMoviesWith,
                                          error -> showEmptyMovieView(
                                                  getString(
                                                          R.string.message_movies_error_loading)));
            getMoviesObservable.connect();

            // If the device is no longer offline, then no point showing the Snackbar notifying the
            // user that their device is offline.
            if (offlineSnackbar != null)
            {
                offlineSnackbar.dismiss();
            }
        }
        else
        {
            // Show a Snackbar that allows the user to retry the request using the same movie
            // sorting criteria that they originally selected.
            offlineSnackbar = Snackbar
                    .make(
                            moviesGridView,
                            R.string.snackbar_offline_message,
                            Snackbar.LENGTH_INDEFINITE)
                    .setAction(
                            R.string.snackbar_offline_action_retry,
                            view -> getMoviesBy(movieSortingCriteria));
            offlineSnackbar.show();
        }
    }

    /**
     * Returns an OnItemClickListener for starting the detail activity for the movie corresponding
     * to the movie poster that was clicked on. This is for use in the movie poster GridView.
     *
     * @return
     *     {@link AdapterView.OnItemClickListener} for opening the detail activity for the movie
     *     clicked on
     */
    @NonNull
    private AdapterView.OnItemClickListener startDetailActivity()
    {
        return (AdapterView<?> parent, View view, int position, long id) ->
        {
            Intent detailIntent = new Intent(getContext(), MovieDetailActivity.class)
                    .putExtra(
                            MovieDetailFragment.KEY_MOVIE,
                            (Movie) parent.getAdapter().getItem(position));
            getContext().startActivity(detailIntent);
        };
    }

    /**
     * Enum to determine which kinds of movies to display.
     */
    enum MovieDisplayCriteria
    {
        POPULAR,
        TOP_RATED,
        FAVORITE
    }
}