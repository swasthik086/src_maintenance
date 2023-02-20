package com.suzuki.interfaces;

import com.suzuki.pojo.ViaPointLocationRealmModel;

import java.util.Date;

import io.realm.RealmList;

public interface IOnclickFromAdapterToActivityAndFragment {
    public void adapterItemIsClicked(int clickedPositon, String actionToPerform);

    public void adapterItemIsClicked(double lat, double lng, String placeAddress, String placeName);


    public void adapterItemIsClicked(int clickedPositon, String actionToPerform, boolean clicked, String date, Date dateTime, String time, String startLoc, String endLoc, String cuurent_lat, String current_long, String destiny_lat, String destiny_long, String tripName, String rideTime,
                                     String totalDistance, String topspeed, String timelt10, RealmList<ViaPointLocationRealmModel> viaPointRealmList, String startTime,String endTime);


}
