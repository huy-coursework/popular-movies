package com.huyvuong.udacity.popularmovies.constants;

/**
 * Constants for the image sizes supported by TMDb for images.
 *
 * Use the image size with an image path to compute the URL for the image as follows:
 *     http://image.tmdb.org/t/p/{image_size}/{image_path}
 *
 * For example, for size {@code w185} and a poster path of
 * {@code "/nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg"}, the link would look like:
 *     http://image.tmdb.org/t/p/w185//nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg
 */
public class TmdbImageSizes
{
    public static final String W92 = "w92";
    public static final String W154 = "w154";
    public static final String W185 = "w185";
    public static final String W342 = "w342";
    public static final String W500 = "w500";
    public static final String W780 = "w780";
    public static final String ORIGINAL = "original";
}
