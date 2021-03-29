package com.example.mapcovid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

//interface to implement listener for when current location changes
interface currentLocationChangedListener {
    public void onCurrentLocationChange();
}

interface getPathCallback {
    void onCallback(ArrayList<PathItem> path);
}

interface mapFragmentListener {
    void fragmentReady();
}

interface permissionsListener {
    void onPermissionsChange();
}

public class Constant {
    //PERMISSIONS//
    private static boolean permissionsGranted;
    //DATA TINGS//
    private static String appId;
    private DatabaseReference database;
    private static ArrayList<City> cities;
    private static String currentLocation;
    private static String lastLocation;
    private static Double current_lat;
    private static Double current_lon;
    private static boolean newLocation;
    //LISTENERS//
    private static List<currentLocationChangedListener> currentLocationListeners = new ArrayList<currentLocationChangedListener>();
    private static List<mapFragmentListener> mapFragmentListeners = new ArrayList<mapFragmentListener>();
    private static List<permissionsListener> permissionsListeners = new ArrayList<permissionsListener>();

    //constructor for fragments
    public Constant() {
        //initialize firebase database reference
        database = get_instance().getReference();
    }

    public Constant(Context context) {
        //initialize firebase database reference
        database = get_instance().getReference();
        //initialize list of City Objects
        set_cities(context);
        //get unique ID for application
        FirebaseInstallations.getInstance().getId().addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                appId = s;
            }
        });
        //get shared preferences
        SharedPreferences preferences = context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        //check permissions saved from last run
        if(preferences.getBoolean("permissionsGranted", true)) {
            setPermissionsGranted(context, true);
        } else {
            setPermissionsGranted(context, false);
        }
    }

    //wrapper for static FirebaseDatabase getInstance()
    public FirebaseDatabase get_instance() {
        return FirebaseDatabase.getInstance();
    }

    public void set_cities(Context context) {
        try {
            InputStream is = null;
            if(context != null) {
                is = context.getAssets().open("city_data.json");
            }
            else {
                is = this.getClass().getClassLoader().getResourceAsStream("city_data.json");
            }
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

    public void addCurrentLocationChangeListener(currentLocationChangedListener l) {
        currentLocationListeners.add(l);
    }

    public void addMapFragmentListener(mapFragmentListener l) {
        mapFragmentListeners.add(l);
    }

    public void fragmentReady() {
        for(mapFragmentListener l: mapFragmentListeners) {
            l.fragmentReady();
        }
    }

    public void setPermissionsGranted(Context context, boolean b) {
        //get shared preferences
        SharedPreferences preferences = context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        //set permissions to boolean
        preferences.edit().putBoolean("permissionsGranted", b).apply();
        permissionsGranted = b;
        for(permissionsListener l: permissionsListeners) {
            l.onPermissionsChange();
        }
    }

    public void addPermissionListener(permissionsListener l) {
        permissionsListeners.add(l);
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

    public ArrayList<City> getCities() {
        return cities;
    }

    public List<permissionsListener> getPermissionListeners() {
        return permissionsListeners;
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

    public boolean getPermissionsGranted() { return permissionsGranted; }

    public City get_city(String city) {
        //added because it failed test case
        if(city == null) {
            return null;
        }
        for(City c: cities) {
            if(c.get_city_name().compareTo(city) == 0) {
                return c;
            }
        }
        return null;
    }

    public DatabaseReference getDatabase() {
        return database;
    }

    //get path for day passed into function from firebase
    public void getPath(String day, final getPathCallback callBack) {
        database.child("e0oPScPeTRy3c84TQcG4LS").child("paths").child(day).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<PathItem> path = new ArrayList<PathItem>();
                for(DataSnapshot ds: snapshot.getChildren()) {
                    PathItem city = ds.getValue(PathItem.class);
                    if(city != null) {
                        path.add(city);
                    }
                }
                //use callback to make call synchronous --> return path AFTER all data has been fetched
                callBack.onCallback(path);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("Constant Class", "Error Reading Path for " + day);
            }
        });
    }

}
