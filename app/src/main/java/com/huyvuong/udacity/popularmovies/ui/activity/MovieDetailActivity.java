package com.huyvuong.udacity.popularmovies.ui.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.huyvuong.udacity.popularmovies.R;

/**
 * Activity containing the detail view of a movie retrieved from TMDb.
 */
public class MovieDetailActivity
        extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
    }
}
