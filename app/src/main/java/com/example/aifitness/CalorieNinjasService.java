package com.example.aifitness;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface CalorieNinjasService {
    @Headers("X-Api-Key: 2hdpufQxPYSfB+yUIyM7QA==d71VEVdTuHtnNvDw") // Replace YOUR_API_KEY with your actual API key
    @GET("v1/nutrition")
    Call<NutritionResponse> getNutrition(@Query("query") String query);
}
