package com.suzuki.activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.viewpager.widget.ViewPager;

import com.sachinvarma.easypermission.EasyPermission;
import com.sachinvarma.easypermission.EasyPermissionConstants;
import com.sachinvarma.easypermission.EasyPermissionInit;
import com.sachinvarma.easypermission.EasyPermissionList;
import com.suzuki.R;
import com.suzuki.adapter.ViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator;

public class IntroscreenActivity extends AppCompatActivity {

    ViewPagerAdapter viewPagerAdapter;
    TextView tvSkip, tvNext;
//    final int REQUEST_CODE_ASK_PERMISSIONS = 123;
//    List<String> permission = new ArrayList<>();
    //private static final int PERMISSION_REQUEST_COARSE_LOCATION = 456;
    BluetoothAdapter mBtAdapter = null;
    //BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;

    String help = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.introscreen_activity);
        final ViewPager viewpager = (ViewPager) findViewById(R.id.view_pager);
        tvNext = (TextView) findViewById(R.id.tvNext);
        tvSkip = (TextView) findViewById(R.id.tvSkip);

        Intent intent = getIntent();

        help = intent.getStringExtra("help");

        viewPagerAdapter = new ViewPagerAdapter(this);
        viewpager.setAdapter(viewPagerAdapter);

        tvNext.setOnClickListener(v -> viewpager.setCurrentItem(viewpager.getCurrentItem() + 1));

        tvSkip.setOnClickListener(v -> viewpager.setCurrentItem(8));

        CircleIndicator indicator = (CircleIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(viewpager);

        viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageSelected(int position) {

                Log.d("pos--", "- " + position);
                if (position == 8) {
                    tvNext.setText("Finish");
                } else {
                    tvNext.setText("Next");
                }




                if (viewpager.getCurrentItem() == 8) {

                    tvSkip.setVisibility(View.GONE);
                    tvNext.setOnClickListener(v -> {


                        if (help.contentEquals("help")) {

                            finish();
                        }
                        else {

                            Intent intent1 = new Intent(IntroscreenActivity.this, CreateProfileActivity.class);
                            SharedPreferences sharedPreferences = getSharedPreferences("FT",MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putInt("first",500);
                            editor.apply();
                            startActivity(intent1);
                            finish();

                        }
                    });

                }



                else if(tvNext.getText()=="Next"){
                    tvSkip.setVisibility(View.VISIBLE);
                    tvNext.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            viewpager.setCurrentItem(viewpager.getCurrentItem() + 1);
                        }
                    });
                }



            }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });
    }

    /*private boolean checkforBluetoothConnection() {
        if (!mBtAdapter.isEnabled()) {

            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);

            return false;
        } else return true;
    }*/

    @Override
    protected void onResume() {
        super.onResume();
    }

    //    public void EnableRuntimePermissionToAccessCallLogs() {
//        Log.i("ksks", "EnableRuntimePermissionToAccessCallLogs called");
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
//            Log.i("sjsj", "giving read call log permission");
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.READ_CALL_LOG},
//                    1);
//            Log.i("js", "giving read call log permission 2");
//        }
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
//            Log.i("actt--", "giving write call log permission");
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.WRITE_CALL_LOG},
//                    1);
//            Log.i("ksks", "giving write call log permission 2");
//
//        }
//        Log.i("jsjs", "EnableRuntimePermissionToAccessCallLogs called 2");
//
//    }
}


