package com.example.aifitness;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.example.aifitness.databinding.ActivityChooseAgeBinding;
import com.example.aifitness.databinding.ActivityChooseGoalBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class choose_goal extends AppCompatActivity {


    ActivityChooseGoalBinding binding;
    String selectedGoal;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChooseGoalBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(getResources().getColor(R.color.night));



        firebaseFirestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetButtonSelections();
                v.setSelected(true);
                selectedGoal = ((Button) v).getText().toString(); // store selected button
                binding.nextBtn.setEnabled(true);
            }
        };

        binding.looseWeight.setOnClickListener(listener);
        binding.gainWeight.setOnClickListener(listener);


        binding.nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(selectedGoal != null)
                {
                    SaveDataFireStore(selectedGoal);
                    Intent intent = new Intent(choose_goal.this,fitness_level.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                }
                else {
                    Toast.makeText(choose_goal.this, "Please select a goal", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
    private void resetButtonSelections() {
        binding.looseWeight.setSelected(false);
        binding.gainWeight.setSelected(false);
    }

    private void SaveDataFireStore(String selectedGoal)
    {
        FirebaseUser user = auth.getCurrentUser();
        if(user != null)
        {
            String userId = user.getUid();

            Map<String,Object> userData = new HashMap<>();
            userData.put("Goal",selectedGoal);

            firebaseFirestore.collection("users").document(userId)
                    .update(userData).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d(TAG, "onSuccess: Goal");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "onFailure: Goal");
                        }
                    });

        }
    }

}