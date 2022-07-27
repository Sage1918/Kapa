package com.example.kapa;

public class MyRequestModel {

    private String driverId;
    private String passengerId;
    private String name;
    private Double pickupLat;
    private Double pickupLong;

    public MyRequestModel() {
    }

    public MyRequestModel(String driverId, String passengerId, String name, Double pickupLat, Double pickupLong) {
        this.driverId = driverId;
        this.passengerId = passengerId;
        this.name = name;
        this.pickupLat = pickupLat;
        this.pickupLong = pickupLong;
    }

    public String getDriverId() {
        return driverId;
    }

    public String getPassengerId() {
        return passengerId;
    }

    public String getName() {
        return name;
    }

    public Double getPickupLat() {
        return pickupLat;
    }

    public Double getPickupLong() {
        return pickupLong;
    }
}
