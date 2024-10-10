package com.example.wattescu.adapters;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wattescu.R;

public class NewsHeadlinesRecycleViewHolder extends RecyclerView.ViewHolder {

    //views
    private TextView tvTitle, tvSource;
    private ImageView ivNews;
    private CardView cvNews;

    public NewsHeadlinesRecycleViewHolder(@NonNull View itemView) {
        super(itemView);
        //hooks
        initComponents(itemView);
    }

    public TextView getTvTitle() {
        return tvTitle;
    }

    public void setTvTitle(TextView tvTitle) {
        this.tvTitle = tvTitle;
    }

    public TextView getTvSource() {
        return tvSource;
    }

    public void setTvSource(TextView tvSource) {
        this.tvSource = tvSource;
    }

    public ImageView getIvNews() {
        return ivNews;
    }

    public void setIvNews(ImageView ivNews) {
        this.ivNews = ivNews;
    }

    public CardView getCvNews() {
        return cvNews;
    }

    public void setCvNews(CardView cvNews) {
        this.cvNews = cvNews;
    }

    private void initComponents(@NonNull View itemView) {
        tvTitle = itemView.findViewById(R.id.rv_row_headline_articles_tv_title);
        tvSource = itemView.findViewById(R.id.rv_row_headline_articles_tv_source);
        ivNews = itemView.findViewById(R.id.rv_row_headline_articles_iv_image);
        cvNews = itemView.findViewById(R.id.cv_articles_container);
    }
}
