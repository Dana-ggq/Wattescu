package com.example.wattescu.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.wattescu.async.Callback;
import com.example.wattescu.entities.Activity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FirebaseActivitiesService {

        public static final String ACTIVITIES_REFERENCE = "activities";
        private final DatabaseReference reference;

        private static FirebaseActivitiesService firebaseService;

        private FirebaseActivitiesService() {
            reference = FirebaseDatabase.getInstance().getReference(ACTIVITIES_REFERENCE);
        }

        public static FirebaseActivitiesService getInstance() {
            if (firebaseService == null) {
                synchronized (FirebaseActivitiesService.class){
                    if(firebaseService==null){
                        firebaseService = new FirebaseActivitiesService();
                    }
                }
            }
            return firebaseService;
        }


        //select
        public void attachDataChangedListener(Callback<List<Activity>> callback){
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Activity> activityList = new ArrayList<>();
                    for(DataSnapshot data: snapshot.getChildren()){
                        Activity activity = data.getValue(Activity.class);
                        activityList.add(activity);
                    }
                    callback.runResultOnUiThread(activityList);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("FirebaseService", "Data is not avaiable");
                }
            });
        }

}
