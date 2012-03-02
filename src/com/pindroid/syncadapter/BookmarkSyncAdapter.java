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

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.ParseException;
import org.apache.http.auth.AuthenticationException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.pindroid.Constants;
import com.pindroid.client.PinboardApi;
import com.pindroid.client.TooManyRequestsException;
import com.pindroid.client.Update;
import com.pindroid.platform.BookmarkManager;
import com.pindroid.platform.TagManager;
import com.pindroid.providers.BookmarkContent.Bookmark;
import com.pindroid.providers.TagContent.Tag;

/**
 * SyncAdapter implementation for syncing bookmarks.
 */
public class BookmarkSyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "BookmarkSyncAdapter";
    

    private final Context mContext;
    private Account mAccount;
    private final AccountManager mAccountManager;

    public BookmarkSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
        mAccountManager = AccountManager.get(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
        ContentProviderClient provider, SyncResult syncResult) {
    	Log.d(TAG, "Beginning Sync");
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
        } catch (final TooManyRequestsException e) {
        	syncResult.delayUntil = e.getBackoff();
        	Log.d(TAG, "Too Many Requests.  Backing off for " + e.getBackoff() + " seconds.");
        } finally {
        	Log.d(TAG, "Finished Sync");
        }
    }
    
    private void InsertBookmarks(Account account, SyncResult syncResult) 
    	throws AuthenticationException, IOException, TooManyRequestsException{
    	
    	long lastUpdate = getServerSyncMarker(account);
    	final String username = account.name;
    	mAccount = account;

    	final Update update = PinboardApi.lastUpdate(account, mContext);
    	
    	if(update.getLastUpdate() > lastUpdate) {
	
			Log.d(TAG, "In Bookmark Load");
			final ArrayList<String> accounts = new ArrayList<String>();
			accounts.add(account.name);
			BookmarkManager.TruncateBookmarks(accounts, mContext, false);
			TagManager.TruncateTags(username, mContext);
			
			final ArrayList<Tag> tagList = PinboardApi.getTags(account, mContext);
			final ArrayList<Bookmark> addBookmarkList = getBookmarkList();
			
			if(!tagList.isEmpty()){
				TagManager.BulkInsert(tagList, username, mContext);
			}

			if(!addBookmarkList.isEmpty()){
				BookmarkManager.BulkInsert(addBookmarkList, username, mContext);
			}
            
            setServerSyncMarker(account, update.getLastUpdate());

    	} else {
    		Log.d(TAG, "No update needed.  Last update time before last sync.");
    	}
    }
    
    private ArrayList<Bookmark> getBookmarkList()
    	throws AuthenticationException, IOException, TooManyRequestsException {
    	int pageSize = Constants.BOOKMARK_PAGE_SIZE;
    	ArrayList<Bookmark> results = new ArrayList<Bookmark>();   	

		int page = 0;
		boolean morePages = true;
		
		do{
			morePages = results.addAll(PinboardApi.getAllBookmarks(null, page++ * pageSize, pageSize, mAccount, mContext));
		} while(morePages);

		return results;
    }
    
    /**
     * This helper function fetches the last known high-water-mark
     * we received from the server - or 0 if we've never synced.
     * @param account the account we're syncing
     * @return the change high-water-mark
     */
    private long getServerSyncMarker(Account account) {
        String markerString = mAccountManager.getUserData(account, Constants.SYNC_MARKER_KEY);
        if (!TextUtils.isEmpty(markerString)) {
            return Long.parseLong(markerString);
        }
        return 0;
    }

    /**
     * Save off the high-water-mark we receive back from the server.
     * @param account The account we're syncing
     * @param marker The high-water-mark we want to save.
     */
    private void setServerSyncMarker(Account account, long marker) {
        mAccountManager.setUserData(account, Constants.SYNC_MARKER_KEY, Long.toString(marker));
    }
}