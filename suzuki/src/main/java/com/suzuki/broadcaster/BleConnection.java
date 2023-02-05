package com.suzuki.broadcaster;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BleConnection extends BroadcastReceiver {

    public BleConnection() { }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();



        Log.d("connectsttaus", "Broadcast received: " + action);

        if (action.equals("status")) {
            boolean state = intent.getExtras().getBoolean("status");

            Log.d("connectsttaus", "Broadcast received:ss " + state);

//            EventBus.getDefault().post(new BleConnectedToBikePojo(bluetoothCheck));
        }
    }
}