package com.suzuki.activity;

import android.Manifest;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.MutableLiveData;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleMtuChangedCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;
import com.suzuki.R;
import com.suzuki.base.BaseActivity;
import com.suzuki.broadcaster.BleConnection;
import com.suzuki.broadcaster.BluetoothCheck;
import com.suzuki.broadcaster.IncomingSms;
import com.suzuki.fragment.DashboardFragment;
import com.suzuki.fragment.MapMainFragment;
import com.suzuki.fragment.MoreFragment;
import com.suzuki.fragment.SettingFragment;
import com.suzuki.pojo.BleDataPojo;
import com.suzuki.pojo.EvenConnectionPojo;
import com.suzuki.pojo.LastParkedLocationRealmModule;
import com.suzuki.preferences.Preferences;
import com.suzuki.services.MyBleService;
import com.suzuki.services.NotificationService;
import com.suzuki.utils.CurrentLoc;
import com.suzuki.utils.DataRequestManager;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import io.realm.Realm;

import static com.suzuki.activity.DeviceListingScanActivity.DEVICESCAN_OBJ;
import static com.suzuki.fragment.DashboardFragment.app;
import static com.suzuki.fragment.DashboardFragment.bluetoothadapter;
import static com.suzuki.fragment.DashboardFragment.displayingDisconnectDialog;
import static com.suzuki.fragment.DashboardFragment.getConnectionStatus;
import static com.suzuki.fragment.DashboardFragment.onDisconnect;
import static com.suzuki.fragment.DashboardFragment.prev_cluster_macAddr;
import static com.suzuki.fragment.DashboardFragment.prev_cluster_name;
import static com.suzuki.fragment.DashboardFragment.readCharacterID;
import static com.suzuki.fragment.DashboardFragment.serviceID;
import static com.suzuki.fragment.DashboardFragment.showBluetoothDialog;
import static com.suzuki.fragment.DashboardFragment.staticConnectionStatus;
import static com.suzuki.fragment.DashboardFragment.staticSendData;
import static com.suzuki.fragment.DashboardFragment.timer;
import static com.suzuki.fragment.DashboardFragment.userStatus;
import static com.suzuki.fragment.DashboardFragment.writeCharacterID;
import static com.suzuki.utils.Common.EXCEPTION;
import static com.suzuki.utils.Common.PRICOL_CONNECTED;
import static com.suzuki.utils.Common.STATUS_PACKET_DELAY;
import static com.suzuki.utils.Common.BikeBleName;
import static com.suzuki.utils.Common.global_bleDevice;

public class HomeScreenActivity extends BaseActivity implements LocationListener, RatingDialogListener {

    BluetoothAdapter mBtAdapter = null, mBluetoothAdapter;
    public static final int TOAST_DURATION = 1000, DELAY_DURATION = 1000; // duration in ms
    public static byte MSG_CLEAR = 0x4E, CALL_CLEAR = 0x4E;
    public static final int PERMISSION_REQUEST_BLUETOOTH_SCAN = 123;
    public static final int PERMISSION_REQUEST_BLUETOOTH_CONNECT = 101;

    private static final int NOTIFICATION_PERMISSION_CODE = 123;
    public static HomeScreenActivity HOME_SCREEN_OBJ;
    public static MyBleService mBoundService;
    public static boolean mServiceBound = false;
    private BleConnection mReceiver;
    public static Double lat, lng;
    Preferences preferences;
    public static UUID[] serviceUuids;
    Intent serviceIntent;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private int RATINGS_SUBMITTED = 987654321;
    public Timer timerService = new Timer();
    private boolean BLE_CONNECTED = false;
    private BroadcastReceiver IncomingSms;
    private BleManager bleManager;
    private boolean connected = false;
    private Realm realm;
    private Handler handler;
    private BluetoothCheck bluetoothCheck;
    private static final int REQUEST_ENABLE_BT = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_screen_activity);

        BikeBleName = new MutableLiveData<>();
        BikeBleName.setValue("");
        //required later
//        Log.e("manufaturer",android.os.Build.MANUFACTURER);
//        startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID)));
        sharedPreferences = getSharedPreferences("BLE_DEVICE", Context.MODE_PRIVATE);
        prev_cluster_name = sharedPreferences.getString("prev_cluster_name", "");
        prev_cluster_macAddr = sharedPreferences.getString("prev_cluster_macAddr", "");

        if (prev_cluster_name.length() > 0 && prev_cluster_name.charAt(1) == 'A')
            PRICOL_CONNECTED = true;
        HOME_SCREEN_OBJ = this;

//        if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_CALL_LOG)!=PackageManager.PERMISSION_GRANTED) {
//            if (Build.VERSION.SDK_INT >= 31) {
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALL_LOG}, 100);
//                return;
//            }
//        }

        {
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
            filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
            this.registerReceiver(BLE_CHECK, filter);
        }

        {
            IncomingSms = new IncomingSms();
            IntentFilter filter = new IntentFilter();
            this.registerReceiver(IncomingSms, filter);
        }


        /**
         * restarting BLE adapter to refresh any connection
         */


            //   BluetoothAdapter.getDefaultAdapter().enable();
//
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                if (Build.VERSION.SDK_INT >= 31) {
//                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 100);
//                    return;
//                }
//            } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
//                if (Build.VERSION.SDK_INT >= 31) {
//                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 100);
//                    return;
//                }
//            }
        {
            if(Build.VERSION.SDK_INT >= 31){
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
                {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_REQUEST_BLUETOOTH_CONNECT);

                    return;
                }
                else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    if (Build.VERSION.SDK_INT >= 31) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, PERMISSION_REQUEST_BLUETOOTH_SCAN);
                    }

                    return;
                }
                else {
                    BluetoothAdapter.getDefaultAdapter().enable();
                }
            }else{
                BluetoothAdapter.getDefaultAdapter().enable();

            }

        }



        /**
         * screen turn on logic
         */
        {

            timerService.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    Log.e("mainscreen", "turnonscreen");
                    turn_on_screen();
                }
            }, 0, 30000);
        }


        /**
         * logic to start user ratings dialog:
         * Starts 3rd time
         * starts 3rd time after later
         * Starts next time after cancel
         * starts playstore if 4 or more star and submit
         * opens comment box if 3 or less star and submit
         * opens next time if else
         */
        {
            sharedPreferences = getSharedPreferences("HOMESCREEN", MODE_PRIVATE);
            editor = sharedPreferences.edit();

            try {
                int START_COUNT = sharedPreferences.getInt("START_COUNT", 0);

                if (START_COUNT != RATINGS_SUBMITTED) {
                    if (START_COUNT >= 3 || !sharedPreferences.getBoolean("EXIT_STATUS", true))
                        showDialog(5, false);
                    else {
                        editor.putInt("START_COUNT", ++START_COUNT);
                        editor.commit();
                    }
                }

            } catch (Exception e) {

                Log.e(EXCEPTION, getClass().getName() + " oncreate " + String.valueOf(e));

            }

            editor.putBoolean("EXIT_STATUS", false);
            editor.commit();
        }


        preferences = new Preferences(this);

        MeowBottomNavigation.Model modeeeee = new MeowBottomNavigation.Model(1, R.drawable.home_blue);

        preferences.clearSharePreference();
        MeowBottomNavigation bottomNavigation = findViewById(R.id.bottomNavigation);

        bottomNavigation.add(modeeeee);

        bottomNavigation.add(new MeowBottomNavigation.Model(2, R.drawable.settings_blue));
        bottomNavigation.add(new MeowBottomNavigation.Model(3, R.drawable.map_blue));
        bottomNavigation.add(new MeowBottomNavigation.Model(4, R.drawable.more_blue));
        bottomNavigation.show(1, true);

        loadFragment(new DashboardFragment());

        bottomNavigation.setOnShowListener(model -> null);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        bottomNavigation.setOnClickMenuListener(model -> {

            if (model.getId() == 1) loadFragment(new DashboardFragment());
            else if (model.getId() == 2) loadFragment(new SettingFragment());
            else if (model.getId() == 3) loadFragment(new MapMainFragment());
            else if (model.getId() == 4) loadFragment(new MoreFragment());
            return null;

        });

        viewRecord();

        setBluetoothStatus();

        serviceIntent = new Intent(this, MyBleService.class);
        try {

            startService(serviceIntent);

        } catch (Exception e) {

            Log.d(EXCEPTION, "HomescreenActivity_Oncreate_startMyBLEservice: exception: " + e);
        }
        try {
            bindService(serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        } catch (Exception e) {
            Log.d(EXCEPTION, "HomescreenActivity_Oncreate_bindservice: exception: " + e);
        }

        Intent mServiceIntent = new Intent(this, NotificationService.class);

        try {
            startService(mServiceIntent);

        } catch (Exception e) {
            Log.d(EXCEPTION, "HomescreenActivity_Oncreate_startService: exception: " + e);
        }

//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            Intent intent = new Intent();
//            String packageName = getPackageName();
//            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
//            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
//                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
//                intent.setData(Uri.parse("package:" + packageName));
//                startActivity(intent);
//            }
//        }

        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, 66);

        if (!check_call_permission()) showExitAlert(getString(R.string.request));


        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) BikeBleName.setValue("");
            }
        }, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    }

    private void turn_on_screen() {
        PowerManager.WakeLock screenLock = null;
        if ((getSystemService(POWER_SERVICE)) != null) {
            screenLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "mainscreen:partial_wake");
            screenLock.acquire();

            scan();
            screenLock.release();
        }
    }

    private boolean check_call_permission() {


        String permission = Manifest.permission.READ_CALL_LOG;
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_BLUETOOTH_SCAN:
            {
              //  if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
                else{

                    startActivity(
                            new Intent(
                                    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", getPackageName(), null)
                            )
                    );
                    }
            }
            break;

            case PERMISSION_REQUEST_BLUETOOTH_CONNECT: {
              //  if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
                else{
                    startActivity(
                            new Intent(
                                    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", getPackageName(), null)
                            )
                    );
                }
                break;
            }
        }
    }

    private void scan() {
        if (BikeBleName != null && (BikeBleName.getValue() == null || BikeBleName.getValue().isEmpty())) {
            bleManager = BleManager.getInstance();
            bleManager.init(getApplication());
            bleManager.scan(new BleScanCallback() {
                @Override
                public void onScanStarted(boolean success) {
                    Log.e("conn_state", "onscan");
                    connected = false;
                }

                @Override
                public void onScanning(BleDevice bleDevice) {
                    Log.e("Onconnect_success", "lookingfor: " + prev_cluster_name);
                    if (prev_cluster_name != null) {
                        Log.e("conn_state", String.valueOf(bleDevice.getName()));
                        if (prev_cluster_name.equals(bleDevice.getName())) {
                            Log.e("conn_state", "cluster found");
                            //bleManager.cancelScan();

                            /*bleManager.connect(bleDevice, new BleGattCallback() {
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
                                    Log.e("conn_state_final","connect success: home");
                                    setMtu(bleDevice, 250);
                                    app.setBleDevice(bleDevice);
                                    BikeBleName.setValue(prev_cluster_name);
                                    //loadFragment(new DashboardFragment());
                                    connected = true;

                                    mBoundService.getServicesList(bleDevice);
                                    BluetoothGattService service = gatt.getServices().get(3);
                                    try {

                                        realm = Realm.getDefaultInstance();
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
                                }

                                @Override
                                public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                                    Log.e("conn_state","disconnected");
                                    loadFragment(new DashboardFragment());
                                    BikeBleName.setValue("");
                                }
                            });*/
                        }
                    }
                }

                @Override
                public void onScanFinished(List<BleDevice> scanResultList) {
                    Log.e("conn_state", "scan completed");
                    scan();
                }
            });
        }
    }

    public final BleGattCallback mGattCallback1 = new BleGattCallback() {
        @Override
        public void onStartConnect() {
            Log.e("Onconnect_success", "mGattCallback1");
//            connectionStarted = true;
            onDisconnect = false;
        }

        @Override
        public void onConnectFail(BleDevice bleDevice, BleException exception) {
//            connectionStarted = false;
            onDisconnect = false;
        }

        @Override
        public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {

            Log.e("conn_state", "connect success: 335 home");

            BikeBleName.setValue(prev_cluster_name);
            boolean isConnected = BleManager.getInstance().isConnected(bleDevice);
            onDisconnect = false;

            if (isConnected) {

                setMtu(bleDevice, 250);
                app.setBleDevice(bleDevice);

                LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(HomeScreenActivity.this);
                localBroadcastManager.sendBroadcast(new Intent("closeDialogActivity"));

                try {
                    DEVICESCAN_OBJ.finish();
                } catch (Exception e) {
                    Log.e(EXCEPTION, getClass().getName() + " onConnectSuccess " + String.valueOf(e));
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Log.e(EXCEPTION, getClass().getName() + " onConnectSuccess_2 " + String.valueOf(e));
                }
            }

            if (mBoundService != null) mBoundService.getServicesList(bleDevice);
        }

        public void connect(final BleDevice bleDevice) {

            new BleManager().connect(bleDevice, new BleGattCallback() {
                @Override
                public void onStartConnect() {
                }

                @Override
                public void onConnectFail(BleDevice bleDevice, BleException exception) {
                    Log.d("BLEservice", "-- " + exception.toString());
                }

                @Override
                public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                    Log.e("conn_state", "connect success: 384 my ble serv");
                    boolean isConnected = new BleManager().isConnected(bleDevice);

                    if (isConnected) {
                        Log.e("connectcheck", "bleservice 383");
                        setMtu(bleDevice, 250);
                        app.setBleDevice(bleDevice);

                        staticConnectionStatus = true;
                        staticSendData = true;

                        Intent i = new Intent("status").putExtra("status", staticConnectionStatus);
                        sendBroadcast(i);

                        getConnectionStatus(staticConnectionStatus, getApplicationContext());

//                        prev_cluster = bleDevice.getMac();
//                        Log.e("test1","BLE: "+ String.valueOf(prev_cluster));
//                        BikeBleName = bleDevice.getName(); Log.e("bikeble","ADDED mybleservice 393: "+String.valueOf(BikeBleName));
//                        if(BikeBleName==null){
//                            BikeBleName="";Log.e("bikeble","cleared mybleservice 396");
//                        }
                        //if (BikeBleName != null) setLastConnectedVehicleName(BikeBleName);
                    }
                }

                @Override
                public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) {
                }
            });
        }

        @Override
        public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
            Log.e("MYBLE", "disconnected");
//            connectionStarted = false;
            staticConnectionStatus = false;
            staticSendData = false;

            connect(global_bleDevice);

            Intent i = new Intent("status").putExtra("status", staticConnectionStatus);
            sendBroadcast(i);
            Log.e("getconnectionstatus", "myble_5");
            getConnectionStatus(staticConnectionStatus, getApplicationContext());
            userStatus = false;
            if (!onDisconnect && !displayingDisconnectDialog) {
                displayingDisconnectDialog = true;
                showBluetoothDialog = false;
                onDisconnect = false;
                Intent intent;
                if (bluetoothadapter.isEnabled()) {
                    connect(global_bleDevice);
                    //intent = new Intent(getApplicationContext(), OnDisconnect.class);
                } else {
                    showBluetoothDialog = false;
                    timerService.cancel();
                    timerService.purge();
                    intent = new Intent(getApplicationContext(), BluetoothDisabledActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(EXCEPTION, getClass().getName() + " onDisConnected " + String.valueOf(e));
                    }
                }
            }
        }
    };

    private void setMtu(BleDevice bleDevice, int mtu) {
        bleManager.setMtu(bleDevice, mtu, new BleMtuChangedCallback() {
            @Override
            public void onSetMTUFailure(BleException exception) {
                Log.e("conn_state", "mtu error");
            }

            @Override
            public void onMtuChanged(int mtu) {
                Log.e("conn_state", "mtu success");
            }
        });
    }


    private void showDialog(int Stars, boolean DisplayCommentBox) {

        new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNeutralButtonText("Later")
                .setNoteDescriptions(Arrays.asList("Very Bad", "Not good", "Quite ok", "Very Good", "Excellent !!!"))
                .setDefaultRating(Stars)
                .setTitle("Suzuki Ride Connect")
                .setDescription("Please Rate Your Experience With The Application")
                .setCommentInputEnabled(DisplayCommentBox)
                .setStarColor(R.color.yellow)
                .setNoteDescriptionTextColor(R.color.black)
                .setTitleTextColor(R.color.black)
                .setDescriptionTextColor(R.color.black)
                .setHint("Please write your comment here ...")
                .setHintTextColor(R.color.grey)
                .setCommentTextColor(R.color.blue)
                .setCommentBackgroundColor(R.color.lightGrey)
                .setWindowAnimation(R.style.MyDialogFadeAnimation)
                .setCancelable(false)
                .setCanceledOnTouchOutside(false)
                .create(HomeScreenActivity.this)
                .show();
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_frame, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {

        BluetoothAdapter bluetoothadapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothadapter.isEnabled()) showBluetoothSwitchOFFAlert();
        else showExitAlert(getString(R.string.exit_app));
    }

    public void showBluetoothSwitchOFFAlert() {
        Dialog dialog = new Dialog(HomeScreenActivity.this, R.style.custom_dialog);
        dialog.setContentView(R.layout.custom_dialog);

        TextView tvAlertText = dialog.findViewById(R.id.tvAlertText);
        tvAlertText.setText("Are you sure you want to switch off the Bluetooth?");
        ImageView ivCross = dialog.findViewById(R.id.ivCross);

        ivCross.setOnClickListener(v -> {
            dialog.cancel();
            showExitAlert(getString(R.string.exit_app));
        });

        ImageView ivCheck = dialog.findViewById(R.id.ivCheck);

        ivCheck.setOnClickListener(v -> {
            dialog.cancel();

            // if (mBluetoothAdapter.isEnabled()) mBluetoothAdapter.disable();
            if(Build.VERSION.SDK_INT >= 31){
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    if (Build.VERSION.SDK_INT >= 31) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_REQUEST_BLUETOOTH_CONNECT);
                    }

                    return;
                }
                else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    if (Build.VERSION.SDK_INT >= 31) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, PERMISSION_REQUEST_BLUETOOTH_SCAN);
                    }

                    return;
                }
                else{
                    //if (mBluetoothAdapter.isEnabled()) mBluetoothAdapter.disable();
                    //BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
                        if (Build.VERSION.SDK_INT >= 32) {
                            //DataRequestManager.isBluetoohDisabled=true;
                            Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                            startActivity(intent);
                        } else {
                            //DataRequestManager.isBluetoohDisabled=true;
                            mBluetoothAdapter.disable();
                        }
                    }

                }
            }else{
                //if (mBluetoothAdapter.isEnabled()) mBluetoothAdapter.disable();
                //BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
                    if (Build.VERSION.SDK_INT >= 32) {
                        //DataRequestManager.isBluetoohDisabled=true;
                        Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                        startActivity(intent);
                    } else {
                        //DataRequestManager.isBluetoohDisabled=true;
                        mBluetoothAdapter.disable();
                    }
                }
            }



//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                if (Build.VERSION.SDK_INT >= 31) {
//                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 100);
//                    return;
//                }
//            } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
//                if (Build.VERSION.SDK_INT >= 31) {
//                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 100);
//                    return;
//                }
//            } else {
//                if (mBluetoothAdapter.isEnabled()) mBluetoothAdapter.disable();
//            }

            showExitAlert(getString(R.string.exit_app));
        });
        dialog.show();
    }

    private final BroadcastReceiver BLE_CHECK = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(intent.getAction()))
                BLE_CONNECTED = true;
            else BLE_CONNECTED = false;
        }
    };


    public void showExitAlert(String messsage) {
        Dialog dialog = new Dialog(HomeScreenActivity.this, R.style.custom_dialog);
        dialog.setContentView(R.layout.custom_dialog);
        TextView tvAlertText = dialog.findViewById(R.id.tvAlertText);
        tvAlertText.setText(messsage);
        ImageView ivCross = dialog.findViewById(R.id.ivCross);
        ivCross.setOnClickListener(v -> dialog.cancel());

        ImageView ivCheck = dialog.findViewById(R.id.ivCheck);

        ivCheck.setOnClickListener(v -> {

            dialog.cancel();

            if (messsage.equals(getString(R.string.exit_app))) {
                if(editor !=null) {
                    editor.putBoolean("EXIT_STATUS", true);
                    editor.commit();
                }
                setBluetoothStatusExit();
                HomeScreenActivity.this.finish();
            }

        });
        dialog.show();
    }


    @Override
    public void onResume() {
        super.onResume();
        STATUS_PACKET_DELAY = 5000;
        if(Build.VERSION.SDK_INT >= 31){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_REQUEST_BLUETOOTH_CONNECT);

            }
            else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, PERMISSION_REQUEST_BLUETOOTH_SCAN);

            }
            else{
                BluetoothAdapter.getDefaultAdapter().enable();
            }
        }
        else{
            BluetoothAdapter.getDefaultAdapter().enable();
        }


//        if (ContextCompat.checkSelfPermission(this,Manifest.permission.BLUETOOTH_CONNECT)!=PackageManager.PERMISSION_GRANTED) {
//            if (Build.VERSION.SDK_INT >= 31) {
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 100);
//            }
//        }else  if (ContextCompat.checkSelfPermission(this,Manifest.permission.BLUETOOTH_SCAN)!=PackageManager.PERMISSION_GRANTED) {
//            if (Build.VERSION.SDK_INT >= 31) {
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 100);
//            }
//        }
//        else{
//            BluetoothAdapter.getDefaultAdapter().enable();
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        setBluetoothStatusExit();
        if (editor != null) {
            editor.putBoolean("EXIT_STATUS", true);
            editor.commit();
        }
        try {
            if (serviceIntent != null) stopService(serviceIntent);

        } catch (Exception ignored){}

        try {
            unbindService(mServiceConnection);
        }
        catch (Exception e) {
        }

        BleManager.getInstance().disconnectAllDevice();

        BleManager.getInstance().destroy();

        if (timer != null) {

            timer.cancel();
            timer.purge();
            timer = null;
        }

        if (mReceiver != null) unregisterReceiver(mReceiver);

        if (preferences != null) {

        preferences.clearSharePreference();}

        System.exit(0);
    }

    @Override
    public void onLocationChanged(Location location) {

        lat = location.getLatitude();
        lng = location.getLongitude();
    }

    @Override
    public void onStatusChanged (String provider, int status, Bundle extras) { }

    @Override
    public void onProviderEnabled(String provider) { }

    @Override
    public void onProviderDisabled(String provider) { }

    @Override
    public void onNegativeButtonClicked() { }

    @Override
    public void onNeutralButtonClicked() {
        editor.putInt("START_COUNT",1);
        editor.commit();
    }

    @Override
    public void onPositiveButtonClicked(int i, @NotNull String s) {

        /**
         * if the ratings are 3 or less and no comments then make the comment box visible
         * else if the ratings are 3 or less and comment is found then open mail and set flag for ratings submitted
         * else open playstore to submit ratings and set flag for the same
         */
        if(i<=3 && s.equals("")) {
            Toast.makeText(HOME_SCREEN_OBJ, "Please write your comment.", Toast.LENGTH_SHORT).show();
            showDialog(i,true);
        }
        else if(i<=3 && !s.equals("")){
            Toast.makeText(HOME_SCREEN_OBJ, "Thanks For Your Feedback!", Toast.LENGTH_SHORT).show();

            editor.putInt("START_COUNT", RATINGS_SUBMITTED);
            editor.commit();

            open_mail(s);
        } else{
            editor.putInt("START_COUNT", RATINGS_SUBMITTED);
            editor.commit();

            this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id="+this.getPackageName())));
        }
    }

    private void open_mail(String feedback) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"customer.queries@suzukimotorcycle.in"});
        i.putExtra(Intent.EXTRA_CC, new String[]{"smiplconnected2020@gmail.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, "User feedback");
        i.putExtra(Intent.EXTRA_TEXT   , "Dear Suzuki Motorcycles India Pvt Ltd,\n\n"+
                "My Vehicle's Cluster ID : "+sharedPreferences.getString("CLUSTER_NAME","never scanned")+
                "\n My Phone Model: "+getDeviceName()+
                "\n Rating:" +
                "\n My Feedback: "+feedback);
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) return model;

        return manufacturer + " " + model;
    }

    /*lass NotificationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String temp = intent.getStringExtra("notification_event");
        }
    }*/

    @Override
    protected void onPause() {
        super.onPause();
        STATUS_PACKET_DELAY=2000;
    }

    /*@RequiresApi(api = Build.VERSION_CODES.M)
    private void request_notification_access_new() {
        NotificationManager nm = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        if (!nm.isNotificationPolicyAccessGranted()) startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void request_notification_access() {
        String permission = Manifest.permission.ACCESS_NOTIFICATION_POLICY;
        int res = this.checkCallingOrSelfPermission(permission);
        if(res != PackageManager.PERMISSION_GRANTED){
            String[] perms = {"android.permission.ACCESS_NOTIFICATION_POLICY"};
            requestPermissions(perms, 0);
        }
    }*/
    //    @Override
//    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
//
//        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
//
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                //Displaying a toast
//                Log.d("permission", "grant");
////                Toast.makeText(this, "Permission granted now you can read the storage", Toast.LENGTH_LONG).show();
//            } else {
//                //Displaying another toast if permission is not granted
//                Log.d("permission", "deny");
////                Toast.makeText(this, "Oops you just denied the permission", Toast.LENGTH_LONG).show();
//            }
//        }
//        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
//    }

    public void addLastParkedLatLong() {
        try {

        if (lat==null&&lng==null) {

            Location location = new CurrentLoc().getCurrentLoc(this);

            if (location != null) {
                lat = location.getLatitude();
                lng = location.getLongitude();
            }
        }

        if (lat != null && lng != null) {
            Realm realm = Realm.getDefaultInstance();
            try {
                realm.executeTransaction(realm1 -> {

                    LastParkedLocationRealmModule lastParkedLocationRealmModule = realm1.where(LastParkedLocationRealmModule.class).equalTo("id", 1).findFirst();

                    if (lastParkedLocationRealmModule == null) {

                        lastParkedLocationRealmModule = realm1.createObject(LastParkedLocationRealmModule.class, 1);
                        lastParkedLocationRealmModule.setLat(lat);
                        lastParkedLocationRealmModule.setLng(lng);

                        realm1.insertOrUpdate(lastParkedLocationRealmModule);
                    } else if (lastParkedLocationRealmModule != null) {
                        lastParkedLocationRealmModule.setLat(lat);
                        lastParkedLocationRealmModule.setLng(lng);

                        realm1.insertOrUpdate(lastParkedLocationRealmModule);
                    }
                });

            } catch (Exception e) {
                Log.e(EXCEPTION,getClass().getName()+" addLastParkedLatLong "+String.valueOf(e));
            }
        }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 66) {
            PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
            boolean isIgnoringBatteryOptimizations = pm.isIgnoringBatteryOptimizations(getPackageName());
            if(isIgnoringBatteryOptimizations){
                Log.e("battery_check","worked");
                // Ignoring battery optimization
            }else{
                Log.e("battery_check","sdkfjn_worked");
                // Not ignoring battery optimization
            }
        }
    }

    public ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            MyBleService.MyBinder myBinder = (MyBleService.MyBinder) service;
            mBoundService = myBinder.getService();
            mServiceBound = true;
        }
    };

    public void setBluetoothStatus() {

        IntentFilter intentFilter = new IntentFilter("status");

        mReceiver = new BleConnection() {

            @Override
            public void onReceive(Context context, Intent intent) {

                Boolean status = intent.getExtras().getBoolean("status");


                DataRequestManager.isBluetoothConnected = status;

                if (!status) addLastParkedLatLong();
               // else addLastParkedLatLong();
            }
        };

        registerReceiver(mReceiver, intentFilter);
    }

    public void setBluetoothStatusExit() {


        if(DataRequestManager.isBluetoothConnected){
            try {
                addLastParkedLatLong();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    public void viewRecord() {

        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(realm1 -> {

                BleDataPojo bleDataPojo = realm1.where(BleDataPojo.class).equalTo("bleId", 1).findFirst();

                if (bleDataPojo != null) {

//                    prev_cluster = bleDataPojo.getDeviceMacAddress();
//                    if(BikeBleName.equals("")) BikeBleName = bleDataPojo.getDeviceName(); Log.e("bikeble","ADDED home 641: "+String.valueOf(BikeBleName));

                    serviceID = bleDataPojo.getServiceID();
                    writeCharacterID = bleDataPojo.getWriteCharacteristic();
                    readCharacterID = bleDataPojo.getReadCharacteristic();

                    UUID uuid1 = UUID.fromString(serviceID);
                    UUID uuid2 = UUID.fromString(writeCharacterID);
                    UUID uuid3 = UUID.fromString(readCharacterID);
                    serviceUuids = new UUID[]{uuid1, uuid2, uuid3};
                }
            });
        } catch (Exception e) {
            Log.e(EXCEPTION,getClass().getName()+" viewRecord "+String.valueOf(e));
        }
    }
}