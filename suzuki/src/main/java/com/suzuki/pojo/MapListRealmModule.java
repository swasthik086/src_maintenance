package com.suzuki.pojo;

import io.realm.RealmObject;

public class MapListRealmModule extends RealmObject {


    private String name;
    int id;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
