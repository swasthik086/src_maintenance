package com.suzuki.maps.traffic;
/**
 * Created by Sahil Sharma on 21-Apr-17.
 */

import android.os.Parcel;

/**
 * A bounding box (usually shortened to bbox) is an area defined by two longitudes and two latitudes, where:
 * <p>
 * Latitude is a decimal number between -90.0 and 90.0.
 * Longitude is a decimal number between -180.0 and 180.0.
 * <p>
 * They usually follow the standard format of:
 * <p>
 * bbox = left,bottom,right,top
 * bbox = min Longitude , min Latitude , max Longitude , max Latitude
 */
public class BoundingBox {
    private double mMinX;
    private double mMinY;
    private double mMaxX;
    private double mMaxY;

    /**
     * @param minX - minimum Longitude (West ie.Screen Left)
     * @param minY - minimum Latitude (South ie.Screen Bottom)
     * @param maxX - maximum Longitude (East ie.Screen Right)
     * @param maxY - maximum Latitude (North ie.Screen Top)
     */
    public BoundingBox(double minY, double minX, double maxY, double maxX) {
        this.mMinX = minX;
        this.mMinY = minY;
        this.mMaxX = maxX;
        this.mMaxY = maxY;
    }

    protected BoundingBox(Parcel in) {
        this.mMinX = in.readDouble();
        this.mMinY = in.readDouble();
        this.mMaxX = in.readDouble();
        this.mMaxY = in.readDouble();
    }

    /**
     * @return - minimum Longitude (West ie.Screen Left)
     */
    public double getMinX() {
        return mMinX;
    }

    /**
     * @param mMinX - minimum Longitude (West ie.Screen Left)
     */
    public void setMinX(double mMinX) {
        this.mMinX = mMinX;
    }

    /**
     * @return - minimum Latitude (South ie.Screen Bottom)
     */
    public double getMinY() {
        return mMinY;
    }

    /**
     * @param mMinY - minimum Latitude (South ie.Screen Bottom)
     */
    public void setMinY(double mMinY) {
        this.mMinY = mMinY;
    }

    /**
     * @return - maximum Longitude (East ie.Screen Right)
     */
    public double getMaxX() {
        return mMaxX;
    }

    /**
     * @param mMaxX - maximum Longitude (East ie.Screen Right)
     */
    public void setMaxX(double mMaxX) {
        this.mMaxX = mMaxX;
    }

    /**
     * @return - maximum Latitude (West ie.Screen Top)
     */
    public double getMaxY() {
        return mMaxY;
    }

    /**
     * @param mMaxY - maximum Latitude (West ie.Screen Top)
     */
    public void setMaxY(double mMaxY) {
        this.mMaxY = mMaxY;
    }

    /**
     * output:  minLat,minLng|maxLat,maxLng
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(mMinY);
        sb.append(",");
        sb.append(mMinX);
        sb.append("|");
        sb.append(mMaxY);
        sb.append(",");
        sb.append(mMaxX);
        return sb.toString();
    }
}
