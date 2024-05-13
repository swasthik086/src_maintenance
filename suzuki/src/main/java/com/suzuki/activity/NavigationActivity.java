package com.suzuki.activity;

import static com.suzuki.activity.RouteActivity.viaPoints;
import static com.suzuki.activity.RouteNearByActivity.tripID;
import static com.suzuki.application.SuzukiApplication.calculateCheckSum;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.suzuki.R;

import com.suzuki.application.SuzukiApplication;
import com.suzuki.base.BaseMapActivity;
import com.suzuki.fragment.MapMainFragment;
import com.suzuki.fragment.NavigationFragment;
import com.suzuki.maps.plugins.BearingIconPlugin;
import com.suzuki.maps.plugins.DirectionPolylinePlugin;
import com.suzuki.maps.plugins.MapEventsPlugin;
import com.suzuki.maps.plugins.RouteArrowPlugin;
import com.suzuki.pojo.ClusterStatusPktPojo;
import com.suzuki.pojo.RecentTripRealmModule;
import com.suzuki.pojo.RiderProfileModule;
import com.suzuki.pojo.ViaPointLocationRealmModel;
import com.suzuki.utils.DataRequestManager;
import com.suzuki.utils.NavigationCompassEngine;
import com.suzuki.utils.NavigationLocationEngine;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import timber.log.Timber;


public class NavigationActivity extends AppCompatActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }
}