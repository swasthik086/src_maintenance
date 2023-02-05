package com.suzuki.pojo;


public class ClusterStatusPktPojo {

    private boolean isValidLength;
    private String clusterData;
    private  byte[] clusterByteData;

    public boolean isValidLength() {
        return isValidLength;
    }

    public void setValidLength(boolean validLength) {
        isValidLength = validLength;
    }

    public String getClusterData() {
        return clusterData;
    }

    public void setClusterData(String clusterData) {
        this.clusterData = clusterData;
    }

    public ClusterStatusPktPojo(String clusterData, byte[] clusterByteData) {
        this.clusterData = clusterData;
        this.clusterByteData = clusterByteData;
    }

    public byte[] getClusterByteData() {
        return clusterByteData;
    }

    public void setClusterByteData(byte[] clusterByteData) {
        this.clusterByteData = clusterByteData;
    }
}
