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
import android.widget.Toast;

import com.example.aifitness.databinding.ActivityChooseAgeBinding;
import com.example.aifitness.databinding.ActivityChooseGenderBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class chooseGender extends AppCompatActivity {

    ActivityChooseGenderBinding binding;
    String selectedGoal;
    FirebaseAuth auth;
    FirebaseFirestore firebaseFirestore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChooseGenderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(getResources().getColor(R.color.night));



        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();


        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetButtonSelections();
                v.setSelected(true);
                selectedGoal = ((Button) v).getText().toString(); // store selected button
                binding.nextBtn.setEnabled(true);
            }
        };

        binding.maleBtn.setOnClickListener(listener);
        binding.femaleBtn.setOnClickListener(listener);


        binding.nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(selectedGoal != null)
                {

                    SaveDataFireStore(selectedGoal);
                    Intent intent = new Intent(chooseGender.this,choose_goal.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                }
                else {
                    Toast.makeText(chooseGender.this, "Please select a gender", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
    private void resetButtonSelections() {
        binding.maleBtn.setSelected(false);
        binding.femaleBtn.setSelected(false);
    }

    private void SaveDataFireStore(String selectedGoal)
    {
        FirebaseUser user = auth.getCurrentUser();
        if(user != null)
        {
            String userId = user.getUid();

            Map<String,Object> userData = new HashMap<>();
            userData.put("Gender",selectedGoal);

            firebaseFirestore.collection("users").document(userId)
                    .update(userData).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d(TAG, "onSuccess: gender");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: gender");
                        }
                    });

        }
    }

}