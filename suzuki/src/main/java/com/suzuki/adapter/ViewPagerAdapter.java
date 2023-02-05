package com.suzuki.adapter;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.suzuki.R;


public class ViewPagerAdapter extends PagerAdapter {

    private Context context;
    private LayoutInflater layoutInflater;
    private Integer[] images = {R.drawable.intro_one, R.drawable.intro_two, R.drawable.intro_three, R.drawable.intro_four, R.drawable.intro_five, R.drawable.intro_six,R.drawable.intro_sev,R.drawable.copy, R.drawable.intro_zero};

    private String[] textMain = {"CONNECTING WITH BIKE", "CONNECTING WITH BIKE", "CONNECTING WITH BIKE", "TURN BY TURN ASSISTANCE", "TURN BY TURN ASSISTANCE", "TO VIEW TRIP RECORD", "TO USE TRIP RECORD", "Custom POI","Suzuki Ride Connect Required Permissions"
    };

    private String[] textSub = {
            "Pair mode in dashboard", "Sync your app to your ride", "Sync your app to your ride", "Set the destination",
            "To start assistance", "Start navigation", "Usage of past trips","Suzuki Sales & Service for customer easy access","We recommend users to allow below permissions to use all the features of the application."

    };

    public ViewPagerAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return images.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {

        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.viewpager_adapter, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.imageView);

        TextView tvMain = (TextView) view.findViewById(R.id.tvMain);

        TextView tvSub = (TextView) view.findViewById(R.id.tvSub);
        imageView.setImageResource(images[position]);

        tvMain.setText(textMain[position]);
        tvSub.setText(textSub[position]);

        ViewPager vp = (ViewPager) container;
        vp.addView(view, 0);
        return view;

    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        ViewPager vp = (ViewPager) container;
        View view = (View) object;
        vp.removeView(view);

    }
}