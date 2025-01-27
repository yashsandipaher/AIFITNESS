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
import com.example.aifitness.databinding.ActivityChooseHeightBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class choose_height extends AppCompatActivity {


    ActivityChooseHeightBinding binding;
    String selectedHeight;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChooseHeightBinding.inflate(getLayoutInflater());
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
                selectedHeight = binding.etHeight.getText().toString();

                if (selectedHeight.isEmpty()) {
                    binding.etHeight.setError("Please enter a height");
                }
                else {
                    int height = Integer.parseInt(selectedHeight);

                    // Validate if the height is between 120 and 250
                    if (height < 120 || height > 250) {
                        binding.etHeight.setError("Please enter a valid height between 120 and 250 cm");
                    }
                    else {
                        // Height is valid, proceed with saving to Firestore
                        SaveDataFireStore(selectedHeight);
                        Intent intent = new Intent(choose_height.this, choose_age.class);
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
            userData.put("Height",selectedGoal);

            firebaseFirestore.collection("users").document(userId)
                    .update(userData).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d(TAG, "onSuccess: height");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: height");
                        }
                    });

        }
    }

}