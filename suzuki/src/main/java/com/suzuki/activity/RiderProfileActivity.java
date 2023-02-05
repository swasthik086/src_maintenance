
package com.suzuki.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.suzuki.R;
import com.suzuki.pojo.ClusterStatusPktPojo;
import com.suzuki.pojo.RiderProfileModule;
import com.suzuki.widgets.CircleImageView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.annotation.Nullable;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

import static com.suzuki.activity.CreateProfileActivity.Avenis;
import static com.suzuki.activity.CreateProfileActivity.BurgmanStreet;
import static com.suzuki.activity.CreateProfileActivity.Burgman_Street_EX;
import static com.suzuki.activity.CreateProfileActivity.V_STORM_SX;
import static com.suzuki.activity.CreateProfileActivity.accessSE125;
import static com.suzuki.activity.CreateProfileActivity.gixxer250images;
import static com.suzuki.activity.CreateProfileActivity.gixxerSFimages;
import static com.suzuki.application.SuzukiApplication.calculateCheckSum;


//import static com.suzuki.activity.CreateProfileActivity.gsx;


public class RiderProfileActivity extends AppCompatActivity {

    LinearLayout riderBack;
    ImageView riderPencil;
    CircleImageView riderImage;
    //FrameLayout flRiderImage;
    Realm realm;
    TextView riderName;
    TextView riderLocation;
    TextView OdometerReading;
    //static String picturePath;
    TextView bikename, tvRide;
    public static Bitmap bitmap;
    //private static final String IMAGE_DIRECTORY = "/demonuts";
    private int GALLERY = 1, CAMERA = 2;
    public static final String MyPREFERENCES = "MyPreferences";
    public static final String key = "USER_IMAGE";
    public static SharedPreferences myPrefrence;
    RealmResults<RiderProfileModule> riderProfileModules;
    ImageView ivUserBike;
    public static final int REQUEST_IMAGE = 100;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.rider_profile_constraint_layout);

        riderName = (TextView) findViewById(R.id.tvRiderName);
        riderLocation = (TextView) findViewById(R.id.tvRiderLocation);
        tvRide = (TextView) findViewById(R.id.tvRide);
        bikename = (TextView) findViewById(R.id.tvBikeName);
        ivUserBike = (ImageView) findViewById(R.id.ivUserBike);

        Realm.init(this);
        realm = Realm.getDefaultInstance();

        riderBack = (LinearLayout) findViewById(R.id.llArrowLayout);
        riderBack.setOnClickListener(v -> finish());

        riderPencil = (ImageView) findViewById(R.id.ivriderEdit);

        riderPencil.setOnClickListener(v -> {

            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
        });

        riderImage = (CircleImageView) findViewById(R.id.img_profile);

        riderImage.setOnClickListener(view -> showImagePickerOptions());

        myPrefrence = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        String photo = myPrefrence.getString(key, "photo");

        if (!photo.equals("photo")) {
            bitmap = decodeBase64(photo);
            riderImage.setImageBitmap(bitmap);
        }
        OdometerReading = (TextView) findViewById(R.id.tvOdoRead);

        sharedPreferences = getSharedPreferences("vehicle_data",MODE_PRIVATE);

        OdometerReading.setText(sharedPreferences.getString("odometer","0 km"));
        tvRide.setText(sharedPreferences.getString("ride_count","0"));

        //viewRecord();
    }

    private void update_profile_view() {
        sharedPreferences = getSharedPreferences("user_data", MODE_PRIVATE);
        riderName.setText(sharedPreferences.getString("name",""));
        riderLocation.setText(sharedPreferences.getString("location",""));

        sharedPreferences = getSharedPreferences("vehicle_data", MODE_PRIVATE);
//        String type = sharedPreferences.getString("vehicle_type","");
        String model = sharedPreferences.getString("vehicle_name","");
        int variant = sharedPreferences.getInt("vehicle_model",0);

        switch (model){
            case "Avenis":
                ivUserBike.setImageResource(Avenis[variant]);
                break;

            case "Access 125":
                ivUserBike.setImageResource(accessSE125[variant]);
                break;

            case "Burgman Street":
                ivUserBike.setImageResource(BurgmanStreet[variant]);
                break;

            case "Burgman Street EX":
                ivUserBike.setImageResource(Burgman_Street_EX[variant]);
                break;

            case "V-STROM SX":
                ivUserBike.setImageResource(V_STORM_SX[variant]);
                break;

            case "GIXXER / GIXXER SF":
                ivUserBike.setImageResource(gixxerSFimages[variant]);
                break;

            case "GIXXER 250 / GIXXER SF 250":
                ivUserBike.setImageResource(gixxer250images[variant]);
                break;
        }

        bikename.setText(model);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onClusterDataRecev(ClusterStatusPktPojo event) {

        ClusterDataPacket(event.getClusterByteData());
    }

    public void ClusterDataPacket(byte[] u1_buffer) {
        if ((u1_buffer[0] == -91) && (u1_buffer[1] == 55) && (u1_buffer[29] == 127)) {
            byte Crc = calculateCheckSum(u1_buffer);

            if (u1_buffer[28] == Crc) {
                String Cluster_data = new String(u1_buffer);

                String Odometer = Cluster_data.substring(5, 11);

                runOnUiThread(() -> OdometerReading.setText(Integer.parseInt(Odometer) + " " + "km"));
            }
        }
    }

    public static String encodeTobase64(Bitmap image) {
        Bitmap immage = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

        return imageEncoded;
    }

    public static Bitmap decodeBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }


//    private void takePhotoFromCamera() {
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        startActivityForResult(intent, CAMERA);
//    }

    private void loadProfile(Bitmap url) {

        bitmap = url;
        SharedPreferences.Editor editor = myPrefrence.edit();
        editor.putString(key, encodeTobase64(url));
        editor.commit();
        riderImage.setImageBitmap(url);
        riderImage.setColorFilter(ContextCompat.getColor(this, android.R.color.transparent));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == Activity.RESULT_OK && data != null) {

                try {
                    Uri uri = data.getParcelableExtra("path");
                    // You can update this bitmap to your server
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);

                    // loading profile image from local cache
                    loadProfile(bitmap);
                } catch (IOException e) {
                }
            }
        }
    }

    private void launchCameraIntent() {

        Intent intent = new Intent(RiderProfileActivity.this, ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_IMAGE_CAPTURE);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);

        // setting maximum bitmap width and height
        intent.putExtra(ImagePickerActivity.INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT, true);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_WIDTH, 1000);
        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_HEIGHT, 1000);

        startActivityForResult(intent, REQUEST_IMAGE);
    }

    private void launchGalleryIntent() {
        Intent intent = new Intent(RiderProfileActivity.this, ImagePickerActivity.class);
        intent.putExtra(ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION, ImagePickerActivity.REQUEST_GALLERY_IMAGE);

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true);
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, 1); // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, 1);
        try {
            startActivityForResult(intent, REQUEST_IMAGE);
        } catch (Exception ignored) {
        }
    }

    private void showImagePickerOptions() {
        ImagePickerActivity.showImagePickerOptions(this, new ImagePickerActivity.PickerOptionListener() {
            @Override
            public void onTakeCameraSelected() {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 111);
                }
                else
                {
                    launchCameraIntent();
                }

            }

            @Override
            public void onChooseGallerySelected() {
                launchGalleryIntent();
            }

            @Override
            public void removePhoto() {

                Drawable res = getResources().getDrawable(R.drawable.rider_photo);
                riderImage.setImageDrawable(res);

                SharedPreferences.Editor editor = myPrefrence.edit();
                editor.putString(key, "photo");
                editor.commit();
            }
        });
    }

    /*public void viewRecord() {

        Realm realm = Realm.getDefaultInstance();
        try {
            realm.executeTransaction(new Realm.Transaction() {

                @Override
                public void execute(Realm realm) {

                    RiderProfileModule riderProfile = realm.where(RiderProfileModule.class).equalTo("id", 1).findFirst();

                    if (riderProfile != null) {

                        String odometer = riderProfile.getOdometer();
                        if (odometer != null) OdometerReading.setText(Integer.parseInt(odometer) + " " + "km");

                        riderName.setText(riderProfile.getName());
                        riderLocation.setText(riderProfile.getLocation());

                        tvRide.setText(String.valueOf(riderProfile.getRideCounts()));
                        //checkBikeData(riderProfile.getUserSelectedImage(), riderProfile.getBikeModel());
                    }
                }
            });
        } catch (Exception e) {
            Log.d("realmex", "--" + e.getMessage());
        }

        riderProfileModules = realm.where(RiderProfileModule.class).findAll();
        riderProfileModules.addChangeListener(new RealmChangeListener<RealmResults<RiderProfileModule>>() {
            @Override
            public void onChange(RealmResults<RiderProfileModule> settingsPojos) {

                RiderProfileModule riderProfile = realm.where(RiderProfileModule.class).equalTo("id", 1).findFirst();

                if (riderProfile != null) {

                    riderName.setText(riderProfile.getName());
                    riderLocation.setText(riderProfile.getLocation());
                    bikename.setText(riderProfile.getBikeModel());
//                    checkBikeData(riderProfile.getUserSelectedImage(), riderProfile.getBikeModel());
                }
            }
        });
    }*/

    /*private void checkBikeData(int userSelectedImage, String type) {

        if (type.contentEquals("V-STROM SX")) ivUserBike.setImageResource(V_STORM_SX[userSelectedImage]);
        *//*else if (type.contentEquals("Gixxer 250")) ivUserBike.setImageResource(gixxer250images[userSelectedImage]);
        else if (type.contentEquals("Gixxer SF")) ivUserBike.setImageResource(gixxerSFimages[userSelectedImage]);
        else if (type.contentEquals("Gixxer SF 250")) ivUserBike.setImageResource(gixxerSF250images[userSelectedImage]);
        else if (type.contentEquals("Intruder")) ivUserBike.setImageResource(intruder[userSelectedImage]);*//*
        else if (type.contentEquals("Avenis")) ivUserBike.setImageResource(Avenis[userSelectedImage]);
        else if (type.contentEquals("Access 125 SE")) ivUserBike.setImageResource(accessSE125[userSelectedImage]);
        else if (type.contentEquals("Burgman Street")) ivUserBike.setImageResource(BurgmanStreet[userSelectedImage]);
    }*/

    public static String getPath(Context context, Uri uri) {
        String result = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(proj[0]);
                result = cursor.getString(column_index);
            }
            cursor.close();
        }
        if (result == null) result = "Not found";

        return result;
    }

    @Override
    protected void onResume() {
        super.onResume();

        update_profile_view();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }
}
