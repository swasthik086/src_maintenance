package com.suzuki.activity;

import static com.suzuki.broadcaster.BluetoothCheck.BLUETOOTH_STATE;
import static com.suzuki.fragment.DashboardFragment.getConnectionStatus;
import static com.suzuki.fragment.DashboardFragment.onDisconnect;
import static com.suzuki.fragment.DashboardFragment.prev_cluster_macAddr;
import static com.suzuki.fragment.DashboardFragment.staticConnectionStatus;
import static com.suzuki.fragment.DashboardFragment.timer;
import static com.suzuki.utils.Common.BikeBleName;
import static com.suzuki.utils.Common.EXCEPTION;

import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.clj.fastble.BleManager;
import com.suzuki.R;
import com.suzuki.broadcaster.BleConnection;
import com.suzuki.pojo.BleDataPojo;
import com.suzuki.pojo.EvenConnectionPojo;
import com.suzuki.services.MyBleService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Objects;

import io.realm.Realm;

public class ConnectedDataActivity extends AppCompatActivity {
    static String bleName = "";
    Realm realm;
    TextView tvStatusS, deviceName;
    ImageView ivSelected;
    private BluetoothDevice mDevice = null;
    static RelativeLayout rlButtonConnect;

    //    BleConnection receiver;
    private BleConnection mReceiver;
    RelativeLayout rlClose;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
        setContentView(R.layout.device_listing_scanfastble_connected_activity);
        realm = Realm.getDefaultInstance();

        tvStatusS = (TextView) findViewById(R.id.tvStatus);
        deviceName = (TextView) findViewById(R.id.deviceName);
        ivSelected = (ImageView) findViewById(R.id.ivSelected);
        rlButtonConnect = (RelativeLayout) findViewById(R.id.rlButtonConnect);
        rlClose = (RelativeLayout) findViewById(R.id.rlClose);
        rlClose.setOnClickListener(v -> finish());

        realm.executeTransaction(realm -> {

            BleDataPojo bleDataPojo = realm.where(BleDataPojo.class).equalTo("bleId", 1).findFirst();

            if (bleDataPojo != null) bleName = bleDataPojo.getDeviceName();
   //        Toast.makeText(this, ""+bleName, Toast.LENGTH_SHORT).show();
        });

        deviceName.setText(BikeBleName.getValue());

        rlButtonConnect.setOnClickListener(v -> {

            Dialog dialog = new Dialog(ConnectedDataActivity.this, R.style.custom_dialog);
            dialog.setContentView(R.layout.custom_dialog);

            TextView tvAlertText = dialog.findViewById(R.id.tvAlertText);
            tvAlertText.setText(R.string.disconnect_vehicle);
            ImageView ivCross = dialog.findViewById(R.id.ivCross);
            ivCross.setOnClickListener(v12 -> dialog.cancel());

            ImageView ivCheck = dialog.findViewById(R.id.ivCheck);

            ivCheck.setOnClickListener(v1 -> {

                onDisconnect = true;
                BleManager.getInstance().disconnectAllDevice();
                BleManager.getInstance().destroy();

                Intent intent = new Intent(getApplicationContext(), MyBleService.class);
                stopService(intent);
//                prev_cluster = null; /*kns*/


                staticConnectionStatus = false;
                Intent i = new Intent("status").putExtra("status", false);
                sendBroadcast(i);
                getConnectionStatus(false, getApplicationContext());

                if(timer!=null) timer.cancel();

                reset_db();
                clear_previous_cluster();

                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("FT",Context.MODE_PRIVATE);
                sharedPreferences.edit().putString("blename","").apply();

//                bleName=""; BikeBleName=""; Log.e("bikeble","CLEARED connected 128");

                dialog.cancel();
                finish();
            });
            dialog.show();
        });
    }

    private void clear_previous_cluster() {
        SharedPreferences sharedPreferences = getSharedPreferences("BLE_DEVICE", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("prev_cluster_macAddr","");
        editor.putString("prev_cluster_name","");
        editor.apply();
        prev_cluster_macAddr = "";
        BikeBleName.setValue("");
    }
    
    private void reset_db() {
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
            Log.e(EXCEPTION,"ConnectedData: reset_DB"+ String.valueOf(e));
        }
    }

    /*public void stopAll() {


        BleManager.getInstance().disconnectAllDevice();
        BleManager.getInstance().destroy();

        try {
            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmResults<BleDataPojo> rows = realm.where(BleDataPojo.class).equalTo("bleId", 1).findAll();
                    rows.deleteAllFromRealm();
                }
            }, new Realm.Transaction.OnSuccess() {
                @Override
                public void onSuccess() {
                    staticConnectionStatus = false;

                    getConnectionStatus(staticConnectionStatus, getApplicationContext());

                    Intent i = new Intent("status").putExtra("status", false);
                    sendBroadcast(i);
                    Log.d("connectsttaus", "jsmsmsjjsuu");
//                            getActivity().finish();
                    //Cheers! some worker thread did it for us!
                }
            });
        } catch (Exception e) {
            e.getMessage();
        } finally {

            realm.close();
            finish();
        }
//        getActivity().finish();
    }*/

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectionEvent(EvenConnectionPojo event) {

        if (!BLUETOOTH_STATE) {

            staticConnectionStatus = false;

            sendBroadcast(new Intent("status").putExtra("status", staticConnectionStatus));
            finish();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter("status");

        mReceiver = new BleConnection() {

            @Override
            public void onReceive(Context context, Intent intent) {
                //extract our message from intent

                boolean status = Objects.requireNonNull(intent.getExtras()).getBoolean("status");
                if (status) {

                } else {
                    finish();
                }

            }
        };
        //registering our receiver
//        registerReceiver(mReceiver, intentFilter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(mReceiver, intentFilter, RECEIVER_EXPORTED);
        }else {
            registerReceiver(mReceiver, intentFilter);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }


}
