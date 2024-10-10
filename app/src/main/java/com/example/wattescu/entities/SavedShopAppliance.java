package com.example.wattescu.entities;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.wattescu.util.CurrentConfigurationConsumption;
import com.google.firebase.encoders.annotations.Encodable;

import java.io.Serializable;
import java.util.List;

public class SavedShopAppliance extends ShopAppliance implements Serializable {

    private String applianceToReplaceId;


    public SavedShopAppliance() {

        super();
        this.id = -1;
    }

    public SavedShopAppliance(ShopAppliance shopAppliance) {
        this.id = shopAppliance.id;
        this.category = shopAppliance.category;
        this.efficiencyClass = shopAppliance.efficiencyClass;
        this.imageUrl = shopAppliance.imageUrl;
        this.name = shopAppliance.name;
        this.power = shopAppliance.power;
        this.price = shopAppliance.price;
        this.room = shopAppliance.room;
        this.url = shopAppliance.url;
        this.yearlyConsumption = shopAppliance.yearlyConsumption;
    }

    public String getApplianceToReplaceId() {
        return applianceToReplaceId;
    }

    public void setApplianceToReplaceId(String applianceToReplaceId) {
        if(applianceToReplaceId!=null) {
            this.applianceToReplaceId = applianceToReplaceId;
        } else {
           this.applianceToReplaceId = null;
        }
    }

    @Encodable.Ignore
    @RequiresApi(api = Build.VERSION_CODES.N)
    public Double getConsumptionReduction(List<Appliance> appliances) throws Exception {
        if (this.applianceToReplaceId != null) {
            Appliance currentAppliance = appliances.stream()
                    .filter(appliance -> this.applianceToReplaceId.equals(appliance.getId()))
                    .findAny()
                    .orElse(null);
            if (currentAppliance != null) {
                if (this.power != null) {
                    return getYearlyConsumptionReductionByPowe(currentAppliance);
                } else if (this.yearlyConsumption != null){
                    return getYearlyConsumptionReduction(currentAppliance);
                }
            }
        }
        throw new Exception("No reduction");
    }

    @Encodable.Ignore
    private Double getYearlyConsumptionReductionByPowe(Appliance currentAppliance) throws Exception {
        Double newYearlyConsumption = 0.0;
        for (Double monthUsage:
                currentAppliance.getAverageUseMonth().values()) {
            newYearlyConsumption += monthUsage*this.power;
        }

        Double currentYearlyConsumption = 0.0;
        for (Double monthConsumption:
                currentAppliance.getAverageConsumptionMonth().values()) {
            currentYearlyConsumption += monthConsumption;
        }

        if(currentYearlyConsumption - newYearlyConsumption> 0){
            return currentYearlyConsumption - newYearlyConsumption;
        } else {
            throw new Exception("No reduction");
        }
    }

    @Encodable.Ignore
    private Double getYearlyConsumptionReduction(Appliance currentAppliance) throws Exception {
        Double currentYearlyConsumption = 0.0;
        for (Double monthConsumption:
             currentAppliance.getAverageConsumptionMonth().values()) {
            currentYearlyConsumption += monthConsumption;
        }
        if(currentYearlyConsumption - this.yearlyConsumption > 0){
            return currentYearlyConsumption - this.yearlyConsumption;
        } else {
            throw new Exception("No reduction");
        }
    }

    @Encodable.Ignore
    @RequiresApi(api = Build.VERSION_CODES.N)
    public Double getSavings(List<Appliance> appliances, Double price) throws Exception {
        if (this.applianceToReplaceId != null) {
            Appliance currentAppliance = appliances.stream()
                    .filter(appliance -> this.applianceToReplaceId.equals(appliance.getId()))
                    .findAny()
                    .orElse(null);
            if (currentAppliance != null) {
               return getConsumptionReduction(appliances) * price;
            }
        }
        throw new Exception("No savings");
    }

    @Encodable.Ignore
    @RequiresApi(api = Build.VERSION_CODES.N)
    public Double getCo2Savings(List<Appliance> appliances) throws Exception {
        if (this.applianceToReplaceId != null) {
            Appliance currentAppliance = appliances.stream()
                    .filter(appliance -> this.applianceToReplaceId.equals(appliance.getId()))
                    .findAny()
                    .orElse(null);
            if (currentAppliance != null) {
                return getConsumptionReduction(appliances) * CurrentConfigurationConsumption.KW_TO_CO2;
            }
        }
        throw new Exception("No savings");
    }
}