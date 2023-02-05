package com.suzuki.utils;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.location.Location;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.mappls.sdk.maps.location.engine.LocationEngine;
import com.mappls.sdk.maps.location.engine.LocationEngineCallback;
import com.mappls.sdk.maps.location.engine.LocationEngineRequest;
import com.mappls.sdk.maps.location.engine.LocationEngineResult;

/**
 * LocationEngine that provides mocked locations simulating GPS updates
 */
public class NavigationLocationEngine implements LocationEngine {
    private Location location = null;

    public NavigationLocationEngine() {
        super();
    }


//    public void updateLocation(Location location) {
//        if (location == null)
//            return;
//        for (LocationEngineListener listener : locationListeners) {
//            listener.onLocationChanged(location);
//        }
//        this.location = location;
//    }




    @Override
    public void getLastLocation(@NonNull LocationEngineCallback<LocationEngineResult> locationEngineCallback) throws SecurityException {

    }

    @Override
    public void requestLocationUpdates(@NonNull LocationEngineRequest locationEngineRequest, @NonNull LocationEngineCallback<LocationEngineResult> locationEngineCallback, @Nullable Looper looper) throws SecurityException {

    }

    @Override
    public void requestLocationUpdates(@NonNull LocationEngineRequest locationEngineRequest, PendingIntent pendingIntent) throws SecurityException {

    }

    @Override
    public void removeLocationUpdates(@NonNull LocationEngineCallback<LocationEngineResult> locationEngineCallback) {

    }

    @Override
    public void removeLocationUpdates(PendingIntent pendingIntent) {

    }
}