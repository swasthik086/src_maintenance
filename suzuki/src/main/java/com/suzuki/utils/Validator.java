package com.suzuki.utils;

import android.text.TextUtils;

import com.mappls.sdk.maps.geometry.LatLng;


/**
 * Created by rashmijv on 03/05/17.
 */

public class Validator {

    private static final String REGEX_ALPHA_NUMERIC = "[A-Za-z0-9]+";


    public static boolean isAlphanumeric(String target) {
        return target != null && target.matches(REGEX_ALPHA_NUMERIC);
    }
//    connect(bleDevice);
    public static boolean isValidLatLng(LatLng latLng) {
        if (latLng == null) {
            return false;
        } else {
            return isValidLatLng(latLng.getLatitude(), latLng.getLongitude());
        }
    }

    public static boolean isValidLatLng(double lat, double lng) {
        return lat > 0 && lat < 90 && lng > 0 && lng < 180;
    }

    /**
     * eLoc must exactly be equals to alphanumeric 6 digits
     */
    public static boolean isValidEloc(String eLoc) {
        return !TextUtils.isEmpty(eLoc) && eLoc.length() == 6 && isAlphanumeric(eLoc);
    }
}

