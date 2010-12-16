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

import java.util.Date;

import com.deliciousdroid.Constants;
import com.deliciousdroid.R;
import com.deliciousdroid.action.BookmarkTaskArgs;
import com.deliciousdroid.action.DeleteBookmarkTask;
import com.deliciousdroid.platform.BookmarkManager;
import com.deliciousdroid.providers.BookmarkContent.Bookmark;
import com.deliciousdroid.providers.TagContent.Tag;
import com.deliciousdroid.providers.BookmarkContentProvider;
import com.deliciousdroid.providers.ContentNotFoundException;
import com.deliciousdroid.ui.TagSpan;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class ViewBookmark extends AppBaseActivity{

	private TextView mTitle;
	private TextView mUrl;
	private TextView mNotes;
	private TextView mTags;
	private TextView mTime;
	private TextView mUsername;
	private Bookmark bookmark;
	private Boolean myself;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.view_bookmark);
		setTitle("View Bookmark Details");
		
		mTitle = (TextView) findViewById(R.id.view_bookmark_title);
		mUrl = (TextView) findViewById(R.id.view_bookmark_url);
		mNotes = (TextView) findViewById(R.id.view_bookmark_notes);
		mTags = (TextView) findViewById(R.id.view_bookmark_tags);
		mTime = (TextView) findViewById(R.id.view_bookmark_time);
		mUsername = (TextView) findViewById(R.id.view_bookmark_account);
		
		mTags.setMovementMethod(LinkMovementMethod.getInstance());
		
		Log.d("browse bookmarks", getIntent().getDataString());
		Uri data = getIntent().getData();
		String path = data.getPath();
		Log.d("path", path);
		
		String username = data.getUserInfo();
		
		myself = mAccount.name.equals(username);
	
		if(path.contains("/bookmarks") && myself){
			
			try{		
				int id = Integer.parseInt(data.getLastPathSegment());

				bookmark = BookmarkManager.GetById(id, mContext);
				
				Date d = new Date(bookmark.getTime());
				
				mTitle.setText(bookmark.getDescription());
				mUrl.setText(bookmark.getUrl());
				mNotes.setText(bookmark.getNotes());
				mTime.setText(d.toString());
				mUsername.setText(bookmark.getAccount());
				
        		SpannableStringBuilder tagBuilder = new SpannableStringBuilder();

        		for(Tag t : bookmark.getTags()) {
        			addTag(tagBuilder, t);
        		}
        		
        		mTags.setText(tagBuilder);
			}
			catch(ContentNotFoundException e){}
		} else if(path.contains("/bookmarks") && !myself) {
			Date d = new Date(Long.parseLong(data.getQueryParameter("time")));
			
			mTitle.setText(data.getQueryParameter("title"));
			mUrl.setText(data.getQueryParameter("url"));
			mNotes.setText(data.getQueryParameter("notes"));
			mTags.setText(data.getQueryParameter("tags"));
			mTime.setText(d.toString());
			mUsername.setText(data.getQueryParameter("account"));
		}
	}
	
    TagSpan.OnTagClickListener tagOnClickListener = new TagSpan.OnTagClickListener() {
        public void onTagClick(String tag) {

    		Intent i = new Intent();
    		i.setAction(Intent.ACTION_VIEW);
    		i.addCategory(Intent.CATEGORY_DEFAULT);
    		Uri.Builder data = new Uri.Builder();
    		data.scheme(Constants.CONTENT_SCHEME);
    		data.encodedAuthority(mAccount.name + "@" + BookmarkContentProvider.AUTHORITY);
    		data.appendEncodedPath("bookmarks");
    		data.appendQueryParameter("tagname", tag);
    		i.setData(data.build());
    		
    		Log.d("uri", data.build().toString());
    		
    		startActivity(i);
        	
        }
    };
    
	private void addTag(SpannableStringBuilder builder, Tag t) {
		int flags = 0;
		
		if (builder.length() != 0) {
			builder.append("  ");
		}
		
		int start = builder.length();
		builder.append(t.getTagName());
		int end = builder.length();
		
		TagSpan span = new TagSpan(t.getTagName());
		span.setOnTagClickListener(tagOnClickListener);

		builder.setSpan(span, start, end, flags);
	}
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.view_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if(!myself) {
			menu.removeItem(R.id.menu_view_editbookmark);
			menu.removeItem(R.id.menu_view_deletebookmark);
		}
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
		    case R.id.menu_view_editbookmark:
				Intent editBookmark = new Intent(this, AddBookmark.class);
				editBookmark.setAction(Intent.ACTION_EDIT);
				
				Uri.Builder data = new Uri.Builder();
				data.scheme(Constants.CONTENT_SCHEME);
				data.encodedAuthority(mAccount + "@" + BookmarkContentProvider.AUTHORITY);
				data.appendEncodedPath("bookmarks");
				data.appendEncodedPath(Integer.toString(bookmark.getId()));
				editBookmark.setData(data.build());

				startActivity(editBookmark);
		    	return true;
		    case R.id.menu_view_deletebookmark:
				BookmarkTaskArgs args = new BookmarkTaskArgs(bookmark, mAccount, this);	
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