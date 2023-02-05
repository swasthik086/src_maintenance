package com.suzuki.pojo;

public class RecentTripPojo {
    private String startLocation;
    private int tripTime;
    private String DateTime;
    private String endLocation;

    public RecentTripPojo(String startLocation) {
        this.startLocation = startLocation;

    }


    public String getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(String startLocation) {
        this.startLocation = startLocation;
    }

    public int getTripTime() {
        return tripTime;
    }

    public void setTripTime(int tripTime) {
        this.tripTime = tripTime;
    }

    public String getDateTime() {
        return DateTime;
    }

    public void setDateTime(String dateTime) {
        DateTime = dateTime;
    }

    public String getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(String endLocation) {
        this.endLocation = endLocation;
    }
}