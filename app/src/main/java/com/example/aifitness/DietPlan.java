package com.example.aifitness;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ekn.gruzer.gaugelibrary.MultiGauge;
import com.example.aifitness.databinding.ActivityDietPlanBinding;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DietPlan extends AppCompatActivity {

    com.example.aifitness.databinding.ActivityDietPlanBinding binding;
    FirebaseAuth auth;
    FirebaseFirestore firebaseFirestore;
    String breakfast;
    String lunch;
    String dinner;
    String snacks;
    String recommendation;
    TextView requiredCalories;

    String modelGeneratedOrNot;
    String ageforRefresh ;
    double bmiforRefresh ;
    String diabetesforRefresh ;
    String emailforRefresh ;
    String fitnessLevelforRefresh ;
    String genderforRefresh ;
    String goalforRefresh ;
    String heightforRefresh ;
    String hypertensionforRefresh ;
    String nameforRefresh ;
    String weightforRefresh ;

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDietPlanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(getResources().getColor(R.color.rosewood));

        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        requiredCalories = findViewById(R.id.Calories);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading..."); // Set your loading message
        progressDialog.setCancelable(false); // Make it non-cancelable

        showPieChart();
        generateDailyNutritionForAPI();
        findRequiredCalories();



        binding.refreshData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                refreshTheData();

            }
        });
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        // Setting up the ViewPager with an adapter
        MealPagerAdapter adapter = new MealPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Attach the TabLayout to the ViewPager
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Brunch");
                            break;
                        case 1:
                            tab.setText("Lunch");
                            break;
                        case 2:
                            tab.setText("Snacks");
                            break;
                        case 3:
                            tab.setText("Dinner");
                            break;
                    }
                }).attach();

        binding.feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inflate the feedback dialog layout
                LayoutInflater inflater = LayoutInflater.from(v.getContext());
                View dialogView = inflater.inflate(R.layout.feedback_dialog, null);

                // Build the AlertDialog
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(v.getContext());
                dialogBuilder.setView(dialogView);
                AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.show();

                // References to the views in the dialog
                RadioGroup dietSatisfactionGroup = dialogView.findViewById(R.id.dietSatisfactionGroup);
                RadioGroup exerciseDifficultyGroup = dialogView.findViewById(R.id.exerciseDifficultyGroup);
                RadioGroup moodGroup = dialogView.findViewById(R.id.moodGroup);

                Button submitFeedback = dialogView.findViewById(R.id.submitFeedback);

                // Set the onClick listener for the submit button inside the dialog
                submitFeedback.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Collect feedback data
                        String dietFeedback = getSelectedFeedback(dietSatisfactionGroup);
                        String exerciseFeedback = getSelectedFeedback(exerciseDifficultyGroup);
                        String moodFeedback = getSelectedFeedback(moodGroup);

                        // Process feedback data (can be stored locally or sent to a server)
                        processFeedback(dietFeedback, exerciseFeedback, moodFeedback);

                        // Dismiss the dialog after submitting the feedback
                        alertDialog.dismiss();
                    }
                });
            }

            // Helper method to get selected feedback from RadioGroup
            private String getSelectedFeedback(RadioGroup group) {
                int selectedId = group.getCheckedRadioButtonId();
                RadioButton selectedRadioButton = group.findViewById(selectedId);
                return selectedRadioButton != null ? selectedRadioButton.getText().toString() : "";
            }

            // Example method to process feedback data
            private void processFeedback(String diet, String exercise, String mood) {
                // Log feedback (you can store or send it to your backend server here)
                Log.d("Feedback", "Diet: " + diet + ", Exercise: " + exercise + ", Mood: " + mood);

                int dietScore = 0;
                int exerciseScore = 0;
                int moodScore = 0;

                if(diet == "Satisfied")
                {
                    dietScore = 1;
                }

                if(diet == "Neutral")
                {
                    dietScore = 0;
                }

                if(diet == "Dissatisfied")
                {
                    dietScore = -1;
                }


                if(exercise == "Satisfied")
                {
                    exerciseScore = 1;
                }

                if(exercise == "Neutral")
                {
                    exerciseScore = 0;
                }

                if(exercise == "Dissatisfied")
                {
                    exerciseScore = -1;
                }

                if(mood == "Happy")
                {
                    dietScore = 1;
                }

                if(mood == "Neutral")
                {
                    dietScore = 0;
                }

                if(mood == "Stressed")
                {
                    dietScore = -1;
                }






            }

        });





//        DISPLAY DIET PLAN
        DISPLAY_DIET_PLAN();

    }

    private void refreshTheData() {

        progressDialog.show();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            firebaseFirestore.collection("users").document(userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {

                                ageforRefresh = document.getString("Age");
                                bmiforRefresh = document.getDouble("BMI");
                                diabetesforRefresh = document.getString("Diabetes");
                                emailforRefresh = document.getString("Email");
                                fitnessLevelforRefresh = document.getString("Fitness Level");
                                genderforRefresh = document.getString("Gender");
                                goalforRefresh = document.getString("Goal");
                                heightforRefresh = document.getString("Height");
                                hypertensionforRefresh = document.getString("Hypertension");
                                nameforRefresh = document.getString("Name");
                                weightforRefresh = document.getString("Weight");

                                sentDataToFLASK();

                            } else {
                                Log.d("TAG", "No such document");
                                progressDialog.dismiss();
                            }
                        } else {
                            Log.d("TAG", "get failed with ", task.getException());
                            progressDialog.dismiss();
                        }
                    });
        } else {
            Log.d("TAG", "No user is logged in");
            progressDialog.dismiss();
        }
    }
    private void sentDataToFLASK() {

        String ageString = ageforRefresh; // Firebase age
        double bmiValue = bmiforRefresh; // Firebase BMI
        String diabetesString = diabetesforRefresh; // Firebase diabetes
        String emailValue = emailforRefresh; // Firebase email
        String fitnessLevelString = fitnessLevelforRefresh; // Firebase fitness level
        String genderString = genderforRefresh; // Firebase gender
        String goalString = goalforRefresh; // Firebase goal
        String heightString = heightforRefresh; // Firebase height
        String hypertensionString = hypertensionforRefresh; // Firebase hypertension
        String nameValue = nameforRefresh; // Firebase name
        String weightString = weightforRefresh; // Firebase weight


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
        inputData.put("age", age);
        inputData.put("height", height);
        inputData.put("weight", weight);
        inputData.put("hypertension", hypertension);
        inputData.put("diabetes", diabetes);
        inputData.put("bmi", bmiValue);
        inputData.put("level", fitnessLevel);
        inputData.put("fitness_goal", fitnessGoal);
        inputData.put("fitness_type", 0); // Modify this based on your requirements

        // Now send inputData to your Flask API
        sendToFlaskAPI(inputData); // Implement this method to handle the API call

    }
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

                    handleApiResponse(response.body());
                    progressDialog.dismiss();
                } else {
                    Log.d("yashaher", "onERROR: "+response.body());
                    progressDialog.dismiss();
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.d("yashaher", "FAILURE: ");
                progressDialog.dismiss();
            }
        });
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
        Log.d("yashaher", "Recommendation: " + recommendation);


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
                .update(aiResponseData) // Use set() to overwrite the document
                .addOnSuccessListener(documentReference -> Log.d("yashaher", "AI Response updated successfully!"))
                .addOnFailureListener(e -> Log.e("yashaher", "Error updating AI Response", e));


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


    private void findRequiredCalories() {

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            firebaseFirestore.collection("users").document(userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {

                                String age = document.getString("Age");
                                Double bmi = document.getDouble("BMI");
                                String diabetes = document.getString("Diabetes");
                                String fitnessLevel = document.getString("Fitness Level");
                                String gender = document.getString("Gender");
                                String goal = document.getString("Goal");
                                String height = document.getString("Height");
                                String hypertension = document.getString("Hypertension");
                                String weight = document.getString("Weight");



                                // Parse the age, height, and weight
                                int ageInt = Integer.parseInt(age);
                                double heightDouble = Double.parseDouble(height); // height in cm
                                double weightDouble = Double.parseDouble(weight); // weight in kg

                                // Calculate BMR
                                double bmr;
                                if (gender.equalsIgnoreCase("male")) {
                                    bmr = 10 * weightDouble + 6.25 * heightDouble - 5 * ageInt + 5;
                                } else { // Assume female if not male
                                    bmr = 10 * weightDouble + 6.25 * heightDouble - 5 * ageInt - 161;
                                }

                                double caloricNeeds;
                                switch (fitnessLevel) {
                                    case "Underweight":
                                        caloricNeeds = bmr * 1.2; // Sedentary
                                        break;
                                    case "Normal weight":
                                        caloricNeeds = bmr * 1.55; // Moderately active
                                        break;
                                    case "Overweight":
                                        caloricNeeds = bmr * 1.75; // Very active
                                        break;
                                    case "Obesity":
                                        caloricNeeds = bmr * 1.2; // Sedentary
                                        break;
                                    default:
                                        caloricNeeds = bmr; // Default case, no adjustment
                                        break;
                                }

                                String caloricNeedsStr = String.valueOf(caloricNeeds);
                                requiredCalories.setText(caloricNeedsStr+" kcal/day");

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

    private void generateDailyNutritionForAPI() {

        binding.searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String vegetable = binding.vegetableInput.getText().toString().trim();
                if (!vegetable.isEmpty()) {
                    fetchNutritionData(vegetable);
                } else {
                    Toast.makeText(DietPlan.this, "Please enter a vegetable", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void fetchNutritionData(String query) {
        CalorieNinjasService service = RetrofitClient.getService();
        Call<NutritionResponse> call = service.getNutrition(query);

        call.enqueue(new Callback<NutritionResponse>() {
            @Override
            public void onResponse(Call<NutritionResponse> call, Response<NutritionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    NutritionResponse nutritionResponse = response.body();
                    if (!nutritionResponse.items.isEmpty()) {
                        NutritionItem item = nutritionResponse.items.get(0); // Fetch the first item

                        // Open dialog to show nutrition info and pie chart
                        showNutritionDialog(item);
                    } else {
                        Log.d("yash", "No data found for the given vegetable: ");
                    }
                } else {
                    Log.d("yash", "error for fetching data: ");
                }
            }

            @Override
            public void onFailure(Call<NutritionResponse> call, Throwable t) {
                Log.d("yash", "Failed to fetch data.");
            }
        });
    }

    private void showNutritionDialog(NutritionItem item) {
        // Inflate the dialog layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.nutrition_info_dialog, null);

        // Create and configure the AlertDialog
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(dialogView);
        AlertDialog alertDialog = dialogBuilder.create();

        // References to the views in the dialog
        PieChart pieChart = dialogView.findViewById(R.id.nutrition_pie_chart);
        TextView caloriesValue = dialogView.findViewById(R.id.calories_value);
        TextView proteinValue = dialogView.findViewById(R.id.protein_value);
        TextView carbsValue = dialogView.findViewById(R.id.carbs_value);
        TextView fatValue = dialogView.findViewById(R.id.fat_value);
        TextView vegetableName = dialogView.findViewById(R.id.vegetable_name);

        // Set values to the TextViews
        caloriesValue.setText(String.valueOf(item.calories));
        proteinValue.setText(String.valueOf(item.protein));
        carbsValue.setText(String.valueOf(item.carbs));
        fatValue.setText(String.valueOf(item.fat));
        vegetableName.setText(String.valueOf(item.name));

        // Update PieChart with the fetched nutrition data
        updatePieChart(pieChart, item.calories, item.protein, item.carbs, item.fat);

        // Show the dialog
        alertDialog.show();

        // Close button functionality
        Button closeButton = dialogView.findViewById(R.id.close_button);
        closeButton.setOnClickListener(view -> alertDialog.dismiss());
    }
    private void updatePieChart(PieChart pieChart, float calories, float protein, float carbs, float fat) {
        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(protein, "Protein"));
        pieEntries.add(new PieEntry(carbs, "Carbs"));
        pieEntries.add(new PieEntry(fat, "Fat"));

        // Initializing colors for the entries
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#FF5722")); // Protein
        colors.add(Color.parseColor("#4CAF50")); // Carbs
        colors.add(Color.parseColor("#FFC107")); // Fat

        PieDataSet pieDataSet = new PieDataSet(pieEntries, "Nutrients");
        pieDataSet.setValueTextSize(12f);
        pieDataSet.setColors(colors);
        PieData pieData = new PieData(pieDataSet);
        pieData.setDrawValues(true);

        // Set the data to the PieChartView and refresh it
        pieChart.setData(pieData);
        pieChart.invalidate(); // Refresh the chart
    }

    private void DISPLAY_DIET_PLAN() {

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
                                    // Get data from the document

                                    breakfast = document.getString("breakfast");
                                    lunch = document.getString("lunch");
                                    dinner = document.getString("dinner");
                                    snacks = document.getString("snacks");
                                    recommendation = document.getString("recommendation");


//                                    binding.breakfast.setText(breakfast);

                                    // Split the data into arrays
//                                    String[] breakfastItems = breakfast.split("/");
//                                    String[] lunchItems = lunch.split("/");
//                                    String[] dinnerItems = dinner.split("/");
//                                    String[] snackItems = snacks.split("/");
//                                    String[] recommendations = recommendation.split("\\.");

                                    // Check for null before splitting
                                    String[] breakfastItems = breakfast != null ? breakfast.split("/") : new String[0];
                                    String[] lunchItems = lunch != null ? lunch.split("/") : new String[0];
                                    String[] dinnerItems = dinner != null ? dinner.split("/") : new String[0];
                                    String[] snackItems = snacks != null ? snacks.split("/") : new String[0];
                                    String[] recommendations = recommendation != null ? recommendation.split("\\.") : new String[0];

                                    // Display the data (Example: using TextViews or RecyclerViews)
                                    displayMeals(breakfastItems, lunchItems, dinnerItems, snackItems, recommendations);
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
    private void displayMeals(String[] breakfastItems, String[] lunchItems, String[] dinnerItems, String[] snackItems, String[] recommendations) {
        // Example: Display in TextViews (or implement RecyclerView)

        // For breakfast
        StringBuilder breakfastDisplay = new StringBuilder("Breakfast:\n");
        for (String item : breakfastItems) {
            breakfastDisplay.append("- ").append(item.trim()).append("\n");
        }
//        binding.breakfast.setText(breakfastDisplay.toString());

    }

    private void showPieChart(){

        ArrayList<PieEntry> pieEntries = new ArrayList<>();
        String label = "type";

        //initializing data
        Map<String, Integer> typeAmountMap = new HashMap<>();
        typeAmountMap.put("Protein",200);
        typeAmountMap.put("Carbs",230);
        typeAmountMap.put("Fats",100);

        //initializing colors for the entries
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#304567"));
        colors.add(Color.parseColor("#309967"));
        colors.add(Color.parseColor("#476567"));

        //input data and fit data into pie chart entry
        for(String type: typeAmountMap.keySet()){
            pieEntries.add(new PieEntry(typeAmountMap.get(type).floatValue(), type));
        }

        //collecting the entries with label name
        PieDataSet pieDataSet = new PieDataSet(pieEntries,label);
        //setting text size of the value
        pieDataSet.setValueTextSize(12f);
        //providing color list for coloring different entries
        pieDataSet.setColors(colors);
        //grouping the data set from entry to chart
        PieData pieData = new PieData(pieDataSet);
        //showing the value of the entries, default true if not set
        pieData.setDrawValues(true);

//        binding.pieChartView.setData(pieData);
//        binding.pieChartView.invalidate();
    }
}