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
public class HomeTest {

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
    public void map_to_testing() {
        onView(withId(R.id.launchBtn)).perform(click());
        onView(withId(R.id.navigation_testing)).perform(click());
        onView(withId(R.id.testing_map)).check(matches(isDisplayed()));
    }

    @Test
    public void map_to_path() {
        onView(withId(R.id.launchBtn)).perform(click());
        onView(withId(R.id.navigation_path)).perform(click());
        onView(withId(R.id.header1)).check(matches(isDisplayed()));
    }

    @Test
    public void map_to_news() {
        onView(withId(R.id.launchBtn)).perform(click());
        onView(withId(R.id.navigation_news)).perform(click());
        onView(withId(R.id.tweet_scroll_view)).check(matches(isDisplayed()));
    }

    @Test
    public void map_to_settings() {
        onView(withId(R.id.launchBtn)).perform(click());
        onView(withId(R.id.navigation_settings)).perform(click());
        onView(withId(R.id.settings_button)).check(matches(isDisplayed()));
    }

    @Test
    public void home_to_launch() {
        onView(withId(R.id.launchBtn)).perform(click());
        ViewInteraction imageButton = onView(Matchers.allOf(withContentDescription("MapCovid"), isDisplayed()));
        imageButton.perform(click());
        onView(withId(R.id.launchBtn)).check(matches(isDisplayed()));
    }

    @Test
    public void testing_to_launch() {
        onView(withId(R.id.launchBtn)).perform(click());
        onView(withId(R.id.navigation_testing)).perform(click());
        ViewInteraction imageButton = onView(Matchers.allOf(withContentDescription("MapCovid"), isDisplayed()));
        imageButton.perform(click());
        onView(withId(R.id.launchBtn)).check(matches(isDisplayed()));
    }

    @Test
    public void path_to_launch() {
        onView(withId(R.id.launchBtn)).perform(click());
        onView(withId(R.id.navigation_path)).perform(click());
        ViewInteraction imageButton = onView(Matchers.allOf(withContentDescription("MapCovid"), isDisplayed()));
        imageButton.perform(click());
        onView(withId(R.id.launchBtn)).check(matches(isDisplayed()));
    }

    @Test
    public void settings_to_launch() {
        onView(withId(R.id.launchBtn)).perform(click());
        onView(withId(R.id.navigation_settings)).perform(click());
        ViewInteraction imageButton = onView(Matchers.allOf(withContentDescription("MapCovid"), isDisplayed()));
        imageButton.perform(click());
        onView(withId(R.id.launchBtn)).check(matches(isDisplayed()));
    }

    @Test
    public void test_about_button() {
        onView(withId(R.id.launchBtn)).perform(click());
        onView(withId(R.id.navigation_settings)).perform(click());
        onView(withId(R.id.about_button)).perform(click());
        onView(withText("About")).inRoot(isDialog()).withFailureHandler(new FailureHandler() {
            @Override
            public void handle(Throwable error, Matcher<View> viewMatcher) {

            }
        }).check(matches(isDisplayed())).perform(click());
    }

    @Test
    public void test_pop_up_info() throws UiObjectNotFoundException {
        onView(withId(R.id.launchBtn)).perform(click());
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        UiObject markerbutton = device.findObject(new UiSelector()
                .descriptionContains("Google Map")
                .childSelector(new UiSelector().instance(1)));
        try {
            markerbutton.click();
            Rect rects = markerbutton.getBounds();

            //Rect(79, 641 - 150, 754)
            device.click(469, 837 );
        } catch (UiObjectNotFoundException e) {
            e.printStackTrace();
        }
        onView(withText("New Cases")).inRoot(isDialog()).withFailureHandler(new FailureHandler() {
            @Override
            public void handle(Throwable error, Matcher<View> viewMatcher) {

            }
        }).check(matches(isDisplayed())).perform(click());

    }


}