package com.example.wattescu.firebase;

import com.example.wattescu.async.Callback;
import com.example.wattescu.entities.Appliance;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public interface FirebaseAppliancesOperations {

    void insertAppliance(Appliance appliance);

    void updateAppliance(Appliance appliance);

    void deleteAppliance(Appliance appliance);

    void getAppliancesDataChangeListener(Callback<List<Appliance>> callback);

    void getAppliancesFiltered(Callback<List<Appliance>> callback, String filter);

    void getAppliancesInRoom(Callback<List<Appliance>> callback, String roomKey);

    ValueEventListener getAppliancesValueEventListener(Callback<List<Appliance>> callback);
}
