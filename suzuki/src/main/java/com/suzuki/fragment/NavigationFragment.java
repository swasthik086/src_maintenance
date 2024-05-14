package com.suzuki.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.viewmodel.CreationExtras;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.mappls.sdk.geojson.LineString;
import com.mappls.sdk.gestures.MoveGestureDetector;
import com.mappls.sdk.maps.MapView;
import com.mappls.sdk.maps.MapplsMap;
import com.mappls.sdk.maps.OnMapReadyCallback;
import com.mappls.sdk.maps.Style;
import com.mappls.sdk.maps.camera.CameraPosition;
import com.mappls.sdk.maps.camera.CameraUpdateFactory;
import com.mappls.sdk.maps.geometry.LatLng;
import com.mappls.sdk.maps.geometry.LatLngBounds;
import com.mappls.sdk.maps.location.LocationComponent;
import com.mappls.sdk.maps.location.engine.LocationEngineProvider;
import com.mappls.sdk.maps.location.modes.RenderMode;
import com.mappls.sdk.maps.style.layers.Property;
import com.mappls.sdk.navigation.MapplsNavigationHelper;
import com.mappls.sdk.navigation.NavLocation;
import com.mappls.sdk.navigation.NavigationApplication;
import com.mappls.sdk.navigation.NavigationFormatter;
import com.mappls.sdk.navigation.NavigationLocationProvider;
import com.mappls.sdk.navigation.camera.INavigation;
import com.mappls.sdk.navigation.camera.NavigationCamera;
import com.mappls.sdk.navigation.data.WayPoint;
import com.mappls.sdk.navigation.events.NavEvent;
import com.mappls.sdk.navigation.iface.INavigationListener;
import com.mappls.sdk.navigation.iface.IStopSession;
import com.mappls.sdk.navigation.iface.JunctionInfoChangedListener;
import com.mappls.sdk.navigation.iface.JunctionViewsLoadedListener;
import com.mappls.sdk.navigation.iface.LocationChangedListener;
import com.mappls.sdk.navigation.iface.NavigationEventListener;
import com.mappls.sdk.navigation.iface.NavigationEventLoadedListener;
import com.mappls.sdk.navigation.iface.OnSpeedLimitListener;
import com.mappls.sdk.navigation.model.AdviseInfo;
import com.mappls.sdk.navigation.model.Junction;
import com.mappls.sdk.navigation.notifications.NavigationNotification;
import com.mappls.sdk.navigation.routing.NavigationStep;
import com.mappls.sdk.navigation.util.GPSInfo;
import com.mappls.sdk.services.api.directions.models.DirectionsRoute;
import com.mappls.sdk.services.api.directions.models.LegStep;
import com.mappls.sdk.services.api.event.route.model.ReportDetails;
import com.mappls.sdk.services.utils.Constants;
import com.suzuki.R;
import com.suzuki.activity.DeviceListingScanActivity;
import com.suzuki.activity.LastParkedLocationActivity;
import com.suzuki.activity.NavigationActivity;
import com.suzuki.activity.RouteActivity;
import com.suzuki.activity.RouteNearByActivity;
import com.suzuki.adapter.NavigationPagerAdapter;
import com.suzuki.application.SuzukiApplication;
import com.suzuki.broadcaster.BleConnection;
import com.suzuki.broadcaster.BluetoothCheck;
import com.suzuki.broadcaster.MapShortDistBroadcast;
import com.suzuki.maps.camera.MathUtils;

import com.suzuki.maps.camera.ProgressChangeListener;
import com.suzuki.maps.camera.RouteInformation;
import com.suzuki.maps.plugins.BearingIconPlugin;
import com.suzuki.maps.plugins.DirectionPolylinePlugin;
import com.suzuki.maps.plugins.MapEventsPlugin;
import com.suzuki.maps.plugins.RouteArrowPlugin;
import com.suzuki.model.Stop;
import com.suzuki.pojo.EvenConnectionPojo;
import com.suzuki.pojo.RiderProfileModule;
import com.suzuki.utils.Logger;
import com.suzuki.utils.NavigationLocationEngine;
import com.suzuki.utils.Utils;
import com.suzuki.views.LockableBottomSheetBehavior;
import com.suzuki.views.RecenterButton;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;

import io.realm.Realm;
import timber.log.Timber;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.RECEIVER_EXPORTED;
import static android.view.View.GONE;
import static com.mappls.sdk.maps.Mappls.getApplicationContext;
import static com.mappls.sdk.maps.style.layers.PropertyFactory.visibility;
import static com.suzuki.activity.HomeScreenActivity.HOME_SCREEN_OBJ;
import static com.suzuki.activity.HomeScreenActivity.mBoundService;
import static com.suzuki.activity.RouteNearByActivity.startClicked;
import static com.suzuki.application.SuzukiApplication.calculateCheckSum;
import static com.suzuki.broadcaster.BluetoothCheck.BLUETOOTH_STATE;
import static com.suzuki.fragment.DashboardFragment.BLE_CONNECTED;
import static com.suzuki.fragment.DashboardFragment.staticConnectionStatus;

import static com.suzuki.fragment.DashboardFragment.NoSignal;
import static com.suzuki.utils.Common.BikeBleName;
import static com.suzuki.utils.Common.EXCEPTION;
import static com.suzuki.utils.Common.FIRST_TIME;


public class NavigationFragment extends Fragment implements
        View.OnClickListener,
        MapplsMap.OnMoveListener,
        LocationChangedListener,
        INavigationListener,
        OnMapReadyCallback, INavigation {

    private boolean isItSaveForFragmentTransaction = true;
    private final boolean mFragmentTransactionSave = true;
    private SuzukiApplication app;
    private int currentPageLocation = 1;
    private GPSInfo gpsInfo;
    private ProgressChangeListener progressChangeListener;
    private AnimationSet fadeInSlowOut;
    private MapplsMap mapmyIndiaMap;
    private static SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
private String ETA;
    //map related
    private BroadcastReceiver mBroadcastReceiver;
    private LocationComponent locationPlugin;
    private DirectionPolylinePlugin directionPolylinePlugin;
    private BearingIconPlugin bearingIconPlugin;
    private NavigationCamera camera;
    private RouteArrowPlugin routeArrowPlugin;
    private MapEventsPlugin mapEventsPlugin;
    private NavigationLocationEngine navigationLocationEngine;
    private FloatingActionButton settingFloatingActionButton;

    //Views
    private BottomSheetBehavior mBottomSheetBehavior;
    private TextView warningTextView;
    private TextView otherInfoTextView;
    private BleConnection mmReceiver;
    private RecenterButton mFollowMeButton;
    private View nextInstructionContainer;
    private ViewPager navigationStripViewPager;
    private ImageView nextInstructionImageView;
    private TextView soundChipText;
    private TextView text_view_reach_eta;
    private TextView text_view_total_distance_left;
    private FloatingActionButton soundFab;
    private boolean ReRouting = false, routeProgress = false;
   private boolean SearchingGPS = false;
    private boolean DestinationReached = false;
    private boolean ViaPoints = false;
    private final Timer timer = new Timer();
    private Handler handler = new Handler();
    private int maneuverID = 0;
    private final Handler gpsHandler = new Handler();
    private String dataShortDistanceUnit, dataEta, dataRemainingDistanceUnit, dataShortDistance, dataRemainingDistance;
    public static String NavigationStatus = "1", navigationModeEnabled = "1";
    private MapShortDistBroadcast mReceiver;
    private BluetoothCheck bluetoothReceiver;
    private RelativeLayout top_strip_layout,options_recycler_view_container;
    FrameLayout llRedAlertBle;
    String lastImageSetFor = null;
    private ImageView junctionViewImageView;
public int saved_speed, top_speeds;
    ImageView ivCustomClose;
    TextView tvCustomTextBtn ;

    private boolean NoNetwork;
    private long last_frame_timestamp = 0L;
    private int counter=0;
    private long DELAY=300;
    private boolean lastparkedlocation = false;

    private int sendCount = 0;

    private boolean initialSendingDone = false;


    private final Runnable gpsRunnable = new Runnable() {
        @Override
        public void run() {
            if (BLUETOOTH_STATE ==false){
                Toast.makeText(getContext(), "ble off", Toast.LENGTH_SHORT).show();
            }
            if (getActivity() == null)
                return;

            if (gpsInfo != null && !gpsInfo.fixed) {
             //   SearchingGPS = true;
                if (warningTextView != null)
                    warningTextView.setBackgroundColor(getResources().getColor(R.color.app_theme_color));
            } else if (gpsInfo != null && gpsInfo.usedSatellites < 3) {
               SearchingGPS = false;
                if (warningTextView != null)
                    warningTextView.setBackgroundColor(getResources().getColor(R.color.common_gray));
            }/* else if (!Connectivity.isConnected(getActivity())) {
                if (warningTextView != null)
                    warningTextView.setBackgroundColor(getResources().getColor(R.color.app_theme_color));
            }*/ else {
                SearchingGPS = false;
                dismissSnackBar();
            }
//            logData("Navigation Fragment gps: " +SearchingGPS );
        }
    };
    private int temp=0;


//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onClusterDataRecev(ClusterStatusPktPojo event) {
//        if (event.getClusterData().length() == 30) {
//            ClusterDataPacket(event.getClusterByteData());
//        }
//    }

    public NavigationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


//        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        if (mBluetoothAdapter == null) {
//            // Device does not support Bluetooth
//        } else if (!mBluetoothAdapter.isEnabled()) {
//
//            Toast.makeText(getContext(), "Turn on Your bluetooth", Toast.LENGTH_SHORT).show();
//            // Bluetooth is not enabled :)
//        } else {
//            // Bluetooth is enabled
//
//        }
        //  setBluetoothStatus();

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

//        if(!mBluetoothAdapter.isEnabled()){
//            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
//            alertBuilder.setCancelable(true);
//            alertBuilder.setMessage("Do you want to enable bluetooth");
//            alertBuilder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.dismiss();
//                    mBluetoothAdapter.enable();
//                }
//            });
//            AlertDialog alert = alertBuilder.create();
//            alert.show();
//        }

        mBroadcastReceiver = new MyLocalBroadcastReceiver();
        app = getMyApplication();
        navigationLocationEngine = new NavigationLocationEngine();
        // RouteActivity.startClicked = true;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

    }



//    public void ClusterDataPacket(byte[] u1_buffer) {
//        if ((u1_buffer[0] == -91) && (u1_buffer[1] == 55) && (u1_buffer[29] == 127)) {
//
//            byte Crc = calculateCheckSum(u1_buffer);
//
//            if (u1_buffer[28] == Crc) {
//                String Cluster_data = new String(u1_buffer);
//
//                Odometer = Cluster_data.substring(5, 11);
//
//                /*sharedPreferences = getApplicationContext().getSharedPreferences("vehicle_data",MODE_PRIVATE);
//                editor = sharedPreferences.edit();`
//                editor.putString("odometer",Odometer);
//                editor.apply();*/
//
//
//                // Toast.makeText(app, ""+speed, Toast.LENGTH_SHORT).show();
//
//                top_speeds= Integer.parseInt(Cluster_data.substring(2,5));
//
//                getActivity().runOnUiThread(() -> {
//                     top_speeds= Integer.parseInt(Cluster_data.substring(2,5));
//                    SharedPreferences.Editor editors = getApplicationContext().getSharedPreferences("top_speed", Context.MODE_MULTI_PROCESS).edit();
//                    editors.putInt("top_speed", top_speeds);
//                    editors.apply();
//
//                    SharedPreferences prefs = getApplicationContext().getSharedPreferences("top_speed", MODE_PRIVATE);
//                    saved_speed = prefs.getInt("top_speed", 0);//"No name defined" is the default value.
//
//                    if (navigationStarted==true){
//                        if (top_speeds>=saved_speed){
//                            SharedPreferences.Editor edit = getApplicationContext().getSharedPreferences("top_speed", MODE_PRIVATE).edit();
//                            edit.putInt("new_top_speed", top_speeds);
//                            edit.apply();
//                        }
//
//                    }
//
//                });
//            }
//        }
//    }



//    private void setBluetoothStatus() {
//
////        if (BLUETOOTH_STATE) {
////            if (staticConnectionStatus) {
////                Toast.makeText(getContext(), "on1", Toast.LENGTH_SHORT).show();
////            }
////            else {
////                Toast.makeText(getContext(), "off1", Toast.LENGTH_SHORT).show();
////            }
////
////        } else {
////            Toast.makeText(getContext(), "off2", Toast.LENGTH_SHORT).show();
////        }
//        IntentFilter intentFilter = new IntentFilter("status");
//
//        mmReceiver = new BleConnection() {
//
//            @Override
//            public void onReceive(Context context, Intent intent) {
//
//                boolean status = Objects.requireNonNull(intent.getExtras()).getBoolean("status");
//
//                if (BLUETOOTH_STATE) {
//                    if (status) {
//                        Toast.makeText(getContext(), "on2", Toast.LENGTH_SHORT).show();
//                        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//
//                        if(!mBluetoothAdapter.isEnabled()){
//                            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
//                            alertBuilder.setCancelable(true);
//                            alertBuilder.setMessage("Do you want to enable bluetooth");
//                            alertBuilder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                    mBluetoothAdapter.enable();
//                                }
//                            });
//                            AlertDialog alert = alertBuilder.create();
//                            alert.show();
//                        }
//                    }
//                    else {
//                        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//                            if(!mBluetoothAdapter.isEnabled()){
//                                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
//                                alertBuilder.setCancelable(true);
//                                alertBuilder.setMessage("Do you want to enable bluetooth");
//                                alertBuilder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        dialog.dismiss();
//                                      //  mBluetoothAdapter.enable();
//                                        startActivity(new Intent(getActivity(), DeviceListingScanActivity.class));
//
//
//                                    }
//                                });
//                                AlertDialog alert = alertBuilder.create();
//                                alert.show();
//                            }
//
//                        Toast.makeText(getContext(), "off3", Toast.LENGTH_SHORT).show();
//                    }
//
//                } else {
//                    Toast.makeText(getContext(), "off4", Toast.LENGTH_SHORT).show();
//                }
//            }
//        };
//
//        Objects.requireNonNull(getActivity()).registerReceiver(mmReceiver, intentFilter);
//    }


    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);

    }
    private void removeFocus() {
        llRedAlertBle.requestFocus();
        if (getContext() != null) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(llRedAlertBle.getWindowToken(), 0);
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectionEvent(EvenConnectionPojo event) {

        if (!BLUETOOTH_STATE) {
            BLE_CONNECTED = false;

            Intent i = new Intent("status").putExtra("status", BLE_CONNECTED);
            requireActivity().sendBroadcast(i);

            if (BLE_CONNECTED)
            {
                llRedAlertBle.setVisibility(View.GONE);
                top_strip_layout.setVisibility(View.VISIBLE);
                options_recycler_view_container.setVisibility(View.VISIBLE);
                soundFab.setVisibility(View.VISIBLE);
                //   main_layout.getForeground().setAlpha(0); // dim


            }
            else{
                if(lastparkedlocation){
                    llRedAlertBle.setVisibility(GONE);
                    soundFab.setVisibility(View.VISIBLE);
                    mFollowMeButton.setVisibility(View.VISIBLE);
                    top_strip_layout.setVisibility(View.VISIBLE);
                    options_recycler_view_container.setVisibility(View.VISIBLE);
                }
                else {
                    llRedAlertBle.setVisibility(View.VISIBLE);
                    soundFab.setVisibility(GONE);
                    mFollowMeButton.setVisibility(GONE);
                    top_strip_layout.setVisibility(GONE);
                    options_recycler_view_container.setVisibility(GONE);
                    //   main_layout.getForeground().setAlpha( 220); // dim
                }

            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

            View view= inflater.inflate(R.layout.fragment_navigation, container, false);

//
//        final Handler handler = new Handler();
//        final int delay = 1000; // 1000 milliseconds == 1 second
//
//        handler.postDelayed(new Runnable() {
//            public void run() {
//                SharedPreferences prefs = getApplicationContext().getSharedPreferences("top_speed", MODE_PRIVATE);
//                int saved_speed = prefs.getInt("new_top_speed", 0);
//                Toast.makeText(getActivity(), ""+saved_speed, Toast.LENGTH_SHORT).show();
//                handler.postDelayed(this, delay);
//            }
//        }, delay);



        return view ;

    }


    private void  setBluetoothStatus() {

        if (BLUETOOTH_STATE) {
            if (BLE_CONNECTED) {
                llRedAlertBle.setVisibility(View.GONE);
                soundFab.setVisibility(View.VISIBLE);

                top_strip_layout.setVisibility(View.VISIBLE);
                options_recycler_view_container.setVisibility(View.VISIBLE);
                //    main_layout.getForeground().setAlpha(0); // dim

            }
            else{
                if(lastparkedlocation){
                    llRedAlertBle.setVisibility(GONE);
                    top_strip_layout.setVisibility(View.VISIBLE);
                    soundFab.setVisibility(View.VISIBLE);
                    mFollowMeButton.setVisibility(View.VISIBLE);

                    options_recycler_view_container.setVisibility(GONE);
                }
                else {
                    llRedAlertBle.setVisibility(View.VISIBLE);
                    top_strip_layout.setVisibility(GONE);
                    soundFab.setVisibility(GONE);
                    mFollowMeButton.setVisibility(GONE);

                    options_recycler_view_container.setVisibility(GONE);
                    //   main_layout.getForeground().setAlpha( 220); // dim
                }

            }

        } else{
            if(lastparkedlocation){
                llRedAlertBle.setVisibility(GONE);
                top_strip_layout.setVisibility(View.VISIBLE);
                soundFab.setVisibility(View.VISIBLE);
                mFollowMeButton.setVisibility(View.VISIBLE);

                options_recycler_view_container.setVisibility(View.VISIBLE);
            }
            else {
                llRedAlertBle.setVisibility(View.VISIBLE);
                top_strip_layout.setVisibility(GONE);
                soundFab.setVisibility(GONE);
                mFollowMeButton.setVisibility(GONE);

                options_recycler_view_container.setVisibility(GONE);
            }
            //    main_layout.getForeground().setAlpha( 220); // dim

        }

        IntentFilter intentFilter = new IntentFilter("status");

        mmReceiver = new BleConnection() {

            @Override
            public void onReceive(Context context, Intent intent) {

                boolean status = Objects.requireNonNull(intent.getExtras()).getBoolean("status");

                if (BLUETOOTH_STATE) {
                    if (status) {
                        llRedAlertBle.setVisibility(View.GONE);
                        top_strip_layout.setVisibility(View.VISIBLE);
                        soundFab.setVisibility(View.VISIBLE);

                        options_recycler_view_container.setVisibility(View.VISIBLE);
                        // main_layout.getForeground().setAlpha( 0); // dim

                    }
                    else{
                        if(lastparkedlocation){
                            llRedAlertBle.setVisibility(GONE);
                            top_strip_layout.setVisibility(View.VISIBLE);
                            soundFab.setVisibility(View.VISIBLE);
                            mFollowMeButton.setVisibility(View.VISIBLE);


                            options_recycler_view_container.setVisibility(View.VISIBLE);
                        }
                        else {
                            llRedAlertBle.setVisibility(View.VISIBLE);
                            top_strip_layout.setVisibility(GONE);
                            soundFab.setVisibility(GONE);
                            mFollowMeButton.setVisibility(GONE);


                            options_recycler_view_container.setVisibility(GONE);
                        }
                        //   main_layout.getForeground().setAlpha( 220); // dim

                    }

                } else{
                    if(lastparkedlocation){
                        llRedAlertBle.setVisibility(GONE);
                        top_strip_layout.setVisibility(View.VISIBLE);
                        soundFab.setVisibility(View.VISIBLE);
                        mFollowMeButton.setVisibility(View.VISIBLE);


                        options_recycler_view_container.setVisibility(View.VISIBLE);
                    }
                    else {
                        llRedAlertBle.setVisibility(View.VISIBLE);
                        top_strip_layout.setVisibility(GONE);
                        soundFab.setVisibility(GONE);
                        mFollowMeButton.setVisibility(GONE);

                        options_recycler_view_container.setVisibility(GONE);
                        //   main_layout.getForeground().setAlpha( 220); // dim
                    }

                }
            }
        };

//        requireActivity().registerReceiver(mmReceiver, intentFilter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireActivity().registerReceiver(mmReceiver, intentFilter, RECEIVER_EXPORTED);
        }else {
            requireActivity().registerReceiver(mmReceiver, intentFilter);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);

        getRideCounts();

        if (getActivity() instanceof NavigationActivity) {
            ((NavigationActivity) getActivity()).getMapView().getMapAsync(this);
            lastparkedlocation = ((NavigationActivity) getActivity()).getLastParkedLocation();
        }


        MapplsNavigationHelper.getInstance().addNavigationListener(this);

        onRouteProgress(MapplsNavigationHelper.getInstance().getAdviseInfo());
        MapplsNavigationHelper.getInstance().setOnSpeedLimitListener(new OnSpeedLimitListener() {
            @Override
            public void onSpeedChanged(double speed, boolean overSpeed) {

                if (settingFloatingActionButton != null) {
                    settingFloatingActionButton.setBackgroundTintList(ColorStateList.valueOf(overSpeed ? Color.RED : Color.GREEN));
                }
            }
        });
        MapplsNavigationHelper.getInstance().setJunctionViewEnabled(true);

        MapplsNavigationHelper.getInstance().setNavigationEventLoadedListener(new NavigationEventLoadedListener() {
            @Override
            public void onNavigationEventsLoaded(List<ReportDetails> events) {

                Timber.d(new Gson().toJson(events));
                if (MapplsNavigationHelper.getInstance().getEvents() != null && MapplsNavigationHelper.getInstance().getEvents().size() > 0&& mapEventsPlugin!=null) {
                    mapEventsPlugin.setNavigationEvents(MapplsNavigationHelper.getInstance().getEvents());
                }
            }
        });

        MapplsNavigationHelper.getInstance().setJunctionVisualPromptBefore(200);

        MapplsNavigationHelper.getInstance().setJunctionViewsLoadedListener(new JunctionViewsLoadedListener() {
            @Override
            public void onJunctionViewsLoaded(List<Junction> junctions) {

            }
        });

        MapplsNavigationHelper.getInstance().setJunctionInfoChangedListener(new JunctionInfoChangedListener() {
            @Override
            public void junctionInfoChanged(Junction point) {
                if (point == null) {
                    Timber.tag("JunctionView").d("Junction point is null");
                    lastImageSetFor = null;
                    junctionViewImageView.setVisibility(View.INVISIBLE);
                    return;
                } else {
                    Timber.tag("JunctionView").d("Junction View approaching %s", point.getLeftDistance());
                    if (point.bitmap != null) {
                        junctionViewImageView.setImageBitmap(point.bitmap);
                    }
                    junctionViewImageView.setVisibility(View.VISIBLE);
                }


            }
        });

        MapplsNavigationHelper.getInstance().setNavigationEventListener(new NavigationEventListener() {
            @Override
            public void onNavigationEvent(NavEvent navEvent) {
                if (navEvent != null)
                    Timber.d("Navigation Event approaching %s in %f", navEvent.getName(), navEvent.getDistanceLeft());
            }
        });

        navigationModeEnabled="1";//resetting

        // sendUserInfoPacket();


        updateDisplay.run();

        tvCustomTextBtn.setOnClickListener(v -> {
//            Intent intent=new Intent(getActivity(),DeviceListingScanActivity.class);
//            intent.putExtra("navigationScreen","navigationScreen");
//            startActivity(intent);
           startActivity(new Intent(getActivity(), DeviceListingScanActivity.class));

        });
        ivCustomClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                llRedAlertBle.setVisibility(View.GONE);
                top_strip_layout.setVisibility(View.VISIBLE);
                soundFab.setVisibility(View.VISIBLE);

                options_recycler_view_container.setVisibility(View.VISIBLE);
                //       main_layout.getForeground().setAlpha( 0);

            }
        });


        setBluetoothStatus();


        Activity activity = getActivity();

        if (activity != null) {
            BikeBleName.observe(requireActivity(), new Observer<String>() {
                @Override
                public void onChanged(String s) {
                    if (s.isEmpty()) {
                        if(lastparkedlocation){
                            llRedAlertBle.setVisibility(GONE);
                            top_strip_layout.setVisibility(View.VISIBLE);
                            soundFab.setVisibility(View.VISIBLE);
                            mFollowMeButton.setVisibility(View.VISIBLE);


                            options_recycler_view_container.setVisibility(View.VISIBLE);
                        }
                        else {
                            llRedAlertBle.setVisibility(View.VISIBLE);
                            top_strip_layout.setVisibility(GONE);

                            soundFab.setVisibility(GONE);
                            mFollowMeButton.setVisibility(GONE);
                            options_recycler_view_container.setVisibility(GONE);
                        }


                    } else {
                        llRedAlertBle.setVisibility(View.GONE);
                        soundFab.setVisibility(View.VISIBLE);


                        top_strip_layout.setVisibility(View.VISIBLE);
                        options_recycler_view_container.setVisibility(View.VISIBLE);
                        //   main_layout.getForeground().setAlpha( 0);
                    }
                }
            });
        }
        /*requireContext().registerReceiver(this.mConnReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));*/

        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getActivity().registerReceiver(this.mConnReceiver, intentFilter, RECEIVER_EXPORTED);
        }else {
            getActivity().registerReceiver(this.mConnReceiver, intentFilter);
        }

    }

    private void sendUserInfoPacket() {
      /*  byte[] PKT_Userinfo = getSmartPhoneUSERINFOPKT(getApplicationContext().getSharedPreferences("user_data", MODE_PRIVATE).getString("name", ""));
        if (mBoundService != null) {
            for (int i = 0; i < 3; i++) {
                final int count = i + 1;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBoundService.writeDataFromAPPtoDevice(PKT_Userinfo, 36);
                        Log.d("Packet sent", "Count: " + count);
                    }
                }, i * 50);
            }
        }*/
       /* byte[] PKT_Userinfo = getSmartPhoneUSERINFOPKT(getApplicationContext().getSharedPreferences("user_data", MODE_PRIVATE).getString("name", ""));
        if (mBoundService != null && sendCount < 3) {
            mBoundService.writeDataFromAPPtoDevice(PKT_Userinfo, 36);
            sendCount++;
        }*/
        byte[] PKT_Userinfo = getSmartPhoneUSERINFOPKT(getApplicationContext().getSharedPreferences("user_data", MODE_PRIVATE).getString("name", ""));
        if (mBoundService != null && sendCount < 5) {
            mBoundService.writeDataFromAPPtoDevice(PKT_Userinfo, 36);
            sendCount++;
            handler.postDelayed(this::sendUserInfoPacket, 200);

            Log.e("Data Packet count1", String.valueOf(sendCount));
        }

    }

    private void getRideCounts() {

        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(realm1 -> {

                RiderProfileModule riderProfile = realm1.where(RiderProfileModule.class).equalTo("id", 1).findFirst();

                if (riderProfile == null) ;
                else if (riderProfile != null) {

                    String ride_count = String.valueOf(riderProfile.getRideCounts());
                    sharedPreferences = getApplicationContext().getSharedPreferences("vehicle_data",MODE_PRIVATE);
                    editor = sharedPreferences.edit();
                    editor.putString("ride_count", ride_count);
                    editor.apply();

                    //FuelLevel = riderProfile.getFuelBars();
                }


            });
        } catch (Exception e) {
            Log.e(EXCEPTION, e + " viewRecords " + e);

        }
    }


    private void drawPolyLine() {
        if (getActivity() == null)
            return;

        if(MapplsNavigationHelper.getInstance().getCurrentRoute() == null) return;
        String locations = MapplsNavigationHelper.getInstance().getCurrentRoute().geometry();

        List<LineString> listOfPoint = new ArrayList<>();
        assert locations != null;
        listOfPoint.add(LineString.fromPolyline(locations, Constants.PRECISION_6));
        LatLng endPoint = new LatLng();
        if (app.getELocation() != null) {
            endPoint.setLatitude(Double.parseDouble(String.valueOf(app.getELocation().latitude)));
            endPoint.setLongitude(Double.parseDouble(String.valueOf(app.getELocation().longitude)));
        }
        if (directionPolylinePlugin != null) {
        //    directionPolylinePlugin.setTrips(listOfPoint, null, endPoint, app.getViaPoints(), app.getTrip().routes()); // need to add way point
            directionPolylinePlugin.setTrips(listOfPoint, null, endPoint, app.getViaPoints(), MapplsNavigationHelper.getInstance().getDirectionsResponse().routes());

            directionPolylinePlugin.setEnabled(true);
        }
    }


    @Override
    public void onDestroyView() {
        if (getActivity() == null)
            return;

        if (directionPolylinePlugin != null)
            directionPolylinePlugin.removeAllData();

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            locationModeNavigation(false);
        }
        MapplsNavigationHelper.getInstance().removeNavigationListener(this);
        if (mapmyIndiaMap != null) {
            mapmyIndiaMap.removeOnMoveListener(this);
            mapmyIndiaMap.removeAnnotations();
        }
        MapplsNavigationHelper.getInstance().stopNavigation();
        MapplsNavigationHelper.getInstance().deleteSession(BikeBleName.getValue(), new IStopSession() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure() {

            }
        });

        timer.cancel();
        try {
            handler.removeCallbacks(updateDisplay);
        } catch (Exception ignored) {
        }

        if (this.mConnReceiver != null) {
            try {
                requireContext().unregisterReceiver(mConnReceiver);
            } catch (Exception ignored) {

            }
        }

        /*try {

            if (mReceiver != null) {
                getActivity().getApplicationContext().unregisterReceiver(mReceiver);
            }

        } catch (IllegalArgumentException e) {

            e.printStackTrace();
        }*/

        startClicked = true;
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        if (directionPolylinePlugin != null)
            directionPolylinePlugin.setEnabled(true);
        super.onDetach();
    }


    public SuzukiApplication getMyApplication() {

        if (getActivity() != null)
            return ((SuzukiApplication) getActivity().getApplication());
        else
            return null;
    }

    @Override
    public void onClick(View view) {

        if (getActivity() == null)
            return;

        switch (view.getId()) {

           /* case R.id.close_navigation_button:
                getActivity().onBackPressed();
                break;*/
            case R.id.follow_button:
                if (llRedAlertBle.getVisibility()==View.INVISIBLE){
                    nextInstructionContainer.setVisibility(View.VISIBLE);

                    followMe(true);
                    setNavigationPadding(true);
                }


                break;

            case R.id.sound_btn:

                toggleMute();
                break;

            case R.id.reset_bounds_button:
                // MMI changes line no(944-964)
//                if (camera != null)
//                   // camera.updateCameraTrackingMode(false);
//
//                List<NavigationStep> routeDirectionIfs = app.getRouteDirections();
//
//
//                ArrayList<LatLng> points = new ArrayList<>();
//
//
//                for (NavigationStep routeDirectionInfo : routeDirectionIfs) {
//                    NavLocation navLocation = routeDirectionInfo.getNavLocation();
//                    if (navLocation != null)
//                        points.add(new LatLng(navLocation.getLatitude(), navLocation.getLongitude()));
//                }
//                if (points.size() > 1) {
//                    LatLngBounds bounds = new LatLngBounds.Builder().includes(points).build();
//
//                    mapmyIndiaMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));
//                }

                if (camera != null)
                    camera.updateCameraTrackingMode(NavigationCamera.NAVIGATION_TRACKING_MODE_NONE); // MMI changes
                List<NavigationStep> routeDirectionIfs = app.getRouteDirections();


                ArrayList<LatLng> points = new ArrayList<>();


                for (NavigationStep routeDirectionInfo : routeDirectionIfs) {
                    NavLocation navLocation = routeDirectionInfo.getNavLocation();
                    if (navLocation != null)
                        points.add(new LatLng(navLocation.getLatitude(), navLocation.getLongitude()));
                }
                if (points.size() > 1) {
                    LatLngBounds bounds = new LatLngBounds.Builder().includes(points).build();

                    mapmyIndiaMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));
                }

                break;


        }
    }

    private float getLocationAngle(NavLocation currentLocation) {
        try {
            List<NavLocation> routeNodes = getMyApplication().getCalculatedRoute().getPath();

            return (float) MathUtils.wrap(currentLocation
                            .bearingTo(routeNodes.get((routeNodes.indexOf(currentLocation) + 1))),
                    0, 360);
        } catch (Exception e) {
            return 0;
        }

    }
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onMapReady(MapplsMap map) {
        map.getUiSettings().enableLogoClick(false);


        try {
            Timber.e("onMapReady");
            if (getActivity() == null)
                return;
            mapmyIndiaMap = map;
            map.getUiSettings().setLogoMargins(0, 0, 0, 250);
            map.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    mapmyIndiaMap.removeAnnotations();
                    locationPlugin = mapmyIndiaMap.getLocationComponent();
                    locationPlugin.setRenderMode(RenderMode.GPS);

                    directionPolylinePlugin = ((NavigationActivity) getActivity()).getDirectionPolylinePlugin();
                    bearingIconPlugin = ((NavigationActivity) getActivity()).getBearingIconPlugin();
                    routeArrowPlugin = ((NavigationActivity) getActivity()).getRouteArrowPlugin();
                    mapEventsPlugin = ((NavigationActivity) getActivity()).getMapEventPlugin();
                    List<NavigationStep> adviseArrayList = app.getRouteDirections();


                    AdviseInfo adviseInfo = MapplsNavigationHelper.getInstance().getAdviseInfo();
if (adviseInfo!=null){
    int position = adviseInfo.getPosition() == 0 ? adviseInfo.getPosition() : adviseInfo.getPosition() - 1;
    NavigationStep currentRouteDirectionInfo = adviseArrayList.get(position);
    LegStep routeLeg = (LegStep) currentRouteDirectionInfo.getExtraInfo();
    LegStep nextRouteLeg = null;
    if (routeArrowPlugin != null) {
        routeArrowPlugin.addUpcomingManeuverArrow(routeLeg,nextRouteLeg);
    }
    directionPolylinePlugin.setOnNewRouteSelectedListener(new DirectionPolylinePlugin.OnNewRouteSelectedListener() {
        @Override
        public void onNewRouteSelected(int index, DirectionsRoute directionsRoute) {
            MapplsNavigationHelper.getInstance().setRouteIndex(index);
        }
    });
    if (bearingIconPlugin != null) {
        bearingIconPlugin.setBearingLayerVisibility(false);
        bearingIconPlugin.setBearingIcon(0, null);
    }


}


//            if (locationPlugin != null)
//                locationPlugin.forceLocationUpdate(getLocationForNavigation());
//
//                    map.addOnMoveListener(NavigationFragment.this);
//
//            if (progressChangeListener != null && camera != null) {
//                camera.updateCameraTrackingLocation(true);
//                AdviseInfo adviseInfo = MapplsNavigationHelper.getInstance().getAdviseInfo();
//                adviseInfo.setLocation(NavigationLocationProvider.convertLocation(getLocationForNavigation(), app));
//                onRouteProgress(MapmyIndiaNavigationHelper.getInstance().getAdviseInfo());
//            }


                    if (getActivity() != null && ((NavigationActivity) getActivity()).getMapView() != null) {
                        MapView mapView = ((NavigationActivity) getActivity()).getMapView();
                        mapView.getCompassView().setY((100f));

                        ((NavigationActivity) getActivity()).getMapView().setOnTouchListener((view1, motionEvent) -> {

                            if (!mBottomSheetBehavior.isHideable()) {
                                followMe(false);

                            }

                            return view1.onTouchEvent(motionEvent);
                        });
                    }

                    if (ActivityCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        locationModeNavigation(true);
                    }
                    setNavigationPadding(true);
                    drawPolyLine();
                    initCamera();

                    // showRouteClassesDetailToast();

                    style.getLayer("mappls-location-shadow-layer").setProperties(visibility(Property.NONE));
                }
            });


        } catch (Exception e) {
            Timber.e(e);
        }
    }

//    @SuppressLint("ClickableViewAccessibility")
//    @Override
//    public void onMapReady(MapplsMap map) {
//
//
//        try {
//            if (getActivity() == null)
//                return;
//            mapmyIndiaMap = map;
//
////            if (new CurrentLoc().nightTime()) {
////                mapmyIndiaMap.setStyle(Style.NIGHT_MODE);
////            }
//
//            setNavigationPadding(true);
//            mapmyIndiaMap.removeAnnotations();
//            locationPlugin = mapmyIndiaMap.getLocationComponent();
//
//            directionPolylinePlugin = ((NavigationActivity) getActivity()).getDirectionPolylinePlugin();
//            bearingIconPlugin = ((NavigationActivity) getActivity()).getBearingIconPlugin();
//
//            if (bearingIconPlugin != null) {
//                bearingIconPlugin.setBearingLayerVisibility(false);
//                bearingIconPlugin.setBearingIcon(0, null);
//            }
//
//
//            if (locationPlugin != null)
//                locationPlugin.forceLocationUpdate(getLocationForNavigation());
//
//            map.addOnMoveListener(this);
//
//            if (progressChangeListener != null && camera != null) {
//                camera.updateCameraTrackingLocation(true);
//                AdviseInfo adviseInfo = MapplsNavigationHelper.getInstance().getAdviseInfo();
//                adviseInfo.setLocation(NavigationLocationProvider.convertLocation(getLocationForNavigation(), app));
//                onRouteProgress(MapplsNavigationHelper.getInstance().getAdviseInfo());
//            }
//
//
//            if (getActivity() != null && ((NavigationActivity) getActivity()).getMapView() != null) {
//                MapView mapView = ((NavigationActivity) getActivity()).getMapView();
//                mapView.getCompassView().setY((100f));
//
//                ((NavigationActivity) getActivity()).getMapView().setOnTouchListener((view1, motionEvent) -> {
//
//                    if (!mBottomSheetBehavior.isHideable()) {
//                        followMe(false);
//
//                    }
//
//                    return view1.onTouchEvent(motionEvent);
//                });
//            }
//            initCamera();
//            drawPolyLine();
//
//
//            if (ActivityCompat.checkSelfPermission(getActivity(),
//                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//                locationModeNavigation(true);
//            }
//
//
//        } catch (Exception e) {
//            Timber.e(e);
//        }
//        map.setMaxZoomPreference(18.5f);
//        map.setMinZoomPreference(4f);
//    }

    @Override
    public void onMapError(int i, String s) {

    }

    @Override
    public void onStart() {
        super.onStart();
//        updateDisplay.run();
        isItSaveForFragmentTransaction = true;
        IntentFilter intentFilter = new IntentFilter(NavigationNotification.NAVIGATION_STOP_NAVIGATION_SERVICE_ACTION);
        if (getActivity() != null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getActivity().registerReceiver(mBroadcastReceiver, intentFilter, RECEIVER_EXPORTED);
            }else {
                getActivity().registerReceiver(mBroadcastReceiver, intentFilter);
            }
        }
//            getActivity().registerReceiver(mBroadcastReceiver, new IntentFilter(NavigationNotification.NAVIGATION_STOP_NAVIGATION_SERVICE_ACTION));


    }

    @Override
    public void onPause() {
        isItSaveForFragmentTransaction = false;
        super.onPause();
        try {
            if (getActivity() != null)
                getActivity().unregisterReceiver(mBroadcastReceiver);
        } catch (Exception e) {
            //ignore
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        boolean navigationActive = MapplsNavigationHelper.getInstance().isNavigating();
        if (getActivity() != null && isItSaveForFragmentTransaction && !navigationActive) {
            app.stopNavigation();
            dismissSnackBar();
        }

        //  sendUserInfoPacket();

        /*IntentFilter intentFilter = new IntentFilter(
                "shortdist");

        mReceiver = new MapShortDistBroadcast() {

            @Override
            public void onReceive(Context context, Intent intent) {
                //extract our message from intent

                String myshortetrs = intent.getExtras().getString("shortdist");
                // String myshortetrs ="";//= Integer.toString(shortDist);
                //  String fff = NavigationFormatter.getFormattedDistance(shortDist, (SuzukiApplication) getApplicationContext());
                //   Log.d("stdiccc", "shortd--" + fff + "---" + shortDist + "conn--" + myshortetrs);

                if (myshortetrs.contains("km")) {
                    dataShortDistanceUnit = "K";
                } else if (myshortetrs.contains("m")) {
                    dataShortDistanceUnit = "M";

                }

                myshortetrs = myshortetrs.replaceAll(" ", "");
                myshortetrs = myshortetrs.replaceAll("[a-z]", "");

                Float St_dsit = Float.valueOf(myshortetrs);
                int St_dsit_int = 0;
                if ((St_dsit < 10) && (dataShortDistanceUnit == "K")) {
                    myshortetrs = ("00000" + myshortetrs).substring(myshortetrs.length());
                    myshortetrs = myshortetrs.substring(0, 4);
                    myshortetrs = ("0000" + myshortetrs).substring(myshortetrs.length());
                }

                if ((St_dsit >= 100) && (dataShortDistanceUnit == "K")) {
                    St_dsit_int = (int) Math.round(St_dsit);

                    myshortetrs = Integer.toString(St_dsit_int);
                }

                if (myshortetrs.length() <= 5) {


                    if (myshortetrs.contains(".")) {
                        myshortetrs = ("00000" + myshortetrs).substring(myshortetrs.length());
                        myshortetrs = myshortetrs.substring(1, 5);
                        myshortetrs = ("0000" + myshortetrs).substring(myshortetrs.length());
                    } else {
                        myshortetrs = ("00000" + myshortetrs).substring(myshortetrs.length());
                        myshortetrs = myshortetrs.substring(1, 5);
                        myshortetrs = ("0000" + myshortetrs).substring(myshortetrs.length());
                    }

                    dataShortDistance = myshortetrs;
                }

                Log.d("meterrrrrSdt", "infrag--" + dataShortDistance);

            }


        };

        getActivity().registerReceiver(mReceiver, intentFilter);*/

    }

    private void updateShortDistance(String myshortetrs) {
        if (myshortetrs.contains("km")) {
            dataShortDistanceUnit = "K";
        } else if (myshortetrs.contains("m")) {
            dataShortDistanceUnit = "M";

        }

//        Log.d("TAG", "updateShortDistance:input " + myshortetrs);

        myshortetrs = myshortetrs.replaceAll(" ", "");
        myshortetrs = myshortetrs.replaceAll("[a-z]", "");

        float St_dsit = Float.parseFloat(myshortetrs);
        if (dataShortDistanceUnit.equalsIgnoreCase("K"))
            myshortetrs = "" + St_dsit;//now suzuki doesn't send into float value so convert this (1 -> 1.0)
        int St_dsit_int = 0;
        if ((St_dsit < 10) && (dataShortDistanceUnit.equals("K"))) {
            if (myshortetrs.length() == 3) {
                myshortetrs = myshortetrs + "0";
            }
            myshortetrs = ("00000" + myshortetrs).substring(myshortetrs.length());
            myshortetrs = myshortetrs.substring(0, 4);
            myshortetrs = ("0000" + myshortetrs).substring(myshortetrs.length());
        }

        if ((St_dsit >= 100) && (dataShortDistanceUnit.equals("K"))) {
            St_dsit_int = (int) Math.round(St_dsit);

            myshortetrs = Integer.toString(St_dsit_int);
        }

        if (myshortetrs.length() <= 5) {


            if (myshortetrs.contains(".")) {
                myshortetrs = ("00000" + myshortetrs).substring(myshortetrs.length());
                myshortetrs = myshortetrs.substring(1, 5);
                myshortetrs = ("0000" + myshortetrs).substring(myshortetrs.length());
            } else {
                myshortetrs = ("00000" + myshortetrs).substring(myshortetrs.length());
                myshortetrs = myshortetrs.substring(1, 5);
                myshortetrs = ("0000" + myshortetrs).substring(myshortetrs.length());
            }

            dataShortDistance = myshortetrs;
//            Log.d("TAG", "updateShortDistance: " + dataShortDistance);
        }
    }

    @Override
    public void onMoveBegin(@NonNull MoveGestureDetector moveGestureDetector) {

    }

    @Override
    public void onMove(@NonNull MoveGestureDetector moveGestureDetector) {

    }

    @Override
    public void onMoveEnd(@NonNull MoveGestureDetector moveGestureDetector) {
        if (!mBottomSheetBehavior.isHideable()) {
            followMe(false);
        }
    }

    /**
     * Initializes the {@link NavigationCamera} that will be used to follow
     * the {@link Location} from navigation service
     */
    private void initCamera() {
        // MMI changes line no: 1347-1354
//        camera = new NavigationCamera(mapmyIndiaMap, this, locationPlugin);
//        camera.start(null);

        camera = new NavigationCamera(mapmyIndiaMap);
        camera.addProgressChangeListener(this);
        camera.start(null);
        // for camera move according to marker move
        camera.updateCameraTrackingMode(NavigationCamera.NAVIGATION_TRACKING_MODE_GPS);
    }

    @RequiresPermission(anyOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    public void locationModeNavigation(boolean enable) {
        try {
            if (mapmyIndiaMap == null)
                return;

            if (enable) {
// MMI changes (removed inside if condition)

                app.getLocationProvider().setLocationChangedListener(this);

                Location location = getLocationForNavigation();

                if (locationPlugin != null && !((NavigationActivity) getActivity()).getMapView().isDestroyed())   //MMI changes
                    locationPlugin.forceLocationUpdate(location);

                mapmyIndiaMap.getLocationComponent().setLocationEngine(new NavigationLocationEngine());   // MMI changes

                followMe(true);

            } else {

                mapmyIndiaMap.getLocationComponent().setLocationEngine(LocationEngineProvider.getBestLocationEngine(getActivity())); // MMI changes
                app.getLocationProvider().setLocationChangedListener(null);
                CameraPosition.Builder builder = new CameraPosition.Builder().bearing(0).tilt(0);

                mapmyIndiaMap.moveCamera(CameraUpdateFactory.newCameraPosition(builder.build()));


            }
            // MMI changes (commented the try block)

//        try {
//            if (mapmyIndiaMap == null)
//                return;
//
//            if (enable) {
//                if (locationPlugin != null) {
//                    locationPlugin.setLocationEngine(navigationLocationEngine);
//                    locationPlugin.setRenderMode(RenderMode.GPS);
//                }
//
//
//                app.getLocationProvider().setLocationChangedListener(this);
//
//                Location location = getLocationForNavigation();
//                if (locationPlugin != null)
//                    locationPlugin.forceLocationUpdate(location);
//                followMe(true);
//
//            } else {
//
//                if (locationPlugin != null) {
//                    mapmyIndiaMap.getLocationComponent().setLocationEngine(LocationEngineProvider.getBestLocationEngine(getActivity()));
//                    locationPlugin.setRenderMode(RenderMode.NORMAL);
//                }
//
//                app.getLocationProvider().setLocationChangedListener(null);
//                CameraPosition.Builder builder = new CameraPosition.Builder().bearing(0).tilt(0);
//
//                mapmyIndiaMap.moveCamera(CameraUpdateFactory.newCameraPosition(builder.build()));
//
//
//            }
//        } catch (Exception e) {
//            Timber.e(e);
//        }
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    public Location getLocationForNavigation() {

        if (getActivity() == null)
            return null;
        Location loc = new Location(LocationManager.GPS_PROVIDER);

        NavLocation navLocation = app.getStartNavigationLocation();
        if (navLocation != null) {
            loc.setLatitude(navLocation.getLatitude());
            loc.setLongitude(navLocation.getLongitude());
        }
        try {
            NavLocation firstNavLocation = MapplsNavigationHelper.getInstance().getFirstLocation();
            if (firstNavLocation.distanceTo(NavigationLocationProvider.convertLocation(loc, app)) < 10) {
                NavLocation secondNavLocation = MapplsNavigationHelper.getInstance().getSecondLocation();
                firstNavLocation.setBearing(firstNavLocation.bearingTo(secondNavLocation));
                return NavigationLocationProvider.revertLocation(firstNavLocation, app);
            } else {
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return null;
                }
                Location location = locationPlugin != null ? locationPlugin.getLastKnownLocation() : null;
                return location != null ? location : NavigationLocationProvider.revertLocation(app.getLocationProvider().getFirstTimeRunDefaultLocation(), app);
            }
        } catch (Exception e) {
            Timber.e(e);
            return NavigationLocationProvider.revertLocation(app.getLocationProvider().getFirstTimeRunDefaultLocation(), app);
        }
    }

    public synchronized void followMe(boolean followButton) {

        if (getActivity() == null)
            return;


       /* if (camera != null && followButton != camera.isTrackingEnabled())
            //   camera.updateCameraTrackingLocation(followButton);   // MMI changes
            camera.updateCameraTrackingMode(followButton?NavigationCamera.NAVIGATION_TRACKING_MODE_GPS:NavigationCamera.NAVIGATION_TRACKING_MODE_NONE); // MMI changes
*/

        if (!followButton) {
            if (mFollowMeButton.getVisibility() != View.VISIBLE && llRedAlertBle.getVisibility() !=View.VISIBLE)
                mFollowMeButton.show();
        } else {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (locationPlugin != null) {
                    Location location = locationPlugin.getLastKnownLocation();
                    if (location != null) {
                        CameraPosition.Builder builder = new CameraPosition.Builder().tilt(45).zoom(16).target(new LatLng(location.getLatitude(), location.getLongitude()));
                        mapmyIndiaMap.moveCamera(CameraUpdateFactory.newCameraPosition(builder.build()));
                    }
                }
            }


            if (mFollowMeButton.getVisibility() == View.VISIBLE) {
                mFollowMeButton.hide();
            }
            if (bearingIconPlugin != null)
                bearingIconPlugin.setBearingLayerVisibility(false);


        }
             if (camera != null && followButton != camera.isTrackingEnabled())
            //   camera.updateCameraTrackingLocation(followButton);
            camera.updateCameraTrackingMode(followButton?NavigationCamera.NAVIGATION_TRACKING_MODE_GPS:NavigationCamera.NAVIGATION_TRACKING_MODE_NONE);

    }

    /**
     * Sets up mute UI event.
     * <p>
     * Shows chip with "Muted" text.
     * Changes sound {@link FloatingActionButton}
     * {@link Drawable} to denote sound is off.
     * <p>
     * Sets private state variable to true (muted)
     */
    private void mute() {
        setSoundChipText(getString(R.string.muted));
        showSoundChip();
        soundFabOff();
        MapplsNavigationHelper.getInstance().setMute(true);
    }

    /**
     * Sets up unmuted UI event.
     * <p>
     * Shows chip with "Unmuted" text.
     * Changes sound {@link FloatingActionButton}
     * {@link Drawable} to denote sound is on.
     * <p>
     * Sets private state variable to false (unmuted)
     */
    private void unmute() {

        setSoundChipText(getString(R.string.unmuted));
        showSoundChip();
        soundFabOn();
        MapplsNavigationHelper.getInstance().setMute(false);
    }

    /**
     * Changes sound {@link FloatingActionButton}
     * {@link Drawable} to denote sound is off.
     */
    private void soundFabOff() {
        soundFab.setImageResource(R.drawable.ic_sound_off);
    }

    /**
     * Changes sound {@link FloatingActionButton}
     * {@link Drawable} to denote sound is on.
     */
    private void soundFabOn() {
        soundFab.setImageResource(R.drawable.ic_sound_on);
    }

    /**
     * Sets {@link TextView} inside of chip view.
     *
     * @param text to be displayed in chip view ("Muted"/"Umuted")
     */
    private void setSoundChipText(String text) {
        soundChipText.setText(text);
    }

    /**
     * Shows and then hides the sound chip using {@link AnimationSet}
     */
    private void showSoundChip() {
        soundChipText.startAnimation(fadeInSlowOut);
    }

    /**
     * Initializes all animations needed to show / hide views.
     */
    private void initAnimations() {

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(300);

        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setStartOffset(1000);
        fadeOut.setDuration(1000);

        fadeInSlowOut = new AnimationSet(false);
        fadeInSlowOut.addAnimation(fadeIn);
        fadeInSlowOut.addAnimation(fadeOut);
    }

    public void toggleMute() {
        if (MapplsNavigationHelper.getInstance().isMute()) {
            unmute();
        } else {
            mute();
        }
    }

    void showSnackBar(final String text) {
        if (warningTextView != null) {
            warningTextView.setText(text);
            warningTextView.setAlpha(1);
            // warningTextView.setVisibility(View.VISIBLE);
        }
    }

    void dismissSnackBar() {
        if (warningTextView != null) {
//            warningTextView.setVisibility(GONE);
            warningTextView.setAlpha(0);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        //SearchingGPS = false;
        gpsHandler.removeCallbacksAndMessages(null);
        if (getActivity() != null && location != null && mapmyIndiaMap != null && mFragmentTransactionSave) {

            if (locationPlugin != null)
                locationPlugin.forceLocationUpdate(location);
            dismissSnackBar();
        }

    }

    @Override
    public void onGPSConnectionChanged(boolean gpsRestored) {

        if (gpsRestored) {
            SearchingGPS = false;
            showSnackBar(getString(R.string.gps_connection_restored));
            dismissSnackBar();
        }
        else {
           SearchingGPS = true;
            showSnackBar(getString(R.string.gps_connection_lost));
        }
    }

    @Override
    public void onSatelliteInfoChanged(GPSInfo gpsInfo) {

        this.gpsInfo = gpsInfo;
        gpsHandler.postDelayed(gpsRunnable, 1000);//change1
    }

    @Override
    public void onNavigationStarted() {

    }

    @Override
    public void onReRoutingRequested() {

        ReRouting = true;

    }

 /* @Override
    public void onWayPointReached(String name) {
        ViaPoints = true;
        routeProgress = false;
        Toast.makeText(getContext(), "Via Point Reached " + name, Toast.LENGTH_SHORT).show();
    }*/

    @Override
    public void onEvent(@Nullable NavEvent navEvent) {

    }

    @Override
    public void onETARefreshed(String s) {

    }

    private int getCongestionPercentage(List<String> congestionText, int index) {
//        val congestion= directionRoute?.legs()?.get(0)?.annotation()?.congestion()

        if (congestionText == null || congestionText.isEmpty())
            return R.color.navigation_eta_text_color_with_out_traffic;

        int heavy = 0;
        int low = 0;
        int congestionPercentage = 0;

        List<String> congestion = null;
        if (congestionText != null && congestionText.isEmpty() && index < congestionText.size()) {
            congestion = new ArrayList<>();
        } else {
            try{
                congestion = congestionText.subList(index, congestionText.size());
            }catch (Exception e){
                e.printStackTrace();
            }

        }
        for (int i = 0; i < congestion.size(); i++) {
            if (congestion.get(i).equals("heavy") || congestion.get(i).equals("moderate") || congestion.get(i).equals("severe")) {
                heavy++;
            } else {
                low++;
            }
        }

        if (!congestion.isEmpty()) {
            congestionPercentage = (heavy * 100 / congestion.size());
        } else {
            congestionPercentage = 1;
        }

        if (congestionPercentage <= 10) {
            return R.color.navigation_eta_text_color_with_out_traffic;
        } else if (congestionPercentage <= 25) {
            return R.color.navigation_eta_text_color_with_low_traffic;
        } else {
            return R.color.navigation_eta_text_color_with_traffic;
        }
    }

    @Override
    public void onNewRoute(String geometry/*boolean isNewRoute, final List<NavLocation> routeNodes*/) {


        if (getActivity() == null)
            return;


        getActivity().runOnUiThread(() -> {
            if (getActivity() == null)
                return;
            setAdapter();
            otherInfoTextView.setVisibility(GONE);

            if (mapmyIndiaMap != null)
                mapmyIndiaMap.removeAnnotations();
            Location location = getLocationForNavigation();
            NavLocation navLocation = new NavLocation("Router");
            if (location != null) {
                navLocation.setLatitude(location.getLatitude());
                navLocation.setLongitude(location.getLongitude());
            }
            if (mapmyIndiaMap != null)
                drawPolyLine();
           /* int color = getCongestionPercentage(MapplsNavigationHelper.getInstance().getCurrentRoute().legs().get(0).annotation().congestion(),
                    MapplsNavigationHelper.getInstance().getNodeIndex());*/
            text_view_reach_eta.setTextColor(ContextCompat.getColor(
                    getContext(),
                    R.color.white
            ));


        });


    }

    @Override
    public void onNavigationCancelled() {


    }

    @Override
    public void onNavigationFinished() {

        try{
            NavigationActivity navigationActivity = (NavigationActivity) getActivity();
            if (navigationActivity != null) {
                navigationActivity.addTripDataToRealm();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }




        //addTripDataToRealm();
        DestinationReached = true;

        //NavigationActivity.addTripACount();

        showExitNavigationAlert(getActivity(), this);

    }
    @Override
    public void onWayPointReached(WayPoint wayPoint) {
        ViaPoints = true;
        routeProgress = false;
        Toast.makeText(getContext(), "Via Point Reached " + wayPoint.getSpokenName(), Toast.LENGTH_SHORT).show();
    }


    public void showExitNavigationAlert(Context context, INavigationListener iNavigationListener) {
        Dialog dialog = new Dialog(context, R.style.custom_dialog);
        dialog.setContentView(R.layout.custom_dialog);

        TextView tvAlertText = dialog.findViewById(R.id.tvAlertText);
        tvAlertText.setText("Destination Reached. Do you want to exit navigation ?");
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
                navigationModeEnabled = "0";
                if (getActivity() == null)
                    return;
                //   updateDisplay(maneuverID, "00000", dataShortDistanceUnit, dataEta, dataRemainingDistance, dataRemainingDistanceUnit, "1", "0");
                if(lastparkedlocation){
                    Intent intent = new Intent(requireActivity(), LastParkedLocationActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

                    startActivity(intent);
                }
                else {
                    // MMI changes commented the if and else condiition
//                    if (RouteActivity.routeActivity != null) {
//                        RouteActivity.routeActivity.finish();
//                    } else if (RouteNearByActivity.routeNearByActivity != null) {
//                        RouteNearByActivity.routeNearByActivity.finish();
//                    }

                    getActivity().finish(); // MMI changes instead of onBackPressed used finish()
                }
            }
        });
        dialog.show();
    }

    @Override
    public void onRouteProgress(AdviseInfo adviseInfo) {

        SearchingGPS=false;

//        Log.d("advise inf", "--" + adviseInfo);

        if (getActivity() != null && adviseInfo != null) {
            if (adviseInfo.isRouteBeingRecalculated() && !adviseInfo.isOnRoute()) {
//                Log.d("advise inf", "--" + adviseInfo.isOnRoute() + adviseInfo.getEta() + adviseInfo.getText() + adviseInfo.getDistanceFromRoute() + adviseInfo.getDistanceToNextAdvise() + adviseInfo.getInfo());
                otherInfoTextView.setVisibility(View.VISIBLE);
                nextInstructionContainer.setVisibility(GONE);
                return;
            }
            otherInfoTextView.setVisibility(GONE);

            routeProgress = true;

            updateShortDistance(foramtShortDistance(adviseInfo.getDistanceToNextAdvise()));

            List<NavigationStep> adviseArrayList = app.getRouteDirections();
            if (adviseInfo.getInfo() instanceof LegStep) {
                LegStep legStep = (LegStep) adviseInfo.getInfo();

                if (adviseArrayList.size() > adviseInfo.getPosition() + 1) {
                    if (legStep.maneuver().type().equalsIgnoreCase("roundabout") || legStep.maneuver().type().equalsIgnoreCase("rotary")) {
                        LegStep nextLegStep = (LegStep) adviseArrayList.get(adviseInfo.getPosition() + 1).getExtraInfo();
                        float angle = Utils.roundaboutAngle(legStep, nextLegStep);
                        int manveuverID = Utils.getManeuverID(angle);
                        adviseInfo.setManeuverID(manveuverID);
                    }
                }

            }
            getMapDataToDevice(adviseInfo);

            if (navigationStripViewPager.getAdapter() == null || !(navigationStripViewPager.getAdapter() instanceof NavigationPagerAdapter))
                setAdapter();
            if (camera != null && camera.isTrackingEnabled())
                navigationStripViewPager.setCurrentItem(adviseInfo.getPosition());
            if (navigationStripViewPager.getAdapter() != null) {
                ((NavigationPagerAdapter) navigationStripViewPager.getAdapter()).setDistance(adviseInfo.getDistanceToNextAdvise());
                ((NavigationPagerAdapter) navigationStripViewPager.getAdapter()).setSelectedPosition(adviseInfo.getPosition());
            }

//            Log.d("advdvvd", "--" + adviseInfo.getDistanceToNextAdvise());

            if (adviseInfo.isOnRoute()) {
//                Log.d("advise inf", "-eta-" + adviseInfo.getEta());
                otherInfoTextView.setVisibility(GONE);
                nextInstructionContainer.setVisibility(View.VISIBLE);
            }


            try {
                nextInstructionContainer.setVisibility(View.VISIBLE);

                if (adviseInfo.getPosition() == navigationStripViewPager.getCurrentItem() && adviseInfo.getPosition() < adviseArrayList.size() - 1) {
                    //show next to next instruction icon
                    NavigationStep routeDirectionInfo = adviseArrayList.get(adviseInfo.getPosition() + 1);
                    LegStep legStep = (LegStep) routeDirectionInfo.getExtraInfo();
                    if (legStep != null) {
//                    nextInstructionManeuverView.setManeuverTypeAndModifier(legStep.maneuver().type(), legStep.maneuver().modifier());
//
//                    if (routeDirectionInfo.getManeuverID() == 21) {
//                        nextInstructionManeuverView.setManeuverTypeAndModifier(legStep.maneuver().type(), legStep.maneuver().modifier() + "_21");
//                    } else {
//                        nextInstructionManeuverView.setManeuverTypeAndModifier(legStep.maneuver().type(), legStep.maneuver().modifier());
//                    }
                        if (legStep.maneuver().type().equalsIgnoreCase("roundabout") || legStep.maneuver().type().equalsIgnoreCase("rotary")) {
                            if (adviseArrayList.size() > adviseInfo.getPosition() + 2) {
                                LegStep nextLegStep = (LegStep) adviseArrayList.get(adviseInfo.getPosition() + 2).getExtraInfo();
                                float angle = Utils.roundaboutAngle(legStep, nextLegStep);
                                // nextInstructionManeuverView.setRoundaboutAngle(angle);
                                nextInstructionImageView.setImageResource(getDrawableResId(Utils.getManeuverID(angle)));
                            }
                        } else {
                            nextInstructionImageView.setImageResource(getDrawableResId(routeDirectionInfo.getManeuverID()));
                        }

                    } else {
                        nextInstructionContainer.setVisibility(GONE);
                    }
                } else {
                    nextInstructionContainer.setVisibility(GONE);
                }
            } catch (Exception e) {
                Timber.e(e);
                nextInstructionContainer.setVisibility(GONE);
            }


            NavigationStep routeDirectionInfo = adviseArrayList.get(adviseInfo.getPosition());


            RouteInformation routeInformation = new RouteInformation();
            routeInformation.route = MapplsNavigationHelper.getInstance().getCurrentRoute();

            LegStep routeLeg = (LegStep) routeDirectionInfo.getExtraInfo();
//            Log.d("advdvvd", "--" + routeDirectionInfo.toString());


            LegStep nextRouteLeg;

            if (adviseArrayList.size() > adviseInfo.getPosition() + 1) {
                nextRouteLeg = (LegStep) adviseArrayList.get(adviseInfo.getPosition() + 1).getExtraInfo();
                routeInformation.nextLegStep = nextRouteLeg;

//                Log.d("advdvvd", "-texxxxaddvici-" + adviseInfo.getText());
            }

//            recenter_text_view.setText("ManeuverID : " + adviseInfo.getManeuverID() + "ShortText : " + adviseInfo.getShortText());

//            Log.d("advdvvd", "leggg--" + routeLeg);
//            Log.d("advdvvd", "-addvici-" + adviseInfo.getText());
            routeInformation.currentLegStep = routeLeg;
            routeInformation.distanceRemaining = adviseInfo.getLeftDistance();
            routeInformation.distanceToNextStepRemaining = adviseInfo.getDistanceToNextAdvise();


//            if (progressChangeListener != null)
//                progressChangeListener.onProgressChange(NavigationLocationProvider.revertLocation(adviseInfo.getLocation(), app), routeInformation);


//            Log.d("advdvvd", "--" + adviseInfo.getText());
            currentPageLocation = adviseInfo.getPosition();
        }

    }

    private String foramtShortDistance(int distance) {
        if (distance > 0) {
            distance = 10 * (Math.round(distance / 10));
        }
        return NavigationFormatter.getFormattedDistance(distance, (NavigationApplication) getApplicationContext());
    }

    int getDrawableResId(int maneuverId) {
        return getResources().getIdentifier("ic_step_" + maneuverId, "drawable", getActivity().getPackageName());
    }

    private void getMapDataToDevice(AdviseInfo adviseInfo) {

      /*  int color = getCongestionPercentage(MapplsNavigationHelper.getInstance().getCurrentRoute().legs().get(0).annotation().congestion(),
                MapplsNavigationHelper.getInstance().getNodeIndex());*/
        text_view_reach_eta.setTextColor(ContextCompat.getColor(
                getContext(),
                R.color.white
        ));
        text_view_reach_eta.setText("ETA - " + adviseInfo.getEta());
        text_view_total_distance_left.setText("DTG - " + NavigationFormatter.getFormattedDistance(adviseInfo.getLeftDistance(), (SuzukiApplication) getApplicationContext()));
//            text_view_total_time_left.setText(adviseInfo.getLeftTime());

        getManeuverLookUp(adviseInfo);
//            Log.d("advdvvd", "info--" + adviseInfo.getInfo());
        otherInfoTextView.setVisibility(GONE);


        String sgsgs = NavigationFormatter.getFormattedDistance(adviseInfo.getLeftDistance(), (SuzukiApplication) getApplicationContext());


        if (sgsgs.contains("km")) {
            dataRemainingDistanceUnit = "K";
        } else if (sgsgs.contains("m")) {
            dataRemainingDistanceUnit = "M";

        }


        String remdeist;
        remdeist = sgsgs.replaceAll(" ", "");
        remdeist = remdeist.replaceAll("[a-z]", "");

        float Rem_dsit = Float.parseFloat(remdeist);
        if (dataRemainingDistanceUnit.equalsIgnoreCase("K"))
            remdeist = "" + Rem_dsit;//now suzuki doesn't send into float value so convert this (1 -> 1.0)
        int Rem_dsit_int = 0;

        if ((Rem_dsit < 10) && (dataRemainingDistanceUnit == "K")) {
            if (remdeist.length() == 3) {
                remdeist = remdeist + "0";
            }
            remdeist = ("00000" + remdeist).substring(remdeist.length());
            remdeist = remdeist.substring(0, 4);
            remdeist = ("0000" + remdeist).substring(remdeist.length());
        }
        if ((Rem_dsit >= 100) && (dataRemainingDistanceUnit == "K")) {
            Rem_dsit_int = (int) Math.round(Rem_dsit);

            remdeist = Integer.toString(Rem_dsit_int);
        }


        if (remdeist.length() <= 5) {
            if (remdeist.contains(".")) {
                remdeist = ("00000" + remdeist).substring(remdeist.length());
                remdeist = remdeist.substring(1, 5);
                remdeist = ("0000" + remdeist).substring(remdeist.length());
            } else {
                remdeist = ("00000" + remdeist).substring(remdeist.length());
                remdeist = remdeist.substring(1, 5);
                remdeist = ("0000" + remdeist).substring(remdeist.length());
            }
            dataRemainingDistance = remdeist;
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hhmma");


        boolean is24hr = android.text.format.DateFormat.is24HourFormat(getContext());

        if (is24hr) {


            try {
                DateFormat dateFormat = new SimpleDateFormat("HH:mm");
                Date d = dateFormat.parse(adviseInfo.getEta());

                String mydat = simpleDateFormat.format(d);

                String etaTime = mydat;
                dataEta = etaTime.replaceAll(" ", "");
                dataEta = dataEta.replaceAll(":", "");
                dataEta = dataEta.replaceAll("pm", "PM");
                dataEta = dataEta.replaceAll("am", "AM");
                dataEta = dataEta.replaceAll(" ", " ");

                dataEta = dataEta.replaceAll(" ","");

                if (dataEta.length() <= 6) {

                    dataEta = ("000000" + dataEta).substring(dataEta.length());
                } else {
                    dataEta.substring(0, 6);
//                dataEta = ("000000" + dataEta).substring(dataEta.length());
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

        } else {
            try {
                DateFormat dateFormat = new SimpleDateFormat("HH:mm a");
//                DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy hh:mm a");

              /*  Date d = dateFormat.parse(adviseInfo.getEta());
                String mydat = simpleDateFormat.format(d);*/


                String etaTime = adviseInfo.getEta();
              //  String etaTime = "6:46 pm";

                dataEta = etaTime.replaceAll(" ", "");
                dataEta = dataEta.replaceAll(":", "");
                dataEta = dataEta.replaceAll("pm", "PM");
                dataEta = dataEta.replaceAll("am", "AM");
                dataEta = dataEta.replaceAll(" ", " ");

                dataEta = dataEta.replaceAll(" ","");



                if (dataEta.length() <= 6) {

                    dataEta = ("000000" + dataEta).substring(dataEta.length());
                } else {
                    dataEta.substring(0, 6);
//                dataEta = ("000000" + dataEta).substring(dataEta.length());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
//        Log.d("skjskkswiw", "ssskk---" + maneuverID + dataShortDistance + dataShortDistanceUnit + dataEta + dataRemainingDistance + dataRemainingDistanceUnit);


        // updateDisplay(maneuverID, myshortetrs, dataShortDistanceUnit, dataEta, dataRemainingDistance, dataRemainingDistanceUnit, "1", "1");
    }

    private int getManeuverLookUp(AdviseInfo adviseInfo) {

        if (adviseInfo.getManeuverID() == 0) {
            maneuverID = 1;

        } else if (adviseInfo.getManeuverID() == 1) {

            maneuverID = 2;

        } else if (adviseInfo.getManeuverID() == 2) {
            maneuverID = 3;

        } else if (adviseInfo.getManeuverID() == 3) {
            maneuverID = 4;

        } else if (adviseInfo.getManeuverID() == 4) {
            maneuverID = 5;
        } else if (adviseInfo.getManeuverID() == 5) {
            maneuverID = 6;

        } else if (adviseInfo.getManeuverID() == 6) {
            maneuverID = 7;

        } else if (adviseInfo.getManeuverID() == 7) {
            maneuverID = 8;

        } else if (adviseInfo.getManeuverID() == 8) {
            maneuverID = 9;

        } else if (adviseInfo.getManeuverID() == 75) {
            maneuverID = 10;

        } else if (adviseInfo.getManeuverID() == 11) {
            maneuverID = 11;

        } else if (adviseInfo.getManeuverID() == 12) {
            maneuverID = 12;

        } else if (adviseInfo.getManeuverID() == 13) {
            maneuverID = 13;

        } else if (adviseInfo.getManeuverID() == 14) {
            maneuverID = 14;

        } else if (adviseInfo.getManeuverID() == 53) {
            maneuverID = 15;

        } else if (adviseInfo.getManeuverID() == 54) {
            maneuverID = 16;

        } else if (adviseInfo.getManeuverID() == 55) {
            maneuverID = 17;

        } else if (adviseInfo.getManeuverID() == 56) {
            maneuverID = 18;

        } else if (adviseInfo.getManeuverID() == 57) {
            maneuverID = 19;

        } else if (adviseInfo.getManeuverID() == 65) {
            maneuverID = 20;

        } else if (adviseInfo.getManeuverID() == 66) {
            maneuverID = 21;

        } else if (adviseInfo.getManeuverID() == 67) {
            maneuverID = 22;

        } else if (adviseInfo.getManeuverID() == 68) {
            maneuverID = 23;

        } else if (adviseInfo.getManeuverID() == 69) {
            maneuverID = 24;

        } else if (adviseInfo.getManeuverID() == 70) {
            maneuverID = 25;

        } else if (adviseInfo.getManeuverID() == 71) {
            maneuverID = 26;

        } else if (adviseInfo.getManeuverID() == 19) {
            maneuverID = 27;

        } else if (adviseInfo.getManeuverID() == 20) {
            maneuverID = 28;

        } else if (adviseInfo.getManeuverID() == 17) {
            maneuverID = 29;

        } else if (adviseInfo.getManeuverID() == 18) {
            maneuverID = 30;

        } else if (adviseInfo.getManeuverID() == 15) {
            maneuverID = 31;

        } else if (adviseInfo.getManeuverID() == 16) {
            maneuverID = 32;

        } else if (adviseInfo.getManeuverID() == 21) {
            maneuverID = 33;

        } else if (adviseInfo.getManeuverID() == 22) {
            maneuverID = 34;

        } else if (adviseInfo.getManeuverID() == 23) {
            maneuverID = 35;

        } else if (adviseInfo.getManeuverID() == 24) {
            maneuverID = 36;

        } else if (adviseInfo.getManeuverID() == 25) {
            maneuverID = 37;

        } else if (adviseInfo.getManeuverID() == 73) {
            maneuverID = 38;

        } else if (adviseInfo.getManeuverID() == 41) {
            maneuverID = 39;

        } else if (adviseInfo.getManeuverID() == 50) {
            maneuverID = 40;

        } else if (adviseInfo.getManeuverID() == 51) {
            maneuverID = 41;

        } else if (adviseInfo.getManeuverID() == 52) {
            maneuverID = 42;

        } else if (adviseInfo.getManeuverID() == 36) {
            maneuverID = 43;

        } else if (adviseInfo.getManeuverID() == 74) {
            maneuverID = 44;

        } else if (adviseInfo.getManeuverID() == 72) {
            maneuverID = 45;

        }
        return maneuverID;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void setNavigationPadding(boolean navigation) {
        if (mapmyIndiaMap == null)
            return;

        if (navigation) {

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                mapmyIndiaMap.setPadding(
                        0, 750, 0, 0
                );
            } else {
                mapmyIndiaMap.setPadding(
                        0, 250, 0, 0
                );
            }
        } else {
            mapmyIndiaMap.setPadding(
                    0, 0, 0, 0
            );
        }
    }
// MMI changes commented the setprogesschangelistener
//    @Override
//    public void setProgressChangeListener(@Nullable ProgressChangeListener progressChangeListener) {
//        this.progressChangeListener = progressChangeListener;
//    }

    private void initViews(View view) {
        initAnimations();
        soundChipText = view.findViewById(R.id.sound_text);
        soundFab = view.findViewById(R.id.sound_btn);
        soundFab.setOnClickListener(this);
        //main_layout=view.findViewById(R.id.main_layout);
        //  main_layout.getForeground().setAlpha( 0);

        mFollowMeButton = view.findViewById(R.id.follow_button);
        mFollowMeButton.setOnClickListener(this);

        view.findViewById(R.id.reset_bounds_button).setOnClickListener(this);

        junctionViewImageView = view.findViewById(R.id.junction_view_image_view);

        warningTextView = view.findViewById(R.id.warning_text_view);
        llRedAlertBle = view.findViewById(R.id.llRedAlertBle);
        ivCustomClose = view.findViewById(R.id.ivCustomClose);
        tvCustomTextBtn=view.findViewById(R.id.tvCustomTextBtn);
        otherInfoTextView = view.findViewById(R.id.other_info_text_view);
        ImageButton imageViewLeft = view.findViewById(R.id.navigation_strip_left_image_button);
        ImageButton imageViewRight = view.findViewById(R.id.navigation_strip_right_image_button);
        navigationStripViewPager = view.findViewById(R.id.navigation_info_layout_new);
        top_strip_layout= view.findViewById(R.id.top_strip_layout);
        options_recycler_view_container= view.findViewById(R.id.options_recycler_view_container);
        nextInstructionImageView = view.findViewById(R.id.next_instruction_image_view);
        nextInstructionContainer = view.findViewById(R.id.next_advise_container);
        settingFloatingActionButton = view.findViewById(R.id.settings_button);
        settingFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MapplsNavigationHelper.getInstance().getCurrentRoute() != null) {
                    if (MapplsNavigationHelper.getInstance().getCurrentRoute().routeClasses() != null) {
                        //openClassesDetailDialog();
                    } else {
                        Toast.makeText(requireContext(), "This route does not contain any Classes", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "Please wait No Route Found", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //custome
        // cutsommmmm
        TextView recenter_text_view = view.findViewById(R.id.recenter_text_view);


        imageViewLeft.setOnClickListener(v -> nextPreviousButtonPressed(true));
        imageViewRight.setOnClickListener(v -> nextPreviousButtonPressed(false));

        mFollowMeButton.setOnClickListener(v -> {
            navigationStripViewPager.setCurrentItem(currentPageLocation);
            nextInstructionContainer.setVisibility(View.VISIBLE);

            followMe(true);
            setNavigationPadding(true);
        });


        View mBottomSheetContainer = view.findViewById(R.id.options_recycler_view_container);

        text_view_reach_eta = view.findViewById(R.id.text_view_reach_eta);
        text_view_total_distance_left = view.findViewById(R.id.text_view_total_distance_left);
        TextView text_view_total_time_left = view.findViewById(R.id.text_view_total_time_left);
        mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheetContainer);
        mBottomSheetBehavior.setHideable(false);
        ((LockableBottomSheetBehavior) mBottomSheetBehavior).setLocked(true);
    }

//    private void openClassesDetailDialog() {
//        FragmentManager fragmentManager = (requireActivity()).getSupportFragmentManager();
//        if (fragmentManager.findFragmentByTag(ClassesDetailDialogFragment.class.getSimpleName()) == null) {
//            ClassesDetailDialogFragment classesDetailDialogFragment = new ClassesDetailDialogFragment();
//            classesDetailDialogFragment.show(fragmentManager, ClassesDetailDialogFragment.class.getSimpleName());
//        }
//    }

    private void setAdapter() {
        if (getActivity() == null)
            return;
        List<NavigationStep> adviseArrayList = app.getRouteDirections();


        Stop stop = new Stop();
        stop.setName("END STOP");

        NavigationPagerAdapter adapter = new NavigationPagerAdapter(getActivity(), adviseArrayList, stop);
        navigationStripViewPager.setAdapter(adapter);
        adapter.setSelectedPosition(0);
        navigationStripViewPager.setCurrentItem(1);


    }

    private void nextPreviousButtonPressed(boolean isLeft) {
        nextInstructionContainer.setVisibility(GONE);
        if (camera != null)
            // camera.updateCameraTrackingLocation(false); // MMI changes
            camera.updateCameraTrackingMode(NavigationCamera.NAVIGATION_TRACKING_MODE_NONE); // MMI changes


        if (mFollowMeButton.getVisibility() != View.VISIBLE)
            mFollowMeButton.setVisibility(View.VISIBLE);

        if (isLeft) {

            if (navigationStripViewPager.getCurrentItem() > currentPageLocation) {
                navigationStripViewPager.setCurrentItem((navigationStripViewPager.getCurrentItem()) - 1);
                if (bearingIconPlugin != null)
                    bearingIconPlugin.setBearingLayerVisibility(true);
                setNavigationPadding(false);
                fixPreviewNavigationMarker();
            }

        } else {
            List<NavigationStep> adviseArrayList = app.getRouteDirections();
            if (navigationStripViewPager.getCurrentItem() < adviseArrayList.size()) {
                navigationStripViewPager.setCurrentItem((navigationStripViewPager.getCurrentItem()) + 1);
                if (bearingIconPlugin != null)
                    bearingIconPlugin.setBearingLayerVisibility(true);
                setNavigationPadding(false);
                fixPreviewNavigationMarker();
            }
        }

    }

    private void fixPreviewNavigationMarker() {
        try {
            if (mapmyIndiaMap != null) {
                if (camera != null)
                    //  camera.updateCameraTrackingLocation(false); // MMI changes
                    camera.updateCameraTrackingMode(NavigationCamera.NAVIGATION_TRACKING_MODE_NONE); // MMI changes

            }
            List<NavigationStep> adviseArrayList = app.getRouteDirections();
            NavigationStep currentRouteDirectionInfo = adviseArrayList.get(navigationStripViewPager.getCurrentItem());

            NavLocation location = currentRouteDirectionInfo.getNavLocation();

            if (mapmyIndiaMap != null && location != null) {
                LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
                CameraPosition.Builder builder = new CameraPosition.Builder()
                        .target(position)
                        .zoom(mapmyIndiaMap.getMaxZoomLevel())
                        .tilt(0);
                mapmyIndiaMap.moveCamera(CameraUpdateFactory.newCameraPosition(builder.build()));


                NavLocation location1 = adviseArrayList.get(navigationStripViewPager.getCurrentItem()).getNavLocation();
                if (location1 != null) {
                    float angle = getLocationAngle(location1);
                    if (bearingIconPlugin != null) {
                        bearingIconPlugin.setBearingIcon(angle,
                                new LatLng(location1.getLatitude(), location1.getLongitude()));
                    }
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void setProgressChangeListener(@Nullable com.mappls.sdk.navigation.camera.ProgressChangeListener progressChangeListener) {

    }

    @Override
    public void removeProgressChangeListener(@Nullable com.mappls.sdk.navigation.camera.ProgressChangeListener progressChangeListener) {

    }

    class MyLocalBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mFragmentTransactionSave && getActivity() != null)
                getActivity().onBackPressed();
        }
    }

    private byte[] GetNavigationPkt(int directionSymbol, String shortDistance, String shortDistanceUnit, String eta, String remaingDistance, String remainingDistanceUnit, String navigationStatus, String navigationModeStatus) {
        String PhoneData;

        byte[] RetArray = new byte[30];

//        Log.d("TAG", "GetNavigationPkt() called with: directionSymbol = [" + directionSymbol + "], shortDistance = [" + shortDistance + "], shortDistanceUnit = [" + shortDistanceUnit + "], eta = [" + eta + "], remaingDistance = [" + remaingDistance + "], remainingDistanceUnit = [" + remainingDistanceUnit + "], navigationStatus = [" + navigationStatus + "], navigationModeStatus = [" + navigationModeStatus + "]");
        try {
            PhoneData = "?"/*Start of Frame*/ + "1"/*Frame ID*/ + "1" + "0" + dataShortDistance + dataShortDistanceUnit + dataEta + "00" + "0" + dataRemainingDistance + dataRemainingDistanceUnit + navigationStatus + navigationModeStatus + "00000";
            Log.e("status_check",String.valueOf(navigationStatus));

            if(!navigationStatus.equals("1") && !navigationStatus.equals("3") && !navigationStatus.equals("5")){
                if(temp==0) temp=maneuverID;
                maneuverID=0x2E;
            } else if(temp!=0){
                maneuverID = temp;
                temp=0;
            }

            try {
                RetArray = PhoneData.getBytes("UTF-8");
                RetArray[0] = (byte) 0xA5;
                RetArray[2] = (byte) maneuverID;
                RetArray[3] = (byte) 0xFF;
                RetArray[15] = (byte) 0xFF;
                RetArray[16] = (byte) 0xFF;
                RetArray[17] = (byte) 0xFF;

                for (int k = 25; k <= 27; k++) {
                    RetArray[k] = (byte) 0xFF;
                }
                RetArray[28] = calculateCheckSum(RetArray);

                RetArray[29] = (byte) 0x7F;

                return RetArray;
            } catch (java.io.IOException e) {
                return RetArray;
            }
        } catch (Exception e) {
            return RetArray;
        }
    }

    private void sendNavigationData(int directionSymbol, String shortdistance, String shortDistanceUnit, String eta, String remainingDist, String remainigDistanceUnit, String navigationStatus, String navigationModeStatus) {

        if (mBoundService != null && BLE_CONNECTED ) {

            if (!initialSendingDone) {
                sendUserInfoPacket();
                if (sendCount > 3) {
                    initialSendingDone = true;
                    sendCount = 0;
                }
            }

            if(Settings.System.getInt(HOME_SCREEN_OBJ.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0)==1) navigationStatus="0";

            LocationManager lm = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
            try {
                if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) navigationStatus="4";
            } catch(Exception ex) {
                Log.e(EXCEPTION,"sendNavigationData: "+String.valueOf(ex));
            }

            if(initialSendingDone) {

                byte[] PKT = GetNavigationPkt(maneuverID, dataShortDistance, shortDistanceUnit, eta, remainingDist, remainigDistanceUnit, navigationStatus, navigationModeStatus);
                if (mBoundService != null) mBoundService.writeDataFromAPPtoDevice(PKT, 31);

                //String Str = new String(PKT);
            }
            //if(BLE_CONNECTED) {
                /*byte[] PKT_Userinfo = getSmartPhoneUSERINFOPKT(getApplicationContext().getSharedPreferences("user_data", MODE_PRIVATE).getString("name", ""));
                if (mBoundService != null&& sendCount <3){
                    mBoundService.writeDataFromAPPtoDevice(PKT_Userinfo, 36);
                    sendCount++;
                }*/
            //  sendUserInfoPacket();


        }
        else {
            sendCount = 0;
            initialSendingDone = false;
        }
    }

    private byte[] getSmartPhoneUSERINFOPKT(String data) {
        String PhoneData;
        byte[] RetArray = new byte[30];
        try {
            if (data.length() <= 22) {

                for (int i = data.length(); i <= 22; i++) {
                    data += "\0";
                }
                PhoneData = "?"/*Start of Frame*/ + "6"/*Frame ID*/ + data/*Data to icd*/ /* */ + "\0\0\0\0\0"/*Fill 0s at reserved byte positions*/ /*End of Frame*/;

            } else {

                data = "\0";
                PhoneData = "?"/*Start of Frame*/ + "6"/*Frame ID*/ + data/*Phone data to icd*/ + "\0\0\0\0\0"/*Fill 0s at reserved byte positions*/ /*End of Frame*/;
            }

            RetArray = PhoneData.getBytes(StandardCharsets.UTF_8);
            RetArray[0] = (byte) 0xA5;


            for (int k = 22; k <= 26; k++) {
                RetArray[k] = (byte) 0xFF;
            }

            //  Log.e("value_checl",String.valueOf(value));
            /*  if( value == 500){
                //First time user connects
                RetArray[27] = (byte) 0x46;
            }
            else
            {
                //Second time user connects
                RetArray[27] = (byte) 0x52;
            }*/

//            RetArray[27] = (byte) 0x46;

//            Realm realm = Realm.getDefaultInstance();
//            RiderProfileModule riderProfile = realm.where(RiderProfileModule.class).equalTo("id", 1).findFirst();

//            SharedPreferences sharedPreferences = HOME_SCREEN_OBJ.getSharedPreferences("BLE_DEVICE", Context.MODE_PRIVATE);
            if(FIRST_TIME){
                RetArray[27] = (byte) 0x46;
            } else RetArray[27] = (byte) 0x52;

            RetArray[28] = calculateCheckSum(RetArray);

            RetArray[29] = (byte) 0x7F;

            calculateCheckSum(RetArray);

        } catch(Exception ignored){ }

        return RetArray;
    }

    private BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {

            NetworkInfo currentNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            NoNetwork = !currentNetworkInfo.isConnected();
        }
    };

    public void logNavigationPacket(byte[] PKT) {
        String hexString = bytesToHex(PKT);
       /* Logger.log(getContext(),"Navigation Packet: " + hexString);
        Log.e("Navigation_data",hexString);*/
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02X ", b));
        }
        return hex.toString().trim();
    }



    private final Runnable updateDisplay = new Runnable() {
        @Override
        public void run() {
            /*if(counter==30) DELAY=1200;
            if(counter==32){
                DELAY=300;
                counter=0;
            }*/
            NavigationStatus();

            sendNavigationData(maneuverID, dataShortDistance, dataShortDistanceUnit, dataEta, dataRemainingDistance, dataRemainingDistanceUnit, NavigationStatus, navigationModeEnabled);
            if (routeProgress && ViaPoints) new Handler().postDelayed(() -> ViaPoints = routeProgress = false, 500);
            handler.postDelayed(this, 500);
            counter++;

            if(navigationModeEnabled.equals("0")) getActivity().finish();
        }
    };

    private void NavigationStatus() {
        if (NoSignal) NavigationStatus = "0";
        else if (NoNetwork) NavigationStatus = "6";
       else if (SearchingGPS ) NavigationStatus = "4";
        else if (ReRouting) {
            new Handler().postDelayed(() -> ReRouting = false, 0);
            ReRouting = false;
            NavigationStatus = "2";
        } else if (DestinationReached) NavigationStatus = "5";
        else if (ViaPoints) NavigationStatus = "3";
        else NavigationStatus = "1";
    }

    public String getTime(Context context, long time) {
        if (android.text.format.DateFormat.is24HourFormat(context)) {
            return new SimpleDateFormat("HH:mm", Locale.US).format(new Date(time));

        } else {
            return new SimpleDateFormat("hh:mm a", Locale.US).format(new Date(time));
        }
    }

}
