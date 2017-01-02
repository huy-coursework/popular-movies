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

import com.annimon.stream.Stream;
import com.huyvuong.udacity.popularmovies.R;
import com.huyvuong.udacity.popularmovies.gateway.TmdbGateway;
import com.huyvuong.udacity.popularmovies.gateway.response.GetMoviesResponse;
import com.huyvuong.udacity.popularmovies.ui.PosterAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.observables.ConnectableObservable;

/**
 * Fragment containing the master view of the movies retrieved from TMDb, represented as movie
 * poster images that the user can click on.
 */
public class MovieMasterFragment
        extends DialogFragment
{
    @BindView(R.id.grid_movies)
    GridView moviesGridView;

    private PosterAdapter posterAdapter;
    private Snackbar offlineSnackbar;
    private Unbinder unbinder;

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

        // Populate the grid view.
        posterAdapter = new PosterAdapter(
                getActivity(),
                R.layout.grid_item_poster,
                new ArrayList<>());
        moviesGridView.setAdapter(posterAdapter);

        // By default, on activity creation, show popular movies.
        getMoviesBy(TmdbGateway.MovieSortingCriteria.POPULAR);

        // Return the root view to display for this fragment.
        return rootView;
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
    private void getMoviesBy(String movieSortingCriteria)
    {
        if (isOnline())
        {
            // Populate the GridView with movie posters as retrieved from TMDb.
            ConnectableObservable<GetMoviesResponse> getMoviesObservable =
                    new TmdbGateway().getMovies(movieSortingCriteria);
            getMoviesObservable.subscribe(
                    response ->
                    {
                        posterAdapter.clear();
                        Stream.of(response.getMovies())
                                .forEach(movie -> posterAdapter.add(movie));
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