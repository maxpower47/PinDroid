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
import com.pindroid.action.IntentHelper;
import com.pindroid.fragment.AddBookmarkFragment;
import com.pindroid.fragment.AddBookmarkFragment.OnBookmarkSaveListener;
import com.pindroid.fragment.BrowseBookmarkFeedFragment;
import com.pindroid.fragment.BrowseBookmarksFragment;
import com.pindroid.fragment.ViewBookmarkFragment;
import com.pindroid.fragment.BrowseBookmarksFragment.OnBookmarkSelectedListener;
import com.pindroid.fragment.BrowseNotesFragment;
import com.pindroid.fragment.BrowseNotesFragment.OnNoteSelectedListener;
import com.pindroid.fragment.BrowseTagsFragment;
import com.pindroid.fragment.BrowseTagsFragment.OnTagSelectedListener;
import com.pindroid.fragment.MainFragment;
import com.pindroid.fragment.ViewBookmarkFragment.OnBookmarkActionListener;
import com.pindroid.fragment.ViewNoteFragment;
import com.pindroid.platform.BookmarkManager;
import com.pindroid.providers.BookmarkContent.Bookmark;
import com.pindroid.providers.NoteContent.Note;
import com.pindroid.fragment.PindroidFragment;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class Main extends FragmentBaseActivity implements MainFragment.OnMainActionListener, OnBookmarkSelectedListener, 
		OnTagSelectedListener, OnNoteSelectedListener, OnBookmarkActionListener, OnBookmarkSaveListener {
	
	private ListView mDrawerList;
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private CharSequence mDrawerTitle;
    private CharSequence mTitle;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedState);
		setContentView(R.layout.main);
		
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mTitle = mDrawerTitle = getTitle();
		
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		
		String[] MENU_ITEMS = new String[] {getString(R.string.main_menu_my_bookmarks),
				getString(R.string.main_menu_my_unread_bookmarks),
				getString(R.string.main_menu_my_tags),
				getString(R.string.main_menu_my_notes),
				getString(R.string.main_menu_recent_bookmarks),
				getString(R.string.main_menu_popular_bookmarks),
				getString(R.string.main_menu_network_bookmarks)};
		
		mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.main_view, MENU_ITEMS));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
		
        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
                ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
		
		if (savedInstanceState == null) {
            selectItem(0);
        }
	}
	
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
	}

	/** Swaps fragments in the main content view */
	private void selectItem(int position) {
		if(position == 0){
			onMyBookmarksSelected();
    	} else if(position == 1){
    		onMyUnreadSelected();
    	} else if(position == 2){
    		onMyTagsSelected();
     	} else if(position == 3){
     		onMyNotesSelected();
      	} else if(position == 4){
      		onRecentSelected();
      	} else if(position == 5){
      		onPopularSelected();
      	} else if(position == 6){
      		onMyNetworkSelected();
      	}
	}
	
	private void replaceMainFragment(Fragment frag, int position){
		// Insert the fragment by replacing any existing fragment
	    FragmentManager fragmentManager = getSupportFragmentManager();
	    fragmentManager.beginTransaction()
	                   .replace(R.id.content_frame, frag)
	                   .commit();

	    // Highlight the selected item, update the title, and close the drawer
	    mDrawerList.setItemChecked(position, true);
	    mDrawerLayout.closeDrawer(mDrawerList);
	}
	
	private void replaceSubFragment(Fragment frag){
		// Insert the fragment by replacing any existing fragment
	    FragmentManager fragmentManager = getSupportFragmentManager();
	    fragmentManager.beginTransaction()
	                   .replace(R.id.content_frame, frag)
	                   .addToBackStack(null)
	                   .commit();
	}

	public void onMyBookmarksSelected() {
		BrowseBookmarksFragment frag = new BrowseBookmarksFragment();
		frag.setQuery(app.getUsername(), null, null);
		replaceMainFragment(frag, 0);
	}

	public void onMyUnreadSelected() {
		BrowseBookmarksFragment frag = new BrowseBookmarksFragment();
		frag.setQuery(app.getUsername(), null, "unread");
		replaceMainFragment(frag, 1);
	}

	public void onMyTagsSelected() {
		BrowseTagsFragment frag = new BrowseTagsFragment();
		frag.setUsername(app.getUsername());
		replaceMainFragment(frag, 2);
	}
	
	public void onMyNotesSelected() {
		BrowseNotesFragment frag = new BrowseNotesFragment();
		frag.setUsername(app.getUsername());
		replaceMainFragment(frag, 3);
	}

	public void onRecentSelected() {
		BrowseBookmarkFeedFragment frag = new BrowseBookmarkFeedFragment();
		frag.setQuery(app.getUsername(), null, "recent");
		replaceMainFragment(frag, 4);
	}
	
	public void onPopularSelected() {
		BrowseBookmarkFeedFragment frag = new BrowseBookmarkFeedFragment();
		frag.setQuery(app.getUsername(), null, "popular");
		replaceMainFragment(frag, 5);
	}
	
	public void onMyNetworkSelected() {
		BrowseBookmarkFeedFragment frag = new BrowseBookmarkFeedFragment();
		frag.setQuery(app.getUsername(), null, "network");
		replaceMainFragment(frag, 6);
	}
	
	@Override
	protected void changeAccount(){
		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_frame);
		
		if(f != null){
			((PindroidFragment)f).setUsername(app.getUsername());
			((PindroidFragment)f).refresh();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);

	    setupSearch(menu);
	    return true;
	}

	public void onBookmarkView(Bookmark b) {
		ViewBookmarkFragment frag = new ViewBookmarkFragment();
		frag.setBookmark(b, BookmarkViewType.VIEW);
		replaceSubFragment(frag);
	}

	public void onBookmarkRead(Bookmark b) {
		ViewBookmarkFragment frag = new ViewBookmarkFragment();
		frag.setBookmark(b, BookmarkViewType.READ);
		replaceSubFragment(frag);
	}

	public void onBookmarkOpen(Bookmark b) {
		ViewBookmarkFragment frag = new ViewBookmarkFragment();
		frag.setBookmark(b, BookmarkViewType.WEB);
		replaceSubFragment(frag);
	}

	public void onBookmarkAdd(Bookmark b) {
		AddBookmarkFragment frag = new AddBookmarkFragment();
		frag.loadBookmark(b, null);
		frag.setUsername(app.getUsername());
		replaceSubFragment(frag);
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
		AddBookmarkFragment frag = new AddBookmarkFragment();
		frag.loadBookmark(b, b);
		frag.setUsername(app.getUsername());
		replaceSubFragment(frag);
	}

	public void onBookmarkDelete(Bookmark b) {
		BookmarkManager.LazyDelete(b, app.getUsername(), this);
	}

	public void onTagSelected(String tag) {
		BrowseBookmarksFragment frag = new BrowseBookmarksFragment();
		frag.setQuery(app.getUsername(), tag, null);
		replaceSubFragment(frag);
	}

	public void onNoteView(Note n) {
		ViewNoteFragment frag = new ViewNoteFragment();
		frag.setNote(n);
		replaceSubFragment(frag);
	}

	public void onViewTagSelected(String tag, String user) {
		BrowseBookmarksFragment frag = new BrowseBookmarksFragment();
		frag.setQuery(app.getUsername(), tag, user);
		replaceSubFragment(frag);
	}

	public void onAccountSelected(String account) {
		BrowseBookmarkFeedFragment frag = new BrowseBookmarkFeedFragment();
		frag.setQuery(app.getUsername(), null, account);
		replaceSubFragment(frag);
	}

	public void onBookmarkSave(Bookmark b) {
		getSupportFragmentManager().popBackStack();
	}

	public void onBookmarkCancel(Bookmark b) {
		getSupportFragmentManager().popBackStack();
	}
	
	@Override
	public void setTitle(CharSequence title){
		super.setTitle(title);
		mTitle = title;

		if(this.findViewById(R.id.action_bar_title) != null) {
			((TextView)this.findViewById(R.id.action_bar_title)).setText(title);
		}
	}
	
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
}