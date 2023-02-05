package com.suzuki.pojo;

import androidx.room.PrimaryKey;

import io.realm.RealmObject;

public class FirstTime extends RealmObject {
    @PrimaryKey
    int id;
    int firstTime;

    public int getFirstTime() {
        return firstTime;
    }

    public void setFirstTime(int firstTime) {
        this.firstTime = firstTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
