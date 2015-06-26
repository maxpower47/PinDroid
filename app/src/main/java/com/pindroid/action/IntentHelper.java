package com.pindroid.action;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.pindroid.Constants;
import com.pindroid.activity.AddBookmark_;
import com.pindroid.activity.Main_;

public class IntentHelper {
	
	public static Intent SendBookmark(String url, String title) {
    	Intent sendIntent = new Intent(Intent.ACTION_SEND);
    	sendIntent.setType("text/plain");
    	sendIntent.putExtra(Intent.EXTRA_TEXT, url);
    	sendIntent.putExtra(Intent.EXTRA_SUBJECT, title);
    	sendIntent.putExtra(Intent.EXTRA_TITLE, title);
    	
    	return sendIntent;
	}
	
	public static Intent AddBookmark(String account, Context context) {
		Intent addBookmark = AddBookmark_.intent(context)
                .action(Intent.ACTION_VIEW)
                .username(account)
                .get();

		Uri.Builder data = new Uri.Builder();
		data.scheme(Constants.CONTENT_SCHEME);
		data.encodedAuthority((account != null ? account + "@" : "") + Constants.INTENT_URI);
		data.appendEncodedPath("bookmarks");
		addBookmark.setData(data.build());

		return addBookmark;
	}
	
	public static Intent ViewBookmarks(String tag, String account, Context context) {
		Intent i = Main_.intent(context)
                .action(Intent.ACTION_VIEW)
                .get();

		Uri.Builder data = new Uri.Builder();
		data.scheme(Constants.CONTENT_SCHEME);
		data.encodedAuthority((account != null ? account + "@" : "") + Constants.INTENT_URI);
		data.appendEncodedPath("bookmarks");
		
		if(tag != null && !tag.equals(""))
			data.appendQueryParameter("tagname", tag);
		
		i.setData(data.build());
		
		return i;
	}
	
	public static Intent ViewUnread(String account, Context context) {
        Intent i = Main_.intent(context)
                .action(Intent.ACTION_VIEW)
                .get();

		Uri.Builder data = new Uri.Builder();
		data.scheme(Constants.CONTENT_SCHEME);
		data.encodedAuthority((account != null ? account + "@" : "") + Constants.INTENT_URI);
		data.appendEncodedPath("bookmarks");
		data.appendQueryParameter("unread", "1");
		i.setData(data.build());
		
		return i;
	}
	
	public static Intent ViewNotes(String account, Context context) {
        Intent i = Main_.intent(context)
                .action(Intent.ACTION_VIEW)
                .get();

		Uri.Builder data = new Uri.Builder();
		data.scheme(Constants.CONTENT_SCHEME);
		data.encodedAuthority((account != null ? account + "@" : "") + Constants.INTENT_URI);
		data.appendEncodedPath("notes");
		i.setData(data.build());
		
		return i;
	}
	
	public static Intent WidgetSearch(String account, Context context){
        Intent i = Main_.intent(context)
                .action(Intent.ACTION_SEARCH)
                .get();

		Uri.Builder data = new Uri.Builder();
		data.scheme(Constants.CONTENT_SCHEME);
		data.encodedAuthority((account != null ? account + "@" : "") + Constants.INTENT_URI);
		data.appendEncodedPath("search");
		i.setData(data.build());
		
		return i;
	}

    public static Intent OpenInBrowser(String url){
        Uri link = Uri.parse(url);
        return new Intent(Intent.ACTION_VIEW, link);
    }
}
