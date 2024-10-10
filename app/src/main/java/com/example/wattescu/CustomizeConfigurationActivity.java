package com.example.wattescu;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.transition.Slide;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomizeConfigurationActivity extends AppCompatActivity {

    public static final String FRIDGE = "fridge";
    public static final String TV = "TV";
    public static final String FREEZER = "freezer";
    public static final String PRINTER = "printer";
    public static final String MICROWAVE = "microwave";
    public static final String ELECTRIC_OVEN = "electric_oven";
    public static final String WASHER = "washer";
    public static final String DISH = "dish";
    public static final String DESKTOP_SCREEN = "desktop_screen";
    public static final String AC = "AC";
    public static final String DESKTOP_PC = "desktop_pc";
    public static final String BOILER = "boiler";
    public static final String AIR_PURIFIER = "air_purifier";
    public static final String BUDGET_KEY = "BUDGET";
    public static final String REQUESTED_APPLIANCES_KEY = "REQUESTED_APPLIANCES";
    public static final String REQUESTED_QUANTITIES_KEY = "REQUESTED_QUANTITIES";
    //configuration details
    private int budget;
    private String requested_appliances;
    private String requested_quantities;
    private Map<String, String> requestes_appliances_map = new HashMap<>();

    //views
    private Button btnNext;
    private TextView tvMinusFridge, tvPlusFridge, tvQuantityFridge;
    private TextView tvMinusFreezer, tvPlusFreezer, tvQuantityFreezer;
    private TextView tvMinusWasher, tvPlusWasherWasher, tvQuantityWasher;
    private TextView tvMinusAC, tvPlusAC, tvQuantityAC;
    private TextView tvMinusDish, tvPlusDish, tvQuantityDish;
    private TextView tvMinusMicrowave, tvPlusMicrowave, tvQuantityMicrowave;
    private TextView tvMinusDesktopScreen, tvPlusDesktopScreen, tvQuantityDesktopScreen;
    private TextView tvMinusDesktopPc, tvPlusDesktopPc, tvQuantityDesktopPc;
    private TextView tvMinusPrinter, tvPlusPrinter, tvQuantityPrinter;
    private TextView tvMinusBoiler, tvPlusBoiler, tvQuantityBoiler;
    private TextView tvMinusTv, tvPlusTv, tvQuantityTv;
    private TextView tvMinusElectricOven, tvPlusElectricOven, tvQuantityElectricOven;
    private TextView tvMinusAirPurifier, tvPlusAirPurifier, tvQuantityAirPurifier;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // enableTransitions();
        setContentView(R.layout.activity_customize_configuraiton);
        initComponents();
    }

    private void initComponents() {
        btnNext = findViewById(R.id.customize_configuration_btn_next);
        initFridge();
        initFreezer();
        initTv();
        initElectricOven();
        initPrinter();
        initMicrowave();
        initWasher();
        initDicsh();
        initDesktopScreen();
        initDesktopPC();
        initAC();
        initBoiler();
        initAirPurifier();
        initEvents();
    }

    private void initAirPurifier() {
        tvMinusAirPurifier = findViewById(R.id.customize_configuration_tv_minus_air_purifier);
        tvPlusAirPurifier = findViewById(R.id.customize_configuration_tv_plus_air_purifier);
        tvQuantityAirPurifier = findViewById(R.id.customize_configuration_tv_quantity_air_purifier);
    }

    private void initTv() {
        tvMinusTv = findViewById(R.id.customize_configuration_tv_minus_TV);
        tvPlusTv = findViewById(R.id.customize_configuration_tv_plus_TV);
        tvQuantityTv = findViewById(R.id.customize_configuration_tv_quantity_TV);
    }

    private void initFreezer() {
        tvMinusFreezer = findViewById(R.id.customize_configuration_tv_minus_freezer);
        tvPlusFreezer= findViewById(R.id.customize_configuration_tv_plus_freezer);
        tvQuantityFreezer = findViewById(R.id.customize_configuration_tv_quantity_freezer);
    }

    private void initElectricOven() {
        tvMinusElectricOven = findViewById(R.id.customize_configuration_tv_minus_oven);
        tvPlusElectricOven = findViewById(R.id.customize_configuration_tv_plus_oven);
        tvQuantityElectricOven = findViewById(R.id.customize_configuration_tv_quantity_oven);
    }

    private void initPrinter() {
        tvMinusPrinter = findViewById(R.id.customize_configuration_tv_minus_printer);
        tvPlusPrinter= findViewById(R.id.customize_configuration_tv_plus_printer);
        tvQuantityPrinter = findViewById(R.id.customize_configuration_tv_quantity_printer);
    }

    private void initMicrowave() {
        tvMinusMicrowave = findViewById(R.id.customize_configuration_tv_minus_microwave);
        tvPlusMicrowave= findViewById(R.id.customize_configuration_tv_plus_microwave);
        tvQuantityMicrowave = findViewById(R.id.customize_configuration_tv_quantity_microwave);
    }

    private void initWasher() {
        tvMinusWasher = findViewById(R.id.customize_configuration_tv_minus_washer);
        tvPlusWasherWasher= findViewById(R.id.customize_configuration_tv_plus_washer);
        tvQuantityWasher = findViewById(R.id.customize_configuration_tv_quantity_washer);
    }

    private void initDicsh() {
        tvMinusDish = findViewById(R.id.customize_configuration_tv_minus_dish);
        tvPlusDish = findViewById(R.id.customize_configuration_tv_plus_dish);
        tvQuantityDish = findViewById(R.id.customize_configuration_tv_quantity_dish);
    }

    private void initDesktopScreen() {
        tvMinusDesktopScreen = findViewById(R.id.customize_configuration_tv_minus_screen);
        tvPlusDesktopScreen = findViewById(R.id.customize_configuration_tv_plus_screen);
        tvQuantityDesktopScreen = findViewById(R.id.customize_configuration_tv_quantity_screen);
    }

    private void initAC() {
        tvMinusAC = findViewById(R.id.customize_configuration_tv_minus_ac);
        tvPlusAC = findViewById(R.id.customize_configuration_tv_plus_ac);
        tvQuantityAC = findViewById(R.id.customize_configuration_tv_quantity_ac);
    }

    private void initBoiler() {
        tvMinusBoiler = findViewById(R.id.customize_configuration_tv_minus_boiler);
        tvPlusBoiler = findViewById(R.id.customize_configuration_tv_plus_boiler);
        tvQuantityBoiler = findViewById(R.id.customize_configuration_tv_quantity_boiler);
    }

    private void initDesktopPC() {
        tvMinusDesktopPc = findViewById(R.id.customize_configuration_tv_minus_desktop_pc);
        tvPlusDesktopPc = findViewById(R.id.customize_configuration_tv_plus_desktop_pc);
        tvQuantityDesktopPc = findViewById(R.id.customize_configuration_tv_quantity_desktop_pc);
    }

    private void initFridge() {
        tvMinusFridge = findViewById(R.id.customize_configuration_tv_minus_fridge);
        tvPlusFridge = findViewById(R.id.customize_configuration_tv_plus_fridge);
        tvQuantityFridge = findViewById(R.id.customize_configuration_tv_quantity_fridge);
    }

    private void initEvents(){
        btnNext.setOnClickListener(getNextListener());
        initMinusEvents();
        initPlusEvents();
    }

    private void initPlusEvents() {
        tvPlusFridge.setOnClickListener(getPlusListener(tvQuantityFridge, FRIDGE));
        tvPlusTv.setOnClickListener(getPlusListener(tvQuantityTv, TV));
        tvPlusFreezer.setOnClickListener(getPlusListener(tvQuantityFreezer, FREEZER));
        tvPlusPrinter.setOnClickListener(getPlusListener(tvQuantityPrinter, PRINTER));
        tvPlusMicrowave.setOnClickListener(getPlusListener(tvQuantityMicrowave, MICROWAVE));
        tvPlusElectricOven.setOnClickListener(getPlusListener(tvQuantityElectricOven, ELECTRIC_OVEN));
        tvPlusWasherWasher.setOnClickListener(getPlusListener(tvQuantityWasher, WASHER));
        tvPlusDish.setOnClickListener(getPlusListener(tvQuantityDish, DISH));
        tvPlusDesktopScreen.setOnClickListener(getPlusListener(tvQuantityDesktopScreen, DESKTOP_SCREEN));
        tvPlusAC.setOnClickListener(getPlusListener(tvQuantityAC, AC));
        tvPlusDesktopPc.setOnClickListener(getPlusListener(tvQuantityDesktopPc, DESKTOP_PC));
        tvPlusBoiler.setOnClickListener(getPlusListener(tvQuantityBoiler, BOILER));
        tvPlusAirPurifier.setOnClickListener(getPlusListener(tvQuantityAirPurifier, AIR_PURIFIER));
    }

    private View.OnClickListener getPlusListener(TextView tvQuantity, String category) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = Integer.parseInt(tvQuantity.getText().toString());
                if(quantity < 2){
                    quantity ++;
                    tvQuantity.setText(String.valueOf(quantity));
                    requestes_appliances_map.put(category, String.valueOf(quantity));
                    }
            }
        };
    }

    private void initMinusEvents() {
        tvMinusFridge.setOnClickListener(getMinusListeaner(tvQuantityFridge, FRIDGE));
        tvMinusTv.setOnClickListener(getMinusListeaner(tvQuantityTv, TV));
        tvMinusFreezer.setOnClickListener(getMinusListeaner(tvQuantityFreezer, FREEZER));
        tvMinusPrinter.setOnClickListener(getMinusListeaner(tvQuantityPrinter, PRINTER));
        tvMinusMicrowave.setOnClickListener(getMinusListeaner(tvQuantityMicrowave, MICROWAVE));
        tvMinusElectricOven.setOnClickListener(getMinusListeaner(tvQuantityElectricOven, ELECTRIC_OVEN));
        tvMinusWasher.setOnClickListener(getMinusListeaner(tvQuantityWasher, WASHER));
        tvMinusDish.setOnClickListener(getMinusListeaner(tvQuantityDish, DISH));
        tvMinusDesktopScreen.setOnClickListener(getMinusListeaner(tvQuantityDesktopScreen, DESKTOP_SCREEN));
        tvMinusAC.setOnClickListener(getMinusListeaner(tvQuantityAC, AC));
        tvMinusDesktopPc.setOnClickListener(getMinusListeaner(tvQuantityDesktopPc, DESKTOP_PC));
        tvMinusBoiler.setOnClickListener(getMinusListeaner(tvQuantityBoiler, BOILER));
        tvMinusAirPurifier.setOnClickListener(getMinusListeaner(tvQuantityAirPurifier, AIR_PURIFIER));
    }

    private View.OnClickListener getMinusListeaner(TextView tvQuantity, String category) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = Integer.parseInt(tvQuantity.getText().toString());
                if(quantity > 0){
                    quantity --;
                    tvQuantity.setText(String.valueOf(quantity));
                    if(quantity == 0){
                        requestes_appliances_map.remove(category);
                    } else {
                        requestes_appliances_map.put(category, String.valueOf(quantity));
                    }
                }
            }
        };
    }

    private View.OnClickListener getNextListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(requestes_appliances_map.isEmpty() || requestes_appliances_map.size() < 1){
                    Toast toast = new Toast(getApplicationContext());
                    editToast(toast, "Niciun electrocasnic selectat.");
                    toast.show();
                    return;
                }
                View budgetDialogView = getLayoutInflater().inflate(R.layout.dialog_set_budget,null);
                AlertDialog.Builder builder = new AlertDialog.Builder(CustomizeConfigurationActivity.this, R.style.MyAlertDialog)
                        .setView(budgetDialogView);
                AlertDialog budgetDialog = builder.create();
                budgetDialog.show();

                //views from dialog
                TextInputEditText tietBudget = budgetDialogView.findViewById(R.id.set_budget_dialog_tiet_budget);
                ImageView ivDialogClose = budgetDialogView.findViewById(R.id.set_budget_dialog_iv_close);
                Button btnSave =budgetDialogView.findViewById(R.id.set_budget_dialog_btn_next);

                //dialog events
                //close dialog
                ivDialogClose.setOnClickListener(getCloseDialogListener(budgetDialog));
                //save
                btnSave.setOnClickListener(getSaveConfigurationListener(budgetDialog, tietBudget));
            }
        };
    }

    private View.OnClickListener getSaveConfigurationListener(AlertDialog budgetDialog, TextInputEditText tietBudget) {
        return new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                try {
                    if(tietBudget.getText().toString()!=null && Double.valueOf(tietBudget.getText().toString().trim())>0){
                        budget = (int) Math.round(Double.valueOf(tietBudget.getText().toString().trim()));
                        budgetDialog.dismiss();
                        Intent intent = new Intent(CustomizeConfigurationActivity.this, CustomConfigurationActivity.class);

                        intent.putExtra(BUDGET_KEY, budget);

                        requested_appliances = requestes_appliances_map.keySet().parallelStream().collect(Collectors.joining(","));
                        intent.putExtra(REQUESTED_APPLIANCES_KEY, requested_appliances);

                        requested_quantities = requestes_appliances_map.values().parallelStream().collect(Collectors.joining(","));
                        intent.putExtra(REQUESTED_QUANTITIES_KEY, requested_quantities);

                        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(CustomizeConfigurationActivity.this);
                        startActivity(intent, options.toBundle());
                        finish();
                    } else{
                        Toast toast = new Toast(getApplicationContext());
                        editToast(toast, getString(R.string.msg_err_budget));
                        toast.show();
                    }
                } catch (Exception ex){
                    Toast toast = new Toast(getApplicationContext());
                    editToast(toast, getString(R.string.msg_err_budget));
                    toast.show();
                }

            }
        };
    }
    private void editToast(Toast toast, String p) {
        View toast_view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.toast_top_layout, null);
        ((TextView) toast_view.findViewById(R.id.toast_layout_tv)).setText(p);
        toast.setView(toast_view);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 0);
    }
    private View.OnClickListener getCloseDialogListener(AlertDialog budgetDialog) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                budgetDialog.dismiss();
            }
        };
    }

    private void enableTransitions() {
        //enable transition
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        // set transitions
        Slide slide = new Slide();
        slide.setSlideEdge(Gravity.END);
        slide.setInterpolator(new AnticipateOvershootInterpolator());
        getWindow().setEnterTransition(slide);
        getWindow().setExitTransition(slide);
    }
}