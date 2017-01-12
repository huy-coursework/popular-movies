package com.huyvuong.udacity.popularmovies.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines table and column names for the local movie database.
 */
public class MovieContract
{
    /**
     * Identifier for use in constructing the URI for the {@link MovieProvider}.
     */
    public static final String CONTENT_AUTHORITY = "com.huyvuong.udacity.popularmovies";

    /**
     * Base URI for use in creating URIs to make requests against the {@link MovieProvider} with.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * URI path for requesting changes against movies.
     */
    public static final String PATH_MOVIES = "movies";

    public static final class MovieEntry implements BaseColumns
    {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                                                              .appendPath(PATH_MOVIES)
                                                              .build();

        public static final String TABLE_NAME = "movie";

        public static final String COLUMN_MOVIE_ID = "id";
        public static final String COLUMN_ORIGINAL_TITLE = "original_title";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_PLOT_SYNOPSIS = "plot_synopsis";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_RELEASE_DATE = "release_date";

        /**
         * Builds a URI referring to a movie with the given movie ID from TMDb.
         *
         * @param tmdbId
         *     movie ID corresponding to the ID assigned by TMDb
         * @return
         *     URI referring to a movie with the given movie ID from TMDb
         */
        public static Uri buildMovieUriWithTmdbId(int tmdbId)
        {
            return CONTENT_URI.buildUpon()
                              .appendPath(String.valueOf(tmdbId))
                              .build();
        }
    }
}
