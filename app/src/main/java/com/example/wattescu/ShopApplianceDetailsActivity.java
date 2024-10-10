package com.example.wattescu;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionManager;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.wattescu.async.Callback;
import com.example.wattescu.firebase.FirebaseCurrentConfigurationService;
import com.example.wattescu.firebase.FirebaseSavedShopApplianceService;
import com.example.wattescu.adapters.StaggeredRecycleViewApplianceAdapter;
import com.example.wattescu.entities.Appliance;
import com.example.wattescu.entities.Provider;
import com.example.wattescu.entities.SavedShopAppliance;
import com.example.wattescu.entities.ShopAppliance;
import com.example.wattescu.enums.CategoryFilterType;
import com.google.firebase.auth.FirebaseAuth;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ShopApplianceDetailsActivity extends AppCompatActivity implements Serializable {

    //views
    private TextView tvReplaceTitle;
    private boolean showReplaceLayout = true;
    private ImageView ivImage;
    private TextView tvTitle;
    private TextView tvDescription;
    private ImageView ivSaved;
    private TextView tvConsumptionType;
    private TextView tvConsumption;
    private TextView tvEfficiencyClass;
    private TextView tvPrice;
    private TextView tvLink;
    private Spinner spnCurrentAppliances;
    private TextView tvConsumptionReduction;
    private TextView tvSavings;
    private TextView tvCo2EmissionsReduction;

    private SavedShopAppliance savedShopAppliance;
    private ShopAppliance shopAppliance;

    //firebase
    private List<Appliance> appliances = new ArrayList<>();
    private FirebaseSavedShopApplianceService firebaseSavedShopApplianceService;
    private FirebaseCurrentConfigurationService firebaseCurrentConfigurationService;
    private Double price;

    private boolean isSaved = false;
    private String applianceToReplaceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_appliance_details);
        getAppliance();
        initComponents();
        initFirebase();
        createViewsFromAppliance();
    }

    private void initFirebase() {
        try {
            firebaseSavedShopApplianceService = FirebaseSavedShopApplianceService.getInstance(FirebaseAuth.getInstance().getCurrentUser().getUid());
            firebaseCurrentConfigurationService = FirebaseCurrentConfigurationService.getInstance(FirebaseAuth.getInstance().getCurrentUser().getUid());
            firebaseCurrentConfigurationService.getProvider(getProviderCallback());
            firebaseCurrentConfigurationService.getAppliancesDataChangeListener(applianceDataChangeCallback());
        } catch (Exception exception) {
            exception.printStackTrace();
        }

    }

    private Callback<List<Appliance>> applianceDataChangeCallback() {
        return new Callback<List<Appliance>>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void runResultOnUiThread(List<Appliance> result) {
                if (result != null) {
                    appliances.clear();
                    appliances.addAll(result);
                    setSpinnerAdapter();
                }
            }
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setSpinnerAdapter() {
        List<String> strings = appliances.stream()
                .map(appliance -> appliance.getName())
                .collect(Collectors.toList());
        strings.add("alege electrocasnic");
        ArrayAdapter<String> adapter = new ArrayAdapter(ShopApplianceDetailsActivity.this, android.R.layout.simple_list_item_1, strings){
            @Override
            public boolean isEnabled(int position) {
                if(position == strings.size()-1){
                    return false;
                }
                return true;
            }

            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View mView = super.getDropDownView(position, convertView, parent);

                TextView mTextView = (TextView) mView;
                if (mTextView.getText().toString().equals("alege electrocasnic")) {
                    mTextView.setTextColor(getColor(R.color.medium_gray));
                } else {
                    mTextView.setTextColor(getColor(R.color.dark_gray));
                }
                return mView;
            }
        };

        spnCurrentAppliances.setAdapter(adapter);
        spnCurrentAppliances.setSelection(strings.size()-1);

        setSpinnerSavedApplianceToReplace();

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setSpinnerSavedApplianceToReplace() {
        if (applianceToReplaceId != null) {
            List<String> ids = appliances.stream()
                    .map(appliance -> appliance.getId())
                    .collect(Collectors.toList());
            int position = ids.indexOf(applianceToReplaceId);
            spnCurrentAppliances.setSelection(position);
            showReplaceLayout = true;
            toggle(showReplaceLayout);
            showReplaceLayout = false;
        }
    }

    private Callback<Provider> getProviderCallback() {
        return new Callback<Provider>() {
            @Override
            public void runResultOnUiThread(Provider result) {
                if (result != null) {
                    price = result.getPriceKw();
                }
            }
        };
    }

    private void createViewsFromAppliance() {
        if (savedShopAppliance != null) {
            initiateApplianceDetails(savedShopAppliance);
        } else if (shopAppliance != null) {
            initiateApplianceDetails(shopAppliance);
        }

    }

    private void initiateApplianceDetails(ShopAppliance appliance) {
        tvTitle.setText(appliance.getName());
        setImage(appliance);
        tvDescription.setText(appliance.getName());
        setConspumtion(appliance);
        tvEfficiencyClass.setText(appliance.getEfficiencyClass());
        tvPrice.setText(getString(R.string.price_details_format, appliance.getPrice()));
    }

    private void setConspumtion(ShopAppliance shopAppliance) {
        if (shopAppliance.getPower() != null) {
            tvConsumptionType.setText(R.string.putere);
            tvConsumption.setText(getString(R.string.format_power, Math.round(shopAppliance.getPower() * 1000)));
        } else if (shopAppliance.getYearlyConsumption() != null) {
            tvConsumptionType.setText(R.string.consum_anual);
            tvConsumption.setText(getString(R.string.format_consumption_int, Math.round(shopAppliance.getYearlyConsumption())));
        }
    }

    private void setImage(ShopAppliance shopAppliance) {
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.white_placeholder);

        Glide.with(getApplicationContext())
                .load(shopAppliance.getImageUrl())
                .apply(requestOptions)
                .into(ivImage);
    }

    private void getAppliance() {
        Intent intent = getIntent();
        if (intent.hasExtra(StaggeredRecycleViewApplianceAdapter.SAVED_APPLIANCE)) {
            savedShopAppliance = (SavedShopAppliance) intent.getSerializableExtra(StaggeredRecycleViewApplianceAdapter.SAVED_APPLIANCE);
            setApplianceToReplace();
        } else if (intent.hasExtra(StaggeredRecycleViewApplianceAdapter.NOT_SAVED_APPLIANCE)) {
            shopAppliance = (ShopAppliance) intent.getSerializableExtra(StaggeredRecycleViewApplianceAdapter.NOT_SAVED_APPLIANCE);

        }
    }

    private void initComponents() {
        tvReplaceTitle = findViewById(R.id.shop_appliance_details_tv_replace_title);
        ivImage = findViewById(R.id.shop_appliance_details_iv_image);
        tvTitle = findViewById(R.id.shop_appliance_details_tv_title);
        tvDescription = findViewById(R.id.shop_appliance_details_tv_desc);
        ivSaved = findViewById(R.id.shop_appliance_details_iv_save);
        setIvSaved();
        setApplianceToReplace();
        tvConsumptionType = findViewById(R.id.shop_appliance_details_tv_consumption_measure_type);
        tvConsumption = findViewById(R.id.shop_appliance_details_tv_consumption);
        tvEfficiencyClass = findViewById(R.id.shop_appliance_details_tv_efficiency_class);
        tvPrice = findViewById(R.id.shop_appliance_details_tv_price);
        tvLink = findViewById(R.id.shop_appliance_details_tv_link);
        spnCurrentAppliances = findViewById(R.id.shop_appliance_details_spn_appliance_to_replace);
        tvConsumptionReduction = findViewById(R.id.shop_appliance_details_tv_consumption_reduction);
        tvSavings = findViewById(R.id.shop_appliance_details_tv_savings);
        tvCo2EmissionsReduction = findViewById(R.id.shop_appliance_details_tv_co2_emissions_reduction);

        initEvents();
    }

    private void setApplianceToReplace() {
        if (savedShopAppliance != null && savedShopAppliance.getApplianceToReplaceId() != null) {
            this.applianceToReplaceId = savedShopAppliance.getApplianceToReplaceId();
        }
    }

    private void setIvSaved() {
        if (savedShopAppliance != null) {
            ivSaved.setImageResource(R.drawable.ic_green_favorite);
            isSaved = true;
        }
    }

    private void initEvents() {
        tvReplaceTitle.setOnClickListener(getTvTitleClickListener());
        tvLink.setOnClickListener(getLinkClickedListener());
        ivSaved.setOnClickListener(getSaveClickListener());
        spnCurrentAppliances.setOnItemSelectedListener(getSpnItemSelectedListener());
    }

    private AdapterView.OnItemSelectedListener getSpnItemSelectedListener() {
        return new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position >= appliances.size()){
                    return;
                }
                if (savedShopAppliance == null) {
                    savedShopAppliance = new SavedShopAppliance(shopAppliance);
                }
                //set selected appliance
                applianceToReplaceId = appliances.get(position).getId();
                savedShopAppliance.setApplianceToReplaceId(applianceToReplaceId);

                //calculate reductions
                calculateReductions();

                checkCategory(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
    }

    private void checkCategory(int position) {
        switch (CategoryFilterType.valueOf(savedShopAppliance.getCategory())) {
            case dish_washer:
                if (!appliances.get(position).getType().toLowerCase().contains("masina de spalat vase") &&
                        !appliances.get(position).getType().toLowerCase().contains("vase")
                        && !appliances.get(position).getType().toLowerCase().contains("electrocasnice pentru bucatarie")) {
                    Toast toast = new Toast(ShopApplianceDetailsActivity.this);
                    editToast(toast, getString(R.string.msg_categories_differ));
                    toast.show();
                }
                break;
            case AC:
                if (!appliances.get(position).getType().toLowerCase().contains("aparat de aer conditionat") &&
                        !appliances.get(position).getType().toLowerCase().contains("aer conditionat")
                        && !appliances.get(position).getType().toLowerCase().contains("ac")
                        && !appliances.get(position).getType().toLowerCase().contains("incalzire")
                        && !appliances.get(position).getType().toLowerCase().contains("racire")) {
                    Toast toast = new Toast(ShopApplianceDetailsActivity.this);
                    editToast(toast, getString(R.string.msg_categories_differ));
                    toast.show();
                }
                break;
            case boiler:
                if (!appliances.get(position).getType().toLowerCase().contains("boiler") &&
                        !appliances.get(position).getType().toLowerCase().contains("incalzire apa")) {
                    Toast toast = new Toast(ShopApplianceDetailsActivity.this);
                    editToast(toast, getString(R.string.msg_categories_differ));
                    toast.show();
                }
                break;
            case freezer:
                if (!appliances.get(position).getType().toLowerCase().contains("congelator") &&
                        !appliances.get(position).getType().toLowerCase().contains("lada frigorifica")) {
                    Toast toast = new Toast(ShopApplianceDetailsActivity.this);
                    editToast(toast, getString(R.string.msg_categories_differ));
                    toast.show();
                }
                break;
            case fridge:
                if (!appliances.get(position).getType().toLowerCase().contains("combina frigorifica") &&
                        !appliances.get(position).getType().toLowerCase().contains("frigider")
                        && !appliances.get(position).getType().toLowerCase().contains("congelator")) {
                    Toast toast = new Toast(ShopApplianceDetailsActivity.this);
                    editToast(toast, getString(R.string.msg_categories_differ));
                    toast.show();
                }
                break;
            case microwave:
                if (!appliances.get(position).getType().toLowerCase().contains("incalzire mancare") &&
                        !appliances.get(position).getType().toLowerCase().contains("microunde")
                        && !appliances.get(position).getType().toLowerCase().contains("cuptor")) {
                    Toast toast = new Toast(ShopApplianceDetailsActivity.this);
                    editToast(toast, getString(R.string.msg_categories_differ));
                    toast.show();
                }
                break;
            case electric_oven:
                if (!appliances.get(position).getType().toLowerCase().contains("cuptor")) {
                    Toast toast = new Toast(ShopApplianceDetailsActivity.this);
                    editToast(toast, getString(R.string.msg_categories_differ));
                    toast.show();
                }
                break;
            case washer:
                if (!appliances.get(position).getType().toLowerCase().contains("masina de spalat rufe") &&
                        !appliances.get(position).getType().toLowerCase().contains("masina de spalat haine")
                        && !appliances.get(position).getType().toLowerCase().contains("uscator de rufe")) {
                    Toast toast = new Toast(ShopApplianceDetailsActivity.this);
                    editToast(toast, getString(R.string.msg_categories_differ));
                    toast.show();
                }
                break;
            case printer:
                if (!appliances.get(position).getType().toLowerCase().contains("imprimanta") &&
                        !appliances.get(position).getType().toLowerCase().contains("scanner")) {
                    Toast toast = new Toast(ShopApplianceDetailsActivity.this);
                    editToast(toast, getString(R.string.msg_categories_differ));
                    toast.show();
                }
                break;
            case air_purifier:
                if (!appliances.get(position).getType().toLowerCase().contains("dezumidificator") &&
                        !appliances.get(position).getType().toLowerCase().contains("purificator")) {
                    Toast toast = new Toast(ShopApplianceDetailsActivity.this);
                    editToast(toast, getString(R.string.msg_categories_differ));
                    toast.show();
                }
                break;
            case TV:
                if (!(appliances.get(position).getType().toLowerCase().contains("tv")) &&
                        !(appliances.get(position).getType().toLowerCase().contains("televizor"))
                        && !appliances.get(position).getType().toLowerCase().contains("ecran")) {
                    Toast toast = new Toast(ShopApplianceDetailsActivity.this);
                    editToast(toast, getString(R.string.msg_categories_differ));
                    toast.show();
                }
                break;
            default:
                if (!appliances.get(position).getType().toLowerCase().contains("laptop") &&
                        !appliances.get(position).getType().toLowerCase().contains("desktop")
                                && !appliances.get(position).getType().toLowerCase().contains("pc")
                                && !appliances.get(position).getType().toLowerCase().contains("ecran")
                                && !appliances.get(position).getType().toLowerCase().contains("calculator")
                                && !appliances.get(position).getType().toLowerCase().contains("computer")
                                && !appliances.get(position).getType().toLowerCase().contains("monitor")) {
                    Toast toast = new Toast(ShopApplianceDetailsActivity.this);
                    editToast(toast, getString(R.string.msg_categories_differ));
                    toast.show();
                }
                break;
        }
    }

    private void editToast(Toast toast, String p) {
        View toast_view = LayoutInflater.from(ShopApplianceDetailsActivity.this).inflate(R.layout.toast_top_layout, null);
        ((TextView) toast_view.findViewById(R.id.toast_layout_tv)).setText(p);
        toast.setView(toast_view);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void calculateReductions() {
        try {
            tvConsumptionReduction.setText(getString(R.string.format_consumption_reduction, savedShopAppliance.getConsumptionReduction(appliances)));
        } catch (Exception exception) {
            tvConsumptionReduction.setText(getString(R.string.format_consumption_reduction, 0f));
        }
        try {
            tvSavings.setText(getString(R.string.format_savings, savedShopAppliance.getSavings(appliances, price)));
        } catch (Exception exception) {
            tvSavings.setText(getString(R.string.format_savings, 0f));
        }
        try {
            tvCo2EmissionsReduction.setText(getString(R.string.format_co2_emissions_reduction, savedShopAppliance.getCo2Savings(appliances)));
        } catch (Exception exception) {
            tvCo2EmissionsReduction.setText(getString(R.string.format_co2_emissions_reduction, 0f));
        }
    }

    private View.OnClickListener getSaveClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSaved) {
                    isSaved = false;
                    ivSaved.setImageResource(R.drawable.ic_green_favorite_border);
                } else {
                    isSaved = true;
                    ivSaved.setImageResource(R.drawable.ic_green_favorite);
                }
            }
        };
    }

    private View.OnClickListener getLinkClickedListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open emag
                if (shopAppliance != null) {
                    goToUrl(shopAppliance.getUrl());
                } else if (savedShopAppliance != null) {
                    goToUrl(savedShopAppliance.getUrl());

                }
            }
        };
    }

    private void goToUrl(String url) {
        try {
            Uri uri = Uri.parse(url);
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), R.string.general_error_msg, Toast.LENGTH_SHORT).show();
        }
    }


    private void toggle(boolean show) {
        tvReplaceTitle.setVisibility(View.INVISIBLE);
        View calcReductionsLayout = findViewById(R.id.shop_appliance_details_cl_replace);
        ViewGroup parent = findViewById(R.id.shop_appliance_details_cl_parent);

        Transition transition = new Slide(Gravity.BOTTOM);
        transition.setDuration(600);
        transition.addTarget(R.id.shop_appliance_details_tv_replace_title);
        transition.addTarget(R.id.shop_appliance_details_cl_replace);

        TransitionManager.beginDelayedTransition(parent, transition);
        tvReplaceTitle.setVisibility(View.VISIBLE);
        calcReductionsLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private View.OnClickListener getTvTitleClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggle(showReplaceLayout);
                showReplaceLayout = !showReplaceLayout;
                //delete appliance to replace if view closed
                if (showReplaceLayout) {
                    applianceToReplaceId = null;
                }
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (savedShopAppliance == null && shopAppliance != null) {
            savedShopAppliance = new SavedShopAppliance(shopAppliance);
        }
        if (isSaved) {
            if (applianceToReplaceId != null) {
                savedShopAppliance.setApplianceToReplaceId(applianceToReplaceId);
            } else {
                savedShopAppliance.setApplianceToReplaceId(null);
            }
            firebaseSavedShopApplianceService.insertShopAppliance(savedShopAppliance);
        } else {
            try {
                firebaseSavedShopApplianceService.removeAppliance(savedShopAppliance);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}

