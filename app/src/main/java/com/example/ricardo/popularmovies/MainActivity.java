package com.example.ricardo.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity implements MovieAdapter.OnItemClickListener {

    private ArrayList<Movie> mMovies;
    private RecyclerView mRecyclerView;
    private MovieAdapter mAdapter;

    // THE MOVIE DB constants
    private String THE_MOVIE_DB_URL = "https://api.themoviedb.org/3/movie/%s?api_key=ef83058e91d65966e65b63151aaaf75c";
    private String THUMBNAIL_BASE_URL = "https://image.tmdb.org/t/p/w185//";
    private String sortBy;

    private SharedPreferences mSharedPreferences;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_movies);
        RecyclerView.LayoutManager layoutManager;

        mSharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        sortBy = mSharedPreferences.getString("sortBy", "popular");

        if (savedInstanceState != null) {
            // Check the bundle if the user rotated the device
            mMovies = savedInstanceState.getParcelableArrayList("moviesList");
        } else {
            // Create a new Movie list and fetch the data
            mMovies = new ArrayList<>();
            new FetchMovies().execute(THE_MOVIE_DB_URL);
        }

        // Check if the device is in portrait or landscape
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            // Set 2 columns in the PORTRAIT orientation
            layoutManager = new GridLayoutManager(this, 2);
        } else {
            // Set 3 columns in the LANDSCAPE orientation
            layoutManager = new GridLayoutManager(this, 3);
        }
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mAdapter = new MovieAdapter(this, mMovies, this);
        mRecyclerView.setAdapter(mAdapter);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("moviesList", mMovies);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onSingleMovieClickListener(int pos) {
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        intent.putExtra("movie", mMovies.get(pos));
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // After the menu is inflated, check the correct option
        if (sortBy.equals("popular")){
            menu.findItem(R.id.most_popular_sort_action).setChecked(true);
        } else {
            menu.findItem(R.id.highest_rated_sort_action).setChecked(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        String userChoice = null;

        switch (item.getItemId()) {
            case R.id.highest_rated_sort_action:
                userChoice = "top_rated";
                item.setChecked(true);
                break;
            case R.id.most_popular_sort_action:
                userChoice = "popular";
                item.setChecked(true);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        // Check first if the user selected a different option
        if (!userChoice.equals(sortBy)) {
            // Set the member variable to the users selected option
            sortBy = userChoice;
            // Save the user's preference in SharedPreferences
            mSharedPreferences.edit().putString("sortBy", sortBy).apply();
            // Fetch new data with the new sort order
            new FetchMovies().execute(THE_MOVIE_DB_URL);
        }

        return true;
    }

    private class FetchMovies extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {

            String movieData = null;
            String finalUrl = String.format(strings[0], sortBy);
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
            if (s != null || !s.isEmpty()) {
                // Fetch the data from the entire JSON file
                fetchFromJson(s);
            }
        }
    }


    private void fetchFromJson(String jsonData) {
        try {
            // Clear the list before fetching new data
            mMovies.clear();

            JSONObject object = new JSONObject(jsonData);
            JSONArray results = object.getJSONArray("results");
            for (int i = 0; i < results.length(); i++) {
                JSONObject jsonObject = results.getJSONObject(i);
                mMovies.add(new Movie(jsonObject.getString("title"),
                        THUMBNAIL_BASE_URL + jsonObject.getString("poster_path"),
                        jsonObject.getString("overview"),
                        jsonObject.getDouble("vote_average"),
                        jsonObject.getString("release_date")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mAdapter.notifyDataSetChanged();
    }

    public static String getResponseFromHttpUrl(URL url) throws IOException {
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
}

