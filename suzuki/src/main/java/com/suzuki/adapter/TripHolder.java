package com.suzuki.adapter;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.suzuki.R;

public class TripHolder extends RecyclerView.ViewHolder {
    TextView date;
    TextView time;
    TextView startLoc;
    TextView endLoc;

    TripHolder(View itemView)
    {
        super(itemView);
        date = (TextView)itemView.findViewById(R.id.tvDate);
        time = (TextView)itemView.findViewById(R.id.tvTime);
        startLoc = (TextView)itemView.findViewById(R.id.tvStartLocation);
        endLoc = (TextView)itemView.findViewById(R.id.tvEndLocation);
    }
}