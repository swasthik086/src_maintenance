package com.suzuki.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.suzuki.R;
import com.suzuki.interfaces.ItemTouchHelperAdapter;
import com.suzuki.pojo.ViaPointPojo;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;

import static com.suzuki.activity.RouteActivity.currentLocChanging;
import static com.suzuki.activity.RouteActivity.destionationChanging;
import static com.suzuki.activity.RouteActivity.stopAutoSuggest;
import static com.suzuki.activity.RouteActivity.viaPointChanging;
import static com.suzuki.activity.RouteActivity.viaPoints;

public class ViaPointAdapter extends RecyclerView.Adapter<ViaPointAdapter.ViaPoint> implements ItemTouchHelperAdapter {

    private final Context context;
    private final ArrayList<ViaPointPojo> dataList;
    private final RecyclerView suggestionRv;
    private final ProgressBar apiProgress;

    private AddressChanged addressChanged;
    private deleteViaPoint deleteViaPoint;
    //New Code

    private Updation updation;
    private LayoutDelete layoutDelete;
    //Via Points Update CallBack Interfaces
    public interface Updation {
        void updateThisVia();
    }

    public void updateTheViaPointFromAdapter(Updation updation) {
        this.updation = updation;
    }

    //Via Point Layout Delete CallBack Interfaces
    public interface LayoutDelete {
        void layoutDelete(int position);
    }

    public void layoutDeleteFromAdapter(LayoutDelete layoutDelete) {
        this.layoutDelete = layoutDelete;
    }
    //End of New Code

    public void addressCallback(AddressChanged addressChanged) {
        this.addressChanged = addressChanged;
    }

    public void deletePointPressed(deleteViaPoint deleteViaPoint) {
        this.deleteViaPoint = deleteViaPoint;
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        stopAutoSuggest = true;
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(dataList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(dataList, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        notifyDataSetChanged();

        new Handler().postDelayed(() -> stopAutoSuggest = false, 1000);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onItemDismiss(int position) {
        stopAutoSuggest = true;
        if (position > 0 && position < dataList.size() - 1) {
            dataList.remove(position);
            notifyItemRemoved(position);
        }
        notifyDataSetChanged();
        new Handler().postDelayed(() -> stopAutoSuggest = false, 1000);
    }

    public interface AddressChanged {
        void onAddressChanged(int position);
    }

    public interface deleteViaPoint {
        void deleteViaPointPressed(int position);
    }

    public ViaPointAdapter(Context context, ArrayList<ViaPointPojo> dataList, RecyclerView suggestionRv, ProgressBar apiProgress) {
        this.context = context;
        this.suggestionRv = suggestionRv;
        this.dataList = dataList;
        this.apiProgress = apiProgress;
    }


    @NonNull
    @Override
    public ViaPoint onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.via_point_item, parent, false);
        return new ViaPoint(view, new AutoCompleteTextWatcher(context, suggestionRv, apiProgress),this);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViaPoint holder, int position) {

        holder.viaPointEt.setText(dataList.get(position).getAddress());
        holder.viaPointEt.setTextColor(dataList.get(position).getColor());

        if (position > 0 && position < dataList.size() - 1) {
            // not source not destination

//            holder.markerView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            holder.markerView.setVisibility(View.INVISIBLE);
            holder.markerNo.setText("" + position);
            holder.markerNo.setVisibility(View.VISIBLE);
            holder.deleteViaBtn.setVisibility(View.VISIBLE);
            holder.viaPointEt.setHint("Add Via Point " + position);
        } else if (position == 0) {
            // source point
            holder.markerNo.setVisibility(View.INVISIBLE);
            holder.deleteViaBtn.setVisibility(View.INVISIBLE);
            holder.markerView.setImageResource(R.drawable.ic_current_location);
//            holder.markerView.setCompoundDrawablesWithIntrinsicBounds(null, context.getResources().getDrawable(R.drawable.ic_current_location), null, null);
            holder.markerView.setVisibility(View.VISIBLE);
        } else if (position == dataList.size() - 1) {
            // destination point
            holder.markerNo.setVisibility(View.INVISIBLE);
            holder.deleteViaBtn.setVisibility(View.INVISIBLE);
            holder.markerView.setImageResource(R.drawable.ic_destination_marker_24dp);
//            holder.markerView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, context.getResources().getDrawable(R.drawable.ic_destination_marker_24dp));
            holder.markerView.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    static class ViaPoint extends RecyclerView.ViewHolder {
        WeakReference<ViaPointAdapter> viaPointAdapterWeakReference;
        EditText viaPointEt;
        AutoCompleteTextWatcher textWatcher;
        TextView markerNo;
        ImageView markerView;
        Button deleteViaBtn;

        @SuppressLint({"ClickableViewAccessibility", "LogNotTimber"})
        private ViaPoint(View view, AutoCompleteTextWatcher textWatcher,ViaPointAdapter viaPointAdapter) {
            super(view);
            viaPointAdapterWeakReference=new WeakReference<>(viaPointAdapter);
            viaPointEt = view.findViewById(R.id.viaPointEt);
            markerView = view.findViewById(R.id.markerView);
            markerNo = view.findViewById(R.id.markerNo);
            deleteViaBtn = view.findViewById(R.id.deleteViaBtn);
            this.textWatcher = textWatcher;

            deleteViaBtn.setOnClickListener(v -> {
                ViaPointAdapter viaPointAdapter1 = viaPointAdapterWeakReference.get();

                if (getAdapterPosition() >= 0 && !viaPointEt.getText().toString().equals("")){
                    viaPointAdapter1.deleteViaPoint.deleteViaPointPressed(getAdapterPosition());
                } else if (getAdapterPosition() >= 0 && viaPointEt.getText().toString().equals("")){
                    viaPointAdapter1.layoutDelete.layoutDelete(getAdapterPosition());
                }
            });

            viaPointEt.setOnTouchListener((v, event) -> {
                ViaPointAdapter viaPointAdapter1=viaPointAdapterWeakReference.get();
                if (event.getAction() == MotionEvent.ACTION_DOWN && getAdapterPosition() != -1) {
                    if (!viaPointAdapter1.dataList.get(getAdapterPosition()).getAddress().equals("") && (getAdapterPosition() > 0 && getAdapterPosition() < viaPointAdapter1.dataList.size() - 1)){
//                        viaPointAdapter1.dataList.get(getAdapterPosition()).setAddress("");
//                        viaPointEt.setText("");
//                        if (getAdapterPosition()>=viaPoints.size()) viaPoints.remove(0);
//                        else viaPoints.remove(getAdapterPosition());

                        viaPointAdapter1.updation.updateThisVia();
                    }
                    viaPointAdapter1.addressChanged.onAddressChanged(getAdapterPosition());

                    viaPointAdapter1.removeTextWatcher(viaPointEt);

                    Log.d("tag", "onBindViewHolder: down " + viaPointAdapter1.dataList.get(getAdapterPosition()).getColor());
                    if (viaPointAdapter1.dataList.get(getAdapterPosition()).getColor() == -1) {
                        /*white color means source*/

                        currentLocChanging = true;
                        destionationChanging = false;
                        viaPointChanging = false;
                    } else if (viaPointAdapter1.dataList.get(getAdapterPosition()).getColor() ==
                            viaPointAdapter1.context.getResources().getColor(R.color.app_theme_color)) {
                        /*app theme color color means destination*/

                        destionationChanging = true;
                        currentLocChanging = false;
                        viaPointChanging = false;
                    } else if (viaPointAdapter1.dataList.get(getAdapterPosition()).getColor() ==
                            viaPointAdapter1.context.getResources().getColor(R.color.lightGrey)) {
                        /* means via points are adding */
                        viaPointChanging = true;
                        destionationChanging = false;
                        currentLocChanging = false;
                    }
                    viaPointAdapter1.suggestionRv.setVisibility(View.GONE);

                    viaPointAdapter1.addTextWatcher(viaPointEt);


                    return false;
                }

                return false;
            });

        }
    }

    private void removeTextWatcher(EditText viaPointEt) {

        AutoCompleteTextWatcher oldWatcher = (AutoCompleteTextWatcher) viaPointEt.getTag();
        if (oldWatcher != null)
            viaPointEt.removeTextChangedListener(oldWatcher);
    }

    private void addTextWatcher(EditText viaPointEt) {
        AutoCompleteTextWatcher textWatcher1 = new AutoCompleteTextWatcher(context, suggestionRv, apiProgress);

        viaPointEt.addTextChangedListener(textWatcher1);

        viaPointEt.setTag(textWatcher1);
    }
}
