package com.huyvuong.udacity.popularmovies.gateway.response;

import com.google.gson.annotations.SerializedName;
import com.huyvuong.udacity.popularmovies.model.Video;

import java.util.List;

/**
 * Response object corresponding to the JSON response returned by The Movie Database (TMDb) API when
 * it returns a list of videos.
 */
public class GetVideosResponse
{
    @SerializedName("id")
    private Integer movieId;

    @SerializedName("results")
    private List<Video> videos;

    private GetVideosResponse(Integer movieId, List<Video> videos)
    {
        this.movieId = movieId;
        this.videos = videos;
    }

    /**
     * Returns this response's movie ID that the videos correspond to.
     *
     * Corresponds to the {@code id} field from a TMDb response.
     *
     * @return
     *     movie ID that videos correspond to
     */
    public Integer getMovieId()
    {
        return movieId;
    }

    /**
     * Returns the list of videos.
     *
     * Corresponds to the {@code results} field from a TMDb response.
     *
     * @return
     *     list of videos for the requested movie
     */
    public List<Video> getVideos()
    {
        return videos;
    }

    /**
     * Builder for use in constructing a new GetVideosResponse object.
     */
    public static class Builder
    {
        private Integer movieId;
        private List<Video> videos;

        /**
         * Sets this response's movie ID that the videos correspond to.
         *
         * @param movieId
         *     movie ID that videos correspond to
         */
        public Builder withMovieId(Integer movieId)
        {
            this.movieId = movieId;
            return this;
        }

        /**
         * Sets the current page's list of videos.
         *
         * @param videos
         *     list of videos for the current page
         * @return
         *     reference to this {@link Builder}
         */
        public Builder withVideos(List<Video> videos)
        {
            this.videos = videos;
            return this;
        }

        /**
         * Constructs a {@link GetVideosResponse} object with the given values for its fields.
         *
         * @return
         *     newly-constructed {@link GetVideosResponse} object with the given values for
         *     its fields
         */
        public GetVideosResponse build()
        {
            return new GetVideosResponse(movieId, videos);
        }
    }
}
