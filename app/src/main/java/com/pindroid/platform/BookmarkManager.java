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
import java.util.Date;
import java.util.List;

import com.pindroid.model.Tag;
import com.pindroid.providers.ContentNotFoundException;
import com.pindroid.model.Bookmark;
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
	
	public static CursorLoader GetBookmarks(String username, String tagname, boolean unread, String sortorder, Context context){
		final String[] projection = new String[] {Bookmark._ID, Bookmark.Url, Bookmark.Description, Bookmark.Notes, Bookmark.Hash,
				Bookmark.Meta, Bookmark.Tags, Bookmark.ToRead, Bookmark.Shared, Bookmark.Synced, Bookmark.Deleted,
				Bookmark.Account, Bookmark.Time};
		String selection = null;
		String[] selectionargs = new String[]{username, "% " + tagname + " %", 
				"% " + tagname, tagname + " %", tagname};
		
		if(tagname != null && !"".equals(tagname)) {
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
		selection += " AND " + Bookmark.Deleted + "=0";
		
		return new CursorLoader(context, Bookmark.CONTENT_URI, projection, selection, selectionargs, sortorder);
	}
	
	public static ArrayList<Bookmark> GetLocalBookmarks(String username, Context context){
		ArrayList<Bookmark> bookmarkList = new ArrayList<>();
		final String[] projection = new String[] {Bookmark._ID, Bookmark.Url, Bookmark.Description, Bookmark.Notes, Bookmark.Hash,
				Bookmark.Meta, Bookmark.Tags, Bookmark.ToRead, Bookmark.Shared, Bookmark.Synced, Bookmark.Deleted};
		String selection = null;
		String[] selectionargs = new String[]{username};
		
		selectionargs = new String[]{username};
		selection = Bookmark.Account + "=? AND " + Bookmark.Synced + "<>1 AND " + Bookmark.Deleted + "=0";
		
		Uri bookmarks = Bookmark.CONTENT_URI;

		Cursor c = context.getContentResolver().query(bookmarks, projection, selection, selectionargs, null);				

		if(c.moveToFirst()){
			do {
				bookmarkList.add(new Bookmark(c));
			} while(c.moveToNext());

		}
		c.close();
		return bookmarkList;
	}
	
	public static ArrayList<Bookmark> GetDeletedBookmarks(String username, Context context){
		ArrayList<Bookmark> bookmarkList = new ArrayList<>();
		final String[] projection = new String[] {Bookmark._ID, Bookmark.Url, Bookmark.Description, Bookmark.Notes, Bookmark.Hash,
				Bookmark.Meta, Bookmark.Tags, Bookmark.ToRead, Bookmark.Shared, Bookmark.Synced, Bookmark.Deleted};
		String selection = null;
		String[] selectionargs = new String[]{username};
		selection = Bookmark.Account + "=? AND " + Bookmark.Synced + "=0 AND " + Bookmark.Deleted + "=1";
		
		Uri bookmarks = Bookmark.CONTENT_URI;

		Cursor c = context.getContentResolver().query(bookmarks, projection, selection, selectionargs, null);				

		if(c.moveToFirst()){
			do {
				bookmarkList.add(new Bookmark(c));
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
			Bookmark b = new Bookmark(c);
			c.close();
			return b;
		} else {
			c.close();
			throw new ContentNotFoundException();
		}
	}

	public static Bookmark GetByUrl(String url, String username, Context context) throws ContentNotFoundException {		
		final String[] projection = new String[] {Bookmark._ID, Bookmark.Account, Bookmark.Url, Bookmark.Description, Bookmark.Notes, Bookmark.Time, Bookmark.Tags, Bookmark.Hash, Bookmark.Meta, Bookmark.ToRead, Bookmark.Shared, Bookmark.Synced, Bookmark.Deleted};
		String selection = Bookmark.Url + "=? AND " + Bookmark.Account + "=? AND " + Bookmark.Deleted + "=0";
		final String[] selectionargs = new String[]{ url, username };
		
		Cursor c = context.getContentResolver().query(Bookmark.CONTENT_URI, projection, selection, selectionargs, null);				
		
		if(c.moveToFirst()){
			Bookmark b = new Bookmark(c);
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
			Bookmark b = new Bookmark(c);
			c.close();
			return b;
		} else {
			c.close();
			throw new ContentNotFoundException();
		}
	}
	
	public static void AddBookmark(Bookmark bookmark, Context context) {
		if(bookmark.getHash() == null || "".equals(bookmark.getHash())){
			bookmark.setHash(Md5Hash.md5(bookmark.getUrl()));
		}
        bookmark.setAccount(bookmark.getAccount());
        bookmark.setSynced(0);
        bookmark.setDeleted(false);

		context.getContentResolver().insert(Bookmark.CONTENT_URI, bookmark.toContentValues());
	}
	
	public static void BulkInsert(ArrayList<Bookmark> list, String account, Context context) {
		int bookmarksize = list.size();
		ContentValues[] bcv = new ContentValues[bookmarksize];
		
		for(int i = 0; i < bookmarksize; i++){
			Bookmark b = list.get(i);
            b.setAccount(account);
            b.setSynced(1);
            b.setDeleted(false);
			bcv[i] = b.toContentValues();
		}
		
		context.getContentResolver().bulkInsert(Bookmark.CONTENT_URI, bcv);
	}
	
	public static void UpdateBookmark(Bookmark bookmark, Context context){

		String hash = "";
		if(bookmark.getHash() == null || "".equals(bookmark.getHash())){
			hash = Md5Hash.md5(bookmark.getUrl());
		} else hash = bookmark.getHash();
		
		final String selection = Bookmark.Hash + "=? AND " + Bookmark.Account + "=?";
		final String[] selectionargs = new String[]{hash, bookmark.getAccount()};

        bookmark.setSynced(0);
        bookmark.setDeleted(false);

		Uri uri = Bookmark.CONTENT_URI.buildUpon().appendPath(Integer.toString(bookmark.getId())).build();
		context.getContentResolver().update(uri, bookmark.toContentValues(), selection, selectionargs);
	}

	public static void SetSynced(Bookmark bookmark, int synced, String account, Context context){
		final String url = bookmark.getUrl();

		String hash = "";
		if(bookmark.getHash() == null || "".equals(bookmark.getHash())){
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
		if(bookmark.getHash() == null || "".equals(bookmark.getHash())){
			hash = Md5Hash.md5(url);
		} else hash = bookmark.getHash();
		
		final String selection = Bookmark.Hash + "=? AND " + Bookmark.Account + "=?";
		final String[] selectionargs = new String[]{hash, account};
		
		final ContentValues values = new ContentValues();
		values.put(Bookmark.Deleted, true);
		values.put(Bookmark.Synced, false);
		
		context.getContentResolver().update(Bookmark.CONTENT_URI, values, selection, selectionargs);
	}

    public static void LazyUndelete(Bookmark bookmark, String account, Context context){
        final String url = bookmark.getUrl();

        String hash = "";
        if(bookmark.getHash() == null || "".equals(bookmark.getHash())){
            hash = Md5Hash.md5(url);
        } else hash = bookmark.getHash();

        final String selection = Bookmark.Hash + "=? AND " + Bookmark.Account + "=?";
        final String[] selectionargs = new String[]{hash, account};

        final ContentValues values = new ContentValues();
        values.put(Bookmark.Deleted, false);
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
		
		final ArrayList<String> selectionList = new ArrayList<>();
		
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
		
		final ArrayList<String> queryList = new ArrayList<>();
		final ArrayList<String> selectionlist = new ArrayList<>();
		
		if(query != null && !"".equals(query) && (tagname == null || "".equals(tagname))) {
			
			
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
		} else if(query != null && !"".equals(query)){
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
		
		return new CursorLoader(context, Bookmark.CONTENT_URI, projection, selection, selectionlist.toArray(new String[selectionlist.size()]), sortorder);
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

    public static void AddOrUpdateBookmark(Bookmark newBookmark, Bookmark oldBookmark, Context context) {
        if(oldBookmark != null && oldBookmark.getId() != 0){
            BookmarkManager.UpdateBookmark(newBookmark, context);

            for(Tag t : oldBookmark.getTags()){
                if(!newBookmark.getTags().contains(t)) {
                    TagManager.UpleteTag(t, context);
                }
            }
        } else {
            BookmarkManager.AddBookmark(newBookmark, context);
        }

        for(Tag t : newBookmark.getTags()){
            TagManager.UpsertTag(t, context);
        }
    }
}