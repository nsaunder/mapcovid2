package com.example.mapcovid;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.content.Context;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(JUnit4.class)
@PrepareForTest({FirebaseDatabase.class})
public class ConstantUnitTest {
    @Mock
    Context mockContext;

    private DatabaseReference mockDB;
    private Constant constants;
    //keeps track of how many times currentLocationChange Listener triggered
    private int locationListener;

    @Before
    public void before() {
        mockDB = Mockito.mock(DatabaseReference.class);

        FirebaseDatabase mockFDB = Mockito.mock(FirebaseDatabase.class);
        Mockito.when(mockFDB.getReference()).thenReturn(mockDB);

        PowerMockito.mockStatic(FirebaseDatabase.class);
        Mockito.when(FirebaseDatabase.getInstance()).thenReturn(mockFDB);

        //initialize constants object
        constants = new Constant();
        //call on set_cities() and retrieve cities that we read in
        constants.set_cities(null);
        //initialize locationListener
        locationListener = 0;
    }

    @Test
    public void testSetCities() {
        //get cities data structure and data it contains
        ArrayList<City> cities = constants.getCities();

        //TESTS//
        //test to make sure cities not null
        assertNotNull(cities);
        //test to make sure cities doesn't contain a null city
        assertFalse(cities.contains(null));
        //test city not in LA County
        City notLA = new City("Boston", 34.14791, -118.7657042,    527.18, 4, 1, 14447, 285);
        assertFalse(cities.contains(notLA));
        //tests city names in wrong format
        City wrongCityNameFormat = new City("agoura_hills", 34.14791, -118.7657042, 527.18, 4, 1, 14447, 285);
        assertFalse(cities.contains(wrongCityNameFormat));
        //test that beginning, middle, and end cities read in properly
        assertEquals("Agoura Hills", cities.get(0).get_city_name());
        assertEquals("Whittier", cities.get(cities.size()-1).get_city_name());
        assertEquals(1, cities.get(0).get_new_deaths());
        assertEquals(1, cities.get(cities.size()-1).get_new_cases());
        assertEquals(14447, cities.get(0).get_total_cases());
        assertEquals(261, cities.get(cities.size()-1).get_total_deaths());
    }

    @Test
    public void testGetCity() {
        //test for when you try to retrieve null city
        assertNull(constants.get_city(null));
        //test for cities outside of CA
        assertNull(constants.get_city("Boston"));
        assertNull(constants.get_city("Houston"));
        //test for cities in CA but outside of LA County
        assertNull(constants.get_city("San Francisco"));
        assertNull(constants.get_city("Temecula"));
        //test for arguments in wrong format
        assertNull(constants.get_city("lOs ANgElEs"));
        assertNull(constants.get_city("redondo_beach"));
        assertNull(constants.get_city("agoura hills"));
        assertNull(constants.get_city(""));
        assertNull(constants.get_city("/@$&"));
        //test for correctness
        assertNotNull(constants.get_city("Torrance"));
        City torrance = constants.get_city("Torrance");
        assertEquals(12.85, torrance.get_radius(), 0.1);
        assertNotNull(constants.get_city("Los Angeles"));
        City la = constants.get_city("Los Angeles");
        assertEquals(4, la.get_new_cases());
    }

    @Test
    public void testCurrentLocationChangeListener() {
        //initialize currentLocationChange listener
        constants.addCurrentLocationChangeListener(new currentLocationChangedListener() {
            @Override
            public void onCurrentLocationChange() {
                currentLocationTriggered();
            }
        });
        //test to make sure listener hasn't been triggered
        assertEquals(0, locationListener);
        constants.get_city("Torrance");
        assertEquals(0, locationListener);
        //test for correctness
        constants.setNewLocation(true);
        assertEquals(1, locationListener);
        assertFalse(locationListener != 1);
        constants.setNewLocation(false);
        assertEquals(2, locationListener);
        assertFalse(locationListener != 2);
        for(int i=0; i < 7; i++) {
            constants.setNewLocation(true);
        }
        assertEquals(9, locationListener);
        assertFalse(locationListener != 9);
    }

    public void currentLocationTriggered() {
        locationListener += 1;
    }





}
