package com.suzuki.broadcaster;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import com.suzuki.pojo.SettingsPojo;
import com.vdurmont.emoji.EmojiParser;

import java.util.Calendar;
import java.util.Timer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

import static com.suzuki.activity.HomeScreenActivity.MSG_CLEAR;
import static com.suzuki.activity.HomeScreenActivity.mBoundService;
import static com.suzuki.application.SuzukiApplication.calculateCheckSum;
import static com.suzuki.fragment.DashboardFragment.staticConnectionStatus;
import static com.suzuki.utils.Common.BROADCAST_RECEIVED;
import static com.suzuki.utils.Common.EXCEPTION;
import static com.suzuki.utils.Common.SMS_COUNTER;
import static com.suzuki.utils.Common.unread_sms;

public class IncomingSms extends BroadcastReceiver {

    public static String SMS_SENDER_NAME = "" ;
    public static int UNREAD_SMS_COUNT=0;
    String unReadSMSStatus, newSMS, noOfUnreadSMS, contactNameSMS = "";
    String smsType = "N";
    final SmsManager sms = SmsManager.getDefault();
    Realm realm;
    
    RealmResults<SettingsPojo> settingsPojos;

    int mainUnread=1;
    boolean isIncomingSMSEnabled = false;
    private Timer timer;
    public static Timer smsCounterTimer, SMS_COUNT_CHECK;// to continue check the sms count
    private Context context;
    public static volatile boolean UNREAD_SMS_FLAG=false;
    private int unread=0;
    private long recevied_time=1;

    public void onReceive(Context context, Intent intent) {

        BROADCAST_RECEIVED = true;
        Log.e("checkmessaging","sms_broadcast_received");

        long currentTime = Calendar.getInstance().getTimeInMillis();

        if(currentTime>recevied_time+5000) {
            recevied_time=currentTime;
            unread_sms++;
        }
        //MSG_CLEAR=0x59;

        final Bundle bundle = intent.getExtras();

        realm = Realm.getDefaultInstance();

        this.context = context;
        if (smsCounterTimer == null) {
            smsCounterTimer = new Timer();
        } else {
            smsCounterTimer.cancel();
            smsCounterTimer.purge();
            smsCounterTimer = null;
            smsCounterTimer = new Timer();
        }

        if (timer == null) {
            timer = new Timer();
        } else {
            timer.cancel();
            timer.purge();
            timer = null;
            timer = new Timer();
        }

        settingsPojos = realm.where(SettingsPojo.class).findAll();

        settingsPojos.addChangeListener(new RealmChangeListener<RealmResults<SettingsPojo>>() {
            @Override
            public void onChange(RealmResults<SettingsPojo> settingsPojos) {

                for (int i = 0; i < settingsPojos.size(); i++){
                    isIncomingSMSEnabled = settingsPojos.get(0).isIncomingSMS();
                }
            }
        });

        SettingsPojo settingsPojo = realm.where(SettingsPojo.class).equalTo("id", 1).findFirst();

        if (settingsPojo != null) isIncomingSMSEnabled = settingsPojo.isIncomingSMS();

        //Log.d("IncomingSms", "--" + isIncomingSMSEnabled);
        try {

            if (bundle != null) {

                Log.e("sms_bundle",String.valueOf(bundle));
                final Object[] pdusObj = (Object[]) bundle.get("pdus");

                for (int i = 0; i < pdusObj.length; i++) {

                    //getSMS(context);
                    //get_SMS_new();
                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    String phoneNumber = currentMessage.getDisplayOriginatingAddress();

                    String senderNum = phoneNumber;
                    String message = currentMessage.getDisplayMessageBody();

                    if (!getContactName(senderNum, context).equals("")) {
                        //getSMS(context);
                        //get_SMS_new();
                        contactNameSMS = numberNameValidation(senderNum, context);
                        unReadSMSStatus = "Y";
                        newSMS = "Y";

                        SMS_SENDER_NAME = contactNameSMS;

                        String data = unReadSMSStatus + newSMS + "1" + contactNameSMS;

                        if (staticConnectionStatus && isIncomingSMSEnabled) updateSMSDisplay(data);

                    } else if (getContactName(senderNum, context) == "") {
                        contactNameSMS = "00";
                    }
                }
            }

        } catch (Exception ignored) {

        }
    }

    //    public String get_SMS_new() {
//        noOfUnreadSMS = String.valueOf(UNREAD_SMS_COUNT);
//
//        mainUnread = UNREAD_SMS_COUNT;
//
//        return noOfUnreadSMS;
//    }
    /*private void checkMsgCounter() {
        Log.e("SMS_counter","check");
        try {
            if (smsCounterTimer != null) {
                smsCounterTimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        //String msgCount = getSMS(context);
                        String msgCount=get_SMS_new();

                        try {
                            int unreadMsg = Integer.parseInt(msgCount);
                            Log.e("UNREAD",String.valueOf(unreadMsg));

                            if (unreadMsg > 0) {
                                MSG_CLEAR = 0x4E;

                            } else {
                                // we can clear msg counter and stop timer
                                MSG_CLEAR = 0x59;
//                                new Handler(Looper.getMainLooper()).postDelayed(() -> MSG_CLEAR = 0x4E,3000);


                                smsCounterTimer.cancel();
                                smsCounterTimer.purge();
                                smsCounterTimer = null;

                                String data = unReadSMSStatus + newSMS + "1" + contactNameSMS;
                                updateSMSDisplay(data);
                            }
                        } catch (NumberFormatException e) {
                            Timber.e(e);
                        }
                    }
                }, new Common(context).generate_delay(), 2000);
            }
        } catch (NullPointerException e) {
            Timber.e(e);
        }
    }*/

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

        title = String.valueOf(check);

        title=title.replace("  "," ");
        return title;
    }

    public String numberNameValidation(String number, Context context) {
        String incomingNumber = number;
        if (!getContactName(number, context).equals("")) {
            incomingNumber = getContactName(number, context);

            incomingNumber = incomingNumber.replaceAll("[!#&$%'(;)<=>?@`{|}~.^,]", " ");

            Pattern pattern = Pattern.compile("[^\\x00-\\x7F]");
            Matcher matchers = pattern.matcher(incomingNumber);
            boolean hasSpecialChars = matchers.find();

            incomingNumber=remove_invalid_char(incomingNumber);

            if (hasSpecialChars) {
                // emoticon is added remove them
                incomingNumber = EmojiParser.replaceAllEmojis(incomingNumber, " ");
//                incomingNumber = incomingNumber.replaceAll("[^A-Za-z0-9]"," "); // to remove everything other than char
            } else {
//                incomingNumber = getContactName(number, context);
                if (incomingNumber.length() >= 20) incomingNumber = incomingNumber.substring(0, 19);
            }
        } else {
            incomingNumber = number.replace("+", "");

            incomingNumber = incomingNumber.trim();
            if (incomingNumber.length() < 1 && number.contains("+")) incomingNumber = number.replace("+", "");
        }
        return incomingNumber;
    }

    private void updateSMSDisplay(final String datatoSend) {
        try {
            if (staticConnectionStatus) {

                SMS_COUNTER++;
                sendSMSDatatoDashboard(datatoSend);
            }
        } catch (Exception z) {
            Log.e(EXCEPTION,"IncomingSms: "+String.valueOf(z));
        }
    }

    public void sendSMSDatatoDashboard(String dataToSend) {

        byte[] PKT = getSmartPhoneSMSPKT(dataToSend);
        MSG_CLEAR = 0x4E;

        for(int i=2; i<5; i++){
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (mBoundService != null) mBoundService.writeDataFromAPPtoDevice(PKT,3);
        }
    }

    public byte[] getSmartPhoneSMSPKT(String data) {
        String PhoneData;
        byte[] RetArray = new byte[30];

        try {

            if (data.length() <= 23) {

                for (int i = data.length(); i <= 23; i++) {
                    data += "\0";
                }
                PhoneData = "?"/*Start of Frame*/ + "5"/*Frame ID*/ + data/*Data to icd*/ /* */ + "N"/*Fill 0s at reserved byte positions*/ + "\0\0\0"/*End of Frame*/;

            } else {

                data = data.substring(0, 23);
                PhoneData = "?"/*Start of Frame*/ + "5"/*Frame ID*/ + data/*Phone data to icd*/ + "N"/*Fill 0s at reserved byte positions*/ + "\0\0\0"/*End of Frame*/;
            }

            try {

                RetArray = PhoneData.getBytes("UTF-8");
                RetArray[0] = (byte) 0xA5;
                RetArray[2] = (byte) 0xFF;//this is recently enabled
//                RetArray[3] = (byte) 0xFF;
                RetArray[4] = (byte) SMS_COUNTER;
                RetArray[25] = (byte) 0x4E;

                RetArray[26] = (byte) 0xFF;
                RetArray[27] = (byte) 0xFF;

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

    /*public String getSMS(Context context) {


        String folder = "content://sms/inbox";

//        Uri message = Uri.parse("content://sms/");
        Uri mSmsQueryUri = Uri.parse(folder);
        String columns[] = new String[]{"person", "address", "body", "date", "status"};
        String sortOrder = "date ASC";


//        ContentResolver cr = context.getContentResolver();

//        Cursor c = cr.query(mSmsQueryUri, null, null, null, null);


//        if (c.moveToFirst()) {
//
//
//            if (getContactName(c.getString(c
//                    .getColumnIndex("address")), context) != null || getContactName(c.getString(c
//                    .getColumnIndex("address")), context) != "")
//                if (c != null) {
//                    contactNameSMS = getContactName(c.getString(c
//                            .getColumnIndex("address")), context);
//
//                } else {
//                    c.close();
//                }
//
//
//        }

//        Cursor cuuu = context.getContentResolver().query(mSmsQueryUri, columns, "read = 0", null, sortOrder);
        Cursor cuuu = context.getContentResolver().query(mSmsQueryUri, columns, "read = 0", null, Telephony.Sms.DATE + " DESC limit 105");

        int kkk = 0;
        if (cuuu.moveToFirst()) {
            for (int i = 0; i < cuuu.getCount(); i++) {


                if (getContactName(cuuu.getString(cuuu.getColumnIndexOrThrow("address")).toString(), context).equals("")) {


                } else if (!getContactName(cuuu.getString(cuuu.getColumnIndexOrThrow("address")).toString(), context).equals("")) {

                    contactNameSMS = getContactName(cuuu.getString(cuuu
                            .getColumnIndex("address")), context);
                    kkk++;

                    Log.d("tag", "getSMS: " + contactNameSMS + kkk*//*+cuuu.getString(cuuu.getColumnIndex("body"))*//*);

                }
                cuuu.moveToNext();

            }
        }

        Log.d("tag", "getSMS: totalMSg " + kkk + " mainThread " + mainUnread + " cuu count " + cuuu.getCount());

        noOfUnreadSMS = String.valueOf(kkk);

        mainUnread = kkk;
        return noOfUnreadSMS;

    }*/

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

        return contactName;
    }
}
