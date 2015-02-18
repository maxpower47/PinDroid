package com.pindroid.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.pindroid.model.FeedBookmark;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.List;

@EBean
public class BookmarkFeedAdapter extends BaseAdapter {

	List<FeedBookmark> feedBookmarks;

	@RootContext Context context;

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		BookmarkFeedView bookmarkFeedView;
		if (convertView == null) {
			bookmarkFeedView = BookmarkFeedView_.build(context);
		} else {
			bookmarkFeedView = (BookmarkFeedView) convertView;
		}

		bookmarkFeedView.bind(getItem(position));

		return bookmarkFeedView;
	}

	@Override
	public int getCount() {
		return feedBookmarks == null ? 0 : feedBookmarks.size();
	}

	@Override
	public FeedBookmark getItem(int position) {
		return feedBookmarks.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void setFeedBookmarks(List<FeedBookmark> feedBookmarks) {
		this.feedBookmarks = feedBookmarks;
		notifyDataSetChanged();
	}
}
