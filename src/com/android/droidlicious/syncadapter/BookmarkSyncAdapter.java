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
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.android.droidlicious.Constants;
import com.android.droidlicious.authenticator.AuthToken;
import com.android.droidlicious.client.NetworkUtilities;
import com.android.droidlicious.client.User;
import com.android.droidlicious.providers.BookmarkContent.Bookmark;

import org.apache.http.ParseException;

import java.util.Date;
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
    	long update = 1;
    	
    	try {
			update = NetworkUtilities.lastUpdate(account.name, account, authtoken, mContext);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
    	
    	if(update > lastUpdate) {
    		Date d = new Date();
    		
    		SharedPreferences.Editor editor = settings.edit();
    		editor.putLong(Constants.PREFS_LAST_SYNC, d.getTime());
            editor.commit();
    		
			ArrayList<User.Bookmark> bookmarkList = new ArrayList<User.Bookmark>();
			ArrayList<User.Bookmark> changeList = new ArrayList<User.Bookmark>();
			ArrayList<User.Bookmark> addList = new ArrayList<User.Bookmark>();
			ArrayList<User.Bookmark> updateList = new ArrayList<User.Bookmark>();
	
			try {
				if(!initialSync){
					Log.d("BookmarkSync", "In Bookmark Load");
					bookmarkList = NetworkUtilities.fetchMyBookmarks(account.name, null, account, authtoken, mContext, true);
				} else {
					Log.d("BookmarkSync", "In Bookmark Update");
					changeList = NetworkUtilities.fetchChangedBookmarks(account.name, account, authtoken, mContext);
					
					for(User.Bookmark b : changeList){
					
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
					for(User.Bookmark b : addList){
						a.add(b.getHash());
					}
					Log.d("size", Integer.toString(a.size()));
					if(a.size() > 0) {
						bookmarkList = NetworkUtilities.fetchBookmark(account.name, a, account, authtoken, mContext);
					}
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if(!bookmarkList.isEmpty()){

	            editor.putBoolean(Constants.PREFS_INITIAL_SYNC, true);
	            editor.commit();
				
				for(User.Bookmark b : bookmarkList){
					ContentValues values = new ContentValues();
					
					values.put(Bookmark.Description, b.getDescription());
					values.put(Bookmark.Url, b.getUrl());
					values.put(Bookmark.Notes, b.getNotes());
					values.put(Bookmark.Tags, b.getTags());
					values.put(Bookmark.Hash, b.getHash());
					values.put(Bookmark.Meta, b.getMeta());
					
					Uri uri = mContext.getContentResolver().insert(Bookmark.CONTENT_URI, values);
					Log.d("bookmark", uri.toString());
				}	
			}
    	} else {
    		Log.d("BookmarkSync", "No update needed.  Last update time before last sync.");
    	}
    }
}
