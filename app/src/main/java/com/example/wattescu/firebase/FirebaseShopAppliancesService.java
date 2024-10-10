package com.example.wattescu.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.wattescu.async.Callback;
import com.example.wattescu.entities.Room;
import com.example.wattescu.entities.ShopAppliance;
import com.example.wattescu.enums.CategoryFilterType;
import com.example.wattescu.enums.RoomType;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FirebaseShopAppliancesService {

    public static final String SHOP_APPLIANCE_REFERENCE = "appliances";
    private final DatabaseReference reference;

    private static FirebaseShopAppliancesService firebaseService;

    private FirebaseShopAppliancesService() {
        reference = FirebaseDatabase.getInstance().getReference(SHOP_APPLIANCE_REFERENCE);
    }

    public static FirebaseShopAppliancesService getInstance() {
        if (firebaseService == null) {
            synchronized (FirebaseShopAppliancesService.class){
                if(firebaseService==null){
                    firebaseService = new FirebaseShopAppliancesService();
                }
            }
        }
        return firebaseService;
    }


    public void attachDataChangedListener(Callback<List<ShopAppliance>> callback, RoomType roomType, CategoryFilterType category){
        if(RoomType.ALL != roomType && category != CategoryFilterType.NONE){
            //filter room+category
            String filterString = String.valueOf(roomType)+"-"+String.valueOf(category);
            Query query = reference.orderByChild("filter_room_category").startAt(filterString).endAt(filterString+"\uf8ff");
            query.removeEventListener(getListener(callback));
            query.addValueEventListener(getListener(callback));
        } else if (category == CategoryFilterType.NONE){
            if(RoomType.ALL != roomType) {
                //filter room
                Query query = reference.orderByChild("room").startAt(String.valueOf(roomType)).endAt(String.valueOf(roomType) + "\uf8ff");
                query.removeEventListener(getListener(callback));
                query.addValueEventListener(getListener(callback));
            }else{
                reference.removeEventListener(getListener(callback));
                reference.addValueEventListener(getListener(callback));
            }
        }else{
            //filter category
            Query query = reference.orderByChild("category").startAt(String.valueOf(category)).endAt(String.valueOf(category) + "\uf8ff");
            query.removeEventListener(getListener(callback));
            query.addValueEventListener(getListener(callback));
        }
    }


    @NonNull
    private ValueEventListener getListener(Callback<List<ShopAppliance>> callback) {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<ShopAppliance> shopApplianceList = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    ShopAppliance shopAppliance = data.getValue(ShopAppliance.class);
                    shopApplianceList.add(shopAppliance);
                }
                callback.runResultOnUiThread(shopApplianceList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseService", "Data is not avaiable");
            }
        };
    }

}
