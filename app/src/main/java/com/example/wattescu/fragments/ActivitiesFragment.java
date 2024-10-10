package com.example.wattescu.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.agrawalsuneet.dotsloader.loaders.CircularDotsLoader;
import com.example.wattescu.R;
import com.example.wattescu.async.Callback;
import com.example.wattescu.firebase.FirebaseActivitiesService;
import com.example.wattescu.adapters.ActivitiesListViewAdapter;
import com.example.wattescu.entities.Activity;

import java.util.ArrayList;
import java.util.List;

public class ActivitiesFragment extends Fragment {

    //views
    private ListView lvActivities;
    private CircularDotsLoader circularDotsLoader;

    private FirebaseActivitiesService firebaseService;
    private List<Activity> activityList = new ArrayList<>();

    public ActivitiesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_activities, container, false);
        initComponents(view);
        return view;
    }

    private void initComponents(View view) {
        lvActivities = view.findViewById(R.id.fragment_activities_lv_activities);
        circularDotsLoader = view.findViewById(R.id.fragment_activities_progress_bar);
        if(getContext()!=null){
            circularDotsLoader.setVisibility(View.VISIBLE);
            ActivitiesListViewAdapter adapter = new ActivitiesListViewAdapter(getContext(),R.layout.lv_row_view_activities,activityList, getLayoutInflater());
            lvActivities.setAdapter(adapter);
            getActivitiesFromFirebase();
        }
    }

    public void notifyAdapter() {
        ArrayAdapter<Activity> adapter = (ArrayAdapter<Activity>) lvActivities.getAdapter();
        adapter.notifyDataSetChanged();

    }

    private void getActivitiesFromFirebase() {
        firebaseService = FirebaseActivitiesService.getInstance();
        firebaseService.attachDataChangedListener(activitisDataChangeCallback());
    }

    private Callback<List<Activity>> activitisDataChangeCallback() {
        return  new Callback<List<Activity>>() {
            @Override
            public void runResultOnUiThread(List<Activity> result) {
                if(result!=null){
                    activityList.clear();
                    activityList.addAll(result);
                    circularDotsLoader.setVisibility(View.GONE);
                    notifyAdapter();
                }
            }
        };
    }
}