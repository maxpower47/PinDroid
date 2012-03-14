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

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.pindroid.Constants;
import com.pindroid.Constants.BookmarkViewType;
import com.pindroid.R;
import com.pindroid.action.IntentHelper;
import com.pindroid.fragment.AddBookmarkFragment;
import com.pindroid.fragment.AddBookmarkFragment.OnBookmarkSaveListener;
import com.pindroid.fragment.BrowseBookmarkFeedFragment;
import com.pindroid.fragment.BrowseBookmarksFragment;
import com.pindroid.fragment.BrowseBookmarksFragment.OnBookmarkSelectedListener;
import com.pindroid.fragment.BrowseTagsFragment;
import com.pindroid.fragment.BrowseTagsFragment.OnTagSelectedListener;
import com.pindroid.fragment.ViewBookmarkFragment;
import com.pindroid.fragment.ViewBookmarkFragment.OnBookmarkActionListener;
import com.pindroid.platform.BookmarkManager;
import com.pindroid.providers.BookmarkContent.Bookmark;

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
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction t = fm.beginTransaction();
		
		if(fm.findFragmentById(R.id.listcontent) == null){
			Fragment bookmarkFrag = new Fragment();
	
			
			if(Intent.ACTION_SEARCH.equals(intent.getAction())) {
	    		Bundle searchData = intent.getBundleExtra(SearchManager.APP_DATA);
	    		
	    		if(searchData != null) {
	    			tagname = searchData.getString("tagname");
	    			username = searchData.getString("username");
	    			unread = searchData.getBoolean("unread");
	    		}
	    		
	    		String query = intent.getStringExtra(SearchManager.QUERY);
	    		
	    		if(intent.hasExtra("username")) {
	    			username = intent.getStringExtra("username");
	    		}
	    		
				bookmarkFrag = new BrowseBookmarksFragment();
				((BrowseBookmarksFragment) bookmarkFrag).setSearchQuery(query, username, tagname, unread);
			} else if(!Constants.ACTION_SEARCH_SUGGESTION.equals(intent.getAction())) {
				if(data != null) {
					
					if(data.getUserInfo() != "") {
						username = data.getUserInfo();
					} else username = mAccount.name;
		
					tagname = data.getQueryParameter("tagname");
					unread = data.getQueryParameter("unread") != null;
					path = data.getPath();
				}
				
				if(isMyself()) {
					bookmarkFrag = new BrowseBookmarksFragment();
					((BrowseBookmarksFragment) bookmarkFrag).setQuery(username, tagname, unread);
				} else {
					bookmarkFrag = new BrowseBookmarkFeedFragment();
					((BrowseBookmarkFeedFragment) bookmarkFrag).setQuery(username, tagname);
				}
			}

			t.add(R.id.listcontent, bookmarkFrag);
		}
		
		BrowseTagsFragment tagFrag = (BrowseTagsFragment) fm.findFragmentById(R.id.tagcontent);
		if(tagFrag != null){
			tagFrag.setAccount(username);
			tagFrag.setAction("notpick");
		}
		
		if(path.contains("tags")){
			t.hide(fm.findFragmentById(R.id.maincontent));
			
		} else{
			if(tagFrag != null){
				t.hide(tagFrag);
			}
		}
		
		Fragment addFrag = fm.findFragmentById(R.id.addcontent);
		if(addFrag != null){
			t.hide(addFrag);
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
		if(b != null){
			if(findViewById(R.id.maincontent) != null || findViewById(R.id.tagcontent) != null) {
				setBookmarkView(b, BookmarkViewType.VIEW);
			} else {
				startActivity(IntentHelper.ViewBookmark(b, BookmarkViewType.VIEW, username, this));
			}
		}
	}

	public void onBookmarkRead(Bookmark b) {
		if(b != null){
			if(findViewById(R.id.maincontent) != null) {
				setBookmarkView(b, BookmarkViewType.READ);
			} else {
				startActivity(IntentHelper.ViewBookmark(b, BookmarkViewType.READ, username, this));
			}
		}
	}

	public void onBookmarkOpen(Bookmark b) {
		if(b != null){
			if(findViewById(R.id.maincontent) != null) {
				setBookmarkView(b, BookmarkViewType.WEB);
			} else {
				startActivity(IntentHelper.OpenInBrowser(b.getUrl()));
			}
		}
	}

	public void onBookmarkAdd(Bookmark b) {
		if(b != null){
			startActivity(IntentHelper.AddBookmark(b.getUrl(), mAccount.name, this));
		}
	}

	public void onBookmarkShare(Bookmark b) {
		if(b != null){
			Intent sendIntent = IntentHelper.SendBookmark(b.getUrl(), b.getDescription());
			startActivity(Intent.createChooser(sendIntent, getString(R.string.share_chooser_title)));
		}
	}

	public void onBookmarkMark(Bookmark b) {
    	if(b != null && isMyself() && b.getToRead()) {
    		b.setToRead(false);
			BookmarkManager.UpdateBookmark(b, mAccount.name, this);
    	}
	}

	public void onBookmarkEdit(Bookmark b) {
		if(b != null){
			if(findViewById(R.id.maincontent) != null) {
				AddBookmarkFragment addFrag = (AddBookmarkFragment) getSupportFragmentManager().findFragmentById(R.id.addcontent);
				addFrag.loadBookmark(b, null);
				addFrag.refreshView();
				FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
				transaction.hide(getSupportFragmentManager().findFragmentById(R.id.maincontent));
				transaction.show(getSupportFragmentManager().findFragmentById(R.id.addcontent));
				transaction.commit();
			} else {
				startActivity(IntentHelper.EditBookmark(b, mAccount.name, this));
			}
		}
	}

	public void onBookmarkDelete(Bookmark b) {
		BookmarkManager.LazyDelete(b, mAccount.name, this);
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
		if(getSupportFragmentManager().findFragmentById(R.id.maincontent).isHidden()){
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			transaction.hide(getSupportFragmentManager().findFragmentById(R.id.addcontent));
			transaction.show(getSupportFragmentManager().findFragmentById(R.id.maincontent));
			transaction.commit();
		}
		
		onBookmarkView(b);
	}

	public void onBookmarkCancel(Bookmark b) {
		if(getSupportFragmentManager().findFragmentById(R.id.maincontent).isHidden()){
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			transaction.hide(getSupportFragmentManager().findFragmentById(R.id.addcontent));
			transaction.show(getSupportFragmentManager().findFragmentById(R.id.maincontent));
			transaction.commit();
		}
		
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
		if(getSupportFragmentManager().findFragmentById(R.id.maincontent).isHidden() && getSupportFragmentManager().findFragmentById(R.id.addcontent).isHidden()){
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			if(getSupportFragmentManager().findFragmentById(R.id.tagcontent).isVisible()){
				transaction.hide(getSupportFragmentManager().findFragmentById(R.id.tagcontent));
			}
			transaction.show(getSupportFragmentManager().findFragmentById(R.id.maincontent));
			transaction.addToBackStack(null);
			transaction.commit();
		} else if(getSupportFragmentManager().findFragmentById(R.id.maincontent).isHidden() && getSupportFragmentManager().findFragmentById(R.id.addcontent).isVisible()){
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			transaction.hide(getSupportFragmentManager().findFragmentById(R.id.addcontent));
			transaction.show(getSupportFragmentManager().findFragmentById(R.id.maincontent));
			transaction.commit();
		}
		
		ViewBookmarkFragment viewFrag = (ViewBookmarkFragment) getSupportFragmentManager().findFragmentById(R.id.maincontent);
		viewFrag.setBookmark(b, viewType);
		viewFrag.loadBookmark();
	}
}