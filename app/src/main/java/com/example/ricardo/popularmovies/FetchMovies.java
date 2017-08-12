package com.example.ricardo.popularmovies;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by Ricardo on 8/12/17.
 */

public class FetchMovies extends AsyncTask<Void, Void, String> {

    private static final String THUMBNAIL_BASE_URL = "https://image.tmdb.org/t/p/w185//";

    private OnTaskCompleted listener;
    private String baseUrl;
    private String sortBy;

    interface OnTaskCompleted{
        void onTaskCompleted(Movie movie);
    }

    public FetchMovies(OnTaskCompleted listener, String baseUrl, String sortBy){
        this.listener = listener;
        this.baseUrl = baseUrl;
        this.sortBy = sortBy;
    }

    @Override
    protected String doInBackground(Void... voids) {

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
            listener.onTaskCompleted(null);
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
            for (int i = 0; i < results.length(); i++) {
                JSONObject jsonObject = results.getJSONObject(i);
                Movie movie = new Movie(jsonObject.getLong("id"),
                        jsonObject.getString("title"),
                        THUMBNAIL_BASE_URL + jsonObject.getString("poster_path"),
                        jsonObject.getString("overview"),
                        jsonObject.getDouble("vote_average"),
                        jsonObject.getString("release_date"));
                listener.onTaskCompleted(movie);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
