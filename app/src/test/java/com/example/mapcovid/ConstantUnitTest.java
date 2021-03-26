package com.example.mapcovid;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.content.Context;

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

import java.util.ArrayList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(JUnit4.class)
@PrepareForTest({FirebaseDatabase.class})
public class ConstantUnitTest {
    @Mock
    Context mockContext;

    private DatabaseReference mockDB;

    @Before
    public void before() {
        mockDB = Mockito.mock(DatabaseReference.class);

        FirebaseDatabase mockFDB = Mockito.mock(FirebaseDatabase.class);
        Mockito.when(mockFDB.getReference()).thenReturn(mockDB);

        PowerMockito.mockStatic(FirebaseDatabase.class);
        Mockito.when(FirebaseDatabase.getInstance()).thenReturn(mockFDB);


    }

    @Test
    public void testSetCities() {
        Constant constants = new Constant();
        //call on set_cities() and retrieve cities that we read in
        constants.set_cities(mockContext);
        ArrayList<City> cities = constants.getCities();

        //TESTS//
        //test to make sure cities not null
        assertNotNull(cities);
        //test to make sure cities doesn't contain a null city
        assertFalse(cities.contains(null));
        //test city not in LA County
        City notLA = new City("Boston", 34.14791, -118.7657042, 527.18, 4, 1, 14447, 285);
        assertFalse(cities.contains(notLA));
        //tests city names in wrong format
        City wrongCityNameFormat = new City("agoura_hills", 34.14791, -118.7657042, 527.18, 4, 1, 14447, 285);
        assertFalse(cities.contains(wrongCityNameFormat));
        //test that beginning, middle, and end cities read in properly
        City beg = new City("Agoura Hills", 34.14791, -118.7657042, 527.18, 4, 1, 14447, 285);
        City middle = new City("Lakewood", 33.84476, -118.08586, 7.96, 2, 2, 11864, 274);
        City end = new City("Whittier", 33.9708782, -118.0308396, 6.87, 1, 4, 11917, 261);
        assertTrue(cities.contains(beg));
        assertTrue(cities.contains(middle));
        assertTrue(cities.contains(end));
    }


}
