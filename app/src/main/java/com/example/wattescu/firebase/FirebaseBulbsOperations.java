package com.example.wattescu.firebase;

import com.example.wattescu.async.Callback;
import com.example.wattescu.entities.Bulb;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public interface FirebaseBulbsOperations {

    void insertBulb(Bulb bulb);

    void updateBulb(Bulb bulb);

    void deleteBulb(Bulb bulb);

    void getBulbsDataChangeListener(Callback<List<Bulb>> callback);

    void getBulbsFiltered(Callback<List<Bulb>> callback, String filter);

    void getBulbsInRoom(Callback<List<Bulb>> callback, String roomKey);

    ValueEventListener getBulbsValueEventListener(Callback<List<Bulb>> callback);

}
