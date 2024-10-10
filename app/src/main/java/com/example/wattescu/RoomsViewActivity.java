package com.example.wattescu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
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

import com.example.wattescu.async.Callback;
import com.example.wattescu.firebase.FirebaseCurrentConfigurationService;
import com.example.wattescu.fragments.dialogs.RoomFormDialog;
import com.example.wattescu.adapters.RoomsListViewAdapter;
import com.example.wattescu.entities.Appliance;
import com.example.wattescu.entities.Bulb;
import com.example.wattescu.entities.Provider;
import com.example.wattescu.entities.Room;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class RoomsViewActivity extends AppCompatActivity implements RoomFormDialog.OnInputListener {

    public static final String ROOM_KEY = "ROOM_KEY";
    public static final String ROOM_NAME = "ROOM_NAME";
    private List<Bulb> bulbs;
    private List<Appliance> appliances;
    private List<Room> rooms;

    //views
    private CheckBox cbSort;
    private ImageView ivSort;
    private ListView lvRooms;
    private ImageView ivAddRoom;
    private TextInputEditText tietFilter;

    //sort order
    private boolean sortOrderAscending = true;

    //firebase
    private FirebaseCurrentConfigurationService firebaseCurrentConfigurationService;
    private FirebaseAuth firebaseAuth;
    private String currentUserUID;
    private int selectedRoom = -1;
    private Double price = 0.0;

    //lv ops
    private Animation animFabOpen, animFabClose;
    private FloatingActionButton fabClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableTransitions();
        setContentView(R.layout.activity_rooms_view);
        bulbs = new ArrayList<>();
        appliances = new ArrayList<>();
        rooms = new ArrayList<>();
        initComponents();
        initFirebase();
    }

    private void initFirebase() {
        //get current user
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserUID = firebaseAuth.getCurrentUser().getUid();
        try {
            firebaseCurrentConfigurationService = FirebaseCurrentConfigurationService.getInstance(currentUserUID);
            firebaseCurrentConfigurationService.getProvider(getProviderCallback());
            firebaseCurrentConfigurationService.getBulbsDataChangeListener(bulbDataChangeCallback());
            firebaseCurrentConfigurationService.getAppliancesDataChangeListener(applianceDataChangeCallback());
            firebaseCurrentConfigurationService.getRoomsDataChangeListener(roomDataChangeCallback());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private Callback<List<Room>> roomDataChangeCallback() {
        return new Callback<List<Room>>() {
            @Override
            public void runResultOnUiThread(List<Room> result) {
                if (result != null) {
                    rooms.clear();
                    rooms.addAll(result);
                    notifyLvAdapter();
                    selectedRoom = -1;
                }
            }
        };
    }

    private Callback<List<Appliance>> applianceDataChangeCallback() {
        return new Callback<List<Appliance>>() {
            @Override
            public void runResultOnUiThread(List<Appliance> result) {
                if (result != null) {
                    appliances.clear();
                    appliances.addAll(result);
                    RoomsListViewAdapter adapter = (RoomsListViewAdapter) lvRooms.getAdapter();
                    adapter.setAppliances(appliances);
                    notifyLvAdapter();
                }
            }
        };
    }

    private void notifyLvAdapter() {
        ArrayAdapter adapter = (ArrayAdapter) lvRooms.getAdapter();
        adapter.notifyDataSetChanged();
    }

    private Callback<List<Bulb>> bulbDataChangeCallback() {
        return new Callback<List<Bulb>>() {
            @Override
            public void runResultOnUiThread(List<Bulb> result) {
                if (result != null) {
                    bulbs.clear();
                    bulbs.addAll(result);
                    RoomsListViewAdapter adapter = (RoomsListViewAdapter) lvRooms.getAdapter();
                    adapter.setBulbs(bulbs);
                    notifyLvAdapter();
                }
            }
        };
    }

    private Callback<Provider> getProviderCallback() {
        return new Callback<Provider>() {
            @Override
            public void runResultOnUiThread(Provider result) {
                price = result.getPriceKw();
                RoomsListViewAdapter adapter = (RoomsListViewAdapter) lvRooms.getAdapter();
                adapter.setPriceKW(price);
                notifyLvAdapter();
            }
        };
    }

    private void initComponents() {
        cbSort = findViewById(R.id.rooms_view_cb_sort);
        ivSort = findViewById(R.id.rooms_view_iv_sort_order);
        lvRooms = findViewById(R.id.rooms_view_lv_rooms);
        addLvRoomsAdapter();
        ivAddRoom = findViewById(R.id.rooms_view_iv_add_room);
        tietFilter = findViewById(R.id.rooms_view_tiet_filter);
        initEvents();
        initAnimations();
    }

    private void addLvRoomsAdapter() {
        try {
            RoomsListViewAdapter adapter = new RoomsListViewAdapter(getApplicationContext(), R.layout.lv_row_view_room,
                    rooms, price, bulbs, appliances, getLayoutInflater());
            lvRooms.setAdapter(adapter);
        }catch (Exception exception){
            Log.e("err_adapter", bulbs.toString());
        }
    }

    private void initAnimations() {
        animFabOpen = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_lv_open);
        animFabClose = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_lv_close);
    }

    private void initEvents() {
        //sort
        cbSort.setOnCheckedChangeListener(getCbSortCheckedChangeListener());
        ivSort.setOnClickListener(getIvSortClickListener());

        //list
        //delete
        //edit
        lvRooms.setOnItemLongClickListener(getLongItemClickLvListener());
        //view bulbs/appliances
        lvRooms.setOnItemClickListener(getItemClickLvListener());

        //open add dialog
        ivAddRoom.setOnClickListener(getOpenAddDialogListener());

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

    private AdapterView.OnItemClickListener getItemClickLvListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                View roomDetailsDialogView = getLayoutInflater().inflate(R.layout.room_details_dialog,null);
                AlertDialog.Builder builder = new AlertDialog.Builder(RoomsViewActivity.this, R.style.MyAlertDialog)
                        .setView(roomDetailsDialogView);
                AlertDialog roomDialog = builder.create();
                roomDialog.show();
                //views from dialog
                ImageView ivDialogClose = roomDetailsDialogView.findViewById(R.id.room_details_dialog_iv_close);
                ImageView ivBulbs = roomDetailsDialogView.findViewById(R.id.room_details_dialog_iv_bulbs);
                ImageView ivAppliances = roomDetailsDialogView.findViewById(R.id.room_details_dialog_iv_appliances);

                //dialog events
                //close dialog
                ivDialogClose.setOnClickListener(getRoomDetailsDialogCloseListener(roomDialog));
                ivBulbs.setOnClickListener(getOpenBulbsViewListener(position, roomDialog));
                ivAppliances.setOnClickListener(getOpenAppliancesViewListener(position, roomDialog));
            }
        };
    }

    private View.OnClickListener getOpenAppliancesViewListener(int position, AlertDialog roomDialog) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AppliancesViewActivity.class);
                intent.putExtra(ROOM_KEY, rooms.get(position).getId());
                intent.putExtra(ROOM_NAME, rooms.get(position).getName());
                startActivity(intent,
                        ActivityOptions.makeSceneTransitionAnimation(RoomsViewActivity.this).toBundle());
                roomDialog.dismiss();
            }
        };
    }

    private View.OnClickListener getOpenBulbsViewListener(int position, AlertDialog roomDialog) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), BulbsViewActivity.class);
                intent.putExtra(ROOM_KEY, rooms.get(position).getId());
                intent.putExtra(ROOM_NAME, rooms.get(position).getName());
                startActivity(intent,
                        ActivityOptions.makeSceneTransitionAnimation(RoomsViewActivity.this).toBundle());
                roomDialog.dismiss();
            }
        };
    }

    private View.OnClickListener getRoomDetailsDialogCloseListener(AlertDialog roomDialog) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                roomDialog.dismiss();
            }
        };
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
                if (tietFilter.getText()!=null || tietFilter.getText().toString().trim().length() >= 2) {
                    firebaseCurrentConfigurationService.getRoomsFiltered(roomDataChangeCallback(), tietFilter.getText().toString().trim());
                    notifyLvAdapter();
                } else {
                    firebaseCurrentConfigurationService.getRoomsDataChangeListener(roomDataChangeCallback());
                    notifyLvAdapter();
                }
            }
        };
    }

    private View.OnClickListener getOpenAddDialogListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RoomFormDialog roomFormDialog = RoomFormDialog.newInstance(null);
                roomFormDialog.show(getSupportFragmentManager(), getString(R.string.room_form_tag));
            }
        };
    }

    private void closeLvItemMenu() {
        if (fabClose != null) {
            selectedRoom = -1;
            fabClose.performClick();
        }
    }

    @NonNull
    private AdapterView.OnItemLongClickListener getLongItemClickLvListener() {
        return new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                //close previous
                closeLvItemMenu();

                selectedRoom = position;

                fabClose = view.findViewById(R.id.lv_row_rooms_fab_close);
                FloatingActionButton fabDelete = view.findViewById(R.id.lv_row_rooms_fab_delete);
                FloatingActionButton fabEdit = view.findViewById(R.id.lv_row_rooms_fab_edit);

                openLvItemMenu(view, fabDelete, fabEdit);
                setLvItemMenuEvents(view, position, fabDelete, fabEdit);
                return true;
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
        view.findViewById(R.id.lv_row_rooms_cl).setAlpha(0.4f);
    }

    private void setLvItemMenuEvents(View view, int position, FloatingActionButton fabDelete, FloatingActionButton fabEdit) {
        fabClose.setOnClickListener(getFabCloseListener(fabClose, fabEdit, fabDelete, view));
        fabEdit.setOnClickListener(getLvItemMenuEditListener(position));
        fabDelete.setOnClickListener(getLvItemMenuDeleteListener(position));
    }

    private View.OnClickListener getLvItemMenuEditListener(int position) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // open form to edit bulb
                RoomFormDialog roomFormDialog = RoomFormDialog.newInstance(rooms.get(position));
                roomFormDialog.show(getSupportFragmentManager(), getString(R.string.room_form_tag));
            }
        };
    }

    private View.OnClickListener getFabCloseListener(FloatingActionButton f1, FloatingActionButton f2, FloatingActionButton f3, View view) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedRoom = -1;
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
                view.findViewById(R.id.lv_row_rooms_cl).setAlpha(1f);

            }
        }, 400);
    }

    private View.OnClickListener getLvItemMenuDeleteListener(int position) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(RoomsViewActivity.this)
                        .setTitle(R.string.title_delete_room)
                        .setMessage(R.string.msg_delete_room)
                        .setPositiveButton(R.string.da, getDeleteRoomPositiveListener(position))
                        .setNegativeButton(R.string.nu, getDeleteaRoomNegativeListener(position))
                        .setNeutralButton(R.string.login_dialog_reset_password_cancel, getDeleteRoomNeutralListener());
                builder.create().show();
            }
        };
    }

    private DialogInterface.OnClickListener getDeleteaRoomNegativeListener(int position) {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (position != -1) {
                    //remove room id for bulbs and appliances
                    firebaseCurrentConfigurationService.getBulbsInRoom(getRemoveRoomFromBulbCallback(), rooms.get(position).getId());
                    firebaseCurrentConfigurationService.getAppliancesInRoom(getRemoveRoomFromApplianceCallback(), rooms.get(position).getId());
                    //delete just room
                    firebaseCurrentConfigurationService.deleteRoom(rooms.get(position));
                }
            }
        };
    }

    private Callback<List<Appliance>> getRemoveRoomFromApplianceCallback() {
        return new Callback<List<Appliance>>() {
            @Override
            public void runResultOnUiThread(List<Appliance> result) {
                if (result != null) {
                    for (Appliance appliance :
                            result) {
                        appliance.setRoomId(Room.NO_ROOM_ID);
                        firebaseCurrentConfigurationService.updateAppliance(appliance);
                    }
                }
            }
        };
    }

    private Callback<List<Bulb>> getRemoveRoomFromBulbCallback() {
        return new Callback<List<Bulb>>() {
            @Override
            public void runResultOnUiThread(List<Bulb> result) {
                if (result != null) {
                    for (Bulb bulb :
                            result) {
                        bulb.setRoomId(Room.NO_ROOM_ID);
                        firebaseCurrentConfigurationService.updateBulb(bulb);
                    }
                }
            }
        };
    }

    private DialogInterface.OnClickListener getDeleteRoomPositiveListener(int position) {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (position != -1) {
                    //remove room id for bulbs and appliances
                    firebaseCurrentConfigurationService.getBulbsInRoom(getDeleteBulbCallback(), rooms.get(position).getId());
                    firebaseCurrentConfigurationService.getAppliancesInRoom(getDeleteApplianceCallback(), rooms.get(position).getId());
                    //delete room
                    firebaseCurrentConfigurationService.deleteRoom(rooms.get(position));
                }
            }
        };
    }

    private Callback<List<Appliance>> getDeleteApplianceCallback() {
        return new Callback<List<Appliance>>() {
            @Override
            public void runResultOnUiThread(List<Appliance> result) {
                if(result!=null){
                    for(Appliance appliance:
                    result){
                        firebaseCurrentConfigurationService.deleteAppliance(appliance);
                    }
                }
            }
        };
    }

    private Callback<List<Bulb>> getDeleteBulbCallback() {
        return new Callback<List<Bulb>>() {
            @Override
            public void runResultOnUiThread(List<Bulb> result) {
                if(result!=null){
                    for (Bulb bulb:
                         result) {
                        firebaseCurrentConfigurationService.deleteBulb(bulb);
                    }
                }
            }
        };
    }

    private DialogInterface.OnClickListener getDeleteRoomNeutralListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                closeLvItemMenu();
            }
        };
    }

    private View.OnClickListener getIvSortClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cbSort.isChecked()) {
                    if (sortOrderAscending == true) {
                        ivSort.setImageResource(R.drawable.ic_arrow_downward);
                        sortRoomsDescending();
                        notifyLvAdapter();
                        sortOrderAscending = false;
                    } else {
                        ivSort.setImageResource(R.drawable.ic_arrow_upward);
                        sortRoomsAscending();
                        notifyLvAdapter();
                        sortOrderAscending = true;

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
                if (isChecked == true) {
                    cbSort.setTextColor(getColor(R.color.coral));
                    ivSort.setColorFilter(getColor(R.color.coral));
                    if(sortOrderAscending == true){
                        sortRoomsAscending();
                        notifyLvAdapter();
                    } else {
                        sortRoomsDescending();
                        notifyLvAdapter();
                    }
                } else {
                    cbSort.setTextColor(getColor(R.color.dark_gray));
                    ivSort.setColorFilter(getColor(R.color.dark_gray));
                    firebaseCurrentConfigurationService.getRoomsDataChangeListener(roomDataChangeCallback());
                }
            }
        };
    }

    @SuppressLint("NewApi")
    private void sortRoomsDescending() {
        rooms.sort(((o1, o2) -> {
            if(o1.getAverageMonthlyConsumption(bulbs,appliances) < o2.getAverageMonthlyConsumption(bulbs,appliances)) return 1;
            else if(o1.getAverageMonthlyConsumption(bulbs,appliances) > o2.getAverageMonthlyConsumption(bulbs,appliances)) return -1;
            else return 0;
        }));
    }

    @SuppressLint("NewApi")
    private void sortRoomsAscending() {
        rooms.sort(((o1, o2) -> {
            if(o1.getAverageMonthlyConsumption(bulbs,appliances) < o2.getAverageMonthlyConsumption(bulbs,appliances)) return -1;
            else if(o1.getAverageMonthlyConsumption(bulbs,appliances) > o2.getAverageMonthlyConsumption(bulbs,appliances)) return 1;
            else return 0;
        }));
    }

    @Override
    public void sendInputToActivity(Room room) {
        //set input to widgets
        if(selectedRoom == -1){
            //insert
            firebaseCurrentConfigurationService.insertRoom(room);
        } else{
            //update
            firebaseCurrentConfigurationService.updateRoom(room);
        }
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