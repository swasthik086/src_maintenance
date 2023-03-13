package com.suzuki.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.ViewCompat;

import com.clj.fastble.BleManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;

import com.mappls.sdk.geojson.LineString;
import com.mappls.sdk.geojson.Point;
import com.mappls.sdk.geojson.utils.PolylineUtils;
import com.mappls.sdk.maps.MapView;
import com.mappls.sdk.maps.MapplsMap;
import com.mappls.sdk.maps.OnMapReadyCallback;
import com.mappls.sdk.maps.annotations.Icon;
import com.mappls.sdk.maps.annotations.IconFactory;
import com.mappls.sdk.maps.annotations.Marker;
import com.mappls.sdk.maps.annotations.MarkerOptions;
import com.mappls.sdk.maps.camera.CameraPosition;
import com.mappls.sdk.maps.camera.CameraUpdateFactory;
import com.mappls.sdk.maps.geometry.LatLng;
import com.mappls.sdk.maps.geometry.LatLngBounds;
import com.mappls.sdk.navigation.MapplsNavigationHelper;
import com.mappls.sdk.navigation.NavLocation;
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
import com.mappls.sdk.services.utils.Constants;
import com.suzuki.R;
import com.suzuki.application.SuzukiApplication;
import com.suzuki.fragment.MapMainFragment;
import com.suzuki.maps.plugins.BearingIconPlugin;
import com.suzuki.maps.plugins.DirectionPolylinePlugin;
import com.suzuki.maps.traffic.TrafficPlugin;
import com.suzuki.pojo.FavouriteTripRealmModule;
import com.suzuki.pojo.RecentTripRealmModule;
import com.suzuki.pojo.ViaPointLocationRealmModel;
import com.suzuki.utils.CurrentLoc;
import com.suzuki.utils.Common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmResults;
import timber.log.Timber;

import static com.suzuki.activity.HomeScreenActivity.TOAST_DURATION;
import static com.suzuki.activity.RouteNearByActivity.dpToPx;
import static com.suzuki.base.BaseActivity.dismissProgress;
import static com.suzuki.base.BaseActivity.showProgress;
import static com.suzuki.utils.Common.BikeBleName;


public class TripDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    static TripDetailsActivity INSTANCE;
    String data="FirstActivity";

    private static final String TAG = TripDetailsActivity.class.getSimpleName();
    private ArrayList<LatLng> listOfLatlang = new ArrayList<>();
    private MapView mapView;
    ImageView ivBack, ivFav, ivEditFav, ivShare;
    private String profile = DirectionsCriteria.PROFILE_DRIVING;
    TextView tvDestinationLoction;
    TextView tvCurrentlocation;
    TextView tvDateTime;
    TextView tvTripName;
    TextView tvTotalDistance;
    TextView tvRideTime;
    TextView tvtimeLt10;
    TextView tvTopSpeed;
    TextView ridestarttime;
    TextView rideendtime;
    EditText etTripName;
    String textFromUser, topspeed, ridetimeLt10;
    Realm realm;
    int id;
    boolean forFavAdd;
    Date dateparse;
    LinearLayout llviewToShare;
    String tripName;
    List<FavouriteTripRealmModule> list;
    RealmList<ViaPointLocationRealmModel> viaPointRealmList=new RealmList<>();

    File imagePath;
    RealmResults<FavouriteTripRealmModule> favTrip;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    RelativeLayout rlNavigate;

    public ELocation eLocation;
    public Location currentlocation;
    StateModel mStateModel;
    private DirectionsResponse trip;
    private DirectionPolylinePlugin directionPolylinePlugin;
    BearingIconPlugin _bearingIconPlugin;
    ArrayList<LatLng> viaPointList = new ArrayList<>();

    private boolean shareButtonClicked;
    Common common;

   // RealmList<ViaPointLocationRealmModel> viaPointRealmList;
    private void verifyStoragePermissions() {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don'timerService have permission so prompt the user
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        } else {
//           captureScreen();
        }
    }

    private static class StateModel {
        private ELocation eLocation;
        private DirectionsResponse trip;

    }

    private NavLocation startNavigationLocation;
    private SuzukiApplication app;
    String cuurent_lat, current_long, destiny_lat, destiny_long, rideTime, totalDist, StartTime, EndTime;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        INSTANCE=this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trip_details_activity);
        FirebaseApp.initializeApp(this);
        realm = Realm.getDefaultInstance();
        common = new Common(this);

        mapView = findViewById(R.id.mapBoxId);
        mapView.onCreate(savedInstanceState);
        ivBack = (ImageView) findViewById(R.id.ivBack);
        verifyStoragePermissions();
        mStateModel = new StateModel();

        tvDestinationLoction = (TextView) findViewById(R.id.tvDestinationLoction);
        rlNavigate = (RelativeLayout) findViewById(R.id.rlNavigate);
        tvCurrentlocation = (TextView) findViewById(R.id.tvCurrentlocation);
        tvDateTime = (TextView) findViewById(R.id.tvDateTime);
        ivFav = (ImageView) findViewById(R.id.ivFav);
        ivEditFav = (ImageView) findViewById(R.id.ivEditFav);
        tvTripName = (TextView) findViewById(R.id.tvTripName);
        llviewToShare = findViewById(R.id.llviewToShare);
        ivShare = findViewById(R.id.ivShare);
        tvTotalDistance = (TextView) findViewById(R.id.tvTotalDistance);
        tvRideTime = (TextView) findViewById(R.id.tvRideTime);
        tvTopSpeed = (TextView) findViewById(R.id.tvTopSpeed);
        rideendtime=(TextView)findViewById(R.id.rideendtime);
        ridestarttime=(TextView)findViewById(R.id.ridestarttime);
        tvtimeLt10 = (TextView) findViewById(R.id.tvtimeLt10);
        Intent intent = getIntent();

//        getDynamicLink();

        String endLoc = intent.getStringExtra("endLoc");

//        if (endLoc==null){
////            startActivity(new Intent(TripDetailsActivity.this,DeepLinkLandingActivity.class));
////            finish();
//            return;
//        }

        String startLoc = intent.getStringExtra("startLoc");
        id = intent.getIntExtra("clickedPositon", 0);
        String date = intent.getStringExtra("date");
        String time = intent.getStringExtra("time");
        String dateTime = intent.getStringExtra("dateTime");
        cuurent_lat = intent.getStringExtra("cuurent_lat");
        current_long = intent.getStringExtra("current_long");
        destiny_lat = intent.getStringExtra("destiny_lat");
        destiny_long = intent.getStringExtra("destiny_long");
        rideTime = intent.getStringExtra("rideTime");
        StartTime=intent.getStringExtra("rideStartTime");
        EndTime=intent.getStringExtra("rideEndTime");
        totalDist = intent.getStringExtra("totalDistance");
        Boolean clicked = getIntent().getExtras().getBoolean("clicked");
        tripName = intent.getStringExtra("tripName");
        topspeed = intent.getStringExtra("topspeed");
        ridetimeLt10 = intent.getStringExtra("timelt10");
        viaPointList = intent.getParcelableArrayListExtra("viaPointList");
        tvTripName.setText(tripName);
        Log.d("kskksksksks", "---" + topspeed + ridetimeLt10);
        forFavAdd = clicked;
        eLocation = new ELocation();
        eLocation.latitude = Double.valueOf(String.valueOf(destiny_lat));
        eLocation.longitude = Double.valueOf(String.valueOf(destiny_long));
        tvRideTime.setText(rideTime);
        ridestarttime.setText(StartTime);
        rideendtime.setText(EndTime);
        tvTotalDistance.setText(totalDist);
        tvTopSpeed.setText(topspeed + " km/hr");
      //  Toast.makeText(TripDetailsActivity.this, ""+topspeed, Toast.LENGTH_SHORT).show();
        tvtimeLt10.setText(ridetimeLt10 + " mins");
        tvCurrentlocation.setText(startLoc);
        tvDestinationLoction.setText(endLoc);


        SimpleDateFormat sdf3 = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
        SimpleDateFormat formatDate = new SimpleDateFormat("E, MMM dd yyyy");
        SimpleDateFormat formatTime = new SimpleDateFormat("hh:mm a");
//          dateparse= new Date();
        try {
            dateparse = sdf3.parse(dateTime);
            tvDateTime.setText(formatDate.format(dateparse) + ", " + formatTime.format(dateparse));
        } catch (ParseException e) {
            e.printStackTrace();
        }


        if (clicked) {


            ivFav.setImageResource(R.drawable.favor);
            ivEditFav.setVisibility(View.VISIBLE);

        } else {

            ivFav.setImageResource(R.drawable.fav);
            ivEditFav.setVisibility(View.GONE);

        }


        favTrip = realm.where(FavouriteTripRealmModule.class).findAll();


        favTrip.addChangeListener(new RealmChangeListener<RealmResults<FavouriteTripRealmModule>>() {
            @Override
            public void onChange(RealmResults<FavouriteTripRealmModule> tripRealmModules) {


                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {

                        favTrip = realm.where(FavouriteTripRealmModule.class)
                                .findAll();


                    }
                });

            }
        });

        ivFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (forFavAdd) {
                    forFavAdd = false;
//                    if(viaPointRealmList!=null){
//                        for (ViaPointLocationRealmModel model : viaPointRealmList){
//                            LatLng latLng = new LatLng();
//                            latLng.setLatitude(model.getLatitude());
//                            latLng.setLongitude(model.getLongitude());
//                            viaPointList.add(latLng);
//                        }
//                    }

                    addFavouriteTripDataToRealm(date, dateparse, id, time, startLoc, endLoc,rideTime,totalDist, forFavAdd, cuurent_lat, current_long, destiny_lat, destiny_long, tripName, topspeed, ridetimeLt10, viaPointRealmList,StartTime,EndTime);
                    updateRecentData(realm, id, forFavAdd);

                    ivFav.setImageResource(R.drawable.fav);
                    ivEditFav.setVisibility(View.GONE);

                }
                else if (forFavAdd == false) {
                    forFavAdd = true;
//                    if(viaPointRealmList!=null){
//                        for (ViaPointLocationRealmModel model : viaPointRealmList){
//                            LatLng latLng = new LatLng();
//                            latLng.setLatitude(model.getLatitude());
//                            latLng.setLongitude(model.getLongitude());
//                            viaPointList.add(latLng);
//                        }
//                    }

                    addFavouriteTripDataToRealm(date, dateparse, id, time, startLoc, endLoc,rideTime,totalDist, forFavAdd, cuurent_lat, current_long, destiny_lat, destiny_long, tripName, topspeed, ridetimeLt10,viaPointRealmList,StartTime,EndTime);

                    updateRecentData(realm, id, forFavAdd);
                    ivFav.setImageResource(R.drawable.favor);
                    ivEditFav.setVisibility(View.VISIBLE);
                }

            }
        });
        ivEditFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Dialog dialog = new Dialog(TripDetailsActivity.this, R.style.custom_dialog);
                dialog.setContentView(R.layout.custom_dialog_for_trip);
                etTripName = dialog.findViewById(R.id.etTripName);
                etTripName.requestFocus();

                TextView tvCustomTextBtn = dialog.findViewById(R.id.tvCustomTextBtn);
                LinearLayout llSave = dialog.findViewById(R.id.llSave);
                tvCustomTextBtn.setText("Save");
                ImageView ivCustomClose = dialog.findViewById(R.id.ivCustomClose);
                ivCustomClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                });


            //    textFromUser = etTripName.getText().toString();

                llSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        tvTripName.setText(etTripName.getText().toString());
                        try {
                            realm.executeTransaction(new Realm.Transaction() {

                                @Override
                                public void execute(Realm realm) {

                                    FavouriteTripRealmModule favouriteTripRealmModule = realm.where(FavouriteTripRealmModule.class).equalTo("id", id).findFirst();
                                    if (favouriteTripRealmModule == null) {

                                    }
                                    else {
                                        favouriteTripRealmModule.setTrip_name(etTripName.getText().toString());
                                        realm.insertOrUpdate(favouriteTripRealmModule);
                                    }

                                    RecentTripRealmModule recentTripRealmModule = realm.where(RecentTripRealmModule.class).equalTo("id", id).findFirst();

                                    if (recentTripRealmModule == null) {

                                    } else {
                                        recentTripRealmModule.setTrip_name(etTripName.getText().toString());

                                        realm.insertOrUpdate(recentTripRealmModule);
                                    }
                                }
                            });
                        } catch (Exception e) {
                            Log.d("realmex", "--" + e.getMessage());

                        }

                        dialog.cancel();
                    }
                });
                dialog.show();

            }
        });


        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        rlNavigate.setOnClickListener(v -> {
            if (BleManager.getInstance().getAllConnectedDevice() != null &&
                    BleManager.getInstance().getAllConnectedDevice().size() > 0)
                startNavigation();

            else
                common.showToast("You are not connected to any vehicle",TOAST_DURATION);
        });


        ivShare.setOnClickListener(v -> {
            if (!shareButtonClicked) {
                shareButtonClicked = true;
//                        shareViaDeepLink();
                shareIt();
            }
        });


        mapView.getMapAsync(this);
    }


    public static TripDetailsActivity getActivityInstance()
    {
        return INSTANCE;
    }

    public String getData()
    {
        return this.data;
    }


    private void checkViaPoints() {
    }

    public void setStartNavigationLocation(NavLocation startNavigationLocation) {
        this.startNavigationLocation = startNavigationLocation;
    }

    public SuzukiApplication getMyApplication() {
        return ((SuzukiApplication) getApplication());
    }

    public NavLocation getUserLocation() {
//        if (currentlocation != null) {
        NavLocation loc = new NavLocation("router");
        loc.setLatitude(Double.parseDouble(cuurent_lat));
        loc.setLongitude(Double.parseDouble(current_long));
        return loc;

//        } else {
//            return null;
//        }
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
                else {
                    Location loc = new CurrentLoc().getCurrentLoc(getApplicationContext());
                    if (loc != null) {
                        currentLocation = new LatLng(loc.getLatitude(), loc.getLongitude());
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
                
                Log.d("tag", "onSuggestionListItemClicked: eLoc async " + eLocation + "   " + viaPointList);

                app.setViaPoints(viaPointList);
                app.setELocation(eLocation);

                List<WayPoint> wayPoints = new ArrayList<>();
                for (LatLng latLng : viaPointList) {
                    wayPoints.add(new WayPoint(latLng.getLatitude(), latLng.getLongitude(), null));
                }

                return MapplsNavigationHelper.getInstance().startNavigation(app.getTrip(),
                        0, currentLocation,
                        getNavigationGeoPoint(eLocation), wayPoints, BikeBleName.getValue());


            } catch (Exception e) {
                Timber.e(e);
                return new NavigationResponse(ErrorType.UNKNOWN_ERROR, e);
            }

        }

        @Override
        protected void onPostExecute(NavigationResponse result) {

            dismissProgress();
            if (result != null && result.getError() != null) {
                if (result.getError().errorCode == 409) {
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
                    /*new AlertDialog.Builder(TripDetailsActivity.this)
                            .setMessage(getApplicationContext().getResources().getString(R.string.Session_Message))
                            .setTitle("Navigation Alert")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    MapplsNavigationHelper.getInstance().deleteSession(BikeBleName, new IStopSession() {
                                        @Override
                                        public void onSuccess() {
                                            LongOperation operation = new LongOperation();
                                            operation.execute();
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
                } else {
                    Log.d(TAG, "onPostExecute: error : " + result.getError().errorMessage);
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
                NavLocation location = getUserLocation();
                if (location != null)
                    currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                NavLocation navLocation = new NavLocation("navigation");
                Point position = mStateModel.trip.legs().get(0).steps().get(0).maneuver().location();
                LatLng point = new LatLng(position.latitude(), position.longitude());
                navLocation.setLongitude(point.getLongitude());
                navLocation.setLatitude(point.getLatitude());
                app = getMyApplication();
                setStartNavigationLocation(navLocation);
                if (currentLocation == null)
                    return false;


                Log.d("kkkjhhj", "--" + app.getTrip());

                return MapplsNavigationHelper.getInstance().startNavigation(app, app.getTrip(), currentLocation,
                        getNavigationGeoPoint(eLocation), null);


            } catch (Exception e) {
                Timber.e(e);
                Log.d("jkkkkhkhkh", "--" + e.getMessage());
                return false;
            }

        }

        @Override
        protected void onPostExecute(Boolean result) {

            if (this == null)
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


    public void startNavigation() {

        Log.d(TAG, "startNavigation: ");

        NavLocation location = getUserLocation();

        if (location == null) {
            return;
        }
        showProgress(this);

        LongOperation operation = new LongOperation();
        operation.execute();
    }

    private void getDynamicLink() {
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        Log.d("TripDetailsActivity", "onSuccess: " + pendingDynamicLinkData);
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                            if (deepLink != null && deepLink.getQueryParameter("location") != null) {
                                String[] location = deepLink.getQueryParameter("location").split(",");
                                double latitude = Double.parseDouble(location[0]);
                                double longitude = Double.parseDouble(location[1]);
                                Log.e("TripDetailsActivity", String.format("received location's latitude %f, longitude %f", latitude, longitude));
                            } else if (deepLink != null && deepLink.getQueryParameter("navigation") != null) {
                                String[] locations = deepLink.getQueryParameter("navigation").split(";");
                                List<LatLng> latLngList = new ArrayList<>();
                                for (int i = 0; i < locations.length; i++) {
                                    String[] locs = locations[i].split(",");
                                    if (locs.length > 1) {
                                        latLngList.add(new LatLng(Double.parseDouble(locs[0]), Double.parseDouble(locs[1])));
                                        Log.e("TripDetailsActivity", String.format("received direction's latitude %s, longitude %s", locs[0], locs[1]));
                                    }
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("TripDetailsActivity", "onException ");
                        Log.e("TripDetailsActivity", "getDynamicLink:onFailure", e);
                    }
                });
    }

    private void shareViaDeepLink() {

        String viaPoint = "";
        String url;
        if (viaPointList != null && viaPointList.size() > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            int placeCounter = 1;
            for (LatLng latLng : viaPointList) {
                stringBuilder.append(latLng.getLatitude()).append(",").append(latLng.getLongitude())
                        .append(",ViaPoint").append(placeCounter).append(";");
                placeCounter++;
            }
            viaPoint = stringBuilder.toString();
        }

        if (viaPoint.length() > 1) {
            url = "" + cuurent_lat + "," + current_long + ",Starting;"
                    + viaPoint + destiny_lat + "," + destiny_long + ",Destination";
        } else {
            url = "" + cuurent_lat + "," + current_long + ",Starting;"
                    + destiny_lat + "," + destiny_long + ",Destination";
        }
        LatLng origin = new LatLng(27.15, 77.99);
        LatLng destination = new LatLng(27.485, 77.67);

        //Generate short dynamic link
        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://linkmmi.com?navigation=" + url))
                .setDomainUriPrefix("https://suzu.page.link/")
                .setAndroidParameters(
                        new DynamicLink.AndroidParameters.Builder("suzuki.com.suzuki")
                                .setFallbackUrl(Uri.parse("https://maps.mapmyindia.com/navigation?places=" + url))
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
                        Intent shareIntent = Intent.createChooser(sendIntent, "share using");
                        startActivity(shareIntent);

                    } else {
                        shareButtonClicked = false;
                        Log.e("link", task.getException().getLocalizedMessage());
                    }
                });
    }

    private void shareIt() {

        String viaPoint = "";
        String url;
        if (viaPointList != null && viaPointList.size() > 0) {
            StringBuilder stringBuilder = new StringBuilder();
            int placeCounter = 1;
            for (LatLng latLng : viaPointList) {
                stringBuilder.append(latLng.getLatitude()).append(",").append(latLng.getLongitude())
                        .append(",ViaPoint").append(placeCounter).append(";");
                placeCounter++;
            }
            viaPoint = stringBuilder.toString();
        }

        if (viaPoint.length() > 1) {
            url = "https://maps.mappls.com/navigation?places=" + cuurent_lat + "," + current_long + ",Starting;"
                    + viaPoint + destiny_lat + "," + destiny_long + ",Destination";
        } else {
            url = "https://maps.mappls.com/navigation?places=" + cuurent_lat + "," + current_long + ",Starting;"
                    + destiny_lat + "," + destiny_long + ",Destination";
        }


        String url1 = "https://maps.mappls.com/@" + destiny_lat + "," + destiny_long;
        if (destiny_lat != null) {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            String shareBody = "" + url + "\n" + "Ride time : " + rideTime + "\n" + "Distance : " + totalDist;
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Ride details");
//            sharingIntent.putExtra(Intent.EXTRA_TEXT, "Ride time : " + rideTime);
            sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
            sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            shareButtonClicked = false;
            startActivity(Intent.createChooser(sharingIntent, "Share via"));
        } else {
            shareButtonClicked = false;
            common.showToast("Sorry,Could fetch data.Please try later!", TOAST_DURATION);
        }

    }

    public Bitmap takeScreenshot() {
        View rootView = findViewById(R.id.llviewToShare).getRootView();
        rootView.setDrawingCacheEnabled(true);
        return rootView.getDrawingCache();
    }

    public void saveBitmap(Bitmap bitmap) {
        imagePath = new File(Environment.getExternalStorageDirectory() + "/screenshot.png");
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(imagePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            Log.e("GREC", e.getMessage(), e);
        } catch (IOException e) {
            Log.e("GREC", e.getMessage(), e);
        }
    }


    public DirectionsResponse getTrip() {
        return trip;
    }

    public void setTrip(DirectionsResponse trip) {
        this.trip = trip;
    }


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

        LatLngBounds latLngBounds = new LatLngBounds.Builder().includes(listOfLatlang).build();
        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 70));
    }


    private void drawPath(List<Point> waypoints, MapplsMap mapboxMap) {
        listOfLatlang = new ArrayList<>();
        for (Point point : waypoints) {
            listOfLatlang.add(new LatLng(point.latitude(), point.longitude()));
        }


        Log.d("geopoosos", "-" + listOfLatlang);

        myDrawing(mapboxMap);

//        mapmyIndiaMap.addPolyline(new PolylineOptions().addAll(listOfLatlang).color(Color.parseColor("#3bb2d0")).width(4));
//        LatLngBounds latLngBounds = new LatLngBounds.Builder().includes(listOfLatlang).build();
//        mapmyIndiaMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 30));
    }

    private void getRoute(final ArrayList<LatLng> wayPoints, final ELocation eLocation, MapplsMap mapboxMap) {
        if (directionPolylinePlugin != null)
            directionPolylinePlugin.removeAllData();
        if (wayPoints == null || wayPoints.size() < 2)
            return;
        LatLng startLocation = new LatLng();
        startLocation.setLongitude(wayPoints.get(0).getLongitude());
        startLocation.setLatitude(wayPoints.get(0).getLatitude());
        Point origin = Point.fromLngLat(wayPoints.get(0).getLongitude(), wayPoints.get(0).getLatitude());
        Point destination = Point.fromLngLat(wayPoints.get(wayPoints.size() - 1).getLongitude(), wayPoints.get(wayPoints.size() - 1).getLatitude());

        MapplsDirections.Builder directions = MapplsDirections.builder()
                .origin(origin)
                .steps(true)
                .resource(DirectionsCriteria.RESOURCE_ROUTE_ETA)
                .profile(DirectionsCriteria.PROFILE_BIKING)
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

    public LatLng getPoint(ELocation eLocation) {
        try {
            return new LatLng(Double.parseDouble(String.valueOf(eLocation.latitude)), Double.parseDouble(String.valueOf(eLocation.longitude)));
        } catch (Exception e) {
            return new LatLng(0, 0);
        }
    }

    @Override
    public void onMapReady(MapplsMap mapboxMap) {
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
                    ArrayList<LatLng> geoPoints = new ArrayList<>();

                    geoPoints.add(new LatLng(Double.parseDouble(cuurent_lat), Double.parseDouble(current_long)));
                    geoPoints.add(getPoint(eLocation));
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
        mapboxMap.setMaxZoomPreference(18.5);

    }

    @Override
    public void onMapError(int i, String s) {

    }

    void setCompassDrawable(MapplsMap mapboxMap) {
        mapView.getCompassView().setBackgroundResource(R.drawable.compass_background);
        mapboxMap.getUiSettings().setCompassImage(getDrawable(R.drawable.compass_north_up));
        int padding = dpToPx(8);
        int elevation = dpToPx(18);
        mapView.getCompassView().setPadding(padding, padding, padding, padding);
        ViewCompat.setElevation(mapView.getCompassView(), elevation);
    }

    protected CameraPosition setCameraAndTilt() {
        CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(
                28.551087, 77.257373)).zoom(5).tilt(0).build();
        return cameraPosition;
    }

    public void myDrawing(MapplsMap mapboxMap) {
//        mapboxMap.addPolyline(new PolylineOptions().addAll(listOfLatlang).color(Color.parseColor("#205fa4")).width(4));


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
        /* this is done for animating/moving camera to particular position */

        LatLngBounds latLngBounds = new LatLngBounds.Builder().includes(listOfLatlang).build();
        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 70));

    }



    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

//    private void addFavouriteTripDataToRealm(String date, Date dateTime, int id, String time, String startLoc, String endLoc, String clicked, String current_lat, boolean current_long, String destiny_lat, String destiny_long, String tripName, String rideTime, String totalDistance, String topspeed, String timelt10, ArrayList<LatLng> viaPointRealmList) {
//        Realm realm = Realm.getDefaultInstance();
//        try {
//
//            realm.executeTransaction(new Realm.Transaction() {
//
//                @Override
//                public void execute(Realm realm) {
//
//                    RealmResults<FavouriteTripRealmModule> results = realm.where(FavouriteTripRealmModule.class).findAll();
//
//                    FavouriteTripRealmModule favouriteTripRealmModule = realm.createObject(FavouriteTripRealmModule.class);
//
//                    FavouriteTripRealmModule favtripUpdateModel = realm.where(FavouriteTripRealmModule.class).equalTo("id", id).findFirst();
//
//                    int tripSize = results.size();
//
//                    if (clicked) {
//
//                        if (tripSize < 11) {
//
//                            if (favtripUpdateModel == null) {
//                                for (ViaPointLocationRealmModel model : viaPointRealmList){
//                                    favouriteTripRealmModule.setPointLocationRealmModels(model);
//                                }
//                                favouriteTripRealmModule.setDate(date);
//                                favouriteTripRealmModule.setId(id);
//                                favouriteTripRealmModule.setTime(time);
//                                favouriteTripRealmModule.setStartlocation(startLoc);
//                                favouriteTripRealmModule.setEndlocation(endLoc);
//                                favouriteTripRealmModule.setFavorite(clicked);
//                                favouriteTripRealmModule.setDateTime(dateTime);
//                                favouriteTripRealmModule.setCurrent_lat(current_lat);
//                                favouriteTripRealmModule.setCurrent_long(current_long);
//                                favouriteTripRealmModule.setDestination_lat(destiny_lat);
//                                favouriteTripRealmModule.setDestination_long(destiny_long);
//                                favouriteTripRealmModule.setDestination_long(tripName);
//                                favouriteTripRealmModule.setRideTime(rideTime);
//                                favouriteTripRealmModule.setTotalDistance(totalDistance);
//                                favouriteTripRealmModule.setTopSpeed(Integer.parseInt(topspeed));
//                                favouriteTripRealmModule.setRideTimeLt10(Integer.parseInt(timelt10));
//                                realm.insert(favouriteTripRealmModule);
//
//                                RealmResults<FavouriteTripRealmModule> resultsafte = realm.where(FavouriteTripRealmModule.class).findAll();
//
//                            } else if (favtripUpdateModel != null) {
//
//                                for (ViaPointLocationRealmModel model : viaPointRealmList){
//                                    favtripUpdateModel.setPointLocationRealmModels(model);
//                                }
//
//                                favtripUpdateModel.setDate(date);
//                                favtripUpdateModel.setId(id);
//                                favtripUpdateModel.setTime(time);
//                                favtripUpdateModel.setStartlocation(startLoc);
//                                favtripUpdateModel.setEndlocation(endLoc);
//                                favtripUpdateModel.setFavorite(clicked);
//                                favtripUpdateModel.setDateTime(dateTime);
//                                favtripUpdateModel.setCurrent_lat(current_lat);
//                                favtripUpdateModel.setCurrent_long(current_long);
//                                favtripUpdateModel.setDestination_lat(destiny_lat);
//                                favtripUpdateModel.setDestination_long(destiny_long);
//                                favtripUpdateModel.setDestination_long(tripName);
//                                favtripUpdateModel.setRideTime(rideTime);
//                                favtripUpdateModel.setTotalDistance(totalDistance);
//                                favouriteTripRealmModule.setTopSpeed(Integer.parseInt(topspeed));
//                                favouriteTripRealmModule.setRideTimeLt10(Integer.parseInt(timelt10));
//                                realm.insertOrUpdate(favtripUpdateModel);
//
//                                RealmResults<FavouriteTripRealmModule> resultsafte = realm.where(FavouriteTripRealmModule.class).findAll();
//                            }
//                        }
//                        else Toast.makeText(getActivity(), "Favourite list is full, Please delete some of your favourites.", Toast.LENGTH_SHORT).show();
//
//                    }
//                    else if (favtripUpdateModel != null) {
//
//                        FavouriteTripRealmModule favtripUpdateModel1 = realm.where(FavouriteTripRealmModule.class).equalTo("id", id).findFirst();
//
//                        favtripUpdateModel1.deleteFromRealm();
//                    }
//
//                    RealmResults<FavouriteTripRealmModule> result = realm.where(FavouriteTripRealmModule.class).equalTo("id", 0).findAll();
//                    result.deleteAllFromRealm();
//                }
//            });
//        } catch (Exception e) {
//            Log.e(EXCEPTION, ClassName+" addFavouriteTripDataToRealm "+e);
//        }
//    }

    private void addFavouriteTripDataToRealm(String date, Date dateTime, int id, String time, String startLoc, String endLoc, String rideTime, String totalDist, boolean clicked, String current_lat, String current_long, String destiny_lat, String destiny_long, String tripName, String topspeed, String ridetimelt10, RealmList<ViaPointLocationRealmModel> viaPointRealmList,String startTime,String endTime) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmResults<FavouriteTripRealmModule> results = realm.where(FavouriteTripRealmModule.class).findAll();
                    FavouriteTripRealmModule favouriteTripRealmModule = realm.createObject(FavouriteTripRealmModule.class);
                    FavouriteTripRealmModule favtripUpdateModel = realm.where(FavouriteTripRealmModule.class).equalTo("id", id).findFirst();
                    int tripSize = results.size();

                    Log.d("daiaia", "-ss" + tripSize + clicked);


                    if (clicked) {

                        if (tripSize < 11) {

                            if (favtripUpdateModel == null) {

                                 for (ViaPointLocationRealmModel model : viaPointRealmList){
                                    favouriteTripRealmModule.setPointLocationRealmModels(model);
                                }

                                favouriteTripRealmModule.setDate(date);
                                favouriteTripRealmModule.setId(id);
                                favouriteTripRealmModule.setTime(time);
                                favouriteTripRealmModule.setStartlocation(startLoc);
                                favouriteTripRealmModule.setEndlocation(endLoc);
                                favouriteTripRealmModule.setFavorite(clicked);
                                favouriteTripRealmModule.setDateTime(dateTime);
                                favouriteTripRealmModule.setCurrent_lat(current_lat);
                                favouriteTripRealmModule.setCurrent_long(current_long);
                                favouriteTripRealmModule.setDestination_lat(destiny_lat);
                                favouriteTripRealmModule.setDestination_long(destiny_long);
                                favouriteTripRealmModule.setRideTime(rideTime);
                                favouriteTripRealmModule.setStartTime(startTime);
                                favouriteTripRealmModule.setETA(endTime);
                                favouriteTripRealmModule.setTotalDistance(totalDist);
                                favouriteTripRealmModule.setTrip_name(tripName);
                                favouriteTripRealmModule.setTopSpeed(Integer.parseInt(topspeed));
                                favouriteTripRealmModule.setRideTimeLt10(Integer.parseInt(ridetimelt10));
                                realm.insert(favouriteTripRealmModule);

                                RealmResults<FavouriteTripRealmModule> resultsafte = realm.where(FavouriteTripRealmModule.class).findAll();

                                for (int i = 0; i < resultsafte.size(); i++) {
                                    Log.d("hjjghj", "ss-dd-" + resultsafte.get(i).getEndlocation() + resultsafte.get(i).getId());

                                }

                            }
                            else if (favtripUpdateModel != null) {

                                for (ViaPointLocationRealmModel model : viaPointRealmList){
                                    favtripUpdateModel.setPointLocationRealmModels(model);
                                }

                                favtripUpdateModel.setDate(date);
                                favtripUpdateModel.setId(id);
                                favtripUpdateModel.setTime(time);
                                favtripUpdateModel.setStartlocation(startLoc);
                                favtripUpdateModel.setEndlocation(endLoc);
                                favtripUpdateModel.setFavorite(clicked);
                                favtripUpdateModel.setDateTime(dateTime);
                                favtripUpdateModel.setCurrent_lat(current_lat);
                                favtripUpdateModel.setCurrent_long(current_long);
                                favtripUpdateModel.setRideTime(rideTime);
                                favtripUpdateModel.setTotalDistance(totalDist);
                                favtripUpdateModel.setStartTime(startTime);
                                favtripUpdateModel.setETA(endTime);
                                favtripUpdateModel.setDestination_lat(destiny_lat);
                                favtripUpdateModel.setDestination_long(destiny_long);
                                favtripUpdateModel.setDestination_long(tripName);
                                favtripUpdateModel.setTopSpeed(Integer.parseInt(topspeed));
                                favtripUpdateModel.setRideTimeLt10(Integer.parseInt(ridetimelt10));
                                realm.insertOrUpdate(favtripUpdateModel);

                                RealmResults<FavouriteTripRealmModule> resultsafte = realm.where(FavouriteTripRealmModule.class).findAll();

                                for (int i = 0; i < resultsafte.size(); i++) {
                                    Log.d("hjjghj", "ss--ssdd" + resultsafte.get(i).getEndlocation() + resultsafte.get(i).getId());

                                }

                            }
                        } else {
                            common.showToast("Favourite list is full, Please delete some of your favourites.", TOAST_DURATION);
                        }

                    } else {
                      //  FavouriteTripRealmModule receFavDataResult = realm.where(FavouriteTripRealmModule.class).equalTo("id", id).findFirst();

                        Log.d("jdjdjdj", "s=" + favtripUpdateModel.getId() + id);

                        if (favtripUpdateModel != null) {
//
                            Log.d("jdjdjdj", "s=" + favtripUpdateModel.getId() + id);
                            FavouriteTripRealmModule favtripUpdateModel1 = realm.where(FavouriteTripRealmModule.class).equalTo("id", id).findFirst();

                            favtripUpdateModel1.deleteFromRealm();


                        }
//                        if (favouriteTripRealmModule != null) {
//                            favouriteTripRealmModule.deleteFromRealm();
//
//                            RealmResults<FavouriteTripRealmModule> resultsafte = realm.where(FavouriteTripRealmModule.class).findAll();
//
//                            Log.d("hjjghj","--"+resultsafte.size());
//                        }


                    }

                    RealmResults<FavouriteTripRealmModule> result = realm.where(FavouriteTripRealmModule.class).equalTo("id", 0).findAll();
                    result.deleteAllFromRealm();

                }
            });

        } catch (Exception e) {
            Log.d("realmex", "--" + e.getMessage());

        }


    }


    private void addFavouriteTripDataToRealmNew(String date, Date dateTime, int id, String time, String startLoc, String endLoc, String rideTime, String totalDist, boolean clicked, String current_lat, String current_long, String destiny_lat, String destiny_long, String tripName, String topspeed, String ridetimelt10, RealmList<ViaPointLocationRealmModel> viaPointRealmList) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmResults<FavouriteTripRealmModule> results = realm.where(FavouriteTripRealmModule.class).findAll();
                    FavouriteTripRealmModule favouriteTripRealmModule = realm.createObject(FavouriteTripRealmModule.class);
                    FavouriteTripRealmModule favtripUpdateModel = realm.where(FavouriteTripRealmModule.class).equalTo("id", id).findFirst();
                    int tripSize = results.size();

                    Log.d("daiaia", "-ss" + tripSize + clicked);


                    if (clicked) {

                        if (tripSize < 11) {

                            if (favtripUpdateModel == null) {

                                for (ViaPointLocationRealmModel model : viaPointRealmList){
                                    favouriteTripRealmModule.setPointLocationRealmModels(model);
                                }
                                favouriteTripRealmModule.setDate(date);
                                favouriteTripRealmModule.setId(id);
                                favouriteTripRealmModule.setTime(time);
                                favouriteTripRealmModule.setStartlocation(startLoc);
                                favouriteTripRealmModule.setEndlocation(endLoc);
                                favouriteTripRealmModule.setFavorite(clicked);
                                favouriteTripRealmModule.setDateTime(dateTime);
                                favouriteTripRealmModule.setCurrent_lat(current_lat);
                                favouriteTripRealmModule.setCurrent_long(current_long);
                                favouriteTripRealmModule.setDestination_lat(destiny_lat);
                                favouriteTripRealmModule.setDestination_long(destiny_long);
                                favouriteTripRealmModule.setRideTime(rideTime);
                                favouriteTripRealmModule.setTotalDistance(totalDist);
                                favouriteTripRealmModule.setTrip_name(tripName);
                                favouriteTripRealmModule.setTopSpeed(Integer.parseInt(topspeed));
                                favouriteTripRealmModule.setRideTimeLt10(Integer.parseInt(ridetimelt10));
                                realm.insert(favouriteTripRealmModule);

                                RealmResults<FavouriteTripRealmModule> resultsafte = realm.where(FavouriteTripRealmModule.class).findAll();

                                for (int i = 0; i < resultsafte.size(); i++) {
                                    Log.d("hjjghj", "ss-dd-" + resultsafte.get(i).getEndlocation() + resultsafte.get(i).getId());

                                }

                            }
                            else if (favtripUpdateModel != null) {

                                for (ViaPointLocationRealmModel model : viaPointRealmList){
                                    favouriteTripRealmModule.setPointLocationRealmModels(model);
                                }

                                favtripUpdateModel.setDate(date);
                                favtripUpdateModel.setId(id);
                                favtripUpdateModel.setTime(time);
                                favtripUpdateModel.setStartlocation(startLoc);
                                favtripUpdateModel.setEndlocation(endLoc);
                                favtripUpdateModel.setFavorite(clicked);
                                favtripUpdateModel.setDateTime(dateTime);
                                favtripUpdateModel.setCurrent_lat(current_lat);
                                favtripUpdateModel.setCurrent_long(current_long);
                                favouriteTripRealmModule.setRideTime(rideTime);
                                favouriteTripRealmModule.setTotalDistance(totalDist);
                                favtripUpdateModel.setDestination_lat(destiny_lat);
                                favtripUpdateModel.setDestination_long(destiny_long);
                                favtripUpdateModel.setDestination_long(tripName);
                                favouriteTripRealmModule.setTopSpeed(Integer.parseInt(topspeed));
                                favouriteTripRealmModule.setRideTimeLt10(Integer.parseInt(ridetimelt10));
                                realm.insertOrUpdate(favtripUpdateModel);

                                RealmResults<FavouriteTripRealmModule> resultsafte = realm.where(FavouriteTripRealmModule.class).findAll();

                                for (int i = 0; i < resultsafte.size(); i++) {
                                    Log.d("hjjghj", "ss--ssdd" + resultsafte.get(i).getEndlocation() + resultsafte.get(i).getId());

                                }

                            }
                        } else {
                            common.showToast("Favourite list is full, Please delete some of your favourites.", TOAST_DURATION);
                        }

                    } else {
//                        FavouriteTripRealmModule receFavDataResult = realm.where(FavouriteTripRealmModule.class).equalTo("id", id).findFirst();

                        Log.d("jdjdjdj", "s=" + favtripUpdateModel.getId() + id);

                        if (favtripUpdateModel != null) {
//
                            Log.d("jdjdjdj", "s=" + favtripUpdateModel.getId() + id);
                            FavouriteTripRealmModule favtripUpdateModel1 = realm.where(FavouriteTripRealmModule.class).equalTo("id", id).findFirst();

                            favtripUpdateModel1.deleteFromRealm();


                        }
//                        if (favouriteTripRealmModule != null) {
//                            favouriteTripRealmModule.deleteFromRealm();
//
//                            RealmResults<FavouriteTripRealmModule> resultsafte = realm.where(FavouriteTripRealmModule.class).findAll();
//
//                            Log.d("hjjghj","--"+resultsafte.size());
//                        }


                    }

                    RealmResults<FavouriteTripRealmModule> result = realm.where(FavouriteTripRealmModule.class).equalTo("id", 0).findAll();
                    result.deleteAllFromRealm();

                }
            });

        } catch (Exception e) {
            Log.d("realmex", "--" + e.getMessage());

        }


    }


    public void updateRecentData(Realm realm, int id, boolean clicked) {

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                RealmResults<FavouriteTripRealmModule> results = realm.where(FavouriteTripRealmModule.class).findAll();

                RecentTripRealmModule receFavDataResult = realm.where(RecentTripRealmModule.class).equalTo("id", id).findFirst();

                int tripSize = results.size();


                if (clicked) {


                    if (favTrip.size() < 10) {

                        Log.d("njkkjkjjk", "--sa" + tripSize);

                        if (receFavDataResult == null) {

                            //something wrong
                        } else if (receFavDataResult != null) {

                            receFavDataResult.setFavorite(true);
                            realm.insertOrUpdate(receFavDataResult);


                        }
                    } else {
                        Log.d("njkkjkjjk", "--sssawsa" + tripSize);
                        if (receFavDataResult == null) {

                            //something wrong
                        } else if (receFavDataResult != null) {

                            receFavDataResult.setFavorite(false);
                            realm.insertOrUpdate(receFavDataResult);


                        }
                    }
                } else {
                    Log.d("njkkjkjjk", "-dqq-sa" + tripSize);

                    if (receFavDataResult == null) {

                        //something wrong
                    } else if (receFavDataResult != null) {

                        receFavDataResult.setFavorite(false);
                        realm.insertOrUpdate(receFavDataResult);


                    }
                }


            }


        });


    }

}
