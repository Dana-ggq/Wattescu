package com.example.wattescu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.agrawalsuneet.dotsloader.loaders.CircularDotsLoader;
import com.example.wattescu.entities.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    //views
    ImageView ivRobot, ivBulb;
    TextView tvh1, tvSecodary, tvCont;
    TextInputLayout tilEmail, tilUsername, tilPassword;
    TextInputEditText tietEmail, tietUsername, tietPassword;
    CircularDotsLoader progressBar;
    FloatingActionButton fabNext;
    Button btnLogin;

    //initiate firebase authentification
    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        //hooks for views
        initComponents();

        //firebase authentification
        firebaseAuth = FirebaseAuth.getInstance();
    }

    private void initComponents() {
        ivRobot =findViewById(R.id.signup_iv_robot);
        ivBulb =findViewById(R.id.signup_iv_bulb);
        tvh1 = findViewById(R.id.signup_tv_h1);
        tvSecodary = findViewById(R.id.signup_tv_h3);
        tvCont = findViewById(R.id.signup_tv_ai_cont);
        tilEmail = findViewById(R.id.signup_til_mail);
        tilPassword = findViewById(R.id.signup_til_password);
        tilUsername = findViewById(R.id.signup_til_username);
        fabNext = findViewById(R.id.signup_fab_next);
        btnLogin = findViewById(R.id.signup_btn_login);
        tietUsername = findViewById(R.id.signup_tiet_username);
        tietPassword = findViewById(R.id.signup_tiet_password);
        tietEmail =findViewById(R.id.signup_tiet_mail);
        progressBar =findViewById(R.id.signup_progress_bar);

        initEvents();
    }

    private void initEvents() {
        btnLogin.setOnClickListener(getLoginListener());
        //inform user on input format
        tietUsername.setOnFocusChangeListener(getTilUsernameFoucusedChangedListener());
        tietPassword.addTextChangedListener(getTietTextChangedListener());

        fabNext.setOnClickListener(getRegisterUserListener());
    }

    private TextWatcher getTietTextChangedListener() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(tietPassword.getText() == null || tietPassword.getText().toString().trim().length() < 6) {
                    tilPassword.setError("Parola trebuie sa contina minim 6 caractere.");
                } else{
                    tilPassword.setErrorEnabled(false);
                }
            }
        };
    }

    private View.OnClickListener getRegisterUserListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    if(isValid()){
                        progressBar.setVisibility(View.VISIBLE);
                        firebaseAuth.createUserWithEmailAndPassword(tietEmail.getText().toString().trim(),tietPassword.getText().toString().trim())
                        .addOnCompleteListener(getCompleteSignUpListener());
                    }
            }
        };
    }

    private OnCompleteListener<AuthResult> getCompleteSignUpListener() {
        return new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        insertUserInFBDatabase();
                    }else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        registrationStatusMessage(R.string.msg_signup_emailAlreadyExists, View.GONE);
                    } else{
                        registrationStatusMessage(R.string.msg_signup_failedRegistration, View.GONE);
                    }
            }
        };
    }

    private void insertUserInFBDatabase() {
        User user = new User(tietEmail.getText().toString().trim(),
                tietUsername.getText().toString().trim(), false);

        String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        user.setId(id);
        FirebaseDatabase.getInstance().getReference("users")
                .child(id)
                .setValue(user)
                .addOnCompleteListener(getCompleteInsertUserInDbListener());
    }

    private void registrationStatusMessage(int p, int gone) {
        //inregistrarea nu a avut loc cu succes
        Toast toast = new Toast(getApplicationContext());
        editToast(toast, getString(p));
        toast.show();
        progressBar.setVisibility(gone);
    }

    private OnCompleteListener<Void> getCompleteInsertUserInDbListener() {
        return new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    firebaseAuth.getCurrentUser().sendEmailVerification();
                    registrationStatusMessage(R.string.msg_singup_succesfullRegistration, View.INVISIBLE);
                    finish();
                }else{
                    //inregistrarea nu a avut loc cu succes
                    registrationStatusMessage(R.string.msg_signup_failedRegistration, View.GONE);
                }
            }
        };
    }

    private boolean isValid() {
        //email adr correct format; UNIQUE IN BD
        if (tietEmail.getText() == null || tietEmail.getText().toString().trim().length()==0 ||
                !Patterns.EMAIL_ADDRESS.matcher(tietEmail.getText().toString().trim()).matches()) {
            Toast toast = new Toast(getApplicationContext());
            editToast(toast, getString(R.string.err_msg_singup_email));
            toast.show();
            return false;
        }
        //username maximum 15 chars;
        if (tietUsername.getText() == null || tietUsername.getText().toString().trim().length()==0 ||
                tietUsername.getText().toString().trim().length()>15) {

            Toast toast = new Toast(getApplicationContext());
            editToast(toast, getString(R.string.err_msg_signup_username));
            toast.show();

            return false;
        }
        //password minimum 6 chars
        if (tietPassword.getText() == null || tietPassword.getText().toString().trim().length()==0 ||
                tilPassword.getError()!=null) {
            Toast toast = new Toast(getApplicationContext());
            editToast(toast, getString(R.string.err_msg_signup_password));
            toast.show();
            return false;
        }
        return true;
    }

    private void editToast(Toast toast, String p) {
        View toast_view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.toast_bottom_layout, null);
        ((TextView) toast_view.findViewById(R.id.toast_layout_tv)).setText(p);
        toast.setView(toast_view);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM, 0, 0);
    }

    private View.OnFocusChangeListener getTilUsernameFoucusedChangedListener() {
        return new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(tietUsername.isFocused()){
                    tilUsername.setCounterEnabled(true);
                } else{
                    tilUsername.setCounterEnabled(false);
                }
            }
        };
    }

    private View.OnClickListener getLoginListener() {
        return  new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();

                //animation code
                Pair[] pairs = new Pair[10];
                pairs[0] = new Pair<View, String>(ivRobot, getResources().getString(R.string.animation_logo_image));
                pairs[1] = new Pair<View, String>(ivBulb, getResources().getString(R.string.animation_logo_image_bulb));
                pairs[2] = new Pair<View, String>(tvh1, getResources().getString(R.string.animation_text_h1));
                pairs[3] = new Pair<View, String>(tvSecodary, getResources().getString(R.string.animation_secondary_text));
                pairs[4] = new Pair<View, String>(tvCont, getResources().getString(R.string.animation_new_user));
                pairs[5] = new Pair<View, String>(tilUsername, getResources().getString(R.string.animation_password));
                pairs[6] = new Pair<View, String>(tilPassword, getResources().getString(R.string.animation_forgot_password));
                pairs[7] = new Pair<View, String>(btnLogin, getResources().getString(R.string.animation_sign_up));
                pairs[8] = new Pair<View, String>(fabNext, getResources().getString(R.string.animation_button_next));
                pairs[9] = new Pair<View, String>(tilEmail, getResources().getString(R.string.animation_username));

                ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(SignUpActivity.this, pairs);
                startActivity(intent, activityOptions.toBundle());
                finish();
            }
        };
    }
}