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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.HeaderViewListAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.pindroid.Constants;
import com.pindroid.Constants.BookmarkViewType;
import com.pindroid.R;
import com.pindroid.action.IntentHelper;
import com.pindroid.authenticator.AuthenticatorActivity_;
import com.pindroid.event.AccountChangedEvent;
import com.pindroid.event.BookmarkDeletedEvent;
import com.pindroid.event.DrawerTagsChangedEvent;
import com.pindroid.fragment.BookmarkBrowser;
import com.pindroid.fragment.BrowseBookmarkFeedFragment;
import com.pindroid.fragment.BrowseBookmarkFeedFragment_;
import com.pindroid.fragment.BrowseBookmarksFragment;
import com.pindroid.fragment.BrowseBookmarksFragment.OnBookmarkSelectedListener;
import com.pindroid.fragment.BrowseBookmarksFragment_;
import com.pindroid.fragment.BrowseNotesFragment;
import com.pindroid.fragment.BrowseNotesFragment.OnNoteSelectedListener;
import com.pindroid.fragment.BrowseNotesFragment_;
import com.pindroid.fragment.BrowseTagsFragment;
import com.pindroid.fragment.BrowseTagsFragment.OnTagSelectedListener;
import com.pindroid.fragment.BrowseTagsFragment_;
import com.pindroid.fragment.MainSearchResultsFragment;
import com.pindroid.fragment.MainSearchResultsFragment.OnSearchActionListener;
import com.pindroid.fragment.ViewBookmarkFragment;
import com.pindroid.fragment.ViewBookmarkFragment.OnBookmarkActionListener;
import com.pindroid.fragment.ViewBookmarkFragment_;
import com.pindroid.fragment.ViewNoteFragment;
import com.pindroid.fragment.ViewNoteFragment_;
import com.pindroid.platform.BookmarkManager;
import com.pindroid.platform.NoteManager;
import com.pindroid.platform.TagManager;
import com.pindroid.model.Bookmark;
import com.pindroid.providers.ContentNotFoundException;
import com.pindroid.model.Note;
import com.pindroid.model.Tag;
import com.pindroid.ui.AccountSpinner;
import com.pindroid.ui.AccountSpinner_;
import com.pindroid.ui.NsMenuAdapter;
import com.pindroid.ui.NsMenuItemModel;
import com.pindroid.util.AccountHelper;
import com.pindroid.util.SettingsHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.Locale;
import java.util.Set;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

@EActivity(R.layout.main)
@OptionsMenu(R.menu.base_menu)
public class Main extends AppCompatActivity implements OnBookmarkSelectedListener,
		OnTagSelectedListener, OnNoteSelectedListener, OnBookmarkActionListener, OnSearchActionListener, LoaderManager.LoaderCallbacks<Cursor> {
	
	@ViewById(R.id.left_drawer_list) ListView mDrawerList;
	@ViewById(R.id.left_drawer) LinearLayout mDrawerWrapper;
	@ViewById(R.id.drawer_layout) DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    AccountSpinner accountSpinnerView;
    String username;

    protected AccountManager mAccountManager;
    
    private NsMenuItemModel unreadItem;

    private Cursor tagData = null;

    @InstanceState boolean savedState;

	@AfterViews
	public void init(){

        mAccountManager = AccountManager.get(this);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

		mTitle = mDrawerTitle = getTitle();
        accountSpinnerView = AccountSpinner_.build(this);

        if(AccountHelper.getAccountCount(this) > 0){
            if(EventBus.getDefault().getStickyEvent(AccountChangedEvent.class) == null) {
                EventBus.getDefault().postSticky(new AccountChangedEvent(AccountHelper.getFirstAccount(this).name));
            }
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

        if(!savedState) {
            savedState = true;
            onMyBookmarksSelected(null);
        }

        processIntent(getIntent());
	}

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        if(AccountHelper.getAccountCount(this) < 1) {
            Intent i = new Intent(this, AuthenticatorActivity_.class);
            startActivityForResult(i, Constants.REQUEST_CODE_ACCOUNT_INIT);
        }
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
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
			if (res == 1) {
				unreadItem = mItem;
				mItem.counter = BookmarkManager.GetUnreadCount(username, this);
			} else if (res == 2) {
				mItem.counter = BookmarkManager.GetUntaggedCount(username, this);
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
                    NsMenuItemModel mItem = new NsMenuItemModel(tagData.getString(1), R.drawable.ic_label_gray_24dp, false, tagData.getInt(2));
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
		
		if(!intentUsername.equals("")){
            EventBus.getDefault().postSticky(new AccountChangedEvent(intentUsername));
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
        return mDrawerToggle.onOptionsItemSelected(item);
    }

    @OptionsItem(R.id.menu_search)
    void onSearch() {
        onSearchRequested();
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
		BrowseBookmarksFragment frag = BrowseBookmarksFragment_.builder()
                .tagname(tagname)
                .build();

		clearBackStack();
		replaceLeftFragment(frag, false);
				clearDrawer(1);
	}

	public void onMyUnreadSelected() {
		BrowseBookmarksFragment frag = BrowseBookmarksFragment_.builder()
                .unread(true)
                .build();

		clearBackStack();
		replaceLeftFragment(frag, false);
		clearDrawer(2);
	}

	public void onMyUntaggedSelected() {
		BrowseBookmarksFragment frag = BrowseBookmarksFragment_.builder()
                .untagged(true)
                .build();

		clearBackStack();
		replaceLeftFragment(frag, false);
		clearDrawer(3);
	}
	
	public void onMyNotesSelected() {
		BrowseNotesFragment frag = BrowseNotesFragment_.builder()
                .build();

		clearBackStack();
		replaceLeftFragment(frag, false);
		clearDrawer(4);
	}

    public void onSettingsSelected() {
        clearDrawer(5);
        Settings_.intent(this).start();
    }

	public void onRecentSelected() {
		BrowseBookmarkFeedFragment frag = BrowseBookmarkFeedFragment_.builder()
                .feed("recent")
                .build();

		clearBackStack();
		replaceLeftFragment(frag, false);
		clearDrawer(8);
	}
	
	public void onPopularSelected() {
        BrowseBookmarkFeedFragment frag = BrowseBookmarkFeedFragment_.builder()
                .feed("popular")
                .build();
		
		clearBackStack();
		replaceLeftFragment(frag, false);
		clearDrawer(7);
	}
	
	public void onMyNetworkSelected() {
        BrowseBookmarkFeedFragment frag = BrowseBookmarkFeedFragment_.builder()
                .feed("network")
                .build();
		
		clearBackStack();
		replaceLeftFragment(frag, false);
		clearDrawer(9);
	}

    private void setSubtitle(String subtitle){
        getSupportActionBar().setSubtitle(subtitle);
    }

    @Subscribe(sticky = true)
	public void onAccountChanged(AccountChangedEvent event){
        username = event.getNewAccount();

        if(AccountHelper.getAccountCount(this) > 1) {
            setSubtitle(event.getNewAccount());
        }

        clearDrawer(1);

        // reset tags in drawer
        getSupportLoaderManager().restartLoader(0, null, this);

        if(unreadItem != null){
			unreadItem.counter = BookmarkManager.GetUnreadCount(event.getNewAccount(), this);
            ((NsMenuAdapter)((HeaderViewListAdapter)mDrawerList.getAdapter()).getWrappedAdapter()).notifyDataSetChanged();
		}
	}

	public void onBookmarkSelected(Bookmark b, BookmarkViewType viewType){
		
		if(BookmarkViewType.EDIT.equals(viewType)){
            onBookmarkAdd(b);
		} else if(BookmarkViewType.WEB.equals(viewType) && SettingsHelper.getUseBrowser(this)) {
            startActivity(IntentHelper.OpenInBrowser(b.getUrl()));
        } else {
            ViewBookmarkFragment frag = new ViewBookmarkFragment_();
            frag.setBookmark(b, viewType);

            FragmentManager fragmentManager = getSupportFragmentManager();

            if(isTwoPane()){
                FragmentTransaction t = fragmentManager.beginTransaction();

                if(fragmentManager.findFragmentByTag("right") instanceof ViewBookmarkFragment){
                    ViewBookmarkFragment viewFrag = (ViewBookmarkFragment_) fragmentManager.findFragmentByTag("right");
                    viewFrag.setBookmark(b, viewType);
                    viewFrag.refresh();
                } else {
                    t.replace(R.id.right_frame, frag, "right");
                    t.commitAllowingStateLoss();
                }
            } else {
                if(fragmentManager.findFragmentByTag("left") instanceof ViewBookmarkFragment){
                    ViewBookmarkFragment viewFrag = (ViewBookmarkFragment_) fragmentManager.findFragmentByTag("left");
                    viewFrag.setBookmark(b, viewType);
                    viewFrag.refresh();
                } else replaceLeftFragment(frag, true);
            }
        }
	}

    public void onBookmarkAdd(Bookmark b) {
        AddBookmark_.intent(this)
            .bookmark(b)
            .username(username)
            .start();
    }

	public void onBookmarkShare(Bookmark b) {
		if(b != null){
			Intent sendIntent = IntentHelper.SendBookmark(b.getUrl(), b.getDescription());
			startActivity(Intent.createChooser(sendIntent, getString(R.string.share_chooser_title)));
		}
	}

	public void onBookmarkMark(Bookmark b) {
		if(b != null && isMyself()) {
    		b.toggleToRead();
			BookmarkManager.UpdateBookmark(b, this);
    	}
	}

    public void onBookmarkMarkRead(Bookmark b) {
        if(b != null && isMyself() && b.getToRead()) {
            b.setToRead(false);
            BookmarkManager.UpdateBookmark(b, this);
        }
    }

	public void onBookmarkDelete(Bookmark b) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        if((isTwoPane() && fragmentManager.findFragmentByTag("right") instanceof ViewBookmarkFragment) ||
                fragmentManager.findFragmentByTag("left") instanceof ViewBookmarkFragment){

            onBackPressed();
        }

        EventBus.getDefault().post(new BookmarkDeletedEvent(b));
        BookmarkManager.LazyDelete(b, username, this);
	}

    public void onTagSelected(String tag) {
        onTagSelected(tag, true);
    }

	public void onTagSelected(String tag, boolean backstack) {
		BrowseBookmarksFragment frag = BrowseBookmarksFragment_.builder()
                .tagname(tag)
                .build();

		replaceLeftFragment(frag, backstack);
	}

	public void onNoteView(Note n) {
		ViewNoteFragment frag = new ViewNoteFragment_();
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
		
		if(user.equals(username)){
			frag = new BrowseBookmarksFragment_();
		} else frag = new BrowseBookmarkFeedFragment_();
		
		((BookmarkBrowser)frag).setTag(tag, user);

		replaceLeftFragment(frag, true);
	}

	public void onAccountSelected(String account) {
        BrowseBookmarkFeedFragment frag = BrowseBookmarkFeedFragment_.builder()
                .feed(account)
                .build();

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
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(false);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            public boolean onQueryTextSubmit(String query) {
                MainSearchResultsFragment frag = new MainSearchResultsFragment();
                frag.setQuery(query);
                replaceLeftFragment(frag, true);
                return true;
            }

            public boolean onQueryTextChange(final String s) {
                return false;
            }
        });
        return true;
	}

	public void onBookmarkSearch(String query) {
		BrowseBookmarksFragment frag = BrowseBookmarksFragment_.builder()
                .query(query)
                .build();

		replaceLeftFragment(frag, true);
	}

	public void onTagSearch(String query) {
		BrowseTagsFragment frag = BrowseTagsFragment_.builder()
                .username(username)
                .query(query)
                .build();

		replaceLeftFragment(frag, true);
	}

	public void onNoteSearch(String query) {
		BrowseNotesFragment frag = BrowseNotesFragment_.builder()
                .query(query)
                .build();

		replaceLeftFragment(frag, true);
	}

	public void onGlobalTagSearch(String query) {
        BrowseBookmarkFeedFragment frag = BrowseBookmarkFeedFragment_.builder()
                .tagname(query)
                .feed("global")
                .build();

		replaceLeftFragment(frag, true);
	}

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return TagManager.GetTags(username, Tag.Name + " ASC", this);
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

    @Subscribe
    public void onDrawerTagsChanged(DrawerTagsChangedEvent event) {
        getSupportLoaderManager().restartLoader(0, null, Main.this);
    }

    public boolean isMyself() {
        for(Account account : mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE)){
            if(username.equals(account.name))
                return true;
        }

        return false;
    }

    @OnActivityResult(Constants.REQUEST_CODE_ACCOUNT_CHANGE)
    void onResult(int result, @OnActivityResult.Extra(value = AccountManager.KEY_ACCOUNT_NAME) String account) {
        if(result == Activity.RESULT_OK) {
            EventBus.getDefault().postSticky(new AccountChangedEvent(account));
        }
    }

    @OnActivityResult(Constants.REQUEST_CODE_ACCOUNT_INIT)
    void onResult(int result) {
        if(result == Activity.RESULT_OK) {
            EventBus.getDefault().postSticky(new AccountChangedEvent(AccountHelper.getFirstAccount(this).name));
        } else {
            finish();
        }
    }
}
