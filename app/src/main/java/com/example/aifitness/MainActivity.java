package com.example.aifitness;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;


import com.ekn.gruzer.gaugelibrary.HalfGauge;
import com.ekn.gruzer.gaugelibrary.Range;
import com.example.aifitness.databinding.ActivityMainBinding;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class MainActivity extends AppCompatActivity {


    ActivityMainBinding binding;
    FirebaseAuth auth;
    FirebaseFirestore firebaseFirestore;
    DocumentReference documentReference;
    private HalfGauge bmiGauge;
    float weight,height,bmi;
    int age;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(getResources().getColor(R.color.rosewood));


        bmiGauge = findViewById(R.id.bmiGauge);
        firebaseFirestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();
        documentReference = firebaseFirestore.collection("users").document(userId);

        fetchUserData();
        fetchDate();
        setupBMIGauge();

        binding.btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                Intent intent = new Intent(MainActivity.this,login.class);
                startActivity(intent);
                finish();
            }
        });

        binding.btnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,userProfile.class);
                startActivity(intent);
            }
        });


        binding.dietPlanStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this,DietPlan.class);
                startActivity(intent);

            }
        });

        binding.exerciseStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MainActivity.this,ExercisePlan.class);
                startActivity(intent);

            }
        });


    }


    private void fetchUserData() {
        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                // Set name
                binding.nameTxt.setText(value.getString("Name"));

                // Get weight and height from Firestore
                String get_weight = value.getString("Weight");
                String get_Height = value.getString("Height");
                String fitnessGoal = value.getString("Goal");
                String fitnessLevelFromDB = value.getString("Fitness Level");

                // Set weight and height to text views
                binding.NormalWeight.setText(get_weight);
                binding.normalHeight.setText(get_Height);

                if (get_weight != null && get_Height != null) {
                    try {
                        // Convert string weight (kg) and height (cm) to double
                        double weight = Double.parseDouble(get_weight);
                        double heightInMeters = Double.parseDouble(get_Height) / 100; // Convert height to meters

                        // Calculate BMI
                        double calculatedBMI = weight / (heightInMeters * heightInMeters);

                        // Format BMI to 2 decimal places and set it to the TextView
                        String formattedBMI = String.format("%.2f", calculatedBMI);
                        binding.BMI.setText(formattedBMI);

                        bmiGauge.setValue(calculatedBMI);

                        // Determine fitness level based on BMI
                        String fitnessLevel;
                        if (calculatedBMI < 18.5) {
                            fitnessLevel = "Underweight";
                        } else if (calculatedBMI >= 18.5 && calculatedBMI < 25) {
                            fitnessLevel = "Normal weight";
                        } else if (calculatedBMI >= 25 && calculatedBMI < 30) {
                            fitnessLevel = "Overweight";
                        } else {
                            fitnessLevel = "Obesity";
                        }
                        binding.Category.setText(fitnessLevel);

                        // Update BMI and fitness level in Firestore
                        Map<String, Object> updatedData = new HashMap<>();
                        updatedData.put("BMI", calculatedBMI);  // Save calculated BMI
                        updatedData.put("Fitness Level", fitnessLevel);  // Save fitness level

                        // Save the updated data to Firestore
                        documentReference.update(updatedData)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "BMI and Fitness Level updated successfully");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error updating BMI and Fitness Level", e);
                                    }
                                });

                        // Trigger the popup if fitness goal contradicts fitness level
                        checkForGoalMismatch(fitnessLevelFromDB, fitnessGoal);

                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Error parsing weight or height", e);
                    }
                } else {
                    binding.BMI.setText("N/A");  // Handle null or invalid weight/height
                }
            }
        });
    }

    private void checkForGoalMismatch(String fitnessLevel, String fitnessGoal) {
        boolean shouldPopup = false;
        String message = "";

        // Check if the user is underweight and selected weight loss
        if (fitnessLevel.equals("Underweight") && fitnessGoal.equals("Weight Loss")) {
            shouldPopup = true;
            message = "You are already underweight and have selected Weight Loss. Please change your goal.";
        }
        // Check if the user is overweight or obese and selected weight gain
        else if ((fitnessLevel.equals("Overweight") || fitnessLevel.equals("Obesity")) && fitnessGoal.equals("Weight Gain")) {
            shouldPopup = true;
            message = "You are already overweight and have selected Weight Gain. Please change your goal.";
        }

        // If there's a mismatch, show the popup
        if (shouldPopup) {
            showGoalPopup(message);
        }
    }

    private void showGoalPopup(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Goal Mismatch")
                .setMessage(message)
                .setPositiveButton("Change Goal", (dialog, which) -> {
                    Intent intent = new Intent(this,userProfile.class);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void fetchDate() {
        Date today = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
        String formattedDate = dateFormat.format(today);
        binding.dateTxt.setText(formattedDate);
    }

    private void setupBMIGauge() {
        bmiGauge.setMinValue(0);  // Minimum value for BMI
        bmiGauge.setMaxValue(40); // Maximum value for BMI, you can adjust this as needed

        // Underweight Range: <18.5
        Range underweightRange = new Range();
        underweightRange.setFrom(0);
        underweightRange.setTo(18.5);
        underweightRange.setColor(Color.rgb(33, 166, 243)); // Light Blue

        // Normal weight Range: 18.5–24.9
        Range normalRange = new Range();
        normalRange.setFrom(18.5);
        normalRange.setTo(24.9);  // Change to 24.9
        normalRange.setColor(Color.rgb(64, 188, 100)); // Lime Green

        // Overweight Range: 25–29.9
        Range overweightRange = new Range();
        overweightRange.setFrom(25.0);
        overweightRange.setTo(29.9);  // Change to 29.9
        overweightRange.setColor(Color.rgb(252, 196, 84)); // Orange

        // Obesity Range: 30 and above
        Range obesityRange = new Range();
        obesityRange.setFrom(30.0);
        obesityRange.setTo(40.0);  // Set max BMI gauge value
        obesityRange.setColor(Color.rgb(252, 84, 72)); // Tomato Red

        // Adding all ranges to the gauge
        bmiGauge.addRange(underweightRange);
        bmiGauge.addRange(normalRange);
        bmiGauge.addRange(overweightRange);
        bmiGauge.addRange(obesityRange);

        // Set the needle color
        bmiGauge.setNeedleColor(Color.DKGRAY);
    }

}