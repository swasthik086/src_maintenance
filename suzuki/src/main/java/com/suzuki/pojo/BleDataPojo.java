package com.suzuki.pojo;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class BleDataPojo extends RealmObject {



    @PrimaryKey
    private int bleId;
    private String deviceName;
    private String deviceMacAddress;
    private  boolean connectStatus;

    private  String writeCharacteristic;

    private  String readCharacteristic;

    private  String  serviceID;

    public String getWriteCharacteristic() {
        return writeCharacteristic;
    }

    public void setWriteCharacteristic(String writeCharacteristic) {
        this.writeCharacteristic = writeCharacteristic;
    }

    public String getReadCharacteristic() {
        return readCharacteristic;
    }

    public void setReadCharacteristic(String readCharacteristic) {
        this.readCharacteristic = readCharacteristic;
    }

    public String getServiceID() {
        return serviceID;
    }

    public void setServiceID(String serviceID) {
        this.serviceID = serviceID;
    }

    public int getBleId() {
        return bleId;
    }

    public void setBleId(int bleId) {
        this.bleId = bleId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceMacAddress() {
        return deviceMacAddress;
    }

    public void setDeviceMacAddress(String deviceMacAddress) {
        this.deviceMacAddress = deviceMacAddress;
    }

    public boolean isConnectStatus() {
        return connectStatus;
    }

    public void setConnectStatus(boolean connectStatus) {
        this.connectStatus = connectStatus;
    }
}
