package com.example.wattescu.python.api.prediction;

import com.example.wattescu.python.api.optimization.OptimalConfigurationRequestObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PredictionApi {
        @GET("predict")
        Call<PredictionRequestObject> getResult(@Query("user") String userId);
}
