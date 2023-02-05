 package com.suzuki.base;

import android.app.AlertDialog;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import android.view.View;
import android.widget.Toast;


import androidx.fragment.app.Fragment;

import com.suzuki.R;
import com.suzuki.preferences.Preferences;

public class BaseFragment extends Fragment {
    protected Preferences preferences;
    static ProgressDialog mProgressDialog;


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    public static void showToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    public void showAlertBox(String message) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle("Error");
        alert.setMessage(message);
        alert.setPositiveButton("OK", null);
        alert.show();
    }

    public void showAlertBoxForNoInternet() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle("Error");
        alert.setCancelable(false);
        alert.setMessage(getResources().getString(R.string.no_internet_connection));
        alert.setPositiveButton("OK", (dialog, which) -> {
            Intent intent = new Intent(
                    Settings.ACTION_WIFI_SETTINGS);
            startActivity(intent);

        });
        alert.show();
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Bundle action);
    }


    public static void showProgress(final Context context) {
        mProgressDialog = ProgressDialog.show(context, null, "Please wait", true, false);


    }

    public static void dismissProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

}
