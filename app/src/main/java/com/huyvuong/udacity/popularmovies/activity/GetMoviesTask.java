package com.huyvuong.udacity.popularmovies.activity;

import android.os.AsyncTask;

import com.huyvuong.udacity.popularmovies.gateway.TmdbGateway;
import com.huyvuong.udacity.popularmovies.model.Movie;
import com.huyvuong.udacity.popularmovies.activity.MovieMasterActivityFragment.PosterAdapter;

import java.util.Collections;
import java.util.List;

/**
 * Asynchronous task that calls TMDb to get movie metadata, and then populates the GridView with
 * the poster images for those movies.
 */
public class GetMoviesTask
        extends AsyncTask<TmdbGateway.MovieSortingCriteria, Void, List<Movie>>
{
    private PosterAdapter posterAdapter;
    private TmdbGateway tmdbGateway;

    public GetMoviesTask(PosterAdapter posterAdapter, TmdbGateway tmdbGateway)
    {
        this.posterAdapter = posterAdapter;
        this.tmdbGateway = tmdbGateway;
    }

    @Override
    protected List<Movie> doInBackground(TmdbGateway.MovieSortingCriteria... params)
    {
        // Return the list of movies from TMDb for the given sorting criteria, provided as
        // the first argument of params. If no sorting criteria is given, return an empty list.
        return (params.length > 0) ?
                tmdbGateway.getMovies(params[0]) :
                Collections.<Movie>emptyList();
    }

    @Override
    protected void onPostExecute(List<Movie> result)
    {
        if (result != null)
        {
            // Replace the existing contents of the adapter with the updated results.
            posterAdapter.clear();
            for (Movie movie : result)
            {
                posterAdapter.add(movie);
            }
        }
    }
}