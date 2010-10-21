package com.android.droidlicious.platform;

import com.android.droidlicious.providers.TagContent.Tag;

import android.content.ContentValues;
import android.content.Context;
import android.provider.BaseColumns;

public class TagManager {
	
	public static void AddTag(Tag tag, Context context){
		ContentValues values = new ContentValues();
		
		values.put(Tag.Name, tag.getTagName());
		values.put(Tag.Count, tag.getCount());
	
		context.getContentResolver().insert(Tag.CONTENT_URI, values);
	}
	
	public static void UpdateTag(Tag tag, Context context){
		
		String selection = Tag.Name + "='" + tag.getTagName() + "'";
		
		ContentValues values = new ContentValues();
		
		values.put(Tag.Count, tag.getCount());
		
		context.getContentResolver().update(Tag.CONTENT_URI, values, selection, null);
		
	}

	public static void DeleteTag(Tag tag, Context context){
		
		String selection = BaseColumns._ID + "=" + tag.getId();
		
		context.getContentResolver().delete(Tag.CONTENT_URI, selection, null);
	}
	
	public static void TruncateTags(Context context){
		context.getContentResolver().delete(Tag.CONTENT_URI, null, null);
	}
}
