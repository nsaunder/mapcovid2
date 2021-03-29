package com.example.mapcovid;

import android.graphics.Rect;
import android.view.Display;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.FailureHandler;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.hamcrest.CustomMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;


import org.junit.Rule;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class PathTest {

    @Rule
    public ActivityTestRule<MainActivity> mHomeRule =
            new ActivityTestRule<>(MainActivity.class);

    @Before
    public void initialize() {
        Intents.init();
        DatabaseReference mockDB = Mockito.mock(DatabaseReference.class);
        FirebaseDatabase mockFDB = Mockito.mock(FirebaseDatabase.class);

        Constant constants = new Constant();
        Constant spy = Mockito.spy(constants);
        Mockito.when(mockFDB.getReference()).thenReturn(mockDB);
        Mockito.when(spy.get_instance()).thenReturn(mockFDB);
    }

    @After
    public void after() {
        Intents.release();
    }

    @Test
    public void add_path() {
        onView(withId(R.id.launchBtn)).perform(click());
        onView(withId(R.id.navigation_path)).perform(click());
        onView(withId(R.id.date_button)).perform(click());
        onView(withId(R.id.numLocations)).check(matches(withText("1")));
    }
}