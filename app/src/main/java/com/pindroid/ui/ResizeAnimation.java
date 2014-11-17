package com.pindroid.ui;

import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

public class ResizeAnimation extends Animation {
    int originalHeight;
    int targetHeight;
    int offsetHeight;
    View view;
    boolean down;

    //This constructor allow us to set a starting height
    public ResizeAnimation(View view, int originalHeight, int targetHeight, boolean down, DisplayMetrics displayMetrics) {

        this.view           = view;
        this.originalHeight = originalHeight;
        this.targetHeight   = targetHeight;
        this.offsetHeight   = targetHeight - originalHeight;
        this.down           = down;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        int newHeight;
        if (down) {
            newHeight = (int) (offsetHeight * interpolatedTime);
        } else {
            newHeight = (int) (offsetHeight * (1 - interpolatedTime));
        }

        //The new view height is based on start height plus the height increment
        view.getLayoutParams().height = newHeight + originalHeight;

        view.requestLayout();
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }
}