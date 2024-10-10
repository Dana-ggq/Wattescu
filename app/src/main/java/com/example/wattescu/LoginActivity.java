package com.example.wattescu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Explode;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.agrawalsuneet.dotsloader.loaders.CircularDotsLoader;
import com.example.wattescu.firebase.FirebaseCurrentConfigurationService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    //views
    Button btnSignup;
    ImageView ivRobot, ivBulb;
    TextView tvh1, tvh2, tvSecondary, tvNewUser;
    TextInputLayout tilEmail, tilPassword;
    Button btnForgotPass;
    FloatingActionButton fabNext;
    TextInputEditText tietEmail, tietPassword;
    CircularDotsLoader progressBar;

    //firebase authentification
    private FirebaseAuth firebaseAuth;

    //current configuration
    private FirebaseCurrentConfigurationService firebaseCurrentConfigurationService;

    //app keeps user logged
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //end activity with transition
        enableTransitions();
        setContentView(R.layout.activity_login);

        //initialize firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
        //initialize sharedprefs
        preferences = getSharedPreferences(SplashScreenActivity.LOGIN_DETAILS,MODE_PRIVATE);
        //hooks
        initComponents();

    }

    private void enableTransitions() {
        //enable transition
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        // set transitions
        Explode explode = new Explode();
        explode.setDuration(1000);
        explode.setInterpolator(new DecelerateInterpolator());
        getWindow().setExitTransition(explode);
    }

    private void initComponents() {
        btnSignup = findViewById(R.id.login_btn_sing_up);
        ivRobot = findViewById(R.id.login_iv_robot);
        ivBulb = findViewById(R.id.login_iv_bulb);
        tvh1 = findViewById(R.id.login_tv_h1);
        tvh2 = findViewById(R.id.login_tv_h2);
        tvSecondary = findViewById(R.id.login_tv_h3);
        tvNewUser = findViewById(R.id.login_tv_utilizator_nou);
        tilEmail = findViewById(R.id.login_til_email);
        tilPassword = findViewById(R.id.login_til_password);
        btnForgotPass = findViewById(R.id.login_btn_forgot_password);
        fabNext = findViewById(R.id.login_fab_next);
        tietEmail = findViewById(R.id.login_tiet_user);
        tietPassword = findViewById(R.id.login_tiet_password);
        progressBar =findViewById(R.id.login_progress_bar);

        initEvents();
    }

    private void initEvents() {
        btnSignup.setOnClickListener(getSignUpListener());
        fabNext.setOnClickListener(getLoginListener());
        btnForgotPass.setOnClickListener(getForgotPasswordListener());
    }

    protected View.OnClickListener getForgotPasswordListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //alert dialog for typing email address
                View dialogView = getLayoutInflater().inflate(R.layout.reset_password_dialog,null);
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                customizeDialog(dialogView, builder);

                AlertDialog dialog = builder.create();
                dialog.show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getColor(R.color.dark_gray));
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                }

                EditText etEmail = dialogView.findViewById(R.id.reset_password_dialog_et_email);
                etEmail.addTextChangedListener(getResetPasswordDialogWatcher(dialog, etEmail));
            }
        };
    }

    private void customizeDialog(View dialogView, AlertDialog.Builder builder) {
        builder.setView(dialogView)
                .setTitle(R.string.login_alert_dialog_reset_password_title)
                .setPositiveButton(R.string.login_dialog_reset_password_ok, getResetPasswordPositiveListener(dialogView))
                .setNegativeButton(R.string.login_dialog_reset_password_cancel, getResetPasswordNegativeListener());
    }

    @NonNull
    private TextWatcher getResetPasswordDialogWatcher(AlertDialog dialog, EditText etEmail) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (etEmail.getText().toString().trim().length() > 4) {
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                } else {
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                }
            }
        };
    }


    private DialogInterface.OnClickListener getResetPasswordNegativeListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                    //nothin happens
            }
        };
    }

    private DialogInterface.OnClickListener getResetPasswordPositiveListener(View dialogView) {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                    //get email address from view
                EditText etEmail = dialogView.findViewById(R.id.reset_password_dialog_et_email);
                if(etEmail.getText().toString().trim().isEmpty()){
                    dialog.cancel();
                }else{
                    firebaseAuth.sendPasswordResetEmail(etEmail.getText().toString().trim())
                            .addOnCompleteListener(getResetPasswordFirebaseCompleteListener());}
            }
        };
    }

    private OnCompleteListener<Void> getResetPasswordFirebaseCompleteListener() {
        return new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast toast = new Toast(getApplicationContext());
                        editToast(toast, getString(R.string.login_link_send_on_email));
                        toast.show();
                    } else {
                        Toast toast = new Toast(getApplicationContext());
                        editToast(toast, getString(R.string.login_dialog_reset_password_error));
                        toast.show();
                    }
            }
        };
    }

    private View.OnClickListener getLoginListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isValid()){
                    progressBar.setVisibility(View.VISIBLE);
                    //login the user
                    firebaseAuth.signInWithEmailAndPassword(tietEmail.getText().toString().trim(),
                            tietPassword.getText().toString().trim())
                    .addOnCompleteListener(getCompletedFirebaseLoginListener());

                }

            }
        };
    }

    private OnCompleteListener<AuthResult> getCompletedFirebaseLoginListener() {
        return new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        //check if email has been verified
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if(user.isEmailVerified()){
                            //set verified email in DB
                            String  id = firebaseAuth.getCurrentUser().getUid();
                            FirebaseDatabase.getInstance().getReference("users")
                                    .child(id).child("verified").setValue(true);
                            //create current configuration if not exists
                            try {
                                firebaseCurrentConfigurationService = FirebaseCurrentConfigurationService.getInstance(id);
                                firebaseCurrentConfigurationService.createCurrentConfiguration();
                            } catch (Exception exception) {
                                exception.printStackTrace();
                            }
                            //save users credentials to keep them logged in
                            saveCredentialsToSharedPreferences();
                            redirectUserToProfile();
                        } else {
                            resendVerificationEmail(user);
                        }
                    }else{
                        failedToLogin(R.string.msg_login_err_wrongCredentials);
                    }
            }
        };
    }

    private void saveCredentialsToSharedPreferences() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SplashScreenActivity.EMAIL, firebaseAuth.getCurrentUser().getEmail());
        editor.putString(SplashScreenActivity.PASSWORD, tietPassword.getText().toString().trim());
        editor.apply();

    }

    private void redirectUserToProfile() {
        //redirect to user profile
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        //start activity
        startActivity(intent,
                ActivityOptions.makeSceneTransitionAnimation(LoginActivity.this).toBundle());
        finish();
    }

    private void failedToLogin(int p) {
        Toast toast = new Toast(getApplicationContext());
        editToast(toast, getString(p));
        toast.show();
        progressBar.setVisibility(View.GONE);
    }

    private void resendVerificationEmail(FirebaseUser user) {
        user.sendEmailVerification();
        Toast toast = new Toast(getApplicationContext());
        editToast(toast, getString(R.string.msg_login_verify_account));
        toast.show();
        progressBar.setVisibility(View.GONE);
    }

    private void editToast(Toast toast, String p) {
        View toast_view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.toast_bottom_layout, null);
        ((TextView) toast_view.findViewById(R.id.toast_layout_tv)).setText(p);
        toast.setView(toast_view);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM, 0, 0);
    }

    private boolean isValid() {
        //email provided
        if (checkTietValueIsProvided(tietEmail, tilEmail, R.string.msg_login_err_email)) return false;
        if (checkTietValueIsProvided(tietPassword, tilPassword, R.string.msg_login_err_password)) return false;
        return true;
    }

    private boolean checkTietValueIsProvided(TextInputEditText textInputEditText, TextInputLayout textInputLayout, int p) {
        if (textInputEditText.getText() == null || textInputEditText.getText().toString().trim().length() == 0) {
            textInputLayout.setError(getString(p));
            textInputLayout.requestFocus();
            return true;
        } else {
            //delete error
            textInputLayout.setErrorEnabled(false);
        }
        return false;
    }

    private View.OnClickListener getSignUpListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);

                //animation code
                Pair[] pairs = getAnimationForScreenChangePairs();

                ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(LoginActivity.this, pairs);
                startActivity(intent, activityOptions.toBundle());
                //finish();
            }
        };
    }

    @NonNull
    private Pair[] getAnimationForScreenChangePairs() {
        Pair[] pairs = new Pair[11];
        pairs[0] = new Pair<View, String>(ivRobot, getResources().getString(R.string.animation_logo_image));
        pairs[1] = new Pair<View, String>(ivBulb, getResources().getString(R.string.animation_logo_image_bulb));
        pairs[2] = new Pair<View, String>(tvh1, getResources().getString(R.string.animation_text_h1));
        pairs[3] = new Pair<View, String>(tvh2, getResources().getString(R.string.animation_text_h1));
        pairs[4] = new Pair<View, String>(tvSecondary, getResources().getString(R.string.animation_secondary_text));
        pairs[5] = new Pair<View, String>(tvNewUser, getResources().getString(R.string.animation_new_user));
        pairs[6] = new Pair<View, String>(tilEmail, getResources().getString(R.string.animation_username));
        pairs[7] = new Pair<View, String>(tilPassword, getResources().getString(R.string.animation_password));
        pairs[8] = new Pair<View, String>(btnSignup, getResources().getString(R.string.animation_sign_up));
        pairs[9] = new Pair<View, String>(fabNext, getResources().getString(R.string.animation_button_next));
        pairs[10] = new Pair<View, String>(btnForgotPass, getResources().getString(R.string.animation_forgot_password));
        return pairs;
    }
}