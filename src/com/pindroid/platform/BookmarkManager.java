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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.text.TextUtils;

public class BookmarkManager {
	
	public static Cursor GetBookmarks(String username, String tagname, boolean unread, String sortorder, Context context){
		final String[] projection = new String[] {Bookmark._ID, Bookmark.Url, Bookmark.Description, 
				Bookmark.Meta, Bookmark.Tags, Bookmark.ToRead, Bookmark.Shared};
		String selection = null;
		String[] selectionargs = new String[]{username, "% " + tagname + " %", 
				"% " + tagname, tagname + " %", tagname};
		
		if(tagname != null && tagname != "") {
			selection = Bookmark.Account + "=? AND " +
				"(" + Bookmark.Tags + " LIKE ? OR " +
				Bookmark.Tags + " LIKE ? OR " +
				Bookmark.Tags + " LIKE ? OR " +
				Bookmark.Tags + " = ?)";

		} else {
			selectionargs = new String[]{username};
			selection = Bookmark.Account + "=?";
		}
		if(unread) {
			selection += " AND " + Bookmark.ToRead + "=1";
		}
		
		return context.getContentResolver().query(Bookmark.CONTENT_URI, projection, selection, selectionargs, sortorder);
	}
	
	public static Bookmark GetById(int id, Context context) throws ContentNotFoundException {		
		final String[] projection = new String[] {Bookmark.Account, Bookmark.Url, Bookmark.Description, Bookmark.Notes, Bookmark.Time, Bookmark.Tags, Bookmark.Hash, Bookmark.Meta, Bookmark.ToRead, Bookmark.Shared};
		final String selection = BaseColumns._ID + "=?";
		final String[] selectionargs = new String[]{Integer.toString(id)};
		
		Cursor c = context.getContentResolver().query(Bookmark.CONTENT_URI, projection, selection, selectionargs, null);				
		
		if(c.moveToFirst()){
			final int accountColumn = c.getColumnIndex(Bookmark.Account);
			final int urlColumn = c.getColumnIndex(Bookmark.Url);
			final int descriptionColumn = c.getColumnIndex(Bookmark.Description);
			final int notesColumn = c.getColumnIndex(Bookmark.Notes);
			final int tagsColumn = c.getColumnIndex(Bookmark.Tags);
			final int hashColumn = c.getColumnIndex(Bookmark.Hash);
			final int metaColumn = c.getColumnIndex(Bookmark.Meta);
			final int timeColumn = c.getColumnIndex(Bookmark.Time);
			final int readColumn = c.getColumnIndex(Bookmark.ToRead);
			final int shareColumn = c.getColumnIndex(Bookmark.Shared);
			
			final boolean read = c.getInt(readColumn) == 0 ? false : true;
			final boolean share = c.getInt(shareColumn) == 0 ? false : true;

			Bookmark b = new Bookmark(id, c.getString(accountColumn), c.getString(urlColumn), 
				c.getString(descriptionColumn), c.getString(notesColumn), c.getString(tagsColumn),
				c.getString(hashColumn), c.getString(metaColumn), c.getLong(timeColumn), read, share);
			
			c.close();
			
			return b;
		} else {
			c.close();
			throw new ContentNotFoundException();
		}
	}
	
	public static Bookmark GetByUrl(String url, Context context) throws ContentNotFoundException {		
		final String[] projection = new String[] {Bookmark._ID, Bookmark.Account, Bookmark.Url, Bookmark.Description, Bookmark.Notes, Bookmark.Time, Bookmark.Tags, Bookmark.Hash, Bookmark.Meta, Bookmark.ToRead, Bookmark.Shared};
		final String selection = Bookmark.Url + "=?";
		final String[] selectionargs = new String[]{ url };
		
		Cursor c = context.getContentResolver().query(Bookmark.CONTENT_URI, projection, selection, selectionargs, null);				
		
		if(c.moveToFirst()){
			final int idColumn = c.getColumnIndex(Bookmark._ID);
			final int accountColumn = c.getColumnIndex(Bookmark.Account);
			final int urlColumn = c.getColumnIndex(Bookmark.Url);
			final int descriptionColumn = c.getColumnIndex(Bookmark.Description);
			final int notesColumn = c.getColumnIndex(Bookmark.Notes);
			final int tagsColumn = c.getColumnIndex(Bookmark.Tags);
			final int hashColumn = c.getColumnIndex(Bookmark.Hash);
			final int metaColumn = c.getColumnIndex(Bookmark.Meta);
			final int timeColumn = c.getColumnIndex(Bookmark.Time);
			final int readColumn = c.getColumnIndex(Bookmark.ToRead);
			final int shareColumn = c.getColumnIndex(Bookmark.Shared);
			
			final boolean read = c.getInt(readColumn) == 0 ? false : true;
			final boolean share = c.getInt(shareColumn) == 0 ? false : true;

			Bookmark b = new Bookmark(c.getInt(idColumn), c.getString(accountColumn), c.getString(urlColumn), 
				c.getString(descriptionColumn), c.getString(notesColumn), c.getString(tagsColumn),
				c.getString(hashColumn), c.getString(metaColumn), c.getLong(timeColumn), read, share);
			
			c.close();
			
			return b;
		} else {
			c.close();
			throw new ContentNotFoundException();
		}
	}
	
	public static void AddBookmark(Bookmark bookmark, String account, Context context) {
		final String url = bookmark.getUrl();
		
		String hash = "";
		if(bookmark.getHash() == null || bookmark.getHash() == ""){
			hash = Md5Hash.md5(url);
		} else hash = bookmark.getHash();
		
		final ContentValues values = new ContentValues();
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
	
	public static void BulkInsert(ArrayList<Bookmark> list, String account, Context context) {
		int bookmarksize = list.size();
		ContentValues[] bcv = new ContentValues[bookmarksize];
		
		for(int i = 0; i < bookmarksize; i++){
			Bookmark b = list.get(i);
			
			ContentValues values = new ContentValues();
			values.put(Bookmark.Description, b.getDescription());
			values.put(Bookmark.Url, b.getUrl());
			values.put(Bookmark.Notes, b.getNotes());
			values.put(Bookmark.Tags, b.getTagString());
			values.put(Bookmark.Hash, b.getHash());
			values.put(Bookmark.Meta, b.getMeta());
			values.put(Bookmark.Time, b.getTime());
			values.put(Bookmark.Account, account);
			values.put(Bookmark.ToRead, b.getToRead() ? 1 : 0);
			values.put(Bookmark.Shared, b.getShared() ? 1 : 0);
			
			bcv[i] = values;
		}
		
		context.getContentResolver().bulkInsert(Bookmark.CONTENT_URI, bcv);
	}
	
	public static void UpdateBookmark(Bookmark bookmark, String account, Context context){
		final String url = bookmark.getUrl();
		
		String hash = "";
		if(bookmark.getHash() == null || bookmark.getHash() == ""){
			hash = Md5Hash.md5(url);
		} else hash = bookmark.getHash();
		
		final String selection = Bookmark.Hash + "=? AND " +
							Bookmark.Account + "=?";
		final String[] selectionargs = new String[]{hash, account};
		
		final ContentValues values = new ContentValues();
		values.put(Bookmark.Description, bookmark.getDescription());
		values.put(Bookmark.Url, url);
		values.put(Bookmark.Notes, bookmark.getNotes());
		values.put(Bookmark.Tags, bookmark.getTagString());
		values.put(Bookmark.Meta, bookmark.getMeta());
		
		if(bookmark.getTime() > 0)
			values.put(Bookmark.Time, bookmark.getTime());
		
		values.put(Bookmark.ToRead, bookmark.getToRead() ? 1 : 0);
		values.put(Bookmark.Shared, bookmark.getShared() ? 1 : 0);
		
		context.getContentResolver().update(Bookmark.CONTENT_URI, values, selection, selectionargs);
	}

	public static void DeleteBookmark(Bookmark bookmark, Context context){
		final int id = bookmark.getId();
		String selection = "";
		
		if(id > 0) {
			selection = BaseColumns._ID + "=" + id;
		} else {
			selection = Bookmark.Url + "='" + bookmark.getUrl() + "'";
		}
		
		context.getContentResolver().delete(Bookmark.CONTENT_URI, selection, null);
	}
	
	public static void TruncateBookmarks(ArrayList<String> accounts, Context context, boolean inverse){
		
		final ArrayList<String> selectionList = new ArrayList<String>();
		
		final String operator = inverse ? "<>" : "=";
		final String logicalOp = inverse ? " AND " : " OR ";
		
		for(String s : accounts) {
			selectionList.add(Bookmark.Account + " " + operator + " '" + s + "'");
		}
		
		final String selection = TextUtils.join(logicalOp, selectionList);
		
		context.getContentResolver().delete(Bookmark.CONTENT_URI, selection, null);
	}
	
	public static Cursor SearchBookmarks(String query, String tagname, boolean unread, String username, Context context) {
		final String[] projection = new String[] {Bookmark._ID, Bookmark.Url, Bookmark.Description, 
				Bookmark.Meta, Bookmark.Tags, Bookmark.Shared, Bookmark.ToRead};
		String selection = null;
		
		final String sortorder = Bookmark.Description + " ASC";
		
		final String[] queryBookmarks = query.split(" ");
		
		final ArrayList<String> queryList = new ArrayList<String>();
		final ArrayList<String> selectionlist = new ArrayList<String>();
		
		if(query != null && query != "" && (tagname == null || tagname == "")) {
			
			
			for(String s : queryBookmarks) {
				queryList.add("(" + Bookmark.Tags + " LIKE ? OR " +
						Bookmark.Description + " LIKE ? OR " +
						Bookmark.Notes + " LIKE ?)");
				selectionlist.add("%" + s + "%");
				selectionlist.add("%" + s + "%");
				selectionlist.add("%" + s + "%");
			}
			selectionlist.add(username);
			
			selection = TextUtils.join(" AND ", queryList) + " AND " +
				Bookmark.Account + "=?";
		} else if(query != null && query != ""){
			for(String s : queryBookmarks) {
				queryList.add("(" + Bookmark.Description + " LIKE ? OR " +
						Bookmark.Notes + " LIKE ?)");
				
				selectionlist.add("%" + s + "%");
				selectionlist.add("%" + s + "%");
			}

			selection = TextUtils.join(" AND ", queryList) +
				" AND " + Bookmark.Account + "=? AND " +
				"(" + Bookmark.Tags + " LIKE '% " + tagname + " %' OR " +
				Bookmark.Tags + " LIKE '% " + tagname + "' OR " +
				Bookmark.Tags + " LIKE '" + tagname + " %' OR " +
				Bookmark.Tags + " = '" + tagname + "')";
			
			selectionlist.add(username);
			selectionlist.add("% " + tagname + " %");
			selectionlist.add("% " + tagname);
			selectionlist.add(tagname + " %");
			selectionlist.add(tagname);
		} else {
			selectionlist.add(username);
			selection = Bookmark.Account + "=?";
		}
		
		if(unread) {
			selection += " AND " + Bookmark.ToRead + "=1";
		}
		
		return context.getContentResolver().query(Bookmark.CONTENT_URI, projection, selection, selectionlist.toArray(new String[]{}), sortorder);
	}
	
	public static int GetUnreadCount(String username, Context context){		
		final String[] projection = new String[] {Bookmark._ID};
		final String selection = Bookmark.Account + "=? AND " + Bookmark.ToRead + "=1";
		final String[] selectionargs = new String[]{username};
		
		final Cursor c = context.getContentResolver().query(Bookmark.CONTENT_URI, projection, selection, selectionargs, null);				
		
		final int count = c.getCount();
		
		c.close();
		return count;
	}
	
	public static Bookmark CursorToBookmark(Cursor c) {
		Bookmark b = new Bookmark();
		b.setId(c.getInt(c.getColumnIndex(Bookmark._ID)));
		b.setDescription(c.getString(c.getColumnIndex(Bookmark.Description)));
		b.setUrl(c.getString(c.getColumnIndex(Bookmark.Url)));
		b.setMeta(c.getString(c.getColumnIndex(Bookmark.Meta)));
		b.setTagString(c.getString(c.getColumnIndex(Bookmark.Tags)));
		b.setToRead(c.getInt(c.getColumnIndex(Bookmark.ToRead)) == 1 ? true : false);
		
		if(c.getColumnIndex(Bookmark.Source) != -1)
			b.setSource(c.getString(c.getColumnIndex(Bookmark.Source)));
		
		if(c.getColumnIndex(Bookmark.Account) != -1)
			b.setAccount(c.getString(c.getColumnIndex(Bookmark.Account)));
		
		if(c.getColumnIndex(Bookmark.Notes) != -1)
			b.setNotes(c.getString(c.getColumnIndex(Bookmark.Notes)));
		
		if(c.getColumnIndex(Bookmark.Time) != -1)
			b.setTime(c.getLong(c.getColumnIndex(Bookmark.Time)));
		
		if(c.getColumnIndex(Bookmark.Shared) != -1)
			b.setShared(c.getInt(c.getColumnIndex(Bookmark.Shared)) == 1 ? true : false);
		
		return b;
	}
}