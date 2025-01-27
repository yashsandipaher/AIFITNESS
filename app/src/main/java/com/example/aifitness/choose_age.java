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
import android.widget.Toast;

import com.example.aifitness.databinding.ActivityChooseAgeBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class choose_age extends AppCompatActivity {


    ActivityChooseAgeBinding binding;
    FirebaseFirestore firebaseFirestore;
    String selectedAge;
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChooseAgeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(getResources().getColor(R.color.night));




        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        binding.nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                selectedAge = binding.etAge.getText().toString();

                // Check if the age is not empty
                if (selectedAge.isEmpty()) {
                    binding.etAge.setError("Please enter a age");
                }
                // Check if the entered age is between 18 and 100
                else {
                    int age = Integer.parseInt(selectedAge);
                    if (age < 18 || age > 100) {
                        binding.etAge.setError("Please enter a valid age between 18 and 100");
                    } else {
                        // Age is valid, proceed to save and move to the next activity
                        SaveDataFireStore(selectedAge);
                        Intent intent = new Intent(choose_age.this, choose_weight.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        finish();
                    }
                }
            }
        });
    }



    private void SaveDataFireStore(String selectedGoal)
    {
        FirebaseUser user = auth.getCurrentUser();
        if(user != null)
        {
            String userId = user.getUid();

            Map<String,Object> userData = new HashMap<>();
            userData.put("Age",selectedGoal);

            firebaseFirestore.collection("users").document(userId)
                    .update(userData).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d(TAG, "onSuccess: age");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: age");
                        }
                    });

        }
    }

}