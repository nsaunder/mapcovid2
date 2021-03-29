package com.example.mapcovid;

import androidx.fragment.app.FragmentManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.mapcovid.TestingFragment;
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

public class addTestingMarkersUnitTest {
    TestingFragment map = new TestingFragment();

    @Test
    public void mapTest(){

        List<TestingFragment.TestingLocation> testingLocations = new ArrayList<>();
        List<WeightedLatLng> latLngs = new ArrayList<>();
        try {
            testingLocations = map.readItems("test_locations-1.json");
        } catch (Exception e) {
            Assert.fail();
        }

        HashMap<String, TestingFragment.TestingLocation> hm = new HashMap<>();

        List<LatLng> am = map.addTestingMarkers(testingLocations, hm);

        assertNotNull(am);
        for(int i = 0; i < am.size(); i++){
            assertEquals(am.get(i).longitude, testingLocations.get(i).getPosition().longitude, .001);
            assertEquals(am.get(i).latitude, testingLocations.get(i).getPosition().latitude, .001);
        }
    }
}
