package com.example.aifitness;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.example.aifitness.databinding.ActivitySplashDetailsBinding;
import com.google.firebase.auth.FirebaseAuth;

public class splash_details extends AppCompatActivity {

    ActivitySplashDetailsBinding binding;
    FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(getResources().getColor(R.color.night));


        auth = FirebaseAuth.getInstance();

        if(auth.getCurrentUser() != null)
        {
            Intent intent = new Intent(splash_details.this,MainActivity.class);
            startActivity(intent);
            finish();
        }

        binding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(splash_details.this,signUp.class);
                startActivity(intent);
                finish();
            }
        });


    }
}