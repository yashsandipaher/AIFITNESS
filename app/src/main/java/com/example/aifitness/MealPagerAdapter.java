package com.example.aifitness;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MealPagerAdapter extends FragmentStateAdapter {

    public MealPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new BreakfastFragment(); // Create this Fragment
            case 1:
                return new LunchFragment(); // Create this Fragment
            case 2:
                return new SnacksFragment(); // Create this Fragment
            case 3:
                return new DinnerFragment(); // Create this Fragment
            default:
                return new BreakfastFragment(); // Default case
        }
    }

    @Override
    public int getItemCount() {
        return 4; // Number of tabs
    }
}
