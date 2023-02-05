package com.suzuki.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class AccessService extends AccessibilityService {

    @Override
    public void onCreate() {
        super.onCreate();

        System.out.println("onServiceCreated");
        Toast.makeText(this, "onServiceCreated", Toast.LENGTH_SHORT).show();
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK;
        info.notificationTimeout = 100;
        info.packageNames = null;
        setServiceInfo(info);
    }

    @Override
    protected void onServiceConnected() {
        Log.d("tag", "onServiceConnected: ");
        Toast.makeText(this, "service connected", Toast.LENGTH_SHORT).show();
        super.onServiceConnected();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d("tag", "onAccessibilityEvent: "+event.toString());
        Toast.makeText(this, "accessibility", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onInterrupt() {
        Log.d("tag", "onInterrupt: ");
        Toast.makeText(this, "interrupt", Toast.LENGTH_SHORT).show();
    }
}
