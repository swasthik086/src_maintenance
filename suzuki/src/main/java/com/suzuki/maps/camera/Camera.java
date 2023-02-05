package com.suzuki.maps.camera;


import com.mappls.sdk.geojson.Point;

import java.util.List;

public abstract class Camera {

    /**
     * The angle, in degrees, of the camera angle from the nadir (directly facing the Earth).
     * See tilt(float) for details of restrictions on the range of values.
     */
    public abstract double tilt(RouteInformation routeInformation);

    /**
     * Zoom level near the center of the screen. See zoom(float) for the definition of the camera's
     * zoom level.
     */
    public abstract double zoom(RouteInformation routeInformation);

    /**
     * Return a list of route coordinates that should be visible when creating the route's overview.
     * @return
     */
    public abstract List<Point> overview(RouteInformation routeInformation);
}
