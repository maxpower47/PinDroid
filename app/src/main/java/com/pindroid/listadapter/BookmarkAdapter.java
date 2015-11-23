package com.pindroid.listadapter;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;

import com.h6ah4i.android.widget.advrecyclerview.swipeable.LegacySwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter;
import com.pindroid.R;
import com.pindroid.event.BookmarkSelectedEvent;
import com.pindroid.platform.BookmarkManager;
import com.pindroid.model.Bookmark;
import com.pindroid.ui.BookmarkView;
import com.pindroid.ui.BookmarkView_;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import de.greenrobot.event.EventBus;

@EBean
public class BookmarkAdapter extends RecyclerCursorAdapter<Bookmark, BookmarkView>
        implements LegacySwipeableItemAdapter<ViewWrapper<BookmarkView>> {

    @RootContext Context context;
    private EventListener listener;

    public interface EventListener {
        void onBookmarkDeleted(Bookmark bookmark);
        void onBookmarkMarked(Bookmark bookmark);
    }

    public BookmarkAdapter(Context context) {
        super(context, null);
    }

    @Override
    protected BookmarkView onCreateItemView(ViewGroup parent, int viewType) {
        return BookmarkView_.build(context);
    }

    @Override
    public void onBindViewHolder(BookmarkView view, Cursor c, ViewWrapper wrapper) {
        final Bookmark bookmark = new Bookmark(c);
        view.bind(bookmark);

        view.getSwipableViewContainter().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new BookmarkSelectedEvent(bookmark));
            }
        });

        view.getSwipableViewContainter().setBackgroundResource(R.drawable.bg_item_swiping_neutral);

        wrapper.setMaxRightSwipeAmount(0.25f);
        wrapper.setMaxLeftSwipeAmount(-0.5f);
    }

    @Override
    public int onGetSwipeReactionType(ViewWrapper<BookmarkView> bookmarkView, int i, int i1, int i2) {
        return RecyclerViewSwipeManager.REACTION_CAN_SWIPE_BOTH;
    }

    @Override
    public void onSetSwipeBackground(ViewWrapper<BookmarkView> holder, int position, int type) {
        switch (type) {
            case RecyclerViewSwipeManager.DRAWABLE_SWIPE_NEUTRAL_BACKGROUND:
                break;
            case RecyclerViewSwipeManager.DRAWABLE_SWIPE_LEFT_BACKGROUND:
                holder.itemView.setBackgroundResource(R.drawable.bg_item_swiping_left);
                break;
            case RecyclerViewSwipeManager.DRAWABLE_SWIPE_RIGHT_BACKGROUND:
                if(new Bookmark(getItemAtPosition(position)).getToRead()) {
                    holder.itemView.setBackgroundResource(R.drawable.bg_item_swiping_right_read);
                } else {
                    holder.itemView.setBackgroundResource(R.drawable.bg_item_swiping_right_unread);
                }
                break;
        }
    }

    @Override
    public int onSwipeItem(ViewWrapper<BookmarkView> bookmarkView, int position, int result) {
        switch (result) {
            case RecyclerViewSwipeManager.RESULT_SWIPED_RIGHT:
                return RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_DEFAULT;
            case RecyclerViewSwipeManager.RESULT_SWIPED_LEFT:
                return RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_REMOVE_ITEM;
            case RecyclerViewSwipeManager.RESULT_CANCELED:
            default:
                return RecyclerViewSwipeManager.AFTER_SWIPE_REACTION_DEFAULT;
        }
    }

    @Override
    public void onPerformAfterSwipeReaction(ViewWrapper<BookmarkView> bookmarkView, int position, int result, int reaction) {

        switch (result) {
            case RecyclerViewSwipeManager.RESULT_SWIPED_RIGHT:
                notifyItemChanged(position);

                if (listener != null) {
                    listener.onBookmarkMarked(new Bookmark(getItemAtPosition(position)));
                }
                break;
            case RecyclerViewSwipeManager.RESULT_SWIPED_LEFT:
                notifyItemRemoved(position);

                if (listener != null) {
                    listener.onBookmarkDeleted(new Bookmark(getItemAtPosition(position)));
                }
                break;
            default:
                break;
        }
    }

    public void setEventListener(EventListener listener) {
        this.listener = listener;
    }
}