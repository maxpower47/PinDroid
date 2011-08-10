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

import com.pindroid.R;
import com.pindroid.action.BookmarkTaskArgs;
import com.pindroid.action.DeleteBookmarkTask;
import com.pindroid.action.IntentHelper;
import com.pindroid.fragment.BrowseBookmarksFragment.OnBookmarkSelectedListener;
import com.pindroid.fragment.ViewBookmarkFragment;
import com.pindroid.fragment.ViewBookmarkFragment.OnBookmarkActionListener;
import com.pindroid.providers.BookmarkContent.Bookmark;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class ViewBookmark extends FragmentBaseActivity implements OnBookmarkActionListener,
	OnBookmarkSelectedListener {
	
	private String path;

	@Override
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_bookmark);
        
        setTitle(R.string.view_bookmark_title);
        
        Intent intent = getIntent();
        
        
        if(Intent.ACTION_SEARCH.equals(intent.getAction())){
			if(intent.hasExtra(SearchManager.QUERY)){
				Intent i = new Intent(this, MainSearchResults.class);
				i.putExtras(intent.getExtras());
				startActivity(i);
				finish();
			} else {
				onSearchRequested();
			}
		} else if(Intent.ACTION_VIEW.equals(intent.getAction())) {
					
			Uri data = intent.getData();
			String tagname = null;
			
			if(data != null) {
				path = data.getPath();
				tagname = data.getQueryParameter("tagname");
			}
			
			if(data.getScheme() == null || !data.getScheme().equals("content")){
				Intent i = new Intent(Intent.ACTION_VIEW, data);
				
				startActivity(i);
				finish();				
			} else if(tagname != null) {
				Intent viewTags = new Intent(this, BrowseBookmarks.class);
				viewTags.setData(data);
				
				Log.d("View Tags Uri", data.toString());
				startActivity(viewTags);
				finish();
			}
			
			if(intent.getData() != null) {
				data = intent.getData();
				path = data.getPath();
				username = data.getUserInfo();
			}
			
			Bookmark bookmark = new Bookmark();
			
			if(path.contains("/bookmarks") && isMyself()){		
				int id = Integer.parseInt(data.getLastPathSegment());
				bookmark.setId(id);
			} else if(path.contains("/bookmarks") && !isMyself()) {
				bookmark.setDescription(data.getQueryParameter("title"));
				bookmark.setUrl(data.getQueryParameter("url"));
				bookmark.setNotes(data.getQueryParameter("notes"));
				bookmark.setTime(Long.parseLong(data.getQueryParameter("time")));
				if(!data.getQueryParameter("tags").equals("null"))
					bookmark.setTagString(data.getQueryParameter("tags"));
				bookmark.setAccount(data.getQueryParameter("account"));
			}
			
			ViewBookmarkFragment frag = (ViewBookmarkFragment) getSupportFragmentManager().findFragmentById(R.id.view_bookmark_fragment);
	        frag.setBookmark(bookmark);
		}
	}
	
	public void onTagSelected(String tag) {		
		startActivity(IntentHelper.ViewBookmarks(tag, username, this));
	}

	public void onUserTagSelected(String tag, String user) {
		startActivity(IntentHelper.ViewBookmarks(tag, user, this));
		
	}

	public void onAccountSelected(String account) {
		startActivity(IntentHelper.ViewBookmarks(null, account, this));
		
	}

	public void onBookmarkView(Bookmark b) {
	}

	public void onBookmarkRead(Bookmark b) {
		startActivity(IntentHelper.ReadBookmark(b.getUrl()));
		
	}

	public void onBookmarkOpen(Bookmark b) {
    	String url = b.getUrl();
    	
    	if(!url.startsWith("http")) {
    		url = "http://" + url;
    	}
		
		startActivity(IntentHelper.OpenInBrowser(url));
	}

	public void onBookmarkAdd(Bookmark b) {
	}

	public void onBookmarkShare(Bookmark b) {
		Intent sendIntent = IntentHelper.SendBookmark(b.getUrl(), b.getDescription());
    	startActivity(Intent.createChooser(sendIntent, getString(R.string.share_chooser_title)));	
	}

	public void onBookmarkMark(Bookmark b) {
	}

	public void onBookmarkEdit(Bookmark b) {
		startActivity(IntentHelper.EditBookmark(b, mAccount.name, this));	
	}

	public void onBookmarkDelete(Bookmark b) {
		BookmarkTaskArgs args = new BookmarkTaskArgs(b, mAccount, this);	
		new DeleteBookmarkTask().execute(args);
	}
}