package com.example.ricardo.popularmovies;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
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

    private static final String TAG = "DetailActivity";
    boolean isSaved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        setTitle("Movie Details");

        ButterKnife.bind(this);

        if (getIntent().hasExtra("movie")) {
            mCurrentMovie = getIntent().getParcelableExtra("movie");
        }
        final Uri uri = Uri.parse(FavoriteMoviesEntry.CONTENT_URI + "/" + mCurrentMovie.getId());
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor.getCount() > 0) isSaved = true;
        cursor.close();

        Picasso.with(this).load(mCurrentMovie.getPosterUrl()).into(poster);

        title.setText(mCurrentMovie.getTitle());
        synopsis.setText(mCurrentMovie.getSynopsis());
        releaseDate.setText(mCurrentMovie.getReleaseDate());
        rating.setText(String.valueOf(mCurrentMovie.getRating()) + "/10");

        favoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO using the ContentProvider, save the current movie
                // long id = cursor.getLong(cursor.getColumnIndex(FavoriteMoviesEntry.COLUMN_MOVIE_ID));
                //Log.i(TAG, "onClick: " + id);
                if (!isSaved) {
                    ContentValues values = new ContentValues();
                    values.put(FavoriteMoviesEntry.COLUMN_MOVIE_ID, mCurrentMovie.getId());
                    values.put(FavoriteMoviesEntry.COLUMN_MOVIE_TITLE, mCurrentMovie.getTitle());

                    getContentResolver().insert(FavoriteMoviesEntry.CONTENT_URI, values);
                    isSaved = true;
                }



            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
