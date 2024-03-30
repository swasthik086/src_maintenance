package com.suzuki.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;


import com.mappls.sdk.geojson.LineString;
import com.mappls.sdk.geojson.Point;
import com.mappls.sdk.geojson.utils.PolylineUtils;
import com.mappls.sdk.maps.MapView;
import com.mappls.sdk.maps.MapplsMap;

import com.mappls.sdk.maps.OnMapReadyCallback;
import com.mappls.sdk.maps.Style;
import com.mappls.sdk.maps.annotations.Icon;
import com.mappls.sdk.maps.annotations.IconFactory;
import com.mappls.sdk.maps.annotations.Marker;
import com.mappls.sdk.maps.annotations.MarkerOptions;
import com.mappls.sdk.maps.annotations.PolylineOptions;
import com.mappls.sdk.maps.camera.CameraPosition;
import com.mappls.sdk.maps.camera.CameraUpdateFactory;
import com.mappls.sdk.maps.geometry.LatLng;
import com.mappls.sdk.maps.geometry.LatLngBounds;
import com.mappls.sdk.maps.location.LocationComponent;
import com.mappls.sdk.maps.location.LocationComponentActivationOptions;
import com.mappls.sdk.maps.location.LocationComponentOptions;
import com.mappls.sdk.maps.location.engine.LocationEngine;
import com.mappls.sdk.maps.location.engine.LocationEngineCallback;
import com.mappls.sdk.maps.location.engine.LocationEngineRequest;
import com.mappls.sdk.maps.location.engine.LocationEngineResult;
import com.mappls.sdk.maps.location.modes.CameraMode;
import com.mappls.sdk.maps.location.modes.RenderMode;
import com.mappls.sdk.maps.location.permissions.PermissionsListener;
import com.mappls.sdk.maps.location.permissions.PermissionsManager;
import com.mappls.sdk.navigation.MapplsNavigationHelper;
import com.mappls.sdk.navigation.NavLocation;
import com.mappls.sdk.navigation.NavigationFormatter;
import com.mappls.sdk.navigation.data.WayPoint;
import com.mappls.sdk.navigation.iface.IStopSession;
import com.mappls.sdk.navigation.model.NavigationResponse;
import com.mappls.sdk.navigation.util.ErrorType;
import com.mappls.sdk.services.api.OnResponseCallback;
import com.mappls.sdk.services.api.autosuggest.model.ELocation;
import com.mappls.sdk.services.api.directions.DirectionsCriteria;
import com.mappls.sdk.services.api.directions.MapplsDirectionManager;
import com.mappls.sdk.services.api.directions.MapplsDirections;
import com.mappls.sdk.services.api.directions.models.DirectionsResponse;
import com.mappls.sdk.services.api.directions.models.DirectionsRoute;
import com.mappls.sdk.services.api.directions.models.DirectionsWaypoint;
import com.mappls.sdk.services.api.directions.models.LegStep;
import com.mappls.sdk.services.utils.Constants;
import com.suzuki.R;
import com.suzuki.application.SuzukiApplication;
import com.suzuki.base.BaseActivity;
import com.suzuki.fragment.MapMainFragment;
import com.suzuki.interfaces.StartDragListener;
import com.suzuki.maps.plugins.BearingIconPlugin;
import com.suzuki.maps.plugins.DashedPolylinePlugin;
import com.suzuki.maps.plugins.DirectionPolylinePlugin;
import com.suzuki.maps.traffic.TrafficPlugin;
import com.suzuki.pojo.LastParkedLocationRealmModule;
import com.suzuki.utils.Common;
import com.suzuki.utils.CurrentLoc;
import com.suzuki.utils.DataRequestManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.realm.Realm;
import timber.log.Timber;

import static com.suzuki.activity.HomeScreenActivity.TOAST_DURATION;
import static com.suzuki.activity.RouteNearByActivity.dpToPx;

//import static com.suzuki.fragment.DashboardFragment.logData;
import static com.suzuki.fragment.MapMainFragment.eLocation;
import static com.suzuki.utils.Common.BikeBleName;


public class LastParkedLocationActivity extends BaseActivity implements OnMapReadyCallback, MapplsMap.OnMapLongClickListener,
        PermissionsListener, StartDragListener {

    public MapView mapView;
    private DirectionPolylinePlugin directionPolylinePlugin;

    LocationComponent locationComponent;
    private String profile = DirectionsCriteria.PROFILE_DRIVING;
    private static final long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private static final long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;

    private boolean onLocationChecked = false;

    private static final int REQUEST_NAVIGATION = 1;
    LocationEngineCallback<LocationEngineResult> locationEngineCallback = new LocationEngineCallback<LocationEngineResult>()
    {

        @Override
        public void onSuccess(LocationEngineResult locationEngineResult) {

            if (locationEngineResult.getLastLocation() != null) {

                //enableLocationComponent();
               /* if(!onLocationChecked) {
                    currentlocation = locationEngineResult.getLastLocation();

                    currentLatitude = currentlocation.getLatitude();
                    currentLongitude = currentlocation.getLongitude();

                    Log.e("loc1", "Latitude: " + currentLatitude + ", Longitude: " + currentLongitude);
                    onLocationChecked = true;

                }*/


               /* if(!onLocationChecked) {
                    mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16));
                    onLocationChanged(location);
                    onLocationChecked = true;
                }*/
                currentlocation = locationEngineResult.getLastLocation();

                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {


                        LastParkedLocationRealmModule locationRealmModule = realm.where(LastParkedLocationRealmModule.class).equalTo("id", 1).findFirst();


                        if (locationRealmModule != null) {

                    lastPark_Lat = locationRealmModule.getLat();
                    lastPark_Lng = locationRealmModule.getLng();

                            //  Log.e("loc0", "--" + lastPark_Lng + "====" + lastPark_Lat);
                            if (lastPark_Lat != null || lastPark_Lng != null) {
//                        getReverseGeoCode(lastPark_Lat, lastPark_Lng);

                                eLocation = new ELocation();
                                eLocation.latitude = Double.valueOf(String.valueOf(lastPark_Lat));
                                eLocation.longitude = Double.valueOf(String.valueOf(lastPark_Lng));
                                eLocation.entryLatitude = Double.valueOf(String.valueOf(lastPark_Lat));
                                eLocation.entryLongitude = Double.valueOf(String.valueOf(lastPark_Lng));


                                //  Log.e("loc01", "--" + eLocation.latitude + "====" + eLocation.longitude);
                            } else {
                               // common.showToast("Could not find any recent last parked location", TOAST_DURATION);
                            }
                        } else {
                           // common.showToast("Could not find any recent last parked location", TOAST_DURATION);
                        }


                    }
                });
              //  mapboxMap.setPadding(20, 20, 20, 20);
                if (lastPark_Lat != null || lastPark_Lng != null) {

                  /*  try {
                        mapboxMap.enableTraffic(true);
                        TrafficPlugin trafficPlugin = new TrafficPlugin(mapView, mapboxMap);
                        trafficPlugin.enableFreeFlow(true);
                    } catch (Exception e) {
                        // logData("onMapReady() : exception: " + e);
                        Timber.e(e);
                    }
                    directionPolylinePlugin = new DirectionPolylinePlugin(mapView, mapboxMap);


                    _bearingIconPlugin = new BearingIconPlugin(mapView, mapboxMap);

                    mapboxMap.setPadding(20, 20, 20, 20);

                    setTrip(mStateModel.trip);*/
                    ArrayList<LatLng> geoPoints = new ArrayList<>();


                    LatLng latLng = new LatLng();

                    //  Log.e("loc10", "--" + DataRequestManager.currentLatitude + "====" + DataRequestManager.currentLongitude);
                    latLng.setLatitude(currentlocation.getLatitude());
                    latLng.setLongitude(currentlocation.getLongitude());
                    // latLng.setLatitude(13.1817249);
                    // latLng.setLongitude(74.9507119);


                    LatLng eLatLng = getPoint(eLocation);
                    geoPoints.add(eLatLng);
                    geoPoints.add(new LatLng(latLng));

                    // geoPoints.add(new LatLng(DataRequestManager.currentLatitude, DataRequestManager.currentLongitude));

                    double distance = latLng.distanceTo(eLatLng);


                    if (distance <= 500) {


                        //getPedestrainRoute(geoPoints, eLocation);

                        //getRoute(geoPoints, eLocation, mapboxMap, true);
                        rlNavigationDetails.setVisibility(View.GONE);

                    } else {
                        //getRoute(geoPoints, eLocation, mapboxMap, false);
                        rlNavigationDetails.setVisibility(View.VISIBLE);
                    }

                } else {

                }
            }
        }

        @Override
        public void onFailure(@NonNull Exception e) {

        }
    };
    ItemTouchHelper touchHelper;
    LocationComponentActivationOptions locationComponentActivationOptions ;
    LocationEngineRequest request;

    private MapplsMap mapboxMap;
    private SuzukiApplication app;
    private boolean firstFix;
    private LocationEngine locationEngine;
    private PermissionsManager permissionsManager;
    BearingIconPlugin _bearingIconPlugin;
    Marker marker;

    //EditText etSearchLoc;
    //boolean swapClicked = false;
    //private String fromLocation, placeName, placeAddress;
    //double destinationLat, destinationLong;
    public ELocation eLocation;

    private DirectionsResponse trip;
    RelativeLayout rlNavigationDetails;
    //    LinearLayout llBack, llStartNavigation;
    StateModel mStateModel;
    ImageView ivBack, ivShareLoc;
    private Location currentlocation;
    private  double currentLatitude = 0.0;
    private  double currentLongitude = 0.0;
    LinearLayout llStartNavigation;
    //private static final String KEY_STATE_MODEL = "state_model";
    Realm realm;

    TextView tvTimeForTravel;
    Double lastPark_Lat, lastPark_Lng;
    private ArrayList<LatLng> listOfLatlang = new ArrayList<>();
    private boolean shareButtonClicked = false;
    Common common;

    ArrayList<LatLng> viaPointList = new ArrayList<>();

    private static class StateModel {
        private ELocation eLocation;
        private DirectionsResponse trip;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.last_parked_activity);
        realm = Realm.getDefaultInstance();
        //  FirebaseApp.initializeApp(this);
        LocationComponentActivationOptions locationComponentActivationOptions ;

        mapView = findViewById(R.id.mapBoxId);
        mapView.onCreate(savedInstanceState);
        ivBack = findViewById(R.id.ivBack);
        ivShareLoc = findViewById(R.id.ivShareLoc);
        llStartNavigation = findViewById(R.id.llStartNavigation);
        tvTimeForTravel = findViewById(R.id.tvTimeForTravel);
        rlNavigationDetails = findViewById(R.id.rlNavigationDetails);

        common = new Common(this);
        mStateModel = new StateModel();

        llStartNavigation.setOnClickListener(v -> {
            startNavigation();
        });

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ivShareLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastPark_Lat != null || lastPark_Lng != null) {
                    if (!shareButtonClicked) {
                        shareButtonClicked = true;
//                        shareViaDeepLink();
                        shareIt();
                    }
                } else common.showToast("Could not find any recent last parked location", TOAST_DURATION);
            }
        });

        if (mapView != null) {

            mapView.getMapAsync(this);
        }
    }

    private void shareIt() {

        String url = "https://maps.mapmyindia.com/@" + lastPark_Lat + "," + lastPark_Lng;

        if (lastPark_Lat != null) {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = "" + url;
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Last parked vehicle address");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
            sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            shareButtonClicked = false;
            startActivity(Intent.createChooser(sharingIntent, "Share via"));

        } else {
            shareButtonClicked = false;
            common.showToast("Sorry,Could fetch data.Please try later!", TOAST_DURATION);
        }
    }

    private void shareViaDeepLink() {
        double latitude = lastPark_Lat;
        double longitude = lastPark_Lng;
        //Generate short dynamic link
        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://linkmmi.com?location=" + latitude + "," + longitude))
                .setDomainUriPrefix("https://suzu.page.link/")
                .setAndroidParameters(
                        new DynamicLink.AndroidParameters.Builder("suzuki.com.suzuki")
                                .setFallbackUrl(Uri.parse("https://maps.mapmyindia.com/@" + latitude + "," + longitude))
                                .build())

                .buildShortDynamicLink()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Short link created
                        Uri shortLink = task.getResult().getShortLink();
                        Uri flowchartLink = task.getResult().getPreviewLink();
                        System.out.println(shortLink);
                        System.out.println(flowchartLink);
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, shortLink.toString());
                        sendIntent.setType("text/plain");

                        shareButtonClicked = false;

                        Intent shareIntent = Intent.createChooser(sendIntent, "Share Using");
                        startActivity(shareIntent);

                    } else {
                        shareButtonClicked = false;
                    }
                });
    }

    public void startNavigation() {
        NavLocation location = getUserLocation();
        if (location == null) return;
        showProgress(this);
        LongOperation operation = new LongOperation();
        operation.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private class LongOperation extends AsyncTask<Void, Void, NavigationResponse> {

        @Override
        protected NavigationResponse doInBackground(Void... params) {
            try {
                LatLng currentLocation = null;
               /* NavLocation location = MapMainFragment.getUserLocation();
                if (location != null) {
                    currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                }
                else {
                    Location loc = new CurrentLoc().getCurrentLoc(getApplicationContext());
                    if (loc != null) {
                        currentLocation = new LatLng(loc.getLatitude(), loc.getLongitude());
                    }
                }*/
                Location loc = new CurrentLoc().getCurrentLoc(getApplicationContext());
                if (loc != null) {
                    currentLocation = new LatLng(loc.getLatitude(), loc.getLongitude());
                }
                else {
                    NavLocation location = MapMainFragment.getUserLocation();
                    if (location != null) {
                        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    }
                }

                NavLocation navLocation = new NavLocation("navigation");
                Point position = mStateModel.trip.routes().get(0).legs().get(0).steps().get(0).maneuver().location();
                LatLng point = new LatLng(position.latitude(), position.longitude());
                navLocation.setLongitude(point.getLongitude());
                navLocation.setLatitude(point.getLatitude());
                app = getMyApplication();
                app.setStartNavigationLocation(navLocation);
                if (currentLocation == null)
                    return new NavigationResponse(ErrorType.UNKNOWN_ERROR, null);

                //Log.d("tag", "onSuggestionListItemClicked: eLoc async " + eLocation + "   " + viaPointList);

                //app.setViaPoints(viaPointList);
                app.setELocation(eLocation);

                List<WayPoint> wayPoints = new ArrayList<>();
                /*for (LatLng latLng : viaPointList) {
                    wayPoints.add(new WayPoint(latLng.getLatitude(), latLng.getLongitude(), null));
                }*/

                return MapplsNavigationHelper.getInstance().startNavigation(app.getTrip(),
                        0, currentLocation,
                        getNavigationGeoPoint(eLocation), wayPoints, BikeBleName.getValue());

               /* NavLocation navLocation = new NavLocation("navigation");

                //if (location != null)


                NavLocation navLocation = new NavLocation("navigation");
               // Point position = mStateModel.trip.routes().get(0).legs().get(0).steps().get(0).maneuver().location();
              //  LatLng point = new LatLng(position.latitude(), position.longitude());



//                currentLocation = point;
                navLocation.setLongitude(point.getLongitude());
                navLocation.setLatitude(point.getLatitude());


                app = getMyApplication();
                app.setStartNavigationLocation(navLocation);

                if (currentLocation == null)
                    return new NavigationResponse(ErrorType.UNKNOWN_ERROR, null);

                List<WayPoint> wayPoints = new ArrayList<>();
               *//* for (LatLng latLng : viaPoints) {
                    wayPoints.add(new WayPoint(latLng.getLatitude(), latLng.getLongitude(), null));

                }*//*


                //  app.setViaPoints(viaPoints);



                app.setELocation(eLocation);

                return MapplsNavigationHelper.getInstance().startNavigation(app.getTrip(), 0, currentLocation,
                        getNavigationGeoPoint(eLocation), wayPoints, BikeBleName.getValue());*/
            } catch (Exception e) {
                Timber.e(e);
                return new NavigationResponse(ErrorType.UNKNOWN_ERROR, e);
            }
        }

        @Override
        protected void onPostExecute(NavigationResponse result) {

            dismissProgress();
                try {
                    Intent intent = new Intent(getApplicationContext(), NavigationActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("LastParkedLocation",true);
                    intent.putExtras(bundle);
                    startActivityForResult(intent, REQUEST_NAVIGATION);

                    //startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }

        }

        @Override
        protected void onPreExecute() { }

        @Override
        protected void onProgressUpdate(Void... values) { }
    }

    public NavLocation getUserLocation() {
//        if (currentlocation != null) {
        NavLocation loc = new NavLocation("router");
      /*  loc.setLatitude(Double.parseDouble(cuurent_lat));
        loc.setLongitude(Double.parseDouble(current_long));*/
        loc.setLatitude(DataRequestManager.currentLatitude);
        loc.setLongitude(DataRequestManager.currentLongitude);
        return loc;

//        } else {
//            return null;
//        }
    }


    public WayPoint getNavigationGeoPoint(ELocation eLocation) {

        try {
            if (eLocation.entryLatitude > 0 && eLocation.entryLongitude > 0) return new WayPoint(eLocation.entryLatitude, eLocation.entryLongitude, eLocation.placeName);
            else return new WayPoint(Double.parseDouble(String.valueOf(eLocation.latitude)), Double.parseDouble(String.valueOf(eLocation.longitude)), eLocation.placeName);
        } catch (Exception e) {
            return new WayPoint(0, 0, null);
        }
    }

    public void addPolyLine(LatLng start, LatLng stop, final DirectionsRoute directionsResponse) {
      /*  if (this == null || mapboxMap == null || directionsResponse == null || directionsResponse.geometry() == null)
            return;*/

        ArrayList<LatLng> latLngs = new ArrayList<>();
        if (directionPolylinePlugin != null) {
            directionPolylinePlugin.setEnabled(true);
            directionPolylinePlugin.removeAllData();
            ArrayList<LineString> lineStrings = new ArrayList<>();
            LineString lineString = LineString.fromPolyline(directionsResponse.geometry(), Constants.PRECISION_6);
            lineStrings.add(lineString);

            directionPolylinePlugin.setTrips(lineStrings, start, stop, null, null);
        }
        if (directionsResponse.legs() != null &&
                directionsResponse.legs().get(0) != null &&
                directionsResponse.legs().get(0).steps() != null &&
                directionsResponse.legs().get(0).steps().size() > 0) {
            for (LegStep legStep : directionsResponse.legs().get(0).steps()) {
                latLngs.add(new LatLng(legStep.maneuver().location().latitude(), legStep.maneuver().location().longitude()));
            }
        }
        if (latLngs.size() == 1) {
            LatLng _point = new LatLng(latLngs.get(0).getLatitude(), latLngs.get(0).getLongitude());
            mapboxMap.animateCamera(CameraUpdateFactory.newLatLng(_point));
        } else {
            try {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.includes(latLngs);
                LatLngBounds bounds = builder.build();
                mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0),
                        300);
            } catch (Exception e) {
                e.printStackTrace();
                LatLng _point = new LatLng(latLngs.get(0).getLatitude(), latLngs.get(0).getLongitude());
                mapboxMap.animateCamera(CameraUpdateFactory.newLatLng(_point));
            }
        }
    }

    private void getPedestrainRoute(final ArrayList<LatLng> wayPoints, final ELocation eLocation) {
        //if (this == null) return;

        if (directionPolylinePlugin != null) directionPolylinePlugin.removeAllData();

       // if (wayPoints == null || wayPoints.size() < 2) return;

        Point origin = Point.fromLngLat(wayPoints.get(0).getLongitude(), wayPoints.get(0).getLatitude());
        Point destination = Point.fromLngLat(wayPoints.get(wayPoints.size() - 1).getLongitude(), wayPoints.get(wayPoints.size() - 1).getLatitude());

        MapplsDirections directions = MapplsDirections.builder()
                .origin(origin)
                .steps(true)
            /*    .resource(DirectionsCriteria.RESOURCE_ROUTE_ETA)*/
                .profile(DirectionsCriteria.PROFILE_DRIVING)
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .destination(destination)
                .build();
        MapplsDirectionManager.newInstance(directions).call(new OnResponseCallback<DirectionsResponse>() {
            @Override
            public void onSuccess(DirectionsResponse response) {

                try {
                   // if (response.code().equals(200)) {
                     //   if (response == null) return;

                      //  DirectionsResponse directionsResponse = response;

                        List<DirectionsRoute> results = response.routes();

                        if (results.size() > 0) {
                            DirectionsRoute directionsRoute = results.get(0);
                            drawPath(PolylineUtils.decode(directionsRoute.geometry(), Constants.PRECISION_6), mapboxMap, true);  // commented TO DO
                        }

                        app =getMyApplication();
                        mStateModel = new StateModel();
                        mStateModel.trip = response;
                        app.setTrip(mStateModel.trip);

                        List<DirectionsWaypoint> waypointsList = response.waypoints();

                        update();
                  //  }
                }

                catch (Exception e) {
                }

            }

            @Override
            public void onError(int i, String s) {
            }
        });


    }

    private void getRoute(final ArrayList<LatLng> wayPoints, final ELocation eLocation, MapplsMap mapboxMap, boolean isWalkingMode) {
        if (directionPolylinePlugin != null)
            directionPolylinePlugin.removeAllData();
        if (wayPoints == null || wayPoints.size() < 2)
            return;
        LatLng startLocation = new LatLng();
        startLocation.setLongitude(wayPoints.get(0).getLongitude());
        startLocation.setLatitude(wayPoints.get(0).getLatitude());
        Point origin = Point.fromLngLat(wayPoints.get(0).getLongitude(), wayPoints.get(0).getLatitude());
        Point destination = Point.fromLngLat(wayPoints.get(wayPoints.size() - 1).getLongitude(), wayPoints.get(wayPoints.size() - 1).getLatitude());

        String drivingMode ;

        if(isWalkingMode){
            drivingMode = DirectionsCriteria.PROFILE_WALKING;
        }
        else{
            drivingMode = DirectionsCriteria.PROFILE_BIKING;
        }

        MapplsDirections.Builder directions = MapplsDirections.builder()
                .origin(origin)
                .steps(true)
                /*.resource(DirectionsCriteria.RESOURCE_ROUTE_ETA)*/
                .profile(drivingMode)
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .destination(destination);

        if (viaPointList != null && viaPointList.size() > 0)
            for (LatLng latLng : viaPointList) {
                directions.addWaypoint(Point.fromLngLat(latLng.getLongitude(), latLng.getLatitude()));
            }
        MapplsDirectionManager.newInstance(directions.build()).call(new OnResponseCallback<DirectionsResponse>() {
            @Override
            public void onSuccess(DirectionsResponse response) {
                try {
                    // You can get generic HTTP info about the response
                    Log.d("Response code: %d", "" + response.code());

                    DirectionsResponse directionsResponse = response;
                    Log.d("diereeee", "--" + directionsResponse.routes().get(0));


                    List<DirectionsRoute> results = directionsResponse.routes();

                    if (results.size() > 0) {
                        DirectionsRoute directionsRoute = results.get(0);
                        drawPath(startLocation, getPoint(eLocation), directionsResponse, mapboxMap);
//                            drawPath(PolylineUtils.decode(directionsRoute.geometry(), Constants.PRECISION_6), mapboxMap);
                    }

                 /*   List<DirectionsRoute> results = response.routes();


                    if (results.size() > 0) {
                        DirectionsRoute directionsRoute = results.get(0);
                        drawPath(PolylineUtils.decode(directionsRoute.geometry(), Constants.PRECISION_6), mapboxMap, true);  // commented TO DO

                    }*/


                    app = getMyApplication();
                    mStateModel = new StateModel();
                    mStateModel.trip = directionsResponse;
                    app.setTrip(mStateModel.trip);
                    Log.d("sjsjsjjs", "--" + app.getTrip());
//                        Log.d("sjsjsjjs", "-sss-" + mStateModel.trip);
                    List<DirectionsWaypoint> waypointsList = directionsResponse.waypoints();

//                        update();
//                        addPolyLine(new LatLng(currentlocation.getLatitude(),
//                                        currentlocation.getLongitude()),
//                                getPoint(eLocation), mStateModel.trip);

//                        LatLng destinyLatLong = new LatLng(Double.parseDouble(destiny_lat), Double.parseDouble(destiny_long));
//                        if (destinyLatLong != null) {
//                            IconFactory iconFactory = IconFactory.getInstance(getApplicationContext());
//                            Icon icon = iconFactory.fromResource(R.drawable.marker);
////            Icon icon = iconFactory.defaultMarker();
//                            MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(destinyLatLong)).icon(icon);
//                            marker = mapboxMap.addMarker(markerOptions);
//
//                            mapboxMap.removeMarker(marker);
//                            markerOptions.setTitle("");
//                            markerOptions.setSnippet("");
//                            marker.setPosition(new LatLng(currentlocation));
//                            marker.setIcon(icon);
//                            mapboxMap.addMarker(markerOptions);
//                        }


                } catch (Exception e) {
                    Timber.e(e);
//                    showErrorMessage(R.string.something_went_wrong);
//                    onFragmentBackPressed();
                }

            }

            @Override
            public void onError(int i, String s) {

            }
        });



//        MapmyIndiaDirections.Builder builder = MapmyIndiaDirections.builder()
//                .origin(origin)
//                .destination(destination)
//                .annotations(DirectionsCriteria.ANNOTATION_CONGESTION)
//                .resource(DirectionsCriteria.RESOURCE_ROUTE_ETA)
//                .profile(DirectionsCriteria.PROFILE_BIKING)
//                .steps(true)
//                .alternatives(true)
//                .overview(DirectionsCriteria.OVERVIEW_FULL);
//
////        showProgress(getApplicationContext());
//
////        if (viaPointList != null && viaPointList.size() > 0)
////            for (LatLng latLng : viaPointList) {
////                builder.addWaypoint(Point.fromLngLat(latLng.getLongitude(), latLng.getLatitude()));
////            }
//        MapmyIndiaDirections mapmyIndiaDirections = builder.build();
//        mapmyIndiaDirections.enqueueCall(new Callback<DirectionsResponse>() {
//            @Override
//            public void onResponse(@NonNull Call<DirectionsResponse> call, @NonNull Response<DirectionsResponse> response) {
//
//                try {
//                    if (response.code() == 200) {
//                        if (response.body() == null) {
////                            showErrorMessage(R.string.something_went_wrong);
////                            onFragmentBackPressed();
//                            Log.d("diereeee", "--" + response.body());
//                            return;
//                        }
//
//                        // You can get generic HTTP info about the response
//                        Log.d("Response code: %d", "" + response.code());
//
//                        DirectionsResponse directionsResponse = response.body();
//                        Log.d("diereeee", "--" + directionsResponse.routes().get(0));
//
//
//                        List<DirectionsRoute> results = directionsResponse.routes();
//
//                        if (results.size() > 0) {
//                            DirectionsRoute directionsRoute = results.get(0);
//                            drawPath(startLocation, getPoint(eLocation), directionsResponse, mapboxMap);
////                            drawPath(PolylineUtils.decode(directionsRoute.geometry(), Constants.PRECISION_6), mapboxMap);
//                        }
//
//                        app = getMyApplication();
//                        mStateModel = new StateModel();
//                        mStateModel.trip = directionsResponse;
//                        app.setTrip(mStateModel.trip);
//                        Log.d("sjsjsjjs", "--" + app.getTrip());
////                        Log.d("sjsjsjjs", "-sss-" + mStateModel.trip);
//                        List<DirectionsWaypoint> waypointsList = directionsResponse.waypoints();
//
////                        update();
////                        addPolyLine(new LatLng(currentlocation.getLatitude(),
////                                        currentlocation.getLongitude()),
////                                getPoint(eLocation), mStateModel.trip);
//
////                        LatLng destinyLatLong = new LatLng(Double.parseDouble(destiny_lat), Double.parseDouble(destiny_long));
////                        if (destinyLatLong != null) {
////                            IconFactory iconFactory = IconFactory.getInstance(getApplicationContext());
////                            Icon icon = iconFactory.fromResource(R.drawable.marker);
//////            Icon icon = iconFactory.defaultMarker();
////                            MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(destinyLatLong)).icon(icon);
////                            marker = mapboxMap.addMarker(markerOptions);
////
////                            mapboxMap.removeMarker(marker);
////                            markerOptions.setTitle("");
////                            markerOptions.setSnippet("");
////                            marker.setPosition(new LatLng(currentlocation));
////                            marker.setIcon(icon);
////                            mapboxMap.addMarker(markerOptions);
////                        }
//
//                    } else {
////                        showErrorMessage(R.string.something_went_wrong);
////                        onFragmentBackPressed();
//                    }
//                } catch (Exception e) {
//                    Timber.e(e);
////                    showErrorMessage(R.string.something_went_wrong);
////                    onFragmentBackPressed();
//                }
////                hideProgress();
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<DirectionsResponse> call, @NonNull Throwable throwable) {
//                if (this == null)
//                    return;
//                Timber.d("onFailure url: %s", call.request().url().toString());
//                if (!call.isCanceled()) {
//                    throwable.printStackTrace();
////                    showErrorMessage(R.string.something_went_wrong);
//                }
////                hideProgress();
////                onFragmentBackPressed();
//            }
//        });

    }

    private void drawPath(List<Point> waypoints, MapplsMap mapboxMap, boolean finalIsDistanceLess) {
        listOfLatlang = new ArrayList<>();
        for (Point point : waypoints) {
            listOfLatlang.add(new LatLng(point.latitude(), point.longitude()));
        }

        try {
            myDrawing(mapboxMap, finalIsDistanceLess);
        } catch (Exception e) {
            Log.e("TAG", "drawPath: ", e);
        }

 /*       mapboxMap.addPolyline(new PolylineOptions().addAll(listOfLatlang).color(Color.parseColor("#3bb2d0")).width(8));
        LatLngBounds latLngBounds = new LatLngBounds.Builder().includes(listOfLatlang).build();
       // mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 30));
        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 70));
*/    }


        private void drawPath(LatLng startLocation, LatLng stop, DirectionsResponse directionsResponse, MapplsMap mapboxMap) {

        ArrayList<LineString> lineStrings = new ArrayList<>();
//        LineString lineString = LineString.fromPolyline(directionsResponse.routes().get(0).geometry(), Constants.PRECISION_6);
//        lineStrings.add(lineString);
        for (DirectionsRoute directionsRoute: directionsResponse.routes() ){
            LineString lineString = LineString.fromPolyline(Objects.requireNonNull(directionsRoute.geometry()), Constants.PRECISION_6);
            lineStrings.add(lineString);
        }

        directionPolylinePlugin.setTrips(lineStrings, startLocation, stop, viaPointList, directionsResponse.routes());
        listOfLatlang = new ArrayList<>();
        for (Point point : PolylineUtils.decode(Objects.requireNonNull(directionsResponse.routes().get(0).geometry()), Constants.PRECISION_6)) {
            listOfLatlang.add(new LatLng(point.latitude(), point.longitude()));
        }

        /* this is done for animating/moving camera to particular position */

            //mapboxMap.addPolyline(new PolylineOptions().addAll(listOfLatlang).color(Color.parseColor("#3bb2d0")).width(8));
        LatLngBounds latLngBounds = new LatLngBounds.Builder().includes(listOfLatlang).build();
        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 70));
    }

    public void myDrawing(MapplsMap mapboxMap, boolean finalIsDistanceLess) {
        if (!finalIsDistanceLess) {
            mapboxMap.addPolyline(new PolylineOptions().addAll(listOfLatlang).color(Color.parseColor("#205fa4")).width(4));
        } else {
            // Dashed polyline will be added
            directionPolylinePlugin = new DirectionPolylinePlugin(mapView, mapboxMap);
            //dashedPolylinePlugin.createPolyline(listOfLatlang);
        }

        if (listOfLatlang != null) {
            Marker marker;
            IconFactory iconFactory = IconFactory.getInstance(getApplicationContext());
            Icon icon = iconFactory.fromResource(R.drawable.oval);
//            Icon icon = iconFactory.defaultMarker();
            MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(listOfLatlang.get(0))).icon(icon);


            marker = mapboxMap.addMarker(markerOptions);

            mapboxMap.removeMarker(marker);
            markerOptions.setTitle("");
            markerOptions.setSnippet("");
            marker.setPosition(new LatLng(listOfLatlang.get(0)));

            marker.setIcon(icon);
            mapboxMap.addMarker(markerOptions);


        }
        if (listOfLatlang != null && listOfLatlang.size() > 0) {
            int listLatLngLength = listOfLatlang.size() - 1;
            LatLng destiny = listOfLatlang.get(listLatLngLength);
            if (destiny != null) {
                Marker marker;
                IconFactory iconFactory = IconFactory.getInstance(getApplicationContext());
                Icon icon = iconFactory.fromResource(R.drawable.oval);
//            Icon icon = iconFactory.defaultMarker();
                MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(destiny)).icon(icon);

                marker = mapboxMap.addMarker(markerOptions);

                mapboxMap.removeMarker(marker);
                markerOptions.setTitle("");
                markerOptions.setSnippet("");
                marker.setPosition(new LatLng(destiny));
                marker.setIcon(icon);
                mapboxMap.addMarker(markerOptions);


            }
        } else return;



        /* this is done for animating/moving camera to particular position */

        LatLngBounds latLngBounds = new LatLngBounds.Builder().includes(listOfLatlang).build();
        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 70));

    }

    public LatLng getPoint(ELocation eLocation) {
        try {
            return new LatLng(Double.parseDouble(String.valueOf(eLocation.latitude)), Double.parseDouble(String.valueOf(eLocation.longitude)));
        } catch (Exception e) {
            //logData("getPoint() :exception: " + e);
            return new LatLng(0, 0);
        }
    }


    private void update() {

        if (mStateModel.trip.routes().get(0).duration() == null)
            return;
        try {
//            Log.d("psoodsods", "--" + placeAddress + placeName + mStateModel.trip.distance() + mStateModel.trip.duration());
//            tvDistance.setText(String.format("%s", NavigationFormatter.getFormattedDistance(mStateModel.trip.distance().floatValue(), getMyApplication())));
           // logData("update() => duration.initValue : " + mStateModel.trip.routes().get(0).duration());
            //        tvTimeForTravel.setText(String.format("%s ", NavigationFormatter.getFormattedDuration(mStateModel.trip.routes().get(0).duration().intValue(), getMyApplication())));
//            tvDestinationAddress.setText(placeAddress);
//            tvPlaceAddress.setText(placeName);
        } catch (Exception e) {
            //logData("update() :  exception: " + e);
            Timber.e(e);
            Log.d("ddderrr", "--" + e.getMessage());
        }
    }

    public MapView getMapView() {
        return mapView;
    }


    public MapplsMap getMapboxMap() {
        if (mapboxMap != null)
            return mapboxMap;
        return null;
    }


    @Override
    public void onMapReady(MapplsMap mapboxMap) {
        this.mapboxMap = mapboxMap;

        mapboxMap.getUiSettings().enableLogoClick(false);


        mapboxMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                enableLocationComponent(style);
            }
        });

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {


                LastParkedLocationRealmModule locationRealmModule = realm.where(LastParkedLocationRealmModule.class).equalTo("id", 1).findFirst();


                if (locationRealmModule != null) {

                    lastPark_Lat = locationRealmModule.getLat();
                    lastPark_Lng = locationRealmModule.getLng();

                  //  Log.e("loc0", "--" + lastPark_Lng + "====" + lastPark_Lat);
                    if (lastPark_Lat != null || lastPark_Lng != null) {
//                        getReverseGeoCode(lastPark_Lat, lastPark_Lng);

                        eLocation = new ELocation();
                        eLocation.latitude = Double.valueOf(String.valueOf(lastPark_Lat));
                        eLocation.longitude = Double.valueOf(String.valueOf(lastPark_Lng));
                        eLocation.entryLatitude = Double.valueOf(String.valueOf(lastPark_Lat));
                        eLocation.entryLongitude = Double.valueOf(String.valueOf(lastPark_Lng));


                      //  Log.e("loc01", "--" + eLocation.latitude + "====" + eLocation.longitude);
                    } else {
                        common.showToast("Could not find any recent last parked location", TOAST_DURATION);
                    }
                }
                else {
                    common.showToast("Could not find any recent last parked location", TOAST_DURATION);
                }


            }
        });
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }

      /*  mapboxMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                if (ActivityCompat.checkSelfPermission(LastParkedLocationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(LastParkedLocationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    common.showToast("Location permission is not given", TOAST_DURATION);
                    return;
                }
                mapboxMap.getLocationComponent().activateLocationComponent(LocationComponentActivationOptions.builder(LastParkedLocationActivity.this, style).build());
                mapboxMap.getLocationComponent().setLocationComponentEnabled(true);
            }
        });*/

        //   mapboxMap.getLocationComponent().activateLocationComponent(locationComponentActivationOptions);
        //   mapboxMap.getLocationComponent().setLocationComponentEnabled(true);


        mapboxMap.setPadding(20, 20, 20, 20);
        if (lastPark_Lat != null || lastPark_Lng != null) {

         /*   if (eLocation != null) {
                Marker marker;


                LatLng latLng = new LatLng();
                //logData("onMapReady() => lat_lng : " + lastPark_Lat + " lng " + lastPark_Lng);
                latLng.setLatitude(lastPark_Lat);
                latLng.setLongitude(lastPark_Lng);

                IconFactory iconFactory = IconFactory.getInstance(getApplicationContext());
                Icon icon = iconFactory.fromResource(R.drawable.marker);
//            Icon icon = iconFactory.defaultMarker();
                MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(latLng)).icon(icon);


                marker = mapboxMap.addMarker(markerOptions);

                mapboxMap.removeMarker(marker);
                markerOptions.setTitle("");
                markerOptions.setSnippet("");
                marker.setPosition(new LatLng(latLng));

                marker.setIcon(icon);
                mapboxMap.addMarker(markerOptions);


            }*/
            try {
                mapboxMap.enableTraffic(true);
                TrafficPlugin trafficPlugin = new TrafficPlugin(mapView, mapboxMap);
                trafficPlugin.enableFreeFlow(true);
            } catch (Exception e) {
               // logData("onMapReady() : exception: " + e);
                Timber.e(e);
            }
            directionPolylinePlugin = new DirectionPolylinePlugin(mapView, mapboxMap);


            _bearingIconPlugin = new BearingIconPlugin(mapView, mapboxMap);

            mapboxMap.setPadding(20, 20, 20, 20);

            setTrip(mStateModel.trip);
            ArrayList<LatLng> geoPoints = new ArrayList<>();


            LatLng latLng = new LatLng();

            latLng.setLatitude(DataRequestManager.currentLatitude);
            latLng.setLongitude(DataRequestManager.currentLongitude);


            LatLng eLatLng = getPoint(eLocation);
            geoPoints.add(eLatLng);
            geoPoints.add(new LatLng(latLng));


            double distance = latLng.distanceTo(eLatLng);

         /*   Log.e("loc_distance", String.valueOf(distance));
            Log.e("loc_eLocation", String.valueOf(eLocation));
            Log.e("loc_eLatLng", String.valueOf(eLatLng));
            Log.e("loc_geoPoints", String.valueOf(geoPoints));
*/
              /*IconFactory iconFactory = IconFactory.getInstance(getApplicationContext());
                Icon icon = iconFactory.fromResource(R.drawable.marker);
//            Icon icon = iconFactory.defaultMarker();
                MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(eLatLng)).icon(icon);


                marker = mapboxMap.addMarker(markerOptions);

                mapboxMap.removeMarker(marker);
                markerOptions.setTitle("");
                markerOptions.setSnippet("");
                marker.setPosition(new LatLng(latLng));

                marker.setIcon(icon);
                mapboxMap.addMarker(markerOptions);*/

            if (distance <= 500) {

              /*  IconFactory iconFactory = IconFactory.getInstance(getApplicationContext());
                Icon icon = iconFactory.fromResource(R.drawable.marker);
//            Icon icon = iconFactory.defaultMarker();
                MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(latLng)).icon(icon);


                marker = mapboxMap.addMarker(markerOptions);

                mapboxMap.removeMarker(marker);
                markerOptions.setTitle("");
                markerOptions.setSnippet("");
                marker.setPosition(new LatLng(latLng));

                marker.setIcon(icon);
                mapboxMap.addMarker(markerOptions);*/

                //getPedestrainRoute(geoPoints, eLocation);
                getRoute(geoPoints, eLocation, mapboxMap, true);
                rlNavigationDetails.setVisibility(View.GONE);
            } else {
                getRoute(geoPoints, eLocation, mapboxMap, false);
                rlNavigationDetails.setVisibility(View.VISIBLE);
            }

                /*    if (distance <= 500) {

                        getPedestrainRoute(geoPoints, eLocation);
                        rlNavigationDetails.setVisibility(View.VISIBLE);
                    } else {

                    }
                 */

            //LatLng latLng = new LatLng();

            //latLng.setLatitude(13.1817249);
            //latLng.setLongitude(74.9507119);
            //geoPoints.add(new LatLng(latLng));

            //LatLng eLatLng = getPoint(eLocation);
           // geoPoints.add(eLatLng);

            //double distance = latLng.distanceTo(eLatLng);


            //getRoute(geoPoints, eLocation, mapboxMap);

           /* mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });*/

            setCompassDrawable(mapboxMap);
           // mapboxMap.setCameraPosition(setCameraAndTilt());
            mapboxMap.getUiSettings().setLogoMargins(40, dpToPx(120), 40, dpToPx(60));
            mapboxMap.getUiSettings().setCompassMargins(40, dpToPx(150), 40, dpToPx(40));
            //mapboxMap.getUiSettings().setRotateGesturesEnabled(true);
           // mapboxMap.getUiSettings().setTiltGesturesEnabled(true);
           // mapboxMap.getUiSettings().setZoomGesturesEnabled(false);
          /*  mapboxMap.setCameraPosition(setCameraAndTilt());
            mapboxMap.setMinZoomPreference(4);
            mapboxMap.setMaxZoomPreference(18.5);*/
            /*mapboxMap.setMaxZoomPreference(18.5);
            mapboxMap.setMinZoomPreference(4);

            setCompassDrawable(mapboxMap);
            mapboxMap.getUiSettings().setLogoMargins(40, dpToPx(120), 40, dpToPx(60));
            mapboxMap.getUiSettings().setCompassMargins(40, dpToPx(150), 40, dpToPx(40));*/

        }

      /*  mapboxMap.getUiSettings().enableLogoClick(false);


        try {
            TrafficPlugin trafficPlugin = new TrafficPlugin(mapView, mapboxMap);
            trafficPlugin.enableFreeFlow(true);
        } catch (Exception e) {
            Timber.e(e);
        }
        directionPolylinePlugin = new DirectionPolylinePlugin(mapView, mapboxMap);
//        enableLocationComponent();

        _bearingIconPlugin = new BearingIconPlugin(mapView, mapboxMap);

        mapboxMap.setPadding(20, 20, 20, 20);


        try {
            if (eLocation != null && mStateModel.trip != null) {
                app = getMyApplication();
                setTrip(mStateModel.trip);
//                update();
//                addPolyLine(new LatLng(currentlocation.getLatitude(),
//                                currentlocation.getLongitude()),
//                        new LatLng(Double.parseDouble(eLocation.latitude),
//                                Double.parseDouble(eLocation.longitude)),
//                        mStateModel.trip);
                Log.d("geopoi", "--" + mStateModel.trip);
            } else {
                try {
//                    if (currentlocation != null) {
                  *//*  ArrayList<LatLng> geoPoints = new ArrayList<>();

                    //geoPoints.add(new LatLng(Double.parseDouble(cuurent_lat), Double.parseDouble(current_long)));
                    geoPoints.add(new LatLng(13.1817249, 74.9507119));
                    geoPoints.add(getPoint(eLocation));*//*
                    ArrayList<LatLng> geoPoints = new ArrayList<>();

                    LatLng latLng = new LatLng();
                   *//* latLng.setLatitude(currentlocation.getLatitude());
                    latLng.setLongitude(currentlocation.getLongitude());*//*

                    geoPoints.add(new LatLng(latLng));

                    LatLng eLatLng = getPoint(eLocation);
                    geoPoints.add(eLatLng);

                    double distance = latLng.distanceTo(eLatLng);


                    getRoute(geoPoints, eLocation, mapboxMap);
//                        Log.d("geopoits", "-" + getNavigationGeoPoint(eLocation) + currentlocation.getLatitude() + "--" + currentlocation.getLongitude());
//                        update();
//                    } else {
//                        Toast.makeText(this, R.string.current_location_not_available, Toast.LENGTH_SHORT).show();
////                        onFragmentBackPressed();
//                    }
                } catch (Exception e) {
                    Timber.e(e);
//                    onFragmentBackPressed();
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        }

        setCompassDrawable(mapboxMap);
        mapboxMap.setCameraPosition(setCameraAndTilt());
        mapboxMap.setMinZoomPreference(4);
        mapboxMap.setMaxZoomPreference(18.5);*/
    }

    protected CameraPosition setCameraAndTilt() {
        CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(
                28.551087, 77.257373)).zoom(5).tilt(0).build();
        return cameraPosition;
    }

    void setCompassDrawable(MapplsMap mapboxMap) {
        mapView.getCompassView().setBackgroundResource(R.drawable.compass_background);
        mapboxMap.getUiSettings().setCompassImage(getDrawable(R.drawable.compass_north_up));
        int padding = dpToPx(8);
        int elevation = dpToPx(18);
        mapView.getCompassView().setPadding(padding, padding, padding, padding);
        ViewCompat.setElevation(mapView.getCompassView(), elevation);
    }

    public SuzukiApplication getMyApplication() {
        return (SuzukiApplication) getApplication();
    }

    public DirectionsResponse getTrip() {
        return trip;
    }

    public void setTrip(DirectionsResponse trip) {
        this.trip = trip;
    }

    private void enableLocationComponent(Style style) {
        LocationComponentOptions options = LocationComponentOptions.builder(this)
                .trackingGesturesManagement(true)
                .accuracyColor(ContextCompat.getColor(this, R.color.colorAccent))
                .foregroundDrawable(R.drawable.location_pointer)
                .build();
// Get an instance of the component LocationComponent
        locationComponent = mapboxMap.getLocationComponent();
        LocationComponentActivationOptions locationComponentActivationOptions = LocationComponentActivationOptions.builder(this, style)
                .locationComponentOptions(options)
                .build();
// Activate with options
        locationComponent.activateLocationComponent(locationComponentActivationOptions);
// Enable to make component visiblelocationEngine
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationComponent.setLocationComponentEnabled(true);
        locationEngine = locationComponent.getLocationEngine();
     /*   LocationEngineCallback<LocationEngineResult> locationEngineCallback = new LocationEngineCallback<LocationEngineResult>()
        {

            @Override
            public void onSuccess(LocationEngineResult locationEngineResult) {
                if (locationEngineResult.getLastLocation() != null) {
                    Location location = locationEngineResult.getLastLocation();
                    //mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16));
                }
            }

            @Override
            public void onFailure(@NonNull Exception e) {

            }
        };*/
        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();
        assert locationEngine != null;
        locationEngine.requestLocationUpdates(request, locationEngineCallback, getMainLooper());
        locationEngine.getLastLocation(locationEngineCallback);
// Set the component's camera mode
        locationComponent.setCameraMode(CameraMode.TRACKING);
        locationComponent.setRenderMode(RenderMode.COMPASS);
    }

    @Override
    public void onConnected() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationEngine.requestLocationUpdates(request, locationEngineCallback, getMainLooper());
    }

    @Override
    public void onLocationChanged(Location location) {

    /* //
       *//* if (app != null) {
            app =(SuzukiApplication) getMyApplication();
        }
        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(location.getLatitude(), location.getLongitude()), 16));
        Log.d("sss", "=" + location.getLongitude() + location.getLatitude());
       *//*


        currentlocation = location;
        //mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location), 16), 500);


        Log.d("locccc", "onloc chang--" + currentlocation.getLatitude() + currentlocation.getLongitude());
//        Log.d("locccc", "onloc chang--" + eLocation.placeName);
       *//* try {
            if (location == null || location.getLatitude() <= 0)
                return;
            if (!firstFix) {
                mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location), 16), 500);
                firstFix = true;
                Log.d("loccc", "--latlng" + location.getLongitude() + location.getLatitude());
//                Log.d("locccc", "onloc chang--" + eLocation.placeName);
//                getReverseGeoCode(location.getLatitude(), location.getLongitude());


            }
            if (app != null)
                app.setCurrentLocation(location);
        } catch (Exception e) {
            //logData("onLocationChanged() : exception: " + e);
            //ignore
        }*//*


        try {
          //  app.setCurrentLocation(location);
          *//*  if (eLocation != null && mStateModel.trip != null) {
                app = (SuzukiApplication) getApplication();
                setTrip(mStateModel.trip);
                update();
                addPolyLine(new LatLng(currentlocation.getLatitude(),
                                currentlocation.getLongitude()),
                        new LatLng(eLocation.latitude,
                                eLocation.longitude),
                        mStateModel.trip.routes().get(0));
                Log.d("geopoi", "--" + mStateModel.trip);
            } else {*//*
                try {
//                    if (currentlocation != null) {
                    ArrayList<LatLng> geoPoints = new ArrayList<>();

                    LatLng latLng = new LatLng();
                   *//* latLng.setLatitude(currentlocation.getLatitude());
                    latLng.setLongitude(currentlocation.getLongitude());*//*

                    geoPoints.add(new LatLng(latLng));

                    LatLng eLatLng = getPoint(eLocation);
                    geoPoints.add(eLatLng);

                    double distance = latLng.distanceTo(eLatLng);


                *//*    if (distance <= 500) {

                        getPedestrainRoute(geoPoints, eLocation);
                        rlNavigationDetails.setVisibility(View.VISIBLE);
                    } else {*//*

                        mapboxMap.getUiSettings().enableLogoClick(false);


                        try {
                            TrafficPlugin trafficPlugin = new TrafficPlugin(mapView, mapboxMap);
                            trafficPlugin.enableFreeFlow(true);
                        } catch (Exception e) {
                            Timber.e(e);
                        }
                        directionPolylinePlugin = new DirectionPolylinePlugin(mapView, mapboxMap);
//        enableLocationComponent();

                        _bearingIconPlugin = new BearingIconPlugin(mapView, mapboxMap);

                        mapboxMap.setPadding(20, 20, 20, 20);
                        getRoute(geoPoints, eLocation, mapboxMap, false);
                        rlNavigationDetails.setVisibility(View.VISIBLE);
                  //  }

                    *//*
                     * commented in v6.2.6
                     * update();
                     * *//*
                } catch (Exception e) {
                    //logData("onLocationChanged() : first exception: " + e);
                    Timber.e(e);
                }
           // }
        } catch (Exception e) {
            Timber.e(e);
            //logData("onLocationChanged() : second exception: " + e);
        }*/


    }

    @Override
    public void onMapError(int i, String s) {

        Log.d("errr", "-" + s + "---" + i);
    }


    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
       /* if (locationEngine != null) {
            locationEngine.removeLocationUpdates(locationEngineCallback);
        }*/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
//        if (locationEngine != null) {
//            locationEngine.deactivate();
//        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        /*if (locationEngine != null)
            locationEngine.removeLocationUpdates(locationEngineCallback);*/
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
       /* if (locationEngine != null) {
            locationEngine.removeLocationUpdates(locationEngineCallback);
            //locationEngine.addLocationEngineListener(this);
        }*/
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    public DirectionPolylinePlugin getDirectionPolylinePlugin() {
        return directionPolylinePlugin;
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {

    }

    @Override
    public boolean onMapLongClick(@NonNull LatLng latLng) {

        return false;
    }

    @Override
    public void requestDrag(RecyclerView.ViewHolder viewHolder) {
        touchHelper.startDrag(viewHolder);
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }


   /* public NavLocation getUserLocation() {
        if (currentlocation != null) {
            NavLocation loc = new NavLocation("router");
            loc.setLatitude(currentlocation.getLatitude());
            loc.setLongitude(currentlocation.getLongitude());
            return loc;
        } else {
            return null;
        }
    }*/

    public void clearPOIs() {
        try {
            if (mapboxMap == null)
                return;
            mapboxMap.removeAnnotations();
            if (directionPolylinePlugin != null)
                directionPolylinePlugin.removeAllData();
        } catch (Exception e) {
            Timber.e(e);
        }
    }
}
