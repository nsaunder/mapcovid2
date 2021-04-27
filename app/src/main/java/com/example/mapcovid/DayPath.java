package com.example.mapcovid;

import java.util.ArrayList;
import java.util.List;

public class DayPath {
    private String date;
    private ArrayList<PathItem> places;

    //empty constructor
    public DayPath() {}

    public DayPath(String date, ArrayList<PathItem> places) {
        this.date = date;
        this.places = places;
    }

    public String getDate() {
        return date;
    }

    public ArrayList<PathItem> getPlaces() {
        return places;
    }
}
