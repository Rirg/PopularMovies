package com.example.ricardo.popularmovies.data;

import android.provider.BaseColumns;

/**
 * Created by Ricardo on 9/6/17.
 */

public class FavoriteMoviesContract {

    public static final class FavoriteMoviesEntry implements BaseColumns {

        public static final String TABLE_NAME = "favorites";
        public static final String COLUMN_MOVIE_ID = "movieId";
        public static final String COLUMN_MOVIE_TITLE = "movieTitle";
    }
}
