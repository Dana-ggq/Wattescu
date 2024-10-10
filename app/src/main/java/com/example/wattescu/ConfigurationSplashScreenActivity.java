package com.example.wattescu;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Explode;
import android.transition.Slide;
import android.view.Gravity;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

public class ConfigurationSplashScreenActivity extends AppCompatActivity {

    private Animation topAnimation, bottomAnimation;
    private ImageView iv;
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableTransitions();
        setContentView(R.layout.activity_configuration_splash_screen);
        initComponents();
        startNextActivity();
    }

    private void initComponents() {
        topAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.top_animation);
        bottomAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bottom_animation);
        iv = findViewById(R.id.configuration_splash_screen_iv);
        tv = findViewById(R.id.configuration_splash_screen_tv);
        //set animations
        iv.setAnimation(topAnimation);
        tv.setAnimation(bottomAnimation);
    }

    private void startNextActivity(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(ConfigurationSplashScreenActivity.this, CustomizeConfigurationActivity.class);
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(ConfigurationSplashScreenActivity.this);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent, options.toBundle());
                finish();
            }
        }, 3000);
    }

    private void enableTransitions() {
        //enable transition
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        // set transitions
        Explode explode = new Explode();
        explode.setDuration(3000);
        explode.setInterpolator(new DecelerateInterpolator());
        getWindow().setExitTransition(explode);
    }
}