package com.deliciousdroid.listadapter;

import android.widget.TextView;

public class ViewHolder {

	static class BookmarkListViewHolder {
		TextView description;
		TextView tags;
	}
	
	static class TagListViewHolder {
		TextView name;
		TextView count;
	}
}
