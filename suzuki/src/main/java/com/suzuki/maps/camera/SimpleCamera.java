package com.suzuki.maps.camera;

import com.mappls.sdk.geojson.LineString;
import com.mappls.sdk.geojson.Point;
import com.mappls.sdk.services.api.directions.models.DirectionsRoute;
import com.mappls.sdk.services.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimpleCamera extends Camera {

    protected static final int DEFAULT_TILT = 50;
    protected static final double DEFAULT_ZOOM = 15d;

    private List<Point> routeCoordinates = new ArrayList<>();
    private DirectionsRoute initialRoute;

    @Override
    public double tilt(RouteInformation routeInformation) {
        return DEFAULT_TILT;
    }

    @Override
    public double zoom(RouteInformation routeInformation) {
        return DEFAULT_ZOOM;
    }

    @Override
    public List<Point> overview(RouteInformation routeInformation) {
        boolean invalidCoordinates = routeCoordinates == null || routeCoordinates.isEmpty();
        if (invalidCoordinates) {
            buildRouteCoordinatesFromRouteData(routeInformation);
        }
        return routeCoordinates;
    }

    private void buildRouteCoordinatesFromRouteData(RouteInformation routeInformation) {

        setupLineStringAndBearing(routeInformation.route);

    }

    private void setupLineStringAndBearing(DirectionsRoute route) {
        if (route.equals(initialRoute)) {
            return; //no need to recalculate these values
        }
        initialRoute = route;
        routeCoordinates = generateRouteCoordinates(route);
    }

    private List<Point> generateRouteCoordinates(DirectionsRoute route) {
        if (route == null) {
            return Collections.emptyList();
        }
        LineString lineString = LineString.fromPolyline(route.geometry(), Constants.PRECISION_6);
        return lineString.coordinates();
    }
}
