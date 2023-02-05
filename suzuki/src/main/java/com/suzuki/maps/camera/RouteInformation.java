package com.suzuki.maps.camera;

import android.location.Location;

import com.mappls.sdk.services.api.directions.models.DirectionsRoute;
import com.mappls.sdk.services.api.directions.models.LegStep;


public class RouteInformation {

    public Location location;
    public DirectionsRoute route;
    public LegStep currentLegStep;
    public LegStep nextLegStep;
    public long distanceRemaining;
    public long distanceToNextStepRemaining;

    public long durationRemaining;
    public long durationToNextStepRemaining;


}
