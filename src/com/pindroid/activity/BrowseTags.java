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

import com.pindroid.R;
import com.pindroid.Constants;
import com.pindroid.listadapter.TagListAdapter;
import com.pindroid.platform.TagManager;
import com.pindroid.providers.BookmarkContentProvider;
import com.pindroid.providers.TagContent.Tag;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.view.*;

public class BrowseTags extends AppBaseListActivity {
		
	private String sortfield = Tag.Name + " ASC";
	
	private final int sortNameAsc = 99999991;
	private final int sortNameDesc = 99999992;
	private final int sortCountAsc = 99999993;
	private final int sortCountDesc = 99999994;
	
	private ArrayList<Tag> tagList = new ArrayList<Tag>();
	
	private boolean loaded = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browse_tags);
		
		if(mAccount != null) {
			
			Intent intent = getIntent();
			String action = intent.getAction();
			
			Uri data = getIntent().getData();
			if(data != null) {
				username = data.getUserInfo();
			} else username = mAccount.name;
			
			if(Intent.ACTION_VIEW.equals(action) && data.getLastPathSegment().equals("bookmarks")) {
				Intent i = new Intent();
				i.setAction(Intent.ACTION_VIEW);
				i.addCategory(Intent.CATEGORY_DEFAULT);
				i.setData(data);
				
				startActivity(i);
				finish();			
				
			} else if(Intent.ACTION_SEARCH.equals(action)) {
	  		
	    		String query = intent.getStringExtra(SearchManager.QUERY);
	    		
	    		setTitle("Tag Search Results For \"" + query + "\"");
	    		
	    		tagList = TagManager.SearchTags(query, username, this);
	    		
	    		setListAdapter(new TagListAdapter(this, R.layout.tag_view, tagList));	
	    		
	    	} else if(mAccount.name.equals(username)){
				try{
					if(Intent.ACTION_VIEW.equals(action)) {
						setTitle("My Tags");
					} else if(Intent.ACTION_PICK.equals(action)) {
						setTitle("Choose A Tag For The Folder");
					}
	
					loadTagList();
					loaded = true;
	
				} catch(Exception e) {
					
				}
				
			}
	
			ListView lv = getListView();
			lv.setTextFilterEnabled(true);
			lv.setFastScrollEnabled(true);
		
			if(action != null && action.equals(Intent.ACTION_PICK)) {
				
				lv.setOnItemClickListener(new OnItemClickListener() {
				    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				    	String tagName = ((TextView)view.findViewById(R.id.tag_name)).getText().toString();
				    	
				    	Intent i = new Intent();
				    	i.putExtra("tagname", tagName);
				    	
						setResult(RESULT_OK, i);
						finish();
				    }
				});
				
			} else {
				lv.setOnItemClickListener(new OnItemClickListener() {
				    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				    	String tagName = ((TextView)view.findViewById(R.id.tag_name)).getText().toString();
				    	
						Intent i = new Intent();
						i.setAction(Intent.ACTION_VIEW);
						i.addCategory(Intent.CATEGORY_DEFAULT);
		
						Uri.Builder dataBuilder = new Uri.Builder();
						dataBuilder.scheme(Constants.CONTENT_SCHEME);
						dataBuilder.encodedAuthority(username + "@" + BookmarkContentProvider.AUTHORITY);
						dataBuilder.appendEncodedPath("bookmarks");
						dataBuilder.appendQueryParameter("tagname", tagName);
						i.setData(dataBuilder.build());
						
						startActivity(i);
				    }
				});
			}
		}
	}
	
	@Override
	public void onResume(){
		super.onResume();
		
		if(loaded) {
			refreshTagList();
		}	
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		
		if(result && isMyself()) {
		    SubMenu sortmenu = menu.addSubMenu(Menu.NONE, Menu.NONE, 1, R.string.menu_sort_title);
		    sortmenu.setIcon(R.drawable.ic_menu_sort_alphabetically);
		    sortmenu.add(Menu.NONE, sortNameAsc, 0, "Name (A-Z)");
		    sortmenu.add(Menu.NONE, sortNameDesc, 1, "Name (Z-A)");
		    sortmenu.add(Menu.NONE, sortCountAsc, 2, "Count (Least First)");
		    sortmenu.add(Menu.NONE, sortCountDesc, 3, "Count (Most First)");
		}
		
	    return result;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		boolean result = false;
		
	    switch (item.getItemId()) {
		    case sortNameAsc:
		    	sortfield = Tag.Name + " ASC";
				result = true;
				break;
		    case sortNameDesc:			
		    	sortfield = Tag.Name + " DESC";
		    	result = true;
		    	break;
		    case sortCountAsc:			
		    	sortfield = Tag.Count + " ASC";
		    	result = true;
		    	break;
		    case sortCountDesc:			
		    	sortfield = Tag.Count + " DESC";
		    	result = true;
		    	break;
	    }
	    
	    if(result) {
	    	loadTagList();
	    } else result = super.onOptionsItemSelected(item);
	    
	    return result;
	}
	
	private void loadTagList() {
		tagList = TagManager.GetTags(username, sortfield, this);
		
		setListAdapter(new TagListAdapter(this, R.layout.tag_view, tagList));	
		((TagListAdapter)getListAdapter()).notifyDataSetChanged();
	}
	
	private void refreshTagList() {
		tagList = TagManager.GetTags(username, sortfield, this);
		TagListAdapter adapter = (TagListAdapter)getListAdapter();
		
		adapter.update(tagList);
		adapter.notifyDataSetChanged();
	}
}
