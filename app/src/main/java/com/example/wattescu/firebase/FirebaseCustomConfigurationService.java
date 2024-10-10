package com.example.wattescu.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.wattescu.async.Callback;
import com.example.wattescu.entities.CustomConfiguration;
import com.example.wattescu.entities.SavedShopAppliance;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FirebaseCustomConfigurationService {
    public static final String CUSTOMCONFIGURATIONS_REFERENCE = "customconfigurations";
    public static final String SAVED_APPLIANCES_REFERENCE = "savedappliances";

    private final DatabaseReference reference;
    private final String userKey;

    private static FirebaseCustomConfigurationService firebaseCustomConfigurationService;

    private FirebaseCustomConfigurationService(String userKey) {
        reference = FirebaseDatabase.getInstance().getReference(CUSTOMCONFIGURATIONS_REFERENCE);
        this.userKey = userKey;
    }

    public static FirebaseCustomConfigurationService getInstance(String userKey) throws Exception {
        if (userKey == null || userKey.trim().isEmpty()) {
            throw new Exception("invalid user key");
        }
        if (firebaseCustomConfigurationService == null) {
            synchronized (FirebaseCurrentConfigurationService.class) {
                if (firebaseCustomConfigurationService == null) {
                    firebaseCustomConfigurationService = new FirebaseCustomConfigurationService(userKey);
                }
            }
        }
        return firebaseCustomConfigurationService;
    }

    //get users configuration
    public DatabaseReference getCurrentUsersConfigurations(){
        try{
            return reference.child(userKey);}
        catch (Exception ex){
            return null;
        }
    }

    //delete all configuration
    public void deleteCustomConfigurations() {
        reference.child(userKey).removeValue();
    }

    //create name and list of appliances
    public void createCustomConfiguration(CustomConfiguration configuration) {
        if(configuration == null || (configuration.getId() != null && !configuration.getId().trim().isEmpty())){
            return;
        }
        String id = reference.push().getKey();
        configuration.setId(id);
        getCurrentUsersConfigurations().child(configuration.getId()).setValue(configuration);
    }

    //delete configuration
    public void deleteCustomConfiguration(CustomConfiguration customConfiguration) {
        if(customConfiguration == null || customConfiguration.getId() == null || customConfiguration.getId().trim().isEmpty()){
            return;
        }
        getCurrentUsersConfigurations().child(customConfiguration.getId()).removeValue();
    }

    //select all configurations
    public void getCustomConfigurationsDataChangeListener(Callback<List<CustomConfiguration>> callback) {
        getCurrentUsersConfigurations().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<CustomConfiguration> customConfigurations = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    CustomConfiguration customConfiguration = data.getValue(CustomConfiguration.class);
                    customConfigurations.add(customConfiguration);
                }
                callback.runResultOnUiThread(customConfigurations);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseService", "Data is not available");
            }
        });
    }


    public void removeApplianceToReplace(String id) {
        getCurrentUsersConfigurations().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    CustomConfiguration customConfiguration = data.getValue(CustomConfiguration.class);
                    Map<String, SavedShopAppliance> savedShopAppliances = customConfiguration.getSavedAppliances();
                    for (SavedShopAppliance shopAppliance:savedShopAppliances.values()){
                        if (shopAppliance.getApplianceToReplaceId()!=null && shopAppliance.getApplianceToReplaceId().equals(id)){
                            SavedShopAppliance newShopAppliance = new SavedShopAppliance();

                            newShopAppliance.setId(shopAppliance.getId());
                            newShopAppliance.setName(shopAppliance.getName());
                            newShopAppliance.setPrice(shopAppliance.getPrice());
                            newShopAppliance.setCategory(shopAppliance.getCategory());
                            newShopAppliance.setEfficiencyClass(shopAppliance.getEfficiencyClass());
                            newShopAppliance.setImageUrl(shopAppliance.getImageUrl());
                            newShopAppliance.setPower(shopAppliance.getPower());
                            newShopAppliance.setRoom(shopAppliance.getRoom());
                            newShopAppliance.setUrl(shopAppliance.getUrl());
                            newShopAppliance.setYearlyConsumption(shopAppliance.getYearlyConsumption());

                            getCurrentUsersConfigurations().child(customConfiguration.getId()).
                                    child("savedAppliances").child(String.valueOf(newShopAppliance.getId())).setValue(newShopAppliance);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseService", "Data is not available");
            }
        });
    }

}
