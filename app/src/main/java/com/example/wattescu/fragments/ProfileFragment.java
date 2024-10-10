package com.example.wattescu.fragments;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.agrawalsuneet.dotsloader.loaders.CircularDotsLoader;
import com.example.wattescu.AppliancesViewActivity;
import com.example.wattescu.BulbsViewActivity;
import com.example.wattescu.R;
import com.example.wattescu.RoomsViewActivity;
import com.example.wattescu.SettingsActivity;
import com.example.wattescu.async.Callback;
import com.example.wattescu.firebase.FirebaseCurrentConfigurationService;
import com.example.wattescu.util.CurrentConfigurationConsumption;
import com.example.wattescu.entities.Appliance;
import com.example.wattescu.entities.Bulb;
import com.example.wattescu.entities.Provider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment{

    public static final String USERS_REFERENCE = "users";

    private List<Bulb> bulbs;
    private List<Appliance> appliances;
    private Double priceKw = 0.0;

    //views
    private ImageButton imgBtnSetting;
    private TextView tvUser;
    private TextView tvTodayConsumption;
    private TextView tvThisMonthConsumtion;
    private TextView tvThisMonthSpendings;
    private TextView tvThisMonthCo2Emissions;

    //bulbs
    private ImageView ivBulbs;
    //rooms
    private ImageView ivRooms;
    //appliences
    private  ImageView ivAppliences;

    //measure temperatures
    private TextView tvInsideTemperature;
    private SensorManager sensorManager;
    private Sensor temperatureSensor;
    private boolean isTemperatureSensor = false;


    //firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseCurrentConfigurationService firebaseCurrentConfigurationService;
    String currentUserid;

    private CircularDotsLoader progressBar;

    public ProfileFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        initComponents(view);
        return view;
    }

    private void initComponents(View view) {
        tvUser = view.findViewById(R.id.fragment_profile_tv_username);
        tvThisMonthCo2Emissions = view.findViewById(R.id.fragment_profile_tv_co2);
        tvThisMonthConsumtion = view.findViewById(R.id.fragment_profile_tv_monthly_consumtion);
        tvThisMonthSpendings = view.findViewById(R.id.fragment_profile_tv_monthly_spendings);
        tvTodayConsumption = view.findViewById(R.id.fragment_profile_tv_todays_consumption);
        imgBtnSetting = view.findViewById(R.id.profile_ib_settings);
        ivBulbs =view.findViewById(R.id.fragment_profile_iv_view_bulbs);
        ivAppliences = view.findViewById(R.id.fragment_profile_iv_view_appliences);
        ivRooms = view.findViewById(R.id.fragment_profile_iv_view_rooms);
        tvInsideTemperature = view.findViewById(R.id.fragment_profile_tv_room_temperature);
        progressBar = view.findViewById(R.id.fragment_profile_progress_bar);
        if (getContext() != null) {
            appliances = new ArrayList<>();
            bulbs = new ArrayList<>();

            initFirebaseOperations();
            //set username
            setUsername();
            //init sensor
            initSensor();
            //init events
            initEvents();
        }
    }

    private void initFirebaseOperations() {
        try {
            progressBar.setVisibility(View.VISIBLE);
            //get current user id
            firebaseAuth = FirebaseAuth.getInstance();
            currentUserid = firebaseAuth.getCurrentUser().getUid();
            //init firebase configuration service
            firebaseCurrentConfigurationService = FirebaseCurrentConfigurationService.getInstance(currentUserid);
            firebaseCurrentConfigurationService.getProvider(getProviderCallback());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private Callback<Provider> getProviderCallback() {
        return new Callback<Provider>() {
            @Override
            public void runResultOnUiThread(Provider result) {
                progressBar.setVisibility(View.VISIBLE);
                if(result!=null){
                    priceKw = result.getPriceKw();
                }
                firebaseCurrentConfigurationService.getAppliancesDataChangeListener(getAppliancerCallback());
            }
        };
    }

    private Callback<List<Appliance>> getAppliancerCallback() {
        return new Callback<List<Appliance>>() {
            @Override
            public void runResultOnUiThread(List<Appliance> result) {
                    if(result!=null){
                        appliances.clear();
                        appliances.addAll(result);
                        firebaseCurrentConfigurationService.getBulbsDataChangeListener(getBulbsCallback());
                    }
            }
        };
    }

    private Callback<List<Bulb>> getBulbsCallback() {
        return new Callback<List<Bulb>>() {
            @Override
            public void runResultOnUiThread(List<Bulb> result) {
                if(result!=null){
                    bulbs.clear();
                    bulbs.addAll(result);
                    initConsumptionTextViews();
                }
                progressBar.setVisibility(View.GONE);
            }
        };
    }

    private void initConsumptionTextViews() {
        tvTodayConsumption.setText(getString(R.string.xx_kwh, CurrentConfigurationConsumption.getDayOfWeekConsumption(bulbs,appliances)));
        tvThisMonthConsumtion.setText(getString(R.string.xx_kwh, CurrentConfigurationConsumption.getMonthlyConsumption(bulbs,appliances)));
        tvThisMonthSpendings.setText(getString(R.string.xxx_lei, CurrentConfigurationConsumption.getMonthlySpendings(bulbs,appliances,priceKw)));
        tvThisMonthCo2Emissions.setText(String.valueOf(Math.round(CurrentConfigurationConsumption.getMonthlyCo2Emissions(bulbs,appliances))));
    }

    private void setUsername() {
        //set usernmae
        FirebaseDatabase.getInstance().getReference(USERS_REFERENCE)
                .child(currentUserid)
                .child("username")
                .addValueEventListener(getUsernameEventListener());
    }

    private ValueEventListener getUsernameEventListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    tvUser.setText(getActivity().getString(R.string.hello_user_message, snapshot.getValue(String.class)));
                    progressBar.setVisibility(View.GONE);
                } catch (Exception ex){
                    setUsername();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                tvUser.setText(getString(R.string.empty));
                progressBar.setVisibility(View.GONE);
            }
        };
    }

    private void initEvents() {
        imgBtnSetting.setOnClickListener(getIbSettingsListener());
        ivBulbs.setOnClickListener(getViewBulbsListener());
        ivRooms.setOnClickListener(getViewRoomsListener());
        ivAppliences.setOnClickListener(getViewAppliencesListener());
    }

    private View.OnClickListener getViewAppliencesListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext().getApplicationContext(), AppliancesViewActivity.class);
                openActivity(intent);
            }
        };
    }

    private View.OnClickListener getViewRoomsListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext().getApplicationContext(), RoomsViewActivity.class);
                openActivity(intent);
            }
        };
    }


    private View.OnClickListener getViewBulbsListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext().getApplicationContext(), BulbsViewActivity.class);
                openActivity(intent);
            }
        };
    }

    private void openActivity(Intent intent) {
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(getActivity());
        startActivity(intent, options.toBundle());
    }

    private void initSensor() {
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) != null) {
            temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
            isTemperatureSensor = true;
        }
        if (isTemperatureSensor == true) {
            sensorManager.registerListener(getTemperatureSensorListener(), temperatureSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private SensorEventListener getTemperatureSensorListener() {
        return new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                try {
                    tvInsideTemperature.setText(getString(R.string.degrees_format, event.values[0]));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) { }
        };
    }

    private View.OnClickListener getIbSettingsListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext().getApplicationContext(), SettingsActivity.class);
                openActivity(intent);
            }
        };
    }


    @Override
    public void onResume() {
        super.onResume();
        //for temperature sensor
        if (isTemperatureSensor == true) {
            sensorManager.registerListener(getTemperatureSensorListener(), temperatureSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(isTemperatureSensor == true){
            sensorManager.unregisterListener(getTemperatureSensorListener());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(isTemperatureSensor == true){
            sensorManager.unregisterListener(getTemperatureSensorListener());
        }
    }
}