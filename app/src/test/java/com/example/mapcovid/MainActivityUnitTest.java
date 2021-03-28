package com.example.mapcovid;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

import java.util.ArrayList;

@RunWith(JUnit4.class)
public class MainActivityUnitTest {
    private MainActivity mainActivity;

    @Before
    public void before() {
        DatabaseReference mockDB = Mockito.mock(DatabaseReference.class);
        FirebaseDatabase mockFDB = Mockito.mock(FirebaseDatabase.class);

        mainActivity = new MainActivity();
        MainActivity spy = Mockito.spy(mainActivity);

        Mockito.doReturn(mockFDB).when(spy).get_instance();
        Mockito.doReturn(mockDB).when(mockFDB).getReference();
    }

    @Test
    public void testGetCityByCoordinates() {
        try {
            //tests null coordinates
            assertEquals(null, mainActivity.getCityByCoordinates(null, null));
            //tests one of coordinates being null
            assertEquals(null, mainActivity.getCityByCoordinates(34.14791, null));
            assertEquals(null, mainActivity.getCityByCoordinates(null, 34.14791));
            //tests correctness of function
            assertEquals("Agoura Hills", mainActivity.getCityByCoordinates(34.14791, -118.7657042));
            assertEquals("Lakewood", mainActivity.getCityByCoordinates(33.84476, -118.08586));
            assertEquals("Whittier", mainActivity.getCityByCoordinates(33.9708782, -118.0308396));
        } catch(Exception e) { }
    }
}
