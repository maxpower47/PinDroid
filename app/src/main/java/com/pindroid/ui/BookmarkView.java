package com.pindroid.ui;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pindroid.R;
import com.pindroid.listadapter.SwipableView;
import com.pindroid.model.Bookmark;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

@EViewGroup(R.layout.bookmark_view)
public class BookmarkView extends LinearLayout implements SwipableView {

    @ViewById(R.id.bookmark_view_container) View containerView;
    @ViewById(R.id.bookmark_description) TextView descriptionView;
    @ViewById(R.id.bookmark_tags) TextView tagsView;
    @ViewById(R.id.bookmark_unread) ImageView unreadView;
    @ViewById(R.id.bookmark_private) ImageView privateView;
    @ViewById(R.id.bookmark_synced) ImageView syncedView;

    public BookmarkView(Context context) {
        super(context);
    }

    public void bind(Bookmark bookmark) {
        descriptionView.setText(bookmark.getDescription());
        tagsView.setText(bookmark.getTagString());

        if(bookmark.getToRead()) {
            unreadView.setVisibility(View.VISIBLE);
        } else {
            unreadView.setVisibility(View.INVISIBLE);
        }

        if(bookmark.getShared()) {
            privateView.setVisibility(View.INVISIBLE);
        } else {
            privateView.setVisibility(View.VISIBLE);
        }

        switch (bookmark.getSynced()) {

            case 1:
                syncedView.setImageResource(R.drawable.sync);
                syncedView.setVisibility(View.VISIBLE);
                break;
            case -1:
                syncedView.setImageResource(R.drawable.sync_fail);
                syncedView.setVisibility(View.VISIBLE);
                break;
            default:
                syncedView.setVisibility(View.INVISIBLE);
                break;
        }
    }

    @Override
    public View getSwipableViewContainter() {
        return containerView;
    }
}
