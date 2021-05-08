package com.example.mapcovid;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    String[] appPermissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.INTERNET
    };
    private static final int PERMISSION_CODE = 100;

    private static final String CHANNEL_ID = "moved location";
    private static final String TAG = MainActivity.class.getSimpleName();
    private GoogleApiClient googleApiClient;

    private LocationRequest locationRequest;
    private long UPDATE_INTERVAL = 10 * 1000; /*10 secs*/
    private long FASTEST_INTERVAL = 2000; /*2 secs*/

    private Constant constants;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //initialize constant data structures
        constants = new Constant(getApplicationContext());
        //get shared preferences
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("my_preferences", MODE_PRIVATE);
        //check if onboarding_complete is false
        if (!preferences.getBoolean("onboarding_complete", false)) {
            //start onboarding activity
            Intent onboarding = new Intent(this, OnboardingActivity.class);
            startActivity(onboarding);
            //close main activity
            finish();
            return;
        }
        //checks to see last day we deleted travel tracking data => stored in shared preferences
        String now = LocalDate.now().toString();
        String lastDay = preferences.getString("last_day_deleted", now);

        if(lastDay != now) {
            try {
                //format of how we store dates in data file
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date lastDayDeleted = dateFormat.parse(lastDay);
                Date today = Calendar.getInstance().getTime();
                //calculate the number of days between last day we deleted data and today
                int diff = daysBetween(lastDayDeleted,today);
                //get data retention period
                int period = constants.getDataRetentionPeriod();
                //exceeded data retention period => delete data
                if(diff >= period) {
                    //retrieve file
                    File file = new File(getApplicationContext().getFilesDir(), "paths.json");
                    //delete file if it exists + clear database
                    if(file.exists()) {
                        file.delete();
                        constants.getPaths().clear();
                    }
                    //update last day data deleted in shared preferences
                    preferences.edit().putString("last_day_deleted",today.toString()).apply();
                }

            } catch(ParseException pe) {
                System.out.println("Something went wrong when parsing date for deleting travel data after data retention period!");
                pe.printStackTrace();
            }
        }

        //setup covidAlarm
        covidAlarm();
        //create GoogleApiClient
        createGoogleApi();

        //create notification channel
        createNotificationChannel();

        //check and request permission for fine location, background location, and internet
        checkPermissions();

        //make sure permissions are granted before sending signal of first location update when MapFragment is created
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            constants.addMapFragmentListener(new mapFragmentListener() {
                @Override
                public void fragmentReady() {
                    constants.setNewLocation(true);
                }
            });
        }
    }

    public void covidAlarm() {
        Calendar calendar = Calendar.getInstance();
        //sets alarm to 10 AM
        calendar.set(Calendar.HOUR_OF_DAY, 10);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 15);

        if (calendar.getTime().compareTo(new Date()) < 0)
            calendar.add(Calendar.DAY_OF_MONTH, 1);

        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }

    //launches next Activity after user selects 'Launch' button
    public void handleLaunch(View view) {
        Intent newActivity = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(newActivity);
    }

    //create GoogleApiClient instance
    private void createGoogleApi() {
        Log.d(TAG, "createGoogleApi()");
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    //create notification channel to push notifications to user
    private void createNotificationChannel() {
        //create the notification channel, but only in API 26+
        if (Build.VERSION.SDK_INT >= 26) {
            CharSequence name = getString(R.string.common_google_play_services_notification_channel_name);
            String description = "notification channel for moving location notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setShowBadge(true);
            channel.setLightColor(Color.rgb(255, 153, 132));
            channel.getLockscreenVisibility();
            channel.enableVibration(true);

            //register the channel with the system; can't change importance or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    //function to check and request permissions
    public boolean checkPermissions() {
        List<String> permissions_needed = new ArrayList<String>();
        //check which permissions are granted
        for (String p : appPermissions) {
            if (ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_DENIED) {
                permissions_needed.add(p);
            }
        }
        //ask for non-granted permissions
        if (!permissions_needed.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissions_needed.toArray(new String[permissions_needed.size()]), PERMISSION_CODE);
            return false;
        }
        //app has all permissions
        //starts background location tracking
        startLocationUpdates();
        return true;
    }

    // This function is called when the user accepts or decline the permission.
    // Request Code is used to check which permission called this function.
    // This request code is provided when the user is prompt for permission.
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CODE) {
            HashMap<String, Integer> permissionResults = new HashMap<>();
            int deniedCount = 0;
            //gather permission grant results
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    permissionResults.put(permissions[i], grantResults[i]);
                    deniedCount++;
                }
            }
            //all permissions are granted
            if (deniedCount == 0) {
                constants.setPermissionsGranted(getApplicationContext(),true);
                //starts background location tracking
                startLocationUpdates();
            }
            //check if all permissions are granted
            if (deniedCount != 0) {
                constants.setPermissionsGranted(getApplicationContext(), false);
                for (Map.Entry<String, Integer> e : permissionResults.entrySet()) {
                    String permission_name = e.getKey();
                    int permission_result = e.getValue();

                    //permission is denied so ask again explaining usage of permission
                    //shouldShowRequestPermissionRationale will return true
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission_name)) {
                        //show dialog of explanation
                        showDialog("", "This app needs Fine Location and Background Location permissions to track your path and send notifications containing Covid-19 related data if you enter a new region.",
                                "Yes, Grant Permissions",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog_interface, int i) {
                                        dialog_interface.dismiss();
                                        checkPermissions();
                                    }
                                },
                                "No, Continue",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog_interface, int i) {
                                        dialog_interface.dismiss();
                                        //finish();
                                    }
                                }, false
                        );
                    }
                    //permission is denied and never ask again is checked
                    //shouldShowRequestPermissionRationale will return false
                    else {
                        showDialog("",
                                "You have denied some permissions. Allow permissions at [Setting] -> [Permissions]",
                                "Go to Settings",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog_interface, int i) {
                                        dialog_interface.dismiss();
                                        //go to app settings
                                    }
                                },
                                "Continue",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog_interface, int i) {
                                        dialog_interface.dismiss();
                                    }
                                }, false
                        );
                        break;
                    }
                }
            }
        }
    }

    //trigger new location updates at interval
    protected void startLocationUpdates() {
        //create the location request to start receiving location updates
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        //create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        //check whether location settings are satisfied
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        //if permissions denied, check permissions and request permissions before proceeding
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            checkPermissions();
            return;
        }

        //gets current location and handles storing location changes to display in user path
        getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                //gets current location and updates "currentLocation" data member
                onLocationChanged(locationResult.getLastLocation());
                //gets last location and updates "lastLocation" data member
                getLastLocation();
                //new location is different from last recorded location
                String currentLocation = constants.getCurrentLocation();
                String lastLocation = constants.getLastLocation();

                if (currentLocation != null && (lastLocation == null || lastLocation.compareTo(currentLocation) != 0)) {
                    if (lastLocation != null) {
                        //only display notifications with relevant Covid-19 info to user when they move within LA County
                        if(constants.inLACounty(currentLocation)) {
                            //System.out.println("Inside LA County: " + currentLocation);
                            createNotification();
                        }
                        else {
                            //if user is outside of LA County, then send notification warning them that we cannot
                            //guarantee certain features
                            //System.out.println("Outside LA County: " + currentLocation);
                            createWarningNotification();
                        }
                        constants.setNewLocation(true);
                    }
                    //only track user's location when they're in LA County
                    if(constants.inLACounty(currentLocation)) {
                        writeToDatabase();
                    }
                }

            }
        }, Looper.myLooper());
    }

    //writes new path location to firebase database
    public void writeToDatabase() {
        String date = LocalDate.now().toString();
        String time = LocalTime.now().toString();
        String city = constants.getCurrentLocation();
        Double lat = constants.getCurrentLat();
        Double lon = constants.getCurrentLon();

        //creates new path item for new change in location
        PathItem newCity = new PathItem(time, city, lat, lon);
        String appID = constants.getAppId();

        //retrieves path for current date or creates new path for current date
        DayPath path = constants.getDayPath(date);
        if(path != null) {
            //there is a path that exists for current date => retrieve path
            List<PathItem> places = path.getPlaces();
            //add new path item to existing path
            places.add(newCity);
            //System.out.println(path.toString());
        } else {
            //there is no path for current date => create a path with new location detected
            ArrayList<PathItem> places = new ArrayList<PathItem>();
            places.add(newCity);
            DayPath newPath = new DayPath(date, places);
            if(constants.getPaths() == null) {
                //happens when there is no file => need to initialize paths
                ArrayList<DayPath> tempPaths = new ArrayList<DayPath>();
                tempPaths.add(newPath);
                constants.setPaths(tempPaths);
            } else {
                //we have a file; therefore we have array list
                //add new path to database
                constants.getPaths().add(newPath);
            }
        } //at this point, database reflects new location change accurately

        try {
            //converting paths to JSON string
            Gson gson = new Gson();
            String data = gson.toJson(constants.getPaths());
            //System.out.println("PATH DATA: " + data);
            //creates/retrieves file to write to
            FileOutputStream fos = getApplicationContext().openFileOutput("paths.json", Context.MODE_PRIVATE);
            if(fos != null) {
                //convert JSON string to bytes and write to file
                fos.write(data.getBytes());
                //save write to file
                fos.flush();
            } else {
                //System.out.println("NO PATH!");
                File file = new File(getApplicationContext().getFilesDir(), "paths.json");
                fos = new FileOutputStream(file);
                //convert JSON string to bytes and write to file
                fos.write(data.getBytes());
                //save write to file
                fos.flush();
            }

            //need to reflect changes in ArrayList in Constant.java => changes only occur when location changes
            constants.setPaths(getApplicationContext());

        } catch(Exception e) {
            constants.logError("Error: Could not write to database. " + e.getMessage(), getApplicationContext());
            e.printStackTrace();
        }
    }

    public void onLocationChanged(Location location) {
        //caught using testOnLocationChanged3
        if(location == null) {
            return;
        }
        //new location has now been determined
        try {
            String city = getCityByCoordinates(location.getLatitude(), location.getLongitude());

            if (city != null) {
                constants.setCurrentLocation(city);
                constants.setCurrentLat(location.getLatitude());
                constants.setCurrentLon(location.getLongitude());
            }
        } catch (IOException ioe) {
            constants.logError("Error: Could not retrieve city coordinates. " + ioe.getMessage(), getApplicationContext());
        }
    }

    public void getLastLocation() {
        //if permissions denied, check permissions and request permissions before proceeding
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            checkPermissions();
            return;
        }

        //get last known recent location
        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);

        Task<Location> prevLocation = locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        double lat = location.getLatitude();
                        double lon = location.getLongitude();
                        try {
                            String city = getCityByCoordinates(lat, lon);
                            if (city != null) constants.setLastLocation(city);
                        } catch (IOException ioe) {
                            constants.logError("get last location failed. " + ioe.getMessage(), getApplicationContext());
                            Log.d(TAG, ioe + "  -  getLastLocation() failed");
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error trying to get last GPS location");
                    }
                });
    }

    public String getCityByCoordinates(Double lat, Double lon) throws IOException {
        Geocoder gc = new Geocoder(this);
        //fetches up to 10 addresses around the coordinates passed in
        List<Address> addresses = gc.getFromLocation(lat, lon, 10);
        //retrieves city associated with coordinates by iterating through "addresses"
        if(addresses!=null && addresses.size()>0) {
            for(Address a: addresses) {
                if(a.getLocality()!=null && a.getLocality().length()>0) {
                    return a.getLocality();
                }
            }
        }
        return null;
    }

    //function to handle notifying users about covid-19 related details based on new location they moved to
    public void createNotification() {
        //right now, if user selects notification, they navigate to heat map --> change to news feed
        Intent intent = new Intent(MainActivity.this, HomeActivity.class); //--------------------------------------------
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        //gets city that user just moved into
        City city = constants.get_city(constants.getCurrentLocation());
        if(city == null) {
            System.out.println("Moved into city that doesn't exist or isn't in LA County!");
            return;
        }

        //notification text
        String content_title = "Covid19 Statistics for " + constants.getCurrentLocation();
        String msg = city.city_notification_message();
        String title_and_msg = content_title + "\n" + msg;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_logo)
                .setContentTitle("MapCovid Detected City Change")
                .setContentText(content_title)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(title_and_msg))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0, builder.build());
    }

    //warning notification only used for when user moves outside of LA County
    public void createWarningNotification() {
        //right now, if user selects notification, they navigate to heat map --> change to news feed
        Intent intent = new Intent(MainActivity.this, HomeActivity.class); //--------------------------------------------
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        //notification text
        String content_title = "You just moved to " + constants.getCurrentLocation() + " which is outside of LA County.";
        String msg = "\nSince you're no longer in LA County, we cannot guarantee you access to the same features " +
                    "that would be available to you if you were located in LA County. These features include " +
                    "but aren't limited to: Covid Map, Testing Locations Map, and Travel Tracking";
        String title_and_msg = content_title + "\n" + msg;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_logo)
                .setContentTitle("MapCovid Detected City Change Outside LA County")
                .setContentText(content_title)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(title_and_msg))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0, builder.build());
    }

    public AlertDialog showDialog(String title, String msg, String positiveLabel, DialogInterface.OnClickListener positiveOnClick,
                                  String negativeLabel, DialogInterface.OnClickListener negativeOnClick, boolean isCancelAble) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setCancelable(isCancelAble);
        builder.setMessage(msg);
        builder.setPositiveButton(positiveLabel, positiveOnClick);
        builder.setNegativeButton(negativeLabel, negativeOnClick);

        AlertDialog alert = builder.create();
        alert.show();
        return alert;
    }

    @Override
    protected void onStart() {
        super.onStart();
        //call GoogleApiClient connection when starting Activity
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //disconnect GoogleApiClient when stopping Activity
        googleApiClient.disconnect();
    }

    //GoogleApiClient.ConnectionCallbacks connected
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected()");
    }

    //GoogleApiClient.ConnectionCallbacks suspended
    @Override
    public void onConnectionSuspended(int i) {
        Log.w(TAG, "onConnectionSuspended()");
    }

    //GoogleApiClient.OnConnectionFailedListener fail
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(TAG, "onConnectionFailed()");
    }

    public Constant getConstants() {
        return constants;
    }

    public int daysBetween(Date d1, Date d2){
        return (int)( (d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
    }
}
