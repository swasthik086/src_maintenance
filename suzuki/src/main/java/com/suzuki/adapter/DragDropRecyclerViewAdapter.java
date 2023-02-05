package com.suzuki.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.suzuki.R;
import com.suzuki.interfaces.ItemMoveCallback;
import com.suzuki.interfaces.MapDragListInterface;
import com.suzuki.interfaces.StartDragListener;
import com.suzuki.pojo.MapListCustomClass;

import java.util.ArrayList;
import java.util.Collections;

public class DragDropRecyclerViewAdapter extends RecyclerView.Adapter<DragDropRecyclerViewAdapter.MyViewHolder> implements ItemMoveCallback.ItemTouchHelperContract {

    ArrayList<MapListCustomClass> mapListCustomClassArrayList;
    private final StartDragListener mStartDragListener;
    MapDragListInterface mapDragListInterface;

    public static ArrayList<MapListCustomClass> updatedCustomList = new ArrayList<>();

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView mTitle;
        private LinearLayout llHolderForDrag;
        RelativeLayout rlView;
        ImageView ivdragDropIcon;
        View rowView;


        public MyViewHolder(View itemView) {
            super(itemView);

            rowView = itemView;
            mTitle = itemView.findViewById(R.id.tvItems);
            llHolderForDrag = itemView.findViewById(R.id.llHolderForDrag);
            rlView = itemView.findViewById(R.id.rlView);
            ivdragDropIcon = itemView.findViewById(R.id.ivdragDropIcon);

        }
    }

    public DragDropRecyclerViewAdapter(ArrayList<MapListCustomClass> mapListCustomClassArrayList, StartDragListener startDragListener, MapDragListInterface mapDragListInterface) {
        mStartDragListener = startDragListener;
//        context = context;


        this.mapListCustomClassArrayList = mapListCustomClassArrayList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.drag_drop_item_adapter, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.mTitle.setText(mapListCustomClassArrayList.get(position).getName());

        if (mapListCustomClassArrayList.get(position).getName().contentEquals("Suzuki Service")) {
            holder.ivdragDropIcon.setImageResource(R.drawable.suzuki_logo);
        } else if (mapListCustomClassArrayList.get(position).getName().contentEquals("Fuel Station")) {
            holder.ivdragDropIcon.setImageResource(R.drawable.gas_icon);
        } else if (mapListCustomClassArrayList.get(position).getName().contentEquals("Hospitals")) {
            holder.ivdragDropIcon.setImageResource(R.drawable.hos);
        } else if (mapListCustomClassArrayList.get(position).getName().contentEquals("Banks and ATM")) {
            holder.ivdragDropIcon.setImageResource(R.drawable.atm);
        } else if (mapListCustomClassArrayList.get(position).getName().contentEquals("Food and Restaurants")) {
            holder.ivdragDropIcon.setImageResource(R.drawable.hotel);
        } else if (mapListCustomClassArrayList.get(position).getName().contentEquals("Suzuki Sales")) {
            holder.ivdragDropIcon.setImageResource(R.drawable.suzuki_logo);
        }
//        else if (mapListCustomClassArrayList.get(position).getName().contentEquals("Favourites")) {
//            holder.ivdragDropIcon.setImageResource(R.drawable.fav_menu_icon);
//        }
        else if (mapListCustomClassArrayList.get(position).getName().contentEquals("Tyre Repair Shops")) {
            holder.ivdragDropIcon.setImageResource(R.drawable.tire);
        } else if (mapListCustomClassArrayList.get(position).getName().contentEquals("Medical Stores")) {
            holder.ivdragDropIcon.setImageResource(R.drawable.pharmacist);
        } else if (mapListCustomClassArrayList.get(position).getName().contentEquals("Parking")) {
            holder.ivdragDropIcon.setImageResource(R.drawable.parking);
        }
        else if (mapListCustomClassArrayList.get(position).getName().contentEquals("Convenience Stores")) {
            holder.ivdragDropIcon.setImageResource(R.drawable.shopping_cart);
        }



        holder.llHolderForDrag.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() ==
                        MotionEvent.ACTION_DOWN) {
                    mStartDragListener.requestDrag(holder);
                }
                return false;
            }
        });

    }


    @Override
    public int getItemCount() {
        return mapListCustomClassArrayList.size();
    }


    @Override
    public void onRowMoved(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mapListCustomClassArrayList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mapListCustomClassArrayList, i, i - 1);
            }
        }

        notifyItemMoved(fromPosition, toPosition);

        updatedCustomList.clear();
        for (int i = 0; i < mapListCustomClassArrayList.size(); i++) {
            Log.d("rowmoved", "kkk--" + mapListCustomClassArrayList.get(i).getName() + "--" + i);


            MapListCustomClass mapListCustomClass = new MapListCustomClass();

            mapListCustomClass.setId(i);
            mapListCustomClass.setName(mapListCustomClassArrayList.get(i).getName());

            updatedCustomList.add(mapListCustomClass);

        }

        for (MapListCustomClass sms : updatedCustomList) {
            Log.d("sjuwiiicust", "cutt--" + sms.getName() + sms.getId());
            Log.d("sjuwiiicust", "cuttsize--" + updatedCustomList.size());
        }

    }


    @Override
    public void onRowSelected(MyViewHolder myViewHolder) {
//        myViewHolder.rowView.setBackgroundColor(Color.GRAY);

    }

    @Override
    public void onRowClear(MyViewHolder myViewHolder) {
//        myViewHolder.rowView.setBackgroundColor(context.getResources().getColor(R.color.));

    }
}


