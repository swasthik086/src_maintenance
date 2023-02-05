package com.suzuki.pojo;


public class BleConnectedToBikePojo {

    private boolean connection;

    public BleConnectedToBikePojo(boolean connection) {
        this.connection = connection;
    }


    public boolean isConnection() {
        return connection;
    }

    public void setConnection(boolean connection) {
        this.connection = connection;
    }
}
