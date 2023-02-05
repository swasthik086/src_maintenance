package com.suzuki.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.suzuki.R;
import com.suzuki.pojo.RecentTripPojo;

import java.util.List;

public class RecentTripAdapter extends RecyclerView.Adapter<RecentTripAdapter.ViewHolder> {
    private RecentTripPojo[] listdata;

    List<RealmRecentHolder> list;
    // RecyclerView recyclerView;
    public RecentTripAdapter(RecentTripPojo[] listdata) {
        this.listdata = listdata;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.recent_adapter_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);


        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        final RecentTripPojo myListData = listdata[position];
        holder.textView.setText(listdata[position].getStartLocation());
//

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RealmRecentHolder itemLabel = list.get(position);

                list.remove(position);
                notifyItemRemoved(position);

                notifyItemRangeChanged(position,list.size());

            }
        });

    }

    public interface onItemClick{

        void onClick(String value);
    }


    @Override
    public int getItemCount() {
        return listdata.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView textView;
        public RelativeLayout relativeLayout;
        public CheckBox deleteButton;

        public ViewHolder(View itemView) {
            super(itemView);
            this.textView = (TextView) itemView.findViewById(R.id.tvStartLocation);
//            relativeLayout = (RelativeLayout)itemView.findViewById(R.id.relativeLayout);
            deleteButton = (CheckBox) itemView.findViewById(R.id.cbCustomMsgCheck);
        }
    }
}  
