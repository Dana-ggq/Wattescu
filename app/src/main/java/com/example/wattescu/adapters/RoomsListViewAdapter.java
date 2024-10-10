package com.example.wattescu.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.wattescu.R;
import com.example.wattescu.entities.Appliance;
import com.example.wattescu.entities.Bulb;
import com.example.wattescu.entities.Room;

import java.util.List;

public class RoomsListViewAdapter extends ArrayAdapter<Room> {

    private Context context;
    private int resource;
    private LayoutInflater inflater;
    private Double priceKW;

    private List<Bulb> bulbs;
    private List<Appliance> appliances;
    private List<Room> rooms;

    public RoomsListViewAdapter(@NonNull Context context, int resource, @NonNull List<Room> objects,
                                @NonNull Double priceKW,
                                @NonNull List<Bulb> bulbs,
                                @NonNull List<Appliance> appliances,
                                LayoutInflater inflater) {
        super(context, resource, objects);
        //init vars
        this.context = context;
        this.resource = resource;
        this.rooms = objects;
        this.priceKW = priceKW;
        this.appliances = appliances;
        this.bulbs = bulbs;
        this.inflater = inflater;
    }

    public List<Bulb> getBulbs() {
        return bulbs;
    }

    public void setBulbs(List<Bulb> bulbs) {
        this.bulbs = bulbs;
    }

    public List<Appliance> getAppliances() {
        return appliances;
    }

    public void setAppliances(List<Appliance> appliances) {
        this.appliances = appliances;
    }

    public Double getPriceKW() {
        return priceKW;
    }

    public void setPriceKW(Double priceKW) {
        this.priceKW = priceKW;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View view = inflater.inflate(resource, parent, false);
        Room room = rooms.get(position);
        if(room == null){
            return view;
        }

        setRoomName(view, room.getName());
        setRoomConsumersNumber(view, room.getNoAppliances(appliances), room.getNoBulbs(bulbs));
        setRoomMontlyConsumption(view, room.getAverageMonthlyConsumption(bulbs,appliances));
        setRoomMontlyAverageSpendings(view, room.getAverageMonthlySpendings(bulbs,appliances,priceKW));

        return view;
    }

    private void setRoomConsumersNumber(View view, int noAppliances, int noBulbs) {
        TextView textView = view.findViewById(R.id.lv_row_rooms_tv_no_devices);
        populateTextViewContent(textView, getContext().getString(R.string.format_no_devices,noAppliances,noBulbs));
    }

    private void setRoomMontlyAverageSpendings(View view, Double averageMonthlySpendings) {
        TextView textView = view.findViewById(R.id.lv_row_rooms_tv_spendings_this_month);
        populateTextViewContent(textView, getContext().getString(R.string.format_spendings, averageMonthlySpendings));
    }

    private void setRoomMontlyConsumption(View view, Double averageMonthlyConsumtion) {
        TextView textView = view.findViewById(R.id.lv_row_rooms_tv_consumption_this_month);
        populateTextViewContent(textView, getContext().getString(R.string.format_consumption,averageMonthlyConsumtion));
    }

    private void setRoomName(View view, String name) {
        TextView textView = view.findViewById(R.id.lv_row_rooms_tv_name);
        populateTextViewContent(textView, name);
    }


    private void populateTextViewContent(TextView textView, String value){
        if(value != null && !value.trim().isEmpty()){
            textView.setText((value));
        }else{
            textView.setText("");
        }
    }
}


