package com.example.wattescu.firebase;

import com.example.wattescu.async.Callback;
import com.example.wattescu.entities.Room;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Map;

public interface FirebaseRoomsService {

    void insertRoom(Room room);

    void updateRoom(Room room);

    void deleteRoom(Room room);

    void getRoomsDataChangeListener(Callback<List<Room>> callback);

    void getRoomsMapKeyNameListener(Callback<Map<String, String>> callback);

    void getRoomsFiltered(Callback<List<Room>> callback, String filter);

    ValueEventListener getRoomsValueEventListener(Callback<List<Room>> callback);
}
