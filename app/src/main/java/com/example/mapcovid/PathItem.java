package com.example.mapcovid;

public class PathItem {
    private String time;
    private String city;
    private Double lat;
    private Double lon;

    //default constructor required for calls to DataSnapshot.getValue(PathItem.class)
    public PathItem() { }

    public PathItem(String time, String city, Double lat, Double lon) {
        this.time = time;
        this.city = city;
        this.lat = lat;
        this.lon = lon;
    }

    public String getTime() {
        return time;
    }

    public String getCity() {
        return city;
    }

    public Double getLat() { return lat; }

    public Double getLon() { return lon; }
}
