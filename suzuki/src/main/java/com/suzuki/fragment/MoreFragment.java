package com.suzuki.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.suzuki.R;
import com.suzuki.activity.HelpActivity;
import com.suzuki.activity.LastParkedLocationActivity;

import com.suzuki.activity.RiderProfileActivity;
import com.suzuki.activity.TripActivity;
import com.suzuki.base.BaseFragment;
import com.suzuki.utils.Common;


public class MoreFragment extends BaseFragment {

    private Common common;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.more_fragment, container, false);

        common = new Common(getContext());
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


}
