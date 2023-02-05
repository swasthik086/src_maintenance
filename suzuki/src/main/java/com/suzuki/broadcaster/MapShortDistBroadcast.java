package com.suzuki.broadcaster;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.suzuki.pojo.EvenConnectionPojo;

import org.greenrobot.eventbus.EventBus;

public class MapShortDistBroadcast extends BroadcastReceiver {

    public MapShortDistBroadcast() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        Log.d("shortdist", "Broadcast received: " + action);

        if (action.equals("status")) {
            String state = intent.getExtras().getString("shortdist");

            Log.d("shortdist", "Broadcast received:ss " + state);


        }



    }
}
