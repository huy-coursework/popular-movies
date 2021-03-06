package com.huyvuong.udacity.popularmovies.model.business;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;
import com.huyvuong.udacity.popularmovies.data.MovieContract;

/**
 * Movie metadata record based on the fields returned by The Movie Database (TMDb).
 */
public class Movie implements Parcelable
{
    @SerializedName("id")
    private Integer id;

    @SerializedName("original_title")
    private String originalTitle;

    @SerializedName("poster_path")
    private String posterPath;

    @SerializedName("overview")
    private String plotSynopsis;

    @SerializedName("vote_average")
    private Double rating;

    @SerializedName("release_date")
    private String releaseDate;

    public static final Creator<Movie> CREATOR = new Creator<Movie>()
    {
        @Override
        public Movie createFromParcel(Parcel inParcel)
        {
            return new Movie(inParcel);
        }

        @Override
        public Movie[] newArray(int size)
        {
            return new Movie[size];
        }
    };

    private Movie(Integer id,
                  String originalTitle,
                  String posterPath,
                  String plotSynopsis,
                  Double rating,
                  String releaseDate)
    {
        this.id = id;
        this.originalTitle = originalTitle;
        this.posterPath = posterPath;
        this.plotSynopsis = plotSynopsis;
        this.rating = rating;
        this.releaseDate = releaseDate;
    }

    /**
     * Constructs a new {@link Movie} instance using the given parcel.
     *
     * @param inParcel
     *     parcel containing field values for this {@link Movie} object
     */
    protected Movie(Parcel inParcel)
    {
        id = inParcel.readInt();
        originalTitle = inParcel.readString();
        posterPath = inParcel.readString();
        plotSynopsis = inParcel.readString();
        rating = inParcel.readDouble();
        releaseDate = inParcel.readString();
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
     * Returns the original title for this movie, in its original language.
     *
     * Corresponds to the {@code original_title} field from a TMDb response.
     *
     * @return
     *     original title in its original language as a Unicode string
     */
    public String getOriginalTitle()
    {
        return originalTitle;
    }

    /**
     * Returns the path for this movie's poster image.
     *
     * Use the poster image with an image size to compute the URL for the poster image as follows:
     *     http://image.tmdb.org/t/p/w185/{poster_path}
     *
     * For example, for size {@code w185} and a poster path of
     * {@code "/nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg"}, the link would look like:
     *     http://image.tmdb.org/t/p/w185//nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg
     *
     * Corresponds to the {@code poster_path} field from a TMDb response.
     *
     * @return
     *     identifier for use in the URL path to this movie's poster image
     */
    public String getPosterPath()
    {
        return posterPath;
    }

    /**
     * Returns the plot synopsis for the movie.
     *
     * Corresponds to the {@code overview} field from a TMDb response.
     *
     * @return
     *     plot synopsis
     */
    public String getPlotSynopsis()
    {
        return plotSynopsis;
    }

    /**
     * Returns the average user rating for this movie.
     *
     * Corresponds to the {@code vote_average} field from a TMDb response.
     *
     * @return
     *     average user rating, rounded to the hundredths place
     */
    public Double getRating()
    {
        return rating;
    }

    /**
     * Returns the original release date for this movie.
     *
     * Corresponds to the {@code release_date} field from a TMDb response.
     *
     * @return
     *     ISO-8601 formatted date string corresponding to this movie's original release date
     */
    public String getReleaseDate()
    {
        return releaseDate;
    }

    /**
     * Converts this Movie object to a {@link ContentValues} object and returns it for use in
     * inserting this movie into a SQLite database.
     *
     * @return
     *     equivalent {@link ContentValues} object with matching values as this object
     */
    public ContentValues toContentValues()
    {
        ContentValues values = new ContentValues();
        values.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, id);
        values.put(MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE, originalTitle);
        values.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, posterPath);
        values.put(MovieContract.MovieEntry.COLUMN_PLOT_SYNOPSIS, plotSynopsis);
        values.put(MovieContract.MovieEntry.COLUMN_RATING, rating);
        values.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, releaseDate);
        return values;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel outParcel, int flags)
    {
        outParcel.writeInt(id);
        outParcel.writeString(originalTitle);
        outParcel.writeString(posterPath);
        outParcel.writeString(plotSynopsis);
        outParcel.writeDouble(rating);
        outParcel.writeString(releaseDate);
    }

    /**
     * Builder for use in constructing a new Movie object.
     */
    public static class Builder
    {
        private Integer id;
        private String originalTitle;
        private String posterPath;
        private String plotSynopsis;
        private Double rating;
        private String releaseDate;

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
         * Sets the movie's original title.
         *
         * @param originalTitle
         *     original title in its original language as a Unicode string
         * @return
         *     reference to this {@link Builder}
         */
        public Builder withOriginalTitle(String originalTitle)
        {
            this.originalTitle = originalTitle;
            return this;
        }

        /**
         * Sets the movie's poster path.
         *
         * @param posterPath
         *     identifier for use in the URL path to this movie's poster image
         * @return
         *     reference to this {@link Builder}
         */
        public Builder withPosterPath(String posterPath)
        {
            this.posterPath = posterPath;
            return this;
        }

        /**
         * Sets the movie's plot synopsis.
         *
         * @param plotSynopsis
         *     plot synopsis
         * @return
         *     reference to this {@link Builder}
         */
        public Builder withPlotSynopsis(String plotSynopsis)
        {
            this.plotSynopsis = plotSynopsis;
            return this;
        }

        /**
         * Sets the movie's average user rating.
         *
         * @param rating
         *     average user rating, rounded to the hundredths place
         * @return
         *     reference to this {@link Builder}
         */
        public Builder withRating(Double rating)
        {
            this.rating = rating;
            return this;
        }

        /**
         * Sets the movie's release date.
         *
         * @param releaseDate
         *     ISO-8601 formatted date string corresponding to this movie's original release date
         * @return
         *     reference to this {@link Builder}
         */
        public Builder withReleaseDate(String releaseDate)
        {
            this.releaseDate = releaseDate;
            return this;
        }

        /**
         * Constructs a {@link Movie} object with the given values for its fields.
         *
         * @return
         *     newly-constructed {@link Movie} object with the given values for its fields
         */
        public Movie build()
        {
            return new Movie(id, originalTitle, posterPath, plotSynopsis, rating, releaseDate);
        }
    }
}
