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
import com.example.wattescu.entities.Bulb;

import java.util.List;

public class BulbsListViewAdapter extends ArrayAdapter<Bulb> {


    private Context context;
    private int resource;
    private LayoutInflater inflater;
    private Double priceKW;

    private List<Bulb> bulbs;

    public BulbsListViewAdapter(@NonNull Context context, int resource, @NonNull List<Bulb> objects, @NonNull Double priceKW,
                                     LayoutInflater inflater) {
        super(context, resource, objects);
        //init vars
        this.context = context;
        this.resource = resource;
        this.bulbs = objects;
        this.priceKW = priceKW;
        this.inflater = inflater;

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
        Bulb bulb = bulbs.get(position);
        if(bulb == null){
            return view;
        }

        setBulbType(view, bulb.getType());
        setBulbName(view, bulb.getName());
        setBulbMontlyConsumption(view, bulb.getAverageMonthlyConsumtion());
        setBulbMontlyAverageSpendings(view, bulb.getAverageMonthlySpendings(priceKW));

        return view;
    }

    private void setBulbMontlyAverageSpendings(View view, Double averageMonthlySpendings) {
        TextView textView = view.findViewById(R.id.lv_row_bulbs_tv_spendings_this_month);
        populateTextViewContent(textView, getContext().getString(R.string.format_spendings, averageMonthlySpendings));
    }

    private void setBulbMontlyConsumption(View view, Double averageMonthlyConsumtion) {
        TextView textView = view.findViewById(R.id.lv_row_bulbs_tv_consumption_this_month);
        populateTextViewContent(textView, getContext().getString(R.string.format_consumption,averageMonthlyConsumtion));
    }

    private void setBulbName(View view, String name) {
        TextView textView = view.findViewById(R.id.lv_row_bulbs_tv_name);
        populateTextViewContent(textView, name);
    }

    private void setBulbType(View view, String type) {
        TextView textView = view.findViewById(R.id.lv_row_bulbs_tv_type);
        populateTextViewContent(textView, type);
    }

    private void populateTextViewContent(TextView textView, String value){
        if(value != null && !value.trim().isEmpty()){
            textView.setText((value));
        }else{
            textView.setText("");
        }
    }
}

