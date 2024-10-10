package com.example.wattescu.fragments;


import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.transition.Slide;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;

import com.agrawalsuneet.dotsloader.loaders.CircularDotsLoader;
import com.example.wattescu.NewsFullArticleActivity;
import com.example.wattescu.R;
import com.example.wattescu.adapters.NewsHeadlinesRecycleViewAdapter;
import com.example.wattescu.news.NewsApiResponse;
import com.example.wattescu.news.NewsHeadline;
import com.example.wattescu.news.NewsRequestManager;
import com.example.wattescu.news.OnFetchDataListener;
import com.example.wattescu.news.SelectArticleListener;

import java.util.ArrayList;
import java.util.List;


public class NewsFragment extends Fragment implements SelectArticleListener {

    public static final String HEADLINE_KEY = "headline";
    private RecyclerView recyclerView;
    private CircularDotsLoader circularDotsLoader;

    public NewsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);
        initComponents(view);
        return view;
    }

    private void initComponents(View view) {
        //custom recycle view
        recyclerView = view.findViewById(R.id.fragment_news_rv_articles);
        recyclerView.setAdapter(new NewsHeadlinesRecycleViewAdapter(getContext(), new ArrayList<NewsHeadline>(),this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(),1));

        circularDotsLoader = view.findViewById(R.id.fragment_news_progress_bar);

        if (getContext() != null) {
            //progress bar enabled
            circularDotsLoader.setVisibility(View.VISIBLE);
            //news manager
            NewsRequestManager requestManager = new NewsRequestManager(getContext());
            requestManager.getNewsHeadlinse(getFetchedDataListener(view));
        }
    }

    private OnFetchDataListener<NewsApiResponse> getFetchedDataListener(View view) {
        return new OnFetchDataListener<NewsApiResponse>() {
            @Override
            public void onFetchData(List<NewsHeadline> headlineList, String response) {
                //show news
                showNews(headlineList, view);
                circularDotsLoader.setVisibility(View.GONE);
            }

            @Override
            public void onError(String response) {
                //toast
            }
        };
    }

    private void showNews(List<NewsHeadline> headlineList, View view) {
        //set adapter
        NewsHeadlinesRecycleViewAdapter adapter = new NewsHeadlinesRecycleViewAdapter(getContext(),headlineList, this);
        recyclerView.setAdapter(adapter);

    }

    @Override
    public void OnNewsClicked(NewsHeadline headline) {
        Intent intent = new Intent(getContext().getApplicationContext(), NewsFullArticleActivity.class);
        intent.putExtra(HEADLINE_KEY, headline);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(getActivity());
        startActivity(intent, options.toBundle());

    }

}