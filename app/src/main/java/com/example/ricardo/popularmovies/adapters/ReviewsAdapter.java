package com.example.ricardo.popularmovies.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.ricardo.popularmovies.R;
import com.example.ricardo.popularmovies.pojos.Review;

import java.util.ArrayList;

/**
 * Created by Ricardo on 9/11/17.
 */

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewsViewHolder> {

    private ArrayList<Review> mReviews;
    private Context mContext;

    public ReviewsAdapter(Context context, ArrayList<Review> reviews) {
        mContext = context;
        mReviews = reviews;
    }


    @Override
    public ReviewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.rv_reviews_item, parent, false);
        return new ReviewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReviewsViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        if (mReviews == null) return 0;
        return mReviews.size();
    }

    class ReviewsViewHolder extends RecyclerView.ViewHolder {

        TextView authorTextView;
        TextView bodyTextView;

        public ReviewsViewHolder(View itemView) {
            super(itemView);

            authorTextView = itemView.findViewById(R.id.tv_review_author);
            bodyTextView = itemView.findViewById(R.id.tv_review_body);
        }

        void bind(int pos) {
            authorTextView.setText(mReviews.get(pos).getAuthor());
            bodyTextView.setText(mReviews.get(pos).getBody());
        }
    }

    public void swapList(ArrayList<Review> reviews) {
        if (reviews != null) {
            mReviews = reviews;
            notifyDataSetChanged();
        }
    }

}

