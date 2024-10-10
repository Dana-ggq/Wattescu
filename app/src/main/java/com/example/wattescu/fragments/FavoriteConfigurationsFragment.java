package com.example.wattescu.fragments;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.agrawalsuneet.dotsloader.loaders.CircularDotsLoader;
import com.example.wattescu.R;
import com.example.wattescu.async.Callback;
import com.example.wattescu.firebase.FirebaseCurrentConfigurationService;
import com.example.wattescu.firebase.FirebaseCustomConfigurationService;
import com.example.wattescu.adapters.CustomConfigurationViewAppliancesAdapter;
import com.example.wattescu.adapters.FavoriteConfigurationsAdapter;
import com.example.wattescu.entities.Appliance;
import com.example.wattescu.entities.CustomConfiguration;
import com.example.wattescu.entities.Provider;
import com.example.wattescu.entities.SavedShopAppliance;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;


public class FavoriteConfigurationsFragment extends Fragment {

    //views
    private ListView lvConfigurations;
    private CircularDotsLoader progressBar;
    //firebase
    private FirebaseCustomConfigurationService firebaseCustomConfigurationService;
    private FirebaseCurrentConfigurationService firebaseCurrentConfigurationService;
    private Double priceKwh;

    private List<CustomConfiguration> customConfigurationList = new ArrayList<>();
    private List<Appliance> currentAppliances = new ArrayList<>();

    public FavoriteConfigurationsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite_configurations, container, false);
        initComponents(view);
        return view;
    }

    private void initComponents(View view) {
        lvConfigurations = view.findViewById(R.id.fragment_favorite_configurations_lv);
        setLvAdapter();
        progressBar = view.findViewById(R.id.fragment_favorite_configurations_progress_bar);
        if(getContext()!=null){
            initFirebase();
            initEvents();
        }
    }

    private void initEvents() {
        lvConfigurations.setOnItemClickListener(getOpenViewItemClickListener());
    }

    private AdapterView.OnItemClickListener getOpenViewItemClickListener() {
        return new AdapterView.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                View appliancesListViewDialog = getLayoutInflater().inflate(R.layout.dialog_custom_configuration_view_appliances,null);
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyAlertDialog)
                        .setView(appliancesListViewDialog);
                AlertDialog customConfigurationDialog = builder.create();
                customConfigurationDialog.show();

                //views from dialog
                ImageView ivDialogClose = appliancesListViewDialog.findViewById(R.id.custom_configuration_view_appliance_dialog_iv_close);
                TextView tvConsumptionReduction = appliancesListViewDialog.findViewById(R.id.custom_configuration_view_appliance_dialog_tv_consumption_reduction);
                TextView tvSavings = appliancesListViewDialog.findViewById(R.id.custom_configuration_view_appliance_dialog_tv_savings);
                TextView tvCo2Reduction = appliancesListViewDialog.findViewById(R.id.custom_configuration_view_appliance_dialog_tv_co2_reduction);
                ListView lvAppliances = appliancesListViewDialog.findViewById(R.id.custom_configuration_view_appliance_dialog_lv_appliances);

                //set data
                Double co2Reduction = getConffigurationCo2Reduction(customConfigurationList.get(position));
                Double consumptionReduction = getConffigurationCunsoptionReduction(customConfigurationList.get(position));
                Double configurationSavings = getConffigurationSavingsReduction(customConfigurationList.get(position));
                if(co2Reduction == 0.0 || consumptionReduction == 0.0 || configurationSavings == 0.0){
                    tvConsumptionReduction.setText(R.string.msg_no_appliance_to_replace);
                }else {
                    tvCo2Reduction.setText(getString(R.string.format_co2_emissions_reduction, getConffigurationCo2Reduction(customConfigurationList.get(position))));
                    tvConsumptionReduction.setText(getString(R.string.format_consumption_reduction, getConffigurationCunsoptionReduction(customConfigurationList.get(position))));
                    tvSavings.setText(getString(R.string.format_savings, getConffigurationSavingsReduction(customConfigurationList.get(position))));
                }
                //set lv
                ArrayList<SavedShopAppliance> appliances = new ArrayList<>();
                appliances.addAll(customConfigurationList.get(position).getSavedAppliances().values());
                CustomConfigurationViewAppliancesAdapter adapter = new CustomConfigurationViewAppliancesAdapter(getContext(), R.layout.lv_row_view_custom_configuration_appliances, appliances, getLayoutInflater());
                adapter.setCurrentAppliances(currentAppliances);
                adapter.setPriceKwH(priceKwh);
                lvAppliances.setAdapter(adapter);

                //dialog events
                //close dialog
                ivDialogClose.setOnClickListener(getCloseViewConfigurationsApplianceDialogListener(customConfigurationDialog));
            }
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private Double getConffigurationSavingsReduction(CustomConfiguration customConfiguration) {
        Double savings = 0.0;
        for (SavedShopAppliance appliance:
                customConfiguration.getSavedAppliances().values()) {
            try {
                savings += appliance.getSavings(currentAppliances, priceKwh);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        return  savings;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private Double getConffigurationCunsoptionReduction(CustomConfiguration customConfiguration) {
        Double consumptionReduction = 0.0;
        for (SavedShopAppliance appliance:
                customConfiguration.getSavedAppliances().values()) {
            try {
                consumptionReduction += appliance.getConsumptionReduction(currentAppliances);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        return  consumptionReduction;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private Double getConffigurationCo2Reduction(CustomConfiguration customConfiguration) {
        Double co2Reduction = 0.0;
        for (SavedShopAppliance appliance:
                customConfiguration.getSavedAppliances().values()) {
            try {
                co2Reduction += appliance.getCo2Savings(currentAppliances);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        return  co2Reduction;
    }

    private View.OnClickListener getCloseViewConfigurationsApplianceDialogListener(AlertDialog customConfigurationDialog) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customConfigurationDialog.dismiss();
            }
        };
    }

    private void initFirebase() {
        try {
            progressBar.setVisibility(View.VISIBLE);
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            firebaseCurrentConfigurationService = FirebaseCurrentConfigurationService.getInstance(userId);
            firebaseCustomConfigurationService = FirebaseCustomConfigurationService.getInstance(userId);

            firebaseCurrentConfigurationService.getAppliancesDataChangeListener(currentApplianceDataChangeCallback());
            firebaseCustomConfigurationService.getCustomConfigurationsDataChangeListener(customConfigurationsDataChangeCallback());
            firebaseCurrentConfigurationService.getProvider(getProviderDetailsDataChangeCallback());

            FavoriteConfigurationsAdapter adapter = (FavoriteConfigurationsAdapter) lvConfigurations.getAdapter();
            adapter.setFirebaseCustomConfigurationService(firebaseCustomConfigurationService);

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private Callback<Provider> getProviderDetailsDataChangeCallback() {
        return new Callback<Provider>() {
            @Override
            public void runResultOnUiThread(Provider result) {
                if(result!=null){
                    priceKwh = result.getPriceKw();
                }
            }
        };
    }

    private Callback<List<CustomConfiguration>> customConfigurationsDataChangeCallback() {
        return new Callback<List<CustomConfiguration>>() {
            @Override
            public void runResultOnUiThread(List<CustomConfiguration> result) {
                progressBar.setVisibility(View.GONE);
                if(result!=null){
                    customConfigurationList.clear();
                    customConfigurationList.addAll(result);
                    notifyAdaper();
                }
            }
        };
    }

    private Callback<List<Appliance>> currentApplianceDataChangeCallback() {
        return new Callback<List<Appliance>>() {
            @Override
            public void runResultOnUiThread(List<Appliance> result) {
                if(result!=null){
                    currentAppliances.clear();
                    currentAppliances.addAll(result);
                    FavoriteConfigurationsAdapter adapter = (FavoriteConfigurationsAdapter) lvConfigurations.getAdapter();
                    adapter.setCurrentAppliances(currentAppliances);
                    notifyAdaper();
                }
            }
        };
    }

    private void notifyAdaper() {
        FavoriteConfigurationsAdapter adapter = (FavoriteConfigurationsAdapter) lvConfigurations.getAdapter();
        adapter.notifyDataSetChanged();
    }

    private void setLvAdapter() {
        FavoriteConfigurationsAdapter adapter = new FavoriteConfigurationsAdapter(getContext(), R.layout.lv_row_view_custom_configuration, customConfigurationList, getLayoutInflater());
        lvConfigurations.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        // TODO: 4/15/2022  find a better way to do this 
        initFirebase();
    }
}