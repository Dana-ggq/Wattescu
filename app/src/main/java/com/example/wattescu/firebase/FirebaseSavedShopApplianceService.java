package com.example.wattescu.firebase;


import android.util.Log;

import androidx.annotation.NonNull;

import com.example.wattescu.async.Callback;
import com.example.wattescu.entities.Appliance;
import com.example.wattescu.entities.Room;
import com.example.wattescu.entities.SavedShopAppliance;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FirebaseSavedShopApplianceService {

    private static final String SAVED_APPLIANCES_REFERENCE = "saved_appliances";
    private final DatabaseReference reference;
    private final String userKey;

    private static FirebaseSavedShopApplianceService service;

    private FirebaseSavedShopApplianceService(String userKey) {
        reference = FirebaseDatabase.getInstance().getReference(SAVED_APPLIANCES_REFERENCE);
        this.userKey = userKey;
    }

    public static FirebaseSavedShopApplianceService getInstance(String userKey) throws Exception {
        if (userKey == null || userKey.trim().isEmpty()) {
            throw new Exception("invalid user key");
        }
        if (service == null) {
            synchronized (FirebaseCurrentConfigurationService.class) {
                if (service == null) {
                    service = new FirebaseSavedShopApplianceService(userKey);
                }
            }
        }
        return service;
    }

    //get users configuration
    public DatabaseReference getCurrentUsersSavedAppliancesReference(){
        try{
            return reference.child(userKey);}
        catch (Exception ex){
            return null;
        }
    }

    //delete saved
    public void deleteAllSavedAppliances() {
        getCurrentUsersSavedAppliancesReference().removeValue();
    }

    //insert appliance
    public void insertShopAppliance(SavedShopAppliance savedShopAppliance) {
        if(savedShopAppliance == null || (savedShopAppliance.getId() == -1)){
            return;
        }
        getCurrentUsersSavedAppliancesReference().child(String.valueOf(savedShopAppliance.getId())).setValue(savedShopAppliance);
    }

    //remove appliance
    public void removeAppliance(SavedShopAppliance savedShopAppliance) {
        if(savedShopAppliance == null || savedShopAppliance.getId() == -1){
            return;
        }
        getCurrentUsersSavedAppliancesReference().child(String.valueOf(savedShopAppliance.getId())).removeValue();
    }

    //select
    public void getSavedShopAppliancesListener(Callback<List<SavedShopAppliance>> callback) {
        getCurrentUsersSavedAppliancesReference().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<SavedShopAppliance> savedShopApplianceList = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    SavedShopAppliance shopAppliance = data.getValue(SavedShopAppliance.class);
                    savedShopApplianceList.add(shopAppliance);
                }
                callback.runResultOnUiThread(savedShopApplianceList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseService", "Data is not available");
            }
        });
    }

    public void removeApplianceToReplace(String filter) {
        Query query = getCurrentUsersSavedAppliancesReference().orderByChild("applianceToReplaceId").startAt(filter).endAt(filter+"\uf8ff");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    SavedShopAppliance shopAppliance = data.getValue(SavedShopAppliance.class);
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

                    getCurrentUsersSavedAppliancesReference().child(String.valueOf(shopAppliance.getId())).setValue(newShopAppliance);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseService", "Data is not available");
            }
        });
    }


}
