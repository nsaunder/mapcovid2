package com.example.mapcovid;

import androidx.fragment.app.FragmentManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.mapcovid.MapsFragment;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.internal.IGoogleMapDelegate;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.heatmaps.WeightedLatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.google.android.material.internal.ContextUtils.getActivity;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

public class addCityMarkersUnitTest {
    MapsFragment map = new MapsFragment();

    @Test
    public void mapTest(){

        List<City> cities = new ArrayList<>();
        List<WeightedLatLng> latLngs = new ArrayList<>();
        try {
            cities = map.readItems("city_data.json");
        } catch (Exception e) {
            Assert.fail();
        }

        HashMap<String, City> hm = new HashMap<>();

        List<LatLng> am = map.addCityMarkers(cities, latLngs, hm);

        assertNotNull(am);
        for(int i = 0; i < am.size(); i++){
            assertEquals(am.get(i).longitude, cities.get(i).get_center_long(), .001);
            assertEquals(am.get(i).latitude, cities.get(i).get_center_lat(), .001);
        }
    }
}
