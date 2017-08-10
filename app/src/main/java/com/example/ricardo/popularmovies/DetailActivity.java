package com.example.ricardo.popularmovies;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "DetailActivity";

    Movie mCurrentMovie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);


        if (getIntent().hasExtra("movie")) {
            mCurrentMovie = getIntent().getParcelableExtra("movie");
            Log.i(TAG, "onCreate: " + mCurrentMovie.getTitle());
        }
    }
}
