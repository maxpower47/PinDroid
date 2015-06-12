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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.kennyc.view.MultiStateView;
import com.pindroid.Constants;
import com.pindroid.Constants.BookmarkViewType;
import com.pindroid.R;
import com.pindroid.client.PinboardFeedClient;
import com.pindroid.event.FeedBookmarkSelectedEvent;
import com.pindroid.fragment.BrowseBookmarksFragment.OnBookmarkSelectedListener;
import com.pindroid.listadapter.BookmarkFeedAdapter;
import com.pindroid.model.FeedBookmark;
import com.pindroid.providers.BookmarkContent.Bookmark;
import com.pindroid.util.AccountHelper;
import com.pindroid.util.SettingsHelper;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import de.greenrobot.event.EventBus;

@EFragment(R.layout.browse_bookmark_feed_fragment)
public class BrowseBookmarkFeedFragment extends Fragment
	implements LoaderManager.LoaderCallbacks<List<FeedBookmark>>, BookmarkBrowser, PindroidFragment  {
	
	@Bean BookmarkFeedAdapter adapter;

    @InstanceState String username = null;
	@InstanceState String tagname = null;
	@InstanceState String feed = null;
	
	FeedBookmark lastSelected = null;
    @ViewById(android.R.id.list) RecyclerView listView;
    @ViewById(R.id.bookmark_feed_refresh) SwipeRefreshLayout refreshLayout;
    @ViewById(R.id.bookmark_feed_multistate) MultiStateView multiStateView;
	
	private BrowseBookmarksFragment.OnBookmarkSelectedListener bookmarkSelectedListener;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setRetainInstance(false);
	}

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
	
	@AfterViews
	public void init(){

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        listView.setLayoutManager(mLayoutManager);
		
		listView.setAdapter(adapter);
        listView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity()).build());

        refreshLayout.setColorSchemeResources(R.color.pindroid_blue);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getLoaderManager().restartLoader(0, null, BrowseBookmarkFeedFragment.this);
            }
        });

		if(username != null) {
            multiStateView.setViewState(MultiStateView.ViewState.LOADING);
	    	getLoaderManager().initLoader(0, null, this);


			//lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
/*
			lv.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
				public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
					menu.setHeaderTitle("Actions");
					MenuInflater inflater = getActivity().getMenuInflater();
					
					inflater.inflate(R.menu.browse_bookmark_context_menu_other, menu);
				}
			});*/
		}
	}

    public void onEvent(FeedBookmarkSelectedEvent event) {
        lastSelected = event.getBookmark();

        String defaultAction = SettingsHelper.getDefaultAction(getActivity());

        switch (defaultAction) {
            case "view":
                viewBookmark(lastSelected.toBookmark());
                break;
            case "read":
                readBookmark(lastSelected.toBookmark());
                break;
            case "edit":
                addBookmark(lastSelected.toBookmark());
                break;
            default:
                openBookmarkInBrowser(lastSelected.toBookmark());
                break;
        }
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
		
		if(Intent.ACTION_SEARCH.equals(getActivity().getIntent().getAction())) {
			String query = getActivity().getIntent().getStringExtra(SearchManager.QUERY);
			getActivity().setTitle(getString(R.string.search_results_global_tag_title, query));
		} else if(feed != null && feed.equals("recent")) {
			getActivity().setTitle(getString(R.string.browse_recent_bookmarks_title));
		} else if(feed != null && feed.equals("popular")) {
			getActivity().setTitle(getString(R.string.browse_popular_bookmarks_title));
		} else if(feed != null && feed.equals("network")) {
			getActivity().setTitle(getString(R.string.browse_network_bookmarks_title));
		} else {	
			if(tagname != null && !tagname.equals("")) {
				getActivity().setTitle(getString(R.string.browse_user_bookmarks_tagged_title, feed, tagname));
			} else {
				getActivity().setTitle(getString(R.string.browse_user_bookmarks_title, feed));
			}
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem aItem) {
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) aItem.getMenuInfo();
		final FeedBookmark feedBookmark = adapter.getItemAtPosition(menuInfo.position);
		
		switch (aItem.getItemId()) {
			case R.id.menu_bookmark_context_open:
				openBookmarkInBrowser(feedBookmark.toBookmark());
				return true;
			case R.id.menu_bookmark_context_view:				
				viewBookmark(feedBookmark.toBookmark());
				return true;
			case R.id.menu_bookmark_context_add:				
				addBookmark(feedBookmark.toBookmark());
				return true;
			case R.id.menu_bookmark_context_read:
				readBookmark(feedBookmark.toBookmark());
				return true;
			case R.id.menu_bookmark_context_share:
				bookmarkSelectedListener.onBookmarkShare(feedBookmark.toBookmark());
				return true;
		}
		return false;
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

	public Loader<List<FeedBookmark>> onCreateLoader(int id, Bundle args) {
		if(Intent.ACTION_SEARCH.equals(getActivity().getIntent().getAction())) {
			String query = getActivity().getIntent().getStringExtra(SearchManager.QUERY);
			return new LoaderDrone(getActivity(), username, query, feed, AccountHelper.getAccount(username, getActivity()));
		} else {
			return new LoaderDrone(getActivity(), username, tagname, feed, AccountHelper.getAccount(username, getActivity()));
		}
	}
	
	public void onLoadFinished(Loader<List<FeedBookmark>> loader, List<FeedBookmark> data) {
	    adapter.setFeedBookmarks(data);
        refreshLayout.setRefreshing(false);
        multiStateView.setViewState(MultiStateView.ViewState.CONTENT);
	}
	
	public void onLoaderReset(Loader<List<FeedBookmark>> loader) {
	    adapter.setFeedBookmarks(null);
	}
	
	public static class LoaderDrone extends AsyncTaskLoader<List<FeedBookmark>> {
        
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
        public List<FeedBookmark> loadInBackground() {
            List<FeedBookmark> results = null;

 		   try {
			   switch (feed) {
				   case "network":
					   String token = AccountManager.get(getContext()).getUserData(account, Constants.PREFS_SECRET_TOKEN);
					   results = PinboardFeedClient.get().getNetworkRecent(token, user);
					   break;
				   case "recent":
					   results = PinboardFeedClient.get().getRecent();
					   break;
				   case "popular":
					   results = PinboardFeedClient.get().getPopular();
					   break;
				   case "global":
					   results = PinboardFeedClient.get().searchGlobalTags(tag);
					   break;
				   default:
					   if(tag != null && !"".equals(tag)) {
						   results = PinboardFeedClient.get().getUserRecent(feed, tag);
					   } else {
						   results = PinboardFeedClient.get().getUserRecent(feed);
					   }
					   break;
			   }

 		   } catch (Exception e) {
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