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

package com.pindroid.activity;

import java.util.Date;

import com.pindroid.R;
import com.pindroid.Constants;
import com.pindroid.action.BookmarkTaskArgs;
import com.pindroid.action.DeleteBookmarkTask;
import com.pindroid.action.IntentHelper;
import com.pindroid.action.MarkReadBookmarkTask;
import com.pindroid.platform.BookmarkManager;
import com.pindroid.providers.BookmarkContentProvider;
import com.pindroid.providers.ContentNotFoundException;
import com.pindroid.providers.BookmarkContent.Bookmark;
import com.pindroid.providers.TagContent.Tag;
import com.pindroid.ui.AccountSpan;
import com.pindroid.ui.TagSpan;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewBookmark extends AppBaseActivity{

	private TextView mTitle;
	private TextView mUrl;
	private TextView mNotes;
	private TextView mTags;
	private TextView mTime;
	private TextView mUsername;
	private ImageView mIcon;
	private Bookmark bookmark;
	
	private String user;
	private String path;
	private Uri data;	

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.view_bookmark);
		
		((ImageButton) findViewById(R.id.action_bar_search)).setOnClickListener(searchHandler);
		
		mTitle = (TextView) findViewById(R.id.view_bookmark_title);
		mUrl = (TextView) findViewById(R.id.view_bookmark_url);
		mNotes = (TextView) findViewById(R.id.view_bookmark_notes);
		mTags = (TextView) findViewById(R.id.view_bookmark_tags);
		mTime = (TextView) findViewById(R.id.view_bookmark_time);
		mUsername = (TextView) findViewById(R.id.view_bookmark_account);
		mIcon = (ImageView) findViewById(R.id.view_bookmark_icon);
		
		setTitle(R.string.view_bookmark_title);

		if(Intent.ACTION_SEARCH.equals(getIntent().getAction())){
			if(getIntent().hasExtra(SearchManager.QUERY)){
				Intent i = new Intent(mContext, MainSearchResults.class);
				i.putExtras(getIntent().getExtras());
				startActivity(i);
				finish();
			} else {
				onSearchRequested();
			}
		}
		
		if(getIntent().getData() != null) {
			data = getIntent().getData();
			path = data.getPath();
			
			username = data.getUserInfo();
			user = data.getQueryParameter("account");
		}
	}
	
	@Override
	public void onResume(){
		super.onResume();
		
		if(path.contains("/bookmarks") && isMyself()){
			
			try{		
				int id = Integer.parseInt(data.getLastPathSegment());

				bookmark = BookmarkManager.GetById(id, mContext);
				
				Date d = new Date(bookmark.getTime());
				
				mTitle.setText(bookmark.getDescription());
				mUrl.setText(bookmark.getUrl());
				mNotes.setText(bookmark.getNotes());
				mTime.setText(d.toString());
				mUsername.setText(bookmark.getAccount());
				
				if(!bookmark.getShared()) {
					mIcon.setImageResource(R.drawable.padlock);
				} else if(bookmark.getToRead()) {
					mIcon.setImageResource(R.drawable.book_open);
				}
				
        		SpannableStringBuilder tagBuilder = new SpannableStringBuilder();

        		for(Tag t : bookmark.getTags()) {
        			addTag(tagBuilder, t, tagOnClickListener);
        		}
        		
        		mTags.setText(tagBuilder);
        		mTags.setMovementMethod(LinkMovementMethod.getInstance());
			}
			catch(ContentNotFoundException e){}
		} else if(path.contains("/bookmarks") && !isMyself()) {

			bookmark = new Bookmark();
			bookmark.setDescription(data.getQueryParameter("title"));
			bookmark.setUrl(data.getQueryParameter("url"));
			bookmark.setNotes(data.getQueryParameter("notes"));
			bookmark.setTime(Long.parseLong(data.getQueryParameter("time")));
			if(!data.getQueryParameter("tags").equals("null"))
				bookmark.setTagString(data.getQueryParameter("tags"));
			bookmark.setAccount(data.getQueryParameter("account"));
			
			Date d = new Date(bookmark.getTime());
			
			if(!bookmark.getDescription().equals("null"))
				mTitle.setText(bookmark.getDescription());
			
			mUrl.setText(bookmark.getUrl());
			
			if(!bookmark.getNotes().equals("null"))
					mNotes.setText(bookmark.getNotes());
			
			mTime.setText(d.toString());
			
    		SpannableStringBuilder tagBuilder = new SpannableStringBuilder();

    		for(Tag t : bookmark.getTags()) {
    			addTag(tagBuilder, t, userTagOnClickListener);
    		}
    		
    		mTags.setText(tagBuilder);
    		mTags.setMovementMethod(LinkMovementMethod.getInstance());

			SpannableStringBuilder builder = new SpannableStringBuilder();
			int start = builder.length();
			builder.append(bookmark.getAccount());
			int end = builder.length();
			
			AccountSpan span = new AccountSpan(bookmark.getAccount());
			span.setOnAccountClickListener(accountOnClickListener);

			builder.setSpan(span, start, end, 0);
			
			mUsername.setText(builder);
			mUsername.setMovementMethod(LinkMovementMethod.getInstance());
		}
	}
	
    TagSpan.OnTagClickListener tagOnClickListener = new TagSpan.OnTagClickListener() {
        public void onTagClick(String tag) {
    		Intent i = new Intent();
    		i.setAction(Intent.ACTION_VIEW);
    		i.addCategory(Intent.CATEGORY_DEFAULT);
    		Uri.Builder data = new Uri.Builder();
    		data.scheme(Constants.CONTENT_SCHEME);
    		data.encodedAuthority(username + "@" + BookmarkContentProvider.AUTHORITY);
    		data.appendEncodedPath("bookmarks");
    		data.appendQueryParameter("tagname", tag);
    		i.setData(data.build());
    		
    		Log.d("uri", data.build().toString());
    		
    		startActivity(i);	
        }
    };
    
    TagSpan.OnTagClickListener userTagOnClickListener = new TagSpan.OnTagClickListener() {
        public void onTagClick(String tag) {
    		Intent i = new Intent();
    		i.setAction(Intent.ACTION_VIEW);
    		i.addCategory(Intent.CATEGORY_DEFAULT);
    		Uri.Builder data = new Uri.Builder();
    		data.scheme(Constants.CONTENT_SCHEME);
    		data.encodedAuthority(user + "@" + BookmarkContentProvider.AUTHORITY);
    		data.appendEncodedPath("bookmarks");
    		data.appendQueryParameter("tagname", tag);
    		i.setData(data.build());
    		
    		Log.d("uri", data.build().toString());
    		
    		startActivity(i);	
        }
    };
    
    AccountSpan.OnAccountClickListener accountOnClickListener = new AccountSpan.OnAccountClickListener() {
        public void onAccountClick(String account) {
    		Intent i = new Intent();
    		i.setAction(Intent.ACTION_VIEW);
    		i.addCategory(Intent.CATEGORY_DEFAULT);
    		Uri.Builder data = new Uri.Builder();
    		data.scheme(Constants.CONTENT_SCHEME);
    		data.encodedAuthority(account + "@" + BookmarkContentProvider.AUTHORITY);
    		data.appendEncodedPath("bookmarks");
    		i.setData(data.build());
    		
    		Log.d("uri", data.build().toString());
    		
    		startActivity(i);
        }
    };
    
	private void addTag(SpannableStringBuilder builder, Tag t, TagSpan.OnTagClickListener listener) {
		int flags = 0;
		
		if (builder.length() != 0) {
			builder.append("  ");
		}
		
		int start = builder.length();
		builder.append(t.getTagName());
		int end = builder.length();
		
		TagSpan span = new TagSpan(t.getTagName());
		span.setOnTagClickListener(listener);

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
		if(!isMyself()) {
			menu.removeItem(R.id.menu_view_editbookmark);
			menu.removeItem(R.id.menu_view_deletebookmark);
		}
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
		    case R.id.menu_view_read:
		    	if(isMyself() && bookmark.getToRead() && markAsRead) {
		    		BookmarkTaskArgs args = new BookmarkTaskArgs(bookmark, mAccount, this);
		    		new MarkReadBookmarkTask().execute(args);
		    	}

				startActivity(IntentHelper.ReadBookmark(((Spannable) mUrl.getText()).toString()));
				return true;
		    case R.id.menu_view_openbookmark:
		    	String url = ((Spannable) mUrl.getText()).toString();
				startActivity(IntentHelper.OpenInBrowser(url));
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
				BookmarkTaskArgs deleteargs = new BookmarkTaskArgs(bookmark, mAccount, this);	
				new DeleteBookmarkTask().execute(deleteargs);
				return true;
		    case R.id.menu_view_sendbookmark:
		    	String sendUrl = ((Spannable) mUrl.getText()).toString();
		    	String sendTitle = mTitle.getText().toString();
		    	Intent sendIntent = IntentHelper.SendBookmark(sendUrl, sendTitle);
		    	startActivity(Intent.createChooser(sendIntent, res.getString(R.string.share_chooser_title)));
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