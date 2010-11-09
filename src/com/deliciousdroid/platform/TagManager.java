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

import com.deliciousdroid.providers.TagContent.Tag;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

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
		String selection = Tag.Name + "='" + tag.getTagName() + "' AND " +
			Tag.Account + " = '" + account + "'";
		Uri tags = Tag.CONTENT_URI;
		
		ContentResolver cr = context.getContentResolver();
		Cursor c = cr.query(tags, projection, selection, null, null);
		
		if(c.getCount() > 0){
			tag.setCount(tag.getCount() + 1);
			UpdateTag(tag, account, context);
		} else {
			AddTag(tag, account, context);
		}
		c.close();
	}
	
	public static void UpdateTag(Tag tag, String account, Context context){
		
		String selection = Tag.Name + "='" + tag.getTagName() + "' AND " +
							Tag.Account + " = '" + account + "'";
		
		ContentValues values = new ContentValues();
		
		values.put(Tag.Count, tag.getCount());
		
		context.getContentResolver().update(Tag.CONTENT_URI, values, selection, null);
		
	}

	public static void DeleteTag(Tag tag, Context context){
		
		String selection = BaseColumns._ID + "=" + tag.getId();
		
		context.getContentResolver().delete(Tag.CONTENT_URI, selection, null);
	}
	
	public static void TruncateTags(String account, Context context){
		
		String selection = Tag.Account + " = '" + account + "'";
		
		context.getContentResolver().delete(Tag.CONTENT_URI, selection, null);
	}
}
