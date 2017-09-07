package com.example.ricardo.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.ricardo.popularmovies.data.FavoritesMoviesContract.FavoriteMoviesEntry;

/**
 * Created by Ricardo on 9/6/17.
 */

public class FavoritesContentProvider extends ContentProvider{

    public static final int CODE_FAVORITES = 100;
    public static final int CODE_FAVORITE_WITH_ID = 101;

    private FavoritesMoviesDbHelper mFavoritesDbHelper;
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher() {

        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(FavoritesMoviesContract.AUTHORITY, FavoritesMoviesContract.PATH_FAVORITES,
                CODE_FAVORITES);
        uriMatcher.addURI(FavoritesMoviesContract.AUTHORITY, FavoritesMoviesContract.PATH_FAVORITES
                + "/#", CODE_FAVORITE_WITH_ID);

        return uriMatcher;
    }



    @Override
    public boolean onCreate() {
        mFavoritesDbHelper = new FavoritesMoviesDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor;
        SQLiteDatabase db = mFavoritesDbHelper.getReadableDatabase();

        int code = sUriMatcher.match(uri);
        switch (code) {
            case CODE_FAVORITES:
                cursor = db.query(
                        FavoriteMoviesEntry.TABLE_NAME,
                        projection,
                        FavoriteMoviesEntry.COLUMN_MOVIE_ID + " = ?",
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            case CODE_FAVORITE_WITH_ID:
                cursor = db.query(
                        FavoriteMoviesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);

        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {

        final SQLiteDatabase db = mFavoritesDbHelper.getWritableDatabase();

        int code = sUriMatcher.match(uri);
        Uri returnedUri;

        switch (code) {
            case CODE_FAVORITES:
                long id = db.insert(FavoriteMoviesEntry.TABLE_NAME, null, contentValues);
                if (id > 0) {
                    returnedUri = ContentUris.withAppendedId(FavoriteMoviesEntry.CONTENT_URI, id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);

        return returnedUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {

        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
