package com.example.wattescu.entities;

import com.google.firebase.database.Exclude;

import java.util.List;

public class Room {

    public static final String NO_ROOM_ID = "-";

    private String id;
    private String name;
    private String type;

    public Room() {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Exclude
    public Double getAverageMonthlyConsumption(List<Bulb> bulbs, List<Appliance> appliances) {
        Double consumption = 0.0;
        if (bulbs != null && bulbs.size() > 0) {
            for (Bulb bulb : bulbs) {
                if (bulb.getRoomId().equals(this.id)) {
                    consumption += bulb.getAverageMonthlyConsumtion();
                }
            }
        }
        if (appliances != null && appliances.size() > 0) {
            for (Appliance appliance : appliances) {
                if (appliance.getRoomId().equals(this.id)) {
                    consumption += appliance.getAverageMonthlyConsumption();
                }
            }
        }
        return consumption;
    }

    @Exclude
    public Double getAverageMonthlySpendings(List<Bulb> bulbs, List<Appliance> appliances, Double price) {
        Double spendings = 0.0;
        if (bulbs != null && bulbs.size() > 0) {
            for (Bulb bulb : bulbs) {
                if (bulb.getRoomId().equals(this.id)) {
                    spendings += bulb.getAverageMonthlySpendings(price);
                }
            }
        }
        if (appliances != null && appliances.size() > 0) {
            for (Appliance appliance : appliances) {
                if (appliance.getRoomId().equals(this.id)) {
                    spendings += appliance.getAverageMonthlySpendings(price);
                }
            }
        }
        return spendings;
    }

    @Exclude
    public int getNoBulbs(List<Bulb> bulbs) {
        int noBulbs = 0;
        if (bulbs != null && bulbs.size() > 0) {
            for (Bulb bulb : bulbs) {
                if (bulb.getRoomId().equals(this.id)) {
                    noBulbs++;
                }
            }
        }
        return noBulbs;
    }

    @Exclude
    public int getNoAppliances(List<Appliance> appliances) {
        int noAppliances = 0;
        if (appliances != null && appliances.size() > 0) {
            for (Appliance appliance : appliances) {
                if (appliance.getRoomId().equals(this.id)) {
                    noAppliances++;
                }
            }
        }
        return noAppliances;
    }

}
