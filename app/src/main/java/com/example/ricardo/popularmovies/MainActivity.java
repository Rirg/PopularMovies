package com.example.ricardo.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.ricardo.popularmovies.adapters.MovieAdapter;
import com.example.ricardo.popularmovies.data.FavoritesMoviesContract;
import com.example.ricardo.popularmovies.data.FavoritesMoviesContract.FavoriteMoviesEntry;
import com.example.ricardo.popularmovies.pojos.Movie;
import com.example.ricardo.popularmovies.pojos.Review;
import com.example.ricardo.popularmovies.utils.FetchMovies;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements MovieAdapter.OnItemClickListener,
        FetchMovies.OnTaskCompleted,
        LoaderManager.LoaderCallbacks<Cursor> {

    // UI
    @BindView(R.id.pb_loading_indicator)
    ProgressBar mLoadingIndicator;
    @BindView(R.id.tv_error_message)
    TextView mErrorMessageTextView;
    @BindView(R.id.rv_movies)
    RecyclerView mRecyclerView;

    private ArrayList<Movie> mMovies;
    private MovieAdapter mAdapter;

    // THE MOVIE DB constants
    private static final String API_KEY = BuildConfig.API_KEY;
    public static final String THE_MOVIE_DB_URL = "https://api.themoviedb.org/3/movie/%s?api_key=" + API_KEY;
    private static final int ID_MOVIES_LOADER = 22;

    // Variables to save the sort criteria user preference
    private String sortBy;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind the views using ButterKnife
        ButterKnife.bind(this);

        // Get the SharedPreferences, if there isn't any saved value, then use "popular" as default
        // sort criteria
        mSharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
        sortBy = mSharedPreferences.getString("sortBy", "popular");

        mMovies = new ArrayList<>();


        // Create a LayoutManager for the RecyclerView
        RecyclerView.LayoutManager layoutManager;

        // Check if the device is in portrait or landscape orientation
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            // Set 2 columns in the PORTRAIT orientation
            layoutManager = new GridLayoutManager(this, 2);
        } else {
            // Set 3 columns in the LANDSCAPE orientation
            layoutManager = new GridLayoutManager(this, 3);
        }
        // Set the LayoutManager to the RecyclerView
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        /// Create a new MovieAdapter and set it to the RecyclerView
        mAdapter = new MovieAdapter(this, mMovies, this);
        mRecyclerView.setAdapter(mAdapter);

    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMovies != null && mMovies.size() > 0) {
            outState.putParcelableArrayList("moviesList", mMovies);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Just fetch new data when the ArrayList is empty
        if (mMovies.size() == 0) {
            if (!sortBy.equals("favorites")) {
                mErrorMessageTextView.setVisibility(View.INVISIBLE);
                mLoadingIndicator.setVisibility(View.VISIBLE);
                new FetchMovies(this, this, THE_MOVIE_DB_URL, sortBy, FetchMovies.MOVIES_CODE).execute();
            }
            // Restart the loader if the sort criteria is set to favorites
            else if (sortBy.equals("favorites")) {
                getSupportLoaderManager().restartLoader(ID_MOVIES_LOADER, null, this);
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mMovies = savedInstanceState.getParcelableArrayList("moviesList");
        mAdapter.swapList(mMovies);
    }

    @Override
    public void onSingleMovieClickListener(Movie movie) {
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        intent.putExtra("movie", movie);
        if (movie.getBlobPoster() != null) {
            intent.putExtra("blob", movie.getBlobPoster());
        }
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // After the menu is inflated, check the correct option
        switch (sortBy) {
            case "popular":
                menu.findItem(R.id.most_popular_sort_action).setChecked(true);
                break;
            case "top_rated":
                menu.findItem(R.id.highest_rated_sort_action).setChecked(true);
                break;
            case "favorites":
                menu.findItem(R.id.favorites_sort_action).setChecked(true);
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Create a variable to save the current user's choice
        String userChoice;

        // Save the user selection and check the corresponding checkbox
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
            // Hide the error message and show the progress bar
            mErrorMessageTextView.setVisibility(View.INVISIBLE);
            mLoadingIndicator.setVisibility(View.VISIBLE);

            // Save the user selection in the member variable
            sortBy = userChoice;

            // Save the user's preference in SharedPreferences
            mSharedPreferences.edit().putString("sortBy", sortBy).apply();

            // Clear the list before fetching new data
            mMovies.clear();
            if (!sortBy.equals("favorites")) {
                // Destroy the loader, we don't need it for fetching new data from the internet
                getSupportLoaderManager().destroyLoader(ID_MOVIES_LOADER);

                // Get new data from the helper class using TheMoviesDB API
                new FetchMovies(this, this, THE_MOVIE_DB_URL, sortBy, 100).execute();

                // Send a null cursor and the current list of movies, because we aren't getting
                // anything from the db.
                mAdapter.swapList(mMovies);
            } else {
                // Restart the loader because we need the data from the db
                getSupportLoaderManager().restartLoader(ID_MOVIES_LOADER, null, this);
            }
        }
        return true;
    }


    @Override
    public void onTaskCompleted(ArrayList<Movie> movies, ArrayList<Review> reviews, String trailerKey, String trailerTitle) {
        // Hide the loading indicator
        mLoadingIndicator.setVisibility(View.INVISIBLE);

        // Check if the variable isn't null before adding it to the list
        if (movies != null) {
            // Hide the error message, add the new movie to the adapter and notify.
            mErrorMessageTextView.setVisibility(View.INVISIBLE);
            mMovies = movies;
            mAdapter.swapList(mMovies);
        } else {
            // The movie object is null, show an error message to the user
            showMessage();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case ID_MOVIES_LOADER:
                // Return a cursor with all the favorites from the db
                return new CursorLoader(this,
                        FavoriteMoviesEntry.CONTENT_URI,
                        null,
                        null,
                        null,
                        null);

            default:
                throw new RuntimeException("Loader Not Implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Hide the loading indicator
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        if (data == null || data.getCount() == 0) {
            // Show a message if the user doesn't have any favorite movie saved yet.
            showMessage();
        } else {
            mErrorMessageTextView.setVisibility(View.INVISIBLE);
        }
        getFavorites(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapList(null);
    }

    // Helper method to show the corresponding error message
    private void showMessage() {
        if (sortBy.equals("favorites")) {
            mErrorMessageTextView.setText(getString(R.string.no_favorites_message));
        } else {
            mErrorMessageTextView.setText(R.string.internet_error_message);
        }
        mErrorMessageTextView.setVisibility(View.VISIBLE);
    }

    private void getFavorites(Cursor cursor) {
        ArrayList<Movie> movies = new ArrayList<>();
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                Movie movie = new Movie(cursor.getInt(cursor.getColumnIndex(FavoritesMoviesContract.FavoriteMoviesEntry.COLUMN_MOVIE_ID)),
                        cursor.getString(cursor.getColumnIndex(FavoritesMoviesContract.FavoriteMoviesEntry.COLUMN_MOVIE_TITLE)),
                        null,
                        cursor.getBlob(cursor.getColumnIndex(FavoritesMoviesContract.FavoriteMoviesEntry.COLUMN_MOVIE_POSTER)),
                        cursor.getString(cursor.getColumnIndex(FavoritesMoviesContract.FavoriteMoviesEntry.COLUMN_SYNOPSIS)),
                        cursor.getDouble(cursor.getColumnIndex(FavoritesMoviesContract.FavoriteMoviesEntry.COLUMN_RATING)),
                        cursor.getString(cursor.getColumnIndex(FavoritesMoviesContract.FavoriteMoviesEntry.COLUMN_RELEASE_DATE)));

                movies.add(movie);
            }
        }
        mMovies = movies;
        mAdapter.swapList(mMovies);
    }
}