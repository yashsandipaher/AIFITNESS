package com.example.aifitness;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.example.aifitness.databinding.ActivityHealthIssueBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class health_issue extends AppCompatActivity {

    ActivityHealthIssueBinding binding;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth auth;
    boolean isDiabetesSelected = false;
    boolean isHypertensionSelected = false;
    Button diabetesButton, hypertensionButton;
    CheckBox noneOfTheseCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHealthIssueBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(getResources().getColor(R.color.night));

        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        diabetesButton = findViewById(R.id.health_sugar);
        hypertensionButton = findViewById(R.id.health_heart);
        noneOfTheseCheckBox = findViewById(R.id.health_none_of_these);

        // Set click listeners
        diabetesButton.setOnClickListener(view -> toggleDiabetesSelection());
        hypertensionButton.setOnClickListener(view -> toggleHypertensionSelection());
        noneOfTheseCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> handleNoneOfTheseSelection(isChecked));

        // Continue button action
        Button continueButton = findViewById(R.id.next_btn);
        continueButton.setOnClickListener(view -> {
            saveHealthData();
            calculateAndStoreBMI(); // Calculate and store BMI after saving health data
        });
    }

    private void toggleDiabetesSelection() {
        isDiabetesSelected = !isDiabetesSelected;
        diabetesButton.setSelected(isDiabetesSelected);  // Set selected state
        noneOfTheseCheckBox.setChecked(false);  // Deselect "None of these" when Diabetes is selected
    }

    private void toggleHypertensionSelection() {
        isHypertensionSelected = !isHypertensionSelected;
        hypertensionButton.setSelected(isHypertensionSelected);  // Set selected state
        noneOfTheseCheckBox.setChecked(false);  // Deselect "None of these" when Hypertension is selected
    }

    private void handleNoneOfTheseSelection(boolean isChecked) {
        if (isChecked) {
            // Deselect both Diabetes and Hypertension when "None of these" is checked
            isDiabetesSelected = false;
            isHypertensionSelected = false;
            diabetesButton.setSelected(false);  // Change button state to unselected
            hypertensionButton.setSelected(false);  // Change button state to unselected
        }
    }

    private void saveHealthData() {
        String userId = auth.getCurrentUser().getUid();

        Map<String, Object> healthData = new HashMap<>();
        if (noneOfTheseCheckBox.isChecked()) {
            healthData.put("Diabetes", "No");
            healthData.put("Hypertension", "No");
        } else {
            healthData.put("Diabetes", isDiabetesSelected ? "Yes" : "No");
            healthData.put("Hypertension", isHypertensionSelected ? "Yes" : "No");
        }

        // Save to Firestore
        firebaseFirestore.collection("users").document(userId)
                .update(healthData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Intent intent = new Intent(health_issue.this, GeneatingAI.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                        Toast.makeText(health_issue.this, "Failed to update health data", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void calculateAndStoreBMI() {
        String userId = auth.getCurrentUser().getUid();

        firebaseFirestore.collection("users").document(userId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            // Get height and weight from Firestore as strings
                            String heightString = documentSnapshot.getString("Height");
                            String weightString = documentSnapshot.getString("Weight");

                            if (heightString != null && weightString != null) {
                                try {
                                    // Convert strings to doubles
                                    double height = Double.parseDouble(heightString);
                                    double weight = Double.parseDouble(weightString);

                                    // Calculate BMI
                                    double bmi = weight / ((height / 100) * (height / 100)); // height in meters

                                    // Determine fitness level
                                    String fitnessLevel;
                                    if (bmi < 18.5) {
                                        fitnessLevel = "Underweight";
                                    } else if (bmi >= 18.5 && bmi < 25) {
                                        fitnessLevel = "Normal weight";
                                    } else if (bmi >= 25 && bmi < 30) {
                                        fitnessLevel = "Overweight";
                                    } else {
                                        fitnessLevel = "Obesity";
                                    }

                                    // Store BMI and fitness level in Firestore
                                    Map<String, Object> bmiData = new HashMap<>();
                                    bmiData.put("BMI", bmi);

                                    firebaseFirestore.collection("users").document(userId)
                                            .update(bmiData)
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d(TAG, "BMI and Fitness Level successfully stored!");
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.w(TAG, "Error updating BMI and Fitness Level", e);
                                            });
                                } catch (NumberFormatException e) {
                                    Log.w(TAG, "Error parsing height or weight", e);
                                    Toast.makeText(health_issue.this, "Invalid height or weight format", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.w(TAG, "Height or weight not found");
                            }
                        } else {
                            Log.w(TAG, "User document does not exist");
                        }
                    }
                })
                .addOnFailureListener(e -> Log.w(TAG, "Error fetching user document", e));
    }




}

