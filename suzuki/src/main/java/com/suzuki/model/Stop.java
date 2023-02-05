package com.suzuki.model;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.mappls.sdk.maps.geometry.LatLng;
import com.suzuki.utils.Validator;

public class Stop implements Parcelable {

    public static final Creator<Stop> CREATOR = new Creator<Stop>() {
        @Override
        public Stop createFromParcel(Parcel source) {
            return new Stop(source);
        }

        @Override
        public Stop[] newArray(int size) {
            return new Stop[size];
        }
    };


    public static String TYPE_MY_LOCATION = "Your current location";
    public static String TYPE_STOP = "Stop";

    private static String NAME_RE = "[^\\p{Alnum}\\(\\)\\s]";

    private String mName;
    private String mAddress;
    private Location mLocation;
    private Location mEntryLocation;
    private int mSiteId;
    private String placeId;
    private boolean changeable = true;
    private String type = TYPE_STOP;

    public Stop() {
    }

    public Stop(String name) {
        setName(name);
    }

    public Stop(String name, Location location) {
        setName(name);
        mLocation = location;
    }

    public Stop(String name, double latitude, double longitude) {
        setName(name);
        mLocation = new Location("MapmyIndia");
        mLocation.setLatitude(latitude);
        mLocation.setLongitude(longitude);
    }

    /**
     * Create a new Stop that is a copy of the given Stop.
     *
     * @param stop the stop
     */
    public Stop(Stop stop) {
        mName = stop.getName();
        mLocation = stop.getLocation();
        mSiteId = stop.getSiteId();
        mAddress = stop.getmAddress();
        placeId = stop.getPlaceId();
    }

    protected Stop(Parcel in) {
        this.mName = in.readString();
        this.mAddress = in.readString();
        this.mLocation = in.readParcelable(Location.class.getClassLoader());
        this.mEntryLocation = in.readParcelable(Location.class.getClassLoader());
        this.mSiteId = in.readInt();
        this.placeId = in.readString();
        this.changeable = in.readByte() != 0;
        this.type = in.readString();
    }

    public static boolean looksValid(String name) {
        if (TextUtils.isEmpty(name) || TextUtils.getTrimmedLength(name) == 0) {
            return false;
        }
        return !name.matches(NAME_RE);
    }

    public void refreshModel(Stop stop) {
        mName = stop.getName();
        mLocation = stop.getLocation();
        mSiteId = stop.getSiteId();
        mAddress = stop.getmAddress();
        placeId = stop.getPlaceId();
        mEntryLocation = stop.getELocation();
        type = stop.getType();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isChangeable() {
        return changeable;
    }

    public void setChangeable(boolean changeable) {
        this.changeable = changeable;
    }

    public LatLng getGeoPoint() {
        try {
            if (mEntryLocation != null && mEntryLocation.getLatitude() != 0 && mEntryLocation.getLongitude() != 0) {
                return new LatLng(mEntryLocation.getLatitude(), mEntryLocation.getLongitude());
            } else {
                return new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new LatLng(0.0, 0.0);
        }

    }

    public LatLng getLatLng() {
        try {
            return new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        } catch (Exception ex) {
            return new LatLng(0.0, 0.0);
        }

    }

    public LatLng getELatLng() {
        try {
            return new LatLng(mEntryLocation.getLatitude(), mEntryLocation.getLongitude());
        } catch (Exception ex) {
            return new LatLng(0.0, 0.0);
        }

    }

    @Override
    public boolean equals(Object o) {
        try {
            Stop lhs = (Stop) o;
            return lhs.getLocation().distanceTo(this.getLocation()) < 5 || (!TextUtils.isEmpty(lhs.getPlaceId()) &&
                    !TextUtils.isEmpty(getPlaceId()) && lhs.getPlaceId().equalsIgnoreCase(getPlaceId()));
        } catch (Exception e) {
            return false;
        }
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {


        mName = name/*.trim().replaceAll(NAME_RE, "")*/;

    }

    public boolean hasName() {
        return !TextUtils.isEmpty(mName);
    }

    public void setLocation(int latitudeE6, int longitudeE6) {
        mLocation = new Location("MapmyIndia");
        mLocation.setLatitude(latitudeE6 / 1E6);
        mLocation.setLongitude(longitudeE6 / 1E6);
    }

    public void setLocation(double latitude, double longitude) {
        mLocation = new Location("MapmyIndia");
        mLocation.setLatitude(latitude);
        mLocation.setLongitude(longitude);
    }

    public void setELocation(double eLatitude, double eLongitude) {
        mEntryLocation = new Location("EMapmyIndia");
        mEntryLocation.setLatitude(eLatitude);
        mEntryLocation.setLongitude(eLongitude);
    }

    public Location getELocation() {
        return mEntryLocation;
    }

    public Location getLocation() {
        return mLocation;
    }

    public void setLocation(Location location) {
        mLocation = location;
    }

    public boolean isMyLocation() {
        return hasName() && type.equals(TYPE_MY_LOCATION);
    }

    public int getSiteId() {
        return mSiteId;
    }

    public void setSiteId(int siteId) {
        mSiteId = siteId;
    }

    public boolean looksValid() {
        return hasName();
    }

    public String getmAddress() {
        return mAddress;
    }

    public void setmAddress(String mAddress) {
        this.mAddress = mAddress;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mName);
        dest.writeString(this.mAddress);
        dest.writeParcelable(this.mLocation, flags);
        dest.writeParcelable(this.mEntryLocation, flags);
        dest.writeInt(this.mSiteId);
        dest.writeString(this.placeId);
        dest.writeByte(this.changeable ? (byte) 1 : (byte) 0);
        dest.writeString(this.type);
    }

    /**
     * It returns a valid display name.
     * It should always return blank or null if no display name condition matches
     */
    public String getDisplayName() {
        return (!TextUtils.isEmpty(getName())) ? getName() : (!TextUtils.isEmpty(getmAddress()))
                ? getmAddress() : (Validator.isValidLatLng(getLatLng().getLatitude(),
                getLatLng().getLongitude())) ? (getLatLng().getLatitude() + ", " + getLatLng().getLongitude()) : "";
    }

    /**
     * A stop is a valid location only if it either has an eloc or a location
     */
    public boolean isValid() {
        return Validator.isValidLatLng(getLatLng()) || Validator.isValidEloc(getPlaceId());
    }

    @Override
    public String toString() {
        return placeId;
    }
}
