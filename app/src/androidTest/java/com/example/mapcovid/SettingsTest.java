package com.example.mapcovid;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class SettingsTest {

    private SettingsTest settingstest;

    @Rule
    public ActivityTestRule<HomeActivity> mHomeRule =
            new ActivityTestRule<>(HomeActivity.class);
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

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
    public void gps_settings() {
        onView(withId(R.id.navigation_settings)).perform(click());
        onView(withId(R.id.switch1)).perform(click());
        onView(withText("Would you like to edit your permissions for GPS?")).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).perform(click());
    }

    @Test
    public void notification_settings() {
        onView(withId(R.id.navigation_settings)).perform(click());
        onView(withId(R.id.switch2)).perform(click());
        onView(withText("Would you like to edit your permissions for notifications?")).check(matches(isDisplayed()));
        onView(withId(android.R.id.button1)).perform(click());
    }

}