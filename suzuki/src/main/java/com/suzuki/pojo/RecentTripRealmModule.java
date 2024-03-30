package com.suzuki.pojo;


import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;

public class RecentTripRealmModule extends RealmObject implements Comparable<RecentTripRealmModule> {


    private String date;
    private String time;
    private String startlocation;
    private String endlocation;
    boolean favorite;
    private int id;
private  String setETA;
    private String trip_name;
    private String rideTime;
    private String totalDistance;
    private String current_lat;
    private String current_long;
    private String destination_lat;
    private String destination_long;
    private Date dateTime;
    private String startTime;
    private int topSpeed;
    private int ridetimeLt10;

    private String vehicleType;

    private RealmList<ViaPointLocationRealmModel> pointLocationRealmModels = new RealmList<>();

    public RealmList<ViaPointLocationRealmModel> getPointLocationRealmModels() {
        return pointLocationRealmModels;
    }

    public void setPointLocationRealmModels(ViaPointLocationRealmModel pointLocationRealmModels) {
        this.pointLocationRealmModels.add(pointLocationRealmModels);
    }


    public int getTopSpeed() {
        return topSpeed;
    }

    public void setTopSpeed(int topSpeed) {
        this.topSpeed = topSpeed;
    }

    public int getRidetimeLt10() {
        return ridetimeLt10;
    }

    public void setRidetimeLt10(int ridetimeLt10) {
        this.ridetimeLt10 = ridetimeLt10;
    }

    public String getRideTime() {
        return rideTime;
    }

    public void setRideTime(String rideTime) {
        this.rideTime = rideTime;
    }

    public String getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(String totalDistance) {
        this.totalDistance = totalDistance;
    }

    public String getCurrent_lat() {
        return current_lat;
    }

    public void setCurrent_lat(String current_lat) {
        this.current_lat = current_lat;
    }

    public String getCurrent_long() {
        return current_long;
    }

    public void setCurrent_long(String current_long) {
        this.current_long = current_long;
    }

    public String getDestination_lat() {
        return destination_lat;
    }

    public void setDestination_lat(String destination_lat) {
        this.destination_lat = destination_lat;
    }

    public String getDestination_long() {
        return destination_long;
    }

    public void setDestination_long(String destination_long) {
        this.destination_long = destination_long;
    }

    public String getTrip_name() {
        return trip_name;
    }

    public void setTrip_name(String trip_name) {
        this.trip_name = trip_name;
    }


    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date datetime) {
        this.dateTime = datetime;
    }

    @Override
    public int compareTo(RecentTripRealmModule o) {
        return getDateTime().compareTo(o.getDateTime());
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStartlocation() {
        return startlocation;
    }

    public void setStartlocation(String startlocation) {
        this.startlocation = startlocation;
    }

    public String getEndlocation() {
        return endlocation;
    }

    public void setEndlocation(String endlocation) {
        this.endlocation = endlocation;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public void setStartTime(String startTime) {
        this.startTime=startTime;
    }

    public String getstartTime() {
        return startTime;
    }


    public void setETA(String dataEta) {
        this.setETA=dataEta;
    }

    public String getETA() {
        return setETA;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }
}
