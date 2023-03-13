package com.suzuki.utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;

import com.clj.fastble.data.BleDevice;
import com.google.android.material.snackbar.Snackbar;
import com.suzuki.R;
import com.suzuki.activity.ProfileActivity;
import com.suzuki.pojo.RiderProfileModule;

import java.util.HashMap;

import io.realm.Realm;

public class Common {

    //TEMP
    /*public static boolean PACKET_TIME_GAP_ACTIVE = false;
    public static int INTER_FRAME_DELAY = 0;//in milli seconds
    private RiderProfileModule riderProfile;
    private SettingsPojo speed_settings;
    private BleManager bleManager;*/

    private Context context;
    private long mLastClickTime = 0;
    //public static long EXECUTION_TIMESTAMP=0;    //this indicates the transmission time of last packet.
    public static boolean CALL_ACTIVE=false;
    public static int DUMMY_VALUE=10101010;
    public static boolean PRICOL_CONNECTED=false;
    public static boolean MARELLI_CONNECTED=false;

    public static int STATUS_PACKET_DELAY=5000;
    public static BleDevice global_bleDevice;
    public static int missedcall_numbers=0;
    public static String bleName_common="", EXCEPTION="EXCEPTION: ", MISSED_NUMBER=" ", USER_NAME = "0x01E";
    public static long MISSEDCALL_TIME=1;
    public static int unread_sms=0;
    private Realm realm;
    public static int MISSED_CALL_COUNT=0, w_MISSED_CALL_COUNT=0;
    public static int SMS_COUNTER=0, w_MSG_COUNTER=0;
    public static long SMS_POSTED=1, w_call_posted=1;
    public static HashMap<String, Integer> MISSED_CALL_RECORD;
    public static boolean DATA_CHANGED=false, LAST_CALL_TYPE_IS_WHATSAPP=false;
    public static boolean BROADCAST_RECEIVED=false, BLE_ENABLED = true, SPEED_FLAG = false, FIRST_TIME = true;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    public static MutableLiveData<String> BikeBleName;

    public Common(Context context){
        this.context = context;
    }

    public static boolean cluster_a_connected=true;

    public boolean is_vehicle_type_changed(){
        realm = Realm.getDefaultInstance();
        RiderProfileModule riderProfile = realm.where(RiderProfileModule.class).equalTo("id", 1).findFirst();

     //   if(BikeBleName.getValue().contains("SCE01") && riderProfile.getBike().equals("Scooter") ||
              //  BikeBleName.getValue().contains("SCE01") && riderProfile.getBike().equals("Motorcycle")) return true;
       if(BikeBleName.getValue().contains("SBM") && riderProfile.getBike().equals("Scooter") ||
                BikeBleName.getValue().contains("SBS") && riderProfile.getBike().equals("Motorcycle")) return true;

        return false;
    }

    /**
     * execution time = last packet transmission time.
     * this method will check of the last execution time in milliseconds, if its zero then return 0 as delay for current packet transmission.
     * if its not zero, then it'll check the diff between last execution time and the current packet arrival time to check if there's a gap of 100ms,
     * if there's a gap of atleast 100ms between last packet transmission and current packet arriaval, then current packet will be transmitted immediately, with-
     * zero delay.
     * else if there's a gap of less than 100ms or still packet transmissions are in progress then,
     * a 300ms delay is added to the next slot in the queue and the time delay till that slot will give the delay required for current packet tranmission.
     * @return returns the delay to be assigned for current packet tranmission.
     */
    /*public long generate_delay(){
        long arrival_time= System.currentTimeMillis();

        if(EXECUTION_TIMESTAMP!=0){
            long diff= EXECUTION_TIMESTAMP - arrival_time;

            if(diff<=-100){
                EXECUTION_TIMESTAMP = arrival_time;
                Log.e("tranmission_delay","zero");
                return 0;
            } else{
                EXECUTION_TIMESTAMP = EXECUTION_TIMESTAMP + 300;
                Log.e("tranmission_delay",String.valueOf(EXECUTION_TIMESTAMP - arrival_time));
                return EXECUTION_TIMESTAMP - arrival_time;
            }
        }else{
            EXECUTION_TIMESTAMP=arrival_time;
            return 0;
        }

    }*/
    /*public int generate_delay(){
        if(INTER_FRAME_DELAY==900) INTER_FRAME_DELAY=0;
        INTER_FRAME_DELAY=INTER_FRAME_DELAY+100;
        return INTER_FRAME_DELAY;
    }*/

    public void show_alert(View view, int message, int duration, String action_button_name){
        Snackbar snackbar= Snackbar.make(view, message, duration);
        snackbar.setAction(action_button_name, view1 -> {

            switch (message){
                case R.string.change_colour:
                    view1.getContext().startActivity(new Intent(view1.getContext(), ProfileActivity.class));
                    break;
            }
        })
                .setActionTextColor(Color.parseColor("#17B5D0"))
                .show();
    }

    public void update_user_data(String name, String location){
        sharedPreferences = context.getSharedPreferences("user_data", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.putString("name",name);
        editor.putString("location",location);
        editor.apply();
    }

    public void update_vehicle_data(String vehicleType, String model, int variant){
        sharedPreferences = context.getSharedPreferences("vehicle_data", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        if (sharedPreferences.getString("vehicle_type","").equals(vehicleType)) editor.putInt("speed",sharedPreferences.getInt("speed",0));
        else{
            editor.putInt("speed",0);
            SPEED_FLAG = true;
        }

        editor.putString("vehicle_type",vehicleType);
        editor.putString("vehicle_name",model);
        editor.putInt("vehicle_model",variant);

        editor.apply();
    }

    public void showToast(String msg, int duration){
        Toast toast = Toast.makeText(context,msg,Toast.LENGTH_SHORT);
        toast.show();

        new Handler().postDelayed(toast::cancel,duration);
    }

    public boolean isDoubleClicked(){
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) return true;

        mLastClickTime = SystemClock.elapsedRealtime();

        return false;
    }

    public boolean checkGPSIsOpen(Context context) {
        if (context == null) return false;

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (locationManager == null) return false;

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    //TEMP
    /*private void setMtu(BleManager bleManager,BleDevice bleDevice, int mtu) {
        bleManager.setMtu(bleDevice, mtu, new BleMtuChangedCallback() {
            @Override
            public void onSetMTUFailure(BleException exception) {

            }

            @Override
            public void onMtuChanged(int mtu) {

            }
        });
    }*/
}
