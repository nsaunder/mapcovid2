package com.example.mapcovid;

import java.util.ArrayList;

public class DayPath {
    private String date;
    private ArrayList<PathItem> path;

    public DayPath(String date, ArrayList<PathItem> path) {
        this.date = date;
        this.path = path;
    }

    public String getDate() {
        return date;
    }

    public ArrayList<PathItem> getPath() {
        return path;
    }

}
