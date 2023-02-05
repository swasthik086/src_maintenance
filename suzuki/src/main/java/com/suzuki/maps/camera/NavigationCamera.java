package com.suzuki.maps.camera;


import android.location.Location;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import com.mappls.sdk.geojson.Point;
import com.mappls.sdk.maps.MapplsMap;
import com.mappls.sdk.maps.camera.CameraPosition;
import com.mappls.sdk.maps.camera.CameraUpdate;
import com.mappls.sdk.maps.camera.CameraUpdateFactory;
import com.mappls.sdk.maps.geometry.LatLng;
import com.mappls.sdk.maps.geometry.LatLngBounds;
import com.mappls.sdk.maps.location.LocationComponent;
import com.mappls.sdk.maps.location.modes.CameraMode;
import com.mappls.sdk.services.api.directions.models.DirectionsRoute;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;


public class NavigationCamera implements LifecycleObserver {

    /**
     * Maximum duration of the zoom/tilt adjustment animation while tracking.
     */
    public static final long NAVIGATION_MAX_CAMERA_ADJUSTMENT_ANIMATION_DURATION = 1500L;

    /**
     * Minimum duration of the zoom adjustment animation while tracking.
     */
    public static final long NAVIGATION_MIN_CAMERA_ZOOM_ADJUSTMENT_ANIMATION_DURATION = 300L;

    /**
     * Minimum duration of the tilt adjustment animation while tracking.
     */
    public static final long NAVIGATION_MIN_CAMERA_TILT_ADJUSTMENT_ANIMATION_DURATION = 750L;
    /**
     * Camera tracks the user location, with bearing provided by the location update.
     * <p>
     * Equivalent of the {@link CameraMode#TRACKING_GPS}.
     */
    /**
     * Camera tracks the user location, with bearing always set to north (0).
     * <p>
     * Equivalent of the {@link CameraMode#TRACKING_GPS_NORTH}.
     */
    public static final int NAVIGATION_TRACKING_MODE_NORTH = 1;
    public static final int NAVIGATION_TRACKING_MODE_NONE = 2;
    public static final int NAVIGATION_TRACKING_MODE_GPS = 0;

    private static final int ONE_POINT = 1;
    private INavigation navigation;
    private MapplsMap mapboxMap;
    private LocationComponent locationLayer;
    private RouteInformation currentRouteInformation;
    private boolean trackingEnabled;
    private DynamicCamera cameraEngine;
    private RouteInformation currentRouteProgress;
    private ProgressChangeListener progressChangeListener = new ProgressChangeListener() {
        @Override
        public void onProgressChange(Location location, RouteInformation routeProgress) {
            currentRouteProgress = routeProgress;
            if (trackingEnabled) {
                currentRouteInformation = buildRouteInformationFromLocation(location, routeProgress);
                adjustCameraFromLocation(currentRouteInformation);
            }
        }
    };
    @TrackingMode
    private int trackingCameraMode = NAVIGATION_TRACKING_MODE_GPS;

    /**
     * Creates an instance of {@link NavigationCamera}.
     * <p>
     * Camera will start tracking current user location by default.
     *
     * @param mapboxMap     for moving the camera
     * @since 0.15.0
     */
    public NavigationCamera(@NonNull MapplsMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        this.locationLayer = locationLayer;
        setTrackingEnabled(true);
    }


    /**
     * Creates an instance of {@link NavigationCamera}.
     *
     * @param mapboxMap     for moving the camera
     * @param navigation    for listening to location updates
     * @param locationLayer for managing camera mode
     * @since 0.6.0
     */
    public NavigationCamera(@NonNull MapplsMap mapboxMap, @NonNull INavigation navigation,
                            @NonNull LocationComponent locationLayer) {
        this.mapboxMap = mapboxMap;
        this.navigation = navigation;
        this.locationLayer = locationLayer;
        initialize();
    }

    /**
     * Called when beginning navigation with a route.
     *
     * @param route used to update route information
     * @since 0.6.0
     */
    public void start(DirectionsRoute route) {
        if (route != null) {
            currentRouteInformation = buildRouteInformationFromRoute(route);
        }
        navigation.setProgressChangeListener(progressChangeListener);
    }

    /**
     * Called during rotation to update route information.
     *
     * @param location used to update route information
     * @since 0.6.0
     */
    public void resume(Location location) {
        if (location != null) {
            currentRouteInformation = buildRouteInformationFromLocation(location, null);
        }
        navigation.setProgressChangeListener(progressChangeListener);
    }

    /**
     * Setter for whether or not the camera should follow the location.
     *
     * @param trackingEnabled true if should track, false if should not
     * @since 0.6.0
     */
    public void updateCameraTrackingLocation(boolean trackingEnabled) {
        setTrackingEnabled(trackingEnabled);
    }

    /**
     * Updates the {@link TrackingMode} that's going to be used when camera tracking is enabled.
     *
     * @param trackingMode the tracking mode
     * @since 0.21.0
     */
    public void updateCameraTrackingMode(@TrackingMode int trackingMode) {
        trackingCameraMode = trackingMode;
        setCameraMode();
    }

    /**
     * Getter for current state of tracking.
     *
     * @return true if tracking, false if not
     * @since 0.6.0
     */
    public boolean isTrackingEnabled() {
        return trackingEnabled;
    }

    private void setTrackingEnabled(boolean trackingEnabled) {
        this.trackingEnabled = trackingEnabled;
        setCameraMode();
    }

    /**
     * Getter for {@link TrackingMode} that's being used when tracking is enabled.
     *
     * @return tracking mode
     * @since 0.21.0
     */
    @TrackingMode
    public int getCameraTrackingMode() {
        return trackingCameraMode;
    }

    /**
     * Enables tracking and updates zoom/tilt based on the available route information.
     *
     * @since 0.6.0
     */
    public void resetCameraPosition() {
        setTrackingEnabled(true);
        if (currentRouteInformation != null) {

            if (cameraEngine instanceof DynamicCamera) {
                ((DynamicCamera) cameraEngine).forceResetZoomLevel();
            }
            adjustCameraFromLocation(currentRouteInformation);
        }
    }

    public void showRouteOverview(int[] padding) {
        setTrackingEnabled(false);
//    RouteInformation routeInformation = buildRouteInformationFromProgress(currentRouteProgress);
//    animateCameraForRouteOverview(routeInformation, padding);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onStart() {
        if (navigation != null) {
            navigation.setProgressChangeListener(progressChangeListener);
        }
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStop() {
        if (navigation != null) {
            navigation.setProgressChangeListener(null);
        }
    }

//  public void addProgressChangeListener(MapboxNavigation navigation) {
//    this.navigation = navigation;
//    navigation.setCameraEngine(new DynamicCamera(mapboxMap));
//    navigation.addProgressChangeListener(progressChangeListener);
//  }

    private void initialize() {
        cameraEngine = new DynamicCamera(mapboxMap);
        setTrackingEnabled(true);
    }

    /**
     * Creates a camera position based on the given route.
     * <p>
     * From the {@link DirectionsRoute}, an initial bearing and target position are created.
     * Then using a preset tilt and zoom (based on screen orientation), a {@link CameraPosition} is built.
     *
     * @param route used to build the camera position
     * @return camera position to be animated to
     */
    @NonNull
    private RouteInformation buildRouteInformationFromRoute(DirectionsRoute route) {
        RouteInformation routeInformation = new RouteInformation();
        routeInformation.route = route;

        return routeInformation;
    }

    /**
     * Creates a camera position based on the given location.
     * <p>
     * From the {@link Location}, a target position is created.
     * Then using a preset tilt and zoom (based on screen orientation), a {@link CameraPosition} is built.
     *
     * @param location used to build the camera position
     * @return camera position to be animated to
     */
    @NonNull
    private RouteInformation buildRouteInformationFromLocation(Location location, RouteInformation routeProgress) {
        if (routeProgress == null)
            routeProgress = new RouteInformation();
        routeProgress.location = location;
        return routeProgress;
    }

    private void animateCameraForRouteOverview(RouteInformation routeInformation, int[] padding) {

        List<Point> routePoints = cameraEngine.overview(routeInformation);
        if (!routePoints.isEmpty()) {
            animateMapboxMapForRouteOverview(padding, routePoints);
        }
    }

    private void animateMapboxMapForRouteOverview(int[] padding, List<Point> routePoints) {
        if (routePoints.size() <= ONE_POINT) {
            return;
        }
        CameraUpdate resetUpdate = buildResetCameraUpdate();
        final CameraUpdate overviewUpdate = buildOverviewCameraUpdate(padding, routePoints);
        mapboxMap.animateCamera(resetUpdate, 150,
                new CameraOverviewCancelableCallback(overviewUpdate, mapboxMap)
        );
    }

    @NonNull
    private CameraUpdate buildResetCameraUpdate() {
        CameraPosition resetPosition = new CameraPosition.Builder().tilt(0).bearing(0).build();
        return CameraUpdateFactory.newCameraPosition(resetPosition);
    }

    @NonNull
    private CameraUpdate buildOverviewCameraUpdate(int[] padding, List<Point> routePoints) {
        final LatLngBounds routeBounds = convertRoutePointsToLatLngBounds(routePoints);
        return CameraUpdateFactory.newLatLngBounds(
                routeBounds, padding[0], padding[1], padding[2], padding[3]
        );
    }

    private LatLngBounds convertRoutePointsToLatLngBounds(List<Point> routePoints) {
        List<LatLng> latLngs = new ArrayList<>();
        for (Point routePoint : routePoints) {
            latLngs.add(new LatLng(routePoint.latitude(), routePoint.longitude()));
        }
        return new LatLngBounds.Builder()
                .includes(latLngs)
                .build();
    }

    private void setCameraMode() {
        @CameraMode.Mode int mode;
        if (trackingEnabled) {
            if (trackingCameraMode == NAVIGATION_TRACKING_MODE_GPS) {
                mode = CameraMode.TRACKING_GPS;
            } else if (trackingCameraMode == NAVIGATION_TRACKING_MODE_NORTH) {
                mode = CameraMode.TRACKING_GPS_NORTH;
            } else {
                mode = CameraMode.NONE;
                Timber.e("Using unsupported camera tracking mode - %d.", trackingCameraMode);
            }
        } else {
            mode = CameraMode.NONE;
        }

        if (mode != locationLayer.getCameraMode()) {
            locationLayer.setCameraMode(mode);
        }
    }

    /**
     * Updates the camera's zoom and tilt while tracking.
     *
     * @param routeInformation with location data
     */
    private void adjustCameraFromLocation(RouteInformation routeInformation) {


        float tilt = (float) cameraEngine.tilt(routeInformation);
        double zoom = cameraEngine.zoom(routeInformation);

        Timber.d("Zoom= %f", zoom);

    /*locationLayer.zoomWhileTracking(zoom, getZoomAnimationDuration(zoom));
    locationLayer.tiltWhileTracking(tilt, getTiltAnimationDuration(tilt));*/
    }

    private long getZoomAnimationDuration(double zoom) {
        double zoomDiff = Math.abs(mapboxMap.getCameraPosition().zoom - zoom);
        return (long) MathUtils.clamp(
                500 * zoomDiff,
                NAVIGATION_MIN_CAMERA_ZOOM_ADJUSTMENT_ANIMATION_DURATION,
                NAVIGATION_MAX_CAMERA_ADJUSTMENT_ANIMATION_DURATION);
    }

    private long getTiltAnimationDuration(double tilt) {
        double tiltDiff = Math.abs(mapboxMap.getCameraPosition().tilt - tilt);
        return (long) MathUtils.clamp(
                500 * tiltDiff,
                NAVIGATION_MIN_CAMERA_TILT_ADJUSTMENT_ANIMATION_DURATION,
                NAVIGATION_MAX_CAMERA_ADJUSTMENT_ANIMATION_DURATION);
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({NAVIGATION_TRACKING_MODE_GPS, NAVIGATION_TRACKING_MODE_NORTH})
    public @interface TrackingMode {
    }
}
