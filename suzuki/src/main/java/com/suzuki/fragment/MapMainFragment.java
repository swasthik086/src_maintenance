package com.suzuki.fragment;

import static android.os.Looper.getMainLooper;
import static android.view.View.GONE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.suzuki.R;
import com.suzuki.activity.DeviceListingScanActivity;
import com.suzuki.activity.RouteActivity;
import com.suzuki.activity.TripActivity;
import com.suzuki.adapter.AutoCompleteTextWatcher;
import com.suzuki.adapter.AutoSuggestAdapter;
import com.suzuki.adapter.DragDropRecyclerViewAdapter;
import com.suzuki.adapter.MapRecentSearchAdapter;
import com.suzuki.adapter.NearByAdapter;
import com.suzuki.adapter.RecyclerItemClickListener;
import com.suzuki.application.SuzukiApplication;
import com.suzuki.broadcaster.BleConnection;
import com.suzuki.interfaces.IOnclickFromAdapterToActivityAndFragment;
import com.suzuki.interfaces.ItemMoveCallback;
import com.suzuki.interfaces.MapDragListInterface;
import com.suzuki.interfaces.StartDragListener;
import com.suzuki.maps.plugins.BearingIconPlugin;
import com.suzuki.maps.plugins.DirectionPolylinePlugin;
import com.suzuki.pojo.EvenConnectionPojo;
import com.suzuki.pojo.MapListCustomClass;
import com.suzuki.pojo.MapListRealmModule;
import com.suzuki.pojo.MapRecentRealmModule;
import com.suzuki.pojo.MapWorkAndHomeRealmModule;
import com.suzuki.pojo.ViaPointLocationRealmModel;
import com.suzuki.utils.CheckInternet;
import com.suzuki.utils.Common;
import com.suzuki.utils.DataRequestManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;
import jahirfiquitiva.libs.fabsmenu.FABsMenu;
import jahirfiquitiva.libs.fabsmenu.FABsMenuListener;
import jahirfiquitiva.libs.fabsmenu.TitleFAB;
import timber.log.Timber;

import static com.suzuki.activity.HomeScreenActivity.TOAST_DURATION;

import static com.suzuki.adapter.DragDropRecyclerViewAdapter.updatedCustomList;
import static com.suzuki.broadcaster.BluetoothCheck.BLUETOOTH_STATE;
import static com.suzuki.fragment.DashboardFragment.staticConnectionStatus;
import static com.suzuki.utils.Common.BikeBleName;


public class MapMainFragment extends Fragment {
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.base_layout, container, false);

        return view;
    }
}