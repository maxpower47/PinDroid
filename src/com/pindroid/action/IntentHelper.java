package com.pindroid.action;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.pindroid.activity.AddBookmark;
import com.pindroid.activity.BrowseBookmarks;
import com.pindroid.activity.BrowseNotes;
import com.pindroid.activity.BrowseTags;
import com.pindroid.activity.Main;
import com.pindroid.activity.ViewBookmark;
import com.pindroid.Constants;
import com.pindroid.Constants.BookmarkViewType;
import com.pindroid.providers.BookmarkContent.Bookmark;
import com.pindroid.providers.NoteContent.Note;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class IntentHelper {

	public static Intent OpenInBrowser(String url){
    	Uri link = Uri.parse(url);
		return new Intent(Intent.ACTION_VIEW, link);
	}
	
	public static Intent ReadBookmark(String url){
    	String readUrl = "";
		try {
			readUrl = Constants.TEXT_EXTRACTOR_URL + URLEncoder.encode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
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
	
	public static Intent AddBookmark(String url, String account, Context context) {
		Intent addBookmark = new Intent(context, Main.class);
		addBookmark.setAction(Intent.ACTION_SEND);
		if(url != null)
			addBookmark.putExtra(Intent.EXTRA_TEXT, url);
		
		addBookmark.putExtra(Constants.EXTRA_INTERNAL, true);
		Uri.Builder data = new Uri.Builder();
		data.scheme(Constants.CONTENT_SCHEME);
		data.encodedAuthority((account != null ? account + "@" : "") + Constants.INTENT_URI);
		data.appendEncodedPath("bookmarks");
		addBookmark.setData(data.build());
		
		return addBookmark;
	}
	
	public static Intent ViewBookmark(Bookmark b, BookmarkViewType type, String account, Context context) {
		Intent viewBookmark = new Intent(context, ViewBookmark.class);
		viewBookmark.setAction(Intent.ACTION_VIEW);
		viewBookmark.addCategory(Intent.CATEGORY_DEFAULT);
		viewBookmark.putExtra(Constants.EXTRA_VIEWTYPE, type);
		viewBookmark.putExtra(Constants.EXTRA_BOOKMARK, b);
		Uri.Builder data = new Uri.Builder();
		data.scheme(Constants.CONTENT_SCHEME);
		data.encodedAuthority((account != null ? account + "@" : "") + Constants.INTENT_URI);
		data.appendEncodedPath("bookmarks");
		data.appendEncodedPath(Integer.toString(b.getId()));

		viewBookmark.setData(data.build());
		
		return viewBookmark;
	}
	
	public static Intent ViewNote(Note n, String account, Context context) {
		Intent viewBookmark = new Intent(context, com.pindroid.activity.ViewNote.class);
		viewBookmark.setAction(Intent.ACTION_VIEW);
		viewBookmark.addCategory(Intent.CATEGORY_DEFAULT);
		Uri.Builder data = new Uri.Builder();
		data.scheme(Constants.CONTENT_SCHEME);
		data.encodedAuthority((account != null ? account + "@" : "") + Constants.INTENT_URI);
		data.appendEncodedPath("notes");
		
		data.appendEncodedPath(Integer.toString(n.getId()));

		viewBookmark.setData(data.build());
		
		return viewBookmark;
	}
	
	public static Intent EditBookmark(Bookmark b, String account, Context context) {
		Intent editBookmark = new Intent(context, AddBookmark.class);
		editBookmark.setAction(Intent.ACTION_EDIT);
		Uri.Builder data = new Uri.Builder();
		data.scheme(Constants.CONTENT_SCHEME);
		data.encodedAuthority((account != null ? account + "@" : "") + Constants.INTENT_URI);
		data.appendEncodedPath("bookmarks");
		data.appendEncodedPath(Integer.toString(b.getId()));
		editBookmark.setData(data.build());
		
		return editBookmark;
	}
	
	public static Intent ViewBookmarks(String tag, String account, String feed, Context context) {
		Intent i = new Intent(context, Main.class);
		i.setAction(Intent.ACTION_VIEW);
		i.addCategory(Intent.CATEGORY_DEFAULT);
		Uri.Builder data = new Uri.Builder();
		data.scheme(Constants.CONTENT_SCHEME);
		data.encodedAuthority((account != null ? account + "@" : "") + Constants.INTENT_URI);
		data.appendEncodedPath("bookmarks");
		
		if(tag != null && !tag.equals(""))
			data.appendQueryParameter("tagname", tag);
		
		if(feed != null && !feed.equals(""))
			data.appendQueryParameter("feed", feed);
		
		i.setData(data.build());
		
		return i;
	}
	
	public static Intent ViewNotes(String account, Context context) {
		Intent i = new Intent(context, BrowseNotes.class);
		i.setAction(Intent.ACTION_VIEW);
		i.addCategory(Intent.CATEGORY_DEFAULT);
		Uri.Builder data = new Uri.Builder();
		data.scheme(Constants.CONTENT_SCHEME);
		data.encodedAuthority((account != null ? account + "@" : "") + Constants.INTENT_URI);
		data.appendEncodedPath("notes");
		
		i.setData(data.build());
		
		return i;
	}
	
	public static Intent ViewUnread(String account, Context context) {
		Intent i = new Intent(context, Main.class);
		i.setAction(Intent.ACTION_VIEW);
		i.addCategory(Intent.CATEGORY_DEFAULT);
		Uri.Builder data = new Uri.Builder();
		data.scheme(Constants.CONTENT_SCHEME);
		data.encodedAuthority((account != null ? account + "@" : "") + Constants.INTENT_URI);
		data.appendEncodedPath("bookmarks");
		data.appendQueryParameter("unread", "1");
		i.setData(data.build());
		
		return i;
	}
	
	public static Intent ViewTags(String account, Context context) {
		Intent i = new Intent(context, Main.class);
		i.setAction(Intent.ACTION_VIEW);
		i.addCategory(Intent.CATEGORY_DEFAULT);
		Uri.Builder data = new Uri.Builder();
		data.scheme(Constants.CONTENT_SCHEME);
		data.encodedAuthority((account != null ? account + "@" : "") + Constants.INTENT_URI);
		data.appendEncodedPath("tags");
		i.setData(data.build());
		
		return i;
	}
	
	public static Intent ViewTabletTags(String account, Context context) {
		Intent i = new Intent(context, BrowseBookmarks.class);
		i.setAction(Intent.ACTION_VIEW);
		i.addCategory(Intent.CATEGORY_DEFAULT);
		Uri.Builder data = new Uri.Builder();
		data.scheme(Constants.CONTENT_SCHEME);
		data.encodedAuthority((account != null ? account + "@" : "") + Constants.INTENT_URI);
		data.appendEncodedPath("tags");
		i.setData(data.build());
		
		return i;
	}
	
	public static Intent SearchBookmarks(String query, String account, Context context) {
		Intent i = new Intent(context, BrowseBookmarks.class);
		i.setAction(Intent.ACTION_SEARCH);
		i.putExtra(SearchManager.QUERY, query);
		i.putExtra("MainSearchResults", "1");
		Uri.Builder data = new Uri.Builder();
		data.scheme(Constants.CONTENT_SCHEME);
		data.encodedAuthority((account != null ? account + "@" : "") + Constants.INTENT_URI);
		i.setData(data.build());
		return i;
	}
	
	public static Intent SearchTags(String query, String account, Context context) {
		Intent i = new Intent(context, BrowseTags.class);
		i.setAction(Intent.ACTION_SEARCH);
		i.putExtra(SearchManager.QUERY, query);
		i.putExtra("MainSearchResults", "1");
		Uri.Builder data = new Uri.Builder();
		data.scheme(Constants.CONTENT_SCHEME);
		data.encodedAuthority((account != null ? account + "@" : "") + Constants.INTENT_URI);
		i.setData(data.build());
		return i;
	}
	
	public static Intent SearchNotes(String query, String account, Context context) {
		Intent i = new Intent(context, BrowseNotes.class);
		i.setAction(Intent.ACTION_SEARCH);
		i.putExtra(SearchManager.QUERY, query);
		i.putExtra("MainSearchResults", "1");
		Uri.Builder data = new Uri.Builder();
		data.scheme(Constants.CONTENT_SCHEME);
		data.encodedAuthority((account != null ? account + "@" : "") + Constants.INTENT_URI);
		i.setData(data.build());
		return i;
	}
	
	public static Intent SearchGlobalTags(String query, String account, Context context) {
		Intent i = new Intent(context, BrowseBookmarks.class);
		i.setAction(Intent.ACTION_SEARCH);
		i.putExtra(SearchManager.QUERY, query);
		i.putExtra("MainSearchResults", "1");
		
		Uri.Builder data = new Uri.Builder();
		data.scheme(Constants.CONTENT_SCHEME);
		data.encodedAuthority((account != null ? account + "@" : "") + Constants.INTENT_URI);
		data.appendEncodedPath("bookmarks");
		data.appendQueryParameter("feed", "global");
		i.setData(data.build());
		
		return i;
	}
	
	public static Intent WidgetSearch(String account, Context context){
		Intent i = new Intent(context, Main.class);
		i.setAction(Intent.ACTION_SEARCH);
		Uri.Builder data = new Uri.Builder();
		data.scheme(Constants.CONTENT_SCHEME);
		data.encodedAuthority((account != null ? account + "@" : "") + Constants.INTENT_URI);
		data.appendEncodedPath("search");
		i.setData(data.build());
		
		return i;
	}
}
