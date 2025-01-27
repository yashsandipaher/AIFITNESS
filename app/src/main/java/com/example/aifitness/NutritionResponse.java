package com.example.aifitness;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NutritionResponse {
    @SerializedName("items")
    public List<NutritionItem> items;
}