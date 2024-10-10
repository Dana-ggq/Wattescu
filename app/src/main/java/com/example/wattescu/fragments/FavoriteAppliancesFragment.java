package com.example.wattescu.fragments;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.agrawalsuneet.dotsloader.loaders.CircularDotsLoader;
import com.example.wattescu.R;
import com.example.wattescu.async.Callback;
import com.example.wattescu.firebase.FirebaseCurrentConfigurationService;
import com.example.wattescu.firebase.FirebaseCustomConfigurationService;
import com.example.wattescu.firebase.FirebaseSavedShopApplianceService;
import com.example.wattescu.adapters.FavoriteAppliancesLvAdapter;
import com.example.wattescu.entities.Appliance;
import com.example.wattescu.entities.CustomConfiguration;
import com.example.wattescu.entities.SavedShopAppliance;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FavoriteAppliancesFragment extends Fragment {

    //views
    private ListView lvSavedAppliances;
    private CircularDotsLoader progressBar;

    private ImageButton ibCreateConfiguration;
    private  ImageButton ibCancelOperation;
    private boolean isCreateConfigurationMenuShown = false;
    private List<SavedShopAppliance> configurationAppliances = new ArrayList<>();
    private Animation animIbOpen, animIbClose;

    //firebase
    private FirebaseSavedShopApplianceService firebaseSavedShopApplianceService;
    private FirebaseCurrentConfigurationService firebaseCurrentConfigurationService;
    private FirebaseCustomConfigurationService firebaseCustomConfigurationService;
    private List<SavedShopAppliance> savedAppliances = new ArrayList<>();
    private List<Appliance> currentAppliances = new ArrayList<>();


    public FavoriteAppliancesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite_appliances, container, false);
        initComponents(view);
        return view;
    }

    private void initComponents(View view) {
        ibCancelOperation = view.findViewById(R.id.fragment_favorite_appliances_ib_cancel);
        ibCreateConfiguration = view.findViewById(R.id.fragment_favorite_appliances_ib_create_configuration);
        lvSavedAppliances = view.findViewById(R.id.fragment_favorite_appliances_lv);
        setLvAppliancesAdapter();
        progressBar = view.findViewById(R.id.fragment_favorite_appliances_progress_bar);
        if(getContext()!=null){
            initAnimations();
            initFirebase();
            initEvents();
        }
    }

    private void initAnimations() {
        animIbOpen = AnimationUtils.loadAnimation(getContext(), R.anim.menu_fab_open);
        animIbClose = AnimationUtils.loadAnimation(getContext(), R.anim.menu_fab_close);
    }

    private void setLvAppliancesAdapter() {
        FavoriteAppliancesLvAdapter adapter = new FavoriteAppliancesLvAdapter(getContext(), R.layout.lv_row_favorite_appliances, savedAppliances,
                getLayoutInflater());
        lvSavedAppliances.setAdapter(adapter);
    }

    private void initFirebase() {
        try {
            progressBar.setVisibility(View.VISIBLE);
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            firebaseSavedShopApplianceService = FirebaseSavedShopApplianceService.getInstance(userId);
            firebaseCurrentConfigurationService = FirebaseCurrentConfigurationService.getInstance(userId);
            firebaseCustomConfigurationService = FirebaseCustomConfigurationService.getInstance(userId);

            firebaseCurrentConfigurationService.getAppliancesDataChangeListener(currentApplianceDataChangeCallback());
            firebaseSavedShopApplianceService.getSavedShopAppliancesListener(savedApplianceDataChangeListener());

            FavoriteAppliancesLvAdapter adapter = (FavoriteAppliancesLvAdapter) lvSavedAppliances.getAdapter();
            adapter.setSavedShopApplianceService(firebaseSavedShopApplianceService);

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private Callback<List<Appliance>> currentApplianceDataChangeCallback() {
        return new Callback<List<Appliance>>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void runResultOnUiThread(List<Appliance> result) {
                if(result!=null){
                    currentAppliances.clear();
                    currentAppliances.addAll(result);
                    FavoriteAppliancesLvAdapter adapter = (FavoriteAppliancesLvAdapter) lvSavedAppliances.getAdapter();
                    adapter.setCurrentAppliances(currentAppliances);
                    notifyAdaper();
                }
            }
        };
    }

    private void notifyAdaper() {
        FavoriteAppliancesLvAdapter adapter = (FavoriteAppliancesLvAdapter) lvSavedAppliances.getAdapter();
        adapter.notifyDataSetChanged();
    }

    private Callback<List<SavedShopAppliance>> savedApplianceDataChangeListener() {
        return new Callback<List<SavedShopAppliance>>() {
            @Override
            public void runResultOnUiThread(List<SavedShopAppliance> result) {
                progressBar.setVisibility(View.GONE);
                if(result!=null){
                    savedAppliances.clear();
                    savedAppliances.addAll(result);
                    notifyAdaper();
                }
            }
        };
    }

    private void initEvents() {
        lvSavedAppliances.setOnItemLongClickListener(getLongItemClickListener());
        ibCancelOperation.setOnClickListener(getIbCancelClickListener());
        ibCreateConfiguration.setOnClickListener(getIbCreateConfigurationClickListener());
    }

    private View.OnClickListener getIbCreateConfigurationClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View configurationDialogView = getLayoutInflater().inflate(R.layout.custom_configuration_set_name_dialog,null);
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyAlertDialog)
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
                btnSave.setOnClickListener(getSaveConfigurationListener(customConfigurationDialog, tietName));

            }
        };
    }

    private View.OnClickListener getSaveConfigurationListener(AlertDialog customConfigurationDialog, TextInputEditText tietName) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(configurationIsValid(tietName, customConfigurationDialog)) {
                    CustomConfiguration customConfiguration = new CustomConfiguration();
                    customConfiguration.setName(tietName.getText().toString().trim());
                    customConfiguration.setOptim(false);
                    Map<String, SavedShopAppliance> currentlySavedAppliances = new HashMap<>();
                    for (SavedShopAppliance appliance:
                         configurationAppliances) {
                        currentlySavedAppliances.put(String.valueOf(appliance.getId()), appliance);
                    }
                    customConfiguration.setSavedAppliances(currentlySavedAppliances);
                    firebaseCustomConfigurationService.createCustomConfiguration(customConfiguration);
                    customConfigurationDialog.dismiss();

                    Toast toast = new Toast(getContext());
                    editToast(toast, getString(R.string.configuration_creates_message));
                    toast.show();

                    ibCancelOperation.performClick();
                }
            }
        };
    }

    private void editToast(Toast toast, String p) {
        View toast_view = LayoutInflater.from(getContext()).inflate(R.layout.toast_top_layout, null);
        ((TextView) toast_view.findViewById(R.id.toast_layout_tv)).setText(p);
        toast.setView(toast_view);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 0);
    }

    private boolean configurationIsValid(TextInputEditText tietName, AlertDialog customConfigurationDialog) {
        if (tietName.getText() == null || tietName.getText().toString().trim().length() < 1) {
            Toast toast = new Toast(getContext());
            editToast(toast, getString(R.string.err_configuration_add_name));
            toast.show();
            return false;
        }
        if(configurationAppliances == null || configurationAppliances.isEmpty() || configurationAppliances.size() <= 1){
            Toast toast = new Toast(getContext());
            editToast(toast, getString(R.string.err_custom_config_select_2_appliances));
            toast.show();
            customConfigurationDialog.dismiss();
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

    private View.OnClickListener getIbCancelClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(configurationAppliances!=null && !configurationAppliances.isEmpty()) {
                    configurationAppliances.clear();
                }

                    closeIbConfigurationMenu();

                    FavoriteAppliancesLvAdapter adapter = (FavoriteAppliancesLvAdapter) lvSavedAppliances.getAdapter();
                    adapter.setCurrentlySelectedAppliances(configurationAppliances);

                    adapter.setClickable(true);
                    notifyAdaper();
            }
        };
    }

    private void closeIbConfigurationMenu(){
        ibCreateConfiguration.startAnimation(animIbClose);
        ibCreateConfiguration.setClickable(false);
        ibCancelOperation.startAnimation(animIbClose);
        ibCancelOperation.setClickable(false);
        isCreateConfigurationMenuShown = false;
    }

    private void openIbConfigurationMenu(){
        ibCreateConfiguration.startAnimation(animIbOpen);
        ibCreateConfiguration.setClickable(true);
        ibCancelOperation.startAnimation(animIbOpen);
        ibCancelOperation.setClickable(true);
        isCreateConfigurationMenuShown = true;
    }

    private AdapterView.OnItemLongClickListener getLongItemClickListener() {
        return new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if(!isCreateConfigurationMenuShown){
                    openIbConfigurationMenu();
                }
                if(!configurationAppliances.contains(savedAppliances.get(position))){
                    //save
                    if(canApplianceCanBeAddedToConfiguration(savedAppliances.get(position))){
                        view.setBackground(getContext().getDrawable(R.drawable.selected_lv_item));
                        configurationAppliances.add(savedAppliances.get(position));
                    }
                } else{
                    //delete
                    view.setBackground(getContext().getDrawable(R.drawable.bottom_round_white_bg));
                    configurationAppliances.remove(savedAppliances.get(position));
                }

                FavoriteAppliancesLvAdapter adapter = (FavoriteAppliancesLvAdapter) lvSavedAppliances.getAdapter();
                adapter.setCurrentlySelectedAppliances(configurationAppliances);
                adapter.setClickable(false);
                notifyAdaper();

                if(configurationAppliances == null || configurationAppliances.isEmpty() || configurationAppliances.size() < 1){
                    ibCancelOperation.performClick();
                }
                return true;
            }
        };
    }

    private boolean canApplianceCanBeAddedToConfiguration(SavedShopAppliance savedShopAppliance) {
        //has replace appliance id
        if(savedShopAppliance.getApplianceToReplaceId() == null || savedShopAppliance.getApplianceToReplaceId().isEmpty()){
            Toast toast = new Toast(getContext());
            editToast(toast, getString(R.string.err_configuraiton_replace_appliance));
            toast.show();
            return false;
        }
        //replace appliance id is unique for all appliances in configuration
        for (SavedShopAppliance shopAppliance: configurationAppliances) {
            if(shopAppliance.getApplianceToReplaceId().equals(savedShopAppliance.getApplianceToReplaceId())){
                Toast toast = new Toast(getContext());
                editToast(toast, getString(R.string.err_configuration_appliance_already_selected));
                toast.show();
                return false;
            }
        }
        return true;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        try{
            if(isCreateConfigurationMenuShown) {
                ibCancelOperation.performClick();
            }
        } catch (Exception exception){
            exception.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try{
            if(isCreateConfigurationMenuShown) {
                ibCancelOperation.performClick();
            }
        } catch (Exception exception){
            exception.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        try{
            notifyAdaper();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
}