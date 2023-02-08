package com.suzuki.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;


import com.mappls.sdk.services.api.OnResponseCallback;
import com.mappls.sdk.services.api.PlaceResponse;
import com.mappls.sdk.services.api.reversegeocode.MapplsReverseGeoCode;
import com.mappls.sdk.services.api.reversegeocode.MapplsReverseGeoCodeManager;
import com.suzuki.R;
import com.suzuki.adapter.ProfileVehicleAdapter;
import com.suzuki.pojo.LastParkedLocationRealmModule;
import com.suzuki.pojo.MapListRealmModule;
import com.suzuki.pojo.RiderProfileModule;
import com.suzuki.utils.Common;
import com.suzuki.utils.CurrentLoc;

import io.realm.Realm;
import io.realm.RealmResults;

import static com.suzuki.activity.HomeScreenActivity.TOAST_DURATION;
import static com.suzuki.activity.ProfileActivity.classid;
import static com.suzuki.activity.ProfileActivity.divid;
import static com.suzuki.utils.Common.EXCEPTION;

import static org.greenrobot.eventbus.EventBus.TAG;

import java.util.ArrayList;

public class CreateProfileActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener, LocationListener {

    Spinner classSpinner, divSpinner;
    ScrollView scrollView;
    public static final int PERMISSION_REQUEST_BLUETOOTH_SCAN = 123;
    public static final int PERMISSION_REQUEST_BLUETOOTH_CONNECT = 101;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 456;

    ViewPager viewPager;
    LinearLayout sliderDotspanel,tnc_focus;
    private int dotscount;
    private ImageView[] dots;
    EditText etLocation, etUserName;
    LinearLayout imArrow;
    TextView ivAdd;
    Realm realm;
    String provider, userName, location, vehicleType, bikeType;
    RealmResults<MapListRealmModule> recentTrip;
    TextView tvPrivacy, tvTermsConditions;

    ArrayAdapter<String> typeArray, modelArray;
    Double lat, lng;
    EditText riderName, riderLocation;
    ProfileVehicleAdapter viewPagerAdapter;
    int selectedPos = 0;
    Context context;
    //String vehicleColorDrawable;
    LinearLayout mLinearLayoutViewPager_layout, mLinearLayout_signupLayout;
    //    private Integer[] images = {R.drawable.gixer_black, R.drawable.gixer_blue, R.drawable.gixer_black, R.drawable.gixer_blue, R.drawable.gixer_silver, R.drawable.gixer_blue};
    //public static Integer[] notselected = {};
    public static Integer[] V_STORM_SX = {R.drawable.vstorm_red, R.drawable.vstorm_yellow, R.drawable.vstorm_black};
 //   public static Integer[] V_STROM_SX_COLOR={R.string.v_strom_red,R.string.v_strom_yellow,R.string.v_strom_black};
    public static ArrayList<String> arraylist= new ArrayList<>();

    public static Integer[] gixxer250images = {R.drawable.gixxer250black, R.drawable.gixxer250blue,R.drawable.gixxer250sfblack, R.drawable.gixxer250sf, R.drawable.gixxer250sfblue};
    public static Integer[] gixxerSFimages = {R.drawable.gixxer150black, R.drawable.gixxer150blue, R.drawable.gixxer150orange,R.drawable.gixxer150sfblack, R.drawable.gixxer150sforange, R.drawable.gixxer150sfblue};


    public static Integer[] accessSE125 = {R.drawable.access_125_se_black, R.drawable.access_dual_tone, R.drawable.access_125_se_matte_blue, R.drawable.access_125_se_bronze,R.drawable.access_125_se_white,R.drawable.access_glossy_grey};
//    public static Integer[] accessSE125color= {R.string.access_black,R.string.access_dualtone,R.string.access_matteblue,R.string.access_glossybronze,R.string.access_white,R.string.access_glossygrey};
    public static Integer[] Avenis = {R.drawable.avenis_blue, R.drawable.avenis_green, R.drawable.avenis_grey, R.drawable.avenis_red, R.drawable.avenis_white};
    public static String[] avenis_color;
    public  static String[]V_STROM_SX_COLOR;
    public static String[] BurgmanStreet_color;
    public static String[] BurgmanStreetEX_color;
    public static String[]accessSE125color;

    public static String[]gixxerSfcolor;
    public static String[]gixxer250color;



    public static Integer[] BurgmanStreet = {R.drawable.burgman_fibroin_grey, R.drawable.burgman_matteblack, R.drawable.burgman_matte_blue, R.drawable.burgman_matte_red, R.drawable.burgman_mirage_white};
  //  public static Integer[] BurgmanStreet_color= {R.string.burgman_fibroingrey,R.string.burgman_matteblack,R.string.burgman_matteblue,R.string.burgman_mattered,R.string.burgman_miragewhite};

    public static Integer[] Burgman_Street_EX = {R.drawable.burgman_ex_black, R.drawable.burgman_ex_silver, R.drawable.burgman_ex_bronze};
  //  public static Integer[] BurgmanStreetEX_color= {R.string.burgmanex_black,R.string.burgmanex_silver,R.string.burgmanex_bronze};

    ImageView currentLocIV;
    LocationManager locationManager;
    private Location currentLocation = null;
    private CheckBox privacyCheckBox;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onResume() {
        super.onResume();

        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                locationManager.requestLocationUpdates(provider, 400, 1, this);
            }
        }

        classSpinner.setSelection(classid);
        divSpinner.setSelection(divid);
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.create_profile);



        int permission = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            permission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.BLUETOOTH_SCAN);
        }
        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to record denied");
            makeRequest();
        }

        {
            sharedPreferences = getSharedPreferences("PROFILE", MODE_PRIVATE);
            editor = sharedPreferences.edit();

            editor.putInt("START_COUNT",1);
            editor.commit();
        }

        context = this;
        classSpinner = (Spinner) findViewById(R.id.classSpinner);
        etUserName = (EditText) findViewById(R.id.etUserName);
        etLocation = (EditText) findViewById(R.id.etLocation);
        tnc_focus=findViewById(R.id.tnc_focus);
        scrollView= findViewById(R.id.scrollView);
        ivAdd = findViewById(R.id.SaveButton);
        imArrow = (LinearLayout) findViewById(R.id.llEditProfileBack);
        riderName = (EditText) findViewById(R.id.etUserName);
        riderLocation = (EditText) findViewById(R.id.etLocation);
        divSpinner = (Spinner) findViewById(R.id.divSpinner);
        tvPrivacy = (TextView) findViewById(R.id.tvPrivacy);
        tvTermsConditions = (TextView) findViewById(R.id.tvTermsConditions);
        currentLocIV = findViewById(R.id.currentLocIv);
        privacyCheckBox = findViewById(R.id.privacyCheckBox);

        V_STROM_SX_COLOR=new String[]{"Pearl Blaze Orange","Champion Yellow","Glass Sparkle Black"};
        BurgmanStreet_color= new String[]{"Glossy Grey","Black","Blue","Bordeaux Red","Pearl Mirage White"};
        avenis_color=new String[]{"Triton Blue","Lush Green","Black","Pearl Blaze Orange","Pearl Mirage White" };
        BurgmanStreetEX_color= new String[]{"Black","Platinum Silver","Royal Bronze"};
        accessSE125color=new String[]{"Black","Dual Tone - Solid Ice Green + Pearl Mirage White","Blue","Royal Bronze","Pearl Mirage White","Glossy Grey"};
        gixxer250color=new String[]{"Matte Black","Matte Stellar Blue","Matte Black","Sonic Silver/Triton Blue","Matte Stellar Blue"};
        gixxerSfcolor=new String[]{"Glass Sparkle Black","Triton Blue","Pearl Blaze Orange","Glass Sparkle Black","Pearl Blaze Orange","Triton Blue"};

        etUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etUserName.setCursorVisible(true);
            }
        });
        etLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etLocation.setCursorVisible(true);
            }
        });

        etUserName.setFilters(new InputFilter[]{new InputFilter.AllCaps(), new InputFilter.LengthFilter(20)});

        currentLocIV.setOnClickListener(v -> getCurrentPlace());

        classSpinner.setOnItemSelectedListener(this);
        divSpinner.setOnItemSelectedListener(this);

        mLinearLayout_signupLayout = findViewById(R.id.lv_singup);
        mLinearLayoutViewPager_layout = findViewById(R.id.viewpager_layout);

        classSpinner.setOnTouchListener((v, event) -> {
            etLocation.clearFocus();
            etUserName.clearFocus();

            InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(etLocation.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(etUserName.getWindowToken(), 0);
            return false;
        });

        divSpinner.setOnTouchListener((v, event) -> {
            etLocation.clearFocus();
            etUserName.clearFocus();
            etUserName.setCursorVisible(false);
            etLocation.setCursorVisible(false);

            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(etLocation.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(etUserName.getWindowToken(), 0);
            return false;
        });

        realm = Realm.getDefaultInstance();
        etUserName.setImeActionLabel("DONE", EditorInfo.IME_ACTION_DONE);
        etLocation.setImeActionLabel("DONE", EditorInfo.IME_ACTION_DONE);

        etUserName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {

                etUserName.clearFocus();
                InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etUserName.getWindowToken(), 0);
            }
            return false;
        });

        etLocation.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                etLocation.clearFocus();
                etUserName.clearFocus();
                etUserName.setCursorVisible(false);
                etLocation.setCursorVisible(false);
                InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);

                imm.hideSoftInputFromWindow(etLocation.getWindowToken(), 0);
            }
            return false;
        });

        tvPrivacy.setOnClickListener(v -> startActivity(new Intent(context, PrivacyActivity.class)));

        tvTermsConditions.setOnClickListener(v -> startActivity(new Intent(context, TermsActivity.class)));
        typeArray = new ArrayAdapter<>(this, R.layout.spinner_item);
        typeArray.setDropDownViewResource(R.layout.spinner_dropdown);
        classSpinner.setAdapter(typeArray);
        typeArray.add("Choose Your Ride Type");
        typeArray.add("Scooter");
        typeArray.add("Motorcycle");
        typeArray.setNotifyOnChange(true);
        classSpinner.setSelection(0);
        modelArray = new ArrayAdapter<>(this, R.layout.spinner_item);
        modelArray.setDropDownViewResource(R.layout.spinner_dropdown);
        divSpinner.setAdapter(modelArray);

        ivAdd.setOnClickListener(v -> {
            if((Build.VERSION.SDK_INT >= 31)){
                check_ble_permission();
            }

            else{

                if (etUserName.getText().toString().trim().equals("") || etUserName.getText().toString().equals(" ")) {
                    new Common(CreateProfileActivity.this).showToast("Enter Rider's Name", TOAST_DURATION);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(etUserName.getWindowToken(), 0);
                } else if (etLocation.getText().toString().trim().equals("") || etLocation.getText().toString().equals(" ")) {
                    new Common(CreateProfileActivity.this).showToast("Enter Location", TOAST_DURATION);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(etLocation.getWindowToken(), 0);
                    imm.hideSoftInputFromWindow(etUserName.getWindowToken(), 0);

                }
                else if (classSpinner.getSelectedItem().toString().equals("Choose Your Ride Type")) {
                    new Common(CreateProfileActivity.this).showToast("Select ride type", TOAST_DURATION);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(etLocation.getWindowToken(), 0);
                    imm.hideSoftInputFromWindow(etUserName.getWindowToken(), 0);
                } else if (divSpinner.getSelectedItem().toString().equals("Choose Your Ride Model")) {
                    new Common(CreateProfileActivity.this).showToast("Select model type", TOAST_DURATION);
                }

                else if (!privacyCheckBox.isChecked()) {
                    new Common(CreateProfileActivity.this).showToast("Please accept the Terms &" +
                            " Conditions and Privacy Policy to proceed further.", 2500);
                }

                else {

                    userName = etUserName.getText().toString();
                    location = etLocation.getText().toString();
                    vehicleType = classSpinner.getSelectedItem().toString();
                    bikeType = divSpinner.getSelectedItem().toString();
                    classid = classSpinner.getSelectedItemPosition();
                    divid = divSpinner.getSelectedItemPosition();

                    new Common(this).update_vehicle_data(vehicleType, bikeType, viewPager.getCurrentItem());
                    new Common(this).update_user_data(userName, location);
                    //deleteRecord();
                    //addRecord();
                    //updateRecord();

                    startActivity(new Intent(getApplicationContext(), HomeScreenActivity.class));
                    finish();
                }
            }

        });
        viewPager = (ViewPager) findViewById(R.id.viewPager);

        sliderDotspanel = (LinearLayout) findViewById(R.id.SliderDots);

        addLastParkedLatLong();

        /*viewPagerAdapter = new ProfileVehicleAdapter(getApplicationContext(), gixxerimages);
        viewPager.setAdapter(viewPagerAdapter);
        setUpViewPager(gixxerimages, 0);*/

        recentTrip = realm.where(MapListRealmModule.class).findAll();
        recentTrip.addChangeListener(tripRealmModules -> recentTrip = realm.where(MapListRealmModule.class).findAll());

        classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (classSpinner.getSelectedItem().toString() == "Choose Your Ride Type") {

                    divSpinner.setEnabled(false);
                    divSpinner.setClickable(false);

                }
                else{
                    divSpinner.setEnabled(true);
                    divSpinner.setClickable(true);
                }

                switch (position) {
                    case 0:
                        nothingSelected();
                        break;

                    case 1:
                        modelArray.clear();
                        scooterModel();
                        divSpinner.setSelection(0);
                        mLinearLayoutViewPager_layout.setVisibility(View.INVISIBLE);
                        viewPagerAdapter = new ProfileVehicleAdapter(getApplicationContext(), Avenis, avenis_color);
                        viewPager.setAdapter(viewPagerAdapter);
                        viewPager.setCurrentItem(0);
                        dotscount = Avenis.length;
                        setUpViewPager(Avenis, 0);
                        viewPagerAdapter.notifyDataSetChanged();
                        findViewById(R.id.slide).setVisibility(View.VISIBLE);
                        break;


                    case 2:
                        modelArray.clear();
                        bikeModel();
                        divSpinner.setSelection(0);
                        mLinearLayoutViewPager_layout.setVisibility(View.INVISIBLE);
                        viewPagerAdapter = new ProfileVehicleAdapter(getApplicationContext(), V_STORM_SX, V_STROM_SX_COLOR);
                        viewPager.setAdapter(viewPagerAdapter);
                        dotscount = V_STORM_SX.length;
                        viewPager.setCurrentItem(0);
                        setUpViewPager(V_STORM_SX, 0);
                        viewPagerAdapter.notifyDataSetChanged();
                        findViewById(R.id.slide).setVisibility(View.VISIBLE);
                        break;

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        divSpinner.setOnItemSelectedListener(this);
    }

    private void makeRequest() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.BLUETOOTH_SCAN},
                    PERMISSION_REQUEST_BLUETOOTH_SCAN);
        }

        else{
            startActivity(
                    new Intent(
                            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", getPackageName(), null)
                    )
            );
        }
    }


    private void check_ble_permission() {

        if(Build.VERSION.SDK_INT >= 31){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_REQUEST_BLUETOOTH_CONNECT);

            }
            else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, PERMISSION_REQUEST_BLUETOOTH_SCAN);

            }
            else{
                if (etUserName.getText().toString().trim().equals("") || etUserName.getText().toString().equals(" ")) {
                    new Common(CreateProfileActivity.this).showToast("Enter Rider's Name", TOAST_DURATION);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(etUserName.getWindowToken(), 0);
                } else if (etLocation.getText().toString().trim().equals("") || etLocation.getText().toString().equals(" ")) {
                    new Common(CreateProfileActivity.this).showToast("Enter Location", TOAST_DURATION);

                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(etLocation.getWindowToken(), 0);
                    imm.hideSoftInputFromWindow(etUserName.getWindowToken(), 0);

                } else if (classSpinner.getSelectedItem().toString().equals("Choose Your Ride Type")) {
                    new Common(CreateProfileActivity.this).showToast("Select ride type", TOAST_DURATION);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(etLocation.getWindowToken(), 0);
                    imm.hideSoftInputFromWindow(etUserName.getWindowToken(), 0);
                } else if (divSpinner.getSelectedItem().toString().equals("Choose Your Ride Model")) {
                    new Common(CreateProfileActivity.this).showToast("Select model type", TOAST_DURATION);
                } else if (!privacyCheckBox.isChecked()) {
                    new Common(CreateProfileActivity.this).showToast("Please accept the Terms &" +
                            " Conditions and Privacy Policy to proceed further.", 2500);
                } else {
                    userName = etUserName.getText().toString();
                    location = etLocation.getText().toString();
                    vehicleType = classSpinner.getSelectedItem().toString();
                    bikeType = divSpinner.getSelectedItem().toString();
                    classid = classSpinner.getSelectedItemPosition();
                    divid = divSpinner.getSelectedItemPosition();

                    new Common(this).update_vehicle_data(vehicleType, bikeType, viewPager.getCurrentItem());
                    new Common(this).update_user_data(userName, location);
                    //deleteRecord();
                    //addRecord();
                    //updateRecord();

                    startActivity(new Intent(getApplicationContext(), HomeScreenActivity.class));
                    finish();
                }
            }
        }
    }

//    private void check_ble_permission() {
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
//        {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            if (Build.VERSION.SDK_INT >= 31) {
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 100);
//            }
//
//        }
//        else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            if (Build.VERSION.SDK_INT >= 31) {
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 100);
//            }
//
//        }
//        else{
//
//            startActivity(new Intent(getApplicationContext(), HomeScreenActivity.class));
//            finish();
//        }
//    }

    private void initLocationManager() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
    }

    @Override
    protected void onPause() {
        super.onPause();

        classid = classSpinner.getSelectedItemPosition();
        divid = divSpinner.getSelectedItemPosition();

        if (locationManager != null) locationManager.removeUpdates(this);
    }

    public void showExitAlert(String message) {
        Dialog dialog = new Dialog(this, R.style.custom_dialog);
        dialog.setContentView(R.layout.custom_dialog);

        TextView tvAlertText = dialog.findViewById(R.id.tvAlertText);
        tvAlertText.setText(message);
        ImageView ivCross = dialog.findViewById(R.id.ivCross);
        ivCross.setOnClickListener(v -> dialog.cancel());

        ImageView ivCheck = dialog.findViewById(R.id.ivCheck);

        ivCheck.setOnClickListener(v -> {
            dialog.cancel();
            String[] perms = {"android.permission.ACCESS_FINE_LOCATION"};

            requestPermissions(perms, 201);
        });
        dialog.show();
    }

    private void setUpPager(int dotscount) {

        for (int i = 0; i < dotscount; i++) {

            dots[i] = new ImageView(this);
            dots[i].setImageDrawable(ContextCompat.getDrawable(this.getApplicationContext(), R.drawable.non_active_dot));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            params.setMargins(8, 0, 8, 0);

            sliderDotspanel.addView(dots[i], params);
        }

        dots[0].setImageDrawable(ContextCompat.getDrawable(this.getApplicationContext(), R.drawable.active_dot));

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageSelected(int position) {

                for (int i = 0; i < dotscount; i++) {
                    dots[i].setImageDrawable(ContextCompat.getDrawable(getApplicationContext().getApplicationContext(), R.drawable.non_active_dot));
                }

                dots[position].setImageDrawable(ContextCompat.getDrawable(getApplicationContext().getApplicationContext(), R.drawable.active_dot));
            }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });
    }

    public void setUpViewPager(Integer[] images, int userSelectedImage) {

        setupPagerIndidcatorDots(images);

        dots[userSelectedImage].setImageResource(R.drawable.active_dot);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < dots.length; i++) {
                    dots[i].setImageResource(R.drawable.non_active_dot);
                }

                selectedPos = position;
                dots[position].setImageResource(R.drawable.active_dot);
            }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });
    }

    private void setupPagerIndidcatorDots(Integer[] images) {
        if (dots != null) {
            for (int k = 0; k < dots.length; k++) {
                sliderDotspanel.removeView(dots[k]);
            }
        }
        dots = new ImageView[images.length];
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new ImageView(getApplicationContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 0, 8, 0);
            dots[i].setLayoutParams(params);
            dots[i].setImageResource(R.drawable.non_active_dot);
            dots[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.setAlpha(1);
                }
            });
            sliderDotspanel.addView(dots[i]);
            sliderDotspanel.bringToFront();
        }
    }

    public void addRecord() {

        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(realm1 -> {

                RiderProfileModule riderProfile = realm1.where(RiderProfileModule.class).equalTo("id", 1).findFirst();

                if (riderProfile == null) {

                    riderProfile = realm1.createObject(RiderProfileModule.class, 1);
                    riderProfile.setName(etUserName.getText().toString());
                    riderProfile.setLocation(etLocation.getText().toString());
                    riderProfile.setBike(vehicleType);
                    riderProfile.setUserSelectedImage(selectedPos);
                    riderProfile.setBikeModel(bikeType);

                    realm1.insertOrUpdate(riderProfile);
                } else if (riderProfile != null) {
                    riderProfile.setName(etUserName.getText().toString());
                    riderProfile.setLocation(etLocation.getText().toString());
                    riderProfile.setBike(vehicleType);
                    riderProfile.setBikeModel(bikeType);
                    riderProfile.setUserSelectedImage(selectedPos);

                    realm1.insertOrUpdate(riderProfile);
                }
            });
        } catch (Exception e) {
            Log.e(EXCEPTION,"Createprofile: addRecord: "+String.valueOf(e));
        }
    }

    public void updateRecord() {


        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(realm1 -> {

                RiderProfileModule riderProfile = realm1.where(RiderProfileModule.class).equalTo("id", 1).findFirst();

                if (riderProfile == null) {

                    riderProfile = realm1.createObject(RiderProfileModule.class, 1);
                    riderProfile.setName(etUserName.getText().toString());
                    riderProfile.setLocation(etLocation.getText().toString());
                    riderProfile.setBike(vehicleType);
                    riderProfile.setBikeModel(bikeType);

                    realm1.insertOrUpdate(riderProfile);
                } else if (riderProfile != null) {
                    riderProfile.setName(etUserName.getText().toString());
                    riderProfile.setLocation(etLocation.getText().toString());
                    riderProfile.setBike(vehicleType);
                    riderProfile.setBikeModel(bikeType);

                    realm1.insertOrUpdate(riderProfile);
                }
            });
        } catch (Exception e) {
            Log.e(EXCEPTION,"Createprofile: updateRecord: "+String.valueOf(e));
        }

    }

    public void deleteRecord() {
        RealmResults<RiderProfileModule> results = realm.where(RiderProfileModule.class).limit(1).findAll();
        realm.beginTransaction();

        results.deleteAllFromRealm();

        realm.commitTransaction();
    }

    public void viewRecord() {

        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(realm1 -> {

                RiderProfileModule riderProfile = realm1.where(RiderProfileModule.class).equalTo("id", 1).findFirst();

                if (riderProfile != null) {
                    riderName.setText(riderProfile.getName());
                    riderLocation.setText(riderProfile.getLocation());
                }
            });

        } catch (Exception e) {
            Log.e(EXCEPTION,"Createprofile: viewRecord: "+String.valueOf(e));
        }
    }


    @Override
    public void onClick(View view) {


//        Log.d("Spinner", "Vehicletype---" + classSpinner.getSelectedItem().toString());
//        Log.d("Spinner", "Scootertype" + divSpinner.getSelectedItem().toString());


    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {

        if (classSpinner.getSelectedItem().toString() == "Choose Your Ride Type") {

            bikeType = classSpinner.getSelectedItem().toString();
            if (bikeType.contentEquals("Choose Your Ride Type")) {
                mLinearLayoutViewPager_layout.setVisibility(View.INVISIBLE);
                viewPager.setVisibility(View.INVISIBLE);

                viewPager.setVisibility(View.INVISIBLE);
                dotscount = 0;
            }
        } else viewPager.setVisibility(View.VISIBLE);



         if (classSpinner.getSelectedItem().toString() == "Scooter") {
            vehicleType = divSpinner.getSelectedItem().toString();
            Log.d("BikeType", "----" + vehicleType);
            switch (position) {
                case 0:
                    viewPager.setVisibility(View.INVISIBLE);
                    mLinearLayoutViewPager_layout.setVisibility(View.INVISIBLE);
                    break;

                case 1:
                    if (vehicleType.contentEquals("Avenis")) {
                        viewPagerAdapter = new ProfileVehicleAdapter(getApplicationContext(), Avenis, avenis_color);

                        viewPager.setAdapter(viewPagerAdapter);
                        viewPager.setCurrentItem(0);
                        dotscount = Avenis.length;
                        setUpViewPager(Avenis, 0);
                        viewPager.setVisibility(View.VISIBLE);
                        mLinearLayoutViewPager_layout.setVisibility(View.VISIBLE);
                    }
                    break;

                case 2:
                    if (vehicleType.contentEquals("Access 125")) {
                        viewPagerAdapter = new ProfileVehicleAdapter(getApplicationContext(), accessSE125,accessSE125color);

                        viewPager.setAdapter(viewPagerAdapter);
                        viewPager.setCurrentItem(0);
                        dotscount = accessSE125.length;
                        setUpViewPager(accessSE125, 0);
                        viewPager.setVisibility(View.VISIBLE);
                        mLinearLayoutViewPager_layout.setVisibility(View.VISIBLE);
                    }
                    break;

                case 3:
                    if (vehicleType.contentEquals("Burgman Street")) {
                        viewPagerAdapter = new ProfileVehicleAdapter(getApplicationContext(), BurgmanStreet, BurgmanStreet_color);


                        viewPager.setAdapter(viewPagerAdapter);
                        viewPager.setCurrentItem(0);
                        dotscount = BurgmanStreet.length;
                        setUpViewPager(BurgmanStreet, 0);
                        viewPager.setVisibility(View.VISIBLE);
                        mLinearLayoutViewPager_layout.setVisibility(View.VISIBLE);
                    }
                    break;

                case 4:
                    if (vehicleType.contentEquals("Burgman Street EX")) {
                        viewPagerAdapter = new ProfileVehicleAdapter(getApplicationContext(), Burgman_Street_EX, BurgmanStreetEX_color);


                        viewPager.setAdapter(viewPagerAdapter);
                        viewPager.setCurrentItem(0);
                        dotscount = Burgman_Street_EX.length;
                        setUpViewPager(Burgman_Street_EX, 0);
                        viewPager.setVisibility(View.VISIBLE);
                        mLinearLayoutViewPager_layout.setVisibility(View.VISIBLE);
                    }
                    break;
            }
        }

//       else if (classSpinner.getSelectedItem().toString() == "Motorcycle") {
//            vehicleType = divSpinner.getSelectedItem().toString();
//
//            switch (position) {
//
//                case 0:
//                    viewPager.setVisibility(View.INVISIBLE);
//                    mLinearLayoutViewPager_layout.setVisibility(View.INVISIBLE);
//                    break;
////                case 0:
////                    if (vehicleType.contentEquals("Choose Your Ride Model")) {
////
////                        //viewPagerAdapter = new ProfileVehicleAdapter(getApplicationContext(), V_STORM_SX);
////
////                        //viewPager.setAdapter(viewPagerAdapter);
////                        //dotscount = V_STORM_SX.length;
////                        //viewPager.setCurrentItem(0);
////                        //setUpViewPager(gixxerimages, 0);
////                        mLinearLayfoutViewPager_layout.setVisibility(View.INVISIBLE);
////                    }
////                    //new Common(CreateProfileActivity.this).showToast("Choose Your Ride Model", TOAST_DURATION);
////
////                    break;
//
//                case 1:
//                    if (vehicleType.contentEquals("V-STROM SX")) {
//                        viewPagerAdapter = new ProfileVehicleAdapter(getApplicationContext(), V_STORM_SX, V_STROM_SX_COLOR);
//                        viewPager.setAdapter(viewPagerAdapter);
//                        dotscount = V_STORM_SX.length;
//                        viewPager.setCurrentItem(0);
//                        setUpViewPager(V_STORM_SX, 0);
//                        viewPager.setVisibility(View.VISIBLE);
//                        mLinearLayoutViewPager_layout.setVisibility(View.VISIBLE);
//                    }
//                    break;
//
//                case 2:
//                    if (vehicleType.contentEquals("GIXXER / GIXXER SF")) {
//                        viewPagerAdapter = new ProfileVehicleAdapter(getApplicationContext(), gixxerSFimages,gixxerSfcolor);
//                        viewPager.setAdapter(viewPagerAdapter);
//                        viewPager.setCurrentItem(0);
//                        dotscount = gixxerSFimages.length;
//                        setUpViewPager(gixxerSFimages, 0);
//                        viewPager.setVisibility(View.VISIBLE);
//                        mLinearLayoutViewPager_layout.setVisibility(View.VISIBLE);
//                    }
//                    break;
//                case 3:
//                    if (vehicleType.contentEquals("GIXXER 250 / GIXXER 250 SF")) {
//
//                        viewPagerAdapter = new ProfileVehicleAdapter(getApplicationContext(), gixxer250images,gixxer250color);
//                        viewPager.setAdapter(viewPagerAdapter);
//                        dotscount = gixxer250images.length;
//                        viewPager.setCurrentItem(0);
//                        setUpViewPager(gixxer250images, 0);
//                        viewPager.setVisibility(View.VISIBLE);
//                        mLinearLayoutViewPager_layout.setVisibility(View.VISIBLE);
//                    }
//                    break;
//
////                case 5:
////                    if (vehicleType.contentEquals("Intruder")) {
////                        viewPagerAdapter = new ProfileVehicleAdapter(getApplicationContext(), intruder);
////
////
////                        viewPager.setAdapter(viewPagerAdapter);
////                        viewPager.setCurrentItem(0);
////                        dotscount = intruder.length;
////                        setUpViewPager(intruder, 0);
////                        viewPager.setVisibility(View.VISIBLE);
////                        mLinearLayoutViewPager_layout.setVisibility(View.VISIBLE);
////                    }
////                    break;
//            }
//
//        }

         if (classSpinner.getSelectedItem().toString() == "Motorcycle") {
            vehicleType = divSpinner.getSelectedItem().toString();
            Log.d("BikeType", "----" + vehicleType);
            switch (position) {
                case 0:
                    viewPager.setVisibility(View.INVISIBLE);
                    mLinearLayoutViewPager_layout.setVisibility(View.INVISIBLE);
                    break;

                case 1:
                    if (vehicleType.contentEquals("V-STROM SX")) {

                        viewPagerAdapter = new ProfileVehicleAdapter(getApplicationContext(), V_STORM_SX, V_STROM_SX_COLOR);
                        viewPager.setAdapter(viewPagerAdapter);
                        dotscount = V_STORM_SX.length;
                        viewPager.setCurrentItem(0);
                        setUpViewPager(V_STORM_SX, 0);
                        viewPager.setVisibility(View.VISIBLE);
                        mLinearLayoutViewPager_layout.setVisibility(View.VISIBLE);
                    }
                    break;

                case 2:
                    if (vehicleType.contentEquals("GIXXER / GIXXER SF")) {

                        viewPagerAdapter = new ProfileVehicleAdapter(getApplicationContext(), gixxerSFimages, gixxerSfcolor);
                        viewPager.setAdapter(viewPagerAdapter);
                        viewPager.setCurrentItem(0);
                        dotscount = gixxerSFimages.length;
                        setUpViewPager(gixxerSFimages, 0);
                        viewPager.setVisibility(View.VISIBLE);
                        viewPagerAdapter.notifyDataSetChanged();
                        mLinearLayoutViewPager_layout.setVisibility(View.VISIBLE);
                    }
                    break;

                case 3:
                    if (vehicleType.contentEquals("GIXXER 250 / GIXXER SF 250")) {
                        viewPagerAdapter = new ProfileVehicleAdapter(getApplicationContext(), gixxer250images,gixxer250color);
                        viewPager.setAdapter(viewPagerAdapter);
                        dotscount = gixxer250images.length;
                        viewPager.setCurrentItem(0);
                        setUpViewPager(gixxer250images, 0);
                        viewPager.setVisibility(View.VISIBLE);
                        viewPagerAdapter.notifyDataSetChanged();
                        mLinearLayoutViewPager_layout.setVisibility(View.VISIBLE);
                    }
                    break;

            }
        }


    }


    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        typeArray.add("Choose Your Ride Type");
        modelArray.add("Choose Your Ride Model");
        mLinearLayoutViewPager_layout.setVisibility(View.INVISIBLE);
    }

    private void nothingSelected() {
        modelArray.clear();
        modelArray.add("Choose Your Ride Model");
    }

    private void bikeModel() {
        modelArray.clear();
        modelArray.add("Choose Your Ride Model");
        modelArray.add("V-STROM SX");
        modelArray.add("GIXXER / GIXXER SF");
        modelArray.add("GIXXER 250 / GIXXER SF 250");

    }

    private void scooterModel() {
        modelArray.clear();
        modelArray.add("Choose Your Ride Model");

        modelArray.add("Avenis");
        modelArray.add("Access 125");
        modelArray.add("Burgman Street");
        modelArray.add("Burgman Street EX");
    }

    public void addLastParkedLatLong() {

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        String provider;

        Criteria criteria = new Criteria();

        // Getting the name of the provider that meets the criteria

        provider = locationManager.getBestProvider(criteria, false);

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(provider);

        if (location != null) {
            lat = location.getLatitude();
            lng = location.getLongitude();
        }

        Realm realm = Realm.getDefaultInstance();

        try {
            realm.executeTransaction(realm1 -> {

                LastParkedLocationRealmModule lastParkedLocationRealmModule = realm1.where(LastParkedLocationRealmModule.class).equalTo("id", 1).findFirst();

                if (lastParkedLocationRealmModule == null) {

                    lastParkedLocationRealmModule = realm1.createObject(LastParkedLocationRealmModule.class, 1);
                    lastParkedLocationRealmModule.setLat(lat);
                    lastParkedLocationRealmModule.setLng(lng);

                    realm1.insertOrUpdate(lastParkedLocationRealmModule);
                } else if (lastParkedLocationRealmModule != null) {
                    lastParkedLocationRealmModule.setLat(lat);
                    lastParkedLocationRealmModule.setLng(lng);

                    realm1.insertOrUpdate(lastParkedLocationRealmModule);
                }
            });

        } catch (Exception e) {
            Log.e(EXCEPTION,"Createprofile: addlocation: "+String.valueOf(e));
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case   PERMISSION_REQUEST_COARSE_LOCATION:
            {
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    /*........... get current loc.............*/
                    initLocationManager();
                }

            }

            case PERMISSION_REQUEST_BLUETOOTH_SCAN: {

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){

                    if (etUserName.getText().toString().trim().equals("") || etUserName.getText().toString().equals(" ")) {
                        new Common(CreateProfileActivity.this).showToast("Enter Rider's Name", TOAST_DURATION);
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(etUserName.getWindowToken(), 0);
                    } else if (etLocation.getText().toString().trim().equals("") || etLocation.getText().toString().equals(" ")) {
                        new Common(CreateProfileActivity.this).showToast("Enter Location", TOAST_DURATION);

                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(etLocation.getWindowToken(), 0);
                        imm.hideSoftInputFromWindow(etUserName.getWindowToken(), 0);

                    } else if (classSpinner.getSelectedItem().toString().equals("Choose Your Ride Type")) {
                        new Common(CreateProfileActivity.this).showToast("Select ride type", TOAST_DURATION);
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(etLocation.getWindowToken(), 0);
                        imm.hideSoftInputFromWindow(etUserName.getWindowToken(), 0);
                    } else if (divSpinner.getSelectedItem().toString().equals("Choose Your Ride Model")) {
                        new Common(CreateProfileActivity.this).showToast("Select model type", TOAST_DURATION);
                    } else if (!privacyCheckBox.isChecked()) {
                        new Common(CreateProfileActivity.this).showToast("Please accept the Terms &" +
                                "Conditions and Privacy Policy to proceed further.", 2500);
                    } else {
                        userName = etUserName.getText().toString();
                        location = etLocation.getText().toString();
                        vehicleType = classSpinner.getSelectedItem().toString();
                        bikeType = divSpinner.getSelectedItem().toString();
                        classid = classSpinner.getSelectedItemPosition();
                        divid = divSpinner.getSelectedItemPosition();

                        new Common(this).update_vehicle_data(vehicleType, bikeType, viewPager.getCurrentItem());
                        new Common(this).update_user_data(userName, location);
                        //deleteRecord();
                        //addRecord();
                        //updateRecord();

                        startActivity(new Intent(getApplicationContext(), HomeScreenActivity.class));
                        finish();
                    }
                }
                else{
                    startActivity(
                            new Intent(
                                    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", getPackageName(), null)
                            )
                    );

                }
            }

            case PERMISSION_REQUEST_BLUETOOTH_CONNECT:
            {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if (etUserName.getText().toString().trim().equals("") || etUserName.getText().toString().equals(" ")) {
                        new Common(CreateProfileActivity.this).showToast("Enter Rider's Name", TOAST_DURATION);
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(etUserName.getWindowToken(), 0);
                    } else if (etLocation.getText().toString().trim().equals("") || etLocation.getText().toString().equals(" ")) {
                        new Common(CreateProfileActivity.this).showToast("Enter Location", TOAST_DURATION);

                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(etLocation.getWindowToken(), 0);
                        imm.hideSoftInputFromWindow(etUserName.getWindowToken(), 0);

                    } else if (classSpinner.getSelectedItem().toString().equals("Choose Your Ride Type")) {
                        new Common(CreateProfileActivity.this).showToast("Select ride type", TOAST_DURATION);
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(etLocation.getWindowToken(), 0);
                        imm.hideSoftInputFromWindow(etUserName.getWindowToken(), 0);
                    } else if (divSpinner.getSelectedItem().toString().equals("Choose Your Ride Model")) {
                        new Common(CreateProfileActivity.this).showToast("Select model type", TOAST_DURATION);
                    } else if (!privacyCheckBox.isChecked()) {
                        new Common(CreateProfileActivity.this).showToast("Please accept the Terms &" +
                                "Conditions and Privacy Policy to proceed further.", 2500);
                    } else {
                        userName = etUserName.getText().toString();
                        location = etLocation.getText().toString();
                        vehicleType = classSpinner.getSelectedItem().toString();
                        bikeType = divSpinner.getSelectedItem().toString();
                        classid = classSpinner.getSelectedItemPosition();
                        divid = divSpinner.getSelectedItemPosition();

                        new Common(this).update_vehicle_data(vehicleType, bikeType, viewPager.getCurrentItem());
                        new Common(this).update_user_data(userName, location);
                        //deleteRecord();
                        //addRecord();
                        //updateRecord();

                        startActivity(new Intent(getApplicationContext(), HomeScreenActivity.class));
                        finish();
                    }
                }
                else{
                    startActivity(
                            new Intent(
                                    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", getPackageName(), null)
                            )
                    );

                }
            }
        }

    }


    private boolean check_permission() {
        String permission = Manifest.permission.ACCESS_FINE_LOCATION;
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    private void getCurrentPlace() {

        if(!check_permission()) showExitAlert("Application requires location access permission to auto-detect your current city.\n" +
                "Please allow location permission on the next screen or deny to enter your city manually.");

        Location location;
        if (currentLocation != null) location = currentLocation;
        else location = new CurrentLoc().getCurrentLoc(this);

        if (location == null) {
            new Common(this).showToast("Not able to fetch location", TOAST_DURATION);
            return;
        }

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        getPlaceName(latitude, longitude);
    }

    private void getPlaceName(double latitude, double longitude) {
        if (latitude <= 0 || longitude <= 0) {
            new Common(this).showToast("Invalid Location", TOAST_DURATION);
            return;
        }

        MapplsReverseGeoCode mapplsReverseGeoCode = MapplsReverseGeoCode.builder()
                .setLocation(latitude,longitude)
                .build();
        MapplsReverseGeoCodeManager.newInstance(mapplsReverseGeoCode).call(new OnResponseCallback<PlaceResponse>() {
            @Override
            public void onSuccess(PlaceResponse response) {
                //Handle Response
                if (response== null) {
                    new Common(CreateProfileActivity.this).showToast("Not able to fetch location", TOAST_DURATION);
                    return;
                }

                if (response.getPlaces().size() < 1) {
                    new Common(CreateProfileActivity.this).showToast("Not able to fetch location", TOAST_DURATION);

                    return;
                }

                String place = response.getPlaces().get(0).getCity();
                String locality = response.getPlaces().get(0).getLocality();
                if (place.length() < 2) place = locality;

                etLocation.setText(place);
            }

            @Override
            public void onError(int code, String message) {
                //Handle Error
            }
        });


//        MapmyIndiaReverseGeoCode.builder()
//                .setLocation(latitude, longitude)
//                .build().enqueueCall(new Callback<PlaceResponse>() {
//                    @Override
//                    public void onResponse(@NotNull Call<PlaceResponse> call, @NotNull Response<PlaceResponse> response) {
//                        if (response.body() == null) {
//                            new Common(CreateProfileActivity.this).showToast("Not able to fetch location", TOAST_DURATION);
//                            return;
//                        }
//
//                        if (response.body().getPlaces().size() < 1) {
//                            new Common(CreateProfileActivity.this).showToast("Not able to fetch location", TOAST_DURATION);
//
//                            return;
//                        }
//
//                        String place = response.body().getPlaces().get(0).getCity();
//                        String locality = response.body().getPlaces().get(0).getLocality();
//                        if (place.length() < 2) place = locality;
//
//                        etLocation.setText(place);
//                    }
//
//                    @Override
//                    public void onFailure(@NotNull Call<PlaceResponse> call, @NotNull Throwable t) { }
//                });
    }

    @Override
    public void onLocationChanged(Location location) {

        if (location != null) currentLocation = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }

    @Override
    public void onProviderEnabled(String provider) { }

    @Override
    public void onProviderDisabled(String provider) { }
}