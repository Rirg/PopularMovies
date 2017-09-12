package com.example.ricardo.popularmovies.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.ricardo.popularmovies.R;
import com.example.ricardo.popularmovies.pojos.Movie;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Ricardo on 8/9/17.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private ArrayList<Movie> mMovies;
    private Context mContext;
    private OnItemClickListener mOnItemCLickListener;

    public interface OnItemClickListener {
        void onSingleMovieClickListener(int pos);
    }

    public MovieAdapter(Context context, ArrayList<Movie> movies, OnItemClickListener onItemClickListener) {
        mContext = context;
        mMovies = movies;
        mOnItemCLickListener = onItemClickListener;
    }

    @Override
    public MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.rv_movies_item, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        if (mMovies == null) return 0;
        return mMovies.size();
    }

    class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView posterImageView;

        public MovieViewHolder(View itemView) {
            super(itemView);

            posterImageView = itemView.findViewById(R.id.poster_image_view);
            itemView.setOnClickListener(this);

        }

        void bind(int pos) {
            if (mMovies.get(pos).getPosterUrl() != null) {
                Picasso.with(mContext)
                        .load(mMovies.get(pos).getPosterUrl())
                        .placeholder(R.drawable.poster_placeholder)
                        .into(posterImageView);
            } else {
                byte[] bitmapdata = mMovies.get(pos).getBlobPoster();
                Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length);
                posterImageView.setImageBitmap(bitmap);
            }
        }

        @Override
        public void onClick(View view) {
            mOnItemCLickListener.onSingleMovieClickListener(getAdapterPosition());
        }
    }
}
