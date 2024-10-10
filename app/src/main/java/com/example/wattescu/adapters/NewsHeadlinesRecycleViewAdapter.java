package com.example.wattescu.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wattescu.R;
import com.example.wattescu.news.NewsHeadline;
import com.example.wattescu.news.SelectArticleListener;
import com.squareup.picasso.Picasso;

import java.util.List;

public class NewsHeadlinesRecycleViewAdapter extends RecyclerView.Adapter<NewsHeadlinesRecycleViewHolder> {

    private Context context;
    private List<NewsHeadline> newsHeadlines;
    private SelectArticleListener selectArticleListener;

    public NewsHeadlinesRecycleViewAdapter(Context context, List<NewsHeadline> newsHeadlines, SelectArticleListener listener) {
        this.context = context;
        this.newsHeadlines = newsHeadlines;
        this.selectArticleListener = listener;
    }

    @NonNull
    @Override
    public NewsHeadlinesRecycleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NewsHeadlinesRecycleViewHolder(LayoutInflater.from(context).inflate(R.layout.rv_row_headline_articles, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NewsHeadlinesRecycleViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.getTvTitle().setText(newsHeadlines.get(position).getTitle());
        holder.getTvSource().setText(newsHeadlines.get(position).getSource().getName());
        try {
            if (newsHeadlines.get(position).getUrlToImage() != null || !newsHeadlines.get(position).getUrlToImage().isEmpty()) {
                Picasso.get().load(newsHeadlines.get(position).getUrlToImage()).into(holder.getIvNews());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        holder.getCvNews().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectArticleListener.OnNewsClicked(newsHeadlines.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return newsHeadlines == null ? 0 : newsHeadlines.size();
    }
}
