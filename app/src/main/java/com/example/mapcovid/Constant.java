package com.example.mapcovid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
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
import android.os.Environment;
import android.renderscript.ScriptGroup;
import android.util.Log;

import androidx.annotation.NonNull;

//interface to implement listener for when current location changes
interface currentLocationChangedListener {
    public void onCurrentLocationChange();
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
    private static ArrayList<DayPath> paths;
    private static String currentLocation;
    private static String lastLocation;
    private static Double current_lat;
    private static Double current_lon;
    private static boolean newLocation;
    private static boolean fileDeleted;
    private static ArrayList<String> errorList;
    private static Integer dataRetentionPeriod; //how long we keep travel tracking data
    private SharedPreferences preferences;
    //LISTENERS//
    private static List<currentLocationChangedListener> currentLocationListeners = new ArrayList<currentLocationChangedListener>();
    private static List<mapFragmentListener> mapFragmentListeners = new ArrayList<mapFragmentListener>();
    private static List<permissionsListener> permissionsListeners = new ArrayList<permissionsListener>();


    //constructor for fragments
    public Constant() { errorList = new ArrayList<>();
        //default data retention period: 21 days
        if(dataRetentionPeriod==null) dataRetentionPeriod = 21;
    }

    public Constant(Context context) {
        //initialize firebase database reference
        database = get_instance().getReference();
        //initialize list of City Objects
        set_cities(context);
        //initialize list of paths
        setPaths(context);
        //get shared preferences
        preferences = context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        //check permissions saved from last run
        if(preferences.getBoolean("permissionsGranted", true)) {
            setPermissionsGranted(context, true);
        } else {
            setPermissionsGranted(context, false);
        }
        //retrieve dataRetentionPeriod => default value = 21
        dataRetentionPeriod = preferences.getInt("dataRetentionPeriod", 21);
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
                //if file doesn't exist, recreate and repopulate file
                if(!file.exists()) {
                    try {
                        Python python = Python.getInstance();
                        PyObject pythonFile = python.getModule("test");
                        PyObject helloWorldString = pythonFile.callAttr("create_new_file");
                        file = new File(context.getFilesDir(), "final_city_data.json");
                    } catch(Exception e) {
                        errorList.add("Error: when scraping data !");
                    }
                }
                is = new FileInputStream(file);
            }
            else {
                is = this.getClass().getClassLoader().getResourceAsStream("final_city_data.json");
                //if file doesn't exist, recreate and repopulate file
                if(is == null) {
                    try {
                        Python python = Python.getInstance();
                        PyObject pythonFile = python.getModule("test");
                        PyObject helloWorldString = pythonFile.callAttr("create_new_file");
                        is = this.getClass().getClassLoader().getResourceAsStream("final_city_data.json");
                    } catch(Exception e) {
                        errorList.add("Error: when scraping data !");
                    }
                }
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            Gson gson = new Gson();
            Type cityList = new TypeToken<ArrayList<City>>(){}.getType();
            this.cities = gson.fromJson(reader,cityList);
            reader.close();
        } catch(Exception e) {
            errorList.add(e.getMessage());
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
                    //System.out.println("PLACE READ: " + item.getCity());
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

    //use when there is no file and we need array list
    public void setPaths(ArrayList<DayPath> list) {
        paths = list;
    }

    //reads paths from file in local storage
    public void setPaths(Context context) {
        Gson gson = new Gson();
        String data = "";

        try {
            //create input stream with file
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
            //errorList.add("Error: when retrieving day path!");
            e.printStackTrace();
        }
        //use gson to recreate list of day paths from data string
        Type pathsType = new TypeToken<ArrayList<DayPath>>(){}.getType();
        paths = gson.fromJson(data, pathsType);
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

    public Integer getDataRetentionPeriod() {
        return dataRetentionPeriod;
    }

    public void setDataRetentionPeriod(Integer num) {
        //update variable specific to program run
        dataRetentionPeriod = num;
        //update in shared preferences for future program runs
        preferences.edit().putInt("dataRetentionPeriod", num.intValue());
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
    public String getErrors(){
        String res = "";
        for(String error: errorList){
            res+=(error+"\n");
        }
        return res;
    }
    public void initializeList(Context context){
        errorList = new ArrayList<>();
        try {
            //create input stream with file
            InputStream is = context.openFileInput("errors.txt");
            //check to see if InputStream is null
            if(is != null) {
                InputStreamReader streamReader = new InputStreamReader(is);
                BufferedReader bufferedReader = new BufferedReader(streamReader);
                String received = "";
                //append lines from buffered reader
                while((received = bufferedReader.readLine()) != null) {
                    errorList.add(received);
                }
                //close input stream and save stringBuilder as a String
                is.close();
            }
        } catch(Exception e) {
            errorList.add("Error: when retrieving day path!");
        }

    }

    public boolean logError(String error, Context cc){
        if(errorList == null){
            initializeList(cc);
        }
        errorList.add(error);

        try{
            String data = getErrors();
            //creates/retrieves file to write to
            FileOutputStream fos = cc.openFileOutput("errors.txt", Context.MODE_PRIVATE);
            if(fos != null) {
                //convert JSON string to bytes and write to file
                fos.write(data.getBytes());
                //save write to file
                fos.flush();
            } else {
                System.out.println("NO PATH!");
                File file = new File(cc.getFilesDir(), "errors.txt");
                fos = new FileOutputStream(file);
                //convert JSON string to bytes and write to file
                fos.write(data.getBytes());
                //save write to file
                fos.flush();
                fos.close();
            }
            return true;
        } catch (Exception e){
            return false;
        }
/*

        try {
            File root = new File(Environment.getExternalStorageDirectory(), "errors");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, "errorLog");
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(error);
            writer.flush();
            writer.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }*/

    }

    public void readFile(Context context){
        try {
            //create input stream with file
            InputStream is = context.openFileInput("errors.txt");
            //check to see if InputStream is null
            if(is != null) {
                InputStreamReader streamReader = new InputStreamReader(is);
                BufferedReader bufferedReader = new BufferedReader(streamReader);
                String received = "";
                //append lines from buffered reader
                int i = 0;
                while((received = bufferedReader.readLine()) != null) {
                    i++;
                    System.out.println(received + "------ ------ ------ ----- ---" + i);
                }
                //close input stream and save stringBuilder as a String
                is.close();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
