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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.pindroid.Constants;
import com.pindroid.Constants.BookmarkViewType;
import com.pindroid.R;
import com.pindroid.action.IntentHelper;
import com.pindroid.fragment.BrowseBookmarksFragment.OnBookmarkSelectedListener;
import com.pindroid.fragment.ViewBookmarkFragment;
import com.pindroid.fragment.ViewBookmarkFragment.OnBookmarkActionListener;
import com.pindroid.platform.BookmarkManager;
import com.pindroid.providers.BookmarkContent.Bookmark;
import com.pindroid.providers.ContentNotFoundException;

public class ViewBookmark extends FragmentBaseActivity implements OnBookmarkActionListener,
	OnBookmarkSelectedListener {
	
	private String path;
	private Bookmark bookmark = null;

	@Override
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.view_bookmark);
        
        setTitle(R.string.view_bookmark_title);
        
        Intent intent = getIntent();
        
        if(Intent.ACTION_VIEW.equals(intent.getAction())) {
					
			Uri data = intent.getData();
			
			if(data != null) {
				path = data.getPath();
			}
			
			if(path.contains("/bookmarks")){
				if(intent.hasExtra(Constants.EXTRA_BOOKMARK))
					bookmark = intent.getParcelableExtra(Constants.EXTRA_BOOKMARK);
				else {
					try {
						int id = Integer.parseInt(data.getLastPathSegment());
						bookmark = BookmarkManager.GetById(id, this);
						
					} catch (NumberFormatException e) {}
					catch (ContentNotFoundException e) {}
				}
				
			}
			
			BookmarkViewType type = (BookmarkViewType) intent.getSerializableExtra(Constants.EXTRA_VIEWTYPE);
			if(type == null)
				type = BookmarkViewType.VIEW;
			
			// used for search suggestions, since we can't put the serializable extra in the string uri
			// provided by the content provider
			if(data.getQueryParameter(Constants.EXTRA_READVIEW) != null && data.getQueryParameter(Constants.EXTRA_READVIEW).equals("1"))
				type = BookmarkViewType.READ;
			
			ViewBookmarkFragment frag = (ViewBookmarkFragment) getSupportFragmentManager().findFragmentById(R.id.view_bookmark_fragment);
	        frag.setBookmark(bookmark, type);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.view_activity_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
		    case R.id.menu_addbookmark:
				startActivity(IntentHelper.AddBookmark(bookmark.getUrl(), null, this));
				return true;
		    default:
		        return super.onOptionsItemSelected(item);
	    }
	}
	
	public void onViewTagSelected(String tag, String user) {		
		startActivity(IntentHelper.ViewBookmarks(tag, null, user, this));
	}

	public void onAccountSelected(String account) {
		startActivity(IntentHelper.ViewBookmarks(null, null, account, this));
	}

	public void onBookmarkView(Bookmark b) {
		ViewBookmarkFragment viewFrag = (ViewBookmarkFragment) getSupportFragmentManager().findFragmentById(R.id.view_bookmark_fragment);
		viewFrag.setBookmark(b, BookmarkViewType.VIEW);
		viewFrag.loadBookmark();
	}

	public void onBookmarkRead(Bookmark b) {
		ViewBookmarkFragment viewFrag = (ViewBookmarkFragment) getSupportFragmentManager().findFragmentById(R.id.view_bookmark_fragment);
		viewFrag.setBookmark(b, BookmarkViewType.READ);
		viewFrag.loadBookmark();
	}

	public void onBookmarkOpen(Bookmark b) {
    	String url = b.getUrl();
    	
    	if(!url.startsWith("http")) {
    		url = "http://" + url;
    	}
		
		startActivity(IntentHelper.OpenInBrowser(url));
	}

	public void onBookmarkAdd(Bookmark b) {
	}

	public void onBookmarkShare(Bookmark b) {
		Intent sendIntent = IntentHelper.SendBookmark(b.getUrl(), b.getDescription());
    	startActivity(Intent.createChooser(sendIntent, getString(R.string.share_chooser_title)));	
	}

	public void onBookmarkMark(Bookmark b) {
    	if(b != null && isMyself() && b.getToRead()) {
    		b.setToRead(false);
			BookmarkManager.UpdateBookmark(b, app.getUsername(), this);
    	}
	}

	public void onBookmarkEdit(Bookmark b) {
		startActivity(IntentHelper.EditBookmark(b, null, this));	
	}

	public void onBookmarkDelete(Bookmark b) {
		BookmarkManager.LazyDelete(b, app.getUsername(), this);
		finish();
	}
	
	@Override
	protected void changeAccount(){}
}