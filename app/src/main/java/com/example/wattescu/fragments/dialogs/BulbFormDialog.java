package com.example.wattescu.fragments.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.wattescu.R;
import com.example.wattescu.entities.Bulb;
import com.example.wattescu.entities.Room;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class BulbFormDialog extends BottomSheetDialogFragment {

    private static Bulb bulb = null;
    private static Map<String, Double> averageUseWeekDays;
    //room
    private static Map<String, String> roomKeys;
    private static String currentRoomKey;

    //input layouts
    private ConstraintLayout clAverage, clDaily;

    //views
    private TextView tvTitle;
    private TextView tvConspumtion;
    private ImageView ivCloseDialog;
    private Button btnSave;
    //for input
    private TextInputEditText tietName;
    private Spinner spnType;
    private Spinner spnRoom;
    private TextInputEditText tietPower;
    private Button btnAverageTime;
    private FloatingActionButton fabOpenDailyInputView;
    //daily consumption
    private TextView tvMonday, tvTuesday, tvWednesday, tvThursday, tvFriday, tvSaturday, tvSunday;
    private ImageView ivCloseDailyInputView;

    //interface for getting the input
    private OnInputListener onInputListener;

    public static BulbFormDialog newInstance(Bulb bulbInstance, Map<String, String> roomKeysMap, String currentRoom) {
        bulb = bulbInstance;
        roomKeys = roomKeysMap;

        currentRoomKey = currentRoom;

        if(bulb!=null){
            averageUseWeekDays = bulb.getAverageUseWeekDays();
        } else {
            averageUseWeekDays = new HashMap<>();
            for(DayOfWeek day: DayOfWeek.values()){
                averageUseWeekDays.put(String.valueOf(day),0.0);
            }
        }
        return new BulbFormDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        setStyle(STYLE_NO_FRAME, R.style.BottomSheetDialog);
        return super.onCreateDialog(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.dialog_add_bulb_layout, container, false);
        initComponents(view);
        return view;
    }

    private void initComponents(View view) {
        if (getContext() != null) {
            clAverage = view.findViewById(R.id.dialog_add_cl_average_consumption);
            clDaily = view.findViewById(R.id.dialog_add_cl_daily_consumption_view);
            tvTitle = view.findViewById(R.id.dialog_add_bulb_tv_title);
            setTitle();
            tvConspumtion = view.findViewById(R.id.dialog_add_bulb_tv_consumption);
            ivCloseDialog = view.findViewById(R.id.dialog_add_bulb_iv_close);
            btnSave = view.findViewById(R.id.dialog_add_bulb_btn_save);
            tietName = view.findViewById(R.id.dialog_add_bulb_tiet_name);
            spnType = view.findViewById(R.id.dialog_add_bulb_spn_type);
            setTypeSpinnerAdapater();
            spnRoom = view.findViewById(R.id.dialog_add_bulb_spn_room);
            setRoomSpinnerAdapter();
            tietPower = view.findViewById(R.id.dialog_add_bulb_tiet_power);
            btnAverageTime = view.findViewById(R.id.dialog_add_bulb_btn_pick_average_time);
            fabOpenDailyInputView = view.findViewById(R.id.dialog_add_bulb_fab_change_to_daily_consumption);
            ivCloseDailyInputView = view.findViewById(R.id.dialog_add_bulb_iv_close_daily_consumption);
            tvMonday = view.findViewById(R.id.dialog_add_bulb_tv_monday_consumtion);
            tvTuesday = view.findViewById(R.id.dialog_add_bulb_tv_tuesday_consumtion);
            tvWednesday = view.findViewById(R.id.dialog_add_bulb_tv_wednesday_consumtion);
            tvThursday = view.findViewById(R.id.dialog_add_bulb_tv_thursday_consumption);
            tvFriday = view.findViewById(R.id.dialog_add_bulb_tv_friday_consumtion);
            tvSaturday = view.findViewById(R.id.dialog_add_bulb_tv_saturday_consumtion);
            tvSunday = view.findViewById(R.id.dialog_add_bulb_tv_sunday_consumtion);

            if(currentRoomKey != null){
                setCurrentRoomKey();
            }

            createViewFromBulb();
            initEvents();
        }
    }

    private void setCurrentRoomKey() {
        try{
            ArrayAdapter adapter = (ArrayAdapter) spnRoom.getAdapter();
            spnRoom.setSelection(0);
            if(roomKeys!=null) {
                Set<String> keys = roomKeys.keySet();
                int i = 1;
                for (String key:
                        keys) {
                    if(key.equals(currentRoomKey)){
                        spnRoom.setSelection(i);
                        spnRoom.setEnabled(false);
                        break;
                    }
                    i++;
                }
            }} catch (Exception ex){

        }
    }

    private void setRoomSpinnerAdapter() {
        ArrayList<String> rooms = new ArrayList();
        rooms.add(Room.NO_ROOM_ID);
        if(roomKeys != null && !roomKeys.isEmpty()){
            rooms.addAll(roomKeys.values());
        }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1,
                    rooms);
            spnRoom.setAdapter(adapter);
    }

    @SuppressLint("NewApi")
    private void createViewFromBulb() {
        if (bulb == null) {
            return;
        }
        clAverage.setVisibility(View.GONE);
        clDaily.setVisibility(View.VISIBLE);
        tietName.setText(bulb.getName());
        tietPower.setText(String.valueOf(bulb.getPower()*1000));
        setTimeTv(tvMonday, DayOfWeek.MONDAY);
        setTimeTv(tvTuesday, DayOfWeek.TUESDAY);
        setTimeTv(tvWednesday, DayOfWeek.WEDNESDAY);
        setTimeTv(tvThursday, DayOfWeek.THURSDAY);
        setTimeTv(tvFriday, DayOfWeek.FRIDAY);
        setTimeTv(tvSaturday, DayOfWeek.SATURDAY);
        setTimeTv(tvSunday, DayOfWeek.SUNDAY);
        selectTypeSpinnerValue();
        selectRoomSpinnerValue();
    }

    private void selectRoomSpinnerValue() {
        ArrayAdapter adapter = (ArrayAdapter) spnRoom.getAdapter();
        spnRoom.setSelection(0);
        if(roomKeys!=null) {
            Set<String> keys = roomKeys.keySet();
            int i = 1;
            for (String key:
                 keys) {
                if(key.equals(bulb.getRoomId())){
                    spnRoom.setSelection(i);
                    break;
                }
                i++;
            }
        }
    }

    private void selectTypeSpinnerValue() {
        ArrayAdapter adapter = (ArrayAdapter) spnType.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            String item = adapter.getItem(i).toString();
            if (item.equals(bulb.getType())) {
                spnType.setSelection(i);
                break;
            }
        }
    }

    private void setTimeTv(TextView tv, DayOfWeek day) {
        Double time = bulb.getAverageUseWeekDays().get(String.valueOf(day));
        int hour = (int) (time * 60 / 60);
        int minute = (int) (time * 60 % 60);
        tv.setText(String.format(Locale.getDefault(), getString(R.string.hour_format), hour, minute));
    }

    private void setTypeSpinnerAdapater() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.bulb_type,
                android.R.layout.simple_spinner_dropdown_item);
        spnType.setAdapter(adapter);
    }

    @SuppressLint("NewApi")
    private void initEvents() {
        ivCloseDialog.setOnClickListener(getCloseDialogListener());
        btnAverageTime.setOnClickListener(getAverageTimeBtnListener());
        fabOpenDailyInputView.setOnClickListener(getFabOpenDailyInputListener());
        ivCloseDailyInputView.setOnClickListener(getIvCloseDailyInputListener());
        tvMonday.setOnClickListener(getDailyDurationListener(tvMonday, DayOfWeek.MONDAY));
        tvTuesday.setOnClickListener(getDailyDurationListener(tvTuesday, DayOfWeek.TUESDAY));
        tvThursday.setOnClickListener(getDailyDurationListener(tvThursday, DayOfWeek.THURSDAY));
        tvWednesday.setOnClickListener(getDailyDurationListener(tvWednesday, DayOfWeek.WEDNESDAY));
        tvFriday.setOnClickListener(getDailyDurationListener(tvFriday, DayOfWeek.FRIDAY));
        tvSaturday.setOnClickListener(getDailyDurationListener(tvSaturday, DayOfWeek.SATURDAY));
        tvSunday.setOnClickListener(getDailyDurationListener(tvSunday, DayOfWeek.SUNDAY));

        btnSave.setOnClickListener(getSaveListener());
    }

    private View.OnClickListener getSaveListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValid()) {
                    if (bulb == null) {
                        bulb = new Bulb();
                        updateBulbFromView();
                    } else {
                        updateBulbFromView();
                    }
                    //send the input
                    onInputListener.sendInputToActivity(bulb);
                    getDialog().dismiss();
                }
            }
        };
    }

    private void updateBulbFromView() {
        bulb.setAverageUseWeekDays(averageUseWeekDays);
        bulb.setName(tietName.getText().toString().trim());
        bulb.setPower(Double.parseDouble(tietPower.getText().toString().trim())/1000);
        bulb.setType(spnType.getSelectedItem().toString());

        String month = String.valueOf(Calendar.getInstance().getDisplayName(Calendar.MONTH, Calendar.LONG,Locale.ENGLISH));
        setBulbRoomId();
    }

    private void setBulbRoomId() {
        bulb.setRoomId(Room.NO_ROOM_ID);
        int position = spnRoom.getSelectedItemPosition()-1;
        if(roomKeys!=null) {
            int i = 0;
            for (String key: roomKeys.keySet()) {
                if(i == position){
                    bulb.setRoomId(key);
                    break;
                }
                i++;
            }
        }
    }

    @SuppressLint("NewApi")
    private boolean isValid() {
        //name min 3 chars
        if (tietName.getText() == null || tietName.getText().toString().trim().length() < 3) {
            tietName.setError("minim 3 caractere necesare");
            tietName.requestFocus();
            return false;
        }
        //power min 1 max 100
        if (tietPower.getText() == null || tietPower.getText().toString().isEmpty()
                || Double.parseDouble(tietPower.getText().toString()) <= 0 || Double.parseDouble(tietPower.getText().toString()) > 100) {
            tietPower.setError("valoare cuprinsa intre 1 si 100");
            tietPower.requestFocus();
            return false;
        }
        //consumption in min 1 day
        for (Double use : averageUseWeekDays.values()) {
            if (use > 0) {
                return true;
            }
        }
            tvConspumtion.setError("Introdu date de consum");
            tvConspumtion.setText("Introdu date de consum");
            tvConspumtion.setTextColor(getContext().getColor(R.color.coral));
            return false;
        }

        private View.OnClickListener getDailyDurationListener (TextView tv, DayOfWeek day){
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                            int hour = selectedHour;
                            int minute = selectedMinute;
                            Double timeInHours = Double.valueOf(hour + (float) minute / 60);
                            averageUseWeekDays.put(String.valueOf(day), timeInHours);
                            tv.setText(String.format(Locale.getDefault(), getString(R.string.hour_format), hour, minute));
                        }
                    };
                    TimePickerDialog timePickerDialog =
                            new TimePickerDialog(getContext(), R.style.MyDialogTheme, onTimeSetListener, 0, 0, true);
                    timePickerDialog.setTitle(R.string.selecteaza_durata);
                    timePickerDialog.show();
                }
            };
        }

        private View.OnClickListener getIvCloseDailyInputListener () {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clDaily.setVisibility(View.GONE);
                    clAverage.setVisibility(View.VISIBLE);
                }
            };
        }

        private View.OnClickListener getFabOpenDailyInputListener () {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clAverage.setVisibility(View.GONE);
                    clDaily.setVisibility(View.VISIBLE);
                }
            };
        }

        private View.OnClickListener getAverageTimeBtnListener () {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                            int hour = selectedHour;
                            int minute = selectedMinute;
                            Double timeInHours = Double.valueOf(hour + (float) minute / 60);
                            for (DayOfWeek day : DayOfWeek.values()) {
                                averageUseWeekDays.put(String.valueOf(day), timeInHours);
                            }
                            btnAverageTime.setText(String.format(Locale.getDefault(), getString(R.string.hour_format), hour, minute));
                        }
                    };
                    TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), R.style.MyDialogTheme, onTimeSetListener, 0, 0, true);
                    timePickerDialog.setTitle(R.string.selecteaza_durata);
                    timePickerDialog.show();
                }
            };
        }

        private View.OnClickListener getCloseDialogListener () {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getDialog().dismiss();
                }
            };
        }

        private void setTitle () {
            if (bulb != null) {
                tvTitle.setText(R.string.modifica_bec);
            }
        }

        public interface OnInputListener {
            void sendInputToActivity(Bulb bulb);
        }

        @Override
        public void onAttach (@NonNull Context context){
            super.onAttach(context);
            try {
                onInputListener = (OnInputListener) getActivity();
            } catch (Exception exception) {
                Log.e("BulbFormDialog", exception.getMessage());
            }
        }
    }
