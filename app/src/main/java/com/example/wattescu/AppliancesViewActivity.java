package com.example.wattescu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.wattescu.async.Callback;
import com.example.wattescu.firebase.FirebaseCurrentConfigurationService;
import com.example.wattescu.firebase.FirebaseCustomConfigurationService;
import com.example.wattescu.firebase.FirebaseSavedShopApplianceService;
import com.example.wattescu.fragments.dialogs.ApplianceFormDialog;
import com.example.wattescu.adapters.AppliancesViewAdapter;
import com.example.wattescu.entities.Appliance;
import com.example.wattescu.entities.Provider;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppliancesViewActivity extends AppCompatActivity implements ApplianceFormDialog.OnInputListener{

    private List<Appliance> appliances;
    private Map<String, String> roomkeys;
    private String currentRoomKey;
    private String currentRoomName;

    //views
    private CheckBox cbSort;
    private ImageView ivSort;
    private ListView lvAppliances;
    private ImageView ivAddAppliance;
    private TextInputEditText tietFilter;
    private TextView tvTitle;

    //sort order
    private boolean sortOrderAscending = true;

    //firebase
    private FirebaseSavedShopApplianceService firebaseSavedShopApplianceService;
    private FirebaseCustomConfigurationService firebaseCustomConfigurationService;
    private FirebaseCurrentConfigurationService firebaseCurrentConfigurationService;
    private FirebaseAuth firebaseAuth;
    private String currentUserUID;
    private int selectedAppliance = -1;
    private Double price = 0.0;

    //lv ops
    private Animation animFabOpen, animFabClose;
    private FloatingActionButton fabClose;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableTransitions();
        setContentView(R.layout.activity_appliences_view);
        getCurrentRoomKey();
        appliances = new ArrayList<>();
        roomkeys = new HashMap<>();
        initComponents();
        initFirebase();
    }

    private void getCurrentRoomKey() {
        Intent intent = getIntent();
        if(intent.hasExtra(RoomsViewActivity.ROOM_KEY) && intent.hasExtra(RoomsViewActivity.ROOM_NAME)){
            currentRoomKey = intent.getStringExtra(RoomsViewActivity.ROOM_KEY);
            currentRoomName = intent.getStringExtra(RoomsViewActivity.ROOM_NAME);
        }
    }

    private void initFirebase() {
        //get current user
        try {
            firebaseAuth = FirebaseAuth.getInstance();
            currentUserUID = firebaseAuth.getCurrentUser().getUid();
            firebaseSavedShopApplianceService = FirebaseSavedShopApplianceService.getInstance(currentUserUID);
            firebaseCurrentConfigurationService = FirebaseCurrentConfigurationService.getInstance(currentUserUID);
            firebaseCustomConfigurationService = FirebaseCustomConfigurationService.getInstance(currentUserUID);
            firebaseCurrentConfigurationService.getProvider(getProviderCallback());
            if(currentRoomKey == null){
                firebaseCurrentConfigurationService.getAppliancesDataChangeListener(applianceDataChangeCallback());
            } else {
                firebaseCurrentConfigurationService.getAppliancesInRoom(applianceDataChangeCallback(),currentRoomKey);
            }
            firebaseCurrentConfigurationService.getRoomsMapKeyNameListener(getRoomKeysListener());
        } catch (Exception exception){
            exception.printStackTrace();
            //this.finish();
        }

    }

    private Callback<Map<String, String>> getRoomKeysListener() {
        return new Callback<Map<String, String>>() {
            @Override
            public void runResultOnUiThread(Map<String, String> result) {
                roomkeys.clear();
                if (result != null) {
                    for (String key :
                            result.keySet()) {
                        roomkeys.put(key, result.get(key));
                    }
                }
            }
        };
    }

    private Callback<Provider> getProviderCallback() {
        return new Callback<Provider>() {
            @Override
            public void runResultOnUiThread(Provider result) {
                if(result!=null) {
                    price = result.getPriceKw();
                    AppliancesViewAdapter adapter = (AppliancesViewAdapter) lvAppliances.getAdapter();
                    adapter.setPriceKW(price);
                    notifyLvAdapter();
                }
            }
        };
    }
    
    private Callback<List<Appliance>> applianceDataChangeCallback() {
        return new Callback<List<Appliance>>() {
            @Override
            public void runResultOnUiThread(List<Appliance> result) {
                if(result!=null){
                    appliances.clear();
                    appliances.addAll(result);
                    notifyLvAdapter();
                    selectedAppliance = -1;
                }
            }
        };
    }

    private void notifyLvAdapter() {
        ArrayAdapter adapter = (ArrayAdapter) lvAppliances.getAdapter();
        adapter.notifyDataSetChanged();
    }
    
    private void initComponents() {
        tvTitle = findViewById(R.id.appliances_view_tv_title);
        setTitle();
        cbSort = findViewById(R.id.appliances_view_cb_sort);
        ivSort = findViewById(R.id.appliances_view_iv_sort_order);
        lvAppliances = findViewById(R.id.appliances_view_lv_appliances);
        addLvAppliancesAdapter();
        ivAddAppliance = findViewById(R.id.appliances_view_iv_add_appliance);
        tietFilter = findViewById(R.id.appliances_view_tiet_filter);
        initAnimations();
        initEvents();
    }

    private void initAnimations() {
        animFabOpen = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_lv_open);
        animFabClose = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_lv_close);
    }

    private void addLvAppliancesAdapter() {
        try {
            AppliancesViewAdapter adapter = new AppliancesViewAdapter(getApplicationContext(), R.layout.lv_row_view_appliances,
                    appliances, price, getLayoutInflater());
            lvAppliances.setAdapter(adapter);
        }catch (Exception exception){
            Log.e("err_adapter", appliances.toString());
        }
    }

    private void setTitle() {
        if(currentRoomKey!=null){
            tvTitle.setText(getString(R.string.format_appliances_view_title, currentRoomName));
        }
    }

    private void initEvents() {
        //sort
        cbSort.setOnCheckedChangeListener(getCbSortCheckedChangeListener());
        ivSort.setOnClickListener(getIvSortClickListener());

        //list
        //delete
        //edit
        lvAppliances.setOnItemLongClickListener(getLongItemClickLvListener());

        //open add dialog
        ivAddAppliance.setOnClickListener(getOpenAddDialogListener());

        //filter
        tietFilter.addTextChangedListener(getTextChangedFilterListener());
        tietFilter.setOnFocusChangeListener(getTietFocusChangeListener());

    }

    private View.OnFocusChangeListener getTietFocusChangeListener() {
        return new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    closeLvItemMenu();
                }
            }
        };
                
    }
    
    private void closeLvItemMenu() {
        if (fabClose != null) {
            selectedAppliance = -1;
            fabClose.performClick();
        }
    }

    private TextWatcher getTextChangedFilterListener() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                cbSort.setChecked(false);
                if(tietFilter.getText()!=null && tietFilter.getText().toString().trim().length() >=2 ){
                    if(currentRoomKey == null){
                        firebaseCurrentConfigurationService.getAppliancesFiltered(applianceDataChangeCallback(),tietFilter.getText().toString().trim());
                    } else {
                        firebaseCurrentConfigurationService.getAppliancesFiltered(applianceFilteredInRoomCallback(), tietFilter.getText().toString().trim());
                    }
                    notifyLvAdapter();
                } else {
                    if(currentRoomKey == null){
                        firebaseCurrentConfigurationService.getAppliancesDataChangeListener(applianceDataChangeCallback());
                    } else {
                        firebaseCurrentConfigurationService.getAppliancesInRoom(applianceDataChangeCallback(),currentRoomKey);
                    }
                    notifyLvAdapter();
                }
            }
        };
    }

    private Callback<List<Appliance>> applianceFilteredInRoomCallback() {
        return new Callback<List<Appliance>>() {
            @Override
            public void runResultOnUiThread(List<Appliance> result) {
                if(result!=null){
                    appliances.clear();
                    for (Appliance appliance:
                            result) {
                        if(currentRoomKey!=null && appliance.getRoomId().equals(currentRoomKey)){
                            appliances.add(appliance);
                        }
                    }
                    notifyLvAdapter();
                    selectedAppliance = -1;
                }
            }
        };
    }

    private View.OnClickListener getOpenAddDialogListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeLvItemMenu();
                ApplianceFormDialog applianceFormDialog = ApplianceFormDialog.newInstance(null, roomkeys, currentRoomKey);
                applianceFormDialog.show(getSupportFragmentManager(),getString(R.string.appliance_tag));
            }
        };
    }

    @NonNull
    private AdapterView.OnItemLongClickListener getLongItemClickLvListener() {
        return new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                closeLvItemMenu();

                selectedAppliance = position;

                fabClose = view.findViewById(R.id.lv_row_appliances_fab_close);
                FloatingActionButton fabDelete = view.findViewById(R.id.lv_row_appliances_fab_delete);
                FloatingActionButton fabEdit = view.findViewById(R.id.lv_row_appliances_fab_edit);

                openLvItemMenu(view, fabDelete, fabEdit);
                setLvItemMenuEvents(view, position, fabDelete, fabEdit);
                return true;
            }
        };
    }

    private void setLvItemMenuEvents(View view, int position, FloatingActionButton fabDelete, FloatingActionButton fabEdit) {
        fabClose.setOnClickListener(getFabCloseListener(fabClose, fabEdit, fabDelete, view));
        fabEdit.setOnClickListener(getLvItemMenuEditListener(position));
        fabDelete.setOnClickListener(getLvItemMenuDeleteListener(position));
    }
    private View.OnClickListener getLvItemMenuDeleteListener(int position) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AppliancesViewActivity.this)
                        .setTitle(R.string.delete_appliance_title)
                        .setMessage(R.string.msg_delete_appliance)
                        .setPositiveButton(getString(R.string.dialog_msg_sterge), getDeleteAppliancePositiveListener(position))
                        .setNegativeButton(R.string.login_dialog_reset_password_cancel, getDeleteApplianceNegativeListener());
                builder.create().show();
            }
        };
    }
   
    
    private DialogInterface.OnClickListener getDeleteApplianceNegativeListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                closeLvItemMenu();
            }
        };
    }

    private DialogInterface.OnClickListener getDeleteAppliancePositiveListener(int position) {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //delete appliance from database
                if(position != -1){
                    // TODO: 5/15/2022 delete appliance from saved appliances and configurations
                    firebaseSavedShopApplianceService.removeApplianceToReplace(appliances.get(position).getId());
                    firebaseCustomConfigurationService.removeApplianceToReplace(appliances.get(position).getId());

                    firebaseCurrentConfigurationService.deleteAppliance(appliances.get(position));
                }
                closeLvItemMenu();
            }
        };
    }

    private View.OnClickListener getLvItemMenuEditListener(int position) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ApplianceFormDialog applianceFormDialog = ApplianceFormDialog.newInstance(appliances.get(position), roomkeys, currentRoomKey);
                applianceFormDialog.show(getSupportFragmentManager(),getString(R.string.appliance_tag));
            }
        };
    }

    private void openLvItemMenu(View view, FloatingActionButton fabDelete, FloatingActionButton fabEdit) {
        fabEdit.setVisibility(View.VISIBLE);
        fabClose.setVisibility(View.VISIBLE);
        fabDelete.setVisibility(View.VISIBLE);

        fabClose.startAnimation(animFabOpen);
        fabDelete.startAnimation(animFabOpen);
        fabEdit.startAnimation(animFabOpen);
        view.findViewById(R.id.lv_row_appliances_cl).setAlpha(0.4f);
    }

    private View.OnClickListener getFabCloseListener(FloatingActionButton f1, FloatingActionButton f2, FloatingActionButton f3, View view) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedAppliance = -1;
                closeLvItemMenuOperations(f1, f2, f3, view);

                fabClose = null;

            }
        };
    }


    private void closeLvItemMenuOperations(FloatingActionButton fabClose, FloatingActionButton fabDelete, FloatingActionButton fabEdit, View view) {
        fabClose.startAnimation(animFabClose);
        fabDelete.startAnimation(animFabClose);
        fabEdit.startAnimation(animFabClose);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                fabEdit.setVisibility(View.GONE);
                fabClose.setVisibility(View.GONE);
                fabDelete.setVisibility(View.GONE);
                view.findViewById(R.id.lv_row_appliances_cl).setAlpha(1f);

            }
        }, 400);
    }
    
    private View.OnClickListener getIvSortClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cbSort.isChecked()) {
                    if (sortOrderAscending) {
                        ivSort.setImageResource(R.drawable.ic_arrow_downward);
                        sortAppliancesDescending();
                        sortOrderAscending = false;
                        notifyLvAdapter();
                    } else {
                        ivSort.setImageResource(R.drawable.ic_arrow_upward);
                        sortAppliancesAscending();
                        sortOrderAscending = true;
                        notifyLvAdapter();
                    }
                }
            }
        };
    }

    private CompoundButton.OnCheckedChangeListener getCbSortCheckedChangeListener() {
        return new CompoundButton.OnCheckedChangeListener() {
            @SuppressLint("NewApi")
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    cbSort.setTextColor(getColor(R.color.coral));
                    ivSort.setColorFilter(getColor(R.color.coral));
                    if(sortOrderAscending){
                        sortAppliancesAscending();
                        notifyLvAdapter();
                    } else {
                        sortAppliancesDescending();
                        notifyLvAdapter();
                    }
                } else {
                    cbSort.setTextColor(getColor(R.color.dark_gray));
                    ivSort.setColorFilter(getColor(R.color.dark_gray));
                    if(currentRoomKey == null){
                        firebaseCurrentConfigurationService.getAppliancesDataChangeListener(applianceDataChangeCallback());
                    } else {
                        firebaseCurrentConfigurationService.getAppliancesInRoom(applianceDataChangeCallback(),currentRoomKey);
                    }
                }
            }
        };
    }

    @SuppressLint("NewApi")
    private void sortAppliancesDescending() {
        appliances.sort(((o1, o2) -> {
            if(o1.getAverageMonthlyConsumption() < o2.getAverageMonthlyConsumption()) return 1;
            else if(o1.getAverageMonthlyConsumption() > o2.getAverageMonthlyConsumption()) return -1;
            else return 0;
        }));
    }

    @SuppressLint("NewApi")
    private void sortAppliancesAscending() {
        appliances.sort(((o1, o2) -> {
            if(o1.getAverageMonthlyConsumption() < o2.getAverageMonthlyConsumption()) return -1;
            else if(o1.getAverageMonthlyConsumption() > o2.getAverageMonthlyConsumption()) return 1;
            else return 0;
        }));
    }

    private void enableTransitions() {
        //enable transition
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        // set transitions
        Slide slide = new Slide();
        slide.setSlideEdge(Gravity.BOTTOM);
        slide.setInterpolator(new DecelerateInterpolator());
        getWindow().setEnterTransition(slide);
        getWindow().setExitTransition(slide);
    }

    @Override
    public void sendInputToActivity(Appliance appliance) {
        //set input to widgets
        if(selectedAppliance == -1){
            //insert
            firebaseCurrentConfigurationService.insertAppliance(appliance);
        } else{
            //update
            firebaseCurrentConfigurationService.updateAppliance(appliance);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof TextInputEditText || v instanceof TextInputLayout) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }
}