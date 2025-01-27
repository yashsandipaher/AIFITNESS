package com.example.aifitness;


import com.google.gson.JsonObject;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("predict")
    Call<JsonObject> predict(@Body HashMap<String, Object> data);
}