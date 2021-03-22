package com.example.mapcovid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class OnboardingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
    }

    public void finishOnboarding(android.view.View view) {
        //get shared preferences
        SharedPreferences preferences = getSharedPreferences("my_preferences", MODE_PRIVATE);
        //set onboarding_complete to true
        preferences.edit().putBoolean("onboarding_complete", true).apply();
        //launch main activity
        Intent main = new Intent(this, MainActivity.class);
        startActivity(main);

        finish();
    }
}