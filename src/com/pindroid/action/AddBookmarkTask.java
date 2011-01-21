package com.pindroid.action;

import android.accounts.Account;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.pindroid.Constants;
import com.pindroid.R;
import com.pindroid.activity.AddBookmark;
import com.pindroid.client.PinboardApi;
import com.pindroid.platform.BookmarkManager;
import com.pindroid.platform.TagManager;
import com.pindroid.providers.BookmarkContent.Bookmark;
import com.pindroid.providers.TagContent.Tag;

public class AddBookmarkTask extends AsyncTask<BookmarkTaskArgs, Integer, Boolean>{
	private Context context;
	private Bookmark bookmark;
	private Bookmark oldBookmark;
	private Account account;
	private Boolean update;
	
	@Override
	protected Boolean doInBackground(BookmarkTaskArgs... args) {
		context = args[0].getContext();
		bookmark = args[0].getBookmark();
		oldBookmark = args[0].getOldBookmark();
		account = args[0].getAccount();
		update = args[0].getUpdate();
		
		try {
			Boolean success = PinboardApi.addBookmark(bookmark, account, context);
			if(success){
				if(update){
					BookmarkManager.UpdateBookmark(bookmark, account.name, context);
				} else {
					BookmarkManager.AddBookmark(bookmark, account.name, context);
				}
				return true;
			} else return false;
		} catch (Exception e) {
			Log.d("addBookmark error", e.toString());
			return false;
		}
	}

    protected void onPostExecute(Boolean result) {

		if(result){
			for(Tag t : bookmark.getTags()){   				
				TagManager.UpsertTag(t, account.name, context);
			}
			
			if(update) {
    			for(Tag t : oldBookmark.getTags()){
    				if(!bookmark.getTags().contains(t)) {
    					TagManager.UpleteTag(t, account.name, context);
    				}
    			}
			}
		} else {
			throwError();
		}
    }
    
    private void throwError(){
    	 NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
    	 Notification n = new Notification(R.drawable.ic_main, "PinDroid Error", System.currentTimeMillis());
    	 Intent ni = new Intent(context, AddBookmark.class);
    	 ni.setAction(Intent.ACTION_SEND);
    	 ni.putExtra(Intent.EXTRA_TEXT, bookmark.getUrl());
    	 ni.putExtra(Constants.EXTRA_DESCRIPTION, bookmark.getDescription());
    	 ni.putExtra(Constants.EXTRA_NOTES, bookmark.getNotes());
    	 ni.putExtra(Constants.EXTRA_TAGS, bookmark.getTagString());
    	 ni.putExtra(Constants.EXTRA_PRIVATE, !bookmark.getShared());
    	 ni.putExtra(Constants.EXTRA_TOREAD, bookmark.getToRead());
    	 PendingIntent ci = PendingIntent.getActivity(context, 0, ni, 0);
    	 n.setLatestEventInfo(context, "PinDroid Error", "Error Saving Bookmark", ci);

    	 nm.notify(1, n);
    }
}