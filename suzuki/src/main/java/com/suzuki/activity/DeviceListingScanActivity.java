package com.suzuki.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleMtuChangedCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.suzuki.R;
import com.suzuki.adapter.BleListingDeviceAdapter;
import com.suzuki.application.SuzukiApplication;
import com.suzuki.base.BaseActivity;
import com.suzuki.fragment.DashboardFragment;
import com.suzuki.pojo.BleDataPojo;
import com.suzuki.pojo.EvenConnectionPojo;
import com.suzuki.pojo.RiderProfileModule;
import com.suzuki.services.MyBleService;
import com.suzuki.utils.Common;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;

import static com.suzuki.activity.ConnectedDataActivity.bleName;
import static com.suzuki.activity.HomeScreenActivity.TOAST_DURATION;
import static com.suzuki.activity.HomeScreenActivity.mBoundService;
import static com.suzuki.broadcaster.BluetoothCheck.BLUETOOTH_STATE;
import static com.suzuki.fragment.DashboardFragment.app;
import static com.suzuki.fragment.DashboardFragment.displayingDisconnectDialog;
import static com.suzuki.fragment.DashboardFragment.getConnectionStatus;
import static com.suzuki.fragment.DashboardFragment.onDisconnect;
import static com.suzuki.fragment.DashboardFragment.prev_cluster_macAddr;
import static com.suzuki.fragment.DashboardFragment.prev_cluster_name;
import static com.suzuki.fragment.DashboardFragment.showBluetoothDialog;
import static com.suzuki.fragment.DashboardFragment.staticConnectionStatus;
import static com.suzuki.fragment.DashboardFragment.staticSendData;
import static com.suzuki.fragment.DashboardFragment.userStatus;
import static com.suzuki.services.MyBleService.connectionStarted;
import static com.suzuki.utils.Common.BikeBleName;
import static com.suzuki.utils.Common.EXCEPTION;
import static com.suzuki.utils.Common.FIRST_TIME;
import static com.suzuki.utils.Common.PRICOL_CONNECTED;
import static com.suzuki.utils.Common.global_bleDevice;


public class DeviceListingScanActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = DeviceListingScanActivity.class.getSimpleName();

    private static final int REQUEST_CODE_OPEN_GPS = 1, REQUEST_CODE_PERMISSION_LOCATION = 2;
    String check_navigation;
    public static boolean IsManualConnection = false;
    boolean switchcluter=false;
    ListView listView_device;
    private BleListingDeviceAdapter mDeviceAdapter;
    private ProgressBar api_progress_bar;
    public BluetoothAdapter bluetoothadapter = null;
    //private static final int REQUEST_ENABLE_BT = 1;
    public static TextView tvStatus;
    public static RelativeLayout rlButtonConnect, rlButtonRefresh, rlClose;
    Realm realm;
    //ProgressDialog mProgressDialog;
View view;
    public static String userName;

    public static BleDevice bleDevice;

    public String readCharacterID, serviceID, writeCharacterID;
    Common common;
    private BleManager bleManager;
    public static DeviceListingScanActivity DEVICESCAN_OBJ;
    private ProgressDialog progress_d;
    private String Model, type;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public SuzukiApplication getMyApplication() {
        return ((SuzukiApplication) this.getApplication());
    }

    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_listing_scanfastble_activity);

        DEVICESCAN_OBJ=this;
        common = new Common(this);
        handler = new Handler();
        bleManager = BleManager.getInstance();
        bleManager.init(getApplication());
        bleManager.enableLog(true)
                .setMaxConnectCount(1)
                .setReConnectCount(1, 5000)
                .setConnectOverTime(20000)
                .setOperateTimeout(5000);

        realm = Realm.getDefaultInstance();
        app = getMyApplication();
        viewRecord();

        Intent intent = getIntent();
        check_navigation = intent.getStringExtra("navigationScreen");

        initView();

        bluetoothadapter = BluetoothAdapter.getDefaultAdapter();

        checkforBluetoothConnection();
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    public void viewRecord() {

        try {
            realm.executeTransaction(realm -> {

                RiderProfileModule riderProfile = realm.where(RiderProfileModule.class).equalTo("id", 1).findFirst();

                if (riderProfile == null) userName = "0";
                else {
                    if (riderProfile.getName() != null) userName = riderProfile.getName();
                    else userName = "0";
                }

                BleDataPojo bleDataPojo = realm.where(BleDataPojo.class).equalTo("bleId", 1).findFirst();

                if (bleDataPojo == null) {
//                    prev_cluster = null;
                    serviceID = null;
                    readCharacterID = null;
//                    BikeBleName = "";Log.e("bikeble","CLEARED devicelist 156");
                    writeCharacterID = null;

                } else {
//                    prev_cluster = bleDataPojo.getDeviceMacAddress();
                    serviceID = bleDataPojo.getServiceID();
                    readCharacterID = bleDataPojo.getReadCharacteristic();
//                    if(BikeBleName.equals("")) BikeBleName = bleDataPojo.getDeviceName(); Log.e("bikeble","ADDED devicelist 164"+String.valueOf(BikeBleName));
//                    if(BikeBleName==null){
//                        BikeBleName="";Log.e("bikeble devicelist 165","CLEARED");
//                    }

                    writeCharacterID = bleDataPojo.getWriteCharacteristic();
                }
            });
        } catch (Exception e) {
            Log.e(EXCEPTION,"ListScan: ViewRecord: "+String.valueOf(e));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewRecord();
        showConnectedDevice();
    }

    @Override
    protected void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectionEvent(EvenConnectionPojo event) {

        if (BLUETOOTH_STATE) startScan();
        else {
            checkPermissions();
            startScan();
        }
    }

    private void checkforBluetoothConnection() {
        if (!bluetoothadapter.isEnabled()) bluetoothadapter.enable();
        else {
            checkPermissions();
            try{
                bleManager.cancelScan();

            }catch (Exception e){
                Log.e(EXCEPTION,"ListScan: checkbtconnection: "+String.valueOf(e));
            }
            startScan();
        }
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (bleManager != null) {
                bleManager.cancelScan();
                startScan();
            }
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        IsManualConnection = false;
        if (handler != null) {
            try {
                handler.removeCallbacks(runnable);
            } catch (Exception e) { }
        }
    }

    @Override
    public void onClick(View v) { }

    @SuppressLint("SetTextI18n")
    private void initView() {

        tvStatus = findViewById(R.id.tvStatus);

        rlButtonConnect = findViewById(R.id.rlButtonConnect);
        rlButtonRefresh = findViewById(R.id.rlButtonRefresh);
        api_progress_bar = findViewById(R.id.api_progress_bar);
        rlClose = findViewById(R.id.rlClose);
        mDeviceAdapter = new BleListingDeviceAdapter(this);

        rlClose.setOnClickListener(v -> finish());
        rlButtonRefresh.setOnClickListener(v -> startScan());

        mDeviceAdapter.setOnDeviceClickListener(new BleListingDeviceAdapter.OnDeviceClickListener() {
            @Override
            public void onConnect(BleDevice bleDevice) {
                if (common != null && common.isDoubleClicked()) return;

                List<BleDevice> connectedDevices = bleManager.getAllConnectedDevice();

                if (connectedDevices == null || connectedDevices.size() <= 0) {

                    bleManager.cancelScan();
                    global_bleDevice=bleDevice;
                    connect(bleDevice);

                } else {

                    new AlertDialog.Builder(DeviceListingScanActivity.this)
                            .setTitle("Vehicle " + connectedDevices.get(0).getName() + " is already connected.")
                            .setMessage("Would you like to disconnect already connected vehicle?")
                            .setPositiveButton("Yes", (dialog, which) -> {

                                bleManager.disconnectAllDevice();

                                dialog.dismiss();
                            })
                            .setNegativeButton("No", ((dialog, which) -> {
                                dialog.dismiss();
                                finish();
                            })).create().show();
                }
            }

            @Override
            public void onDisConnect(final BleDevice bleDevice) {
                if (bleManager.isConnected(bleDevice)) bleManager.disconnect(bleDevice);
            }

            @Override
            public void onDetail(BleDevice bleDevice) {
                if (bleManager.isConnected(bleDevice)) {
                    boolean isConnected = bleManager.isConnected(bleDevice);

                    if (isConnected) {
                        tvStatus.setText("CONNECTED");
                        rlButtonConnect.setVisibility(View.VISIBLE);
                        tvStatus.setTextColor(getResources().getColor(R.color.app_theme_color));
                    }
                    app.setBleDevice(bleDevice);
                }
            }
        });

        listView_device = findViewById(R.id.frame_device_list);
        listView_device.setAdapter(mDeviceAdapter);

        rlButtonConnect.setOnClickListener(v -> {

            Dialog dialog = new Dialog(DeviceListingScanActivity.this, R.style.custom_dialog);
            dialog.setContentView(R.layout.custom_dialog);

            TextView tvAlertText = dialog.findViewById(R.id.tvAlertText);
            tvAlertText.setText(R.string.disconnect_vehicle);
            ImageView ivCross = dialog.findViewById(R.id.ivCross);
            ivCross.setOnClickListener(v12 -> dialog.cancel());

            ImageView ivCheck = dialog.findViewById(R.id.ivCheck);

            ivCheck.setOnClickListener(v1 -> {
                bluetoothadapter.disable();
                onDisconnect = true;
                bleManager.disconnectAllDevice();
                bleManager.destroy();

                Intent intent = new Intent(getApplicationContext(), MyBleService.class);
                stopService(intent);

                staticConnectionStatus = false;
                Intent i = new Intent("status").putExtra("status", false);
                sendBroadcast(i);
                getConnectionStatus(false, getApplicationContext());

                try {
                    realm.executeTransaction(realm -> {

                        BleDataPojo bleDataPojo = realm.where(BleDataPojo.class).equalTo("bleId", 1).findFirst();

                        if (bleDataPojo == null) {

                            bleDataPojo = realm.createObject(BleDataPojo.class, 1);
                            bleDataPojo.setDeviceMacAddress("");
                            bleDataPojo.setDeviceName("");

                            bleDataPojo.setReadCharacteristic("");
                            bleDataPojo.setWriteCharacteristic("");
                            bleDataPojo.setServiceID("");
                            realm.insertOrUpdate(bleDataPojo);

                        } else {

                            bleDataPojo.setDeviceMacAddress("");
                            bleDataPojo.setDeviceName("");

                            bleDataPojo.setReadCharacteristic("");
                            bleDataPojo.setWriteCharacteristic("");
                            bleDataPojo.setServiceID("");
                            realm.insertOrUpdate(bleDataPojo);

                        }
                    });
                } catch (Exception e) {
                    Log.e(EXCEPTION,"ListScan: initview: "+String.valueOf(e));
                }
                dialog.cancel();

                finish();
            });
            dialog.show();
        });
    }

    private void showConnectedDevice() {
        List<BleDevice> deviceList = bleManager.getAllConnectedDevice();
        mDeviceAdapter.clearConnectedDevice();
        for (BleDevice bleDevice : deviceList) {
            mDeviceAdapter.addDevice(bleDevice);
        }

        mDeviceAdapter.notifyDataSetChanged();
    }

    private void startScan() {

        bleManager.scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {

                mDeviceAdapter.clearScanDevice();
                showConnectedDevice();
                mDeviceAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLeScan(BleDevice bleDevice) {
                super.onLeScan(bleDevice);
            }

            @Override
            public void onScanning(BleDevice bleDevice) {
                mDeviceAdapter.addDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
                api_progress_bar.setVisibility(View.GONE);

                for (int i = 0; i < scanResultList.size(); i++) {

                    BleDevice bleDevice7 = scanResultList.get(i);

                    boolean isConnected = bleManager.isConnected(bleDevice7);
                    if (isConnected) {
                        app.setBleDevice(bleDevice);
                        setMtu(bleDevice, 250);

                        if (bleDevice7.getName() != null) bleName = bleDevice7.getName();

                        if(mBoundService!=null) mBoundService.getServicesList(bleDevice);

                        Intent intent = new Intent("status").putExtra("status", staticConnectionStatus);
                        sendBroadcast(intent);
                        getConnectionStatus(staticConnectionStatus, getApplicationContext());

                        tvStatus.setText("CONNECTED");
                        rlButtonConnect.setVisibility(View.VISIBLE);
                        tvStatus.setTextColor(getResources().getColor(R.color.app_theme_color));
                    }
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    public void connect(final BleDevice bleDevice) {
        if (handler != null) {
            try {
                handler.removeCallbacks(runnable);
            } catch (Exception e) {
                Log.e(EXCEPTION,"ListScan: connect: "+String.valueOf(e));
            }
        }

        bleManager.connect(bleDevice, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                try{
                    progress_d=new ProgressDialog(DeviceListingScanActivity.this);
                    progress_d.setMessage("Please Wait...");
                    progress_d.setCancelable(false);
                    progress_d.show();
                }catch (Exception ignored){}

            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                common.showToast(getResources().getString(R.string.fail), TOAST_DURATION);
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {

                mDeviceAdapter.addDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();
                boolean isConnected = bleManager.isConnected(bleDevice);

                if (isConnected) {

                    setMtu(bleDevice, 250);
                    app.setBleDevice(bleDevice);

                    onDisconnect = false;
                    staticConnectionStatus = true;
                    staticSendData = true;

                    Intent i = new Intent("status").putExtra("status", staticConnectionStatus);
                    sendBroadcast(i);
                    getConnectionStatus(staticConnectionStatus, getApplicationContext());
                    tvStatus.setText("CONNECTED");
                    rlButtonConnect.setVisibility(View.VISIBLE);
                    tvStatus.setTextColor(getResources().getColor(R.color.app_theme_color));
                    rlButtonConnect.setVisibility(View.VISIBLE);
//                    prev_cluster = bleDevice.getMac();

                    BikeBleName.setValue(bleDevice.getName());

                    update_vehicle_data();

                    prev_cluster_name = BikeBleName.getValue();
                    prev_cluster_macAddr = bleDevice.getMac();
                    SharedPreferences sharedPreferencesFinal = getSharedPreferences("BLE_DEVICE",Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferencesFinal.edit();
                    editor.putString("prev_cluster_macAddr", bleDevice.getMac());

                    if (sharedPreferencesFinal.getString("prev_cluster","").equals(BikeBleName.getValue())){
                        FIRST_TIME = false;
                    }

                    else FIRST_TIME = true;
                    editor.putString("prev_cluster_name", BikeBleName.getValue());
                    editor.putString("prev_cluster", BikeBleName.getValue()); //for feedback purpose
                    editor.apply();


                    if (navigationStarted==true){
                        finish();
                    }
                   else if(switching_of_vehicle==false){
                        finish();
                    }
                    else{
                        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(DeviceListingScanActivity.this);//this==context
                        if (!prefs.contains("FirstTimeConnection")) {
                            SharedPreferences.Editor editors = prefs.edit();
                            editors.putBoolean("FirstTimeConnection", false);
                            editors.commit();
                            finish();
                        }
                        else {
                            Intent intent=new Intent(DeviceListingScanActivity.this, ProfileActivity.class);
                            intent.putExtra("SwitchCluster","SwitchCluster");
                            startActivity(intent);
                            finish();
                            switching_of_vehicle=false;
                        }
                    }






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
                            bleDataPojo.setReadCharacteristic(service.getCharacteristics().get(1).getUuid().toString());
                            bleDataPojo.setWriteCharacteristic(service.getCharacteristics().get(0).getUuid().toString());
                            bleDataPojo.setServiceID(gatt.getServices().get(3).getUuid().toString());
                            realm.insertOrUpdate(bleDataPojo);

                        });
                    } catch (Exception e) {

                        Log.e(EXCEPTION,"ListScan: connect: "+String.valueOf(e));

                    }

                    SharedPreferences sharedPreferencesFinal = getSharedPreferences("FT",Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferencesFinal.edit();
                    editor.remove("first");
                    editor.putInt("first",500);
                    editor.apply();
                    finish();
                }, 500);
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {

                Log.e("conn_state","manually disconnected");
                BikeBleName.setValue("");
                mDeviceAdapter.removeDevice(bleDevice);
                mDeviceAdapter.notifyDataSetChanged();
                userStatus = false;
                staticConnectionStatus = false;
                staticSendData = false;
                connectionStarted = false;

                Intent i = new Intent("status").putExtra("status", staticConnectionStatus);
                sendBroadcast(i);
                getConnectionStatus(staticConnectionStatus, getApplicationContext());

                if ((!onDisconnect && !displayingDisconnectDialog))
                {
                    displayingDisconnectDialog = true;
                    showBluetoothDialog = false;
                    onDisconnect = false;

                    if (!bluetoothadapter.isEnabled()) bluetoothadapter.enable();
                    connect(global_bleDevice);
                }
            }
        });
    }

//    private void update_vehicle_data() {
//        sharedPreferences = getSharedPreferences("vehicle_data",MODE_PRIVATE);
//        String prev_type = sharedPreferences.getString("vehicle_type","");
//
//
//        if (BikeBleName.getValue().charAt(1) == 'A'){
//            type = "Scooter";
//            Model = "Burgman Street";
//            PRICOL_CONNECTED = true;
//        }
//
//        else if (BikeBleName.getValue().charAt(1) == 'B'){
//            PRICOL_CONNECTED=false;
//
//            if (BikeBleName.getValue().charAt(2) == 'S'){
//                type = "Scooter";
//                Model = "Avenis";
//            }
//
//            else if (BikeBleName.getValue().charAt(2) == 'M'){
//                type = "Motorcycle";
//                Model = "V-STROM SX";
//            }
//        }
//
//        if (!prev_type.equals(type)){
//            new Common(DeviceListingScanActivity.this).update_vehicle_data(type,Model,0);
//            Log.e("vehicle_data","updated at devicelistscan 600");
//        }
//    }


    private void update_vehicle_data() {
        sharedPreferences = getSharedPreferences("vehicle_data",MODE_PRIVATE);
        String prev_type = sharedPreferences.getString("vehicle_type","");
        int prev_model=sharedPreferences.getInt("vehicle_model",0);

        if (BikeBleName.getValue().charAt(1) == 'A'){

            //access, burgman
            if (BikeBleName.getValue().charAt(2) == 'S') {

                //Scooter of Pricol
                if (BikeBleName.getValue().charAt(3) == '0' && BikeBleName.getValue().charAt(4) == '1') {
                    type = "Scooter";
                    Model = "Access 125";
                    PRICOL_CONNECTED = true;
                    switchcluter=true;

                }

                else if (BikeBleName.getValue().charAt(3) == '1' && BikeBleName.getValue().charAt(4) == '1') {
                    type = "Scooter";
                    Model = "Burgman Street";
                    PRICOL_CONNECTED = true;

                }

               else if (BikeBleName.getValue().charAt(3) == '0' && BikeBleName.getValue().charAt(4) == '2') {
                    type = "Scooter";
                    Model = "Access 125";
                    PRICOL_CONNECTED = true;

                    switchcluter=true;
                }

                else if (BikeBleName.getValue().charAt(3) == '1' && BikeBleName.getValue().charAt(4) == '2') {
                    type = "Scooter";
                    Model = "Burgman Street";
                    PRICOL_CONNECTED = true;
                    Bundle args= new Bundle();
                    args.putString("access","access");
                  //  DashboardFragment dashboardFragment=new DashboardFragment();
                   // dashboardFragment.putArguments(args);
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
                    Model = "Avenis";
                    Bundle args= new Bundle();
                    args.putString("access","access");
                    //DashboardFragment dashboardFragment=new DashboardFragment();
                    //dashboardFragment.putArguments(args);

                } else if (BikeBleName.getValue().charAt(3) == '3' && BikeBleName.getValue().charAt(4) == '1') {
                    type = "Scooter";
                    Model = "Burgman Street EX";
                    Bundle args= new Bundle();
                    args.putString("access","access");
                  //  DashboardFragment dashboardFragment=new DashboardFragment();
                 //   dashboardFragment.putArguments(args);
                }
            }

            else if (BikeBleName.getValue().charAt(2) == 'M'){
                if (BikeBleName.getValue().charAt(3) == '0' && BikeBleName.getValue().charAt(4) == '1') {
                    type = "Motorcycle";
                    Model = "V-STROM SX";




                }

                else if (BikeBleName.getValue().charAt(3) == '1' && BikeBleName.getValue().charAt(4) == '1') {
                    type = "Motorcycle";
                    Model = "GIXXER / GIXXER SF";

                }
                else if (BikeBleName.getValue().charAt(3) == '2' && BikeBleName.getValue().charAt(4) == '1') {
                    type = "Motorcycle";
                    Model = "GIXXER 250 / GIXXER SF 250";

                }
            }
        }

        if (!prev_type.equals(type)){
            new Common(DeviceListingScanActivity.this).update_vehicle_data(type,Model,0);
            Log.e("vehicle_data","updated at devicelistscan 600");
        }

        if (!String.valueOf(prev_model).equals(Model)){
            new Common(DeviceListingScanActivity.this).update_vehicle_data(type,Model,0);
            Log.e("vehicle_data","updated at devicelistscan 600");
        }
    }

    private void setMtu(BleDevice bleDevice, int mtu) {
        bleManager.setMtu(bleDevice, mtu, new BleMtuChangedCallback() {
            @Override
            public void onSetMTUFailure(BleException exception) {
                Log.i(TAG, "onsetMTUFailure" + exception.toString());
            }

            @Override
            public void onMtuChanged(int mtu) {
                Log.i(TAG, "onMtuChanged: " + mtu);
            }
        });
    }


    @Override
    public final void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        /*if (requestCode == REQUEST_CODE_PERMISSION_LOCATION) {
            if (grantResults.length > 0) {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        onPermissionGranted(permissions[i]);
                    }
                }
            }
        }*/
    }

    private void checkPermissions() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            common.showToast(getResources().getString(R.string.turn_ble), TOAST_DURATION);
            return;
        }

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        List<String> permissionDeniedList = new ArrayList<>();
        for (String permission : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                //onPermissionGranted(permission);
            } else {
                permissionDeniedList.add(permission);
            }
        }

        if (!permissionDeniedList.isEmpty()) {
            String[] deniedPermissions = permissionDeniedList.toArray(new String[permissionDeniedList.size()]);
            ActivityCompat.requestPermissions(this, deniedPermissions, REQUEST_CODE_PERMISSION_LOCATION);
        }
    }

    private boolean checkGPSIsOpen() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) return false;

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_OPEN_GPS && checkGPSIsOpen()) startScan();
    }
}
