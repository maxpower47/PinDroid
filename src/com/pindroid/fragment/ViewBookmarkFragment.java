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

import java.util.Date;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import com.pindroid.Constants.BookmarkViewType;
import com.pindroid.Constants;
import com.pindroid.R;
import com.pindroid.action.IntentHelper;
import com.pindroid.fragment.BrowseBookmarksFragment.OnBookmarkSelectedListener;
import com.pindroid.providers.BookmarkContent.Bookmark;
import com.pindroid.providers.TagContent.Tag;
import com.pindroid.ui.AccountSpan;
import com.pindroid.ui.TagSpan;
import com.pindroid.util.SettingsHelper;

public class ViewBookmarkFragment extends Fragment implements PindroidFragment {
	
	private ScrollView mBookmarkView;
	private TextView mTitle;
	private TextView mUrl;
	private TextView mNotesTitle;
	private TextView mNotes;
	private TextView mTagsTitle;
	private TextView mTags;
	private TextView mTime;
	private TextView mUsername;
	private ImageView mPrivateIcon;
	private ImageView mSyncedIcon;
	private ImageView mUnreadIcon;
	private WebView mWebContent;
	private Bookmark bookmark;
	private BookmarkViewType viewType;
	
	private OnBookmarkActionListener bookmarkActionListener;
	private OnBookmarkSelectedListener bookmarkSelectedListener;
	
	private static final String STATE_VIEWTYPE = "viewType";
	
	public interface OnBookmarkActionListener {
		public void onViewTagSelected(String tag, String user);
		public void onAccountSelected(String account);
	}
	
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		
	    if (savedInstanceState != null) {
	        viewType = (BookmarkViewType)savedInstanceState.getSerializable(STATE_VIEWTYPE);
	    } 
		
		mBookmarkView = (ScrollView) getView().findViewById(R.id.bookmark_scroll_view);
		mTitle = (TextView) getView().findViewById(R.id.view_bookmark_title);
		mUrl = (TextView) getView().findViewById(R.id.view_bookmark_url);
		mNotesTitle = (TextView) getView().findViewById(R.id.view_bookmark_notes_title);
		mNotes = (TextView) getView().findViewById(R.id.view_bookmark_notes);
		mTagsTitle = (TextView) getView().findViewById(R.id.view_bookmark_tags_title);
		mTags = (TextView) getView().findViewById(R.id.view_bookmark_tags);
		mTime = (TextView) getView().findViewById(R.id.view_bookmark_time);
		mUsername = (TextView) getView().findViewById(R.id.view_bookmark_account);
		mPrivateIcon = (ImageView) getView().findViewById(R.id.view_bookmark_private);
		mSyncedIcon = (ImageView) getView().findViewById(R.id.view_bookmark_synced);
		mUnreadIcon = (ImageView) getView().findViewById(R.id.view_bookmark_unread);
		mWebContent = (WebView) getView().findViewById(R.id.web_view);
		
		mWebContent.getSettings().setJavaScriptEnabled(true);
		
		setHasOptionsMenu(true);
	}
	
    TagSpan.OnTagClickListener tagOnClickListener = new TagSpan.OnTagClickListener() {
        public void onTagClick(String tag) {
    		bookmarkActionListener.onViewTagSelected(tag, isMyself() ? null : bookmark.getAccount());
        }
    };
    
    AccountSpan.OnAccountClickListener accountOnClickListener = new AccountSpan.OnAccountClickListener() {
        public void onAccountClick(String account) {
        	bookmarkActionListener.onAccountSelected(account);
        }
    };
    
	public void setBookmark(Bookmark bookmark, BookmarkViewType viewType) {
		this.viewType = viewType;
		this.bookmark = bookmark;
		
		//ActivityCompat.invalidateOptionsMenu(this.getActivity());
	}
	
	public void clearView() {
		this.viewType = BookmarkViewType.VIEW;
		this.bookmark = null;
		
		mBookmarkView.setVisibility(View.GONE);
		mWebContent.setVisibility(View.GONE);
	}
	
	private void addTag(SpannableStringBuilder builder, Tag t, TagSpan.OnTagClickListener listener) {
		int flags = 0;
		
		if (builder.length() != 0) {
			builder.append("  ");
		}
		
		int start = builder.length();
		builder.append(t.getTagName());
		int end = builder.length();
		
		TagSpan span = new TagSpan(t.getTagName());
		span.setOnTagClickListener(listener);

		builder.setSpan(span, start, end, flags);
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	    savedInstanceState.putSerializable(STATE_VIEWTYPE, viewType);
	    
	    super.onSaveInstanceState(savedInstanceState);
	}
    
	@Override
	@TargetApi(14)
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
	    inflater.inflate(R.menu.view_menu, menu);
	    
	    if(android.os.Build.VERSION.SDK_INT >= 14) {
	    	if(bookmark != null){	
			    ShareActionProvider shareActionProvider = (ShareActionProvider) menu.findItem(R.id.menu_view_sendbookmark).getActionProvider();
			    shareActionProvider.setShareIntent(IntentHelper.SendBookmark(bookmark.getUrl(), bookmark.getDescription()));
	    	}
	    }
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		if(bookmark != null){
			if(!isMyself()){
				menu.removeItem(R.id.menu_view_editbookmark);
				menu.removeItem(R.id.menu_view_deletebookmark);
			} else {
				menu.removeItem(R.id.menu_addbookmark);
			}
		} else {
			menu.removeItem(R.id.menu_view);
			menu.removeItem(R.id.menu_view_sendbookmark);
			menu.removeItem(R.id.menu_view_editbookmark);
			menu.removeItem(R.id.menu_view_deletebookmark);
		}
	}
	
	@Override
	@TargetApi(14)
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
		    case R.id.menu_view_details:
		    	bookmarkSelectedListener.onBookmarkView(bookmark);
				return true;
		    case R.id.menu_view_read:
				bookmarkSelectedListener.onBookmarkRead(bookmark);
				return true;
		    case R.id.menu_view_openbookmark:
		    	bookmarkSelectedListener.onBookmarkOpen(bookmark);
				return true;
		    case R.id.menu_view_editbookmark:
		    	bookmarkSelectedListener.onBookmarkEdit(bookmark);
		    	return true;
		    case R.id.menu_view_deletebookmark:
		    	bookmarkSelectedListener.onBookmarkDelete(bookmark);
				return true;
		    case R.id.menu_view_sendbookmark:
		    	if(android.os.Build.VERSION.SDK_INT < 14 || item.getActionProvider() == null || !(item.getActionProvider() instanceof ShareActionProvider)) {
		    		bookmarkSelectedListener.onBookmarkShare(bookmark);
		    	}
		    	return true;
		    default:
		        return super.onOptionsItemSelected(item);
	    }
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.view_bookmark_fragment, container, false);
    }
    
    private boolean isMyself() {
    	return bookmark != null && bookmark.getId() != 0;
    }
    
    @Override
    public void onStart(){
    	super.onStart();
    	
    	loadBookmark();
    }

    public void loadBookmark(){
    	if(bookmark != null){
    		if(viewType == BookmarkViewType.VIEW){
				mBookmarkView.setVisibility(View.VISIBLE);
				mWebContent.setVisibility(View.GONE);
				
				Date d = new Date(bookmark.getTime());
				
				if(bookmark.getDescription() != null && !bookmark.getDescription().equals("null"))
					mTitle.setText(bookmark.getDescription());
				
				mUrl.setText(bookmark.getUrl());
				
				if(bookmark.getNotes() != null && !bookmark.getNotes().equals("null") && !bookmark.getNotes().equals("")) {
					mNotes.setText(bookmark.getNotes());
					mNotes.setVisibility(View.VISIBLE);
					mNotesTitle.setVisibility(View.VISIBLE);
				} else {
					mNotes.setVisibility(View.GONE);
					mNotesTitle.setVisibility(View.GONE);
				}
				
				mTime.setText(d.toString());
				
				mTags.setMovementMethod(LinkMovementMethod.getInstance());
				SpannableStringBuilder tagBuilder = new SpannableStringBuilder();
				
				if(bookmark.getTags().size() > 0) {
		    		for(Tag t : bookmark.getTags()) {
		    			addTag(tagBuilder, t, tagOnClickListener);
		    		}
		    		
		    		mTags.setText(tagBuilder);
		    		
		    		mTags.setVisibility(View.VISIBLE);
					mTagsTitle.setVisibility(View.VISIBLE);
				} else {
					mTags.setVisibility(View.GONE);
					mTagsTitle.setVisibility(View.GONE);
				}
				
				if(isMyself()){
					mUsername.setText(bookmark.getAccount());
					
					if(mPrivateIcon != null){
						if(!bookmark.getShared())
							mPrivateIcon.setVisibility(View.VISIBLE);
						else mPrivateIcon.setVisibility(View.GONE);
					}
					
					if(mSyncedIcon != null){
						if(bookmark.getSynced() != 0)
							mSyncedIcon.setVisibility(View.VISIBLE);
						else mSyncedIcon.setVisibility(View.GONE);
						
						if(bookmark.getSynced() == -1)
							mSyncedIcon.setImageResource(R.drawable.sync_fail);
						else mSyncedIcon.setImageResource(R.drawable.sync);
					}
					
					if(mUnreadIcon != null){
						if(bookmark.getToRead())
							mUnreadIcon.setVisibility(View.VISIBLE);
						else mUnreadIcon.setVisibility(View.GONE);
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
    	
		mWebContent.clearView();
		mWebContent.clearCache(true);
		mBookmarkView.setVisibility(View.GONE);
		mWebContent.setVisibility(View.VISIBLE);
			
		CookieManager cookieManager = CookieManager.getInstance(); 
		CookieSyncManager.createInstance(getActivity());

		cookieManager.setAcceptCookie(true);
		cookieManager.setCookie("http://www.instapaper.com", "iptcolor=" + readingBackground + "; expires=Sat, 25-Mar-2023 00:00:00 GMT;path=/;");
		cookieManager.setCookie("http://www.instapaper.com", "iptfont=" + readingFont + "; expires=Sat, 25-Mar-2023 00:00:00 GMT;path=/;");
		cookieManager.setCookie("http://www.instapaper.com", "iptsize=" + readingFontSize + "; expires=Sat, 25-Mar-2023 00:00:00 GMT;path=/;");

		CookieSyncManager.getInstance().sync();

		mWebContent.setWebViewClient(new WebViewClient(){ });

		mWebContent.loadUrl(url);
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

	public void setUsername(String username) {
		// TODO Auto-generated method stub
		
	}

	public void refresh() {
		// TODO Auto-generated method stub
		
	}
}