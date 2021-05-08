package com.example.mapcovid;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.google.android.gms.location.LocationRequest;
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
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;



public class MapsFragment extends Fragment {


    private LocationRequest locationRequest;
    private long UPDATE_INTERVAL = 10*1000; /*10 secs*/
    private long FASTEST_INTERVAL = 2000; /*2 secs*/
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
            Marker currentMark = null;
            LatLng cur = null;
            constants = new Constant();
            constants.logError("New error", getContext());
            HashMap<String, City> citiesMap = new HashMap<>();

            String day = LocalDate.now().toString();
            try {
                ArrayList<PathItem> path = constants.getPath(getContext(), day);
                if (path != null) {
                    try {
                        LatLng lastCoordinates = new LatLng(path.get(path.size() - 1).getLat(), path.get(path.size() - 1).getLon());
                        LatLng temp = null;
                        for (int i = path.size() - 2; i >= 0; i--) {
                            PathItem p = path.get(i);
                            temp = new LatLng(p.getLat(), p.getLon());
                            Polyline line = mMap.addPolyline(new PolylineOptions()
                                    .add(temp, lastCoordinates)
                                    .width(10)
                                    .color(Color.BLUE));
                            lastCoordinates = temp;
                        }
                        if(temp != null) {
                            currentMark = mMap.addMarker(new MarkerOptions()
                                    .position(lastCoordinates)
                                    .title("Current Location")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                            currentMark.showInfoWindow();
                            cur = lastCoordinates;
                        }

                    } catch (Exception e) {
                        constants.logError("Error: " + e.getMessage(), getContext());
                    }
                }
            }
            catch (Exception e) {
                constants.logError("Error: Permisssions are off", getContext());
            }
            //Original: constants.getCurrentLat() would return null causing app to crash
            //This was because we called ^ before we fetched first location...
            //Fixed by moving the currentlocation code to here and adding a line in mainactivity so the listner knows to fetch first location
            ImageButton button = (ImageButton) getView().findViewById(R.id.markerButton);
            ImageButton labutton = (ImageButton) getView().findViewById(R.id.LACameraButton);
            ImageButton curposbutton = (ImageButton) getView().findViewById(R.id.currentposbutton);
            ImageButton infobutton = (ImageButton) getView().findViewById(R.id.infoButton);

            final Marker mark = currentMark;
            final LatLng fucr = cur;
            constants.addCurrentLocationChangeListener(new currentLocationChangedListener() {
                Marker lastMarker = null;
                LatLng lastLocation = null;
                Marker otherMar = mark;
                LatLng otherCur = fucr;

                @Override
                public void onCurrentLocationChange() {
                    //If lastLocation is not null then remove lastlocation
                    //Otherwise create a "current Location"
                    //Follow same logic
                    if (lastMarker != null) {   //If there exists a last location
                        lastMarker.remove();
                        if(constants.getCurrentLat() != null) {
                            Polyline line = mMap.addPolyline(new PolylineOptions()
                                    .add(lastLocation, new LatLng(constants.getCurrentLat(), constants.getCurrentLon()))
                                    .width(10)
                                    .color(Color.BLUE));
                        }

                    }
                    else{
                        if(otherMar != null) {
                            otherMar.remove();
                        }

                    }

                    //lastLocation = new LatLng(34.2, -118.23);
                    try{
                            lastLocation = new LatLng(constants.getCurrentLat(), constants.getCurrentLon());


                    if(citiesMap.containsKey(constants.getCurrentLocation())){
                        labutton.setVisibility(View.GONE);
                    }
                    else {
                        labutton.setVisibility(View.VISIBLE);
                    }

                    lastMarker = mMap.addMarker(new MarkerOptions()
                            .position(lastLocation)
                            .title("Current Location")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                    lastMarker.showInfoWindow();

                    mMap.moveCamera(CameraUpdateFactory.newLatLng(lastLocation));

                    }
                    catch(Exception e){
                        constants.logError("Error: " + e.getMessage(), getContext());
                    }
                }

            });
            constants.fragmentReady();

            if(!constants.getPermissionsGranted()) {
                button.setVisibility(View.VISIBLE);
                labutton.setVisibility(View.VISIBLE);
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
                    if(constants != null && constants.getCurrentLat() != null)
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(constants.getCurrentLat(), constants.getCurrentLon())));
                    else {
                        if(marky != null) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(marky.getPosition()));
                        }
                    }
                }

            });
            ImageButton screenButton = (ImageButton) getView().findViewById(R.id.share_button);
            screenButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                      captureScreen();
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

            infobutton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    AlertDialog ad = new AlertDialog.Builder(getContext())
                            .create();
                    ad.setCancelable(false);
                    LayoutInflater factory = LayoutInflater.from(getContext());
                    final View view = factory.inflate(R.layout.legend, null);
                    ad.setView(view);
                    ad.setButton(DialogInterface.BUTTON_NEUTRAL,"OK", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }

                    });
                    ad.show();
                }
            });

            constants.addPermissionListener(new permissionsListener() {
                @Override
                public void onPermissionsChange() {
                    if(constants.getPermissionsGranted() == false)
                        button.setVisibility(View.VISIBLE);
                    else
                        button.setVisibility(View.GONE);
                }
            });

            constants.fragmentReady();

//            View b = findViewById(R.id.button);
//b.setVisibility(View.GONE);


            List<City> cities = null;
            List<WeightedLatLng> latLngs = new ArrayList<>();
            // Get the data: latitude/longitude positions of police stations.
            try {

                cities = readItems("final_city_data.json");
            } catch (JSONException e) {
                constants.logError("Error: " + e.getMessage(), getContext());
            } catch (IOException e) {
                constants.logError("Error: " + e.getMessage(), getContext());
            }

            addCityMarkers(cities, latLngs, citiesMap);




            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

                @Override
                public void onInfoWindowClick(Marker marker) {
                    try {
                        if(!marker.getTitle().equals("Current Location"))
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
                    } catch(Exception e) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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

                }
            });
            // Create a heat map tile provider, passing it the latlngs of the police stations.
            if(latLngs != null && !latLngs.isEmpty()) {
                HeatmapTileProvider provider = new HeatmapTileProvider.Builder()
                        .weightedData(latLngs)
                        .opacity(0.7)
                        .radius(30)
                        .maxIntensity(9)
                        .build();
                // Add a tile overlay to the map, using the heat map tile provider.
                TileOverlay overlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(provider));
            }

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(33.947029, -118.258471), 10f));
        }
    };

    public List<LatLng> addCityMarkers(List<City> cities, List<WeightedLatLng> latLngs, HashMap<String, City> citiesMap){
        List<LatLng> res = new ArrayList<>();
        for(int i = 0; i < cities.size(); i++)
        {
            City city = cities.get(i);
            //citiesMap -> (cityName, City)
            citiesMap.put(city.get_city_name(), city);

            //Create a LatLng
            LatLng citypos = new LatLng(cities.get(i).get_center_lat(),
                    city.get_center_long());

            //Create the weightedlatlng
            WeightedLatLng temp = new WeightedLatLng(citypos, city.get_new_cases());
            latLngs.add(temp);  //Add to latLngs arraylist

            //Add map marker to to map with the city name that can be shown by clicking
            //This will be helpful for onMarkerClickListener
            Marker mark = null;
            if(mMap != null) {
                mark = mMap.addMarker(
                        new MarkerOptions()
                                .position(citypos)
                                .title(city.get_city_name())
                                .snippet("More info..."));
                if(city.get_new_cases() < 3){
                    mark.setIcon(BitmapDescriptorFactory.defaultMarker(105.0f));
                }
                else if(city.get_new_cases() >= 3 && city.get_new_cases() < 6){
                    mark.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
                }
                else if(city.get_new_cases() >= 6 && city.get_new_cases() < 9){
                    mark.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                }
                else {
                    mark.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                }

                res.add(mark.getPosition());
            }
            else
            {
                res.add(citypos);
            }

            //mark.showInfoWindow();

        }
        return res;
    }


    public List<City> readItems(String filename) throws JSONException, IOException {
        List<City> cities = new ArrayList<>();
        try {

            InputStream is = null;
            if(getContext() != null) {
                //is = getContext().getAssets().open(filename);
                //File file = new File(Environment.getExternalStorageDirectory(), filename);
                File file = new File(getContext().getFilesDir(), "final_city_data.json");
                //if file doesn't exist, recreate and repopulate file
                if(!file.exists()) {
                    try {
                        Python python = Python.getInstance();
                        PyObject pythonFile = python.getModule("test");
                        PyObject helloWorldString = pythonFile.callAttr("create_new_file");
                        file = new File(getContext().getFilesDir(), "final_city_data.json");
                    } catch(Exception e) {
                        constants.logError("Error: Could not scrape data turn on internet " + e.getMessage(), getContext());
                    }
                }
                is = new FileInputStream(file);
            }
            else {
                is = this.getClass().getClassLoader().getResourceAsStream("final_city_data.json");
                //if file doesn't exist, recreate and repopulate file
                if(is == null) {
                    try {
                        Python python = Python.getInstance();
                        PyObject pythonFile = python.getModule("test");
                        PyObject helloWorldString = pythonFile.callAttr("create_new_file");
                        is = this.getClass().getClassLoader().getResourceAsStream("final_city_data.json");
                    } catch(Exception e) {
                        constants.logError("Error: Could not scrape data turn on internet " + e.getMessage(), getContext());
                    }
                }
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            Gson gson = new Gson();
            Type cityList = new TypeToken<ArrayList<City>>(){}.getType();
            cities = gson.fromJson(reader,cityList);
            reader.close();
        } catch(Exception e) {
            constants.logError("Error: " + e.getMessage(), getContext());
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

    public void shareImage(File file) {
        Uri uri = FileProvider.getUriForFile(getContext(),
                BuildConfig.APPLICATION_ID + ".provider",
                file);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");

        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, "");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
//        intent.putExtra(android.content.Intent.)
        try {
            startActivity(Intent.createChooser(intent, "Share Screenshot"));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), "No App Available", Toast.LENGTH_SHORT).show();
        }

    }

    public void captureScreen()
    {
        GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback()
        {

            @Override
            public void onSnapshotReady(Bitmap snapshot)
            {
                // TODO Auto-generated method stub
                Bitmap bitmap = snapshot;

                File file = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), "Screenshot.png");
                FileProvider.getUriForFile(Objects.requireNonNull(requireActivity().getApplicationContext()),
                        BuildConfig.APPLICATION_ID + ".provider", file);
                final String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Screenshots";
                File dir = new File(dirPath);
                if(!dir.exists())
                    dir.mkdirs();

                try
                {
                    FileOutputStream fout = new FileOutputStream(file);

                    // Write the string to the file
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, fout);
                    fout.flush();
                    fout.close();
                }
                catch (FileNotFoundException e)
                {
                    // TODO Auto-generated catch block
                    constants.logError("Error: " + e.getMessage(), getContext());
                }
                catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    constants.logError("Error: " + e.getMessage(), getContext());
                }

                shareImage(file);
            }
        };

        mMap.snapshot(callback);
    }
}