package com.example.wattescu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;

import android.app.ActivityOptions;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wattescu.fragments.NewsFragment;
import com.example.wattescu.news.NewsHeadline;
import com.squareup.picasso.Picasso;

public class NewsFullArticleActivity extends AppCompatActivity {

    private NewsHeadline headline;
    private ImageView ivHeadline;
    private TextView tvTitle, tvAuthor, tvTime, tvDescription, tvContent, tvLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableTransitions();
        setContentView(R.layout.activity_news_full_article);
        //get headline form intent
        if(!getIntent().hasExtra(NewsFragment.HEADLINE_KEY)) {
            finish();
        }
        headline = (NewsHeadline) getIntent().getSerializableExtra(NewsFragment.HEADLINE_KEY);
        initComponents();
    }

    private void initComponents() {
        ivHeadline = findViewById(R.id.news_full_article_iv_news_image);
        tvTitle = findViewById(R.id.news_full_article_tv_title);
        tvAuthor = findViewById(R.id.news_full_article_tv_author);
        tvTime = findViewById(R.id.news_full_article_tv_time);
        tvDescription =findViewById(R.id.news_full_article_tv_description);
        tvContent = findViewById(R.id.news_full_article_tv_content);
        tvLink = findViewById(R.id.news_full_article_tv_link);
        createViewsFromIntent();
    }

    private void createViewsFromIntent() {
        tvTitle.setText(headline.getTitle());
        tvAuthor.setText(headline.getAuthor());
        tvTime.setText(headline.getPublishedAt());
        tvDescription.setText(headline.getDescription());
        tvContent.setText(headline.getContent().split("\\[")[0]);
        Picasso.get().load(headline.getUrlToImage()).into(ivHeadline);
        tvLink.setText(headline.getUrl());
        tvLink.setOnClickListener(getGoToLinkListener());
    }

    private View.OnClickListener getGoToLinkListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(headline.getUrl());
                startActivity(new Intent(Intent.ACTION_VIEW, uri));

            }
        };
    }


    private void enableTransitions() {
        //enable transition
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        // set transitions
        Slide slide = new Slide();
        slide.setSlideEdge(Gravity.RIGHT);
        slide.setInterpolator(new DecelerateInterpolator());
        getWindow().setEnterTransition(slide);
        getWindow().setExitTransition(slide);

    }


}