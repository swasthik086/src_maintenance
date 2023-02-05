package com.suzuki.fragment;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static com.mappls.sdk.maps.Mappls.getApplicationContext;
import static com.suzuki.R.layout.dashboard_fragment_constraint_layout;
import static com.suzuki.activity.DeviceListingScanActivity.DEVICESCAN_OBJ;
import static com.suzuki.activity.HomeScreenActivity.CALL_CLEAR;
import static com.suzuki.activity.HomeScreenActivity.HOME_SCREEN_OBJ;
import static com.suzuki.activity.HomeScreenActivity.MSG_CLEAR;
import static com.suzuki.activity.HomeScreenActivity.mBoundService;
import static com.suzuki.activity.RiderProfileActivity.decodeBase64;
import static com.suzuki.application.SuzukiApplication.bleDevice;
import static com.suzuki.application.SuzukiApplication.calculateCheckSum;
import static com.suzuki.application.SuzukiApplication.isRegionFixed;
import static com.suzuki.broadcaster.BluetoothCheck.BLUETOOTH_STATE;
import static com.suzuki.services.MyBleService.connectionStarted;
import static com.suzuki.utils.Common.BikeBleName;
import static com.suzuki.utils.Common.EXCEPTION;
import static com.suzuki.utils.Common.FIRST_TIME;
import static com.suzuki.utils.Common.PRICOL_CONNECTED;
import static com.suzuki.utils.Common.STATUS_PACKET_DELAY;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;

import com.clj.fastble.BleManager;
import com.cunoraz.gifview.library.GifView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.suzuki.R;
import com.suzuki.activity.ConnectedDataActivity;
import com.suzuki.activity.DeviceListingScanActivity;
import com.suzuki.activity.ProfileActivity;
import com.suzuki.activity.ZoomProfileImageActivity;
import com.suzuki.adapter.BleListingDeviceAdapter;
import com.suzuki.application.SuzukiApplication;
import com.suzuki.base.BaseFragment;
import com.suzuki.broadcaster.BleConnection;
import com.suzuki.pojo.BleDataPojo;
import com.suzuki.pojo.ClusterStatusPktPojo;
import com.suzuki.pojo.EvenConnectionPojo;
import com.suzuki.pojo.LastParkedLocationRealmModule;
import com.suzuki.pojo.RiderProfileModule;
import com.suzuki.pojo.SettingsPojo;
import com.suzuki.services.MyBleService;
import com.suzuki.utils.Common;
import com.suzuki.utils.CurrentLoc;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import io.realm.Realm;
import io.realm.RealmResults;

public class DashboardFragment extends BaseFragment implements View.OnClickListener {

    String Fuel;


    ImageView overlapImage, ivUserBike, ivFuelMeter;
    static TextView tvBleName, ProfileName;
    TextView tvOdometer, rideCount, tvTripB, tvTripA;
    public static ConstraintLayout rlButtonConnect, rlButtonWhiePair;

    TelephonyManager Tel;

    MyPhoneStateListener MyListener;

    SharedPreferences myPrefrence;
    RealmResults<LastParkedLocationRealmModule> favTrip;

    static boolean synccc = true;
    Boolean status;
    public static boolean BLE_CONNECTED = false, staticConnectionStatus = false, staticSendData = false, onDisconnect = false, displayingDisconnectDialog = false;
    private static final int REQUEST_CODE_OPEN_GPS = 1, REQUEST_CODE_PERMISSION_LOCATION = 2;
    int deviceStatus;
    static int speedAlert;
    public static final String MyPREFERENCES = "MyPreferences", key = "USER_IMAGE";
    public static String userName, serviceID, writeCharacterID, readCharacterID, prev_cluster_name, prev_cluster_macAddr;//, BikeBleName="";

    DashboardFragment dashboardFragment;
    public static SuzukiApplication app;

    private BleConnection mReceiver;
    public static boolean userStatus = false, SignalNavigation = false, NoSignal = false;

    IntentFilter intentfilter;
    String currentBatteryStatus = "1N", signal = "1";
    SimpleDateFormat simpleDateFormat;
    public int FuelLevel = 5;
    String finaltime, speedAlertForDashboard, Odometer, TripA, TripB;
    static Realm realm;
    RealmResults<SettingsPojo> settingsPojos;
    RealmResults<BleDataPojo> bleDataPojoRealmResults;
    public static BluetoothAdapter bluetoothadapter;

    public static Timer timer;
    TextView tvKM, tvTripAKM, tvTripBKM;
    static SimpleDateFormat dateFormat;
    public static boolean showBluetoothDialog = false;
    private static GifView fuelGif;
//    private Common common;

    private FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
    private int msg_clear_counter_no = 1, call_clear_counter_no = 1, value;

    private View view;
    private long prev=0;
    private Snackbar snackbar;
    private boolean ALREADY_DISPLAYED=false;
    private BluetoothAdapter btAdapter;
    private static SharedPreferences sharedPreferences;
    public static boolean flag=true;
    private static boolean Scooter=true;
    private static boolean wrong_fuel_data_discarded=false;
    //    private static BleManager bleManager;
    private BleListingDeviceAdapter mDeviceAdapter;
    private boolean in_prog;
    private static String ClassName;
    private String type, model;
    private int variant;
    private SharedPreferences.Editor editor;
//    private static MutableLiveData<Boolean> check;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(dashboard_fragment_constraint_layout, container, false);

        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss_SSS");
        realm = Realm.getDefaultInstance();
        bluetoothadapter = BluetoothAdapter.getDefaultAdapter();
        BleManager.getInstance().init(getActivity().getApplication());
        dashboardFragment = DashboardFragment.this;
        Typeface typefaceRobotoRegular = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Regular.ttf");

        if (bluetoothadapter != null) BLUETOOTH_STATE = true;
        else BLUETOOTH_STATE = false;

        app = getMyApplication();
//        layoutTop = (RelativeLayout) view.findViewById(R.id.layoutTop);

        rlButtonConnect = view.findViewById(R.id.rlButtonConnect);
        rlButtonWhiePair = view.findViewById(R.id.rlButtonWhiePair);
//        layoutTop = (RelativeLayout) view.findViewById(R.id.layoutTop);
        tvBleName = (TextView) view.findViewById(R.id.tvBleName);
        ProfileName = (TextView) view.findViewById(R.id.ProfileName);
        tvOdometer = (TextView) view.findViewById(R.id.tvOdometer);
        rideCount = (TextView) view.findViewById(R.id.rideCount);
        tvTripB = (TextView) view.findViewById(R.id.tvTripB);
        tvTripA = (TextView) view.findViewById(R.id.tvTripA);
        tvBleName.setTypeface(typefaceRobotoRegular);
        overlapImage = (ImageView) view.findViewById(R.id.overlapImage);
        ivUserBike = (ImageView) view.findViewById(R.id.ivUserBike);
        ivFuelMeter = (ImageView) view.findViewById(R.id.ivFuelMeter);
        ProfileName.setTypeface(typefaceRobotoRegular);
        tvKM = (TextView) view.findViewById(R.id.tvKm);
        tvTripAKM = (TextView) view.findViewById(R.id.tvTripAKm);
        // tvTripBKM = (TextView) view.findViewById(R.id.tvTripBKm);
        //LinearLayout rlfuel = (LinearLayout) view.findViewById(R.id.rlfuel);
        //RelativeLayout rltripa = (RelativeLayout) view.findViewById(R.id.rltripa);
        //RelativeLayout rlodo = (RelativeLayout) view.findViewById(R.id.rlodo);
        //fuelGif = view.findViewById(R.id.fuel_gif);

        ProfileName.setText(getContext().getSharedPreferences("user_data", MODE_PRIVATE).getString("name", ""));

        fuelGif = (GifView) view.findViewById(R.id.fuel_gif);

        fuelGif.setVisibility(View.VISIBLE);
        fuelGif.play();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        HOME_SCREEN_OBJ.registerReceiver(check_ble_conn, filter);

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowmanager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        windowmanager.getDefaultDisplay().getMetrics(displayMetrics);

        settingsPojos = realm.where(SettingsPojo.class).findAll();
        bleDataPojoRealmResults = realm.where(BleDataPojo.class).findAll();

        simpleDateFormat = new SimpleDateFormat("hhmmss");

        for (SettingsPojo settingsPojo : settingsPojos) {

            speedAlert = settingsPojo.getSpeedAlert();
            synccc = settingsPojo.isSpeedSet();
        }

        settingsPojos.addChangeListener(settingsPojos -> {

            for (int i = 0; i < settingsPojos.size(); i++) {
                speedAlert = settingsPojos.get(0).getSpeedAlert();
                synccc = settingsPojos.get(0).isSpeedSet();
            }
        });

        rlButtonWhiePair.setOnClickListener(v -> {

            if(!check_permission()) showExitAlert("The application needs location access permission to scan for nearby Suzuki 2 Wheelers.\n" + "Please allow location access permission to discover and connect with your Suzuki 2 Wheeler.");
            else if(checkGPSIsOpen()) startActivity(new Intent(getActivity(), DeviceListingScanActivity.class));
            else showExitAlert("Please enable location settings on the next screen to discover and connect with your Suzuki 2 Wheeler.");
        });

        rlButtonConnect.setOnClickListener(v -> {

            boolean isConnected = BleManager.getInstance().isConnected(bleDevice);

            if (isConnected) startActivity(new Intent(getActivity(), ConnectedDataActivity.class));
            else startActivity(new Intent(getActivity(), DeviceListingScanActivity.class));
        });

        rlButtonConnect.setOnLongClickListener(view -> {
            startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
            return false;
        });

        getActivity().registerReceiver(broadcastreceiver, intentfilter);

        MyListener = new MyPhoneStateListener();
        Tel = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);

        Tel.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);


        /*realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                BleDataPojo bleDataPojo = realm.where(BleDataPojo.class).equalTo("bleId", 1).findFirst();

                if (bleDataPojo == null) {

                } else if (bleDataPojo != null) {

                    ("macdadres", "-" + bleDataPojo.getDeviceMacAddress() + bleDataPojo.getDeviceName());

                    //bleName = bleDataPojo.getDeviceName();
                    //tvBleName.setText(BikeBleName);
                    //Log.e("blecheck","three");
//                    service_init();
                    //Toast.makeText(getContext(), bleName, Toast.LENGTH_SHORT).show();
                }

            }
        });*/

        realm.addChangeListener(realm -> {

            //temp deactivated
            //update_dashboard(realm);
        });

        /*bleDataPojoRealmResults.addChangeListener(bleDataPojos -> {
            if (bleDataPojos!=null){
                if(bleDataPojos.size() > 1) bleName = bleDataPojos.get(1).getDeviceName();
            }
        });*/

        boolean isConnected = BleManager.getInstance().isConnected(bleDevice);

        if (BLUETOOTH_STATE) {
            if (isConnected && staticConnectionStatus) {

                try{
                    DEVICESCAN_OBJ.finish();
                }catch (Exception ignored){}
            } else {
                connectionStarted = false;

                update_button(false);

                reset_db();
            }
        } else {
            connectionStarted = false;
            update_button(false);

            reset_db();
        }

        favTrip = realm.where(LastParkedLocationRealmModule.class).findAll();

        favTrip.addChangeListener(tripRealmModules -> realm.executeTransaction(realm -> favTrip = realm.where(LastParkedLocationRealmModule.class).findAll()));

        myPrefrence = getActivity().getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);

        String photo = myPrefrence.getString(key, "photo");

        if (!photo.equals("photo")) {
            Bitmap bitmap = decodeBase64(photo);
            overlapImage.setImageBitmap(bitmap);
        }

        overlapImage.setOnClickListener(v -> startActivity(new Intent(getActivity(), ZoomProfileImageActivity.class)));

        sharedPreferences= getApplicationContext().getSharedPreferences("DASHBOARD", MODE_PRIVATE);

        ClassName = getClass().getName();




        BikeBleName.observe(getActivity(), new Observer<String>() {
            @Override
            public void onChanged(String s) {

                Log.e("call_check", "called");
                DashboardFragment.this.update_dashboard_new();
            }
        });

        sharedPreferences = getApplicationContext().getSharedPreferences("vehicle_data",MODE_PRIVATE);
        FuelLevel = sharedPreferences.getInt("fuel_level",0);

        viewRecord();
//        showExitAlert(getString(R.string.request));

        return view;
    }

    /*public void update_dashboard() {

        get_vehicle_type();
        update_fuel_ui();

        try {
            if(new Common(getContext()).is_vehicle_type_changed()){
                if(Scooter){
                    fuelGif.setGifResource(R.drawable.fuel_gif);
//                    checkBikeData(0,"Avenis");
                } else{
                    fuelGif.setGifResource(R.drawable.fuel_gif_six);
                    //checkBikeData(0,"V-STORM SX");
                }

                if(!ALREADY_DISPLAYED && dashboardFragment.isVisible()) change_color_popup();

                //update_vehicle_inDB();
            }

            update_button(true);

        } catch (Exception e) {
            Log.e(EXCEPTION,ClassName+" update_dashboard: "+e);
        }
    }*/

    /*private static boolean get_vehicle_type() {

        if(staticConnectionStatus){
            if(BikeBleName.getValue().contains("SBM")) Scooter=false;
            else if(BikeBleName.getValue().contains("SBS")) Scooter=true;
        }else{
            RiderProfileModule riderProfile = realm.where(RiderProfileModule.class).equalTo("id", 1).findFirst();
            if(riderProfile.getBike().equals("Scooter")) Scooter=true;
            else if(riderProfile.getBike().equals("Motorcycle")) Scooter=false;
        }

        return Scooter;
    }*/

    public void change_color_popup(){
        long current=Calendar.getInstance().getTimeInMillis();
        if(current>prev+5000){
            prev=current;
            flag=true;
            ALREADY_DISPLAYED=true;

            new Common(getContext()).show_alert(view, R.string.change_colour, 5000, "CHANGE");
        }
    }

    private final BroadcastReceiver check_ble_conn = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            Log.e("check_action",action);
//            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //Device found
            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                BLE_CONNECTED=true;

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //Done searching
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                BLE_CONNECTED=false;
                ALREADY_DISPLAYED=false;
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                BLE_CONNECTED=false;
                ALREADY_DISPLAYED=false;
                update_button(false);
            }
        }
    };

    private void update_vehicle_inDB(){
        Realm realm = Realm.getDefaultInstance();

        try{
            realm.executeTransaction(realm1 -> {
                RiderProfileModule riderProfile = realm1.where(RiderProfileModule.class).equalTo("id", 1).findFirst();

                if(BikeBleName.getValue().contains("SBS")){
                    riderProfile.setBike("Scooter");
                    riderProfile.setBikeModel("Access 125");
                    Log.e("vehicle_update","scooter");
                }else{
                    riderProfile.setBike("Motorcycle");
                    riderProfile.setBikeModel("V-STROM SX");
                    Log.e("vehicle_update","bike");
                }

                riderProfile.setUserSelectedImage(0);
                realm1.insertOrUpdate(riderProfile);
            });
        }catch (Exception e){
            Log.e(EXCEPTION,ClassName+" update_vehicle_inDB: "+e);
        }

        try{
            realm.executeTransaction(realm12 -> {

                SettingsPojo speed_settings= realm12.where(SettingsPojo.class).equalTo("id", 1).findFirst();

                if(BikeBleName.getValue().contains("SBS")) speed_settings.setSpeedAlert(5);
                else speed_settings.setSpeedAlert(10);

                speed_settings.setSpeedSet(false);

                realm12.insertOrUpdate(speed_settings);
            });
        }catch (Exception e){
            Log.e(EXCEPTION,ClassName+" update_vehicle_inDB: "+e);
        }
    }

    private static void reset_db() {
        try {

            realm.executeTransaction(realm -> {

                BleDataPojo bleDataPojo = realm.where(BleDataPojo.class).equalTo("bleId", 1).findFirst();

                if (bleDataPojo == null) bleDataPojo = realm.createObject(BleDataPojo.class, 1);

                bleDataPojo.setDeviceMacAddress("");
                bleDataPojo.setDeviceName("");
                bleDataPojo.setReadCharacteristic("");
                bleDataPojo.setWriteCharacteristic("");
                bleDataPojo.setServiceID("");
                realm.insertOrUpdate(bleDataPojo);
            });
        } catch (Exception e) {
            Log.e(EXCEPTION,ClassName+" reset_DB: "+e);
        }
    }

    public void showExitAlert(String message) {
        Dialog dialog = new Dialog(getContext(), R.style.custom_dialog);
        dialog.setContentView(R.layout.custom_dialog);

        TextView tvAlertText = dialog.findViewById(R.id.tvAlertText);
        tvAlertText.setText(message);
        ImageView ivCross = dialog.findViewById(R.id.ivCross);
        ivCross.setOnClickListener(v -> dialog.cancel());

        ImageView ivCheck = dialog.findViewById(R.id.ivCheck);

        ivCheck.setOnClickListener(v -> {
            dialog.cancel();

            if(message.contains("settings")) startActivity( new Intent(Settings. ACTION_LOCATION_SOURCE_SETTINGS ));
            else requestPermissions(new String[] {"android.permission.ACCESS_FINE_LOCATION"}, 201);
        });
        dialog.show();
    }

    private boolean check_permission() {
        String permission = Manifest.permission.ACCESS_FINE_LOCATION;
        int res = getContext().checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //checkforBluetoothConnection();

        setRegion();

        /*try {
            configureRemoteConfig();
        } catch (Exception e) {
            e.printStackTrace();
        }*/

//        turnOffDozeMode(getContext());

    }

    private void setRegion() {
        Location currentLoc = new CurrentLoc().getCurrentLoc(getContext());
        if (currentLoc != null && (!isRegionFixed)) {
            try {
                getMyApplication().setRegion(currentLoc.getLatitude(), currentLoc.getLongitude());
            } catch (Exception ignored) {
            }
        }
    }

    /*private void configureRemoteConfig() throws Exception {
        //Setting the Default Map Value with the current version code
        HashMap<String, Object> firebaseDefaultMap = new HashMap<>();
        firebaseDefaultMap.put(VERSION_CODE_KEY, getCurrentVersionCode());
        firebaseDefaultMap.put(UPDATE_URL_KEY, "");
        firebaseDefaultMap.put(FORCE_UPDATE_REQUIRED, false);
        mFirebaseRemoteConfig.setDefaultsAsync(firebaseDefaultMap);

        //Setting Developer Mode enabled to fast retrieve the values
        mFirebaseRemoteConfig.setConfigSettingsAsync(
                new FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(0L)
                        .build())
                .addOnCompleteListener(task -> { //Fetching the values here
                    mFirebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(task1 -> {
                        *//*("TAG", "Fetched value: " + mFirebaseRemoteConfig.getString(VERSION_CODE_KEY)
                        +" force update "+mFirebaseRemoteConfig.getString(FORCE_UPDATE_REQUIRED)
                        +" url:- "+mFirebaseRemoteConfig.getString(UPDATE_URL_KEY));*//*
                        //calling function to check if new version is available or not
                        try {
                            checkForUpdate();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                });
    }*/

    public void viewRecord() {
        sharedPreferences = getApplicationContext().getSharedPreferences("vehicle_data", MODE_PRIVATE);
        type = sharedPreferences.getString("vehicle_type","");
        model = sharedPreferences.getString("vehicle_name","");
        variant = sharedPreferences.getInt("vehicle_model",0);

        Odometer = "000000";

        //  tvKM.setVisibility(View.VISIBLE);
        String result = Odometer.charAt(0) + "." + Odometer.charAt(5);

        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(realm1 -> {

                RiderProfileModule riderProfile = realm1.where(RiderProfileModule.class).equalTo("id", 1).findFirst();

                if (riderProfile == null) userName = "0";
                else if (riderProfile != null) {

                    String ride_count = String.valueOf(riderProfile.getRideCounts());
                    rideCount.setText(ride_count);
                    sharedPreferences = getApplicationContext().getSharedPreferences("vehicle_data",MODE_PRIVATE);
                    editor = sharedPreferences.edit();
                    editor.putString("ride_count", ride_count);
                    editor.apply();

                    String odometer = riderProfile.getOdometer();
                    Odometer = odometer;
                    String tripA = riderProfile.getTripA();
                    TripA = tripA;
                    String tripB = riderProfile.getTripB();
                    TripB = tripB;

                    if (odometer != null) tvOdometer.setText(Integer.parseInt(odometer) + " km");

                    if (tripA != null) tvTripA.setText(tripKMUpdate(tripA) + " km");

                    if (tripB != null) tvTripB.setText(tripKMUpdate(tripB) + " km");

                    //FuelLevel = riderProfile.getFuelBars();

                    if (riderProfile.getName() != null) {

                        //userName = riderProfile.getName();
                        ProfileName.setText(getContext().getSharedPreferences("user_data", MODE_PRIVATE).getString("name", ""));
//                        rideCount.setText(String.valueOf(riderProfile.getRideCounts()));
                        //checkBikeData(riderProfile.getUserSelectedImage(), riderProfile.getBikeModel());

                        String bike_type = riderProfile.getBike();

                        if(bike_type.contains("Motorcycle")) Scooter=false;

                        rideCount.setText(String.valueOf(riderProfile.getRideCounts()));

                    } else userName = "0";
                }

                BleDataPojo bleDataPojo = realm1.where(BleDataPojo.class).equalTo("bleId", 1).findFirst();
                update_fuel_ui();

                if (bleDataPojo != null) {
//                    prev_cluster = bleDataPojo.getDeviceMacAddress();
//                    BikeBleName = bleDataPojo.getDeviceName(); Log.e("bikeble","added dash 692: "+String.valueOf(BikeBleName));

                    serviceID = bleDataPojo.getServiceID();
                  //  Toast.makeText(app, ""+serviceID, Toast.LENGTH_SHORT).show();
                    writeCharacterID = bleDataPojo.getWriteCharacteristic();
                    readCharacterID = bleDataPojo.getReadCharacteristic();
                }
            });
        } catch (Exception e) {
            Log.e(EXCEPTION, ClassName + " viewRecords " + e);

        }
    }

    private void update_fuel_ui() {

        if(FuelLevel > 1) ivFuelMeter.setColorFilter(Color.parseColor("#00c1d9"));
        else ivFuelMeter.setColorFilter(Color.parseColor("#FFC107"));

        /*if(FuelLevel>0) changeVisibility(true);
        else changeVisibility(false);*/

        if(FuelLevel > 0){
            fuelGif.setVisibility(View.GONE);
            ivFuelMeter.setVisibility(View.VISIBLE);
        }else{
            fuelGif.setVisibility(View.VISIBLE);
            ivFuelMeter.setVisibility(View.GONE);
        }

        switch (FuelLevel){
            case 6: ivFuelMeter.setImageResource(R.drawable.fuel_six_b); break;

            case 5:
                if (type.equals("Scooter")) ivFuelMeter.setImageResource(R.drawable.fuel);
                else ivFuelMeter.setImageResource(R.drawable.fuel_five_b);
                break;

            case 4:
                if (type.equals("Scooter")) ivFuelMeter.setImageResource(R.drawable.fuel_four);
                else ivFuelMeter.setImageResource(R.drawable.fuel_four_b);
                break;

            case 3:
                if (type.equals("Scooter")) ivFuelMeter.setImageResource(R.drawable.fuel_three);
                else ivFuelMeter.setImageResource(R.drawable.fuel_three_b);
                break;

            case 2:
                if (type.equals("Scooter")) ivFuelMeter.setImageResource(R.drawable.fuel_two);
                else ivFuelMeter.setImageResource(R.drawable.fuel_two_b);
                break;

            case 1:
                if (type.equals("Scooter")) ivFuelMeter.setImageResource(R.drawable.fuel_one);
                else ivFuelMeter.setImageResource(R.drawable.fuel_one_b);
                break;

            case 0:
                if (type.equals("Scooter")) fuelGif.setGifResource(R.drawable.fuel_gif);
                else fuelGif.setGifResource(R.drawable.fuel_gif_six);

                break;
        }

        /*if (Scooter) {
            if (FuelLevel == 5) ivFuelMeter.setImageResource(R.drawable.fuel);
            else if (FuelLevel == 4) ivFuelMeter.setImageResource(R.drawable.fuel_four);
            else if (FuelLevel == 3) ivFuelMeter.setImageResource(R.drawable.fuel_three);
            else if (FuelLevel == 2) ivFuelMeter.setImageResource(R.drawable.fuel_two);
            else if (FuelLevel == 1) ivFuelMeter.setImageResource(R.drawable.fuel_one);
            else fuelGif.setGifResource(R.drawable.fuel_gif);
            //else if (FuelLevel == 0) ivFuelMeter.setImageResource(R.drawable.fuel_gif);

        } else {
            if (FuelLevel == 6) ivFuelMeter.setImageResource(R.drawable.fuel_six_b);
            else if (FuelLevel == 5) ivFuelMeter.setImageResource(R.drawable.fuel_five_b);
            else if (FuelLevel == 4) ivFuelMeter.setImageResource(R.drawable.fuel_four_b);
            else if (FuelLevel == 3) ivFuelMeter.setImageResource(R.drawable.fuel_three_b);
            else if (FuelLevel == 2) ivFuelMeter.setImageResource(R.drawable.fuel_two_b);
            else if (FuelLevel == 1) ivFuelMeter.setImageResource(R.drawable.fuel_one_b);
            else fuelGif.setGifResource(R.drawable.fuel_gif_six);
            //else if (FuelLevel == 0) ivFuelMeter.setImageResource(R.drawable.fuel_gif_six);
        }*/
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    public void sendTimeSyncData() {
        check_sim();
        if (finaltime != null) {
            if(Settings.System.getInt(HOME_SCREEN_OBJ.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0)==1) signal="0";

            String dataToSend = currentBatteryStatus + speedAlertForDashboard + signal + finaltime;

            checkMsgCounter();
            //checkCallCounter();

            byte[] PKT = GetSmartPhoneStatusPkt(dataToSend);

            for(int i = 2 ; i < 5 ; i++){
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (mBoundService != null) mBoundService.writeDataFromAPPtoDevice(PKT,33);
            }
        }
    }

    private void check_sim() {
        TelephonyManager telMgr = (TelephonyManager) HOME_SCREEN_OBJ.getSystemService(Context.TELEPHONY_SERVICE);
        int simState = telMgr.getSimState();
        switch (simState) {
            case TelephonyManager.SIM_STATE_ABSENT:
                signal = "0";
                SignalNavigation = false;
                NoSignal = true;
                break;
        }
    }

    private void checkMsgCounter() {
        if (MSG_CLEAR == 0x59) {
            if (msg_clear_counter_no > 3) {
                MSG_CLEAR = 0x4E;
                msg_clear_counter_no = 1;
            }
            msg_clear_counter_no++;
        } else msg_clear_counter_no = 1;
    }

    /*private void checkCallCounter() {
        Log.e("statuscheck",String.valueOf(CALL_CLEAR));
        if (CALL_CLEAR == 0x59) {
            Log.e("statuscheck","true");
            if (call_clear_counter_no > 3) {
                CALL_CLEAR = 0x4E;
                Log.e("clear_flag","missed_call_2");
                call_clear_counter_no = 1;
            }
            call_clear_counter_no++;
        } else call_clear_counter_no = 1;
    }*/

    private void updateDisplay(final String userinfo) {

        if (timer != null) {
            try {
                timer.cancel();
                timer.purge();
            } catch (Exception ignored) {
            }

            timer = null;
            timer = new Timer();

            timer.scheduleAtFixedRate(new TimerTask() {
                int i = 1;
                int Count = 5;

                @Override
                public void run() {
                    Log.e("timercheck","good");
                    if (staticConnectionStatus && staticSendData) {

                        if (i>1) {
                            Log.e("Dashboard",String.valueOf(STATUS_PACKET_DELAY));
                            //Count++;
                            Calendar c = Calendar.getInstance();
                            finaltime = simpleDateFormat.format(c.getTime());

                            sharedPreferences = getApplicationContext().getSharedPreferences("vehicle_data",MODE_PRIVATE);
                            speedAlert = sharedPreferences.getInt("speed",0);

                            speedAlertForDashboard = String.format("%03d", speedAlert);
                            Log.e("statuspacket_check",String.valueOf(Count));
                            sendTimeSyncData();
                            //Count=0;

                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) registerPhoneListener();

                            if(i==2){

                                Realm realm = Realm.getDefaultInstance();

                                try{
                                    realm.executeTransaction(realm1 -> {
                                        RiderProfileModule riderProfile = realm1.where(RiderProfileModule.class).equalTo("id", 1).findFirst();

                                        riderProfile.setPrev_cluster(BikeBleName.getValue());

                                        realm1.insertOrUpdate(riderProfile);
                                    });
                                }catch (Exception e){
                                    Log.e("update_profile",String.valueOf(e));
                                    e.printStackTrace();
                                }
                                i=5;
                            }

                        } else {
                            Log.e("statuspacket", String.valueOf(i));
                            sendUSERINFOtoDashboard(getApplicationContext().getSharedPreferences("user_data", MODE_PRIVATE).getString("name", ""));
                            i++;
                            wrong_fuel_data_discarded=false;
                        }
                    }
                }
            }, 0, 2000);//Update text every second
        }
    }

    private void sendUSERINFOtoDashboard(String dataToSend) {

        byte[] PKT = getSmartPhoneUSERINFOPKT(dataToSend);
        if (mBoundService != null) mBoundService.writeDataFromAPPtoDevice(PKT,36);
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

            Log.e("value_checl",String.valueOf(value));
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

    @Override
    public void onResume() {
        super.onResume();

        try{
            if (!flag) snackbar.dismiss();
        }catch (Exception e){
            e.printStackTrace();
        }

        //update_dashboard();

        //update_dashboard_new();
        if(!staticConnectionStatus) update_button(false);

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("FT", MODE_PRIVATE);
        value = sharedPreferences.getInt("first",0);

        IntentFilter intentFilterBleStatus = new IntentFilter("status");

        mReceiver = new BleConnection() {

            @Override
            public void onReceive(Context context, Intent intent) {
                //extract our message from intent

                status = Objects.requireNonNull(intent.getExtras()).getBoolean("status");

                if (timer == null) timer = new Timer();

                timer.cancel();
                timer.purge();
                timer = null;
                timer = new Timer();

                if (BLUETOOTH_STATE) {

                    if (status) {

                        boolean isConnected = BleManager.getInstance().isConnected(bleDevice);

                        if (isConnected) updateDisplay(userName);

                    } else {

                        timer.cancel();
                        timer.purge();
                        timer = null;
                    }
                } else {

                    timer.cancel();
                    timer.purge();
                    timer = null;
                }
            }
        };
        String prev_conn_cluster = sharedPreferences.getString("blename","");

        //registering our receiver
        requireActivity().registerReceiver(mReceiver, intentFilterBleStatus);

        //don't run scan and try to connect ble here
        /*bleManager = BleManager.getInstance();
        bleManager.init(HOME_SCREEN_OBJ.getApplication());
        mDeviceAdapter = new BleListingDeviceAdapter(HOME_SCREEN_OBJ);
        if(!prev_conn_cluster.isEmpty()){

            List<BleDevice> test = bleManager.getAllConnectedDevice();
            for (int i=0;i<test.size();i++){
                if(String.valueOf(test.get(i).getName()).equals(prev_conn_cluster)) staticConnectionStatus = true; break;
            }

            if(!staticConnectionStatus){
                bleManager.scan(new BleScanCallback() {
                    @Override
                    public void onScanStarted(boolean success) { }

                    @Override
                    public void onScanning(BleDevice bleDevice) {
                        Log.e("nametest_matching",String.valueOf(prev_conn_cluster)+String.valueOf(bleDevice.getName()));
                        if(String.valueOf(bleDevice.getName()).equals(prev_conn_cluster)){
                            Log.e("nametest_matched",String.valueOf(bleDevice.getName()));
                            bleManager.cancelScan();

                            if(!in_prog){
                                bleManager.connect(bleDevice,new BleGattCallback(){

                                    @Override
                                    public void onStartConnect() {
                                        in_prog = true;
                                    }

                                    @Override
                                    public void onConnectFail(BleDevice bleDevice, BleException exception) {
                                        in_prog = false;
                                    }

                                    @Override
                                    public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {

                                        boolean isConnected = bleManager.isConnected(bleDevice);

                                        if (isConnected) {

                                            setMtu(bleDevice, 250);
                                            app.setBleDevice(bleDevice);

                                            onDisconnect = false;
                                            staticConnectionStatus = true;
                                            staticSendData = true;

                                            Intent i = new Intent("status").putExtra("status", staticConnectionStatus);
                                            HOME_SCREEN_OBJ.sendBroadcast(i);
                                            getConnectionStatus(staticConnectionStatus, getApplicationContext());

//                                            prev_cluster = bleDevice.getMac();

//                                            BikeBleName = bleDevice.getName(); Log.e("bikeble","ADDED dash 1078: "+String.valueOf(BikeBleName));

//                                            if(BikeBleName==null) BikeBleName=""; Log.e("bikeble","cleared dash 1080");

                                            try{
                                                if (BikeBleName.getValue().charAt(1)=='A') PRICOL_CONNECTED=true;
                                                else PRICOL_CONNECTED=false;
                                            }catch (Exception e){

                                            }
                                            Log.e("state","Connected: "+BikeBleName+":::"+String.valueOf(PRICOL_CONNECTED));
                                        }

                                        if(mBoundService!=null) new Handler().postDelayed(() -> mBoundService.getServicesList(bleDevice), 500);

                                        new Handler().postDelayed(() -> {
                                            try {
                                                BluetoothGattService service = gatt.getServices().get(3);

                                                realm.executeTransaction(realm -> {

                                                    BleDataPojo bleDataPojo = realm.where(BleDataPojo.class).equalTo("bleId", 1).findFirst();

                                                    if (bleDataPojo == null) bleDataPojo = realm.createObject(BleDataPojo.class, 1);

                                                    bleDataPojo.setDeviceMacAddress(bleDevice.getMac());
                                                    bleDataPojo.setDeviceName(bleDevice.getName());
//                                                    prev_cluster = bleDevice.getMac();

//                                                    BikeBleName = bleDevice.getName(); Log.e("bikeble","ADDED dash 1107: "+String.valueOf(BikeBleName));
                                                    *//*if(BikeBleName==null){
                                                        BikeBleName="";Log.e("bikeble","cleared dash 1110");
                                                    }*//*

                                                    bleDataPojo.setReadCharacteristic(service.getCharacteristics().get(1).getUuid().toString());
                                                    bleDataPojo.setWriteCharacteristic(service.getCharacteristics().get(0).getUuid().toString());
                                                    bleDataPojo.setServiceID(gatt.getServices().get(3).getUuid().toString());
                                                    realm.insertOrUpdate(bleDataPojo);
                                                });
                                            } catch (Exception e) {
                                                Log.e(EXCEPTION,"ListScan: connect: "+String.valueOf(e));
                                            }
                                        }, 500);
                                    }

                                    @Override
                                    public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) { }
                                });
                            }
                        }
                    }

                    @Override
                    public void onScanFinished(List<BleDevice> scanResultList) { }
                });
            }
        }*/
    }

    private static void update_button(Boolean connected) {
        /*Log.e("update_button", String.valueOf(connected));
        if(connected){

            if (BikeBleName.equals("")) BikeBleName = prev_cluster_name;
            tvBleName.setText(BikeBleName);Log.e("blesetcheck","SET dash 1142");
            rlButtonWhiePair.setVisibility(View.GONE);
            rlButtonConnect.setVisibility(View.VISIBLE);
            //wrong_fuel_data_discarded=false;

            get_vehicle_type();
        }else{

            staticConnectionStatus=connected;
            rlButtonConnect.setVisibility(View.GONE);
            rlButtonWhiePair.setVisibility(View.VISIBLE);

            //scan();
        }*/
    }

    /*private static void scan() {
        bleManager = BleManager.getInstance();
        bleManager.init(HOME_SCREEN_OBJ.getApplication());

        bleManager.scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                Log.e("conn_state","onscan");
            }

            @Override
            public void onScanning(BleDevice bleDevice) {

                if (prev_cluster_name!=null){
                    if (prev_cluster_name.equals(bleDevice.getName())){
                        Log.e("conn_state","cluster found");
                        bleManager.connect(bleDevice, new BleGattCallback() {
                            @Override
                            public void onStartConnect() {
                                Log.e("conn_state","start connect");
                            }

                            @Override
                            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                                Log.e("conn_state","connect error");
                            }

                            @Override
                            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                                Log.e("conn_state","connect success");
                                bleManager.cancelScan();
                            }

                            @Override
                            public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                                Log.e("conn_state","disconnected");
                            }
                        });
                    }
                }

//                Log.e("scan_state","scanning: "+String.valueOf(bleDevice.getName()));
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                Log.e("conn_state","scan completed");
                scan();
            }
        });
    }*/

    public static void logData(String log) {
        try {
            String timeStamp = dateFormat.format(new Date());
            File root = new File(Environment.getExternalStorageDirectory(), "Suzuki");
            if (!root.exists()) {
                root.mkdirs();
            }
            File file = new File(root, "LogFile.txt");
            FileWriter writer = new FileWriter(file, true);
            writer.append(timeStamp).append(": ").append(log).append("\n");
            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //don't set MTU on dashboard
    /*private void setMtu(BleDevice bleDevice, int mtu) {
        bleManager.setMtu(bleDevice, mtu, new BleMtuChangedCallback() {
            @Override
            public void onSetMTUFailure(BleException exception) {
                Log.e(EXCEPTION, getClass().getName()+" error " + exception.toString());
            }

            @Override
            public void onMtuChanged(int mtu) {
                Log.e("notification", getClass().getName()+" onMtuChanged: " + mtu);
            }
        });
    }*/

    private String tripKMUpdate(String tripKM) {

        String result = tripKM.substring(0, 5) + "." + tripKM.substring(5, tripKM.length());
        result = String.valueOf(Float.parseFloat(result));

        return result;
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onPause() {
        sharedPreferences = getContext().getSharedPreferences("vehicle_data", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putInt("fuel_level", FuelLevel);
        editor.apply();

        Realm realm = Realm.getDefaultInstance();
        realm.executeTransaction(realm1 -> {
            RiderProfileModule riderProfile = realm1.where(RiderProfileModule.class).equalTo("id", 1).findFirst();

            if (riderProfile == null) {
                riderProfile = realm1.createObject(RiderProfileModule.class, 1);

                riderProfile.setTripA("0.0");
                riderProfile.setTripB("0.0");
                riderProfile.setOdometer("0");
                riderProfile.setFuelBars(1);

            } else {
                riderProfile.setTripA(TripA);
                riderProfile.setTripB(TripB);
                riderProfile.setOdometer(Odometer);
                riderProfile.setFuelBars(FuelLevel);
            }
            realm1.insertOrUpdate(riderProfile);
        });

        super.onPause();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        intentfilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

        timer = new Timer();
        BleManager.getInstance()
                .enableLog(true)
                .setMaxConnectCount(1)
                .setReConnectCount(1, 1000)
                .setConnectOverTime(60000)
                .setOperateTimeout(5000);
        isMyServiceRunning();

        try {
            readClusterFile();
        } catch (Exception ignored) {
        }
    }

    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (MyBleService.class.getName().equals(service.service.getClassName())) return true;
        }

        return false;
    }

    private boolean checkGPSIsOpen() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) return false;

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //if (requestCode == REQUEST_CODE_PERMISSION_LOCATION) displayAutoStartDialog();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        //if (requestCode == REQUEST_CODE_OPEN_GPS) displayAutoStartDialog();

        super.onActivityResult(requestCode, resultCode, data);
    }

    private BroadcastReceiver broadcastreceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            deviceStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int batteryLevel = (int) (((float) level / (float) scale) * 100.0f);

            if (deviceStatus == BatteryManager.BATTERY_STATUS_CHARGING) {

                if (batteryLevel >= 75) currentBatteryStatus = "3" + "Y";
                else if (batteryLevel >= 50 && batteryLevel < 75) currentBatteryStatus = "2" + "Y";
                else if (batteryLevel >= 25 && batteryLevel < 50) currentBatteryStatus = "1" + "Y";
                else if (batteryLevel >= 0 && batteryLevel < 25) currentBatteryStatus = "0" + "Y";

            } else if (deviceStatus == BatteryManager.BATTERY_STATUS_DISCHARGING) {


                if (batteryLevel >= 75) currentBatteryStatus = "3" + "N";
                else if (batteryLevel >= 50 && batteryLevel < 75) currentBatteryStatus = "2" + "N";
                else if (batteryLevel >= 25 && batteryLevel < 50) currentBatteryStatus = "1" + "N";
                else if (batteryLevel >= 0 && batteryLevel < 25) currentBatteryStatus = "0" + "N";

            } else if (deviceStatus == BatteryManager.BATTERY_STATUS_FULL) {
                currentBatteryStatus = "3" + "N";

            } else if (deviceStatus == BatteryManager.BATTERY_STATUS_UNKNOWN) {

                if (batteryLevel >= 75) {
                    currentBatteryStatus = "3" + "N";


                } else if (batteryLevel >= 50 && batteryLevel < 75) {
                    currentBatteryStatus = "2" + "N";


                } else if (batteryLevel >= 25 && batteryLevel < 50) {
                    currentBatteryStatus = "1" + "N";


                } else if (batteryLevel >= 0 && batteryLevel < 25) {
                    currentBatteryStatus = "0" + "N";

                }
            }
            if (deviceStatus == BatteryManager.BATTERY_STATUS_NOT_CHARGING) {

                if (batteryLevel >= 75) {
                    currentBatteryStatus = "3" + "N";


                } else if (batteryLevel >= 50 && batteryLevel < 75) {
                    currentBatteryStatus = "2" + "N";


                } else if (batteryLevel >= 25 && batteryLevel < 50) {
                    currentBatteryStatus = "1" + "N";


                } else if (batteryLevel >= 0 && batteryLevel < 25) {
                    currentBatteryStatus = "0" + "N";

                }
            }
        }
    };

    @Override
    public void onClick(View view) {
        startActivity(new Intent(HOME_SCREEN_OBJ, ProfileActivity.class));
    }

    private class MyPhoneStateListener extends PhoneStateListener {

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);

            updateSignalStrength(signalStrength);
        }

        @Override
        public void onDataConnectionStateChanged(int state) {
            super.onDataConnectionStateChanged(state);
        }
    }

    private void registerPhoneListener() {
        if (Tel != null && MyListener != null) {
            Tel.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        }
    }

    private void updateSignalStrength(SignalStrength signalStrength) {

        if (signalStrength.getLevel()==0){
            signal = "0";
            SignalNavigation = false;
            NoSignal = true;
        }

        /*for calculating in dBm use this
        (2 * mSignalStrength) - 113*/
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int level = signalStrength.getLevel();
            if (level >= 4) {

                signal = "3";
                SignalNavigation = false;
                NoSignal = false;

            } else if (level == 3) {
                signal = "2";
                SignalNavigation = false;
                NoSignal = false;

            } else if (level == 2) {
                signal = "1";
                SignalNavigation = false;
                NoSignal = false;

            } else if (level == 1) {
                signal = "0";
                SignalNavigation = false;
                NoSignal = true;

            } else {
                SignalNavigation = true;
                NoSignal = false;
            }
        } else {
            if (signalStrength.getGsmSignalStrength() > 30) {

                signal = "3";
                SignalNavigation = false;
                NoSignal = false;

            } else if (signalStrength.getGsmSignalStrength() > 20 && signalStrength.getGsmSignalStrength() < 30) {
                signal = "2";
                SignalNavigation = false;
                NoSignal = false;

            } else if (signalStrength.getGsmSignalStrength() < 20 && signalStrength.getGsmSignalStrength() > 3) {
                signal = "1";
                SignalNavigation = false;
                NoSignal = false;

            } else if (signalStrength.getGsmSignalStrength() < 3) {
                signal = "0";
                SignalNavigation = false;
                NoSignal = true;

            } else {
                SignalNavigation = true;
                NoSignal = false;
            }
        }
    }

    public byte[] GetSmartPhoneStatusPkt(String time) {
        String PhoneData;
        byte[] RetArray = new byte[30];

        String data = currentBatteryStatus + speedAlertForDashboard + signal + finaltime;

        try {
            if (data.length() <= 12) {

//            time += "\0";
                for (int i = data.length(); i <= 12; i++) {
//                ("Rets", "-: " + RetArray[i]);
                    data += "\0";
                }
                PhoneData = "?"/*Start of Frame*/ + "3"/*Frame ID*/ + currentBatteryStatus + speedAlertForDashboard + signal + finaltime/*Data to icd*/ /* */ + "0000000000000000"/*Fill 0s at reserved byte positions*/ /*End of Frame*/;
//            PhoneData = "?"/*Start of Frame*/ + "3"/*Frame ID*/ + time/*Data to icd*/ /* */ + "\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0"/*Fill 0s at reserved byte positions*/ /*End of Frame*/;
//            ÿ

            } else {

                data = data.substring(0, 12);
                PhoneData = "?"/*Start of Frame*/ + "3"/*Frame ID*/ + currentBatteryStatus + speedAlertForDashboard + signal + finaltime/*Phone data to icd*/ + "0000000000000000"/*Fill 0s at reserved byte positions*/ /*End of Frame*/;
            }

            try {

                RetArray = PhoneData.getBytes("UTF-8");
                RetArray[0] = (byte) 0xA5;

                if (Integer.parseInt(speedAlertForDashboard)==0) {
                    RetArray[4] = (byte) 0xFF;
                    RetArray[5] = (byte) 0xFF;
                    RetArray[6] = (byte) 0xFF;
                }

                if (finaltime.contentEquals("000000")) {
                    for (int i = 8; i <= 13; i++) {
                        RetArray[i] = (byte) 0xFF;
                    }
                }

                for (int k = 16; k <= 27; k++) RetArray[k] = (byte) 0xFF;

                // msg clear flag
                RetArray[14] = MSG_CLEAR;

                // missed call clear flag
                RetArray[15] = CALL_CLEAR;
                Log.e("clear_flag",String.valueOf(MSG_CLEAR)+"::"+String.valueOf(CALL_CLEAR));

                RetArray[28] = calculateCheckSum(RetArray);
//            RetArray[28] =(byte) 0xFF;

                RetArray[29] = (byte) 0x7F;

//            calculateCheckSum(RetArray);

//            ("Phbtry Checksum pkt", "- " + RetArray[28]);

                return RetArray;
            } catch (java.io.IOException e) {
                return RetArray;
            }
        } catch (Exception e) {
            return RetArray;
        }
    }

    public static void getConnectionStatus(final boolean status, final Context context) {
        Log.e("getconnectionstatus",String.valueOf(context.getClass()));

        updateBluetoothStatusToUI(status);
    }

    public SuzukiApplication getMyApplication() {
//        if (((SuzukiApplication) getActivity().getApplication()) != null)
        return ((SuzukiApplication) getActivity().getApplication());
    }

    private static void updateBluetoothStatusToUI(final boolean status) {
        new Handler(Looper.getMainLooper()).post(() -> {

            if (BLUETOOTH_STATE) {
                if (status) update_button(true);
                else update_button(false);

            } else {
                connectionStarted = false;

                //app was crashing when the location permission was not given and search is started for cluster
                try{
                    update_button(false);
                    Log.e("white","four");
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    public void ClusterDataPacket(byte[] u1_buffer) {
        if ((u1_buffer[0] == -91) && (u1_buffer[1] == 55) && (u1_buffer[29] == 127)) {

            byte Crc = calculateCheckSum(u1_buffer);

            if (u1_buffer[28] == Crc) {
                String Cluster_data = new String(u1_buffer);

                Odometer = Cluster_data.substring(5, 11);

                /*sharedPreferences = getApplicationContext().getSharedPreferences("vehicle_data",MODE_PRIVATE);
                editor = sharedPreferences.edit();
                editor.putString("odometer",Odometer);
                editor.apply();*/

                TripA = Cluster_data.substring(11, 17);
                TripB = Cluster_data.substring(17, 23);
                Fuel = Cluster_data.substring(24, 25);

                //Log.e("Fuel",String.valueOf(Fuel));
                byte FuelValue = u1_buffer[24];

                if(!wrong_fuel_data_discarded){
                    wrong_fuel_data_discarded=true;
                    Log.e("FuelVlaue", String.valueOf(FuelValue)+": DISCARDED");
                    return;
                }
                else Log.e("FuelVlaue", String.valueOf(FuelValue));

                if (FuelValue == 0x36) FuelLevel = 6;
                else if (FuelValue == 0x35) FuelLevel = 5;
                else if ((FuelValue == 0x34)) FuelLevel = 4;
                else if ((FuelValue == 0x33)) FuelLevel = 3;
                else if ((FuelValue == 0x32)) FuelLevel = 2;
                else if ((FuelValue == 0x31)) FuelLevel = 1;
                else FuelLevel = 0;

                getActivity().runOnUiThread(() -> {

                    String odometer_reading = Integer.parseInt(Odometer) + " " + "km";
                    tvOdometer.setText(odometer_reading);

                    sharedPreferences = getApplicationContext().getSharedPreferences("vehicle_data",MODE_PRIVATE);
                    editor = sharedPreferences.edit();
                    editor.putString("odometer",odometer_reading);
                    editor.apply();

                    tvTripA.setText(tripKMUpdate(TripA) + " km");
                    tvTripB.setText(tripKMUpdate(TripB) + " km");

                    update_fuel_ui();
                });
            }
        }
    }

    //subscriber is required
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectionEvent(EvenConnectionPojo event) {

        if (event.isConnection()) {
            BLUETOOTH_STATE = true;

//            mBoundService.setScanRule();

        } else {

            BLUETOOTH_STATE = false;
            staticConnectionStatus = false;
            update_button(BLUETOOTH_STATE);
            Log.e("update_button","onConnectionEvent");
            Intent i = new Intent("status").putExtra("status", staticConnectionStatus);
            getActivity().sendBroadcast(i);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onClusterDataRecev(ClusterStatusPktPojo event) {
        if (event.getClusterData().length() == 30) {
            ClusterDataPacket(event.getClusterByteData());
        }
    }

    private static ArrayList<String> readFileTOCheckData(File file) {

//        StringBuilder text = new StringBuilder();
        ArrayList<String> savedDevices = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                savedDevices.add(line);
//                text.append(line);
//                text.append('\n');
            }

            if (savedDevices.size() >= 10) {
                savedDevices.remove(0);
            }

            br.close();

        } catch (IOException e) {
            //You'll need to add proper error handling here
        }

        return savedDevices;
    }

    /*private void askForAutoStart() {
        String manufacturer = android.os.Build.MANUFACTURER;
        try {
            Intent intent = new Intent();
            if ("xiaomi".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
            } else if ("oppo".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"));
            } else if ("vivo".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"));
            } else if ("Letv".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity"));
            } else if ("Honor".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"));
            }

            List<ResolveInfo> list = getApplicationContext().getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (list.size() > 0) {
                startActivity(intent);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }
    public void update_clusted_detail() {
        if (staticConnectionStatus) {

            rlButtonWhiePair.setVisibility(View.GONE);
            tvBleName.setText(bleName); Log.e("blesetcheck","SET dash 1696");
            Log.e("blecheck","one");

            rlButtonConnect.setVisibility(View.VISIBLE);
        } else {
            connectionStarted = false;
            rlButtonConnect.setVisibility(View.GONE);
            rlButtonWhiePair.setVisibility(View.VISIBLE);
        }
    }
    public static void saveToCluster(String msg) {
        try {
            Log.e("mybleservice","savetocluster");
            File root = new File(Environment.getExternalStorageDirectory(), "Suzuki");
            if (!root.exists()) {
                root.mkdirs();
            }
            File file = new File(root, "Data.txt");

            ArrayList<String> savedDeviceList = readFileTOCheckData(file);

            FileWriter writer = new FileWriter(file, false);

            for (String device : savedDeviceList) {
                if (!device.equalsIgnoreCase(msg))
                    writer.append(device).append("\n");
            }
            Log.e("mybleservice_msg",msg);
            writer.append(msg).append("\n");
            writer.flush();
            writer.close();

        } catch (IOException e) {
            Log.e("mybleservice","savetocluster: "+String.valueOf(e));
            e.printStackTrace();
        }
    }*/
    private boolean readClusterFile() {
        boolean clusterMatched = false;

        File root = new File(Environment.getExternalStorageDirectory(), "Suzuki");
        if (!root.exists()) return false;

        //Get the text file
        File file = new File(root, "Data.txt");

        //Read text from file
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            ArrayList<String> deviceList = new ArrayList<>();

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
                deviceList.add(line);
            }

            br.close();

            if (bleDevice != null) if (text.toString().contains(bleDevice.getName())) clusterMatched = true;

        } catch (IOException e) {
            //You'll need to add proper error handling here
        }
        return clusterMatched;
    }

    private void update_dashboard_new() {
        //fuel data
        //fuelGif.setVisibility(View.VISIBLE);
        //ivFuelMeter.setVisibility(View.GONE);


        //image data
        sharedPreferences = getApplicationContext().getSharedPreferences("vehicle_data", MODE_PRIVATE);
        type = sharedPreferences.getString("vehicle_type","");
        model = sharedPreferences.getString("vehicle_name","");
        variant = sharedPreferences.getInt("vehicle_model",0);

        Log.e("vehicle_data",type+" "+model+" "+String.valueOf(variant));

        if (type.equals("Scooter")){
            fuelGif.setGifResource(R.drawable.fuel_gif);

            switch (model){
                case "Avenis":
                    switch (variant){
                        case 0:
                            ivUserBike.setImageResource(R.drawable.avenis_blue);
                            break;

                        case 1:
                            ivUserBike.setImageResource(R.drawable.avenis_green);
                            break;

                        case 2:
                            ivUserBike.setImageResource(R.drawable.avenis_grey);
                            break;

                        case 3:
                            ivUserBike.setImageResource(R.drawable.avenis_red);
                            break;

                        case 4:
                            ivUserBike.setImageResource(R.drawable.avenis_white);
                            break;
                    }

                    break;

                case "Access 125":


                    switch (variant){
                        case 0:
                            ivUserBike.setImageResource(R.drawable.access_125_se_black);
                            break;

                        case 1:
                            ivUserBike.setImageResource(R.drawable.access_dual_tone);
                            break;

                        case 2:
                            ivUserBike.setImageResource(R.drawable.access_125_se_matte_blue);
                            break;

                        case 3:
                            ivUserBike.setImageResource(R.drawable.access_125_se_bronze);
                            break;

                        case 4:
                            ivUserBike.setImageResource(R.drawable.access_125_se_white);
                            break;

                        case 5:
                            ivUserBike.setImageResource(R.drawable.access_glossy_grey);
                            break;
                    }
                    break;

                case "Burgman Street":
                    switch (variant){
                        case 0:
                            ivUserBike.setImageResource(R.drawable.burgman_fibroin_grey);
                            break;

                        case 1:
                            ivUserBike.setImageResource(R.drawable.burgman_matteblack);
                            break;

                        case 2:
                            ivUserBike.setImageResource(R.drawable.burgman_matte_blue);
                            break;

                        case 3:
                            ivUserBike.setImageResource(R.drawable.burgman_matte_red);
                            break;

                        case 4:
                            ivUserBike.setImageResource(R.drawable.burgman_mirage_white);
                            break;
                    }

                    break;


                case "Burgman Street EX":
                    switch (variant){
                        case 0:
                            ivUserBike.setImageResource(R.drawable.burgman_ex_black);
                            break;

                        case 1:
                            ivUserBike.setImageResource(R.drawable.burgman_ex_silver);
                            break;

                        case 2:
                            ivUserBike.setImageResource(R.drawable.burgman_ex_bronze);
                            break;
                    }
                    break;
            }
        }

        else if (type.equals("Motorcycle")){
            fuelGif.setGifResource(R.drawable.fuel_gif_six);
            switch (model){

                case "V-STROM SX":

                    switch (variant){
                        case 0:
                            ivUserBike.setImageResource(R.drawable.vstorm_red);
                            break;

                        case 1:
                            ivUserBike.setImageResource(R.drawable.vstorm_yellow);
                            break;

                        case 2:
                            ivUserBike.setImageResource(R.drawable.vstorm_black);
                            break;
                    }
                    break;

                case "GIXXER / GIXXER SF":
                    switch (variant){
                        case 0:
                            ivUserBike.setImageResource(R.drawable.gixxer150black);
                            break;

                        case 1:
                            ivUserBike.setImageResource(R.drawable.gixxer150blue);
                            break;

                        case 2:
                            ivUserBike.setImageResource(R.drawable.gixxer150orange);
                            break;
                            
                        case 3:
                            ivUserBike.setImageResource(R.drawable.gixxer150sfblack);
                            break;

                        case 4:
                            ivUserBike.setImageResource(R.drawable.gixxer150sforange);
                            break;

                        case 5:
                            ivUserBike.setImageResource(R.drawable.gixxer150sfblue);
                            break;
                    }
                    break;

                case "GIXXER 250 / GIXXER SF 250":
                    switch (variant){
                        case 0:
                            ivUserBike.setImageResource(R.drawable.gixxer250black);
                            break;

                        case 1:
                            ivUserBike.setImageResource(R.drawable.gixxer250blue);
                            break;

                        case 2:
                            ivUserBike.setImageResource(R.drawable.gixxer250sfblack);
                            break;

                        case 3:
                            ivUserBike.setImageResource(R.drawable.gixxer250sf);
                            break;

                        case 4:
                            ivUserBike.setImageResource(R.drawable.gixxer250sfblue);
                            break;
                    }
                    break;

            }
        }

        //button data
        if (BikeBleName.getValue().isEmpty()){
            rlButtonWhiePair.setVisibility(View.VISIBLE);
            rlButtonConnect.setVisibility(View.GONE);
        } else{
            tvBleName.setText(BikeBleName.getValue());
            rlButtonWhiePair.setVisibility(View.GONE);
            rlButtonConnect.setVisibility(View.VISIBLE);
        }
    }

    private void update_vehicle_data() {

           // sharedPreferences = getSharedPreferences("vehicle_data",MODE_PRIVATE);
            String prev_type = sharedPreferences.getString("vehicle_type","");
            int prev_model=sharedPreferences.getInt("vehicle_model",0);

            if (Objects.requireNonNull(BikeBleName.getValue()).charAt(1) == 'A'){

                //access, burgman
                if (BikeBleName.getValue().charAt(2) == 'S') {

                    //Scooter of Pricol
                    if (BikeBleName.getValue().charAt(3) == '0' && BikeBleName.getValue().charAt(4) == '1') {
                        type = "Scooter";

                    }

                    else if (BikeBleName.getValue().charAt(3) == '1' && BikeBleName.getValue().charAt(4) == '1') {
                        type = "Scooter";

                    }

                    else if (BikeBleName.getValue().charAt(3) == '0' && BikeBleName.getValue().charAt(4) == '2') {
                        type = "Scooter";


                    }

                    else if (BikeBleName.getValue().charAt(3) == '1' && BikeBleName.getValue().charAt(4) == '2') {
                        type = "Scooter";

                    }
                }
                else {
                    //Bike of pricol
                }

//            type = "Scooter";
//            Model = "Burgman Street";
//            PRICOL_CONNECTED = true;

            }

            else if (BikeBleName.getValue().charAt(1) == 'B'){
                //Avenis, Burgman EX, V strom, Gixxer , Gixxer SF
                PRICOL_CONNECTED=false;
                if (BikeBleName.getValue().charAt(2) == 'S'){
                    if (BikeBleName.getValue().charAt(3) == '2' && BikeBleName.getValue().charAt(4) == '1') {
                        type = "Scooter";

                    } else if (BikeBleName.getValue().charAt(3) == '3' && BikeBleName.getValue().charAt(4) == '1') {
                        type = "Scooter";

                    }
                }

                else if (BikeBleName.getValue().charAt(2) == 'M'){
                    if (BikeBleName.getValue().charAt(3) == '0' && BikeBleName.getValue().charAt(4) == '1') {
                        type = "Motorcycle";
                    } else if (BikeBleName.getValue().charAt(3) == '1' && BikeBleName.getValue().charAt(4) == '1') {
                        type = "Motorcycle";
                    } else if (BikeBleName.getValue().charAt(3) == '2' && BikeBleName.getValue().charAt(4) == '1') {
                        type = "Motorcycle";
                    }
                }
            }

//            if (!prev_type.equals(type)){
//                new Common(DeviceListingScanActivity.this).update_vehicle_data(type,Model,0);
//                Log.e("vehicle_data","updated at devicelistscan 600");
//            }
//
//            if (!String.valueOf(prev_model).equals(Model)){
//                new Common(DeviceListingScanActivity.this).update_vehicle_data(type,Model,0);
//                Log.e("vehicle_data","updated at devicelistscan 600");
//            }
            }
}