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

import java.util.ArrayList;
import java.util.List;

import com.pindroid.Constants.BookmarkViewType;
import com.pindroid.Constants;
import com.pindroid.R;
import com.pindroid.action.IntentHelper;
import com.pindroid.fragment.AddBookmarkFragment;
import com.pindroid.fragment.AddBookmarkFragment.OnBookmarkSaveListener;
import com.pindroid.fragment.BookmarkBrowser;
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
import com.pindroid.providers.ContentNotFoundException;
import com.pindroid.providers.BookmarkContent.Bookmark;
import com.pindroid.providers.NoteContent.Note;
import com.pindroid.util.SettingsHelper;
import com.pindroid.util.StringUtils;
import com.pindroid.fragment.PindroidFragment;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class Main extends FragmentBaseActivity implements MainFragment.OnMainActionListener, OnBookmarkSelectedListener, 
		OnTagSelectedListener, OnNoteSelectedListener, OnBookmarkActionListener, OnBookmarkSaveListener {
	
	private ListView mDrawerList;
	private LinearLayout mDrawerWrapper;
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private Spinner mAccountSpinner;
    private int spinnerSelectionCount = 0;

	@Override
	public void onCreate(Bundle savedInstanceState){
		Log.d("main", "onCreateStart");
		
		super.onCreate(savedState);
		setContentView(R.layout.main);

		
		mDrawerList = (ListView) findViewById(R.id.left_drawer_list);
		mDrawerWrapper = (LinearLayout) findViewById(R.id.left_drawer);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mTitle = mDrawerTitle = getTitle();
		mAccountSpinner = (Spinner) findViewById(R.id.account_spinner);
		
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

		
		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, getAccountNames());
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		mAccountSpinner.setAdapter(adapter);
		
		if(app.getUsername() == null || app.getUsername().equals("")) {
			Log.d("spinnerDefaultAccount", (String)mAccountSpinner.getSelectedItem());
			setAccount((String)mAccountSpinner.getSelectedItem());
		}

		mAccountSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				
				// onItemSelected is called on initialization of the spinner, so prevent this from being called the first time
				if(spinnerSelectionCount > 0) {
					Log.d("spinnerOnItemSelected", (String)parent.getItemAtPosition(pos));
					setAccount((String)parent.getItemAtPosition(pos));
				}
				spinnerSelectionCount++;
			}

			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}		
		});

		
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
		
		
		processIntent(getIntent());
		
		Log.d("main", "onCreateEnd");
	}
	
	@Override
	public void onNewIntent(Intent intent){
		setIntent(intent);
		processIntent(intent);
	}
	
	private void processIntent(Intent intent){
		Log.d("processIntent", intent.getDataString() == null ? "" : intent.getDataString());
		Log.d("processIntent: currentUsername", app.getUsername());
		
		String action = intent.getAction();
		String lastPath = (intent.getData() != null && intent.getData().getLastPathSegment() != null) ? intent.getData().getLastPathSegment() : "";
		String intentUsername = (intent.getData() != null && intent.getData().getUserInfo() != null) ? intent.getData().getUserInfo() : "";
		
		if(!intentUsername.equals("") && !app.getUsername().equals(intentUsername)){
			setAccount(intentUsername);
			Log.d("processIntent: changeUsername", intentUsername);
		}
		
		if(Intent.ACTION_VIEW.equals(action)) {
			if(lastPath.equals("bookmarks")){
				if(intent.getData().getQueryParameter("unread") != null && intent.getData().getQueryParameter("unread").equals("1")){
					Log.d("processIntent", "unread");
					onMyUnreadSelected();
					
				} else{
					Log.d("processIntent", "bookmarks");
					onMyBookmarksSelected();
					
				}	
			} else if(lastPath.equals("tags")){
				Log.d("processIntent", "tags");
				onMyTagsSelected();
			}
		} else if(Intent.ACTION_SEND.equals(action)){
			onBookmarkAdd(null);
		}
	}
	
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
	}

	/** Swaps fragments in the main content view */
	private void selectItem(int position) {
		switch(position){
			case 0:
				onMyBookmarksSelected();
				break;
			case 1:
				onMyUnreadSelected();
				break;
			case 2:
				onMyTagsSelected();
				break;
			case 3:
				onMyNotesSelected();
				break;
			case 4:
				onRecentSelected();
				break;
			case 5:
				onPopularSelected();
				break;
			case 6:
				onMyNetworkSelected();
				break;
		}
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
          return true;
        }
	    switch (item.getItemId()) {
		    case R.id.menu_addbookmark:
		    	onBookmarkAdd(null);
				return true;
		    case R.id.menu_settings:
				Intent prefs = new Intent(this, Preferences.class);
				startActivity(prefs);
		        return true;
		    case R.id.menu_search:
		    	onSearchRequested();
		    	return true;
		    default:
		        return super.onOptionsItemSelected(item);
	    }
    }
	
    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerWrapper);
        return super.onPrepareOptionsMenu(menu);
    }
	
	private void replaceLeftFragment(Fragment frag, boolean backstack){
		// Insert the fragment by replacing any existing fragment
	    FragmentManager fragmentManager = getSupportFragmentManager();
	    FragmentTransaction t = fragmentManager.beginTransaction();
	    t.replace(R.id.list_frame, frag, "left");
	    if(backstack)
	    	t.addToBackStack(null);
	    t.commit();
	    
	    clearRightFragment();
	}
	
	private void clearRightFragment(){
		// Insert the fragment by replacing any existing fragment
	    FragmentManager fragmentManager = getSupportFragmentManager();
	    Fragment f = fragmentManager.findFragmentByTag("right");
	    
	    if(f != null){
		    FragmentTransaction t = fragmentManager.beginTransaction();
		    t.remove(f);
		    t.commit();
	    }
	}
	
	private void replaceRightFragment(Fragment frag, boolean backstack){
		// Insert the fragment by replacing any existing fragment
	    FragmentManager fragmentManager = getSupportFragmentManager();
	    FragmentTransaction t = fragmentManager.beginTransaction();
	    t.replace(R.id.content_frame, frag, "right");
	    if(backstack)
	    	t.addToBackStack(null);
	    t.commit();
	}
	
	private void clearDrawer(int position){		
		// Highlight the selected item, update the title, and close the drawer
	    mDrawerList.setItemChecked(position, true);
	    mDrawerLayout.closeDrawer(mDrawerWrapper);
	}
	
	private void clearBackStack(){
		if(getSupportFragmentManager().getBackStackEntryCount() > 0){
			int id = getSupportFragmentManager().getBackStackEntryAt(0).getId();
			getSupportFragmentManager().popBackStackImmediate(id, FragmentManager.POP_BACK_STACK_INCLUSIVE);
		}
	}
	
	private boolean isTwoPane(){
		return getResources().getBoolean(R.bool.has_two_panes);
	}

	public void onMyBookmarksSelected() {
		BrowseBookmarksFragment frag = new BrowseBookmarksFragment();
		frag.setQuery(app.getUsername(), null, null);
		
		clearBackStack();
		
		if(isTwoPane()){
			replaceLeftFragment(frag, false);
		} else {
			replaceRightFragment(frag, false);
		}
		
		clearDrawer(0);
	}

	public void onMyUnreadSelected() {
		BrowseBookmarksFragment frag = new BrowseBookmarksFragment();
		frag.setQuery(app.getUsername(), null, "unread");
		
		clearBackStack();
		
		if(isTwoPane()){
			replaceLeftFragment(frag, false);
		} else {
			replaceRightFragment(frag, false);
		}
		
		clearDrawer(1);
	}

	public void onMyTagsSelected() {
		BrowseTagsFragment frag = new BrowseTagsFragment();
		frag.setUsername(app.getUsername());
		
		clearBackStack();

		if(isTwoPane()){
			replaceLeftFragment(frag, false);
		} else {
			replaceRightFragment(frag, false);
		}
		
		clearDrawer(2);
	}
	
	public void onMyNotesSelected() {
		BrowseNotesFragment frag = new BrowseNotesFragment();
		frag.setUsername(app.getUsername());
		
		clearBackStack();

		if(isTwoPane()){
			replaceLeftFragment(frag, false);
		} else {
			replaceRightFragment(frag, false);
		}
		
		clearDrawer(3);
	}

	public void onRecentSelected() {
		BrowseBookmarkFeedFragment frag = new BrowseBookmarkFeedFragment();
		frag.setQuery(app.getUsername(), null, "recent");
		
		clearBackStack();

		if(isTwoPane()){
			replaceLeftFragment(frag, false);
		} else {
			replaceRightFragment(frag, false);
		}
		
		clearDrawer(4);
	}
	
	public void onPopularSelected() {
		BrowseBookmarkFeedFragment frag = new BrowseBookmarkFeedFragment();
		frag.setQuery(app.getUsername(), null, "popular");
		
		clearBackStack();

		if(isTwoPane()){
			replaceLeftFragment(frag, false);
		} else {
			replaceRightFragment(frag, false);
		}
		
		clearDrawer(5);
	}
	
	public void onMyNetworkSelected() {
		BrowseBookmarkFeedFragment frag = new BrowseBookmarkFeedFragment();
		frag.setQuery(app.getUsername(), null, "network");
		
		clearBackStack();

		if(isTwoPane()){
			replaceLeftFragment(frag, false);
		} else {
			replaceRightFragment(frag, false);
		}
		
		clearDrawer(6);
	}
	
	@Override
	protected void changeAccount(){

		int position = ((ArrayAdapter<CharSequence>)mAccountSpinner.getAdapter()).getPosition(app.getUsername());
		Log.d("spinnerSetSectionStart", Integer.toString(mAccountSpinner.getSelectedItemPosition()));
		mAccountSpinner.setSelection(position);
		Log.d("spinnerSetSection", Integer.toString(position));

		
		Fragment cf = getSupportFragmentManager().findFragmentById(R.id.content_frame);
		Fragment lf = getSupportFragmentManager().findFragmentById(R.id.list_frame);
		
		if(cf != null){
			((PindroidFragment)cf).setUsername(app.getUsername());
			((PindroidFragment)cf).refresh();
		}
		
		if(lf != null){
			((PindroidFragment)lf).setUsername(app.getUsername());
			((PindroidFragment)lf).refresh();
		}
	}
	
	public void onBookmarkSelected(Bookmark b, BookmarkViewType viewType){
		ViewBookmarkFragment frag = new ViewBookmarkFragment();
		frag.setBookmark(b, viewType);
		
		FragmentManager fragmentManager = getSupportFragmentManager();
		
		
		if(isTwoPane()){
			FragmentTransaction t = fragmentManager.beginTransaction();

			if(fragmentManager.findFragmentByTag("left") instanceof BrowseTagsFragment){
				Fragment right = fragmentManager.findFragmentByTag("right");
				Fragment newRight = duplicateFragment(right);
				
				t.replace(R.id.list_frame, newRight, "left");
				t.addToBackStack(null);
			}
			
			t.replace(R.id.content_frame, frag, "right");
			t.commit();
		} else {
			replaceRightFragment(frag, true);
		}
	}

	public void onBookmarkView(Bookmark b) {
		onBookmarkSelected(b, BookmarkViewType.VIEW);
	}

	public void onBookmarkRead(Bookmark b) {
		onBookmarkSelected(b, BookmarkViewType.READ);
	}

	public void onBookmarkOpen(Bookmark b) {
		onBookmarkSelected(b, BookmarkViewType.WEB);
	}

	public void onBookmarkAdd(Bookmark b) {
		AddBookmarkFragment frag = new AddBookmarkFragment();
		frag.loadBookmark(b, null);
		frag.setUsername(app.getUsername());
		replaceRightFragment(frag, true);
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
		replaceRightFragment(frag, true);
	}

	public void onBookmarkDelete(Bookmark b) {
		BookmarkManager.LazyDelete(b, app.getUsername(), this);
	}

	public void onTagSelected(String tag) {
		BrowseBookmarksFragment frag = new BrowseBookmarksFragment();
		frag.setQuery(app.getUsername(), tag, null);
		
		if(isTwoPane()){
			replaceRightFragment(frag, false);		
		} else {
			replaceRightFragment(frag, true);
		}
	}

	public void onNoteView(Note n) {
		ViewNoteFragment frag = new ViewNoteFragment();
		frag.setNote(n);
		replaceRightFragment(frag, true);
	}

	public void onViewTagSelected(String tag, String user) {
		Fragment frag = null;
		
		if(user.equals(app.getUsername())){
			frag = new BrowseBookmarksFragment();
		} else frag = new BrowseBookmarkFeedFragment();
		
		((BookmarkBrowser)frag).setQuery(app.getUsername(), tag, user);

		if(isTwoPane()){
			replaceLeftFragment(frag, true);		    
		} else {
			replaceRightFragment(frag, true);
		}
	}

	public void onAccountSelected(String account) {
		BrowseBookmarkFeedFragment frag = new BrowseBookmarkFeedFragment();
		frag.setQuery(app.getUsername(), null, account);

		if(isTwoPane()){
			replaceLeftFragment(frag, true);		    
		} else {
			replaceRightFragment(frag, true);
		}
	}

	public void onBookmarkSave(Bookmark b) {
		getSupportFragmentManager().popBackStack();
	}

	public void onBookmarkCancel(Bookmark b) {
		getSupportFragmentManager().popBackStack();
	}
	
	private Bookmark loadBookmarkFromShareIntent() {
		Bookmark bookmark = new Bookmark();
		
		ShareCompat.IntentReader reader = ShareCompat.IntentReader.from(this);
		
		String url = StringUtils.getUrl(reader.getText().toString());
		bookmark.setUrl(url);
		
		if(reader.getSubject() != null)
			bookmark.setDescription(reader.getSubject());
		
		bookmark.setToRead(SettingsHelper.getToReadDefault(this));
		bookmark.setShared(!SettingsHelper.getPrivateDefault(this));
		
		return bookmark;
	}
	
	private Bookmark findExistingBookmark() {
		Bookmark bookmark = new Bookmark();

		try{
			Bookmark old = BookmarkManager.GetByUrl(bookmark.getUrl(), app.getUsername(), this);
			bookmark = old.copy();
		} catch(ContentNotFoundException e) {
			return null;
		}

		return bookmark;
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
    
    private String[] getAccountNames(){
    	
    	List<String> accountNames = new ArrayList<String>();
    	
    	for(Account account : AccountManager.get(this).getAccountsByType(Constants.ACCOUNT_TYPE)) {
    		accountNames.add(account.name);
    	}
    	
    	return accountNames.toArray(new String[]{});
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.base_menu, menu);
	    
	    setupSearch(menu);
	    
	    return true;
	}
    
    private Fragment duplicateFragment(Fragment f)
    {
        try {
            Fragment.SavedState oldState = getSupportFragmentManager().saveFragmentInstanceState(f);

            Fragment newInstance = f.getClass().newInstance();
            newInstance.setInitialSavedState(oldState);

            return newInstance;
        }
        catch (Exception e) // InstantiationException, IllegalAccessException
        {

        }
        
        return null;
    }
}