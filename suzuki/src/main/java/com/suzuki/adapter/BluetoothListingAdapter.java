package com.suzuki.adapter;


import android.content.Context;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import com.suzuki.R;
import com.suzuki.interfaces.IOnclickFromAdapterToActivityAndFragment;
import com.suzuki.pojo.BluetoothDeviceList;

import java.util.List;

//true
public class BluetoothListingAdapter extends RecyclerView.Adapter<BluetoothListingAdapter.MyViewHolder> {
    private static final String TAG = BluetoothListingAdapter.class.getSimpleName();
    Context context;
    List<BluetoothDeviceList> devices;
    LayoutInflater inflater;
    IOnclickFromAdapterToActivityAndFragment iOnclickFromAdapterToActivityAndFragment;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        //        @BindView(R.id.macAddressText)
        public TextView deviceName;

        ImageView ivSelected;

//        @BindView(R.id.card_view)
//        CardView card_view;

        //        @BindView(R.id.rbMask)
        LinearLayout llList;


        //        @OnClick({R.id.rbMask, R.id.card_view})
        public void changeofSelectedDevice() {
            iOnclickFromAdapterToActivityAndFragment.adapterItemIsClicked((Integer) llList.getTag(), "");
        }

        public MyViewHolder(View view) {
            super(view);

            deviceName = (TextView) view.findViewById(R.id.deviceName);
            llList = (LinearLayout) view.findViewById(R.id.llList);
            ivSelected = (ImageView) view.findViewById(R.id.ivSelected);
//            ButterKnife.bind(this, view);
        }
    }


    public BluetoothListingAdapter(Context context, List<BluetoothDeviceList> devices, IOnclickFromAdapterToActivityAndFragment iOnclickFromAdapterToActivityAndFragment) {
        this.devices = devices;
        this.context = context;
        this.iOnclickFromAdapterToActivityAndFragment = iOnclickFromAdapterToActivityAndFragment;
        inflater = LayoutInflater.from(context);


    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_device, parent, false);


        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        BluetoothDeviceList device = devices.get(position);
//        holder.rbMask.setTag(position);

//        holder.deviceName.setText(device.getMacAddress());
        Log.d("ddevv-", "de-" + device.getMacAddress() + device.getName());

//        if (device.getName().indexOf("Suz") != -1) {
        holder.deviceName.setText(device.getName());
        holder.llList.setTag(position);
        Log.d("ddevv-", "de-" + device.getMacAddress() + device.getName());
//        }
        holder.llList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.changeofSelectedDevice();
            }
        });

        Log.d("checked--","-"+device.getChecked());
//        holder.ivSelected.setChecked(device.getChecked());
//        holder.rbMask.setChecked(device.getChecked());


        if(device.getChecked()){
            holder.deviceName.setTextColor(context.getResources().getColor(R.color.app_theme_color));
            holder.ivSelected.setImageDrawable(context.getResources().getDrawable(R.drawable.list_selected_blue));


        }else {

            holder.deviceName.setTextColor(context.getResources().getColor(R.color.white));
            holder.ivSelected.setImageDrawable(context.getResources().getDrawable(R.drawable.listnot));
        }

    }

    @Override
    public int getItemCount() {
        return devices.size();

    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}


