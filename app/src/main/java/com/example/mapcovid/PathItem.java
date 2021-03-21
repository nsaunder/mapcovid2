package com.example.mapcovid;

public class PathItem {
    private String time;
    private String city;

    //default constructor required for calls to DataSnapshot.getValue(DayPath.class)
    public PathItem() { }

    public PathItem(String time, String city) {
        this.time = time;
        this.city = city;
    }

    public String getTime() {
        return time;
    }

    public String getCity() {
        return city;
    }
}
