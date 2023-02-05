package com.suzuki.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.suzuki.R;


public class RecenterButton extends LinearLayout {

    private Animation slideUpBottom;

    public RecenterButton(Context context) {
        this(context, null);
    }

    public RecenterButton(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public RecenterButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * Sets visibility to VISIBLE and starts custom animation.
     *
     * @since 0.6.0
     */
    public void show() {
        setVisibility(VISIBLE);
        startAnimation(slideUpBottom);
    }

    /**
     * Sets visibility to INVISIBLE.
     *
     * @since 0.6.0
     */
    public void hide() {
        setVisibility(INVISIBLE);
    }

    /**
     * Once inflation of the view has finished,
     * create the custom animation.
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initAnimation();
    }

    /**
     * Inflates the layout.
     */
    private void init() {
        inflate(getContext(), R.layout.recenter_btn_layout, this);
    }

    /**
     * Creates the custom animation used to show this button.
     */
    private void initAnimation() {
        slideUpBottom = new TranslateAnimation(0f, 0f, 125f, 0f);
        slideUpBottom.setDuration(300);
        slideUpBottom.setInterpolator(new OvershootInterpolator(2.0f));
    }


}