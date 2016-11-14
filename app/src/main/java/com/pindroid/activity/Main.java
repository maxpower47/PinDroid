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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.pindroid.Constants;
import com.pindroid.Constants.BookmarkViewType;
import com.pindroid.R;
import com.pindroid.action.IntentHelper;
import com.pindroid.fragment.BookmarkBrowser;
import com.pindroid.fragment.BrowseBookmarkFeedFragment;
import com.pindroid.fragment.BrowseBookmarksFragment;
import com.pindroid.fragment.BrowseBookmarksFragment.OnBookmarkSelectedListener;
import com.pindroid.fragment.BrowseNotesFragment;
import com.pindroid.fragment.BrowseNotesFragment.OnNoteSelectedListener;
import com.pindroid.fragment.BrowseTagsFragment;
import com.pindroid.fragment.BrowseTagsFragment.OnTagSelectedListener;
import com.pindroid.fragment.MainSearchResultsFragment;
import com.pindroid.fragment.MainSearchResultsFragment.OnSearchActionListener;
import com.pindroid.fragment.PindroidFragment;
import com.pindroid.fragment.ViewBookmarkFragment;
import com.pindroid.fragment.ViewBookmarkFragment.OnBookmarkActionListener;
import com.pindroid.fragment.ViewNoteFragment;
import com.pindroid.platform.BookmarkManager;
import com.pindroid.platform.NoteManager;
import com.pindroid.platform.TagManager;
import com.pindroid.providers.BookmarkContent.Bookmark;
import com.pindroid.providers.ContentNotFoundException;
import com.pindroid.providers.NoteContent.Note;
import com.pindroid.providers.TagContent;
import com.pindroid.ui.NsMenuAdapter;
import com.pindroid.ui.NsMenuItemModel;
import com.pindroid.ui.ResizeAnimation;
import com.pindroid.util.AccountHelper;
import com.pindroid.util.SettingsHelper;
import com.pindroid.util.StringUtils;

public class Main extends FragmentBaseActivity implements OnBookmarkSelectedListener, 
		OnTagSelectedListener, OnNoteSelectedListener, OnBookmarkActionListener, OnSearchActionListener, LoaderManager.LoaderCallbacks<Cursor> {
	
	private ListView mDrawerList;
	private LinearLayout mDrawerWrapper;
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    LinearLayout accountSpinnerView;
    ImageView accountSpinnerButton;
    boolean accountSpinnerOpen = false;
    LinearLayout accountList;
    TextView accountSelected;
    
    private NsMenuItemModel unreadItem;

    private Cursor tagData = null;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(prefListner);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
		
		mDrawerList = (ListView) findViewById(R.id.left_drawer_list);
		mDrawerWrapper = (LinearLayout) findViewById(R.id.left_drawer);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mTitle = mDrawerTitle = getTitle();
        accountSpinnerView = (LinearLayout) getLayoutInflater().inflate(R.layout.menu_spinner, null);
        accountSpinnerButton = (ImageView) accountSpinnerView.findViewById(R.id.account_button);
        accountList = (LinearLayout) accountSpinnerView.findViewById(R.id.account_list);
        accountSelected = (TextView) accountSpinnerView.findViewById(R.id.account_selected);

        if(AccountHelper.getAccountCount(this) > 0){
            if(app.getUsername() == null || app.getUsername().equals("")) {
                setAccount(AccountHelper.getFirstAccount(this).name);
            } else setAccount(app.getUsername());
        }

		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.statusbar_background));
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
        getSupportLoaderManager().initLoader(0, null, this);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
		
		if (savedInstanceState == null) {
            onMyBookmarksSelected(null);
        }

        processIntent(getIntent());
	}

    @Override
    public void onDestroy() {
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(prefListner);
        super.onDestroy();
    }
	
	private void _initMenu() {
		// Set up menu
		NsMenuAdapter mAdapter = new NsMenuAdapter(this);

		String[] myMenuItems = getResources().getStringArray(R.array.main_menu_my);
		String[] myMenuItemsIcon = getResources().getStringArray(R.array.main_menu_my_icons);

		String[] feedMenuItems = getResources().getStringArray(R.array.main_menu_feeds);
        String[] feedsMenuItemsIcon = getResources().getStringArray(R.array.main_menu_feeds_icons);

		for (int res = 0; res < myMenuItems.length; res++) {

			int id_title = getResources().getIdentifier(myMenuItems[res], "string", this.getPackageName());
			int id_icon = getResources().getIdentifier(myMenuItemsIcon[res], "drawable", this.getPackageName());

			NsMenuItemModel mItem = new NsMenuItemModel(id_title, id_icon);
			switch (res) {
				case 0:
					mItem.counter = BookmarkManager.GetAllBookmarksCount(app.getUsername(), this);
					break;
				case 1:
					unreadItem = mItem;
					mItem.counter = BookmarkManager.GetUnreadCount(app.getUsername(), this);
					break;
				case 2:
					mItem.counter = BookmarkManager.GetUntaggedCount(app.getUsername(), this);
					break;
				default:
					break;
			}
			mAdapter.addItem(mItem);
		}
		
		mAdapter.addHeader(R.string.main_menu_feeds_header);

		for (int res = 0; res < feedMenuItems.length; res++) {

			int id_title = getResources().getIdentifier(feedMenuItems[res], "string", this.getPackageName());
			int id_icon = getResources().getIdentifier(feedsMenuItemsIcon[res], "drawable", this.getPackageName());

			NsMenuItemModel mItem = new NsMenuItemModel(id_title, id_icon);
			mAdapter.addItem(mItem);
		}

        mAdapter.addHeader(R.string.main_menu_tags_header);

        int res = 0;

        if(tagData != null) {
            Set<String> tags = SettingsHelper.getDrawerTags(this);

            while (tagData.moveToNext()) {

                if(tags.size() == 0 || tags.contains(tagData.getString(1))) {
                    NsMenuItemModel mItem = new NsMenuItemModel(tagData.getString(1), R.drawable.main_menu_tag, false, tagData.getInt(2));
                    mAdapter.addItem(mItem);
                    res++;
                }
            }
        }

        if(mDrawerList.getHeaderViewsCount() < 1) {
            mDrawerList.addHeaderView(accountSpinnerView, null, false);
        }

		if (mDrawerList != null) {
            mDrawerList.setAdapter(mAdapter);
        }
		 
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
	}
	
	@Override
	public void onNewIntent(Intent intent){
		setIntent(intent);
		processIntent(intent);
	}
	
	private void processIntent(Intent intent){
        String action = intent.getAction();
		String path = intent.getData() != null ? intent.getData().getPath() : "";
		String lastPath = (intent.getData() != null && intent.getData().getLastPathSegment() != null) ? intent.getData().getLastPathSegment() : "";
		String intentUsername = (intent.getData() != null && intent.getData().getUserInfo() != null) ? intent.getData().getUserInfo() : "";
		
		if(!intentUsername.equals("") && !app.getUsername().equals(intentUsername)){
			setAccount(intentUsername);
		}
		
		if(Intent.ACTION_VIEW.equals(action)) {
			if(lastPath.equals("bookmarks")){
				if(intent.getData().getQueryParameter("unread") != null && intent.getData().getQueryParameter("unread").equals("1")){
					onMyUnreadSelected();
				} else{
					onMyBookmarksSelected(intent.getData().getQueryParameter("tagname"));
				}	
			} else if(lastPath.equals("notes")){
					onMyNotesSelected();
			}
		} else if(Intent.ACTION_SEND.equals(action)){
			Bookmark b = loadBookmarkFromShareIntent();
			b = findExistingBookmark(b);
			onBookmarkAdd(b);
		} else if(Constants.ACTION_SEARCH_SUGGESTION_VIEW.equals(action)){
			if(path.contains("bookmarks") && TextUtils.isDigitsOnly(lastPath) && intent.hasExtra(SearchManager.USER_QUERY)) {
				try {
					String defaultAction = SettingsHelper.getDefaultAction(this);
					BookmarkViewType viewType = null;
					try{
						viewType = BookmarkViewType.valueOf(defaultAction.toUpperCase(Locale.US));
					} catch(Exception e){
						viewType = BookmarkViewType.VIEW;
					}
					
					onBookmarkSelected(BookmarkManager.GetById(Integer.parseInt(lastPath), this), viewType);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (ContentNotFoundException e) {
					e.printStackTrace();
				}
			} else if(path.contains("bookmarks") && intent.hasExtra(SearchManager.USER_QUERY)){
				if(intent.getData() != null && intent.getData().getQueryParameter("tagname") != null)
				onTagSelected(intent.getData().getQueryParameter("tagname"), true);
			} else if(path.contains("notes") && TextUtils.isDigitsOnly(lastPath) && intent.hasExtra(SearchManager.USER_QUERY)){
				try {
					onNoteView(NoteManager.GetById(Integer.parseInt(lastPath), this));
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (ContentNotFoundException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(position <= 9) {
                switch(position){
                    case 1:
                        onMyBookmarksSelected(null);
                        break;
                    case 2:
                        onMyUnreadSelected();
                        break;
					case 3:
						onMyUntaggedSelected();
						break;
                    case 4:
                        onMyNotesSelected();
                        break;
                    case 5:
                        onSettingsSelected();
                        break;
                    case 7:
                        onPopularSelected();
                        break;
                    case 8:
                        onRecentSelected();
                        break;
                    case 9:
                        onMyNetworkSelected();
                        break;
                }
            } else {
                String tag = ((TextView) view.findViewById(R.id.menurow_title)).getText().toString();
                clearBackStack();
                onTagSelected(tag, false);
                clearDrawer(position);
            }
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
        
        if(drawerOpen){
            menu.clear();
        }
        
        return super.onPrepareOptionsMenu(menu);
    }
	
	private void replaceLeftFragment(Fragment frag, boolean backstack){
		// Insert the fragment by replacing any existing fragment
	    FragmentManager fragmentManager = getSupportFragmentManager();
	    FragmentTransaction t = fragmentManager.beginTransaction();
	    t.replace(R.id.left_frame, frag, "left");
	    if(backstack) {
            t.addToBackStack(null);
        }
	    t.commitAllowingStateLoss();
	    
	    clearRightFragment();
	}
	
	private void clearRightFragment(){
		// Insert the fragment by replacing any existing fragment
	    FragmentManager fragmentManager = getSupportFragmentManager();
	    Fragment f = fragmentManager.findFragmentByTag("right");
	    
	    if(f != null){
		    FragmentTransaction t = fragmentManager.beginTransaction();
		    t.remove(f);
            t.commitAllowingStateLoss();
	    }
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

	public void onMyBookmarksSelected(String tagname) {
		BrowseBookmarksFragment frag = new BrowseBookmarksFragment();
		frag.setQuery(app.getUsername(), tagname, null);
		
		clearBackStack();

		replaceLeftFragment(frag, false);

		clearDrawer(1);
	}

	public void onMyUnreadSelected() {
		BrowseBookmarksFragment frag = new BrowseBookmarksFragment();
		frag.setQuery(app.getUsername(), null, "unread");
		
		clearBackStack();

		replaceLeftFragment(frag, false);
		
		clearDrawer(2);
	}

	public void onMyUntaggedSelected() {
		BrowseBookmarksFragment frag = new BrowseBookmarksFragment();
		frag.setQuery(app.getUsername(), null, "untagged");

		clearBackStack();

		replaceLeftFragment(frag, false);

		clearDrawer(3);
	}
	
	public void onMyNotesSelected() {
		BrowseNotesFragment frag = new BrowseNotesFragment();
		frag.setUsername(app.getUsername());
		
		clearBackStack();

		replaceLeftFragment(frag, false);
		
		clearDrawer(4);
	}

    public void onSettingsSelected() {
        clearDrawer(5);
        Intent prefs = new Intent(this, Settings.class);
        startActivity(prefs);
    }

	public void onRecentSelected() {
		BrowseBookmarkFeedFragment frag = new BrowseBookmarkFeedFragment();
		frag.setQuery(app.getUsername(), null, "recent");
		
		clearBackStack();

		replaceLeftFragment(frag, false);
		
		clearDrawer(8);
	}
	
	public void onPopularSelected() {
		BrowseBookmarkFeedFragment frag = new BrowseBookmarkFeedFragment();
		frag.setQuery(app.getUsername(), null, "popular");
		
		clearBackStack();

		replaceLeftFragment(frag, false);
		
		clearDrawer(7);
	}
	
	public void onMyNetworkSelected() {
		BrowseBookmarkFeedFragment frag = new BrowseBookmarkFeedFragment();
		frag.setQuery(app.getUsername(), null, "network");
		
		clearBackStack();

		replaceLeftFragment(frag, false);
		
		clearDrawer(9);
	}
	
	@Override
	protected void changeAccount(){
        // reset account picker
        if(accountSpinnerOpen) {
            toggleAccountSpinner(false);
        }

        clearDrawer(1);

        accountSelected.setText(app.getUsername());

        accountList.removeAllViews();

        final List<String> accounts = getAccountNames();
        accounts.remove(app.getUsername());

        if(accounts.size() > 0) {

            accountSpinnerButton.setVisibility(View.VISIBLE);

            for (String account : accounts) {
                View accountView = getLayoutInflater().inflate(R.layout.account_list_view, null);
                ((TextView) accountView.findViewById(R.id.account_title)).setText(account);

                accountView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setAccount(((TextView) v.findViewById(R.id.account_title)).getText().toString());
                    }
                });

                accountList.addView(accountView);
            }

            accountSpinnerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleAccountSpinner(true);
                }
            });
        }

        // reset tags in drawer
        getSupportLoaderManager().restartLoader(0, null, this);

        if(unreadItem != null){
			unreadItem.counter = BookmarkManager.GetUnreadCount(app.getUsername(), this);
            ((NsMenuAdapter)((HeaderViewListAdapter)mDrawerList.getAdapter()).getWrappedAdapter()).notifyDataSetChanged();
		}

        // reset current fragments
		Fragment cf = getSupportFragmentManager().findFragmentById(R.id.right_frame);
		Fragment lf = getSupportFragmentManager().findFragmentById(R.id.left_frame);
		
		if(cf != null){
			((PindroidFragment)cf).setUsername(app.getUsername());
			((PindroidFragment)cf).refresh();
		}
		
		if(lf != null){
			((PindroidFragment)lf).setUsername(app.getUsername());
			((PindroidFragment)lf).refresh();
		}
	}

    private void toggleAccountSpinner(Boolean animate) {
        accountSpinnerButton.setImageResource(accountSpinnerOpen ? R.drawable.ic_arrow_drop_down : R.drawable.ic_arrow_drop_up);

        if(animate) {
            ResizeAnimation animation;

            if (accountSpinnerOpen) {
                animation = new ResizeAnimation(accountList, accountList.getChildCount() * (int) getResources().getDimension(R.dimen.account_list_height), 0, true, getResources().getDisplayMetrics());
            } else {
                animation = new ResizeAnimation(accountList, 0, accountList.getChildCount() * (int) getResources().getDimension(R.dimen.account_list_height), true, getResources().getDisplayMetrics());
            }

            animation.setDuration(200);
            accountList.startAnimation(animation);
        } else {
            accountList.getLayoutParams().height = accountSpinnerOpen ? 0 : accountList.getChildCount() * (int) getResources().getDimension(R.dimen.account_list_height);
        }

        accountSpinnerOpen = !accountSpinnerOpen;
    }

	public void onBookmarkSelected(Bookmark b, BookmarkViewType viewType){
		
		if(BookmarkViewType.EDIT.equals(viewType)){
            onBookmarkAdd(b, b);
		} else if(BookmarkViewType.WEB.equals(viewType) && SettingsHelper.getUseBrowser(this)) {
            startActivity(IntentHelper.OpenInBrowser(b.getUrl()));
        } else {
            ViewBookmarkFragment frag = new ViewBookmarkFragment();
            frag.setBookmark(b, viewType);

            FragmentManager fragmentManager = getSupportFragmentManager();

            if(isTwoPane()){
                FragmentTransaction t = fragmentManager.beginTransaction();

                if(fragmentManager.findFragmentByTag("right") instanceof ViewBookmarkFragment){
                    ViewBookmarkFragment viewFrag = (ViewBookmarkFragment) fragmentManager.findFragmentByTag("right");
                    viewFrag.setBookmark(b, viewType);
                    viewFrag.refresh();
                } else {
                    t.replace(R.id.right_frame, frag, "right");
                    t.commitAllowingStateLoss();
                }
            } else {
                if(fragmentManager.findFragmentByTag("left") instanceof ViewBookmarkFragment){
                    ViewBookmarkFragment viewFrag = (ViewBookmarkFragment) fragmentManager.findFragmentByTag("left");
                    viewFrag.setBookmark(b, viewType);
                    viewFrag.refresh();
                } else replaceLeftFragment(frag, true);
            }

        }
	}

	public void onBookmarkAdd(Bookmark b) {
        onBookmarkAdd(b, null);
	}

    public void onBookmarkAdd(Bookmark b, Bookmark oldBookmark) {
        Intent intent = new Intent(this, AddBookmark.class);
        intent.putExtra("bookmark", b);

        if(oldBookmark != null) {
            intent.putExtra("oldBookmark", oldBookmark);
        }

        intent.putExtra("username", app.getUsername());
        startActivity(intent);
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

	public void onBookmarkDelete(Bookmark b) {
		BookmarkManager.LazyDelete(b, app.getUsername(), this);
	}

    public void onTagSelected(String tag) {
        onTagSelected(tag, true);
    }

	public void onTagSelected(String tag, boolean backstack) {
		BrowseBookmarksFragment frag = new BrowseBookmarksFragment();
		frag.setQuery(app.getUsername(), tag, null);

		replaceLeftFragment(frag, backstack);
	}

	public void onNoteView(Note n) {
		ViewNoteFragment frag = new ViewNoteFragment();
		frag.setNote(n);
		
		FragmentManager fragmentManager = getSupportFragmentManager();
		
		if(isTwoPane()){
			FragmentTransaction t = fragmentManager.beginTransaction();
			
			if(fragmentManager.findFragmentByTag("right") instanceof ViewNoteFragment){
				ViewNoteFragment viewFrag = (ViewNoteFragment) fragmentManager.findFragmentByTag("right");
				viewFrag.setNote(n);
				viewFrag.refresh();
			} else {
				t.replace(R.id.right_frame, frag, "right");
                t.commitAllowingStateLoss();
			}
		} else {
			if(fragmentManager.findFragmentByTag("left") instanceof ViewNoteFragment){
				ViewNoteFragment viewFrag = (ViewNoteFragment) fragmentManager.findFragmentByTag("left");
				viewFrag.setNote(n);
				viewFrag.refresh();
			} else replaceLeftFragment(frag, true);
		}
	}

	public void onViewTagSelected(String tag, String user) {
		Fragment frag = null;
		
		if(user.equals(app.getUsername())){
			frag = new BrowseBookmarksFragment();
		} else frag = new BrowseBookmarkFeedFragment();
		
		((BookmarkBrowser)frag).setQuery(app.getUsername(), tag, user);

		replaceLeftFragment(frag, true);
	}

	public void onAccountSelected(String account) {
		BrowseBookmarkFeedFragment frag = new BrowseBookmarkFeedFragment();
		frag.setQuery(app.getUsername(), null, account);

		replaceLeftFragment(frag, true);
	}
	
	private Bookmark loadBookmarkFromShareIntent() {
		Bookmark bookmark = new Bookmark();
		
		ShareCompat.IntentReader reader = ShareCompat.IntentReader.from(this);
		
		if(reader != null){
			if(reader.getText() != null){
				String url = StringUtils.getUrl(reader.getText().toString());
				bookmark.setUrl(url);
			}
			
			if(reader.getSubject() != null)
				bookmark.setDescription(reader.getSubject());
		}
		
		bookmark.setToRead(SettingsHelper.getToReadDefault(this));
		bookmark.setShared(!SettingsHelper.getPrivateDefault(this));
		
		return bookmark;
	}
	
	private Bookmark findExistingBookmark(Bookmark bookmark) {

		try{
			Bookmark old = BookmarkManager.GetByUrl(bookmark.getUrl(), app.getUsername(), this);
			bookmark = old.copy();
		} catch(Exception e) {
		}

		return bookmark;
	}
	
	protected void startSearch(final String query) {
		MainSearchResultsFragment frag = new MainSearchResultsFragment();
		frag.setQuery(query);
		replaceLeftFragment(frag, true);
	}
	
	@Override
	public void setTitle(CharSequence title){
		super.setTitle(title);
		mTitle = title;
		getSupportActionBar().setTitle(title);
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
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
     
    private List<String> getAccountNames(){
    	
    	List<String> accountNames = new ArrayList<String>();
    	
    	for(Account account : AccountManager.get(this).getAccountsByType(Constants.ACCOUNT_TYPE)) {
    		accountNames.add(account.name);
    	}
    	
    	return accountNames;
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.base_menu, menu);
	    
	    setupSearch(menu);
	    
	    return true;
	}

	public void onBookmarkSearch(String query) {
		BrowseBookmarksFragment frag = new BrowseBookmarksFragment();
		frag.setSearchQuery(query, app.getUsername(), null, false);

		replaceLeftFragment(frag, true);
	}

	public void onTagSearch(String query) {
		BrowseTagsFragment frag = new BrowseTagsFragment();
		frag.setUsername(app.getUsername());
		frag.setQuery(query);

		replaceLeftFragment(frag, true);
	}

	public void onNoteSearch(String query) {
		BrowseNotesFragment frag = new BrowseNotesFragment();
		frag.setUsername(app.getUsername());
		frag.setQuery(query);

		replaceLeftFragment(frag, true);
	}

	public void onGlobalTagSearch(String query) {
		BrowseBookmarkFeedFragment frag = new BrowseBookmarkFeedFragment();
		frag.setQuery(app.getUsername(), query, "global");

		replaceLeftFragment(frag, true);
	}

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return TagManager.GetTags(app.getUsername(), TagContent.Tag.Name + " ASC", this);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(tagData != null) {
            tagData.close();
        }

        tagData = data;
        _initMenu();
    }

    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void onBackPressed() {
        if(mDrawerLayout.isDrawerOpen(mDrawerWrapper)) {
            mDrawerLayout.closeDrawer(mDrawerWrapper);
        } else {
            super.onBackPressed();
        }
    }

    SharedPreferences.OnSharedPreferenceChangeListener prefListner = new SharedPreferences.OnSharedPreferenceChangeListener(){
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            if(key.equals(getApplicationContext().getResources().getString(R.string.pref_drawertags_key))) {
                getSupportLoaderManager().restartLoader(0, null, Main.this);
            }
        }
    };
}
