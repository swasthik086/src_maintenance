package com.suzuki.broadcaster;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.suzuki.pojo.EvenConnectionPojo;

import org.greenrobot.eventbus.EventBus;

public class BluetoothCheck extends BroadcastReceiver {

    public static boolean BLUETOOTH_STATE =false;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.e("ble_State",action);

        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
            if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF) {
                BLUETOOTH_STATE = false;
                Log.e("BluetoothCheck","BLE_OFF");

                //new Common(context).show_custom_dialog(R.string.BLUETOOTH_OFF);

                EventBus.getDefault().post(new EvenConnectionPojo(BLUETOOTH_STATE));

            } else if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_ON) {
                Log.e("BluetoothCheck","BLE_ON");
                BLUETOOTH_STATE = true;
                EventBus.getDefault().post(new EvenConnectionPojo(BLUETOOTH_STATE));
            }

            // Bluetooth is disconnected, do handling here
        }

    }
}