package com.example.wattescu.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.wattescu.R;
import com.example.wattescu.entities.Activity;

import java.util.List;

public class ActivitiesListViewAdapter extends ArrayAdapter<Activity> {

    public static final String LIGHT = "light";
    public static final String HOME = "home";
    public static final String WATER = "water";
    public static final String BATTERY = "battery";
    private Context context;
    private int resource;
    private LayoutInflater inflater;

    private List<Activity> activityList;

    public ActivitiesListViewAdapter(@NonNull Context context, int resource, @NonNull List<Activity> objects,
                       LayoutInflater inflater) {
        super(context, resource, objects);
        //init vars
        this.context = context;
        this.resource = resource;
        this.activityList = objects;
        this.inflater = inflater;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View view = inflater.inflate(resource, parent, false);
        Activity activity = activityList.get(position);
        if(activity == null){
            return view;
        }

        setActivityCategory(view, activity.getCategory());

        //pop tv
        addTitle(view, activity.getTitle());
        addContent(view, activity.getContent());

        return view;
    }

    private void addTitle(View view, String title) {
        TextView textView = view.findViewById(R.id.lv_row_activities_tv_title);
        populateTextViewContent(textView, title);
    }

    private void addContent(View view, String content) {
        TextView textView = view.findViewById(R.id.lv_row_activities_tv_content);
        populateTextViewContent(textView, content);
    }

    private void setActivityCategory(View view, String category) {
        ImageView ivCategory = view.findViewById(R.id.lv_row_activities_iv_category);
        switch (category){
            case LIGHT:
                ivCategory.setImageResource(R.drawable.ic_lightbulb);
                break;
            case HOME:
                ivCategory.setImageResource(R.drawable.ic_home);
                break;
            case WATER:
                ivCategory.setImageResource(R.drawable.ic_water);
                break;
            case BATTERY:
                ivCategory.setImageResource(R.drawable.ic_battery);
                break;
        }
    }

    private void populateTextViewContent(TextView textView, String value){
        if(value != null && !value.trim().isEmpty()){
            textView.setText((value));
        }else{
            textView.setText("");
        }
    }

}
