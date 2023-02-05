package com.suzuki.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.clj.fastble.BleManager;
import com.suzuki.R;

public class BluetoothDisabledActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_disabled);

        ImageView ivCustomClose = findViewById(R.id.ivCustomClose);
        ivCustomClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BleManager.getInstance().getBluetoothAdapter().isEnabled()){
                    BleManager.getInstance().getBluetoothAdapter().enable();
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent in = new Intent(BluetoothDisabledActivity.this, DeviceListingScanActivity.class);
                        startActivity(in);
                        finish();
                    }
                },100);
            }
        });

        LinearLayout yes = findViewById(R.id.llConnect);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BleManager.getInstance().getBluetoothAdapter().isEnabled()){
                    BleManager.getInstance().getBluetoothAdapter().enable();
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent in = new Intent(BluetoothDisabledActivity.this, DeviceListingScanActivity.class);
                        startActivity(in);
                        finish();
                    }
                },100);
            }
        });
    }
}
