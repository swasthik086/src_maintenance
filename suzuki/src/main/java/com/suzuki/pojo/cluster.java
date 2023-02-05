package com.suzuki.pojo;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class cluster extends RealmObject {

    @PrimaryKey
    private int id;
    String prev_cluster;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPrev_cluster() {
        return prev_cluster;
    }

    public void setPrev_cluster(String prev_cluster) {
        this.prev_cluster = prev_cluster;
    }
}
