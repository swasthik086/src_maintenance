package com.suzuki.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.suzuki.R;
import com.suzuki.adapter.FaqPagerAdapter;
import com.suzuki.fragment.faqFragments.About;
import com.suzuki.fragment.faqFragments.General;
import com.suzuki.fragment.faqFragments.Profile;
import com.suzuki.fragment.faqFragments.ProfileSetting;
import com.suzuki.fragment.faqFragments.Rides;
import com.suzuki.fragment.faqFragments.Settings;

public class FaqActivity extends AppCompatActivity {

    Toolbar toolbar;
    TextView titleTv;
    ImageButton backButton;
    TabLayout tabLayout;
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);

        toolbar = findViewById(R.id.toolBar);
        titleTv = findViewById(R.id.titleTv);
        backButton = findViewById(R.id.backButton);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        setSupportActionBar(toolbar);

        FaqPagerAdapter faqPagerAdapter = new FaqPagerAdapter(getSupportFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        faqPagerAdapter.addFragment(new Rides(),"Rides");
        faqPagerAdapter.addFragment(new Profile(),"Profile");
        faqPagerAdapter.addFragment(new About(),"About");
        faqPagerAdapter.addFragment(new General(),"General");
        faqPagerAdapter.addFragment(new Settings(),"Settings");
        faqPagerAdapter.addFragment(new ProfileSetting(),"Profile Setting");
        viewPager.setAdapter(faqPagerAdapter);
        tabLayout.setupWithViewPager(viewPager,true);
    }
}
