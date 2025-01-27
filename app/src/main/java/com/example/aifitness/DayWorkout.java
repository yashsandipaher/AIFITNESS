package com.example.aifitness;

public class DayWorkout {
    private String day;
    private String workoutDetails;

    public DayWorkout(String day, String workoutDetails) {
        this.day = day;
        this.workoutDetails = workoutDetails;
    }

    public String getDay() {
        return day;
    }

    public String getWorkoutDetails() {
        return workoutDetails;
    }
}
