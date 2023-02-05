package com.suzuki.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.ViewPager;

//import androidx.viewpager.widget.ViewPager;


import com.google.android.material.tabs.TabLayout;
import com.suzuki.R;
import com.suzuki.adapter.PagerCustomAdapter;
import com.suzuki.base.BaseFragment;

public class TripFragment extends BaseFragment {


    TabLayout tabLayout;
    ViewPager viewPager;
    PagerCustomAdapter viewPagerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.trip_activity, container, false);


        viewPager = (ViewPager) view.findViewById(R.id.viewPager);


        setupMyViewPager(viewPager);

        tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        return view;

    }


    private void setupMyViewPager(ViewPager viewPager) {
        PagerCustomAdapter adapter = new PagerCustomAdapter(getFragmentManager());
        adapter.addFragment(new RecentFragment(), "Recent");
        adapter.addFragment(new SettingFragment(), "Favourites");

        viewPager.setAdapter(adapter);
    }
}
