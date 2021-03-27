package com.example.mapcovid;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

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
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;


import org.junit.Rule;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LaunchTest {

    @Rule
    public ActivityTestRule<MainActivity> mLaunchRule =
            new ActivityTestRule<>(MainActivity.class);
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

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
    public void launch_test() {
        onView(withId(R.id.launchBtn)).perform(click());
        intended(hasComponent(HomeActivity.class.getName()));
    }
}

