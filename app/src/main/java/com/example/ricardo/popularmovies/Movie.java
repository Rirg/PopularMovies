package com.example.ricardo.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Ricardo on 8/10/17.
 */

public class Movie implements Parcelable {

    private long id;
    private String title;
    private String posterUrl;
    private String synopsis;
    private double rating;
    private String releaseDate;

    public Movie() {
    }

    public Movie(long id, String title, String posterUrl, String synopsis, double rating, String releaseDate) {
        this.id = id;
        this.title = title;
        this.posterUrl = posterUrl;
        this.synopsis = synopsis;
        this.rating = rating;
        this.releaseDate = releaseDate;
    }

    protected Movie(Parcel in) {
        id = in.readLong();
        title = in.readString();
        posterUrl = in.readString();
        synopsis = in.readString();
        rating = in.readDouble();
        releaseDate = in.readString();
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(title);
        parcel.writeString(posterUrl);
        parcel.writeString(synopsis);
        parcel.writeDouble(rating);
        parcel.writeString(releaseDate);

    }
    public static Creator<Movie> getCREATOR() {
        return CREATOR;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
