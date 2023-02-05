package com.suzuki.interfaces;

import androidx.recyclerview.widget.RecyclerView;

public interface StartDragListener {
    void onConnected();

    void requestDrag(RecyclerView.ViewHolder viewHolder);
}
