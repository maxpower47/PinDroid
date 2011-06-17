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
import com.pindroid.action.IntentHelper;
import com.pindroid.fragment.BrowseBookmarkFeedFragment;
import com.pindroid.fragment.BrowseBookmarksFragment;
import com.pindroid.providers.BookmarkContent.Bookmark;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;

public class BrowseBookmarks extends FragmentBaseActivity {
	
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
		
		if(!isMyself()) {
			FragmentManager fm = getSupportFragmentManager();
			BrowseBookmarkFeedFragment frag = new BrowseBookmarkFeedFragment();
			FragmentTransaction t = fm.beginTransaction();
			
			t.replace(R.id.listcontent, frag);
			t.commit();
		}
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
}