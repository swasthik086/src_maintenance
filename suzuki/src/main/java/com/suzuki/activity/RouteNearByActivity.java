package com.suzuki.activity;

import static android.os.Looper.getMainLooper;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;


import com.mappls.sdk.geojson.LineString;
import com.mappls.sdk.geojson.Point;
import com.mappls.sdk.maps.MapView;
import com.mappls.sdk.maps.MapplsMap;
import com.mappls.sdk.maps.OnMapReadyCallback;
import com.mappls.sdk.maps.Style;
import com.mappls.sdk.maps.annotations.Icon;
import com.mappls.sdk.maps.annotations.IconFactory;
import com.mappls.sdk.maps.annotations.Marker;
import com.mappls.sdk.maps.annotations.MarkerOptions;
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
import com.suzuki.broadcaster.BleConnection;
import com.suzuki.fragment.MapMainFragment;
import com.suzuki.interfaces.StartDragListener;
import com.suzuki.maps.plugins.BearingIconPlugin;
import com.suzuki.maps.plugins.DirectionPolylinePlugin;
import com.suzuki.maps.traffic.TrafficPlugin;
import com.suzuki.pojo.RecentTripRealmModule;
import com.suzuki.pojo.RiderProfileModule;
import com.suzuki.pojo.SettingsPojo;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import timber.log.Timber;

import static com.suzuki.fragment.DashboardFragment.prev_cluster_name;
import static com.suzuki.fragment.DashboardFragment.staticConnectionStatus;
import static com.suzuki.fragment.MapMainFragment.currentlocation;
import static com.suzuki.utils.Common.BikeBleName;


public class RouteNearByActivity extends BaseActivity implements OnMapReadyCallback, MapplsMap.OnMapLongClickListener,
        PermissionsListener, StartDragListener {
    private String profile = DirectionsCriteria.PROFILE_DRIVING;
    public ELocation eLocation;
    LocationEngineRequest request;

    ProgressBar progressBar;
    public static MapView mapView;
    ProgressBar apiProgressBar;
    ItemTouchHelper touchHelper;
    private MapplsMap mapboxMap;
    private static SuzukiApplication app;
    private boolean firstFix;
    private static DirectionPolylinePlugin directionPolylinePlugin;
    private LocationEngine locationEngine;
    private PermissionsManager permissionsManager;
    BearingIconPlugin _bearingIconPlugin;
    Marker marker;
    boolean moveToNavigation;
    private BleConnection mReceiver;
    LocationComponent locationComponent;
    boolean swapClicked, bookmarkClicked;
    private String fromLocation, placeName, placeAddress;
    double destinationLat, destinationLong;
    public static boolean startClicked = true;
    public static  int tripID;
    private static final long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;

    private static final long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;
    LocationEngineCallback<LocationEngineResult> locationEngineCallback = new LocationEngineCallback<LocationEngineResult>()
    {

        @Override
        public void onSuccess(LocationEngineResult locationEngineResult) {
            if (locationEngineResult.getLastLocation() != null) {
                Location location = locationEngineResult.getLastLocation();
                mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16));
            }
        }

        @Override
        public void onFailure(@NonNull Exception e) {

        }
    };
    String currentPlaceName, destionationPlaceName, currentLat, currentLong, destinyLat, destinyLong;
    ImageView ivBookMark;
    RelativeLayout rlLocDetails, rlSwapLoc, rlBookMark, rlNavigationDetails;
    LinearLayout llBack, llStartNavigation;
    TextView tvCurrentloc, tvDestination, tvTimeForTravel, tvDistance, tvPlaceAddress, tvDestinationAddress;
    StateModel mStateModel;
    Realm realm;
    private static final String KEY_STATE_MODEL = "state_model";

    public static RouteNearByActivity routeNearByActivity;

    @Override
    public boolean onMapLongClick(@NonNull LatLng latLng) {
        return false;
    }

    private static class StateModel {
        private ELocation eLocation;
        private DirectionsResponse trip;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.route_activity);

        routeNearByActivity = this;


        realm = Realm.getDefaultInstance();
        apiProgressBar = findViewById(R.id.api_progress_bar);
        mapView = findViewById(R.id.mapBoxId);
        rlLocDetails = findViewById(R.id.rlLocDetails);
        llBack = findViewById(R.id.llBack);
        rlSwapLoc = findViewById(R.id.rlSwapLoc);
        rlBookMark = findViewById(R.id.rlBookMark);
        tvCurrentloc = findViewById(R.id.tvCurrentloc);
        tvDestination = findViewById(R.id.tvDestination);
        rlNavigationDetails = findViewById(R.id.rlNavigationDetails);
        llStartNavigation = findViewById(R.id.llStartNavigation);
        tvDestination = findViewById(R.id.tvDestination);
        tvPlaceAddress = findViewById(R.id.tvPlaceAddress);
        tvTimeForTravel = findViewById(R.id.tvTimeForTravel);
        tvDistance = findViewById(R.id.tvDistance);
        ivBookMark = findViewById(R.id.ivBookMark);

        tvDestinationAddress = findViewById(R.id.tvDestinationAddress);
        try {

            Intent intent = getIntent();
            fromLocation = intent.getStringExtra("fromLocation");
            placeName = intent.getStringExtra("placeName");
            placeAddress = intent.getStringExtra("placeAddress");
            destinationLat = (intent.getDoubleExtra("lat", 0));
            destinationLong = (intent.getDoubleExtra("long", 0));

//          destinationLat = Double.parseDouble(mydestinationLat);
//          destinationLong = Double.parseDouble(mydestinationLong);
            Log.d("dkdkkdkkkk", "--" + destinationLat + destinationLong + placeAddress + placeName + fromLocation);

            tvCurrentloc.setText(fromLocation);
            tvDestination.setText(placeName);


            currentPlaceName = fromLocation;
            destionationPlaceName = placeName;
            currentLat = String.valueOf(currentlocation.getLatitude());
            currentLong = String.valueOf(currentlocation.getLongitude());

            Log.d("dkdkdkdk", "---" + destinationLat + destinationLong);
            destinyLat = String.valueOf(destinationLat);
            destinyLong = String.valueOf(destinationLong);


//            mStateModel = savedInstanceState.getParcelable(KEY_STATE_MODEL);

//            mStateModel.eLocation = eLocation;
//            Log.d("loccddsinac", "--" + mStateModel.eLocation);

        } catch (Exception e) {
            e.printStackTrace();
        }

        setBluetoothStatus();

        getSaveTripsData(realm);


        rlBookMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (bookmarkClicked == false) {
                    bookmarkClicked = true;
                    ivBookMark.setImageResource(R.drawable.book_mark_blue);
//                    updateRecentData(realm, bookmarkClicked);


                } else {
                    bookmarkClicked = false;
                    ivBookMark.setImageResource(R.drawable.book_mark);
//                    updateRecentData(realm, bookmarkClicked);

                }


            }
        });
        llBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        rlSwapLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (swapClicked == false) {
                    swapClicked = true;
                    tvCurrentloc.setText(placeName);
                    tvDestination.setText(fromLocation);


//                    currentPlaceName = placeName;
//                    destionationPlaceName = fromLocation;
//                    destinyLat = String.valueOf(currentlocation.getLatitude());
//                    destinyLong = String.valueOf(currentlocation.getLongitude());

//                    currentLat = String.valueOf(eLocation.latitude);
//                    currentLong = String.valueOf(eLocation.longitude);

//                    ELocation swapELocation = new ELocation();
//
//                    swapELocation.longitude = currentLong;
//                    swapELocation.latitude = currentLat;
//
//
//                    ArrayList<LatLng> geoPointsw = new ArrayList<>();
//                    geoPointsw.add(new LatLng(Double.parseDouble(destinyLat), Double.parseDouble(destinyLong)));
//                    geoPointsw.add(getNavigationGeoPoint(swapELocation));
//                    getRoute(geoPointsw, swapELocation);

                    if (destinyLat != null && mStateModel.trip != null) {
                        app = getMyApplication();
                        app.setTrip(mStateModel.trip);
                        update(placeAddress, placeName);
                        addPolyLine(new LatLng(destinationLat,
                                        destinationLong),
                                new LatLng((currentlocation.getLatitude()),
                                        currentlocation.getLongitude()),
                                mStateModel.trip.routes().get(0));

                    }


                } else {
                    swapClicked = false;
                    tvCurrentloc.setText(fromLocation);
                    tvDestination.setText(placeName);


//                    currentPlaceName = fromLocation;
//                    destionationPlaceName = placeName;
//                    currentLat = String.valueOf(currentlocation.getLatitude());
//                    currentLong = String.valueOf(currentlocation.getLongitude());

//                    destinyLat = String.valueOf(eLocation.latitude);
//                    destinyLong = String.valueOf(eLocation.longitude);


                    if (destinyLat != null && mStateModel.trip != null) {
                        app = getMyApplication();
                        app.setTrip(mStateModel.trip);
                        update(fromLocation, placeAddress);
                        addPolyLine(new LatLng(currentlocation.getLatitude(),
                                        currentlocation.getLongitude()),
                                new LatLng(Double.parseDouble(destinyLat),
                                        Double.parseDouble(destinyLong)),
                                mStateModel.trip.routes().get(0));

                    }

                }
            }
        });

//        ArrayList<LatLng> geoPointsw = new ArrayList<>();
//        geoPointsw.add(new LatLng(currentlocation.getLatitude(), currentlocation.getLongitude()));
//        geoPointsw.add(getNavigationGeoPoint(eLocation));
//        getRoute(geoPointsw, eLocation);
        mStateModel = new StateModel();
//        update();

//        if (savedInstanceState != null) {
//            mStateModel = savedInstanceState.getParcelable(KEY_STATE_MODEL);
//        } else {
//            mStateModel = new StateModel();
//            if (this != null) {
//                if (this.containsKey(ARG_E_LOCATION))
//                    mStateModel.eLocation = new Gson().fromJson(getArguments().getString(ARG_E_LOCATION), ELocation.class);
//                fromLocation = getArguments().getString(ARG_E_FROM_LOCATION);
//            }
//        }
        try {
            if ((destinyLat) != null && mStateModel.trip != null) {
                app = getMyApplication();
                app.setTrip(mStateModel.trip);
                update();
                addPolyLine(new LatLng(currentlocation.getLatitude(),
                                currentlocation.getLongitude()),
                        new LatLng((destinationLat),
                                (destinationLong)),
                        mStateModel.trip.routes().get(0));
                Log.d("geopoi", "--" + mStateModel.trip);
            } else {
                try {
                    if (currentlocation != null) {
                        ELocation eLocation = new ELocation();
                        eLocation.latitude = Double.valueOf(String.valueOf(destinationLat));
                        eLocation.longitude = Double.valueOf(String.valueOf(destinationLong));
                        ArrayList<LatLng> geoPoints = new ArrayList<>();

                        geoPoints.add(new LatLng(currentlocation.getLatitude(), currentlocation.getLongitude()));
                        geoPoints.add(getPoint(eLocation));
                        getRoute(geoPoints, eLocation);
                        Log.d("geopoits", "-" + getNavigationGeoPoint(eLocation) + currentlocation.getLatitude() + "--" + currentlocation.getLongitude());
                        update();
                    } else {
                        Toast.makeText(this, R.string.current_location_not_available, Toast.LENGTH_SHORT).show();
//                        onFragmentBackPressed();
                    }
                } catch (Exception e) {
                    Timber.e(e);
                    Toast.makeText(this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
//                    onFragmentBackPressed();
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        }


        llStartNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                startNavigation();
//                addTripDataToRealm();
                if (moveToNavigation) {
                    if (bookmarkClicked) {

                        if (startClicked) {
                            startClicked = false;
                            startNavigation();
                            addTripDataToRealm();
                            addRideCount();
                        } else {
                            Toast.makeText(RouteNearByActivity.this, "Please wait while we load ! ", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        showSaveTripsAlert();
                    }

                } else {
                    showConnectToBluetoothAlert();
                }
            }
        });
        mapView.getMapAsync(this);
    }

    public void addRideCount() {


        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(new Realm.Transaction() {

                @Override
                public void execute(Realm realm) {

                    RiderProfileModule riderProfile = realm.where(RiderProfileModule.class).equalTo("id", 1).findFirst();

                    if (riderProfile == null) {
                        riderProfile = realm.createObject(RiderProfileModule.class, 1);

                        riderProfile.setRideCounts(1);
                        realm.insertOrUpdate(riderProfile);

                    } else if (riderProfile != null) {

                        int count = riderProfile.getRideCounts();
                        Log.d("ddkddk", "==" + count + count++);
                        riderProfile.setRideCounts(count++);
                        realm.insertOrUpdate(riderProfile);

                    }


                }
            });

        } catch (Exception e) {
            Log.d("realmex", "--" + e.getMessage());

        }


    }

    public void showSaveTripsAlert() {


        Dialog dialog = new Dialog(RouteNearByActivity.this, R.style.custom_dialog);
        dialog.setContentView(R.layout.custom_dialog_for_savetrips);


        TextView tvCustomTextBtn = dialog.findViewById(R.id.tvCustomTextBtn);
        LinearLayout llSave = dialog.findViewById(R.id.llSave);
        tvCustomTextBtn.setText("Save");
        ImageView ivCustomClose = dialog.findViewById(R.id.ivCustomClose);
        ivCustomClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startNavigation();
//                addRideCount();
                dialog.cancel();

            }
        });


        llSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startNavigation();

                addTripDataToRealm();
                addRideCount();
                dialog.cancel();
            }
        });

        dialog.show();


    }

    public void showConnectToBluetoothAlert() {
        Dialog dialog = new Dialog(RouteNearByActivity.this, R.style.custom_dialog);
        dialog.setContentView(R.layout.custom_dialog_for_ble);


        ImageView ivCustomClose = dialog.findViewById(R.id.ivCustomClose);
        ivCustomClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        LinearLayout llConnect = dialog.findViewById(R.id.llConnect);


        llConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(RouteNearByActivity.this, DeviceListingScanActivity.class);
                startActivity(in);
                dialog.cancel();
            }
        });
        dialog.show();
    }

    private void addTripDataToRealm() {

        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(new Realm.Transaction() {

                @Override
                public void execute(Realm realm) {


                    RealmResults<RecentTripRealmModule> results = realm.where(RecentTripRealmModule.class).findAll();


                    RecentTripRealmModule recentTripRealmModule = realm.createObject(RecentTripRealmModule.class);

                    int tripSize = results.size();
                    Date date = new Date();
                     tripID = (int) new Date().getTime();


                    recentTripRealmModule.setDate(getDate());
                    recentTripRealmModule.setId(tripID);
                    recentTripRealmModule.setTime(String.format("%s ", NavigationFormatter.getFormattedDuration(mStateModel.trip.routes().get(0).duration().intValue(), getMyApplication())));
                    recentTripRealmModule.setTotalDistance(String.format("%s", NavigationFormatter.getFormattedDistance(mStateModel.trip.routes().get(0).distance().floatValue(), getMyApplication())));
                    recentTripRealmModule.setRideTime(String.format("%s ", NavigationFormatter.getFormattedDuration(mStateModel.trip.routes().get(0).duration().intValue(), getMyApplication())));
                    recentTripRealmModule.setStartlocation(currentPlaceName);
                    recentTripRealmModule.setEndlocation(destionationPlaceName);
                    recentTripRealmModule.setFavorite(false);
                    recentTripRealmModule.setCurrent_lat(currentLat);
                    recentTripRealmModule.setCurrent_long(currentLong);
                    recentTripRealmModule.setDestination_lat(destinyLat);
                    recentTripRealmModule.setDestination_long(destinyLong);
                    recentTripRealmModule.setDateTime(date);
                    realm.insert(recentTripRealmModule);

//                    tvDistance.setText(String.format("%s", NavigationFormatter.getFormattedDistance(mStateModel.trip.distance().floatValue(), getMyApplication())));
//                    tvTimeForTravel.setText(String.format("%s ", NavigationFormatter.getFormattedDuration(mStateModel.trip.duration().intValue(), getMyApplication())));


                    if (tripSize > 10) {
                        RealmResults<RecentTripRealmModule> recentTrip = realm.where(RecentTripRealmModule.class)
                                .sort("dateTime", Sort.ASCENDING)
                                .findAll();

                        recentTrip.get(0).deleteFromRealm();
                    }


                }
            });

        } catch (Exception e) {
            Log.d("realmex", "--" + e.getMessage());

        }


    }

    private String getDate() {

        Calendar c = Calendar.getInstance();
        int day = c.get(Calendar.DAY_OF_MONTH);

        int year = c.get(Calendar.YEAR);

        String[] monthName = {"JAN", "FEB",
                "MAR", "APR", "MAY", "JUN", "JUL",
                "AUG", "SEP", "OCT", "NOV",
                "DEC"};


        String monthname = monthName[c.get(Calendar.MONTH)];

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String tripDate = monthname + " " + day + ", " + year;

        Log.d("datet--", "-" + c + timestamp);

        return tripDate;

    }

    private void deleteTripRecord() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.delete(RecentTripRealmModule.class);
            }
        });
    }


    public void startNavigation() {
        Log.d("startnavig", "--" + MapMainFragment.getUserLocation());
        if (this == null)
            return;
        NavLocation location = MapMainFragment.getUserLocation();

        if (location == null)
            return;

        LongOperation operation = new LongOperation();
        operation.execute();
    }

    private class LongOperation extends AsyncTask<Void, Void, NavigationResponse> {


        @Override
        protected NavigationResponse doInBackground(Void... params) {

            try {
                Log.d("dddd", "---" + params);

                LatLng currentLocation = null;
                NavLocation location = MapMainFragment.getUserLocation();
                if (location != null)
                    currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                NavLocation navLocation = new NavLocation("navigation");
                Point position = mStateModel.trip.routes().get(0).legs().get(0).steps().get(0).maneuver().location();
                LatLng point = new LatLng(position.latitude(), position.longitude());
                navLocation.setLongitude(point.getLongitude());
                navLocation.setLatitude(point.getLatitude());
                app = getMyApplication();
                app.setStartNavigationLocation(navLocation);
                if (currentLocation == null)
                    return new NavigationResponse(ErrorType.UNKNOWN_ERROR, null);


                Log.d("kkkjhhj", "--" + app.getTrip());

               // Log.d("tag", "onSuggestionListItemClicked: eLoc async "+getNavigationGeoPoint(eLocation));

                return MapplsNavigationHelper.getInstance().startNavigation(app.getTrip(), 0, currentLocation,
                        getNavigationGeoPoint(eLocation), null, prev_cluster_name);
                //TODo device id will be passed here


            } catch (Exception e) {
                Timber.e(e);
                Log.d("jkkkkhkhkh", "--" + e.getMessage());
                return new NavigationResponse(ErrorType.UNKNOWN_ERROR, e);
            }

        }

        @Override
        protected void onPostExecute(NavigationResponse result) {

            if (this == null)
                return;
//            dismissProgress();
            if (result != null && result.getError() != null) {
                if(result.getError().errorCode == 409) {
                    MapplsNavigationHelper.getInstance().deleteSession(BikeBleName.getValue(), new IStopSession() {
                        @Override
                        public void onSuccess() {
                            LongOperation operation = new LongOperation();
                            operation.execute();
                        }

                        @Override
                        public void onFailure() {

                        }
                    });
                    /*show dialog to user*/

                    /*new AlertDialog.Builder(RouteNearByActivity.this)
                            .setMessage(getApplicationContext().getResources().getString(R.string.Session_Message))
                            .setTitle("Navigation Alert")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    MapmyIndiaNavigationHelper.getInstance().deleteSession(MACID, new IStopSession() {
                                        @Override
                                        public void onSuccess() {
                                            Toast.makeText(RouteNearByActivity.this, "Start Navigation Now", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onFailure() {

                                        }
                                    });
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    dialog.dismiss();
                                }
                            })
                            .create()
                            .show();*/
                }

                return;
            }


//            this.startActivity(new Intent(this, NavigationActivity.class));
            Intent ittt = new Intent(getApplicationContext(), NavigationActivity.class);
            startActivity(ittt);


//            getApplication().startActivity(new Intent(getApplication(), NavigationActivity.class));

        }

        @Override
        protected void onPreExecute() {

//            showProgress(getApplicationContext());


        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

/*
    private class LongOperation extends AsyncTask<Void, Void, Boolean> {


        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                Log.d("dddd", "---" + params);

                LatLng currentLocation = null;
                NavLocation location = MapMainFragment.getUserLocation();
                if (location != null)
                    currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                NavLocation navLocation = new NavLocation("navigation");
                Point position = mStateModel.trip.legs().get(0).steps().get(0).maneuver().location();
                LatLng point = new LatLng(position.latitude(), position.longitude());
                navLocation.setLongitude(point.getLongitude());
                navLocation.setLatitude(point.getLatitude());
                app = getMyApplication();
                app.setStartNavigationLocation(navLocation);
                if (currentLocation == null)
                    return false;


                Log.d("kkkjhhj", "--" + app.getTrip());
                ELocation eLocation = new ELocation();

                eLocation.longitude = destinyLong;
                eLocation.latitude = destinyLat;
//                eLocation.placeName =

                return MapmyIndiaNavigationHelper.getInstance().startNavigation(app, app.getTrip(), currentLocation,
                        getNavigationGeoPoint(eLocation), null);


            } catch (Exception e) {
                Timber.e(e);
                Log.d("jkkkkhkhkh", "--" + e.getMessage());
                return false;
            }

        }

        @Override
        protected void onPostExecute(Boolean result) {

            if (this == null || result == null)
                return;
//            dismissProgress();
            if (result != null && !result) {


                return;
            }


//            this.startActivity(new Intent(this, NavigationActivity.class));
            Intent ittt = new Intent(getApplicationContext(), NavigationActivity.class);
            startActivity(ittt);


//            getApplication().startActivity(new Intent(getApplication(), NavigationActivity.class));

        }

        @Override
        protected void onPreExecute() {

//            showProgress(getApplicationContext());


        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }
*/

    public WayPoint getNavigationGeoPoint(ELocation eLocation) {
        try {
            if (eLocation.entryLatitude > 0 && eLocation.entryLongitude > 0)
                return new WayPoint(eLocation.entryLatitude, eLocation.entryLongitude, eLocation.placeName);
            else
                return new WayPoint(Double.parseDouble(String.valueOf(eLocation.latitude)), Double.parseDouble(String.valueOf(eLocation.longitude)), eLocation.placeName);
        } catch (Exception e) {
            return new WayPoint(0, 0, null);
        }
    }

    public void addPolyLine(LatLng start, LatLng stop, final DirectionsRoute directionsResponse) {
        if (this == null || mapboxMap == null || directionsResponse == null || directionsResponse.geometry() == null)
            return;

        ArrayList<LatLng> latLngs = new ArrayList<>();
        if (directionPolylinePlugin != null) {
            directionPolylinePlugin.setEnabled(true);
            directionPolylinePlugin.removeAllData();
            ArrayList<LineString> lineStrings = new ArrayList<>();
            LineString lineString = LineString.fromPolyline(directionsResponse.geometry(), Constants.PRECISION_6);
            lineStrings.add(lineString);

            for (Point point : lineString.coordinates()) {
                latLngs.add(new LatLng(point.latitude(), point.longitude()));
            }
            directionPolylinePlugin.setTrips(lineStrings, start, stop, null,null);
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
                mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 40, dpToPx(120), 40, dpToPx(140)),
                        300);
            } catch (Exception e) {
                e.printStackTrace();
                LatLng _point = new LatLng(latLngs.get(0).getLatitude(), latLngs.get(0).getLongitude());
                mapboxMap.animateCamera(CameraUpdateFactory.newLatLng(_point));
            }
        }
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    private void getRoute(final ArrayList<LatLng> wayPoints, final ELocation eLocation) {
        if (this == null) {
            return;
        }
        if (directionPolylinePlugin != null)
            directionPolylinePlugin.removeAllData();
        if (wayPoints == null || wayPoints.size() < 2)
            return;
        Point origin = Point.fromLngLat(wayPoints.get(0).getLongitude(), wayPoints.get(0).getLatitude());
        Point destination = Point.fromLngLat(wayPoints.get(wayPoints.size() - 1).getLongitude(), wayPoints.get(wayPoints.size() - 1).getLatitude());


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
//        MapmyIndiaDirections mapmyIndiaDirections = builder.build();
//        mapmyIndiaDirections.enqueueCall(new Callback<DirectionsResponse>() {
//            @Override
//            public void onResponse(@NonNull Call<DirectionsResponse> call, @NonNull Response<DirectionsResponse> response) {
//                // You can get generic HTTP info about the response
//                Timber.d("Response code: %d", response.code());
//                Timber.d("Response url: %s", call.request().url().toString());
//                try {
//                    if (response.code() == 200) {
//                        if (response.body() == null) {
////                            showErrorMessage(R.string.something_went_wrong);
////                            onFragmentBackPressed();
//                            return;
//                        }
//
//
//                        DirectionsResponse directionsResponse = response.body();
//                        Log.d("diereeee", "--" + directionsResponse.routes().get(0));
//
//
//                        List<DirectionsRoute> results = directionsResponse.routes();
//
////                        if (results.size() > 0) {
////                            DirectionsRoute directionsRoute = results.get(0);
////                            drawPath(PolylineUtils.decode(directionsRoute.geometry(), Constants.PRECISION_6));
////                        }
//
//                        app = getMyApplication();
//                        mStateModel = new StateModel();
//                        mStateModel.trip = directionsResponse;
//                        app.setTrip(mStateModel.trip);
//                        Log.d("sjsjsjjs", "--" + mStateModel.trip);
//                        Log.d("sjsjsjjs", "-sss-" + mStateModel.trip);
//                        List<DirectionsWaypoint> waypointsList = directionsResponse.waypoints();
//
//                        update();
//                        addPolyLine(new LatLng(currentlocation.getLatitude(),
//                                        currentlocation.getLongitude()),
//                                getPoint(eLocation), mStateModel.trip.routes().get(0));
//
//                        LatLng destinyLatLong = new LatLng(destinationLat, destinationLong);
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



        MapplsDirections directions = MapplsDirections.builder()
                .origin(origin)
                .steps(true)
                .resource(DirectionsCriteria.RESOURCE_ROUTE_ETA)
                .profile(DirectionsCriteria.PROFILE_DRIVING)
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .destination(destination)
                .build();
        MapplsDirectionManager.newInstance(directions).call(new OnResponseCallback<DirectionsResponse>() {
            @Override
            public void onSuccess(DirectionsResponse response) {

                try {
                    if (response.code().equals(200)) {
                        if (response == null) {
//                            showErrorMessage(R.string.something_went_wrong);
//                            onFragmentBackPressed();
                            return;
                        }


                        DirectionsResponse directionsResponse = response;
                        Log.d("diereeee", "--" + directionsResponse.routes().get(0));


                        List<DirectionsRoute> results = directionsResponse.routes();

//                        if (results.size() > 0) {
//                            DirectionsRoute directionsRoute = results.get(0);
//                            drawPath(PolylineUtils.decode(directionsRoute.geometry(), Constants.PRECISION_6));
//                        }

                        app = getMyApplication();
                        mStateModel = new StateModel();
                        mStateModel.trip = directionsResponse;
                        app.setTrip(mStateModel.trip);
                        Log.d("sjsjsjjs", "--" + mStateModel.trip);
                        Log.d("sjsjsjjs", "-sss-" + mStateModel.trip);
                        List<DirectionsWaypoint> waypointsList = directionsResponse.waypoints();

                        update();
                        addPolyLine(new LatLng(currentlocation.getLatitude(),
                                        currentlocation.getLongitude()),
                                getPoint(eLocation), mStateModel.trip.routes().get(0));

                        LatLng destinyLatLong = new LatLng(destinationLat, destinationLong);
                        if (destinyLatLong != null) {
                            IconFactory iconFactory = IconFactory.getInstance(getApplicationContext());
                            Icon icon = iconFactory.fromResource(R.drawable.marker);
//            Icon icon = iconFactory.defaultMarker();
                            MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(destinyLatLong)).icon(icon);
                            marker = mapboxMap.addMarker(markerOptions);

                            mapboxMap.removeMarker(marker);
                            markerOptions.setTitle("");
                            markerOptions.setSnippet("");
                            marker.setPosition(new LatLng(currentlocation));
                            marker.setIcon(icon);
                            mapboxMap.addMarker(markerOptions);
                        }

                    } else {
//                        showErrorMessage(R.string.something_went_wrong);
//                        onFragmentBackPressed();
                    }
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
    }


    private void drawPath(List<Point> waypoints) {
        ArrayList<LatLng> listOfLatlang = new ArrayList<>();
        for (Point point : waypoints) {
            listOfLatlang.add(new LatLng(point.latitude(), point.longitude()));
        }

        Log.d("geopoosos", "-" + listOfLatlang);

//        mapmyIndiaMap.addPolyline(new PolylineOptions().addAll(listOfLatlang).color(Color.parseColor("#3bb2d0")).width(4));
//        LatLngBounds latLngBounds = new LatLngBounds.Builder().includes(listOfLatlang).build();
//        mapmyIndiaMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 30));
    }

    public LatLng getPoint(ELocation eLocation) {
        try {
            return new LatLng(Double.parseDouble(String.valueOf(eLocation.latitude)), Double.parseDouble(String.valueOf(eLocation.longitude)));
        } catch (Exception e) {
            return new LatLng(0, 0);
        }
    }

    public void setCompassDrawable() {
        mapView.getCompassView().setY((170f));
        mapView.getCompassView().setBackgroundResource(R.drawable.compass_background);
        mapboxMap.getUiSettings().setCompassImage(ContextCompat.getDrawable(this, R.drawable.compass_north_up));
        int padding = dpToPx(8);
        int elevation = dpToPx(8);
        mapView.getCompassView().setPadding(padding, padding, padding, padding);
        ViewCompat.setElevation(mapView.getCompassView(), elevation);
    }

    public int dpToPx(final float dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void update() {

        if (this == null)
            return;
        try {
//            Log.d("psoodsods", "--" + placeAddress + placeName + mStateModel.trip.distance() + mStateModel.trip.duration());
            tvDistance.setText(String.format("%s", NavigationFormatter.getFormattedDistance(mStateModel.trip.routes().get(0).distance().floatValue(),getMyApplication())));
            tvTimeForTravel.setText(String.format("%s ", NavigationFormatter.getFormattedDuration(mStateModel.trip.routes().get(0).duration().intValue(), getMyApplication())));
            tvDestinationAddress.setText(placeAddress);
            tvPlaceAddress.setText(placeName);
        } catch (Exception e) {
            Timber.e(e);
//            Toast.makeText(this,"Could not find directions,")
            Log.d("ddderrr", "--" + e.getMessage());
        }
    }

    private void update(String placeAddress, String placeName) {

        if (this == null)
            return;
        try {
//            Log.d("psoodsods", "--" + placeAddress + placeName + mStateModel.trip.distance() + mStateModel.trip.duration());
            tvDistance.setText(String.format("%s", NavigationFormatter.getFormattedDistance(mStateModel.trip.routes().get(0).distance().floatValue(), getMyApplication())));
            tvTimeForTravel.setText(String.format("%s ", NavigationFormatter.getFormattedDuration(mStateModel.trip.routes().get(0).duration().intValue(), getMyApplication())));
            tvDestinationAddress.setText(placeAddress);
            tvPlaceAddress.setText(placeName);
        } catch (Exception e) {
            Timber.e(e);
            Log.d("ddderrr", "--" + e.getMessage());
        }
    }

    public static MapView getMapView() {
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

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }


        if (currentlocation != null) {
            IconFactory iconFactory = IconFactory.getInstance(this);
            Icon icon = iconFactory.fromResource(R.drawable.marker);
//            Icon icon = iconFactory.defaultMarker();
            MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(currentlocation)).icon(icon);
            marker = mapboxMap.addMarker(markerOptions);

            mapboxMap.removeMarker(marker);
            markerOptions.setTitle("");
            markerOptions.setSnippet("");
            marker.setPosition(new LatLng(currentlocation));
            marker.setIcon(icon);
            mapboxMap.addMarker(markerOptions);
        }

        try {
            TrafficPlugin trafficPlugin = new TrafficPlugin(mapView, mapboxMap);
            trafficPlugin.enableNonFreeFlow(true);
        } catch (Exception e) {
            Timber.e(e);
        }
        directionPolylinePlugin = new DirectionPolylinePlugin(mapView, mapboxMap);
        mapboxMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                enableLocationComponent(style);
            }
        });

        _bearingIconPlugin = new BearingIconPlugin(mapView, mapboxMap);
        mapboxMap.setMaxZoomPreference(18.5);
        mapboxMap.setMinZoomPreference(4);
        setCompassDrawable();
        mapboxMap.getUiSettings().setLogoMargins(40, dpToPx(120), 40, dpToPx(140));
        mapboxMap.getUiSettings().setCompassMargins(40, dpToPx(120), 40, dpToPx(40));

        //        try {
//            this.mapboxMap = mapboxMap;
//            directionPolylinePlugin = MapMainFragment.getDirectionPolylinePlugin();
//            if (mStateModel != null && mStateModel.trip != null) {
//                addPolyLine(new LatLng(app.getCurrentLocation().getLatitude(),
//                                app.getCurrentLocation().getLongitude()),
//                        getPoint(mStateModel.eLocation), mStateModel.trip);
//            }
//        } catch (Exception e) {
//            Timber.e(e);
//        }
        apiProgressBar.setVisibility(View.GONE);
    }

    public SuzukiApplication getMyApplication() {
        return ((SuzukiApplication) getApplication());
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
//    private void enableLocationComponent() {
//        // Check if permissions are enabled and if not request
//        if (PermissionsManager.areLocationPermissionsGranted(this)) {
//
//            LocationComponentOptions options = LocationComponentOptions.builder(this)
//                    .trackingGesturesManagement(true)
//                    .accuracyColor(ContextCompat.getColor(this, R.color.mapboxGreen))
//                    .build();
//
//
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                return;
//            }
//            // Get an instance of the component
//            LocationComponent locationComponent = mapboxMap.getLocationComponent();
//
//
//            // Activate with options
//            locationComponent.activateLocationComponent(this, options);
//
//            // Enable to make component visible
//            locationComponent.setLocationComponentEnabled(true);
//            locationEngine = locationComponent.getLocationEngine();
//            if (locationEngine != null) {
//                locationEngine.addLocationEngineListener(this);
//            }
//            // Set the component's camera mode
//            locationComponent.setCameraMode(CameraMode.NONE);
//            locationComponent.setRenderMode(RenderMode.COMPASS);
//            mapboxMap.setPadding(20, 20, 20, 20);
//
//
//        } else {
//            permissionsManager = new PermissionsManager(this);
//            permissionsManager.requestLocationPermissions(this);
//        }
//    }

    @Override
    public void onConnected() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        locationEngine.requestLocationUpdates(request, locationEngineCallback, getMainLooper());
    }

    @Override
    public void onLocationChanged(Location location) {
//        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
//                new LatLng(location.getLatitude(), location.getLongitude()), 16));
//        Log.d("sss", "=" + location.getLongitude() + location.getLatitude());
//        Timber.i("onLocationChanged");


//        currentlocation = location;
//
//        Log.d("locccc", "onloc chang--" + currentlocation.getLatitude() + currentlocation.getLongitude());
////        Log.d("locccc", "onloc chang--" + eLocation.placeName);
//        getReverseGeoCode(currentlocation.getLatitude(), currentlocation.getLongitude());
//        try {
//            if (location == null || location.getLatitude() <= 0)
//                return;
//            if (!firstFix) {
//                mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location), 16), 500);
//                firstFix = true;
//                Log.d("loccc", "--latlng" + location.getLongitude() + location.getLatitude());
////                Log.d("locccc", "onloc chang--" + eLocation.placeName);
//                getReverseGeoCode(location.getLatitude(), location.getLongitude());
//
//
//            }

//            getReverseGeoCode(location.getLatitude(), location.getLongitude());
//        app.setCurrentLocation(location);
//        } catch (Exception e) {
//            //ignore
//        }
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
//        if (locationEngine != null) {
//            locationEngine.removeLocationEngineListener(this);
//            locationEngine.removeLocationUpdates();
//        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
//        if (locationEngine != null) {
//            locationEngine.deactivate();
//        }
        realm.close();
        RouteActivity.startClicked = true;
        RouteNearByActivity.startClicked = true;
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
       // if (locationEngine != null)
            //locationEngine.removeLocationEngineListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        apiProgressBar.setVisibility(View.VISIBLE);
        mapView.onResume();
        if (locationEngine != null) {
//            locationEngine.removeLocationEngineListener(this);
//            locationEngine.addLocationEngineListener(this);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    public static DirectionPolylinePlugin getDirectionPolylinePlugin() {
        return directionPolylinePlugin;
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {

    }

    @Override
    public void requestDrag(RecyclerView.ViewHolder viewHolder) {
        touchHelper.startDrag(viewHolder);
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
//        if (mStateModel != null)
//            outState.putString(KEY_STATE_MODEL, new Gson().toJson(mStateModel));
    }


    public static NavLocation getUserLocation() {
        if (currentlocation != null) {
            NavLocation loc = new NavLocation("router");
            loc.setLatitude(currentlocation.getLatitude());
            loc.setLongitude(currentlocation.getLongitude());
            return loc;
        } else {
            return null;
        }
    }


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

    public void getSaveTripsData(Realm realm) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {


                SettingsPojo settingsPojo = realm.where(SettingsPojo.class).equalTo("id", 1).findFirst();


                if (settingsPojo != null) {
                    if (settingsPojo.isSaveTrips()) {
                        ivBookMark.setImageResource(R.drawable.book_mark_blue);
                        bookmarkClicked = true;
                    } else {
                        ivBookMark.setImageResource(R.drawable.book_mark);
                        bookmarkClicked = false;
                    }
                }


            }
        });
    }


    public void updateRecentData(Realm realm, boolean save) {

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {


                SettingsPojo settingsPojo = realm.where(SettingsPojo.class).equalTo("id", 1).findFirst();

                if (settingsPojo == null) {

                    settingsPojo = realm.createObject(SettingsPojo.class, 1);
                    settingsPojo.setSaveTrips(save);


                    realm.insertOrUpdate(settingsPojo);

                } else if (settingsPojo != null) {

                    settingsPojo.setSaveTrips(save);

                    realm.insertOrUpdate(settingsPojo);
                }

            }


        });

        getSaveTripsData(realm);


    }


    public void setBluetoothStatus() {

        if (staticConnectionStatus) {

            moveToNavigation = true;
        } else {
            moveToNavigation = false;
        }
        IntentFilter intentFilter = new IntentFilter(
                "status");

        mReceiver = new BleConnection() {

            @Override
            public void onReceive(Context context, Intent intent) {
                //extract our message from intent

                Boolean status = intent.getExtras().getBoolean("status");

                if (status) {
                    moveToNavigation = true;

                } else {
                    moveToNavigation = false;
                }
            }


        };

        registerReceiver(mReceiver, intentFilter);
    }

}
