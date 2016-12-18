package com.huyvuong.udacity.popularmovies.ui.activity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.huyvuong.udacity.popularmovies.R;
import com.huyvuong.udacity.popularmovies.gateway.TmdbGateway;
import com.huyvuong.udacity.popularmovies.model.Movie;
import com.huyvuong.udacity.popularmovies.ui.GetMoviesTask;
import com.huyvuong.udacity.popularmovies.ui.PosterAdapter;

import java.util.ArrayList;

/**
 * Fragment containing the master view of the movies retrieved from TMDb, represented as movie
 * poster images that the user can click on.
 */
public class MovieMasterFragment
        extends DialogFragment
{
    private static final String LOG_TAG = MovieMasterFragment.class.getSimpleName();

    private GridView moviesGridView;
    private Snackbar offlineSnackbar;

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
}