package com.example.ricardo.popularmovies;

import android.content.ContentValues;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ricardo.popularmovies.data.FavoritesMoviesContract.FavoriteMoviesEntry;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivity extends AppCompatActivity {

    private Movie mCurrentMovie;

    @BindView(R.id.tv_title) TextView title;
    @BindView(R.id.iv_poster) ImageView poster;
    @BindView(R.id.tv_synopsis) TextView synopsis;
    @BindView(R.id.tv_release_date) TextView releaseDate;
    @BindView(R.id.tv_rating) TextView rating;
    @BindView(R.id.btn_favorite) Button favoriteBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        setTitle("Movie Details");

        ButterKnife.bind(this);

        if (getIntent().hasExtra("movie")) {
            mCurrentMovie = getIntent().getParcelableExtra("movie");
        }


        Picasso.with(this).load(mCurrentMovie.getPosterUrl()).into(poster);

        title.setText(mCurrentMovie.getTitle());
        synopsis.setText(mCurrentMovie.getSynopsis());
        releaseDate.setText(mCurrentMovie.getReleaseDate());
        rating.setText(String.valueOf(mCurrentMovie.getRating()) + "/10");

        favoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO using the ContentProvider, save the current movie
                ContentValues values = new ContentValues();
                values.put(FavoriteMoviesEntry.COLUMN_MOVIE_ID, mCurrentMovie.getId());
                values.put(FavoriteMoviesEntry.COLUMN_MOVIE_TITLE, mCurrentMovie.getTitle());

                getContentResolver().insert(FavoriteMoviesEntry.CONTENT_URI, values);

            }
        });

    }
}
