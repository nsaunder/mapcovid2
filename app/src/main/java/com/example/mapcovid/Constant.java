package com.example.mapcovid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import android.content.Context;

public class Constant {
    private static ArrayList<City> cities;
    private static ArrayList<DayPath> paths;

    public Constant() {
        this.paths = new ArrayList<DayPath>();
    }
    public void set_cities(Context context) {
        try {
            InputStream is = context.getAssets().open("city_data.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            Gson gson = new Gson();
            Type cityList = new TypeToken<ArrayList<City>>(){}.getType();
            this.cities = gson.fromJson(reader,cityList);
            reader.close();
        } catch(Exception e) {
            System.err.println(e);
        }
    }

    public void set_paths(ArrayList<DayPath> paths) {
        this.paths = paths;
    }

    public ArrayList<City> get_cities() {
        return this.cities;
    }

    public City get_city(String city) {
        for(City c: cities) {
            if(c.get_city_name().compareTo(city) == 0) {
                return c;
            }
        }
        return null;
    }

    public ArrayList<DayPath> get_paths() {
        return this.paths;
    }

    public void printPaths() {
        if(this.paths.size() == 0) {
            System.out.println("Didn't travel anywhere");
        } else {
            System.out.println("Paths:");
            for (DayPath day : this.paths) {
                System.out.println("Date: " + day.getDate());
                System.out.print("Cities: ");
                for (PathItem item : day.getPath()) {
                    System.out.print(item.getCity());
                }
                System.out.println();
            }
        }
    }
}
