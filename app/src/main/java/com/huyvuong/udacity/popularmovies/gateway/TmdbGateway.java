package com.huyvuong.udacity.popularmovies.gateway;

import android.support.annotation.NonNull;
import android.util.Log;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.huyvuong.udacity.popularmovies.BuildConfig;
import com.huyvuong.udacity.popularmovies.gateway.response.GetMoviesResponse;
import com.huyvuong.udacity.popularmovies.gateway.response.GetReviewsResponse;
import com.huyvuong.udacity.popularmovies.gateway.response.GetVideosResponse;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

/**
 * Entry point for all calls against The Movie Database (TMDb). This manages configuring and
 * constructing calls to TMDb using Retrofit; no code outside of this class should attempt to build
 * a Retrofit instance for contacting TMDb.
 *
 * This product uses the TMDb API but is not endorsed or certified by TMDb.
 */
public class TmdbGateway
{
    private static final String LOG_TAG = TmdbGateway.class.getSimpleName();
    private static final String BASE_URL = "http://api.themoviedb.org/";
    private static final String QUERY_API_KEY = "api_key";

    private TmdbEndpointInterface tmdbService;

    /**
     * Constructs a new TmdbGateway instance configured to call the TMDb APIs.
     */
    public TmdbGateway()
    {
        OkHttpClient clientWithApiKey = buildClientWithApiKey();
        Retrofit retrofit = buildRetrofitForTmdb(clientWithApiKey);
        tmdbService = retrofit.create(TmdbEndpointInterface.class);
    }

    /**
     * Builds an {@code OkHttpClient} with the {@code api_key} query parameter value set to the
     * string set for {@code TmdbApiKey} in the gradle.properties file. This allows all calls using
     * this gateway to automatically supply the API key when calling TMDb.
     *
     * Pass this client when building a Retrofit instance
     *
     * From: http://stackoverflow.com/a/33667739
     *
     * @return
     *     {@code OkHttpClient} with the {@code api_key} query parameter already set up
     */
    @NonNull
    private OkHttpClient buildClientWithApiKey()
    {
        return new OkHttpClient.Builder()
                    .addInterceptor(
                            chain ->
                            {
                                Request request = chain.request();
                                HttpUrl url = request.url().newBuilder()
                                        .addQueryParameter(QUERY_API_KEY, BuildConfig.TMDB_API_KEY)
                                        .build();
                                request = request.newBuilder()
                                        .url(url)
                                        .build();
                                return chain.proceed(request);
                            })
                    .build();
    }

    /**
     * Configures and builds a {@code Retrofit} instance with the given {@code OkHttpClient}
     * instance. This provided instance allows the caller the ability pass in a customized
     * configuration for {@code OkHttpClient}.
     *
     * @param client
     *     {@code OkHttpClient} instance to use for HTTP calls via Retrofit
     * @return
     *     {@code Retrofit} instance configured with the given client to call the TMDb API
     */
    @NonNull
    private Retrofit buildRetrofitForTmdb(OkHttpClient client)
    {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
    }

    /**
     * Returns a ReactiveX {@code ConnectedObservable} for getting the list of movies from TMDb for
     * the given movie sorting criteria.
     *
     * @param movieSortingCriteria
     *     sorting criteria used to determine what kind of movies to look up
     * @return
     *     ReactiveX {@code ConnectedObservable} that obtains a list of movies from TMDb based on
     *     the given movie sorting criteria
     */
    public ConnectableObservable<GetMoviesResponse> getMovies(String movieSortingCriteria)
    {
        Log.d(LOG_TAG, String.format("Request -> getMovies(\"%s\")", movieSortingCriteria));
        ConnectableObservable<GetMoviesResponse> observable =
                tmdbService.getMovies(movieSortingCriteria)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .share()
                        .replay();
        observable.subscribe(
                response -> Log.i(
                        LOG_TAG,
                        String.format(
                                "Response <- getMovies(\"%s\"): %s",
                                movieSortingCriteria,
                                Stream.of(response.getMovies())
                                        .map(movie -> "\"" + movie.getOriginalTitle() + "\"")
                                        .collect(Collectors.toList()))),
                error -> Log.e(
                        LOG_TAG,
                        String.format(
                                "Error <- getMovies(\"%s\"): %s",
                                movieSortingCriteria,
                                error.getMessage()),
                        error));
        return observable;
    }

    /**
     * Returns a ReactiveX {@code ConnectedObservable} for getting the list of reviews from TMDb for
     * the given movie ID.
     *
     * @param movieId
     *     movie ID corresponding to the movie to obtain reviews for
     * @return
     *     ReactiveX {@code ConnectedObservable} that obtains a list of reviews from TMDb based on
     *     the given movie ID
     */
    public ConnectableObservable<GetReviewsResponse> getReviews(int movieId)
    {
        Log.d(LOG_TAG, String.format("Request -> getReviews(\"%s\")", movieId));
        ConnectableObservable<GetReviewsResponse> observable =
                tmdbService.getReviews(movieId)
                           .subscribeOn(Schedulers.io())
                           .observeOn(AndroidSchedulers.mainThread())
                           .share()
                           .replay();
        observable.subscribe(
                response -> Log.i(
                        LOG_TAG,
                        String.format(
                                "Response <- getReviews(\"%s\"): %s",
                                movieId,
                                Stream.of(response.getReviews())
                                      .map(review -> "\"" + review.getAuthor() + "\"")
                                      .collect(Collectors.toList()))),
                error -> Log.e(
                        LOG_TAG,
                        String.format(
                                "Error <- getReviews(\"%s\"): %s",
                                movieId,
                                error.getMessage()),
                        error));
        return observable;
    }

    /**
     * Returns a ReactiveX {@code ConnectedObservable} for getting the list of videos from TMDb for
     * the given movie ID.
     *
     * @param movieId
     *     movie ID corresponding to the movie to obtain videos for
     * @return
     *     ReactiveX {@code ConnectedObservable} that obtains a list of videos from TMDb based on
     *     the given movie ID
     */
    public ConnectableObservable<GetVideosResponse> getVideos(int movieId)
    {
        Log.d(LOG_TAG, String.format("Request -> getVideos(\"%s\")", movieId));
        ConnectableObservable<GetVideosResponse> observable =
                tmdbService.getVideos(movieId)
                           .subscribeOn(Schedulers.io())
                           .observeOn(AndroidSchedulers.mainThread())
                           .share()
                           .replay();
        observable.subscribe(
                response -> Log.i(
                        LOG_TAG,
                        String.format(
                                "Response <- getVideos(\"%s\"): %s",
                                movieId,
                                Stream.of(response.getVideos())
                                      .map(video -> "\"" + video.getName() + "\"")
                                      .collect(Collectors.toList()))),
                error -> Log.e(
                        LOG_TAG,
                        String.format(
                                "Error <- getVideos(\"%s\"): %s",
                                movieId,
                                error.getMessage()),
                        error));
        return observable;
    }

    /**
     * Criteria to use in determining what kind of movies to look up. Each value represents a
     * different metric to measure a movie by and find the 'highest' of.
     */
    public static final class MovieSortingCriteria
    {
        public static final String POPULAR = "popular";
        public static final String TOP_RATED = "top_rated";
    }

    /**
     * Endpoints for accessing TMDb by. Used to define endpoints for Retrofit.
     */
    private interface TmdbEndpointInterface
    {
        /**
         * Returns the list of movies from TMDb that best match the given movie sorting criteria.
         *
         * @param movieSortingCriteria
         *     sorting criteria from {@link MovieSortingCriteria} to sort movies by
         * @return
         *     list of movies returned by TMDb
         */
        @GET("3/movie/{criteria}")
        Observable<GetMoviesResponse> getMovies(@Path("criteria") String movieSortingCriteria);

        /**
         * Returns the list of reviews from TMDb for the given movie ID.
         *
         * @param movieId
         *     movie ID corresponding to the movie to obtain reviews for
         * @return
         *     list of reviews returned by TMDb
         */
        @GET("3/movie/{movieId}/reviews")
        Observable<GetReviewsResponse> getReviews(@Path("movieId") int movieId);

        /**
         * Returns the list of videos from TMDb for the given movie ID.
         *
         * @param movieId
         *     movie ID corresponding to the movie to obtain videos for
         * @return
         *     list of videos returned by TMDb
         */
        @GET("3/movie/{movieId}/videos")
        Observable<GetVideosResponse> getVideos(@Path("movieId") int movieId);
    }
}
