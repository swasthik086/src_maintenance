package com.suzuki.interfaces;

import android.location.Location;

public interface MapDragListInterface {
    public void dragItem(int id, String name);

    void onLocationChanged(Location location);
}
