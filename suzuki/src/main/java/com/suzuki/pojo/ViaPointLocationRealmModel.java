package com.suzuki.pojo;

import io.realm.RealmObject;

public class ViaPointLocationRealmModel extends RealmObject {

    public ViaPointLocationRealmModel(){}

    private double latitude;
    private double longitude;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
