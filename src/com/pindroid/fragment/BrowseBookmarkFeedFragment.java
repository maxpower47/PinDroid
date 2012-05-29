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
import com.pindroid.activity.FragmentBaseActivity;
import com.pindroid.client.PinboardFeed;
import com.pindroid.fragment.BrowseBookmarksFragment.OnBookmarkSelectedListener;
import com.pindroid.listadapter.BookmarkViewBinder;
import com.pindroid.platform.BookmarkManager;
import com.pindroid.providers.BookmarkContent.Bookmark;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
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
	implements LoaderManager.LoaderCallbacks<Cursor> {
	
	private SimpleCursorAdapter mAdapter;
	private FragmentBaseActivity base;
	
	private String username = null;
	private String tagname = null;
	private Intent intent = null;
	String path = null;
	
	ListView lv;
	
	private BrowseBookmarksFragment.OnBookmarkSelectedListener bookmarkSelectedListener;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);

		base = (FragmentBaseActivity)getActivity();
		intent = base.getIntent();
		
		setHasOptionsMenu(true);
		
		mAdapter = new SimpleCursorAdapter(base, R.layout.bookmark_view, null, 
				new String[]{Bookmark.Description, Bookmark.Tags, Bookmark.ToRead, Bookmark.Shared}, 
				new int[]{R.id.bookmark_description, R.id.bookmark_tags, R.id.bookmark_unread, R.id.bookmark_private}, 0);
		
		setListAdapter(mAdapter);
		mAdapter.setViewBinder(new BookmarkViewBinder());

		if(base.mAccount != null) {					    	
	    	getLoaderManager().initLoader(0, null, this);
	    	
			lv = getListView();
			lv.setTextFilterEnabled(true);
			lv.setFastScrollEnabled(true);

			lv.setItemsCanFocus(false);
			lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
			lv.setOnItemClickListener(new OnItemClickListener() {
			    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					final Cursor c = (Cursor)lv.getItemAtPosition(position);
					Bookmark b = BookmarkManager.CursorToBookmark(c);
	
			    	if(base.defaultAction.equals("view")) {
			    		viewBookmark(b);
			    	} else if(base.defaultAction.equals("read")) {
			    		readBookmark(b);
			    	} else {
			    		openBookmarkInBrowser(b);
			    	}   	
			    }
			});
			
			/* Add Context-Menu listener to the ListView. */
			lv.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
				public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
					menu.setHeaderTitle("Actions");
					MenuInflater inflater = base.getMenuInflater();
					
					inflater.inflate(R.menu.browse_bookmark_context_menu_other, menu);
				}
			});
		}
	}
	
	public void setQuery(String username, String tagname){
		this.username = username;
		this.tagname = tagname;
	}
	
	@Override
	public void onResume(){
		super.onResume();
		
		if(Intent.ACTION_SEARCH.equals(intent.getAction())) {		
			String query = intent.getStringExtra(SearchManager.QUERY);
			base.setTitle(getString(R.string.search_results_global_tag_title, query));
		} else if(username.equals("recent")) {
			base.setTitle(getString(R.string.browse_recent_bookmarks_title));
		} else if(username.equals("network")) {
			base.setTitle(getString(R.string.browse_network_bookmarks_title));
		} else {	
			if(tagname != null && tagname != "") {
				base.setTitle(getString(R.string.browse_user_bookmarks_tagged_title, username, tagname));
			} else {
				base.setTitle(getString(R.string.browse_user_bookmarks_title, username));
			}
		}
		
		Uri data = base.getIntent().getData();
		if(data != null && data.getUserInfo() != null && data.getUserInfo() != "") {
			username = data.getUserInfo();
		} else if(base.getIntent().hasExtra("username")){
			username = base.getIntent().getStringExtra("username");
		} else username = base.mAccount.name;
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
				bookmarkSelectedListener.onBookmarkAdd(b);
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
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.main_menu, menu);
		
		base.setupSearch(menu);
	}
		
	private void openBookmarkInBrowser(Bookmark b) {
		bookmarkSelectedListener.onBookmarkOpen(b);
	}
	
	private void viewBookmark(Bookmark b) {
		bookmarkSelectedListener.onBookmarkView(b);
	}
	
	private void readBookmark(Bookmark b){
		bookmarkSelectedListener.onBookmarkRead(b);
	}

	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if(Intent.ACTION_SEARCH.equals(intent.getAction())) {		
			String query = intent.getStringExtra(SearchManager.QUERY);
			return new LoaderDrone(base, "global", query);
		} else if(username.equals("recent")) {
			return new LoaderDrone(base, "recent", null);
		} else if(username.equals("network")) {
			return new LoaderDrone(base, "network", null);
		} else {			
			return new LoaderDrone(base, username, tagname);
		}
	}
	
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
	    mAdapter.swapCursor(data);
	}
	
	public void onLoaderReset(Loader<Cursor> loader) {
	    mAdapter.swapCursor(null);
	}
	
	public static class LoaderDrone extends AsyncTaskLoader<Cursor> {
        
		private String user = "";
		private String tag = "";

		private FragmentBaseActivity base = null;
		
        public LoaderDrone(Context context, String u, String t) {
        	super(context);
        	
        	user = u;
            tag = t;
            base = (FragmentBaseActivity)context;
        	
            onForceLoad();
        }

        @Override
        public Cursor loadInBackground() {
            Cursor results = null;
            
 	       if(user.equals("global"))
 	    	   user = "";
        
 		   try {
 			   if(user.equals("network")) {
 				   String token = AccountManager.get(getContext()).getUserData(base.mAccount, Constants.PREFS_SECRET_TOKEN);
 				   results = PinboardFeed.fetchNetworkRecent(base.mAccount.name, token);
 			   } else if(user.equals("recent")) {
 				  results = PinboardFeed.fetchRecent();
 			   } else {
 				  results = PinboardFeed.fetchUserRecent(user, tag);
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