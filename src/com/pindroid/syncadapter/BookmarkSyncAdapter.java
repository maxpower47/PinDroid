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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.auth.AuthenticationException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.pindroid.Constants;
import com.pindroid.client.PinboardApi;
import com.pindroid.client.PinboardException;
import com.pindroid.client.TooManyRequestsException;
import com.pindroid.client.Update;
import com.pindroid.platform.BookmarkManager;
import com.pindroid.platform.NoteManager;
import com.pindroid.platform.TagManager;
import com.pindroid.providers.BookmarkContent.Bookmark;
import com.pindroid.providers.NoteContent.Note;
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
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
    	
    	boolean upload = extras.containsKey(ContentResolver.SYNC_EXTRAS_UPLOAD);
    	boolean manual = extras.containsKey(ContentResolver.SYNC_EXTRAS_IGNORE_BACKOFF) && extras.containsKey(ContentResolver.SYNC_EXTRAS_IGNORE_SETTINGS);
    	
        try {
        	if(upload){
        		Log.d(TAG, "Beginning Upload Sync");
        		DeleteBookmarks(account, syncResult);
        		UploadBookmarks(account, syncResult);
        	} else {
            	if(manual)
            		Log.d(TAG, "Beginning Manual Download Sync");
            	else Log.d(TAG, "Beginning Download Sync");
            	
        		DeleteBookmarks(account, syncResult);
        		UploadBookmarks(account, syncResult);
        		InsertBookmarks(account, syncResult);
        	}
        	
        	checkSecretToken(account);
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
        } catch (PinboardException e) {
        	syncResult.stats.numSkippedEntries++;
            Log.e(TAG, "PinboardException", e);
		} finally {
        	Log.d(TAG, "Finished Sync");
        }
    }
    
    private void InsertBookmarks(Account account, SyncResult syncResult) 
    	throws AuthenticationException, IOException, TooManyRequestsException, ParseException, PinboardException{
    	
    	long lastUpdate = getServerSyncMarker(account);
    	final String username = account.name;
    	mAccount = account;

    	final Update update = PinboardApi.lastUpdate(account, mContext);
    	
    	if(update.getLastUpdate() > lastUpdate) {
	
			Log.d(TAG, "In Bookmark Load");
			final ArrayList<String> accounts = new ArrayList<String>();
			accounts.add(account.name);
	
			final ArrayList<Bookmark> addBookmarkList = getBookmarkList();	
			BookmarkManager.TruncateBookmarks(accounts, mContext, false);		
			if(!addBookmarkList.isEmpty()){
				List<Bookmark> unsyncedBookmarks = BookmarkManager.GetLocalBookmarks(username, mContext);
				addBookmarkList.removeAll(unsyncedBookmarks);
				
				BookmarkManager.BulkInsert(addBookmarkList, username, mContext);
			}
			
			final ArrayList<Tag> tagList = PinboardApi.getTags(account, mContext);
			TagManager.TruncateTags(username, mContext);
			if(!tagList.isEmpty()){
				TagManager.BulkInsert(tagList, username, mContext);
			}
			
			SyncNotes();
        
            setServerSyncMarker(account, update.getLastUpdate());

            syncResult.stats.numEntries += addBookmarkList.size();
    	} else {
    		Log.d(TAG, "No update needed.  Last update time before last sync.");
    	}
    }
    
    private void SyncNotes() throws AuthenticationException, IOException, TooManyRequestsException, PinboardException{
    	
		final ArrayList<Note> noteList = PinboardApi.getNoteList(mAccount, mContext);
		NoteManager.TruncateNotes(mAccount.name, mContext);
		
		for(Note n : noteList){
			//NoteManager.UpsertNote(n, mAccount.name, mContext);
			
			Note t = PinboardApi.getNote(n.getPid(), mAccount, mContext);
			n.setText(t.getText());
		}
		
		if(!noteList.isEmpty()){
			NoteManager.BulkInsert(noteList, mAccount.name, mContext);
		}
    }
    
    private void UploadBookmarks(Account account, SyncResult syncResult) 
		throws AuthenticationException, IOException, TooManyRequestsException, ParseException{
    
    	final ArrayList<Bookmark> bookmarks = BookmarkManager.GetLocalBookmarks(account.name, mContext);
    	
    	for(Bookmark b : bookmarks)
    	{
    		try{
				PinboardApi.addBookmark(b, account, mContext);
	
				Log.d(TAG, "Bookmark edited: " + (b.getHash() == null ? "" : b.getHash()));
				b.setSynced(1);
				BookmarkManager.SetSynced(b, 1, account.name, mContext);
				
				syncResult.stats.numEntries++;
    		}
    		catch(PinboardException e){
    			Log.d(TAG, "Error editing bookmark: " + (b.getHash() == null ? "" : b.getHash()));
				b.setSynced(-1);
				BookmarkManager.SetSynced(b, -1, account.name, mContext);
    		}
    	}	
    }
    
    private void DeleteBookmarks(Account account, SyncResult syncResult) 
		throws AuthenticationException, IOException, TooManyRequestsException, ParseException, PinboardException{
	
		final ArrayList<Bookmark> bookmarks = BookmarkManager.GetDeletedBookmarks(account.name, mContext);
		
		for(Bookmark b : bookmarks)
		{
			PinboardApi.deleteBookmark(b, account, mContext);
	
			Log.d(TAG, "Bookmark deleted: " + (b.getHash() == null ? "" : b.getHash()));
			BookmarkManager.DeleteBookmark(b, mContext);
		}
		
		syncResult.stats.numEntries += bookmarks.size();
	}
    
    private ArrayList<Bookmark> getBookmarkList()
    	throws AuthenticationException, IOException, TooManyRequestsException, PinboardException {
    	int pageSize = Constants.BOOKMARK_PAGE_SIZE;
    	ArrayList<Bookmark> results = new ArrayList<Bookmark>();   	

		int page = 0;
		boolean morePages = true;
		
		do{
			morePages = results.addAll(PinboardApi.getAllBookmarks(null, page++ * pageSize, pageSize, mAccount, mContext));
		} while(morePages);

		return results;
    }
    
    private void checkSecretToken(Account account) throws AuthenticationException, IOException, TooManyRequestsException, ParseException, PinboardException{
    	
    	String token = mAccountManager.getUserData(account, Constants.PREFS_SECRET_TOKEN);
    	
    	if(token == null){
    		token = PinboardApi.getSecretToken(account, mContext);		
			mAccountManager.setUserData(account, Constants.PREFS_SECRET_TOKEN, token);
    	}
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