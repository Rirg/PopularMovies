package com.example.ricardo.popularmovies.pojos;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Ricardo on 9/11/17.
 */

public class Review implements Parcelable {

    String author;
    String body;

    public Review(String author, String body) {
        this.author = author;
        this.body = body;
    }

    protected Review(Parcel in) {
        author = in.readString();
        body = in.readString();
    }

    public static final Creator<Review> CREATOR = new Creator<Review>() {
        @Override
        public Review createFromParcel(Parcel in) {
            return new Review(in);
        }

        @Override
        public Review[] newArray(int size) {
            return new Review[size];
        }
    };

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(author);
        parcel.writeString(body);
    }
}
