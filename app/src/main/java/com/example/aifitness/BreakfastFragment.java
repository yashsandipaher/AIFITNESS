package com.example.aifitness;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class BreakfastFragment extends Fragment {

    FirebaseAuth auth;
    FirebaseFirestore firebaseFirestore;
    private GridLayout breakfastGridLayout;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_breakfast, container, false);
        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();


        // Initialize the GridLayout from the layout
        breakfastGridLayout = view.findViewById(R.id.breakfastGridLayout);
        // Fetch and display breakfast data
        fetchBreakfastData();

        return view;
    }


    private void fetchBreakfastData()
    {

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

                                    String breakfast = document.getString("breakfast");

//                                    // Split the data into arrays
//                                    String[] breakfastItems = breakfast.split("/");
//                                    // Dynamically add TextViews for each breakfast item
//                                    addBreakfastItemsToGridLayout(breakfastItems);
                                    if (breakfast != null) {
                                        // Split the data into arrays
                                        String[] breakfastItems = breakfast.split("/");

                                        // Dynamically add TextViews for each breakfast item
                                        addBreakfastItemsToGridLayout(breakfastItems);
                                    } else {
                                        Log.d("yashaher", "No breakfast data found");
                                    }

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

    // Method to dynamically add TextViews to the GridLayout
    // Method to dynamically add TextViews to the GridLayout
    private void addBreakfastItemsToGridLayout(String[] breakfastItems) {
        for (String item : breakfastItems) {
            TextView textView = new TextView(getContext());
            textView.setText(item.trim());
            textView.setPadding(16, 8, 16, 8); // Padding for each item
            textView.setBackgroundResource(R.drawable.breakfast_item_background); // Custom background
            textView.setTextSize(14); // Set text size
            textView.setMaxEms(10); // Allow wider text
            textView.setGravity(Gravity.CENTER_VERTICAL);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            // Set gravity inside the TextView to fill horizontally
            textView.setGravity(View.TEXT_ALIGNMENT_CENTER);

            // Create layout params for GridLayout items
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();

            // Set width to 0dp to make sure each column takes equal space
            params.width = 0;
            params.height = 180;

            // Specify that this item should take up 1 column with equal width
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f); // Use 1f for equal space
            params.setMargins(8, 8, 8, 8); // Set margins
            // Apply the layout parameters to the TextView
            textView.setLayoutParams(params);

            // Add the TextView to the GridLayout
            breakfastGridLayout.addView(textView);
        }
    }


}