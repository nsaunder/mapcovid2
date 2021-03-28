package com.example.mapcovid;

import android.Manifest;
import android.app.Instrumentation;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

import java.util.ArrayList;

@RunWith(RobolectricTestRunner.class)
public class MainActivityUnitTest {
    Context mockContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

    private MainActivity mainActivity;
    private MainActivity spy;
    //private FusedLocationProviderClient locationClient;
    private Constant constants;

    @Before
    public void before() {
        DatabaseReference mockDB = Mockito.mock(DatabaseReference.class);
        FirebaseDatabase mockFDB = Mockito.mock(FirebaseDatabase.class);

        mainActivity = new MainActivity();
        spy = Mockito.spy(mainActivity);

        Mockito.doReturn(mockFDB).when(spy).get_instance();
        Mockito.doReturn(mockDB).when(mockFDB).getReference();

        //locationClient = new FusedLocationProviderClient();
        constants = spy.getConstants();
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

//    @Test
//    public void testGetLastLocation() {
//        Mockito.doReturn(this.locationClient).when(spy).getLocationClient();
//
//        Location mockLocation = new Location(LocationManager.GPS_PROVIDER);
//        mockLocation.setLatitude(33.98719);
//        mockLocation.setLongitude(-118.52719);
//        mockLocation.setAccuracy(3.0f);
//
//        locationClient.setMockLocation(mockLocation);
//        spy.getLastLocation();
//        assertEquals("Santa Monica", constants.getLastLocation());
//
//    }
}
