package com.suzuki.base;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import com.suzuki.R;
import com.suzuki.preferences.Preferences;

import timber.log.Timber;


public abstract class BaseActivity extends AppCompatActivity {


    static ProgressDialog mProgressDialog;
    public  static  boolean navigationStarted= false;

    protected Preferences preferences;
    FragmentManager manager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    public static void showProgress(final Context context) {
        mProgressDialog = ProgressDialog.show(context, null, "Please wait", true, false);
    }

    public static void dismissProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }


    public void showToast(String toastMessage) {

        Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
    }

    public void showAlertBox(String message) {
        AlertDialog.Builder alert = new AlertDialog.Builder(BaseActivity.this);
        alert.setMessage(message);
        alert.setPositiveButton("OK", null);
        alert.show();

    }

    public void showAlertBoxForNoInternet() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Error");
        alert.setCancelable(false);
        alert.setMessage(getResources().getString(R.string.no_internet_connection));
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(
                        Settings.ACTION_WIFI_SETTINGS);
                startActivity(intent);


            }
        });
        alert.show();

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

    public abstract void onLocationChanged(Location location);
}
