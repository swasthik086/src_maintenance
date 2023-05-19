package com.suzuki.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;

import com.sachinvarma.easypermission.EasyPermissionInit;
import com.sachinvarma.easypermission.EasyPermissionList;
import com.suzuki.R;
import com.suzuki.activity.DeviceListingScanActivity;
import com.suzuki.broadcaster.BleConnection;

import com.suzuki.pojo.EvenConnectionPojo;
import com.suzuki.pojo.RiderProfileModule;
import com.suzuki.pojo.SettingsPojo;
import com.suzuki.utils.Common;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.realm.Realm;

import static com.mappls.sdk.maps.Mappls.getApplicationContext;
import static com.suzuki.activity.HomeScreenActivity.HOME_SCREEN_OBJ;
import static com.suzuki.activity.HomeScreenActivity.TOAST_DURATION;
import static com.suzuki.broadcaster.BluetoothCheck.BLUETOOTH_STATE;
import static com.suzuki.fragment.DashboardFragment.staticConnectionStatus;
import static com.suzuki.utils.Common.BikeBleName;
import static com.suzuki.utils.Common.EXCEPTION;
import static com.suzuki.utils.Common.SPEED_FLAG;

public class SettingFragment extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private CheckBox cbCallYouBack;
    private CheckBox cbBusy;
    private CheckBox cbRiding;
    private CheckBox cbCustomMsgCheck;
    List<String> permission = new ArrayList<>();
    private RelativeLayout llEditCustomMsg, rlCustom;
    private SeekBar SpeedSeekbar;
    private TextView tvTextCount;

    private LinearLayout SpeedLayout, llSecondLayout, llFirstLayout, llRedAlertBle;
//    Switch switchClock;

    private RiderProfileModule riderProfile;
    private Realm realm;

    private int scooterMinSpeed = 5, scooterMaxSpeed = 120;
    private int bikeMinSpeed = 10, bikeMaxSpeed = 299;
    private Switch SpeedSwitch;
    private Switch switchSms, switchSocial, switchIncomingCall, switchIncomingSMS, switchWhatsappCall, switchWhatsappMSG, switchSaveAllTrips;
    private EditText etCustomMsg, SpeedValue;

    private BleConnection mReceiver;

    private int setMaxSpeed;
    private Common common;
    private boolean isSeekBarTouched, isScooterSelected, isBikeSelected;
    private int speedFromDb;
    private BroadcastReceiver myReceiver;

    private SharedPreferences.Editor editor;
    private SharedPreferences sharedPreferences;
    private int speed_value=0, min_speed, max_speed;

    //private boolean isSaveTripClicked;

    @RequiresApi(api = Build.VERSION_CODES.O)

    @SuppressLint("SetTextI18n")
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.settings_fragment, container, false);

        common = new Common(getContext());


        sharedPreferences = getApplicationContext().getSharedPreferences("vehicle_data", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        realm = Realm.getDefaultInstance();
        initWidgets(view);
        etCustomMsg.setHorizontallyScrolling(false);
        etCustomMsg.setMaxLines(5);
        cbCustomMsgCheck.setOnCheckedChangeListener(this);
        cbCallYouBack.setOnCheckedChangeListener(this);
        cbBusy.setOnCheckedChangeListener(this);
        cbRiding.setOnCheckedChangeListener(this);
        rlCustom.setOnClickListener(this);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());//this==context
        if (!prefs.contains("FirstTimeDisclaimer")) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("FirstTimeDisclaimer", true);
            editor.commit();
            saveTripsDisclaimer("Total 10 trips can be saved\n"+ "for both Recent and Favourites.\n"+" While 11th trip, oldest will get deleted automatically.");

        }


            //Other dialog code
//                    alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//
//                        public void onClick(DialogInterface dialog, int which) {
//                            SharedPreferences.Editor editor = prefs.edit();
//                            editor.putBoolean("FirstTime",true);
//                            editor.commit();
//                            //more code....
//                        }
//                    });

        /*isSaveTripClicked = sharedPreferences.getBoolean("isSaveTripsChecked",false);
        if(isSaveTripClicked){
            switchSaveAllTrips.setChecked(true);
        }
        else{
            switchSaveAllTrips.setChecked(false);
        }*/



        switchSaveAllTrips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(getActivity());//this==context
                if(!prefs.contains("FirstTime")){
                    if (!switchSaveAllTrips.isChecked()){

                        SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean("FirstTime",true);
                            editor.commit();

                        dontSaveTripsAlert("Do you want to turn OFF Save All Trips?");

                    }


                    //Other dialog code
//                    alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//
//                        public void onClick(DialogInterface dialog, int which) {
//                            SharedPreferences.Editor editor = prefs.edit();
//                            editor.putBoolean("FirstTime",true);
//                            editor.commit();
//                            //more code....
//                        }
//                    });
                }
            }
        });

//        if (SpeedSwitch.isChecked()==false){
//            SpeedValue.clearFocus();
//            InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//            imm.hideSoftInputFromWindow(SpeedValue.getWindowToken(), 0);
//        }

        {
            speed_value = sharedPreferences.getInt("speed",0);

            if(speed_value == 0){
                SpeedSwitch.setChecked(false);

            }
            else {

                update_speed_data_view();
            }
        }

//        Log.e("check_sdk",String.valueOf(android.os.Build.VERSION.SDK_INT));
        {
            SpeedSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    if (i==115 && (android.os.Build.VERSION.SDK_INT==26 || android.os.Build.VERSION.SDK_INT==27)) i=120;
                    else if (i==289 && (android.os.Build.VERSION.SDK_INT==26 || android.os.Build.VERSION.SDK_INT==27)) i=299;

                    if(sharedPreferences.getString("vehicle_type","").equals("Scooter")){
                        min_speed = 5; max_speed = 120;
                    } else if (sharedPreferences.getString("vehicle_type","").equals("Motorcycle")){
                        min_speed = 10; max_speed = 299;
                    }

                    speed_value = i;
                    if(i < min_speed) {
                        seekBar.setProgress(min_speed);
                    }
                    SpeedValue.setText(String.valueOf(speed_value));

//                    SpeedValue.addTextChangedListener(new TextWatcher() {
//                        @Override
//                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//
//
//                        }
//
//                        @Override
//                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                            String str= SpeedValue.getText().toString();
//                            //   speed_value= str;
//                            Toast.makeText(getContext(), ""+
//                                    str, Toast.LENGTH_SHORT).show();
//                        }
//
//                        @Override
//                        public void afterTextChanged(Editable editable) {
//
//
//
////                            if (SpeedValue.getText().toString()==" "){
////                                Toast.makeText(getContext(), "blank now", Toast.LENGTH_SHORT).show();
////                                speed_value=0;
////                            }
////                            else {
////                                speed_value= Integer.parseInt(String.valueOf(SpeedValue.getText()));
////
////                            }
//
//                        }
//                    });


                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) { }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                    editor.putInt("speed",speed_value);
                    editor.apply();
                }
            });
        }
        /*myReceiver = new BroadcastReceiver() {

            public void onReceive(Context context, Intent intent) {

            }
        };*/

        try {
            realm.executeTransaction(realm -> {

                SettingsPojo settingsPojo = realm.where(SettingsPojo.class).equalTo("id", 1).findFirst();

                if (settingsPojo != null) {

                    if (settingsPojo.isAutoReplySMS()) {

                        if (settingsPojo.isCustomMsg()) {
                            cbCustomMsgCheck.setChecked(true);
                            etCustomMsg.setText(settingsPojo.getMessage());
                            int length = etCustomMsg.length();
                            String convert = String.valueOf(length);

                            if (length <= 0) tvTextCount.setText("0" + "/160");
                            else tvTextCount.setText(convert + "/160");

                        } else if (settingsPojo.isCallYouback()) cbCallYouBack.setChecked(true);
                        else if (settingsPojo.isImbusy()) cbBusy.setChecked(true);
                        else if (settingsPojo.isImRiding()) cbRiding.setChecked(true);

                        switchSms.setChecked(true);
                        llSecondLayout.setVisibility(View.VISIBLE);
                    }


                    if (settingsPojo.isCallsms()) {

                        switchSocial.setChecked(true);
                        llFirstLayout.setVisibility(View.VISIBLE);
                        if (settingsPojo.isIncomingCall()) switchIncomingCall.setChecked(true);

                        if (!settingsPojo.isIncomingCall()) switchIncomingCall.setChecked(false);

                        if (settingsPojo.isIncomingSMS()) switchIncomingSMS.setChecked(true);

                        if (!settingsPojo.isIncomingSMS()) switchIncomingSMS.setChecked(false);

                        if (settingsPojo.isWhatsappCall()) switchWhatsappCall.setChecked(true);

                        if (!settingsPojo.isWhatsappCall()) switchWhatsappCall.setChecked(false);

                        if (settingsPojo.isWhatsappMSG()) switchWhatsappMSG.setChecked(true);

                        if (!settingsPojo.isWhatsappMSG()) switchWhatsappMSG.setChecked(false);

                    }
                    else if (!settingsPojo.isCallsms()) {
                        switchSocial.setChecked(false);
                        llFirstLayout.setVisibility(View.GONE);
                    }

                    if (switchSaveAllTrips.isChecked() == true){
                        settingsPojo.setSaveTrips(true);
                    }
                    else {
                        settingsPojo.setSaveTrips(false);
                    }

                   if (settingsPojo.isSaveTrips()) {
                       switchSaveAllTrips.setChecked(true);
                      /* editor.putBoolean("isSaveTripsChecked",true);
                       editor.apply();*/
                   }
                   else if (!settingsPojo.isSaveTrips()){
                       switchSaveAllTrips.setChecked(false);
                      /* editor.putBoolean("isSaveTripsChecked",false);
                       editor.apply();*/
                   }


                }
            });
        } catch (Exception k) {
            Log.e(EXCEPTION, String.valueOf(k));
        }

        switchIncomingCall.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {
                if(!check_call_permission()) showExitAlert("App needs Call Log permission to display the incoming call notifications on Suzuki vehicle's display. Please allow the permission on next screen.",201);
                Realm realm = Realm.getDefaultInstance();
                try {
                    realm.executeTransaction(realm110 -> {

                        SettingsPojo settingsPojo = realm110.where(SettingsPojo.class).equalTo("id", 1).findFirst();

                        if (settingsPojo == null) {

                            settingsPojo = realm110.createObject(SettingsPojo.class, 1);
                            settingsPojo.setIncomingCall(true);

                            realm110.insertOrUpdate(settingsPojo);
                        } else {
                            settingsPojo.setIncomingCall(true);
                            realm110.insertOrUpdate(settingsPojo);
                        }
                    });

                } catch (Exception ignored) {

                }

            } else {

                Realm realm = Realm.getDefaultInstance();
                try {
                    realm.executeTransaction(realm1 -> {

                        SettingsPojo settingsPojo = realm1.where(SettingsPojo.class).equalTo("id", 1).findFirst();

                        if (settingsPojo == null) {

                            settingsPojo = realm1.createObject(SettingsPojo.class, 1);
                            settingsPojo.setIncomingCall(false);
                            realm1.insertOrUpdate(settingsPojo);
                        } else {
                            settingsPojo.setIncomingCall(false);
                            realm1.insertOrUpdate(settingsPojo);
                        }
                    });

                }
                catch (Exception ignored) { }
                if (isAllSocialUnchecked()) switchSocial.setChecked(false);
            }
        });

        switchWhatsappCall.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {

                if(!check_call_permission()) showExitAlert("App needs WhatsApp Call Log permission to display the incoming call notifications on Suzuki vehicle's display. Please allow the permission on next screen.",201);
                Realm realm = Realm.getDefaultInstance();
                try {
                    realm.executeTransaction(realm12 -> {

                        SettingsPojo settingsPojo = realm12.where(SettingsPojo.class).equalTo("id", 1).findFirst();

                        if (settingsPojo == null) {

                            settingsPojo = realm12.createObject(SettingsPojo.class, 1);
                            settingsPojo.setWhatsappCall(true);

                            realm12.insertOrUpdate(settingsPojo);
                        } else {
                            settingsPojo.setWhatsappCall(true);
                            realm12.insertOrUpdate(settingsPojo);
                        }
                    });

                } catch (Exception ignored) {

                }

            } else {

                Realm realm = Realm.getDefaultInstance();
                try {
                    realm.executeTransaction(realm13 -> {

                        SettingsPojo settingsPojo = realm13.where(SettingsPojo.class).equalTo("id", 1).findFirst();

                        if (settingsPojo == null) {

                            settingsPojo = realm13.createObject(SettingsPojo.class, 1);
                            settingsPojo.setWhatsappCall(false);
                            realm13.insertOrUpdate(settingsPojo);

                        }
                        else {
                            settingsPojo.setWhatsappCall(false);
                            realm13.insertOrUpdate(settingsPojo);
                        }
                    });

                } catch (Exception ignored) {

                }

                if (isAllSocialUnchecked()) switchSocial.setChecked(false);
            }
        });

        switchWhatsappMSG.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {

                if(!check_permission(Manifest.permission.RECEIVE_SMS)){
                    showExitAlert("WhatsApp SMS permission is required to display SMS notifications on the vehicle's cluster. Please allow the permission on next screen.",202);
                }

                Realm realm = Realm.getDefaultInstance();
                try {
                    realm.executeTransaction(realm14 -> {

                        SettingsPojo settingsPojo = realm14.where(SettingsPojo.class).equalTo("id", 1).findFirst();

                        if (settingsPojo == null) {

                            settingsPojo = realm14.createObject(SettingsPojo.class, 1);
                            settingsPojo.setWhatsappMSG(true);
                            realm14.insertOrUpdate(settingsPojo);

                        }
                        else {
                            settingsPojo.setWhatsappMSG(true);
                            realm14.insertOrUpdate(settingsPojo);
                        }
                    });

                } catch (Exception k) {
                    Log.e(EXCEPTION, String.valueOf(k));
                }

            }
            else {

                Realm realm = Realm.getDefaultInstance();
                try {
                    realm.executeTransaction(realm15 -> {
                        SettingsPojo settingsPojo = realm15.where(SettingsPojo.class).equalTo("id", 1).findFirst();

                        if (settingsPojo == null) {

                            settingsPojo = realm15.createObject(SettingsPojo.class, 1);
                            settingsPojo.setWhatsappMSG(false);

                            realm15.insertOrUpdate(settingsPojo);
                        } else {
                            settingsPojo.setWhatsappMSG(false);
                            realm15.insertOrUpdate(settingsPojo);
                        }
                    });
                } catch (Exception ignored) { }

                if (isAllSocialUnchecked()) switchSocial.setChecked(false);
            }
        });

        switchIncomingSMS.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {

                if(!check_permission(Manifest.permission.RECEIVE_SMS)){
                    showExitAlert("SMS permission is required to display SMS notifications on the vehicle's cluster. Please allow the permission on next screen.",202);
                }

                Realm realm = Realm.getDefaultInstance();
                try {
                    realm.executeTransaction(realm16 -> {

                        SettingsPojo settingsPojo = realm16.where(SettingsPojo.class).equalTo("id", 1).findFirst();

                        if (settingsPojo == null) {

                            settingsPojo = realm16.createObject(SettingsPojo.class, 1);
                            settingsPojo.setIncomingSMS(true);

                            realm16.insertOrUpdate(settingsPojo);
                        }
                        else {
                            settingsPojo.setIncomingSMS(true);
                            realm16.insertOrUpdate(settingsPojo);
                        }
                    });
                } catch (Exception ignored) { }

            } else {
                Realm realm = Realm.getDefaultInstance();
                try {
                    realm.executeTransaction(realm17 -> {

                        SettingsPojo settingsPojo = realm17.where(SettingsPojo.class).equalTo("id", 1).findFirst();

                        if (settingsPojo == null) {

                            settingsPojo = realm17.createObject(SettingsPojo.class, 1);
                            settingsPojo.setIncomingSMS(false);

                            realm17.insertOrUpdate(settingsPojo);
                        } else {
                            settingsPojo.setIncomingSMS(false);
                            realm17.insertOrUpdate(settingsPojo);
                        }
                    });
                } catch (Exception ignored) { }

                if (isAllSocialUnchecked()) switchSocial.setChecked(false);
            }
        });

        switchSocial.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                //request_notification_access_new();

                llFirstLayout.setVisibility(View.VISIBLE);
                switchWhatsappMSG.setChecked(true);
                switchWhatsappCall.setChecked(true);
                switchIncomingSMS.setChecked(true);
                switchIncomingCall.setChecked(true);

                Realm realm = Realm.getDefaultInstance();
                try {
                    realm.executeTransaction(realm18 -> {

                        SettingsPojo settingsPojo = realm18.where(SettingsPojo.class).equalTo("id", 1).findFirst();

                        if (settingsPojo == null) settingsPojo = realm18.createObject(SettingsPojo.class, 1);

                      //  if(check_permission(Manifest.permission.RECEIVE_SMS)){
                        //    settingsPojo.setWhatsappMSG(true);

                       // }
                       // if(check_call_permission()) {
                        //    settingsPojo.setWhatsappCall(true);
                       // }

                        settingsPojo.setCallsms(true);
                        settingsPojo.setWhatsappMSG(true);
                        settingsPojo.setWhatsappCall(true);
                        settingsPojo.setIncomingSMS(true);
                        settingsPojo.setIncomingCall(true);
                        realm18.insertOrUpdate(settingsPojo);

                    });

                }
                catch (Exception ignored) {

                }

            } else {
                switchWhatsappMSG.setChecked(false);
                switchWhatsappCall.setChecked(false);
                switchIncomingSMS.setChecked(false);
                switchIncomingCall.setChecked(false);
                llFirstLayout.setVisibility(View.GONE);

                Realm realm = Realm.getDefaultInstance();
                try {
                    realm.executeTransaction(realm19 -> {
                        SettingsPojo settingsPojo = realm19.where(SettingsPojo.class).equalTo("id", 1).findFirst();

                        if (settingsPojo == null) {
                            settingsPojo = realm19.createObject(SettingsPojo.class, 1);
                        }
                        settingsPojo.setCallsms(false);
                     //   settingsPojo.setWhatsappMSG(false);

                        settingsPojo.setWhatsappMSG(false);
                        settingsPojo.setWhatsappCall(false);
                        settingsPojo.setIncomingSMS(false);
                        settingsPojo.setIncomingCall(false);
                        realm19.insertOrUpdate(settingsPojo);
                    });

                } catch (Exception k) {
                    Log.e(EXCEPTION,String.valueOf(k));
                }
            }
        });

        setSpeedWarning();

        switchSms.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {

                if(!check_permission(Manifest.permission.SEND_SMS)){
                    showExitAlert("SMS permission is required to send auto SMS reply to incoming caller. Please allow the permission on the next screen.",200);
                } //else showExitAlert("Standard SMS Charges Applies, \nAs Per Your Network Carrier Plan.",205);

                llSecondLayout.setVisibility(View.VISIBLE);

                try {
                    realm.executeTransaction(realm -> {
//
                        SettingsPojo settingsPojo = realm.where(SettingsPojo.class).equalTo("id", 1).findFirst();

                        if (settingsPojo == null) {

                            settingsPojo = realm.createObject(SettingsPojo.class, 1);
                        }
                        settingsPojo.setAutoReplySMS(true);
                        settingsPojo.setImRiding(true);
                        realm.insertOrUpdate(settingsPojo);

                    });
                } catch (Exception e) {
                    Log.e(EXCEPTION,String.valueOf(e));
                }

                cbRiding.setChecked(true);
            } else {
                llSecondLayout.setVisibility(View.GONE);

                realm.executeTransaction(realm -> {
//
                    SettingsPojo settingsPojo = realm.where(SettingsPojo.class).equalTo("id", 1).findFirst();

                    if (settingsPojo == null) settingsPojo = realm.createObject(SettingsPojo.class, 1);

                    settingsPojo.setAutoReplySMS(false);
                    settingsPojo.setCallYouback(false);
                    settingsPojo.setImRiding(false);
                    settingsPojo.setImbusy(false);
                    settingsPojo.setCustomMsg(false);
                    settingsPojo.setMessage("");
                    realm.insertOrUpdate(settingsPojo);
                });
            }
        });

        switchSaveAllTrips.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (isChecked) {

               /* editor.putBoolean("isSaveTripsChecked",true);
                editor.apply();*/

                try {
                    realm.executeTransaction(realm -> {

                        SettingsPojo settingsPojo = realm.where(SettingsPojo.class).equalTo("id", 1).findFirst();

                        if (settingsPojo == null) {

                            settingsPojo = realm.createObject(SettingsPojo.class, 1);
                            settingsPojo.setSaveTrips(true);

                            realm.insertOrUpdate(settingsPojo);
                        } else {
                            settingsPojo.setSaveTrips(true);

                            realm.insertOrUpdate(settingsPojo);
                        }
                    });
                } catch (Exception ignored) {

                }

            }

            else {

              /*  editor.putBoolean("isSaveTripsChecked",false);
                editor.apply();*/

                try {
                    realm.executeTransaction(realm -> {

                        SettingsPojo settingsPojo = realm.where(SettingsPojo.class).equalTo("id", 1).findFirst();

                        if (settingsPojo == null) {

                            settingsPojo = realm.createObject(SettingsPojo.class, 1);
                            settingsPojo.setSaveTrips(false);

                            realm.insertOrUpdate(settingsPojo);
                        } else {
                            settingsPojo.setSaveTrips(false);

                            realm.insertOrUpdate(settingsPojo);
                        }
                    });
                } catch (Exception e) {
                    Log.e(EXCEPTION,"settingsFragment 599: "+String.valueOf(e));
                }
            }
        });

        SpeedSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                SpeedLayout.setVisibility(View.VISIBLE);

                if(sharedPreferences.getString("vehicle_type","").equals("Scooter")){
                    min_speed = 5; max_speed = 120;
                } else if (sharedPreferences.getString("vehicle_type","").equals("Motorcycle")){
                    min_speed = 10; max_speed = 299;
                }

                try {
                    if((Build.VERSION.SDK_INT > 25)) {
                        SpeedSeekbar.setMin(min_speed);
                        SpeedSeekbar.setMax(max_speed);
                        SpeedSeekbar.setProgress(speed_value = min_speed);

                        SpeedValue.setText(String.valueOf(speed_value));
                    }
                    else{
                        //SpeedSeekbar.setMin(min_speed);
                        SpeedSeekbar.setMax(max_speed);
                        SpeedSeekbar.setProgress(speed_value = min_speed);

                        SpeedValue.setText(String.valueOf(speed_value));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            } else{
                SpeedLayout.setVisibility(View.GONE);

                speed_value=0;


                InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

            }

            editor.putInt("speed",speed_value);
            editor.apply();
        });

        etCustomMsg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {

                int length = etCustomMsg.length();
                String convert = String.valueOf(length);
                if (length <= 0) tvTextCount.setText("0" + "/160");
                else tvTextCount.setText(convert + "/160");
                etCustomMsg.setOnEditorActionListener((v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {

                        Realm realm = Realm.getDefaultInstance();
                        try {
                            realm.executeTransaction(realm111 -> {

                                SettingsPojo settingsPojo = realm111.where(SettingsPojo.class).equalTo("id", 1).findFirst();

                                if (settingsPojo == null) {

                                    settingsPojo = realm111.createObject(SettingsPojo.class, 1);
                                    settingsPojo.setMessage(etCustomMsg.getText().toString());
                                    realm111.insertOrUpdate(settingsPojo);
                                }
                                else {

                                    settingsPojo.setMessage(etCustomMsg.getText().toString());
                                    realm111.insertOrUpdate(settingsPojo);

                                }
                            });

                        } catch (Exception ignored) {

                        }

                        etCustomMsg.clearFocus();
                        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(etCustomMsg.getWindowToken(), 0);
                    }
                    return false;
                });
            }
        });

        llRedAlertBle.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), DeviceListingScanActivity.class));
        });

        setBluetoothStatus();
        manaul_speed_entry();
        editTextOkListener();

        BikeBleName.observe(getActivity(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (s.isEmpty()) llRedAlertBle.setVisibility(View.VISIBLE);
                else llRedAlertBle.setVisibility(View.GONE);
            }
        });
        return view;
    }

    public void saveTripsDisclaimer(String message) {
        Dialog dialog = new Dialog(getContext(), R.style.custom_dialog);
        dialog.setContentView(R.layout.custom_dialog_for_savetrips);

        TextView tvAlertText = dialog.findViewById(R.id.etTripName);
        tvAlertText.setText(message);
        ImageView ivCross = dialog.findViewById(R.id.ivCustomClose);
        ivCross.setOnClickListener(v -> dialog.cancel());

        LinearLayout llSave = dialog.findViewById(R.id.llSave);

        llSave.setOnClickListener(v -> {

            dialog.cancel();
        });


        dialog.show();
    }

    public void dontSaveTripsAlert(String message) {
        Dialog dialog = new Dialog(getContext(), R.style.custom_dialog);
        dialog.setContentView(R.layout.custom_dialog);

        TextView tvAlertText = dialog.findViewById(R.id.tvAlertText);
        tvAlertText.setText(message);
        ImageView ivCross = dialog.findViewById(R.id.ivCross);
        ivCross.setOnClickListener(v -> dialog.cancel());

        ImageView ivCheck = dialog.findViewById(R.id.ivCheck);

        ivCheck.setOnClickListener(v -> {

            switchSaveAllTrips.setChecked(false);
          /*  editor.putBoolean("isSaveTripsChecked",false);
            editor.apply();*/
            dialog.dismiss();
        });

        ivCross.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* editor.putBoolean("isSaveTripsChecked",true);
                editor.apply();*/
                switchSaveAllTrips.setChecked(true);
                dialog.cancel();
            }
        });
        dialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void update_speed_data_view() {

        SpeedSwitch.setChecked(true);
        if(sharedPreferences.getString("vehicle_type","").equals("Scooter")){
            min_speed = 5; max_speed = 120;
        } else if (sharedPreferences.getString("vehicle_type","").equals("Motorcycle")){
            min_speed = 10; max_speed = 299;
        }

        if (speed_value < min_speed) speed_value = min_speed;

        if (speed_value > max_speed) speed_value = max_speed;

        if(Build.VERSION.SDK_INT>25) {
            SpeedSeekbar.setMin(min_speed);
        }
        SpeedSeekbar.setMax(max_speed);
        SpeedSeekbar.setProgress(speed_value);
        SpeedLayout.setVisibility(View.VISIBLE);
        SpeedValue.setText(String.valueOf(speed_value));
    }

    @Override
    public void onResume() {
        super.onResume();
        speed_value = sharedPreferences.getInt("speed",0);

        if (SPEED_FLAG) SpeedSwitch.setChecked(SPEED_FLAG = false);

        String enabledNotificationListeners = Settings.Secure.getString(getContext().getContentResolver(), "enabled_notification_listeners");

        if (enabledNotificationListeners == null || !enabledNotificationListeners.contains("com.suzuki.services.NotificationService")) {
            if(check_call_permission() && check_permission(Manifest.permission.RECEIVE_SMS)){
                startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
            }
        }
    }

    /*public void update_speed_settings(boolean isChecked) {

        editor.putInt("speed",0);
        editor.apply();
        if (isChecked) {

            SpeedLayout.setVisibility(View.VISIBLE);

            String vehicle_type = sharedPreferences.getString("vehicle_type","");

            if (vehicle_type.equals("Scooter")){
                SpeedValue.setText(String.valueOf(scooterMinSpeed));

            } else if(vehicle_type.equals("Motorcycle")){
                SpeedValue.setText(String.valueOf(scooterMinSpeed));
            }


            *//*
            try {
                realm.executeTransaction(realm -> {
                    SettingsPojo settingsPojo = realm.where(SettingsPojo.class).equalTo("id", 1).findFirst();

                    if (settingsPojo == null) settingsPojo = realm.createObject(SettingsPojo.class, 1);

                    if (riderProfile.getBike().contentEquals("Scooter")) {

                        etSpeedAlert.setText(String.valueOf(scooterMinSpeed));
                        settingsPojo.setSpeedAlert(scooterMinSpeed);

                    }else if (riderProfile.getBike().contentEquals("Motorcycle")) {

                        etSpeedAlert.setText(String.valueOf(bikeMinSpeed));
                        settingsPojo.setSpeedAlert(bikeMinSpeed);
                    }

                    settingsPojo.setSpeedSet(true);
                    realm.insertOrUpdate(settingsPojo);
                });
            } catch (Exception e) {
                Log.e("update_speed_setting",String.valueOf(e));
            }*//*

        } else {
            SpeedLayout.setVisibility(View.GONE);

            //.putInt("speed",0);
            //new common_DB_transaction().RESET_SPEED_SETTING();
        }
    }*/

    /*public void reset_speed_setting(){
        switchSpeed.setChecked(false);
    }*/

    private boolean check_call_permission() {
        String permission = Manifest.permission.READ_CALL_LOG;
        int res = getContext().checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    private boolean isAllSocialUnchecked() {
        return !switchIncomingCall.isChecked() && !switchIncomingSMS.isChecked() && !switchWhatsappCall.isChecked() && !switchWhatsappMSG.isChecked();
    }

    private void setSpeedWarning() {
        if (riderProfile != null) {
            if (riderProfile.getBike().contentEquals("Scooter")) {
                setMaxSpeed = scooterMaxSpeed;
                isScooterSelected = true;
            } else if (riderProfile.getBike().contentEquals("Motorcycle")) {
                setMaxSpeed = bikeMaxSpeed;
                isBikeSelected = true;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) SpeedSeekbar.setMin(0);

            SpeedSeekbar.setMax(setMaxSpeed);

            if (speedFromDb > 0) SpeedSeekbar.setProgress(speedFromDb);

            //seekBarProgressListener();
            changeInEditText();
            try {
                editTextOkListener();
            } catch (Exception e) {
                Log.e(EXCEPTION, String.valueOf(e));
            }
        }
    }

    private boolean check_permission(String PERMISSION) {
        int res = getContext().checkCallingOrSelfPermission(PERMISSION);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    private void manaul_speed_entry(){
        /*SpeedValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.e("check_call","called");

                if (!charSequence.toString().isEmpty() && speed_value != Integer.parseInt(charSequence.toString())){
                    if (Integer.parseInt(charSequence.toString()) < min_speed){

                        Toast.makeText(getContext(), "Minimum speed is "+String.valueOf(min_speed)+" Kmph", Toast.LENGTH_SHORT).show();
                        speed_value = min_speed;

                    } else if (Integer.parseInt(charSequence.toString()) > max_speed){

                        Toast.makeText(getContext(), "Maximum speed is "+String.valueOf(max_speed)+" Kmph", Toast.LENGTH_SHORT).show();
                        speed_value = max_speed;
                    }

                    SpeedValue.setText(String.valueOf(speed_value));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });*/
    }

    @SuppressLint("SetTextI18n")
    private void editTextOkListener() {
        SpeedValue.setOnEditorActionListener((v, actionId, event) -> {

            String s = SpeedValue.getText().toString().trim();

            if (s.isEmpty()) s = "0";

            int speed = 0;
            if (s.length() > 0) speed = Integer.parseInt(s);

            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_NEXT) {

                speed_value = speed;

                if (speed_value < min_speed){

                    speed_value = min_speed;
                    Toast.makeText(getContext(), "Minimum speed is "+String.valueOf(min_speed)+" kmph", Toast.LENGTH_SHORT).show();

                } else if (speed_value > max_speed){

                    speed_value = max_speed;
                    Toast.makeText(getContext(), "Maximum speed is "+String.valueOf(max_speed)+" kmph", Toast.LENGTH_SHORT).show();
                }

                /*if (isScooterSelected) {
                    if (s.equals("")) {
                        speed = scooterMinSpeed;
                        SpeedValue.setText("" + scooterMinSpeed);
                        Toast.makeText(getContext(), "Minimum speed is "+String.valueOf(min_speed)+" kmph", Toast.LENGTH_SHORT).show();
                    } else if (Integer.parseInt(s) < scooterMinSpeed) {
                        speed = scooterMinSpeed;
                        SpeedValue.setText("" + scooterMinSpeed);
                        Toast.makeText(getContext(), "Minimum speed is "+String.valueOf(min_speed)+" kmph", Toast.LENGTH_SHORT).show();
                        common.showToast("Minimum speed is " + scooterMinSpeed, TOAST_DURATION);
                    }
                } else if (isBikeSelected) {
                    if (s.equals("")) {
                        speed = bikeMinSpeed;
                        SpeedValue.setText("" + bikeMinSpeed);
                        common.showToast("Minimum speed is " + bikeMinSpeed, TOAST_DURATION);
                    } else if (Integer.parseInt(s) < bikeMinSpeed) {
                        speed = bikeMinSpeed;
                        SpeedValue.setText("10");
                        common.showToast("Minimum speed is 10", TOAST_DURATION);
                    }
                }*/
                SpeedValue.setText(String.valueOf(speed_value));
                SpeedSeekbar.setProgress(speed_value);

                speed_value= Integer.parseInt(s);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    getFragmentManager()
                            .beginTransaction().detach(this).commitNow();
                    getFragmentManager().beginTransaction().attach(this).commitNow();
                    Log.i("IsRefresh", "Yes");
                } else {
                    assert getFragmentManager() != null;
                    getFragmentManager().beginTransaction().detach(this).attach(this).commit();
                    Log.i("IsRefresh", "Yes");
                }

                // updateSpeedInDb(speed_value);
                removeFocus();

            }
            return false;
        });
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) {
            if (!isSeekBarTouched) {
                if (s.length() > 0) {
                    int speed;
                    try {
                        speed = Integer.parseInt(s.toString());
                        SpeedSeekbar.setProgress(speed);

                        if (speed >= scooterMaxSpeed) checkMaxSpeedLimit(speed);
                        else SpeedSeekbar.setProgress(speed);

                    } catch (Exception ignored) {

                    }
                } else {
                    SpeedSeekbar.setProgress(0);
                }
            }
        }
    };

    private void changeInEditText() {
        SpeedValue.addTextChangedListener(textWatcher);
    }

//    @SuppressLint("SetTextI18n")
//    private void checkMinSpeedLimit(int speed) {
//        if (isScooterSelected) {
//            if (speed <= scooterMinSpeed) {
//                SpeedSeekbar.setProgress(scooterMinSpeed);
//                SpeedValue.setText("" + scooterMinSpeed);
//                common.showToast("Min Speed Limit is " + scooterMinSpeed, TOAST_DURATION);
//            }
//        }
//        else if (isBikeSelected) {
//            if (speed <= bikeMinSpeed) {
//                SpeedSeekbar.setProgress(bikeMinSpeed);
//                SpeedValue.setText("" + bikeMinSpeed);
//                common.showToast("Min Speed Limit is " + bikeMinSpeed, TOAST_DURATION);
//            }
//        }
//    }

    @SuppressLint("SetTextI18n")
    private void checkMaxSpeedLimit(int speed) {
        if (isScooterSelected) {
            if (speed >= scooterMaxSpeed) {
                SpeedValue.removeTextChangedListener(textWatcher);
                SpeedSeekbar.setProgress(scooterMaxSpeed);
                SpeedValue.setText("" + scooterMaxSpeed);
//                updateSpeedInDb(scooterMaxSpeed);
                removeFocus();
                common.showToast("Max Speed Limit is " + scooterMaxSpeed, TOAST_DURATION);
                SpeedValue.addTextChangedListener(textWatcher);
            }

        } else if (isBikeSelected) {
            if (speed >= bikeMaxSpeed) {
                SpeedValue.removeTextChangedListener(textWatcher);
                SpeedSeekbar.setProgress(bikeMaxSpeed);
                SpeedValue.setText("" + bikeMaxSpeed);
                //updateSpeedInDb(bikeMaxSpeed);
                removeFocus();
                common.showToast("Max Speed Limit is " + bikeMaxSpeed, TOAST_DURATION);
                SpeedValue.addTextChangedListener(textWatcher);
            }
        }
    }

    private void removeFocus() {
        SpeedValue.clearFocus();
        llRedAlertBle.requestFocus();
        if (getContext() != null) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(llRedAlertBle.getWindowToken(), 0);
        }
    }

    @Override
    public void onPause() {
        editor.putInt("speed",speed_value);
        editor.apply();
        super.onPause();
    }

    /*private void seekBarProgressListener() {
     *//*SpeedSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @SuppressLint("SetTextI18n")
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (isSeekBarTouched) SpeedValue.setText("" + progress);
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeekBarTouched = true;
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                checkMinSpeedLimit(seekBar.getProgress());
                updateSpeedInDb(seekBar.getProgress());
                isSeekBarTouched = false;
            }
        });*//*
    }*/

    private void updateSpeedInDb(int speed) {
        try {
            realm.executeTransaction(realm -> {

                SettingsPojo settingsPojo = realm.where(SettingsPojo.class).equalTo("id", 1).findFirst();

                if (settingsPojo == null) settingsPojo = realm.createObject(SettingsPojo.class, 1);

                settingsPojo.setSpeedAlert(speed);
                realm.insertOrUpdate(settingsPojo);
            });
        } catch (Exception ignored) {

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectionEvent(EvenConnectionPojo event) {

        if (!BLUETOOTH_STATE) {
            staticConnectionStatus = false;

            Intent i = new Intent("status").putExtra("status", staticConnectionStatus);
            requireActivity().sendBroadcast(i);

            if (staticConnectionStatus) llRedAlertBle.setVisibility(View.GONE);
            else llRedAlertBle.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        EventBus.getDefault().unregister(this);
    }

    private void initWidgets(View view) {

        switchSocial = view.findViewById(R.id.switchSocial);
//        switchClock = (Switch) view.findViewById(R.id.switchClock);
        SpeedValue = view.findViewById(R.id.SpeedValue);
        SpeedLayout = view.findViewById(R.id.SpeedLayout);
        SpeedSwitch = view.findViewById(R.id.SpeedSwitch);
        llFirstLayout = view.findViewById(R.id.llFirstLayout);
        SpeedSeekbar = view.findViewById(R.id.SpeedSeekbar);
        rlCustom = view.findViewById(R.id.rlCustom);
        switchSms = view.findViewById(R.id.switchSms);
        llSecondLayout = view.findViewById(R.id.llSecondLayout);
        etCustomMsg = view.findViewById(R.id.etCustommsg);
        tvTextCount = view.findViewById(R.id.tvTextCount);
        cbCustomMsgCheck = view.findViewById(R.id.cbCustomMsgCheck);
        cbCallYouBack = view.findViewById(R.id.cbCallYouback);
        cbBusy = view.findViewById(R.id.cbBusy);
        cbRiding = view.findViewById(R.id.cbRiding);
        llEditCustomMsg = view.findViewById(R.id.llEditCustomMsg);
        switchIncomingCall = view.findViewById(R.id.switchIncomingCall);
        switchIncomingSMS = view.findViewById(R.id.switchIncomingSMS);
        switchWhatsappCall = view.findViewById(R.id.switchWhatsappCall);
        switchWhatsappMSG = view.findViewById(R.id.switchWhatsappMSG);
        switchSaveAllTrips = view.findViewById(R.id.switchSaveAllTrips);
        llRedAlertBle = view.findViewById(R.id.llRedAlertBle);

    }

    public void showExitAlert(String message, int PERMISSION_CODE) {
        Dialog dialog = new Dialog(HOME_SCREEN_OBJ, R.style.custom_dialog);
        dialog.setContentView(R.layout.custom_dialog);

        TextView tvAlertText = dialog.findViewById(R.id.tvAlertText);
        tvAlertText.setText(message);
        ImageView ivCross = dialog.findViewById(R.id.ivCross);

        if (PERMISSION_CODE==205) ivCross.setVisibility(View.GONE);

        ivCross.setOnClickListener(v -> {
            dialog.cancel();
            if(PERMISSION_CODE==200) switchSms.setChecked(false);
            else if(PERMISSION_CODE==201) switchIncomingCall.setChecked(false);
            else if(PERMISSION_CODE==202) switchIncomingSMS.setChecked(false);

        });

        ImageView ivCheck = dialog.findViewById(R.id.ivCheck);

        ivCheck.setOnClickListener(v -> {
            dialog.cancel();

            if(PERMISSION_CODE==200){
                String[] perms = {"android.permission.SEND_SMS"};
                requestPermissions(perms, PERMISSION_CODE);
            }else if(PERMISSION_CODE==201){
                permission.add(EasyPermissionList.READ_CALL_LOG);
                permission.add(EasyPermissionList.READ_CONTACTS);
                permission.add(EasyPermissionList.ANSWER_PHONE_CALLS);
                permission.add(EasyPermissionList.MODIFY_PHONE_STATE);
                permission.add(EasyPermissionList.READ_LOGS);
                permission.add(EasyPermissionList.READ_PHONE_NUMBERS);
                permission.add(EasyPermissionList.READ_PHONE_STATE);
                permission.add(EasyPermissionList.PROCESS_OUTGOING_CALLS);
                permission.add(EasyPermissionList.ACCESS_NOTIFICATION_POLICY);
                permission.add(EasyPermissionList.CALL_PHONE);

                new EasyPermissionInit((Activity) getContext(), permission);
            }else if(PERMISSION_CODE==202){
                String[] perms = {"android.permission.RECEIVE_SMS"};

                requestPermissions(perms, PERMISSION_CODE);
            }
        });
        dialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case 200:
                if(grantResults[0] != PackageManager.PERMISSION_GRANTED) switchSms.setChecked(false);

                break;

            case 201:
                if(grantResults[0] != PackageManager.PERMISSION_GRANTED) switchIncomingCall.setChecked(false);

                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        if (buttonView.getId() == cbCustomMsgCheck.getId()) {

            if (cbCustomMsgCheck.isChecked()) {

                cbRiding.setChecked(false);
                cbBusy.setChecked(false);
                cbCallYouBack.setChecked(false);

                llEditCustomMsg.setVisibility(View.VISIBLE);
                new Handler().post(() -> {
                    Realm realm = Realm.getDefaultInstance();
                    try {
                        realm.executeTransaction(realm1 -> {

                            SettingsPojo settingsPojo = realm1.where(SettingsPojo.class).equalTo("id", 1).findFirst();

                            if (settingsPojo == null) {

                                settingsPojo = realm1.createObject(SettingsPojo.class, 1);
                                settingsPojo.setMessage(etCustomMsg.getText().toString());
                                settingsPojo.setCustomMsg(true);
                                settingsPojo.setImRiding(false);
                                settingsPojo.setCallYouback(false);
                                settingsPojo.setImbusy(false);

                                realm1.insertOrUpdate(settingsPojo);
                            } else {
                                settingsPojo.setMessage(etCustomMsg.getText().toString());
                                settingsPojo.setCustomMsg(true);
                                settingsPojo.setImRiding(false);
                                settingsPojo.setCallYouback(false);
                                settingsPojo.setImbusy(false);

                                realm1.insertOrUpdate(settingsPojo);
                            }
                        });

                    } catch (Exception e) {
                    }
                });

            }
            else {
                llEditCustomMsg.setVisibility(View.GONE);
                if (isAllSmsAlertUnchecked()) switchSms.setChecked(false);
            }

        }
        else if (buttonView.getId() == cbRiding.getId()) {
            if (cbRiding.isChecked()) {
                cbCustomMsgCheck.setChecked(false);
                cbBusy.setChecked(false);
                cbCallYouBack.setChecked(false);
                llEditCustomMsg.setVisibility(View.GONE);

                new Handler().post(() -> {
                    Realm realm = Realm.getDefaultInstance();
                    try {
                        realm.executeTransaction(realm12 -> {

                            SettingsPojo settingsPojo = realm12.where(SettingsPojo.class).equalTo("id", 1).findFirst();

                            if (settingsPojo == null) {
                                settingsPojo = realm12.createObject(SettingsPojo.class, 1);
                                settingsPojo.setMessage("I'm Riding");
                                settingsPojo.setImRiding(true);
                                settingsPojo.setImbusy(false);
                                settingsPojo.setCallYouback(false);
                                settingsPojo.setCustomMsg(false);
                                realm12.insertOrUpdate(settingsPojo);

                            }
                            else {
                                settingsPojo.setMessage("I'm Riding");
                                settingsPojo.setImRiding(true);
                                settingsPojo.setImbusy(false);
                                settingsPojo.setCallYouback(false);
                                settingsPojo.setCustomMsg(false);
                                realm12.insertOrUpdate(settingsPojo);
                            }
                        });
                    } catch (Exception ignored) {

                    }
                });
            } else if (isAllSmsAlertUnchecked()) switchSms.setChecked(false);

        } else if (buttonView.getId() == cbCallYouBack.getId()) {
            if (cbCallYouBack.isChecked()) {
                cbCustomMsgCheck.setChecked(false);
                cbBusy.setChecked(false);
                cbRiding.setChecked(false);
                llEditCustomMsg.setVisibility(View.GONE);
                new Handler().post(() -> {
                    Realm realm = Realm.getDefaultInstance();
                    try {
                        realm.executeTransaction(realm13 -> {

                            SettingsPojo settingsPojo = realm13.where(SettingsPojo.class).equalTo("id", 1).findFirst();

                            if (settingsPojo == null) {

                                settingsPojo = realm13.createObject(SettingsPojo.class, 1);
                                settingsPojo.setMessage("I'll Call You Later");
                                settingsPojo.setCallYouback(true);
                                settingsPojo.setImRiding(false);
                                settingsPojo.setImbusy(false);
                                settingsPojo.setCustomMsg(false);

                                realm13.insertOrUpdate(settingsPojo);
                            } else {
                                settingsPojo.setMessage("I'll Call You Later");
                                settingsPojo.setCallYouback(true);
                                settingsPojo.setImRiding(false);
                                settingsPojo.setImbusy(false);
                                settingsPojo.setCustomMsg(false);

                                realm13.insertOrUpdate(settingsPojo);
                            }
                        });
                    } catch (Exception ignored) {

                    }
                });

            } else if (isAllSmsAlertUnchecked()) switchSms.setChecked(false);

        } else if (buttonView.getId() == cbBusy.getId()) {
            if (cbBusy.isChecked()) {
                cbCustomMsgCheck.setChecked(false);
                cbRiding.setChecked(false);
                cbCallYouBack.setChecked(false);
                llEditCustomMsg.setVisibility(View.GONE);

                new Handler().post(() -> {
                    Realm realm = Realm.getDefaultInstance();
                    try {
                        realm.executeTransaction(realm14 -> {

                            SettingsPojo settingsPojo = realm14.where(SettingsPojo.class).equalTo("id", 1).findFirst();

                            if (settingsPojo == null) {

                                settingsPojo = realm14.createObject(SettingsPojo.class, 1);
                                settingsPojo.setMessage("I'm Busy");
                                settingsPojo.setImbusy(true);
                                settingsPojo.setCustomMsg(false);
                                settingsPojo.setImRiding(false);
                                settingsPojo.setCallYouback(false);

                                realm14.insertOrUpdate(settingsPojo);
                            } else {
                                settingsPojo.setMessage("I'm Busy");
                                settingsPojo.setImbusy(true);
                                settingsPojo.setCustomMsg(false);
                                settingsPojo.setImRiding(false);
                                settingsPojo.setCallYouback(false);

                                realm14.insertOrUpdate(settingsPojo);
                            }
                        });
                    } catch (Exception ignored) {

                    }
                });

            } else if (isAllSmsAlertUnchecked()) switchSms.setChecked(false);
        }
    }

    private boolean isAllSmsAlertUnchecked() {
        return !cbRiding.isChecked() && !cbBusy.isChecked() && !cbCallYouBack.isChecked() && !cbCustomMsgCheck.isChecked();
    }

    private void  setBluetoothStatus() {

        if (BLUETOOTH_STATE) {
            if (staticConnectionStatus) llRedAlertBle.setVisibility(View.GONE);
            else llRedAlertBle.setVisibility(View.VISIBLE);

        } else llRedAlertBle.setVisibility(View.VISIBLE);

        IntentFilter intentFilter = new IntentFilter("status");

        mReceiver = new BleConnection() {

            @Override
            public void onReceive(Context context, Intent intent) {

                boolean status = Objects.requireNonNull(intent.getExtras()).getBoolean("status");

                if (BLUETOOTH_STATE) {
                    if (status) llRedAlertBle.setVisibility(View.GONE);
                    else llRedAlertBle.setVisibility(View.VISIBLE);

                } else llRedAlertBle.setVisibility(View.VISIBLE);
            }
        };

        requireActivity().registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        requireActivity().unregisterReceiver(mReceiver);
        realm.close();

    }

    @SuppressLint("SetTextI18n")
    /*private void viewProfileRecord() {

        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("vehicle_data",Context.MODE_PRIVATE);

        if(sharedPreferences.getString("vehicle_type","").equals("Scooter")){

        } else if (sharedPreferences.getString("vehicle_type","").equals("Motorcycle")){

        }

        sharedPreferences.getInt("speed",0);

        *//*
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(realm1 -> {

                riderProfile = realm1.where(RiderProfileModule.class).equalTo("id", 1).findFirst();

                if (riderProfile != null) {
                    if (riderProfile.getBike().contentEquals("Scooter")) etSpeedAlert.setText("" + scooterMinSpeed);
                    else if (riderProfile.getBike().contentEquals("Bike")) etSpeedAlert.setText("" + bikeMinSpeed);
                }
            });
        } catch (Exception ignored) {

        }*//*
    }*/

    @Override
    public void onClick(View view) { }
}