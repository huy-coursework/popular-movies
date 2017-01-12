package com.huyvuong.udacity.popularmovies.gateway.response;

import com.google.gson.annotations.SerializedName;
import com.huyvuong.udacity.popularmovies.model.Review;

import java.util.List;

/**
 * Response object corresponding to the JSON response returned by The Movie Database (TMDb) API when
 * it returns a list of reviews.
 */
public class GetReviewsResponse
{
    @SerializedName("id")
    private Integer movieId;

    @SerializedName("page")
    private Long pageNumber;

    @SerializedName("results")
    private List<Review> reviews;

    @SerializedName("total_results")
    private Long totalResultCount;

    @SerializedName("total_pages")
    private Long totalPageCount;

    private GetReviewsResponse(Integer movieId,
                               Long pageNumber,
                               List<Review> reviews,
                               Long totalResultCount,
                               Long totalPageCount)
    {
        this.movieId = movieId;
        this.pageNumber = pageNumber;
        this.reviews = reviews;
        this.totalResultCount = totalResultCount;
        this.totalPageCount = totalPageCount;
    }

    /**
     * Returns this response's movie ID that the reviews correspond to.
     *
     * Corresponds to the {@code id} field from a TMDb response.
     *
     * @return
     *     movie ID that reviews correspond to
     */
    public Integer getMovieId()
    {
        return movieId;
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
     * Returns this current page's list of reviews.
     *
     * Corresponds to the {@code results} field from a TMDb response.
     *
     * @return
     *     list of reviews for the requested movie
     */
    public List<Review> getReviews()
    {
        return reviews;
    }

    /**
     * Returns the total amount of reviews that exist across all pages.
     *
     * Corresponds to the {@code total_results} field from a TMDb response.
     *
     * @return
     *     total number of reviews that exist across all pages
     */
    public Long getTotalResultCount()
    {
        return totalResultCount;
    }

    /**
     * Returns the total number of pages that exist for the entire list of reviews.
     *
     * Corresponds to the {@code total_pages} field from a TMDb response.
     *
     * @return
     *     total number of pages that exist for the entire list of reviews
     */
    public Long getTotalPageCount()
    {
        return totalPageCount;
    }

    /**
     * Builder for use in constructing a new GetReviewsResponse object.
     */
    public static class Builder
    {
        private Integer movieId;
        private Long pageNumber;
        private List<Review> reviews;
        private Long totalResultCount;
        private Long totalPageCount;

        /**
         * Sets this response's movie ID that the reviews correspond to.
         *
         * @param movieId
         *     movie ID that reviews correspond to
         */
        public Builder withMovieId(Integer movieId)
        {
            this.movieId = movieId;
            return this;
        }

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
         * Sets the current page's list of reviews.
         *
         * @param reviews
         *     list of reviews for the current page
         * @return
         *     reference to this {@link Builder}
         */
        public Builder withReviews(List<Review> reviews)
        {
            this.reviews = reviews;
            return this;
        }

        /**
         * Sets the total amount of reviews that exist across all pages.
         *
         * @param totalResultCount
         *     total number of reviews that exist across all pages
         * @return
         *     reference to this {@link Builder}
         */
        public Builder withTotalResultCount(Long totalResultCount)
        {
            this.totalResultCount = totalResultCount;
            return this;
        }

        /**
         * Sets the total number of pages that exist for the entire list of reviews.
         *
         * @param totalPageCount
         *     total number of pages that exist for the entire list of reviews
         * @return
         *     reference to this {@link Builder}
         */
        public Builder withTotalPageCount(Long totalPageCount)
        {
            this.totalPageCount = totalPageCount;
            return this;
        }

        /**
         * Constructs a {@link GetReviewsResponse} object with the given values for its fields.
         *
         * @return
         *     newly-constructed {@link GetReviewsResponse} object with the given values for
         *     its fields
         */
        public GetReviewsResponse build()
        {
            return new GetReviewsResponse(
                    movieId,
                    pageNumber,
                    reviews,
                    totalResultCount,
                    totalPageCount);
        }
    }
}
