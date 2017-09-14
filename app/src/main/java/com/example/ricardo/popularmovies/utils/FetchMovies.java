package com.example.ricardo.popularmovies.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import com.example.ricardo.popularmovies.pojos.Movie;
import com.example.ricardo.popularmovies.pojos.Review;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by Ricardo on 8/12/17.
 */

public class FetchMovies extends AsyncTask<Void, Void, String> {

    private static final String THUMBNAIL_BASE_URL = "https://image.tmdb.org/t/p/w185//";

    private OnTaskCompleted listener;
    private String baseUrl;
    private String sortBy;
    private Context mContext;
    private int mCode;

    private static final String TAG = "FetchMovies";

    public static final int MOVIES_CODE = 100;
    public static final int REVIEWS_CODE = 200;
    public static final int TRAILER_CODE = 300;

    public interface OnTaskCompleted {
        void onTaskCompleted(ArrayList<Movie> movies, ArrayList <Review> reviews, String trailerKey, String trailerTitle);
    }

    public FetchMovies(Context context, OnTaskCompleted listener, String baseUrl, String sortBy, int code) {
        this.mContext = context;
        this.listener = listener;
        this.baseUrl = baseUrl;
        this.sortBy = sortBy;
        this.mCode = code;
    }


    @Override
    protected String doInBackground(Void... voids) {

        if (!isOnline(mContext)) return null;


        String movieData = null;
        String finalUrl = String.format(baseUrl, sortBy);
        try {
            movieData = getResponseFromHttpUrl(new URL(finalUrl));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return movieData;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        // Hide the progress bar
        // Check if the string variable isn't empty or null
        if (s != null && !s.isEmpty()) {
            // Fetch the data from the entire JSON file
            fetchFromJson(s);
        } else {
            listener.onTaskCompleted(null, null, null, null);
        }
    }

    private static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {

            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    private void fetchFromJson(String jsonData) {

        try {

            JSONObject object = new JSONObject(jsonData);
            JSONArray results = object.getJSONArray("results");

            switch (mCode) {
                case MOVIES_CODE:
                    ArrayList<Movie> movies = new ArrayList<>();
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject jsonObject = results.getJSONObject(i);
                        Movie movie = new Movie(jsonObject.getLong("id"),
                                jsonObject.getString("title"),
                                THUMBNAIL_BASE_URL + jsonObject.getString("poster_path"),
                                null,
                                jsonObject.getString("overview"),
                                jsonObject.getDouble("vote_average"),
                                jsonObject.getString("release_date"));
                        movies.add(movie);
                    }
                    listener.onTaskCompleted(movies, null, null, null);
                    break;

                case REVIEWS_CODE:
                    ArrayList<Review> reviews = new ArrayList<>();
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject jsonObject = results.getJSONObject(i);
                        Review review = new Review(jsonObject.getString("author"),
                                jsonObject.getString("content"));
                        reviews.add(review);
                    }
                    listener.onTaskCompleted(null, reviews, null, null);
                    break;

                case TRAILER_CODE:
                    JSONObject jsonObject = results.getJSONObject(0);
                    listener.onTaskCompleted(null, null, jsonObject.getString("key"), jsonObject.getString("name"));
                    break;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Method for checking the current network status
    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

}
