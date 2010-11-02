package com.android.droidlicious.platform;

import com.android.droidlicious.providers.TagContent.Tag;

import android.content.ContentValues;
import android.content.Context;
import android.provider.BaseColumns;

public class TagManager {
	
	public static void AddTag(Tag tag, String account, Context context){
		ContentValues values = new ContentValues();
		
		values.put(Tag.Name, tag.getTagName());
		values.put(Tag.Count, tag.getCount());
		values.put(Tag.Account, account);
	
		context.getContentResolver().insert(Tag.CONTENT_URI, values);
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
