package com.suzuki.adapter;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.mappls.sdk.navigation.NavigationFormatter;
import com.mappls.sdk.services.api.nearby.model.NearbyAtlasResult;
import com.suzuki.R;
import com.suzuki.application.SuzukiApplication;
import com.suzuki.interfaces.IOnclickFromAdapterToActivityAndFragment;

import java.util.ArrayList;

/**
 * Created by CEINFO on 26-02-2019.
 */

public class NearByAdapter extends RecyclerView.Adapter<NearByAdapter.NearByView> {

    ArrayList<NearbyAtlasResult> list;
    public int meters;
    public long distance = 0;
    Context context;
    IOnclickFromAdapterToActivityAndFragment clicked;

    public NearByAdapter(ArrayList<NearbyAtlasResult> list, Context context, IOnclickFromAdapterToActivityAndFragment clicked) {
        this.list = list;
        this.context = context;
        this.clicked = clicked;
    }


    @Override
    public NearByView onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.auto_suggest_adapter_row, parent, false);
        return new NearByView(v);
    }

    @Override
    public void onBindViewHolder(NearByView holder, @SuppressLint("RecyclerView")  int position) {
        holder.viewName.setText(list.get(position).getPlaceAddress());
        holder.auto_list_item.setText(list.get(position).getPlaceName());


//        meters = (distance > 0 ? 10 * (Math.round(distance / 10)) : 0);
//        if (meters >= 100 * 1000) {
//            meters = (int) (meters / 1000 + 0.5);
//        }


        holder.auto_list_item_address.setText(NavigationFormatter.getFormattedDistance(list.get(position).getDistance(), (SuzukiApplication) context.getApplicationContext()));


        holder.llMainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (list.get(position).entryLatitude == null || list.get(position).entryLongitude == null)
                    clicked.adapterItemIsClicked(list.get(position).latitude, list.get(position).longitude, list.get(position).getPlaceAddress(), list.get(position).getPlaceName());
                else
                    clicked.adapterItemIsClicked(list.get(position).entryLatitude, list.get(position).entryLongitude, list.get(position).getPlaceAddress(), list.get(position).getPlaceName());


            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class NearByView extends RecyclerView.ViewHolder {
        TextView viewName;
        TextView auto_list_item, auto_list_item_address;
        LinearLayout llMainLayout;

        public NearByView(View itemView) {
            super(itemView);
            viewName = itemView.findViewById(R.id.textView);
            auto_list_item = itemView.findViewById(R.id.auto_list_item);
            auto_list_item_address = itemView.findViewById(R.id.auto_list_item_address);
            llMainLayout = itemView.findViewById(R.id.llMainLayout);
        }
    }
}
