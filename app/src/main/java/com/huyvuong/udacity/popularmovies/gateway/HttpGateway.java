package com.huyvuong.udacity.popularmovies.gateway;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Entry point for all HTTP calls against any web service.
 *
 * Only logic generic to all HTTP calls belong in this class. Store any service-specific logic in
 * its own gateway class in this package.
 */
public class HttpGateway
{
    private static final String LOG_TAG = HttpGateway.class.getSimpleName();

    /**
     * Makes an HTTP GET request against the given URL and for a 200 OK response, returns the
     * response body as a string. If the response was not a 200 OK response, returns an
     * empty string.
     *
     * @param url
     *     URL to make the GET request against
     * @return
     *     response given by the server
     */
    @NonNull
    public String makeGetRequestTo(URL url)
    {
        // Attempt to call TMDb and stores the JSON response in a string. If the call fails for any
        // reason, return an empty string.
        Log.i(LOG_TAG, String.format("Request: GET %s", url));
        String response = "";
        InputStream inputStream = null;
        try
        {
            // Make a GET request to TMDb.
            HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
            httpUrlConnection.setRequestMethod("GET");
            httpUrlConnection.connect();

            // Convert the response contents into a string. If the entire operation succeeds,
            // return the JSON string returned by TMDb.
            inputStream = httpUrlConnection.getInputStream();
            if (inputStream != null)
            {
                // Emit a log based on the returned response.
                int status = httpUrlConnection.getResponseCode();
                response = collectInputStreamToString(inputStream);
                if (status != HttpURLConnection.HTTP_OK)
                {
                    // If the call resulted in a 400 or 500 error from the server, return an empty
                    // string and log the error.
                    Log.e(LOG_TAG, String.format("Error: GET %s -> %d: %s", url, status, response));
                    response = "";
                }
                else
                {
                    Log.i(LOG_TAG, String.format("Response: GET %s -> %s", url, response));
                }
            }
        }
        catch (IOException e)
        {
            // If we encounter any exceptions, log the request that caused an error and return a
            // null response string.
            Log.e(LOG_TAG, String.format("Error: GET %s", url), e);
        }
        finally
        {
            try
            {
                if (inputStream != null)
                {
                    // Close the input stream once we're done using it.
                    inputStream.close();
                }
            }
            catch (IOException e)
            {
                Log.e(LOG_TAG, String.format("Could not close inputStream for URL: %s", url), e);
            }
        }
        return response;
    }

    /**
     * Converts the given input stream to a string value.
     *
     * @param inputStream
     *     input stream whose contents to extract and convert to a string
     * @return
     *     string contents extracted from the given input string
     * @throws IOException
     *     if while reading the stream, an I/O error is thrown
     */
    @NonNull
    private String collectInputStreamToString(InputStream inputStream) throws IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder buffer = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null)
        {
            buffer.append(line).append("\n");
        }

        return (buffer.length() == 0) ? "" : buffer.toString();
    }
}
