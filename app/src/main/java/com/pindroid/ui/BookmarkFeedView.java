package com.pindroid.ui;

import android.content.Context;
import android.text.TextUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pindroid.R;
import com.pindroid.model.FeedBookmark;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

@EViewGroup(R.layout.bookmark_feed_view)
public class BookmarkFeedView extends LinearLayout {

	@ViewById(R.id.bookmark_feed_description) TextView description;
	@ViewById(R.id.bookmark_feed_tags) TextView tags;

	public BookmarkFeedView(Context context) {
		super(context);
	}

	public void bind(FeedBookmark feedBookmark) {
		description.setText(feedBookmark.getDescription());
		tags.setText(feedBookmark.getTagString());
	}
}
