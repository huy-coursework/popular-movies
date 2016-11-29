package com.huyvuong.udacity.popularmovies.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.huyvuong.udacity.popularmovies.R;
import com.squareup.picasso.Picasso;

/**
 * Fragment containing the detail view of a movie retrieved from TMDb, which shows more detailed
 * metadata about a given movie.
 */
public class MovieDetailActivityFragment
        extends Fragment
{
    // Intent Extra Keys
    public static final String KEY_ORIGINAL_TITLE = "originalTitle";
    public static final String KEY_POSTER_PATH = "posterPath";
    public static final String KEY_PLOT_SYNOPSIS = "plotSynopsis";
    public static final String KEY_RATING = "rating";
    public static final String KEY_RELEASE_DATE = "releaseDate";

    // Used for loading the poster image.
    private static final String MOVIE_POSTER_URL_FORMAT = "http://image.tmdb.org/t/p/w342/%s";

    // Indicates that no average rating for a movie was found.
    private static final double NOT_FOUND = -1.0;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        // If an intent was passed to this activity, get movie data from that intent's extra data
        // and populate the views for this fragment with that data.
        Intent intent = getActivity().getIntent();
        if (intent != null)
        {
            // Populate the original title.
            TextView originalTitleText = (TextView) rootView.findViewById(R.id.text_original_title);
            String title = intent.getStringExtra(KEY_ORIGINAL_TITLE);
            originalTitleText.setText(title);
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null)
            {
                actionBar.setTitle(title);
            }

            // Load the poster image.
            ImageView posterImage = (ImageView) rootView.findViewById(R.id.image_poster);
            Picasso.with(getActivity())
                    .load(String.format(
                            MOVIE_POSTER_URL_FORMAT,
                            intent.getStringExtra(KEY_POSTER_PATH)))
                    .into(posterImage);

            // Populate the average rating.
            TextView ratingText = (TextView) rootView.findViewById(R.id.text_rating);
            double rating = intent.getDoubleExtra(KEY_RATING, NOT_FOUND);
            ratingText.setText((rating > NOT_FOUND) ? "" + rating : "--");

            // Populate the release date.
            TextView releaseDateText = (TextView) rootView.findViewById(R.id.text_release_date);
            releaseDateText.setText(intent.getStringExtra(KEY_RELEASE_DATE));

            // Populate the plot synopsis.
            TextView plotSynopsisText = (TextView) rootView.findViewById(R.id.text_plot_synopsis);
            plotSynopsisText.setText(intent.getStringExtra(KEY_PLOT_SYNOPSIS));
        }

        return rootView;
    }
}
