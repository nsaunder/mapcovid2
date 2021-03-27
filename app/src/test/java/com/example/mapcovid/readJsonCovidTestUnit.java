package com.example.mapcovid;

import android.content.Context;
import android.content.res.AssetManager;

import com.example.mapcovid.MapsFragment;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.junit.Assert.*;

public class readJsonCovidTestUnit {
    MapsFragment mf = new MapsFragment();
    List<City> cities = new ArrayList<>();

    public List<City> getJson(){
        List<City> res = new ArrayList<>();
        
        try {

            res = mf.readItems("city_data.json");

        } catch (JSONException e) {
            System.out.println("Here");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Here2");
            e.printStackTrace();
        }
        return res;
    }

    @Test
    public void mapsReadJson() {

        cities = getJson();
        assertNotNull(cities);
        assertNotEquals(cities.size(), 0);
    }

    @Test
    public void mapsCorrectValues(){
        cities = getJson();
        assertEquals("Agoura Hills", cities.get(0).get_city_name());
        assertEquals("Whittier", cities.get(cities.size()-1).get_city_name());


        assertEquals(1, cities.get(0).get_new_deaths());
        assertEquals(1, cities.get(cities.size()-1).get_new_cases());

        assertEquals(14447, cities.get(0).get_total_cases());
        assertEquals(261, cities.get(cities.size()-1).get_total_deaths());
    }

}
