package com.example.wattescu.entities;

import java.util.HashMap;
import java.util.Map;

public class CustomConfiguration {

    private  String id;
    private String name;
    private Map<String, SavedShopAppliance> savedAppliances;
    private boolean isOptim;

    public CustomConfiguration() {
        savedAppliances = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, SavedShopAppliance> getSavedAppliances() {
        return savedAppliances;
    }

    public void setSavedAppliances(Map<String, SavedShopAppliance> savedAppliances) {
        this.savedAppliances = savedAppliances;
    }

    public boolean isOptim() {
        return isOptim;
    }

    public void setOptim(boolean optim) {
        isOptim = optim;
    }
}
