package com.example.mapcovid.ui.covidmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mapcovid.City;
import com.example.mapcovid.Constant;
import com.example.mapcovid.R;
import com.example.mapcovid.ui.testingmap.TestingFragment;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
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

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MapsFragment extends Fragment {

    private LocationRequest locationRequest;
    private long UPDATE_INTERVAL = 10*1000; /*10 secs*/
    private long FASTEST_INTERVAL = 2000; /*2 secs*/
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
            HashMap<String, City> citiesMap = new HashMap<>();
            // Add a marker in Sydney and move the camera
            LatLng losAngeles = new LatLng(34, -118);

            Marker melbourne = mMap.addMarker(
                    new MarkerOptions()
                            .position(losAngeles)
                            .title("CurrentLocation"));
            melbourne.showInfoWindow();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(losAngeles, 10f));


            List<City> cities = null;
            List<WeightedLatLng> latLngs = new ArrayList<>();
            // Get the data: latitude/longitude positions of police stations.
            try {
                cities = readItems("city_data.json");
            } catch (JSONException e) {
                System.err.println(e);
            } catch (IOException e) {
                System.err.println(e);
            }

            for(int i = 0; i < cities.size(); i++)
            {
                //citiesMap -> (cityName, City)
                citiesMap.put(cities.get(i).get_city_name(), cities.get(i));

                //Create a LatLng
                LatLng citypos = new LatLng(cities.get(i).get_center_lat(),
                        cities.get(i).get_center_long());

                //Create the weightedlatlng
                WeightedLatLng temp = new WeightedLatLng(citypos, cities.get(i).get_new_deaths());
                latLngs.add(temp);  //Add to latLngs arraylist

                //Add map marker to to map with the city name that can be shown by clicking
                //This will be helpful for onMarkerClickListener
                Marker mark = mMap.addMarker(
                        new MarkerOptions()
                                .position(citypos)
                                .title(cities.get(i).get_city_name()));

                mark.showInfoWindow();

            }
            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    if(!marker.getTitle().equals("CurrentLocation"))
                    {
                        City loc = citiesMap.get(marker.getTitle());
                        AlertDialog ad = new AlertDialog.Builder(getContext())
                                .create();
                        ad.setCancelable(false);
                        ad.setTitle(loc.get_city_name());
                        ad.setMessage("\nNew Cases: " + loc.get_new_cases() +
                                "\nNew Deaths: " + loc.get_new_deaths()+
                                "\nTotal Cases: "+ loc.get_total_cases() +
                                "\nTotal Deaths: " + loc.get_total_deaths());
                        ad.setButton("OK", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        ad.show();
                    }
                    return true;

                }
            });
            // Create a heat map tile provider, passing it the latlngs of the police stations.
            HeatmapTileProvider provider = new HeatmapTileProvider.Builder()
                    .weightedData(latLngs)
                    .opacity(0.7)
                    .radius(30)
                    .maxIntensity(9)
                    .build();

            // Add a tile overlay to the map, using the heat map tile provider.
            TileOverlay overlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(losAngeles, 10f));
        }
    };

    private List<City> readItems(String filename) throws JSONException, IOException {
        List<City> cities = new ArrayList<>();
        try {
            InputStream is = getContext().getAssets().open("city_data.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            Gson gson = new Gson();
            Type cityList = new TypeToken<ArrayList<City>>(){}.getType();
            cities = gson.fromJson(reader,cityList);
            reader.close();
        } catch(Exception e) {
            System.err.println(e);
        }
        return cities;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
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