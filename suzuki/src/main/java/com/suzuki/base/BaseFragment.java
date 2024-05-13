 package com.suzuki.base;

import static android.content.Context.MODE_PRIVATE;

import static com.suzuki.application.SuzukiApplication.calculateCheckSum;

import android.app.AlertDialog;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;

import android.util.Log;
import android.view.View;
import android.widget.Toast;


import androidx.fragment.app.Fragment;

import com.suzuki.R;
import com.suzuki.preferences.Preferences;

public class BaseFragment extends Fragment {
    protected Preferences preferences;
    static ProgressDialog mProgressDialog;
    public static String test;



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


    public void ClusterDataPacket(byte[] u1_buffer) {
        if ((u1_buffer[0] == -91) && (u1_buffer[1] == 55) && (u1_buffer[29] == 127)) {

            byte Crc = calculateCheckSum(u1_buffer);

            if (u1_buffer[28] == Crc) {
                String Cluster_data = new String(u1_buffer);

                /*sharedPreferences = getApplicationContext().getSharedPreferences("vehicle_data",MODE_PRIVATE);
                editor = sharedPreferences.edit();`
                editor.putString("odometer",Odometer);
                editor.apply();*/

                int top_speeds= Integer.parseInt(Cluster_data.substring(2,5));
                SharedPreferences.Editor editors = getActivity().getSharedPreferences("top_speed", Context.MODE_MULTI_PROCESS).edit();
                editors.putInt("top_speed", top_speeds);
                editors.apply();

                SharedPreferences prefs = getActivity().getSharedPreferences("top_speed", MODE_PRIVATE);
                int saved_speed = prefs.getInt("top_speed", 0);//"No name defined" is the default value.


                if (top_speeds>=saved_speed){
                    SharedPreferences.Editor edit = getActivity().getSharedPreferences("top_speed", MODE_PRIVATE).edit();
                    edit.putInt("new_top_speed", top_speeds);
                    edit.apply();
                }
                // Toast.makeText(app, ""+speed, Toast.LENGTH_SHORT).show();

            }
        }
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
