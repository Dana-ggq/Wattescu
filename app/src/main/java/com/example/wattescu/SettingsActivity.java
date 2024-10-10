package com.example.wattescu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Slide;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wattescu.async.Callback;
import com.example.wattescu.firebase.FirebaseConsumptionService;
import com.example.wattescu.firebase.FirebaseCurrentConfigurationService;
import com.example.wattescu.entities.Provider;
import com.example.wattescu.firebase.FirebaseCustomConfigurationService;
import com.example.wattescu.firebase.FirebaseSavedShopApplianceService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class SettingsActivity extends AppCompatActivity {

    private final String URL_OFFERS_COMPARATOR = "https://www.anre.ro/ro/info-consumatori/comparator-de-tarife";

    //Views
    private Button btnResetPassword;
    private Button btnResetUsername;
    private Button btnLogout;
    private TextView tvLinkProvider;
    private TextView tvDeleteAccount;
    private TextView tvEditProvider;

    //firebaseUser
    FirebaseAuth firebaseAuth;
    String loggedInUserId;

    //manage logged in user's current configuration
    FirebaseCurrentConfigurationService currentConfigurationService;
    FirebaseSavedShopApplianceService firebaseSavedShopApplianceService;
    FirebaseCustomConfigurationService firebaseCustomConfigurationService;
    FirebaseConsumptionService firebaseConsumptionService;

    //remove saved login credentials
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableTransitions();
        setContentView(R.layout.activity_settings);

        //initialize firebase vars
        firebaseAuth = FirebaseAuth.getInstance();
        loggedInUserId = firebaseAuth.getCurrentUser().getUid();
        try {
            currentConfigurationService = FirebaseCurrentConfigurationService.getInstance(loggedInUserId);
            firebaseConsumptionService = FirebaseConsumptionService.getInstance(loggedInUserId);
            firebaseCustomConfigurationService = FirebaseCustomConfigurationService.getInstance(loggedInUserId);
            firebaseSavedShopApplianceService = FirebaseSavedShopApplianceService.getInstance(loggedInUserId);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        //initialize sharedprefs
        preferences = getSharedPreferences(SplashScreenActivity.LOGIN_DETAILS, MODE_PRIVATE);

        initComponents();
    }

    private void initComponents() {
        btnResetPassword = findViewById(R.id.settings_btn_change_password);
        btnResetUsername = findViewById(R.id.settings_btn_change_username);
        btnLogout = findViewById(R.id.settings_btn_logout);
        tvLinkProvider = findViewById(R.id.settings_tv_link_provider);
        tvDeleteAccount = findViewById(R.id.settings_tv_delete_accout);
        tvEditProvider = findViewById(R.id.settings_tv_edit_provider);

        initEvents();
    }

    private void initEvents() {
        btnResetPassword.setOnClickListener(getResetPasswordListener());
        btnResetUsername.setOnClickListener(getResetUsernameListener());
        btnLogout.setOnClickListener(getLogoutListener());
        tvLinkProvider.setOnLongClickListener(getProviderLinkListener());
        tvDeleteAccount.setOnClickListener(getDeleteAccountListener());
        tvEditProvider.setOnClickListener(getEditProviderListener());
    }

    private View.OnClickListener getEditProviderListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View providerDialogView = getLayoutInflater().inflate(R.layout.provider_dialog,null);
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this)
                        .setView(providerDialogView);
                AlertDialog providerDialog = builder.create();
                providerDialog.show();
                //views from dialog
                AutoCompleteTextView actvProviderDialogProviderName = providerDialogView.findViewById(R.id.provider_dialog_actv_provider_name);
                TextInputEditText tietProviderDialogPriceKw = providerDialogView.findViewById(R.id.provider_dialog_tv_priceKw);
                ImageView ivProviderDialogClose = providerDialogView.findViewById(R.id.provider_dialog_iv_close);
                Button btnProviderDialogSave = providerDialogView.findViewById(R.id.provider_dialog_btn_save);
                Button btnProviderDialogDeleteProvider =providerDialogView.findViewById(R.id.provider_dialog_delete_provider);

                //adapter
                setActvProviderNameAdapter(actvProviderDialogProviderName);
                //set current providers details
                currentConfigurationService.getProvider(getCurrentProviderDetailsCallback(tietProviderDialogPriceKw,actvProviderDialogProviderName));
                //dialog events
                //close dialog
                ivProviderDialogClose.setOnClickListener(getProviderDialogCloseListener(providerDialog));
                //delete current provider reset to general one
                btnProviderDialogDeleteProvider.setOnClickListener(getProviderDialogDeleteProviderListener(providerDialog));
                //update provider details
                btnProviderDialogSave.setOnClickListener(getProvderDialogSaveListener(providerDialog, actvProviderDialogProviderName,tietProviderDialogPriceKw));

            }
        };
    }

    private View.OnClickListener getProvderDialogSaveListener(AlertDialog providerDialog, AutoCompleteTextView actvName, TextInputEditText tietPrice) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(providerIsValid(actvName, tietPrice)) {
                    Provider provider = new Provider(actvName.getText().toString().trim(), Double.parseDouble(tietPrice.getText().toString().trim()));
                    currentConfigurationService.updateProvider(provider);
                    providerDialog.dismiss();
                    Toast toast = new Toast(getApplicationContext());
                    editToast(toast, getString(R.string.msg_provider_data_saved));
                    toast.show();
                }
            }
        };
    }

    private boolean providerIsValid(AutoCompleteTextView actvName, TextInputEditText tietPrice) {
        if (actvName.getText() == null || actvName.getText().toString().trim().length() < 3) {
            actvName.setError("Introduceti un nume valid.");
            actvName.requestFocus();
            return false;
        }
        try {
            if (tietPrice.getText() == null || Double.parseDouble(tietPrice.getText().toString().trim())<=0){
                tietPrice.setError("Introduceti un pret valid.");
                tietPrice.requestFocus();
                return false;
            }
        } catch (Exception exception){
            tietPrice.setError("Introduceti un pret valid.");
            tietPrice.requestFocus();
            return false;
        }
        return true;
    }

    private View.OnClickListener getProviderDialogDeleteProviderListener(AlertDialog providerDialog) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentConfigurationService.insertGeneralProvider();
                providerDialog.dismiss();
                Toast toast = new Toast(getApplicationContext());
                editToast(toast, getString(R.string.msg_setting_deleted_provider));
                toast.show();
            }
        };
    }

    private Callback<Provider> getCurrentProviderDetailsCallback(TextInputEditText tietProviderDialogPriceKw, AutoCompleteTextView actvProviderDialogProviderName) {
        return new Callback<Provider>() {
            @Override
            public void runResultOnUiThread(Provider result) {
                tietProviderDialogPriceKw.setText(result.getPriceKw().toString());
                actvProviderDialogProviderName.setText(result.getName());
            }
        };
    }


    private void setActvProviderNameAdapter(AutoCompleteTextView actvProviderDialogProviderName) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(),
                R.array.providers, android.R.layout.simple_list_item_1);
        actvProviderDialogProviderName.setAdapter(adapter);
    }

    private View.OnClickListener getProviderDialogCloseListener(AlertDialog providerDialog) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                providerDialog.dismiss();
            }
        };
    }

    private View.OnClickListener getDeleteAccountListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle(R.string.setting_delete_account)
                        .setMessage(R.string.setting_msg_dialog_delete_account)
                        .setPositiveButton(R.string.setting_delete_account, getDeleteAccountPositiveListener())
                        .setNegativeButton(R.string.login_dialog_reset_password_cancel, getDeleteAccoutNegativeListener());
                builder.create().show();

            }
        };
    }

    private DialogInterface.OnClickListener getDeleteAccountPositiveListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                firebaseConsumptionService.deleteUserConsumtpion();
                firebaseSavedShopApplianceService.deleteAllSavedAppliances();
                firebaseCustomConfigurationService.deleteCustomConfigurations();
                currentConfigurationService.deleteCurrentConfiguration();
                FirebaseDatabase.getInstance().getReference("users")
                        .child(loggedInUserId).removeValue().addOnCompleteListener(getCompleteDeleteUsernameListener());

            }
        };
    }

    private OnCompleteListener<Void> getCompletedUserDeletionListener() {
        return new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast toast = new Toast(getApplicationContext());
                    editToast(toast, getString(R.string.deleted_account_message));
                    toast.show();
                    goToLoginActivity();
                } else {
                    Toast toast = new Toast(getApplicationContext());
                    editToast(toast, getString(R.string.general_error_msg));
                    toast.show();
                }
            }
        };
    }

    private OnCompleteListener<Void> getCompleteDeleteUsernameListener() {
        return new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    firebaseAuth.getCurrentUser().delete().addOnCompleteListener(getCompletedUserDeletionListener());
                }else{
                    Toast toast = new Toast(getApplicationContext());
                    editToast(toast, getString(R.string.general_error_msg));
                    toast.show();
                }
            }
        };
    }

    private DialogInterface.OnClickListener getDeleteAccoutNegativeListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //do nothing
            }
        };
    }

    private View.OnLongClickListener getProviderLinkListener() {
        return new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                goToUrl(URL_OFFERS_COMPARATOR);
                return true;
            }
        };
    }

    private void goToUrl(String url_offers_comparator) {
        Uri uri = Uri.parse(url_offers_comparator);
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }


    private View.OnClickListener getLogoutListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //sign out
                firebaseAuth.signOut();
                //remove saved preferences for credentials
                preferences.edit().clear().apply();
                //start login activity
                goToLoginActivity();
            }
        };
    }

    private void goToLoginActivity() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent,
                ActivityOptions.makeSceneTransitionAnimation(SettingsActivity.this).toBundle());
        finish();
    }

    private View.OnClickListener getResetUsernameListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //alert dialog for entering new username
                View dialogView = getLayoutInflater().inflate(R.layout.reset_username_dialog,null);
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                customizeUsernameDialog(dialogView, builder);

                AlertDialog dialog = builder.create();
                dialog.show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getColor(R.color.dark_gray));
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                }

                EditText etUsername = dialogView.findViewById(R.id.reset_username_dialog_et_username);
                etUsername.addTextChangedListener(getResetUsernameDialogWatcher(dialog, etUsername));
            }
        };
    }

    private TextWatcher getResetUsernameDialogWatcher(AlertDialog dialog, EditText etUsername) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (etUsername.getText().toString().trim().length() == 0 || etUsername.getText().toString().trim().length() > 15) {
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        };
    }

    private void customizeUsernameDialog(View dialogView, AlertDialog.Builder builder) {
        builder.setView(dialogView)
                .setTitle(R.string.settings_title_change_username)
                .setPositiveButton(R.string.reset_dialog_ok, getResetUsernamePositiveListener(dialogView))
                .setNegativeButton(R.string.login_dialog_reset_password_cancel, getResetUsernameNegativeListener());
    }

    private DialogInterface.OnClickListener getResetUsernamePositiveListener(View dialogView) {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //get email adr
                EditText etUsername = dialogView.findViewById(R.id.reset_username_dialog_et_username);
                String newUsername = etUsername.getText().toString().trim();
                if (newUsername.isEmpty()) {
                    dialog.cancel();
                } else {
                    //reset username
                    String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    FirebaseDatabase.getInstance().getReference("users")
                            .child(id).child("username").setValue(newUsername).addOnCompleteListener(getCompleteResetUsernameListener());
                }
            }
        };
    }

    private OnCompleteListener<Void> getCompleteResetUsernameListener() {
        return new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast toast = new Toast(getApplicationContext());
                    editToast(toast,getString(R.string.msg_username_succesfully_changed));
                    toast.show();
                }else{
                    Toast toast = new Toast(getApplicationContext());
                    editToast(toast, getString(R.string.general_error_msg));
                    toast.show();
                }
            }
        };
    }

    private DialogInterface.OnClickListener getResetUsernameNegativeListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //nothin happens
            }
        };
    }

    protected View.OnClickListener getResetPasswordListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //alert dialog for entering email adr
                View dialogView = getLayoutInflater().inflate(R.layout.reset_password_dialog,null);
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                customizePasswordDialog(dialogView, builder);

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

    private void customizePasswordDialog(View dialogView, AlertDialog.Builder builder) {
        builder.setView(dialogView)
                .setTitle(R.string.msg_settings_change_password)
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
                //get email adr
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
                    goToLoginActivity();
                } else {
                    Toast toast = new Toast(getApplicationContext());
                    editToast(toast, getString(R.string.login_dialog_reset_password_error));
                    toast.show();
                }
            }
        };
    }

    private void editToast(Toast toast, String p) {
        View toast_view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.toast_top_layout, null);
        ((TextView) toast_view.findViewById(R.id.toast_layout_tv)).setText(p);
        toast.setView(toast_view);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 0, 0);
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