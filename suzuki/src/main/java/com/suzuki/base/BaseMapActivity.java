package com.suzuki.base;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;


import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.mappls.sdk.maps.MapplsMap;
import com.mappls.sdk.maps.geometry.LatLng;
import com.mappls.sdk.navigation.activities.MapActivity;
import com.suzuki.R;

import timber.log.Timber;

public abstract class BaseMapActivity extends MapActivity {

    FragmentManager manager;
//    private MaterialDialog mProgressDialog;
    private boolean safeToPerformFragmentTransactions = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void navigateTo(Fragment newFragment, boolean addToBackStack) {
        try {
            manager = getSupportFragmentManager();
            FragmentTransaction ft = manager.beginTransaction();
            ft.replace(R.id.fragment_container, newFragment, newFragment.getClass().getSimpleName());
            try {
                if (addToBackStack) {
                    String name = newFragment.getClass().getSimpleName();
                    ft.addToBackStack(name);
                }
                ft.commitAllowingStateLoss();
                manager.executePendingTransactions();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @SuppressLint("MissingPermission")
    public abstract void onConnected();

    public abstract void onLocationChanged(Location location);

    public abstract boolean onMapLongClick(LatLng latLng);

    public abstract void onMapError(int i, String s);

    public abstract void onMapReady(MapplsMap map);


//    public void showProgress() {
//        if (!safeToPerformFragmentTransactions)
//            return;
//
//        if (isProgressVisible()) {
//            return;
//        }
//
//        mProgressDialog = new MaterialDialog.Builder(this)
//                .content("Loading...Please Wait")
//                .progress(true, 0)
//                .progressIndeterminateStyle(false)
//                .build();
//
//        mProgressDialog.setCancelable(true);
//        mProgressDialog.setCanceledOnTouchOutside(false);
//        mProgressDialog.show();
//    }

//    public void hideProgress() {
//        if (isProgressVisible()) {
//            if (safeToPerformFragmentTransactions) {
//                mProgressDialog.dismiss();
//            }
//        }
//    }

//    public boolean isProgressVisible() {
//        if (mProgressDialog != null) {
//            return mProgressDialog.isShowing();
//        }
//        return false;
//    }


}
