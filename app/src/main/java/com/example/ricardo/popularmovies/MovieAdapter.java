package com.example.ricardo.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Ricardo on 8/9/17.
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    ArrayList<Movie> mMovies;
    Context mContext;
    OnItemClickListener mOnItemCLickListener;

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

        View view = LayoutInflater.from(mContext).inflate(R.layout.recyclerview_item, parent, false);
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
            Picasso.with(mContext)
                    .load(mMovies.get(pos).getPosterUrl())
                    .into(posterImageView);
        }

        @Override
        public void onClick(View view) {
            mOnItemCLickListener.onSingleMovieClickListener(getAdapterPosition());
        }
    }
}
