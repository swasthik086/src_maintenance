package com.suzuki.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

public class CurrentLoc {
//    connect(bleDevice);
    public Location getCurrentLoc(Context context) {
        Location location = null;
        try {
            LocationManager locationManager = (LocationManager) context
                    .getSystemService(Context.LOCATION_SERVICE);

            // getting GPS status
            boolean isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            Log.v("isGPSEnabled", "=" + isGPSEnabled);

            // getting network status
            boolean isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            Log.v("isNetworkEnabled", "=" + isNetworkEnabled);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
                Toast.makeText(context, "No location Provider is available", Toast.LENGTH_SHORT).show();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    Activity#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for Activity#requestPermissions for more details.
                        Toast.makeText(context, "Location Permission Missing", Toast.LENGTH_SHORT).show();
                        return null;
                    }
                }
                if (isNetworkEnabled) {

                    location = locationManager
                            .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    Log.e("location_network", String.valueOf(location));
                    if(location==null){
                        location =  locationManager
                                .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        Log.e("location_gps", String.valueOf(location));

                    }


                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    Location locationGps = locationManager
                            .getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    if (locationGps!=null){
                        location = locationGps;
                        Log.e("location_gps", String.valueOf(location));
                    }
                    if(location==null){
                        location =  locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        Log.e("location_network", String.valueOf(location));

                    }

                }
            }
        } catch (Exception e) {
        }

        return location;
    }


    public boolean nightTime() {
        boolean isNightTime = false;

        Calendar calendar1 = Calendar.getInstance();
        calendar1.set(Calendar.HOUR_OF_DAY,Integer.parseInt("18"));
        calendar1.set(Calendar.MINUTE,Integer.parseInt("00"));
        calendar1.set(Calendar.SECOND,Integer.parseInt("00"));



        Calendar calendar2 = Calendar.getInstance();
        calendar2.set(Calendar.HOUR_OF_DAY,Integer.parseInt("06"));
        calendar2.set(Calendar.MINUTE,Integer.parseInt("00"));
        calendar2.set(Calendar.SECOND,Integer.parseInt("00"));
        calendar2.add(Calendar.DATE, 1);


        Calendar calendar3 = Calendar.getInstance();
        Date x = calendar3.getTime();

        if (x.after(calendar1.getTime())&&x.before(calendar2.getTime())){
            isNightTime = true;
        }


        return isNightTime;
    }

}
