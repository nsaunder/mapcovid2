package com.example.mapcovid.ui.testingmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mapcovid.City;
import com.example.mapcovid.Constant;
import com.example.mapcovid.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
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
            HashMap<String, TestingLocation> testingMap = new HashMap<>();
            // Add a marker in Sydney and move the camera
            LatLng losAngeles = new LatLng(34, -118);

            Marker melbourne = mMap.addMarker(
                    new MarkerOptions()
                            .position(losAngeles)
                            .title("CurrentLocation"));
            melbourne.showInfoWindow();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(losAngeles, 10f));

            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {

                    // TODO Auto-generated method stub
                    if(marker.equals(melbourne)){
                        System.out.println("\n1\n2\n3\n4\n6");
                        return true;
                    }
                    return false;

                }
            });


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
                                .title(testingLocations.get(i).getName()));

                mark.showInfoWindow();

            }

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(losAngeles, 10f));
        }
    };

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