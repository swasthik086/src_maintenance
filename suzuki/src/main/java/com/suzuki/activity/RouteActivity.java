package com.suzuki.activity;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.mappls.sdk.geojson.LineString;
import com.mappls.sdk.geojson.Point;
import com.mappls.sdk.maps.MapView;
import com.mappls.sdk.maps.MapplsMap;
import com.mappls.sdk.maps.OnMapReadyCallback;
import com.mappls.sdk.maps.Style;
import com.mappls.sdk.maps.annotations.Marker;
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
import com.mappls.sdk.navigation.iface.INavigationListener;
import com.mappls.sdk.navigation.iface.IStopSession;
import com.mappls.sdk.navigation.model.NavigationResponse;
import com.mappls.sdk.navigation.util.ErrorType;
import com.mappls.sdk.services.api.OnResponseCallback;
import com.mappls.sdk.services.api.PlaceResponse;
import com.mappls.sdk.services.api.autosuggest.model.ELocation;
import com.mappls.sdk.services.api.directions.DirectionsCriteria;
import com.mappls.sdk.services.api.directions.MapplsDirectionManager;
import com.mappls.sdk.services.api.directions.MapplsDirections;
import com.mappls.sdk.services.api.directions.models.DirectionsResponse;
import com.mappls.sdk.services.api.directions.models.DirectionsRoute;
import com.mappls.sdk.services.api.directions.models.DirectionsWaypoint;
import com.mappls.sdk.services.api.directions.models.LegStep;
import com.mappls.sdk.services.api.reversegeocode.MapplsReverseGeoCode;
import com.mappls.sdk.services.api.reversegeocode.MapplsReverseGeoCodeManager;
import com.mappls.sdk.services.utils.Constants;
import com.suzuki.R;
import com.suzuki.adapter.AutoCompleteTextWatcher;
import com.suzuki.adapter.AutoSuggestAdapter;
import com.suzuki.adapter.RecyclerItemClickListener;
import com.suzuki.adapter.ViaPointAdapter;
import com.suzuki.application.SuzukiApplication;
import com.suzuki.base.BaseActivity;
import com.suzuki.broadcaster.BleConnection;
import com.suzuki.fragment.DashboardFragment;
import com.suzuki.fragment.MapMainFragment;
import com.suzuki.interfaces.ItemTouchHelperAdapter;
import com.suzuki.interfaces.StartDragListener;
import com.suzuki.maps.plugins.BearingIconPlugin;
import com.suzuki.maps.plugins.DirectionPolylinePlugin;
import com.suzuki.maps.traffic.TrafficPlugin;
import com.suzuki.pojo.ClusterStatusPktPojo;
import com.suzuki.pojo.FavouriteTripRealmModule;
import com.suzuki.pojo.RecentTripRealmModule;
import com.suzuki.pojo.RiderProfileModule;
import com.suzuki.pojo.SettingsPojo;
import com.suzuki.pojo.ViaPointLocationRealmModel;
import com.suzuki.pojo.ViaPointPojo;
import com.suzuki.utils.Common;
import com.suzuki.utils.DataRequestManager;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static com.mappls.sdk.maps.Mappls.getApplicationContext;
import static com.suzuki.activity.HomeScreenActivity.TOAST_DURATION;
import static com.suzuki.activity.RouteNearByActivity.tripID;
import static com.suzuki.application.SuzukiApplication.calculateCheckSum;
import static com.suzuki.fragment.DashboardFragment.staticConnectionStatus;
import static com.suzuki.fragment.MapMainFragment.currentlocation;
import static com.suzuki.utils.Common.BikeBleName;
import static com.suzuki.utils.Common.EXCEPTION;
import static com.suzuki.fragment.MapMainFragment.eLocation;


public  class RouteActivity extends BaseActivity implements OnMapReadyCallback,   MapplsMap.OnMapLongClickListener,
        PermissionsListener, StartDragListener, ViaPointAdapter.AddressChanged, ViaPointAdapter.deleteViaPoint,ViaPointAdapter.Updation , ViaPointAdapter.LayoutDelete {

    public MapView mapView;
    LocationComponent locationComponent;
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

    ItemTouchHelper touchHelper;
    private MapplsMap mapboxMap;
    private static SuzukiApplication app;
    private boolean firstFix;
    private static DirectionPolylinePlugin directionPolylinePlugin;
    private LocationEngine locationEngine;
    private PermissionsManager permissionsManager;
    BearingIconPlugin _bearingIconPlugin;
    Marker marker, sourceMarker;
     boolean moveToNavigation;
     boolean startNav;
    private String top_speed;

    private BleConnection mReceiver;
    LocationEngineRequest request;
    LatLng latLng;
    private static final long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private static final long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;
    boolean swapClicked, bookmarkClicked;
    private String fromLocation, fromAddress, placeName, placeAddress;
    String destinationLat, destinationLong;
    public static boolean startClicked = true;
    String endTime,currentPlaceName, destionationPlaceName, destinationAddress, currentLat, currentLong, destinyLat, destinyLong;
    ImageView ivBookMark;
    RelativeLayout rlLocDetails, rlSwapLoc, rlBookMark, rlNavigationDetails;
    LinearLayout llBack, llStartNavigation;
    TextView tvTimeForTravel, tvDistance, tvPlaceAddress, tvDestinationAddress;
    EditText tvCurrentloc, tvDestination;
    StateModel mStateModel;
    Realm realm;
    private RecyclerView mSuggestionListView;
    private AutoCompleteTextWatcher textWatcher;
    ProgressBar apiProgressBar;
    public static boolean currentLocChanging, destionationChanging, viaPointChanging;
    private LatLng startingLatLng;
    @SuppressLint("StaticFieldLeak")
    public static Activity routeActivity;
    RecyclerView viaPointRv;
    ViaPointAdapter viaPointAdapter;
    ArrayList<ViaPointPojo> viaPointPojoArrayList = new ArrayList<>();

    //private static final String KEY_STATE_MODEL = "state_model";

    int viaPointEditPos = -1;
    public static boolean stopAutoSuggest = false;
    ImageView addViaIV;
    public static ArrayList<LatLng> viaPoints = new ArrayList<>();
    int sizeIncCounter = 0; // for increasing layout height after adding new via points
    ItemTouchHelperAdapter mAdapter;
    Common common;
    public static final int REQUEST_CODE_PERMISSIONS = 101;

    @Override
    public void onAddressChanged(int position) {
        this.viaPointEditPos = position;
    }



    @Override
    public void deleteViaPointPressed(int position) {
        if (mAdapter != null) {
            if (position != -1 && viaPoints.size() >= position) {
                viaPoints.remove(position - 1);

                ArrayList<LatLng> geoPoints = new ArrayList<>();
                geoPoints.add(startingLatLng);
                geoPoints.add(getPoint(eLocation));
                getRoute(startingLatLng, geoPoints, eLocation);
                update();

            }

            mAdapter.onItemDismiss(position);
            if (viaPointPojoArrayList.size() == 2) {
                rlSwapLoc.setVisibility(View.VISIBLE);
            }
            if (sizeIncCounter > 0 && viaPointPojoArrayList.size() < 4) {
                rlLocDetails.setLayoutParams(new RelativeLayout.LayoutParams(rlLocDetails.getLayoutParams().width, rlLocDetails.getLayoutParams().height - 60));
                sizeIncCounter--;
            }
        }
    }

    @Override
    public void updateThisVia() {
        ArrayList<LatLng> geoPoints = new ArrayList<>();
        geoPoints.add(startingLatLng);

        geoPoints.add(getPoint(eLocation));
        getRoute(startingLatLng, geoPoints, eLocation);
        update();
    }

    @Override
    public void layoutDelete(int position) {
        if (mAdapter != null) {
            mAdapter.onItemDismiss(position);
            if (viaPointPojoArrayList.size() == 2) rlSwapLoc.setVisibility(View.VISIBLE);

            if (sizeIncCounter > 0 && viaPointPojoArrayList.size() < 4) {
                rlLocDetails.setLayoutParams(new RelativeLayout.LayoutParams(rlLocDetails.getLayoutParams().width,
                        rlLocDetails.getLayoutParams().height - 60));
                sizeIncCounter--;
            }
        }
    }

    @Override
    public boolean onMapLongClick(@NonNull LatLng latLng) {
        return false;

    }


    private static class StateModel {
        //private ELocation eLocation;
        private DirectionsResponse trip;
        private int selectedIndex;

    }

    @SuppressLint({"ClickableViewAccessibility", "NotifyDataSetChanged"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.route_activity);

     //   routeActivity = this;

        common = new Common(this);
        realm = Realm.getDefaultInstance();
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
        viaPointRv = findViewById(R.id.viaPointRV);
        addViaIV = findViewById(R.id.ivAddVia);
        mapView.onCreate(savedInstanceState);

        try {

            Intent intent = getIntent();
            fromLocation = intent.getStringExtra("fromLocation");

            placeName = intent.getStringExtra("placeName");
            placeAddress = intent.getStringExtra("placeAddress");
            destinationLat = intent.getStringExtra("lat");
            destinationLong = intent.getStringExtra("long");

            fromAddress = fromLocation;
            tvCurrentloc.setText(fromLocation);
            tvDestination.setText(placeName);


            viaPointPojoArrayList.add(new ViaPointPojo(fromAddress, Color.WHITE));
            viaPointPojoArrayList.add(new ViaPointPojo(placeName, getResources().getColor(R.color.app_theme_color)));
            currentPlaceName = fromLocation;
            destionationPlaceName = placeName;
            destinationAddress = placeAddress;
           currentLat = String.valueOf(currentlocation.getLatitude());
           currentLong = String.valueOf(currentlocation.getLongitude());

            startingLatLng = new LatLng(currentlocation.getLatitude(), currentlocation.getLongitude());

            destinyLat = String.valueOf(eLocation.latitude);
            destinyLong = String.valueOf(eLocation.longitude);



        } catch (Exception e) {
            e.printStackTrace();
        }

        setBluetoothStatus();

        getSaveTripsData(realm);

        rlBookMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!bookmarkClicked) {
                    bookmarkClicked = true;
                    ivBookMark.setImageResource(R.drawable.book_mark_blue);

                } else {
                    bookmarkClicked = false;
                    ivBookMark.setImageResource(R.drawable.book_mark);
                }
            }
        });
        llBack.setOnClickListener(v -> finish());

        rlSwapLoc.setOnClickListener(v -> {

            if (viaPointPojoArrayList.size() < 2) {
                common.showToast("Can't swap", TOAST_DURATION);
                return;
            }

            String loc = fromAddress;
            fromAddress = destinationAddress;
            destinationAddress = loc;
            tvCurrentloc.removeTextChangedListener(textWatcher);
            tvDestination.removeTextChangedListener(textWatcher);
            stopAutoSuggest = true;

            if (!swapClicked) { // change current loc as destination loc and vice -versa
                swapClicked = true;
                tvCurrentloc.setText(placeName);
                tvDestination.setText(fromLocation);

                viaPointPojoArrayList.get(0).setAddress(placeName);
                viaPointPojoArrayList.get(1).setAddress(fromLocation);
                viaPointAdapter.notifyDataSetChanged();

                currentPlaceName = placeName;
                destionationPlaceName = fromLocation;
                // swap destination loc LatLng
                destinyLat = String.valueOf(startingLatLng.getLatitude());
                destinyLong = String.valueOf(startingLatLng.getLongitude());
                // swap current loc LatLng
                currentLat = String.valueOf(eLocation.latitude);
                currentLong = String.valueOf(eLocation.longitude);
               // Toast.makeText(app, ""+currentLat+currentLong, Toast.LENGTH_SHORT).show();

                // set new destination as swapELocation and old eLocation will be starting eLocation
                ELocation swapELocation = new ELocation();
                swapELocation.longitude = Double.valueOf((String.valueOf(startingLatLng.getLongitude())));
                swapELocation.latitude = Double.valueOf((String.valueOf(startingLatLng.getLatitude())));

                ArrayList<LatLng> geoPointsw = new ArrayList<>();
                geoPointsw.add(new LatLng(Double.parseDouble(String.valueOf(eLocation.latitude)), Double.parseDouble(String.valueOf(eLocation.longitude))));
                geoPointsw.add(getPoint(swapELocation));
                double lat = Double.parseDouble(String.valueOf(eLocation.latitude));
                double lng = Double.parseDouble(String.valueOf(eLocation.longitude));

                // update old eLocation because new destination eLoc is swapELocation
                eLocation = swapELocation;
                update();

                getRoute(new LatLng(lat, lng), geoPointsw, swapELocation);

            }
            else { // change destination loc as current loc and vice -versa
                swapClicked = false;
                tvCurrentloc.setText(fromLocation);
                tvDestination.setText(placeName);

                viaPointPojoArrayList.get(0).setAddress(fromLocation);
                viaPointPojoArrayList.get(1).setAddress(placeName);
                viaPointAdapter.notifyDataSetChanged();

                currentPlaceName = fromLocation;
                destionationPlaceName = placeName;

                // destination is now equal to previous current loc
                destinyLat = currentLat;
                destinyLong = currentLong;
            //    Toast.makeText(app, ""+destinyLat+destinyLong, Toast.LENGTH_SHORT).show();
                // get current lat lng from startingLatLng because they are constant
                currentLat = String.valueOf(startingLatLng.getLatitude());
                currentLong = String.valueOf(startingLatLng.getLongitude());

                /*destinyLat = String.valueOf(eLocation.latitude);
                destinyLong = String.valueOf(eLocation.longitude);*/

                // update eLocation
                eLocation.latitude = Double.valueOf(destinyLat);
                eLocation.longitude = Double.valueOf(destinyLong);

                if (mStateModel.trip != null) {
                    app = getMyApplication();
                    app.setTrip(mStateModel.trip);
                    ArrayList<LatLng> geoPointsw = new ArrayList<>();
                    LatLng sLatLng = new LatLng(startingLatLng.getLatitude(),
                            startingLatLng.getLongitude());
                    geoPointsw.add(sLatLng);
                    geoPointsw.add(getPoint(eLocation));
                    update();
                    getRoute(sLatLng, geoPointsw, eLocation);

                }

            }

            tvCurrentloc.addTextChangedListener(textWatcher);
            tvDestination.addTextChangedListener(textWatcher);

            new Handler().postDelayed(() -> stopAutoSuggest = false, 1500);



        });

        // auto suggestion
        mSuggestionListView = findViewById(R.id.rvAutosuggest);
        apiProgressBar = findViewById(R.id.api_progress_bar);
        textWatcher = new AutoCompleteTextWatcher(this, mSuggestionListView, apiProgressBar);

        // via point

        viaPointRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        viaPointAdapter = new ViaPointAdapter(this, viaPointPojoArrayList, mSuggestionListView, apiProgressBar);
        viaPointAdapter.addressCallback(this);
        viaPointAdapter.deletePointPressed(this);
        viaPointAdapter.updateTheViaPointFromAdapter(this);
        viaPointAdapter.layoutDeleteFromAdapter(this);
        viaPointRv.setAdapter(viaPointAdapter);

        ItemTouchHelper.Callback callback =
                new SimpleItemTouchHelperCallback(viaPointAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(viaPointRv);

        mapView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(tvCurrentloc.getWindowToken(), 0);
                InputMethodManager imms = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imms.hideSoftInputFromWindow(tvDestination.getWindowToken(), 0);
                mSuggestionListView.setVisibility(View.GONE);

                return false;
            }
        });

        mSuggestionListView.setHasFixedSize(true);

        mSuggestionListView.addOnItemTouchListener(new RecyclerItemClickListener(this, (view1, position) -> {
            hideKey();
            onSuggestionListItemClicked(view1, false);
        }));

        tvCurrentloc.addTextChangedListener(textWatcher);
        tvDestination.addTextChangedListener(textWatcher);

        tvCurrentloc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                currentLocChanging = true;
                destionationChanging = false;
            }
        });

        tvDestination.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                destionationChanging = true;
                currentLocChanging = false;
            }
        });


        llStartNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (viaPointAdapter != null) {
                    boolean isAnyEditTextEmpty = false;

                    for (int i = 1; i < viaPointAdapter.getItemCount(); i++) {
                        ViaPointAdapter.ViaPoint viaPointViewHolder = (ViaPointAdapter.ViaPoint) viaPointRv.findViewHolderForAdapterPosition(i);

                        if (viaPointViewHolder != null) {
                            EditText viaPointEt = viaPointViewHolder.viaPointEt;
                            if (viaPointEt != null) {
                                String editTextValue = viaPointEt.getText().toString().trim();

                                if (editTextValue.isEmpty()) {
                                    isAnyEditTextEmpty = true;
                                    break;
                                }
                            }
                        }
                    }

                    if (isAnyEditTextEmpty) {
                        Toast.makeText(RouteActivity.this, "Please enter the destination address to continue", Toast.LENGTH_SHORT).show();
                    } else {
                        NavLocation location=MapMainFragment.getUserLocation();
                        if (moveToNavigation) {
                            if (bookmarkClicked||DataRequestManager.isSaveTripsEnabled) {

                                if (startClicked) {
                                    startClicked = false;
                                  //  addTripDataToRealm();
                                    LatLng currentLatlng= new LatLng(location.getLatitude(),location.getLongitude());
                                    LatLng destinationLatlng= new LatLng(eLocation.latitude,eLocation.longitude);
                                    if(currentLatlng.distanceTo(destinationLatlng) < 30 ){
                                        showExitNavigationAlert();

                                    }else {
                                        startNavigation();
                                    }
                                    addRideCount();
                                }
                            } else {


                                showSaveTripsAlert();

                            }

                        } else {
                            showConnectToBluetoothAlert();
                            //startNavigation();
                        }
                    }

                }

//                startNavigation();
//                addTripDataToRealm();
            /*    NavLocation location=MapMainFragment.getUserLocation();
                if (moveToNavigation) {
                    if (bookmarkClicked||DataRequestManager.isSaveTripsEnabled) {

                        if (startClicked) {
                            startClicked = false;
                            addTripDataToRealm();
                            LatLng currentLatlng= new LatLng(location.getLatitude(),location.getLongitude());
                            LatLng destinationLatlng= new LatLng(eLocation.latitude,eLocation.longitude);
                            if(currentLatlng.distanceTo(destinationLatlng) < 30 ){
                                showExitNavigationAlert();

                            }else {
                                startNavigation();
                            }
                            addRideCount();
                        }
                    } else {


                        showSaveTripsAlert();

                    }

                } else {
                    showConnectToBluetoothAlert();
                }*/
            }
        });


        addViaIV.setOnClickListener(v -> {
            if (viaPoints.size() > 8) {
                // to add maximum 9 via-points
                Snackbar.make(v,"Maximum Via Points added", BaseTransientBottomBar.LENGTH_SHORT).show();
                return;
            }
            if (viaPoints.size() >= viaPointPojoArrayList.size() - 2) {
                if (sizeIncCounter <= 1) {
                    rlLocDetails.setLayoutParams(new RelativeLayout.LayoutParams(rlLocDetails.getLayoutParams().width,
                            rlLocDetails.getLayoutParams().height + 60));
                    sizeIncCounter++;
                }
                rlSwapLoc.setVisibility(View.INVISIBLE);
                viaPointPojoArrayList.add(viaPointPojoArrayList.size() - 1,
                        new ViaPointPojo("", getResources().getColor(R.color.lightGrey)));
                viaPointAdapter.notifyDataSetChanged();
                viaPointRv.smoothScrollToPosition(viaPointPojoArrayList.size() - 1);
            } else {
                common.showToast("Please add proper via point in previous empty field", TOAST_DURATION);
            }
        });

        mapView.getMapAsync(this);
        requestLocationPermission();

        DataRequestManager.isSaveTripsClikced = true;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    void hideKey() {

        if (tvCurrentloc != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null)
                imm.hideSoftInputFromWindow(tvCurrentloc.getWindowToken(), 0);
        }
        if (tvDestination != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null)
                imm.hideSoftInputFromWindow(tvDestination.getWindowToken(), 0);
        }
    }


    @SuppressLint("LogNotTimber")
    private void onSuggestionListItemClicked(View view, boolean nearby) {
        mSuggestionListView.setVisibility(View.GONE);

        try {
            if (view.getTag() instanceof AutoSuggestAdapter.WrapperAutoSuggestResult) {
                AutoSuggestAdapter.WrapperAutoSuggestResult wrapper = (AutoSuggestAdapter.WrapperAutoSuggestResult) view.getTag();

                if (wrapper == null) {
                    return;
                }
                ELocation eLocation1 = wrapper.getElocation();
                if (eLocation1 != null) {
                    if (!TextUtils.isEmpty(eLocation1.placeName)) {
                        //    searchEditText.setText(eLocation.placeName);
//                        textViewPutRouteName.setText(eLocation.placeName);
//                        showHideBottomSheet();
                    }
                    double longitude = 0, latitude = 0, eLatitude = 0, eLongitude = 0;
                    try {
                        latitude = eLocation1.latitude != null ? Double.parseDouble(String.valueOf(eLocation1.latitude)) : 0;
                        longitude = eLocation1.longitude != null ? Double.parseDouble(String.valueOf(eLocation1.longitude)) : 0;
                        eLatitude = eLocation1.entryLatitude;
                        eLongitude = eLocation1.entryLongitude;
                    } catch (NumberFormatException e) {
                        Timber.e(e);
                    }

                  /*  if (latitude > 0 && longitude > 0) {
                        eLocation.latitude = latitude + "";
                        eLocation.longitude = longitude + "";
                        eLocation.entryLatitude = eLatitude;
                        eLocation.entryLongitude = eLongitude;
                    }
                    eLocation = eLocation;*/

                    Log.d("eLoc", "-- top " + eLocation1.placeName + " middle " + eLocation1.alternateName + " end " + eLocation1.placeAddress);

                    if (currentLocChanging) {
                        // update current loc
                        updateCurrentLoc(eLocation1, latitude, longitude);

                    } else if (destionationChanging) {
                        // update the destination part
                        updateDestination(eLocation1, latitude, longitude);

                    } else if (viaPointChanging) {
                        // update/add via points
                        updateOrAddViaPoint(eLocation1, latitude, longitude);
                    }
                }
            }

        } catch (Exception e) {
            Timber.e(e);
        }
    }

    private void updateOrAddViaPoint(ELocation eLocation1, double latitude, double longitude) {
        try{
            if (viaPointEditPos != -1 && viaPointEditPos < viaPointPojoArrayList.size()) {
                viaPoints.add(viaPointEditPos - 1, new LatLng(latitude, longitude));

                stopAutoSuggest = true;
                viaPointPojoArrayList.get(viaPointEditPos).setAddress(eLocation1.placeName);
                viaPointAdapter.notifyDataSetChanged();
                viaPointEditPos = -1; // update again for surety next time

                ArrayList<LatLng> geoPoints = new ArrayList<>();
                geoPoints.add(startingLatLng);
                geoPoints.add(getPoint(eLocation));
                getRoute(startingLatLng, geoPoints, eLocation);
                update();

                new Handler().postDelayed(() -> stopAutoSuggest = false, 1000);
            }
        }catch (Exception e){
            Log.e(EXCEPTION,"RouteActivity: updateOrAddViaPoint: "+String.valueOf(e));
        }

    }


    @SuppressLint("NotifyDataSetChanged")
    private void updateDestination(ELocation eLocation1, double latitude, double longitude) {
        ArrayList<LatLng> geoPoints = new ArrayList<>();
        eLocation = eLocation1;
        //destinationLat = String.valueOf(latitude);
        //destinationLong = String.valueOf(longitude);
        placeAddress = eLocation1.placeAddress;
        destinationAddress = eLocation1.placeAddress;
        placeName = eLocation1.placeName;
        destionationPlaceName = placeName;
        tvDestination.removeTextChangedListener(textWatcher);
        tvDestination.setText(placeName);

        stopAutoSuggest = true;

        if (viaPointEditPos != -1 && viaPointEditPos < viaPointPojoArrayList.size()) {
            viaPointPojoArrayList.get(viaPointEditPos).setAddress(eLocation1.placeName);
            viaPointAdapter.notifyDataSetChanged();
            viaPointEditPos = -1; // update again for surety next time
            geoPoints.add(startingLatLng);
            geoPoints.add(getPoint(eLocation));
            getRoute(startingLatLng, geoPoints, eLocation);
//        Log.d("geoPoints", "-" + getNavigationGeoPoint(eLocation) + latitude + "--" + latitude);
            update();
            tvDestination.addTextChangedListener(textWatcher);

            new Handler().postDelayed(() -> stopAutoSuggest = false, 1000);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateCurrentLoc(ELocation eLocation1, double latitude, double longitude) {
        ArrayList<LatLng> geoPoints = new ArrayList<>();
        LatLng startLatLng = new LatLng(latitude, longitude);
        fromLocation = eLocation1.placeName;
        fromAddress = eLocation1.placeAddress;
        currentPlaceName = fromLocation;
        startingLatLng = startLatLng;
        geoPoints.add(startLatLng);
        geoPoints.add(getPoint(eLocation));
        getRoute(startLatLng, geoPoints, eLocation);
        tvCurrentloc.removeTextChangedListener(textWatcher);
        tvCurrentloc.setText(eLocation1.placeName);

        stopAutoSuggest = true;

        if (viaPointEditPos != -1 && viaPointEditPos < viaPointPojoArrayList.size()) {
            viaPointPojoArrayList.get(viaPointEditPos).setAddress(eLocation1.placeName);
            viaPointEditPos = -1; // update again for surety next time
            viaPointAdapter.notifyDataSetChanged();
            //        Log.d("geoPoints", "-" + getNavigationGeoPoint(eLocation) + latitude + "--" + latitude);
            update();
            tvCurrentloc.addTextChangedListener(textWatcher);

            new Handler().postDelayed(() -> stopAutoSuggest = false, 1000);
        }
    }

    private boolean getDetailsFromReverseGeoCode(@NotNull LatLng latLng) {
        double lat = latLng.getLatitude();
        double lng = latLng.getLongitude();
        if (lat <= 0 || lng <= 0) {
            common.showToast("Invalid Location", TOAST_DURATION);
            return false;
        }


        MapplsReverseGeoCode mapplsReverseGeoCode = MapplsReverseGeoCode.builder()
                .setLocation(lat,lng)
                .build();
        MapplsReverseGeoCodeManager.newInstance(mapplsReverseGeoCode).call(new OnResponseCallback<PlaceResponse>() {
            @Override
            public void onSuccess(PlaceResponse response) {
                //Handle Response
                Timber.d(new Gson().toJson(response));
                Timber.d("lat : " + lat + " lng : " + lng);
                if (response == null || response.getPlaces().size() < 1) {
                    common.showToast("Issue in fetching details", TOAST_DURATION);
                    return;
                }

                String address = response.getPlaces().get(0).getFormattedAddress();
                String place = response.getPlaces().get(0).getLocality();
                String poi = response.getPlaces().get(0).getPoi();
                if (place.length() < 2) {
                    if (poi.length() < 2) {
                        if (address.length() < 2) {
                            common.showToast("Couldn't find map data at selected point", TOAST_DURATION);
                            return;
                        } else {
                            place = address;
                        }
                    } else {
                        place = poi;
                    }
                }
                ELocation eLocation1 = new ELocation();

                eLocation1.latitude = Double.valueOf(lat + "");
                eLocation1.longitude = Double.valueOf(lng + "");
                eLocation1.placeAddress = address;
                eLocation1.placeName = place;

                if (currentLocChanging) {
                    // update current loc
                    updateCurrentLoc(eLocation1, lat, lng);

                } else if (destionationChanging) {
                    // update the destination part
                    updateDestination(eLocation1, lat, lng);

                } else if (viaPointChanging) {
                    // update/add via points
                    updateOrAddViaPoint(eLocation1, lat, lng);
                }
                Log.d("TAG", "onResponse: " + address + " place " + place + currentLocChanging + destionationChanging + viaPointChanging);
            }

            @Override
            public void onError(int code, String message) {
                //Handle Error
            }
        });

        return false;
    }

    public void showSaveTripsAlert() {

        Dialog dialog = new Dialog(RouteActivity.this, R.style.custom_dialog);
        dialog.setContentView(R.layout.custom_dialog_for_savetrips);

        TextView tvCustomTextBtn = dialog.findViewById(R.id.tvCustomTextBtn);
        LinearLayout llSave = dialog.findViewById(R.id.llSave);
        tvCustomTextBtn.setText("Save");
        ImageView ivCustomClose = dialog.findViewById(R.id.ivCustomClose);
        ivCustomClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataRequestManager.isSaveTripsClikced = false;
                NavLocation location=MapMainFragment.getUserLocation();
                LatLng currentLatlng= new LatLng(location.getLatitude(),location.getLongitude());
                LatLng destinationLatlng= new LatLng(eLocation.latitude,eLocation.longitude);
                if(currentLatlng.distanceTo(destinationLatlng) < 30 ){
                    showExitNavigationAlert();
                }else{
                    startNavigation();
                }
              //  startNavigation();
//                addRideCount();
                startClicked = true;
//                DataRequestManager.isSaveTripsClikced=false;
                dialog.cancel();

            }
        });


        llSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataRequestManager.isSaveTripsClikced = true;

                NavLocation location=MapMainFragment.getUserLocation();
               // addTripDataToRealm();
                LatLng currentLatlng= new LatLng(location.getLatitude(),location.getLongitude());
                LatLng destinationLatlng= new LatLng(eLocation.latitude,eLocation.longitude);
                if(currentLatlng.distanceTo(destinationLatlng) < 30 ){
                    showExitNavigationAlert();
                  //  Toast.makeText(RouteActivity.this, "Destination Nearby", Toast.LENGTH_SHORT).show();
                }else{
                    startNavigation();

                }
              //  startNavigation();

                addRideCount();
                startClicked = true;
//                DataRequestManager.isSaveTripsClikced=true;
                dialog.cancel();
            }
        });

        dialog.show();


    }


    public void showExitNavigationAlert() {
        Dialog dialog = new Dialog(this, R.style.custom_dialog);
        dialog.setContentView(R.layout.custom_dialog);

        TextView tvAlertText = dialog.findViewById(R.id.tvAlertText);
        tvAlertText.setText("This place seems to be nearby. Please look around.");
        ImageView ivCross = dialog.findViewById(R.id.ivCross);
        ivCross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                //  updateDisplay(maneuverID, "00000", dataShortDistanceUnit, dataEta, dataRemainingDistance, dataRemainingDistanceUnit, "1", "0");

//                getActivity().onBackPressed();

            }
        });

        ImageView ivCheck = dialog.findViewById(R.id.ivCheck);


        ivCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
                if (this== null)
                    return;
                //   updateDisplay(maneuverID, "00000", dataShortDistanceUnit, dataEta, dataRemainingDistance, dataRemainingDistanceUnit, "1", "0");
                if (RouteActivity.routeActivity != null) {
                    RouteActivity.routeActivity.finish();
                } else if (RouteNearByActivity.routeNearByActivity != null) {
                    RouteNearByActivity.routeNearByActivity.finish();
                }


            }
        });
        dialog.show();
    }


    public void addRideCount() {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(realm1 -> {
                RiderProfileModule riderProfile = realm1.where(RiderProfileModule.class).equalTo("id", 1).findFirst();

                if (riderProfile == null) {
                    riderProfile = realm1.createObject(RiderProfileModule.class, 1);

                    riderProfile.setRideCounts(1);
                    realm1.insertOrUpdate(riderProfile);

                }
                else if (riderProfile != null) {

                    int count = riderProfile.getRideCounts();
                    Log.d("ddkddk", "==" + count + count++);
                    riderProfile.setRideCounts(count++);
                    realm1.insertOrUpdate(riderProfile);
                }
            });

        } catch (Exception e) {
            Log.d("realmex", "--" + e.getMessage());

        }
    }

    public void showConnectToBluetoothAlert() {
        Dialog dialog = new Dialog(RouteActivity.this, R.style.custom_dialog);
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
                Intent in = new Intent(RouteActivity.this, DeviceListingScanActivity.class);
                startActivity(in);
                dialog.cancel();
            }
        });
        dialog.show();
    }




    private void addTripDataToRealm() {
        //  Date startTime = Calendar.getInstance().getTime();


        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(realm1 -> {

                RealmResults<RecentTripRealmModule> results = realm1.where(RecentTripRealmModule.class).findAll();
                RecentTripRealmModule recentTripRealmModule = realm1.createObject(RecentTripRealmModule.class);

                Date d=new Date();
                SimpleDateFormat sdf=new SimpleDateFormat("hh:mm a");
                String startTime = sdf.format(d);

                Calendar currentTimeNow = Calendar.getInstance();
                System.out.println("Current time now : " + currentTimeNow.getTime());
                currentTimeNow.add(Calendar.MINUTE, mStateModel.trip.routes().get(mStateModel.selectedIndex).duration().intValue());
                long getETA = System.currentTimeMillis()+(mStateModel.trip.routes().get(mStateModel.selectedIndex).duration().intValue()*1000);
                String dateString = DateFormat.format("hh:mm a", new Date(getETA)).toString();
                SharedPreferences prefs = getApplicationContext().getSharedPreferences("top_speed", MODE_PRIVATE);
                int saved_speed = prefs.getInt("new_top_speed", 0);

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
                recentTripRealmModule.setTime(String.format("%s ", NavigationFormatter.getFormattedDuration(mStateModel.trip.routes().get(mStateModel.selectedIndex).duration().intValue(), getMyApplication())));
                recentTripRealmModule.setTotalDistance(String.format("%s", NavigationFormatter.getFormattedDistance(mStateModel.trip.routes().get(mStateModel.selectedIndex).distance().floatValue(), getMyApplication())));
                recentTripRealmModule.setRideTime(String.format("%s ", NavigationFormatter.getFormattedDuration(mStateModel.trip.routes().get(mStateModel.selectedIndex).duration().intValue(), getMyApplication())));
                recentTripRealmModule.setStartlocation(currentPlaceName);
              //  recentTripRealmModule.setTopSpeed(saved_speed);
                recentTripRealmModule.setTopSpeed(0);

                recentTripRealmModule.setEndlocation(destionationPlaceName);
                recentTripRealmModule.setFavorite(false);
                recentTripRealmModule.setCurrent_lat(currentLat);
                recentTripRealmModule.setCurrent_long(currentLong);
                recentTripRealmModule.setStartTime(startTime);
                recentTripRealmModule.setETA(dateString);
                recentTripRealmModule.setDestination_lat(destinyLat);
                recentTripRealmModule.setDestination_long(destinyLong);
                recentTripRealmModule.setDateTime(date);
                realm1.insert(recentTripRealmModule);


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
        realm.executeTransaction(realm -> realm.delete(RecentTripRealmModule.class));
    }

    public void startNavigation() {

        navigationStarted=true;
        NavLocation location = MapMainFragment.getUserLocation();
        if (location == null)
            return;
        showProgress(this);
        LongOperation operation = new LongOperation();
        operation.execute();
    }


    public static boolean startNav() {


        if (startNav()) {
            return true;
        } else {
            return false;
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class LongOperation extends AsyncTask<Void, Void, NavigationResponse> {

        @Override
        protected NavigationResponse doInBackground(Void... params) {
            try {
                LatLng currentLocation = null;
                NavLocation location = MapMainFragment.getUserLocation();
                if (location != null)
                    currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                NavLocation navLocation = new NavLocation("navigation");
                Point position = mStateModel.trip.routes().get(mStateModel.selectedIndex).legs().get(0).steps().get(0).maneuver().location();
                LatLng point = new LatLng(position.latitude(), position.longitude());
//                currentLocation = point;
                navLocation.setLongitude(point.getLongitude());
                navLocation.setLatitude(point.getLatitude());
                app = getMyApplication();
                app.setStartNavigationLocation(navLocation);
                if (currentLocation == null)
                    return new NavigationResponse(ErrorType.UNKNOWN_ERROR, null);

                List<WayPoint> wayPoints = new ArrayList<>();
                for (LatLng latLng : viaPoints) {
                    wayPoints.add(new WayPoint(latLng.getLatitude(), latLng.getLongitude(), null));
                }

                app.setELocation(eLocation);
                app.setViaPoints(viaPoints);

                return MapplsNavigationHelper.getInstance().startNavigation(app.getTrip(), mStateModel.selectedIndex, currentLocation,
                        getNavigationGeoPoint(eLocation), wayPoints, BikeBleName.getValue());
            } catch (Exception e) {
                Timber.e(e);
                return new NavigationResponse(ErrorType.UNKNOWN_ERROR, e);
            }
        }

        @Override
        protected void onPostExecute(NavigationResponse result) {

            dismissProgress();

            if (result != null && result.getError() != null)
            {
                if (result.getError().errorCode == 409) {

                    MapplsNavigationHelper.getInstance().deleteSession(BikeBleName.getValue(), new IStopSession() {
                        @Override
                        public void onSuccess() {
                            LongOperation operation = new LongOperation();
                            operation.execute();

                        }

                        @Override
                        public void onFailure() {
                            Toast.makeText(RouteActivity.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                        }
                    });
                    /*show dialog to user*/
                    /*new AlertDialog.Builder(RouteActivity.this)
                            .setMessage(getApplicationContext().getResources().getString(R.string.Session_Message))
                            .setTitle("Navigation Alert")
                            .setPositiveButton("Ok", (dialog, which) ->
                                    MapmyIndiaNavigationHelper.getInstance().deleteSession(BikeBleName, new IStopSession() {
                                        @Override
                                        public void onSuccess() {
                                            LongOperation operation = new LongOperation();
                                            operation.execute();
                                        }

                                        @Override
                                        public void onFailure() {

                                        }
                                    }))
                            .setNegativeButton("No", (dialog, which) -> {
                                dialog.cancel();
                                dialog.dismiss();
                            })
                            .create()
                            .show();*/
                }
                else {
                    Toast.makeText(RouteActivity.this, "Your vehicle BT ID is not whitelisted. Kindly contact the Suzuki customer support team for further assistance", Toast.LENGTH_SHORT).show();

                    //   Toast.makeText(RouteActivity.this, ""+result.getError().errorMessage, Toast.LENGTH_SHORT).show();
                }

                return;
            }

            else{
               /* Intent ittt = new Intent(getApplicationContext(), NavigationActivity.class);
                startActivity(ittt);*/
                try {
                    Intent intent = new Intent(getApplicationContext(), NavigationActivity.class);

                    Bundle bundle = new Bundle();

                    bundle.putString("currentPlaceName", currentPlaceName);

                    bundle.putString("destionationPlaceName", destionationPlaceName);

                    bundle.putDouble("currentLat", Double.parseDouble(currentLat));

                    bundle.putDouble("currentLong", Double.parseDouble(currentLong));

                    bundle.putDouble("destinyLat", Double.parseDouble(destinyLat));

                    bundle.putDouble("destinyLong", Double.parseDouble(destinyLong));

                    // bundle.putSerializable("mStateModel", mStateModel.trip);
                    bundle.putString("rideduration", String.valueOf(mStateModel.trip.routes().get(mStateModel.selectedIndex).duration().intValue()));

                    bundle.putString("ridetime", String.valueOf(mStateModel.trip.routes().get(mStateModel.selectedIndex).distance().floatValue()));

                    Log.e("rideduration: ", String.valueOf(mStateModel.trip.routes().get(mStateModel.selectedIndex).duration().intValue()));
                    Log.e("ridetime: ", String.valueOf(mStateModel.trip.routes().get(mStateModel.selectedIndex).distance().floatValue()));

                    intent.putExtras(bundle);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }






        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    public WayPoint getNavigationGeoPoint(ELocation eLocation) {
        try {
            if (eLocation.entryLatitude != null && eLocation.entryLongitude != null)
                return new WayPoint(eLocation.entryLatitude, eLocation.entryLongitude, eLocation.placeName);
            else
                return new WayPoint(Double.parseDouble(String.valueOf(eLocation.latitude)), Double.parseDouble(String.valueOf(eLocation.longitude)), eLocation.placeName);
        } catch (Exception e) {
            return new WayPoint(0, 0, null);
        }
    }

    public void addPolyLines(LatLng start, LatLng stop, final DirectionsResponse directionsResponse) {
        if (mapboxMap == null || directionsResponse == null)
            return;

        ArrayList<LatLng> latLngs = new ArrayList<>();
        if (directionPolylinePlugin != null) {
            directionPolylinePlugin.setEnabled(true);
            directionPolylinePlugin.setEnableCongestion(true);
            directionPolylinePlugin.removeAllData();


            ArrayList<LineString> lineStrings = new ArrayList<>();
            for (DirectionsRoute directionsRoute : directionsResponse.routes()) {
                if (directionsRoute.geometry() != null) {
                    LineString lineString = LineString.fromPolyline(directionsRoute.geometry(), Constants.PRECISION_6);
                    lineStrings.add(lineString);
                }
            }

            // if we want marker at starting pos then pass @start else pass null
            directionPolylinePlugin.setTrips(lineStrings, null, stop, viaPoints, directionsResponse.routes());

            directionPolylinePlugin.setOnNewRouteSelectedListener((index, directionsRoute) -> {
                // index is selected index && directionRoute is selected route

                app = getMyApplication();
                mStateModel = new StateModel();
                mStateModel.trip = directionsResponse;
                mStateModel.selectedIndex = index;
                app.setTrip(mStateModel.trip);

                update();

                directionPolylinePlugin.setSelected(index);

            });


        }
        if (directionsResponse.routes().get(0).legs() != null &&
                directionsResponse.routes().get(0).legs().get(0) != null &&
                directionsResponse.routes().get(0).legs().get(0).steps() != null &&
                directionsResponse.routes().get(0).legs().get(0).steps().size() > 0) {
            for (LegStep legStep : Objects.requireNonNull(directionsResponse.routes().get(0).legs().get(0).steps())) {
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
                mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 20, dpToPx(120), 20, dpToPx(140)),
                        300);
            } catch (Exception e) {
                e.printStackTrace();
                LatLng _point = new LatLng(latLngs.get(0).getLatitude(), latLngs.get(0).getLongitude());
                mapboxMap.animateCamera(CameraUpdateFactory.newLatLng(_point));
            }
        }
    }

    public void addPolyLine(LatLng start, LatLng stop, final DirectionsResponse directionsResponse) {
        if (mapboxMap == null || directionsResponse == null || directionsResponse.routes() == null)
            return;

        ArrayList<LatLng> latLngs = new ArrayList<>();
        if (directionPolylinePlugin != null) {
            directionPolylinePlugin.setEnabled(true);
            directionPolylinePlugin.removeAllData();
            ArrayList<LineString> lineStrings = new ArrayList<>();
            for (DirectionsRoute directionsRoute: directionsResponse.routes() ){
                LineString lineString = LineString.fromPolyline(directionsRoute.geometry(), Constants.PRECISION_6);
                lineStrings.add(lineString);
            }

            directionPolylinePlugin.setTrips(lineStrings, start, stop, viaPoints, directionsResponse.routes());
        }
        DirectionsRoute directionsRoute= directionsResponse.routes().get(mStateModel.selectedIndex);
        if (directionsRoute.legs() != null &&
                directionsRoute.legs().get(0) != null &&
                directionsRoute.legs().get(0).steps() != null &&
                Objects.requireNonNull(directionsRoute.legs().get(0).steps()).size() > 0) {
            for (LegStep legStep : Objects.requireNonNull(directionsRoute.legs().get(0).steps())) {
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
                mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 20, dpToPx(120), 20, dpToPx(140)),
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


    private void getRoute(LatLng startLocation, ArrayList<LatLng> wayPoints,ELocation eLocation)
    {

        if (directionPolylinePlugin != null)
            directionPolylinePlugin.removeAllData();
        if (wayPoints == null || wayPoints.size() < 2)
            return;
        Point origin = Point.fromLngLat(wayPoints.get(0).getLongitude(), wayPoints.get(0).getLatitude());
        Point destination = Point.fromLngLat(wayPoints.get(wayPoints.size() - 1).getLongitude(), wayPoints.get(wayPoints.size() - 1).getLatitude());

        @SuppressLint("HardwareIds") MapplsDirections.Builder directions = MapplsDirections.builder()
                .origin(origin)
                .steps(true)
                .alternatives(true)
                .annotations(DirectionsCriteria.ANNOTATION_CONGESTION)
                .routeRefresh(true)
                .deviceId(Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID))
                .resource(DirectionsCriteria.RESOURCE_ROUTE_ETA)
                .profile(DirectionsCriteria.PROFILE_BIKING)
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .destination(destination);

        for (LatLng latLng : viaPoints) {

            directions.addWaypoint(Point.fromLngLat(latLng.getLongitude(), latLng.getLatitude()));
        }
        MapplsDirectionManager.newInstance(directions.build()).call(new OnResponseCallback<DirectionsResponse>() {
            @Override
            public void onSuccess(DirectionsResponse response) {
                try {

                    response.code();

                  DirectionsResponse directionsResponse = response;

                    if (mapboxMap != null) {
                        mapboxMap.clear();
                    }

                    app = getMyApplication();

                    mStateModel = new StateModel();
                    mStateModel.trip = directionsResponse;
                    mStateModel.selectedIndex = 0;
                    app.setTrip(mStateModel.trip);
                    List<DirectionsWaypoint> waypointsList = response.waypoints();

                    update();
                    //Toast.makeText(RouteActivity.this, ""+eLocation, Toast.LENGTH_SHORT).show();
                    addPolyLines(startLocation, getPoint(eLocation), directionsResponse);

                } catch (Exception e) {
                    Timber.e(e);
                }
            }

            @Override
            public void onError(int i, String s) {
//                if (!call.isCanceled()) {
//                    throwable.printStackTrace();
////                    showErrorMessage(R.string.something_went_wrong);
//                }
            }
        });

    }

//    private void getRoute(LatLng startLocation, final ArrayList<LatLng> wayPoints, final ELocation eLocation) {
//
//        if (directionPolylinePlugin != null)
//            directionPolylinePlugin.removeAllData();
//        if (wayPoints == null || wayPoints.size() < 2)
//            return;
//        Point origin = Point.fromLngLat(wayPoints.get(0).getLongitude(), wayPoints.get(0).getLatitude());
//        Point destination = Point.fromLngLat(wayPoints.get(wayPoints.size() - 1).getLongitude(), wayPoints.get(wayPoints.size() - 1).getLatitude());
//
//        MapplsDirections directions = MapplsDirections.builder()
//                .origin(origin)
//                .steps(true)
//                .resource(DirectionsCriteria.RESOURCE_ROUTE_ETA)
//                .profile(DirectionsCriteria.PROFILE_DRIVING)
//                .overview(DirectionsCriteria.OVERVIEW_FULL)
//                .destination(destination)
//                .build();
//        MapplsDirectionManager.newInstance(directions).call(new OnResponseCallback<DirectionsResponse>() {
//            @Override
//            public void onSuccess(DirectionsResponse response) {
//                try {
//
//
//                        if (response == null) {
//                           //showErrorMessage(R.string.something_went_wrong);
//                          // onFragmentBackPressed();
//                            return;
//                        }
//
//
//                    if (mapboxMap != null) {
//                            mapboxMap.clear();
//                        }
//
//                        app = getMyApplication();
//                        mStateModel = new StateModel();
//                        mStateModel.trip = response;
//                        mStateModel.selectedIndex = 0;
//                        app.setTrip(mStateModel.trip);
//                        List<DirectionsWaypoint> waypointsList = response.waypoints();
//
//                        update();
//                        addPolyLines(startLocation, getPoint(eLocation), response);
//
//
//                } catch (Exception e) {
//                    Timber.e(e);
//                }
//            }
//
//            @Override
//            public void onError(int i, String s) {
//
//            }
//        });
//
//    }

    public LatLng getPoint(ELocation eLocation) {
        try {
            return new LatLng(eLocation.latitude,eLocation.longitude);
        } catch (Exception e) {
            return new LatLng(0, 0);
        }
    }


    private void update() {

        if (mStateModel == null || mStateModel.trip == null)
            return;
        try {
//            Log.d("psoodsods", "--" + placeAddress + placeName + mStateModel.trip.distance() + mStateModel.trip.duration());
            tvDistance.setText(String.format("%s", NavigationFormatter.getFormattedDistance(mStateModel.trip.routes().get(mStateModel.selectedIndex).distance().floatValue(), getMyApplication())));
            tvTimeForTravel.setText(String.format("%s ", NavigationFormatter.getFormattedDuration(mStateModel.trip.routes().get(mStateModel.selectedIndex).duration().intValue(), getMyApplication())));
//            tvDestinationAddress.setText(placeAddress);
            tvDestinationAddress.setText(destinationAddress);
//            tvPlaceAddress.setText(placeName);
            tvPlaceAddress.setText(destionationPlaceName);
        } catch (Exception e) {
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

        mapboxMap.addOnMapLongClickListener(this::getDataByReverseGeoCode);
		setMapDependentData();
//        if (viaPointEditPos != -1 && viaPointEditPos < viaPointPojoArrayList.size()) {
//            if ((viaPointEditPos == 0 && currentLocChanging) || (viaPointEditPos == viaPointPojoArrayList.size() - 1 && destionationChanging)
//                    || (viaPointEditPos > 0 && viaPointEditPos < viaPointPojoArrayList.size() - 1))
//                getDetailsFromReverseGeoCode(latLng);
//            else {
//                common.showToast("Set current focus to change the address", TOAST_DURATION);
//            }
//        } else {
//            common.showToast("Set current focus to change the address", TOAST_DURATION);
//        }
        try {
          //  TrafficPlugin trafficPlugin = new TrafficPlugin(mapView, mapboxMap);
           // trafficPlugin.setEnabled(true);
          //  trafficPlugin.isEnable();
            mapboxMap.enableTraffic(true);
          //  mapboxMap.enableTrafficNonFreeFlow(false); //To enable/disable Non free flow (Red/Orange) Overlay
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
        assert mapboxMap.getUiSettings() != null;
        mapboxMap.getUiSettings().setLogoMargins(40, dpToPx(120), 40, dpToPx(140));
        mapboxMap.getUiSettings().setCompassMargins(40, dpToPx(120), 40, dpToPx(40));

        mapView.getCompassView().setY((170f));
    }

    private boolean getDataByReverseGeoCode(LatLng latLng) {
        if (viaPointEditPos != -1 && viaPointEditPos < viaPointPojoArrayList.size()) {
            if ((viaPointEditPos == 0 && currentLocChanging) || (viaPointEditPos == viaPointPojoArrayList.size() - 1 && destionationChanging)
                    || (viaPointEditPos > 0 && viaPointEditPos < viaPointPojoArrayList.size() - 1))
                getDetailsFromReverseGeoCode(latLng);
            else {
                common.showToast("Set current focus to change the address", TOAST_DURATION);
            }
        } else {
            common.showToast("Set current focus to change the address", TOAST_DURATION);
        }
        return false;
    }

    private void setMapDependentData() {
        mStateModel = new StateModel();
        try {
            if (eLocation != null && mStateModel.trip != null) {
                app = getMyApplication();
                app.setTrip(mStateModel.trip);
                update();
                addPolyLine(new LatLng(currentlocation.getLatitude(),
                                currentlocation.getLongitude()),
                        new LatLng(Double.parseDouble(String.valueOf(eLocation.latitude)),
                                Double.parseDouble(String.valueOf(eLocation.longitude))),
                        mStateModel.trip);
//.get(mStateModel.selectedIndex)
                Log.d("geoPoints", "--" + mStateModel.trip);
            } else {
                try {
                    if (currentlocation != null) {
                        ArrayList<LatLng> geoPoints = new ArrayList<>();

                        LatLng currentLatLng = new LatLng(currentlocation.getLatitude(), currentlocation.getLongitude());
                        geoPoints.add(currentLatLng);
                        geoPoints.add(getPoint(eLocation));
                        getRoute(currentLatLng, geoPoints, eLocation);
                        update();
                    } else {
                        common.showToast(getResources().getString(R.string.current_location_not_available), TOAST_DURATION);
//                        onFragmentBackPressed();
                    }
                } catch (Exception e) {
                    Timber.e(e);
                    common.showToast(getResources().getString(R.string.something_went_wrong), TOAST_DURATION);
//                    onFragmentBackPressed();
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    public void setCompassDrawable() {
        mapView.getCompassView().setBackgroundResource(R.drawable.compass_background);
        mapboxMap.getUiSettings().setCompassImage(ContextCompat.getDrawable(this, R.drawable.compass_north_up));
        int padding = dpToPx(8);
        int elevation = dpToPx(8);
        mapView.getCompassView().setPadding(padding, padding, padding, padding);
        ViewCompat.setElevation(mapView.getCompassView(), elevation);
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
        LocationEngineCallback<LocationEngineResult> locationEngineCallback = new LocationEngineCallback<LocationEngineResult>()
        {

            @Override
            public void onSuccess(LocationEngineResult locationEngineResult) {
                if (locationEngineResult.getLastLocation() != null) {
                    Location location = locationEngineResult.getLastLocation();
                  //  mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16));
                }
            }

            @Override
            public void onFailure(@NonNull Exception e) {

            }
        };
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
//
//            mapboxMap.setPadding(20, 20, 20, 20);
//
//        } else {
//            permissionsManager = new PermissionsManager(this);
//            permissionsManager.requestLocationPermissions(this);
//        }
//    }

//    @Override
//    public void onConnected() {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }
//
//
//        locationEngine.requestLocationUpdates(request, locationEngineCallback, getMainLooper());
//    }

   // @Override
//    public void onLocationChanged(Location location) {
////        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
////                new LatLng(location.getLatitude(), location.getLongitude()), 16));
////        Log.d("sss", "=" + location.getLongitude() + location.getLatitude());
////        Timber.i("onLocationChanged");
//
//
////        currentlocation = location;
////
////        Log.d("locccc", "onloc chang--" + currentlocation.getLatitude() + currentlocation.getLongitude());
//////        Log.d("locccc", "onloc chang--" + eLocation.placeName);
////        getReverseGeoCode(currentlocation.getLatitude(), currentlocation.getLongitude());
////        try {
////            if (location == null || location.getLatitude() <= 0)
////                return;
////            if (!firstFix) {
////                mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location), 16), 500);
////                firstFix = true;
////                Log.d("loccc", "--latlng" + location.getLongitude() + location.getLatitude());
//////                Log.d("locccc", "onloc chang--" + eLocation.placeName);
////                getReverseGeoCode(location.getLatitude(), location.getLongitude());
////
////
////            }
//
////            getReverseGeoCode(location.getLatitude(), location.getLongitude());
////        app.setCurrentLocation(location);
////        } catch (Exception e) {
////            //ignore
////        }
//    }

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
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(locationEngineCallback);
           // locationEngine.removeLocationUpdates();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viaPoints.clear();
        viaPointPojoArrayList.clear();
        mapView.onDestroy();
//        if (locationEngine != null) {
//            locationEngine.deactivate();
//        }
        realm.close();
        RouteActivity.startClicked = true;
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        if (locationEngine != null)
            locationEngine.removeLocationUpdates(locationEngineCallback);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(locationEngineCallback);
         //   locationEngine.addLocationEngineListener(this);
        }
        /*if (realm != null)
            getSaveTripsData(realm);*/
        startClicked = true;
    }

    private void requestLocationPermission() {

        boolean foreground = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (foreground) {
            boolean background;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                background = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;

                if (!background) {
                    showDialogForBackgroundLocation("Background location permission allows the navigation to work in the background, such as while riding with the phone locked in your pocket.\n" +
                            "User location data is not collected or stored and is used only for providing navigation and last parked location feature.\n" +
                            "Please read our privacy policy for more information about user data protection.\n" +
                            "Please allow background location permission on the next page to enable navigation.");
                }
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_PERMISSIONS);
            }
        }
    }

    public void showDialogForBackgroundLocation(String message) {
        Dialog dialog = new Dialog(this, R.style.custom_dialog);
        dialog.setContentView(R.layout.location_permission_dialog);

        TextView tvAlertText = dialog.findViewById(R.id.tvAlertText);

        SpannableString spannable=new SpannableString(message);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                startActivity(new Intent(RouteActivity.this , PrivacyActivity.class));
            }
        };

        spannable.setSpan(clickableSpan, 260, 340, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannable.setSpan(new ForegroundColorSpan(Color.RED), 260, 340, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvAlertText.setText(spannable);
        tvAlertText.setMovementMethod(LinkMovementMethod.getInstance());


        ImageView ivCross = dialog.findViewById(R.id.ivCross);
        ivCross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();

            }
        });

        ImageView ivCheck = dialog.findViewById(R.id.ivCheck);


        ivCheck.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                dialog.cancel();

                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 101);

            }
        });
        dialog.show();
    }
    /*private void showDialogForBackgroundLocation() {

        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Allow Suzuki Application to use Background Location.")
                .setMessage(Html.fromHtml("This enables the Application to get Location while screen is locked.\n" +
                        "It can be reverted back anytime.\nPlease select <b>Allow all the time</b> option."))
                .setPositiveButton("OK", (dialog, which) -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, REQUEST_CODE_PERMISSIONS);
                    }
                })
                .setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {

            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equalsIgnoreCase(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    //foreground permission allowed
                    if (grantResults[i] >= 0) {
                        continue;
                    } else {
                        common.showToast("Location Permission denied", TOAST_DURATION);
                        break;
                    }
                }

                if (permissions[i].equalsIgnoreCase(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        common.showToast("Background location permission denied", TOAST_DURATION);
                    }
                }
            }
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
    public void onConnected() {

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
            if (mapboxMap == null) return;
            mapboxMap.removeAnnotations();
            if (directionPolylinePlugin != null) directionPolylinePlugin.removeAllData();
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

        moveToNavigation = staticConnectionStatus;
        IntentFilter intentFilter = new IntentFilter(
                "status");

        mReceiver = new BleConnection() {

            @Override
            public void onReceive(Context context, Intent intent) {
                //extract our message from intent

                moveToNavigation = Objects.requireNonNull(intent.getExtras()).getBoolean("status");
            }


        };

        registerReceiver(mReceiver, intentFilter);
    }


    public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback {

        SimpleItemTouchHelperCallback(ItemTouchHelperAdapter adapter) {
            mAdapter = adapter;
        }

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            if (viewHolder.getAdapterPosition() == 0 || viewHolder.getAdapterPosition() == viaPointPojoArrayList.size() - 1) {
                common.showToast("Can't swap Source and destination", TOAST_DURATION);
                return 0;
            }
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            if (viewHolder.getAdapterPosition() == 0 || viewHolder.getAdapterPosition() == viaPointPojoArrayList.size() - 1 ||
                    target.getAdapterPosition() == 0 || target.getAdapterPosition() == viaPointPojoArrayList.size() - 1) {
                common.showToast("Can't change Source and destination position", TOAST_DURATION);
                return true;
            }

            if (viewHolder.getAdapterPosition() != -1 && target.getAdapterPosition() <= viaPoints.size()
                    && viewHolder.getAdapterPosition() <= viaPoints.size()) {
                LatLng preTargetLatLng = viaPoints.get(target.getAdapterPosition() - 1); // this was at target pos
                LatLng preFromLatLng = viaPoints.get(viewHolder.getAdapterPosition() - 1); // this is at from dest
                // now swap these two latLng


                viaPoints.add(target.getAdapterPosition() - 1, preFromLatLng);
                viaPoints.remove(target.getAdapterPosition());

                viaPoints.add(viewHolder.getAdapterPosition() - 1, preTargetLatLng);
                viaPoints.remove(viewHolder.getAdapterPosition());


                ArrayList<LatLng> geoPoints = new ArrayList<>();
                geoPoints.add(startingLatLng);
                geoPoints.add(getPoint(eLocation));
                getRoute(startingLatLng, geoPoints, eLocation);
                update();

            } else {
                common.showToast("Via points are not added properly, Can't swap", TOAST_DURATION);
                return true;
            }

            mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            /*if (viewHolder.getAdapterPosition() != -1 && viaPoints.size() >= viewHolder.getAdapterPosition()) {

                viaPoints.remove(viewHolder.getAdapterPosition() - 1);

                ArrayList<LatLng> geoPoints = new ArrayList<>();
                geoPoints.add(startingLatLng);

                geoPoints.add(getPoint(eLocation));
                getRoute(startingLatLng, geoPoints, eLocation);
                update();
            }*/

            mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
            if (viaPointPojoArrayList.size() == 2) {
                rlSwapLoc.setVisibility(View.VISIBLE);
            }
            if (sizeIncCounter > 0 && viaPointPojoArrayList.size() < 4) {
                rlLocDetails.setLayoutParams(new RelativeLayout.LayoutParams(rlLocDetails.getLayoutParams().width,
                        rlLocDetails.getLayoutParams().height - 60));
                sizeIncCounter--;
            }

        }

    }

}
