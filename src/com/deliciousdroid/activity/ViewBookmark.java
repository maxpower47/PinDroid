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

import com.deliciousdroid.Constants;
import com.deliciousdroid.R;
import com.deliciousdroid.providers.BookmarkContent.Bookmark;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class ViewBookmark extends Activity implements View.OnClickListener{

	private TextView mTitle;
	private TextView mUrl;
	private TextView mNotes;
	private TextView mTags;
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
		
		context = this;
		mAccountManager = AccountManager.get(this);
		Account[] al = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
		account = al[0];
		
		Log.d("browse bookmarks", getIntent().getDataString());
		Uri data = getIntent().getData();
		String scheme = data.getScheme();
		String path = data.getPath();
		Log.d("path", path);
		
		String username = data.getQueryParameter("username");
		
		myself = account.name.equals(username);
		

	
		if(scheme.equals("content") && path.contains("/bookmarks") && myself){
			
			try{		
				int id = Integer.parseInt(data.getLastPathSegment());
				
				String[] projection = new String[] {Bookmark._ID, Bookmark.Url, Bookmark.Description, Bookmark.Notes, Bookmark.Meta, Bookmark.Tags};
				String selection = BaseColumns._ID + "=" + id;
				
				Uri bookmarks = Bookmark.CONTENT_URI;
				
				Cursor c = managedQuery(bookmarks, projection, selection, null, null);				
				
				if(c.moveToFirst()){
					int idColumn = c.getColumnIndex(Bookmark._ID);
					int urlColumn = c.getColumnIndex(Bookmark.Url);
					int descriptionColumn = c.getColumnIndex(Bookmark.Description);
					int notesColumn = c.getColumnIndex(Bookmark.Notes);
					int tagsColumn = c.getColumnIndex(Bookmark.Tags);
					int metaColumn = c.getColumnIndex(Bookmark.Meta);
					
					mTitle.setText(c.getString(descriptionColumn));
					mUrl.setText(c.getString(urlColumn));
					mNotes.setText(c.getString(notesColumn));
					mTags.setText(c.getString(tagsColumn));
				}	
			}
			catch(Exception e){}
		} 	
	}

    /**
     * {@inheritDoc}
     */
    public void onClick(View v) {

    }
}