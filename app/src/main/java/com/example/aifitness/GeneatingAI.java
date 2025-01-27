package com.example.aifitness;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.airbnb.lottie.LottieAnimationView;
import com.example.aifitness.databinding.ActivityGeneatingAiBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GeneatingAI extends AppCompatActivity {


    ActivityGeneatingAiBinding binding;
    FirebaseAuth auth;
    FirebaseFirestore firebaseFirestore;
    DocumentReference documentReference;

    String modelGeneratedOrNot;
    String age;
    double bmi;
    String diabetes;
    String email;
    String fitnessLevel;
    String gender;
    String goal;
    String height;
    String hypertension;
    String name;
    String weight;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGeneatingAiBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(getResources().getColor(R.color.night));


        firebaseFirestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();
        documentReference = firebaseFirestore.collection("users").document(userId);



        binding.generateAiModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the Lottie animation
                binding.lottie.setVisibility(View.VISIBLE);
                binding.lottie.playAnimation();

                fetchUserData();

            }
        });
    }


    private void fetchUserData() {
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                String userId = user.getUid();
                firebaseFirestore.collection("users").document(userId)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document != null && document.exists()) {

                                    age = document.getString("Age");
                                    bmi = document.getDouble("BMI");
                                    diabetes = document.getString("Diabetes");
                                    email = document.getString("Email");
                                    fitnessLevel = document.getString("Fitness Level");
                                    gender = document.getString("Gender");
                                    goal = document.getString("Goal");
                                    height = document.getString("Height");
                                    hypertension = document.getString("Hypertension");
                                    name = document.getString("Name");
                                    weight = document.getString("Weight");


                                    sentDataToFLASK();

                                } else {
                                    Log.d("TAG", "No such document");
                                }
                            } else {
                                Log.d("TAG", "get failed with ", task.getException());
                            }
                        });
            } else {
                Log.d("TAG", "No user is logged in");
            }
    }

    private void sentDataToFLASK() {

        String ageString = age; // Firebase age
        double bmiValue = bmi; // Firebase BMI
        String diabetesString = diabetes; // Firebase diabetes
        String emailValue = email; // Firebase email
        String fitnessLevelString = fitnessLevel; // Firebase fitness level
        String genderString = gender; // Firebase gender
        String goalString = goal; // Firebase goal
        String heightString = height; // Firebase height
        String hypertensionString = hypertension; // Firebase hypertension
        String nameValue = name; // Firebase name
        String weightString = weight; // Firebase weight


        // Convert age and height to integer
        int age = Integer.parseInt(ageString);
        int height = Integer.parseInt(heightString);
        int weight = Integer.parseInt(weightString);

        // Convert diabetes and hypertension to 0 or 1
        int diabetes = diabetesString.equalsIgnoreCase("Yes") ? 1 : 0;
        int hypertension = hypertensionString.equalsIgnoreCase("Yes") ? 1 : 0;

        // Convert gender to 0 or 1
        int sex = genderString.equalsIgnoreCase("Male") ? 1 : 0;

        // Convert fitness level to an integer based on your defined levels
        int fitnessLevel = getFitnessLevelInt(fitnessLevelString); // Implement this method based on your criteria

        // Convert fitness goal to 0 or 1 based on your defined goals
        int fitnessGoal = getFitnessGoalInt(goalString); // Implement this method based on your criteria

        // Prepare the input data map
        Map<String, Object> inputData = new HashMap<>();
        inputData.put("sex", sex);
        inputData.put("fitness_type", 0);
        inputData.put("age", age);
        inputData.put("bmi",bmiValue);
        inputData.put("level",fitnessLevel);
        inputData.put("weight", weight);
        inputData.put("height", height);
        inputData.put("hypertension", hypertension);
        inputData.put("diabetes", diabetes);
        inputData.put("fitness_goal", fitnessGoal);

        // Now send inputData to your Flask API
        sendToFlaskAPI(inputData); // Implement this method to handle the API call

    }

    // Implement this method based on your defined fitness levels
    private int getFitnessLevelInt(String fitnessLevel) {
        switch (fitnessLevel.toLowerCase()) {
            case "underweight":
                return 0; // Underweight
            case "normal weight":
                return 1; // Normal weight
            case "overweight":
                return 2; // Overweight
            case "obesity":
                return 3; // Obesity
            default:
                return -1; // Example for undefined level
        }
    }
    // Implement this method based on your defined fitness goals
    private int getFitnessGoalInt(String goal) {
        switch (goal.toLowerCase()) {
            case "weight loss":
                return 0;
            case "weight gain":
                return 1;
            default:
                return -1; // Example for undefined goal
        }
    }

    // Implement this method to send inputData to your Flask API
    private void sendToFlaskAPI(Map<String, Object> inputData) {

        // Create Retrofit instance
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://myfalskapp.onrender.com") // Change to your Flask server URL
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Create API interface
        ApiService apiService = retrofit.create(ApiService.class);
        Call<JsonObject> call = apiService.predict((HashMap<String, Object>) inputData);

        // Make the API call
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("yashaher", "onResponse: "+response.body().toString());

                    modelGeneratedOrNot = "true";
                    // Store response in the singleton
                    // Handle the API response
                    handleApiResponse(response.body());
                    stopLottieAndNavigate(); // Stop animation and navigate
                } else {
                    Log.d("yashaher", "onERROR: "+response.body());
//                    modelGeneratedOrNot = "false";
//                    retrySendingDataToFlask(inputData);
                     handleErrorResponse();
                    stopLottieAndNavigate();
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("yashaher", "FAILURE: ");
//                modelGeneratedOrNot = "false";
//                retrySendingDataToFlask(inputData); // Retry sending the data
                 handleErrorResponse();
                stopLottieAndNavigate();
            }
        });
    }
    private void stopLottieAndNavigate() {
        binding.lottie.cancelAnimation();
        binding.lottie.setVisibility(View.GONE);

        // Intent to switch to MainActivity
        Intent i = new Intent(GeneatingAI.this, MainActivity.class);
        startActivity(i);
        finish(); // Close the current activity
    }

//    private void retrySendingDataToFlask(Map<String, Object> inputData) {
//        // Optionally implement a delay before retrying or just call sendToFlaskAPI(inputData) again
//        sendToFlaskAPI(inputData); // Retry sending the data
//    }



    private void handleErrorResponse()
    {

        String breakfast = "Null";
        String lunch = "Null";
        String dinner = "Null";
        String snacks = "Null";
        String equipment = "Null";
        String exercises = "Null";
        String recommendation = "Null";

        // Log or display the extracted data as needed
        Log.d("yashaher", "Breakfast: " + breakfast);
        Log.d("yashaher", "Lunch: " + lunch);
        Log.d("yashaher", "Dinner: " + dinner);
        Log.d("yashaher", "Snacks: " + snacks);
        Log.d("yashaher", "Equipment: " + equipment);
        Log.d("yashaher", "Exercises: " + exercises);


        // Get the current user's ID
        String userId = auth.getCurrentUser().getUid();

        // Prepare data to be stored in Firestore
        Map<String, Object> aiResponseData = new HashMap<>();
        aiResponseData.put("breakfast", breakfast);
        aiResponseData.put("lunch", lunch);
        aiResponseData.put("dinner", dinner);
        aiResponseData.put("snacks", snacks);
        aiResponseData.put("equipment", equipment);
        aiResponseData.put("exercises", exercises);
        aiResponseData.put("recommendation", recommendation);


        // Use a fixed document ID for the AI response
        String documentId = "yashsandipaher";

        firebaseFirestore.collection("users").document(userId)
                .collection("userAIResponses") // Optional: Create a sub-collection for AI responses
                .document(documentId) // Specify the document ID
                .set(aiResponseData) // Use set() to overwrite the document
                .addOnSuccessListener(documentReference -> Log.d("yashaher", "AI Response updated successfully!"))
                .addOnFailureListener(e -> Log.e("yashaher", "Error updating AI Response", e));


    }
    private void handleApiResponse(JsonObject response) {
        // Extracting data from the JSON response
        String breakfast = response.get("breakfast").getAsString();
        String lunch = response.get("lunch").getAsString();
        String dinner = response.get("dinner").getAsString();
        String snacks = response.get("snacks").getAsString();
        String equipment = response.get("equipment").getAsString();
        String exercises = response.get("exercises").getAsString();
        String recommendation = response.get("recommendation").getAsString();

        // Log or display the extracted data as needed
        Log.d("yashaher", "Breakfast: " + breakfast);
        Log.d("yashaher", "Lunch: " + lunch);
        Log.d("yashaher", "Dinner: " + dinner);
        Log.d("yashaher", "Snacks: " + snacks);
        Log.d("yashaher", "Equipment: " + equipment);
        Log.d("yashaher", "Exercises: " + exercises);


        // Get the current user's ID
        String userId = auth.getCurrentUser().getUid();

        // Prepare data to be stored in Firestore
        Map<String, Object> aiResponseData = new HashMap<>();
        aiResponseData.put("breakfast", breakfast);
        aiResponseData.put("lunch", lunch);
        aiResponseData.put("dinner", dinner);
        aiResponseData.put("snacks", snacks);
        aiResponseData.put("equipment", equipment);
        aiResponseData.put("exercises", exercises);
        aiResponseData.put("recommendation", recommendation);


        // Use a fixed document ID for the AI response
        String documentId = "yashsandipaher";

        firebaseFirestore.collection("users").document(userId)
                .collection("userAIResponses") // Optional: Create a sub-collection for AI responses
                .document(documentId) // Specify the document ID
                .set(aiResponseData) // Use set() to overwrite the document
                .addOnSuccessListener(documentReference -> Log.d("yashaher", "AI Response updated successfully!"))
                .addOnFailureListener(e -> Log.e("yashaher", "Error updating AI Response", e));


    }

}