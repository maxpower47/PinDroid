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

import com.pindroid.providers.TagContent.Tag;

import android.content.ContentValues;
import android.content.Context;
import android.support.v4.content.CursorLoader;
import android.database.Cursor;
import android.text.TextUtils;

public class TagManager {
	
	public static CursorLoader GetTags(String account, String sortorder, Context context) {		
		final String[] projection = new String[] {Tag._ID, Tag.Name, Tag.Count};
		final String selection = Tag.Account + "=?";
		final String[] selectionargs = new String[]{account};
		
		return new CursorLoader(context, Tag.CONTENT_URI, projection, selection, selectionargs, sortorder);
	}
	
	public static Cursor GetTagsAsCursor(String query, String account, String sortorder, Context context) {	
		final String[] projection = new String[] { Tag._ID, Tag.Name, Tag.Count };
		String selection = null;
		String[] selectionargs = new String[]{account, query + "%"};
		
		if(query != null) {
			selection = Tag.Account + "=?" + 
				" AND " + Tag.Name + " LIKE ?";
		} else {
			selection = Tag.Account + "=?";
		}
		
		return context.getContentResolver().query(Tag.CONTENT_URI, projection, selection, selectionargs, sortorder);
	}
	
	public static void AddTag(Tag tag, String account, Context context){
		final ContentValues values = new ContentValues();
		
		values.put(Tag.Name, tag.getTagName());
		values.put(Tag.Count, tag.getCount());
		values.put(Tag.Account, account);
	
		context.getContentResolver().insert(Tag.CONTENT_URI, values);
	}
	
	public static void BulkInsert(ArrayList<Tag> list, String account, Context context) {
		int tagsize = list.size();
		ContentValues[] tcv = new ContentValues[tagsize];
		
		for(int i = 0; i < tagsize; i++){	
			Tag t = list.get(i);
			
			ContentValues values = new ContentValues();
			
			values.put(Tag.Name, t.getTagName());
			values.put(Tag.Count, t.getCount());
			values.put(Tag.Account, account);
			
			tcv[i] = values;
		}
		
		context.getContentResolver().bulkInsert(Tag.CONTENT_URI, tcv);
	}
	
	public static void UpsertTag(Tag tag, String account, Context context){
		final String[] projection = new String[] {Tag.Name, Tag.Count};
		final String selection = Tag.Name + "=? AND " +	Tag.Account + "=?";
		final String[] selectionargs = new String[]{tag.getTagName(), account};
		
		final Cursor c = context.getContentResolver().query(Tag.CONTENT_URI, projection, selection, selectionargs, null);
		
		if(c.moveToFirst()){
			final int countColumn = c.getColumnIndex(Tag.Count);
			final int count = c.getInt(countColumn);
			
			tag.setCount(count + 1);
			UpdateTag(tag, account, context);
		} else {
			tag.setCount(1);
			AddTag(tag, account, context);
		}
		c.close();
	}
	
	public static void UpdateTag(Tag tag, String account, Context context){
		
		final String selection = Tag.Name + "=? AND " +	Tag.Account + "=?";
		final String[] selectionargs = new String[]{tag.getTagName(), account};
		
		final ContentValues values = new ContentValues();
		values.put(Tag.Count, tag.getCount());
		
		context.getContentResolver().update(Tag.CONTENT_URI, values, selection, selectionargs);
	}
	
	public static void UpleteTag(Tag tag, String account, Context context){
		final String[] projection = new String[] {Tag.Name, Tag.Count};
		final String selection = Tag.Name + "=? AND " +	Tag.Account + "=?";
		final String[] selectionargs = new String[]{tag.getTagName(), account};

		final Cursor c = context.getContentResolver().query(Tag.CONTENT_URI, projection, selection, selectionargs, null);
		
		if(c.moveToFirst()){
			final int countColumn = c.getColumnIndex(Tag.Count);
			final int count = c.getInt(countColumn);
			
			if(count > 1){
				tag.setCount(count - 1);
				UpdateTag(tag, account, context);
			} else {
				DeleteTag(tag, account, context);
			}
		}
		c.close();
	}

	public static void DeleteTag(Tag tag, String account, Context context){
		final String selection = Tag.Name + "=? AND " +	Tag.Account + "=?";
		final String[] selectionargs = new String[]{tag.getTagName(), account};
		
		context.getContentResolver().delete(Tag.CONTENT_URI, selection, selectionargs);
	}
	
	public static void TruncateTags(String account, Context context){
		
		final String selection = Tag.Account + "=?";
		final String[] selectionargs = new String[]{account};
		
		context.getContentResolver().delete(Tag.CONTENT_URI, selection, selectionargs);
	}
	
	public static void TruncateOldTags(ArrayList<String> accounts, Context context){
		
		final ArrayList<String> selectionList = new ArrayList<String>();
		
		for(String s : accounts) {
			selectionList.add(Tag.Account + " <> '" + s + "'");
		}
		
		final String selection = TextUtils.join(" AND ", selectionList);
		
		context.getContentResolver().delete(Tag.CONTENT_URI, selection, null);
	}
	
	public static CursorLoader SearchTags(String query, String username, Context context) {
		final String[] projection = new String[] { Tag._ID, Tag.Name, Tag.Count };
		String selection = null;
		
		final String sortorder = Tag.Name + " ASC";
		final ArrayList<String> selectionlist = new ArrayList<String>();
		
		ArrayList<String> queryList = new ArrayList<String>();
		
		for(String s : query.split(" ")) {
			queryList.add(Tag.Name + " LIKE ?");
			selectionlist.add("%" + s + "%");
		}
		
		selectionlist.add(username);
		
		if(query != null && query != "") {
			selection = "(" + TextUtils.join(" OR ", queryList) + ")" + 
				" AND " + Tag.Account + "=?";
		} else {
			selection = Tag.Account + "=?";
		}
		
		return new CursorLoader(context, Tag.CONTENT_URI, projection, selection, selectionlist.toArray(new String[]{}), sortorder);
	}
}