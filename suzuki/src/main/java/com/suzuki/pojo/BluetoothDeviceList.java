package com.suzuki.pojo;

public class BluetoothDeviceList {

    private String name;
    private String macAddress;
    private Boolean checked;

    public BluetoothDeviceList(String name) {
        this.name = name;
    }

    public BluetoothDeviceList() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public Boolean getChecked() {
        return checked;
    }

    public void setChecked(Boolean checked) {
        this.checked = checked;
    }
}