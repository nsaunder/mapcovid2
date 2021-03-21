package com.example.mapcovid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import android.content.Context;

public class Constant {
    private static ArrayList<City> cities;
    private String currentLocation;
    private String lastLocation;

    public void set_cities(Context context) {
        try {
            InputStream is = context.getAssets().open("city_data.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            Gson gson = new Gson();
            Type cityList = new TypeToken<ArrayList<City>>(){}.getType();
            this.cities = gson.fromJson(reader,cityList);
            reader.close();
        } catch(Exception e) {
            System.err.println(e);
        }
    }

    public void setCurrentLocation(String location) {
        currentLocation = location;
    }

    public void setLastLocation(String location) {
        lastLocation = location;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public String getLastLocation() {
        return lastLocation;
    }

    public City get_city(String city) {
        for(City c: cities) {
            if(c.get_city_name().compareTo(city) == 0) {
                return c;
            }
        }
        return null;
    }

}
