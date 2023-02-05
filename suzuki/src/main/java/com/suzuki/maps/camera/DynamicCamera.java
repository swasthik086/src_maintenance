package com.suzuki.maps.camera;

import android.location.Location;


import androidx.annotation.NonNull;

import com.mappls.sdk.geojson.Point;
import com.mappls.sdk.maps.MapplsMap;
import com.mappls.sdk.maps.camera.CameraPosition;
import com.mappls.sdk.maps.geometry.LatLng;
import com.mappls.sdk.maps.geometry.LatLngBounds;
import com.mappls.sdk.services.api.directions.models.LegStep;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class DynamicCamera extends SimpleCamera {

    /**
     * 125 seconds remaining is considered a low alert level when
     * navigating along a {@link LegStep}.
     *
     * @since 0.9.0
     */
    public static final int NAVIGATION_LOW_ALERT_DURATION = 125;
    /**
     * 70 seconds remaining is considered a medium alert level when
     * navigating along a {@link LegStep}.
     *
     * @since 0.9.0
     */
    public static final int NAVIGATION_MEDIUM_ALERT_DURATION = 70;
    /**
     * 15 seconds remaining is considered a high alert level when
     * navigating along a {@link LegStep}.
     *
     * @since 0.10.1
     */
    public static final int NAVIGATION_HIGH_ALERT_DURATION = 15;
    private static final double MAX_CAMERA_TILT = 60d;
    private static final double MIN_CAMERA_TILT = 45d;
    private static final double MAX_CAMERA_ZOOM = 16d;
    private static final double MIN_CAMERA_ZOOM = 12d;
    private MapplsMap mapboxMap;
    private LegStep currentStep;
    private boolean hasPassedLowAlertLevel;
    private boolean hasPassedMediumAlertLevel;
    private boolean hasPassedHighAlertLevel;
    private boolean forceUpdateZoom;
    private boolean isShutdown = false;

    public DynamicCamera(@NonNull MapplsMap mapboxMap) {
        this.mapboxMap = mapboxMap;
    }

    @Override
    public double tilt(RouteInformation routeInformation) {
        if (isShutdown) {
            return DEFAULT_TILT;
        }


        if (routeInformation != null) {
            double distanceRemaining = routeInformation.distanceToNextStepRemaining;
            return createTilt(distanceRemaining);
        }
        return super.tilt(routeInformation);
    }

    @Override
    public double zoom(RouteInformation routeInformation) {
        if (isShutdown) {
            return DEFAULT_ZOOM;
        }

        if (validLocationAndProgress(routeInformation) && shouldUpdateZoom(routeInformation)) {
            return createZoom(routeInformation);
        } else if (routeInformation.route != null) {
            return super.zoom(routeInformation);
        }
        return mapboxMap.getCameraPosition().zoom;
    }


    /**
     * Called when the zoom level should force update on the next usage
     * of {@link DynamicCamera#zoom(RouteInformation)}.
     */
    public void forceResetZoomLevel() {
        forceUpdateZoom = true;
    }

    public void clearMap() {
        isShutdown = true;
        mapboxMap = null;
    }

    /**
     * Creates a tilt value based on the distance remaining for the current {@link LegStep}.
     * <p>
     * Checks if the calculated value is within the set min / max bounds.
     *
     * @param distanceRemaining from the current step
     * @return tilt within set min / max bounds
     */
    private double createTilt(double distanceRemaining) {
        double tilt = distanceRemaining / 5;
        if (tilt > MAX_CAMERA_TILT) {
            return MAX_CAMERA_TILT;
        } else if (tilt < MIN_CAMERA_TILT) {
            return MIN_CAMERA_TILT;
        }
        return Math.round(tilt);
    }

    /**
     * Creates a zoom value based on the result of {@link MapboxMap#getCameraForLatLngBounds(LatLngBounds, int[])}.
     * <p>
     * 0 zoom is the world view, while 22 (default max threshold) is the closest you can position
     * the camera to the map.
     *
     * @param routeInformation for current location and progress
     * @return zoom within set min / max bounds
     */
    private double createZoom(RouteInformation routeInformation) {
        CameraPosition position = createCameraPosition(routeInformation.location, routeInformation.nextLegStep);
        if (position.zoom > MAX_CAMERA_ZOOM) {
            return MAX_CAMERA_ZOOM;
        } else if (position.zoom < MIN_CAMERA_ZOOM) {
            return MIN_CAMERA_ZOOM;
        }
        return position.zoom;
    }

    /**
     * Creates a camera position with the current location and upcoming maneuver location.
     * <p>
     * Using {@link MapboxMap#getCameraForLatLngBounds(LatLngBounds, int[])} with a {@link LatLngBounds}
     * that includes the current location and upcoming maneuver location.
     *
     * @param location     for current location
     * @param upComingStep for upcoming maneuver location
     * @return camera position that encompasses both locations
     */
    private CameraPosition createCameraPosition(Location location, LegStep upComingStep) {

        if (upComingStep != null) {
            Point stepManeuverPoint = upComingStep.maneuver().location();

            List<LatLng> latLngs = new ArrayList<>();
            LatLng currentLatLng = new LatLng(location);
            LatLng maneuverLatLng = new LatLng(stepManeuverPoint.latitude(), stepManeuverPoint.longitude());
            latLngs.add(currentLatLng);
            latLngs.add(maneuverLatLng);

            if (latLngs.size() < 1 || currentLatLng.equals(maneuverLatLng)) {
                return mapboxMap.getCameraPosition();
            }

            LatLngBounds cameraBounds = new LatLngBounds.Builder()
                    .includes(latLngs)
                    .build();

            int[] padding = {0, 0, 0, 0};
            return mapboxMap.getCameraForLatLngBounds(cameraBounds, padding);
        }
        return mapboxMap.getCameraPosition();
    }

    private boolean isForceUpdate() {
        if (forceUpdateZoom) {
            forceUpdateZoom = false;
            return true;
        }
        return false;
    }

    /**
     * Looks to see if we have a new step.
     *
     * @param routeInformation provides updated step information
     * @return true if new step, false if not
     */
    private boolean isNewStep(RouteInformation routeInformation) {
        boolean isNewStep = currentStep == null || !currentStep.equals(routeInformation.currentLegStep);
        currentStep = routeInformation.currentLegStep;
        resetAlertLevels(isNewStep);
        return isNewStep;
    }

    private void resetAlertLevels(boolean isNewStep) {
        if (isNewStep) {
            hasPassedLowAlertLevel = false;
            hasPassedMediumAlertLevel = false;
            hasPassedHighAlertLevel = false;
        }
    }

    private boolean validLocationAndProgress(RouteInformation routeInformation) {
        return routeInformation != null && routeInformation.location != null;
    }

    private boolean shouldUpdateZoom(RouteInformation routeInformation) {


        return isForceUpdate()
                || isNewStep(routeInformation)
                || isLowAlert(routeInformation)
                || isMediumAlert(routeInformation)
                || isHighAlert(routeInformation);
    }

    private boolean isLowAlert(RouteInformation progress) {
        if (!hasPassedLowAlertLevel) {
            double durationRemaining = progress.durationToNextStepRemaining;
            double stepDuration = progress.currentLegStep.duration();

            Timber.d("isLowAlert durationRemaining %f", durationRemaining);
            Timber.d("isLowAlert stepDuration %f", stepDuration);

            boolean isLowAlert = durationRemaining < NAVIGATION_LOW_ALERT_DURATION;
            boolean hasValidStepDuration = stepDuration > NAVIGATION_LOW_ALERT_DURATION;
            if (hasValidStepDuration && isLowAlert) {
                hasPassedLowAlertLevel = true;
                return true;
            }
        }
        return false;
    }

    private boolean isMediumAlert(RouteInformation progress) {
        if (!hasPassedMediumAlertLevel) {
            double durationRemaining = progress.durationToNextStepRemaining;
            double stepDuration = progress.currentLegStep.duration();

            Timber.d("isMediumAlert durationRemaining %f", durationRemaining);
            Timber.d("isMediumAlert stepDuration %f", stepDuration);

            boolean isMediumAlert = durationRemaining < NAVIGATION_MEDIUM_ALERT_DURATION;
            boolean hasValidStepDuration = stepDuration > NAVIGATION_MEDIUM_ALERT_DURATION;
            if (hasValidStepDuration && isMediumAlert) {
                hasPassedMediumAlertLevel = true;
                return true;
            }
        }
        return false;
    }

    private boolean isHighAlert(RouteInformation progress) {
        if (!hasPassedHighAlertLevel) {
            double durationRemaining = progress.durationToNextStepRemaining;
            double stepDuration = progress.currentLegStep.duration();


            Timber.d("isHighAlert durationRemaining %f", durationRemaining);
            Timber.d("isHighAlert stepDuration %f", stepDuration);
            boolean isHighAlert = durationRemaining < NAVIGATION_HIGH_ALERT_DURATION;
            boolean hasValidStepDuration = stepDuration > NAVIGATION_HIGH_ALERT_DURATION;
            if (hasValidStepDuration && isHighAlert) {
                hasPassedHighAlertLevel = true;
                return true;
            }
        }
        return false;
    }
}
