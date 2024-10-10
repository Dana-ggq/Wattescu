package com.example.wattescu.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.agrawalsuneet.dotsloader.loaders.TrailingCircularDotsLoader;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.wattescu.R;
import com.example.wattescu.ShopApplianceDetailsActivity;
import com.example.wattescu.entities.Appliance;
import com.example.wattescu.entities.SavedShopAppliance;
import com.example.wattescu.firebase.FirebaseSavedShopApplianceService;
;import java.util.List;

public class FavoriteAppliancesLvAdapter extends ArrayAdapter<SavedShopAppliance> {

    private Context context;
    private int resource;
    private LayoutInflater inflater;

    private List<SavedShopAppliance> currentlySelectedAppliances;
    private List<SavedShopAppliance> savedShopAppliances;
    private List<Appliance> currentAppliances;
    private FirebaseSavedShopApplianceService savedShopApplianceService;

    private boolean isClickable = true;


    public FavoriteAppliancesLvAdapter(@NonNull Context context, int resource, @NonNull List<SavedShopAppliance> objects,
                                       LayoutInflater inflater) {
        super(context, resource, objects);
        //init vars
        this.context = context;
        this.resource = resource;
        this.savedShopAppliances = objects;
        this.inflater = inflater;
    }

    public boolean isClickable() {
        return isClickable;
    }

    public void setClickable(boolean clickable) {
        isClickable = clickable;
    }

    public List<Appliance> getCurrentAppliances() {
        return currentAppliances;
    }

    public void setCurrentAppliances(List<Appliance> currentAppliances) {
        this.currentAppliances = currentAppliances;
    }

    public FirebaseSavedShopApplianceService getSavedShopApplianceService() {
        return savedShopApplianceService;
    }

    public void setSavedShopApplianceService(FirebaseSavedShopApplianceService savedShopApplianceService) {
        this.savedShopApplianceService = savedShopApplianceService;
    }

    public List<SavedShopAppliance> getCurrentlySelectedAppliances() {
        return currentlySelectedAppliances;
    }

    public void setCurrentlySelectedAppliances(List<SavedShopAppliance> currentlySelectedAppliances) {
        this.currentlySelectedAppliances = currentlySelectedAppliances;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = inflater.inflate(resource, parent, false);
        SavedShopAppliance savedShopAppliance = savedShopAppliances.get(position);
        if(savedShopAppliance == null){
            return view;
        }
        setIsCurrentlySelected(view, savedShopAppliance);
        initComponents(view, savedShopAppliance);
        if(isClickable) {
            initEvents(view, savedShopAppliance);
        }
        return view;
    }

    private void setIsCurrentlySelected(View view, SavedShopAppliance savedShopAppliance) {
        if(currentlySelectedAppliances!=null && !currentlySelectedAppliances.isEmpty()
                && currentlySelectedAppliances.contains(savedShopAppliance)){
            view.setBackground(context.getDrawable(R.drawable.selected_lv_item));
        }
    }

    private void initEvents(View view, SavedShopAppliance savedShopAppliance) {
        ImageView ivDelete = view.findViewById(R.id.lv_row_saved_appliance_iv_delete);
        ivDelete.setOnClickListener(getDeleteListener(savedShopAppliance));

        ConstraintLayout clLayout = view.findViewById(R.id.lv_row_saved_appliance_layout);
        clLayout.setOnClickListener(getItemLayoutClickListener(view, savedShopAppliance));
    }

    private View.OnClickListener getItemLayoutClickListener(View view, SavedShopAppliance savedShopAppliance) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open details activity
                Intent intent = new Intent(context, ShopApplianceDetailsActivity.class);

                intent.putExtra(StaggeredRecycleViewApplianceAdapter.SAVED_APPLIANCE, savedShopAppliance);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                ImageView imageView = view.findViewById(R.id.lv_row_saved_appliance_iv_image);
                context.startActivity(intent);
            }
        };
    }

    private View.OnClickListener getDeleteListener(SavedShopAppliance savedShopAppliance) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    savedShopApplianceService.removeAppliance(savedShopAppliance);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initComponents(View view, SavedShopAppliance savedShopAppliance) {
        setImage(view, savedShopAppliance.getImageUrl());
        setName(view, savedShopAppliance.getName());
        try {
            setConsumptionReduction(view, savedShopAppliance.getConsumptionReduction(currentAppliances));
        } catch (Exception exception) {
            exception.printStackTrace();
            populateTextViewContent(view.findViewById(R.id.lv_row_saved_appliance_tv_consumption_reduction), "");
        }
        if(savedShopAppliance.getYearlyConsumption()!=null){
            setConsumption(view, savedShopAppliance.getYearlyConsumption());
        } else if (savedShopAppliance.getPower() != null){
            setPower(view, savedShopAppliance.getPower());
        }
        setPrice(view, savedShopAppliance.getPrice());
    }

    private void setImage(View view, String imageUrl) {
        TrailingCircularDotsLoader progressbar = view.findViewById(R.id.lv_row_saved_appliance_progress_bar);
        progressbar.setVisibility(View.VISIBLE);

        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.white_placeholder);
        ImageView ivImage = view.findViewById(R.id.lv_row_saved_appliance_iv_image);

        Glide.with(context)
                .load(imageUrl)
                .apply(requestOptions)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }
                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
                                                   DataSource dataSource, boolean isFirstResource) {
                        progressbar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(ivImage);
    }

    private void setPrice(View view, Double price) {
        TextView textView = view.findViewById(R.id.lv_row_saved_appliance_tv_price);
        populateTextViewContent(textView, context.getString(R.string.price_details_format, price));
    }

    private void setPower(View view, Double power) {
        TextView textView = view.findViewById(R.id.lv_row_saved_appliance_tv_consumption);
        populateTextViewContent(textView, context.getString(R.string.format_power, Math.round(power*1000)));
    }

    private void setConsumption(View view, Double yearlyConsumption) {
        TextView textView = view.findViewById(R.id.lv_row_saved_appliance_tv_consumption);
        populateTextViewContent(textView, context.getString(R.string.format_yearly_consumption, Math.round(yearlyConsumption)));
    }

    private void setConsumptionReduction(View view, Double consumptionReduction) {
        TextView textView = view.findViewById(R.id.lv_row_saved_appliance_tv_consumption_reduction);
        populateTextViewContent(textView, context.getString(R.string.format_lv_saved_appliance_consumption_reduction, consumptionReduction));
    }

    private void setName(View view, String name) {
        TextView textView = view.findViewById(R.id.lv_row_saved_appliance_tv_title);
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

