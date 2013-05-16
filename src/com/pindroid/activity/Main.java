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
import com.pindroid.action.IntentHelper;
import com.pindroid.fragment.BrowseBookmarkFeedFragment;
import com.pindroid.fragment.BrowseBookmarksFragment;
import com.pindroid.fragment.BrowseBookmarksFragment.OnBookmarkSelectedListener;
import com.pindroid.fragment.BrowseNotesFragment;
import com.pindroid.fragment.BrowseNotesFragment.OnNoteSelectedListener;
import com.pindroid.fragment.BrowseTagsFragment;
import com.pindroid.fragment.BrowseTagsFragment.OnTagSelectedListener;
import com.pindroid.fragment.MainFragment;
import com.pindroid.providers.BookmarkContent.Bookmark;
import com.pindroid.providers.NoteContent.Note;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class Main extends FragmentBaseActivity implements MainFragment.OnMainActionListener, OnBookmarkSelectedListener, OnTagSelectedListener, OnNoteSelectedListener {
	
	private ListView mDrawerList;
	private DrawerLayout mDrawerLayout;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedState);
		setContentView(R.layout.main);
		
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		
		String[] MENU_ITEMS = new String[] {getString(R.string.main_menu_my_bookmarks),
				getString(R.string.main_menu_my_unread_bookmarks),
				getString(R.string.main_menu_my_tags),
				getString(R.string.main_menu_my_notes),
				getString(R.string.main_menu_recent_bookmarks),
				getString(R.string.main_menu_popular_bookmarks),
				getString(R.string.main_menu_network_bookmarks)};
		
		mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.main_view, MENU_ITEMS));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
	}
	
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
	}

	/** Swaps fragments in the main content view */
	private void selectItem(int position) {
		Fragment frag = null;
		
		if(position == 0){
			frag = new BrowseBookmarksFragment();
			((BrowseBookmarksFragment)frag).setQuery(app.getUsername(), null, null);
    	} else if(position == 1){
    		frag = new BrowseBookmarksFragment();
			((BrowseBookmarksFragment)frag).setQuery(app.getUsername(), null, "unread");
    	} else if(position == 2){
     		frag = new BrowseTagsFragment();
 			((BrowseTagsFragment)frag).setAccount(app.getUsername());
     	} else if(position == 3){
      		frag = new BrowseNotesFragment();
  			((BrowseNotesFragment)frag).setAccount(app.getUsername());
      	} else if(position == 4){
      		frag = new BrowseBookmarkFeedFragment();
  			((BrowseBookmarkFeedFragment)frag).setQuery(app.getUsername(), null, "recent");
      	} else if(position == 5){
      		frag = new BrowseBookmarkFeedFragment();
  			((BrowseBookmarkFeedFragment)frag).setQuery(app.getUsername(), null, "popular");
      	} else if(position == 6){
      		frag = new BrowseBookmarkFeedFragment();
  			((BrowseBookmarkFeedFragment)frag).setQuery(app.getUsername(), null, "network");
      	}
		
		
		
		// Insert the fragment by replacing any existing fragment
	    FragmentManager fragmentManager = getSupportFragmentManager();
	    fragmentManager.beginTransaction()
	                   .replace(R.id.content_frame, frag)
	                   .commit();

	    // Highlight the selected item, update the title, and close the drawer
	    mDrawerList.setItemChecked(position, true);
	    mDrawerLayout.closeDrawer(mDrawerList);   
	}

	public void onMyBookmarksSelected() {
		
	}

	public void onMyUnreadSelected() {
		startActivity(IntentHelper.ViewUnread(null, this));
	}

	public void onMyTagsSelected() {
		if(getResources().getBoolean(R.bool.has_two_panes)){
			startActivity(IntentHelper.ViewTabletTags(null, this));
		} else {
			startActivity(IntentHelper.ViewTags(null, this));
		}	
	}
	
	public void onMyNotesSelected() {
		startActivity(IntentHelper.ViewNotes(null, this));	
	}

	public void onMyNetworkSelected() {
		startActivity(IntentHelper.ViewBookmarks("", null, "network", this));	
	}

	public void onRecentSelected() {
		startActivity(IntentHelper.ViewBookmarks("", null, "recent", this));	
	}
	
	public void onPopularSelected() {
		startActivity(IntentHelper.ViewBookmarks("", null, "popular", this));	
	}
	
	@Override
	protected void changeAccount(){}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);

	    setupSearch(menu);
	    return true;
	}

	public void onBookmarkView(Bookmark b) {
		// TODO Auto-generated method stub
		
	}

	public void onBookmarkRead(Bookmark b) {
		// TODO Auto-generated method stub
		
	}

	public void onBookmarkOpen(Bookmark b) {
		// TODO Auto-generated method stub
		
	}

	public void onBookmarkAdd(Bookmark b) {
		// TODO Auto-generated method stub
		
	}

	public void onBookmarkShare(Bookmark b) {
		// TODO Auto-generated method stub
		
	}

	public void onBookmarkMark(Bookmark b) {
		// TODO Auto-generated method stub
		
	}

	public void onBookmarkEdit(Bookmark b) {
		// TODO Auto-generated method stub
		
	}

	public void onBookmarkDelete(Bookmark b) {
		// TODO Auto-generated method stub
		
	}

	public void onTagSelected(String tag) {
		// TODO Auto-generated method stub
		
	}

	public void onNoteView(Note n) {
		// TODO Auto-generated method stub
		
	}
}