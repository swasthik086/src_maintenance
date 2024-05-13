package com.suzuki.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.viewmodel.CreationExtras;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.suzuki.R;
import com.suzuki.activity.DeviceListingScanActivity;
import com.suzuki.activity.LastParkedLocationActivity;
import com.suzuki.activity.NavigationActivity;
import com.suzuki.activity.RouteActivity;
import com.suzuki.activity.RouteNearByActivity;
import com.suzuki.adapter.NavigationPagerAdapter;
import com.suzuki.application.SuzukiApplication;
import com.suzuki.broadcaster.BleConnection;
import com.suzuki.broadcaster.BluetoothCheck;
import com.suzuki.broadcaster.MapShortDistBroadcast;
import com.suzuki.maps.camera.MathUtils;

import com.suzuki.maps.camera.ProgressChangeListener;
import com.suzuki.maps.camera.RouteInformation;
import com.suzuki.maps.plugins.BearingIconPlugin;
import com.suzuki.maps.plugins.DirectionPolylinePlugin;
import com.suzuki.maps.plugins.MapEventsPlugin;
import com.suzuki.maps.plugins.RouteArrowPlugin;
import com.suzuki.model.Stop;
import com.suzuki.pojo.EvenConnectionPojo;
import com.suzuki.pojo.RiderProfileModule;
import com.suzuki.utils.Logger;
import com.suzuki.utils.NavigationLocationEngine;
import com.suzuki.utils.Utils;
import com.suzuki.views.LockableBottomSheetBehavior;
import com.suzuki.views.RecenterButton;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;

import io.realm.Realm;
import timber.log.Timber;

import static android.content.Context.MODE_PRIVATE;
import static android.view.View.GONE;
import static com.suzuki.activity.HomeScreenActivity.HOME_SCREEN_OBJ;
import static com.suzuki.activity.HomeScreenActivity.mBoundService;
import static com.suzuki.broadcaster.BluetoothCheck.BLUETOOTH_STATE;
import static com.suzuki.fragment.DashboardFragment.BLE_CONNECTED;
import static com.suzuki.fragment.DashboardFragment.staticConnectionStatus;

import static com.suzuki.fragment.DashboardFragment.NoSignal;
import static com.suzuki.utils.Common.BikeBleName;
import static com.suzuki.utils.Common.EXCEPTION;
import static com.suzuki.utils.Common.FIRST_TIME;


public class NavigationFragment extends Fragment {

//        implements
//        View.OnClickListener,
//        MapplsMap.OnMoveListener,
//        LocationChangedListener,
//        INavigationListener,
//        OnMapReadyCallback, INavigation {


    public NavigationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}