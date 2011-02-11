package com.pindroid.action;

import java.net.URLEncoder;

import com.pindroid.Constants;
import com.pindroid.providers.BookmarkContent.Bookmark;
import com.pindroid.providers.BookmarkContentProvider;

import android.content.Intent;
import android.net.Uri;

public class IntentHelper {

	public static Intent OpenInBrowser(String url){
    	Uri link = Uri.parse(url);
		return new Intent(Intent.ACTION_VIEW, link);
	}
	
	public static Intent ReadBookmark(String url){
    	String readUrl = Constants.INSTAPAPER_URL + URLEncoder.encode(url);
    	Uri readLink = Uri.parse(readUrl);
		return new Intent(Intent.ACTION_VIEW, readLink);
	}
	
	public static Intent SendBookmark(String url, String title) {
    	Intent sendIntent = new Intent(Intent.ACTION_SEND);
    	sendIntent.setType("text/plain");
    	sendIntent.putExtra(Intent.EXTRA_TEXT, url);
    	sendIntent.putExtra(Intent.EXTRA_SUBJECT, title);
    	sendIntent.putExtra(Intent.EXTRA_TITLE, title);
    	
    	return sendIntent;
	}
	
	public static Intent ViewBookmark(Bookmark b, String account) {
		
		Intent viewBookmark = new Intent();
		viewBookmark.setAction(Intent.ACTION_VIEW);
		viewBookmark.addCategory(Intent.CATEGORY_DEFAULT);
		Uri.Builder data = new Uri.Builder();
		data.scheme(Constants.CONTENT_SCHEME);
		data.encodedAuthority(account + "@" + BookmarkContentProvider.AUTHORITY);
		data.appendEncodedPath("bookmarks");
		
		if(b.getId() != 0) {
			data.appendEncodedPath(Integer.toString(b.getId()));
		} else {
			data.appendEncodedPath(Integer.toString(0));
			data.appendQueryParameter("url", b.getUrl());
			data.appendQueryParameter("title", b.getDescription());
			data.appendQueryParameter("notes", b.getNotes());
			data.appendQueryParameter("tags", b.getTagString());
			data.appendQueryParameter("time", Long.toString(b.getTime()));
			data.appendQueryParameter("account", b.getAccount());
		}
		viewBookmark.setData(data.build());
		
		return viewBookmark;
	}
}
