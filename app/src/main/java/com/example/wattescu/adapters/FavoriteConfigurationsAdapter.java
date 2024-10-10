package com.example.wattescu.adapters;


import android.content.Context;
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

import com.agrawalsuneet.dotsloader.loaders.TrailingCircularDotsLoader;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.wattescu.R;
import com.example.wattescu.firebase.FirebaseCustomConfigurationService;
import com.example.wattescu.entities.Appliance;
import com.example.wattescu.entities.CustomConfiguration;
import com.example.wattescu.entities.SavedShopAppliance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FavoriteConfigurationsAdapter extends ArrayAdapter<CustomConfiguration> {

    private Context context;
    private int resource;
    private LayoutInflater inflater;

    private List<CustomConfiguration> customConfigurations;
    private List<Appliance> currentAppliances = new ArrayList<>();
    private FirebaseCustomConfigurationService firebaseCustomConfigurationService;

    public FavoriteConfigurationsAdapter(@NonNull Context context, int resource, @NonNull List<CustomConfiguration> objects,
                                       LayoutInflater inflater) {
        super(context, resource, objects);
        //init vars
        this.context = context;
        this.resource = resource;
        this.customConfigurations = objects;
        this.inflater = inflater;
    }

    public FirebaseCustomConfigurationService getFirebaseCustomConfigurationService() {
        return firebaseCustomConfigurationService;
    }

    public void setFirebaseCustomConfigurationService(FirebaseCustomConfigurationService firebaseCustomConfigurationService) {
        this.firebaseCustomConfigurationService = firebaseCustomConfigurationService;
    }

    public List<Appliance> getCurrentAppliances() {
        return currentAppliances;
    }

    public void setCurrentAppliances(List<Appliance> currentAppliances) {
        this.currentAppliances = currentAppliances;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = inflater.inflate(resource, parent, false);
        CustomConfiguration customConfiguration = customConfigurations.get(position);
        if(customConfiguration == null){
            return view;
        }
        initComponents(view, customConfiguration);
        initEvents(view, customConfiguration);
        return view;
    }


    private void initEvents(View view, CustomConfiguration customConfiguration) {
        ImageView ivDelete = view.findViewById(R.id.lv_row_custom_configuration_iv_delete);
        ivDelete.setOnClickListener(getDeleteListener(customConfiguration));
    }


    private View.OnClickListener getDeleteListener(CustomConfiguration customConfiguration) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    firebaseCustomConfigurationService.deleteCustomConfiguration(customConfiguration);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initComponents(View view, CustomConfiguration customConfiguration) {

        int count = 0;
        SavedShopAppliance appliance1 = null;
        SavedShopAppliance appliance2 = null;
        for (SavedShopAppliance appliance:
                customConfiguration.getSavedAppliances().values()) {
            if(count == 0){
                appliance1 = appliance;
            } else if (count == 1){
                appliance2 = appliance;
            } else {
                break;
            }
            count++;
        }
            setImage(view.findViewById(R.id.lv_row_custom_configuration_iv_appliance1_image),
                    view.findViewById(R.id.lv_row_custom_configuration_progress_bar1), appliance1.getImageUrl());
            setImage(view.findViewById(R.id.lv_row_custom_configuration_iv_appliance2_image),
                    view.findViewById(R.id.lv_row_custom_configuration_progress_bar2), appliance2.getImageUrl());
        

        setName(view, customConfiguration.getName());
        setConsumptionReduction(view, getConsumptionReduction(customConfiguration));
        setNoAppliances(view, customConfiguration.getSavedAppliances().size());
        setPrice(view, getPrice(customConfiguration));
        setIsOptim(view, customConfiguration.isOptim());
    }

    private void setIsOptim(View view, boolean optim) {
        ImageView ivIsOptim = view.findViewById(R.id.lv_row_custom_configuration_iv_is_optim);
        if(optim){
            ivIsOptim.setVisibility(View.VISIBLE);
        }
    }

    private Double getPrice(CustomConfiguration customConfiguration) {
        Double price = 0.0;
        for (SavedShopAppliance appliance:
                customConfiguration.getSavedAppliances().values()) {
            try {
                price += appliance.getPrice();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        return  price;
    }

    private void setNoAppliances(View view, int size) {
        TextView textView = view.findViewById(R.id.lv_row_custom_configuration_tv_no_appliances);
        populateTextViewContent(textView, context.getString(R.string.format_configuration_no_appliances, size));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private Double getConsumptionReduction(CustomConfiguration customConfiguration) {
        Double consumptionReduction = 0.0;
        for (SavedShopAppliance appliance:
             customConfiguration.getSavedAppliances().values()) {
            try {
                consumptionReduction += appliance.getConsumptionReduction(currentAppliances);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        return  consumptionReduction;
    }


    private void setImage(ImageView ivImage, TrailingCircularDotsLoader progressbar,  String imageUrl) {
        progressbar.setVisibility(View.VISIBLE);

        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.white_placeholder);

        Glide.with(context)
                .load(imageUrl)
                .apply(requestOptions)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }
                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        progressbar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(ivImage);
    }

    private void setPrice(View view, Double price) {
        TextView textView = view.findViewById(R.id.lv_row_custom_configuration_tv_price);
        populateTextViewContent(textView, context.getString(R.string.price_details_format, price));
    }


    private void setConsumptionReduction(View view, Double consumptionReduction) {
        TextView textView = view.findViewById(R.id.lv_row_custom_configuration_tv_consumption_reduction);
        if (consumptionReduction == 0.0){
            textView.setVisibility(View.GONE);
            return;
        }
        populateTextViewContent(textView, context.getString(R.string.format_consumption_reduction, consumptionReduction));
    }

    private void setName(View view, String name) {
        TextView textView = view.findViewById(R.id.lv_row_custom_configuration_tv_name);
        populateTextViewContent(textView, name.substring(0, 1).toUpperCase() + name.substring(1));
    }

    private void populateTextViewContent(TextView textView, String value){
        if(value != null && !value.trim().isEmpty()){
            textView.setText((value));
        }else{
            textView.setText("");
        }
    }


}

