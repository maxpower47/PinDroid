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

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.v4.content.CursorLoader;
import android.text.TextUtils;

public class BookmarkManager {
	
	public static CursorLoader GetBookmarks(String username, String tagname, boolean unread, boolean untagged, String sortorder, Context context){
		final String[] projection = new String[] {Bookmark._ID, Bookmark.Url, Bookmark.Description, Bookmark.Notes, Bookmark.Hash,
				Bookmark.Meta, Bookmark.Tags, Bookmark.ToRead, Bookmark.Shared, Bookmark.Synced, Bookmark.Deleted,
				Bookmark.Account, Bookmark.Time};
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
		if(untagged) {
			selection += " AND " + nullOrEmpty(Bookmark.Tags);
		}
		selection += " AND " + Bookmark.Deleted + "=0";
		
		return new CursorLoader(context, Bookmark.CONTENT_URI, projection, selection, selectionargs, sortorder);
	}
	
	public static ArrayList<Bookmark> GetLocalBookmarks(String username, Context context){
		ArrayList<Bookmark> bookmarkList = new ArrayList<Bookmark>();
		final String[] projection = new String[] {Bookmark._ID, Bookmark.Url, Bookmark.Description, Bookmark.Notes, Bookmark.Hash,
				Bookmark.Meta, Bookmark.Tags, Bookmark.ToRead, Bookmark.Shared, Bookmark.Synced, Bookmark.Deleted};
		String selection = null;
		String[] selectionargs = new String[]{username};
		
		selectionargs = new String[]{username};
		selection = Bookmark.Account + "=? AND " + Bookmark.Synced + "<>1 AND " + Bookmark.Deleted + "=0";
		
		Uri bookmarks = Bookmark.CONTENT_URI;

		Cursor c = context.getContentResolver().query(bookmarks, projection, selection, selectionargs, null);				

		if(c.moveToFirst()){
			int idColumn = c.getColumnIndex(Bookmark._ID);
			int urlColumn = c.getColumnIndex(Bookmark.Url);
			int descriptionColumn = c.getColumnIndex(Bookmark.Description);
			int tagsColumn = c.getColumnIndex(Bookmark.Tags);
			int metaColumn = c.getColumnIndex(Bookmark.Meta);
			int readColumn = c.getColumnIndex(Bookmark.ToRead);
			int shareColumn = c.getColumnIndex(Bookmark.Shared);
			int notesColumn = c.getColumnIndex(Bookmark.Notes);
			int hashColumn = c.getColumnIndex(Bookmark.Hash);

			do {

				Bookmark b = new Bookmark(c.getInt(idColumn), "", c.getString(urlColumn), 
						c.getString(descriptionColumn), c.getString(notesColumn), c.getString(tagsColumn), c.getString(hashColumn), 
						c.getString(metaColumn), 0, c.getInt(readColumn) == 0 ? false : true,
						c.getInt(shareColumn) == 0 ? false : true, 0, false);

				bookmarkList.add(b);

			} while(c.moveToNext());

		}
		c.close();
		return bookmarkList;
	}
	
	public static ArrayList<Bookmark> GetDeletedBookmarks(String username, Context context){
		ArrayList<Bookmark> bookmarkList = new ArrayList<Bookmark>();
		final String[] projection = new String[] {Bookmark._ID, Bookmark.Url, Bookmark.Description, Bookmark.Notes, Bookmark.Hash,
				Bookmark.Meta, Bookmark.Tags, Bookmark.ToRead, Bookmark.Shared, Bookmark.Synced, Bookmark.Deleted};
		String selection = null;
		String[] selectionargs = new String[]{username};
		
		selectionargs = new String[]{username};
		selection = Bookmark.Account + "=? AND " + Bookmark.Synced + "=0 AND " + Bookmark.Deleted + "=1";
		
		Uri bookmarks = Bookmark.CONTENT_URI;

		Cursor c = context.getContentResolver().query(bookmarks, projection, selection, selectionargs, null);				

		if(c.moveToFirst()){
			int idColumn = c.getColumnIndex(Bookmark._ID);
			int urlColumn = c.getColumnIndex(Bookmark.Url);
			int descriptionColumn = c.getColumnIndex(Bookmark.Description);
			int tagsColumn = c.getColumnIndex(Bookmark.Tags);
			int metaColumn = c.getColumnIndex(Bookmark.Meta);
			int readColumn = c.getColumnIndex(Bookmark.ToRead);
			int shareColumn = c.getColumnIndex(Bookmark.Shared);
			int notesColumn = c.getColumnIndex(Bookmark.Notes);
			int hashColumn = c.getColumnIndex(Bookmark.Hash);

			do {

				Bookmark b = new Bookmark(c.getInt(idColumn), "", c.getString(urlColumn), 
						c.getString(descriptionColumn), c.getString(notesColumn), c.getString(tagsColumn), c.getString(hashColumn), 
						c.getString(metaColumn), 0, c.getInt(readColumn) == 0 ? false : true,
						c.getInt(shareColumn) == 0 ? false : true, 0, true);

				bookmarkList.add(b);

			} while(c.moveToNext());

		}
		c.close();
		return bookmarkList;
	}
	
	public static Bookmark GetById(int id, Context context) throws ContentNotFoundException {		
		final String[] projection = new String[] {Bookmark.Account, Bookmark.Url, Bookmark.Description, Bookmark.Notes, Bookmark.Time, Bookmark.Tags, Bookmark.Hash, Bookmark.Meta, Bookmark.ToRead, Bookmark.Shared, Bookmark.Synced, Bookmark.Deleted};
		String selection = Bookmark.Deleted + "=0";
		
		Uri uri = ContentUris.appendId(Bookmark.CONTENT_URI.buildUpon(), id).build();
			
		Cursor c = context.getContentResolver().query(uri, projection, selection, null, null);				
		
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
			final int syncedColumn = c.getColumnIndex(Bookmark.Synced);
			final int deletedColumn = c.getColumnIndex(Bookmark.Deleted);
			
			final boolean read = c.getInt(readColumn) == 0 ? false : true;
			final boolean share = c.getInt(shareColumn) == 0 ? false : true;
			final int synced = c.getInt(syncedColumn);
			final boolean deleted = c.getInt(deletedColumn) == 0 ? false : true;

			Bookmark b = new Bookmark(id, c.getString(accountColumn), c.getString(urlColumn), 
				c.getString(descriptionColumn), c.getString(notesColumn), c.getString(tagsColumn),
				c.getString(hashColumn), c.getString(metaColumn), c.getLong(timeColumn), read, share, synced, deleted);
			
			c.close();
			
			return b;
		} else {
			c.close();
			throw new ContentNotFoundException();
		}
	}
	
	// TODO normalize url (remove trailing slash)
	public static Bookmark GetByUrl(String url, String username, Context context) throws ContentNotFoundException {		
		final String[] projection = new String[] {Bookmark._ID, Bookmark.Account, Bookmark.Url, Bookmark.Description, Bookmark.Notes, Bookmark.Time, Bookmark.Tags, Bookmark.Hash, Bookmark.Meta, Bookmark.ToRead, Bookmark.Shared, Bookmark.Synced, Bookmark.Deleted};
		String selection = Bookmark.Url + "=? AND " + Bookmark.Account + "=? AND " + Bookmark.Deleted + "=0";
		final String[] selectionargs = new String[]{ url, username };
		
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
			final int syncedColumn = c.getColumnIndex(Bookmark.Synced);
			final int deletedColumn = c.getColumnIndex(Bookmark.Deleted);
			
			final boolean read = c.getInt(readColumn) == 0 ? false : true;
			final boolean share = c.getInt(shareColumn) == 0 ? false : true;
			final int synced = c.getInt(syncedColumn);
			final boolean deleted = c.getInt(deletedColumn) == 0 ? false : true;

			Bookmark b = new Bookmark(c.getInt(idColumn), c.getString(accountColumn), c.getString(urlColumn), 
				c.getString(descriptionColumn), c.getString(notesColumn), c.getString(tagsColumn),
				c.getString(hashColumn), c.getString(metaColumn), c.getLong(timeColumn), read, share, synced, deleted);
			
			c.close();
			
			return b;
		} else {
			c.close();
			throw new ContentNotFoundException();
		}
	}
	
	public static Bookmark GetByHash(String hash, String username, Context context) throws ContentNotFoundException {		
		final String[] projection = new String[] {Bookmark._ID, Bookmark.Account, Bookmark.Url, Bookmark.Description, Bookmark.Notes, Bookmark.Time, Bookmark.Tags, Bookmark.Hash, Bookmark.Meta, Bookmark.ToRead, Bookmark.Shared, Bookmark.Synced, Bookmark.Deleted};
		String selection = Bookmark.Hash + "=? AND " + Bookmark.Account + "=? AND " + Bookmark.Deleted + "=0";
		final String[] selectionargs = new String[]{ hash, username };
		
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
			final int syncedColumn = c.getColumnIndex(Bookmark.Synced);
			final int deletedColumn = c.getColumnIndex(Bookmark.Deleted);
			
			final boolean read = c.getInt(readColumn) == 0 ? false : true;
			final boolean share = c.getInt(shareColumn) == 0 ? false : true;
			final int synced = c.getInt(syncedColumn);
			final boolean deleted = c.getInt(deletedColumn) == 0 ? false : true;

			Bookmark b = new Bookmark(c.getInt(idColumn), c.getString(accountColumn), c.getString(urlColumn), 
				c.getString(descriptionColumn), c.getString(notesColumn), c.getString(tagsColumn),
				c.getString(hashColumn), c.getString(metaColumn), c.getLong(timeColumn), read, share, synced, deleted);
			
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
		values.put(Bookmark.Synced, 0);
		values.put(Bookmark.Deleted, 0);
		
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
			values.put(Bookmark.Synced, 1);
			values.put(Bookmark.Deleted, false);
			
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
		
		final String selection = Bookmark.Hash + "=? AND " + Bookmark.Account + "=?";
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
		values.put(Bookmark.Synced, 0);
		values.put(Bookmark.Deleted, false);
		
		Uri uri = Bookmark.CONTENT_URI.buildUpon().appendPath(Integer.toString(bookmark.getId())).build();	
		context.getContentResolver().update(uri, values, selection, selectionargs);
	}
	
	public static void SetSynced(Bookmark bookmark, int synced, String account, Context context){
		final String url = bookmark.getUrl();
		
		String hash = "";
		if(bookmark.getHash() == null || bookmark.getHash() == ""){
			hash = Md5Hash.md5(url);
		} else hash = bookmark.getHash();
		
		final String selection = Bookmark.Hash + "=? AND " + Bookmark.Account + "=?";
		final String[] selectionargs = new String[]{hash, account};
		
		final ContentValues values = new ContentValues();
		values.put(Bookmark.Synced, synced);
		
		Uri uri = Bookmark.CONTENT_URI.buildUpon().appendPath(Integer.toString(bookmark.getId())).build();	
		context.getContentResolver().update(uri, values, selection, selectionargs);
	}
	
	public static void LazyDelete(Bookmark bookmark, String account, Context context){
		final String url = bookmark.getUrl();
		
		String hash = "";
		if(bookmark.getHash() == null || bookmark.getHash() == ""){
			hash = Md5Hash.md5(url);
		} else hash = bookmark.getHash();
		
		final String selection = Bookmark.Hash + "=? AND " + Bookmark.Account + "=?";
		final String[] selectionargs = new String[]{hash, account};
		
		final ContentValues values = new ContentValues();
		values.put(Bookmark.Deleted, true);
		values.put(Bookmark.Synced, false);
		
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
		
		String selection = TextUtils.join(logicalOp, selectionList);
		
		if(accounts.size() > 0)
			selection += " AND " + Bookmark.Synced + "=1";
		else selection += Bookmark.Synced + "=1";
		
		context.getContentResolver().delete(Bookmark.CONTENT_URI, selection, null);
	}
	
	public static CursorLoader SearchBookmarks(String query, String tagname, boolean unread, String username, Context context) {
		final String[] projection = new String[] {Bookmark._ID, Bookmark.Url, Bookmark.Description, Bookmark.Hash,
				Bookmark.Meta, Bookmark.Tags, Bookmark.Shared, Bookmark.ToRead, Bookmark.Synced, Bookmark.Deleted};
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
				"(" + Bookmark.Tags + " LIKE ? OR " +
				Bookmark.Tags + " LIKE ? OR " +
				Bookmark.Tags + " LIKE ? OR " +
				Bookmark.Tags + " = ?)";
			
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
		
		selection += " AND " + Bookmark.Deleted + "=0";
		
		return new CursorLoader(context, Bookmark.CONTENT_URI, projection, selection, selectionlist.toArray(new String[]{}), sortorder);
	}
	
	public static int GetUnreadCount(String username, Context context){
		if(username == null || username.equals(""))
			return 0;
		
		final String[] projection = new String[] {Bookmark._ID};
		final String selection = Bookmark.Account + "=? AND " + Bookmark.ToRead + "=1";
		final String[] selectionargs = new String[]{username};
		
		final Cursor c = context.getContentResolver().query(Bookmark.CONTENT_URI, projection, selection, selectionargs, null);				
		
		final int count = c.getCount();
		
		c.close();
		return count;
	}

	public static String nullOrEmpty(String columnName) {
		return "(" + columnName + " IS NULL OR " + columnName + " = '' )";
	}

	public static int GetUntaggedCount(String username, Context context){
		if(username == null || username.equals(""))
			return 0;

		final String[] projection = new String[] {Bookmark._ID};
		final String selection = Bookmark.Account + "=? AND " + nullOrEmpty(Bookmark.Tags);
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
		b.setHash(c.getString(c.getColumnIndex(Bookmark.Hash)));
		b.setMeta(c.getString(c.getColumnIndex(Bookmark.Meta)));
		b.setTagString(c.getString(c.getColumnIndex(Bookmark.Tags)));
		b.setToRead(c.getInt(c.getColumnIndex(Bookmark.ToRead)) == 1 ? true : false);
		
		if(c.getColumnIndex(Bookmark.Account) != -1)
			b.setAccount(c.getString(c.getColumnIndex(Bookmark.Account)));
		
		if(c.getColumnIndex(Bookmark.Notes) != -1)
			b.setNotes(c.getString(c.getColumnIndex(Bookmark.Notes)));
		
		if(c.getColumnIndex(Bookmark.Time) != -1)
			b.setTime(c.getLong(c.getColumnIndex(Bookmark.Time)));
		
		if(c.getColumnIndex(Bookmark.Shared) != -1)
			b.setShared(c.getInt(c.getColumnIndex(Bookmark.Shared)) == 1 ? true : false);
		
		if(c.getColumnIndex(Bookmark.Synced) != -1)
			b.setSynced(c.getInt(c.getColumnIndex(Bookmark.Synced)));
		
		if(c.getColumnIndex(Bookmark.Deleted) != -1)
			b.setDeleted(c.getInt(c.getColumnIndex(Bookmark.Deleted)) == 1 ? true : false);
		
		return b;
	}
}