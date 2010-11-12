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

package com.deliciousdroid.activity;

import java.text.ParseException;
import java.util.Date;

import com.deliciousdroid.Constants;
import com.deliciousdroid.R;
import com.deliciousdroid.action.BookmarkTaskArgs;
import com.deliciousdroid.action.DeleteBookmarkTask;
import com.deliciousdroid.platform.BookmarkManager;
import com.deliciousdroid.providers.BookmarkContent.Bookmark;
import com.deliciousdroid.providers.ContentNotFoundException;
import com.deliciousdroid.util.DateParser;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ViewBookmark extends Activity{

	private TextView mTitle;
	private TextView mUrl;
	private TextView mNotes;
	private TextView mTags;
	private TextView mTime;
	private AccountManager mAccountManager;
	private Account account;
	private Bookmark bookmark;
	private Context context;
	private Boolean myself;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.view_bookmark);
		
		mTitle = (TextView) findViewById(R.id.view_bookmark_title);
		mUrl = (TextView) findViewById(R.id.view_bookmark_url);
		mNotes = (TextView) findViewById(R.id.view_bookmark_notes);
		mTags = (TextView) findViewById(R.id.view_bookmark_tags);
		mTime = (TextView) findViewById(R.id.view_bookmark_time);
		
		context = this;
		mAccountManager = AccountManager.get(this);
		Account[] al = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
		account = al[0];
		
		Log.d("browse bookmarks", getIntent().getDataString());
		Uri data = getIntent().getData();
		String scheme = data.getScheme();
		String path = data.getPath();
		Log.d("path", path);
		
		String username = data.getUserInfo();
		
		myself = account.name.equals(username);
	
		if(scheme.equals("content") && path.contains("/bookmarks") && myself){
			
			try{		
				int id = Integer.parseInt(data.getLastPathSegment());

				bookmark = BookmarkManager.GetById(id, context);
				
				Date d = new Date(bookmark.getTime());
				
				mTitle.setText(bookmark.getDescription());
				mUrl.setText(bookmark.getUrl());
				mNotes.setText(bookmark.getNotes());
				mTags.setText(bookmark.getTags());
				mTime.setText(d.toString());

			}
			catch(ContentNotFoundException e){}
		} 	
	}
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.view_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.menu_view_openbookmark:
	    	String url = (String) mUrl.getText();
	    	Uri link = Uri.parse(url);
			Intent i = new Intent(Intent.ACTION_VIEW, link);
			startActivity(i);
			return true;
	    case R.id.menu_view_deletebookmark:
			BookmarkTaskArgs args = new BookmarkTaskArgs(bookmark, account, this);	
			new DeleteBookmarkTask().execute(args);
			return true;	
	    case R.id.menu_view_settings:
			Intent prefs = new Intent(this, Preferences.class);
			startActivity(prefs);
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
}