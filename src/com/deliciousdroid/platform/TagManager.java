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

import java.util.ArrayList;

import com.deliciousdroid.providers.TagContent.Tag;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

public class TagManager {
	
	public static void AddTag(Tag tag, String account, Context context){
		ContentValues values = new ContentValues();
		
		values.put(Tag.Name, tag.getTagName());
		values.put(Tag.Count, tag.getCount());
		values.put(Tag.Account, account);
	
		context.getContentResolver().insert(Tag.CONTENT_URI, values);
	}
	
	public static void UpsertTag(Tag tag, String account, Context context){
		String[] projection = new String[] {Tag.Name, Tag.Count};
		String selection = Tag.Name + "=? AND " +
			Tag.Account + "=?";
		String[] selectionargs = new String[]{tag.getTagName(), account};
		Uri tags = Tag.CONTENT_URI;
		
		ContentResolver cr = context.getContentResolver();
		Cursor c = cr.query(tags, projection, selection, selectionargs, null);
		
		if(c.moveToFirst()){
			int countColumn = c.getColumnIndex(Tag.Count);
			int count = c.getInt(countColumn);
			
			tag.setCount(count + 1);
			UpdateTag(tag, account, context);
		} else {
			AddTag(tag, account, context);
		}
		c.close();
	}
	
	public static void UpdateTag(Tag tag, String account, Context context){
		
		String selection = Tag.Name + "=? AND " +
							Tag.Account + "=?";
		String[] selectionargs = new String[]{tag.getTagName(), account};
		
		ContentValues values = new ContentValues();
		
		values.put(Tag.Count, tag.getCount());
		
		context.getContentResolver().update(Tag.CONTENT_URI, values, selection, selectionargs);
	}
	
	public static void UpleteTag(Tag tag, String account, Context context){
		String[] projection = new String[] {Tag.Name, Tag.Count};
		String selection = Tag.Name + "=? AND " +
			Tag.Account + "=?";
		String[] selectionargs = new String[]{tag.getTagName(), account};
		Uri tags = Tag.CONTENT_URI;
		
		ContentResolver cr = context.getContentResolver();
		Cursor c = cr.query(tags, projection, selection, selectionargs, null);
		
		if(c.moveToFirst()){
			int countColumn = c.getColumnIndex(Tag.Count);
			int count = c.getInt(countColumn);
			
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
		
		String selection = Tag.Name + "=? AND " +
			Tag.Account + "=?";
		String[] selectionargs = new String[]{tag.getTagName(), account};
		
		context.getContentResolver().delete(Tag.CONTENT_URI, selection, selectionargs);
	}
	
	public static void TruncateTags(String account, Context context){
		
		String selection = Tag.Account + "=?";
		String[] selectionargs = new String[]{account};
		
		context.getContentResolver().delete(Tag.CONTENT_URI, selection, selectionargs);
	}
	
	public static void TruncateOldTags(ArrayList<String> accounts, Context context){
		
		ArrayList<String> selectionList = new ArrayList<String>();
		
		for(String s : accounts) {
			selectionList.add(Tag.Account + " <> '" + s + "'");
		}
		
		String selection = TextUtils.join(" AND ", selectionList);
		
		context.getContentResolver().delete(Tag.CONTENT_URI, selection, null);
	}
	
	public static ArrayList<Tag> SearchTags(String query, String username, Context context) {
		ArrayList<Tag> tagList = new ArrayList<Tag>();
		String[] projection = new String[] { Tag._ID, Tag.Name, Tag.Count };
		String selection = null;
		String[] selectionargs = new String[]{ username };
		String sortorder = null;
		
		String[] queryTags = query.split(" ");
		
		ArrayList<String> queryList = new ArrayList<String>();
		
		for(String s : queryTags) {
			queryList.add(Tag.Name + " LIKE '%" + s + "%'");
		}
		
		if(query != null && query != "") {
			selection = "(" + TextUtils.join(" OR ", queryList) + ")" + 
				" AND " + Tag.Account + "=?";
		} else {
			selection = Tag.Account + "=?";
		}
		
		sortorder = Tag.Name + " ASC";
		
		Uri tags = Tag.CONTENT_URI;
		
		Cursor c = context.getContentResolver().query(tags, projection, selection, selectionargs, sortorder);				
		
		if(c.moveToFirst()){
			int nameColumn = c.getColumnIndex(Tag.Name);
			int countColumn = c.getColumnIndex(Tag.Count);
			
			do {
				Tag t = new Tag(c.getString(nameColumn), c.getInt(countColumn));
				
				tagList.add(t);
			} while(c.moveToNext());	
		}
		c.close();
		return tagList;
	}
}