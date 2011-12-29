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

import java.net.URLEncoder;
import java.util.Date;

import com.pindroid.Constants;
import com.pindroid.R;
import com.pindroid.activity.FragmentBaseActivity;
import com.pindroid.fragment.BrowseBookmarksFragment.OnBookmarkSelectedListener;
import com.pindroid.platform.BookmarkManager;
import com.pindroid.providers.ContentNotFoundException;
import com.pindroid.providers.BookmarkContent.Bookmark;
import com.pindroid.providers.TagContent.Tag;
import com.pindroid.ui.AccountSpan;
import com.pindroid.ui.TagSpan;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

public class ViewBookmarkFragment extends Fragment {

	private FragmentBaseActivity base;
	
	private ScrollView mBookmarkView;
	private TextView mTitle;
	private TextView mUrl;
	private TextView mNotes;
	private TextView mTags;
	private TextView mTime;
	private TextView mUsername;
	private ImageView mIcon;
	private WebView mWebContent;
	private Bookmark bookmark;
	private String viewType;
	
	private OnBookmarkActionListener bookmarkActionListener;
	private OnBookmarkSelectedListener bookmarkSelectedListener;
	
	public interface OnBookmarkActionListener {
		public void onViewTagSelected(String tag);
		public void onUserTagSelected(String tag, String user);
		public void onAccountSelected(String account);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		
		base = (FragmentBaseActivity)getActivity();
		
		mBookmarkView = (ScrollView) getView().findViewById(R.id.bookmark_scroll_view);
		mTitle = (TextView) getView().findViewById(R.id.view_bookmark_title);
		mUrl = (TextView) getView().findViewById(R.id.view_bookmark_url);
		mNotes = (TextView) getView().findViewById(R.id.view_bookmark_notes);
		mTags = (TextView) getView().findViewById(R.id.view_bookmark_tags);
		mTime = (TextView) getView().findViewById(R.id.view_bookmark_time);
		mUsername = (TextView) getView().findViewById(R.id.view_bookmark_account);
		mIcon = (ImageView) getView().findViewById(R.id.view_bookmark_icon);
		mWebContent = (WebView) getView().findViewById(R.id.web_view);
		
		setHasOptionsMenu(true);
		setRetainInstance(true);
	}
	
    TagSpan.OnTagClickListener tagOnClickListener = new TagSpan.OnTagClickListener() {
        public void onTagClick(String tag) {
    		bookmarkActionListener.onViewTagSelected(tag);
        }
    };
    
    TagSpan.OnTagClickListener userTagOnClickListener = new TagSpan.OnTagClickListener() {
        public void onTagClick(String tag) {
        	bookmarkActionListener.onUserTagSelected(tag, bookmark.getAccount());
        }
    };
    
    AccountSpan.OnAccountClickListener accountOnClickListener = new AccountSpan.OnAccountClickListener() {
        public void onAccountClick(String account) {
        	bookmarkActionListener.onAccountSelected(account);
        }
    };
    
	public void setBookmark(Bookmark b, String viewType) {
		this.viewType = viewType;
		bookmark = b;
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
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
	    inflater.inflate(R.menu.view_menu, menu);
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		if(bookmark != null){
			if(!isMyself()) {
				menu.removeItem(R.id.menu_view_editbookmark);
				menu.removeItem(R.id.menu_view_deletebookmark);
			}
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
		    case R.id.menu_view_details:
				bookmarkSelectedListener.onBookmarkView(bookmark);
				return true;
		    case R.id.menu_view_read:
		    	if(isMyself() && bookmark.getToRead() && base.markAsRead)
		    		bookmarkSelectedListener.onBookmarkMark(bookmark);
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
		    	bookmarkSelectedListener.onBookmarkShare(bookmark);
		    	return true;
		    default:
		        return super.onOptionsItemSelected(item);
	    }
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.view_bookmark_fragment, container, false);
    }
    
    private boolean isMyself() {
    	return bookmark.getId() != 0;
    }

    
    public void loadBookmark(){
    	if(bookmark != null){
    		if(viewType.equals("view")){
				mBookmarkView.setVisibility(View.VISIBLE);
				mWebContent.setVisibility(View.GONE);
				if(isMyself()){
					
					try{		
						int id = bookmark.getId();
		
						bookmark = BookmarkManager.GetById(id, base);
						
						Date d = new Date(bookmark.getTime());
						
						mTitle.setText(bookmark.getDescription());
						mUrl.setText(bookmark.getUrl());
						mNotes.setText(bookmark.getNotes());
						mTime.setText(d.toString());
						mUsername.setText(bookmark.getAccount());
						
						if(!bookmark.getShared()) {
							mIcon.setImageResource(R.drawable.padlock);
						} else if(bookmark.getToRead()) {
							mIcon.setImageResource(R.drawable.book_open);
						}
						
		        		SpannableStringBuilder tagBuilder = new SpannableStringBuilder();
		
		        		for(Tag t : bookmark.getTags()) {
		        			addTag(tagBuilder, t, tagOnClickListener);
		        		}
		        		
		        		mTags.setText(tagBuilder);
		        		mTags.setMovementMethod(LinkMovementMethod.getInstance());
					}
					catch(ContentNotFoundException e){}
				} else {
					
					Date d = new Date(bookmark.getTime());
					
					if(!bookmark.getDescription().equals("null"))
						mTitle.setText(bookmark.getDescription());
					
					mUrl.setText(bookmark.getUrl());
					
					if(!bookmark.getNotes().equals("null"))
							mNotes.setText(bookmark.getNotes());
					
					mTime.setText(d.toString());
					
		    		SpannableStringBuilder tagBuilder = new SpannableStringBuilder();
		
		    		for(Tag t : bookmark.getTags()) {
		    			addTag(tagBuilder, t, userTagOnClickListener);
		    		}
		    		
		    		mTags.setText(tagBuilder);
		    		mTags.setMovementMethod(LinkMovementMethod.getInstance());
		
					SpannableStringBuilder builder = new SpannableStringBuilder();
					int start = builder.length();
					builder.append(bookmark.getAccount());
					int end = builder.length();
					
					AccountSpan span = new AccountSpan(bookmark.getAccount());
					span.setOnAccountClickListener(accountOnClickListener);
		
					builder.setSpan(span, start, end, 0);
					
					mUsername.setText(builder);
					mUsername.setMovementMethod(LinkMovementMethod.getInstance());
				}
			} else if(viewType.equals("read")){
				mWebContent.clearView();
				mWebContent.clearCache(true);
				mBookmarkView.setVisibility(View.GONE);
				mWebContent.setVisibility(View.VISIBLE);
				String readUrl = Constants.INSTAPAPER_URL + URLEncoder.encode(bookmark.getUrl());
				mWebContent.loadUrl(readUrl);
			} else if(viewType.equals("web")){
				mWebContent.clearView();
				mWebContent.clearCache(true);
				mBookmarkView.setVisibility(View.GONE);
				mWebContent.setVisibility(View.VISIBLE);
				mWebContent.loadUrl(bookmark.getUrl());
			}
    	}
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
}
