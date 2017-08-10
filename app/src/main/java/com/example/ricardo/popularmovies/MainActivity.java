package com.example.ricardo.popularmovies;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

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

    private static final String TAG = "MainActivity";

    // POPULAR TOP MOVIES URL PATH
    private String THE_MOVIE_DB_URL = "https://api.themoviedb.org/3/movie/popular?api_key=ef83058e91d65966e65b63151aaaf75c";
    private String THUMBNAIL_BASE_URL = "https://image.tmdb.org/t/p/w185//";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mMovies = new ArrayList<>();

        new FetchMovies().execute(THE_MOVIE_DB_URL);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_movies);
        RecyclerView.LayoutManager layoutManager;

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            layoutManager = new GridLayoutManager(this, 2);
        } else {
            layoutManager = new GridLayoutManager(this, 3);
        }
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mAdapter = new MovieAdapter(this, mMovies, this);
        mRecyclerView.setAdapter(mAdapter);

    }

    @Override
    public void onSingleMovieClickListener(int pos) {
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        intent.putExtra("movie", mMovies.get(pos));
        startActivity(intent);
    }


    private class FetchMovies extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {

            String movieData = null;

            try {
                movieData = getResponseFromHttpUrl(new URL(strings[0]));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return movieData;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s != null || !s.isEmpty()) {
                fetchFromJson(s);
            }
        }
    }


    private void fetchFromJson(String jsonData) {
        try {
            JSONObject object = new JSONObject(jsonData);
            JSONArray results = object.getJSONArray("results");
            for (int i = 0; i < results.length(); i++) {
                JSONObject jsonObject = results.getJSONObject(i);
                mMovies.add(new Movie(jsonObject.getString("title"),
                        THUMBNAIL_BASE_URL + jsonObject.getString("poster_path"),
                        jsonObject.getString("overview"),
                        jsonObject.getDouble("vote_average"),
                        jsonObject.getString("release_date")));
                Log.i(TAG, "fetchFromJson: " + mMovies.toString());
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

