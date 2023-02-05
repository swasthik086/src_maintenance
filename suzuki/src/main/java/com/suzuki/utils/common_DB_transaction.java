package com.suzuki.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.clj.fastble.data.BleDevice;
import com.google.android.material.snackbar.Snackbar;
import com.suzuki.R;
import com.suzuki.activity.ProfileActivity;
import com.suzuki.pojo.RiderProfileModule;
import com.suzuki.pojo.SettingsPojo;

import io.realm.Realm;



public class common_DB_transaction {

    private Realm realm;
    private RiderProfileModule riderProfile;
    private SettingsPojo speed_settings;

    public void RESET_SPEED_SETTING(){
        realm = Realm.getDefaultInstance();
        riderProfile = realm.where(RiderProfileModule.class).equalTo("id", 1).findFirst();

        speed_settings = realm.where(SettingsPojo.class).equalTo("id", 1).findFirst();
        if (speed_settings == null) speed_settings = realm.createObject(SettingsPojo.class, 1);

        try {
            realm.executeTransaction(realm -> {

                if (riderProfile.getBike().contentEquals("Scooter")) speed_settings.setSpeedAlert(5);
                else if (riderProfile.getBike().contentEquals("Motorcycle")) speed_settings.setSpeedAlert(10);

                speed_settings.setSpeedSet(false);
                realm.insertOrUpdate(speed_settings);
            });
        } catch (Exception e) {
            Log.e("common_db",String.valueOf(e));
        }
    }

}
