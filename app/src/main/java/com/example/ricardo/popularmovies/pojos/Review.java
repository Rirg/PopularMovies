package com.example.ricardo.popularmovies.pojos;

/**
 * Created by Ricardo on 9/11/17.
 */

public class Review {

    String author;
    String body;

    public Review(String author, String body) {
        this.author = author;
        this.body = body;
    }

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
}
