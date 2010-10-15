/*
 * Copyright (C) 2010 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.android.droidlicious.syncadapter;

import android.accounts.Account;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.android.droidlicious.Constants;
import com.android.droidlicious.R;
import com.android.droidlicious.activity.Main;
import com.android.droidlicious.authenticator.AuthToken;
import com.android.droidlicious.client.NetworkUtilities;
import com.android.droidlicious.client.Update;
import com.android.droidlicious.providers.BookmarkContent.Bookmark;
import com.android.droidlicious.providers.TagContent.Tag;

import org.apache.http.ParseException;

import java.util.ArrayList;

/**
 * SyncAdapter implementation for syncing sample SyncAdapter contacts to the
 * platform ContactOperations provider.
 */
public class BookmarkSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "BookmarkSyncAdapter";

    private final Context mContext;
    
    private String authtoken = null;

    public BookmarkSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
        ContentProviderClient provider, SyncResult syncResult) {

         try {
        	 AuthToken at = new AuthToken(mContext, account);
        	 authtoken = at.getAuthToken();

            
            InsertBookmarks(account);
        }catch (final ParseException e) {
            syncResult.stats.numParseExceptions++;
            Log.e(TAG, "ParseException", e);
        }
    }
    
    private void InsertBookmarks(Account account){
    	
    	SharedPreferences settings = mContext.getSharedPreferences(Constants.AUTH_PREFS_NAME, 0);
    	Boolean initialSync = settings.getBoolean(Constants.PREFS_INITIAL_SYNC, false);
    	long lastUpdate = settings.getLong(Constants.PREFS_LAST_SYNC, 0);
    	Update update = null;
    	Boolean success = true;
    	
    	try {
			update = NetworkUtilities.lastUpdate(account.name, account, authtoken, mContext);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		if(update.getInboxNew() > 0) {
			NotificationManager nm = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
			Notification n = new Notification(R.drawable.icon, "New Delicious Bookmarks", System.currentTimeMillis());
			Intent ni = new Intent(mContext, Main.class);
			PendingIntent ci = PendingIntent.getActivity(mContext, 0, ni, 0);
			n.setLatestEventInfo(mContext, "New Bookmarks", "You Have " + Integer.toString(update.getInboxNew()) + " New Bookmark(s)", ci);
			
			nm.notify(1, n);
		}
    	
    	if(update.getLastUpdate() > lastUpdate) {
	
			ArrayList<Bookmark> bookmarkList = new ArrayList<Bookmark>();
			ArrayList<Bookmark> changeList = new ArrayList<Bookmark>();
			ArrayList<Bookmark> addList = new ArrayList<Bookmark>();
			ArrayList<Bookmark> updateList = new ArrayList<Bookmark>();
			ArrayList<Tag> tagList = new ArrayList<Tag>();

			try {
				if(!initialSync){
					Log.d("BookmarkSync", "In Bookmark Load");
					tagList = NetworkUtilities.fetchTags(account.name, account, authtoken, mContext);
					bookmarkList = NetworkUtilities.fetchMyBookmarks(account.name, null, account, authtoken, mContext, true);
				} else {
					Log.d("BookmarkSync", "In Bookmark Update");
					changeList = NetworkUtilities.fetchChangedBookmarks(account.name, account, authtoken, mContext);
					
					for(Bookmark b : changeList){
					
						String[] projection = new String[] {Bookmark.Hash, Bookmark.Meta};
						String selection = Bookmark.Hash + "=?";
						String[] selectionArgs = new String[] {b.getHash()};
						
						Uri bookmarks = Bookmark.CONTENT_URI;
						
						Cursor c = mContext.getContentResolver().query(bookmarks, projection, selection, selectionArgs, null);
						
						if(c.getCount() == 0){
							addList.add(b);
						}
						
						if(c.moveToFirst()){
							int metaColumn = c.getColumnIndex(Bookmark.Meta);
							
							do {							
								if(c.getString(metaColumn) == b.getMeta()) {
									updateList.add(b);
								}	
							} while(c.moveToNext());
						}
						
						c.close();
					}
					
					ArrayList<String> a = new ArrayList<String>();
					for(Bookmark b : addList){
						a.add(b.getHash());
					}
					Log.d("size", Integer.toString(a.size()));
					if(a.size() > 0) {
						bookmarkList = NetworkUtilities.fetchBookmark(account.name, a, account, authtoken, mContext);
					}
					
				}
			} catch (Exception e) {
				success = false;
				e.printStackTrace();
			}
			
			for(Tag b : tagList){
				ContentValues values = new ContentValues();
				
				values.put(Tag.Name, b.getTagName());
				values.put(Tag.Count, b.getCount());

				
				Uri uri = mContext.getContentResolver().insert(Tag.CONTENT_URI, values);
				Log.d("tag", uri.toString());
			}
			
			if(success){
	    		SharedPreferences.Editor editor = settings.edit();
	    		editor.putLong(Constants.PREFS_LAST_SYNC, update.getLastUpdate());
	            editor.commit();

				if(!bookmarkList.isEmpty()){
	
		            editor.putBoolean(Constants.PREFS_INITIAL_SYNC, true);
		            editor.commit();
					
					for(Bookmark b : bookmarkList){
						ContentValues values = new ContentValues();
						
						values.put(Bookmark.Description, b.getDescription());
						values.put(Bookmark.Url, b.getUrl());
						values.put(Bookmark.Notes, b.getNotes());
						values.put(Bookmark.Tags, b.getTags());
						values.put(Bookmark.Hash, b.getHash());
						values.put(Bookmark.Meta, b.getMeta());
						values.put(Bookmark.Time, b.getTime());
						
						Uri uri = mContext.getContentResolver().insert(Bookmark.CONTENT_URI, values);
						Log.d("bookmark", uri.toString());
					}
					

				}
			}
    	} else {
    		Log.d("BookmarkSync", "No update needed.  Last update time before last sync.");
    		Log.d("update", Long.toString(update.getLastUpdate()));
    		Log.d("lastupdate", Long.toString(lastUpdate));
    	}
    }
}
