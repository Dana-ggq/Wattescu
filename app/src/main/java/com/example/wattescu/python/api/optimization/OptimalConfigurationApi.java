package com.example.wattescu.python.api.optimization;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OptimalConfigurationApi {
    @GET("optimalconfig")
    Call<OptimalConfigurationRequestObject> getResult(@Query("budget") int budget,
                                                      @Query("categories") String requestedCategories,
                                                      @Query("quantities") String requestedQuantities);
}