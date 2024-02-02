package com.suzuki.services;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import com.sachinvarma.easypermission.EasyPermissionList;
import com.suzuki.broadcaster.CallReceiverBroadcast;
import com.suzuki.broadcaster.IncomingSms;
import com.suzuki.pojo.SettingsPojo;
import com.vdurmont.emoji.EmojiParser;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.Realm;
import io.realm.RealmResults;

import static com.suzuki.activity.HomeScreenActivity.CALL_CLEAR;
import static com.suzuki.activity.HomeScreenActivity.MSG_CLEAR;
import static com.suzuki.activity.HomeScreenActivity.mBoundService;
import static com.suzuki.application.SuzukiApplication.calculateCheckSum;
import static com.suzuki.broadcaster.CallReceiverBroadcast.callClearTimer;
import static com.suzuki.broadcaster.IncomingSms.SMS_SENDER_NAME;
import static com.suzuki.broadcaster.IncomingSms.UNREAD_SMS_FLAG;
import static com.suzuki.fragment.DashboardFragment.staticConnectionStatus;
import static com.suzuki.utils.Common.BROADCAST_RECEIVED;
import static com.suzuki.utils.Common.CALL_ACTIVE;
import static com.suzuki.utils.Common.DUMMY_VALUE;
import static com.suzuki.utils.Common.EXCEPTION;
import static com.suzuki.utils.Common.LAST_CALL_TYPE_IS_WHATSAPP;
import static com.suzuki.utils.Common.MISSED_CALL_COUNT;
import static com.suzuki.utils.Common.MISSED_CALL_RECORD;
import static com.suzuki.utils.Common.MISSED_NUMBER;
import static com.suzuki.utils.Common.SMS_COUNTER;
import static com.suzuki.utils.Common.SMS_POSTED;
import static com.suzuki.utils.Common.w_MISSED_CALL_COUNT;
import static com.suzuki.utils.Common.w_MSG_COUNTER;
import static com.suzuki.utils.Common.w_call_posted;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class NotificationService extends NotificationListenerService {

    //TEMP
    /*final int interval = 200; // 1 Second
    private TimerTask tt;
    Handler handler = new Handler();
    Handler wCallHandler = new Handler();
    Runnable runnable, wCallRunnable;*/
    public static Timer SMSClearTimer;
    private String TAG = this.getClass().getSimpleName();
    private NLServiceReceiver nlservicereciver;
    RealmResults<SettingsPojo> settingsPojos;
    Realm realm;

    boolean whatsappCallEnabled = false;
    boolean whatsappMSGEnabled = false;
    boolean SMSEnabled = false;
    int unread;
   // long unread = 0L;
    private Timer timer;

    private String previousString = "";
    private Boolean sendFlag = false, issueFlag = false;

    String sender = "";
    private String datatoSend;
    private String dataForDisplay;
    private int SMS_NOTIFICATION_ID = 0, RECEIVED_NOTIFICATION_ID = DUMMY_VALUE;
    private int REASON = 0;
    //    private boolean SMS_PENDING=false;
//    private byte msg_type=0x4E;
    private String title = " ", text = " ", full = " ", test = " ";
    private String prev_title = "";

    @Override
    public void onCreate() {
        super.onCreate();

        realm = Realm.getDefaultInstance();

        settingsPojos = realm.where(SettingsPojo.class).findAll();

        settingsPojos.addChangeListener(settingsPojos -> {

            for (int i = 0; i < settingsPojos.size(); i++) {

                whatsappCallEnabled = settingsPojos.get(0).isWhatsappCall();
                whatsappMSGEnabled = settingsPojos.get(0).isWhatsappMSG();
            }
        });

        //nlservicereciver = new NLServiceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("NOTIFICATION_LISTENER_SERVICE_EXAMPLE");
        registerReceiver(nlservicereciver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //unregisterReceiver(nlservicereciver);
    }

    @SuppressLint("LogNotTimber")
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        PowerManager.WakeLock screenLock = null;
        if ((getSystemService(POWER_SERVICE)) != null) {
            screenLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "mainscreen:partial_wake");
            screenLock.acquire(100000);
        }

        Realm realm = Realm.getDefaultInstance();
        SettingsPojo settingsPojo = realm.where(SettingsPojo.class).equalTo("id", 1).findFirst();
        if (settingsPojo != null) {
            whatsappCallEnabled = settingsPojo.isWhatsappCall();
            whatsappMSGEnabled = settingsPojo.isWhatsappMSG();
            SMSEnabled = settingsPojo.isIncomingSMS();
        }

        if (SMSClearTimer == null ) SMSClearTimer = new Timer();
        else {
            try {
                SMSClearTimer.cancel();
                SMSClearTimer.purge();
                SMSClearTimer = null;
                SMSClearTimer = new Timer();
            } catch (Exception e) {
                Log.e(EXCEPTION, getClass().getName() + " onNotificationPosted " + String.valueOf(e));
            }
        }

        Bundle extras = sbn.getNotification().extras;
        Log.e("Notification bundle0", String.valueOf(extras));
        String title = "", text = " ", full = " ", test = " ";

        title = String.valueOf(extras.get("android.title"));
        if (!title.equals("Suzuki Ride Connect is running") && !title.equals("null")) {
            text = String.valueOf(extras.get("android.text"));

            if (!text.isEmpty()) {
                text = text.toString().toLowerCase();
                full = String.valueOf(extras.toString());
            } else return;

            if (!full.isEmpty()) {
                full = full.toString().toLowerCase();
                test = title;
                test = remove_invalid_char(test);
            } else return;
        }

        if (sbn.getNotification().when < System.currentTimeMillis() + 2000 && sbn.getNotification().when > System.currentTimeMillis() - 2000) {
            if (System.currentTimeMillis() > SMS_POSTED + 1000) {

                if (BROADCAST_RECEIVED) {
                    SMS_POSTED = System.currentTimeMillis();
                    BROADCAST_RECEIVED = false;

                }
                else if (SMSEnabled && (full.contains("messag") || full.contains("mms"))) {

                    if (!full.contains("whatsapp")){

                        try{
                            if (Build.VERSION.SDK_INT>=33 &&whatsappMSGEnabled&& sbn.getNotification().toString().contains("whatsapp")&& sbn.getNotification().extras.get("android.largeIcon")!=null && !getPhoneNumber(title, getApplicationContext()).equals("") || !getContactName(title, getApplicationContext()).equals("") ) {
                                w_MSG_COUNTER++;
                                sendDatatoDashboard("YY1" + title, (byte) 0x57);
                            }
                            else if(!sbn.getNotification().toString().contains("whatsapp")&&!(getPhoneNumber(title, getApplicationContext()).equals("") || !getContactName(title, getApplicationContext()).equals(""))){
                                SMS_COUNTER++;
                                sendDatatoDashboard("YY1" + test, (byte) 0x4E);
                            }
                        }catch (Exception e){
                            Log.e(EXCEPTION,getClass().getName()+" onNotificationPosted_3 "+String.valueOf(e));
                        }
                    }
                    else if (full.contains("whatsapp") && whatsappMSGEnabled){
                        try{

                            if ( !getPhoneNumber(title, getApplicationContext()).equals("") || !getContactName(title, getApplicationContext()).equals("")) {
                                //whatsapp_notification_1
                                Log.e("messag check", "cond 1 sent");
                                w_MSG_COUNTER++;
                                sendDatatoDashboard("YY1" + test, (byte) 0x57);
                                prev_title = title;
                            } else if ( title.toLowerCase().equals("whatsapp")) {

                                Log.e("messag check", "title: " + title);
                                Log.e("messag check", "full: " + full);

                                title = get_backup_title(full);

                                if (Build.VERSION.SDK_INT>= 33 && title.matches(".*[\\u0900-\\u097F]+.*") && title.matches(".*[a-zA-Z]+.*")) {
                                    w_MSG_COUNTER++;
                                    Log.e("messag check", "worked: " + title);
                                    sendDatatoDashboard("YY1" + title, (byte) 0x57);
                                    return;

                                }
                                else if (title.matches(".*[a-zA-Z]+.*") && title.matches(".*[\\u0900-\\u097F]+.*") ) {
                                    w_MSG_COUNTER++;
                                    Log.e("messag check", "worked: " + title);
                                    sendDatatoDashboard("YY1" + title, (byte) 0x57);
                                    return;
                                }
                            }
                        } catch (Exception e) {
                            Log.e(EXCEPTION, getClass().getName() + " onNotificationPosted_4 " + String.valueOf(e));
                        }
                    }
                }
            }
        }

        RECEIVED_NOTIFICATION_ID = sbn.getId();

        if (timer == null) {
            timer = new Timer();
        } else {
            timer.cancel();
            timer.purge();
            timer = null;
            timer = new Timer();
        }


        Bundle abc = sbn.getNotification().extras;

        Log.e("Notification bundle1", String.valueOf(abc));

        String name = abc.getString("android.title");
        name = prev_title;
        String packageName = null;
        String UNREAD_SMS_COUNT = " ";
        try {
            packageName = "" + abc.getString("android.text");
            UNREAD_SMS_COUNT = packageName.replaceAll("[^0-9]", "");
        }
        catch (NumberFormatException e) {
            Log.e(EXCEPTION, getClass().getName() + " onNotificationPosted_5 NumberFormatException: " + String.valueOf(e));
            return;
        }
        catch (Exception e) {
            Log.e(EXCEPTION, getClass().getName() + " onNotificationPosted_5 " + String.valueOf(e));
            return;
        }

        try {
            if (!SMS_SENDER_NAME.equals("") && name.contains(SMS_SENDER_NAME) && !UNREAD_SMS_FLAG) {
                if (UNREAD_SMS_COUNT.equals("")) IncomingSms.UNREAD_SMS_COUNT += 1;
                else IncomingSms.UNREAD_SMS_COUNT = Integer.parseInt(UNREAD_SMS_COUNT);
            }
        } catch (Exception e) {
             Log.e(EXCEPTION, getClass().getName() + " onNotificationPosted_6 " + String.valueOf(e));
        }

        try {
            if (sbn.getPackageName().contains("mms")) {
                SMS_NOTIFICATION_ID = sbn.getId();
                UNREAD_SMS_FLAG = true;
            }

        } catch (Exception e) {
              Log.e(EXCEPTION, getClass().getName() + " onNotificationPosted_7 " + String.valueOf(e));
        }

        /* check only whats app msg */
        if (sbn.getId() == 1 && REASON != 8) {
            checkWhatsAppMsg(sbn, abc);
        } else REASON = 0;

        ArrayList<StatusBarNotification> tempNoti = new ArrayList<>();
        try {
            StatusBarNotification[] sdbjk = getActiveNotifications();
            for (StatusBarNotification statusBarNotification : sdbjk) {

                if (statusBarNotification.getPackageName().contentEquals("com.whatsapp")  || statusBarNotification.getPackageName().contentEquals("com.whatsapp.w4b") && tempNoti.add(statusBarNotification));
            }
            if (tempNoti.size() > 1) {
                String msgString = "" + tempNoti.get(1).getNotification().extras.getString("android.text");
                if (!(previousString.equals(msgString))) {
                    previousString = msgString;
                    sendFlag = true;
                }
            }
        } catch (Exception e) {
            Log.e(EXCEPTION, getClass().getName() + " onNotificationPosted_8 " + String.valueOf(e));
            issueFlag = true;
        }

        if (packageName != null) {
            if (sbn.getPackageName().contentEquals("com.whatsapp") || sbn.getPackageName().contentEquals("com.whatsapp.w4b")) {

                if (packageName.contentEquals("Ongoing voice call")) {
                    // String  datatoSend = numberNameValidationForCall(name);

                    if (!getPhoneNumber(name, getApplicationContext()).equals("") || !getContactName(name, getApplicationContext()).equals("")) {

                        datatoSend = name.trim();
                        // datatoSend= name.replaceAll(" ","");

                    } else {
                        datatoSend = numberNameValidationForCall(name);
                    }

                    if (!datatoSend.equals("") && staticConnectionStatus && whatsappCallEnabled) sendWhatspCAllDatatoDashboard(datatoSend, "2");
                } else if (packageName.contentEquals("Incoming voice call")) {

                    // String  datatoSend = numberNameValidationForCall(title);

                    if (!getPhoneNumber(title, getApplicationContext()).equals("") || !getContactName(title, getApplicationContext()).equals("")) {

                        datatoSend = title.trim();
                        //datatoSend= name.replaceAll(" ","");

                    } else {
                        datatoSend = numberNameValidationForCall(title);
                    }
                    datatoSend = datatoSend.trim();

                    for (int i = 0, k = 0; i < datatoSend.length(); i++) {
                        k = datatoSend.charAt(i);
                        if (k > 127) {
                            datatoSend = " ";
                            break;
                        }
                    }

                    if (staticConnectionStatus && whatsappCallEnabled) sendWhatspCAllDatatoDashboard(datatoSend, "1");

                } else if (title != null) { //missed call
                    if (packageName.contains("group voice call")) return;
                    if (Build.VERSION.SDK_INT >= 33 && staticConnectionStatus && whatsappCallEnabled &&  full.toLowerCase().contains("com.whatsapp") || full.toLowerCase().contains("com.whatsapp.w4b")){
                        sendMISSEDCAllDatatoDashboard(datatoSend, "Y");

                    }
//                    else  if (Build.VERSION.SDK_INT >= 33 && staticConnectionStatus && whatsappCallEnabled &&  full.toLowerCase().contains("com.whatsapp") ){
//                        sendMISSEDCAllDatatoDashboard(datatoSend, "Y");
//
//                    }
                    else if (staticConnectionStatus && whatsappCallEnabled && (full.contains("missed call") || full.contains("missed voice call"))){
                        sendMISSEDCAllDatatoDashboard(datatoSend, "Y");
                    }
                }
            }
        }

        Intent i = new Intent("NOTIFICATION_LISTENER_EXAMPLE");
        i.putExtra("notification_event", "onNotificationPosted :" + sbn.getPackageName() + "\n");
        sendBroadcast(i);

        screenLock.release();
    }

    private String get_backup_title(String full) {
        int b = full.indexOf("android.textlines=");
        String backup_text = "";
        if (b != -1) {
            backup_text = full.substring(b, full.indexOf(']', b));
            backup_text = backup_text.substring(backup_text.lastIndexOf(',') + 1, backup_text.lastIndexOf(':'));
            backup_text = backup_text.trim();
        }

        return backup_text;
    }

    public String getContactName(final String phoneNumber, Context context) {
        try {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));

        String[] projection = new String[]{ContactsContract.PhoneLookup.HAS_PHONE_NUMBER};

        String contactName = "";
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                contactName = cursor.getString(0);
            }
            cursor.close();
        }
        return contactName;
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return "";
    }

    /*public String getPhoneNumber(String name, Context context) {
        try {
            boolean allowed = false;

            if (ContextCompat.checkSelfPermission(context, EasyPermissionList.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions((Activity) context,
                        new String[]{EasyPermissionList.READ_CONTACTS},
                        100);
            } else {
                allowed = true;
            }
            if (allowed) {
                try {
                    allowed = false;
                    String ret = null;
                    String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " like'%" + name + "%'";
                    String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
                    Cursor c = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            projection, selection, null, null);
                    if (c.moveToFirst()) {
                        ret = c.getString(0);
                    }
                    c.close();
                    if (ret == null) ret = "";
                    return ret;
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            }
        } catch (SecurityException | IllegalArgumentException e) {
            e.printStackTrace();
        }

        return "";

    }*/

    public String getPhoneNumber(String name, Context context) {

        if (ContextCompat.checkSelfPermission(context, EasyPermissionList.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            if (context instanceof Activity) {
                ActivityCompat.requestPermissions((Activity) context,
                        new String[]{EasyPermissionList.READ_CONTACTS},
                        100);
            }
            return "";
        }

        String ret = null;
        String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " like '%" + name + "%'";
        String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};

        try {
            Cursor c = context.getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    projection, selection, null, null);

            if (c != null && c.moveToFirst()) {
                ret = c.getString(0);
                c.close();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return ret != null ? ret : "";
    }


    @NotNull
    private String remove_invalid_char(String title) {
        char[] check = title.toCharArray();

        for (int i = 0; i < title.length(); i++) {
            try{
            int k = title.charAt(i);
            if (k > 127) {
                check[i] = ' ';
                if (k > 8500) {
                    i++;
                    check[i] = ' ';
                }
            }
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }

        title = String.valueOf(check);

        title = title.replace("  ", " ");
        return title;
    }
   /* @NotNull
    private String remove_invalid_char(String title) {
        char[] check = title.toCharArray();
        int writeIndex = 0;

        for (int i = 0; i < title.length(); i++) {
            int k = title.charAt(i);
            if (k <= 127 || k > 8500) { // Keep valid characters (ASCII <= 127 and ASCII > 8500).
               // check[writeIndex] = check[i];
                if (writeIndex < check.length) {
                    check[writeIndex] = check[i];
                }
                writeIndex++;
            }
        }

        title = new String(check, 0, writeIndex);

        title = title.replace("  ", " ");
        return title;
    }*/


    private void checkWhatsAppMsg(StatusBarNotification sbn, Bundle bundle) {


        if (sbn.getNotification().tickerText == null) {
            sender = bundle.getString("android.title");
//            return;
        }

        if (sbn.getPackageName() != null && sbn.getNotification().tickerText != null) {
            if ( whatsappMSGEnabled) {

//


                if (sender != null && !sender.isEmpty()) {
                    String[] arr = sbn.getNotification().tickerText.toString().split("\\s+");
                    if (arr.length > 2) {
                        sender = arr[2];
                    }
                }


                String title = bundle.getString("android.title");
                String info = "" + bundle.getString("android.text");

                if (sbn.getId() == 1) {
                    if (sbn.getPackageName().equalsIgnoreCase("com.whatsapp") || sbn.getPackageName().equalsIgnoreCase("com.whatsapp.w4b")  ) {
                        String notificationInfo = "" + bundle.getString("android.text");
                        if (notificationInfo != null) {

                            String[] arr = notificationInfo.split("\\s+");
                            if (arr.length > 0) {
                                String noOfMsg = arr[0];
                                if (noOfMsg.matches("[0-9]+")) {
                                    unread = Integer.parseInt(noOfMsg);
                                } else {
                                    unread = 1;
                                }

                           /* if (noOfMsg.matches("[0-9]+")) {
                                unread = Long.parseLong(noOfMsg);
                            } else {
                                unread = 1L;
                            }*/


                                String unReadSMSStatus = "Y";
                                String newSMS = "Y";
                                String noOfUnreadSMS = "";
                                String contactNameSMS = sender;
                                String smsType = "W";
                              //  String isOnlyNo = sender.replace("+", "");
                               // String isOnlyNo = sender != null ? sender.replace("+", "") : null;


                                contactNameSMS = numberNameValidation(sender);

                                if (contactNameSMS != null && contactNameSMS.length() > 0) {
                                    String data = unReadSMSStatus + newSMS + "1" + contactNameSMS;
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    public String numberNameValidationForCall(String number) {
        if (number == null) return "";

        String incomingNumber = number;
        if (!number.equals("")) {

            incomingNumber = incomingNumber.replaceAll("[!#&$%'(;)<=>?@`{|}~.^,]", " ");

            Pattern pattern = Pattern.compile("[^\\x00-\\x7F ]");
            Matcher matchers = pattern.matcher(incomingNumber);
            boolean hasSpecialChars = matchers.find();

            if (hasSpecialChars) {
                // emoticon is added remove them
                incomingNumber = EmojiParser.replaceAllEmojis(incomingNumber, " ");
                //   incomingNumber = incomingNumber.replaceAll("[^A-Za-z0-9]", " ");

            } else {

                pattern = Pattern.compile("[^0-9-+ ]");
                matchers = pattern.matcher(incomingNumber);
                hasSpecialChars = matchers.find();

                // incoming no condition for 123+(number + "+", because if it is unsaved so length will > 9 <= 15)
                if (hasSpecialChars || incomingNumber.length() < 10 || incomingNumber.length() > 15) {

                    // only char
                    if (incomingNumber.length() >= 20) {
                        incomingNumber = incomingNumber.substring(0, 19);
                    }

                } else {
                    // unsaved contact no
                    if (incomingNumber.contains("+")) {
                        incomingNumber = number.replace("+", "");
                    }
                    incomingNumber = incomingNumber.replace(" ", "");
                }
            }
        }
        incomingNumber = incomingNumber.trim();
        return incomingNumber;
    }

    public String numberNameValidation(String number) {

        String incomingNumber = number;
        if (!number.equals("")) {

            if (incomingNumber.contains("+")) {
                incomingNumber = incomingNumber.replace("+", "");
            }
            // these chars are not supported in cluster(these are remaining:-  []")
            incomingNumber = incomingNumber.replaceAll("[!#&$%'(;)<=>?@`{|}~.^,]", " ");

            Pattern pattern = Pattern.compile("[^\\x00-\\x7F ]");
            Matcher matchers = pattern.matcher(incomingNumber);
            boolean hasSpecialChars = matchers.find();


            if (hasSpecialChars) {
                // emoticon is added
                incomingNumber = EmojiParser.replaceAllEmojis(incomingNumber, " ");
//                incomingNumber = incomingNumber.replaceAll("[^A-Za-z0-9]", " ");

            } else if (incomingNumber.length() >= 20) incomingNumber = incomingNumber.substring(0, 19);
        }
        return incomingNumber;
    }

    public void sendDatatoDashboard(String dataToSend, byte smsType) {

        //whatsapp_notification_2
        Log.e("messaging check", "D2D: " + dataToSend);
        SMS_POSTED = System.currentTimeMillis();

        byte[] PKT = getSmartPhoneSMSPKT(dataToSend, smsType);

        MSG_CLEAR = 0x4E;

        for (int i = 2; i < 5; i++) {

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Log.e(EXCEPTION, getClass().getName() + " sendDatatoDashboard " + String.valueOf(e));
            }
            if (mBoundService != null) mBoundService.writeDataFromAPPtoDevice(PKT, 6);
        }
    }
    /*private void updateWhatsapSMSDisplay(final String datatoSend, final String smsType) {

        for(int i =2;i<5;i++) {

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sendDatatoDashboard(datatoSend, (byte) 0x57);
        }
    }*/


    public void sendWhatspCAllDatatoDashboard(String dataToSend, String status) {
        CALL_ACTIVE = true;
        byte[] PKT = getSmartWhatspPhoneCALLPKT(dataToSend, "W", status);

        for (int i = 2; i < 5; i++) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Log.e(EXCEPTION, getClass().getName() + " sendWhatspCAllDatatoDashboard " + String.valueOf(e));
            }
            if (mBoundService != null) mBoundService.writeDataFromAPPtoDevice(PKT, 61);
        }
    }

    public byte[] getSmartPhoneSMSPKT(String data, byte smsType) {

        String PhoneData;
        byte[] RetArray = new byte[30];

        //int count = SMS_RECORD.get(datatoSend);
        String sms_type = "N";
        int count = 0;

        if (smsType == 0x57) {
            sms_type = "W";
            count = w_MSG_COUNTER;
        } else {
            sms_type = "N";
            count = SMS_COUNTER;
        }

        //int readforDashboard = 1;
        /*if (unread <= 100) {
            if (unread == 0) {
                readforDashboard = 1;
            } else {
                readforDashboard = unread;
            }
        }*/

        //readforDashboard = unread;

        try {

            if (data.length() <= 23) {

                for (int i = data.length(); i <= 23; i++) {
                    data += "\0";
                }
                PhoneData = "?"/*Start of Frame*/ + "5"/*Frame ID*/ + data/*Data to icd*/ /* */ + sms_type/*Fill 0s at reserved byte positions*/ + "\0\0\0"/*End of Frame*/;

            } else {
                data = data.substring(0, 23);

//            data = "\0";
                PhoneData = "?"/*Start of Frame*/ + "5"/*Frame ID*/ + data/*Phone data to icd*/ + sms_type/*Fill 0s at reserved byte positions*/ + "\0\0\0"/*End of Frame*/;
            }

            try {

                RetArray = PhoneData.getBytes("UTF-8");
                RetArray[0] = (byte) 0xA5;
                RetArray[2] = (byte) 0xFF;
                RetArray[3] = (byte) 0x59;
                RetArray[4] = (byte) count;
                RetArray[25] = (byte) smsType;
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

    public void sendMISSEDCAllDatatoDashboard(String dataToSend, String status) {

        if (w_call_posted + 1000 < System.currentTimeMillis()) {

            MISSED_NUMBER = dataToSend;
//            remove + symbol from Missed number
//            MISSED_NUMBER = MISSED_NUMBER.replaceAll("[!#&$%'(;)<=>?@`{|}~.^,]", " ");

//            MISSED_NUMBER=remove_invalid_char(MISSED_NUMBER);
//
//            Pattern pattern = Pattern.compile("[^\\x00-\\x7F]");
//            Matcher matchers = pattern.matcher(MISSED_NUMBER);


            byte[] PKT = getSmartMISSEDPhoneCALLPKT(MISSED_NUMBER, status,++w_MISSED_CALL_COUNT);
            if (mBoundService != null) {
                /*cancel msgClear timer if that is running*/
                if (callClearTimer != null) {
                    callClearTimer.cancel();
                    callClearTimer.purge();
                    callClearTimer = null;
                }

                // set missed call clear flag to false
                CALL_CLEAR = 0x4E;
                LAST_CALL_TYPE_IS_WHATSAPP = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        CALL_ACTIVE = false;
                    }
                }, 1000);

                for (int i = 2; i < 5; i++) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        Log.e(EXCEPTION, getClass().getName() + " sendMISSEDCAllDatatoDashboard " + String.valueOf(e));
                    }
                    if (mBoundService != null) mBoundService.writeDataFromAPPtoDevice(PKT, 34);
                }
            }
            w_call_posted = System.currentTimeMillis();
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
                PhoneData = "?"/*Start of Frame*/ + "4"/*Frame ID*/ + alteredData/*Data to icd*/ /* */ + "\0\0\0\0\0"/*Fill 0s at reserved byte positions*/ /*End of Frame*/;


            } else {
                alteredData = alteredData.substring(0, 22);
                PhoneData = "?"/*Start of Frame*/ + "4"/*Frame ID*/ + alteredData/*Phone data to icd*/ + "\0\0\0\0\0"/*Fill 0s at reserved byte positions*/ /*End of Frame*/;
            }

            try {

                RetArray = PhoneData.getBytes("UTF-8");
                RetArray[0] = (byte) 0xA5;
                RetArray[2] = (byte) 0xFF;
                RetArray[3] = (byte) count;
                for (int k = 25; k <= 27; k++) {
                    RetArray[k] = (byte) 0xFF;
                }

                RetArray[24] = (byte) 0x57;

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

    /**
     * when remove called -> check one by one all phone numbers from record if its present in sbn -> if present, then check the count and minus this count from the COUNT variable and del the entry from RECORD.
     *
     * @param sbn
     * @param rankingMap
     * @param reason
     */


    @Override
    public void onNotificationRemoved(StatusBarNotification sbn, RankingMap rankingMap, int reason) {
        try{

        if (!CALL_ACTIVE ) {
            Bundle extra = sbn.getNotification().extras;
            try {
                title = String.valueOf(extra.get("android.title").toString().toLowerCase());
                text = String.valueOf(extra.get("android.text").toString().toLowerCase());
                full = String.valueOf(extra.toString().toLowerCase());

                if (full.contains("missed") || full.contains("voice")) {

                    for (int k = 0; k < MISSED_CALL_RECORD.size(); k++) {
                        String phone_number = MISSED_CALL_RECORD.keySet().toArray()[k].toString();
                        String temp_phone_number = phone_number.replace(" ","");;
                        full=full.replace(" ","");
                        if(full.contains(temp_phone_number)){

                            int GetCount = MISSED_CALL_RECORD.get(phone_number);

                            if (GetCount > MISSED_CALL_COUNT) MISSED_CALL_COUNT = 0;
                            else MISSED_CALL_COUNT = MISSED_CALL_COUNT - GetCount;

                            MISSED_CALL_RECORD.remove(phone_number);

                            if (MISSED_CALL_RECORD.isEmpty()) MISSED_CALL_COUNT = 0;

                            if(!LAST_CALL_TYPE_IS_WHATSAPP) new CallReceiverBroadcast().sendMISSEDCAllDatatoDashboard(MISSED_NUMBER,"Y", MISSED_CALL_COUNT);

                            break;
                        }

                        else if (full.contains("missedcall")) {
                            MISSED_CALL_COUNT = 0;
                            MISSED_CALL_RECORD.clear();
                            if(!LAST_CALL_TYPE_IS_WHATSAPP) new CallReceiverBroadcast().sendMISSEDCAllDatatoDashboard(MISSED_NUMBER,"Y", MISSED_CALL_COUNT);
                        }
                    }
                }
                else{
                    {
                        StatusBarNotification activeNotifications[] = getActiveNotifications();

                        int sms = 0, w_msg = 0, w_call = 0;

                        for (StatusBarNotification sbn_one : activeNotifications) {
                            Bundle extras = sbn_one.getNotification().extras;

                            title = String.valueOf(extras.get("android.title"));
                            if (Build.VERSION.SDK_INT>=33 && !title.equals("Suzuki Ride Connect is running") && !title.equals("null")) {
                                title = title.toLowerCase();

                                text = String.valueOf(extras.get("android.text"));

                                if (!text.isEmpty()) {
                                    text = text.toLowerCase();
                                    full = String.valueOf(extras.toString());
                                } else return;

                                if (!full.isEmpty()) {
                                    full = full.toLowerCase();
                                } else return;

//                        if (Build.VERSION.SDK_INT == 33 && !full.contains("whatsapp") && full.contains("missed") || full.contains("voice")){
//                            w_call++;
//
//                        }else  if (!full.contains("whatsapp")  && full.contains("missed") || full.contains("voice")) w_call++ ;
//
//                        if (Build.VERSION.SDK_INT == 33 && !full.contains("whatsapp")  && full.contains("msg") || full.contains("messag") || full.contains("mms")){
//                            w_msg++;
//                        }else if(!full.contains("whatsapp")  && full.contains("msg") || full.contains("messag") || full.contains("mms")){
//                            w_msg++;
//                        }
//
//                        if ((Build.VERSION.SDK_INT == 33 && full.contains("msg") || full.contains("messag") || full.contains("mms"))&& !full.contains("missed")) {
//                            sms++;
//                        }
//                        else if ((full.contains("msg") || full.contains("messag") || full.contains("mms"))&& !full.contains("missed")) {
//                            sms++;
//                        }
//
                                if ( !full.contains("whatsapp")) {

                                    if (full.contains("missed") || full.contains("voice")) w_call++;
                                    else if (full.contains("msg") || full.contains("messag") || full.contains("mms")) w_msg++;

                                }
                                // else if ((full.contains("msg") || full.contains("messag") || full.contains("mms"))&& !full.contains("missed")) sms++;
                            }
                        }


                        if(sms==0) SMS_COUNTER=0;
                        if(w_msg==0) w_MSG_COUNTER=0;
                        if(w_call==0){
                            w_MISSED_CALL_COUNT=0;
                            if(LAST_CALL_TYPE_IS_WHATSAPP) new CallReceiverBroadcast().sendMISSEDCAllDatatoDashboard(MISSED_NUMBER,"Y", w_MISSED_CALL_COUNT);
                        }

                        if(SMS_COUNTER==0 && w_MSG_COUNTER==0) MSG_CLEAR=0x59;
                    }
                }
            } catch (Exception z) {
                Log.e(EXCEPTION, getClass().getName() + " onNotificationRemoved: CALL_ACTIVE" + String.valueOf(z));
            }

            {
                StatusBarNotification activeNotifications[] = getActiveNotifications();

                int sms = 0, w_msg = 0, w_call = 0;

                for (StatusBarNotification sbn_one : activeNotifications) {
                    Bundle extras = sbn_one.getNotification().extras;

                    title = String.valueOf(extras.get("android.title"));
                    if (Build.VERSION.SDK_INT>=33 && !title.equals("Suzuki Ride Connect is running") && !title.equals("null")) {
                        title = title.toLowerCase();

                        text = String.valueOf(extras.get("android.text"));

                        if (!text.isEmpty()) {
                            text = text.toLowerCase();
                            full = String.valueOf(extras.toString());
                        } else return;

                        if (!full.isEmpty()) {
                            full = full.toLowerCase();
                        } else return;

//                        if (Build.VERSION.SDK_INT == 33 && !full.contains("whatsapp") && full.contains("missed") || full.contains("voice")){
//                            w_call++;
//
//                        }else  if (!full.contains("whatsapp")  && full.contains("missed") || full.contains("voice")) w_call++ ;
//
//                        if (Build.VERSION.SDK_INT == 33 && !full.contains("whatsapp")  && full.contains("msg") || full.contains("messag") || full.contains("mms")){
//                            w_msg++;
//                        }else if(!full.contains("whatsapp")  && full.contains("msg") || full.contains("messag") || full.contains("mms")){
//                            w_msg++;
//                        }
//
//                        if ((Build.VERSION.SDK_INT == 33 && full.contains("msg") || full.contains("messag") || full.contains("mms"))&& !full.contains("missed")) {
//                            sms++;
//                        }
//                        else if ((full.contains("msg") || full.contains("messag") || full.contains("mms"))&& !full.contains("missed")) {
//                            sms++;
//                        }
//
                        if ( !full.contains("whatsapp")) {

                            if (full.contains("missed") || full.contains("voice")) w_call++;
                            else if (full.contains("msg") || full.contains("messag") || full.contains("mms")) w_msg++;

                        }
                        // else if ((full.contains("msg") || full.contains("messag") || full.contains("mms"))&& !full.contains("missed")) sms++;
                    }
                }


                if(sms==0) SMS_COUNTER=0;
                if(w_msg==0) w_MSG_COUNTER=0;
                if(w_call==0){

                    w_MISSED_CALL_COUNT=0;
                    if(LAST_CALL_TYPE_IS_WHATSAPP) new CallReceiverBroadcast().sendMISSEDCAllDatatoDashboard(MISSED_NUMBER,"Y", w_MISSED_CALL_COUNT);
                }

                //if(SMS_COUNTER==0 && w_MSG_COUNTER==0) MSG_CLEAR=0x59;
            }

            Bundle extras = sbn.getNotification().extras;
            try {
                title = String.valueOf(extras.get("android.title").toString().toLowerCase());
                text = String.valueOf(extras.get("android.text").toString().toLowerCase());
                full = String.valueOf(extras.toString().toLowerCase());

            } catch (Exception e) {
                Log.e(EXCEPTION, getClass().getName() + " onNotificationRemoved" + String.valueOf(e));
            }

            if (Build.VERSION.SDK_INT>=33 && sbn.getPackageName() != null && sbn.getPackageName().equalsIgnoreCase("com.whatsapp")||sbn.getPackageName() != null && sbn.getPackageName().equalsIgnoreCase("com.whatsapp.w4b")) {
                Notification notification = sbn.getNotification();
                if (Build.VERSION.SDK_INT>=33 && notification != null) {
                    whatsAppNotificationRemoved(notification);
                }
            }
        }

            super.onNotificationRemoved(sbn, rankingMap, reason);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void whatsAppNotificationRemoved(Notification notification) {
        boolean foundAnyMsg = false;

        //check for message
        /*if (notification.category != null && notification.category.equalsIgnoreCase("msg") && notification.getGroup() != null && notification.getGroup().contains("group_key_messages")) {
            for (StatusBarNotification sbn2 : getActiveNotifications()) {
                    Notification notification2 = sbn2.getNotification();
                    if (notification2 != null)
                        if (notification2.getGroup() != null && notification2.getGroup().contains("group_key_messages")) {
                            foundAnyMsg = true;
                         }
            }
            if (!foundAnyMsg) {//optimise this
                clearWhatsAppMsgCounter();
            }
        }*/

        //check for call
        if (notification.category != null && notification.category.equalsIgnoreCase("call")) {
            foundAnyMsg = false;
            for (StatusBarNotification sbn2 : getActiveNotifications()) {
                if (sbn2.getPackageName() != null && sbn2.getPackageName().equalsIgnoreCase("com.whatsapp") || sbn2.getPackageName() != null && sbn2.getPackageName().equalsIgnoreCase("com.whatsapp.w4b")) {
                    Notification notification2 = sbn2.getNotification();
                    if (notification2 != null) {
                        if (notification2.category != null && notification2.category.equalsIgnoreCase("call")) {
                            foundAnyMsg = true;
                        }
                    }
                }
            }


            //if (!foundAnyMsg) clearWhatsAppCallCounter();
        }
    }

    /*private void clearWhatsAppCallCounter() {
        //cancel msgClear timer if that is running
        if (callClearTimer != null) {
            callClearTimer.cancel();
            callClearTimer.purge();
            callClearTimer = null;
        }
        // set missed call clear flag to true
        Log.e("BLEservice","wpcleared");
        CALL_CLEAR = 0x59;
    }*/

    /*private void clearWhatsAppMsgCounter() {
        //cancel msgClear timer if that is running

        *//*if (smsCounterTimer != null) {
            smsCounterTimer.cancel();
            smsCounterTimer.purge();
            smsCounterTimer = null;
        }*//*
        // set msg clear flag to true
        //MSG_CLEAR = 0x59;//deactivated recently
//        new Handler(Looper.getMainLooper()).postDelayed(() -> MSG_CLEAR = 0x4E,3000);
    }*/

    class NLServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getStringExtra("command").equals("clearall")) {
                NotificationService.this.cancelAllNotifications();
            } else if (intent.getStringExtra("command").equals("list")) {
                Intent i1 = new Intent("NOTIFICATION_LISTENER_EXAMPLE");
                i1.putExtra("notification_event", "=====================");
                sendBroadcast(i1);
                int i = 1;
                for (StatusBarNotification sbn : NotificationService.this.getActiveNotifications()) {
                    Intent i2 = new Intent("NOTIFICATION_LISTENER_EXAMPLE");
                    i2.putExtra("notification_event", i + " " + sbn.getPackageName() + "\n");
                    sendBroadcast(i2);
                    i++;
                }
                Intent i3 = new Intent("NOTIFICATION_LISTENER_EXAMPLE");
                i3.putExtra("notification_event", "===== Notification List ====");
                sendBroadcast(i3);

            }
        }
    }

    public byte[] getSmartWhatspPhoneCALLPKT(String data, String incomingCallType, String callActivSTatus) {
        String PhoneData;
        byte[] RetArray = new byte[30];

        try {
            if (data.length() <= 20) {

                for (int i = data.length(); i <= 20; i++) {
                    data += "\0";
                }


                PhoneData = "?"/*Start of Frame*/ + "2"/*Frame ID*/ + data/*Data to icd*/ /* */ + "W1"/*Fill 0s at reserved byte positions*/ /*End of Frame*/ + "\0\0\0\0\0";

            } else {

                data = data.substring(0, 20);
                PhoneData = "?"/*Start of Frame*/ + "2"/*Frame ID*/ + data/*Phone data to icd*/ + "W1"/*Fill 0s at reserved byte positions*//*End of Frame*/ + "\0\0\0\0\0";
            }

            try {

                RetArray = PhoneData.getBytes("UTF-8");
                RetArray[0] = (byte) 0xA5;

                RetArray[22] = (byte) 0x57;
                RetArray[23] = (byte) 0x31;

                if (callActivSTatus == "2") {
                    RetArray[23] = (byte) 0x32;
                } else {
                    RetArray[23] = (byte) 0x31;
                }
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

    /*public void get_notifications(){
        try {
            if (SMSClearTimer != null) {
                SMSClearTimer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        Log.e("get_notifications","CALLED");
                        StatusBarNotification sbn[]=getActiveNotifications();

                        for(StatusBarNotification sbn_one:sbn){
                            Bundle extras=sbn_one.getNotification().extras;
                            String title=" ", text=" ", full=" ", test=" ";
                            try {
                                title = String.valueOf(extras.get("android.title").toString().toLowerCase());
                                text = String.valueOf(extras.get("android.text").toString().toLowerCase());
                                full = String.valueOf(extras.toString().toLowerCase());

                                if(full.contains("msg") || full.contains("messaging") || full.contains("messages") || full.contains("mms")){
                                    SMS_PENDING=true;
                                    break;
                                }else SMS_PENDING=false;

                            }catch (Exception e){ }
                        }
                        if(!SMS_PENDING){
                            SMS_COUNTER=0;
                            MSG_CLEAR=0x59;

                            if(SMSClearTimer!=null){
                                SMSClearTimer.cancel();
                                SMSClearTimer.purge();
                                SMSClearTimer = null;
                                SMSClearTimer = new Timer();
                            }
                        }
                    }
                }, 0, 3000);
            }
        } catch (Exception e) {
            Log.e(EXCEPTION,"NL_service: get_notification: "+String.valueOf(e));
        }
    }*/
}