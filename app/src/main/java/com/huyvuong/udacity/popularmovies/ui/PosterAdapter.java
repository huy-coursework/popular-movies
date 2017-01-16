package com.huyvuong.udacity.popularmovies.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.huyvuong.udacity.popularmovies.R;
import com.huyvuong.udacity.popularmovies.model.business.Movie;
import com.huyvuong.udacity.popularmovies.ui.activity.MovieDetailActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Adapter for rendering a set of poster images for a given list of Movie objects, which when
 * clicked on, take the user to a different activity for seeing more detailed information about
 * the movie.
 *
 * Implemented with reference to: https://guides.codepath.com/android/using-the-recyclerview
 */
public class PosterAdapter
        extends RecyclerView.Adapter<PosterAdapter.ViewHolder>
{
    // Used for loading the poster image.
    private static final String MOVIE_POSTER_URL_FORMAT = "http://image.tmdb.org/t/p/w342/%s";

    private Context context;
    private List<Movie> movies;

    public PosterAdapter(Context context, List<Movie> movies)
    {
        this.context = context;
        this.movies = movies;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        // Inflate the grid item that displays the poster.
        Context parentContext = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(parentContext);
        View posterView = layoutInflater.inflate(R.layout.grid_item_poster, parent, false);

        // Create a ViewHolder from the inflated view.
        return new ViewHolder(posterView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {
        // Get the movie corresponding to the given position.
        Movie movie = movies.get(position);

        // Configure the viewholder.
        Picasso.with(context)
               .load(String.format(MOVIE_POSTER_URL_FORMAT, movie.getPosterPath()))
               .into(holder.posterImageView);
    }

    @Override
    public int getItemCount()
    {
        return movies.size();
    }

    /**
     * Viewholder for using the Viewholder Pattern with the RecyclerView.
     */
    public class ViewHolder extends RecyclerView.ViewHolder
    {
        @BindView(R.id.image_poster_preview)
        ImageView posterImageView;

        public ViewHolder(View itemView)
        {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(
                    view ->
                    {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION)
                        {
                            Movie movieToShowDetailsFor = movies.get(position);
                            context.startActivity(
                                    new Intent(context, MovieDetailActivity.class)
                                            .putExtra(MovieDetailActivity.KEY_MOVIE,
                                                      movieToShowDetailsFor));
                        }
                    });
        }
    }
}