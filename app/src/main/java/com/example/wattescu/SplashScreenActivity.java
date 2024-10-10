package com.example.wattescu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SplashScreenActivity extends AppCompatActivity {

    //constants for shared pref
    public static final String LOGIN_DETAILS = "login_details";
    public static final String PASSWORD = "password";
    public static final String EMAIL = "email";

    //animations
    private Animation topAnimation, bottomAnimation;
    private ImageView imageView;
    private TextView textView;
    private ImageView imageViewBulb;
    private Pair[] pairs;

    //animation duration
    public static final int  DURATION_SPLASH_SCREEN = 3000;

    //app keeps users logged in authomatically
    private SharedPreferences preferences;
    private String password, email;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(LOGIN_DETAILS,MODE_PRIVATE);
        firebaseAuth = FirebaseAuth.getInstance();
        getCredentials();
        initComponents();
        StartMainActivity();
    }

    private void getCredentials() {
        password = preferences.getString(PASSWORD, "");
        email = preferences.getString(EMAIL,"");
    }

    private  void initComponents(){
        //remove action bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash_screen);
        //hooks
        topAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.top_animation);
        bottomAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bottom_animation);
        imageView = findViewById(R.id.splash_iw_robot);
        textView = findViewById(R.id.tw_splash_wattescu);
        imageViewBulb = findViewById(R.id.splash_iw_bulb);
        //set animations
        imageView.setAnimation(bottomAnimation);
        imageViewBulb.setAnimation(topAnimation);
        textView.setAnimation(bottomAnimation);

    }

    private void StartMainActivity(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                pairs = new Pair[2];
                pairs[0] = new Pair<View, String>(imageView, getResources().getString(R.string.animation_logo_image));
                pairs[1] = new Pair<View, String>(imageViewBulb, getResources().getString(R.string.animation_logo_image_bulb));

                if(password == null || email == null ||
                        password.equals("") || password.isEmpty() || email.equals("") || email.isEmpty()) {
                    //start login activity
                    startLoginActivity();
                } else{
                    //login user
                    firebaseAuth.signInWithEmailAndPassword(email,password)
                            .addOnCompleteListener(getCompletedFirebaseLoginListener());
                }

            }
        }, DURATION_SPLASH_SCREEN);
    }

    private void startLoginActivity() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(SplashScreenActivity.this, pairs);
        startActivity(intent, options.toBundle());
        finish();
    }

    private OnCompleteListener<AuthResult> getCompletedFirebaseLoginListener() {
        return new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    //user has logged in at least once so no validations are needed
                    //redirect to main activity
                    startMainActivity();
                }else{
                    //there was a problem so the user shall be redirected to classic login page
                    startLoginActivity();
                }
            }
        };
    }

    private void startMainActivity() {
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(intent);
        finish();
    }
}