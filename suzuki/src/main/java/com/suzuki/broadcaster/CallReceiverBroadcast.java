package com.suzuki.broadcaster;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.telecom.TelecomManager;
//import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;

import com.android.internal.telephony.ITelephony;
import com.google.api.LogDescriptor;
import com.suzuki.pojo.SettingsPojo;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

import static com.suzuki.activity.HomeScreenActivity.CALL_CLEAR;
import static com.suzuki.activity.HomeScreenActivity.DELAY_DURATION;
import static com.suzuki.activity.HomeScreenActivity.mBoundService;
import static com.suzuki.application.SuzukiApplication.calculateCheckSum;
import static com.suzuki.fragment.DashboardFragment.staticConnectionStatus;
import static com.suzuki.utils.Common.CALL_ACTIVE;
import static com.suzuki.utils.Common.DATA_CHANGED;
import static com.suzuki.utils.Common.EXCEPTION;
import static com.suzuki.utils.Common.LAST_CALL_TYPE_IS_WHATSAPP;
import static com.suzuki.utils.Common.MISSEDCALL_TIME;
import static com.suzuki.utils.Common.MISSED_CALL_COUNT;
import static com.suzuki.utils.Common.MISSED_CALL_RECORD;
import static com.suzuki.utils.Common.MISSED_NUMBER;

public class CallReceiverBroadcast extends BroadcastReceiver {

    //TEMP
    /*private Handler handler=new Handler();*/

    private Timer timer;
    public static Timer callClearTimer;
    static String savedNumber, incomingNumber;
    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    Realm realm;
    RealmResults<SettingsPojo> settingsPojos;
    ITelephony telephonyService;
    String message = "";
    Context callContext;
    private boolean isIncoming = false, sendMessage, incomingcallEnabled = false, isAutoReply = false;
    private int missedcount=1;
    private long check_;
    //private PhonecallStartEndDetector listner;

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.e("CallBroadcast","OnReceive");

//        if(listner== null) listner = new PhonecallStartEndDetector();

        sendMessage = true;
        this.callContext = context;
        realm = Realm.getDefaultInstance();

        settingsPojos = realm.where(SettingsPojo.class).findAll();

        try{
            timer.cancel();
            timer.purge();
        }catch (Exception ignored) {}

        if (callClearTimer == null) callClearTimer = new Timer();
        else {
            callClearTimer.cancel();
            callClearTimer.purge();
            callClearTimer = null;
            callClearTimer = new Timer();
        }

        settingsPojos.addChangeListener(new RealmChangeListener<RealmResults<SettingsPojo>>() {
            @Override
            public void onChange(RealmResults<SettingsPojo> settingsPojos) {
                for (int i = 0; i < settingsPojos.size(); i++) {

                    isAutoReply = settingsPojos.get(0).isAutoReplySMS();
                    message = settingsPojos.get(0).getMessage();
                    incomingcallEnabled = settingsPojos.get(0).isIncomingCall();
                }
            }
        });

        SettingsPojo settingsPojo = realm.where(SettingsPojo.class).equalTo("id", 1).findFirst();

        if (settingsPojo != null) {
            isAutoReply = settingsPojos.get(0).isAutoReplySMS();
            message = settingsPojo.getMessage();
            incomingcallEnabled = settingsPojo.isIncomingCall();
        }

        if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
        else {
            String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
            String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);

            int state = 0;
            if (stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                state = TelephonyManager.CALL_STATE_IDLE;

                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    isIncoming = true;
                    if (number != null) {
                        if (!getContactName(number, context).equals("")) savedNumber = numberNameValidation(number, context);
                        else savedNumber = number;

                        String PATH = "content://call_log/calls";

                        String[] projection = new String[]{CallLog.Calls.CACHED_NAME, CallLog.Calls.NUMBER, CallLog.Calls.DATE, CallLog.Calls.TYPE};

//                        String sortOrder = CallLog.Calls.DATE + " DESC";
                        String sortOrder = CallLog.Calls.DATE + " DESC";

                        StringBuffer sb = new StringBuffer();
                        sb.append(CallLog.Calls.TYPE).append("=?").append(" and ").append(CallLog.Calls.IS_READ).append("=?");

                        Cursor cursor = context.getContentResolver().query(
                                Uri.parse(PATH),
                                projection,
                                sb.toString(),
                                new String[]{String.valueOf(CallLog.Calls.MISSED_TYPE), "0"}, sortOrder);

                        cursor.moveToFirst();
                        int cursorCount = cursor.getCount();

                        missedcount = 0;
                        if (cursor.getCount() == 0) missedcount = 0;
                        else missedcount = cursorCount++;

                        if (staticConnectionStatus && incomingcallEnabled) {
                            if(MISSED_CALL_RECORD==null) MISSED_CALL_RECORD = new HashMap<>();

                            String CHECK = savedNumber.toLowerCase();

                            if (MISSED_CALL_RECORD.containsKey(CHECK)) MISSED_CALL_RECORD.put(CHECK, MISSED_CALL_RECORD.get(CHECK)+1);
                            else MISSED_CALL_RECORD.put(CHECK.toLowerCase(), 1);
                            Log.d("miscall number", savedNumber);
                            savedNumber = numberNameValidation(number, context);
                            Log.d("xtmisscall", savedNumber);
                            sendMISSEDCAllDatatoDashboard(savedNumber, "Y", ++MISSED_CALL_COUNT);

                            if (callClearTimer != null) checkMissedCall(context);
                        }
                    }
                }
            } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                state = TelephonyManager.CALL_STATE_OFFHOOK;

                if (staticConnectionStatus) {
                    if (incomingcallEnabled) {

                        if (isAutoReply && isIncoming) {

                            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                            try {

                                Method m = Class.forName(tm.getClass().getName()).getDeclaredMethod("getITelephony");

                                m.setAccessible(true);
                                telephonyService = (ITelephony) m.invoke(tm);

                                if ((number != null)) {
                                    telephonyService.endCall();
                                    sendSMS(number);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
//                                endCall2();
                                TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    try {
                                        telecomManager.endCall();
                                    } catch (SecurityException e1) { }
                                }
                                if ((number != null)) {
                                    sendSMS(number);
                                    Log.d("tag", "sendSMS onReceive: 2 offhook");
                                }
                            }
                        } else {
                            if (lastState != 0 && number != null) {

                                incomingNumber = numberNameValidation(number, context);

                                sendCAllDatatoDashboard(incomingNumber, "2");
                            }
                        }
                    }
                } else if (incomingcallEnabled == false) {

                    if (staticConnectionStatus) {
                        if (isAutoReply && isIncoming) {

                            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                            try {
                                Method m = Class.forName(tm.getClass().getName()).getDeclaredMethod("getITelephony");

                                m.setAccessible(true);
                                telephonyService = (ITelephony) m.invoke(tm);

                                if ((number != null)) {
                                    telephonyService.endCall();
                                    sendSMS(number);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                                endCall2();
                                if ((number != null)) {
                                    sendSMS(number);
                                }
                                TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    try {
                                        telecomManager.endCall();
                                    } catch (SecurityException e1) {
                                    }
                                }
                            }
                        }
                    }
                }

            } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                state = TelephonyManager.CALL_STATE_RINGING;

                isIncoming = true;

                if (staticConnectionStatus) {
                    if (incomingcallEnabled) {
                        if (isAutoReply && isIncoming) {

                            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                            try {
                                Method m = Class.forName(tm.getClass().getName()).getDeclaredMethod("getITelephony");

                                m.setAccessible(true);
                                telephonyService = (ITelephony) m.invoke(tm);

                                if ((number != null)) {
                                    telephonyService.endCall();
                                    sendSMS(number);
                                    Log.d("tag", "sendSMS onReceive: 1 ringing");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                endCall2();
                                if ((number != null)) sendSMS(number);

                                Log.d("tag", "sendSMS onReceive: 2 ringing");
                                TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    try {
                                        telecomManager.endCall();
                                    } catch (SecurityException e1) {
                                    }
                                }
                            }
                        } else {

                            if (number != null) {
                                incomingNumber = numberNameValidation(number, context);

                                sendCAllDatatoDashboard(incomingNumber, "1");
                            }
                        }
                    } else if (incomingcallEnabled == false) {

                        if (staticConnectionStatus) {
                            if (isAutoReply && isIncoming) {

                                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                                try {
                                    Method m = Class.forName(tm.getClass().getName()).getDeclaredMethod("getITelephony");

                                    m.setAccessible(true);
                                    telephonyService = (ITelephony) m.invoke(tm);

                                    if ((number != null)) {
                                        telephonyService.endCall();
                                        sendSMS(number);
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    endCall2();
                                    if ((number != null)) {
                                        sendSMS(number);
                                    }
                                    TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                        try {
                                            telecomManager.endCall();
                                        } catch (SecurityException e1) { }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            onCallStateChanged(context, state, number);
        }


    }



    /*public class PhonecallStartEndDetector extends PhoneStateListener {
        int lastState = TelephonyManager.CALL_STATE_IDLE;

        //Incoming call-  goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when its hung up
        //Outgoing call-  goes from IDLE to OFFHOOK when it dials out, to IDLE when hung up
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);

            switch (state) {

                case TelephonyManager.CALL_STATE_IDLE:
                    //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                    Log.e("call_test","done");
                    break;
            }
            lastState = state;
        }

    }*/

    private void checkMissedCall(Context context) {
        try {
            if (callClearTimer != null) {
                callClearTimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        int missedCall = getMissedCall(context);

                        if(DATA_CHANGED){
                            sendMISSEDCAllDatatoDashboard(savedNumber, "Y", MISSED_CALL_COUNT);
                            DATA_CHANGED = false;
                            Log.e("ArraySize","data_sent: " + String.valueOf(MISSED_CALL_COUNT));
                        }

                        Log.e("BLEservice","mc: "+String.valueOf(missedCall)+":::::"+"clear: "+String.valueOf(CALL_CLEAR));
                        if (missedCall == 0 || CALL_CLEAR == 0x59) {
                            Log.e("BLEservice","cleared");
                            CALL_CLEAR = 0x59;
                            MISSED_CALL_COUNT = 0;

                            if(callClearTimer!=null){
                                callClearTimer.cancel();
                                callClearTimer.purge();
                                callClearTimer = null;
                            }
                        } else CALL_CLEAR = 0x4E;

                    }
                }, DELAY_DURATION, DELAY_DURATION);
            }
        } catch (Exception e) {
            Log.e(EXCEPTION,"CallReceiverBroadcast: check:" + String.valueOf(e));
        }
    }

    private int getMissedCall(Context context) {
        int missedCall = 2; // for safer side
        String PATH = "content://call_log/calls";

        String[] projection = new String[]{CallLog.Calls.CACHED_NAME,
                CallLog.Calls.NUMBER, CallLog.Calls.DATE, CallLog.Calls.TYPE};

//                        String sortOrder = CallLog.Calls.DATE + " DESC";
        String sortOrder = CallLog.Calls.DATE + " DESC";

        StringBuffer sb = new StringBuffer();
        sb.append(CallLog.Calls.TYPE).append("=?").append(" and ").append(CallLog.Calls.IS_READ).append("=?");

        Cursor cursor = context.getContentResolver().query(
                Uri.parse(PATH),
                projection,
                sb.toString(),
                new String[]{String.valueOf(CallLog.Calls.MISSED_TYPE), "0"}, sortOrder);

        if (cursor != null) {
            cursor.moveToFirst();
            missedCall = cursor.getCount();
        }
        return missedCall;
    }

    public void onCallStateChanged(Context context, int state, String number) {
        if (lastState == state) {
            //No change, debounce extras
            return;
        }
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                isIncoming = true;

                if (staticConnectionStatus) {

                    if (incomingcallEnabled) {

                        if (isAutoReply && isIncoming) {

                            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                            try {
                                Method m = Class.forName(tm.getClass().getName()).getDeclaredMethod("getITelephony");

                                m.setAccessible(true);
                                telephonyService = (ITelephony) m.invoke(tm);

                                if ((number != null)) {
                                    telephonyService.endCall();
                                    sendSMS(number);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                                endCall2();
                                if ((number != null)) {
                                    sendSMS(number);
                                }
                                TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    try {
                                        telecomManager.endCall();
                                    } catch (SecurityException e1) { }
                                }
                            }
                        } else {

                            if (number != null) {
                                incomingNumber = numberNameValidation(number, context);

                                sendCAllDatatoDashboard(incomingNumber, "1");
                            }
                        }
                    } else if (!incomingcallEnabled) {

                        if (staticConnectionStatus) {
                            if (isAutoReply && isIncoming) {
                                
                                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                                try {
                                    Method m = Class.forName(tm.getClass().getName()).getDeclaredMethod("getITelephony");

                                    m.setAccessible(true);
                                    telephonyService = (ITelephony) m.invoke(tm);

                                    if ((number != null)) {
                                        telephonyService.endCall();
                                        sendSMS(number);
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    endCall2();
                                    if ((number != null)) {
                                        sendSMS(number);
                                    }
                                    TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                        try {
                                            telecomManager.endCall();
                                        } catch (SecurityException e1) {
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:

                if (staticConnectionStatus) {
                    if (incomingcallEnabled) {

                        if (isAutoReply && isIncoming) {

                            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                            try {
                                Method m = Class.forName(tm.getClass().getName()).getDeclaredMethod("getITelephony");

                                m.setAccessible(true);
                                telephonyService = (ITelephony) m.invoke(tm);

                                if ((number != null)) {
                                    telephonyService.endCall();
                                    sendSMS(number);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                                endCall2();
                                if ((number != null)) {
                                    sendSMS(number);
                                }
                                TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    try {
                                        telecomManager.endCall();
                                    } catch (SecurityException e1) {
                                    }
                                }
                            }
                        } else {
                            if (lastState != 0 && state != 2 && number != null) {
                                incomingNumber = numberNameValidation(number, context);

                                sendCAllDatatoDashboard(incomingNumber, "2");
                            }
                        }
                    } else if (incomingcallEnabled == false) {

                        if (staticConnectionStatus) {
                            if (isAutoReply && isIncoming) {

                                TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                                try {
                                    Method m = Class.forName(tm.getClass().getName()).getDeclaredMethod("getITelephony");

                                    m.setAccessible(true);
                                    telephonyService = (ITelephony) m.invoke(tm);

                                    if ((number != null)) {
                                        telephonyService.endCall();
                                        sendSMS(number);
                                    }

                                } catch (Exception e) {
                                    Log.d("sjsjjsjsjsjs", "--oo" + e.getMessage());
                                    //  endCall2();
                                    if ((number != null)) {
                                        sendSMS(number);
                                    }
                                    TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                        try {
                                            telecomManager.endCall();
                                        } catch (SecurityException e1) {
                                        }
                                    }
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
                //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                    isIncoming = false;
                }

                break;
            case TelephonyManager.CALL_STATE_IDLE:
                isIncoming = false;

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        CALL_ACTIVE = false;
                    }
                },1000);

                Log.e("call_check_B",String.valueOf(CALL_ACTIVE));

                break;
        }

        if (number != null) lastState = state;
    }

    public String getContactName(final String phoneNumber, Context context) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));

        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

        String contactName = "";
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                contactName = cursor.getString(0);
            }
            cursor.close();
        }
        Log.d("getContactNameContext", String.valueOf(context));
        return contactName;
    }

    public void sendCAllDatatoDashboard(String dataToSend, String callStatus) {
        byte[] PKT = getSmartPhoneCALLPKT(dataToSend, "N", callStatus);

        CALL_ACTIVE=true;
        Log.e("call_check_B",String.valueOf(CALL_ACTIVE));
        for(int i=2; i<5; i++){
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (mBoundService != null) mBoundService.writeDataFromAPPtoDevice(PKT,2);
        }
    }

    public void sendMISSEDCAllDatatoDashboard(String dataToSend, String status, int count) {
        MISSEDCALL_TIME = System.currentTimeMillis();
//        Log.e("just","works");
        CALL_CLEAR = 0x4E;
        byte[] PKT = getSmartMISSEDPhoneCALLPKT(dataToSend, status, count);
        LAST_CALL_TYPE_IS_WHATSAPP = false;
        MISSED_NUMBER = dataToSend;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                CALL_ACTIVE = false;
            }
        },1000);
        check_=System.currentTimeMillis();
        Log.e("call_check_B2",String.valueOf(CALL_ACTIVE));

        for(int i=2; i<5; i++){
            try {
                Thread.sleep(200);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (mBoundService != null && !isAutoReply) mBoundService.writeDataFromAPPtoDevice(PKT,22);
        }
    }

    public byte[] getSmartPhoneCALLPKT(String data, String incomingCallType, String callActivSTatus) {
        String PhoneData;
        byte[] RetArray = new byte[30];

        try {
            if (data.length() <= 20) {

                for (int i = data.length(); i <= 20; i++) {
                    data += "\0";
                }


                PhoneData = "?"/*Start of Frame*/ + "2"/*Frame ID*/ + data/*Data to icd*/ /* */ + "N1"/*Fill 0s at reserved byte positions*/ /*End of Frame*/ + "\0\0\0\0\0";

            } else {

                data = data.substring(0, 20);
                PhoneData = "?"/*Start of Frame*/ + "2"/*Frame ID*/ + data/*Phone data to icd*/ + "N1"/*Fill 0s at reserved byte positions*//*End of Frame*/ + "\0\0\0\0\0";
            }

            try {

                RetArray = PhoneData.getBytes("UTF-8");
                RetArray[0] = (byte) 0xA5;

                RetArray[22] = (byte) 0x4e;
                if (callActivSTatus == "2" && lastState != TelephonyManager.CALL_STATE_IDLE) RetArray[23] = (byte) 0x32;
                else RetArray[23] = (byte) 0x31;

                for (int k = 24; k <= 27; k++) {
                    RetArray[k] = (byte) 0xFF;
                }

                RetArray[28] = calculateCheckSum(RetArray);

                RetArray[29] = (byte) 0x7F;

                return RetArray;
            } catch (java.io.IOException e) {
                return RetArray;
            }
        } catch (Exception e) {
            return RetArray;
        }
    }

    public byte[] getSmartMISSEDPhoneCALLPKT(String data, String status, int count) {
        String PhoneData;
        byte[] RetArray = new byte[30];
        String alteredData = "Y" + "1" + data;

        try {
            if (alteredData.length() <= 22) {

                for (int i = alteredData.length(); i <= 22; i++) {
                    alteredData += "\0";
                }
                PhoneData = "?"/*Start of Frame*/ + "4"/*Frame ID*/ + alteredData +/*Data to icd*/ /* */  "\0\0\0\0\0"/*Fill 0s at reserved byte positions*/ /*End of Frame*/;

            } else {
                alteredData = alteredData.substring(0, 22);
                PhoneData = "?"/*Start of Frame*/ + "4"/*Frame ID*/ + alteredData +/*Phone data to icd*/  "\0\0\0\0\0"/*Fill 0s at reserved byte positions*/ /*End of Frame*/;
            }

            try {

                RetArray = PhoneData.getBytes("UTF-8");
                RetArray[0] = (byte) 0xA5;
                RetArray[2] = (byte) 0xFF;
                Log.e("missedcall_check",String.valueOf(count));
                RetArray[3] = (byte) count;

                for (int k = 25; k <= 27; k++) {
                    RetArray[k] = (byte) 0xFF;
                }
                RetArray[24] = (byte) 0x4E;

                RetArray[28] = calculateCheckSum(RetArray);

                RetArray[29] = (byte) 0x7F;


            } catch (java.io.IOException e) {

                return RetArray;
            }

        } catch (Exception e) {
            Log.d("sjjsjsjsjs", "----" + e.getMessage());
        }
        return RetArray;
    }

    private void sendSMS(String number) {
        if (sendMessage) {
            sendMessage = false;
            try {
                SmsManager sms = SmsManager.getDefault();
                sms.sendTextMessage(number, null, message, null, null);
            } catch (Exception e) {
                e.getMessage();
            }
        }
    }

    @NotNull
    private String remove_invalid_char(String title) {
        char[] check = title.toCharArray();

        for (int i=0; i<title.length();i++){
            int k=title.charAt(i);
            if(k>127){
                check[i]=' ';
                if(k>8500){
                    i++;
                    check[i]=' ';
                }
            }
        }
        title=String.valueOf(check);
        title=title.replace("  "," ");
        return title;
    }

    public String numberNameValidation(String number, Context context) {

        String incomingNumber = number;
        if (!getContactName(number, context).equals("")) {
            incomingNumber = getContactName(number, context);

            incomingNumber = incomingNumber.replaceAll("[!#&$%'(;)<=>?@`{|}~.^,]", " ");

            incomingNumber=remove_invalid_char(incomingNumber);

            Pattern pattern = Pattern.compile("[^\\x00-\\x7F]");
            Matcher matchers = pattern.matcher(incomingNumber);
            boolean hasSpecialChars = matchers.find();

            if (incomingNumber.length() >= 20) {
                incomingNumber = incomingNumber.substring(0, 19);
            }
        } else {
            incomingNumber = incomingNumber.trim();
            incomingNumber = number.replace("+", "");
            if (incomingNumber.length() < 1) {
                if (number.contains("+")) {
                    incomingNumber = number.replace("+", "");
                }
            }
        }

        return incomingNumber;
    }


    private void endCall2() {
        try {
            Intent buttonUp = new Intent(Intent.ACTION_MEDIA_BUTTON);
            buttonUp.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK));
            callContext.sendOrderedBroadcast(buttonUp, "android.permission.CALL_PRIVILEGED");
        } catch (SecurityException e) {
        }
    }

    //TEMP
    /*private void disconnectCall(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        Class clazz = null;
        try {
            clazz = Class.forName(telephonyManager.getClass().getName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }
        Method method = null;
        try {
            method = clazz.getDeclaredMethod("getITelephony");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return;
        }
        method.setAccessible(true);
        ITelephony telephonyService = null;
        try {
            telephonyService = (ITelephony) method.invoke(telephonyManager);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return;
        }

        telephonyService.endCall();

    }*/
    //    public String numberNameValidation(String number, Context context) {
//
//        String incomingNumber = number;
//        if (getContactName(number, context) != "") {
//            incomingNumber = getContactName(number, context);
//
//
//            Pattern pattern = Pattern.compile("[^A-Za-z0-9 ]");
//            Matcher matchers = pattern.matcher(incomingNumber);
//            boolean hasSpecialChars = matchers.find();
//            Log.d("skksksksks", "===" + hasSpecialChars);
//
//
//            if (hasSpecialChars) {
//
//                incomingNumber = number;
//
//
//                if (incomingNumber.contains("+")) {
//                    incomingNumber = number.replace("+", "");
//                }
//            } else {
//                incomingNumber = getContactName(number, context);
//            }
//        } else {
//            incomingNumber = number.replace("+", "");
//
//        }
//
//
////            incomingNumber = incomingNumber.replaceAll("[^a-zA-Z]", "");
////            if (incomingNumber.length() == 0) {
////                incomingNumber = number;
////                if (incomingNumber.contains("+")) {
////                    incomingNumber = number.replace("+", "");
////
////
////                } else {
////
////                    incomingNumber = number;
////                }
////
////            } else {
////                incomingNumber = getContactName(number, context);
////            }
////
////
////        } else {
////            incomingNumber = incomingNumber.replace("+", "");
////
////        }
//
//        Log.d("kskskskskksks", "--" + incomingNumber);
//        return incomingNumber;
//    }


//    public String numberNameValidation(String number, Context context) {
//
//        String incomingNumber = number;
//        if (getContactName(number, context) != "") {
//            incomingNumber = getContactName(number, context);
//
//
//            incomingNumber = incomingNumber.replaceAll("[^a-zA-Z]", "");
//            if (incomingNumber.length() == 0) {
//                incomingNumber = number;
//                if (incomingNumber.contains("+")) {
//                    incomingNumber = number.replace("+", "");
//
//
//                } else {
//
//                    incomingNumber = number;
//                }
//
//            } else {
//                incomingNumber = getContactName(number, context);
//            }
//
//
//        } else {
//            incomingNumber = incomingNumber.replace("+", "");
//
//        }
//        return incomingNumber;
//    }
    /*private void updateDisplay(final String datatoSend, String callStatus) {

//        final Timer timer = new Timer();
        Log.e("transmission_call",String.valueOf(callStatus));
        try {
                sendCAllDatatoDashboard(datatoSend, callStatus);

            *//*if (timer != null) {

                timer.schedule(new TimerTask() {
                    int i = 1;

                    @Override
                    public void run() {


                        if (staticConnectionStatus) {

                            if (i <= 10) {

                                sendCAllDatatoDashboard(datatoSend, callStatus);

                                i++;

                            } else {

                                timer.cancel();

                            }

                        }

                    }

                    }, 0, 0);//Update text every second

            }*//*
        } catch (Exception e) {
            e.getMessage();
        }
    }*/
    /*private void updateMissedcallDisplay(String datatoSend, String status, int count) {
        Log.e("transmission_missedcall",String.valueOf(count));
        sendMISSEDCAllDatatoDashboard(datatoSend, status, count);

        *//*try {
            if (timer != null) {

                int finalCount = count;
                timer.schedule(new TimerTask() {
                    int i = 1;
                    @Override
                    public void run() {

                        if (staticConnectionStatus) {
                            sendMISSEDCAllDatatoDashboard(datatoSend, status, finalCount);
                            *//**//*if (i <= 10) {

                                i++;


                            } else {

                                timer.cancel();


                            }*//**//*
                        }
                        Log.e("missed_call_check","complete");

                    }

                //}, new Common(callContext).generate_delay(), 200);//Update text every second
                }, 0, 10000);//Update text every second
            }

        } catch (Exception e) {
            e.getMessage();
        }
    }*/
}