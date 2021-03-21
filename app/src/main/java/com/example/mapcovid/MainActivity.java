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
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    //list of all permissions we need for app
    String[] appPermissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
    };
    private static final int PERMISSION_CODE = 100;
    private static final String CHANNEL_ID = "moved location";
    private static final String TAG = MainActivity.class.getSimpleName();
    private GoogleApiClient googleApiClient;

    private LocationRequest locationRequest;
    private long UPDATE_INTERVAL = 10*1000; /*10 secs*/
    private long FASTEST_INTERVAL = 2000; /*2 secs*/

    private Constant constants;

    private DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize constant data structures
        constants = new Constant(getApplicationContext());

        //create GoogleApiClient
        createGoogleApi();

        //create notification channel
        createNotificationChannel();

        //check and request permission for fine location and background location
        checkPermissions();

        //starts background location tracking
        startLocationUpdates();
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
        if(Build.VERSION.SDK_INT >= 26) {
            CharSequence name = getString(R.string.common_google_play_services_notification_channel_name);
            String description = "notification channel for moving location notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setShowBadge(true);
            channel.setLightColor(Color.rgb(255,153,132));
            channel.getLockscreenVisibility();
            channel.enableVibration(true);

            //register the channel with the system; can't change importance or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    //function to check and request permissions
    private boolean checkPermissions() {
        List<String> permissions_needed = new ArrayList<>();
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
            //check if all permissions are granted
            if (deniedCount != 0) {
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
                                "No, Exit App",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog_interface, int i) {
                                        dialog_interface.dismiss();
                                        finish();
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
    @SuppressLint("MissingPermission")
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

                if(currentLocation != null && (lastLocation == null || lastLocation.compareTo(currentLocation) != 0)) {
                    //
                    if(lastLocation != null) {
                        createNotification();
                    }
                    writeToDatabase();
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

        PathItem newCity = new PathItem(time, city, lat, lon);
        String appID = constants.getAppId();

        //pushes new city location to date's path
        database.child(appID).child("paths").child(date).push().setValue(newCity);
    }

    public void onLocationChanged(Location location) {
        //new location has now been determined
        try {
            String city = getCityByCoordinates(location.getLatitude(), location.getLongitude());

            if(city != null) {
                Toast.makeText(this, city, Toast.LENGTH_SHORT).show();
                constants.setCurrentLocation(city);
                constants.setCurrentLat(location.getLatitude());
                constants.setCurrentLon(location.getLongitude());
            }
        } catch(IOException ioe) {
            Log.d(TAG, ioe+"    -   Couldn't retrieve city from updated location coordinates");
        }
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
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
                            if(city != null) constants.setLastLocation(city);
                        } catch(IOException ioe) {
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

    public String getCityByCoordinates(double lat, double lon) throws IOException {
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


}