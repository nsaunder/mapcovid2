package com.example.mapcovid;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mapcovid.Constant;
import com.example.mapcovid.R;
import com.example.mapcovid.currentLocationChangedListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class TestingFragment extends Fragment {

    public class TestingLocation {
        String name;
        LatLng position;
        boolean walkup;
        boolean driveup;
        public TestingLocation(String name, LatLng ll, boolean w, boolean d)
        {
            this.name = name;
            this.position = ll;
            this.walkup = w;
            this.driveup = d;
        }

        public String getName(){return name;}
        public LatLng getPosition(){return position;}
        public boolean getWalkUp(){return walkup;}
        public boolean getDriveUp(){return driveup;}
    }
    private double currentX = 34;
    private double currentY = -118;
    private Constant constants;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            GoogleMap mMap = googleMap;
            constants = new Constant();
            HashMap<String, TestingLocation> testingMap = new HashMap<>();
            String day = LocalDate.now().toString();
            constants.getPath(day, new getPathCallback() {
                boolean newPath = false;
                @Override
                public void onCallback(ArrayList<PathItem> path) {
                    if(path != null) {
                        try {
                            if (newPath == false) {
                                LatLng lastCoordinates = new LatLng(path.get(path.size() - 1).getLat(), path.get(path.size() - 1).getLon());

                                for (int i = path.size() - 2; i >= 0; i--) {
                                    PathItem p = path.get(i);
                                    LatLng temp = new LatLng(p.getLat(), p.getLon());
                                    Polyline line = mMap.addPolyline(new PolylineOptions()
                                            .add(temp, lastCoordinates)
                                            .width(10)
                                            .color(Color.BLUE));
                                    lastCoordinates = temp;
                                }
                                newPath = true;
                            }
                        } catch (Exception e) {
                            System.out.println("RESTART THE APP PLEASE IT WILL WORK WHEN YOU RESTART ---------- 1RESTART THE APP PLEASE IT WILL WORK WHEN YOU RESTART ---------- 2RESTART THE APP PLEASE IT WILL WORK WHEN YOU RESTART ---------- 3RESTART THE APP PLEASE IT WILL WORK WHEN YOU RESTART ----------");
                        }
                    }
                }
            });
            //Original: constants.getCurrentLat() would return null causing app to crash
            //This was because we called ^ before we fetched first location...
            //Fixed by moving the currentlocation code to here and adding a line in mainactivity so the listner knows to fetch first location

            constants.addCurrentLocationChangeListener(new currentLocationChangedListener() {
                Marker lastMarker = null;
                LatLng lastLocation = null;

                @Override
                public void onCurrentLocationChange() {
                    //If lastLocation is not null then remove lastlocation
                    //Otherwise create a "current Location"
                    //Follow same logic
                    if (lastMarker != null) {   //If there exists a last location
                        lastMarker.remove();
                        Polyline line = mMap.addPolyline(new PolylineOptions()
                                .add(lastLocation, new LatLng(constants.getCurrentLat(), constants.getCurrentLon()))
                                .width(10)
                                .color(Color.BLUE));
                    }

                    lastLocation = new LatLng(constants.getCurrentLat(), constants.getCurrentLon());
                    lastMarker = mMap.addMarker(new MarkerOptions()
                            .position(lastLocation)
                            .title("Current Location")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                    lastMarker.showInfoWindow();

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, 10f));
                }

            });

            constants.fragmentReady();





            List<TestingLocation> testingLocations = null;
            // Get the data: latitude/longitude positions of police stations.
            try {
                testingLocations = readItems("test_locations-1.json");
            } catch (JSONException e) {
                System.err.println(e);
            } catch (IOException e) {
                System.err.println(e);
            }

            for(int i = 0; i < testingLocations.size(); i++)
            {
                //testingMap -> (location name, TestingLocation)
                testingMap.put(testingLocations.get(i).getName(), testingLocations.get(i));


                //Add map marker to to map with the city name that can be shown by clicking
                //This will be helpful for onMarkerClickListener
                Marker mark = mMap.addMarker(
                        new MarkerOptions()
                                .position(testingLocations.get(i).getPosition())
                                .title(testingLocations.get(i).getName())
                                .snippet("More info..."));

                mark.showInfoWindow();

            }

            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

                @Override
                public void onInfoWindowClick(Marker marker) {
                    if(!marker.getTitle().equals("Current Location"))
                    {
                        TestingLocation loc = testingMap.get(marker.getTitle());
                        String endpoint = "https://www.google.com/maps/dir/?api=1&origin ="+currentX+","+currentY+"&destination="+loc.getPosition().latitude+","+loc.getPosition().longitude;
                        System.out.println(endpoint);
                        AlertDialog ad = new AlertDialog.Builder(getContext())
                                .create();
                        ad.setCancelable(false);
                        ad.setTitle(loc.getName());
                        ad.setMessage("\nDrive-through: " + loc.getDriveUp() +
                                "\nWalk-in: " + loc.getWalkUp());
                        ad.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        ad.setButton(DialogInterface.BUTTON_NEGATIVE,"Directions", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(endpoint));
                                startActivity(browserIntent);
                                dialog.dismiss();
                            }
                        });
                        ad.setButton(DialogInterface.BUTTON_NEUTRAL,"Website", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://covid19.lacounty.gov/testing/"));
                                startActivity(browserIntent);
                                dialog.dismiss();
                            }
                        });
                        ad.show();
                    }
                    }
            });
        }
    };

    /*public void openDialog(TestingLocation loc){
        InfoDialog info = new InfoDialog();
        info.setTitle(loc.getName());
        info.setInformation("\nDrive-through: " + loc.getDriveUp() +
                            "\nWalk-in: " + loc.getWalkUp()+
                            "\nDirections: www.google.com");
        info.onCreateDialog(new Bundle());
    }*/
    private List<TestingLocation> readItems(String filename) throws JSONException, IOException {
        List<TestingLocation> result = new ArrayList<>();
        InputStream inputStream = getContext().getAssets().open(filename);
        String json = new Scanner(inputStream).useDelimiter("\\A").next();
        JSONArray array = new JSONArray(json);
        Random rand = new Random();
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            double lat = object.getDouble("lat");
            double lng = object.getDouble("longitude");
            String n = object.getString("testing_location_name");
            boolean d = object.getBoolean("drive_up");
            boolean w = object.getBoolean("walk_up");

            LatLng temp = new LatLng(lat, lng);
            result.add(new TestingLocation(n, temp, d, w));
        }
        return result;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_testing, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }
}