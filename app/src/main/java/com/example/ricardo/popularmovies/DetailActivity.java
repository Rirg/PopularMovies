package com.example.ricardo.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ricardo.popularmovies.adapters.ReviewsAdapter;
import com.example.ricardo.popularmovies.data.FavoritesMoviesContract.FavoriteMoviesEntry;
import com.example.ricardo.popularmovies.pojos.Movie;
import com.example.ricardo.popularmovies.pojos.Review;
import com.example.ricardo.popularmovies.utils.FetchMovies;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity implements FetchMovies.OnTaskCompleted {

    // UI
    @BindView(R.id.tv_title)
    TextView title;
    @BindView(R.id.iv_poster)
    ImageView poster;
    @BindView(R.id.tv_synopsis)
    TextView synopsis;
    @BindView(R.id.tv_release_date)
    TextView releaseDate;
    @BindView(R.id.tv_rating)
    TextView rating;
    @BindView(R.id.btn_favorite)
    ImageButton favoriteBtn;
    @BindView(R.id.ib_trailer)
    ImageButton trailerBtn;
    @BindView(R.id.tv_trailer_title)
    TextView trailerTv;

    // Variables to save movie data
    private Movie mCurrentMovie;
    private String trailerUrl;
    private ArrayList<Review> mReviews;
    private ReviewsAdapter mAdapter;
    private boolean isSaved = false;

    // Trailer and reviews base urls
    public static final String API_KEY = BuildConfig.API_KEY;
    public static final String TRAILER_BASE_URL = "https://api.themoviedb.org/3/movie/%s/videos?api_key=" + API_KEY;
    public static final String REVIEWS_BASE_URL = "https://api.themoviedb.org/3/movie/%s/reviews?api_key=" + API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Set the activity title
        setTitle("Movie Details");

        // Bind the views with ButterKnife
        ButterKnife.bind(this);

        // Create a new ArrayList to hold the reviews
        mReviews = new ArrayList<>();

        // Create an Adapter and pass in the ArrayList
        mAdapter = new ReviewsAdapter(this, mReviews);

        // Create and setup the RecyclerView
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_reviews);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mAdapter);

        // Check the movie extra
        if (getIntent().hasExtra("movie")) {
            // Get the Movie
            mCurrentMovie = getIntent().getParcelableExtra("movie");
        }
        // Check the blob extra
        if (getIntent().hasExtra("blob")) {
            // Get the poster from a byte array
            mCurrentMovie.setBlobPoster(getIntent().getByteArrayExtra("blob"));
        }

        // If there isn't Internet connection, then hide the Reviews and Trailer section
        if (!FetchMovies.isOnline(this)) {
            (findViewById(R.id.trailer_and_reviews_container)).setVisibility(View.GONE);
        }

        final Uri uri = Uri.parse(FavoriteMoviesEntry.CONTENT_URI + "/" + mCurrentMovie.getId());
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor.getCount() > 0) {
            isSaved = true;
            favoriteBtn.setImageResource(R.drawable.ic_star);
        }
        cursor.close();

        // Check if there is a Url or Blob from the current movie and load the image
        if (mCurrentMovie.getPosterUrl() != null) {
            Picasso.with(this)
                    .load(mCurrentMovie.getPosterUrl())
                    .placeholder(R.drawable.poster_placeholder)
                    .into(poster);
        } else if (mCurrentMovie.getBlobPoster() != null) {
            byte[] blob = mCurrentMovie.getBlobPoster();
            Bitmap bmp = BitmapFactory.decodeByteArray(blob, 0, blob.length);
            poster.setImageBitmap(bmp);
        } else {
            poster.setImageResource(R.drawable.poster_placeholder);
        }
        // Use substring() to get just the year from the release date
        String release = mCurrentMovie.getReleaseDate();
        String releaseYear = release.substring(0, Math.min(release.length(), 4));

        title.setText(mCurrentMovie.getTitle());
        synopsis.setText(mCurrentMovie.getSynopsis());
        releaseDate.setText(releaseYear);
        rating.setText(String.valueOf(mCurrentMovie.getRating()) + "/10");

        favoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check if the movie isn't already saved in the DB first
                if (!isSaved) {
                    // Create a ContentValues object to save the information
                    final ContentValues values = new ContentValues();
                    values.put(FavoriteMoviesEntry.COLUMN_MOVIE_ID, mCurrentMovie.getId());
                    values.put(FavoriteMoviesEntry.COLUMN_MOVIE_TITLE, mCurrentMovie.getTitle());
                    values.put(FavoriteMoviesEntry.COLUMN_RATING, mCurrentMovie.getRating());
                    values.put(FavoriteMoviesEntry.COLUMN_SYNOPSIS, mCurrentMovie.getSynopsis());
                    values.put(FavoriteMoviesEntry.COLUMN_RELEASE_DATE, mCurrentMovie.getReleaseDate());

                    // Use Picasso to get the Image from the URL and then pass it to Bitmap so we can
                    // save it in the DB as a Blob variable.
                    Picasso.with(DetailActivity.this)
                            .load(mCurrentMovie.getPosterUrl())
                            .into(new Target() {
                                @Override
                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

                                    values.put(FavoriteMoviesEntry.COLUMN_MOVIE_POSTER, out.toByteArray());
                                }

                                @Override
                                public void onBitmapFailed(Drawable errorDrawable) {
                                }

                                @Override
                                public void onPrepareLoad(Drawable placeHolderDrawable) {
                                }
                            });
                    // Get the content resolver and insert the new values
                    getContentResolver().insert(FavoriteMoviesEntry.CONTENT_URI, values);

                    // Change the boolean to true, to avoid repeated movies in the db
                    isSaved = true;

                    // Change the favorite button to let known the user that the movie is already
                    // saved in the favorites db.
                    favoriteBtn.setImageResource(R.drawable.ic_star);

                    Toast.makeText(DetailActivity.this, "Added to favorites", Toast.LENGTH_SHORT).show();
                } else {
                    // If the user press the button when the movie is already saved, get the correct
                    // Uri and delete it from the db
                    Uri uri = Uri.parse(FavoriteMoviesEntry.CONTENT_URI + "/" + mCurrentMovie.getId());
                    getContentResolver().delete(uri, null, null);
                    isSaved = false;
                    favoriteBtn.setImageResource(R.drawable.ic_star_border);
                }
            }
        });

        trailerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (trailerUrl != null) {
                    // Start an intent to open YouTube with the trailer url
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(trailerUrl)));
                }
            }
        });
    }


    @Override
    public void onTaskCompleted(ArrayList<Movie> movies, ArrayList<Review> reviews, String trailerKey, String trailerTitle) {
        if (trailerKey != null && trailerTitle != null) {
            // Append the trailer key to the base Youtube Url
            trailerUrl = "https://www.youtube.com/watch?v=" + trailerKey;

            // Set the trailer title to the TextView
            trailerTv.setText(trailerTitle);
        }

        if (reviews != null) {
            mReviews = reviews;
            mAdapter.swapList(reviews);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Just fetch new data when the ArrayList is empty
        if (mReviews.size() == 0) {
            // Fetch the reviews from the internet using the util class using the corresponding code
            new FetchMovies(this, this, REVIEWS_BASE_URL,
                    String.valueOf(mCurrentMovie.getId()), FetchMovies.REVIEWS_CODE).execute();
        }

        if (trailerUrl == null || trailerTv.getText().toString().isEmpty()) {
            // Fetch the trailer from the internet using the util class using the corresponding code
            new FetchMovies(DetailActivity.this, DetailActivity.this, TRAILER_BASE_URL,
                    String.valueOf(mCurrentMovie.getId()), FetchMovies.TRAILER_CODE).execute();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mReviews != null) {
            outState.putParcelableArrayList("reviews", mReviews);
        }
        if (trailerUrl != null && !trailerTv.getText().toString().isEmpty()) {
            outState.putString("trailer", trailerUrl);
            outState.putString("trailerTitle", trailerTv.getText().toString());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mReviews = savedInstanceState.getParcelableArrayList("reviews");
        trailerUrl = savedInstanceState.getString("trailer");
        trailerTv.setText(savedInstanceState.getString("trailerTitle"));
        mAdapter.swapList(mReviews);
    }
}