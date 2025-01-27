package com.example.aifitness;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.aifitness.databinding.ActivityChooseAgeBinding;
import com.example.aifitness.databinding.ActivitySignUpBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class signUp extends AppCompatActivity {

    ActivitySignUpBinding binding;
    FirebaseAuth auth;
    FirebaseFirestore firebaseFirestore;
    String userID;
    private boolean isPasswordVisible = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(getResources().getColor(R.color.night));


        togglePasswordFeature();


        auth= FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        if(auth.getCurrentUser() != null)
        {
            Intent intent = new Intent(signUp.this,login.class);
            startActivity(intent);
            finish();
        }

        binding.signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String name = binding.etName.getText().toString().trim();
                String email = binding.etEmail.getText().toString().trim();
                String pass = binding.etPass.getText().toString().trim();

                if(TextUtils.isEmpty(name))
                {
                    binding.etName.setError("Name is Required");
                    return;
                }

                if(TextUtils.isEmpty(email))
                {
                    binding.etEmail.setError("Email is Required");
                    return;
                }

                if(TextUtils.isEmpty(pass))
                {
                    binding.etPass.setError("Password is Required");
                    return;
                }

                binding.progressBar.setVisibility(View.VISIBLE);

                auth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(signUp.this, "User Created", Toast.LENGTH_SHORT).show();

                            userID = auth.getCurrentUser().getUid();
                            DocumentReference documentReference = firebaseFirestore.collection("users").document(userID);
                            Map<String,Object> user = new HashMap<>();
                            user.put("Name",name);
                            user.put("Email",email);
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Log.d(TAG, "onSuccess: DONE");
                                }
                            });

                            Intent intent = new Intent(signUp.this,chooseGender.class);
                            startActivity(intent);
                            finish();
                        }else {
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(signUp.this, "Email is already in use", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(signUp.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            binding.progressBar.setVisibility(View.GONE);
                        }
                    }
                });

            }
        });


        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(signUp.this,login.class);
                startActivity(intent);
            }
        });

    }

    private void togglePasswordFeature() {


       binding.eyeToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    // Hide password
                    binding.etPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    binding.eyeToggle.setImageResource(R.drawable.ic_eye_closed);
                } else {
                    // Show password
                    binding.etPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    binding.eyeToggle.setImageResource(R.drawable.ic_eye_opened);
                }
                isPasswordVisible = !isPasswordVisible;
                binding.etPass.setSelection(binding.etPass.getText().length()); // Move cursor to the end
            }
        });

    }
}