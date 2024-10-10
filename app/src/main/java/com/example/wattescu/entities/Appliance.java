package com.example.wattescu.entities;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Appliance implements Serializable {

    private String id;
    private String roomId;
    private String name;

    private String type;
    private String efficiencyClass;
    private Double power;
    private Map<String, Double> averageUseWeekDays;
    private Map<String, Double> averageUseMonth;
    private Map<String, Double> averageConsumptionMonth;

    public Appliance() {
        //0 consumption
        averageUseWeekDays = new HashMap<>();
        for(DayOfWeek day: DayOfWeek.values()){
            averageUseWeekDays.put(String.valueOf(day),0.0);
        }
        averageUseMonth = new HashMap<>();
        averageConsumptionMonth = new HashMap<>();
        for(Month month: Month.values()){
            averageUseMonth.put(String.valueOf(month),0.0);
            averageConsumptionMonth.put(String.valueOf(month),0.0);
        }
        // no room asigned
        roomId = Room.NO_ROOM_ID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getPower() {
        return power;
    }

    public void setPower(Double power) {
        this.power = power;
    }

    public  Map<String, Double> getAverageUseWeekDays() {
        return averageUseWeekDays;
    }

    public void setAverageUseWeekDays(  Map<String, Double> averageUseWeekDays) {
        this.averageUseWeekDays = averageUseWeekDays;
    }
    public Map<String, Double> getAverageUseMonth() {
        return averageUseMonth;
    }

    public void setAverageUseMonth(Map<String, Double> averageUseMonth) {
        this.averageUseMonth = averageUseMonth;
    }
    public String getEfficiencyClass() {
        return efficiencyClass;
    }

    public void setEfficiencyClass(String efficiencyClass) {
        this.efficiencyClass = efficiencyClass;
    }

    public Map<String, Double> getAverageConsumptionMonth() {
        return averageConsumptionMonth;
    }

    public void setAverageConsumptionMonth(Map<String, Double> averageConsumptionMonth) {
        this.averageConsumptionMonth = averageConsumptionMonth;
    }

    @Exclude
    public Double getAverageDailyConsumption(){
        Double sum = 0.0;
        for(DayOfWeek day: DayOfWeek.values()){
            sum+=averageUseWeekDays.get(String.valueOf(day));
        }
        return sum/7*power;
    }

    @Exclude
    public Double getAverageDailyUse(){
        Double sum = 0.0;
        for(DayOfWeek day: DayOfWeek.values()){
            sum+=averageUseWeekDays.get(String.valueOf(day));
        }
        return sum/7;
    }

    @Exclude
    public Double getAverageMonthlyUse(){
        return getAverageDailyUse()* Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
    }
    @Exclude
    public Double getAverageMonthlyConsumption(){
        return getAverageDailyConsumption()* Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    @Exclude
    public Double getAverageMonthlySpendings(Double price){
        return getAverageMonthlyConsumption()*price;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Exclude
    public Double getCurrentWeekDayConsumption(){
        DayOfWeek currentDay = LocalDate.now().getDayOfWeek();
        return averageUseWeekDays.get(String.valueOf(currentDay))*power;

    }
}
