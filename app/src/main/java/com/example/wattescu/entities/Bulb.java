package com.example.wattescu.entities;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Bulb implements Serializable {

    private String id;
    private String roomId;
    private String name;
    private String type;
    private Double power;
    private Map<String, Double> averageUseWeekDays;

    public Bulb() {
        //0 consumption
        averageUseWeekDays = new HashMap<>();
        for(DayOfWeek day: DayOfWeek.values()){
            averageUseWeekDays.put(String.valueOf(day),0.0);
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

    @Exclude
    public Double getAverageDailyConsumption(){
        Double sum = 0.0;
        for(DayOfWeek day: DayOfWeek.values()){
            sum+=averageUseWeekDays.get(String.valueOf(day));
        }
        return sum/7*power;
    }

    @Exclude
    public Double getAverageMonthlyConsumtion(){
        return getAverageDailyConsumption()* Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    @Exclude
    public Double getAverageMonthlySpendings(Double price){
        return getAverageMonthlyConsumtion()*price;
    }

    @Exclude
    @RequiresApi(api = Build.VERSION_CODES.O)
    public Double getCurrentWeekDayConsumption(){
        DayOfWeek currentDay = LocalDate.now().getDayOfWeek();
        try {
            return averageUseWeekDays.get(String.valueOf(currentDay)) * power;
        }catch (Exception ex){
            return null;
        }
    }
}
