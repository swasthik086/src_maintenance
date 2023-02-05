package com.suzuki.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.suzuki.R;

import static com.suzuki.fragment.DashboardFragment.displayingDisconnectDialog;

public class OnDisconnect extends AppCompatActivity {

    private LocalBroadcastManager mLocalBroadcastManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.custom_dialog_for_ble);

        ImageView ivCustomClose = findViewById(R.id.ivCustomClose);
        ivCustomClose.setOnClickListener(v -> {
            displayingDisconnectDialog = false;
            finish();
        });

        LinearLayout llConnect = findViewById(R.id.llConnect);

        llConnect.setOnClickListener(v -> {
            displayingDisconnectDialog = false;
            Intent in = new Intent(OnDisconnect.this, DeviceListingScanActivity.class);
            startActivity(in);
            finish();
        });

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("closeDialogActivity");
        mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, mIntentFilter);
        IntentFilter intentFilterBleStatus = new IntentFilter("disconnect_status");

        registerReceiver(broadcastReceiver, intentFilterBleStatus);
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (intent.getExtras() != null) {
                    boolean status = intent.getExtras().getBoolean("disconnect_status", false);
                    if (status) {
                        displayingDisconnectDialog = false;
                        if (!isFinishing())
                            finish();
                    }
                }
            }
        }
    };

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null)
                if (intent.getAction().equals("closeDialogActivity")) {
                    displayingDisconnectDialog = false;
                    if (!isFinishing())
                        finish();
                }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        displayingDisconnectDialog = false;
        try {
            mLocalBroadcastManager.unregisterReceiver(mBroadcastReceiver);
        } catch (Exception ignored) { }

        try {
            unregisterReceiver(broadcastReceiver);
        } catch (Exception ignored) { }
    }
}
