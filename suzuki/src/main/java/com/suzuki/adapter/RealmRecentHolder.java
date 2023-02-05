package com.suzuki.adapter;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.recyclerview.widget.RecyclerView;

import com.suzuki.R;

public class RealmRecentHolder extends RecyclerView.ViewHolder {

    TextView datee;
    TextView timee;
    TextView Startloc;
    TextView Endloc, trpName;
    ImageView ivFav;
    View view;
    CheckBox cbCustomMsgCheck;
    LinearLayout llRecentData;

    public RealmRecentHolder(View itemView) {
        super(itemView);

        datee = (TextView) itemView.findViewById(R.id.tvDate);
        timee = (TextView) itemView.findViewById(R.id.tvTime);
        cbCustomMsgCheck = (CheckBox) itemView.findViewById(R.id.cbCustomMsgCheck);
        Startloc = (TextView) itemView.findViewById(R.id.tvStartLocation);
        Endloc = (TextView) itemView.findViewById(R.id.tvEndLocation);
        trpName = (TextView) itemView.findViewById(R.id.trpName);
        ivFav = (ImageView) itemView.findViewById(R.id.ivFavorite);
        view = (View) itemView.findViewById(R.id.view);
        llRecentData = (LinearLayout) itemView.findViewById(R.id.llRecentData);
    }
}
