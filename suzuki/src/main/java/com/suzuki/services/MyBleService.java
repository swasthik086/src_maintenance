package com.suzuki.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleMtuChangedCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.suzuki.R;
import com.suzuki.activity.BluetoothDisabledActivity;
import com.suzuki.activity.DeviceListingScanActivity;
import com.suzuki.pojo.ClusterStatusPktPojo;
import com.suzuki.utils.Common;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.suzuki.activity.DeviceListingScanActivity.DEVICESCAN_OBJ;
import static com.suzuki.activity.DeviceListingScanActivity.IsManualConnection;
import static com.suzuki.activity.HomeScreenActivity.mBoundService;
import static com.suzuki.application.SuzukiApplication.bleDevice;
import static com.suzuki.fragment.DashboardFragment.BLE_CONNECTED;
import static com.suzuki.fragment.DashboardFragment.app;
import static com.suzuki.fragment.DashboardFragment.bluetoothadapter;
import static com.suzuki.fragment.DashboardFragment.displayingDisconnectDialog;
import static com.suzuki.fragment.DashboardFragment.getConnectionStatus;
import static com.suzuki.fragment.DashboardFragment.onDisconnect;
import static com.suzuki.fragment.DashboardFragment.prev_cluster_macAddr;
import static com.suzuki.fragment.DashboardFragment.prev_cluster_name;
import static com.suzuki.fragment.DashboardFragment.showBluetoothDialog;
import static com.suzuki.fragment.DashboardFragment.staticConnectionStatus;
import static com.suzuki.fragment.DashboardFragment.staticSendData;
import static com.suzuki.fragment.DashboardFragment.userStatus;
import static com.suzuki.utils.Common.BLE_ENABLED;
import static com.suzuki.utils.Common.EXCEPTION;
import static com.suzuki.utils.Common.BikeBleName;
import static com.suzuki.utils.Common.FIRST_TIME;
import static com.suzuki.utils.Common.PRICOL_CONNECTED;
import static com.suzuki.utils.Common.global_bleDevice;

public class MyBleService extends Service {
    private static String TAG = MyBleService.class.getSimpleName();
    private IBinder mBinder = new MyBinder();

    public Runnable runnable;
    public Timer timerService = new Timer();
    public static boolean connectionStarted = false;
    private static String CHANNEL_ID = "serviceChannelId";
    public static final String NOTIFICATION_NAME = "foreground_service";
    private Handler handler=new Handler();
    private String type, Model;

    //TEMP DEACTIVATED
    //public BluetoothGatt mBluetoothGatt;

    @Override  /*auto connect kns*/
    public void onCreate() {
        try {
            Log.e("bleservice","checking");
            timerService.scheduleAtFixedRate(new TimerTask() {

                @Override
                public void run() {
                                                     /*SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("FT",getApplicationContext().MODE_PRIVATE);

                                                     //MACID = "24:9F:89:B0:AD:7B";
                                                     if (MACID.isEmpty()){
                                                         Log.e("macidtest","checked");
                                                         MACID = sharedPreferences.getString("MACID","test");
                                                     }*/
                    //MACID = "C4:64:E3:1F:71:87";
//                                                     BleManager.getInstance().connect(prev_cluster_macAddr, mGattCallback1);

                    Log.e("Onconnect_success","run: "+String.valueOf(prev_cluster_name));
                    if (showBluetoothDialog && !displayingDisconnectDialog) {
                        Log.e("ble_state_check","c1 ok");
                        if (!bluetoothadapter.isEnabled()) {
                            BLE_ENABLED = false;
                            Log.e("ble_state_check","c2 ok");
                            showBluetoothDialog = false;
                            timerService.cancel();
                            timerService.purge();

                            connectionStarted = false;
                            staticConnectionStatus = false;
                            staticSendData = false;
                            Intent i = new Intent("status").putExtra("status", staticConnectionStatus);
                            sendBroadcast(i);
                            Log.e("getconnectionstatus","myble_1");
                            getConnectionStatus(staticConnectionStatus, getApplicationContext());
                            userStatus = false;

                            bluetoothadapter.enable();
                        } else BLE_ENABLED = true;
                    }

                    if (!connectionStarted && !staticConnectionStatus && !IsManualConnection && prev_cluster_macAddr != null && !prev_cluster_macAddr.equals("") ) {
                        Log.e("ble_state_check","c3 ok");
                        try {
                            connectionStarted = true;
                            BleManager.getInstance().getBluetoothAdapter().enable();
                            BleManager.getInstance().connect(prev_cluster_macAddr, mGattCallback1);

                                                             /*if (!connectionStarted) {
                                                                 Log.e("ble_state_check","c4 ok");
                                                                 if (BleManager.getInstance().getBluetoothAdapter().isEnabled()) {
                                                                     Log.e("ble_state_check","c5 ok");
                                                                     connectionStarted = true;

                                                                     if (!prev_cluster_macAddr.equals("")){
                                                                         Log.e("ble_state_check","connect success my ble");
                                                                         BleManager.getInstance().connect(prev_cluster_macAddr, mGattCallback1);
                                                                     }
                                                                 }
                                                                 *//*Handler handler = new Handler(Looper.getMainLooper());
                                                                 handler.post(() -> {

                                                                     if (BleManager.getInstance().getBluetoothAdapter().isEnabled()) {
                                                                         Log.e("ble_state_check","c5 ok");
                                                                         connectionStarted = true;

                                                                         if (!prev_cluster_macAddr.equals("")){
                                                                             Log.e("conn_state","connect success my ble");
                                                                             BleManager.getInstance().connect(prev_cluster_macAddr, mGattCallback1);
                                                                         }
                                                                     }
                                                                 });*//*
                                                             }else Log.e("test1","MAC_ID_NULL");
*/
                        } catch (Exception e) {
                            Log.e(EXCEPTION,"MyBleService: MACID"+String.valueOf(e));
                        }
                    }

                    // to dispose the dialog in android 10 devices
                    if (displayingDisconnectDialog && staticConnectionStatus) {
                        Log.e("ble_state_check","c6 ok");
                        connectionStarted = false;
                        Intent i = new Intent("disconnect_status").putExtra("disconnect_status", staticConnectionStatus);
                        sendBroadcast(i);
                    }
                }
            },0,10000);
        } catch (Exception e) {
            Log.e("ble_state_check","exception");
            Log.e(EXCEPTION,"MyBleService: MACID_main"+String.valueOf(e));
        }
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startForegroundService();

        return START_STICKY;
//        return super.onStartCommand(intent, flags, startId);

    }

    private void startForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) createNotificationChannel(notificationManager);
        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setAutoCancel(false)
                .setContentTitle("Suzuki Ride Connect is running")
                .setSmallIcon(R.drawable.suzuki_logo)
                .setColor(Color.TRANSPARENT)
                .build();
        startForeground(1,notification);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel(NotificationManager notificationManager) {
        NotificationChannel notificationChannel = new NotificationChannel(
                CHANNEL_ID,NOTIFICATION_NAME,NotificationManager.IMPORTANCE_HIGH
        );
        notificationManager.createNotificationChannel(notificationChannel);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public class MyBinder extends Binder {
        public MyBleService getService() {
            return MyBleService.this;
        }
    }

    private void setMtu(BleDevice bleDevice, int mtu) {
        BleManager.getInstance().setMtu(bleDevice, mtu, new BleMtuChangedCallback() {
            @Override
            public void onSetMTUFailure(BleException exception) { }

            @Override
            public void onMtuChanged(int mtu) { }
        });
    }

    public void writeDataFromAPPtoDevice(byte[] PKT, int Flag) {
        Log.e("test_Packet",String.valueOf(Flag));

        String str = new String(PKT);
        Log.e("Data Packet",str);

        if (Flag==36){
            for (int i=0; i<PKT.length; i++){

                Log.e("transmitted packet",String.valueOf(PKT[i]));
            }
        }
        try {
            BluetoothGattCharacteristic characteristic = app.getBluetoothGattService().getCharacteristics().get(0);

            BleManager.getInstance().write(
                    bleDevice,
                    characteristic.getService().getUuid().toString(),
                    characteristic.getUuid().toString(),
                    PKT, false,false,150,
                    new BleWriteCallback() {

                        @Override
                        public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {
                            Log.e("transmission","SUCCESS: "+ String.valueOf(Flag));
                        }

                        @Override
                        public void onWriteFailure(final BleException exception) {
                            Log.e("transmission","error: "+exception.toString());
                        }
                    });

        } catch (Exception e) {
            Log.e(EXCEPTION,getClass().getName()+" writeDataFromAPPtoDevice "+String.valueOf(e));
        }
    }

    public void getServicesList(BleDevice bleDevice) {

        BluetoothGatt gatt = BleManager.getInstance().getBluetoothGatt(bleDevice);
        try {

            BluetoothGattService service = gatt.getServices().get(3);
            app.setBluetoothGattService(service);
            BluetoothGattCharacteristic characteristic = service.getCharacteristics().get(1);

            app.setCharacteristic(characteristic);
            app.setCharaProp(1);

//            final int charaProp = app.getCharaProp();

            BleManager.getInstance().notify(
                    bleDevice,
                    characteristic.getService().getUuid().toString(),
                    characteristic.getUuid().toString(),
                    new BleNotifyCallback() {

                        @Override
                        public void onNotifySuccess() {

//                            BikeBleName = bleDevice.getName(); Log.e("bikeble","ADDED bleservice 273:"+String.valueOf(BikeBleName));


//                            if(BikeBleName==null) BikeBleName=""; Log.e("bikeble","cleared bleservice 276");

                            showBluetoothDialog = true;
                            connectionStarted = true;
                            onDisconnect = false;

                            staticConnectionStatus = true;
                            staticSendData = true;
//                            logData(" Connection notified success ");
                            Intent i = new Intent("status").putExtra("status", staticConnectionStatus);
                            sendBroadcast(i);
                            getConnectionStatus(staticConnectionStatus, getApplicationContext());


                            //temp deactivated
                            //new Handler().postDelayed(() -> saveToCluster(bleDevice.getName()), 3000);
                        }

                        @Override
                        public void onNotifyFailure(final BleException exception) {
                            connectionStarted = false;
                            onDisconnect = false;
                            staticSendData = false;
                        }

                        @Override
                        public void onCharacteristicChanged(byte[] data) {
                            byte[] charValue = characteristic.getValue();

                            EventBus.getDefault().post(new ClusterStatusPktPojo(new String(charValue), data));
                        }
                    });
        } catch (Exception e) {
            Log.e(EXCEPTION,getClass().getName()+" getServicesList: "+ String.valueOf(e));
        }
    }

    public final BleGattCallback mGattCallback1 = new BleGattCallback() {
        @Override
        public void onStartConnect() {
            Log.e("Onconnect_success","mGattCallback1");
            connectionStarted = true;
            onDisconnect = false;
        }

        @Override
        public void onConnectFail(BleDevice bleDevice, BleException exception) {
            connectionStarted = false;
            onDisconnect = false;
        }

        @Override
        public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {

            Log.e("conn_state_final","connect success: 335 my ble serv");
            BikeBleName.setValue(prev_cluster_name);
            SharedPreferences sharedPreferencesFinal = getSharedPreferences("BLE_DEVICE",Context.MODE_PRIVATE);
            if (sharedPreferencesFinal.getString("prev_cluster","").equals(BikeBleName.getValue())){
                FIRST_TIME = false;
            }else FIRST_TIME = true;

            /*if (BikeBleName.getValue().charAt(1) == 'A'){
                type = "Scooter";
                Model = "Burgman Street";
                PRICOL_CONNECTED = true;

            } else if (BikeBleName.getValue().charAt(1) == 'B'){
                PRICOL_CONNECTED = false;
                if (BikeBleName.getValue().charAt(2) == 'S'){
                    type = "Scooter";
                    Model = "Avenis";

                } else if (BikeBleName.getValue().charAt(2) == 'M'){
                    type = "Motorcycle";
                    Model = "V-STORM SX";
                }
            }*/

//            new Common(getApplicationContext()).update_vehicle_data(type,Model,0);

            boolean isConnected = BleManager.getInstance().isConnected(bleDevice);
            onDisconnect = false;

            if (isConnected) {

                setMtu(bleDevice, 250);
                app.setBleDevice(bleDevice);

                LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(MyBleService.this);
                localBroadcastManager.sendBroadcast(new Intent("closeDialogActivity"));

                try{
                    DEVICESCAN_OBJ.finish();
                }catch (Exception e){
                    Log.e(EXCEPTION,getClass().getName()+" onConnectSuccess "+String.valueOf(e));
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Log.e(EXCEPTION,getClass().getName()+" onConnectSuccess_2 "+String.valueOf(e));
                }
            }

            if(mBoundService!=null) mBoundService.getServicesList(bleDevice);
        }

        public void connect(final BleDevice bleDevice) {

            if (handler != null) {
                try {
                    handler.removeCallbacks(runnable);
                } catch (Exception e) {
                    Log.e(EXCEPTION,getClass().getName()+" connect "+String.valueOf(e));
                }
            }

            new BleManager().connect(bleDevice, new BleGattCallback() {
                @Override
                public void onStartConnect() { }

                @Override
                public void onConnectFail(BleDevice bleDevice, BleException exception) {
                    Log.d("BLEservice", "-- " + exception.toString());
                }

                @Override
                public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                    Log.e("conn_state_final","connect success: 384 my ble serv");
                    boolean isConnected = new BleManager().isConnected(bleDevice);

                    if (isConnected) {
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
                public void onDisConnected(boolean isActiveDisConnected, BleDevice bleDevice, BluetoothGatt gatt, int status) { }
            });
        }

        @Override
        public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
            Log.e("MYBLE","disconnected");
            BikeBleName.setValue("");

            connectionStarted = false;
            staticConnectionStatus = false;
            staticSendData = false;

            connect(global_bleDevice);

            Intent i = new Intent("status").putExtra("status", staticConnectionStatus);
            sendBroadcast(i);
            Log.e("getconnectionstatus","myble_5");
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
                        Log.e(EXCEPTION,getClass().getName()+" onDisConnected "+String.valueOf(e));
                    }
                }
            }
        }
    };
    /*@SuppressLint("SetTextI18n")
    private void ClusterOFF() {
        Dialog dialog = new Dialog(this, R.style.custom_dialog);
        dialog.setContentView(R.layout.custom_dialog);
        TextView tvAlertText = dialog.findViewById(R.id.tvAlertText);
        tvAlertText.setText("App Disconnected from Cluster");
        ImageView ivCross = dialog.findViewById(R.id.ivCross);
        ivCross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        ImageView ivCheck = dialog.findViewById(R.id.ivCheck);


        ivCheck.setOnClickListener(v -> {

        });
    }
    public void setScanRule() {

        boolean isConnected = BleManager.getInstance().isConnected(bleDevice);
        if (!isConnected) {
            try {
                BleManager.getInstance().connect(MACID, new BleGattCallback() {
                    @Override
                    public void onStartConnect() {

                    }

                    @Override
                    public void onConnectFail(BleDevice bleDevice, BleException exception) {
                    }

                    @Override
                    public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {

                        app.setBleDevice(bleDevice);


                        mBluetoothGatt = gatt;

                        Intent i = new Intent("status").putExtra("status", false);
                        sendBroadcast(i);
                        getConnectionStatus(false, getApplicationContext());
                        setMtu(bleDevice, 250);
                        mBoundService.getServicesList(bleDevice);

                    }

                    @Override
                    public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                        Log.e("DeviceListing","disconnected_2");
                        staticConnectionStatus = false;
                        Intent i = new Intent("status").putExtra("status", staticConnectionStatus);
                        sendBroadcast(i);
                        Log.e("getconnectionstatus","myble_2");
                        getConnectionStatus(staticConnectionStatus, getApplicationContext());
                    }
                });


            } catch (Exception ignored) {
            }
        }
    }*/
}
