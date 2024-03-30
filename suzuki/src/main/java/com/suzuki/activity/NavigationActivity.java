package com.suzuki.activity;

import static com.suzuki.activity.RouteActivity.viaPoints;
import static com.suzuki.activity.RouteNearByActivity.tripID;
import static com.suzuki.application.SuzukiApplication.calculateCheckSum;
import static com.suzuki.fragment.NavigationFragment.navigationModeEnabled;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.mappls.sdk.maps.MapView;
import com.mappls.sdk.maps.MapplsMap;
import com.mappls.sdk.maps.Style;
import com.mappls.sdk.maps.annotations.Marker;
import com.mappls.sdk.maps.geometry.LatLng;
import com.mappls.sdk.maps.location.LocationComponent;
import com.mappls.sdk.maps.location.LocationComponentActivationOptions;
import com.mappls.sdk.maps.location.LocationComponentOptions;
import com.mappls.sdk.maps.location.OnCameraTrackingChangedListener;
import com.mappls.sdk.maps.location.modes.CameraMode;
import com.mappls.sdk.maps.location.modes.RenderMode;
import com.mappls.sdk.maps.location.permissions.PermissionsListener;
import com.mappls.sdk.maps.location.permissions.PermissionsManager;
import com.mappls.sdk.navigation.MapplsNavigationHelper;
import com.mappls.sdk.navigation.NavLocation;
import com.mappls.sdk.navigation.NavigationFormatter;
import com.mappls.sdk.services.api.OnResponseCallback;
import com.mappls.sdk.services.api.Place;
import com.mappls.sdk.services.api.PlaceResponse;
import com.mappls.sdk.services.api.autosuggest.model.ELocation;
import com.mappls.sdk.services.api.reversegeocode.MapplsReverseGeoCode;
import com.mappls.sdk.services.api.reversegeocode.MapplsReverseGeoCodeManager;
import com.suzuki.R;

import com.suzuki.application.SuzukiApplication;
import com.suzuki.base.BaseMapActivity;
import com.suzuki.fragment.MapMainFragment;
import com.suzuki.fragment.NavigationFragment;
import com.suzuki.maps.plugins.BearingIconPlugin;
import com.suzuki.maps.plugins.DirectionPolylinePlugin;
import com.suzuki.maps.plugins.MapEventsPlugin;
import com.suzuki.maps.plugins.RouteArrowPlugin;
import com.suzuki.pojo.ClusterStatusPktPojo;
import com.suzuki.pojo.RecentTripRealmModule;
import com.suzuki.pojo.RiderProfileModule;
import com.suzuki.pojo.ViaPointLocationRealmModel;
import com.suzuki.utils.DataRequestManager;
import com.suzuki.utils.NavigationCompassEngine;
import com.suzuki.utils.NavigationLocationEngine;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import timber.log.Timber;


public class NavigationActivity extends BaseMapActivity implements MapplsMap.InfoWindowAdapter,
        View.OnClickListener, MapplsMap.OnMarkerClickListener, MapplsMap.OnMapLongClickListener,
        FragmentManager.OnBackStackChangedListener,
        OnCameraTrackingChangedListener,
        PermissionsListener {

    public static int DEFAULT_PADDING;
    public static int DEFAULT_BOTTOM_PADDING;
    public MapplsMap mapplsMap;
    boolean isVisible = false;
    Handler backStackHandler = new Handler();
    //    bearing plugin
    BearingIconPlugin _bearingIconPlugin;
    private SuzukiApplication app;
    private Bundle savedInstanceState;
    private NavigationLocationEngine navigationLocationEngine;

    private PermissionsManager permissionsManager;
    //location layer plugin
    private DirectionPolylinePlugin directionPolylinePlugin;
    private RouteArrowPlugin routeArrowPlugin;
    private MapEventsPlugin mapEventsPlugin;
    private boolean firstFix;
    private Fragment currentFragment;

    private int top_speeds=0;

    int Max_speed;

    private String currentPlaceName, destionationPlaceName;

    //RouteActivity.StateModel mStateModel;

    private Double currentLat, currentLong, destinyLat, destinyLong;

    private String  tripARideStart, tripARideEnd, duration, rideTime;

    String endTime;

    private boolean isLastParkedLocation = false;

    private String vehicleType;




    Runnable backStackRunnable = () -> {
        try {
            onBackStackChangedWithDelay();
        } catch (Exception e) {
            Timber.e(e);
        }
    };
    private FloatingActionButton floatingActionButton;

    @SuppressLint("RestrictedApi")
    private void setupUI() {

        floatingActionButton = findViewById(R.id.move_to_current_location);
        floatingActionButton.setOnClickListener(this);
        floatingActionButton.setVisibility(View.GONE);
    }

    public SuzukiApplication getMyApplication() {
        return ((SuzukiApplication) getApplication());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
        try {
            app = getMyApplication();
            DEFAULT_PADDING = (int) getResources().getDimension(R.dimen.default_map_padding);
            DEFAULT_BOTTOM_PADDING = (int) getResources().getDimension(R.dimen.default_map_bottom_padding);

            navigationLocationEngine = new NavigationLocationEngine();
            getSupportFragmentManager().addOnBackStackChangedListener(this);
            setupUI();
        } catch (Exception e) {
            //ignore
        }
        this.navigateTo(new NavigationFragment(), true);

        try {
            Intent intent = getIntent();

            Bundle bundle = intent.getExtras();

            if (bundle != null) {
                currentPlaceName = bundle.getString("currentPlaceName");
                destionationPlaceName = bundle.getString("destionationPlaceName");
                currentLat = bundle.getDouble("currentLat");
                currentLong = bundle.getDouble("currentLong");
                destinyLat = bundle.getDouble("destinyLat");
                destinyLong = bundle.getDouble("destinyLong");

                duration = bundle.getString("rideduration");
                rideTime = bundle.getString("ridetime");
                isLastParkedLocation = bundle.getBoolean("LastParkedLocation");

            }
        }
        catch (Exception e){
            e.printStackTrace();
        }


    }

    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onConnected() {

    }

    @Override
    public void onLocationChanged(Location location) {

    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onClick(View v) {

    }

    @Nullable
    @Override
    public View getInfoWindow(@NonNull Marker marker) {
        return null;
    }

    @Override
    public void onCameraTrackingDismissed() {

    }


    @Override
    public void onCameraTrackingChanged(int currentMode) {

    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }



    public DirectionPolylinePlugin getDirectionPolylinePlugin() {
        return directionPolylinePlugin;
    }


    public MapView getMapView() {
        return mapView;
    }

    public NavLocation getUserLocation() {
        if (app.getCurrentLocation() != null) {
            NavLocation loc = new NavLocation("router");
            loc.setLatitude(app.getCurrentLocation().getLatitude());
            loc.setLongitude(app.getCurrentLocation().getLongitude());
            return loc;
        } else {
            return null;
        }
    }

    public void startNavigation() {
        onBackPressed();
        navigateTo(new NavigationFragment(), true);

    }

    public boolean getLastParkedLocation(){
        return isLastParkedLocation;
    }

    public float getLocationAccuracy() {
        if (app.getCurrentLocation() != null) {
            return app.getCurrentLocation().getAccuracy();
        } else {
            return 0;
        }
    }

    public void clearPOIs() {
        try {
            if (mapplsMap == null)
                return;
            mapplsMap.removeAnnotations();
            if (directionPolylinePlugin != null)
                directionPolylinePlugin.removeAllData();
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public boolean onMapLongClick(@NonNull LatLng latLng) {
//        Toast.makeText(this, "Long clicked", Toast.LENGTH_SHORT).show();

        return false;
    }

    Fragment fragmentOnTopOfStack() {
        int index = getSupportFragmentManager().getBackStackEntryCount() - 1;
        if (index >= 0) {
            FragmentManager.BackStackEntry backEntry = getSupportFragmentManager().getBackStackEntryAt(index);
            String tag = backEntry.getName();
            return getSupportFragmentManager().findFragmentByTag(tag);
        } else {
            return null;
        }
    }

    Fragment getFragmentOnTopOfBackStack() {
        int index = getSupportFragmentManager().getBackStackEntryCount() - 1;
        if (index >= 0) {
            FragmentManager.BackStackEntry backEntry = getSupportFragmentManager().getBackStackEntryAt(index);
            String tag = backEntry.getName();
            Timber.i(tag, " fragment Tag");
            return getSupportFragmentManager().findFragmentByTag(tag);
        } else {
            return null;
        }
    }

    private void onBackStackChangedWithDelay() {
        currentFragment = getFragmentOnTopOfBackStack();

        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {

            finish();
        }
    }

    public void addTripDataToRealm() {
        //  Date startTime = Calendar.getInstance().getTime();


        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(realm1 -> {

                RealmResults<RecentTripRealmModule> results = realm1.where(RecentTripRealmModule.class).findAll();
                RecentTripRealmModule recentTripRealmModule = realm1.createObject(RecentTripRealmModule.class);
                RiderProfileModule riderProfile = realm1.where(RiderProfileModule.class).equalTo("id", 1).findFirst();

                Date d=new Date();
                SimpleDateFormat sdf=new SimpleDateFormat("hh:mm a");
                String startTime = sdf.format(d);

                Calendar currentTimeNow = Calendar.getInstance();
                System.out.println("Current time now : " + currentTimeNow.getTime());
                currentTimeNow.add(Calendar.MINUTE, Integer.parseInt(duration));
                long getETA = System.currentTimeMillis()+(Integer.parseInt(duration)*1000);
                String dateString = DateFormat.format("hh:mm a", new Date(getETA)).toString();

                SharedPreferences prefs = getApplicationContext().getSharedPreferences("top_speed", MODE_PRIVATE);
                int saved_speed = prefs.getInt("new_top_speed", 0);

                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("vehicle_data", MODE_PRIVATE);
                vehicleType = sharedPreferences.getString("vehicle_type","");

                if(vehicleType!=null) {
                    //tvVehicleType.setText(vehicleType);
                }

                //getting end time from navigation



                SharedPreferences conn = getSharedPreferences("endTimeAppPref", MODE_PRIVATE);
                endTime = conn.getString("endTime","");

                int tripSize = results.size();
                Date date = new Date();
                tripID = (int) new Date().getTime();


                recentTripRealmModule.setDate(getDate());
                recentTripRealmModule.setId(tripID);
                for (LatLng latLng : viaPoints) {
                    ViaPointLocationRealmModel model = new ViaPointLocationRealmModel();
                    model.setLatitude(latLng.getLatitude());
                    model.setLongitude(latLng.getLongitude());
                    recentTripRealmModule.setPointLocationRealmModels(model);
                }
                recentTripRealmModule.setTime(String.format("%s ", NavigationFormatter.getFormattedDuration(Integer.parseInt(duration), getMyApplication())));
                recentTripRealmModule.setTotalDistance(String.format("%s", NavigationFormatter.getFormattedDistance(Float.parseFloat(rideTime), getMyApplication())));
                recentTripRealmModule.setRideTime(String.format("%s ", NavigationFormatter.getFormattedDuration(Integer.parseInt(duration), getMyApplication())));
                recentTripRealmModule.setStartlocation(currentPlaceName);
                //  recentTripRealmModule.setTopSpeed(saved_speed);
                //recentTripRealmModule.setTopSpeed(0);

                recentTripRealmModule.setEndlocation(destionationPlaceName);
                recentTripRealmModule.setFavorite(false);
                recentTripRealmModule.setCurrent_lat(String.valueOf(currentLat));
                recentTripRealmModule.setCurrent_long(String.valueOf(currentLong));
                recentTripRealmModule.setStartTime(startTime);
                recentTripRealmModule.setETA(dateString);
                recentTripRealmModule.setDestination_lat(String.valueOf(destinyLat));
                recentTripRealmModule.setDestination_long(String.valueOf(destinyLong));
                recentTripRealmModule.setDateTime(date);
                recentTripRealmModule.setTopSpeed(top_speeds);
                if(vehicleType!=null) {
                    recentTripRealmModule.setVehicleType(vehicleType);
                }


                Log.e("Top_speed_saved", String.valueOf(top_speeds));

                realm1.insertOrUpdate(recentTripRealmModule);


//                    tvDistance.setText(String.format("%s", NavigationFormatter.getFormattedDistance(mStateModel.trip.distance().floatValue(), getMyApplication())));
//                    tvTimeForTravel.setText(String.format("%s ", NavigationFormatter.getFormattedDuration(mStateModel.trip.duration().intValue(), getMyApplication())));


                if (tripSize > 10) {
                    RealmResults<RecentTripRealmModule> recentTrip = realm1.where(RecentTripRealmModule.class)
                            .sort("dateTime", Sort.ASCENDING)
                            .findAll();

                    recentTrip.get(0).deleteFromRealm();
                }
            });

        }


        catch (Exception e) {
            Log.d("realmex", "--" + e.getMessage());

        }
    }


    @Override
    public void onBackStackChanged() {
        backStackHandler.removeCallbacksAndMessages(null);
        backStackHandler.postDelayed(backStackRunnable, 100);
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


    public void getReverseGeoCode(LatLng latLng) {

     //   showProgress();
        MapplsReverseGeoCode reverseGeoCode = MapplsReverseGeoCode.builder()
                .setLocation(latLng.getLatitude(), latLng.getLongitude())
                .build();

        MapplsReverseGeoCodeManager.newInstance(reverseGeoCode).call(new OnResponseCallback<PlaceResponse>() {
            @Override
            public void onSuccess(PlaceResponse placeResponse) {
                if (placeResponse != null) {
                    List<Place> placesList = placeResponse.getPlaces();
                    Place place = placesList.get(0);

                    ELocation eLocation = new ELocation();
                    eLocation.entryLongitude = latLng.getLongitude();

                    eLocation.longitude = latLng.getLongitude();
                    eLocation.entryLatitude = latLng.getLatitude();
                    eLocation.latitude = latLng.getLatitude();
                    eLocation.placeName = place.getFormattedAddress();

                    eLocation.placeAddress = getString(R.string.point_on_map);
                    if (currentFragment != null) {
                        try {
                            ((MapMainFragment) currentFragment).showInfoOnLongClick(eLocation);
                        } catch (Exception e) {
                            //ignore
                        }
                    }
                }
               // hideProgress();
            }

            @Override
            public void onError(int i, String s) {
                Toast.makeText(NavigationActivity.this, s, Toast.LENGTH_LONG).show();
               // hideProgress();
            }
        });
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onMapError(int i, String s) {

    }

    @SuppressWarnings({"MissingPermission"})
    private void enableLocationComponent(Style style) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            LocationComponentOptions options = LocationComponentOptions.builder(this)
                    .trackingGesturesManagement(true)
                    .accuracyColor(ContextCompat.getColor(this, R.color.colorAccent))
                    .build();

            // Get an instance of the component
            LocationComponent locationComponent = mapplsMap.getLocationComponent();
            LocationComponentActivationOptions activationOptions = LocationComponentActivationOptions.builder(this, style)
                    .locationComponentOptions(options)
                    .locationEngine(navigationLocationEngine)
                    .build();

            // Activate with options
            locationComponent.activateLocationComponent(activationOptions);

            locationComponent.setCompassEngine(new NavigationCompassEngine());

//            locationComponent.setMaxAnimationFps(15);

            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setRenderMode(RenderMode.COMPASS);


        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    public MapplsMap getMapboxMap() {
        if (mapplsMap != null)
            return mapplsMap;
        return null;
    }


    public BearingIconPlugin getBearingIconPlugin() {
        return _bearingIconPlugin;
    }

    private void showExitDialog() {
        Dialog dialog = new Dialog(this, R.style.custom_dialog);
        dialog.setContentView(R.layout.custom_dialog);

        TextView tvAlertText = dialog.findViewById(R.id.tvAlertText);
        tvAlertText.setText("Do you want to exit from Navigation?");
        ImageView ivCross = dialog.findViewById(R.id.ivCross);
        ivCross.setOnClickListener(v -> dialog.dismiss());

        ImageView ivCheck = dialog.findViewById(R.id.ivCheck);


        ivCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(DataRequestManager.isSaveTripsClikced) {
                    addTripDataToRealm();
                }
                MapplsNavigationHelper.getInstance().stopNavigation();
                navigationModeEnabled = "0";
//                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(NavigationActivity.this);
//                SharedPreferences.Editor editor = preferences.edit();
//                editor.putString("endTime",endTime);
//                editor.apply();
          //      Toast.makeText(app, ""+endTime, Toast.LENGTH_SHORT).show();
                dialog.dismiss();

                if(isLastParkedLocation){

                    Intent intent = new Intent(NavigationActivity.this, LastParkedLocationActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

                    startActivity(intent);
                }
                else{
                    finish();
                }

            }
        });

        dialog.show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onClusterDataRecev(ClusterStatusPktPojo event) {
        if(DataRequestManager.isSaveTripsClikced) {
            ClusterDataPacket(event.getClusterByteData());
        }
    }

    public void ClusterDataPacket(byte[] u1_buffer) {
        if ((u1_buffer[0] == -91) && (u1_buffer[1] == 55) && (u1_buffer[29] == 127)) {
            byte Crc = calculateCheckSum(u1_buffer);

            if (u1_buffer[28] == Crc) {
                try {
                    String cluster_data = new String(u1_buffer);

                    //String tripA = cluster_data.substring(11, 17);
                    //int Max_speed;
                    Max_speed = Integer.parseInt(cluster_data.substring(2, 5));

                    if (Max_speed > top_speeds) {
                        top_speeds = Max_speed;
                    }

                    Log.e("Top_speed_data", String.valueOf(top_speeds));
                } catch (NumberFormatException e) {
                    top_speeds=0;
                    e.printStackTrace();
                }


            }
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
            showExitDialog();
//            finish();
        } else {
            super.onBackPressed();
        }

    }
    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {

    }

    @Override
    public void onMapReady(MapplsMap map) {
        try {
            if (map == null)
                return;
            this.mapplsMap = map;
            map.enableTraffic(true);  // MMI changes
            mapplsMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    try {
//                        TrafficPlugin trafficPlugin = new TrafficPlugin(mapView, mapplsMap);
//                        trafficPlugin.setEnabled(false);
                    } catch (Exception e) {
                        Timber.e(e);
                    }
                    directionPolylinePlugin = new DirectionPolylinePlugin(mapView, map);
                    routeArrowPlugin = new RouteArrowPlugin(mapView, mapplsMap);
                    mapEventsPlugin = new MapEventsPlugin(mapView, mapplsMap);
                    enableLocationComponent(style);
                    directionPolylinePlugin.setEnableCongestion(true);

                    _bearingIconPlugin = new BearingIconPlugin(mapView, mapplsMap);
                    map.setInfoWindowAdapter(NavigationActivity.this);
                    mapplsMap.setMaxZoomPreference(18.5);
                    mapplsMap.setMinZoomPreference(4);
                    setCompassDrawable();
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

   public RouteArrowPlugin getRouteArrowPlugin() {
        return routeArrowPlugin;
    }

    public MapEventsPlugin getMapEventPlugin() {
        return mapEventsPlugin;
    }

    public void setCompassDrawable() {
        mapView.getCompassView().setBackgroundResource(R.drawable.compass_background);
        mapplsMap.getUiSettings().setCompassImage(ContextCompat.getDrawable(this, R.drawable.compass_north_up));
        int padding = dpToPx( 8);
        int elevation = dpToPx( 8);
        mapView.getCompassView().setPadding(padding, padding, padding, padding);
        ViewCompat.setElevation(mapView.getCompassView(), elevation);
        mapplsMap.getUiSettings().setCompassMargins(dpToPx(20),dpToPx(120),dpToPx(20),dpToPx(20));
    }

    public  int dpToPx(final float dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

}
