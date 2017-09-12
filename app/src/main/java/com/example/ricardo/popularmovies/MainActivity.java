package com.example.ricardo.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.ricardo.popularmovies.adapters.MovieAdapter;
import com.example.ricardo.popularmovies.data.FavoritesMoviesContract.FavoriteMoviesEntry;
import com.example.ricardo.popularmovies.pojos.Movie;
import com.example.ricardo.popularmovies.pojos.Review;
import com.example.ricardo.popularmovies.utils.FetchMovies;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements MovieAdapter.OnItemClickListener, FetchMovies.OnTaskCompleted {

    private ArrayList<Movie> mMovies;
    private MovieAdapter mAdapter;

    @BindView(R.id.pb_loading_indicator)
    ProgressBar mLoadingIndicator;
    @BindView(R.id.tv_error_message)
    TextView mErrorMessageTextView;
    @BindView(R.id.rv_movies)
    RecyclerView mRecyclerView;

    // THE MOVIE DB constants
    private static final String API_KEY = BuildConfig.API_KEY;
    public static final String THE_MOVIE_DB_URL = "https://api.themoviedb.org/3/movie/%s?api_key=" + API_KEY;
    private String sortBy;

    private SharedPreferences mSharedPreferences;

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mSharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        sortBy = mSharedPreferences.getString("sortBy", "popular");

        // Get the movies list from bundle if it isn't null
        if (savedInstanceState != null)
            mMovies = savedInstanceState.getParcelableArrayList("moviesList");

        // If there isn't a movies list available then create a new one and fetch the data
        if (mMovies == null) mMovies = new ArrayList<>();

        if (!sortBy.equals("favorites")) {
            mErrorMessageTextView.setVisibility(View.INVISIBLE);
            mLoadingIndicator.setVisibility(View.VISIBLE);
            new FetchMovies(this, this, THE_MOVIE_DB_URL, sortBy, 100).execute();
        }
        RecyclerView.LayoutManager layoutManager;
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

        if (sortBy.equals("favorites")) {
            getFavorites();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMovies.size() > 0) {
            outState.putParcelableArrayList("moviesList", mMovies);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onSingleMovieClickListener(int pos) {
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        intent.putExtra("movie", mMovies.get(pos));
        if (mMovies.get(pos).getBlobPoster() != null) {
            intent.putExtra("blob", mMovies.get(pos).getBlobPoster());
        }
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // After the menu is inflated, check the correct option
        if (sortBy.equals("popular")) {
            menu.findItem(R.id.most_popular_sort_action).setChecked(true);
        } else {
            menu.findItem(R.id.highest_rated_sort_action).setChecked(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Create a variable to save the current user's choice
        String userChoice;

        switch (item.getItemId()) {
            case R.id.highest_rated_sort_action:
                userChoice = "top_rated";
                item.setChecked(true);
                break;
            case R.id.most_popular_sort_action:
                userChoice = "popular";
                item.setChecked(true);
                break;
            case R.id.favorites_sort_action:
                userChoice = "favorites";
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
            mMovies.clear();
            mAdapter.notifyDataSetChanged();
            if (!sortBy.equals("favorites")) {
                new FetchMovies(this, this, THE_MOVIE_DB_URL, sortBy, 100).execute();
            } else {
                getFavorites();
            }
        }
        return true;
    }



    @Override
    public void onTaskCompleted(Movie movie, Review review, String trailerKey, String trailerTitle) {
        // Hide the progress
        mLoadingIndicator.setVisibility(View.INVISIBLE);

        // Check if the variable isn't null before adding it to the list
        if (movie != null) {
            mMovies.add(movie);
            mAdapter.notifyDataSetChanged();
        } else {
            mErrorMessageTextView.setVisibility(View.VISIBLE);
        }
    }

    private void getFavorites() {
        // Clear the list before query
        mMovies.clear();

        // Query all the favorites using the provider
        Cursor cursor = getContentResolver().query(
                FavoriteMoviesEntry.CONTENT_URI, null, null, null, null);

        // Check if the user saved any favorites
        if (cursor != null && cursor.getCount() > 0) {
            mErrorMessageTextView.setVisibility(View.INVISIBLE);
            while (cursor.moveToNext()) {
                Movie movie = new Movie(cursor.getInt(cursor.getColumnIndex(FavoriteMoviesEntry.COLUMN_MOVIE_ID)),
                        cursor.getString(cursor.getColumnIndex(FavoriteMoviesEntry.COLUMN_MOVIE_TITLE)),
                        null,
                        cursor.getBlob(cursor.getColumnIndex(FavoriteMoviesEntry.COLUMN_MOVIE_POSTER)),
                        cursor.getString(cursor.getColumnIndex(FavoriteMoviesEntry.COLUMN_SYNOPSIS)),
                        cursor.getDouble(cursor.getColumnIndex(FavoriteMoviesEntry.COLUMN_RATING)),
                        cursor.getString(cursor.getColumnIndex(FavoriteMoviesEntry.COLUMN_RELEASE_DATE)));

                mMovies.add(movie);
            }
            cursor.close();
            mAdapter.notifyDataSetChanged();
        } else {
            // Show a message if the user doesn't have any favorite movie saved yet.
            mErrorMessageTextView.setText("You don't have any favorites yet!");
            mErrorMessageTextView.setVisibility(View.VISIBLE);
        }
    }
}