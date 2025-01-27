package com.example.aifitness;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.aifitness.databinding.ActivityUserProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class userProfile extends AppCompatActivity {

    ActivityUserProfileBinding binding;
    FirebaseAuth auth;
    FirebaseFirestore firebaseFirestore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(getResources().getColor(R.color.rosewood));

        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();


        getUserData();

        binding.btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showEditProfileDialog();

            }
        });


    }

    private void getUserData() {

        String userId = auth.getCurrentUser().getUid();

        DocumentReference documentReference = firebaseFirestore.collection("users").document(userId);

        documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {

                binding.tvName.setText(value.getString("Name"));
                binding.tvEmail.setText(value.getString("Email"));
                binding.tvAge.setText(value.getString("Age"));
                binding.tvGender.setText(value.getString("Gender"));
                binding.tvHeight.setText(value.getString("Height"));
                binding.tvWeight.setText(value.getString("Weight"));
                binding.tvFitnessLevel.setText(value.getString("Fitness Level"));
                binding.tvFitnessGoal.setText(value.getString("Goal"));
                binding.tvHealthIssueDiabetes.setText(value.getString("Diabetes"));
                binding.tvHealthIssueHypertension.setText(value.getString("Hypertension"));
            }
        });
    }


    private void showEditProfileDialog() {
        String userId = auth.getCurrentUser().getUid();
        DocumentReference documentReference = firebaseFirestore.collection("users").document(userId);

        documentReference.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                final Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.activity_edit_profile_dialog);

                // Initialize EditText fields
                EditText etName = dialog.findViewById(R.id.etName);
                EditText etEmail = dialog.findViewById(R.id.etEmail);
                EditText etAge = dialog.findViewById(R.id.etAge);
                EditText etGender = dialog.findViewById(R.id.etGender);
                EditText etHeight = dialog.findViewById(R.id.etHeight);
                EditText etWeight = dialog.findViewById(R.id.etWeight);
                EditText etFitnessLevel = dialog.findViewById(R.id.etFitnessLevel);
                EditText etFitnessGoal = dialog.findViewById(R.id.etFitnessGoal);
                EditText etHealthIssueDiabetes = dialog.findViewById(R.id.etHealthIssueDiabetes);
                EditText etHealthIssueHypertension = dialog.findViewById(R.id.etHealthIssueHypertension);

                // Pre-fill the fields with the current data from Firestore
                etName.setText(documentSnapshot.getString("Name"));
                etEmail.setText(documentSnapshot.getString("Email"));
                etAge.setText(documentSnapshot.getString("Age"));
                etGender.setText(documentSnapshot.getString("Gender"));
                etHeight.setText(documentSnapshot.getString("Height"));
                etWeight.setText(documentSnapshot.getString("Weight"));
                etFitnessLevel.setText(documentSnapshot.getString("Fitness Level"));
                etFitnessGoal.setText(documentSnapshot.getString("Goal"));
                etHealthIssueDiabetes.setText(documentSnapshot.getString("Diabetes"));
                etHealthIssueHypertension.setText(documentSnapshot.getString("Hypertension"));

                // Set up the save button to update the data in Firestore
                Button btnSave = dialog.findViewById(R.id.btnSave);
                btnSave.setOnClickListener(v -> {
                    // Get the updated values from EditTexts
                    String name = etName.getText().toString();
                    String email = etEmail.getText().toString();
                    String age = etAge.getText().toString();
                    String gender = etGender.getText().toString();
                    String height = etHeight.getText().toString();
                    String weight = etWeight.getText().toString();
                    String fitnessLevel = etFitnessLevel.getText().toString();
                    String fitnessGoal = etFitnessGoal.getText().toString();
                    String healthIssueDiabetes = etHealthIssueDiabetes.getText().toString();
                    String healthIssueHypertension = etHealthIssueHypertension.getText().toString();

                    // Validate the inputs
                    if (validateInputs(etAge, etHeight, etWeight)) {
                        // Update Firestore with the new values
                        updateUserProfile(name, email, age, gender, height, weight, fitnessLevel, fitnessGoal, healthIssueDiabetes, healthIssueHypertension);
                        dialog.dismiss();
                    }
                });

                dialog.show();
            } else {
                Toast.makeText(userProfile.this, "User data not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(userProfile.this, "Error fetching user data", Toast.LENGTH_SHORT).show());
    }

    private boolean validateInputs(EditText etAge, EditText etHeight, EditText etWeight) {
        boolean isValid = true;

        // Age validation (10-100 years)
        String ageStr = etAge.getText().toString();
        if (!TextUtils.isEmpty(ageStr)) {
            int age = Integer.parseInt(ageStr);
            if (age < 10 || age > 100) {
                etAge.setError("Please enter a valid age between 10 and 100");
                isValid = false;
            }
        } else {
            etAge.setError("Please enter an age");
            isValid = false;
        }

        // Height validation (120-250 cm)
        String heightStr = etHeight.getText().toString();
        if (!TextUtils.isEmpty(heightStr)) {
            int height = Integer.parseInt(heightStr);
            if (height < 120 || height > 250) {
                etHeight.setError("Please enter a valid height between 120 and 250 cm");
                isValid = false;
            }
        } else {
            etHeight.setError("Please enter a height");
            isValid = false;
        }

        // Weight validation (30-300 kg)
        String weightStr = etWeight.getText().toString();
        if (!TextUtils.isEmpty(weightStr)) {
            int weight = Integer.parseInt(weightStr);
            if (weight < 30 || weight > 300) {
                etWeight.setError("Please enter a valid weight between 30 and 300 kg");
                isValid = false;
            }
        } else {
            etWeight.setError("Please enter a weight");
            isValid = false;
        }

        return isValid;
    }

    private void updateUserProfile(String name, String email, String age, String gender, String height,
                                   String weight, String fitnessLevel, String fitnessGoal, String healthIssueDiabetes, String healthIssueHypertension) {
        String userId = auth.getCurrentUser().getUid();
        DocumentReference documentReference = firebaseFirestore.collection("users").document(userId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("Name", name);
        updates.put("Email", email);
        updates.put("Age", age);
        updates.put("Gender", gender);
        updates.put("Height", height);
        updates.put("Weight", weight);
        updates.put("Fitness Level", fitnessLevel);
        updates.put("Goal", fitnessGoal);
        updates.put("Diabetes", healthIssueDiabetes);
        updates.put("Hypertension", healthIssueHypertension);

        documentReference.update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(userProfile.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                    getUserData(); // Refresh the user data
                })
                .addOnFailureListener(e -> Toast.makeText(userProfile.this, "Error updating profile", Toast.LENGTH_SHORT).show());
    }
}