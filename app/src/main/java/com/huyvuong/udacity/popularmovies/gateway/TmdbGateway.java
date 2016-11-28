package com.huyvuong.udacity.popularmovies.gateway;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.huyvuong.udacity.popularmovies.BuildConfig;
import com.huyvuong.udacity.popularmovies.model.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Entry point for all calls against The Movie Database (TMDb).
 *
 * This product uses the TMDb API but is not endorsed or certified by TMDb.
 */
public class TmdbGateway
{
    // TODO Need to add to the Settings somewhere in a list of attributions / credits:
    //      "This product uses the TMDb API but is not endorsed or certified by TMDb."
    private static final String LOG_TAG = TmdbGateway.class.getSimpleName();

    private HttpGateway httpGateway;

    public TmdbGateway()
    {
        this.httpGateway = new HttpGateway();
    }

    /**
     * Returns the list of movies from TMDb that best match the given movie sorting criteria.
     *
     * @param movieSortingCriteria
     *     sorting criteria to sort movies by
     * @return
     *     list of movies returned by TMDb
     */
    @NonNull
    public List<Movie> getMovies(MovieSortingCriteria movieSortingCriteria)
    {
        // Create a URL object from the given movie sorting criteria.
        Uri moviesUri = buildGetMoviesUri(movieSortingCriteria);
        URL moviesUrl;
        try
        {
            moviesUrl = new URL(moviesUri.toString());
        }
        catch (MalformedURLException e)
        {
            // If the URL is malformed, return an empty collection.
            Log.e(
                    LOG_TAG,
                    String.format("The URL was malformed for the URI: %s", moviesUri.toString()),
                    e);
            return Collections.emptyList();
        }

        // Call TMDb to get a list of movies for the URL constructed from the given movie
        // sorting criteria.
        String response = httpGateway.makeGetRequestTo(moviesUrl);
        if (response.isEmpty())
        {
            // If the call resulted in an error, return an empty list of movies.
            return Collections.emptyList();
        }

        try
        {
            // Deserialize each JSON object representation of a movie into a Movie object and add it
            // to the list of movies.
            JSONObject responseObject = new JSONObject(response);
            JSONArray results = responseObject.getJSONArray("results");
            List<Movie> movies = new ArrayList<>();
            for (int i = 0; i < results.length(); i++)
            {
                JSONObject movieObject = results.getJSONObject(i);
                Movie movie = buildMovieFromJsonObject(movieObject);
                movies.add(movie);
            }
            return movies;
        }
        catch (JSONException e)
        {
            Log.e(LOG_TAG, String.format("Error parsing the returned JSON: %s", response), e);
        }

        return Collections.emptyList();
    }

    /**
     * Builds a URI object containing a URL to call TMDb with for the given movie sorting criteria.
     *
     * @param movieSortingCriteria
     *     movie sorting criteria to use in constructing the URL to call TMDb with
     * @return
     *     URI object with a URL to call TMDb with
     */
    @NonNull
    private Uri buildGetMoviesUri(MovieSortingCriteria movieSortingCriteria)
    {
        return new Uri.Builder()
                .scheme("http")
                .authority("api.themoviedb.org")
                .appendPath("3")
                .appendPath("movie")
                .appendPath(movieSortingCriteria.getUrlPath())
                .appendQueryParameter("api_key", BuildConfig.TMDB_API_KEY)
                .build();
    }

    /**
     * Builds an equivalent Movie object for the given JSON object representation of a movie.
     *
     * @param movieObject
     *     JSON object representing a movie
     * @return
     *     Movie object equivalent to the given JSON object
     * @throws JSONException
     *     if the given JSON object is missing necessary fields or uses the incorrect type
     */
    @NonNull
    private Movie buildMovieFromJsonObject(JSONObject movieObject) throws JSONException
    {
        return new Movie.Builder()
                .withId(movieObject.getInt("id"))
                .withOriginalTitle(movieObject.getString("original_title"))
                .withPlotSynopsis(movieObject.getString("overview"))
                .withPosterPath(movieObject.getString("poster_path"))
                .withRating(movieObject.getDouble("vote_average"))
                .withReleaseDate(movieObject.getString("release_date"))
                .build();
    }

    /**
     * Criteria to use in determining what kind of movies to look up. Each value represents a
     * different metric to measure a movie by and find the 'highest' of.
     */
    public enum MovieSortingCriteria
    {
        POPULAR("popular"),
        TOP_RATED("top_rated");

        private String urlPath;

        MovieSortingCriteria(String urlPath)
        {
            this.urlPath = urlPath;
        }

        /**
         * Returns the TMDb URL path corresponding to this criterion.
         *
         * @return
         *     URL path name to use for the given criteria
         */
        public String getUrlPath()
        {
            return this.urlPath;
        }
    }
}
