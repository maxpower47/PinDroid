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

package com.deliciousdroid.syncadapter;

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

import com.deliciousdroid.R;
import com.deliciousdroid.Constants;
import com.deliciousdroid.activity.Main;
import com.deliciousdroid.client.DeliciousApi;
import com.deliciousdroid.client.Update;
import com.deliciousdroid.platform.BookmarkManager;
import com.deliciousdroid.platform.TagManager;
import com.deliciousdroid.providers.BookmarkContent.Bookmark;
import com.deliciousdroid.providers.TagContent.Tag;

import org.apache.http.ParseException;
import org.apache.http.auth.AuthenticationException;

import java.io.IOException;
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
            InsertBookmarks(account, syncResult);
        } catch (final ParseException e) {
            syncResult.stats.numParseExceptions++;
            Log.e(TAG, "ParseException", e);
        } catch (final AuthenticationException e) {
            syncResult.stats.numAuthExceptions++;
            Log.e(TAG, "AuthException", e);
        } catch (final IOException e) {
            syncResult.stats.numIoExceptions++;
            Log.e(TAG, "IOException", e);
        }
    }
    
    private void InsertBookmarks(Account account, SyncResult syncResult) 
    	throws AuthenticationException, IOException{
    	
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
    	long lastUpdate = settings.getLong(Constants.PREFS_LAST_SYNC, 0);
    	Boolean notifyPref = settings.getBoolean("pref_notification", true);
    	Update update = null;
    	String username = account.name;

    	update = DeliciousApi.lastUpdate(account, mContext);
		
		if(notifyPref && update.getInboxNew() > 0) {
			NotificationManager nm = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
			Notification n = new Notification(R.drawable.ic_main, "New Delicious Bookmarks", System.currentTimeMillis());
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

			if(lastUpdate == 0){
				Log.d("BookmarkSync", "In Bookmark Load");
				tagList = DeliciousApi.getTags(account, mContext);
				ArrayList<String> accounts = new ArrayList<String>();
				accounts.add(account.name);
				BookmarkManager.TruncateBookmarks(accounts, mContext, false);
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
						
						BookmarkManager.SetLastUpdate(b, update.getLastUpdate(), username, mContext);
						Log.d(b.getHash(), Long.toString(update.getLastUpdate()));
						
						do {							
							if(c.getString(metaColumn) == null || !c.getString(metaColumn).equals(b.getMeta())) {
								updateList.add(b);
							}	
						} while(c.moveToNext());
					}
					
					c.close();
				}
	
				BookmarkManager.DeleteOldBookmarks(update.getLastUpdate(), username, mContext);
				
				ArrayList<String> addHashes = new ArrayList<String>();
				for(Bookmark b : addList){
					addHashes.add(b.getHash());
				}
				Log.d("add size", Integer.toString(addHashes.size()));
				syncResult.stats.numInserts = addHashes.size();
				if(addHashes.size() > 0) {
					addBookmarkList = DeliciousApi.getBookmark(addHashes, account, mContext);
				}
				
				ArrayList<String> updateHashes = new ArrayList<String>();
				for(Bookmark b : updateList){
					updateHashes.add(b.getHash());
				}
				Log.d("update size", Integer.toString(updateHashes.size()));
				syncResult.stats.numUpdates = updateHashes.size();
				if(updateHashes.size() > 0) {
					updateBookmarkList = DeliciousApi.getBookmark(updateHashes, account, mContext);
				}
			}
			
			TagManager.TruncateTags(username, mContext);
			for(Tag b : tagList){
				TagManager.AddTag(b, username, mContext);
			}

			if(!addBookmarkList.isEmpty()){				
				for(Bookmark b : addBookmarkList){
					BookmarkManager.AddBookmark(b, username, mContext);
				}
			}
			
			if(!updateBookmarkList.isEmpty()){		
				for(Bookmark b : updateBookmarkList){
					BookmarkManager.UpdateBookmark(b, username, mContext);
				}
			}
			
    		SharedPreferences.Editor editor = settings.edit();
    		editor.putLong(Constants.PREFS_LAST_SYNC, update.getLastUpdate());
            editor.commit();
    	} else {
    		Log.d("BookmarkSync", "No update needed.  Last update time before last sync.");
    	}
    }
}
