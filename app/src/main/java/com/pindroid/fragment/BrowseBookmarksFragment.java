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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.melnykov.fab.FloatingActionButton;
import com.pindroid.Constants.BookmarkViewType;
import com.pindroid.R;
import com.pindroid.listadapter.BookmarkViewBinder;
import com.pindroid.platform.BookmarkManager;
import com.pindroid.providers.BookmarkContent.Bookmark;
import com.pindroid.syncadapter.BookmarkSyncAdapter;
import com.pindroid.util.SettingsHelper;
import com.pindroid.util.SyncUtils;

import java.lang.ref.WeakReference;

public class BrowseBookmarksFragment extends ListFragment 
	implements LoaderManager.LoaderCallbacks<Cursor>, BookmarkBrowser, PindroidFragment, SwipeRefreshLayout.OnRefreshListener {

    private ListView listView;
	private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton actionButton;
	
	private SimpleCursorAdapter mAdapter;
	
	private String sortfield = Bookmark.Time + " DESC";

	@Nullable
	private String username = null;

	@Nullable
	private String tagname = null;

	private boolean unread = false;
	private boolean untagged = false;
	private String query = null;
	
	ListView lv;
	
	private static final String STATE_USERNAME = "username";
	private static final String STATE_TAGNAME = "tagname";
	
	private OnBookmarkSelectedListener bookmarkSelectedListener;

	public interface OnBookmarkSelectedListener {
		public void onBookmarkSelected(Bookmark b, BookmarkViewType type);
		public void onBookmarkAdd(Bookmark b);
		public void onBookmarkShare(Bookmark b);
		public void onBookmarkMark(Bookmark b);
		public void onBookmarkDelete(Bookmark b);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setRetainInstance(false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);

        listView = (ListView) getView().findViewById(android.R.id.list);
		swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipe_refresh);
		swipeRefreshLayout.setOnRefreshListener(this);
		actionButton = (FloatingActionButton) getView().findViewById(R.id.add_button);

        actionButton.attachToListView(listView);
		
	    if (savedInstanceState != null) {
	        username = savedInstanceState.getString(STATE_USERNAME);
	        tagname = savedInstanceState.getString(STATE_TAGNAME);
	    }
		
		setHasOptionsMenu(true);
		
		mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.bookmark_view, null, 
				new String[]{Bookmark.Description, Bookmark.Tags, Bookmark.ToRead, Bookmark.Shared, Bookmark.Synced}, 
				new int[]{R.id.bookmark_description, R.id.bookmark_tags, R.id.bookmark_unread, R.id.bookmark_private, R.id.bookmark_synced}, 0);
		
		setListAdapter(mAdapter);
		mAdapter.setViewBinder(new BookmarkViewBinder());

		if(username != null) {				
	
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
					
					String defaultAction = SettingsHelper.getDefaultAction(getActivity());
	
			    	if(defaultAction.equals("view")) {
			    		viewBookmark(b);
			    	} else if(defaultAction.equals("read")) {
			    		readBookmark(b);
			    	} else if(defaultAction.equals("edit")){
			    		editBookmark(b);
			    	} else {
			    		openBookmarkInBrowser(b);
			    	}   	
			    }
			});
			
			/* Add Context-Menu listener to the ListView. */
			lv.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
				public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
					menu.setHeaderTitle("Actions");
					MenuInflater inflater = getActivity().getMenuInflater();

					inflater.inflate(R.menu.browse_bookmark_context_menu_self, menu);
				}
			});

            actionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    bookmarkSelectedListener.onBookmarkAdd(null);
                }
            });
		}
	}
	
	public void setQuery(String username, String tagname, @Nullable String feed) {
		this.username = username;
		this.tagname = tagname;
		this.unread = "unread".equals(feed);
		this.untagged = "untagged".equals(feed);
	}
	
	public void setSearchQuery(String query, String username, String tagname, boolean unread){
		this.query = query;
		this.username = username;
		this.tagname = tagname;
		this.unread = unread;
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
        updateTitle();
	}

    /**
     * Update title for {@link Activity} which contains this fragment.<br/>
     * Title also tells the number of bookmarks that this fragment is showing.
     */
    private void updateTitle() {
        int numOfBookmarks = mAdapter.getCount();
        String title = getTitle();
        if (!TextUtils.isEmpty(title)) {
            if (numOfBookmarks != 0) {
                title = getString(R.string.browse_my_bookmarks_title_count, title, numOfBookmarks);
            }
            getActivity().setTitle(title);
        }
    }

    /**
     * Pick appropriate title for what this fragment shows
     */
    private String getTitle() {
        String title = null;
        if (query != null) {
            if (unread) {
                title = getString(R.string.unread_search_results_title, query);
            } else {
                title = getString(R.string.bookmark_search_results_title, query);
            }
            // TODO untagged search result
        } else {
            if (unread && !TextUtils.isEmpty(tagname)) {
                title = getString(R.string.browse_my_unread_bookmarks_tagged_title, tagname);
            } else if (unread && TextUtils.isEmpty(tagname)) {
                title = getString(R.string.browse_my_unread_bookmarks_title);
            } else if (untagged && TextUtils.isEmpty(tagname)) {
                title = getString(R.string.browse_my_untagged_bookmarks_title);
            } else if (!TextUtils.isEmpty(tagname)) {
                title = getString(R.string.browse_my_bookmarks_tagged_title, tagname);
            } else {
                title = getString(R.string.browse_my_bookmarks_title);
            }
        }
        return title;
    }
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	    savedInstanceState.putString(STATE_USERNAME, username);
	    savedInstanceState.putString(STATE_TAGNAME, tagname);
	    super.onSaveInstanceState(savedInstanceState);
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
			case R.id.menu_bookmark_context_edit:
				bookmarkSelectedListener.onBookmarkSelected(b, BookmarkViewType.EDIT);
				return true;
			case R.id.menu_bookmark_context_delete:
				bookmarkSelectedListener.onBookmarkDelete(b);
				return true;
			case R.id.menu_bookmark_context_share:
				Log.d("share", "browse");
				bookmarkSelectedListener.onBookmarkShare(b);
				return true;
			case R.id.menu_bookmark_context_read:
				readBookmark(b);
				return true;
			case R.id.menu_bookmark_context_markread:
				markBookmark(b);
				return true;
		}
		return false;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.browse_bookmark_menu, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		boolean result = false;
		
	    switch (item.getItemId()) {
		    case R.id.menu_bookmark_sort_date_asc:
		    	sortfield = Bookmark.Time + " ASC";
				result = true;
				break;
		    case R.id.menu_bookmark_sort_date_desc:			
		    	sortfield = Bookmark.Time + " DESC";
		    	result = true;
		    	break;
		    case R.id.menu_bookmark_sort_description_asc:			
		    	sortfield = Bookmark.Description + " ASC";
		    	result = true;
		    	break;
		    case R.id.menu_bookmark_sort_description_desc:			
		    	sortfield = Bookmark.Description + " DESC";
		    	result = true;
		    	break;
		    case R.id.menu_bookmark_sort_url_asc:			
		    	sortfield = Bookmark.Url + " ASC";
		    	result = true;
		    	break;
		    case R.id.menu_bookmark_sort_url_desc:			
		    	sortfield = Bookmark.Url + " DESC";
		    	result = true;
		    	break;
	    }
	    
	    if(result) {
	    	refresh();
	    } else result = super.onOptionsItemSelected(item);
	    
	    return result;
	}
	
	private void openBookmarkInBrowser(Bookmark b) {
		bookmarkSelectedListener.onBookmarkSelected(b, BookmarkViewType.WEB);
	}
	
	private void readBookmark(Bookmark b){
		bookmarkSelectedListener.onBookmarkSelected(b, BookmarkViewType.READ);
	}
	
	private void markBookmark(Bookmark b){
		bookmarkSelectedListener.onBookmarkMark(b);
	}
	
	private void viewBookmark(Bookmark b) {
		bookmarkSelectedListener.onBookmarkSelected(b, BookmarkViewType.VIEW);
	}
	
	private void editBookmark(Bookmark b) {
		bookmarkSelectedListener.onBookmarkSelected(b, BookmarkViewType.EDIT);
	}
    
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {

		if (query != null) {
			return BookmarkManager.SearchBookmarks(query, tagname, unread, username, getActivity());
		} else {
			return BookmarkManager.GetBookmarks(username, tagname, unread, untagged, sortfield, getActivity());
		}
	}
	
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
	    mAdapter.swapCursor(data);
        updateTitle();
	}
	
	public void onLoaderReset(Loader<Cursor> loader) {
	    mAdapter.swapCursor(null);
        updateTitle();
	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.browse_bookmark_fragment, container, false);
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

	@Override
	public void onRefresh() {
		swipeRefreshLayout.setRefreshing(true);
		SyncFinishedBroadcastReceiver.registerCallback(this);
		SyncUtils.requestSync(getActivity());
	}

	/*
		Callback for bookmarks sync status
	 */
	private static class SyncFinishedBroadcastReceiver extends BroadcastReceiver {

		@NonNull
		private final WeakReference<BrowseBookmarksFragment> fragmentRef;

		private SyncFinishedBroadcastReceiver(@NonNull BrowseBookmarksFragment fragment) {
			fragmentRef = new WeakReference<>(fragment);
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
			BrowseBookmarksFragment fragment = fragmentRef.get();
			if (fragment != null) {
				fragment.swipeRefreshLayout.setRefreshing(false);
			}
		}

		static void registerCallback(@NonNull BrowseBookmarksFragment fragment) {
			IntentFilter filter = new IntentFilter(BookmarkSyncAdapter.SYNC_FINISHED_ACTION);
			LocalBroadcastManager
					.getInstance(fragment.getActivity())
					.registerReceiver(new SyncFinishedBroadcastReceiver(fragment), filter);
		}
	}
}