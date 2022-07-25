package com.example.kapa;

public class DriverModel {
    private String user_id;
    private String user_name;

    private double from_latitude;

    private double from_longitude;

    private double to_latitude;
    private double to_longitude;
    private int numberOfSeats;
    private int time;
    private int date;

    public DriverModel() {
    }
    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public void setFrom_latitude(double from_latitude) {
        this.from_latitude = from_latitude;
    }

    public void setFrom_longitude(double from_longitude) {
        this.from_longitude = from_longitude;
    }

    public void setTo_latitude(double to_latitude) {
        this.to_latitude = to_latitude;
    }

    public void setTo_longitude(double to_longitude) {
        this.to_longitude = to_longitude;
    }

    public void setNumberOfSeats(int numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_id() {
        return user_id;
    }

    public double getFrom_latitude() {
        return from_latitude;
    }

    public double getFrom_longitude() {
        return from_longitude;
    }

    public double getTo_latitude() {
        return to_latitude;
    }

    public double getTo_longitude() {
        return to_longitude;
    }

    public int getNumberOfSeats() {
        return numberOfSeats;
    }

    public int getTime() {
        return time;
    }

    public int getDate() {
        return date;
    }

    public String getUser_name() {
        return user_name;
    }
}
