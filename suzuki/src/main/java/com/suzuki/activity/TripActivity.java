package com.suzuki.activity;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

//import androidx.viewpager.widget.ViewPager;


import com.google.android.material.tabs.TabLayout;
import com.suzuki.R;
import com.suzuki.adapter.PagerCustomAdapter;
import com.suzuki.fragment.FavouriteTripFragment;
import com.suzuki.fragment.RecentFragment;
import com.suzuki.pojo.RecentTripRealmModule;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class TripActivity extends AppCompatActivity {


    TabLayout tabLayout;
    ViewPager viewPager;
    PagerCustomAdapter viewPagerAdapter;
    public static LinearLayout llBack, llDelete;
    public static ArrayList<RecentTripRealmModule> tripdata;
    Realm realm;
    public static ImageView ivDelete;
    RealmResults<RecentTripRealmModule> recentTrip;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trip_activity);
        realm = Realm.getDefaultInstance();


        Intent intent = getIntent();


        String fav = intent.getStringExtra("fav");


        viewPager = (ViewPager) findViewById(R.id.viewPager);
        llDelete = (LinearLayout) findViewById(R.id.llDelete);
        ivDelete = (ImageView) findViewById(R.id.ivDelete);
        setupMyViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);

        tabLayout.setupWithViewPager(viewPager);

        llBack = (LinearLayout) findViewById(R.id.llBack);

        tripdata = new ArrayList<>();

        llBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                finish();
            }
        });
        readTripRecords();
        if (fav != null) {
            viewPager.setCurrentItem(1);
        }

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                if (position == 0) {

                    if (recentTrip.size() > 0) {
                        ivDelete.setVisibility(View.VISIBLE);
                    } else {
                        ivDelete.setVisibility(View.GONE);
                    }
                } else {
                    ivDelete.setVisibility(View.GONE);
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        recentTrip.addChangeListener(new RealmChangeListener<RealmResults<RecentTripRealmModule>>() {
            @Override
            public void onChange(RealmResults<RecentTripRealmModule> tripRealmModules) {


                readTripRecords();


            }
        });


    }

    private List<RecentTripRealmModule> readTripRecords() {


        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {


                recentTrip = realm.where(RecentTripRealmModule.class)
                        .sort("dateTime", Sort.DESCENDING)
                        .findAll();

                if (recentTrip.size() > 0) {
                    ivDelete.setVisibility(View.VISIBLE);
                } else {
                    ivDelete.setVisibility(View.GONE);
                }

            }
        });

        return recentTrip;
    }


    private void setupMyViewPager(ViewPager viewPager) {
        PagerCustomAdapter adapter = new PagerCustomAdapter(this.getSupportFragmentManager());
        adapter.addFragment(new RecentFragment(), "Recent");
        adapter.addFragment(new FavouriteTripFragment(), "Favourites");

        viewPager.setAdapter(adapter);
    }


}
