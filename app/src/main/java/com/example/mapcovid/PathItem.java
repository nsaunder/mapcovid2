package com.example.mapcovid;

public class PathItem {
    private String time;
    private String city;

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
