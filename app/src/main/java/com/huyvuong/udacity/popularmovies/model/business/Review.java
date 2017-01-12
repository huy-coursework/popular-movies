package com.huyvuong.udacity.popularmovies.model.business;

import com.google.gson.annotations.SerializedName;

/**
 * Review record based on the fields returned by The Movie Database (TMDb).
 */
public class Review
{
    @SerializedName("id")
    private String id;

    @SerializedName("author")
    private String author;

    @SerializedName("content")
    private String content;

    private Review(String id, String author, String content)
    {
        this.id = id;
        this.author = author;
        this.content = content;
    }

    /**
     * Returns this review's review ID assigned by TMDb.
     *
     * Corresponds to the {@code id} field from a TMDb response.
     *
     * @return
     *     review ID assigned by TMDb
     */
    public String getId()
    {
        return id;
    }

    /**
     * Returns the name of the author who wrote the review.
     *
     * Corresponds to the {@code author} field from a TMDb response.
     *
     * @return
     *     name of the author who wrote the review
     */
    public String getAuthor()
    {
        return author;
    }

    /**
     * Returns the content of the review written by the author.
     *
     * Corresponds to the {@code content} field from a TMDb response.
     *
     * @return
     *     content of the review written by the author
     */
    public String getContent()
    {
        return content;
    }

    /**
     * Builder for use in constructing a new Review object.
     */
    public static class Builder
    {
        private String id;
        private String author;
        private String content;

        /**
         * Sets the review ID.
         *
         * @param id
         *     review ID assigned by TMDb
         * @return
         *     reference to this {@link Builder}
         */
        public Builder withId(String id)
        {
            this.id = id;
            return this;
        }

        /**
         * Sets the name of the author who wrote the review.
         *
         * @param author
         *     name of the author who wrote the review
         * @return
         *     reference to this {@link Builder}
         */
        public Builder withAuthor(String author)
        {
            this.author = author;
            return this;
        }

        /**
         * Sets the content of the review written by the author.
         *
         * @param content
         *     content of the review written by the author
         * @return
         *     reference to this {@link Builder}
         */
        public Builder withContent(String content)
        {
            this.content = content;
            return this;
        }

        /**
         * Constructs a {@link Review} object with the given values for its fields.
         *
         * @return
         *     newly-constructed {@link Review} object with the given values for its fields
         */
        public Review build()
        {
            return new Review(id, author, content);
        }
    }
}
