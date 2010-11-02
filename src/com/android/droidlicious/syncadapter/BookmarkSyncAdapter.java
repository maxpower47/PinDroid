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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.droidlicious.Constants;
import com.android.droidlicious.R;
import com.android.droidlicious.activity.Main;
import com.android.droidlicious.client.DeliciousApi;
import com.android.droidlicious.client.Update;
import com.android.droidlicious.platform.BookmarkManager;
import com.android.droidlicious.platform.TagManager;
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

    public BookmarkSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
        ContentProviderClient provider, SyncResult syncResult) {

         try {
            InsertBookmarks(account);
        }catch (final ParseException e) {
            syncResult.stats.numParseExceptions++;
            Log.e(TAG, "ParseException", e);
        }
    }
    
    private void InsertBookmarks(Account account){
    	
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
    	long lastUpdate = settings.getLong(Constants.PREFS_LAST_SYNC, 0);
    	Boolean notifyPref = settings.getBoolean("pref_notification", true);
    	Update update = null;
    	Boolean success = true;
    	
    	try {
    		update = DeliciousApi.lastUpdate(account, mContext);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		if(notifyPref && update.getInboxNew() > 0) {
			NotificationManager nm = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
			Notification n = new Notification(R.drawable.icon, "New Delicious Bookmarks", System.currentTimeMillis());
			Intent ni = new Intent(mContext, Main.class);
			PendingIntent ci = PendingIntent.getActivity(mContext, 0, ni, 0);
			n.setLatestEventInfo(mContext, "New Bookmarks", "You Have " + Integer.toString(update.getInboxNew()) + " New Bookmark(s)", ci);
			
			nm.notify(1, n);
		}
    	
    	if(update.getLastUpdate() > lastUpdate) {
	
			ArrayList<Bookmark> addBookmarkList = new ArrayList<Bookmark>();
			ArrayList<Bookmark> updateBookmarkList = new ArrayList<Bookmark>();
			ArrayList<Bookmark> changeList = new ArrayList<Bookmark>();
			ArrayList<Bookmark> addList = new ArrayList<Bookmark>();
			ArrayList<Bookmark> updateList = new ArrayList<Bookmark>();
			ArrayList<Tag> tagList = new ArrayList<Tag>();

			try {
				if(lastUpdate == 0){
					Log.d("BookmarkSync", "In Bookmark Load");
					tagList = DeliciousApi.getTags(account, mContext);
					addBookmarkList = DeliciousApi.getAllBookmarks(null, account, mContext);
				} else {
					Log.d("BookmarkSync", "In Bookmark Update");
					tagList = DeliciousApi.getTags(account, mContext);
					changeList = DeliciousApi.getChangedBookmarks(account, mContext);
					
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
							
							BookmarkManager.SetLastUpdate(b, update.getLastUpdate(), mContext);
							Log.d(b.getHash(), Long.toString(update.getLastUpdate()));
							
							do {							
								if(c.getString(metaColumn) == null || !c.getString(metaColumn).equals(b.getMeta())) {
									updateList.add(b);
								}	
							} while(c.moveToNext());
						}
						
						c.close();
					}
		
					BookmarkManager.DeleteOldBookmarks(update.getLastUpdate(), mContext);
					
					ArrayList<String> addHashes = new ArrayList<String>();
					for(Bookmark b : addList){
						addHashes.add(b.getHash());
					}
					Log.d("add size", Integer.toString(addHashes.size()));
					if(addHashes.size() > 0) {
						addBookmarkList = DeliciousApi.getBookmark(addHashes, account, mContext);
					}
					
					ArrayList<String> updateHashes = new ArrayList<String>();
					for(Bookmark b : updateList){
						updateHashes.add(b.getHash());
					}
					Log.d("update size", Integer.toString(updateHashes.size()));
					if(updateHashes.size() > 0) {
						updateBookmarkList = DeliciousApi.getBookmark(updateHashes, account, mContext);
					}
					
				}
			} catch (Exception e) {
				success = false;
				e.printStackTrace();
			}
			
			TagManager.TruncateTags(mContext);
			for(Tag b : tagList){
				TagManager.AddTag(b, mContext);
			}
			
			if(success){
	    		SharedPreferences.Editor editor = settings.edit();
	    		editor.putLong(Constants.PREFS_LAST_SYNC, update.getLastUpdate());
	            editor.commit();

				if(!addBookmarkList.isEmpty()){				
					for(Bookmark b : addBookmarkList){
						BookmarkManager.AddBookmark(b, mContext);
					}
				}
				
				if(!updateBookmarkList.isEmpty()){		
					for(Bookmark b : updateBookmarkList){
						BookmarkManager.UpdateBookmark(b, mContext);
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
