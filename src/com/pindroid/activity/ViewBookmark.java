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
import com.pindroid.fragment.ViewBookmarkFragment.OnBookmarkActionListener;
import com.pindroid.providers.BookmarkContent.Bookmark;

import android.content.Intent;
import android.os.Bundle;

public class ViewBookmark extends FragmentBaseActivity implements OnBookmarkActionListener,
	OnBookmarkSelectedListener {

	@Override
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_bookmark);
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