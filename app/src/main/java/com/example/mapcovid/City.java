package com.example.mapcovid;

public class City {
    private String city_name;
    private double center_lat;
    private double center_long;
    private double radius;
    private int new_cases;
    private int new_deaths;
    private int total_cases;
    private int total_deaths;

    public City(String city_name, double center_lat, double center_long, double radius, int new_cases, int new_deaths, int total_cases, int total_deaths) {
        this.city_name = city_name;
        this.center_lat = center_lat;
        this.center_long = center_long;
        this.radius = radius;
        this.new_cases = new_cases;
        this.new_deaths = new_deaths;
        this.total_cases = total_cases;
        this.total_deaths = total_deaths;
    }

    //getters to retrieve data
    public String get_city_name() {
        return city_name;
    }

    public double get_center_lat() {
        return center_lat;
    }

    public double get_center_long() {
        return center_long;
    }

    public double get_radius() {
        return radius;
    }

    public int get_new_cases() { return new_cases; }

    public int get_new_deaths() { return new_deaths; }

    public int get_total_cases() { return total_cases; }

    public int get_total_deaths() { return total_deaths; }

    public String to_String() {
        String msg = "city_name: " + city_name + ", center_lat: " + center_lat + ", center_long: " + center_long + ", radius: " + radius
                + ", new_cases: " + new_cases + ", new_deaths: " + new_deaths + ", total_cases: " + total_cases + ", total_deaths: " + total_deaths;
        return msg;
    }

    public String city_notification_message() {
        String msg = "New Cases: " + new_cases + "\n" +
                "New Deaths: " + new_deaths + "\n" +
                "Total Cases: " + total_cases + "\n" +
                "Total Deaths: " + total_deaths;
        return msg;
    }

}
