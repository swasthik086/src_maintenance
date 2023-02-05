package com.suzuki.maps.camera;


import androidx.annotation.Nullable;

public interface INavigation {
    void onETARefreshed(String s);

    void setProgressChangeListener(@Nullable ProgressChangeListener progressChangeListener);
}
