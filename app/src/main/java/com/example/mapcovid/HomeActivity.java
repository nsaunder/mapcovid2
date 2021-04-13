package com.example.mapcovid;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class HomeActivity extends AppCompatActivity {
    private Constant constants;
    private DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // "context" must be an Activity, Service or Application object from your app.
        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
        Python python = Python.getInstance();
        PyObject pythonFile = python.getModule("test");
        PyObject helloWorldString = pythonFile.callAttr("create_new_file");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        BottomNavigationView navView = findViewById(R.id.nav_view);

        getSupportActionBar().setTitle("MapCovid");
        getSupportActionBar().setHomeActionContentDescription("MapCovid");

        constants = new Constant();

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_covid, R.id.navigation_testing, R.id.navigation_path, R.id.navigation_news, R.id.navigation_settings)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        //NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
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

    public int getInfo(String day, Context cc, LinearLayout ll, final boolean tf, ArrayList<PathItem> p) {
        int count = 0;
        if(!tf) {
            constants.getPath(day, new getPathCallback() {
                boolean t = tf; //Make it only call once only when the above method is called

                @Override
                public void onCallback(ArrayList<PathItem> oldPath) {
                    ArrayList<PathItem> path = removeConsecutiveDuplicates(oldPath);
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

                            for (PathItem p : path) {
                                TextView temp = new TextView(cc);
                                temp.setGravity(Gravity.CENTER);
                                temp.setText(p.getCity() + "------" + p.getTime());
                                ll.addView(temp); //add view to linear layout

                                visits.add(p.getCity());
                                int count = map.getOrDefault(p.getCity(), 0);
                                map.put(p.getCity(), count + 1);
                                if (count + 1 > maxNum) {
                                    maxNum = count + 1;
                                    popCity = p.getCity();
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
                    t = true;
                }

            });
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

    //when user clicks on button, deletes all path data for user from firebase
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
                    database.child(constants.getAppId()).removeValue();
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

    public void showAbout(View view){
        AlertDialog ad = new AlertDialog.Builder(this)
                .create();
        ad.setCancelable(false);
        ad.setTitle("About");
        ad.setMessage("Version: 1.0"+"\n\nWhat's new: ---\n\n"
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


}