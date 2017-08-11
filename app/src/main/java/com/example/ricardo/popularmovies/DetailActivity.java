package com.example.ricardo.popularmovies;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "DetailActivity";

    Movie mCurrentMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        setTitle("Movie Details");

        if (getIntent().hasExtra("movie")) {
            mCurrentMovie = getIntent().getParcelableExtra("movie");
        }

        TextView title = (TextView) findViewById(R.id.tv_title);
        ImageView poster = (ImageView) findViewById(R.id.iv_poster);
        TextView synopsis = (TextView) findViewById(R.id.tv_synopsis);
        TextView release = (TextView) findViewById(R.id.tv_release_date);
        TextView rating = (TextView) findViewById(R.id.tv_rating);

        Picasso.with(this).load(mCurrentMovie.getPosterUrl()).into(poster);

        title.setText(mCurrentMovie.getTitle());
        synopsis.setText(mCurrentMovie.getSynopsis());
        release.setText(mCurrentMovie.getReleaseDate());
        rating.setText(String.valueOf(mCurrentMovie.getRating()) + "/10");

    }
}
