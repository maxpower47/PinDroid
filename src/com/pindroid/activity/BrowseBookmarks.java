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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

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
import com.pindroid.util.SettingsHelper;
import com.pindroid.fragment.BookmarkBrowser;

public class BrowseBookmarks extends FragmentBaseActivity implements OnBookmarkSelectedListener, 
	OnBookmarkActionListener, OnBookmarkSaveListener, OnTagSelectedListener {

	private String query = "";
	private String tagname = "";
	private Boolean unread = false;
	private String path = "";
	private String feed = "";
	private Bookmark lastSelected = null;
	private BookmarkViewType lastViewType = null;
	
	static final String STATE_LASTBOOKMARK = "lastBookmark";
	static final String STATE_LASTVIEWTYPE = "lastViewType";
	static final String STATE_TAGNAME = "tagname";
	static final String STATE_UNREAD = "unread";
	static final String STATE_QUERY = "query";
	static final String STATE_PATH = "path";
	static final String STATE_FEED = "feed";
	
	private Fragment bookmarkFrag;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browse_bookmarks);
		
		Intent intent = getIntent();

		Uri data = intent.getData();
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction t = fm.beginTransaction();
		
		if(fm.findFragmentById(R.id.listcontent) == null){
			if(Intent.ACTION_SEARCH.equals(intent.getAction())) {
	    		Bundle searchData = intent.getBundleExtra(SearchManager.APP_DATA);
	    		
	    		if(searchData != null) {
	    			tagname = searchData.getString("tagname");
	    			app.setUsername(searchData.getString("username"));
	    			unread = searchData.getBoolean("unread");
	    		}
	    		
	    		query = intent.getStringExtra(SearchManager.QUERY);
	    		
	    		if(intent.hasExtra("username")) {
	    			app.setUsername(intent.getStringExtra("username"));
	    		}
	    		
	    		if(data != null){
	    			feed = data.getQueryParameter("feed");
	    			
	    			if(data.getUserInfo() != null){
	    				app.setUsername(data.getUserInfo());
	    			}
	    		}
			} else {
				if(data != null) {
					tagname = data.getQueryParameter("tagname");
					feed = data.getQueryParameter("feed");
					unread = data.getQueryParameter("unread") != null;
					path = data.getPath();
				}
			}
			
			if(feed == null || feed.equals("")) {
				bookmarkFrag = new BrowseBookmarksFragment();
			} else {
				bookmarkFrag = new BrowseBookmarkFeedFragment();
			}

			t.add(R.id.listcontent, bookmarkFrag);
		} else {
			if(savedInstanceState != null){
			    tagname = savedInstanceState.getString(STATE_TAGNAME);
			    unread = savedInstanceState.getBoolean(STATE_UNREAD);
			    query = savedInstanceState.getString(STATE_QUERY);
			    path = savedInstanceState.getString(STATE_PATH);
			    feed = savedInstanceState.getString(STATE_FEED);
			}
			
			bookmarkFrag = fm.findFragmentById(R.id.listcontent);
		}
		
		if(feed == null || feed.equals("")){
			if(query != null && !query.equals("")){
				((BrowseBookmarksFragment) bookmarkFrag).setSearchQuery(query, app.getUsername(), tagname, unread);
			} else {
				((BookmarkBrowser) bookmarkFrag).setQuery(app.getUsername(), tagname, unread ? "unread" : null);
			}
			
			((BrowseBookmarksFragment) bookmarkFrag).refresh();
		} else {
			if(query == null || query.equals("")){
				((BookmarkBrowser) bookmarkFrag).setQuery(app.getUsername(), tagname, feed);
			} else {
				((BookmarkBrowser) bookmarkFrag).setQuery(app.getUsername(), query, feed);
			}
		}
		
		BrowseTagsFragment tagFrag = (BrowseTagsFragment) fm.findFragmentById(R.id.tagcontent);
		
		if(tagFrag != null){
			tagFrag.setUsername(app.getUsername());
		}
		
		if(path != null && path.contains("tags")){
			t.hide(fm.findFragmentById(R.id.maincontent));
			findViewById(R.id.panel_collapse_button).setVisibility(View.GONE);
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
			contextData.putString("username", app.getUsername());
			contextData.putBoolean("unread", unread);
			startSearch(null, false, contextData, false);
		} else {
			startSearch(null, false, Bundle.EMPTY, false);
		}
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(bookmarkFrag != null && bookmarkFrag instanceof BrowseBookmarkFeedFragment && isTwoPane()) {
		    switch (item.getItemId()) {
			    case R.id.menu_addbookmark:
			    	startActivity(IntentHelper.AddBookmark(lastSelected.getUrl(), null, this));
			    	return true;
			    default:
			        return super.onOptionsItemSelected(item);
		    }
		} else return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		if(lastSelected != null && lastViewType != null){
			savedInstanceState.putParcelable(STATE_LASTBOOKMARK, lastSelected);
	    	savedInstanceState.putSerializable(STATE_LASTVIEWTYPE, lastViewType);
		}
		
		savedInstanceState.putString(STATE_TAGNAME, tagname);
		savedInstanceState.putBoolean(STATE_UNREAD, unread);
		savedInstanceState.putString(STATE_QUERY, query);
		savedInstanceState.putString(STATE_FEED, feed);
		
		BrowseTagsFragment tagFrag = (BrowseTagsFragment) getSupportFragmentManager().findFragmentById(R.id.tagcontent);
		if(tagFrag != null && tagFrag.isVisible()){
			savedInstanceState.putString(STATE_PATH, path);
		}

	    super.onSaveInstanceState(savedInstanceState);
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
	    super.onRestoreInstanceState(savedInstanceState);
	    
	    if(isTwoPane()) {
	    	lastSelected = savedInstanceState.getParcelable(STATE_LASTBOOKMARK);
	    	lastViewType = (BookmarkViewType)savedInstanceState.getSerializable(STATE_LASTVIEWTYPE);
	    	if(lastSelected != null) {
	    		if(!lastViewType.equals(BookmarkViewType.EDIT)){
	    			setBookmarkView(lastSelected, lastViewType);
	    		} else {
	    			onBookmarkEdit(lastSelected);
	    		}
	    	}
	    }
	}
	
	@Override
	public void onBackPressed(){
		super.onBackPressed();
		
		Fragment tagFrag = getSupportFragmentManager().findFragmentById(R.id.tagcontent);
		View panelBtn = findViewById(R.id.panel_collapse_button);
		
		if(tagFrag != null && panelBtn != null){
			if(tagFrag.isVisible())
				findViewById(R.id.panel_collapse_button).setVisibility(View.GONE);
			else findViewById(R.id.panel_collapse_button).setVisibility(View.VISIBLE);
		}
	}
	
	private boolean isTwoPane(){
		return getResources().getBoolean(R.bool.has_two_panes);
	}

	public void onBookmarkView(Bookmark b) {
		if(b != null){
			if(isTwoPane() || findViewById(R.id.tagcontent) != null) {
				lastSelected = b;
				lastViewType = BookmarkViewType.VIEW;
				setBookmarkView(b, BookmarkViewType.VIEW);
			} else {
				startActivity(IntentHelper.ViewBookmark(b, BookmarkViewType.VIEW, null, this));
			}
		}
	}

	public void onBookmarkRead(Bookmark b) {
		if(b != null){
			if(isTwoPane()) {
				lastSelected = b;
				lastViewType = BookmarkViewType.READ;
				setBookmarkView(b, BookmarkViewType.READ);
			} else {
				startActivity(IntentHelper.ViewBookmark(b, BookmarkViewType.READ, null, this));
			}
		}
	}

	public void onBookmarkOpen(Bookmark b) {
		if(b != null){
			if(isTwoPane() && !SettingsHelper.getUseBrowser(this)) {
				lastSelected = b;
				lastViewType = BookmarkViewType.WEB;
				setBookmarkView(b, BookmarkViewType.WEB);
			} else {
				startActivity(IntentHelper.OpenInBrowser(b.getUrl()));
			}
		}
	}

	public void onBookmarkAdd(Bookmark b) {
		if(b != null){
			startActivity(IntentHelper.AddBookmark(b.getUrl(), null, this));
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
			BookmarkManager.UpdateBookmark(b, app.getUsername(), this);
    	}
	}

	public void onBookmarkEdit(Bookmark b) {		
		if(b != null){
			if(isTwoPane()) {
				lastSelected = b;
				lastViewType = BookmarkViewType.EDIT;
				
				AddBookmarkFragment addFrag = (AddBookmarkFragment) getSupportFragmentManager().findFragmentById(R.id.addcontent);
				addFrag.loadBookmark(b, b);
				addFrag.setUsername(app.getUsername());
				addFrag.refreshView();
				FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
				if(getSupportFragmentManager().findFragmentById(R.id.tagcontent).isVisible()){
					transaction.hide(getSupportFragmentManager().findFragmentById(R.id.tagcontent));
					transaction.show(getSupportFragmentManager().findFragmentById(R.id.maincontent));
					transaction.addToBackStack(null);
				}
				transaction.show(getSupportFragmentManager().findFragmentById(R.id.addcontent));
				transaction.commit();
				transaction = getSupportFragmentManager().beginTransaction();
				transaction.hide(getSupportFragmentManager().findFragmentById(R.id.maincontent));
				transaction.commit();
			} else {
				startActivity(IntentHelper.EditBookmark(b, null, this));
			}
		}
	}

	public void onBookmarkDelete(Bookmark b) {
		BookmarkManager.LazyDelete(b, app.getUsername(), this);
	}

	public void onViewTagSelected(String tag, String user) {
		if(isTwoPane()) {
			BrowseBookmarksFragment frag = new BrowseBookmarksFragment();
			frag.setQuery(app.getUsername(), tag, user);
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			transaction.replace(R.id.listcontent, frag);
			transaction.addToBackStack(null);
			transaction.commit();
		} else {
			startActivity(IntentHelper.ViewBookmarks(tag, null, user, this));
		}
	}

	public void onAccountSelected(String account) {
		if(isTwoPane()) {
			BrowseBookmarkFeedFragment frag = new BrowseBookmarkFeedFragment();
			frag.setQuery(app.getUsername(), null, account);
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			transaction.replace(R.id.listcontent, frag);
			transaction.addToBackStack(null);
			transaction.commit();
		} else {
			startActivity(IntentHelper.ViewBookmarks(null, null, account, this));
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
		tagname = tag;
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		BrowseBookmarksFragment frag = new BrowseBookmarksFragment();
		frag.setQuery(app.getUsername(), tag, null);
		transaction.replace(R.id.listcontent, frag);
		transaction.commit();
	}
	
	private void setBookmarkView(Bookmark b, BookmarkViewType viewType){
		if(getSupportFragmentManager().findFragmentById(R.id.maincontent).isHidden() && getSupportFragmentManager().findFragmentById(R.id.addcontent).isHidden()){
			FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
			if(getSupportFragmentManager().findFragmentById(R.id.tagcontent).isVisible()){
				transaction.hide(getSupportFragmentManager().findFragmentById(R.id.tagcontent));
				findViewById(R.id.panel_collapse_button).setVisibility(View.VISIBLE);
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
	
	public void collapsePanel(View v) {
		
		if(findViewById(R.id.listcontent) != null){
			View bookmarkList = findViewById(R.id.listcontent);
			
			if(bookmarkList.getVisibility() == View.VISIBLE)
				bookmarkList.setVisibility(View.GONE);
			else bookmarkList.setVisibility(View.VISIBLE);
		}
	}
	
	public void saveHandler(View v) {
		FragmentManager fm = getSupportFragmentManager();
		AddBookmarkFragment addFrag = (AddBookmarkFragment)fm.findFragmentById(R.id.addcontent);
		
		if(addFrag != null){
			addFrag.saveHandler(v);
		}
	}
	
	public void cancelHandler(View v) {
		FragmentManager fm = getSupportFragmentManager();
		AddBookmarkFragment addFrag = (AddBookmarkFragment)fm.findFragmentById(R.id.addcontent);
		
		if(addFrag != null) {
			addFrag.cancelHandler(v);
		}
	}
	
	@Override 
	protected void changeAccount() {
		FragmentManager fm = getSupportFragmentManager();

		((BookmarkBrowser) bookmarkFrag).setUsername(app.getUsername());
		((BookmarkBrowser) bookmarkFrag).refresh();
		
		ViewBookmarkFragment viewFrag = (ViewBookmarkFragment) fm.findFragmentById(R.id.maincontent);
		if(viewFrag != null) {
			viewFrag.clearView();
		}
		
		BrowseTagsFragment tagFrag = (BrowseTagsFragment) fm.findFragmentById(R.id.tagcontent);
		if(tagFrag != null){
			tagFrag.setUsername(app.getUsername());
			tagFrag.refresh();
		}
	}
}