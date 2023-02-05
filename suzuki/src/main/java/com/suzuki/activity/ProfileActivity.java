package com.suzuki.activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.ViewPager;

import com.mappls.sdk.services.api.OnResponseCallback;
import com.mappls.sdk.services.api.PlaceResponse;
import com.mappls.sdk.services.api.reversegeocode.MapplsReverseGeoCode;
import com.mappls.sdk.services.api.reversegeocode.MapplsReverseGeoCodeManager;
import com.suzuki.R;
import com.suzuki.adapter.ProfileVehicleAdapter;
import com.suzuki.pojo.RiderProfileModule;
import com.suzuki.pojo.SettingsPojo;
import com.suzuki.utils.Common;
import com.suzuki.utils.CurrentLoc;

import io.realm.Realm;
import io.realm.RealmResults;

import static com.suzuki.activity.CreateProfileActivity.Avenis;
import static com.suzuki.activity.CreateProfileActivity.BurgmanStreet;
import static com.suzuki.activity.CreateProfileActivity.BurgmanStreetEX_color;
import static com.suzuki.activity.CreateProfileActivity.BurgmanStreet_color;
import static com.suzuki.activity.CreateProfileActivity.Burgman_Street_EX;
import static com.suzuki.activity.CreateProfileActivity.V_STORM_SX;
import static com.suzuki.activity.CreateProfileActivity.V_STROM_SX_COLOR;
import static com.suzuki.activity.CreateProfileActivity.accessSE125;

import static com.suzuki.activity.CreateProfileActivity.accessSE125color;
import static com.suzuki.activity.CreateProfileActivity.avenis_color;
import static com.suzuki.activity.CreateProfileActivity.gixxer250images;
import static com.suzuki.activity.CreateProfileActivity.gixxerSFimages;
import static com.suzuki.activity.HomeScreenActivity.TOAST_DURATION;
import static com.suzuki.fragment.DashboardFragment.flag;
import static com.suzuki.fragment.DashboardFragment.staticConnectionStatus;
import static com.suzuki.utils.Common.EXCEPTION;

public class ProfileActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Spinner vehicleTypeSpinner, modelTypeSpinner;
    private ViewPager viewPager;
    private ImageView[] dots;
    private EditText etUserName, etLocation, riderName, riderLocation;
    private LinearLayout ivAdd, imArrow, sliderDotspanel;
    private Realm realm;
    private String UserName, Location, vehicleType, bikeType, vehicleModel, changeinBIKEMODEL;
    static int classid, divid, dotscount;
    private ArrayAdapter<String> typeArray, modelArray;
    private RealmResults<RiderProfileModule> riderProfileModules;
    private ProfileVehicleAdapter viewPagerAdapter;
    private ConstraintLayout mLinearLayout_profile_viewpager;
    ImageView currentLocIV;
    private SharedPreferences sharedPreferences;
    public static String[] avenis_color;
    public  static String[]V_STROM_SX_COLOR;
    public static String[] BurgmanStreet_color;
    public static String[] BurgmanStreetEX_color;
    public static String[]accessSE125color;
    public static String[]gixxerSfcolor;
    public static String[]gixxer250color;
//    private SharedPreferences.Editor editor;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.profile_activity_scroll_test);
        vehicleTypeSpinner = (Spinner) findViewById(R.id.classSpinner);
        etUserName = (EditText) findViewById(R.id.etUserName);
        etLocation = (EditText) findViewById(R.id.etLocation);
        ivAdd = (LinearLayout) findViewById(R.id.llEditProfileSave);
        imArrow = (LinearLayout) findViewById(R.id.llEditProfileBack);
        riderName = (EditText) findViewById(R.id.etUserName);
        riderLocation = (EditText) findViewById(R.id.etLocation);
        modelTypeSpinner = (Spinner) findViewById(R.id.divSpinner);
        mLinearLayout_profile_viewpager = findViewById(R.id.profile_viewpager);
        currentLocIV = findViewById(R.id.currentLocIv);
        V_STROM_SX_COLOR=new String[]{"Pearl Blaze Orange","Champion Yellow","Glass Sparkle Black"};
        BurgmanStreet_color= new String[]{"Glossy Grey","Black","Blue","Bordeaux Red","Pearl Mirage White"};
        avenis_color=new String[]{"Triton Blue","Lush Green","Black","Pearl Blaze Orange","Pearl Mirage White" };
        BurgmanStreetEX_color= new String[]{"Black","Platinum Silver","Royal Bronze"};
        accessSE125color=new String[]{"Black","Dual Tone - Solid Ice Green + Pearl Mirage White","Blue","Royal Bronze","Pearl Mirage White","Glossy Grey"};
       // etUserName.setFocusable(true);
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

        //etUserName.requestFocus();

        /*........... get current loc.............*/

        currentLocIV.setOnClickListener(v -> getCurrentPlace());

        etUserName.setFilters(new InputFilter[]{new InputFilter.AllCaps(), new InputFilter.LengthFilter(20)});

//-------------Profile Activity-------------------
        vehicleTypeSpinner.setOnTouchListener((v, event) -> {

            etLocation.clearFocus();
            etUserName.clearFocus();

            InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(etLocation.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(etUserName.getWindowToken(), 0);
            return false;
        });

        realm = Realm.getDefaultInstance();


        modelTypeSpinner.setOnTouchListener((v, event) -> {

            etLocation.clearFocus();
            etUserName.clearFocus();
            etUserName.setCursorVisible(false);
            etLocation.setCursorVisible(false);

            InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(etLocation.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(etUserName.getWindowToken(), 0);
            return false;
        });

//-----------------------Bike----------------------------------------
        typeArray = new ArrayAdapter<>(getApplicationContext(), R.layout.spinner_item);
        typeArray.setDropDownViewResource(R.layout.spinner_dropdown);

        typeArray.add("Choose Your Ride Type");

        typeArray.add("Scooter");
        typeArray.add("Motorcycle");
        vehicleTypeSpinner.setAdapter(typeArray);

        modelArray = new ArrayAdapter<>(this, R.layout.spinner_item);
        modelArray.setDropDownViewResource(R.layout.spinner_dropdown);

        modelTypeSpinner.setAdapter(modelArray);

        ivAdd.setOnClickListener(v -> {
            if (etUserName.getText().toString().trim().equals("") || etUserName.getText().toString().equals(" ")) {
                Toast.makeText(ProfileActivity.this, "Enter Rider's Name", Toast.LENGTH_LONG).show();

            } else if (etLocation.getText().toString().trim().equals("") || etLocation.getText().toString().equals(" ")) {
                Toast.makeText(ProfileActivity.this, "Enter Location", Toast.LENGTH_LONG).show();

            } else if (vehicleTypeSpinner.getSelectedItem().toString().equals("Choose Your Ride Type")) {
                Toast.makeText(ProfileActivity.this, "Select ride type", Toast.LENGTH_LONG).show();

            } else if (modelTypeSpinner.getSelectedItem().toString().equals("Choose Your Ride Model")) {
                Toast.makeText(ProfileActivity.this, "Select model type", Toast.LENGTH_LONG).show();

            } else {
                UserName = etUserName.getText().toString();
                Location = etLocation.getText().toString();
                vehicleType = vehicleTypeSpinner.getSelectedItem().toString();
                bikeType = modelTypeSpinner.getSelectedItem().toString();
                classid = vehicleTypeSpinner.getSelectedItemPosition();
                divid = modelTypeSpinner.getSelectedItemPosition();

                new Common(this).update_vehicle_data(vehicleType, bikeType, viewPager.getCurrentItem());
                new Common(this).update_user_data(UserName, Location);

                try {
                    if (!changeinBIKEMODEL.contentEquals(vehicleType)) {
                        Realm realm = Realm.getDefaultInstance();
                        try {
                            realm.executeTransaction(realm1 -> {

                                SettingsPojo settingsPojo = realm1.where(SettingsPojo.class).equalTo("id", 1).findFirst();

                                if (settingsPojo == null) {

                                    settingsPojo = realm1.createObject(SettingsPojo.class, 1);
                                    settingsPojo.setSpeedSet(false);

                                    realm1.insertOrUpdate(settingsPojo);
                                } else if (settingsPojo != null) {
                                    settingsPojo.setSpeedSet(false);

                                    realm1.insertOrUpdate(settingsPojo);
                                }
                            });

                        } catch (Exception e) {
                            Log.d("realmex", "--" + e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    Log.e(EXCEPTION,"Profile: oncreate: "+String.valueOf(e));
                }

                addRecord();
                updateRecord();
                finish();
            }
            flag = false;
        });

        viewPager = (ViewPager) findViewById(R.id.viewPager);

        sliderDotspanel = (LinearLayout) findViewById(R.id.SliderDots);

//        viewPagerAdapter = new ProfileVehicleAdapter(getApplicationContext(), V_STORM_SX);

        viewPager.setAdapter(viewPagerAdapter);

        imArrow.setOnClickListener(v -> finish());

//        viewRecord();
        update_profile_view();

        vehicleTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (vehicleTypeSpinner.getSelectedItem().toString() == "Choose Your Ride Type") {

                    modelTypeSpinner.setEnabled(false);
                    modelTypeSpinner.setClickable(false);

                }
                else{
                    modelTypeSpinner.setEnabled(true);
                    modelTypeSpinner.setClickable(true);
                }
                switch (position)  {
                    case 0:
                        nothingSelected();

                        mLinearLayout_profile_viewpager.setVisibility(View.INVISIBLE);
                        break;

                    case 1:
                        modelArray.clear();
                        scooterModel();
                        modelTypeSpinner.setSelection(0);
                        mLinearLayout_profile_viewpager.setVisibility(View.INVISIBLE);
                        viewPagerAdapter.notifyDataSetChanged();
                        viewPagerAdapter = new ProfileVehicleAdapter(getApplicationContext(), Avenis, avenis_color);
                        viewPager.setAdapter(viewPagerAdapter);
                        viewPager.setCurrentItem(0);
                        dotscount = Avenis.length;
                        setUpViewPager(Avenis, 0);
                        break;

                    case 2:
                        modelArray.clear();
                        bikeModel();
                        modelTypeSpinner.setSelection(0);
                        mLinearLayout_profile_viewpager.setVisibility(View.INVISIBLE);
                        viewPagerAdapter.notifyDataSetChanged();

                        viewPagerAdapter = new ProfileVehicleAdapter(getApplicationContext(), V_STORM_SX, V_STROM_SX_COLOR);
                        viewPager.setAdapter(viewPagerAdapter);
                        dotscount = V_STORM_SX.length;
                        viewPager.setCurrentItem(0);
                        setUpViewPager(V_STORM_SX, 0);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        modelTypeSpinner.setOnItemSelectedListener(this);

        if(staticConnectionStatus) vehicleTypeSpinner.setEnabled(false);
    }

    private void update_profile_view() {
        sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE);

        riderName.setText(sharedPreferences.getString("name",""));
        riderLocation.setText(sharedPreferences.getString("location",""));

        sharedPreferences = getSharedPreferences("vehicle_data", MODE_PRIVATE);
        String type= sharedPreferences.getString("vehicle_type","");
        String model = sharedPreferences.getString("vehicle_name","");
        int variant = sharedPreferences.getInt("vehicle_model",0);

        if (type.equals("Scooter")){
            vehicleTypeSpinner.setSelection(1, true);
            scooterModel();
            switch (model) {
                case "Avenis":
                    modelTypeSpinner.setSelection(1, true);
                    viewPagerAdapter = new ProfileVehicleAdapter(getApplicationContext(), Avenis, avenis_color);
                    viewPager.setAdapter(viewPagerAdapter);
                    viewPager.setCurrentItem(variant);
                    setUpViewPager(Avenis, variant);
                    break;

                case "Access 125":
                    modelTypeSpinner.setSelection(2, true);
                    viewPagerAdapter = new ProfileVehicleAdapter(getApplicationContext(), accessSE125, accessSE125color);
                    viewPager.setAdapter(viewPagerAdapter);
                    viewPager.setCurrentItem(variant);
                    setUpViewPager(accessSE125, variant);
                    break;

                case "Burgman Street":
                    modelTypeSpinner.setSelection(3, true);
                    viewPagerAdapter = new ProfileVehicleAdapter(getApplicationContext(), BurgmanStreet, BurgmanStreet_color);
                    viewPager.setAdapter(viewPagerAdapter);
                    viewPager.setCurrentItem(variant);
                    setUpViewPager(BurgmanStreet, variant);
                    break;

                case "Burgman Street EX":
                    modelTypeSpinner.setSelection(4, true);
                    viewPagerAdapter = new ProfileVehicleAdapter(getApplicationContext(), Burgman_Street_EX, BurgmanStreetEX_color);
                    viewPager.setAdapter(viewPagerAdapter);
                    viewPager.setCurrentItem(variant);
                    setUpViewPager(Burgman_Street_EX, variant);
                    break;
            }
           // scooterModel();

      //      viewPager.setAdapter(viewPagerAdapter);
       //     viewPager.setCurrentItem(variant);



        }


        else if (type.equals("Motorcycle")){
            vehicleTypeSpinner.setSelection(2, true);
            bikeModel();
            switch (model) {
                case "V-STROM SX":
               // vehicleTypeSpinner.setSelection(2, true);
               // bikeModel();
                modelTypeSpinner.setSelection(1, true);
                viewPagerAdapter = new ProfileVehicleAdapter(getApplicationContext(), V_STORM_SX, V_STROM_SX_COLOR);
                viewPager.setAdapter(viewPagerAdapter);
                viewPager.setCurrentItem(variant);
                setUpViewPager(V_STORM_SX, variant);
                viewPagerAdapter.notifyDataSetChanged();
                break;


                case "GIXXER / GIXXER SF":
                    // vehicleTypeSpinner.setSelection(2, true);
                    // bikeModel();
                    modelTypeSpinner.setSelection(2, true);
                    viewPagerAdapter = new ProfileVehicleAdapter(getApplicationContext(), gixxerSFimages, gixxerSfcolor);

                    viewPager.setAdapter(viewPagerAdapter);
                    viewPager.setCurrentItem(variant);
                    setUpViewPager(gixxerSFimages, variant);
                    viewPagerAdapter.notifyDataSetChanged();

                    break;


                case "GIXXER 250 / GIXXER SF 250":
                    // vehicleTypeSpinner.setSelection(2, true);
                    // bikeModel();
                    modelTypeSpinner.setSelection(3, true);
                    viewPagerAdapter = new ProfileVehicleAdapter(getApplicationContext(), gixxer250images, gixxer250color);

                    viewPager.setAdapter(viewPagerAdapter);
                    viewPager.setCurrentItem(variant);
                    setUpViewPager(gixxer250images, variant);
                    viewPagerAdapter.notifyDataSetChanged();

                    break;
            }
        }

        switch (model){
            case "Avenis":
                modelTypeSpinner.setSelection(1, true);

                //ivUserBike.setImageResource(Avenis[variant]);
                break;

            case "Access 125":
                modelTypeSpinner.setSelection(2, true);
                //ivUserBike.setImageResource(accessSE125[variant]);
                break;

            case "Burgman Street":
                modelTypeSpinner.setSelection(3, true);
                //ivUserBike.setImageResource(BurgmanStreet[variant]);
                break;

            case "Burgman Street EX":
                modelTypeSpinner.setSelection(4, true);
                //ivUserBike.setImageResource(BurgmanStreet[variant]);
                break;

            case "V-STROM SX":
                modelTypeSpinner.setSelection(1, true);
                //ivUserBike.setImageResource(V_STORM_SX[variant]);
                break;

            case "GIXXER / GIXXER SFX":
                modelTypeSpinner.setSelection(2, true);
                //ivUserBike.setImageResource(V_STORM_SX[variant]);
                break;

            case "GIXXER 250 / GIXXER SF 250":
                modelTypeSpinner.setSelection(3, true);
                //ivUserBike.setImageResource(V_STORM_SX[variant]);
                break;
        }
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
            //ivArrayDotsPager[i].setAlpha(0.4f);
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

    public void onResume() {
        super.onResume();
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
                    riderProfile.setBikeModel(bikeType);
                    riderProfile.setUserSelectedImage(viewPager.getCurrentItem());

                    realm1.insertOrUpdate(riderProfile);
                } else if (riderProfile != null) {
                    riderProfile.setName(etUserName.getText().toString());
                    riderProfile.setLocation(etLocation.getText().toString());
                    riderProfile.setBike(vehicleType);
                    riderProfile.setBikeModel(bikeType);
                    riderProfile.setUserSelectedImage(viewPager.getCurrentItem());
                    realm1.insertOrUpdate(riderProfile);
                }
            });

        } catch (Exception e) {
            Log.e(EXCEPTION,"Profile: addrecord: "+String.valueOf(e));
        }
    }

    public void updateRecord() {

        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(new Realm.Transaction() {

                @Override
                public void execute(Realm realm) {

                    RiderProfileModule riderProfile = realm.where(RiderProfileModule.class).equalTo("id", 1).findFirst();

                    if (riderProfile == null) {

                        riderProfile = realm.createObject(RiderProfileModule.class, 1);
                        riderProfile.setName(etUserName.getText().toString());
                        riderProfile.setLocation(etLocation.getText().toString());

                        realm.insertOrUpdate(riderProfile);
                    } else if (riderProfile != null) {
                        riderProfile.setName(etUserName.getText().toString());
                        riderProfile.setLocation(etLocation.getText().toString());

                        realm.insertOrUpdate(riderProfile);
                    }
                }
            });

        } catch (Exception e) {
            Log.e(EXCEPTION,"Profile: updaterecord: "+String.valueOf(e));
        }
    }

    /*public void deleteRecord() {
        RealmResults<RiderProfileModule> results = realm.where(RiderProfileModule.class).limit(1).findAll();
        realm.beginTransaction();

        results.deleteAllFromRealm();
        realm.commitTransaction();
    }*/


    /*public void viewRecord() {
        riderProfileModules = realm.where(RiderProfileModule.class).findAll();

        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(realm1 -> {

                RiderProfileModule riderProfile = realm1.where(RiderProfileModule.class).equalTo("id", 1).findFirst();

                if (riderProfile != null) {

                    riderName.setText(riderProfile.getName());
                    riderLocation.setText(riderProfile.getLocation());
                    vehicleModel = riderProfile.getBikeModel();
                    changeinBIKEMODEL = riderProfile.getBike();

                    if (riderProfile.getBike().contentEquals("Motorcycle")) {
                        vehicleTypeSpinner.setSelection(2, true);
                        bikeModel();

                    } else if (riderProfile.getBike().contentEquals("Scooter")) {
                        vehicleTypeSpinner.setSelection(1, true);
                        scooterModel();
                    }

                    checkBikeData(riderProfile.getUserSelectedImage(), riderProfile.getBikeModel());
                }
            });
        } catch (Exception e) {
            Log.e(EXCEPTION,"Profile: viewRecord: "+String.valueOf(e));
        }
    }*/

    /*private void checkBikeData(int userSelectedImage, String type) {

        modelTypeSpinner.setSelection(modelArray.getPosition(type), true);

        if (type.contentEquals("V-STORM SX")) {

            bikeModel();
            viewPagerAdapter = new ProfileVehicleAdapter(getApplicationContext(), V_STORM_SX);

            viewPager.setAdapter(viewPagerAdapter);

            setUpViewPager(V_STORM_SX, userSelectedImage);
            viewPager.setCurrentItem(userSelectedImage);

        }else if (type.contentEquals("Avenis")) {

            scooterModel();
            viewPagerAdapter = new ProfileVehicleAdapter(getApplicationContext(), Avenis);

            viewPager.setAdapter(viewPagerAdapter);

            viewPager.setCurrentItem(userSelectedImage);

            setUpViewPager(Avenis, userSelectedImage);

        } else if (type.contentEquals("Access 125 SE")) {

            scooterModel();
            viewPagerAdapter = new ProfileVehicleAdapter(getApplicationContext(), accessSE125);

            viewPager.setAdapter(viewPagerAdapter);

            viewPager.setCurrentItem(userSelectedImage);
            setUpViewPager(accessSE125, userSelectedImage);

        } else if (type.contentEquals("Burgman Street")) {

            scooterModel();
            viewPagerAdapter = new ProfileVehicleAdapter(getApplicationContext(), BurgmanStreet);

            viewPager.setAdapter(viewPagerAdapter);

            viewPager.setCurrentItem(userSelectedImage);
            setUpViewPager(BurgmanStreet, userSelectedImage);
        }
    }*/

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {

        if (vehicleTypeSpinner.getSelectedItem().toString() == "Motorcycle") {
            vehicleType = modelTypeSpinner.getSelectedItem().toString();

            switch (position) {
                case 0:
                    modelTypeSpinner.setSelection(0);
                    mLinearLayout_profile_viewpager.setVisibility(View.INVISIBLE);
                    break;

                case 1:
                    if (vehicleType.contentEquals("V-STROM SX")) {
                        viewPagerAdapter = new ProfileVehicleAdapter(getApplicationContext(), V_STORM_SX, V_STROM_SX_COLOR);
                        mLinearLayout_profile_viewpager.setVisibility(View.VISIBLE);
                        viewPager.setAdapter(viewPagerAdapter);
                        dotscount = V_STORM_SX.length;
                        viewPager.setCurrentItem(0);
                        setUpViewPager(V_STORM_SX, 0);
                        viewPagerAdapter.notifyDataSetChanged();

                    }
                    break;

                case 2:
                    if (vehicleType.contentEquals("GIXXER / GIXXER SF")) {

                        viewPagerAdapter = new ProfileVehicleAdapter(getApplicationContext(), gixxerSFimages, gixxerSfcolor);
                        mLinearLayout_profile_viewpager.setVisibility(View.VISIBLE);
                        viewPager.setAdapter(viewPagerAdapter);
                        viewPager.setCurrentItem(0);
                        dotscount = gixxerSFimages.length;
                        setUpViewPager(gixxerSFimages, 0);
                       // viewPager.setVisibility(View.VISIBLE);
                        viewPagerAdapter.notifyDataSetChanged();
                    }
                    break;

                case 3:
                    if (vehicleType.contentEquals("GIXXER 250 / GIXXER SF 250")) {
                        viewPagerAdapter = new ProfileVehicleAdapter(getApplicationContext(), gixxer250images,gixxer250color);
                        mLinearLayout_profile_viewpager.setVisibility(View.VISIBLE);
                        viewPager.setAdapter(viewPagerAdapter);
                        dotscount = gixxer250images.length;
                        viewPager.setCurrentItem(0);
                        setUpViewPager(gixxer250images, 0);
                       // viewPager.setVisibility(View.VISIBLE);
                        viewPagerAdapter.notifyDataSetChanged();

                    }
                    break;

                /*case 2:
                    if (vehicleType.contentEquals("Gixxer 250")) {
                        mLinearLayout_profile_viewpager.setVisibility(View.VISIBLE);
                        viewPagerAdapter = new ProfileVehicleAdapter(getApplicationContext(), gixxer250images);
                        viewPager.setAdapter(viewPagerAdapter);
                        dotscount = gixxer250images.length;
                        viewPager.setCurrentItem(0);
                        setUpViewPager(gixxer250images, 0);
                    }
                    break;

                case 3:
                    if (vehicleType.contentEquals("Gixxer SF")) {
                        viewPagerAdapter = new ProfileVehicleAdapter(getApplicationContext(), gixxerSFimages);

                        mLinearLayout_profile_viewpager.setVisibility(View.VISIBLE);
                        viewPager.setAdapter(viewPagerAdapter);
                        viewPager.setCurrentItem(0);
                        dotscount = gixxerSFimages.length;
                        setUpViewPager(gixxerimages, 0);
                    }
                    break;

                case 4:
                    if (vehicleType.contentEquals("Gixxer SF 250")) {
                        mLinearLayout_profile_viewpager.setVisibility(View.VISIBLE);
                        viewPagerAdapter = new ProfileVehicleAdapter(getApplicationContext(), gixxerSF250images);
                        viewPager.setAdapter(viewPagerAdapter);
                        viewPager.setCurrentItem(0);
                        dotscount = gixxerSF250images.length;
                        setUpViewPager(gixxerSF250images, 0);
                    }
                    break;

                case 5:
                    if (vehicleType.contentEquals("Intruder")) {
                        viewPagerAdapter = new ProfileVehicleAdapter(getApplicationContext(), intruder);

                        mLinearLayout_profile_viewpager.setVisibility(View.VISIBLE);
                        viewPager.setAdapter(viewPagerAdapter);
                        viewPager.setCurrentItem(0);
                        dotscount = intruder.length;
                        setUpViewPager(intruder, 0);
                    }

                    break;*/
            }

        }

         if (vehicleTypeSpinner.getSelectedItem().toString() == "Scooter") {
            vehicleType = modelTypeSpinner.getSelectedItem().toString();

            switch (position) {
                case 0:
                    modelTypeSpinner.setSelection(0);
                    mLinearLayout_profile_viewpager.setVisibility(View.INVISIBLE);
                    break;

                case 1:
                    if (vehicleType.contentEquals("Avenis")) {
                        viewPagerAdapter = new ProfileVehicleAdapter(getApplicationContext(), Avenis, avenis_color);

                        mLinearLayout_profile_viewpager.setVisibility(View.VISIBLE);
                        viewPager.setAdapter(viewPagerAdapter);
                        viewPager.setCurrentItem(0);
                        dotscount = Avenis.length;
                        setUpViewPager(Avenis, 0);
                    }
                    break;

                case 2:
                    if (vehicleType.contentEquals("Access 125")) {
                        viewPagerAdapter = new ProfileVehicleAdapter(getApplicationContext(), accessSE125, accessSE125color);

                        mLinearLayout_profile_viewpager.setVisibility(View.VISIBLE);
                        viewPager.setAdapter(viewPagerAdapter);
                        viewPager.setCurrentItem(0);
                        dotscount = accessSE125.length;
                        Log.e("dotscount",String.valueOf(dotscount));
                        setUpViewPager(accessSE125, 0);
                    }
                    break;

                case 3:
                    if (vehicleType.contentEquals("Burgman Street")) {
                        viewPagerAdapter = new ProfileVehicleAdapter(getApplicationContext(), BurgmanStreet, BurgmanStreet_color);
                        mLinearLayout_profile_viewpager.setVisibility(View.VISIBLE);

                        viewPager.setAdapter(viewPagerAdapter);
                        viewPager.setCurrentItem(0);
                        dotscount = BurgmanStreet.length;
                        setUpViewPager(BurgmanStreet, 0);
                    }
                    break;

                case 4:
                    if (vehicleType.contentEquals("Burgman Street EX")) {
                        viewPagerAdapter = new ProfileVehicleAdapter(getApplicationContext(), Burgman_Street_EX, BurgmanStreetEX_color);
                        mLinearLayout_profile_viewpager.setVisibility(View.VISIBLE);

                        viewPager.setAdapter(viewPagerAdapter);
                        viewPager.setCurrentItem(0);
                        dotscount = Burgman_Street_EX.length;
                        setUpViewPager(Burgman_Street_EX, 0);
                    }
                    break;
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        typeArray.add("Choose Your Ride Type");
        modelArray.add("Choose Your Ride Model");
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

    private void getCurrentPlace() {
        Location location = new CurrentLoc().getCurrentLoc(this);

        if (location == null) {
            Toast.makeText(this, "Not able to fetch location", Toast.LENGTH_SHORT).show();
            return;
        }

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        getPlaceName(latitude, longitude);
    }

    private void getPlaceName(double latitude, double longitude) {
        if (latitude <= 0 || longitude <= 0){
            new Common(this).showToast("Invalid Location",TOAST_DURATION);
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
                    new Common(ProfileActivity.this).showToast("Not able to fetch location", TOAST_DURATION);
                    return;
                }

                if (response.getPlaces().size() < 1) {
                    new Common(ProfileActivity.this).showToast("Not able to fetch location", TOAST_DURATION);

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
//                            Toast.makeText(ProfileActivity.this, "Issue in fetching details",
//                                    Toast.LENGTH_SHORT).show();
//                            return;
//                        }
//
//                        if (response.body().getPlaces().size() < 1) {
//                            Toast.makeText(ProfileActivity.this, "Issue in fetching details", Toast.LENGTH_SHORT).show();
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
    protected void onPause() {
        super.onPause();
        hideKeyboard(this);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) view = new View(activity);

        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}