package com.pindroid.listadapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.pindroid.event.FeedBookmarkSelectedEvent;
import com.pindroid.model.FeedBookmark;
import com.pindroid.ui.BookmarkFeedView;
import com.pindroid.ui.BookmarkFeedView_;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.List;

import de.greenrobot.event.EventBus;

@EBean
public class BookmarkFeedAdapter extends RecyclerAdapter<FeedBookmark, BookmarkFeedView> {

	@RootContext Context context;

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void setFeedBookmarks(List<FeedBookmark> feedBookmarks) {
        items = feedBookmarks;
		notifyDataSetChanged();
	}

    public FeedBookmark getItemAtPosition(int position) {
        return items.get(position);
    }

    @Override
    protected BookmarkFeedView onCreateItemView(ViewGroup parent, int viewType) {
        return BookmarkFeedView_.build(context);
    }

    @Override
    public void onBindViewHolder(ViewWrapper<BookmarkFeedView> viewHolder, int position) {
        BookmarkFeedView view = viewHolder.getView();
        final FeedBookmark bookmark = items.get(position);
        view.bind(bookmark);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new FeedBookmarkSelectedEvent(bookmark));
            }
        });
    }
}
