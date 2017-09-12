package com.example.ricardo.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.ricardo.popularmovies.data.FavoritesMoviesContract.FavoriteMoviesEntry;

/**
 * Created by Ricardo on 9/6/17.
 */

public class FavoritesMoviesDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "movies.db";
    public static final int DATABASE_VERSION = 1;

    public FavoritesMoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String SQL_CREATE_FAVORITE_MOVIES_TABLE = "CREATE TABLE " +
                FavoriteMoviesEntry.TABLE_NAME + " (" +
                FavoriteMoviesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                FavoriteMoviesEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                FavoriteMoviesEntry.COLUMN_MOVIE_TITLE + " TEXT NOT NULL, " +
                FavoriteMoviesEntry.COLUMN_MOVIE_POSTER + " BLOB, " +
                FavoriteMoviesEntry.COLUMN_SYNOPSIS + " TEXT, " +
                FavoriteMoviesEntry.COLUMN_RATING + " INTEGER, " +
                FavoriteMoviesEntry.COLUMN_RELEASE_DATE + " TEXT" +
                ");";

        sqLiteDatabase.execSQL(SQL_CREATE_FAVORITE_MOVIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FavoriteMoviesEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
