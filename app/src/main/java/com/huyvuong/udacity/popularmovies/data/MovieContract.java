package com.huyvuong.udacity.popularmovies.data;

import android.provider.BaseColumns;

/**
 * Defines table and column names for the local movie database.
 */
public class MovieContract
{
    public static final class MovieEntry implements BaseColumns
    {
        public static final String TABLE_NAME = "movie";

        public static final String COLUMN_MOVIE_ID = "id";
        public static final String COLUMN_ORIGINAL_TITLE = "original_title";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_PLOT_SYNOPSIS = "plot_synopsis";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_RELEASE_DATE = "release_date";
    }
}
