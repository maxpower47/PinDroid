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

import com.pindroid.Constants;
import com.pindroid.R;
import com.pindroid.providers.BookmarkContent.Bookmark;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

public class WebViewFragment extends Fragment {

	private Bookmark bookmark;
	private boolean web;
	private WebView content;
	
	private OnBookmarkViewListener bookmarkViewListener;
	
	public interface OnBookmarkViewListener {
		public void onBookmarkView(Bookmark b);
		public void onBookmarkRead(Bookmark b);
		public void onBookmarkOpen(Bookmark b);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		
		content = (WebView) getView().findViewById(R.id.web_view);
		
		setHasOptionsMenu(true);
		setRetainInstance(true);
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		if(web) {
			inflater.inflate(R.menu.web_menu, menu);
		} else {
			inflater.inflate(R.menu.read_menu, menu);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
		    case R.id.menu_view_details:
				bookmarkViewListener.onBookmarkView(bookmark);
				return true;
		    case R.id.menu_view_read:
				bookmarkViewListener.onBookmarkRead(bookmark);
				return true;
		    case R.id.menu_view_openbookmark:
		    	bookmarkViewListener.onBookmarkOpen(bookmark);
				return true;
		    default:
		        return super.onOptionsItemSelected(item);
	    }
	}
	
	public void setUrl(Bookmark bookmark, boolean web){
		this.bookmark = bookmark;
		this.web = web;
	}
	
	@Override
	public void onStart(){
		super.onStart();

		if(bookmark != null){
			if(web){
				content.loadUrl(bookmark.getUrl());
			} else {
				String readUrl = Constants.INSTAPAPER_URL + URLEncoder.encode(bookmark.getUrl());
				content.loadUrl(readUrl);
			}
		}
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.web_view_fragment, container, false);
    }
    
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			bookmarkViewListener = (OnBookmarkViewListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnBookmarkViewListener");
		}
	}
}
