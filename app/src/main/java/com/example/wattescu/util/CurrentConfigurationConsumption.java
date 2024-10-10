package com.example.wattescu.util;

import android.annotation.SuppressLint;

import com.example.wattescu.entities.Appliance;
import com.example.wattescu.entities.Bulb;

import java.util.List;

public class CurrentConfigurationConsumption {

    public static final double KW_TO_CO2 = 0.31;

    //this day of week consumption
    @SuppressLint("NewApi")
    public static Double getDayOfWeekConsumption(List<Bulb> bulbs, List<Appliance> appliances){
        Double consumption = 0.0;
        if(bulbs!=null){
            for (Bulb bulb:
                 bulbs) {
            consumption += bulb.getCurrentWeekDayConsumption();
            }
        }
        if(appliances!=null){
            for (Appliance appliance:
                    appliances) {
                consumption += appliance.getCurrentWeekDayConsumption();
            }
        }
        return consumption;
    }

    //this month consumption
    public static Double getMonthlyConsumption(List<Bulb> bulbs, List<Appliance> appliances){
        Double consumption = 0.0;
        if(bulbs!=null){
            for (Bulb bulb:
                    bulbs) {
                consumption += bulb.getAverageMonthlyConsumtion();
            }
        }
        if(appliances!=null){
            for (Appliance appliance:
                    appliances) {
                consumption += appliance.getAverageMonthlyConsumption();
            }
        }
        return consumption;
    }

    //this month spendings
    public static Double getMonthlySpendings(List<Bulb> bulbs, List<Appliance> appliances, Double priceKw){
        Double spendings = 0.0;
        if(bulbs!=null){
            for (Bulb bulb:
                    bulbs) {
                spendings += bulb.getAverageMonthlySpendings(priceKw);
            }
        }
        if(appliances!=null){
            for (Appliance appliance:
                    appliances) {
                spendings += appliance.getAverageMonthlySpendings(priceKw);
            }
        }
        return spendings;
    }
    //this month kg CO2 emissions
    public static Double getMonthlyCo2Emissions(List<Bulb> bulbs, List<Appliance> appliances){
        return CurrentConfigurationConsumption.getMonthlyConsumption(bulbs,appliances)* KW_TO_CO2;
    }
}
