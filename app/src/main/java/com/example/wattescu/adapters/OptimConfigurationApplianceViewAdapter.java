package com.example.wattescu.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
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
import com.example.wattescu.enums.CategoryFilterType;
import com.example.wattescu.entities.Appliance;
import com.example.wattescu.entities.SavedShopAppliance;

import java.util.List;
import java.util.stream.Collectors;

public class OptimConfigurationApplianceViewAdapter extends ArrayAdapter<SavedShopAppliance> {

    private Context context;
    private int resource;
    private LayoutInflater inflater;

    private List<SavedShopAppliance> savedShopAppliances;
    private List<Appliance> currentAppliances;

    public OptimConfigurationApplianceViewAdapter(@NonNull Context context, int resource, @NonNull List<SavedShopAppliance> objects,
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
        return view;
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initComponents(View view, SavedShopAppliance savedShopAppliance) {
        setImage(view, savedShopAppliance.getImageUrl());
        setName(view, savedShopAppliance.getName());
        setSpinnerAdapter(view,savedShopAppliance);
        if(savedShopAppliance.getYearlyConsumption()!=null){
            setConsumption(view, savedShopAppliance.getYearlyConsumption());
        } else if (savedShopAppliance.getPower() != null){
            setPower(view, savedShopAppliance.getPower());
        }
        setPrice(view, savedShopAppliance.getPrice());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setSpinnerAdapter(View view, SavedShopAppliance savedShopAppliance) {
        Spinner spnAppliances = view.findViewById(R.id.lv_row_custom_configuration_appliance_spn_appliance_to_replace);
        List<String> strings = currentAppliances.stream()
                .map(appliance -> appliance.getName())
                .collect(Collectors.toList());
        strings.add("alege electrocasnic");
        ArrayAdapter<String> adapter = new ArrayAdapter(context, android.R.layout.simple_list_item_1, strings){
            @Override
            public boolean isEnabled(int position) {
//                if(position == strings.size()-1){
//                    return false;
//                }
                return true;
            }

            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View mView = super.getDropDownView(position, convertView, parent);
                TextView mTextView = (TextView) mView;
                if (mTextView.getText().toString().equals("alege electrocasnic")) {
                    mTextView.setTextColor(context.getColor(R.color.medium_gray));
                } else {
                    mTextView.setTextColor(context.getColor(R.color.dark_gray));
                }
                return mView;
            }

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View mView = super.getView(position, convertView, parent);
                TextView mTextView = (TextView) mView;
                if (position == strings.size()-1) {
                    mTextView.setTextColor(context.getColor(R.color.coral));
                }
                return mView;
            }
        };

        spnAppliances.setAdapter(adapter);
        spnAppliances.setSelection(strings.size()-1);

        spnAppliances.setOnItemSelectedListener(getApplianceSelectedListener(savedShopAppliance));
    }

    private AdapterView.OnItemSelectedListener getApplianceSelectedListener(SavedShopAppliance savedShopAppliance) {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position >= currentAppliances.size()){
                    return;
                }
                //set selected appliance
                savedShopAppliance.setApplianceToReplaceId(currentAppliances.get(position).getId());
                checkIfAppliancesMatch(position, savedShopAppliance);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
    }
    private void checkIfAppliancesMatch(int position, SavedShopAppliance savedShopAppliance) {
        switch (CategoryFilterType.valueOf(savedShopAppliance.getCategory())) {
            case dish_washer:
                if (!currentAppliances.get(position).getType().toLowerCase().contains("masina de spalat vase") &&
                        !currentAppliances.get(position).getType().toLowerCase().contains("vase")
                        && !currentAppliances.get(position).getType().toLowerCase().contains("electrocasnice pentru bucatarie")) {
                    showCategoriesNotMatchToast();
                }
                break;
            case AC:
                if (!currentAppliances.get(position).getType().toLowerCase().contains("aparat de aer conditionat") &&
                        !currentAppliances.get(position).getType().toLowerCase().contains("aer conditionat")
                        && !currentAppliances.get(position).getType().toLowerCase().contains("ac")
                        && !currentAppliances.get(position).getType().toLowerCase().contains("incalzire")
                        && !currentAppliances.get(position).getType().toLowerCase().contains("racire")) {
                    showCategoriesNotMatchToast();

                }
                break;
            case boiler:
                if (!currentAppliances.get(position).getType().toLowerCase().contains("boiler") &&
                        !currentAppliances.get(position).getType().toLowerCase().contains("incalzire apa")) {
                    showCategoriesNotMatchToast();

                }
                break;
            case freezer:
                if (!currentAppliances.get(position).getType().toLowerCase().contains("congelator") &&
                        !currentAppliances.get(position).getType().toLowerCase().contains("lada frigorifica")) {
                    showCategoriesNotMatchToast();

                }
                break;
            case fridge:
                if (!currentAppliances.get(position).getType().toLowerCase().contains("combina frigorifica") &&
                        !currentAppliances.get(position).getType().toLowerCase().contains("frigider")
                        && !currentAppliances.get(position).getType().toLowerCase().contains("congelator")) {
                    showCategoriesNotMatchToast();

                }
                break;
            case microwave:
                if (!currentAppliances.get(position).getType().toLowerCase().contains("incalzire mancare") &&
                        !currentAppliances.get(position).getType().toLowerCase().contains("microunde")
                        && !currentAppliances.get(position).getType().toLowerCase().contains("cuptor")) {
                    showCategoriesNotMatchToast();

                }
                break;
            case electric_oven:
                if (!currentAppliances.get(position).getType().toLowerCase().contains("cuptor")) {
                    showCategoriesNotMatchToast();

                }
                break;
            case washer:
                if (!currentAppliances.get(position).getType().toLowerCase().contains("masina de spalat rufe") &&
                        !currentAppliances.get(position).getType().toLowerCase().contains("masina de spalat haine")
                        && !currentAppliances.get(position).getType().toLowerCase().contains("uscator de rufe")) {
                    showCategoriesNotMatchToast();

                }
                break;
            case printer:
                if (!currentAppliances.get(position).getType().toLowerCase().contains("imprimanta") &&
                        !currentAppliances.get(position).getType().toLowerCase().contains("scanner")) {
                    showCategoriesNotMatchToast();

                }
                break;
            case air_purifier:
                if (!currentAppliances.get(position).getType().toLowerCase().contains("dezumidificator") &&
                        !currentAppliances.get(position).getType().toLowerCase().contains("purificator")) {
                    showCategoriesNotMatchToast();

                }
                break;
            case TV:
                if (!(currentAppliances.get(position).getType().toLowerCase().contains("tv")) &&
                        !(currentAppliances.get(position).getType().toLowerCase().contains("televizor"))
                        && !(currentAppliances.get(position).getType().toLowerCase().contains("ecran"))) {
                    showCategoriesNotMatchToast();

                }
                break;
            default:
                if (!currentAppliances.get(position).getType().toLowerCase().contains("laptop") &&
                        !currentAppliances.get(position).getType().toLowerCase().contains("desktop")
                        && !currentAppliances.get(position).getType().toLowerCase().contains("pc")
                        && !currentAppliances.get(position).getType().toLowerCase().contains("ecran")
                        && !currentAppliances.get(position).getType().toLowerCase().contains("calculator")
                        && !currentAppliances.get(position).getType().toLowerCase().contains("computer")
                        && !currentAppliances.get(position).getType().toLowerCase().contains("monitor")) {
                    showCategoriesNotMatchToast();

                }
                break;
        }
    }

    private void showCategoriesNotMatchToast() {
        Toast toast = new Toast(context);
        editToast(toast, context.getString(R.string.msg_categories_differ));
        toast.show();
    }

    private void setImage(View view, String imageUrl) {
        TrailingCircularDotsLoader progressbar = view.findViewById(R.id.lv_row_custom_configuration_appliance_progress_bar);
        progressbar.setVisibility(View.VISIBLE);

        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.white_placeholder);
        ImageView ivImage = view.findViewById(R.id.lv_row_custom_configuration_appliance_iv_image);

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
        TextView textView = view.findViewById(R.id.lv_row_custom_configuration_appliance_tv_price);
        populateTextViewContent(textView, context.getString(R.string.price_details_format, price));
    }

    private void setPower(View view, Double power) {
        TextView textView = view.findViewById(R.id.lv_row_custom_configuration_appliance_tv_consumption);
        populateTextViewContent(textView, context.getString(R.string.format_power, Math.round(power*1000)));
    }

    private void setConsumption(View view, Double yearlyConsumption) {
        TextView textView = view.findViewById(R.id.lv_row_custom_configuration_appliance_tv_consumption);
        populateTextViewContent(textView, context.getString(R.string.format_yearly_consumption, Math.round(yearlyConsumption)));
    }

    private void setName(View view, String name) {
        TextView textView = view.findViewById(R.id.lv_row_custom_configuration_appliance_tv_title);
        populateTextViewContent(textView, name);
    }

    private void populateTextViewContent(TextView textView, String value){
        if(value != null && !value.trim().isEmpty()){
            textView.setText((value));
        }else{
            textView.setText("");
        }
    }

    private void editToast(Toast toast, String string) {
        View toast_view = LayoutInflater.from(context).inflate(R.layout.toast_top_layout, null);
        ((TextView) toast_view.findViewById(R.id.toast_layout_tv)).setText(string);
        toast.setView(toast_view);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setGravity(Gravity.TOP, 0, 0);
    }

}

