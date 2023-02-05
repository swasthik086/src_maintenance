package com.suzuki.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.clj.fastble.BleManager;
import com.clj.fastble.data.BleDevice;
import com.suzuki.R;

import java.util.ArrayList;
import java.util.List;

import static com.suzuki.utils.Common.bleName_common;
import static com.suzuki.utils.Common.cluster_a_connected;

import static com.suzuki.activity.DeviceListingScanActivity.rlButtonConnect;
import static com.suzuki.activity.DeviceListingScanActivity.rlButtonRefresh;
import static com.suzuki.activity.DeviceListingScanActivity.tvStatus;
import static com.suzuki.application.SuzukiApplication.getLastConnectedVehicleName;
import static com.suzuki.application.SuzukiApplication.getOldConnectedDeviceList;


public class BleListingDeviceAdapter extends BaseAdapter {

    private Context context;
    private List<BleDevice> bleDeviceList;
    public static String name;

    public BleListingDeviceAdapter(Context context) {
        this.context = context;
        bleDeviceList = new ArrayList<>();
    }

    public void addDevice(BleDevice bleDevice) {

        boolean deviceFound = false;

        if (bleDevice.getName() != null) {
//            if (device.getName().indexOf("Suz") != -1) {
            for (BleDevice listDev : bleDeviceList) {

                if (listDev.getMac().equals(bleDevice.getMac())) {

                    deviceFound = true;
                    break;
                }
            }
//            if (!deviceFound) {
//
//
//                {
//
//                    BleDevice bluetoothDeviceList = new BleDevice(bleDevice.getDevice());
//
//                    bluetoothDeviceList.setDevice(bleDevice.getDevice());
//                    bleDeviceList.add(bluetoothDeviceList);
//                }
//
//
//            }
            if (!deviceFound) {
                {
                    if (bleDevice.getName().contains("SA") || bleDevice.getName().contains("SB")) {
                        if (bleDevice.getName().length() == 12) {
                            BleDevice bluetoothDeviceList = new BleDevice(bleDevice.getDevice());

                            bluetoothDeviceList.setDevice(bleDevice.getDevice());
                            int pos = bleDeviceList.size();
                            if (getOldConnectedDeviceList() != null && getOldConnectedDeviceList().contains(bleDevice.getName())) {
                                if (bleDeviceList.size() > 0)
                                    if (bleDeviceList.get(0).getDevice().getName().equalsIgnoreCase(getLastConnectedVehicleName()))
                                        pos = 1;
                                    else pos = 0;
                            }
                            bleDeviceList.add(pos, bluetoothDeviceList);
                        }
                    }
                }
            }
            //getYoung
        }

        if (bleDeviceList.size() > 0) {
            tvStatus.setText("TAP TO PAIR");
            rlButtonConnect.setVisibility(View.GONE);
            rlButtonRefresh.setVisibility(View.GONE);
        } else if (bleDeviceList.size() == 0) {
            tvStatus.setText("NO VEHICLE FOUND");
            rlButtonRefresh.setVisibility(View.VISIBLE);
            rlButtonConnect.setVisibility(View.GONE);
        }
    }

    public void removeDevice(BleDevice bleDevice) {
        for (int i = 0; i < bleDeviceList.size(); i++) {
            BleDevice device = bleDeviceList.get(i);
            if (bleDevice.getKey().equals(device.getKey())) {
                bleDeviceList.remove(device);
            }
        }
    }

    public void clearConnectedDevice() {
        ArrayList<BleDevice> dummyList = new ArrayList<>();
        for (int i = 0; i < bleDeviceList.size(); i++) {
            BleDevice device = bleDeviceList.get(i);
            if (!BleManager.getInstance().isConnected(device)) {
//                bleDeviceList.remove(device);
                dummyList.add(device);
            }
        }
        bleDeviceList = dummyList;
    }

    public void clearScanDevice() {
        bleDeviceList.clear();
        /*for (int i = 0; i < bleDeviceList.size(); i++) {
            BleDevice device = bleDeviceList.get(i);
            if (!BleManager.getInstance().isConnected(device)) {
                bleDeviceList.remove(device);
            }
        }*/
    }

    public void clear() {
        clearConnectedDevice();
        clearScanDevice();
    }

    @Override
    public int getCount() {
        return bleDeviceList.size();
    }

    @Override
    public BleDevice getItem(int position) {
        if (position > bleDeviceList.size()) return null;

        return bleDeviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = View.inflate(context, R.layout.listitem_device, null);
            holder = new ViewHolder();
            convertView.setTag(holder);
            holder.img_blue = (ImageView) convertView.findViewById(R.id.ivSelected);
            holder.txt_name = (TextView) convertView.findViewById(R.id.deviceName);
//            holder.txt_mac = (TextView) convertView.findViewById(R.id.txt_mac);
//            holder.txt_rssi = (TextView) convertView.findViewById(R.id.txt_rssi);
//            holder.layout_idle = (LinearLayout) convertView.findViewById(R.id.layout_idle);
            holder.layout_connected = (LinearLayout) convertView.findViewById(R.id.llList);
//            holder.btn_disconnect = (Button) convertView.findViewById(R.id.btn_disconnect);
//            holder.btn_connect = (Button) convertView.findViewById(R.id.btn_connect);
//            holder.btn_detail = (Button) convertView.findViewById(R.id.btn_detail);
        }

        final BleDevice bleDevice = getItem(position);
        if (bleDevice != null) {
            if (bleDevice.getName() != null) {
                boolean isConnected = BleManager.getInstance().isConnected(bleDevice);
                name = bleDevice.getName();
                bleName_common=name;

                Log.e("blename",name);

                String mac = bleDevice.getMac();
                int rssi = bleDevice.getRssi();
                SharedPreferences sharedPreferences;
                SharedPreferences.Editor editor;

                sharedPreferences=context.getSharedPreferences("HOMESCREEN",Context.MODE_PRIVATE);
                editor=sharedPreferences.edit();

                holder.txt_name.setText(name);

                if (isConnected) {
                    Log.e("bleconnected",name);
                    if(name.contains("SB")) cluster_a_connected=false;
                    else cluster_a_connected=true;
                    Log.e("bleconnected",name+" "+cluster_a_connected);

                    /**
                     * storing connected cluster name for user feedback purpose
                     */
                    {
                        editor.putString("CLUSTER_NAME", name);
                        editor.commit();
                    }

                    holder.img_blue.setImageResource(R.drawable.list_selected_blue);
                    holder.txt_name.setTextColor(context.getResources().getColor(R.color.app_theme_color));

                } else {
                    holder.img_blue.setImageResource(R.drawable.listnot);
                    holder.txt_name.setTextColor(context.getResources().getColor(R.color.white));

                }
            } else {
//                bleDeviceList.remove(position);

            }
        } else {
//            bleDeviceList.remove(position);
        }
//        }

        holder.layout_connected.setOnClickListener(view -> {

            if (mListener != null) mListener.onConnect(bleDevice);
        });
//        holder.btn_disconnect.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (mListener != null) {
//                    mListener.onDisConnect(bleDevice);
//                }
//            }
//        });

//        holder.btn_detail.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (mListener != null) {
//                    mListener.onDetail(bleDevice);
//                }
//            }
//        });
        return convertView;
    }

    class ViewHolder {
        ImageView img_blue;
        TextView txt_name;
        LinearLayout layout_connected;

    }

    public interface OnDeviceClickListener {
        void onConnect(BleDevice bleDevice);

        void onDisConnect(BleDevice bleDevice);

        void onDetail(BleDevice bleDevice);
    }

    private OnDeviceClickListener mListener;

    public void setOnDeviceClickListener(OnDeviceClickListener listener) {
        this.mListener = listener;
    }

}
