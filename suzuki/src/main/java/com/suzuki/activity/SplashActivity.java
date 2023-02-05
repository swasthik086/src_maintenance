package com.suzuki.activity;

import static org.greenrobot.eventbus.EventBus.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.suzuki.R;
import com.suzuki.pojo.MapListRealmModule;
import com.suzuki.pojo.RiderProfileModule;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

public class SplashActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 456;
    private  static  final  int PERMISSION_REQUEST_BLUETOOTH_SCAN=1001;


    SharedPreferences prefs = null;

//    boolean checkUsername = false;
    ArrayList<String> stringArrayList = new ArrayList<>();

    RealmResults<MapListRealmModule> mapListItem;

    Realm realm;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.splash_activity);

//        int permission = 0;
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
//            permission = ContextCompat.checkSelfPermission(this,
//                    Manifest.permission.BLUETOOTH_SCAN);
//        }
//        if (permission != PackageManager.PERMISSION_GRANTED) {
//            Log.i(TAG, "Permission to record denied");
//            makeRequest();
//        }

        if (!isTaskRoot()
                && getIntent().hasCategory(Intent.CATEGORY_LAUNCHER)
                && getIntent().getAction() != null
                && getIntent().getAction().equals(Intent.ACTION_MAIN)) {
            finish();
            return;
        }

        //        /**
//         * restarting BLE adapter to refresh any connection
//         */
//        {
//            BluetoothAdapter BLE_ADAPTER=BluetoothAdapter.getDefaultAdapter();
//            if(BLE_ADAPTER.isEnabled()) BLE_ADAPTER.disable();
//        }

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        realm = Realm.getDefaultInstance();
        prefs = getSharedPreferences("suzuki", MODE_PRIVATE);

        new Handler().postDelayed(() -> {

            //checkMapRecord(realm);
            mapListItem = realm.where(MapListRealmModule.class).findAll();
            //viewRecord();
            if (mapListItem.size() == 0) addMapItemsDataToRealm();
            moveToHome();
        },1500);
    }

    private void makeRequest() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_SCAN},
                    PERMISSION_REQUEST_BLUETOOTH_SCAN);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {


            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    moveToHome();
            }
//            case PERMISSION_REQUEST_BLUETOOTH_SCAN:{
//                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
//                    moveToHome();
//            }


        }
    }

    /*public void viewRecord() {

        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(realm1 -> {

                RiderProfileModule riderProfile = realm1.where(RiderProfileModule.class).equalTo("id", 1).findFirst();

                if (riderProfile == null) checkUsername = false;
                else if (riderProfile.getName() != null) {
                    if (riderProfile.getName().equals("")) checkUsername = false;
                    else checkUsername = true;
                }
            });
        } catch (Exception e) {
            Log.d("realmex", "viewRecord--" + e.getMessage());
        }
    }*/

    private void moveToHome() {

        SharedPreferences sharedPreferences = getSharedPreferences("user_data",MODE_PRIVATE);
        String name=sharedPreferences.getString("name","");
        Log.e("namecheck","check: "+name);
        if(name.isEmpty()){
            Intent i = new Intent(SplashActivity.this, IntroscreenActivity.class);
            i.putExtra("help", "splash");
            startActivity(i);

        }
        else startActivity(new Intent(SplashActivity.this, HomeScreenActivity.class));

        /*if (checkUsername) startActivity(new Intent(SplashActivity.this, HomeScreenActivity.class));
        else {
            Intent i = new Intent(SplashActivity.this, IntroscreenActivity.class);
            i.putExtra("help", "splash");
            startActivity(i);
        }*/
        finish();
    }

    private void addMapItemsDataToRealm() {

        if (stringArrayList.size() > 0) stringArrayList.clear();

        stringArrayList.add(getResources().getString(R.string.suzukiservice));
        stringArrayList.add(getResources().getString(R.string.fuel));
        stringArrayList.add(getResources().getString(R.string.hospitals));
        stringArrayList.add(getResources().getString(R.string.atm));
        stringArrayList.add(getResources().getString(R.string.food));
        stringArrayList.add(getResources().getString(R.string.sales));
        stringArrayList.add(getResources().getString(R.string.tyrerepair));
        stringArrayList.add(getResources().getString(R.string.medicals));
        stringArrayList.add(getResources().getString(R.string.parking));
        stringArrayList.add(getResources().getString(R.string.convenience));

        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(realm1 -> {

                //RealmResults<MapListRealmModule> results = realm.where(MapListRealmModule.class).findAll();
                for (int i = 0; i < stringArrayList.size(); i++) {
                    MapListRealmModule mapListRealmModule = realm1.createObject(MapListRealmModule.class);
                    mapListRealmModule.setId(i);
                    mapListRealmModule.setName(stringArrayList.get(i));
                    realm1.insert(mapListRealmModule);
                }
            });
        } catch (Exception e) {
            Log.d("realmex", "addMapData--" + e.getMessage());
        }
    }
}