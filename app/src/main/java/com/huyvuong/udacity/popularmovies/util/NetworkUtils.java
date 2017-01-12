package com.huyvuong.udacity.popularmovies.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Utility class containing methods involved with Internet and connectivity.
 */
public class NetworkUtils
{
    private NetworkUtils()
    {
        throw new UnsupportedOperationException("Do not instantiate utility classes.");
    }

    /**
     * Returns true if the device is connected to the Internet. Returns false otherwise.
     *
     * From: http://stackoverflow.com/a/4009133
     *
     * @param context
     *     object that stores the application's environment state
     * @return
     *     true if the device is currently online, false otherwise
     */
    public static boolean isOnline(Context context)
    {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}
