package com.huyvuong.udacity.popularmovies.model.business;

import com.google.gson.annotations.SerializedName;

/**
 * Video metadata record based on the fields returned by The Movie Database (TMDb).
 */
public class Video
{
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("key")
    private String key;

    @SerializedName("type")
    private String type;

    private Video(String id, String name, String key, String type)
    {
        this.id = id;
        this.name = name;
        this.key = key;
        this.type = type;
    }

    /**
     * Returns this video's video ID assigned by TMDb.
     *
     * Corresponds to the {@code id} field from a TMDb response.
     *
     * @return
     *     video ID assigned by TMDb
     */
    public String getId()
    {
        return id;
    }

    /**
     * Returns the name of the video.
     *
     * Corresponds to the {@code name} field from a TMDb response.
     *
     * @return
     *     name of the video
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns the YouTube video ID for this video.
     *
     * Use the YouTube video ID to compute the YouTube video URL for the video as follows:
     *     https://www.youtube.com/watch?v={key}
     *
     * For example, for a YouTube video ID or key of {@code "QH2-TGUlwu4"}, the link would
     * look like:
     *     https://www.youtube.com/watch?v=QH2-TGUlwu4
     *
     * Corresponds to the {@code key} field from a TMDb response.
     *
     * @return
     *     identifier for use in a YouTube video URL for this video
     */
    public String getKey()
    {
        return key;
    }

    /**
     * Returns the type of the video, such as whether it is a trailer video or a teaser video.
     *
     * Corresponds to the {@code type} field from a TMDb response.
     *
     * @return
     *     type of the video
     */
    public String getType()
    {
        return type;
    }

    /**
     * Builder for use in constructing a new Video object.
     */
    public static class Builder
    {
        private String id;
        private String name;
        private String key;
        private String type;

        /**
         * Sets the video ID.
         *
         * @param id
         *     video ID assigned by TMDb
         * @return
         *     reference to this {@link Builder}
         */
        public Builder withId(String id)
        {
            this.id = id;
            return this;
        }

        /**
         * Sets the video's name.
         *
         * @param name
         *     name of the video
         * @return
         *     reference to this {@link Builder}
         */
        public Builder withName(String name)
        {
            this.name = name;
            return this;
        }

        /**
         * Sets the video's YouTube video ID.
         *
         * @param key
         *     identifier for use in a YouTube video URL for this video
         * @return
         *     reference to this {@link Builder}
         */
        public Builder withKey(String key)
        {
            this.key = key;
            return this;
        }

        /**
         * Sets the type of the video, such as whether it is a trailer video or a teaser video.
         *
         * @param type
         *     type of the video
         * @return
         *     reference to this {@link Builder}
         */
        public Builder withType(String type)
        {
            this.type = type;
            return this;
        }

        /**
         * Constructs a {@link Video} object with the given values for its fields.
         *
         * @return
         *     newly-constructed {@link Video} object with the given values for its fields
         */
        public Video build()
        {
            return new Video(id, name, key, type);
        }
    }
}
