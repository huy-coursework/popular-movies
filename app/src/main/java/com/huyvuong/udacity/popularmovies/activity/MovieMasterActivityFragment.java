package com.huyvuong.udacity.popularmovies.activity;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.huyvuong.udacity.popularmovies.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieMasterActivityFragment extends Fragment
{

    public MovieMasterActivityFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_movie_master, container, false);
    }
}
