package com.example.wattescu.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.example.wattescu.entities.Appliance;
import com.example.wattescu.entities.SavedShopAppliance;

import java.util.ArrayList;
import java.util.List;

public class CustomConfigurationViewAppliancesAdapter extends ArrayAdapter<SavedShopAppliance> {

    private Context context;
    private int resource;
    private LayoutInflater inflater;
    private List<SavedShopAppliance> savedShopAppliances;
    private List<Appliance> currentAppliances = new ArrayList<>();
    private Double priceKwH;

    public CustomConfigurationViewAppliancesAdapter(@NonNull Context context, int resource, @NonNull List<SavedShopAppliance> objects,
                                       LayoutInflater inflater) {
        super(context, resource, objects);
        //init vars
        this.context = context;
        this.resource = resource;
        this.savedShopAppliances = objects;
        this.inflater = inflater;
    }


    public List<Appliance> getCurrentAppliances() {
        return currentAppliances;
    }

    public void setCurrentAppliances(List<Appliance> currentAppliances) {
        this.currentAppliances = currentAppliances;
    }

    public Double getPriceKwH() {
        return priceKwH;
    }

    public void setPriceKwH(Double priceKwH) {
        this.priceKwH = priceKwH;
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
        initComponents(view, savedShopAppliance);
        initEvents(view, savedShopAppliance);
        return view;
    }


    private void initEvents(View view, SavedShopAppliance savedShopAppliance) {
        TextView tvLink = view.findViewById(R.id.lv_row_view_custom_configuration_appliances_tv_link);
        tvLink.setOnClickListener(getGoToUrlListener(savedShopAppliance.getUrl()));
    }

    private View.OnClickListener getGoToUrlListener(String url) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Uri uri = Uri.parse(url);
                    context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
                } catch (Exception ex) {
                    Toast.makeText(context, R.string.general_error_msg, Toast.LENGTH_SHORT).show();
                }
            }
        };
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initComponents(View view, SavedShopAppliance savedShopAppliance) {
        setImage(view, savedShopAppliance.getImageUrl());
        setName(view, savedShopAppliance.getName());
        if(savedShopAppliance.getApplianceToReplaceId()!=null) {
            setApplianceToReplace(view, savedShopAppliance.getApplianceToReplaceId());
            try {
                setConsumptionReduction(view, savedShopAppliance.getConsumptionReduction(currentAppliances));
                setSavings(view, savedShopAppliance.getSavings(currentAppliances, priceKwH));
                setCo2Reductions(view, savedShopAppliance.getCo2Savings(currentAppliances));
            } catch (Exception exception) {
                setConsumptionReduction(view, 0.0);
                setSavings(view, 0.0);
                setCo2Reductions(view, 0.0);
                exception.printStackTrace();
            }
        }
        setPrice(view, savedShopAppliance.getPrice());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setApplianceToReplace(View view, String applianceToReplaceId) {
        TextView textView = view.findViewById(R.id.lv_row_view_custom_configuration_appliances_tv_replacement_for);
        Appliance currentAppliance = currentAppliances.stream()
                .filter(appliance -> applianceToReplaceId.equals(appliance.getId()))
                .findAny()
                .orElse(null);
        if(currentAppliance!=null) {
            populateTextViewContent(textView, context.getString(R.string.format_replacement_for_appliance, currentAppliance.getName()));
        }
    }

    private void setCo2Reductions(View view, Double co2Savings) {
        TextView textView = view.findViewById(R.id.lv_row_view_custom_configuration_appliances_tv_Co2_emissions_reduction);
        populateTextViewContent(textView, context.getString(R.string.format_co2_emissions_reduction, co2Savings));
    }

    private void setSavings(View view, Double savings) {
        TextView textView = view.findViewById(R.id.lv_row_view_custom_configuration_appliances_tv_savings);
        populateTextViewContent(textView, context.getString(R.string.format_savings, savings));
    }

    private void setImage(View view, String imageUrl) {
        TrailingCircularDotsLoader progressbar = view.findViewById(R.id.lv_row_view_custom_configuration_appliances_progress_bar);
        progressbar.setVisibility(View.VISIBLE);

        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.white_placeholder);
        ImageView ivImage = view.findViewById(R.id.lv_row_view_custom_configuration_appliances_iv_image);

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
        TextView textView = view.findViewById(R.id.lv_row_view_custom_configuration_appliances_tv_price);
        populateTextViewContent(textView, context.getString(R.string.price_details_format, price));
    }

    private void setConsumptionReduction(View view, Double consumptionReduction) {
        TextView textView = view.findViewById(R.id.lv_row_view_custom_configuration_appliances_tv_consumption_reduction);
        populateTextViewContent(textView, context.getString(R.string.format_consumption_reduction, consumptionReduction));
    }

    private void setName(View view, String name) {
        TextView textView = view.findViewById(R.id.lv_row_view_custom_configuration_appliances_tv_name);
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

