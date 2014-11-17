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

package com.pindroid.fragment;

import java.io.IOException;
import java.text.ParseException;

import com.pindroid.Constants;
import com.pindroid.R;
import com.pindroid.Constants.BookmarkViewType;
import com.pindroid.client.PinboardFeed;
import com.pindroid.fragment.BrowseBookmarksFragment.OnBookmarkSelectedListener;
import com.pindroid.listadapter.BookmarkViewBinder;
import com.pindroid.platform.BookmarkManager;
import com.pindroid.providers.BookmarkContent.Bookmark;
import com.pindroid.util.AccountHelper;
import com.pindroid.util.SettingsHelper;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.support.v4.widget.SimpleCursorAdapter;

public class BrowseBookmarkFeedFragment extends ListFragment 
	implements LoaderManager.LoaderCallbacks<Cursor>, BookmarkBrowser, PindroidFragment  {
	
	private SimpleCursorAdapter mAdapter;
	
	private String username = null;
	private String tagname = null;
	private Intent intent = null;
	private String feed = null;
	String path = null;
	
	Bookmark lastSelected = null;
	
	ListView lv;
	
	static final String STATE_USERNAME = "username";
	static final String STATE_TAGNAME = "tagname";
	static final String STATE_FEED = "feed";
	
	private BrowseBookmarksFragment.OnBookmarkSelectedListener bookmarkSelectedListener;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setRetainInstance(false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		
	    if (savedInstanceState != null) {
	        username = savedInstanceState.getString(STATE_USERNAME);
	        tagname = savedInstanceState.getString(STATE_TAGNAME);
	        feed = savedInstanceState.getString(STATE_FEED);
	    } 

		intent = getActivity().getIntent();
		
		setHasOptionsMenu(true);
		
		mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.bookmark_feed_view, null, 
				new String[]{Bookmark.Description, Bookmark.Tags}, 
				new int[]{R.id.bookmark_feed_description, R.id.bookmark_feed_tags}, 0);
		
		setListAdapter(mAdapter);
		mAdapter.setViewBinder(new BookmarkViewBinder());

		if(username != null) {
			setListShown(false);
			
	    	getLoaderManager().initLoader(0, null, this);
	    	
			lv = getListView();
			lv.setTextFilterEnabled(true);
			lv.setFastScrollEnabled(true);

			lv.setItemsCanFocus(false);
			lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
			lv.setOnItemClickListener(new OnItemClickListener() {
			    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					final Cursor c = (Cursor)lv.getItemAtPosition(position);
					lastSelected = BookmarkManager.CursorToBookmark(c);
	
					String defaultAction = SettingsHelper.getDefaultAction(getActivity());
					
			    	if(defaultAction.equals("view")) {
			    		viewBookmark(lastSelected);
			    	} else if(defaultAction.equals("read")) {
			    		readBookmark(lastSelected);
			    	} else if(defaultAction.equals("edit")){
			    		addBookmark(lastSelected);
			    	} else {
			    		openBookmarkInBrowser(lastSelected);
			    	}   	
			    }
			});
			
			/* Add Context-Menu listener to the ListView. */
			lv.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
				public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
					menu.setHeaderTitle("Actions");
					MenuInflater inflater = getActivity().getMenuInflater();
					
					inflater.inflate(R.menu.browse_bookmark_context_menu_other, menu);
				}
			});
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	    savedInstanceState.putString(STATE_USERNAME, username);
	    savedInstanceState.putString(STATE_TAGNAME, tagname);
	    savedInstanceState.putString(STATE_FEED, feed);
	    
	    super.onSaveInstanceState(savedInstanceState);
	}
	
	public void setQuery(String username, String tagname, String feed){
		this.username = username;
		this.tagname = tagname;
		this.feed = feed;
	}
	
	public void setUsername(String username){
		this.username = username;
	}
	
	public void refresh(){
		try{
			getLoaderManager().restartLoader(0, null, this);
		} catch(Exception e){}
	}
	
	@Override
	public void onResume(){
		super.onResume();
		
		if(Intent.ACTION_SEARCH.equals(intent.getAction())) {		
			String query = intent.getStringExtra(SearchManager.QUERY);
			getActivity().setTitle(getString(R.string.search_results_global_tag_title, query));
		} else if(feed != null && feed.equals("recent")) {
			getActivity().setTitle(getString(R.string.browse_recent_bookmarks_title));
		} else if(feed != null && feed.equals("popular")) {
			getActivity().setTitle(getString(R.string.browse_popular_bookmarks_title));
		} else if(feed != null && feed.equals("network")) {
			getActivity().setTitle(getString(R.string.browse_network_bookmarks_title));
		} else {	
			if(tagname != null && tagname != "") {
				getActivity().setTitle(getString(R.string.browse_user_bookmarks_tagged_title, feed, tagname));
			} else {
				getActivity().setTitle(getString(R.string.browse_user_bookmarks_title, feed));
			}
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem aItem) {
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) aItem.getMenuInfo();
		final Cursor c = (Cursor)lv.getItemAtPosition(menuInfo.position);
		Bookmark b = BookmarkManager.CursorToBookmark(c);
		
		switch (aItem.getItemId()) {
			case R.id.menu_bookmark_context_open:
				openBookmarkInBrowser(b);
				return true;
			case R.id.menu_bookmark_context_view:				
				viewBookmark(b);
				return true;
			case R.id.menu_bookmark_context_add:				
				addBookmark(b);
				return true;
			case R.id.menu_bookmark_context_read:
				readBookmark(b);
				return true;
			case R.id.menu_bookmark_context_share:
				bookmarkSelectedListener.onBookmarkShare(b);
				return true;
		}
		return false;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		boolean result = false;
		
	    switch (item.getItemId()) {
		    case R.id.menu_addbookmark:
				addBookmark(lastSelected);
				return true;
	    }
	    
	    if(result) {
	    	getLoaderManager().restartLoader(0, null, this);
	    } else result = super.onOptionsItemSelected(item);
	    
	    return result;
	}
		
	private void openBookmarkInBrowser(Bookmark b) {
		bookmarkSelectedListener.onBookmarkSelected(b, BookmarkViewType.WEB);
	}
	
	private void viewBookmark(Bookmark b) {
		bookmarkSelectedListener.onBookmarkSelected(b, BookmarkViewType.VIEW);
	}
	
	private void readBookmark(Bookmark b){
		bookmarkSelectedListener.onBookmarkSelected(b, BookmarkViewType.READ);
	}
	
	private void addBookmark(Bookmark b){
		bookmarkSelectedListener.onBookmarkAdd(b);
	}

	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if(Intent.ACTION_SEARCH.equals(intent.getAction())) {		
			String query = intent.getStringExtra(SearchManager.QUERY);
			return new LoaderDrone(getActivity(), username, query, feed, AccountHelper.getAccount(username, getActivity()));
		} else {			
			return new LoaderDrone(getActivity(), username, tagname, feed, AccountHelper.getAccount(username, getActivity()));
		}
	}
	
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
	    mAdapter.swapCursor(data);
	    
	    // The list should now be shown.
        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
	}
	
	public void onLoaderReset(Loader<Cursor> loader) {
	    mAdapter.swapCursor(null);
	}
	
	public static class LoaderDrone extends AsyncTaskLoader<Cursor> {
        
		private String user = "";
		private String tag = "";
		private String feed = "";
		private Account account = null;
		
        public LoaderDrone(Context context, String u, String t, String f, Account a) {
        	super(context);
        	
        	user = u;
            tag = t;
            feed = f;
            account = a;
        	
            onForceLoad();
        }

        @Override
        public Cursor loadInBackground() {
            Cursor results = null;
            
 	       if(feed.equals("global"))
 	    	   feed = "";
        
 		   try {
 			   if(feed.equals("network")) {
 				   String token = AccountManager.get(getContext()).getUserData(account, Constants.PREFS_SECRET_TOKEN);

 				   results = PinboardFeed.fetchNetworkRecent(user, token);
 			   } else if(feed.equals("recent")) {
 				  results = PinboardFeed.fetchRecent();
 			   } else if(feed.equals("popular")) {
  				  results = PinboardFeed.fetchPopular();
  			   } else {
 				  results = PinboardFeed.fetchUserRecent(feed, tag);
 			   }

 		   }catch (ParseException e) {
 			   e.printStackTrace();
 		   }catch (IOException e) {
 			   e.printStackTrace();
 		   }

           return results;
        }
    }
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			bookmarkSelectedListener = (OnBookmarkSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnBookmarkSelectedListener");
		}
	}
}