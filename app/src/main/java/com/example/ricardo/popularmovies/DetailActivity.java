package com.example.ricardo.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ricardo.popularmovies.adapters.ReviewsAdapter;
import com.example.ricardo.popularmovies.data.FavoritesMoviesContract.FavoriteMoviesEntry;
import com.example.ricardo.popularmovies.pojos.Movie;
import com.example.ricardo.popularmovies.pojos.Review;
import com.example.ricardo.popularmovies.utils.FetchMovies;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity implements FetchMovies.OnTaskCompleted {

    private Movie mCurrentMovie;

    @BindView(R.id.tv_title) TextView title;
    @BindView(R.id.iv_poster) ImageView poster;
    @BindView(R.id.tv_synopsis) TextView synopsis;
    @BindView(R.id.tv_release_date) TextView releaseDate;
    @BindView(R.id.tv_rating) TextView rating;
    @BindView(R.id.btn_favorite) ImageButton favoriteBtn;
    @BindView(R.id.ib_trailer) ImageButton trailerBtn;
    @BindView(R.id.tv_trailer_title) TextView trailerTv;

    private static final String TAG = "DetailActivity";
    boolean isSaved = false;
    String trailerUrl;
    ArrayList<Review> mReviews;
    ReviewsAdapter mAdapter;

    public static final String TRAILER_BASE_URL = "https://api.themoviedb.org/3/movie/%s/videos?api_key=ef83058e91d65966e65b63151aaaf75c";
    public static final String REVIEWS_BASE_URL = "https://api.themoviedb.org/3/movie/%s/reviews?api_key=ef83058e91d65966e65b63151aaaf75c";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        setTitle("Movie Details");

        ButterKnife.bind(this);

        mReviews = new ArrayList<>();
        mAdapter = new ReviewsAdapter(this, mReviews);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_reviews);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mAdapter);


        if (getIntent().hasExtra("movie")) {
            mCurrentMovie = getIntent().getParcelableExtra("movie");
        }

        new FetchMovies(this, this, REVIEWS_BASE_URL, String.valueOf(mCurrentMovie.getId()), 200).execute();

        final Uri uri = Uri.parse(FavoriteMoviesEntry.CONTENT_URI + "/" + mCurrentMovie.getId());
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor.getCount() > 0) {
            isSaved = true;
            favoriteBtn.setImageResource(R.drawable.ic_favorite_black);
        }
        cursor.close();

        new FetchMovies(DetailActivity.this, DetailActivity.this, TRAILER_BASE_URL, String.valueOf(mCurrentMovie.getId()), 300).execute();

        Picasso.with(this).load(mCurrentMovie.getPosterUrl()).into(poster);

        title.setText(mCurrentMovie.getTitle());
        synopsis.setText(mCurrentMovie.getSynopsis());
        releaseDate.setText(mCurrentMovie.getReleaseDate());
        rating.setText(String.valueOf(mCurrentMovie.getRating()) + "/10");

        favoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isSaved) {
                    ContentValues values = new ContentValues();
                    values.put(FavoriteMoviesEntry.COLUMN_MOVIE_ID, mCurrentMovie.getId());
                    values.put(FavoriteMoviesEntry.COLUMN_MOVIE_TITLE, mCurrentMovie.getTitle());

                    getContentResolver().insert(FavoriteMoviesEntry.CONTENT_URI, values);
                    isSaved = true;
                    favoriteBtn.setImageResource(R.drawable.ic_favorite_black);
                } else {
                    Uri uri = Uri.parse(FavoriteMoviesEntry.CONTENT_URI + "/" + mCurrentMovie.getId());
                    getContentResolver().delete(uri, null, null);
                    isSaved = false;
                    favoriteBtn.setImageResource(R.drawable.ic_favorite_border_black);
                }
            }
        });

        trailerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "onClick: " + trailerUrl);
                if (trailerUrl != null) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(trailerUrl)));
                }
            }
        });
    }

    @Override
    public void onTaskCompleted(Movie movie, Review review, String trailerKey, String trailerTitle) {
        if (trailerKey != null) {
            trailerUrl = "https://www.youtube.com/watch?v=" + trailerKey;
            trailerTv.setText(trailerTitle);
        }

        if (review != null) {
            mReviews.add(review);
            mAdapter.notifyDataSetChanged();
        }
    }
}