package com.example.mapcovid;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        BottomNavigationView navView = findViewById(R.id.nav_view);

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
            getInfo(date, cc, ll);
            System.out.println("finished");
        }
    }

    public void getInfo(String day, Context cc, LinearLayout ll) {
        constants.getPath(day, new getPathCallback() {
                @Override
                public void onCallback(ArrayList<PathItem> path) {
                    Set<String> visits = new HashSet<>();
                    HashMap<String, Integer> popLocation = new HashMap<>();
                    int currMax = 0;
                    String popCity = "";
                    for(PathItem p: path)
                    {
                        TextView temp = new TextView(cc);
                        temp.setGravity(Gravity.CENTER);
                        temp.setText(p.getCity() + "------"+p.getTime());
                        ll.addView(temp);
                        int count = popLocation.getOrDefault(p.getCity(), 0);
                        popLocation.put(p.getCity(), count+1);
                        if(count + 1 > currMax)
                        {
                            currMax = count+1;
                            popCity = p.getCity();
                        }
                        visits.add(p.getCity());
                        //String n = p.getTime(); Parse string for day then add to days update days at the end
                    }

                    TextView numLoc = (TextView) findViewById(R.id.numLocations);
                    numLoc.setText((visits.size()+""));

                    TextView pop = (TextView) findViewById(R.id.popCity);
                    pop.setText(popCity);

                }
        });
    }

    public void deletePath(View view){

    }

}