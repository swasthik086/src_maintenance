package com.suzuki.fragment;

import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;


import com.suzuki.R;

import com.suzuki.activity.NavigationActivity;
import com.suzuki.application.SuzukiApplication;
import com.suzuki.maps.plugins.DirectionPolylinePlugin;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;


import static com.suzuki.fragment.DashboardFragment.app;
import static com.suzuki.fragment.DashboardFragment.prev_cluster_name;
import static com.suzuki.utils.Common.BikeBleName;

public class RouteFragment extends Fragment{


    public RouteFragment() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
  //      app = getMyApplication();


        if (savedInstanceState == null) {

            app.setTrip(null);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_route, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

}



