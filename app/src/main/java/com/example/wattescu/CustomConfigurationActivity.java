package com.example.wattescu;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.transition.Slide;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.agrawalsuneet.dotsloader.loaders.CircularDotsLoader;
import com.example.wattescu.adapters.OptimConfigurationApplianceViewAdapter;
import com.example.wattescu.entities.Appliance;
import com.example.wattescu.entities.CustomConfiguration;
import com.example.wattescu.entities.SavedShopAppliance;
import com.example.wattescu.entities.ShopAppliance;
import com.example.wattescu.enums.CategoryFilterType;
import com.example.wattescu.enums.RoomType;
import com.example.wattescu.firebase.FirebaseCurrentConfigurationService;
import com.example.wattescu.firebase.FirebaseCustomConfigurationService;
import com.example.wattescu.firebase.FirebaseShopAppliancesService;
import com.example.wattescu.python.api.optimization.OptimalConfigurationApi;
import com.example.wattescu.python.api.optimization.OptimalConfigurationRequestObject;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CustomConfigurationActivity extends AppCompatActivity {

    public static final String INTERNAL_ERROR = "internal error";
    public static final String INFEASIBLE = "Infeasible";
    public static final String OPTIMAL = "Optimal";
    //views
    private CircularDotsLoader progressBar;
    private LinearLayout llInfesible;
    private TextView tvInfesibleMessage;
    private Button btnInfesibleRetake;
    private ListView lvAppliances;
    private Button btnSaveConfiguration;
    private TextView tvTitle;

    //customize config values
    private int budget;
    private String requested_appliances;
    private String requested_appliances_quantities;

    private FirebaseCurrentConfigurationService firebaseCurrentConfigurationService;
    private FirebaseShopAppliancesService firebaseShopAppliancesService;
    private FirebaseCustomConfigurationService firebaseCustomConfigurationService;
    private List<ShopAppliance> shopAppliances = new ArrayList<>();
    private List<Appliance> currentAppliances = new ArrayList<>();
    private List<SavedShopAppliance> optimConfigurationAppliances = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableTransitions();
        setContentView(R.layout.activity_custom_configuration);

        getRequestValuesFromIntent();
        initComponents();
        initFirebase();
    }

    private void initFirebase() {
        try {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            firebaseCurrentConfigurationService = FirebaseCurrentConfigurationService.getInstance(userId);
            firebaseCustomConfigurationService = FirebaseCustomConfigurationService.getInstance(userId);
            firebaseShopAppliancesService = FirebaseShopAppliancesService.getInstance();

            firebaseCurrentConfigurationService.getAppliancesDataChangeListener(currentApplianceDataChangeCallback());
            firebaseShopAppliancesService.attachDataChangedListener(getShopAppliances(), RoomType.ALL, CategoryFilterType.NONE);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private com.example.wattescu.async.Callback<List<ShopAppliance>> getShopAppliances() {
        return new com.example.wattescu.async.Callback<List<ShopAppliance>>() {
            @Override
            public void runResultOnUiThread(List<ShopAppliance> result) {
                if(result!=null){
                    shopAppliances.clear();
                    shopAppliances.addAll(result);
                    //get optim configuration from API
                    getPythonApiResponse(budget, requested_appliances,requested_appliances_quantities);

                }
            }
        };
    }

    private com.example.wattescu.async.Callback<List<Appliance>> currentApplianceDataChangeCallback() {
        return new com.example.wattescu.async.Callback<List<Appliance>>() {
            @Override
            public void runResultOnUiThread(List<Appliance> result) {
                if(result!=null){
                    currentAppliances.clear();
                    currentAppliances.addAll(result);
                    OptimConfigurationApplianceViewAdapter adapter = (OptimConfigurationApplianceViewAdapter) lvAppliances.getAdapter();
                    adapter.setCurrentAppliances(currentAppliances);
                    notifyAdaper();
                }
            }
        };
    }

    private void notifyAdaper() {
        OptimConfigurationApplianceViewAdapter adapter = (OptimConfigurationApplianceViewAdapter) lvAppliances.getAdapter();
        adapter.notifyDataSetChanged();
    }

    private void initComponents() {
        progressBar = findViewById(R.id.custom_configuration_progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        tvTitle = findViewById(R.id.custom_configuration_tv_title);
        llInfesible = findViewById(R.id.custom_configuration_ll_infesible);
        tvInfesibleMessage = findViewById(R.id.custom_configuration_ll_infesible_tv_error_message);
        btnInfesibleRetake = findViewById(R.id.custom_configuration_ll_infesible_btn_restart);
        lvAppliances = findViewById(R.id.custom_configuration_lv_appliances);
        setLvAppliancesAdapter();
        btnSaveConfiguration = findViewById(R.id.custom_configuration_btn_save);
        initEvents();
    }

    private void setLvAppliancesAdapter() {
        OptimConfigurationApplianceViewAdapter adapter = new OptimConfigurationApplianceViewAdapter(CustomConfigurationActivity.this,
                R.layout.lv_row_custom_configuration_appliance,optimConfigurationAppliances,getLayoutInflater());
        lvAppliances.setAdapter(adapter);
    }

    private void initEvents() {
        btnInfesibleRetake.setOnClickListener(getRetakeListener());
        btnSaveConfiguration.setOnClickListener(getSaveConfigurationListener());
    }

    private View.OnClickListener getSaveConfigurationListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertSomeAppliancesAreNotReplacing();

                View configurationDialogView = getLayoutInflater().inflate(R.layout.custom_configuration_set_name_dialog,null);
                AlertDialog.Builder builder = new AlertDialog.Builder(CustomConfigurationActivity.this, R.style.MyAlertDialog)
                        .setView(configurationDialogView);
                AlertDialog customConfigurationDialog = builder.create();
                customConfigurationDialog.show();

                //views from dialog
                TextInputEditText tietName = configurationDialogView.findViewById(R.id.custom_configuration_dialog_tiet_name);
                ImageView ivDialogClose = configurationDialogView.findViewById(R.id.custom_configuration_dialog_iv_close);
                Button btnSave =configurationDialogView.findViewById(R.id.custom_configuration_dialog_btn_save);

                //dialog events
                //close dialog
                ivDialogClose.setOnClickListener(getCloseCreateConfigurationDialogListener(customConfigurationDialog));
                //save
                btnSave.setOnClickListener(getCreateConfigurationListener(customConfigurationDialog, tietName));
            }
        };
    }

    private View.OnClickListener getCreateConfigurationListener(AlertDialog customConfigurationDialog, TextInputEditText tietName) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(configurationIsValid(tietName, customConfigurationDialog)) {
                    CustomConfiguration customConfiguration = new CustomConfiguration();
                    customConfiguration.setName(tietName.getText().toString().trim());
                    customConfiguration.setOptim(true);
                    Map<String, SavedShopAppliance> currentlySavedAppliances = new HashMap<>();

                    for (SavedShopAppliance appliance:
                            optimConfigurationAppliances) {
                        currentlySavedAppliances.put(String.valueOf(appliance.getId()), appliance);
                    }

                    customConfiguration.setSavedAppliances(currentlySavedAppliances);
                    firebaseCustomConfigurationService.createCustomConfiguration(customConfiguration);
                    customConfigurationDialog.dismiss();

                    Toast toast = new Toast(CustomConfigurationActivity.this);
                    editToast(toast, getString(R.string.configuration_creates_message));
                    toast.show();
                    CustomConfigurationActivity.this.finish();

                }
            }
        };
    }

    private void alertSomeAppliancesAreNotReplacing() {
        for (SavedShopAppliance appliance:optimConfigurationAppliances) {
            if(appliance.getApplianceToReplaceId() == null || appliance.getApplianceToReplaceId().isEmpty()){
                Toast toast = new Toast(CustomConfigurationActivity.this);
                editToast(toast, getString(R.string.msg_not_all_appliances_have_replacement));
                toast.show();
                return;
            }
        }
    }

    private void editToast(Toast toast, String string) {
        View toast_view = LayoutInflater.from(CustomConfigurationActivity.this).inflate(R.layout.toast_top_layout, null);
        ((TextView) toast_view.findViewById(R.id.toast_layout_tv)).setText(string);
        toast.setView(toast_view);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 0, 0);
    }

    private boolean configurationIsValid(TextInputEditText tietName, AlertDialog customConfigurationDialog) {
        if (tietName.getText() == null || tietName.getText().toString().trim().length() < 1) {
            Toast toast = new Toast(CustomConfigurationActivity.this);
            editToast(toast, getString(R.string.err_configuration_add_name));
            toast.show();
            return false;
        }
        //replace appliance id is unique for all appliances in configuration
        List<String> idsToReplace = new ArrayList<>();
        for (SavedShopAppliance shopAppliance: optimConfigurationAppliances) {
            idsToReplace.add(shopAppliance.getApplianceToReplaceId());
        }
        Set<String> set = new HashSet<String>(idsToReplace);
        if(set.size() < idsToReplace.size()){
            Toast toast = new Toast(CustomConfigurationActivity.this);
            editToast(toast, getString(R.string.err_msg_replaces_same_appliance));
            toast.show();
            return false;
        }
        return true;
    }


    private View.OnClickListener getCloseCreateConfigurationDialogListener(AlertDialog customConfigurationDialog) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customConfigurationDialog.dismiss();
            }
        };
    }

    private View.OnClickListener getRetakeListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CustomConfigurationActivity.this, CustomizeConfigurationActivity.class);
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(CustomConfigurationActivity.this);
                startActivity(intent, options.toBundle());
                finish();
            }
        };
    }

    private void getRequestValuesFromIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(CustomizeConfigurationActivity.BUDGET_KEY)){
            budget = intent.getIntExtra(CustomizeConfigurationActivity.BUDGET_KEY, 0);
        }
        if(intent.hasExtra(CustomizeConfigurationActivity.REQUESTED_APPLIANCES_KEY)){
            requested_appliances = intent.getStringExtra(CustomizeConfigurationActivity.REQUESTED_APPLIANCES_KEY);
        }
        if(intent.hasExtra(CustomizeConfigurationActivity.REQUESTED_QUANTITIES_KEY)){
            requested_appliances_quantities = intent.getStringExtra(CustomizeConfigurationActivity.REQUESTED_QUANTITIES_KEY);
        }
    }

    private void getPythonApiResponse(int budget, String requestedAppliances, String requestedQuantities) {

        //create retrofit instance
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://danaggeorgescu.pythonanywhere.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        //create api
        OptimalConfigurationApi optimalConfigurationApi = retrofit.create(OptimalConfigurationApi.class);
        Call<OptimalConfigurationRequestObject> callApi = optimalConfigurationApi.getResult(budget, requestedAppliances, requestedQuantities);
        callApi.enqueue(new Callback<OptimalConfigurationRequestObject>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(Call<OptimalConfigurationRequestObject> call, Response<OptimalConfigurationRequestObject> response) {
                progressBar.setVisibility(View.GONE);

                if(!response.isSuccessful()){
                    //error
                    showInternalError();
                    return;
                }

                //got answer
                OptimalConfigurationRequestObject optimalConfigurationRequestResponse = response.body();
                if(optimalConfigurationRequestResponse.getResult().equals(INTERNAL_ERROR)){
                    showInternalError();
                } else if (optimalConfigurationRequestResponse.getResult().equals(INFEASIBLE)){
                    llInfesible.setVisibility(View.VISIBLE);
                } else if(optimalConfigurationRequestResponse.getResult().equals(OPTIMAL)){
                    makeConfigurationViewsVisible();
                    String[] ids = optimalConfigurationRequestResponse.getAppliancesIds().split(" ");
                    for(int i=0; i<ids.length; i++){
                        int j = i;
                        ShopAppliance currentAppliance = shopAppliances.stream()
                                .filter(appliance -> ids[j].equals(String.valueOf(appliance.getId())))
                                .findAny()
                                .orElse(null);
                        if (currentAppliance != null) {
                            optimConfigurationAppliances.add(new SavedShopAppliance(currentAppliance));
                        }
                    }
                    notifyAdaper();
                } else {
                    showInternalError();
                }
            }

            @Override
            public void onFailure(Call<OptimalConfigurationRequestObject> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                showInternalError();
            }
        });


    }

    private void showInternalError() {
        tvInfesibleMessage.setText(R.string.msg_infesible_config_internal_error);
        llInfesible.setVisibility(View.VISIBLE);
    }

    private void makeConfigurationViewsVisible() {
        tvTitle.setVisibility(View.VISIBLE);
        lvAppliances.setVisibility(View.VISIBLE);
        btnSaveConfiguration.setVisibility(View.VISIBLE);
    }

    private void enableTransitions() {
        //enable transition
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        // set transitions
        Slide slide = new Slide();
        slide.setSlideEdge(Gravity.RIGHT);
        slide.setInterpolator(new AnticipateOvershootInterpolator());
        getWindow().setEnterTransition(slide);
        getWindow().setExitTransition(slide);
    }
}