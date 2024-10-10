package com.example.wattescu.fragments;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wattescu.ConfigurationSplashScreenActivity;
import com.example.wattescu.CustomConfigurationActivity;
import com.example.wattescu.R;
import com.example.wattescu.SearchAppliancesActivity;
import com.example.wattescu.enums.RoomType;


public class SearchFragment extends Fragment {


    public static final String ROOM = "room";
    //views
    private ImageView ivLivingRoom;
    private ImageView ivBathroom;
    private ImageView ivOffice;
    private ImageView ivKitchen;
    private TextView tvAll;
    private ImageView ivcustomConfiguration;


    public SearchFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        initComponents(view);
        return view;
    }

    private void initComponents(View view) {
        ivLivingRoom = view.findViewById(R.id.fragment_search_iv_living_room);
        ivOffice = view.findViewById(R.id.search_fragment_iv_office);
        ivKitchen = view.findViewById(R.id.search_fragment_iv_kitchen);
        ivBathroom = view.findViewById(R.id.fragment_search_iv__bathroom);
        ivcustomConfiguration = view.findViewById(R.id.search_fragment_iv_personalized_configuration);
        tvAll = view.findViewById(R.id.search_fragment_tv_all);
        
        if(getContext()!=null){
            initEvents();
        }
    }

    private void initEvents() {
        ivLivingRoom.setOnClickListener(getLivingRoomListener());
        ivOffice.setOnClickListener(getOfficeListener());
        ivKitchen.setOnClickListener(getKitchenListener());
        ivBathroom.setOnClickListener(getBathroomListener());
        ivcustomConfiguration.setOnClickListener(getCustomConfigurationListener());
        tvAll.setOnClickListener(getAllListener());


    }

    private View.OnClickListener getAllListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), SearchAppliancesActivity.class);
                openActivity(intent);
            }
        };
    }

    private View.OnClickListener getCustomConfigurationListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ConfigurationSplashScreenActivity.class);
                openActivity(intent);
            }
        };
    }

    private View.OnClickListener getBathroomListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSearchAppliancesActivity(String.valueOf(RoomType.bathroom));
            }
        };
    }

    private void startSearchAppliancesActivity(String room) {
        Intent intent = new Intent(getContext(), SearchAppliancesActivity.class);
        intent.putExtra(ROOM, room);
        openActivity(intent);
    }

    private View.OnClickListener getKitchenListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSearchAppliancesActivity(String.valueOf(RoomType.kitchen));
            }
        };

    }

    private View.OnClickListener getOfficeListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSearchAppliancesActivity(String.valueOf(RoomType.office));
            }
        };
    }

    private View.OnClickListener getLivingRoomListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSearchAppliancesActivity(String.valueOf(RoomType.living_room));
            }
        };
    }

    private void openActivity(Intent intent) {
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(getActivity());
        startActivity(intent, options.toBundle());
    }
}