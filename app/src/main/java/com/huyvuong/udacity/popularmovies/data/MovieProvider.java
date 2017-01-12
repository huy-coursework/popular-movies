package com.huyvuong.udacity.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

/**
 * Content provider for interacting and managing movie data cached from The Movie Database (TMDb).
 */
public class MovieProvider extends ContentProvider
{
    // URIs supported by this content provider.
    private static final int CODE_MOVIE = 100;
    private static final int CODE_MOVIE_WITH_TMDB_ID = 101;

    private static final UriMatcher uriMatcher = buildUriMatcher();

    private MovieDbHelper movieDbHelper;

    @Override
    public boolean onCreate()
    {
        // Set up the DB helper.
        movieDbHelper = new MovieDbHelper(getContext());

        // Return true to indicate a successful setup.
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri,
                        String[] projection,
                        String selection,
                        String[] selectionArgs,
                        String sortOrder)
    {
        SQLiteDatabase sqLiteDatabase;
        Cursor queryResults;

        switch (uriMatcher.match(uri))
        {
            case CODE_MOVIE:
                sqLiteDatabase = movieDbHelper.getReadableDatabase();
                queryResults = sqLiteDatabase.query(MovieContract.MovieEntry.TABLE_NAME,
                                                    projection,
                                                    selection,
                                                    selectionArgs,
                                                    null,
                                                    null,
                                                    sortOrder);
                break;
            case CODE_MOVIE_WITH_TMDB_ID:
                sqLiteDatabase = movieDbHelper.getReadableDatabase();
                String tmdbId = uri.getPathSegments().get(1);
                queryResults = sqLiteDatabase.query(MovieContract.MovieEntry.TABLE_NAME,
                                                    projection,
                                                    MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                                                    new String[] {tmdbId},
                                                    null,
                                                    null,
                                                    sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }

        // Register this cursor to watch this URI for changes.
        if (getContext() != null && getContext().getContentResolver() != null)
        {
            queryResults.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return queryResults;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values)
    {
        switch (uriMatcher.match(uri))
        {
            case CODE_MOVIE:
                // Insert the provided values into the database.
                SQLiteDatabase sqLiteDatabase = movieDbHelper.getWritableDatabase();
                long rowId =
                        sqLiteDatabase.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
                if (rowId < 0)
                {
                    throw new SQLException("Failed to insert row at URI: " + uri);
                }

                // Notify anyone listening on the resulting URI of changes to it.
                int tmdbId = values.getAsInteger(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
                Uri insertedUri = MovieContract.MovieEntry.buildMovieUriWithTmdbId(tmdbId);
                if (getContext() != null && getContext().getContentResolver() != null)
                {
                    getContext().getContentResolver().notifyChange(insertedUri, null);
                }
                return insertedUri;
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
    }


    @Override
    public int update(@NonNull Uri uri,
                      ContentValues values,
                      String selection,
                      String[] selectionArgs)
    {
        switch (uriMatcher.match(uri))
        {
            case CODE_MOVIE_WITH_TMDB_ID:
                // Update the row with a matching TMDb-assigned ID as the one provided in the URI.
                String tmdbId = uri.getPathSegments().get(1);
                SQLiteDatabase sqLiteDatabase = movieDbHelper.getWritableDatabase();
                int rowsUpdated = sqLiteDatabase.update(
                        MovieContract.MovieEntry.TABLE_NAME,
                        values,
                        MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                        new String[] {tmdbId});

                // Notify anyone listening on the given URI of changes to it.
                if (getContext() != null && getContext().getContentResolver() != null)
                {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsUpdated;
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs)
    {
        switch (uriMatcher.match(uri))
        {
            case CODE_MOVIE_WITH_TMDB_ID:
                // Delete the row with a matching TMDb-assigned ID as the one provided in the URI.
                String tmdbId = uri.getPathSegments().get(1);
                SQLiteDatabase sqLiteDatabase = movieDbHelper.getWritableDatabase();
                int rowsDeleted = sqLiteDatabase.delete(
                        MovieContract.MovieEntry.TABLE_NAME,
                        MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?",
                        new String[] {tmdbId});

                // Notify anyone listening on the given URI of changes to it.
                if (getContext() != null && getContext().getContentResolver() != null)
                {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
    }

    @Override
    public String getType(@NonNull Uri uri)
    {
        switch (uriMatcher.match(uri))
        {
            case CODE_MOVIE:
                return "vnd.android.cursor.dir/vnd.com.huyvuong.udacity.popularmovies.movies";
            case CODE_MOVIE_WITH_TMDB_ID:
                return "vnd.android.cursor.item/vnd.com.huyvuong.udacity.popularmovies.movies";
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
    }

    private static UriMatcher buildUriMatcher()
    {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY,
                          MovieContract.PATH_MOVIES,
                          CODE_MOVIE);
        uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY,
                          MovieContract.PATH_MOVIES + "/#",
                          CODE_MOVIE_WITH_TMDB_ID);
        return uriMatcher;
    }
}
