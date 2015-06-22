package com.pindroid.listadapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractSwipeableItemViewHolder;

public class ViewWrapper<V extends View & SwipableView> extends AbstractSwipeableItemViewHolder {

    private final V view;

    public ViewWrapper(V itemView) {
        super(itemView);
        view = itemView;
    }

    public V getView() {
        return view;
    }

    @Override
    public View getSwipeableContainerView() {
        return view.getSwipableViewContainter();
    }
}