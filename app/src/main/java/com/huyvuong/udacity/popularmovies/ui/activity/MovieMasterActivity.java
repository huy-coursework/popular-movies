package com.huyvuong.udacity.popularmovies.ui.activity;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.huyvuong.udacity.popularmovies.R;
import com.huyvuong.udacity.popularmovies.data.MovieContract;
import com.huyvuong.udacity.popularmovies.gateway.TmdbGateway;
import com.huyvuong.udacity.popularmovies.model.business.Movie;
import com.huyvuong.udacity.popularmovies.model.transport.GetMoviesResponse;
import com.huyvuong.udacity.popularmovies.ui.PosterAdapter;
import com.huyvuong.udacity.popularmovies.util.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

/**
 * Activity containing the master view of the movies retrieved from TMDb.
 */
public class MovieMasterActivity
        extends AppCompatActivity
{
    private static final String LOG_TAG = MovieMasterActivity.class.getSimpleName();

    private static final String KEY_CRITERIA = "movieDisplayCriteria";
    private static final String BUNDLE_MOVIE_LIST = "movies";
    private static final String BUNDLE_RECYCLER_VIEW_STATE = "recyclerViewState";

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

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.recycler_movies)
    RecyclerView moviesRecyclerView;

    @BindView(R.id.text_empty_movies)
    TextView emptyMovieTextView;

    private PosterAdapter posterAdapter;
    private GridLayoutManager gridLayoutManager;
    private Parcelable recyclerViewState;
    private ArrayList<Movie> movies;
    private Snackbar offlineSnackbar;
    private MovieDisplayCriteria movieDisplayCriteria;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_master);

        // Initialize the RecyclerView to show movie posters.
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        // Populate the grid view with movie posters that when clicked, open the detail view for
        // that movie.
        if (savedInstanceState != null &&
            savedInstanceState.getParcelableArrayList(BUNDLE_MOVIE_LIST) != null)
        {
            movies = savedInstanceState.getParcelableArrayList(BUNDLE_MOVIE_LIST);
        }
        else
        {
            movies = new ArrayList<>();
        }
        posterAdapter = new PosterAdapter(this, movies);
        moviesRecyclerView.setAdapter(posterAdapter);
        gridLayoutManager = new GridLayoutManager(
                this,
                getResources().getInteger(R.integer.movie_recycler_span_count));
        moviesRecyclerView.setLayoutManager(gridLayoutManager);

        // Determine the previous criteria used to display movies. If there were none, default to
        // showing popular movies.
        movieDisplayCriteria = loadMovieDisplayCriteria();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (moviesRecyclerView != null)
        {
            moviesRecyclerView.getAdapter().notifyDataSetChanged();
            if (MovieDisplayCriteria.POPULAR.equals(movieDisplayCriteria) &&
                moviesRecyclerView.getAdapter().getItemCount() == 0)
            {
                // Show popular movies.
                getMoviesBy(TmdbGateway.MovieSortingCriteria.POPULAR);
            }
            else if (MovieDisplayCriteria.TOP_RATED.equals(movieDisplayCriteria) &&
                     moviesRecyclerView.getAdapter().getItemCount() == 0)
            {
                // Show top rated movies.
                getMoviesBy(TmdbGateway.MovieSortingCriteria.TOP_RATED);
            }
            else if (MovieDisplayCriteria.FAVORITE.equals(movieDisplayCriteria))
            {
                // Show favorite movies.
                getFavoriteMovies();
            }
        }
        if (gridLayoutManager != null)
        {
            gridLayoutManager.onRestoreInstanceState(recyclerViewState);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        // Restore the scroll state of the RecyclerView.
        //
        // From:
        // - http://stackoverflow.com/a/12503875
        // - http://stackoverflow.com/a/28262885
        if (savedInstanceState != null &&
            savedInstanceState.getParcelable(BUNDLE_RECYCLER_VIEW_STATE) != null &&
            moviesRecyclerView != null)
        {
            recyclerViewState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_VIEW_STATE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        // Store the most recent display criteria so that when the activity is reloaded, it
        // restores that state without the user needing to select anything.
        saveMovieDisplayCriteria(movieDisplayCriteria);

        // Store the scroll state of the RecyclerView to restore when reloading this activity.
        //
        // From:
        // - http://stackoverflow.com/a/12503875
        // - http://stackoverflow.com/a/28262885
        if (moviesRecyclerView != null &&
            moviesRecyclerView.getLayoutManager() != null &&
            gridLayoutManager != null)
        {
            recyclerViewState = gridLayoutManager.onSaveInstanceState();
            outState.putParcelable(BUNDLE_RECYCLER_VIEW_STATE, recyclerViewState);
        }
        if (movies != null)
        {
            outState.putParcelableArrayList(BUNDLE_MOVIE_LIST, movies);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_movie_master, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_set_criteria_popular:
                // Show popular movies.
                movieDisplayCriteria = MovieDisplayCriteria.POPULAR;
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
                getFavoriteMovies();
                return true;
            case R.id.action_legal:
                Toast.makeText(this, R.string.legal_tmdb_disclaimer, Toast.LENGTH_SHORT).show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Shows a TextView indicating why there are no movies shown in the UI and hides the
     * RecyclerView containing each of the movie posters.
     *
     * Call this method when there are no movies to show, either because there are none or if an
     * error occurred.
     */
    private void showEmptyMovieView(String message)
    {
        if (moviesRecyclerView != null)
        {
            moviesRecyclerView.setVisibility(View.GONE);
        }
        if (emptyMovieTextView != null)
        {
            emptyMovieTextView.setText(message);
            emptyMovieTextView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Shows the RecyclerView containing the movies posters and hides the TextView indicating that
     * there were no movies to show.
     *
     * Call this method when there exist movies to show.
     */
    private void showMoviesRecyclerView()
    {
        if (moviesRecyclerView != null)
        {
            moviesRecyclerView.setVisibility(View.VISIBLE);
            moviesRecyclerView.invalidate();
        }
        if (emptyMovieTextView != null)
        {
            emptyMovieTextView.setVisibility(View.GONE);
        }
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
        Cursor cursor = getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                MOVIE_PROJECTION,
                null,
                null,
                null);

        // Convert the returned database rows to Movie objects.
        List<Movie> favoriteMovies = new ArrayList<>();
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
                favoriteMovies.add(movie);
            }
            cursor.close();
        }
        return favoriteMovies;
    }

    /**
     * Populates the grid view of movies with the given list of movies.
     *
     * @param moviesToPopulateWith
     *     movies whose posted to populate the grid view with
     */
    private void populateMoviesWith(List<Movie> moviesToPopulateWith)
    {
        movies.clear();
        movies.addAll(moviesToPopulateWith);
        if (movies.isEmpty())
        {
            showEmptyMovieView(getString(R.string.message_movies_empty));
        }
        else
        {
            posterAdapter.notifyDataSetChanged();
            showMoviesRecyclerView();
        }
    }

    /**
     * Calls TMDb to populate the RecyclerView with movies fulfilling the given search criteria.
     *
     * If the device is currently offline, shows a Snackbar instead that indicates that the device
     * is offline and provides the user a way to retry.
     *
     * @param movieSortingCriteria
     *     sorting criteria to sort movies by
     */
    private void getMoviesBy(String movieSortingCriteria)
    {
        if (NetworkUtils.isOnline(this))
        {
            // Populate the RecyclerView with movie posters as retrieved from TMDb.
            ConnectableObservable<GetMoviesResponse> getMoviesObservable =
                    new TmdbGateway().getMovies(movieSortingCriteria);
            getMoviesObservable.flatMap(response -> Observable.from(response.getMovies()))
                               .toList()
                               .subscribe(this::populateMoviesWith,
                                          error ->
                                          {
                                              showEmptyMovieView(
                                                      getString(
                                                              R.string.message_movies_error_loading));
                                              Log.e(LOG_TAG, error.getMessage(), error);
                                          });
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
                            moviesRecyclerView,
                            R.string.snackbar_offline_message,
                            Snackbar.LENGTH_INDEFINITE)
                    .setAction(
                            R.string.snackbar_offline_action_retry,
                            view -> getMoviesBy(movieSortingCriteria));
            offlineSnackbar.show();
        }
    }

    /**
     * Calls the MovieProvider to populate the RecyclerView with movies that the user marked as
     * their favorite movies.
     */
    private void getFavoriteMovies()
    {
        Observable.from(queryForFavoriteMovies())
                  .subscribeOn(Schedulers.io())
                  .observeOn(AndroidSchedulers.mainThread())
                  .toList()
                  .subscribe(this::populateMoviesWith,
                             error ->
                             {
                                 showEmptyMovieView(
                                         getString(
                                                 R.string.message_movies_error_loading));
                                 Log.e(LOG_TAG, error.getMessage(), error);
                             });
    }

    /**
     * Saves the user's selected movie display criteria into a SharedPreferences. This helps retain
     * whether the user was viewing Popular movies, Top Rated movies, or their Favorite movies
     * previously in this activity.
     *
     * Note that the method name here avoids the word `get`--this is to avoid misapprehending this
     * method as a Java getter.
     *
     * @param movieDisplayCriteria
     *     enum value describing what list of movies the user was viewing earlier
     */
    private void saveMovieDisplayCriteria(MovieDisplayCriteria movieDisplayCriteria)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putString(KEY_CRITERIA, movieDisplayCriteria.toString()).apply();
    }

    /**
     * Loads the user's selected movie display criteria from a SharedPreferences. This helps retain
     * whether the user was viewing Popular movies, Top Rated movies, or their Favorite movies
     * previously in this activity.
     *
     * Note that the method name here avoids the word `set`--this is to avoid misapprehending this
     * method as a Java setter.
     *
     * @return
     *     user's selected movie display criteria
     */
    private MovieDisplayCriteria loadMovieDisplayCriteria()
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String movieDisplayCriteriaString =
                sharedPreferences.getString(KEY_CRITERIA, null);
        return (movieDisplayCriteriaString != null) ?
               MovieDisplayCriteria.valueOf(movieDisplayCriteriaString) :
               MovieDisplayCriteria.POPULAR;
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
