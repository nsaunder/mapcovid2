package com.example.mapcovid;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class LocationService extends Service {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String CHANNEL_ID = "moved location";
    Constant constants;
    private LocationRequest locationRequest;
    private long UPDATE_INTERVAL = 10000; /*10 secs*/
    private long FASTEST_INTERVAL = 5000; /*5 secs*/
    private DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //initialize constant data structures
        constants = new Constant();
        constants.set_cities(getApplicationContext());

        prepareForegroundNotification();
        startLocationUpdates();

        return START_STICKY;
    }

    private void prepareForegroundNotification() {
        //create the notification channel, but only in API 26+
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String description = "notification channel for moving location notifications";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Location Service Channel", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(description);
            channel.setShowBadge(true);
            channel.getLockscreenVisibility();
            channel.enableVibration(true);

            //register the channel with the system; can't change importance or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        //creates notification to let user know that we're tracking their location in background
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Background Location Service")
                .setContentText("Background Location Service Running")
                .setSmallIcon(R.drawable.notification_logo)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
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

        //pushes new city location to date's path
        database.child("paths").child(date).push().setValue(newCity);
    }

    private void onLocationChanged(Location location) {
        //new location has now been determined
        try {
            String city = getCityByCoordinates(location.getLatitude(), location.getLongitude());
            if(city != null) {
                Toast.makeText(this, city, Toast.LENGTH_SHORT).show();
                constants.setCurrentLocation(city);
            }
        } catch(IOException ioe) {
            Log.d(TAG, "Couldn't retrieve city from updated location coordinates");
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
                            Log.d(TAG, "getLastLocation() failed");
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

    private String getCityByCoordinates(double lat, double lon) throws IOException {
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
        Intent intent = new Intent(this, HomeActivity.class);
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

    @Override
    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
    }
}
