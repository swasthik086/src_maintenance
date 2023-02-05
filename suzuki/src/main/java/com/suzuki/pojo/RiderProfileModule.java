package com.suzuki.pojo;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RiderProfileModule extends RealmObject {
    private String name;
    private String location;
    private String bike;
    private String imagepath;
    private String bikeModel;
    private int userSelectedImage;
    private int rideCounts;
    private int fuelBars;
    private String Odometer;
    private String tripA;
    private String tripB;
    private String prev_cluster="";

    @PrimaryKey
    private int id;

    public void setPrev_cluster(String prev_cluster) {
        this.prev_cluster = prev_cluster;
    }

    public String getPrev_cluster() {
        return prev_cluster;
    }

    public int getFuelBars() {
        return fuelBars;
    }

    public void setFuelBars(int fuelBars) { this.fuelBars = fuelBars; }

    public String getOdometer() {
        return Odometer;
    }

    public void setOdometer(String odometer) {
        Odometer = odometer;
    }

    public String getTripA() {
        return tripA;
    }

    public void setTripA(String tripA) {
        this.tripA = tripA;
    }

    public String getTripB() {
        return tripB;
    }

    public void setTripB(String tripB) {
        this.tripB = tripB;
    }

    public int getRideCounts() {
        return rideCounts;
    }

    public void setRideCounts(int rideCounts) {
        this.rideCounts = rideCounts;
    }

    public int getUserSelectedImage() {
        return userSelectedImage;
    }

    public void setUserSelectedImage(int userSelectedImage) {
        this.userSelectedImage = userSelectedImage;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBikeModel() {
        return bikeModel;
    }

    public void setBikeModel(String bikeModel) {
        this.bikeModel = bikeModel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getBike() {
        return bike;
    }

    public void setBike(String bike) {
        this.bike = bike;
    }

    public String getImagepath() {
        return imagepath;
    }

    public void setImagepath(String imagepath) {
        this.imagepath = imagepath;
    }


}