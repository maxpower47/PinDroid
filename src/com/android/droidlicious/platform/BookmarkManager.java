package com.android.droidlicious.platform;

import com.android.droidlicious.providers.BookmarkContent.Bookmark;

import android.content.Context;
import android.provider.BaseColumns;

public class BookmarkManager {

	public static void DeleteBookmark(Bookmark bookmark, Context context){
		
		String selection = BaseColumns._ID + "=" + bookmark.getId();
		
		context.getContentResolver().delete(Bookmark.CONTENT_URI, selection, null);
	}
}
