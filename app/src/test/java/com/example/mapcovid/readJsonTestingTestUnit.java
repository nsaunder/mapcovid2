package com.example.mapcovid;

import com.example.mapcovid.TestingFragment;

import org.json.JSONException;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import org.mockito.Matchers;
import org.powermock.core.classloader.annotations.PrepareForTest;

public class readJsonTestingTestUnit {

    TestingFragment testing = new TestingFragment();
    List<TestingFragment.TestingLocation> testLocations = new ArrayList<>();

    public List<TestingFragment.TestingLocation> getJson(){
        List<TestingFragment.TestingLocation> res = new ArrayList<>();

        try {

            res = testing.readItems("test_locations-1.json");

        } catch (JSONException e) {
            System.out.println("Here");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Here2");
            e.printStackTrace();
        }
        return res;
    }

    @Test
    public void testingReadJson() {

        testLocations = getJson();

        assertNotNull(testLocations);
        assertNotEquals(testLocations.size(), 0);

    }

    public void testingCorrectValues(){
        testLocations = getJson();

        assertEquals(testLocations.get(0).getName(), "Edendale Library - Echo Park");
        assertEquals(testLocations.get(testLocations.size()-1).getName(), "Wesley Health Centers - Downey");

        assertEquals(34.078850, testLocations.get(0).getPosition().latitude, .001);
        assertEquals(-118.262250, testLocations.get(0).getPosition().longitude, .001);
        assertEquals(33.93729, testLocations.get(testLocations.size()-1).getPosition().latitude, .001);
        assertEquals(-118.12892, testLocations.get(testLocations.size()-1).getPosition().longitude, .001);
        assertEquals(false, testLocations.get(0).getDriveUp());
        assertEquals(testLocations.get(testLocations.size()-1).getWalkUp(), true);
    }

}
