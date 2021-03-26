package com.example.mapcovid;

import com.example.mapcovid.TestingFragment;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class readJsonTestingTestUnit {
    TestingFragment testing = new TestingFragment();

    @Test
    public void mapsReadJson() {
        List<TestingFragment.TestingLocation> cities = new ArrayList<>();
        try {
            cities = testing.readItems("city_data.json");
        } catch (Exception e){

        }
        assertEquals(cities.get(0).getName(), "Edendale Library - Echo Park");
        assertEquals(cities.get(cities.size()-1).getName(), "Wesley Health Centers - Downey");

        /*assertEquals(cities.get(cities.get(0).getName(), 34.078850);
        assertEquals(cities.get(cities.get(0).getName(), -118.12892);
        assertEquals(cities.get(cities.size()-1).getPosition().latitude, 33.93729);
        assertEquals(cities.get(cities.size()-1).getPosition().longitude, -118.12892);

        assertEquals(cities.get(0).get_total_cases(), 14447);
        assertEquals(cities.get(cities.size()-1).get_total_deaths(), 261);*/
    }

}
