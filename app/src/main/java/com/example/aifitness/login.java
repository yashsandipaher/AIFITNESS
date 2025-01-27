package com.example.aifitness;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.aifitness.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class login extends AppCompatActivity {


    ActivityLoginBinding binding;
    FirebaseAuth auth;
    private boolean isPasswordVisible = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(getResources().getColor(R.color.night));
        
        togglePassword();

        auth = FirebaseAuth.getInstance();

        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = binding.etEmail.getText().toString().trim();
                String pass = binding.etPassword.getText().toString().trim();

                if(TextUtils.isEmpty(email))
                {
                    binding.etEmail.setError("Email is Required");
                    return;
                }

                if(TextUtils.isEmpty(pass))
                {
                    binding.etPassword.setError("Password is Required");
                    return;
                }

                binding.progessBar.setVisibility(View.VISIBLE);

                auth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(login.this, "login successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(login.this,MainActivity.class);
                            startActivity(intent);
                            finish();
                        }else {
                            Toast.makeText(login.this, "login Failed !", Toast.LENGTH_SHORT).show();
                            binding.progessBar.setVisibility(View.GONE);
                        }
                    }
                });

            }
        });


        binding.signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(login.this,signUp.class);
                startActivity(intent);
            }
        });


    }

    private void togglePassword() {

        binding.eyeToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    // Hide the password
                    binding.etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    binding.eyeToggle.setImageResource(R.drawable.ic_eye_closed);
                } else {
                    // Show the password
                    binding.etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    binding.eyeToggle.setImageResource(R.drawable.ic_eye_opened);
                }
                isPasswordVisible = !isPasswordVisible;
                binding.etPassword.setSelection(binding.etPassword.getText().length()); // Move cursor to end of the text
            }
        });

    }
}