package com.huyvuong.udacity.popularmovies.ui.activity;

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
import com.huyvuong.udacity.popularmovies.model.Movie;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Fragment containing the detail view of a movie retrieved from TMDb, which shows more detailed
 * metadata about a given movie.
 */
public class MovieDetailFragment
        extends Fragment
{
    // Intent Extra Keys
    public static final String KEY_MOVIE = "movie";

    // Used for loading the poster image.
    private static final String MOVIE_POSTER_URL_FORMAT = "http://image.tmdb.org/t/p/w342/%s";

    // Indicates that no average rating for a movie was found.
    private static final double NOT_FOUND = -1.0;

    @BindView(R.id.text_original_title)
    TextView originalTitleText;

    @BindView(R.id.image_poster)
    ImageView posterImage;

    @BindView(R.id.text_rating)
    TextView ratingText;

    @BindView(R.id.text_release_date)
    TextView releaseDateText;

    @BindView(R.id.text_plot_synopsis)
    TextView plotSynopsisText;

    private Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        // If an intent was passed to this activity, get movie data from that intent's extra data
        // and populate the views for this fragment with that data.
        Intent intent = getActivity().getIntent();
        if (intent != null &&
                intent.hasExtra(KEY_MOVIE) &&
                intent.getParcelableExtra(KEY_MOVIE) instanceof Movie)
        {
            // Obtain the Movie object from the intent.
            Movie movie = intent.getParcelableExtra(KEY_MOVIE);

            // Populate the original title.
            String title = movie.getOriginalTitle();
            originalTitleText.setText(title);
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null)
            {
                actionBar.setTitle(title);
            }

            // Load the poster image.
            Picasso.with(getActivity())
                    .load(String.format(
                            MOVIE_POSTER_URL_FORMAT,
                            movie.getPosterPath()))
                    .into(posterImage);

            // Populate the average rating.
            double rating = movie.getRating();
            ratingText.setText((rating > NOT_FOUND) ? String.valueOf(rating) : "--");

            // Populate the release date.
            releaseDateText.setText(movie.getReleaseDate());

            // Populate the plot synopsis.
            plotSynopsisText.setText(movie.getPlotSynopsis());
        }

        return rootView;
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        unbinder.unbind();
    }
}
