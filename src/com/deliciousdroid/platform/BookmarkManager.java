/*
 * DeliciousDroid - http://code.google.com/p/DeliciousDroid/
 *
 * Copyright (C) 2010 Matt Schmidt
 *
 * DeliciousDroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * DeliciousDroid is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DeliciousDroid; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */

package com.deliciousdroid.platform;

import com.deliciousdroid.providers.BookmarkContent.Bookmark;
import com.deliciousdroid.util.Md5Hash;

import android.content.ContentValues;
import android.content.Context;
import android.provider.BaseColumns;
import android.util.Log;

public class BookmarkManager {
	
	public static void AddBookmark(Bookmark bookmark, String account, Context context) {
		String url = bookmark.getUrl();

		if(url.lastIndexOf("/") <= 7){
			url = url + "/";
		}
		
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
		values.put(Bookmark.Account, account);
		
		context.getContentResolver().insert(Bookmark.CONTENT_URI, values);
	}
	
	public static void UpdateBookmark(Bookmark bookmark, String account, Context context){
		
		String selection = Bookmark.Hash + "='" + bookmark.getHash() + "' AND " +
							Bookmark.Account + " = '" + account + "'";
		
		ContentValues values = new ContentValues();
		values.put(Bookmark.Description, bookmark.getDescription());
		values.put(Bookmark.Url, bookmark.getUrl());
		values.put(Bookmark.Notes, bookmark.getNotes());
		values.put(Bookmark.Tags, bookmark.getTags());
		values.put(Bookmark.Meta, bookmark.getMeta());
		values.put(Bookmark.Time, bookmark.getTime());
		
		context.getContentResolver().update(Bookmark.CONTENT_URI, values, selection, null);
		
	}

	public static void DeleteBookmark(Bookmark bookmark, Context context){
		
		String selection = BaseColumns._ID + "=" + bookmark.getId();
		
		context.getContentResolver().delete(Bookmark.CONTENT_URI, selection, null);
	}
	
	public static void SetLastUpdate(Bookmark bookmark, Long lastUpdate, String account, Context context){
		
		String selection = Bookmark.Hash + "='" + bookmark.getHash() + "' AND " +
							Bookmark.Account + " = '" + account + "'";
		
		ContentValues values = new ContentValues();	
		values.put(Bookmark.LastUpdate, lastUpdate);
		
		context.getContentResolver().update(Bookmark.CONTENT_URI, values, selection, null);
	}
	
	public static void DeleteOldBookmarks(Long lastUpdate, String account, Context context){
		String selection = "(" + Bookmark.LastUpdate + "<" + Long.toString(lastUpdate) + " OR " +
		Bookmark.LastUpdate + " is null) AND " +
		Bookmark.Account + " = '" + account + "'";
		
		Log.d("DeleteOldSelection", selection);
		
		context.getContentResolver().delete(Bookmark.CONTENT_URI, selection, null);
	}
}