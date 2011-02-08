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

package com.pindroid.syncadapter;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.pindroid.Constants;
import com.pindroid.client.PinboardApi;
import com.pindroid.client.Update;
import com.pindroid.platform.BookmarkManager;
import com.pindroid.platform.TagManager;
import com.pindroid.providers.BookmarkContent.Bookmark;
import com.pindroid.providers.TagContent.Tag;

import org.apache.http.ParseException;
import org.apache.http.auth.AuthenticationException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * SyncAdapter implementation for syncing bookmarks.
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

    	final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
    	long lastUpdate = settings.getLong(Constants.PREFS_LAST_SYNC, 0);
    	Update update = null;
    	final String username = account.name;

    	update = PinboardApi.lastUpdate(account, mContext);
    	
    	if(update.getLastUpdate() > lastUpdate) {
	
			Log.d("BookmarkSync", "In Bookmark Load");
			final ArrayList<String> accounts = new ArrayList<String>();
			accounts.add(account.name);
			BookmarkManager.TruncateBookmarks(accounts, mContext, false);
			TagManager.TruncateTags(username, mContext);
			
			final ArrayList<Tag> tagList = PinboardApi.getTags(account, mContext);
			final ArrayList<Bookmark> addBookmarkList = PinboardApi.getAllBookmarks(null, account, mContext);
			
			final ContentResolver resolver = mContext.getContentResolver();
			
			int tagsize = tagList.size();
			ContentValues[] tcv = new ContentValues[tagsize];
			
			if(!tagList.isEmpty()){
				for(int i = 0; i < tagsize; i++){	
					Tag t = tagList.get(i);
					
					ContentValues values = new ContentValues();
					
					values.put(Tag.Name, t.getTagName());
					values.put(Tag.Count, t.getCount());
					values.put(Tag.Account, username);
					
					tcv[i] = values;
				}
			}
			
			resolver.bulkInsert(Tag.CONTENT_URI, tcv);
			tcv = null;

			int bookmarksize = addBookmarkList.size();
			ContentValues[] bcv = new ContentValues[bookmarksize];

			if(!addBookmarkList.isEmpty()){
				for(int i = 0; i < bookmarksize; i++){
					Bookmark b = addBookmarkList.get(i);
					
					ContentValues values = new ContentValues();
					values.put(Bookmark.Description, b.getDescription());
					values.put(Bookmark.Url, b.getUrl());
					values.put(Bookmark.Notes, b.getNotes());
					values.put(Bookmark.Tags, b.getTagString());
					values.put(Bookmark.Hash, b.getHash());
					values.put(Bookmark.Meta, b.getMeta());
					values.put(Bookmark.Time, b.getTime());
					values.put(Bookmark.Account, username);
					values.put(Bookmark.ToRead, b.getToRead() ? 1 : 0);
					values.put(Bookmark.Shared, b.getShared() ? 1 : 0);
					
					bcv[i] = values;
				}
			}
			
			resolver.bulkInsert(Bookmark.CONTENT_URI, bcv);
			
    		final SharedPreferences.Editor editor = settings.edit();
    		editor.putLong(Constants.PREFS_LAST_SYNC, update.getLastUpdate());
            editor.commit();

    	} else {
    		Log.d("BookmarkSync", "No update needed.  Last update time before last sync.");
    	}
    }
}