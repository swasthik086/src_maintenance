package com.suzuki.adapter;

import android.content.Context;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Sahil Sharma on 12-Jan-16.
 */
public class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {
    private Context context;
    private OnMyItemClickListener myItemClickListener;
    private GestureDetector mGestureDetector;

    public RecyclerItemClickListener(Context context, OnMyItemClickListener listener) {
        this.context = context;
        myItemClickListener = listener;
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            //Manually Override method of CLASS GestureDetector.SimpleOnGestureListener
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });
    }

    //abstract method of RecyclerView.OnItemTouchListener Interface
    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        View childView = rv.findChildViewUnder(e.getX(), e.getY());
        if (childView != null && myItemClickListener != null && mGestureDetector.onTouchEvent(e)) {
            myItemClickListener.onItemClick(childView, rv.getChildAdapterPosition(childView));
        }
        return false;
    }

    //abstract method of RecyclerView.OnItemTouchListener Interface
    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
    }

    //abstract method of RecyclerView.OnItemTouchListener Interface
    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }

    public interface OnMyItemClickListener {
        void onItemClick(View view, int position);
    }
}
