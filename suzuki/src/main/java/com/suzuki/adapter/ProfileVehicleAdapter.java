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

//import info.androidhive.imagepicker.R;


public class ProfileVehicleAdapter extends PagerAdapter {

    private final Context context;
    private LayoutInflater layoutInflater;
    private final Integer[] images;
    String[] colors;
    TextView textView;

    public ProfileVehicleAdapter(Context context, Integer[] images, String[]colors) {
        this.context = context;
        this.images = images;
      this.colors=colors;
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
        View view = layoutInflater.inflate(R.layout.item, null);
        ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
        textView=view.findViewById(R.id.imagecolor);
        if (colors!= null){
            textView.setText(colors[position]);
        }else{

        }

//       textView.setText(colors[position]);
       // Toast.makeText(context, ""+colors[position], Toast.LENGTH_SHORT).show();
        imageView.setImageResource(images[position]);
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