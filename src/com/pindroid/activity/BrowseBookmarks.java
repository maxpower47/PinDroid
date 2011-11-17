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
import com.pindroid.action.MarkReadBookmarkTask;
import com.pindroid.fragment.BrowseBookmarkFeedFragment;
import com.pindroid.fragment.BrowseBookmarksFragment;
import com.pindroid.fragment.ViewBookmarkFragment;
import com.pindroid.fragment.ViewBookmarkFragment.OnBookmarkActionListener;
import com.pindroid.fragment.WebViewFragment;
import com.pindroid.fragment.WebViewFragment.OnBookmarkViewListener;
import com.pindroid.providers.BookmarkContent.Bookmark;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class BrowseBookmarks extends FragmentBaseActivity implements BrowseBookmarksFragment.OnBookmarkSelectedListener, 
	OnBookmarkActionListener, OnBookmarkViewListener {

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browse_bookmarks);
		
		Intent intent = getIntent();

		Uri data = intent.getData();
		String tagname = "";
		Boolean unread = false;

		if(data != null) {
			
			if(data.getUserInfo() != "") {
				username = data.getUserInfo();
			} else username = mAccount.name;

			tagname = data.getQueryParameter("tagname");
			unread = data.getQueryParameter("unread") != null;

	    	if(!data.getScheme().equals("content")) {
	    		startActivity(IntentHelper.OpenInBrowser(data.toString()));
	    		finish();
	    	}
		}		
		
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction t = fm.beginTransaction();
	
		if(isMyself()) {
			BrowseBookmarksFragment frag = new BrowseBookmarksFragment(username, tagname, unread);
			t.add(R.id.listcontent, frag);
		} else {
			BrowseBookmarkFeedFragment frag = new BrowseBookmarkFeedFragment(username, tagname);
			t.add(R.id.listcontent, frag);
		}
		
		t.commit();
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
		if(findViewById(R.id.maincontent) != null) {
			ViewBookmarkFragment viewFrag = new ViewBookmarkFragment();
			viewFrag.setBookmark(b);
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			transaction.replace(R.id.maincontent, viewFrag);
			transaction.commit();
		} else {
			viewBookmark(b);
		}
	}

	public void onBookmarkRead(Bookmark b) {
		if(findViewById(R.id.maincontent) != null) {
			WebViewFragment webFrag = new WebViewFragment();
			webFrag.setUrl(b, false);
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			transaction.replace(R.id.maincontent, webFrag);
			transaction.commit();
		} else {
			startActivity(IntentHelper.ReadBookmark(b.getUrl()));
		}	
	}

	public void onBookmarkOpen(Bookmark b) {
    	String url = b.getUrl();
    	
    	if(!url.startsWith("http")) {
    		url = "http://" + url;
    	}
    	
		if(findViewById(R.id.maincontent) != null) {
			WebViewFragment webFrag = new WebViewFragment();
			webFrag.setUrl(b, true);
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			transaction.replace(R.id.maincontent, webFrag);
			transaction.commit();
		} else {
			startActivity(IntentHelper.OpenInBrowser(url));
		}		
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
    		BookmarkTaskArgs unreadArgs = new BookmarkTaskArgs(b, mAccount, this);
    		new MarkReadBookmarkTask().execute(unreadArgs);
    	}
	}

	public void onBookmarkEdit(Bookmark b) {
		startActivity(IntentHelper.EditBookmark(b, mAccount.name, this));
		
	}

	public void onBookmarkDelete(Bookmark b) {
		BookmarkTaskArgs args = new BookmarkTaskArgs(b, mAccount, this);	
		new DeleteBookmarkTask().execute(args);
	}

	public void onTagSelected(String tag) {
		if(findViewById(R.id.maincontent) != null) {
			BrowseBookmarksFragment frag = new BrowseBookmarksFragment(username, tag, false);
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			transaction.replace(R.id.listcontent, frag);
			transaction.addToBackStack(null);
			transaction.commit();
		} else {
			startActivity(IntentHelper.ViewBookmarks(tag, username, this));
		}
	}

	public void onUserTagSelected(String tag, String user) {
		if(findViewById(R.id.maincontent) != null) {
			BrowseBookmarkFeedFragment frag = new BrowseBookmarkFeedFragment(user, tag);
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			transaction.replace(R.id.listcontent, frag);
			transaction.addToBackStack(null);
			transaction.commit();
		} else {
			startActivity(IntentHelper.ViewBookmarks(tag, user, this));
		}
	}

	public void onAccountSelected(String account) {
		if(findViewById(R.id.maincontent) != null) {
			BrowseBookmarkFeedFragment frag = new BrowseBookmarkFeedFragment(account, null);
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			transaction.replace(R.id.listcontent, frag);
			transaction.addToBackStack(null);
			transaction.commit();
		} else {
			startActivity(IntentHelper.ViewBookmarks(null, account, this));
		}
	}
}