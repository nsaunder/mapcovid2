package com.example.mapcovid;

import com.example.mapcovid.MapsFragment;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class readJsonCovidTestUnit {
    MapsFragment mf = new MapsFragment();

    @Test
    public void mapsReadJson() {
        List<City> cities = new ArrayList<>();
        try {
            cities = mf.readItems("city_data.json");
        } catch (Exception e){

        }

        assertEquals(cities.get(0).get_city_name(), "Agoura Hills");
        assertEquals(cities.get(cities.size()-1).get_city_name(), "Whittier");

        assertEquals(cities.get(0).get_new_deaths(), 4);
        assertEquals(cities.get(cities.size()-1).get_new_cases(), 1);

        assertEquals(cities.get(0).get_total_cases(), 14447);
        assertEquals(cities.get(cities.size()-1).get_total_deaths(), 261);
    }

}
