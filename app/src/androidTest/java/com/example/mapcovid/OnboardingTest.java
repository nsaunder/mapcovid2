package com.example.mapcovid;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;


import org.junit.Rule;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class OnboardingTest {

    @Rule
    public ActivityTestRule<OnboardingActivity> mActivityRule =
            new ActivityTestRule<>(OnboardingActivity.class);

    @Before
    public void initialize() {
        Intents.init();
    }

    @After
    public void after() {
        Intents.release();
    }

    @Test
    public void onboarding_test() {
        onView(withId(R.id.finish_button)).perform(scrollTo()).perform(click());
        intended(hasComponent(MainActivity.class.getName()));
    }

}