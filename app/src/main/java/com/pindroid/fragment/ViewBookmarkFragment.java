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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.pindroid.Constants;
import com.pindroid.Constants.BookmarkViewType;
import com.pindroid.R;
import com.pindroid.action.IntentHelper;
import com.pindroid.event.AccountChangedEvent;
import com.pindroid.fragment.BrowseBookmarksFragment.OnBookmarkSelectedListener;
import com.pindroid.platform.BookmarkManager;
import com.pindroid.providers.BookmarkContent.Bookmark;
import com.pindroid.providers.BookmarkContentProvider;
import com.pindroid.providers.ContentNotFoundException;
import com.pindroid.ui.AccountSpan;
import com.pindroid.ui.ColorGenerator;
import com.pindroid.ui.TagView;
import com.pindroid.util.SettingsHelper;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import de.greenrobot.event.EventBus;

@EFragment(R.layout.view_bookmark_fragment)
public class ViewBookmarkFragment extends Fragment {
	
	@ViewById(R.id.bookmark_scroll_view) ScrollView mBookmarkView;
	@ViewById(R.id.view_bookmark_title) TextView mTitle;
	@ViewById(R.id.view_bookmark_url) TextView mUrl;
	@ViewById(R.id.view_bookmark_notes_section) View notesSection;
	@ViewById(R.id.view_bookmark_notes) TextView mNotes;
	@ViewById(R.id.view_bookmark_tag_section) View tagsSection;
	@ViewById(R.id.view_bookmark_tags) TagView mTags;
	@ViewById(R.id.view_bookmark_time) TextView mTime;
	@ViewById(R.id.view_bookmark_account) TextView mUsername;
	@ViewById(R.id.view_bookmark_title_icon) ImageView bookmarkIcon;
	@ViewById(R.id.web_view) WebView mWebContent;

	private Bookmark bookmark;
	private BookmarkViewType viewType;

	private OnBookmarkActionListener bookmarkActionListener;
	private OnBookmarkSelectedListener bookmarkSelectedListener;
	
	private final ContentObserver observer = new MyObserver(new Handler());
	
	private static final String STATE_VIEWTYPE = "viewType";
	private static final String STATE_BOOKMARK = "bookmark";
	
	public interface OnBookmarkActionListener {
		void onViewTagSelected(String tag, String user);
		void onAccountSelected(String account);
	}
	
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		
		mWebContent.getSettings().setJavaScriptEnabled(true);

		mTags.setListener(tagOnClickListener);
		mTags.setColorGenerator(ColorGenerator.DEFAULT);

		if (savedInstanceState != null) {
	        viewType = (BookmarkViewType)savedInstanceState.getSerializable(STATE_VIEWTYPE);
	        bookmark = savedInstanceState.getParcelable(STATE_BOOKMARK);
	        mWebContent.restoreState(savedInstanceState);
	    } 
		
		setHasOptionsMenu(true);
		
		if(savedInstanceState == null || (viewType != null && viewType.equals(BookmarkViewType.VIEW))){
			refresh();
		} else setViews();
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
	
    final TagView.OnTagClickListener tagOnClickListener = new TagView.OnTagClickListener() {
        public void onTagClick(String tag) {
			bookmarkActionListener.onViewTagSelected(tag, bookmark.getAccount());
        }
    };
    
    final AccountSpan.OnAccountClickListener accountOnClickListener = new AccountSpan.OnAccountClickListener() {
        public void onAccountClick(String account) {
        	bookmarkActionListener.onAccountSelected(account);
        }
    };
    
	public void setBookmark(Bookmark bookmark, BookmarkViewType viewType) {
		this.viewType = viewType;
		this.bookmark = bookmark;
	}
	
	public void clearView() {
		this.viewType = BookmarkViewType.VIEW;
		this.bookmark = null;

        if(mBookmarkView != null) {
            mBookmarkView.setVisibility(View.GONE);
        }
        if(mWebContent != null) {
            mWebContent.setVisibility(View.GONE);
        }
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
	    savedInstanceState.putSerializable(STATE_VIEWTYPE, viewType);
	    savedInstanceState.putParcelable(STATE_BOOKMARK, bookmark);
	    
	    if(mWebContent != null){
	    	mWebContent.saveState(savedInstanceState);
	    }
	}
    
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
	    inflater.inflate(R.menu.view_menu, menu);
	    
	    if(bookmark != null){
	    	MenuItem shareItem = menu.findItem(R.id.menu_view_sendbookmark);
	    	ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
			shareActionProvider.setShareIntent(IntentHelper.SendBookmark(bookmark.getUrl(), bookmark.getDescription()));
	    }
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {	
		if(bookmark != null){
			if(!isMyself()){
				menu.removeItem(R.id.menu_addbookmark);
				menu.removeItem(R.id.menu_view_editbookmark);
				menu.removeItem(R.id.menu_view_deletebookmark);
			} else {
				menu.removeItem(R.id.menu_view_addbookmark);
			}
		} else {
			menu.removeItem(R.id.menu_view);
			menu.removeItem(R.id.menu_view_sendbookmark);
			menu.removeItem(R.id.menu_view_editbookmark);
			menu.removeItem(R.id.menu_view_deletebookmark);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
		    case R.id.menu_view_details:
		    	bookmarkSelectedListener.onBookmarkSelected(bookmark, BookmarkViewType.VIEW);
				return true;
		    case R.id.menu_view_read:
				bookmarkSelectedListener.onBookmarkSelected(bookmark, BookmarkViewType.READ);
				return true;
		    case R.id.menu_view_openbookmark:
		    	bookmarkSelectedListener.onBookmarkSelected(bookmark, BookmarkViewType.WEB);
				return true;
		    case R.id.menu_view_editbookmark:
		    	bookmarkSelectedListener.onBookmarkSelected(bookmark, BookmarkViewType.EDIT);
		    	return true;
		    case R.id.menu_view_deletebookmark:
		    	bookmarkSelectedListener.onBookmarkDelete(bookmark);
				return true;
		    case R.id.menu_view_addbookmark:
		    	bookmarkSelectedListener.onBookmarkAdd(bookmark.copyForSharing());
				return true;
		    default:
		        return super.onOptionsItemSelected(item);
	    }
	}

	@Override
	public void onResume(){
		super.onResume();

		if(!getResources().getBoolean(R.bool.has_two_panes)) {
			getActivity().setTitle(getString(R.string.browse_my_bookmarks_title));
		}
	}

    public void onEvent(AccountChangedEvent event) {
        refresh();
    }

    private boolean isMyself() {
    	return bookmark != null && bookmark.getId() != 0;
    }

    public void refresh(){

    	setViews();
    	loadBookmark();
    }
    
    private void setViews(){
    	if(bookmark != null){
    		if(viewType == BookmarkViewType.VIEW){

				mBookmarkView.setVisibility(View.VISIBLE);
				mWebContent.setVisibility(View.GONE);
			} else {
				mBookmarkView.setVisibility(View.GONE);
				mWebContent.setVisibility(View.VISIBLE);
			}
    	} else {
    		clearView();
    	}
    }
    
    private void loadBookmark(){
    	if(bookmark != null){
    		if(viewType == BookmarkViewType.VIEW){
				
				if(bookmark.getDescription() != null && !bookmark.getDescription().equals("null"))
					mTitle.setText(bookmark.getDescription());
				
				mUrl.setText(bookmark.getUrl());
				
				if(bookmark.getNotes() != null && !bookmark.getNotes().equals("null") && !bookmark.getNotes().equals("")) {
					mNotes.setText(bookmark.getNotes());
					notesSection.setVisibility(View.VISIBLE);
				} else {
                    notesSection.setVisibility(View.GONE);
				}
				
				mTime.setText(bookmark.getTime().toString());

				if(bookmark.getTags().size() > 0) {
					mTags.setTags(bookmark.getTags(), " ");
		    		
		    		tagsSection.setVisibility(View.VISIBLE);
				} else {
                    tagsSection.setVisibility(View.GONE);
				}
				
				if(isMyself()){
					Uri.Builder ub = new Uri.Builder();
					ub.scheme("content");
					ub.authority(BookmarkContentProvider.AUTHORITY);
					ub.appendPath("bookmark");
					ub.appendPath(Integer.toString(bookmark.getId()));
					
					getActivity().getContentResolver().unregisterContentObserver(observer);
					getActivity().getContentResolver().registerContentObserver(ub.build(), true, observer);
					
					mUsername.setText(bookmark.getAccount());

					if(bookmark.getToRead() && bookmark.getShared()) {
                        bookmarkIcon.setImageResource(R.drawable.ic_unread);
                    } else if(!bookmark.getToRead() && bookmark.getShared()) {
                        bookmarkIcon.setImageResource(R.drawable.ic_bookmark);
                    } else if (bookmark.getToRead() && !bookmark.getShared()) {
                        bookmarkIcon.setImageResource(R.drawable.ic_unread_private);
                    } else if (!bookmark.getToRead() && !bookmark.getShared()) {
                        bookmarkIcon.setImageResource(R.drawable.ic_bookmark_private);
                    }




				} else {	
		    		if(bookmark.getAccount() != null){
						SpannableStringBuilder builder = new SpannableStringBuilder();
						int start = builder.length();
						builder.append(bookmark.getAccount());
						int end = builder.length();
						
						AccountSpan span = new AccountSpan(bookmark.getAccount());
						span.setOnAccountClickListener(accountOnClickListener);
			
						builder.setSpan(span, start, end, 0);
						
						mUsername.setText(builder);
		    		}
					
					mUsername.setMovementMethod(LinkMovementMethod.getInstance());
				}
			} else if(viewType == BookmarkViewType.READ){
				showInWebView(Constants.INSTAPAPER_URL + bookmark.getUrl());
				
				if(isMyself() && bookmark.getToRead() && SettingsHelper.getMarkAsRead(getActivity()))
		    		bookmarkSelectedListener.onBookmarkMark(bookmark);
			} else if(viewType == BookmarkViewType.WEB){
				showInWebView(bookmark.getUrl());
			}
    	} else {
    		clearView();
    	}
    }
    
    private void showInWebView(String url){
		String readingBackground = SettingsHelper.getReadingBackground(getActivity());
		String readingFont = SettingsHelper.getReadingFont(getActivity());
		String readingFontSize = SettingsHelper.getReadingFontSize(getActivity());
		String readingMargins = SettingsHelper.getReadingMargins(getActivity());
		
		mWebContent.loadUrl("about:blank");
		mWebContent.clearCache(true);
			
		CookieManager cookieManager = CookieManager.getInstance(); 
		CookieSyncManager.createInstance(getActivity());

		cookieManager.setAcceptCookie(true);
		cookieManager.setCookie("http://www.instapaper.com", "iptcolor=" + readingBackground + "; expires=Sat, 25-Mar-2023 00:00:00 GMT;path=/;");
		cookieManager.setCookie("http://www.instapaper.com", "iptfont=" + readingFont + "; expires=Sat, 25-Mar-2023 00:00:00 GMT;path=/;");
		cookieManager.setCookie("http://www.instapaper.com", "iptsize=" + readingFontSize + "; expires=Sat, 25-Mar-2023 00:00:00 GMT;path=/;");
		cookieManager.setCookie("http://www.instapaper.com", "iptwidth=" + readingMargins + "; expires=Sat, 25-Mar-2023 00:00:00 GMT;path=/;");

		CookieSyncManager.getInstance().sync();

		mWebContent.setWebViewClient(new WebViewClient(){ });

		mWebContent.loadUrl(url);
    }
    
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	getActivity().getContentResolver().unregisterContentObserver(observer);
    }
    
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			bookmarkActionListener = (OnBookmarkActionListener) activity;
			bookmarkSelectedListener = (OnBookmarkSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnBookmarkActionListener and OnBookmarkSelectedListener");
		}
	}
	
	class MyObserver extends ContentObserver {		
		public MyObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			try {
				bookmark = BookmarkManager.GetByHash(bookmark.getHash(), bookmark.getAccount(), getActivity());
			} catch (ContentNotFoundException e) {
			}
			refresh();
		}
	}
}