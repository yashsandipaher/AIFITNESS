package com.example.aifitness;

import com.google.gson.annotations.SerializedName;
import java.util.List;
public class NutritionItem {
    @SerializedName("name")
    public String name;

    @SerializedName("calories")
    public float calories;

    @SerializedName("protein_g")
    public float protein;

    @SerializedName("carbohydrates_total_g")
    public float carbs;

    @SerializedName("fat_total_g")
    public float fat;
}
