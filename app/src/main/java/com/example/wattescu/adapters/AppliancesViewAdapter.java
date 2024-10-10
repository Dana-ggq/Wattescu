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

import java.util.List;

public class AppliancesViewAdapter extends ArrayAdapter<Appliance> {

    private Context context;
    private int resource;
    private LayoutInflater inflater;
    private Double priceKW;

    private List<Appliance> appliancess;

    public AppliancesViewAdapter(@NonNull Context context, int resource, @NonNull List<Appliance> objects, @NonNull Double priceKW,
                                LayoutInflater inflater) {
        super(context, resource, objects);
        //init vars
        this.context = context;
        this.resource = resource;
        this.appliancess = objects;
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
        Appliance appliances = appliancess.get(position);
        if(appliances == null){
            return view;
        }

        setappliancesEfficiencyClass(view, appliances.getEfficiencyClass());
        setappliancesName(view, appliances.getName());
        setappliancesMontlyConsumption(view, appliances.getAverageMonthlyConsumption());
        setappliancesMontlyAverageSpendings(view, appliances.getAverageMonthlySpendings(priceKW));

        return view;
    }

    private void setappliancesMontlyAverageSpendings(View view, Double averageMonthlySpendings) {
        TextView textView = view.findViewById(R.id.lv_row_appliances_tv_spendings_this_month);
        populateTextViewContent(textView, getContext().getString(R.string.format_spendings, averageMonthlySpendings));
    }

    private void setappliancesMontlyConsumption(View view, Double averageMonthlyConsumtion) {
        TextView textView = view.findViewById(R.id.lv_row_appliances_tv_consumption_this_month);
        populateTextViewContent(textView, getContext().getString(R.string.format_consumption,averageMonthlyConsumtion));
    }

    private void setappliancesName(View view, String name) {
        TextView textView = view.findViewById(R.id.lv_row_appliances_tv_name);
        populateTextViewContent(textView, name);
    }


    private void setappliancesEfficiencyClass(View view, String efficiencyClass) {
        TextView textView = view.findViewById(R.id.lv_row_appliances_tv_efficiency_class);
        switch (efficiencyClass){
            case "A":
                textView.setTextColor(getContext().getColor(R.color.deep_green));
                break;
            case "B":
                textView.setTextColor(getContext().getColor(R.color.medium_green));
                break;
            case "C":
                textView.setTextColor(getContext().getColor(R.color.ligh_green));
                break;
            case "D":
                textView.setTextColor(getContext().getColor(R.color.yellow));
                break;
            case "E":
                textView.setTextColor(getContext().getColor(R.color.mustard));
                break;
            case "F":
                textView.setTextColor(getContext().getColor(R.color.orange));
                break;
            case "G":
                textView.setTextColor(getContext().getColor(R.color.red));
                break;
        }
        populateTextViewContent(textView, context.getString(R.string.format_efficiency_class, efficiencyClass));
    }

    private void populateTextViewContent(TextView textView, String value){
        if(value != null && !value.trim().isEmpty()){
            textView.setText((value));
        }else{
            textView.setText("");
        }
    }
}

