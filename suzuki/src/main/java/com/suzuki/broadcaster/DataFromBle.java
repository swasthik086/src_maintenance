package com.suzuki.broadcaster;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DataFromBle extends BroadcastReceiver {

    public DataFromBle() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        Log.d("fromble", "Broadcast received: " + action);

        if (action.equals("ble")) {
            String state = intent.getExtras().getString("ble");

            Log.d("fromble", "Broadcast received:ss " + state);


        }

    }
}
