package com.example.mapcovid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ScreenSlidePageFragment extends Fragment {
    private int imageID;
    private ImageView image;

    //need default constructor
    public ScreenSlidePageFragment() {

    }

    public ScreenSlidePageFragment(int id) {
        imageID = id;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  (ViewGroup) inflater.inflate(
                R.layout.fragment_screen_slide_page, container, false);

        image = view.findViewById(R.id.screen);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        image.setImageResource(imageID);
    }
}
