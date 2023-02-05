package com.suzuki.services;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.exception.BleException;

import java.util.Timer;
import java.util.TimerTask;

import timber.log.Timber;

import static com.suzuki.activity.HomeScreenActivity.CALL_CLEAR;
import static com.suzuki.activity.HomeScreenActivity.MSG_CLEAR;
import static com.suzuki.activity.HomeScreenActivity.mBoundService;
import static com.suzuki.application.SuzukiApplication.bleDevice;
import static com.suzuki.application.SuzukiApplication.calculateCheckSum;

import static com.suzuki.fragment.DashboardFragment.app;

public class work extends Worker{

    public work(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Log.e("work","works");
                byte[] PKT = GetSmartPhoneStatusPkt("data");
            /*WorkRequest workRequest= new OneTimeWorkRequest.Builder(work.class).build();
            WorkManager.getInstance(getContext()).enqueue(workRequest);*/
//            logData("SmartPhonePkt : " + Arrays.toString(PKT));

                try{
                    for(int i=0;i<3;i++) writeDataFromAPPtoDevice(PKT,1);
                }catch (Exception ignored){ }
            }
        },0,1000);

        return null;
    }

    public void writeDataFromAPPtoDevice(byte[] PKT, int Flag) {

        if(Flag==5) Log.e("BLEservice","Navigation_packet");
        else if(Flag==1) Log.e("BLEservice","Status_packet");

        try {
            BluetoothGattCharacteristic characteristic = app.getBluetoothGattService().getCharacteristics().get(0);

            BleManager.getInstance().write(
                    bleDevice,
                    characteristic.getService().getUuid().toString(),
                    characteristic.getUuid().toString(),
                    PKT, false,
                    new BleWriteCallback() {

                        @Override
                        public void onWriteSuccess(final int current, final int total, final byte[] justWrite) {
                            Log.e("transmission","writesuccess");
                        }

                        @Override
                        public void onWriteFailure(final BleException exception) {
                            Timber.e(exception.toString());
                            //Log.d(TAG, "onWriteSuccess: mGattCallback data mobile status service error");
                        }
                    });

        } catch (Exception e) {
            e.getMessage();
        }

    }
    public byte[] GetSmartPhoneStatusPkt(String time) {
        String currentBatteryStatus="1Y",speedAlertForDashboard="000",signal="2",finaltime="111837";

        String PhoneData;
        byte[] RetArray = new byte[30];
        String data = currentBatteryStatus + speedAlertForDashboard + signal + finaltime;

        try {
            if (data.length() <= 12) {

//            time += "\0";
                for (int i = data.length(); i <= 12; i++) {
//                Log.d("Rets", "-: " + RetArray[i]);
                    data += "\0";
                }
                PhoneData = "?"/*Start of Frame*/ + "3"/*Frame ID*/ + currentBatteryStatus + speedAlertForDashboard + signal + finaltime/*Data to icd*/ /* */ + "0000000000000000"/*Fill 0s at reserved byte positions*/ /*End of Frame*/;
//            PhoneData = "?"/*Start of Frame*/ + "3"/*Frame ID*/ + time/*Data to icd*/ /* */ + "\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0"/*Fill 0s at reserved byte positions*/ /*End of Frame*/;
//            ÿ

            } else {

                data = data.substring(0, 12);
                PhoneData = "?"/*Start of Frame*/ + "3"/*Frame ID*/ + currentBatteryStatus + speedAlertForDashboard + signal + finaltime/*Phone data to icd*/ + "0000000000000000"/*Fill 0s at reserved byte positions*/ /*End of Frame*/;
            }

            try {

                RetArray = PhoneData.getBytes("UTF-8");
                RetArray[0] = (byte) 0xA5;


                boolean synccc=false;
                if (synccc == false) {
                    RetArray[4] = (byte) 0xFF;
                    RetArray[5] = (byte) 0xFF;
                    RetArray[6] = (byte) 0xFF;
                }
                if (finaltime.contentEquals("000000")) {
                    for (int i = 8; i <= 13; i++) {
                        RetArray[i] = (byte) 0xFF;
                    }
                }

                for (int k = 14; k <= 27; k++) {
                    RetArray[k] = (byte) 0xFF;
                }

                // msg clear flag
                RetArray[14] = MSG_CLEAR;

                // missed call clear flag
                RetArray[15] = CALL_CLEAR;

                RetArray[28] = calculateCheckSum(RetArray);
//            RetArray[28] =(byte) 0xFF;

                RetArray[29] = (byte) 0x7F;

//            calculateCheckSum(RetArray);

//            Log.d("Phbtry Checksum pkt", "- " + RetArray[28]);
                Log.d("PhonePkt", "Phone Smart status pkt- " + new String(RetArray));


                return RetArray;
            } catch (java.io.IOException e) {
                return RetArray;
            }
        } catch (Exception e) {

            return RetArray;
        }

    }
}