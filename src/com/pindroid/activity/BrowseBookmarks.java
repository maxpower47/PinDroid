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

import com.pindroid.Constants.BookmarkViewType;
import com.pindroid.R;
import com.pindroid.action.BookmarkTaskArgs;
import com.pindroid.action.DeleteBookmarkTask;
import com.pindroid.action.IntentHelper;
import com.pindroid.action.MarkReadBookmarkTask;
import com.pindroid.fragment.AddBookmarkFragment;
import com.pindroid.fragment.AddBookmarkFragment.OnBookmarkSaveListener;
import com.pindroid.fragment.BrowseBookmarkFeedFragment;
import com.pindroid.fragment.BrowseBookmarksFragment;
import com.pindroid.fragment.BrowseBookmarksFragment.OnBookmarkSelectedListener;
import com.pindroid.fragment.BrowseTagsFragment;
import com.pindroid.fragment.BrowseTagsFragment.OnTagSelectedListener;
import com.pindroid.fragment.ViewBookmarkFragment;
import com.pindroid.fragment.ViewBookmarkFragment.OnBookmarkActionListener;
import com.pindroid.providers.BookmarkContent.Bookmark;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class BrowseBookmarks extends FragmentBaseActivity implements OnBookmarkSelectedListener, 
	OnBookmarkActionListener, OnBookmarkSaveListener, OnTagSelectedListener {

	private String tagname = "";
	private Boolean unread = false;
	private String path = "";
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browse_bookmarks);
		
		Intent intent = getIntent();

		Uri data = intent.getData();

		if(data != null) {
			
			if(data.getUserInfo() != "") {
				username = data.getUserInfo();
			} else username = mAccount.name;

			tagname = data.getQueryParameter("tagname");
			unread = data.getQueryParameter("unread") != null;
			path = data.getPath();
			
	    	if(!data.getScheme().equals("content")) {
	    		startActivity(IntentHelper.OpenInBrowser(data.toString()));
	    		finish();
	    	}
		}		
		
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction t = fm.beginTransaction();
		
		Fragment bookmarkFrag = new Fragment();
		
		BrowseTagsFragment tagFrag = (BrowseTagsFragment) fm.findFragmentById(R.id.tagcontent);
		tagFrag.setAccount(username);
		tagFrag.setAction("notpick");

		
		if(isMyself()) {
			bookmarkFrag = new BrowseBookmarksFragment();
			((BrowseBookmarksFragment) bookmarkFrag).setQuery(username, tagname, unread);
		} else {
			bookmarkFrag = new BrowseBookmarkFeedFragment();
			((BrowseBookmarkFeedFragment) bookmarkFrag).setQuery(username, tagname);
		}
	
		if(path.contains("bookmarks")){
			t.add(R.id.listcontent, bookmarkFrag);
			t.hide(tagFrag);
		} else if(path.contains("tags")){
			t.hide(fm.findFragmentById(R.id.maincontent));
			t.add(R.id.listcontent, bookmarkFrag);
		}
		
		t.commit();
    }
	
	@Override
	public boolean onSearchRequested() {
		if(isMyself()) {
			Bundle contextData = new Bundle();
			contextData.putString("tagname", tagname);
			contextData.putString("username", username);
			contextData.putBoolean("unread", unread);
			startSearch(null, false, contextData, false);
		} else {
			startSearch(null, false, Bundle.EMPTY, false);
		}
		return true;
	}

	public void onBookmarkView(Bookmark b) {	
		if(findViewById(R.id.maincontent) != null || findViewById(R.id.tagcontent) != null) {
			setBookmarkView(b, BookmarkViewType.VIEW);
		} else {
			startActivity(IntentHelper.ViewBookmark(b, username, this));
		}
	}

	public void onBookmarkRead(Bookmark b) {
		if(findViewById(R.id.maincontent) != null) {
			setBookmarkView(b, BookmarkViewType.READ);
		} else {
			startActivity(IntentHelper.ReadBookmark(b.getUrl()));
		}	
	}

	public void onBookmarkOpen(Bookmark b) {
		if(findViewById(R.id.maincontent) != null) {
			setBookmarkView(b, BookmarkViewType.WEB);
		} else {
			startActivity(IntentHelper.OpenInBrowser(b.getUrl()));
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
		if(findViewById(R.id.maincontent) != null) {
			AddBookmarkFragment addFrag = new AddBookmarkFragment();
			addFrag.loadBookmark(b, null);
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			transaction.replace(R.id.maincontent, addFrag);
			transaction.commit();
		} else {
			startActivity(IntentHelper.EditBookmark(b, mAccount.name, this));
		}		
	}

	public void onBookmarkDelete(Bookmark b) {
		BookmarkTaskArgs args = new BookmarkTaskArgs(b, mAccount, this);	
		new DeleteBookmarkTask().execute(args);
		
		if(findViewById(R.id.maincontent) != null) {
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			transaction.remove(getSupportFragmentManager().findFragmentById(R.id.maincontent));
			transaction.commit();
		}
	}

	public void onViewTagSelected(String tag) {
		if(findViewById(R.id.maincontent) != null) {
			BrowseBookmarksFragment frag = new BrowseBookmarksFragment();
			frag.setQuery(username, tag, false);
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
			BrowseBookmarkFeedFragment frag = new BrowseBookmarkFeedFragment();
			frag.setQuery(user, tag);
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
			BrowseBookmarkFeedFragment frag = new BrowseBookmarkFeedFragment();
			frag.setQuery(account, null);
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			transaction.replace(R.id.listcontent, frag);
			transaction.addToBackStack(null);
			transaction.commit();
		} else {
			startActivity(IntentHelper.ViewBookmarks(null, account, this));
		}
	}

	public void onBookmarkSave(Bookmark b) {
		onBookmarkView(b);
	}

	public void onBookmarkCancel(Bookmark b) {
		onBookmarkView(b);
	}

	public void onTagSelected(String tag) {
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		BrowseBookmarksFragment frag = new BrowseBookmarksFragment();
		frag.setQuery(username, tag, false);
		transaction.replace(R.id.listcontent, frag);
		transaction.commit();
	}
	
	private void setBookmarkView(Bookmark b, BookmarkViewType viewType){
		if(getSupportFragmentManager().findFragmentById(R.id.maincontent).isHidden()){
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			transaction.hide(getSupportFragmentManager().findFragmentById(R.id.tagcontent));
			transaction.show(getSupportFragmentManager().findFragmentById(R.id.maincontent));
			transaction.addToBackStack(null);
			transaction.commit();
		}
		
		ViewBookmarkFragment viewFrag = (ViewBookmarkFragment) getSupportFragmentManager().findFragmentById(R.id.maincontent);
		viewFrag.setBookmark(b, viewType);
		viewFrag.loadBookmark();
	}
}