package com.example.wattescu.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.wattescu.async.Callback;
import com.example.wattescu.entities.Appliance;
import com.example.wattescu.entities.Bulb;
import com.example.wattescu.entities.Provider;
import com.example.wattescu.entities.Room;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FirebaseCurrentConfigurationService implements FirebaseProviderOperations,
        FirebaseBulbsOperations, FirebaseAppliancesOperations, FirebaseRoomsService{

    public static final String CURRENTCONFIGURATIONS_REFERENCE = "currentconfigurations";
    public static final double AVERAGE_PRICE_KW = 0.75;
    public static final String GENERAL_NAME = "-";
    public static final String PROVIDER_REFERENCE = "provider";
    public static final String BULBS_REFERENCE = "bulbs";
    public static final String APPLIANCES_REFERENCE = "appliances";
    public static final String ROOMS_REFERENCE = "rooms";

    private final DatabaseReference reference;
    private final String userKey;

    private static FirebaseCurrentConfigurationService firebaseCurrentConfigurationService;

    private FirebaseCurrentConfigurationService(String userKey) {
        reference = FirebaseDatabase.getInstance().getReference(CURRENTCONFIGURATIONS_REFERENCE);
        this.userKey = userKey;
    }

    public static FirebaseCurrentConfigurationService getInstance(String userKey) throws Exception {
        if (userKey == null || userKey.trim().isEmpty()) {
            throw new Exception("invalid user key");
        }
        if (firebaseCurrentConfigurationService == null) {
            synchronized (FirebaseCurrentConfigurationService.class) {
                if (firebaseCurrentConfigurationService == null) {
                    firebaseCurrentConfigurationService = new FirebaseCurrentConfigurationService(userKey);
                }
            }
        }
        return firebaseCurrentConfigurationService;
    }

    //get users configuration
    public DatabaseReference getConfiguration(){
        try{
        return reference.child(userKey);}
        catch (Exception ex){
            return null;
        }
    }

    //delete configuration
    public void deleteCurrentConfiguration() {
        reference.child(userKey).removeValue();
    }

    //create configuration
    public void createCurrentConfiguration() {
        reference.child(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    reference.child(userKey).child("id").setValue(userKey);
                    //add general provider
                    insertGeneralProvider();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //PROVIDER OPERATIONS
    //insert (create general provider with mean values)
    @Override
    public void insertGeneralProvider(){
        Provider provider = new Provider(GENERAL_NAME, AVERAGE_PRICE_KW);
        reference.child(userKey).child(PROVIDER_REFERENCE).setValue(provider);
    }
    //update (edit provider data)
    @Override
    public void updateProvider(Provider provider){
        if (provider == null) {
            return;
        }
        reference.child(userKey).child(PROVIDER_REFERENCE).setValue(provider);
    }
    //select (get provider data)
    @Override
    public void getProvider(Callback<Provider> callback){
        reference.child(userKey).child(PROVIDER_REFERENCE).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Provider provider = snapshot.getValue(Provider.class);
                if (provider!=null){
                    callback.runResultOnUiThread(provider);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseService", "Data is not avaiable");
            }
        });
    }

    //BULB OPERATIONS
    @Override
    public void insertBulb(Bulb bulb) {
        if(bulb == null || (bulb.getId() != null && !bulb.getId().trim().isEmpty())){
            return;
        }
        String id = reference.push().getKey();
        bulb.setId(id);
        getConfiguration().child(BULBS_REFERENCE).child(bulb.getId()).setValue(bulb);
    }

    @Override
    public void updateBulb(Bulb bulb) {
        if(bulb == null || bulb.getId() == null || bulb.getId().trim().isEmpty()){
            return;
        }
        getConfiguration().child(BULBS_REFERENCE).child(bulb.getId()).setValue(bulb);
    }

    @Override
    public void deleteBulb(Bulb bulb) {
        if(bulb == null || bulb.getId() == null || bulb.getId().trim().isEmpty()){
            return;
        }
        getConfiguration().child(BULBS_REFERENCE).child(bulb.getId()).removeValue();
    }

    @Override
    public void getBulbsDataChangeListener(Callback<List<Bulb>> callback) {
        getConfiguration().child(BULBS_REFERENCE).addValueEventListener(getBulbsValueEventListener(callback));
    }


    @Override
    public void getBulbsFiltered(Callback<List<Bulb>> callback, String filter) {
        Query query = getConfiguration().child(BULBS_REFERENCE).orderByChild("name").startAt(filter).endAt(filter+"\uf8ff");
        query.addValueEventListener(getBulbsValueEventListener(callback));
    }

    @Override
    public void getBulbsInRoom(Callback<List<Bulb>> callback, String roomKey) {
        Query query = getConfiguration().child(BULBS_REFERENCE).orderByChild("roomId").startAt(roomKey).endAt(roomKey+"\uf8ff");
        query.addValueEventListener(getBulbsValueEventListener(callback));
    }

    @Override
    public ValueEventListener getBulbsValueEventListener(Callback<List<Bulb>> callback) {
       return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Bulb> bulbs = new ArrayList<>();
                for(DataSnapshot data: snapshot.getChildren()){
                    Bulb bulb = data.getValue(Bulb.class);
                    bulbs.add(bulb);
                }
                callback.runResultOnUiThread(bulbs);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseService", "Data is not avaiable");
            }
        };
    }

    //APPLIANCE OPERATIONS
    @Override
    public void insertAppliance(Appliance appliance) {
        if(appliance == null || (appliance.getId() != null && !appliance.getId().trim().isEmpty())){
            return;
        }
        String id = reference.push().getKey();
        appliance.setId(id);
        getConfiguration().child(APPLIANCES_REFERENCE).child(appliance.getId()).setValue(appliance);
    }

    @Override
    public void updateAppliance(Appliance appliance) {
        if(appliance == null || appliance.getId() == null || appliance.getId().trim().isEmpty()){
            return;
        }
        getConfiguration().child(APPLIANCES_REFERENCE).child(appliance.getId()).setValue(appliance);
    }

    @Override
    public void deleteAppliance(Appliance appliance) {
        if(appliance == null || appliance.getId() == null || appliance.getId().trim().isEmpty()){
            return;
        }
        getConfiguration().child(APPLIANCES_REFERENCE).child(appliance.getId()).removeValue();
    }

    @Override
    public void getAppliancesDataChangeListener(Callback<List<Appliance>> callback) {
        getConfiguration().child(APPLIANCES_REFERENCE).addValueEventListener(getAppliancesValueEventListener(callback));
    }

    @Override
    public void getAppliancesFiltered(Callback<List<Appliance>> callback, String filter) {
        Query query = getConfiguration().child(APPLIANCES_REFERENCE).orderByChild("name").startAt(filter).endAt(filter+"\uf8ff");
        query.addValueEventListener(getAppliancesValueEventListener(callback));
    }

    @Override
    public void getAppliancesInRoom(Callback<List<Appliance>> callback, String roomKey) {
        Query query = getConfiguration().child(APPLIANCES_REFERENCE).orderByChild("roomId").startAt(roomKey).endAt(roomKey+"\uf8ff");
        query.addValueEventListener(getAppliancesValueEventListener(callback));
    }

    @Override
    public ValueEventListener getAppliancesValueEventListener(Callback<List<Appliance>> callback) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Appliance> appliances = new ArrayList<>();
                for(DataSnapshot data: snapshot.getChildren()){
                    Appliance appliance = data.getValue(Appliance.class);
                    appliances.add(appliance);
                }
                callback.runResultOnUiThread(appliances);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseService", "Data is not avaiable");
            }
        };
    }

    //ROOM OPERATIONS
    @Override
    public void insertRoom(Room room) {
        if(room == null || (room.getId() != null && !room.getId().trim().isEmpty())){
            return;
        }
        String id = reference.push().getKey();
        room.setId(id);
        getConfiguration().child(ROOMS_REFERENCE).child(room.getId()).setValue(room);
    }

    @Override
    public void updateRoom(Room room) {
        if(room == null || room.getId() == null || room.getId().trim().isEmpty()){
            return;
        }
        getConfiguration().child(ROOMS_REFERENCE).child(room.getId()).setValue(room);
    }

    @Override
    public void deleteRoom(Room room) {
        if(room == null || room.getId() == null || room.getId().trim().isEmpty()){
            return;
        }
        getConfiguration().child(ROOMS_REFERENCE).child(room.getId()).removeValue();
    }

    @Override
    public void getRoomsDataChangeListener(Callback<List<Room>> callback) {
        getConfiguration().child(ROOMS_REFERENCE).addValueEventListener(getRoomsValueEventListener(callback));

    }

    @Override
    public void getRoomsMapKeyNameListener(Callback<Map<String, String>> callback) {
        getConfiguration().child(ROOMS_REFERENCE).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String,String> roomKeys = new HashMap<>();
                for(DataSnapshot data: snapshot.getChildren()){
                    Room room = data.getValue(Room.class);
                    roomKeys.put(room.getId(), room.getName());
                }
                callback.runResultOnUiThread(roomKeys);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseService", "Data is not avaiable");
            }
        });
    }

    @Override
    public void getRoomsFiltered(Callback<List<Room>> callback, String filter) {
        Query query = getConfiguration().child(ROOMS_REFERENCE).orderByChild("name").startAt(filter).endAt(filter+"\uf8ff");
        query.addValueEventListener(getRoomsValueEventListener(callback));
    }

    @Override
    public ValueEventListener getRoomsValueEventListener(Callback<List<Room>> callback) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Room> rooms = new ArrayList<>();
                for(DataSnapshot data: snapshot.getChildren()){
                    Room room = data.getValue(Room.class);
                    rooms.add(room);
                }
                callback.runResultOnUiThread(rooms);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseService", "Data is not avaiable");
            }
        };
    }
}
