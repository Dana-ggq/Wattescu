package com.example.wattescu.python.api.optimization;

import com.google.gson.annotations.SerializedName;

public class OptimalConfigurationRequestObject {

    private String result;

    @SerializedName("appliances_ids")
    private String appliancesIds;

    public String getResult() {
        return result;
    }

    public String getAppliancesIds() {
        return appliancesIds;
    }
}
