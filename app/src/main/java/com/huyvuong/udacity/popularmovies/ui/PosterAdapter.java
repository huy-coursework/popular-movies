package com.huyvuong.udacity.popularmovies.ui;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.huyvuong.udacity.popularmovies.R;
import com.huyvuong.udacity.popularmovies.model.business.Movie;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Adapter for rendering a set of poster images for a given list of Movie objects, which when
 * clicked on, take the user to a different activity for seeing more detailed information about
 * the movie.
 */
public class PosterAdapter
        extends ArrayAdapter<Movie>
{
    // Used for loading the poster image.
    private static final String MOVIE_POSTER_URL_FORMAT = "http://image.tmdb.org/t/p/w342/%s";

    public PosterAdapter(Context context, int resource, List<Movie> movies)
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
        }

        return convertView;
    }
}