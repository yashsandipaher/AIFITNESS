package com.example.aifitness;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aifitness.databinding.ActivityDietPlanBinding;
import com.example.aifitness.databinding.ActivityExercisePlanBinding;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kotlin.sequences.USequencesKt;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExercisePlan extends AppCompatActivity {

    ActivityExercisePlanBinding binding;
    FirebaseAuth auth;
    FirebaseFirestore firebaseFirestore;
    String UserGoal,exercises;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExercisePlanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(getResources().getColor(R.color.rosewood));

        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();


        DISPLAY_WORKOUT_PLAN();
        fetchUserData();
        fetchExercisePlanFromFirestore();

        binding.refreshData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String prompt = "i want workout plan for " + UserGoal + " and your exercise type includes: " + exercises +" give me answer in monday to sunday daywise";

//                String prompt = "Generate a workout plan for " + UserGoal +
//                        " with the following types of exercises: " + exercises +
//                        ". Please provide a detailed schedule for each day of the week (Monday to Sunday)," +
//                        "including both morning and evening workouts. " +
//                        "every day is seperated by *****" +
//                        "does not add *for any title";


                String prompt = "Generate a workout plan for " + UserGoal +
                        " with the following types of exercises: " + exercises +
                        ". Please provide a detailed schedule for each day of the week (Monday to Sunday)," +
                        "including both morning and evening workouts. " +
                        "every day is seperated by *****" + "result direct started from monday";

                geminiData(prompt);
            }
        });
    }



    private void geminiData(String prompt) {

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false); // Prevent dismissing by tapping outside
        progressDialog.show();

        // Specify a Gemini model appropriate for your use case
        GenerativeModel gm =
                new GenerativeModel(
                        /* modelName */ "gemini-1.5-flash",
                        // Access your API key as a Build Configuration variable (see "Set up your API key"
                        // above)
                        /* apiKey */ "AIzaSyDHAT9xHeF9bfVrRuwMvlk-Ak5-gVu_PPU");
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        Content content =
                new Content.Builder().addText(prompt).build();

// For illustrative purposes only. You should use an executor that fits your needs.
        Executor executor = Executors.newSingleThreadExecutor();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(
                response,
                new FutureCallback<GenerateContentResponse>() {
                    @Override
                    public void onSuccess(GenerateContentResponse result) {
                        String resultText = result.getText();
                        Log.d("yashaher", "onSuccess: "+resultText);
                        saveExercisePlanToFirestore(resultText);
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        t.printStackTrace();
                        progressDialog.dismiss();
                    }
                },
                executor);
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
                                UserGoal = document.getString("Goal");
                                Log.d("yashaher", UserGoal);
                            } else {
                                Log.d("yashaher", "No such document");
                            }
                        } else {
                            Log.d("yashaher", "get failed with ", task.getException());
                        }
                    });
        } else {
            Log.d("yashher", "No user is logged in");
        }
    }

    private void DISPLAY_WORKOUT_PLAN() {

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            // Fetch the AI responses from Firestore
            firebaseFirestore.collection("users")
                    .document(userId)
                    .collection("userAIResponses") // Collection where AI responses are stored
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                if (document.exists()) {
                                    exercises = document.getString("exercises");
                                    Log.d("yashaher", exercises);

                                } else {
                                    Log.d("yashaher", "No AI response found for user");
                                }
                            }
                        } else {
                            Log.d("yashaher", "Fetch failed with ", task.getException());
                        }
                    });
        } else {
            Log.d("yashaher", "No user is logged in");
        }
    }


    private void saveExercisePlanToFirestore(String exercises) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            // Create a map to store the exercises
            Map<String, Object> exerciseData = new HashMap<>();
            exerciseData.put("exercises", exercises);

            // Save the generated exercises in Firestore
            firebaseFirestore.collection("users")
                    .document(userId)
                    .collection("userAIResponses")
                    .document("exercisePlan") // You can use a unique ID or "exercisePlan" as the document name
                    .set(exerciseData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("yashaher", "Exercise plan successfully written!");
                        Toast.makeText(this, "Exercise plan saved!", Toast.LENGTH_SHORT).show();
                        fetchExercisePlanFromFirestore();
                    })
                    .addOnFailureListener(e -> {
                        Log.w("yashaher", "Error writing document", e);
                        Toast.makeText(this, "Failed to save exercise plan.", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.d("yashaher", "No user is logged in");
        }
    }
    private void fetchExercisePlanFromFirestore() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            firebaseFirestore.collection("users")
                    .document(userId)
                    .collection("userAIResponses")
                    .document("exercisePlan") // Document ID
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String exercises = document.getString("exercises");
                                Log.d("yashaher", "Fetched exercises: " + exercises);
//                                binding.result.setText(exercises); // Display in TextView
                                // Display the parsed workout plan
                                displayExercisesInBoxes(exercises);
                            } else {
                                Log.d("yashaher", "No exercise plan found");
//                                binding.result.setText("No exercise plan available."); // Display a message if no data is found
                            }
                        } else {
                            Log.d("yashaher", "Fetch failed with ", task.getException());
                        }
                    });
        } else {
            Log.d("yashaher", "No user is logged in");
        }
    }


    private void displayExercisesInBoxes(String exercises) {
        // Split the exercises string by the delimiter "*****"
        String[] days = exercises.split("\\*\\*\\*\\*\\*"); // Split by "*****"
        LinearLayout exerciseContainer = findViewById(R.id.exerciseContainer);
        exerciseContainer.removeAllViews(); // Clear previous views

        for (String day : days) {
            // Clean up the day string to format it properly
            day = day.trim();

            // Skip empty strings
            if (day.isEmpty()) continue;

            // Split the day string into title and content
            String[] lines = day.split("\n", 2); // Split into title and content
            String title = lines[0].replaceAll("\\*+", "").trim(); // Remove leading asterisks and trim
            String content = lines.length > 1 ? lines[1].trim() : ""; // Get the content if available

            // Create a LinearLayout for each day's exercises
            LinearLayout dayLayout = new LinearLayout(this);
            dayLayout.setOrientation(LinearLayout.VERTICAL);

            // Set padding for the box
            dayLayout.setPadding(20, 20, 20, 20); // Set padding for the box (left, top, right, bottom)

            // Set background resource for the layout
            dayLayout.setBackgroundResource(R.drawable.workout_bg); // Create a drawable for the background

            // Create LayoutParams with margins for the day's layout
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 20, 0, 0); // Set top margin for spacing
            dayLayout.setLayoutParams(params); // Apply LayoutParams with margins

            // Create a TextView for the day title
            TextView titleView = new TextView(this);
            titleView.setText(title);
            titleView.setTextSize(20); // Increase font size for the title
            titleView.setTypeface(titleView.getTypeface(), Typeface.BOLD); // Make title bold
            titleView.setTextColor(getResources().getColor(R.color.black)); // Adjust text color as needed

            // Create LayoutParams for the title with margins
            LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            titleParams.setMargins(0, 10, 0, 10); // Set margins for the title
            titleView.setLayoutParams(titleParams); // Apply LayoutParams to the title

            dayLayout.addView(titleView); // Add the title to the layout

            // Create a TextView for the day's content
            TextView contentView = new TextView(this);
            contentView.setText(content);
            contentView.setTextSize(16); // Adjust text size for the content
            contentView.setTextColor(getResources().getColor(R.color.black)); // Adjust text color as needed

            // Create LayoutParams for the content with margins
            LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            contentParams.setMargins(0, 4, 0, 8); // Set margins for the content
            contentView.setLayoutParams(contentParams); // Apply LayoutParams to the content

            dayLayout.addView(contentView); // Add the content to the layout

            exerciseContainer.addView(dayLayout); // Add the day's layout to the container
        }
    }




}