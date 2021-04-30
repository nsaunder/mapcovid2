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

interface deleteFileListener {
    void onDelete();
}

public class Constant {
    //PERMISSIONS//
    private static boolean permissionsGranted;
    //DATA TINGS//
    private static String appId;
    private DatabaseReference database;
    private static ArrayList<City> cities;
    private static ArrayList<DayPath> paths;
    private static String currentLocation;
    private static String lastLocation;
    private static Double current_lat;
    private static Double current_lon;
    private static boolean newLocation;
    private static boolean fileDeleted;
    //LISTENERS//
    private static List<currentLocationChangedListener> currentLocationListeners = new ArrayList<currentLocationChangedListener>();
    private static List<mapFragmentListener> mapFragmentListeners = new ArrayList<mapFragmentListener>();
    private static List<permissionsListener> permissionsListeners = new ArrayList<permissionsListener>();
    private static List<deleteFileListener> deleteFileListeners = new ArrayList<deleteFileListener>();

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
        //initialize list of paths
        setPaths(context);
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
                //is = getContext().getAssets().open(filename);
                //File file = new File(Environment.getExternalStorageDirectory(), filename);
                File file = new File(context.getFilesDir(), "final_city_data.json");
                is = new FileInputStream(file);
            }
            else {
                is = this.getClass().getClassLoader().getResourceAsStream("final_city_data.json");
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

    public void setFileDeleted(boolean b) {
        fileDeleted = b;
        for(deleteFileListener l: deleteFileListeners) {
            l.onDelete();
        }
    }

    public void addFileDeletedListener(deleteFileListener l) {
        deleteFileListeners.add(l);
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
//    public void getPath(String day, final getPathCallback callBack) {
//        database.child(appId).child("paths").child(day).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                ArrayList<PathItem> path = new ArrayList<PathItem>();
//                for(DataSnapshot ds: snapshot.getChildren()) {
//                    PathItem city = ds.getValue(PathItem.class);
//                    if(city != null) {
//                        path.add(city);
//                    }
//                }
//                //use callback to make call synchronous --> return path AFTER all data has been fetched
//                callBack.onCallback(path);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.d("Constant Class", "Error Reading Path for " + day);
//            }
//        });
//    }

    //gets path for day passed into function from database in local storage
    public ArrayList<PathItem> getPath(Context context, String day) {
        if(paths == null) {
            System.out.println("NO PATHS!!!!");
            return null;
        }
        //search for date passed in
        for(DayPath dayPath: paths) {
            if(dayPath.getDate().compareTo(day) == 0) {
                for(PathItem item: dayPath.getPlaces()) {
                    System.out.println("PLACE READ: " + item.getCity());
                }
                return dayPath.getPlaces();
            }
        }
        //if we get here, there is no path for date passed in
        return null;
    }

    public ArrayList<DayPath> getPaths() {
        return paths;
    }

    //reads paths from file in local storage
    public void setPaths(Context context) {
        Gson gson = new Gson();
        String data = "";

        try {
            //retrieve file
            //File file = new File(context.getFilesDir(), "paths.json");
            //create input stream with file
            //InputStream is = new FileInputStream(file);
            InputStream is = context.openFileInput("paths.json");
            StringBuilder sb = new StringBuilder();
            //check to see if InputStream is null
            if(is != null) {
                InputStreamReader streamReader = new InputStreamReader(is);
                BufferedReader bufferedReader = new BufferedReader(streamReader);
                String received = "";
                //append lines from buffered reader
                while((received = bufferedReader.readLine()) != null) {
                    sb.append(received);
                }
                //close input stream and save stringBuilder as a String
                is.close();
                data = sb.toString();
            }
        } catch(Exception e) {
            System.out.println("Error when retrieving day path!");
            e.printStackTrace();
        }
        //use gson to recreate list of day paths from data string
        Type pathsType = new TypeToken<ArrayList<DayPath>>(){}.getType();
        paths = gson.fromJson(data, pathsType);

        System.out.println("NUMBER OF PATHS: " + paths.size()  + " -------------------------------");
    }

    public DayPath getDayPath(String date) {
        if(paths == null) {
            return null;
        }
        for(DayPath p: paths) {
            if(p.getDate().compareTo(date) == 0) {
                return p;
            }
        }
        //if we reach here, that means there is no path for date passed in
        return null;
    }

    //checks if city is in LA County
    public boolean inLACounty(String city) {
        //if we didn't populate cities, then return false
        if(cities == null || city == null) {
            return false;
        }
        //traverse cities list
        for(City c: cities) {
            if(c.get_city_name().compareTo(city) == 0) {
                return true;
            }
        }
        //all else fails, return false
        return false;
    }

}
