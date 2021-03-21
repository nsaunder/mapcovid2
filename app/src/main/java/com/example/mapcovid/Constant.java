package com.example.mapcovid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import android.content.Context;

//interface to implement listener for when current location changes
interface currentLocationChangedListener {
    public void onCurrentLocationChange();
}

public class Constant {
    private static String appId;
    private static ArrayList<City> cities;
    private static String currentLocation;
    private static boolean newLocation;
    private static List<currentLocationChangedListener> currentLocationListeners = new ArrayList<currentLocationChangedListener>();
    private static String lastLocation;
    private static Double current_lat;
    private static Double current_lon;

    //constructor for fragments
    public Constant() { }

    public Constant(Context context) {
        //initialize list of City Objects
        set_cities(context);
        //get unique ID for application
        FirebaseInstallations.getInstance().getId().addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                appId = s;
            }
        });
    }

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

    public void setNewLocation(boolean b) {
        newLocation = b;

        for(currentLocationChangedListener l: currentLocationListeners) {
            l.onCurrentLocationChange();
        }
    }

    public static void addCurrentLocationChangeListener(currentLocationChangedListener l) {
        currentLocationListeners.add(l);
    }

    public void setCurrentLat(Double lat) {
        current_lat = lat;
    }

    public void setCurrentLon(Double lon) {
        current_lon = lon;
    }

    public void setLastLocation(String location) {
        lastLocation = location;
    }

    public String getAppId() {
        return appId;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public Double getCurrentLat() {
        return current_lat;
    }

    public Double getCurrentLon() {
        return current_lon;
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
