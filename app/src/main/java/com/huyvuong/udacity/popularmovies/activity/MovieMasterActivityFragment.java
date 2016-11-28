package com.huyvuong.udacity.popularmovies.activity;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.huyvuong.udacity.popularmovies.R;
import com.huyvuong.udacity.popularmovies.gateway.TmdbGateway;
import com.huyvuong.udacity.popularmovies.model.Movie;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Fragment containing the master view of the movies retrieved from TMDb, represented as movie
 * poster images that the user can click on.
 */
public class MovieMasterActivityFragment
        extends Fragment
{
    private GridView moviesGridView;

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
        new GetMoviesTask(new TmdbGateway()).execute(TmdbGateway.MovieSortingCriteria.POPULAR);

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
            case R.id.action_popular:
                // Show popular movies.
                new GetMoviesTask(new TmdbGateway())
                        .execute(TmdbGateway.MovieSortingCriteria.POPULAR);
                return true;
            case R.id.action_top_rated:
                // Show top rated movies.
                new GetMoviesTask(new TmdbGateway())
                        .execute(TmdbGateway.MovieSortingCriteria.TOP_RATED);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Asynchronous task that calls TMDb to get movie metadata, and then populates the GridView with
     * the poster images for those movies.
     */
    public class GetMoviesTask
            extends AsyncTask<TmdbGateway.MovieSortingCriteria, Void, List<Movie>>
    {
        private TmdbGateway tmdbGateway;

        GetMoviesTask(TmdbGateway tmdbGateway)
        {
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
                PosterAdapter posterAdapter = (PosterAdapter) moviesGridView.getAdapter();
                posterAdapter.clear();
                for (Movie movie : result)
                {
                    posterAdapter.add(movie);
                }
            }
        }
    }

    /**
     * Adapter for rendering a set of poster images for a given list of Movie objects, which when
     * clicked on, show a Toast message containing the original title for that movie.
     */
    private class PosterAdapter
            extends ArrayAdapter<Movie>
    {
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
                posterImageView = (ImageView) convertView.findViewById(R.id.image_poster);
                convertView.setTag(posterImageView);
            }
            else
            {
                posterImageView = (ImageView) convertView.getTag();
            }

            // Get the poster path from the corresponding movie and use it to obtain the movie
            // poster image to load into the ImageView to show in the GridView. Set up the grid item
            // so that clicking on it also shows a Toast message with the original title in it.
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
                        Toast.makeText(context, movie.getOriginalTitle(), Toast.LENGTH_SHORT)
                                .show();
                    }
                });
            }

            return convertView;
        }
    }
}