package com.suzuki.pojo;

public class ViaPointPojo {
    String address;
    int color;

    public ViaPointPojo(String address, int color) {
        this.address = address;
        this.color = color;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
