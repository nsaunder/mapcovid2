package com.example.mapcovid;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

public class OnboardAdapter extends FragmentStateAdapter {
    private List<Integer> screens;

    public OnboardAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        setScreens();
    }

    private void setScreens() {
        screens = new ArrayList<Integer>();
        //add resource IDs for all onboarding screens sequentially 1,2,...
        screens.add(700081);
        screens.add(700083);
        screens.add(700092);
        screens.add(700095);
        screens.add(700098);
        screens.add(700101);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        //gets appropriate image to display
        int screenID = screens.get(position);
        return new ScreenSlidePageFragment(screenID);
    }

    @Override
    public int getItemCount() {
        return screens.size();
    }
}
