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

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.gson.Gson;

import com.mappls.sdk.maps.MapView;
import com.mappls.sdk.maps.MapplsMap;
import com.mappls.sdk.maps.OnMapReadyCallback;
import com.mappls.sdk.maps.Style;
import com.mappls.sdk.maps.annotations.Icon;
import com.mappls.sdk.maps.annotations.IconFactory;
import com.mappls.sdk.maps.annotations.Marker;
import com.mappls.sdk.maps.annotations.MarkerOptions;
import com.mappls.sdk.maps.camera.CameraUpdateFactory;
import com.mappls.sdk.maps.geometry.LatLng;
import com.mappls.sdk.maps.location.LocationComponent;
import com.mappls.sdk.maps.location.LocationComponentActivationOptions;
import com.mappls.sdk.maps.location.LocationComponentOptions;
import com.mappls.sdk.maps.location.engine.LocationEngine;
import com.mappls.sdk.maps.location.engine.LocationEngineCallback;
import com.mappls.sdk.maps.location.engine.LocationEngineRequest;
import com.mappls.sdk.maps.location.engine.LocationEngineResult;
import com.mappls.sdk.maps.location.modes.CameraMode;
import com.mappls.sdk.maps.location.modes.RenderMode;
import com.mappls.sdk.maps.location.permissions.PermissionsListener;
import com.mappls.sdk.maps.location.permissions.PermissionsManager;
import com.mappls.sdk.navigation.NavLocation;
import com.mappls.sdk.services.api.OnResponseCallback;
import com.mappls.sdk.services.api.Place;
import com.mappls.sdk.services.api.PlaceResponse;
import com.mappls.sdk.services.api.autosuggest.model.ELocation;
import com.mappls.sdk.services.api.directions.DirectionsCriteria;
import com.mappls.sdk.services.api.nearby.MapplsNearby;
import com.mappls.sdk.services.api.nearby.MapplsNearbyManager;
import com.mappls.sdk.services.api.nearby.model.NearbyAtlasResponse;
import com.mappls.sdk.services.api.nearby.model.NearbyAtlasResult;
import com.mappls.sdk.services.api.reversegeocode.MapplsReverseGeoCode;
import com.mappls.sdk.services.api.reversegeocode.MapplsReverseGeoCodeManager;
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
import static com.suzuki.activity.RouteNearByActivity.dpToPx;
import static com.suzuki.adapter.DragDropRecyclerViewAdapter.updatedCustomList;
import static com.suzuki.application.SuzukiApplication.isRegionFixed;
import static com.suzuki.broadcaster.BluetoothCheck.BLUETOOTH_STATE;
import static com.suzuki.fragment.DashboardFragment.staticConnectionStatus;
import static com.suzuki.utils.Common.BikeBleName;


public class MapMainFragment extends Fragment implements OnMapReadyCallback, MapplsMap.OnMapLongClickListener,
        PermissionsListener,StartDragListener,MapDragListInterface, IOnclickFromAdapterToActivityAndFragment {

    public static MapView mapView;
    LocationEngineRequest request;
    private static final int REQUEST_CHECK_SETTINGS = 214;
    private String profile = DirectionsCriteria.PROFILE_DRIVING;
    LocationComponent locationComponent;
    private static final long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    LocationComponentActivationOptions locationComponentActivationOptions;

    private static final long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;
    LocationEngineCallback<LocationEngineResult> locationEngineCallback = new LocationEngineCallback<LocationEngineResult>() {
        @Override
        public void onSuccess(LocationEngineResult locationEngineResult) {
            if (locationEngineResult.getLastLocation() != null) {
                Location location = locationEngineResult.getLastLocation();
                mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16));
            }
        }

        @Override
        public void onFailure(@NonNull Exception e) {

        }
    };

    RelativeLayout rlMainLayout;
    ItemTouchHelper touchHelper;
    private MapplsMap mapboxMap;
    private SuzukiApplication app;
    private boolean firstFix;
    private static DirectionPolylinePlugin directionPolylinePlugin;
    private LocationEngine locationEngine;
    private PermissionsManager permissionsManager;
    BearingIconPlugin _bearingIconPlugin;
    Marker marker;

    String dataToNearBy;
    RelativeLayout rlCurrentLocation;
    TitleFAB TFMore, TFab3, TFab2, TFab1, TFab4;
    RecyclerView rvDragDropCats, rvNearbyAutosuggest;
    DragDropRecyclerViewAdapter mAdapter;
    boolean mapIsReady = false;
    RelativeLayout rlMapLayout;
    EditText etSearchLoc;
    EditText etNearbySearchLoc;
    private String fromLocation;
    private AutoCompleteTextWatcher textWatcher;
    private RecyclerView mSuggestionListView;
    ProgressBar apiProgressBar, progressBar;

    public static ELocation eLocation;
    RelativeLayout rlSearchBar;
    LinearLayout llSearchBar;
    LinearLayout llRedAlertBle;
    public static Location currentlocation;
    private BleConnection mReceiver;

    RealmResults<MapListRealmModule> mapListItem;
    Realm realm;
    ArrayList<MapListCustomClass> mapListCustomClassArrayList;
    FABsMenu FabMenu;
    ListView recentSearchLV;
    MapRecentSearchAdapter recentSearchAdapter;
    ConstraintLayout recentHomelayout, homeConstraintLayout, workConstraintLayout;
    View recentSearchGap;
    RealmResults<MapRecentRealmModule> orderedRecentSearchResult = null;
    TextView homeAddressTv, workAddressTv;
    ProgressDialog proDialog;
    private boolean addressSetting, homeAddressSetting, workAddressSetting;
    private TextView addressInfoTv;
    private ImageButton homeEditBtn, homeDeleteBtn, workEditBtn, workDeleteBtn;
    private String navigationHint = "Where do you want to go ?";
    private String setHomeAddressHint = "Search your Home Address", setWorkAddressHint = "Search your Work Address";
    private Common common;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.base_layout, container, false);


        // proDialog = ProgressDialog.show(getContext(), "", "");

        common = new Common(getContext());
        mapView = view.findViewById(R.id.map_view);
        realm = Realm.getDefaultInstance();
        apiProgressBar = view.findViewById(R.id.api_progress_bar);
        FabMenu = view.findViewById(R.id.FabMenu);
        TFMore = view.findViewById(R.id.TFMore);
        TFab3 = view.findViewById(R.id.TFab3);
        TFab2 = view.findViewById(R.id.TFab2);
        TFab1 = view.findViewById(R.id.TFab1);
        TFab4 = view.findViewById(R.id.TFab4);
        etSearchLoc = view.findViewById(R.id.etSearchLoc);
        rlMainLayout = view.findViewById(R.id.rlMainLayout);
        etNearbySearchLoc = view.findViewById(R.id.etNearbySearchLoc);
        mSuggestionListView = view.findViewById(R.id.rvAutosuggest);
        rvNearbyAutosuggest = view.findViewById(R.id.rvNearbyAutosuggest);
        rlSearchBar = view.findViewById(R.id.rlSearchBar);
        llSearchBar = view.findViewById(R.id.llSearchBar);
        rlMapLayout = view.findViewById(R.id.rlMaplayout);
        llRedAlertBle = view.findViewById(R.id.llRedAlertBle);

        rlCurrentLocation = view.findViewById(R.id.rlCurrentLocation);

//        View recentView = view.findViewById(R.id.preFixedView);
        recentSearchLV = view.findViewById(R.id.recentSearchList);
//        recentSearchLayout = view.findViewById(R.id.recentSearchLayout);
        recentHomelayout = view.findViewById(R.id.homeLayout);
        homeConstraintLayout = view.findViewById(R.id.homeConstraintLayout);
        workConstraintLayout = view.findViewById(R.id.workConstraintLayout);
        recentSearchGap = view.findViewById(R.id.gapView);
        homeAddressTv = view.findViewById(R.id.homeAddress);
        workAddressTv = view.findViewById(R.id.workAddress);
        progressBar = view.findViewById(R.id.progressBar);
        addressInfoTv = view.findViewById(R.id.addressInfoTv);
        homeEditBtn = view.findViewById(R.id.editHome);
        homeDeleteBtn = view.findViewById(R.id.deleteHome);
        workEditBtn = view.findViewById(R.id.editWork);
        workDeleteBtn = view.findViewById(R.id.deleteWork);

        textWatcher = new AutoCompleteTextWatcher(getActivity(), mSuggestionListView, apiProgressBar);

        mapListItem = realm.where(MapListRealmModule.class).findAll();

        mapView.onCreate(savedInstanceState);
        readMapItemRecords(realm);
        if (!common.checkGPSIsOpen(getContext())) {
            displayLocationSettingsRequest(getContext());
        }

        getHomeAndWorkAddress(realm);

        homeConstraintLayout.setOnClickListener(v -> {
            MapWorkAndHomeRealmModule homeRealmModule = realm.where(MapWorkAndHomeRealmModule.class)
                    .equalTo("id", "Home").findFirst();
            if (homeRealmModule == null) {

                if (getContext() != null) {
                    Dialog dialog = new Dialog(getContext());
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.custom_alert_dialog);
                    if (dialog.getWindow() != null)
                        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    TextView titleTv = dialog.findViewById(R.id.titleTv);
                    TextView messageTv = dialog.findViewById(R.id.messageTv);
                    Button yesBtn = dialog.findViewById(R.id.yesButton);
                    Button noBtn = dialog.findViewById(R.id.noButton);

                    titleTv.setText(getString(R.string.set_home_address));
                    messageTv.setText(getString(R.string.want_to_proceed));
                    yesBtn.setOnClickListener(view1 -> {
                        dialog.cancel();
                        setRecentLayoutVisibility(false);
                        addressInfoTv.setVisibility(View.VISIBLE);
                        addressSetting = true;
                        homeAddressSetting = true;
                        etSearchLoc.setHint(setHomeAddressHint);
                        etNearbySearchLoc.setHint(setHomeAddressHint);
                    });

                    noBtn.setOnClickListener(view1 -> {
                        addressSetting = false;
                        homeAddressSetting = false;
                        addressInfoTv.setVisibility(View.GONE);
                        etSearchLoc.setHint(navigationHint);
                        etNearbySearchLoc.setHint(navigationHint);
                        dialog.cancel();
                    });

                    dialog.show();

                }

                /*AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setCancelable(true);
                builder.setTitle(getString(R.string.set_home_address));
                builder.setMessage(getString(R.string.want_to_proceed));
                builder.setPositiveButton("Yes", (dialog, which) -> {
                    dialog.cancel();
                    setRecentLayoutVisibility(false);
                    addressInfoTv.setVisibility(View.VISIBLE);
                    addressSetting = true;
                    homeAddressSetting = true;
                    etSearchLoc.setHint(setHomeAddressHint);
                    etNearbySearchLoc.setHint(setHomeAddressHint);
                });
                builder.setNegativeButton("No", (dialog, which) -> {
                    addressSetting = false;
                    homeAddressSetting = false;
                    addressInfoTv.setVisibility(View.GONE);
                    etSearchLoc.setHint(navigationHint);
                    etNearbySearchLoc.setHint(navigationHint);
                    dialog.cancel();
                });
                builder.create();
                builder.show();*/

            } else {
                getDirectionsByRecent(homeRealmModule.getLat(), homeRealmModule.getLng(), homeRealmModule.getPlaceName()
                        , homeRealmModule.getAddress(), homeRealmModule.isNearby());
            }
        });

        homeEditBtn.setOnClickListener(v -> {
            editHomeAddress(getString(R.string.want_to_proceed));
        });

        homeDeleteBtn.setOnClickListener(v -> {
            deleteAddress("Home");
        });

        homeConstraintLayout.setOnLongClickListener(v -> {
            editHomeAddress(getString(R.string.want_to_proceed));
            return true;
        });

        workConstraintLayout.setOnClickListener(v -> {
            MapWorkAndHomeRealmModule workRealmModule = realm.where(MapWorkAndHomeRealmModule.class)
                    .equalTo("id", "Work").findFirst();

            if (workRealmModule == null) {

                if (getContext() != null) {
                    Dialog dialog = new Dialog(getContext());
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.custom_alert_dialog);
                    if (dialog.getWindow() != null)
                        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    TextView titleTv = dialog.findViewById(R.id.titleTv);
                    TextView messageTv = dialog.findViewById(R.id.messageTv);
                    Button yesBtn = dialog.findViewById(R.id.yesButton);
                    Button noBtn = dialog.findViewById(R.id.noButton);

                    titleTv.setText(getString(R.string.set_work_address));
                    messageTv.setText(getString(R.string.want_to_proceed));
                    yesBtn.setOnClickListener(view1 -> {
                        dialog.cancel();
                        setRecentLayoutVisibility(false);
                        addressInfoTv.setVisibility(View.VISIBLE);
                        addressSetting = true;
                        workAddressSetting = true;
                        etSearchLoc.setHint(setWorkAddressHint);
                        etNearbySearchLoc.setHint(setWorkAddressHint);
                    });

                    noBtn.setOnClickListener(view1 -> {
                        addressSetting = false;
                        workAddressSetting = false;
                        addressInfoTv.setVisibility(View.GONE);
                        etSearchLoc.setHint(navigationHint);
                        etNearbySearchLoc.setHint(navigationHint);
                        dialog.cancel();
                    });

                    dialog.show();

                }

                /*AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setCancelable(true);
                builder.setTitle(getString(R.string.set_work_address));
                builder.setMessage(getString(R.string.want_to_proceed));
                builder.setPositiveButton("Yes", (dialog, which) -> {
                    dialog.cancel();
                    setRecentLayoutVisibility(false);
                    addressInfoTv.setVisibility(View.VISIBLE);
                    addressSetting = true;
                    workAddressSetting = true;
                    etSearchLoc.setHint(setWorkAddressHint);
                    etNearbySearchLoc.setHint(setWorkAddressHint);
                });
                builder.setNegativeButton("No", (dialog, which) -> {
                    addressSetting = false;
                    workAddressSetting = false;
                    addressInfoTv.setVisibility(View.GONE);
                    etSearchLoc.setHint(navigationHint);
                    etNearbySearchLoc.setHint(navigationHint);
                    dialog.cancel();
                });
                builder.create();
                builder.show();
*/
            } else {
                getDirectionsByRecent(workRealmModule.getLat(), workRealmModule.getLng(), workRealmModule.getPlaceName()
                        , workRealmModule.getAddress(), workRealmModule.isNearby());
            }

        });

        workEditBtn.setOnClickListener(v -> {
            editWorkAddress(getString(R.string.want_to_proceed));
        });

        workDeleteBtn.setOnClickListener(v -> {
            deleteAddress("Work");
        });

        workConstraintLayout.setOnLongClickListener(v -> {
            editWorkAddress(getString(R.string.want_to_proceed));
            return true;
        });

        recentSearchLV.setOnItemClickListener((parent, view12, position, id) -> {
            if (orderedRecentSearchResult == null)
                return;
            if (position >= orderedRecentSearchResult.size())
                return;
            MapRecentRealmModule module = orderedRecentSearchResult.get(position);
            assert module != null;
            getDirectionsByRecent(module.getLat(), module.getLng(), module.getPlaceName()
                    , module.getAddress(), module.isNearby());
        });

        recentSearchLV.setOnItemLongClickListener((parent, view13, position, id) -> {

            if (getContext() != null) {
                Dialog dialog = new Dialog(getContext());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.custom_alert_dialog);
                if (dialog.getWindow() != null)
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                TextView titleTv = dialog.findViewById(R.id.titleTv);
                TextView messageTv = dialog.findViewById(R.id.messageTv);
                Button yesBtn = dialog.findViewById(R.id.yesButton);
                Button noBtn = dialog.findViewById(R.id.noButton);

                titleTv.setText(getString(R.string.delete_recent_search));
                messageTv.setText(getString(R.string.want_to_proceed));
                yesBtn.setOnClickListener(view1 -> {
                    MapRecentRealmModule module = orderedRecentSearchResult.get(position);
                    if (module != null) {
                        realm.executeTransaction(realm1 -> {
                            orderedRecentSearchResult.deleteFromRealm(position);
                            recentSearchAdapter.notifyDataSetChanged();
                        });
                    }
                    dialog.cancel();
                });

                noBtn.setOnClickListener(view1 -> {
                    dialog.cancel();
                });

                dialog.show();

            }

           /* new AlertDialog.Builder(getContext())
                    .setCancelable(true)
                    .setTitle(getString(R.string.delete_recent_search))
                    .setMessage(getString(R.string.want_to_proceed))
                    .setPositiveButton("Yes", (dialog, which) -> {
                        MapRecentRealmModule module = orderedRecentSearchResult.get(position);
                        if (module != null) {
                            realm.executeTransaction(realm1 -> {
                                orderedRecentSearchResult.deleteFromRealm(position);
                                recentSearchAdapter.notifyDataSetChanged();
                            });
                        }
                        dialog.cancel();
                    })
                    .setNegativeButton("No", ((dialog, which) -> dialog.cancel()))
                    .create()
                    .show();*/
            return true;
        });

        rlMainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FabMenu.collapse();
                etSearchLoc.setCursorVisible(false);
            }
        });

        etSearchLoc.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    FabMenu.collapse();
//                    FabMenu.setVisibility(View.INVISIBLE);
//                    rlCurrentLocation.setVisibility(View.INVISIBLE);

                } else {
                    FabMenu.setVisibility(View.VISIBLE);
                    rlCurrentLocation.setVisibility(View.VISIBLE);
                }
            }
        });

        etNearbySearchLoc.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    FabMenu.collapse();
                }
            }
        });


        etNearbySearchLoc.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    FabMenu.collapse();
                    String etNearby = etNearbySearchLoc.getText().toString();
                    String etSearch = etSearchLoc.getText().toString();
                    if (etNearby.length() < 1 && etSearch.length() < 1) {
                        setRecentLayoutVisibility(true);
                    }
                    return false;
                }
                return false;
            }
        });

//            etNearbySearchLoc.setOnTouchListener((v, event) -> {
//
//
//                return false;
//            });

        etSearchLoc.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                FabMenu.collapse();
                String etNearby = etNearbySearchLoc.getText().toString();
                String etSearch = etSearchLoc.getText().toString();
                if (etNearby.length() < 1 && etSearch.length() < 1) {
                    setRecentLayoutVisibility(true);
                }
                return false;
            }
            return false;
        });

        llSearchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FabMenu.collapse();
                String etNearby = etNearbySearchLoc.getText().toString();
                String etSearch = etSearchLoc.getText().toString();
                if (etNearby.length() < 1 && etSearch.length() < 1) {
                    setRecentLayoutVisibility(true);
                }
            }
        });
        etNearbySearchLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FabMenu.collapse();
                String etNearby = etNearbySearchLoc.getText().toString();
                String etSearch = etSearchLoc.getText().toString();
                if (etNearby.length() < 1 && etSearch.length() < 1) {
                    setRecentLayoutVisibility(true);
                }
            }
        });

        etSearchLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String etNearby = etNearbySearchLoc.getText().toString();
                String etSearch = etSearchLoc.getText().toString();
                if (etNearby.length() < 1 && etSearch.length() < 1) {
                    setRecentLayoutVisibility(true);
                }

                if (etSearchLoc.getText().length() < 1) {
//                    recentSearchLayout.setVisibility(View.VISIBLE);
                    FabMenu.setVisibility(View.INVISIBLE);
                    rlCurrentLocation.setVisibility(View.INVISIBLE);
                    setRecentLayoutVisibility(true);
                }
            }
        });

        rlSearchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FabMenu.collapse();
                String etNearby = etNearbySearchLoc.getText().toString();
                String etSearch = etSearchLoc.getText().toString();
                if (etNearby.length() < 1 && etSearch.length() < 1) {
                    setRecentLayoutVisibility(true);
                }
            }
        });

        mapView.setOnTouchListener((v, event) -> {
            FabMenu.collapseImmediately();
            etSearchLoc.clearFocus();

            etSearchLoc.setText("");
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(etSearchLoc.getWindowToken(), 0);
            InputMethodManager imms = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imms.hideSoftInputFromWindow(etNearbySearchLoc.getWindowToken(), 0);
            rvNearbyAutosuggest.setVisibility(View.GONE);
            mSuggestionListView.setVisibility(View.GONE);
            etSearchLoc.setVisibility(View.VISIBLE);
            etNearbySearchLoc.setVisibility(View.GONE);
//                recentSearchLayout.setVisibility(View.GONE);
            setRecentLayoutVisibility(false);
            return (false);
        });


        etNearbySearchLoc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                etSearchLoc.setText("");
                dataToNearBy = s.toString();

                if (s.toString().contentEquals("Suzuki Service")) {

                    dataToNearBy = "SMISST"; //SMIDLR;SMIBRN;   SMISST;SMIDLR;SMIBRN
                } else if (s.toString().contentEquals("Fuel Station")) {
                    dataToNearBy = "TRNPMP";
                } else if (s.toString().contentEquals("Hospitals")) {
                    dataToNearBy = "HLTCLI;HLTHSP;CLICHD;HSPCHD";
                } else if (s.toString().contentEquals("Banks and ATM")) {
                    dataToNearBy = "FINATM;FINBNK";
                } else if (s.toString().contentEquals("Food and Restaurants")) {
                    dataToNearBy = "FODIND;FODPUB;FODRDS;FODCOF;FODFFD;FODCON;FODOTL;FODPLZ;FODNGT";
                } else if (s.toString().contentEquals("Suzuki Sales")) {
                    dataToNearBy = "SMIDLR;SMIBRN";
                } else if (s.toString().contentEquals("Tyre Repair Shops")) {
                    dataToNearBy = "REP2WL;STRTYR;SHPREP";
                } else if (s.toString().contentEquals("Medical Stores")) {
                    dataToNearBy = "HLTMDS;MDSAUR;MDSVET;MDS24H";
                } else if (s.toString().contentEquals("Parking")) {
                    dataToNearBy = "PRKSRF;PRKWPM;PRKRDS;PRKMBK;TRNPRK;PRKUNG;PRKMLT";
                } else if (s.toString().contentEquals("Convenience Stores")) {
                    dataToNearBy = "RTSKRN;RTSBKS;RTSMBT;SHPRTC;RTCMBL;SHPPLZ;GOVRTS;SHPDST;RTCSPR;MKTCPX;SHPMKT";
                } else {
                    dataToNearBy = s.toString();
                    etNearbySearchLoc.setVisibility(View.GONE);
                    etSearchLoc.setVisibility(View.VISIBLE);
                    etSearchLoc.setCursorVisible(true);
                    if (etNearbySearchLoc != null) {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(etNearbySearchLoc.getWindowToken(), 0);
                    }
                    rvNearbyAutosuggest.setVisibility(View.GONE);
                    mSuggestionListView.setVisibility(View.GONE);
                }
                FabMenu.collapse();

                rvNearbyAutosuggest.setVisibility(View.GONE);
                if (currentlocation != null) {
                    if (dataToNearBy != null) {
                        if (dataToNearBy.contentEquals("FODIND;FODPUB;FODRDS;FODCOF;FODFFD;FODCON;FODOTL;FODPLZ;FODNGT") || dataToNearBy.contentEquals("FINATM;FINBNK") || dataToNearBy.contentEquals("HLTCLI;HLTHSP;CLICHD;HSPCHD") || dataToNearBy.contentEquals("TRNPMP") || dataToNearBy.contentEquals("SMISST") || dataToNearBy.contentEquals("SMIDLR;SMIBRN") || dataToNearBy.contentEquals("REP2WL;STRTYR;SHPREP") || dataToNearBy.contentEquals("HLTMDS;MDSAUR;MDSVET;MDS24H") || dataToNearBy.contentEquals("PRKSRF;PRKWPM;PRKRDS;PRKMBK;TRNPRK;PRKUNG;PRKMLT") || dataToNearBy.contentEquals("RTSKRN;RTSBKS;RTSMBT;SHPRTC;RTCMBL;SHPPLZ;GOVRTS;SHPDST;RTCSPR;MKTCPX;SHPMKT")) {

                            if (CheckInternet.isNetworkAvailable(getContext())) {
                                apiProgressBar.setVisibility(View.VISIBLE);

                                getNearBy(currentlocation.getLatitude(), currentlocation.getLongitude(), dataToNearBy);
                            } else {
//                    Toast.makeText(getContext(), "Please check your internet!", Toast.LENGTH_SHORT).show();

                            }
                        } else if (dataToNearBy.contentEquals("")) {
                            rvNearbyAutosuggest.setVisibility(View.GONE);
                            etNearbySearchLoc.setVisibility(View.GONE);
                            etSearchLoc.setVisibility(View.VISIBLE);
                            etSearchLoc.setFocusable(true);
                            etSearchLoc.setCursorVisible(true);
                            if (etNearbySearchLoc != null) {
                                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(etNearbySearchLoc.getWindowToken(), 0);
                            }

                        }

                    } else {
                        rvNearbyAutosuggest.setVisibility(View.GONE);
                        etNearbySearchLoc.setVisibility(View.GONE);
                        etSearchLoc.setVisibility(View.VISIBLE);
                        etSearchLoc.setFocusable(true);
                        etSearchLoc.setCursorVisible(true);
                        if (etNearbySearchLoc != null) {
                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(etNearbySearchLoc.getWindowToken(), 0);
                        }

                    }
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    setRecentLayoutVisibility(false);
//                    recentSearchLayout.setVisibility(View.GONE);
                } else {
                    setRecentLayoutVisibility(true);
//                    recentSearchLayout.setVisibility(View.VISIBLE);
                }

            }
        });
        FabMenu.setMenuListener(new FABsMenuListener() {
            @Override
            public void onMenuClicked(FABsMenu fabsMenu) {
                super.onMenuClicked(fabsMenu);

                FabMenu.invalidate();
                FabMenu.setAnimationDuration(0);

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etSearchLoc.getWindowToken(), 0);

                rvNearbyAutosuggest.setVisibility(View.GONE);
                mSuggestionListView.setVisibility(View.GONE);
//                recentSearchLayout.setVisibility(View.GONE);
                setRecentLayoutVisibility(false);

            }

            @Override
            public void onMenuExpanded(FABsMenu fabsMenu) {
                super.onMenuExpanded(fabsMenu);

                etSearchLoc.setVisibility(View.GONE);
                etNearbySearchLoc.setVisibility(View.VISIBLE);
                etNearbySearchLoc.setText("");
            }

            @Override
            public void onMenuCollapsed(FABsMenu fabsMenu) {
                super.onMenuCollapsed(fabsMenu);
//                etNearbySearchLoc.setVisibility(View.GONE);
//                etSearchLoc.setVisibility(View.VISIBLE);
            }
        });

        TFMore.setOnClickListener(v -> {
            FabMenu.collapse();

            Dialog dialog = new Dialog(getActivity(), android.R.style.Theme_Translucent_NoTitleBar);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.categories_activity);
            Window window = dialog.getWindow();
            WindowManager.LayoutParams wlp = window.getAttributes();

            ImageView ivSave = dialog.findViewById(R.id.ivSave);
            ivSave.setOnClickListener(v1 -> {
                Log.d("sckckkc", "--" + updatedCustomList.size());
                if (updatedCustomList.size() > 0) {
                    addUpdatedMapListDataToRealm(updatedCustomList);
                    readMapItemRecords(realm);
                    FabMenu.invalidate();
                    dialog.cancel();
                } else {
                    dialog.cancel();
                }
            });

            ImageView ivBack = dialog.findViewById(R.id.ivBack);
            ivBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.cancel();
                }
            });


            wlp.gravity = Gravity.CENTER;
            wlp.flags &= ~WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
            window.setAttributes(wlp);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            dialog.show();

            readMapItemRecords(realm, dialog, getContext());
//                populateRecyclerView(dialog, getContext());
        });


        llRedAlertBle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(getActivity(), DeviceListingScanActivity.class);
                startActivity(in);
            }
        });

        setBluetoothStatus();

        etSearchLoc.addTextChangedListener(textWatcher);

        etSearchLoc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                FabMenu.collapse();
                rvNearbyAutosuggest.setVisibility(View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    setRecentLayoutVisibility(false);
//                    recentSearchLayout.setVisibility(View.GONE);
                } else {
                    setRecentLayoutVisibility(true);
//                    recentSearchLayout.setVisibility(View.VISIBLE);
                }
            }
        });


//        etSearchLoc.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        etSearchLoc.setOnEditorActionListener((v, actionId, event) -> {
            Log.d("sksksks", "--" + event);


            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                in.hideSoftInputFromWindow(etSearchLoc.getWindowToken(), 0);
            }
            if (textWatcher != null) {

                mSuggestionListView.setVisibility(View.VISIBLE);
                textWatcher.hitTextSearchApiNew(etSearchLoc.getText().toString().trim(), new TextSearchListener() {
                    @Override
                    public void showProgress() {
                        MapMainFragment.this.showProgress();
                    }

                    @Override
                    public void hideProgress() {
                        MapMainFragment.this.hideProgress();
                    }
                });
            }

            return true;
        });


        mSuggestionListView.setHasFixedSize(true);

        mSuggestionListView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), (view1, position) -> {
            hideKey();
            onSuggestionListItemClicked(view1);
        }));


//        rvNearbyAutosuggest.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), (view1, position) -> {
//            hideKey();
//            onSuggestionListItemClicked(view1, true);
//        }));

        etSearchLoc.addTextChangedListener(textWatcher);


//        SpeedDialView speedDialView = view.findViewById(R.id.speedDial);
//        speedDialView.addActionItem(
//                new SpeedDialActionItem.Builder(R.id.curr, R.drawable.ic_link_white_24dp)
//                        .create()
//        );
        rlCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etSearchLoc.getText().clear();
                FabMenu.collapse();

                if (etSearchLoc != null) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null)
                        imm.hideSoftInputFromWindow(etSearchLoc.getWindowToken(), 0);
                }
                if (etNearbySearchLoc != null) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null)
                        imm.hideSoftInputFromWindow(etNearbySearchLoc.getWindowToken(), 0);
                }
                rvNearbyAutosuggest.setVisibility(View.GONE);
                etSearchLoc.setVisibility(View.VISIBLE);
                etNearbySearchLoc.setVisibility(View.GONE);

                if (getActivity() != null)
                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        requestLocationPermission();
                        return;
                    }

                if (mapIsReady) {

                    if (!common.checkGPSIsOpen(getContext())) {


                        displayLocationSettingsRequest(getContext());
                    } else {
                        mapboxMap.getStyle(new Style.OnStyleLoaded() {
                            @Override
                            public void onStyleLoaded(@NonNull Style style) {
                                enableLocationComponent(style);
                            }
                        });
                    }
                }

            }
        });


        mapView.getMapAsync(this);

        BikeBleName.observe(requireActivity(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (s != null && s.isEmpty()) {
                    llRedAlertBle.setVisibility(View.VISIBLE);
                }

                else{
                    llRedAlertBle.setVisibility(View.GONE);
                }
            }
        });
        return view;
    }

    private void deleteAddress(String str) {
        String title = getString(R.string.delete_home_address);
        if (str.equalsIgnoreCase("Work"))
            title = getString(R.string.delete_work_address);

        if (getContext() != null) {
            Dialog dialog = new Dialog(getContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.custom_alert_dialog);
            if (dialog.getWindow() != null)
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            TextView titleTv = dialog.findViewById(R.id.titleTv);
            TextView messageTv = dialog.findViewById(R.id.messageTv);
            Button yesBtn = dialog.findViewById(R.id.yesButton);
            Button noBtn = dialog.findViewById(R.id.noButton);

            titleTv.setText(title);
            messageTv.setText(getString(R.string.want_to_proceed));
            yesBtn.setOnClickListener(v -> {
                if (str.equalsIgnoreCase("Home")) {
                    realm.executeTransaction(realm -> {
                        MapWorkAndHomeRealmModule homeRealmModule = realm.where(MapWorkAndHomeRealmModule.class)
                                .equalTo("id", "Home").findFirst();
                        if (homeRealmModule != null) {
                            homeRealmModule.deleteFromRealm();
                            homeDeleteBtn.setVisibility(View.INVISIBLE);
                            homeAddressTv.setText("Set Address");
                        }
                    });
                } else if (str.equalsIgnoreCase("Work")) {
                    realm.executeTransaction(realm1 -> {
                        MapWorkAndHomeRealmModule workRealmModule = realm.where(MapWorkAndHomeRealmModule.class)
                                .equalTo("id", "Work").findFirst();

                        if (workRealmModule != null) {
                            workRealmModule.deleteFromRealm();
                            workDeleteBtn.setVisibility(View.INVISIBLE);
                            workAddressTv.setText("Set Address");
                        }
                    });
                }
                dialog.cancel();
            });

            noBtn.setOnClickListener(v -> {
                dialog.cancel();
            });

            dialog.show();

        }

       /* new AlertDialog.Builder(getContext())
                .setCancelable(true)
                .setTitle(title)
                .setMessage(getString(R.string.want_to_proceed))
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (str.equalsIgnoreCase("Home")) {
                        realm.executeTransaction(realm -> {
                            MapWorkAndHomeRealmModule homeRealmModule = realm.where(MapWorkAndHomeRealmModule.class)
                                    .equalTo("id", "Home").findFirst();
                            if (homeRealmModule != null) {
                                homeRealmModule.deleteFromRealm();
                                homeDeleteBtn.setVisibility(View.INVISIBLE);
                                homeAddressTv.setText("Set Address");
                            }
                        });
                    } else if (str.equalsIgnoreCase("Work")) {
                        realm.executeTransaction(realm1 -> {
                            MapWorkAndHomeRealmModule workRealmModule = realm.where(MapWorkAndHomeRealmModule.class)
                                    .equalTo("id", "Work").findFirst();

                            if (workRealmModule != null) {
                                workRealmModule.deleteFromRealm();
                                workDeleteBtn.setVisibility(View.INVISIBLE);
                                workAddressTv.setText("Set Address");
                            }
                        });
                    }
                    dialog.cancel();
                })
                .setNegativeButton("No", ((dialog, which) -> dialog.cancel()))
                .create()
                .show();*/
    }

    private void editHomeAddress(String msg) {

        if (getContext() != null) {
            Dialog dialog = new Dialog(getContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.custom_alert_dialog);
            if (dialog.getWindow() != null)
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            TextView titleTv = dialog.findViewById(R.id.titleTv);
            TextView messageTv = dialog.findViewById(R.id.messageTv);
            Button yesBtn = dialog.findViewById(R.id.yesButton);
            Button noBtn = dialog.findViewById(R.id.noButton);

            titleTv.setText(getString(R.string.edit_home_address));
            messageTv.setText(msg);
            yesBtn.setOnClickListener(v -> {
                dialog.cancel();
                setRecentLayoutVisibility(false);
                addressInfoTv.setVisibility(View.VISIBLE);
                addressSetting = true;
                homeAddressSetting = true;
                etSearchLoc.setHint(setHomeAddressHint);
                etNearbySearchLoc.setHint(setHomeAddressHint);
            });

            noBtn.setOnClickListener(v -> {
                addressSetting = false;
                homeAddressSetting = false;
                addressInfoTv.setVisibility(View.GONE);
                etSearchLoc.setHint(navigationHint);
                etNearbySearchLoc.setHint(navigationHint);
                dialog.cancel();
            });

            dialog.show();

        }

       /* AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setCancelable(true);
        builder.setTitle(getString(R.string.edit_home_address));
        builder.setMessage(msg);
        builder.setPositiveButton("Yes", (dialog, which) -> {
            dialog.cancel();
            setRecentLayoutVisibility(false);
            addressInfoTv.setVisibility(View.VISIBLE);
            addressSetting = true;
            homeAddressSetting = true;
            etSearchLoc.setHint(setHomeAddressHint);
            etNearbySearchLoc.setHint(setHomeAddressHint);
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            addressSetting = false;
            homeAddressSetting = false;
            addressInfoTv.setVisibility(View.GONE);
            etSearchLoc.setHint(navigationHint);
            etNearbySearchLoc.setHint(navigationHint);
            dialog.cancel();
        });
        builder.create();
        builder.show();*/
    }


    private void editWorkAddress(String msg) {

        if (getContext() != null) {
            Dialog dialog = new Dialog(getContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.custom_alert_dialog);
            if (dialog.getWindow() != null)
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            TextView titleTv = dialog.findViewById(R.id.titleTv);
            TextView messageTv = dialog.findViewById(R.id.messageTv);
            Button yesBtn = dialog.findViewById(R.id.yesButton);
            Button noBtn = dialog.findViewById(R.id.noButton);

            titleTv.setText(getString(R.string.edit_work_address));
            messageTv.setText(msg);
            yesBtn.setOnClickListener(v -> {
                dialog.cancel();
                setRecentLayoutVisibility(false);
                addressInfoTv.setVisibility(View.VISIBLE);
                addressSetting = true;
                workAddressSetting = true;
                etSearchLoc.setHint(setWorkAddressHint);
                etNearbySearchLoc.setHint(setWorkAddressHint);
            });

            noBtn.setOnClickListener(v -> {
                addressSetting = false;
                workAddressSetting = false;
                addressInfoTv.setVisibility(View.GONE);
                etSearchLoc.setHint(navigationHint);
                etNearbySearchLoc.setHint(navigationHint);
                dialog.cancel();
            });

            dialog.show();

        }

        /*AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setCancelable(true);
        builder.setTitle(getString(R.string.edit_work_address));
        builder.setMessage(msg);
        builder.setPositiveButton("Yes", (dialog, which) -> {
            dialog.cancel();
            setRecentLayoutVisibility(false);
            addressInfoTv.setVisibility(View.VISIBLE);
            addressSetting = true;
            workAddressSetting = true;
            etSearchLoc.setHint(setWorkAddressHint);
            etNearbySearchLoc.setHint(setWorkAddressHint);
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            addressSetting = false;
            workAddressSetting = false;
            addressInfoTv.setVisibility(View.GONE);
            etSearchLoc.setHint(navigationHint);
            etNearbySearchLoc.setHint(navigationHint);
            dialog.cancel();
        });
        builder.create();
        builder.show();*/
    }

    private void setHomeAddress(String placeName, String alternateName, String placeAddress, Double latitude, Double longitude) {


        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(realm1 -> {

                MapWorkAndHomeRealmModule homeRealmModule = realm1.where(MapWorkAndHomeRealmModule.class)
                        .equalTo("id", "Home").findFirst();

                if (homeRealmModule != null) {
                    homeRealmModule.setPlaceName(placeName);
                    homeRealmModule.setAlternateName(alternateName);
                    homeRealmModule.setAddress(placeAddress);
                    homeRealmModule.setLat(String.valueOf(latitude));
                    homeRealmModule.setLng(String.valueOf(longitude));
                    homeRealmModule.setNearby(false);
                    realm1.insertOrUpdate(homeRealmModule);

                    etSearchLoc.setHint(navigationHint);
                    etNearbySearchLoc.setHint(navigationHint);
                    //Toast.makeText(app, "Address updated successfully", Toast.LENGTH_SHORT).show();
                    common.showToast("Address updated successfully", TOAST_DURATION);
                } else {

                    MapWorkAndHomeRealmModule module = realm1.createObject(MapWorkAndHomeRealmModule.class, "Home");
                    module.setPlaceName(placeName);
                    module.setAlternateName(alternateName);
                    module.setAddress(placeAddress);
                    module.setLat(String.valueOf(latitude));
                    module.setLng(String.valueOf(longitude));
                    module.setNearby(false);
                    realm1.insert(module);
                    etSearchLoc.setHint(navigationHint);
                    etNearbySearchLoc.setHint(navigationHint);
                    common.showToast("Address saved successfully", TOAST_DURATION);
                }
            });
        } catch (Exception e) {
            Timber.e(e.toString(), "setHomeAddress: ");
        }

        addressSetting = false;
        homeAddressSetting = false;
        addressInfoTv.setVisibility(View.GONE);
        setRecentLayoutVisibility(true);
        getHomeAndWorkAddress(realm);
        etSearchLoc.getText().clear();
    }

    private void setWorkAddress(String placeName, String alternateName, String placeAddress, Double latitude, Double longitude) {

        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(new Realm.Transaction() {

                @Override
                public void execute(@NotNull Realm realm) {

                    MapWorkAndHomeRealmModule workRealmModule = realm.where(MapWorkAndHomeRealmModule.class)
                            .equalTo("id", "Work").findFirst();

                    if (workRealmModule != null) {
                        workRealmModule.setPlaceName(placeName);
                        workRealmModule.setAlternateName(alternateName);
                        workRealmModule.setAddress(placeAddress);
                        workRealmModule.setLat(String.valueOf(latitude));
                        workRealmModule.setLng(String.valueOf(longitude));
                        workRealmModule.setNearby(false);
                        realm.insertOrUpdate(workRealmModule);
                        etSearchLoc.setHint(navigationHint);
                        etNearbySearchLoc.setHint(navigationHint);
                        common.showToast("Address updated successfully", TOAST_DURATION);
                    } else {

                        MapWorkAndHomeRealmModule module = realm.createObject(MapWorkAndHomeRealmModule.class, "Work");
                        module.setPlaceName(placeName);
                        module.setAlternateName(alternateName);
                        module.setAddress(placeAddress);
                        module.setLat(String.valueOf(latitude));
                        module.setLng(String.valueOf(longitude));
                        module.setNearby(false);
                        realm.insert(module);
                        etSearchLoc.setHint(navigationHint);
                        etNearbySearchLoc.setHint(navigationHint);
                        common.showToast("Address saved successfully", TOAST_DURATION);
                    }
                }
            });
        } catch (Exception e) {
            Timber.e(e.toString(), "setWorkAddress: ");
        }

        addressSetting = false;
        workAddressSetting = false;
        addressInfoTv.setVisibility(View.GONE);
        setRecentLayoutVisibility(true);
        getHomeAndWorkAddress(realm);
        etSearchLoc.getText().clear();
    }

    private void getHomeAndWorkAddress(Realm realm) {
        try {
            realm.executeTransaction(realm1 -> {
                MapWorkAndHomeRealmModule homeResults = realm1.where(MapWorkAndHomeRealmModule.class)
                        .equalTo("id", "Home").findFirst();

                MapWorkAndHomeRealmModule workResults = realm1.where(MapWorkAndHomeRealmModule.class)
                        .equalTo("id", "Work").findFirst();


                // set home and work address in ui part
                if (homeResults != null) {
                    String homeAddress = homeResults.getPlaceName();
                    if (homeAddress.length() > 0) {
                        homeAddressTv.setText(homeAddress);
                        homeDeleteBtn.setVisibility(View.VISIBLE);
                    }
                }

                if (workResults != null) {
                    String workAddress = workResults.getPlaceName();
                    if (workAddress.length() > 0) {
                        workAddressTv.setText(workAddress);
                        workDeleteBtn.setVisibility(View.VISIBLE);
                    }
                }

            });
        } catch (Exception ignored) {
        }
    }


    public static MapView getMapView() {


        return mapView;
    }

    public void showInfoOnLongClick(ELocation eLocation) {
        this.eLocation = eLocation;
//        textViewPutRouteName.setText(eLocation.placeName);
//        showHideBottomSheet();
    }


    public void setBluetoothStatus() {


        if (BLUETOOTH_STATE) {
            if (staticConnectionStatus) {
                llRedAlertBle.setVisibility(View.GONE);
            } else {
                llRedAlertBle.setVisibility(View.VISIBLE);
            }
        } else {
            llRedAlertBle.setVisibility(View.VISIBLE);
        }
        IntentFilter intentFilter = new IntentFilter(
                "status");

        mReceiver = new BleConnection() {

            @Override
            public void onReceive(Context context, Intent intent) {
                //extract our message from intent

                Boolean status = intent.getExtras().getBoolean("status");
                if (BLUETOOTH_STATE) {
                    if (status) {
                        llRedAlertBle.setVisibility(View.GONE);

                    } else {
                        llRedAlertBle.setVisibility(View.VISIBLE);
                    }
                } else {
                    llRedAlertBle.setVisibility(View.VISIBLE);
                }
            }


        };

        getActivity().registerReceiver(mReceiver, intentFilter);
    }

    private void onSuggestionListItemClicked(View view) {
        mSuggestionListView.setVisibility(View.GONE);
        if (getActivity() == null)
            return;
        try {
            if (view.getTag() instanceof AutoSuggestAdapter.WrapperAutoSuggestResult) {
                AutoSuggestAdapter.WrapperAutoSuggestResult wrapper = (AutoSuggestAdapter.WrapperAutoSuggestResult) view.getTag();

                if (wrapper == null) {
                    return;
                }
                ELocation eLocation = wrapper.getElocation();
                if (eLocation != null) {
                    if (!TextUtils.isEmpty(eLocation.placeName)) {
                        //    searchEditText.setText(eLocation.placeName);
//                        textViewPutRouteName.setText(eLocation.placeName);
//                        showHideBottomSheet();
                    }
                    double longitude = 0, latitude = 0, eLatitude = 0, eLongitude = 0;
                    try {
                        latitude = eLocation.latitude != null ? eLocation.latitude : 0;
                        longitude = eLocation.longitude != null ? eLocation.longitude : 0;
                        eLatitude = eLocation.entryLatitude;
                        eLongitude = eLocation.entryLongitude;
                    } catch (NumberFormatException e) {
                        Timber.e(e);
                    }
                    if (latitude > 0 && longitude > 0) {
                        eLocation.latitude = Double.valueOf(latitude + "");
                        eLocation.longitude = Double.valueOf(longitude + "");
                        eLocation.entryLatitude = eLatitude;
                        eLocation.entryLongitude = eLongitude;
                    }
//                    if (getActivity() != null && getActivity().getApplication() != null)
//                        ((SuzukiApplication) (getActivity().getApplication())).setELocation(eLocation);
                    this.eLocation = eLocation;
                    Log.d("elooc", "-- top " + this.eLocation.placeName + " middle " + this.eLocation.alternateName + " end " + this.eLocation.placeAddress);


                    if (addressSetting) { // user wants to set address
                        if (workAddressSetting) {
                            showAddressConfirmationDialog(this.eLocation.placeName, this.eLocation.alternateName,
                                    this.eLocation.placeAddress, this.eLocation.latitude, this.eLocation.longitude, true);
//                            setWorkAddress(this.eLocation.placeName, this.eLocation.alternateName,
//                                    this.eLocation.placeAddress, this.eLocation.latitude, this.eLocation.longitude);
                        } else {
                            showAddressConfirmationDialog(this.eLocation.placeName, this.eLocation.alternateName,
                                    this.eLocation.placeAddress, this.eLocation.latitude, this.eLocation.longitude, false);
//                            setHomeAddress(this.eLocation.placeName, this.eLocation.alternateName,
//                                    this.eLocation.placeAddress, this.eLocation.latitude, this.eLocation.longitude);
                        }
                    } else { // user wants to navigation
                        storeRecentTripToRealm(this.eLocation.placeName, this.eLocation.alternateName,
                                this.eLocation.placeAddress, this.eLocation.latitude, this.eLocation.longitude, false);

                        getDirections(this.eLocation.latitude, this.eLocation.longitude, false);
                    }
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    private void storeRecentTripToRealm(String placeName, String alternateName, String address, Double lat, Double lng, boolean nearby) {
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(new Realm.Transaction() {

                @Override
                public void execute(@NotNull Realm realm) {

//                    RealmResults<MapRecentRealmModule> results = realm.where(MapRecentRealmModule.class).findAll();
                    RealmResults<MapRecentRealmModule> results = realm.where(MapRecentRealmModule.class)
                            .sort("id", Sort.ASCENDING).findAll();

                    Log.d("tag", "size--- " + results.size());

                    for (MapRecentRealmModule module : results) {
                        Log.d("tag", "place ---" + module.getPlaceName());
                        if (module.getLat().equalsIgnoreCase(String.valueOf(lat)) && module.getLng().equalsIgnoreCase(String.valueOf(lng))) {
                            Log.d("tag", "place matched ---" + module.getPlaceName());
                            int id = module.getId();
                            RealmResults<MapRecentRealmModule> matchingResult =
                                    realm.where(MapRecentRealmModule.class).equalTo("id", id).findAll();
                            matchingResult.deleteAllFromRealm();
                        }
                    }
                    results = realm.where(MapRecentRealmModule.class)
                            .sort("id", Sort.ASCENDING).findAll();

                    Log.d("tag", "size ---2 " + results.size());

                    for (MapRecentRealmModule module : results) {
                        Log.d("tag", "place ---2 " + module.getPlaceName());
                    }

                    Log.d("tag", "---1 " + results.size());

                    MapRecentRealmModule mapRecentRealmModule = realm.createObject(MapRecentRealmModule.class);
                    mapRecentRealmModule.setPlaceName(placeName);
                    mapRecentRealmModule.setAlternateName(alternateName);
                    mapRecentRealmModule.setAddress(address);
                    mapRecentRealmModule.setLat(String.valueOf(lat));
                    mapRecentRealmModule.setLng(String.valueOf(lng));
                    mapRecentRealmModule.setNearby(nearby);
                    mapRecentRealmModule.setId((int) System.currentTimeMillis() / 1000);
                    realm.insert(mapRecentRealmModule);


//                    for (MapRecentRealmModule module : results) {
//                        Log.d("tag", "---2 " + module.getPlaceName());
//                    }

                    if (results.size() > 5) {
                        results.deleteFirstFromRealm();
                    }

//                    for (MapRecentRealmModule module : results) {
//                        Log.d("tag", "---4 " + module.getPlaceName());
//                    }

//                    results.deleteAllFromRealm();

                }
            });

        } catch (Exception e) {
            Log.d("realmex", "--" + e.getMessage());

        }
    }


    private void getRecentSearches(Realm realm) {

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {


                orderedRecentSearchResult = realm.where(MapRecentRealmModule.class)
                        .sort("id", Sort.DESCENDING).findAll();

                RealmResults<MapRecentRealmModule> results =
                        realm.where(MapRecentRealmModule.class).findAll();


                recentSearchAdapter = new MapRecentSearchAdapter(getContext(), orderedRecentSearchResult);
                recentSearchLV.setAdapter(recentSearchAdapter);

            }
        });
    }

    private void setRecentLayoutVisibility(boolean setVisible) {
        if (setVisible) {
            addressInfoTv.setVisibility(View.GONE);
            recentHomelayout.setVisibility(View.VISIBLE);
            recentSearchGap.setVisibility(View.VISIBLE);
            recentSearchLV.setVisibility(View.VISIBLE);
        } else {
            recentHomelayout.setVisibility(View.GONE);
            recentSearchGap.setVisibility(View.GONE);
            recentSearchLV.setVisibility(View.GONE);
        }
    }

    private void getNearBy(double latitude, double longitude, String data) {
       mapboxMap.clear();

        MapplsNearby mapplsNearby = MapplsNearby.builder()
                .setLocation(latitude, longitude)
                .radius(50000)
                .keyword(data)
                .build();
        MapplsNearbyManager.newInstance(mapplsNearby).call(new OnResponseCallback<NearbyAtlasResponse>() {
            @Override
            public void onSuccess(NearbyAtlasResponse response) {
                ArrayList<NearbyAtlasResult> nearByList = response.getSuggestedLocations();
                if (nearByList.size() > 0) {
//                                    addMarker(nearByList);
                    mSuggestionListView.setVisibility(View.GONE);
                    rvNearbyAutosuggest.setVisibility(View.VISIBLE);
                    rvNearbyAutosuggest.setAdapter(new NearByAdapter(nearByList, getContext(), MapMainFragment.this));
                }
                //Handle Response
//                if (response.equals(200)) {
//
//
//                    if (response != null) {
//
//                    } else {
//                        common.showToast("Not able to get data,Try later", TOAST_DURATION);
//                    }
//
//                }
                apiProgressBar.setVisibility(View.GONE);

            }

            @Override
            public void onError(int code, String message) {
                //Handle Error
                apiProgressBar.setVisibility(View.GONE);
            }
        });

//        MapmyIndiaNearby.builder()
//                .setLocation(latitude, longitude)
//                .radius(50000)
//                .keyword(data)
//                .build()
//                .enqueueCall(new Callback<NearbyAtlasResponse>() {
//                    @Override
//                    public void onResponse(Call<NearbyAtlasResponse> call, Response<NearbyAtlasResponse> response) {
//
//                        if (response.code() == 200) {
//                            if (response.body() != null) {
//                                ArrayList<NearbyAtlasResult> nearByList = response.body().getSuggestedLocations();
//                                if (nearByList.size() > 0) {
////                                    addMarker(nearByList);
//                                    mSuggestionListView.setVisibility(View.GONE);
//                                    rvNearbyAutosuggest.setVisibility(View.VISIBLE);
//                                    rvNearbyAutosuggest.setAdapter(new NearByAdapter(nearByList, getContext(), MapMainFragment.this));
//                                }
//                            } else {
//                                common.showToast("Not able to get data,Try later", TOAST_DURATION);
//                            }
//                        }
//
//                        apiProgressBar.setVisibility(View.GONE);
////                        progressDialogHide();
//                    }
//
//                    @Override
//                    public void onFailure(Call<NearbyAtlasResponse> call, Throwable t) {
////                        progressDialogHide();
//                        apiProgressBar.setVisibility(View.GONE);
//                    }
//                });
    }

    public void getReverseGeoCode(Double latitude, Double longitude) {
//        fromLocation = "Axiom";
        if (latitude <= 0 || longitude <= 0) {
            common.showToast("Invalid Location", TOAST_DURATION);
            return;
        }

        MapplsReverseGeoCode mapplsReverseGeoCode = MapplsReverseGeoCode.builder()
                .setLocation(latitude, longitude)
                .build();
        MapplsReverseGeoCodeManager.newInstance(mapplsReverseGeoCode).call(new OnResponseCallback<PlaceResponse>() {
            @Override
            public void onSuccess(PlaceResponse response) {

                //Handle Response
                if (response.getResponseCode() == 200) {
                    List<Place> placesList = response.getPlaces();
                    Place place = placesList.get(0);
                    fromLocation = place.getFormattedAddress();
                }
            }

            @Override
            public void onError(int code, String message) {
                //Handle Error
                common.showToast("getReverseGeoCode:- " + message.toString(), TOAST_DURATION);

            }
        });

//        MapmyIndiaReverseGeoCode.builder()
//                .setLocation(latitude, longitude)
//                .build().enqueueCall(new Callback<PlaceResponse>() {
//            @Override
//            public void onResponse(@NonNull Call<PlaceResponse> call, @NonNull Response<PlaceResponse> response) {
//                if (response.code() == 200) {
//                    if (response.body() != null) {
//                        List<Place> placesList = response.body().getPlaces();
//                        Place place = placesList.get(0);
//                        fromLocation = place.getFormattedAddress();
//                    }
//                }
//
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<PlaceResponse> call, @NonNull Throwable t) {
//                common.showToast("getReverseGeoCode:- " + t.toString(), TOAST_DURATION);
//            }
//        });
    }


    private void getDirections(Double lat, Double lng, boolean nearby) {

        etNearbySearchLoc.getText().clear();
        etSearchLoc.getText().clear();


        if (getActivity() == null)
            return;
        try {

            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                common.showToast("Location permission is not given", TOAST_DURATION);
                return;
            }
            Location location = getMapboxMap().getLocationComponent().getLastKnownLocation();

            if (location != null) {

                //   ((HomeScreenActivity) getActivity()).navigateTo(RouteFragment.newInstance(eLocation, fromLocation), true);

//                this.eLocation = eLocation;
//                loadFragment(RouteFragment.newInstance(eLocation, fromLocation));

                if (eLocation.placeName != null && eLocation.placeAddress != null && fromLocation != null) {
                    if (nearby) {

//                        Intent in = new Intent(getActivity(), RouteNearByActivity.class);
                        Intent in = new Intent(getActivity(), RouteActivity.class);
                        etSearchLoc.addTextChangedListener(textWatcher);
                        in.putExtra("fromLocation", fromLocation);
                        in.putExtra("placeName", eLocation.placeName);
                        in.putExtra("lat", lat);
                        in.putExtra("long", lng);
                        in.putExtra("placeAddress", eLocation.placeAddress);
                        setRecentLayoutVisibility(false);
                        startActivity(in);
                    } else {
//                        Intent in = new Intent(getActivity(), RouteNearByActivity.class);
//                        etSearchLoc.addTextChangedListener(textWatcher);
//                        in.putExtra("fromLocation", fromLocation);
//                        in.putExtra("placeName", eLocation.placeName);
//                        in.putExtra("lat", Double.parseDouble(lat));
//                        in.putExtra("long", Double.parseDouble(lng));
//                        in.putExtra("placeAddress", eLocation.placeAddress);

                        Intent in = new Intent(getActivity(), RouteActivity.class);
                        etSearchLoc.addTextChangedListener(textWatcher);
                        in.putExtra("fromLocation", fromLocation);
                        in.putExtra("placeName", eLocation.placeName);
                        in.putExtra("lat", lat);
                        in.putExtra("long", lng);
                        in.putExtra("placeAddress", eLocation.placeAddress);
                        setRecentLayoutVisibility(false);
                        startActivity(in);

                    }

                } else {
                    common.showToast("Could not fetch data. Please try later !", TOAST_DURATION);
                }
            } else {
                common.showToast(getResources().getString(R.string.current_location_not_available), TOAST_DURATION);
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    private void getDirectionsByRecent(String lat, String lng, String placeName, String address, boolean nearby) {

        if (getActivity() == null)
            return;
        try {

            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                common.showToast("Location permission is not given", TOAST_DURATION);
                return;
            }
            Location location = getMapboxMap().getLocationComponent().getLastKnownLocation();

            if (location != null) {
//                ((HomeScreenActivity) getActivity()).navigateTo(RouteFragment.newInstance(eLocation, fromLocation), true);
           //     Toast.makeText(getActivity(), "" + fromLocation, Toast.LENGTH_SHORT).show();

//                this.eLocation = eLocation;
//                Log.d("loccccdd", "--" + eLocation.placeName + "--" + eLocation.placeAddress);
//                Log.d("locccc", "ssss--" + fromLocation + eLocation.placeAddress + eLocation.placeName);
//                loadFragment(RouteFragment.newInstance(eLocation, fromLocation));

                if (placeName != null && address != null && fromLocation != null) {

                    if (nearby) {
                   //     Toast.makeText(getActivity(), "7", Toast.LENGTH_SHORT).show();

//                        Intent in = new Intent(getActivity(), RouteNearByActivity.class);
                        Intent in = new Intent(getActivity(), RouteActivity.class);

                        ELocation eLocation = new ELocation();
                        eLocation.latitude = Double.valueOf(lat + "");
                        eLocation.longitude = Double.valueOf(lng + "");
                        eLocation.entryLatitude = Double.parseDouble(lat);
                        eLocation.entryLongitude = Double.parseDouble(lng);
                        this.eLocation = eLocation;


                        etSearchLoc.addTextChangedListener(textWatcher);
                        in.putExtra("fromLocation", fromLocation);
                        in.putExtra("placeName", placeName);
                        in.putExtra("lat", (lat));
                        in.putExtra("long", (lng));
                        in.putExtra("placeAddress", address);
                        setRecentLayoutVisibility(false);
                        startActivity(in);
                    } else {
//                        Intent in = new Intent(getActivity(), RouteNearByActivity.class);
//                        etSearchLoc.addTextChangedListener(textWatcher);
//                        in.putExtra("fromLocation", fromLocation);
//                        in.putExtra("placeName", eLocation.placeName);
//                        in.putExtra("lat", Double.parseDouble(lat));
//                        in.putExtra("long", Double.parseDouble(lng));
//                        in.putExtra("placeAddress", eLocation.placeAddress);
                        ELocation eLocation = new ELocation();
                        eLocation.latitude = Double.valueOf(lat + "");
                        eLocation.longitude = Double.valueOf(lng + "");
                        eLocation.entryLatitude = Double.parseDouble(lat);
                        eLocation.entryLongitude = Double.parseDouble(lng);
                        this.eLocation = eLocation;

                        Intent in = new Intent(getActivity(), RouteActivity.class);
                        etSearchLoc.addTextChangedListener(textWatcher);
                        in.putExtra("fromLocation", fromLocation);
                        in.putExtra("placeName", placeName);
                        in.putExtra("lat", lat);
                        in.putExtra("long", lng);
                        in.putExtra("placeAddress", address);
                        setRecentLayoutVisibility(false);
                        startActivity(in);

                    }

                } else {
                    common.showToast("Could not fetch data. Please try later !", TOAST_DURATION);
                }
            } else {
                common.showToast(getResources().getString(R.string.current_location_not_available), TOAST_DURATION);
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }


//        private void getDirectionsByRecent(String lat, String lng, String placeName, String address, boolean nearby) {
//
//
//            if (getActivity() == null)
//                return;
//            try {
//
//                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//                        && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                    common.showToast("Location permission is not given", TOAST_DURATION);
//                    return;
//                }
//                Location location = getMapboxMap().getLocationComponent().getLastKnownLocation();
//
//                if (location != null) {
////                ((HomeScreenActivity) getActivity()).navigateTo(RouteFragment.newInstance(eLocation, fromLocation), true);
//
////                this.eLocation = eLocation;
////                Log.d("loccccdd", "--" + eLocation.placeName + "--" + eLocation.placeAddress);
////                Log.d("locccc", "ssss--" + fromLocation + eLocation.placeAddress + eLocation.placeName);
////                loadFragment(RouteFragment.newInstance(eLocation, fromLocation));
//
//                    if (placeName != null && address != null && fromLocation != null) {
//                        if (nearby) {
////                        Intent in = new Intent(getActivity(), RouteNearByActivity.class);
//                            Intent in = new Intent(getActivity(), RouteActivity.class);
//
//                            ELocation eLocation = new ELocation();
//                            eLocation.latitude = Double.valueOf(lat + "");
//                            eLocation.longitude = Double.valueOf(lng + "");
//                            eLocation.entryLatitude = Double.parseDouble(lat);
//                            eLocation.entryLongitude = Double.parseDouble(lng);
//                            this.eLocation = eLocation;
//
//
//                            etSearchLoc.addTextChangedListener(textWatcher);
//                            in.putExtra("fromLocation", fromLocation);
//                            in.putExtra("placeName", placeName);
//                            in.putExtra("lat", (lat));
//                            in.putExtra("long", (lng));
//                            in.putExtra("placeAddress", address);
//                            setRecentLayoutVisibility(false);
//                            startActivity(in);
//                        } else {
////                        Intent in = new Intent(getActivity(), RouteNearByActivity.class);
////                        etSearchLoc.addTextChangedListener(textWatcher);
////                        in.putExtra("fromLocation", fromLocation);
////                        in.putExtra("placeName", eLocation.placeName);
////                        in.putExtra("lat", Double.parseDouble(lat));
////                        in.putExtra("long", Double.parseDouble(lng));
////                        in.putExtra("placeAddress", eLocation.placeAddress);
//                            ELocation eLocation = new ELocation();
//                            eLocation.latitude = Double.valueOf(lat + "");
//                            eLocation.longitude = Double.valueOf(lng + "");
//                            eLocation.entryLatitude = Double.parseDouble(lat);
//                            eLocation.entryLongitude = Double.parseDouble(lng);
//                            this.eLocation = eLocation;
//
//                            Intent in = new Intent(getActivity(), RouteActivity.class);
//                            etSearchLoc.addTextChangedListener(textWatcher);
//                            in.putExtra("fromLocation", fromLocation);
//                            in.putExtra("placeName", placeName);
//                            in.putExtra("lat", lat);
//                            in.putExtra("long", lng);
//                            in.putExtra("placeAddress", address);
//                            setRecentLayoutVisibility(false);
//                            startActivity(in);
//
//                        }
//
//                    } else {
//                        common.showToast("Could not fetch data. Please try later !", TOAST_DURATION);
//                    }
//                } else {
//                    common.showToast(getResources().getString(R.string.current_location_not_available), TOAST_DURATION);
//                }
//            } catch (Exception e) {
//                Timber.e(e);
//            }
//        }

    public MapplsMap getMapboxMap() {
        if (mapboxMap != null)
            return mapboxMap;
        return null;
    }


    @Override
    public void adapterItemIsClicked(int clickedPositon, String actionToPerform) {

    }

    @Override
    public void adapterItemIsClicked(double lat, double lng, String placeAddress, String placeName) {

//        Intent in = new Intent(getActivity(), RouteNearByActivity.class);
        Intent in = new Intent(getActivity(), RouteActivity.class);
        FabMenu.collapse();
        etSearchLoc.setVisibility(View.VISIBLE);
        etNearbySearchLoc.setVisibility(View.GONE);
        rvNearbyAutosuggest.setVisibility(View.GONE);
        in.putExtra("fromLocation", fromLocation);
        in.putExtra("placeName", placeName);
        in.putExtra("lat", String.valueOf(lat));
        in.putExtra("long", String.valueOf(lng));
        in.putExtra("placeAddress", placeAddress);
        ELocation eLocation1 = new ELocation();
        eLocation1.latitude = Double.valueOf(lat + "");
        eLocation1.longitude = Double.valueOf(lng + "");
        eLocation1.placeName = placeName;
        eLocation1.placeAddress = placeAddress;
        eLocation = eLocation1;

        storeRecentTripToRealm(placeName, "",
                placeAddress, lat, lng, false);
        setRecentLayoutVisibility(false);

        startActivity(in);

    }

    @Override
    public void adapterItemIsClicked(int clickedPositon, String actionToPerform, boolean clicked, String date, Date dateTime, String time, String startLoc, String endLoc, String cuurent_lat, String current_long, String destiny_lat, String destiny_long, String tripName, String rideTime, String totalDistance, String topspeed, String timelt10, RealmList<ViaPointLocationRealmModel> viaPointRealmList, String startTime,String endTime, String vehicleType) {

    }


    @Override
    public void dragItem(int id, String name) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onConnected() {

    }

    @Override
    public void requestDrag(RecyclerView.ViewHolder viewHolder) {

    }


    public interface TextSearchListener {
            void showProgress();

            void hideProgress();
        }

        void hideKey() {
            if (getActivity() == null)
                return;
            if (etSearchLoc != null) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.hideSoftInputFromWindow(etSearchLoc.getWindowToken(), 0);
            }
        }


    private void showProgress() {
            try {
                if (getActivity() == null)
                    return;
//            ((HomeActivity) getActivity()).showProgress();
            }
            catch (Exception e) {
                Timber.e(e);
            }
        }

        private void hideProgress() {
            try {
                if (getActivity() == null)
                    return;
//            ((HomeActivity) getActivity()).hideProgress();
            } catch (Exception e) {
                Timber.e(e);
            }
        }


        @Override
        public void onMapReady(MapplsMap mapboxMap) {

        /*if (new CurrentLoc().nightTime()) {
            mapboxMap.setStyle(Style.NIGHT_MODE);
        }*/
            mapboxMap.getUiSettings().enableLogoClick(false);
            mapboxMap.addOnMapClickListener(new MapplsMap.OnMapClickListener() {
                @Override
                public boolean onMapClick(@NonNull LatLng latLng) {
                    return false;
                }
            });


            this.mapboxMap = mapboxMap;
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
            mapboxMap.setMinZoomPreference(3);
            mapboxMap.setMaxZoomPreference(18.5);


            //
            final Marker[] tapMarker = {null};

            mapboxMap.addOnMapLongClickListener(new MapplsMap.OnMapLongClickListener() {
                @Override
                public boolean onMapLongClick(@NonNull LatLng latLng) {
                    MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(latLng));
                    if (tapMarker[0] != null) {
                        mapboxMap.removeMarker(tapMarker[0]);
                    }
                    tapMarker[0] = mapboxMap.addMarker(markerOptions);

                    if (addressSetting) { // user wants to set address

                        // directly get the data
                        getDetailsFromReverseGeoCode(tapMarker[0]);

                    } else {
                        showConfirmationDialog(tapMarker[0]);
                    }

                    return false;
                }
            });


            //custom marker
//        MarkerOptions markerOptions =  new MarkerOptions().position(point).icon(icon);
//        Marker marker = mapboxMap.addMarker(markerOptions);
//        String tittle = "loc";
//        marker.setTitle(tittle);
//        mapboxMap.setInfoWindowAdapter(new MapboxMap.InfoWindowAdapter() {
//            @Nullable
//            @Override
//            public View getInfoWindow(@NonNull Marker marker) {
//                View view = (context).getLayoutInflater()
//                        .inflate(R.layout.layout, null);
//                TextView text = (TextView)view.findViewById(R.id.text);
//                text.setText(marker.getTitle());
//                return  view;
//            }
//        });
            if (getActivity() == null)
                return;

            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        common.showToast("Location permission is not given", TOAST_DURATION);
                        requestLocationPermission();
                        return;
                    }
                    mapboxMap.getLocationComponent().activateLocationComponent(LocationComponentActivationOptions.builder(getActivity(), style).build());
                    mapboxMap.getLocationComponent().setLocationComponentEnabled(true);
                    mapIsReady = true;
                }
            });


            if (currentlocation != null) {

                apiProgressBar.setVisibility(View.GONE);

             /*   IconFactory iconFactory = IconFactory.getInstance(getActivity());
                Icon icon = iconFactory.fromResource(R.drawable.marker);
//              Icon icon = iconFactory.defaultMarker();
                MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(currentlocation)).icon(icon);
                marker = mapboxMap.addMarker(markerOptions);
                mapboxMap.removeMarker(marker);
                markerOptions.setTitle("");
                markerOptions.setSnippet("");
                marker.setPosition(new LatLng(currentlocation));
           //     marker.setIcon(icon);
                mapboxMap.addMarker(markerOptions);*/
            }

            try {
                mapboxMap.enableTraffic(true);
//                TrafficPlugin trafficPlugin = new TrafficPlugin(mapView, mapboxMap);
//                trafficPlugin.enableFreeFlow(true);
            } catch (Exception e) {
                Timber.e(e);
            }

            directionPolylinePlugin = new DirectionPolylinePlugin(mapView, mapboxMap);
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });

            _bearingIconPlugin = new BearingIconPlugin(mapView, mapboxMap);
            mapboxMap.setMaxZoomPreference(18.5);
            mapboxMap.setMinZoomPreference(4);


            setCompassDrawable();
            mapboxMap.getUiSettings().setLogoMargins(40, dpToPx(190), 40, dpToPx(120));
            mapboxMap.getUiSettings().setCompassMargins(40, dpToPx(150), 40, dpToPx(40));


        }

        public void showExitAlert(String message) {
            Dialog dialog = new Dialog(getContext(), R.style.custom_dialog);
            dialog.setContentView(R.layout.custom_dialog);

            TextView tvAlertText = dialog.findViewById(R.id.tvAlertText);

            tvAlertText.setText(message);


            ImageView ivCross = dialog.findViewById(R.id.ivCross);
            ivCross.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.cancel();

                }
            });

            ImageView ivCheck = dialog.findViewById(R.id.ivCheck);


            ivCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.cancel();
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);

                }
            });
            dialog.show();
        }

        private void requestLocationPermission() {
            showExitAlert("Location permission is used to enable application's built-in map and navigation feature.\n" +
                    "Location data will only be accessed to display user's current location, set route and enable navigation.\n" +
                    "Please allow location access permission to use map and navigation.");
        }

        private void showConfirmationDialog(Marker marker) {
            Dialog dialog = new Dialog(requireContext(), R.style.custom_dialog);
            dialog.setContentView(R.layout.custom_dialog);

            TextView tvAlertText = dialog.findViewById(R.id.tvAlertText);
            tvAlertText.setText("Are you sure to navigate to this point ?");
            ImageView ivCross = dialog.findViewById(R.id.ivCross);
            ivCross.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    marker.remove();
                    dialog.cancel();
                }
            });

            ImageView ivCheck = dialog.findViewById(R.id.ivCheck);


            ivCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.cancel();
                    marker.remove();
                    getDetailsFromReverseGeoCode(marker);
                }
            });
            dialog.show();
        }

        private void getDetailsFromReverseGeoCode(@NotNull Marker marker) {
            double lat = marker.getPosition().getLatitude();
            double lng = marker.getPosition().getLongitude();

            progressBar.setVisibility(View.VISIBLE);

            if (lat <= 0 || lng <= 0) {
                common.showToast("Invalid Location", TOAST_DURATION);
                return;
            }

            MapplsReverseGeoCode mapplsReverseGeoCode = MapplsReverseGeoCode.builder()
                    .setLocation(lat, lng)
                    .build();
            MapplsReverseGeoCodeManager.newInstance(mapplsReverseGeoCode).call(new OnResponseCallback<PlaceResponse>() {
                @Override
                public void onSuccess(PlaceResponse response) {
                    //Handle Response

                    Timber.d(new Gson().toJson(response));
                    Timber.d("lat : " + lat + " lng : " + lng);
                    progressBar.setVisibility(View.GONE);
                    if (response == null || response.getPlaces().size() < 1) {

                        common.showToast("Issue in fetching details", TOAST_DURATION);
                        return;
                    }

                    String address = response.getPlaces().get(0).getFormattedAddress();
                    String place = response.getPlaces().get(0).getLocality();
                    String poi = response.getPlaces().get(0).getPoi();
                    if (place.length() < 2) {
                        place = poi;
                    }

                    ELocation eLocation1 = new ELocation();

                    eLocation1.latitude = Double.valueOf(lat + "");
                    eLocation1.longitude = Double.valueOf(lng + "");
                    eLocation1.placeAddress = address;
                    eLocation1.placeName = place;
//                            eLocation.entryLatitude = eLatitude;
//                            eLocation.entryLongitude = eLongitude;
                    eLocation = eLocation1;


                    marker.remove();
                    if (addressSetting) {
                        if (workAddressSetting) {
                            showAddressConfirmationDialog(place, poi,
                                    address, eLocation.latitude, eLocation.longitude, true);
//                        setWorkAddress(place, poi,
//                                address, eLocation.latitude, eLocation.longitude);
                        } else {
                            showAddressConfirmationDialog(place, poi,
                                    address, eLocation.latitude, eLocation.longitude, false);
//                        setHomeAddress(place, poi,
//                                address, eLocation.latitude, eLocation.longitude);
                        }
                    } else {
                        getDirections(eLocation.latitude, eLocation.longitude, false);
                    }

                }

                @Override
                public void onError(int code, String message) {
                    //Handle Error
                    progressBar.setVisibility(View.GONE);
                }
            });


//        MapmyIndiaReverseGeoCode.builder()
//                .setLocation(lat, lng)
//                .build().enqueueCall(new Callback<PlaceResponse>() {
//            @Override
//            public void onResponse(@NotNull Call<PlaceResponse> call, @NotNull Response<PlaceResponse> response) {
//                Timber.d(new Gson().toJson(response.body()));
//                Timber.d("lat : " + lat + " lng : " + lng);
//                progressBar.setVisibility(View.GONE);
//                if (response.body() == null || response.body().getPlaces().size() < 1) {
//                    common.showToast("Issue in fetching details", TOAST_DURATION);
//                    return;
//                }
//
//                String address = response.body().getPlaces().get(0).getFormattedAddress();
//                String place = response.body().getPlaces().get(0).getLocality();
//                String poi = response.body().getPlaces().get(0).getPoi();
//                if (place.length() < 2) {
//                    place = poi;
//                }
//
//                ELocation eLocation1 = new ELocation();
//
//                eLocation1.latitude = lat + "";
//                eLocation1.longitude = lng + "";
//                eLocation1.placeAddress = address;
//                eLocation1.placeName = place;
////                            eLocation.entryLatitude = eLatitude;
////                            eLocation.entryLongitude = eLongitude;
//                eLocation = eLocation1;
//
//
//                marker.remove();
//                if (addressSetting) {
//                    if (workAddressSetting) {
//                        showAddressConfirmationDialog(place, poi,
//                                address, eLocation.latitude, eLocation.longitude, true);
////                        setWorkAddress(place, poi,
////                                address, eLocation.latitude, eLocation.longitude);
//                    } else {
//                        showAddressConfirmationDialog(place, poi,
//                                address, eLocation.latitude, eLocation.longitude, false);
////                        setHomeAddress(place, poi,
////                                address, eLocation.latitude, eLocation.longitude);
//                    }
//                } else {
//                    getDirections(eLocation.latitude, eLocation.longitude, false);
//                }
//
//            }
//
//            @Override
//            public void onFailure(@NotNull Call<PlaceResponse> call, @NotNull Throwable t) {
//                t.printStackTrace();
//                progressBar.setVisibility(View.GONE);
//            }
//        });

        }

        private void showAddressConfirmationDialog(String place, String poi, String address,
                                                   Double latitude, Double longitude, boolean workAddress) {
            String msg = getString(R.string.confirm_home_address);
            if (workAddress)
                msg = getString(R.string.confirm_work_address);
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setCancelable(false);
            builder.setTitle(msg);
            builder.setMessage(address + ".\n\n\n" + getString(R.string.want_to_proceed));
            builder.setPositiveButton("Yes", (dialog, which) -> {
                dialog.dismiss();
                if (workAddress) {
                    setWorkAddress(place, poi, address, latitude, longitude);
                } else {
                    setHomeAddress(place, poi, address, latitude, longitude);
                }
            });

            builder.setNegativeButton("No", (dialog, which) -> {
                common.showToast("Please select different address", TOAST_DURATION);
                dialog.dismiss();
            });

            builder.create();
            builder.show();

        }

        public void setCompassDrawable() {

            mapView.getCompassView().setBackgroundResource(R.drawable.compass_background);
            assert mapboxMap.getUiSettings() != null;
            mapboxMap.getUiSettings().setCompassImage(ContextCompat.getDrawable(getActivity(), R.drawable.compass_north_up));
            int padding = dpToPx(8);
            int elevation = dpToPx(18);
            mapView.getCompassView().setPadding(padding, padding, padding, padding);
            ViewCompat.setElevation(mapView.getCompassView(), elevation);
        }

        public SuzukiApplication getMyApplication() {

//        if (((SuzukiApplication) getActivity().getApplication()) != null)
            if(isAdded()) {
                return ((SuzukiApplication) requireActivity().getApplication());
            }
            else
                return null;

        }

        @Subscribe(threadMode = ThreadMode.MAIN)
        public void onConnectionEvent(EvenConnectionPojo event) {


            if (BLUETOOTH_STATE) {


            } else {
                staticConnectionStatus = false;

                Intent i = new Intent("status").putExtra("status", staticConnectionStatus);
                getActivity().sendBroadcast(i);
                if (staticConnectionStatus) {
                    llRedAlertBle.setVisibility(View.GONE);

                } else {
                    llRedAlertBle.setVisibility(View.VISIBLE);
                }
            }
        }

        private void enableLocationComponent(Style style) {
            if (ActivityCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }else{

                LocationComponentOptions options = LocationComponentOptions.builder(getActivity())
                        .trackingGesturesManagement(true)
                        .build();

                // .accuracyColor(ContextCompat.getColor(getActivity(), R.color.colorAccent))
                //                        .foregroundDrawable(R.drawable.location_pointer)


// Get an instance of the component LocationComponent
                locationComponent = mapboxMap.getLocationComponent();
                LocationComponentActivationOptions locationComponentActivationOptions = LocationComponentActivationOptions.builder(getActivity(), style)
                        .locationComponentOptions(options)
                        .build();
// Activate with options
                locationComponent.activateLocationComponent(locationComponentActivationOptions);
// Enable to make component visiblelocationEngine

                locationComponent.setLocationComponentEnabled(true);
                locationEngine = locationComponent.getLocationEngine();

                LocationEngineCallback<LocationEngineResult> locationEngineCallback = new LocationEngineCallback<LocationEngineResult>() {
                    @Override
                    public void onSuccess(LocationEngineResult locationEngineResult) {
                        if(locationEngineResult.getLastLocation() != null) {
                            Location location = locationEngineResult.getLastLocation();


                            currentlocation =location ;
                            if (!isRegionFixed) { // first check region is already set or not
                                if (getMyApplication() != null)
                                    getMyApplication().setRegion(location.getLatitude(), location.getLongitude());
                            }

//        Log.d("locccc", "onloc chang--" + eLocation.placeName);
//        getReverseGeoCode(currentlocation.getLatitude(), currentlocation.getLongitude());
                            try {
                                if (location.getLatitude() <= 0)
                                    return;
                                if (!firstFix) {
                                    mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location), 16), 500);
                                    firstFix = true;
//                Log.d("locccc", "onloc chang--" + eLocation.placeName);
                                    getReverseGeoCode(location.getLatitude(), location.getLongitude());
//                fromLocation = "Axiom";
                                    if(getMyApplication()!=null) {
                                        app = getMyApplication();
                                        app.setCurrentLocation(location);
                                    }



                                }

//            getReverseGeoCode(location.getLatitude(), location.getLongitude());
                               // app.setCurrentLocation(location);
                            } catch (Exception e) {
                                //ignore
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                };
                LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                        .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                        .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();
                assert locationEngine != null;
                locationEngine.requestLocationUpdates(request, locationEngineCallback, getMainLooper());
                locationEngine.getLastLocation(locationEngineCallback);
// Set the component's camera mode
                locationComponent.setCameraMode(CameraMode.TRACKING);
                locationComponent.setRenderMode(RenderMode.COMPASS);

            }

        }

//    private void enableLocationComponent() {
//        // Check if permissions are enabled and if not request
//        if (getActivity() != null)
//            if (PermissionsManager.areLocationPermissionsGranted(getActivity())) {
//
//                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
//                        ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                    common.showToast("Location permission is not given", TOAST_DURATION);
//                    requestLocationPermission();
//                    return;
//                }
//
//                //Current Location Activation
//                LocationComponentOptions options = LocationComponentOptions.builder(this)
//                        .foregroundDrawable(R.drawable.location_pointer)
//                        .build();
//// Get an instance of the component LocationComponent
//                LocationComponent locationComponent = mapboxMap.getLocationComponent();
//                LocationComponentActivationOptions locationComponentActivationOptions = LocationComponentActivationOptions.builder(this, style)
//                        .locationComponentOptions(options)
//                        .build();
//// Activate with options
//                locationComponent.activateLocationComponent(locationComponentActivationOptions);
////LocationChange Listener
//                LocationEngineCallback<LocationEngineResult> locationEngineCallback = new LocationEngineCallback<LocationEngineResult>() {
//                    @Override
//                    public void onSuccess(LocationEngineResult locationEngineResult) {
//                        if (locationEngineResult.getLastLocation() != null) {
//                            Location location = locationEngineResult.getLastLocation();
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//
//                    }
//                };
//                LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
//                        .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
//                        .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();
//                ////Request Location Update & add location change callback
//                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                    // TODO: Consider calling
//                    //    ActivityCompat#requestPermissions
//                    // here to request the missing permissions, and then overriding
//                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                    //                                          int[] grantResults)
//                    // to handle the case where the user grants the permission. See the documentation
//                    // for ActivityCompat#requestPermissions for more details.
//                    return;
//                }
//                locationEngine.requestLocationUpdates(request, locationEngineCallback, getMainLooper());
////Remove location update & callback
//                locationEngine.removeLocationUpdates(locationEngineCallback);
//                LocationComponentOptions options = LocationComponentOptions.builder(getActivity())
//                        .trackingGesturesManagement(true)
//                        .accuracyColor(ContextCompat.getColor(getActivity(), R.color.mapboxGreen))
//                        .build();
//
//
//                // Get an instance of the component
//                locationComponent = mapboxMap.getLocationComponent();
//
//
//                // Activate with options
//                locationComponent.activateLocationComponent(getActivity(), options);
//
//                // Enable to make component visible
//                locationComponent.setLocationComponentEnabled(true);
//                locationEngine = locationComponent.getLocationEngine();
//
//                if (locationEngine != null) {
//                    locationEngine.addLocationEngineListener(this);
//                }
//
//                // Set the component's camera mode
//                locationComponent.setCameraMode(CameraMode.TRACKING);
//                locationComponent.setRenderMode(RenderMode.COMPASS);
//                mapboxMap.setMaxZoomPreference(18.5);
//                mapboxMap.setMinZoomPreference(4);
//                firstFix = false;
//
//
//            } else {
//                permissionsManager = new PermissionsManager(this);
//                permissionsManager.requestLocationPermissions(getActivity());
//            }
//    }

//        @Override
//    public void onConnected() {
//
//        try {
//            if (getContext() != null)
//                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
//                        ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                    common.showToast("Location permission is not given", TOAST_DURATION);
//                    requestLocationPermission();
//                    return;
//                }
//        } catch (Exception e) {
//            e.getMessage();
//        }
//
//            locationEngine.requestLocationUpdates(request, locationEngineCallback, getMainLooper());
//        }
//
//
//    public void onLocationChanged(Location location) {
//        /*mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
//                new LatLng(location.getLatitude(), location.getLongitude()), 16));*/
//        currentlocation = location;
//        if (!isRegionFixed) { // first check region is already set or not
//            if (getMyApplication() != null)
//
//            getMyApplication().setRegion(location.getLatitude(), location.getLongitude());
//        }
//
////        Log.d("locccc", "onloc chang--" + eLocation.placeName);
////        getReverseGeoCode(currentlocation.getLatitude(), currentlocation.getLongitude());
//        try {
//
//            if (location.getLatitude() <= 0)
//                return;
//
//            if (!firstFix) {
//
//                mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location), 16), 500);
//                firstFix = true;
////                Log.d("locccc", "onloc chang--" + eLocation.placeName);
//                getReverseGeoCode(location.getLatitude(), location.getLongitude());
//
////                fromLocation = "Axiom";
//                app = getMyApplication();
//                app.setCurrentLocation(location);
//
//
//            }
//
////            getReverseGeoCode(location.getLatitude(), location.getLongitude());
//            app.setCurrentLocation(location);
//        } catch (Exception e) {
//            //ignore
//        }
//    }


        @Override
        public void onMapError(int i, String s) {
            Log.d("errr", "-" + s + "---" + i);
        }

   /* @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView.onCreate(savedInstanceState);
    }*/

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            mapView.onDestroy();
            getActivity().unregisterReceiver(mReceiver);
        }


        @Override
        public void onStart() {
            super.onStart();
            mapView.onStart();
            EventBus.getDefault().register(this);
        }


        @Override
        public void onStop() {
            super.onStop();
            mapView.onStop();
            if (locationEngine != null) {
                locationEngine.removeLocationUpdates(locationEngineCallback);
            }
            EventBus.getDefault().unregister(this);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mapView.onDestroy();
            if (locationEngine != null) {
                locationEngine.removeLocationUpdates(locationEngineCallback);
            }
        }

        @Override
        public void onPause() {
            super.onPause();

            mapView.onPause();

            if (locationEngine != null)
                locationEngine.removeLocationUpdates(locationEngineCallback);

        }

        @Override
        public void onResume() {
            super.onResume();

            mapView.onResume();
            if (locationEngine != null) {
//                locationEngine.requestLocationUpdates(locationEngineCallback);
//                locationEngine.addLocationEngineListener(this);
            }

            getRecentSearches(realm);

            // first time we want to set FabMenu and currentLoc to visible as well as focus on search loc

        }

        @Override
        public void onLowMemory() {
            super.onLowMemory();
            mapView.onLowMemory();
        }

        public static DirectionPolylinePlugin getDirectionPolylinePlugin() {
            return directionPolylinePlugin;
        }

        @Override
        public void onExplanationNeeded(List<String> permissionsToExplain) {
        }

        @Override
        public void onPermissionResult(boolean granted) {

        }

        @Override
        public boolean onMapLongClick(@NonNull LatLng latLng) {
            return false;
        }

//        @Override
//        public void requestDrag(RecyclerView.ViewHolder viewHolder) {
//            touchHelper.startDrag(viewHolder);
//        }


        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            mapView.onSaveInstanceState(outState);
        }


        public static NavLocation getUserLocation() {
            if (currentlocation != null) {
                NavLocation loc = new NavLocation("router");
                loc.setLatitude(currentlocation.getLatitude());
                loc.setLongitude(currentlocation.getLongitude());
                return loc;
            } else {
                return null;
            }
        }


        public void clearPOIs() {
            try {
                if (mapboxMap == null)
                    return;
                mapboxMap.removeAnnotations();
                if (directionPolylinePlugin != null)
                    directionPolylinePlugin.onDidFinishLoadingStyle();
            } catch (Exception e) {
                Timber.e(e);
            }
        }


        private void readMapItemRecords(Realm realm, Dialog dialog, Context context) {


            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {


//                mapListItem = realm.where(MapListRealmModule.class).findAll();
                    mapListItem = realm.where(MapListRealmModule.class)
                            .sort("id", Sort.ASCENDING)
                            .findAll();

                }
            });

            mapListCustomClassArrayList = new ArrayList<>();


            for (int i = 0; i < mapListItem.size(); i++) {

                MapListCustomClass mapListCustomClass = new MapListCustomClass();
                mapListCustomClass.setId(mapListItem.get(i).getId());
                mapListCustomClass.setName(mapListItem.get(i).getName());
                mapListCustomClassArrayList.add(mapListCustomClass);
            }


            rvDragDropCats = dialog.findViewById(R.id.rvDragDropCats);
            rvDragDropCats.setLayoutManager(new LinearLayoutManager(context));
            mAdapter = new DragDropRecyclerViewAdapter(mapListCustomClassArrayList, this, this);
            rvDragDropCats.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));


            ItemTouchHelper.Callback callback =
                    new ItemMoveCallback(mAdapter);
            touchHelper = new ItemTouchHelper(callback);
            touchHelper.attachToRecyclerView(rvDragDropCats);


            rvDragDropCats.setAdapter(mAdapter);

        }


        private void readMapItemRecords(Realm realm) {


            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {


//                mapListItem = realm.where(MapListRealmModule.class).findAll();
                    mapListItem = realm.where(MapListRealmModule.class)
                            .sort("id", Sort.ASCENDING)
                            .findAll();

                }
            });
            if (mapListItem.size() == 10) {

                TFab1.setTitle(mapListItem.get(0).getName());
                TFab2.setTitle(mapListItem.get(1).getName());
                TFab3.setTitle(mapListItem.get(2).getName());
                TFab4.setTitle(mapListItem.get(3).getName());


                if (mapListItem.get(0).getName().contentEquals("Suzuki Service")) {
                    TFab1.setImageResource(R.drawable.suzuki_logo);
                } else if (mapListItem.get(0).getName().contentEquals("Fuel Station")) {
                    TFab1.setImageResource(R.drawable.gas_icon);
                } else if (mapListItem.get(0).getName().contentEquals("Hospitals")) {
                    TFab1.setImageResource(R.drawable.hos_building2);
                } else if (mapListItem.get(0).getName().contentEquals("Banks and ATM")) {
                    TFab1.setImageResource(R.drawable.atm);
                } else if (mapListItem.get(0).getName().contentEquals("Food and Restaurants")) {
                    TFab1.setImageResource(R.drawable.hotel);
                }
//            else if (mapListItem.get(0).getName().contentEquals("Favourites")) {
//                TFab1.setImageResource(R.drawable.fav_menu_icon);
//            }
                else if (mapListItem.get(0).getName().contentEquals("Suzuki Sales")) {
                    TFab1.setImageResource(R.drawable.suzuki_logo);
                } else if (mapListItem.get(0).getName().contentEquals("Tyre Repair Shops")) {
                    TFab1.setImageResource(R.drawable.tire);
                } else if (mapListItem.get(0).getName().contentEquals("Medical Stores")) {
                    TFab1.setImageResource(R.drawable.pharmacist);
                } else if (mapListItem.get(0).getName().contentEquals("Parking")) {
                    TFab1.setImageResource(R.drawable.parking);
                } else if (mapListItem.get(0).getName().contentEquals("Convenience Stores")) {
                    TFab1.setImageResource(R.drawable.shopping_cart);
                }


                if (mapListItem.get(1).getName().contentEquals("Suzuki Service")) {
                    TFab2.setImageResource(R.drawable.suzuki_logo);
                } else if (mapListItem.get(1).getName().contentEquals("Fuel Station")) {
                    TFab2.setImageResource(R.drawable.gas_icon);
                } else if (mapListItem.get(1).getName().contentEquals("Hospitals")) {
                    TFab2.setImageResource(R.drawable.hos_building2);
                } else if (mapListItem.get(1).getName().contentEquals("Banks and ATM")) {
                    TFab2.setImageResource(R.drawable.atm);
                } else if (mapListItem.get(1).getName().contentEquals("Food and Restaurants")) {
                    TFab2.setImageResource(R.drawable.hotel);
                }
//            else if (mapListItem.get(1).getName().contentEquals("Favourites")) {
//                TFab2.setImageResource(R.drawable.fav_menu_icon);
//            }
                else if (mapListItem.get(1).getName().contentEquals("Suzuki Sales")) {
                    TFab2.setImageResource(R.drawable.suzuki_logo);
                } else if (mapListItem.get(1).getName().contentEquals("Tyre Repair Shops")) {
                    TFab2.setImageResource(R.drawable.tire);
                } else if (mapListItem.get(1).getName().contentEquals("Medical Stores")) {
                    TFab2.setImageResource(R.drawable.pharmacist);
                } else if (mapListItem.get(1).getName().contentEquals("Parking")) {
                    TFab2.setImageResource(R.drawable.parking);
                } else if (mapListItem.get(1).getName().contentEquals("Convenience Stores")) {
                    TFab2.setImageResource(R.drawable.shopping_cart);
                }


                if (mapListItem.get(2).getName().contentEquals("Suzuki Service")) {
                    TFab3.setImageResource(R.drawable.suzuki_logo);
                } else if (mapListItem.get(2).getName().contentEquals("Fuel Station")) {
                    TFab3.setImageResource(R.drawable.gas_icon);
                } else if (mapListItem.get(2).getName().contentEquals("Hospitals")) {
                    TFab3.setImageResource(R.drawable.hos_building2);
                } else if (mapListItem.get(2).getName().contentEquals("Banks and ATM")) {
                    TFab3.setImageResource(R.drawable.atm);
                } else if (mapListItem.get(2).getName().contentEquals("Food and Restaurants")) {
                    TFab3.setImageResource(R.drawable.hotel);
                }
//            else if (mapListItem.get(2).getName().contentEquals("Favourites")) {
//                TFab3.setImageResource(R.drawable.fav_menu_icon);
//            }
                else if (mapListItem.get(2).getName().contentEquals("Suzuki Sales")) {
                    TFab3.setImageResource(R.drawable.suzuki_logo);
                } else if (mapListItem.get(2).getName().contentEquals("Tyre Repair Shops")) {
                    TFab3.setImageResource(R.drawable.tire);
                } else if (mapListItem.get(2).getName().contentEquals("Medical Stores")) {
                    TFab3.setImageResource(R.drawable.pharmacist);
                } else if (mapListItem.get(2).getName().contentEquals("Parking")) {
                    TFab3.setImageResource(R.drawable.parking);
                } else if (mapListItem.get(2).getName().contentEquals("Convenience Stores")) {
                    TFab3.setImageResource(R.drawable.shopping_cart);
                }


                if (mapListItem.get(3).getName().contentEquals("Suzuki Service")) {
                    TFab4.setImageResource(R.drawable.suzuki_logo);
                } else if (mapListItem.get(3).getName().contentEquals("Fuel Station")) {
                    TFab4.setImageResource(R.drawable.gas_icon);
                } else if (mapListItem.get(3).getName().contentEquals("Hospitals")) {
                    TFab4.setImageResource(R.drawable.hos_building2);
                } else if (mapListItem.get(3).getName().contentEquals("Banks and ATM")) {
                    TFab4.setImageResource(R.drawable.atm);
                } else if (mapListItem.get(3).getName().contentEquals("Food and Restaurants")) {
                    TFab4.setImageResource(R.drawable.hotel);
                }
//            else if (mapListItem.get(3).getName().contentEquals("Favourites")) {
//                TFab4.setImageResource(R.drawable.fav_menu_icon);
//            }
                else if (mapListItem.get(3).getName().contentEquals("Suzuki Sales")) {
                    TFab4.setImageResource(R.drawable.suzuki_logo);
                } else if (mapListItem.get(3).getName().contentEquals("Tyre Repair Shops")) {
                    TFab4.setImageResource(R.drawable.tire);
                } else if (mapListItem.get(3).getName().contentEquals("Medical Stores")) {
                    TFab4.setImageResource(R.drawable.pharmacist);
                } else if (mapListItem.get(3).getName().contentEquals("Parking")) {
                    TFab4.setImageResource(R.drawable.parking);
                } else if (mapListItem.get(3).getName().contentEquals("Convenience Stores")) {
                    TFab4.setImageResource(R.drawable.shopping_cart);
                }


                TFab1.setOnClickListener(v -> {

                    if (mapListItem.get(0).getName().contentEquals("Favourites")) {
                        Intent in = new Intent(getActivity(), TripActivity.class);
                        in.putExtra("fav", "fav");
                        startActivity(in);
                        FabMenu.collapse();

                    } else {

                        etNearbySearchLoc.setVisibility(View.VISIBLE);
                        etSearchLoc.setVisibility(View.GONE);
                        etNearbySearchLoc.setText(mapListItem.get(0).getName());
                        FabMenu.collapse();
                    }
                });

                TFab2.setOnClickListener(v -> {

                    if (mapListItem.get(1).getName().contentEquals("Favourites")) {
                        Intent in = new Intent(getActivity(), TripActivity.class);
                        in.putExtra("fav", "fav");
                        startActivity(in);
                        FabMenu.collapse();

                    } else {

                        etNearbySearchLoc.setVisibility(View.VISIBLE);
                        etSearchLoc.setVisibility(View.GONE);
                        etNearbySearchLoc.setText(mapListItem.get(1).getName());
                        FabMenu.collapse();

                    }
                });

                TFab3.setOnClickListener(v -> {

                    if (mapListItem.get(2).getName().contentEquals("Favourites")) {
                        Intent in = new Intent(getActivity(), TripActivity.class);
                        in.putExtra("fav", "fav");
                        startActivity(in);
                        FabMenu.collapse();

                    } else {
                        etNearbySearchLoc.setVisibility(View.VISIBLE);
                        etSearchLoc.setVisibility(View.GONE);

                        etNearbySearchLoc.setText(mapListItem.get(2).getName());
                        FabMenu.collapse();
                    }
                });

                TFab4.setOnClickListener(v -> {

                    if (mapListItem.get(3).getName().contentEquals("Favourites")) {
                        Intent in = new Intent(getActivity(), TripActivity.class);
                        in.putExtra("fav", "fav");
                        startActivity(in);
                        FabMenu.collapse();

                    } else {
                        etNearbySearchLoc.setVisibility(View.VISIBLE);
                        etSearchLoc.setVisibility(View.GONE);
                        etNearbySearchLoc.setText(mapListItem.get(3).getName());
                        FabMenu.collapse();
                    }
                });
            } else {
//            addMapItemsDataToRealm();
            }
        }

        private void addMapItemsDataToRealm() {

            ArrayList<String> stringArrayList = new ArrayList<>();
            if (stringArrayList.size() > 0) {
                stringArrayList.clear();
            }
            stringArrayList.add(getResources().getString(R.string.suzukiservice));
            stringArrayList.add(getResources().getString(R.string.fuel));
            stringArrayList.add(getResources().getString(R.string.hospitals));
//        stringArrayList.add(getResources().getString(R.string.favourites));
            stringArrayList.add(getResources().getString(R.string.atm));
            stringArrayList.add(getResources().getString(R.string.food));
            stringArrayList.add(getResources().getString(R.string.sales));
            stringArrayList.add(getResources().getString(R.string.tyrerepair));
            stringArrayList.add(getResources().getString(R.string.medicals));
            stringArrayList.add(getResources().getString(R.string.parking));
            stringArrayList.add(getResources().getString(R.string.convenience));

//        deleteTripRecord();
            Realm realm = Realm.getDefaultInstance();
            try {
                realm.executeTransaction(new Realm.Transaction() {

                    @Override
                    public void execute(Realm realm) {

                        RealmResults<MapListRealmModule> results = realm.where(MapListRealmModule.class).findAll();


                        Log.d("sisiisi", "---" + stringArrayList.size());

                        for (int i = 0; i < stringArrayList.size(); i++) {
                            MapListRealmModule mapListRealmModule = realm.createObject(MapListRealmModule.class);
                            mapListRealmModule.setId(i);
                            mapListRealmModule.setName(stringArrayList.get(i));
                            realm.insert(mapListRealmModule);

                        }


                    }
                });

            } catch (Exception e) {
                Log.d("realmex", "--" + e.getMessage());

            }

            readMapItemRecords(realm);

        }

        private void addUpdatedMapListDataToRealm(ArrayList<MapListCustomClass> mapList) {

            Realm realm = Realm.getDefaultInstance();
            try {
                realm.executeTransaction(realm1 -> {


                    RealmResults<MapListRealmModule> results = realm1.where(MapListRealmModule.class).findAll();

                    results.deleteAllFromRealm();

                    for (int i = 0; i < mapList.size(); i++) {


                        MapListRealmModule recentTripRealmModule = realm1.createObject(MapListRealmModule.class);


                        recentTripRealmModule.setId(mapList.get(i).getId());
                        recentTripRealmModule.setName(mapList.get(i).getName());

                        realm1.insert(recentTripRealmModule);
                    }


                });

            } catch (Exception e) {
                Log.d("realmex", "--" + e.getMessage());

            }


        }

        @SuppressLint("LogNotTimber")
        private void displayLocationSettingsRequest(Context context) {
            GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                    .addApi(LocationServices.API).build();
            googleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(10000 / 2);

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
            builder.setAlwaysShow(true);

            PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(result1 -> {
                final Status status = result1.getStatus();
                Log.d("sts---", "-" + status.getStatusCode() + "-- " + status.getStatusMessage());
                switch (status.getStatusCode()) {

                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i("loc", "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i("loc", "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");
                        try {

                            status.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i("loc", "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i("loc", "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            });
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            if (requestCode == 101) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    common.showToast("Location Permission Granted", TOAST_DURATION);
                    if (mapView != null)
                        mapView.getMapAsync(this);
                }
            }

        }

        @SuppressLint("LogNotTimber")
        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
        {

            if (requestCode == REQUEST_CHECK_SETTINGS)
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Log.i("TAG", "User agreed to make required location settings changes.");
                        break;
                    case Activity.RESULT_CANCELED:
                        Log.i("TAG", "User chose not to make required location settings changes.");
                        Toast.makeText(getContext(), "Gps is not enabled. This will effect location services ", Toast.LENGTH_SHORT).show();
                        break;
                }
            super.onActivityResult(requestCode, resultCode, data);

        }

    }










