package com.suzuki.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.suzuki.R;
import com.suzuki.interfaces.IOnclickFromAdapterToActivityAndFragment;
import com.suzuki.pojo.RecentTripRealmModule;

import java.util.List;

public class RealmRecentAdapter extends RecyclerView.Adapter<RealmRecentHolder> {

    List<RecentTripRealmModule> list;
    Context context;

    IOnclickFromAdapterToActivityAndFragment clickedItem;

    public RealmRecentAdapter(List<RecentTripRealmModule> list, Context context, IOnclickFromAdapterToActivityAndFragment iOnclickFromAdapterToActivityAndFragment) {
        this.list = list;
        this.context = context;
        this.clickedItem = iOnclickFromAdapterToActivityAndFragment;
    }

    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public RealmRecentHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View photoView = inflater.inflate(R.layout.recent_adapter, parent, false);
        RealmRecentHolder viewHolder = new RealmRecentHolder(photoView);

        return viewHolder;
    }

    @SuppressLint("RecyclerView")
    @Override
    public void onBindViewHolder(RealmRecentHolder viewHolder,  int position) {

        viewHolder.datee.setText(list.get(position).getDate());
        viewHolder.timee.setText(list.get(position).getTime());
        viewHolder.Startloc.setText(list.get(position).getStartlocation());
        viewHolder.Endloc.setText(list.get(position).getEndlocation());

        if (position < (list.size() - 1)) viewHolder.view.setVisibility(View.VISIBLE);
        else viewHolder.view.setVisibility(View.GONE);

        if (list.get(position).isFavorite()) viewHolder.ivFav.setImageResource(R.drawable.favor);
        else viewHolder.ivFav.setImageResource(R.drawable.fav);

        viewHolder.llRecentData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                clickedItem.adapterItemIsClicked(list.get(position).getId(), "details", list.get(position).isFavorite(),
                        list.get(position).getDate(), list.get(position).getDateTime(), list.get(position).getTime(),
                        list.get(position).getStartlocation(), list.get(position).getEndlocation(),
                        list.get(position).getCurrent_lat(), list.get(position).getCurrent_long(),
                        list.get(position).getDestination_lat(), list.get(position).getDestination_long(),
                        list.get(position).getTrip_name(), list.get(position).getRideTime(), list.get(position).getTotalDistance(),
                        String.valueOf(list.get(position).getTopSpeed()), String.valueOf(list.get(position).getRidetimeLt10()),
                        list.get(position).getPointLocationRealmModels(),list.get(position).getstartTime(),list.get(position).getETA());
              //  Toast.makeText(context, ""+ list.get(position).getCurrent_lat()+list.get(position).getCurrent_long(), Toast.LENGTH_SHORT).show();
            }

        });

        viewHolder.ivFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (list.get(position).isFavorite()) {
//                    clickedItem.adapterItemIsClicked(list.get(position).getId(), "fav", false, list.get(position).getDate(), list.get(position).getDateTime(), list.get(position).getTime(), list.get(position).getStartlocation(), list.get(position).getEndlocation());

                    clickedItem.adapterItemIsClicked(list.get(position).getId(), "fav", false, list.get(position).getDate(), list.get(position).getDateTime(),
                            list.get(position).getTime(), list.get(position).getStartlocation(), list.get(position).getEndlocation(),
                            list.get(position).getCurrent_lat(), list.get(position).getCurrent_long(),
                            list.get(position).getDestination_lat(), list.get(position).getDestination_long(),
                            list.get(position).getTrip_name(), list.get(position).getRideTime(), list.get(position).getTotalDistance(),
                            String.valueOf(list.get(position).getTopSpeed()), String.valueOf(list.get(position).getRidetimeLt10()),
                            list.get(position).getPointLocationRealmModels(),list.get(position).getstartTime(),list.get(position).getETA());
                } else {
                    clickedItem.adapterItemIsClicked(list.get(position).getId(), "fav", true, list.get(position).getDate(),
                            list.get(position).getDateTime(), list.get(position).getTime(), list.get(position).getStartlocation(),
                            list.get(position).getEndlocation(), list.get(position).getCurrent_lat(), list.get(position).getCurrent_long(), list.get(position).getDestination_lat(),
                            list.get(position).getDestination_long(), list.get(position).getTrip_name(), list.get(position).getRideTime(),
                            list.get(position).getTotalDistance(), String.valueOf(list.get(position).getTopSpeed()),
                            String.valueOf(list.get(position).getRidetimeLt10()), list.get(position).getPointLocationRealmModels(),
                            list.get(position).getstartTime(),
                            list.get(position).getETA());
                }
            }
        });

        viewHolder.cbCustomMsgCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                clickedItem.adapterItemIsClicked(list.get(position).getId(), "delete", isChecked, list.get(position).getDate(),
                        list.get(position).getDateTime(), list.get(position).getTime(), list.get(position).getStartlocation(),
                        list.get(position).getEndlocation(), list.get(position).getCurrent_lat(), list.get(position).getCurrent_long(),
                        list.get(position).getDestination_lat(), list.get(position).getDestination_long(), list.get(position).getTrip_name(),
                        list.get(position).getRideTime(), list.get(position).getTotalDistance(), String.valueOf(list.get(position).getTopSpeed()),
                        String.valueOf(list.get(position).getRidetimeLt10()), list.get(position).getPointLocationRealmModels(),list.get(position).getstartTime(),list.get(position).getETA());
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
