package com.huyvuong.udacity.popularmovies.model.transport;

import com.google.gson.annotations.SerializedName;
import com.huyvuong.udacity.popularmovies.model.business.Movie;

import java.util.List;

/**
 * Response object corresponding to the JSON response returned by The Movie Database (TMDb) API when
 * it returns a list of movies.
 */
public class GetMoviesResponse
{
    @SerializedName("page")
    private Long pageNumber;

    @SerializedName("results")
    private List<Movie> movies;

    @SerializedName("total_results")
    private Long totalResultCount;

    @SerializedName("total_pages")
    private Long totalPageCount;

    private GetMoviesResponse(Long pageNumber,
                              List<Movie> movies,
                              Long totalResultCount,
                              Long totalPageCount)
    {
        this.pageNumber = pageNumber;
        this.movies = movies;
        this.totalResultCount = totalResultCount;
        this.totalPageCount = totalPageCount;
    }

    /**
     * Returns this response's current page number.
     *
     * Corresponds to the {@code page} field from a TMDb response.
     *
     * @return
     *     current page number
     */
    public Long getPageNumber()
    {
        return pageNumber;
    }

    /**
     * Returns this current page's list of movies.
     *
     * Corresponds to the {@code results} field from a TMDb response.
     *
     * @return
     *     list of movies for the current page
     */
    public List<Movie> getMovies()
    {
        return movies;
    }

    /**
     * Returns the total amount of movies that exist across all pages.
     *
     * Corresponds to the {@code total_results} field from a TMDb response.
     *
     * @return
     *     total number of movies that exist across all pages
     */
    public Long getTotalResultCount()
    {
        return totalResultCount;
    }

    /**
     * Returns the total number of pages that exist for the entire list of movies.
     *
     * Corresponds to the {@code total_pages} field from a TMDb response.
     *
     * @return
     *     total number of pages that exist for the entire list of movies
     */
    public Long getTotalPageCount()
    {
        return totalPageCount;
    }

    /**
     * Builder for use in constructing a new GetMoviesResponse object.
     */
    public static class Builder
    {
        private Long pageNumber;
        private List<Movie> movies;
        private Long totalResultCount;
        private Long totalPageCount;

        /**
         * Sets the response's current page number.
         *
         * @param pageNumber
         *     current page number
         * @return
         *     reference to this {@link Builder}
         */
        public Builder withPageNumber(Long pageNumber)
        {
            this.pageNumber = pageNumber;
            return this;
        }

        /**
         * Sets the current page's list of movies.
         *
         * @param movies
         *     list of movies for the current page
         * @return
         *     reference to this {@link Builder}
         */
        public Builder withMovies(List<Movie> movies)
        {
            this.movies = movies;
            return this;
        }

        /**
         * Sets the total amount of movies that exist across all pages.
         *
         * @param totalResultCount
         *     total number of movies that exist across all pages
         * @return
         *     reference to this {@link Builder}
         */
        public Builder withTotalResultCount(Long totalResultCount)
        {
            this.totalResultCount = totalResultCount;
            return this;
        }

        /**
         * Sets the total number of pages that exist for the entire list of movies.
         *
         * @param totalPageCount
         *     total number of pages that exist for the entire list of movies
         * @return
         *     reference to this {@link Builder}
         */
        public Builder withTotalPageCount(Long totalPageCount)
        {
            this.totalPageCount = totalPageCount;
            return this;
        }

        /**
         * Constructs a {@link GetMoviesResponse} object with the given values for its fields.
         *
         * @return
         *     newly-constructed {@link GetMoviesResponse} object with the given values for
         *     its fields
         */
        public GetMoviesResponse build()
        {
            return new GetMoviesResponse(pageNumber, movies, totalResultCount, totalPageCount);
        }
    }
}
