package com.suzuki.fragment;

import static com.mappls.sdk.maps.Mappls.getApplicationContext;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.suzuki.R;
import com.suzuki.activity.HelpActivity;
import com.suzuki.activity.LastParkedLocationActivity;

import com.suzuki.activity.ProfileActivity;
import com.suzuki.activity.RiderProfileActivity;
import com.suzuki.activity.TripActivity;
import com.suzuki.base.BaseFragment;
import com.suzuki.utils.Common;

import java.util.Calendar;


public class MoreFragment extends BaseFragment {

    private Common common;
    private long prev=0;
    private View view;

    private RelativeLayout faq_disclaimer;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         view = inflater.inflate(R.layout.more_fragment, container, false);

        common = new Common(getContext());
        faq_disclaimer=view.findViewById(R.id.faq_disclaimer);

        LinearLayout llTrips = (LinearLayout) view.findViewById(R.id.llTrips);
        llTrips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (common.isDoubleClicked())
                    return;
                Intent i = new Intent(getActivity(), TripActivity.class);
                startActivity(i);

            }
        });

        LinearLayout llLastParkedLoc = (LinearLayout) view.findViewById(R.id.llLastParkedLoc);
        llLastParkedLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (common.isDoubleClicked())
                    return;
                Intent i = new Intent(getActivity(), LastParkedLocationActivity.class);
                startActivity(i);
            }
        });

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());//this==context
        if (!prefs.contains("FirstTimeFaq")) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("FirstTimeFaq", true);
            editor.commit();
            change_color_popup();

        }

        LinearLayout llProfile1 = (LinearLayout) view.findViewById(R.id.llProfile);
        llProfile1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (common.isDoubleClicked())
                    return;
                Intent i = new Intent(getActivity(), RiderProfileActivity.class);
                startActivity(i);
            }
        });

        LinearLayout llHelp = (LinearLayout) view.findViewById(R.id.llHelp);
        llHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (common.isDoubleClicked())
                    return;
                Intent i = new Intent(getActivity(), HelpActivity.class);
                startActivity(i);

            }
        });

        return view;

    }

    public void change_color_popup(){
        //  changeColorAlert("Do you want to change the vehicle color?");

        long current= Calendar.getInstance().getTimeInMillis();
        if(current>prev+5000){
            prev=current;
            faq_disclaimer.setVisibility(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    faq_disclaimer.setVisibility(View.GONE);
                    Animation aniSlide = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.anim);
                    faq_disclaimer.startAnimation(aniSlide);
                }
            }, 5000);
        }
    }

    private void show_alert(View view, int message, int duration, String action_button_name) {
        Snackbar snackbar= Snackbar.make(view, message, duration);
        snackbar.setAction(action_button_name, view1 -> {

                    switch (message){
                        case R.string.change_colour:
                            view1.getContext().startActivity(new Intent(view1.getContext(), ProfileActivity.class));
                            break;
                    }
                })
                .setActionTextColor(Color.parseColor("#17B5D0"))
                .show();

    }


}
