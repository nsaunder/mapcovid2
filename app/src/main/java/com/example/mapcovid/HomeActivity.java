package com.example.mapcovid;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.example.mapcovid.ui.settings.SettingsFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class HomeActivity extends AppCompatActivity {
    private Constant constants;
    private DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    private static final String CHANNEL_ID = "test notification";
    private Switch darkSwitch;
    private boolean dark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // "context" must be an Activity, Service or Application object from your app.
        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
        Python python = Python.getInstance();
        PyObject pythonFile = python.getModule("test");

        try {
            PyObject helloWorldString = pythonFile.callAttr("create_new_file");
        } catch (Exception e) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Please make sure you have access to internet and airplane mode is not disabled. Some features may not be available.")
                    .setCancelable(false)
                    .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        BottomNavigationView navView = findViewById(R.id.nav_view);

        getSupportActionBar().setTitle("MapCovid");
        getSupportActionBar().setHomeActionContentDescription("MapCovid");

        constants = new Constant();

        //create notification channel to send test notification
        createNotificationChannel();

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_covid, R.id.navigation_testing, R.id.navigation_path, R.id.navigation_news, R.id.navigation_settings)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        //NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }

    @Override
    public Resources.Theme getTheme() {
        Resources.Theme theme = super.getTheme();
        if(dark){
            theme.applyStyle(R.style.darkTheme, true);
        }
        else{
            theme.applyStyle(R.style.Theme_MapCovid, true);
        }
        // you could also use a switch if you have many themes that could apply
        return theme;
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

    public void setDate(View view){
        DatePicker dp =(DatePicker) findViewById(R.id.datePicker);
        ScrollView sv = (ScrollView) findViewById(R.id.scroll_view);
        LinearLayout ll = (LinearLayout) findViewById(R.id.linear_layout);

        if(dp != null) {
            String day = dp.getDayOfMonth()+"";
            String month = (dp.getMonth()+1)+"";
            String year = dp.getYear()+"";
            if(day.length() == 1)
                day = "0" + day;
            if(month.length() == 1)
                month = "0" + month;

            String date = year+"-"+month+"-"+day;
            Context cc = this;
            boolean tf = false;
            if(tf == false) {
                getInfo(date, cc, ll, false, new ArrayList<PathItem>());
                tf = true;
            }
        }
    }

    public void setDeleteDate(View view){
        DatePicker dp =(DatePicker) findViewById(R.id.deleteDatePicker);

        if(dp != null) {
            String day = dp.getDayOfMonth()+"";
            String month = (dp.getMonth()+1)+"";
            String year = dp.getYear()+"";
            if(day.length() == 1)
                day = "0" + day;
            if(month.length() == 1)
                month = "0" + month;

            String date = year+"-"+month+"-"+day;

            deleteDayPath(view, date);
        }
    }

    public int getInfo(String day, Context cc, LinearLayout ll, final boolean tf, ArrayList<PathItem> p) {
        int count = 0;
        if(!tf) {
            try{
            ArrayList<PathItem> path = constants.getPath(cc, day);
                boolean t = tf; //Make it only call once only when the above method is called

                    path = removeConsecutiveDuplicates(path);
                    TextView numLoc = (TextView) findViewById(R.id.numLocations);
                    TextView pop = (TextView) findViewById(R.id.popCity);
                    ll.removeAllViews();

                    if (path.size() == 0) {
                        if (numLoc != null && pop != null) {
                            numLoc.setText("0");
                            pop.setText("N/A");
                        }
                        TextView nolocations = new TextView(cc);
                        nolocations.setGravity(Gravity.CENTER);
                        nolocations.setText("No path available.");
                        ll.addView(nolocations);
                    } else {
                        if (t == false) {
                            Set<String> visits = new HashSet<>();
                            HashMap<String, Integer> map = new HashMap<>();
                            int maxNum = 0;
                            String popCity = "";

                            for (PathItem pi : path) {
                                TextView temp = new TextView(cc);
                                temp.setGravity(Gravity.CENTER);
                                temp.setText(pi.getCity() + "------" + pi.getTime());
                                ll.addView(temp); //add view to linear layout

                                visits.add(pi.getCity());
                                count = map.getOrDefault(pi.getCity(), 0);
                                map.put(pi.getCity(), count + 1);
                                if (count + 1 > maxNum) {
                                    maxNum = count + 1;
                                    popCity = pi.getCity();
                                }
                            }
                            if (!visits.isEmpty() && !map.isEmpty()) {
                                numLoc.setText((visits.size() + ""));
                                pop.setText(popCity);
                            } else {
                                numLoc.setText("0");
                                pop.setText("---");
                            }
                        }
                    }
            }
            catch (Exception e){
                TextView temp = new TextView(cc);
                temp.setGravity(Gravity.CENTER);
                temp.setText("No available path!");
                ll.addView(temp);
            }
        }
        else{
            ArrayList<PathItem> path = removeConsecutiveDuplicates(p);

            for (PathItem pp : path) {
                count++;
                TextView temp = new TextView(cc);
                temp.setText(pp.getCity() + "------" + pp.getTime());
                ll.addView(temp); //add view to linear layout
            }
        }
        return count;
    }

    public ArrayList<PathItem> removeConsecutiveDuplicates(ArrayList<PathItem> p) {
        //if path is empty or only contains one city, no need to remove anything
        if(p == null)
            return p;
        if(p.size() <= 1) {
            return p;
        }

        ArrayList<PathItem> toDelete = new ArrayList<PathItem>();
        PathItem prevCity = p.get(0);
        for(int i=1; i < p.size(); i++) {
            //if current city and prev city are same city, then add current city to list of cities to delete
            if(p.get(i).getCity().compareTo(prevCity.getCity()) == 0) {
                toDelete.add(p.get(i));
            }
            prevCity = p.get(i);
        }

        //remove all cities marked to be deleted from p
        p.removeAll(toDelete);
        return p;
    }

    //when user selects a day, deletes all path data for specific day from database
    public void deleteDayPath(View view, String day) {
        AlertDialog ad = new AlertDialog.Builder(this)
                .create();
        ad.setCancelable(false);
        ad.setTitle("ALERT");
        ad.setMessage("You are about to delete your path history for " + day + "...");
        boolean tf = false;
        if(tf == false) {
            ad.setButton(DialogInterface.BUTTON_POSITIVE, "Accept", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //retrieve file
                    File file = new File(getApplicationContext().getFilesDir(), "paths.json");
                    //delete path associated with day if file exists
                    if(file.exists()) {
                        //retrieves database
                        ArrayList<DayPath> paths = constants.getPaths();
                        DayPath toRemove = null;
                        //search for path with date that user selected
                        for(DayPath dp: paths) {
                            if(dp.getDate().compareTo(day) == 0) {
                                //if path exists for day, then mark it for removal
                                toRemove = dp;
                            }
                        }

                        if(toRemove != null) {
                            //delete path from database
                            paths.remove(toRemove);
                            //reflect deletion in data file
                            try {
                                //converting paths to JSON string
                                Gson gson = new Gson();
                                String data = gson.toJson(constants.getPaths());
                                System.out.println("PATH DATA: " + data);
                                //creates/retrieves file to write to
                                FileOutputStream fos = getApplicationContext().openFileOutput("paths.json", Context.MODE_PRIVATE);
                                //convert JSON string to bytes and write to file
                                fos.write(data.getBytes());
                                //save write to file
                                fos.flush();
                                //need to reflect changes in ArrayList in Constant.java => changes only occur when location changes
                                constants.setPaths(getApplicationContext());
                            } catch(Exception e) {
                                System.out.println("Something went wrong when trying to write to database - deleteDayPath() in HomeActivity!");
                                e.printStackTrace();
                            }
                        }
                    }
                    //database.child(constants.getAppId()).removeValue();
                    dialog.dismiss();
                }
            });
            ad.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            tf = true;
        }
        ad.show();
    }

    //when user clicks on button, deletes all path data for user from database
    public void deletePath(View view){
        AlertDialog ad = new AlertDialog.Builder(this)
                .create();
        ad.setCancelable(false);
        ad.setTitle("ALERT");
        ad.setMessage("You are about to delete your path history...");
        boolean tf = false;
        if(tf == false) {
            ad.setButton(DialogInterface.BUTTON_POSITIVE, "Accept", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    //retrieve file
                    File file = new File(getApplicationContext().getFilesDir(), "paths.json");
                    //delete file if it exists + clear database
                    if(file.exists()) {
                        file.delete();
                        constants.getPaths().clear();
                    }
                    //database.child(constants.getAppId()).removeValue();
                    dialog.dismiss();
                }
            });
            ad.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            tf = true;
        }
        ad.show();
    }

    public void updateDataRetentionPeriod(View view, int num) {
        constants.setDataRetentionPeriod(num);
    }

    //when user clicks on button, send a test notification
    public void sendTestNotification(View view) {
        //right now, if user selects notification, they navigate to heat map --> change to news feed
        Intent intent = new Intent(this, HomeActivity.class); //--------------------------------------------
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        //notification text
        String content_title = "Congratulations! You Successfully Received Test Notification!";
        String msg = "\nNow that you read this test notification. You're good to go in terms of receiving notifications through the app!";
        String title_and_msg = content_title + "\n" + msg;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_logo)
                .setContentTitle("Test Notification")
                .setContentText(content_title)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(title_and_msg))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0, builder.build());
    }

    public void showStorage(View view) {
        int totalStorage = 0;
        TextView storageText = (TextView) findViewById(R.id.storageText);
        //retrieves cached files
        File cityFile = new File(getApplicationContext().getFilesDir(), "final_city_data.json");
        File pathFile = new File(getApplicationContext().getFilesDir(), "paths.json");

        if(cityFile.exists()){
            totalStorage += cityFile.length();
        }
        if(pathFile.exists()) {
            totalStorage += pathFile.length();
        }
        //gets storage used by file
        String msg = String.valueOf(totalStorage) + " Bytes Used";
        //set text in settings
        storageText.setText(msg);
    }

    public void deleteStorage(View view) {
        //retrieves cached files
        File cityFile = new File(getApplicationContext().getFilesDir(), "final_city_data.json");
        File pathFile = new File(getApplicationContext().getFilesDir(), "paths.json");
        //deletes cached city data file + paths file if files exist
        if(cityFile.exists()) {
            cityFile.delete();
        }
        if(pathFile.exists()) {
            pathFile.delete();
            //also need to clear database
            constants.getPaths().clear();
        }
    }

    public void showAbout(View view){
        AlertDialog ad = new AlertDialog.Builder(this)
                .create();
        ad.setCancelable(false);
        ad.setTitle("About");
        ad.setMessage("Version: 1.1"+"\n\nWhat's new: Updated UI, Updated location services functionality, Added additional map functionality, Added dark mode functionality, Fixed an error where after changing settings the app would crash\n\n"
                    +"Developers: \nCarson Greengrove\nCatherine Phu\nCyprien Toffa\nNicholas Saunders\nRahul Mehta\nSmrithi Balebail\n");
        ad.setButton(DialogInterface.BUTTON_POSITIVE, "Accept", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        ad.show();
    }

    public void statusCheck(View view) {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        buildAlertMessageNoGps();

    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Would you like to edit your permissions for GPS?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public void notiStautsCheck(View view) {
        final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        buildAlertMessageNotif();

    }

    private void buildAlertMessageNotif() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Would you like to edit your permissions for notifications?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public void darkModeOn(View view){
        dark = !dark;
    }


}
