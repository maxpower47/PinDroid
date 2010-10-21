package com.android.droidlicious.platform;

import com.android.droidlicious.providers.BookmarkContent.Bookmark;
import com.android.droidlicious.util.Md5Hash;

import android.content.ContentValues;
import android.content.Context;
import android.provider.BaseColumns;
import android.util.Log;

public class BookmarkManager {
	
	public static void AddBookmark(Bookmark bookmark, Context context){
		String url = bookmark.getUrl();

		url = url + "/";
		Log.d("url", url);
		
		String hash = "";
		if(bookmark.getHash() == null || bookmark.getHash() == ""){
			hash = Md5Hash.md5(url);
			Log.d(url, hash);
		} else hash = bookmark.getHash();
		
		ContentValues values = new ContentValues();
		
		values.put(Bookmark.Description, bookmark.getDescription());
		values.put(Bookmark.Url, url);
		values.put(Bookmark.Notes, bookmark.getNotes());
		values.put(Bookmark.Tags, bookmark.getTags());
		values.put(Bookmark.Hash, hash);
		values.put(Bookmark.Meta, bookmark.getMeta());
		values.put(Bookmark.Time, bookmark.getTime());
		
		context.getContentResolver().insert(Bookmark.CONTENT_URI, values);
	}

	public static void DeleteBookmark(Bookmark bookmark, Context context){
		
		String selection = BaseColumns._ID + "=" + bookmark.getId();
		
		context.getContentResolver().delete(Bookmark.CONTENT_URI, selection, null);
	}
}
