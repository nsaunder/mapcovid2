package com.example.mapcovid;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
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
import android.widget.ImageButton;
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
        public TestingLocation(String name, LatLng ll, boolean d, boolean w)
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
    private GoogleMap mMap;
    private Marker marky;

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
            mMap = googleMap;
            mMap.setMinZoomPreference(10f);
            constants = new Constant();
            HashMap<String, TestingLocation> testingMap = new HashMap<>();

            String day = LocalDate.now().toString();
            try {
                ArrayList<PathItem> path = constants.getPath(getContext(), day);
                if (path != null) {
                    try {
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
                    } catch (Exception e) {
                        constants.logError("Error: Could not upload path", getContext());
                    }
                }
            }
            catch (Exception e) {
                constants.logError("Error: Incorrect Permissions", getContext());
            }
            //Original: constants.getCurrentLat() would return null causing app to crash
            //This was because we called ^ before we fetched first location...
            //Fixed by moving the currentlocation code to here and adding a line in mainactivity so the listner knows to fetch first location
            ImageButton button = (ImageButton) getView().findViewById(R.id.test_markerButton);
            ImageButton labutton = (ImageButton) getView().findViewById(R.id.test_LACameraButton);
            ImageButton curposbutton = (ImageButton) getView().findViewById(R.id.test_currentposbutton);

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

                    try {
                        lastLocation = new LatLng(constants.getCurrentLat(), constants.getCurrentLon());

                        if(testingMap.containsKey(constants.getCurrentLocation())){
                            labutton.setVisibility(View.GONE);
                        }
                        else {
                            labutton.setVisibility((View.VISIBLE));
                        }
                        lastMarker = mMap.addMarker(new MarkerOptions()
                                .position(lastLocation)
                                .title("Current Location")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

                        lastMarker.showInfoWindow();

                        mMap.moveCamera(CameraUpdateFactory.newLatLng(lastLocation));
                    } catch(Exception e) {
                        constants.logError("Error: Internet not enabled", getContext());
                    }
                }

            });

            constants.fragmentReady();

            if(!constants.getPermissionsGranted()) {
                button.setVisibility(View.VISIBLE);
            }
            else {
                button.setVisibility(View.GONE);
            }


            button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    markerPlace(false);
                }

            });
            curposbutton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(constants.getCurrentLat(), constants.getCurrentLon())));
                }

            });
            labutton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(33.947029, -118.258471)));
                }

            });



            List<TestingLocation> testingLocations = null;
            // Get the data: latitude/longitude positions of police stations.
            try {
                testingLocations = readItems("test_locations-1.json");
            } catch (JSONException e) {
                constants.logError("Error: " + e.getMessage(), getContext());
            } catch (IOException e) {
                constants.logError("Error: " + e.getMessage(), getContext());
            }

            addTestingMarkers(testingLocations, testingMap);

            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

                @Override
                public void onInfoWindowClick(Marker marker) {
                    if(!marker.getTitle().equals("Current Location"))
                    {
                        TestingLocation loc = testingMap.get(marker.getTitle());
                        String endpoint = "https://www.google.com/maps/dir/?api=1&origin ="+currentX+","+currentY+"&destination="+loc.getPosition().latitude+","+loc.getPosition().longitude;
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
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(33.947029, -118.258471), 10f));
        }
    };

    public List<LatLng> addTestingMarkers(List<TestingLocation> testingLocations, HashMap<String, TestingLocation> testingMap){
        List<LatLng> res = new ArrayList<>();
        for(int i = 0; i < testingLocations.size(); i++)
        {
            //testingMap -> (location name, TestingLocation)
            testingMap.put(testingLocations.get(i).getName(), testingLocations.get(i));


            //Add map marker to to map with the city name that can be shown by clicking
            //This will be helpful for onMarkerClickListener
            Marker mark = null;
            if(mMap != null) {
                mark = mMap.addMarker(
                        new MarkerOptions()
                                .position(testingLocations.get(i).getPosition())
                                .title(testingLocations.get(i).getName())
                                .snippet("More info..."));
                res.add(mark.getPosition());
            }
            else{
                res.add(testingLocations.get(i).getPosition());
            }

        }
        return res;
    }


    public List<TestingLocation> readItems(String filename) throws JSONException, IOException {
        List<TestingLocation> result = new ArrayList<>();
        InputStream inputStream = null;
        if(getContext() != null) {
            inputStream = getContext().getAssets().open(filename);
        }
        else {
            inputStream = this.getClass().getClassLoader().getResourceAsStream(filename);
        }

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

    public void markerPlace(final boolean tf){
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            boolean placeOnce = tf;
            @Override
            public void onMapClick(LatLng arg0) {
                if (placeOnce == false) {
                    if(marky != null)
                        marky.remove();
                    marky = mMap.addMarker(new MarkerOptions()
                            .position(arg0)
                            .title("Current Location")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                    marky.showInfoWindow();
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(arg0, 10f));
                    placeOnce = true;


                }
            }

        });
    }
}