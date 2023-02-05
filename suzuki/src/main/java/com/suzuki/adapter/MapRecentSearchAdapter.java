package com.suzuki.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.suzuki.R;
import com.suzuki.pojo.MapRecentRealmModule;

import io.realm.RealmResults;


public class MapRecentSearchAdapter extends BaseAdapter {

    Context context;
    private RealmResults<MapRecentRealmModule> results;

    public MapRecentSearchAdapter(Context context, RealmResults<MapRecentRealmModule> results){
        this.context = context;
        this.results = results;
    }

    @Override
    public int getCount() {
        return results.size();
    }

    @Override
    public Object getItem(int position) {
        return results.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MyViewHolder holder;
        if (convertView==null){
            convertView = LayoutInflater.from(context).inflate(R.layout.auto_complete_list_item,parent,false);

            holder = new MyViewHolder();

            holder.titleTv = convertView.findViewById(R.id.auto_list_item);
            holder.alternateNameTv = convertView.findViewById(R.id.auto_list_item_alternate_name);
            holder.addressTv = convertView.findViewById(R.id.auto_list_item_address);

            convertView.setTag(holder);
        }  else {
            holder = (MyViewHolder) convertView.getTag();
        }
        MapRecentRealmModule module = results.get(position);
        assert module != null;
        holder.titleTv.setText(module.getPlaceName());
        if (module.getAlternateName().length()<=1){
            holder.alternateNameTv.setVisibility(View.GONE);
        } else {
            holder.alternateNameTv.setVisibility(View.VISIBLE);
            holder.alternateNameTv.setText(module.getAlternateName());
        }
        holder.addressTv.setText(module.getAddress());

        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getViewTypeCount() {
        return super.getViewTypeCount();
    }

    private class MyViewHolder{
        TextView titleTv, alternateNameTv, addressTv;
    }
}