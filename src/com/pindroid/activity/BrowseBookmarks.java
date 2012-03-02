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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;

import com.pindroid.R;
import com.pindroid.action.IntentHelper;
import com.pindroid.fragment.BrowseBookmarkFeedFragment;
import com.pindroid.fragment.BrowseBookmarksFragment;
import com.pindroid.platform.BookmarkManager;
import com.pindroid.providers.BookmarkContent.Bookmark;

public class BrowseBookmarks extends FragmentBaseActivity implements BrowseBookmarksFragment.OnBookmarkSelectedListener {
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browse_bookmarks);
		
		Intent intent = getIntent();

		Uri data = intent.getData();
		String path = "";

		if(data != null) {
			path = data.getPath();
			
			if(data.getUserInfo() != "") {
				username = data.getUserInfo();
			} else username = mAccount.name;
		}
		
		if(path.contains("bookmarks") && TextUtils.isDigitsOnly(data.getLastPathSegment())) {
			viewBookmark(Integer.parseInt(data.getLastPathSegment()));
			finish();
		} 	
		
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction t = fm.beginTransaction();
	
		if(isMyself()) {
			BrowseBookmarksFragment frag = new BrowseBookmarksFragment();
			t.add(R.id.listcontent, frag);
		} else {
			BrowseBookmarkFeedFragment frag = new BrowseBookmarkFeedFragment();
			t.add(R.id.listcontent, frag);
		}
		t.commit();
    }
	
	private void viewBookmark(int id) {
		Bookmark b = new Bookmark(id);
		viewBookmark(b);
	}
	
	private void viewBookmark(Bookmark b) {
		startActivity(IntentHelper.ViewBookmark(b, username, this));
	}
	
	@Override
	public boolean onSearchRequested() {

		if(isMyself()) {
			BrowseBookmarksFragment frag = (BrowseBookmarksFragment) getSupportFragmentManager().findFragmentById(R.id.listcontent);

			return frag.onSearchRequested();
		} else {
			BrowseBookmarkFeedFragment frag = (BrowseBookmarkFeedFragment) getSupportFragmentManager().findFragmentById(R.id.listcontent);

			return frag.onSearchRequested();
		}
	}

	public void onBookmarkView(Bookmark b) {
		viewBookmark(b);
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
		startActivity(IntentHelper.AddBookmark(b.getUrl(), mAccount.name, this));
	}

	public void onBookmarkShare(Bookmark b) {
		Intent sendIntent = IntentHelper.SendBookmark(b.getUrl(), b.getDescription());
    	startActivity(Intent.createChooser(sendIntent, getString(R.string.share_chooser_title)));	
	}

	public void onBookmarkMark(Bookmark b) {
    	if(isMyself() && b.getToRead()) {
    		b.setToRead(false);
			BookmarkManager.UpdateBookmark(b, mAccount.name, this);
    	}
	}

	public void onBookmarkEdit(Bookmark b) {
		startActivity(IntentHelper.EditBookmark(b, mAccount.name, this));
		
	}

	public void onBookmarkDelete(Bookmark b) {
		BookmarkManager.LazyDelete(b, mAccount.name, this);
	}
}