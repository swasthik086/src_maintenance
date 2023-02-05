package com.suzuki.maps.camera;

import com.mappls.sdk.maps.MapplsMap;
import com.mappls.sdk.maps.camera.CameraUpdate;

class CameraOverviewCancelableCallback implements MapplsMap.CancelableCallback {

    private static final int OVERVIEW_UPDATE_DURATION_IN_MILLIS = 750;

    private CameraUpdate overviewUpdate;
    private MapplsMap mapboxMap;

    CameraOverviewCancelableCallback(CameraUpdate overviewUpdate, MapplsMap mapboxMap) {
        this.overviewUpdate = overviewUpdate;
        this.mapboxMap = mapboxMap;
    }

    @Override
    public void onCancel() {
        // No-op
    }

    @Override
    public void onFinish() {
        mapboxMap.animateCamera(overviewUpdate, OVERVIEW_UPDATE_DURATION_IN_MILLIS);
    }
}
