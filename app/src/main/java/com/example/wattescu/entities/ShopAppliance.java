package com.example.wattescu.entities;

import java.io.Serializable;

public class ShopAppliance implements Serializable {

    long id;
    String category;
    String name;
    Double price;
    String efficiencyClass;
    Double power;
    String url;
    String imageUrl;
    String room;
    Double yearlyConsumption;

    public ShopAppliance() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getEfficiencyClass() {
        return efficiencyClass;
    }

    public void setEfficiencyClass(String efficiencyClass) {
        this.efficiencyClass = efficiencyClass;
    }

    public Double getPower() {
        return power;
    }

    public void setPower(Double power) {
        this.power = power;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public Double getYearlyConsumption() {
        return yearlyConsumption;
    }

    public void setYearlyConsumption(Double yearlyConsumption) {
        this.yearlyConsumption = yearlyConsumption;
    }
}
