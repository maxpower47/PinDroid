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

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;
import android.widget.ImageButton;

import com.pindroid.R;

public class AboutActivity extends AppBaseActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.about_view);
		setTitle(R.string.about_activity_title);
		
		if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
			((ImageButton) findViewById(R.id.action_bar_search)).setOnClickListener(searchHandler);
		}
		
		WebView content = (WebView) findViewById(R.id.about_text_view);
		content.loadData(getString(R.string.about_text), "text/html", "utf-8");
		
		Intent intent = getIntent();
		
		if(Intent.ACTION_SEARCH.equals(intent.getAction())){
			if(intent.hasExtra(SearchManager.QUERY)){
				Intent i = new Intent(mContext, MainSearchResults.class);
				i.putExtras(intent.getExtras());
				startActivity(i);
				finish();
			} else {
				onSearchRequested();
			}
		} else if(Intent.ACTION_VIEW.equals(intent.getAction())) {
			
			Uri data = intent.getData();
			String path = null;
			String tagname = null;
			
			if(data != null) {
				path = data.getPath();
				tagname = data.getQueryParameter("tagname");
			}
			
			if(data.getScheme() == null || !data.getScheme().equals("content")){
				Intent i = new Intent(Intent.ACTION_VIEW, data);
				
				startActivity(i);
				finish();				
			} else if(path.contains("bookmarks") && TextUtils.isDigitsOnly(data.getLastPathSegment())) {
				Intent viewBookmark = new Intent(this, ViewBookmark.class);
				viewBookmark.setData(data);
				
				Log.d("View Bookmark Uri", data.toString());
				startActivity(viewBookmark);
				finish();
			} else if(tagname != null) {
				Intent viewTags = new Intent(this, BrowseBookmarks.class);
				viewTags.setData(data);
				
				Log.d("View Tags Uri", data.toString());
				startActivity(viewTags);
				finish();
			}
		}
	}
}