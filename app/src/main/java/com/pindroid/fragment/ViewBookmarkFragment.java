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
import android.widget.TextView;

import com.pindroid.Constants;
import com.pindroid.Constants.BookmarkViewType;
import com.pindroid.R;
import com.pindroid.action.IntentHelper;
import com.pindroid.fragment.BrowseBookmarksFragment.OnBookmarkSelectedListener;
import com.pindroid.platform.BookmarkManager;
import com.pindroid.providers.BookmarkContent.Bookmark;
import com.pindroid.providers.BookmarkContentProvider;
import com.pindroid.providers.ContentNotFoundException;
import com.pindroid.providers.TagContent.Tag;
import com.pindroid.ui.AccountSpan;
import com.pindroid.ui.TagSpan;
import com.pindroid.util.SettingsHelper;

public class ViewBookmarkFragment extends Fragment implements PindroidFragment {
	
	private ScrollView mBookmarkView;
	private TextView mTitle;
	private TextView mUrl;
	private View notesSection;
	private TextView mNotes;
	private View tagsSection;
	private TextView mTags;
	private TextView mTime;
	private TextView mUsername;
    private ImageView bookmarkIcon;
	private WebView mWebContent;
	private Bookmark bookmark;
	private BookmarkViewType viewType;
	
	private OnBookmarkActionListener bookmarkActionListener;
	private OnBookmarkSelectedListener bookmarkSelectedListener;
	
	private ContentObserver observer = new MyObserver(new Handler());
	
	private static final String STATE_VIEWTYPE = "viewType";
	private static final String STATE_BOOKMARK = "bookmark";
	
	public interface OnBookmarkActionListener {
		public void onViewTagSelected(String tag, String user);
		public void onAccountSelected(String account);
	}
	
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		
		mBookmarkView = (ScrollView) getView().findViewById(R.id.bookmark_scroll_view);
		mTitle = (TextView) getView().findViewById(R.id.view_bookmark_title);
		mUrl = (TextView) getView().findViewById(R.id.view_bookmark_url);
		notesSection = getView().findViewById(R.id.view_bookmark_notes_section);
		mNotes = (TextView) getView().findViewById(R.id.view_bookmark_notes);
		tagsSection = getView().findViewById(R.id.view_bookmark_tag_section);
		mTags = (TextView) getView().findViewById(R.id.view_bookmark_tags);
		mTime = (TextView) getView().findViewById(R.id.view_bookmark_time);
		mUsername = (TextView) getView().findViewById(R.id.view_bookmark_account);
		bookmarkIcon = (ImageView) getView().findViewById(R.id.view_bookmark_title_icon);
		mWebContent = (WebView) getView().findViewById(R.id.web_view);
		
		mWebContent.getSettings().setJavaScriptEnabled(true);
		
		if (savedInstanceState != null) {
	        viewType = (BookmarkViewType)savedInstanceState.getSerializable(STATE_VIEWTYPE);
	        bookmark = (Bookmark)savedInstanceState.getParcelable(STATE_BOOKMARK);
	        mWebContent.restoreState(savedInstanceState);
	    } 
		
		setHasOptionsMenu(true);
		
		if(savedInstanceState == null || (viewType != null && viewType.equals(BookmarkViewType.VIEW))){
			refresh();
		} else setViews();

	}
	
    TagSpan.OnTagClickListener tagOnClickListener = new TagSpan.OnTagClickListener() {
        public void onTagClick(String tag) {
    		bookmarkActionListener.onViewTagSelected(tag, bookmark.getAccount());
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.view_bookmark_fragment, container, false);
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

				Date d = new Date(bookmark.getTime());
				
				if(bookmark.getDescription() != null && !bookmark.getDescription().equals("null"))
					mTitle.setText(bookmark.getDescription());
				
				mUrl.setText(bookmark.getUrl());
				
				if(bookmark.getNotes() != null && !bookmark.getNotes().equals("null") && !bookmark.getNotes().equals("")) {
					mNotes.setText(bookmark.getNotes());
					notesSection.setVisibility(View.VISIBLE);
				} else {
                    notesSection.setVisibility(View.GONE);
				}
				
				mTime.setText(d.toString());
				
				mTags.setMovementMethod(LinkMovementMethod.getInstance());
				SpannableStringBuilder tagBuilder = new SpannableStringBuilder();
				
				if(bookmark.getTags().size() > 0) {
		    		for(Tag t : bookmark.getTags()) {
		    			addTag(tagBuilder, t, tagOnClickListener);
		    		}
		    		
		    		mTags.setText(tagBuilder);
		    		
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

	public void setUsername(String username) {

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