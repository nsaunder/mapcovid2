package com.example.mapcovid;

import android.Manifest;
import android.app.Instrumentation;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import com.github.redouane59.twitter.dto.tweet.Geo;
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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;

import java.util.ArrayList;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(JUnit4.class)
@PrepareForTest({FirebaseDatabase.class})
public class ServiceLocationChangeUnitTest {
    private LocationService locationService;
    private LocationService spy;
    private Constant constants;
    private DatabaseReference mockDB;

    private Double lat;
    private Double lon;

    @Before
    public void before() {
        mockDB = Mockito.mock(DatabaseReference.class);

        FirebaseDatabase mockFDB = Mockito.mock(FirebaseDatabase.class);
        Mockito.when(mockFDB.getReference()).thenReturn(mockDB);

        PowerMockito.mockStatic(FirebaseDatabase.class);
        Mockito.when(FirebaseDatabase.getInstance()).thenReturn(mockFDB);

        locationService = new LocationService();
        locationService.setConstants(null);
        spy = Mockito.spy(locationService);

        constants = locationService.getConstants();
    }

    @Test
    //tests city in LA County
    public void testOnLocationChange1() {
        try {
            //lat and long to pass into onLocationChange()
            Double currentLat = 34.14791;
            Double currentLon = -118.7657042;
            //mocks
            Location mockLocation = new Location("mock");
            Location spyLocation = Mockito.spy(mockLocation);
            Mockito.doAnswer(new Answer() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    setLat(invocation.getArgumentAt(0, Double.class));
                    return null;
                }
            }).when(spyLocation).setLatitude(currentLat);
            Mockito.doAnswer(new Answer() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    setLon(invocation.getArgumentAt(0, Double.class));
                    return null;
                }
            }).when(spyLocation).setLongitude(currentLon);
            Mockito.doReturn(currentLat).when(spyLocation).getLatitude();
            Mockito.doReturn(currentLon).when(spyLocation).getLongitude();
            Mockito.when(spy.setGeocoder()).thenReturn(null);

            //TEST//
            spyLocation.setLatitude(currentLat);
            spyLocation.setLongitude(currentLon);
            Mockito.when(spy.getCityByCoordinates(currentLat, currentLon)).thenReturn("Agoura Hills");
            spy.onLocationChanged(spyLocation);
            assertEquals("Agoura Hills", constants.getCurrentLocation());
            assertEquals(34.14791, constants.getCurrentLat(), 0.1);
            assertEquals(-118.7657042, constants.getCurrentLon(), 0.1);

        } catch(Exception e) {
            System.out.println("Error in testOnLocationChange!");
            e.printStackTrace();
        }
    }

    @Test
    //tests city not in LA County --> should still work to make app scalable
    public void testOnLocationChange2() {
        try {
            //lat and long to pass into onLocationChange()
            Double currentLat = 37.773972;
            Double currentLon = -122.431297;
            //mocks
            Location mockLocation = new Location("mock");
            Location spyLocation = Mockito.spy(mockLocation);
            Mockito.doAnswer(new Answer() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    setLat(invocation.getArgumentAt(0, Double.class));
                    return null;
                }
            }).when(spyLocation).setLatitude(currentLat);
            Mockito.doAnswer(new Answer() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    setLon(invocation.getArgumentAt(0, Double.class));
                    return null;
                }
            }).when(spyLocation).setLongitude(currentLon);
            Mockito.doReturn(currentLat).when(spyLocation).getLatitude();
            Mockito.doReturn(currentLon).when(spyLocation).getLongitude();
            Mockito.when(spy.setGeocoder()).thenReturn(null);

            //TEST//
            spyLocation.setLatitude(currentLat);
            spyLocation.setLongitude(currentLon);
            Mockito.when(spy.getCityByCoordinates(currentLat, currentLon)).thenReturn("San Francisco");
            spy.onLocationChanged(spyLocation);
            assertEquals("San Francisco", constants.getCurrentLocation());
            assertEquals(37.773972, constants.getCurrentLat(), 0.1);
            assertEquals(-122.431297, constants.getCurrentLon(), 0.1);

        } catch(Exception e) {
            System.out.println("Error in testOnLocationChange!");
            e.printStackTrace();
        }
    }

    @Test
    //tests null city --> should be same as San Francisco
    public void testOnLocationChange3() {
        try {
            spy.onLocationChanged(null);
            assertEquals("San Francisco", constants.getCurrentLocation());
            assertEquals(37.773972, constants.getCurrentLat(), 0.1);
            assertEquals(-122.431297, constants.getCurrentLon(), 0.1);

        } catch(Exception e) {
            System.out.println("Error in testOnLocationChange!");
            e.printStackTrace();
        }
    }

    public void setLat(Double d) {
        this.lat = d;
    }

    public void setLon(Double d) {
        this.lon = d;
    }

}
