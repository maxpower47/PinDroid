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
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;

import com.kennyc.view.MultiStateView;
import com.pindroid.Constants.BookmarkViewType;
import com.pindroid.R;
import com.pindroid.event.AccountChangedEvent;
import com.pindroid.event.BookmarkDeletedEvent;
import com.pindroid.event.BookmarkSelectedEvent;
import com.pindroid.event.SyncCompleteEvent;
import com.pindroid.listadapter.BookmarkAdapter;
import com.pindroid.platform.BookmarkManager;
import com.pindroid.providers.BookmarkContent.Bookmark;
import com.pindroid.providers.BookmarkContentProvider;
import com.pindroid.util.AccountHelper;
import com.pindroid.util.SettingsHelper;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import de.greenrobot.event.EventBus;

@EFragment(R.layout.browse_bookmark_fragment)
@OptionsMenu(R.menu.browse_bookmark_menu)
public class BrowseBookmarksFragment extends Fragment
	implements LoaderManager.LoaderCallbacks<Cursor>, BookmarkBrowser {

    @ViewById(android.R.id.list) RecyclerView listView;
	@ViewById(R.id.floating_add_button) FloatingActionButton actionButton;
    @ViewById(R.id.bookmark_refresh) SwipeRefreshLayout refreshLayout;
    @ViewById(R.id.bookmark_multistate) MultiStateView multiStateView;
	
	@Bean BookmarkAdapter mAdapter;

    private String sortfield = Bookmark.Time + " DESC";

    String username;
	@FragmentArg String tagname;
	@FragmentArg boolean unread;
	@FragmentArg String query;
	
	private OnBookmarkSelectedListener bookmarkSelectedListener;

	public interface OnBookmarkSelectedListener {
		void onBookmarkSelected(Bookmark b, BookmarkViewType type);
		void onBookmarkAdd(Bookmark b);
		void onBookmarkShare(Bookmark b);
		void onBookmarkMark(Bookmark b);
		void onBookmarkDelete(Bookmark b);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().registerSticky(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    public void setTag(String tag, String user) {
        this.tagname = tag;
    }
	
	@AfterViews
	public void init(){
		listView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        listView.setLayoutManager(mLayoutManager);

		listView.setAdapter(mAdapter);

        listView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity()).build());

		if(username != null) {				
	
	    	getLoaderManager().initLoader(0, null, this);

			/* Add Context-Menu listener to the ListView. */
			/*listView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
				public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
					menu.setHeaderTitle("Actions");
					MenuInflater inflater = getActivity().getMenuInflater();

					inflater.inflate(R.menu.browse_bookmark_context_menu_self, menu);
				}
			});*/

            refreshLayout.setColorSchemeResources(R.color.pindroid_blue);
            refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    ContentResolver.requestSync(AccountHelper.getAccount(username, getActivity()), BookmarkContentProvider.AUTHORITY, new Bundle());
                }
            });
		}
	}

    @Click(R.id.floating_add_button)
    public void onAddButton() {
        bookmarkSelectedListener.onBookmarkAdd(null);
    }

    public void onEventMainThread(SyncCompleteEvent event) {
        refreshLayout.setRefreshing(false);
    }

    public void onEventMainThread(final BookmarkDeletedEvent event) {
        Snackbar.make(listView, R.string.snackbar_deleted, Snackbar.LENGTH_LONG)
                .setActionTextColor(getResources().getColorStateList(R.color.snackbar_button))
                .setAction(R.string.snackbar_undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        BookmarkManager.LazyUndelete(event.getBookmark(), username, getActivity());
                    }
                })
                .show();
    }

    public void onEvent(BookmarkSelectedEvent event) {
        String defaultAction = SettingsHelper.getDefaultAction(getActivity());

        switch (defaultAction) {
            case "view":
                viewBookmark(event.getBookmark());
                break;
            case "read":
                readBookmark(event.getBookmark());
                break;
            case "edit":
                editBookmark(event.getBookmark());
                break;
            default:
                openBookmarkInBrowser(event.getBookmark());
                break;
        }
    }

    public void onEvent(AccountChangedEvent event) {
        this.username = event.getNewAccount();
        refresh();
    }

	public void refresh(){
		try{
			getLoaderManager().restartLoader(0, null, this);
		} catch(Exception e){}
	}
	
	@Override
	public void onResume(){
		super.onResume();

		if(query != null) {
			if(unread) {
				getActivity().setTitle(getString(R.string.unread_search_results_title, query));
			} else getActivity().setTitle(getString(R.string.bookmark_search_results_title, query));
		} else {
			if(unread && tagname != null && !"".equals(tagname)) {
				getActivity().setTitle(getString(R.string.browse_my_unread_bookmarks_tagged_title, tagname));
			} else if(unread && (tagname == null || tagname.equals(""))) {
				getActivity().setTitle(getString(R.string.browse_my_unread_bookmarks_title));
			} else if(tagname != null && !"".equals(tagname)) {
				getActivity().setTitle(getString(R.string.browse_my_bookmarks_tagged_title, tagname));
			} else {
				getActivity().setTitle(getString(R.string.browse_my_bookmarks_title));
			}
		}
	}
/*
	@Override
	public boolean onContextItemSelected(MenuItem aItem) {
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) aItem.getMenuInfo();
		final Cursor c = mAdapter.getItemAtPosition(menuInfo.position);
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
	*/
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

		if(query != null) {
			return BookmarkManager.SearchBookmarks(query, tagname, unread, username, getActivity());
		} else {
			return BookmarkManager.GetBookmarks(username, tagname, unread, sortfield, getActivity());
		}
	}
	
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
	    mAdapter.changeCursor(data);
        if(mAdapter.getItemCount() < 1) {
            if(unread) {
                multiStateView.setViewForState(R.layout.unread_empty, MultiStateView.ViewState.EMPTY);
            } else {
                multiStateView.setViewForState(R.layout.bookmark_empty, MultiStateView.ViewState.EMPTY);
            }
            multiStateView.setViewState(MultiStateView.ViewState.EMPTY);
        } else {
            multiStateView.setViewState(MultiStateView.ViewState.CONTENT);
        }
	}
	
	public void onLoaderReset(Loader<Cursor> loader) {
	    mAdapter.changeCursor(null);
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