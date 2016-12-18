package com.huyvuong.udacity.popularmovies.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.huyvuong.udacity.popularmovies.R;
import com.huyvuong.udacity.popularmovies.gateway.TmdbGateway;
import com.huyvuong.udacity.popularmovies.model.Movie;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment containing the master view of the movies retrieved from TMDb, represented as movie
 * poster images that the user can click on.
 */
public class MovieMasterActivityFragment
        extends DialogFragment
{
    private static final String LOG_TAG = MovieMasterActivityFragment.class.getSimpleName();

    private GridView moviesGridView;
    private Snackbar offlineSnackbar;

    public MovieMasterActivityFragment()
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
        moviesGridView = (GridView) rootView.findViewById(R.id.grid_movies);
        moviesGridView.setAdapter(new PosterAdapter(
                getActivity(),
                R.layout.grid_item_poster,
                new ArrayList<Movie>()));

        // By default, on activity creation, show popular movies.
        getMoviesBy(TmdbGateway.MovieSortingCriteria.POPULAR);

        // Return the root view to display for this fragment.
        return rootView;
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
                getMoviesBy(TmdbGateway.MovieSortingCriteria.POPULAR);
                return true;
            case R.id.action_set_criteria_top_rated:
                // Show top rated movies.
                getMoviesBy(TmdbGateway.MovieSortingCriteria.TOP_RATED);
                return true;
        }

        return super.onOptionsItemSelected(item);
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
    private void getMoviesBy(final TmdbGateway.MovieSortingCriteria movieSortingCriteria)
    {
        if (isOnline())
        {
            // Populate the GridView with movie posters as retrieved from TMDb.
            new GetMoviesTask((PosterAdapter) moviesGridView.getAdapter(), new TmdbGateway())
                    .execute(movieSortingCriteria);

            // If the device is no longer offline, then no point showing the Snackbar notifying the
            // user that their device is offline.
            if (offlineSnackbar != null)
            {
                offlineSnackbar.dismiss();
            }
        }
        else
        {
            offlineSnackbar = Snackbar
                    .make(
                            moviesGridView,
                            R.string.snackbar_offline_message,
                            Snackbar.LENGTH_INDEFINITE)
                    .setAction(
                            R.string.snackbar_offline_action_retry,
                            new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    // Retrying should try for the same movie sorting criteria that
                                    // the user originally selected.
                                    getMoviesBy(movieSortingCriteria);
                                }
                            });
            offlineSnackbar.show();
        }
    }

    /**
     * Returns true if the device is connected to the Internet. Returns false otherwise.
     *
     * From: http://stackoverflow.com/a/4009133
     *
     * @return
     *     true if the device is currently online, false otherwise
     */
    private boolean isOnline()
    {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /**
     * Adapter for rendering a set of poster images for a given list of Movie objects, which when
     * clicked on, take the user to a different activity for seeing more detailed information about
     * the movie.
     */
    class PosterAdapter
            extends ArrayAdapter<Movie>
    {
        // Used for loading the poster image.
        private static final String MOVIE_POSTER_URL_FORMAT = "http://image.tmdb.org/t/p/w342/%s";

        private PosterAdapter(Context context, int resource, List<Movie> movies)
        {
            super(context, resource, movies);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent)
        {
            // Setup of convertView derived from: http://stackoverflow.com/a/21833388
            final Context context = getContext();
            LayoutInflater layoutInflater =
                    (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            ImageView posterImageView;
            if (convertView == null)
            {
                convertView = layoutInflater.inflate(R.layout.grid_item_poster, null);
                posterImageView = (ImageView) convertView.findViewById(R.id.image_poster_preview);
                convertView.setTag(posterImageView);
            }
            else
            {
                posterImageView = (ImageView) convertView.getTag();
            }

            // Get the poster path from the corresponding movie and use it to obtain the movie
            // poster image to load into the ImageView to show in the GridView. Set up the grid item
            // so that clicking on it takes the user to the MovieDetailActivity to show more details
            // on the individual movie itself.
            final Movie movie = getItem(position);
            if (movie != null)
            {
                Picasso.with(context)
                        .load(String.format(MOVIE_POSTER_URL_FORMAT, movie.getPosterPath()))
                        .into(posterImageView);
                convertView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent detailIntent = new Intent(getActivity(), MovieDetailActivity.class)
                                .putExtra(
                                        MovieDetailActivityFragment.KEY_ORIGINAL_TITLE,
                                        movie.getOriginalTitle())
                                .putExtra(
                                        MovieDetailActivityFragment.KEY_PLOT_SYNOPSIS,
                                        movie.getPlotSynopsis())
                                .putExtra(
                                        MovieDetailActivityFragment.KEY_POSTER_PATH,
                                        movie.getPosterPath())
                                .putExtra(
                                        MovieDetailActivityFragment.KEY_RATING,
                                        movie.getRating())
                                .putExtra(
                                        MovieDetailActivityFragment.KEY_RELEASE_DATE,
                                        movie.getReleaseDate());
                        startActivity(detailIntent);
                    }
                });
            }

            return convertView;
        }
    }
}