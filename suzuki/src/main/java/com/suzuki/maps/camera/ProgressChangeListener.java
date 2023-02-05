package com.suzuki.maps.camera;

import android.location.Location;

public interface ProgressChangeListener {
    void onProgressChange(Location location, RouteInformation routeProgress);
}
