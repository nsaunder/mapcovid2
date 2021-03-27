package com.example.mapcovid;

import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;


import org.junit.Rule;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class HomeTest {

    @Rule
    public ActivityTestRule<MainActivity> mHomeRule =
            new ActivityTestRule<>(MainActivity.class);

    @Before
    public void initialize() {
        DatabaseReference mockDB = Mockito.mock(DatabaseReference.class);
        FirebaseDatabase mockFDB = Mockito.mock(FirebaseDatabase.class);

        Constant constants = new Constant();
        Constant spy = Mockito.spy(constants);
        Mockito.when(mockFDB.getReference()).thenReturn(mockDB);
        Mockito.when(spy.get_instance()).thenReturn(mockFDB);
    }

    @After
    public void after() {

    }

    @Test
    public void map_to_testing() {
        onView(withId(R.id.launchBtn)).perform(click());
        onView(withId(R.id.navigation_testing)).perform(click());
        onView(withId(R.id.testing_map)).check(matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void map_to_path() {
        onView(withId(R.id.launchBtn)).perform(click());
        onView(withId(R.id.navigation_path)).perform(click());
        onView(withId(R.id.header1)).check(matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void map_to_news() {
        onView(withId(R.id.launchBtn)).perform(click());
        onView(withId(R.id.navigation_news)).perform(click());
        onView(withId(R.id.tweet_scroll_view)).check(matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void map_to_settings() {
        onView(withId(R.id.launchBtn)).perform(click());
        onView(withId(R.id.navigation_settings)).perform(click());
        onView(withId(R.id.settings_button)).check(matches(ViewMatchers.isDisplayed()));
    }


}