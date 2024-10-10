package com.example.wattescu.fragments.dialogs;


import android.app.Dialog;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.wattescu.R;
import com.example.wattescu.entities.Room;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;

public class RoomFormDialog extends BottomSheetDialogFragment {

    private static Room room = null;

    //views
    private TextView tvTitle;
    private ImageView ivCloseDialog;
    private Button btnSave;
    //for input
    private TextInputEditText tietName;
    private Spinner spnType;

    //interface for getting the input
    private RoomFormDialog.OnInputListener onInputListener;

    public static RoomFormDialog newInstance(Room roomInstance) {
        room = roomInstance;
        return new RoomFormDialog();
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
        View view = inflater.inflate(R.layout.dialog_add_room_layout, container, false);
        initComponents(view);
        return view;
    }

    private void initComponents(View view) {
        if (getContext() != null) {
            tvTitle = view.findViewById(R.id.dialog_add_room_tv_title);
            setTitle();
            ivCloseDialog = view.findViewById(R.id.dialog_add_room_iv_close);
            btnSave = view.findViewById(R.id.dialog_add_room_btn_save);
            tietName = view.findViewById(R.id.dialog_add_room_tiet_name);
            spnType = view.findViewById(R.id.dialog_add_room_spn_type);
            setTypeSpinnerAdapater();

            createViewFromRoom();
            initEvents();
        }
    }

    private void createViewFromRoom() {
        if (room == null) {
            return;
        }
        tietName.setText(room.getName());
        selectTypeSpinnerValue();
    }

    private void selectTypeSpinnerValue() {
        ArrayAdapter adapter = (ArrayAdapter) spnType.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            String item = adapter.getItem(i).toString();
            if (item.equals(room.getType())) {
                spnType.setSelection(i);
                break;
            }
        }
    }

    private void setTypeSpinnerAdapater() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.room_type,
                android.R.layout.simple_spinner_dropdown_item);
        spnType.setAdapter(adapter);
    }

    private void initEvents() {
        ivCloseDialog.setOnClickListener(getCloseDialogListener());
        btnSave.setOnClickListener(getSaveListener());
    }

    private View.OnClickListener getSaveListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValid()) {
                    if (room == null) {
                        room = new Room();
                    }
                    updateRoomFromView();
                    //send the input
                    onInputListener.sendInputToActivity(room);
                    getDialog().dismiss();
                }
            }
        };
    }

    private void updateRoomFromView() {
        room.setName(tietName.getText().toString().trim());
        room.setType(spnType.getSelectedItem().toString());
    }

    private boolean isValid() {
        //name min 3 chars
        if (tietName.getText() == null || tietName.getText().toString().trim().length() < 3) {
            tietName.setError("minim 3 caractere necesare");
            tietName.requestFocus();
            return false;
        }
        return true;
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
        if (room != null) {
            tvTitle.setText(R.string.modifica_bec);
        }
    }

    public interface OnInputListener {
        void sendInputToActivity(Room room);
    }

    @Override
    public void onAttach (@NonNull Context context){
        super.onAttach(context);
        try {
            onInputListener = (RoomFormDialog.OnInputListener) getActivity();
        } catch (Exception exception) {
            Log.e("roomFormDialog", exception.getMessage());
        }
    }
}
