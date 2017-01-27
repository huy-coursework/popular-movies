package com.huyvuong.udacity.popularmovies.model.transport;

import com.google.gson.annotations.SerializedName;

/**
 * Response object corresponding to the JSON response returned by The Movie Database (TMDb) API when
 * it returns the details for a given movie.
 */
public class GetMovieDetailsResponse
{
    @SerializedName("id")
    private Integer id;

    @SerializedName("backdrop_path")
    private String backdropPath;

    private GetMovieDetailsResponse(Integer id, String backdropPath)
    {
        this.id = id;
        this.backdropPath = backdropPath;
    }

    /**
     * Returns this movie's movie ID as assigned by TMDb.
     *
     * Corresponds to the {@code id} field from a TMDb response.
     *
     * @return
     *     movie ID assigned by TMDb
     */
    public Integer getId()
    {
        return id;
    }

    /**
     * Returns the path for this movie's backdrop image, which is a horizontally-oriented
     * promotional image for the movie.
     *
     * Use the backdrop image with an image size to compute the URL for the backdrop image
     * as follows:
     *     http://image.tmdb.org/t/p/w185/{backdrop_path}
     *
     * For example, for size {@code w185} and a backdrop path of
     * {@code "/djgM2d3e42p9GFQObg6lwK2SVw2.jpg"}, the link would look like:
     *     http://image.tmdb.org/t/p/w185//djgM2d3e42p9GFQObg6lwK2SVw2.jpg
     *
     * Corresponds to the {@code backdrop_path} field from a TMDb response.
     *
     * @return
     *     identifier for use in the URL path to this movie's backdrop image
     */
    public String getBackdropPath()
    {
        return backdropPath;
    }

    /**
     * Builder for use in constructing a new GetMovieDetailsResponse object.
     */
    public static class Builder
    {
        private Integer id;
        private String backdropPath;

        /**
         * Sets the movie ID.
         *
         * @param id
         *     movie ID assigned by TMDb
         * @return
         *     reference to this {@link Builder}
         */
        public Builder withId(Integer id)
        {
            this.id = id;
            return this;
        }

        /**
         * Sets the movie's backdrop path.
         *
         * @param backdropPath
         *     identifier for use in the URL path to this movie's backdrop image
         * @return
         *     reference to this {@link Builder}
         */
        public Builder withBackdropPath(String backdropPath)
        {
            this.backdropPath = backdropPath;
            return this;
        }

        /**
         * Constructs a {@link GetMovieDetailsResponse} object with the given values for its fields.
         *
         * @return
         *     newly-constructed {@link GetMovieDetailsResponse} object with the given values for
         *     its fields
         */
        public GetMovieDetailsResponse build()
        {
            return new GetMovieDetailsResponse(id, backdropPath);
        }
    }
}
