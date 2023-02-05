package com.suzuki.pojo;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class LastParkedLocationRealmModule extends RealmObject {

    private Double lat;
    private Double lng;
    @PrimaryKey
    private int id;

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
