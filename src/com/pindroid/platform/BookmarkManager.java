/*
 * PinDroid - http://code.google.com/p/PinDroid/
 *
 * Copyright (C) 2010 Matt Schmidt
 *
 * PinDroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * PinDroid is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PinDroid; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */

package com.pindroid.platform;

import java.util.ArrayList;

import com.pindroid.providers.ContentNotFoundException;
import com.pindroid.providers.BookmarkContent.Bookmark;
import com.pindroid.util.Md5Hash;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

public class BookmarkManager {
	
	public static ArrayList<Bookmark> GetBookmarks(String username, String tagname, boolean unread, String sortorder, Context context){
		ArrayList<Bookmark> bookmarkList = new ArrayList<Bookmark>();
		String[] projection = new String[] {Bookmark._ID, Bookmark.Url, Bookmark.Description, 
				Bookmark.Meta, Bookmark.Tags, Bookmark.ToRead, Bookmark.Shared};
		String selection = null;
		String[] selectionargs = new String[]{username};
		
		if(tagname != null && tagname != "") {
			selection = "(" + Bookmark.Tags + " LIKE '% " + tagname + " %' OR " +
				Bookmark.Tags + " LIKE '% " + tagname + "' OR " +
				Bookmark.Tags + " LIKE '" + tagname + " %' OR " +
				Bookmark.Tags + " = '" + tagname + "') AND " +
				Bookmark.Account + "=?";
		} else {
			selection = Bookmark.Account + "=?";
		}
		if(unread) {
			selection += " AND " + Bookmark.ToRead + "=1";
		}
		
		Uri bookmarks = Bookmark.CONTENT_URI;
		
		Cursor c = context.getContentResolver().query(bookmarks, projection, selection, selectionargs, sortorder);				
		
		if(c.moveToFirst()){
			int idColumn = c.getColumnIndex(Bookmark._ID);
			int urlColumn = c.getColumnIndex(Bookmark.Url);
			int descriptionColumn = c.getColumnIndex(Bookmark.Description);
			int tagsColumn = c.getColumnIndex(Bookmark.Tags);
			int metaColumn = c.getColumnIndex(Bookmark.Meta);
			int readColumn = c.getColumnIndex(Bookmark.ToRead);
			int shareColumn = c.getColumnIndex(Bookmark.Shared);
			
			do {
				
				Bookmark b = new Bookmark(c.getInt(idColumn), "", c.getString(urlColumn), 
						c.getString(descriptionColumn), "", c.getString(tagsColumn), "", 
						c.getString(metaColumn), 0, c.getInt(readColumn) == 0 ? false : true,
						c.getInt(shareColumn) == 0 ? false : true);
				
				bookmarkList.add(b);
				
			} while(c.moveToNext());
				
		}
		c.close();
		return bookmarkList;
	}
	
	public static Bookmark GetById(int id, Context context) throws ContentNotFoundException {		
		String[] projection = new String[] {Bookmark.Account, Bookmark.Url, Bookmark.Description, Bookmark.Notes, Bookmark.Time, Bookmark.Tags, Bookmark.Hash, Bookmark.Meta, Bookmark.ToRead, Bookmark.Shared};
		String selection = BaseColumns._ID + "=?";
		String[] selectionargs = new String[]{Integer.toString(id)};
		
		Uri bookmarks = Bookmark.CONTENT_URI;
		
		Cursor c = context.getContentResolver().query(bookmarks, projection, selection, selectionargs, null);				
		
		if(c.moveToFirst()){
			int accountColumn = c.getColumnIndex(Bookmark.Account);
			int urlColumn = c.getColumnIndex(Bookmark.Url);
			int descriptionColumn = c.getColumnIndex(Bookmark.Description);
			int notesColumn = c.getColumnIndex(Bookmark.Notes);
			int tagsColumn = c.getColumnIndex(Bookmark.Tags);
			int hashColumn = c.getColumnIndex(Bookmark.Hash);
			int metaColumn = c.getColumnIndex(Bookmark.Meta);
			int timeColumn = c.getColumnIndex(Bookmark.Time);
			int readColumn = c.getColumnIndex(Bookmark.ToRead);
			int shareColumn = c.getColumnIndex(Bookmark.Shared);
			
			String account = c.getString(accountColumn);
			String url = c.getString(urlColumn);
			String description = c.getString(descriptionColumn);
			String notes = c.getString(notesColumn);
			String tags = c.getString(tagsColumn);
			String hash = c.getString(hashColumn);
			String meta = c.getString(metaColumn);
			long time = c.getLong(timeColumn);
			boolean read = c.getInt(readColumn) == 0 ? false : true;
			boolean share = c.getInt(shareColumn) == 0 ? false : true;
			
			c.close();
			
			return new Bookmark(id, account, url, description, notes, tags, hash, meta, time, read, share);
		} else {
			c.close();
			throw new ContentNotFoundException();
		}
	}
	
	public static void AddBookmark(Bookmark bookmark, String account, Context context) {
		String url = bookmark.getUrl();
		
		String hash = "";
		if(bookmark.getHash() == null || bookmark.getHash() == ""){
			hash = Md5Hash.md5(url);
		} else hash = bookmark.getHash();
		
		ContentValues values = new ContentValues();
		values.put(Bookmark.Description, bookmark.getDescription());
		values.put(Bookmark.Url, url);
		values.put(Bookmark.Notes, bookmark.getNotes());
		values.put(Bookmark.Tags, bookmark.getTagString());
		values.put(Bookmark.Hash, hash);
		values.put(Bookmark.Meta, bookmark.getMeta());
		values.put(Bookmark.Time, bookmark.getTime());
		values.put(Bookmark.Account, account);
		values.put(Bookmark.ToRead, bookmark.getToRead() ? 1 : 0);
		values.put(Bookmark.Shared, bookmark.getShared() ? 1 : 0);
		
		context.getContentResolver().insert(Bookmark.CONTENT_URI, values);
	}
	
	public static ContentProviderOperation AddBookmarkBatch(Bookmark bookmark, String account, Context context) {
		String url = bookmark.getUrl();
		
		String hash = "";
		if(bookmark.getHash() == null || bookmark.getHash() == ""){
			hash = Md5Hash.md5(url);
		} else hash = bookmark.getHash();
		
		ContentValues values = new ContentValues();
		values.put(Bookmark.Description, bookmark.getDescription());
		values.put(Bookmark.Url, url);
		values.put(Bookmark.Notes, bookmark.getNotes());
		values.put(Bookmark.Tags, bookmark.getTagString());
		values.put(Bookmark.Hash, hash);
		values.put(Bookmark.Meta, bookmark.getMeta());
		values.put(Bookmark.Time, bookmark.getTime());
		values.put(Bookmark.Account, account);
		values.put(Bookmark.ToRead, bookmark.getToRead() ? 1 : 0);
		values.put(Bookmark.Shared, bookmark.getShared() ? 1 : 0);
		
		return ContentProviderOperation.newInsert(Bookmark.CONTENT_URI).withValues(values).build();
	}
	
	public static void UpdateBookmark(Bookmark bookmark, String account, Context context){
		String url = bookmark.getUrl();
		
		String hash = "";
		if(bookmark.getHash() == null || bookmark.getHash() == ""){
			hash = Md5Hash.md5(url);
			Log.d(url, hash);
		} else hash = bookmark.getHash();
		
		String selection = Bookmark.Hash + "=? AND " +
							Bookmark.Account + "=?";
		String[] selectionargs = new String[]{hash, account};
		
		ContentValues values = new ContentValues();
		values.put(Bookmark.Description, bookmark.getDescription());
		values.put(Bookmark.Url, url);
		values.put(Bookmark.Notes, bookmark.getNotes());
		values.put(Bookmark.Tags, bookmark.getTagString());
		values.put(Bookmark.Meta, bookmark.getMeta());
		values.put(Bookmark.Time, bookmark.getTime());
		values.put(Bookmark.ToRead, bookmark.getToRead() ? 1 : 0);
		values.put(Bookmark.Shared, bookmark.getShared() ? 1 : 0);
		
		context.getContentResolver().update(Bookmark.CONTENT_URI, values, selection, selectionargs);
	}

	public static void DeleteBookmark(Bookmark bookmark, Context context){
		
		String selection = BaseColumns._ID + "=" + bookmark.getId();
		
		context.getContentResolver().delete(Bookmark.CONTENT_URI, selection, null);
	}
	
	public static void TruncateBookmarks(ArrayList<String> accounts, Context context, boolean inverse){
		
		ArrayList<String> selectionList = new ArrayList<String>();
		
		String operator = inverse ? "<>" : "=";
		String logicalOp = inverse ? " AND " : " OR ";
		
		for(String s : accounts) {
			selectionList.add(Bookmark.Account + " " + operator + " '" + s + "'");
		}
		
		String selection = TextUtils.join(logicalOp, selectionList);
		
		context.getContentResolver().delete(Bookmark.CONTENT_URI, selection, null);
	}
	
	public static ArrayList<Bookmark> SearchBookmarks(String query, String tagname, boolean unread, String username, Context context) {
		ArrayList<Bookmark> bookmarkList = new ArrayList<Bookmark>();
		String[] projection = new String[] {Bookmark._ID, Bookmark.Url, Bookmark.Description, 
				Bookmark.Meta, Bookmark.Tags, Bookmark.Shared, Bookmark.ToRead};
		String selection = null;
		String[] selectionargs = new String[]{username};
		String sortorder = null;
		
		String[] queryBookmarks = query.split(" ");
		
		ArrayList<String> queryList = new ArrayList<String>();
		
		if(query != null && query != "" && (tagname == null || tagname == "")) {
			for(String s : queryBookmarks) {
				queryList.add("(" + Bookmark.Tags + " LIKE '%" + s + "%' OR " +
						Bookmark.Description + " LIKE '%" + s + "%' OR " +
						Bookmark.Notes + " LIKE '%" + s + "%')");
			}
			
			selection = TextUtils.join(" AND ", queryList) + " AND " +
				Bookmark.Account + "=?";
		} else if(query != null && query != ""){
			for(String s : queryBookmarks) {
				queryList.add("(" + Bookmark.Description + " LIKE '%" + s + "%' OR " +
						Bookmark.Notes + " LIKE '%" + s + "%')");
			}

			selection = TextUtils.join(" AND ", queryList) +
				" AND " + Bookmark.Account + "=? AND " +
				"(" + Bookmark.Tags + " LIKE '% " + tagname + " %' OR " +
				Bookmark.Tags + " LIKE '% " + tagname + "' OR " +
				Bookmark.Tags + " LIKE '" + tagname + " %' OR " +
				Bookmark.Tags + " = '" + tagname + "')";
		} else {
			selection = Bookmark.Account + "=?";
		}
		
		if(unread) {
			selection += " AND " + Bookmark.ToRead + "=1";
		}
		
		sortorder = Bookmark.Description + " ASC";
		
		Uri bookmarks = Bookmark.CONTENT_URI;
		
		Cursor c = context.getContentResolver().query(bookmarks, projection, selection, selectionargs, sortorder);				
		
		if(c.moveToFirst()){
			int idColumn = c.getColumnIndex(Bookmark._ID);
			int urlColumn = c.getColumnIndex(Bookmark.Url);
			int descriptionColumn = c.getColumnIndex(Bookmark.Description);
			int tagsColumn = c.getColumnIndex(Bookmark.Tags);
			int metaColumn = c.getColumnIndex(Bookmark.Meta);
			int readColumn = c.getColumnIndex(Bookmark.ToRead);
			int shareColumn = c.getColumnIndex(Bookmark.Shared);
			
			do {
				
				Bookmark b = new Bookmark(c.getInt(idColumn), "", c.getString(urlColumn), 
						c.getString(descriptionColumn), "", c.getString(tagsColumn), "", 
						c.getString(metaColumn), 0, c.getInt(readColumn) == 0 ? false : true,
						c.getInt(shareColumn) == 0 ? false : true);
				
				bookmarkList.add(b);
				
			} while(c.moveToNext());
				
		}
		c.close();
		return bookmarkList;
	}
}