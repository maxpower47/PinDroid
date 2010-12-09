/*
 * DeliciousDroid - http://code.google.com/p/DeliciousDroid/
 *
 * Copyright (C) 2010 Matt Schmidt
 *
 * DeliciousDroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * DeliciousDroid is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DeliciousDroid; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */

package com.deliciousdroid.activity;

import java.util.ArrayList;

import com.deliciousdroid.R;
import com.deliciousdroid.Constants;
import com.deliciousdroid.client.DeliciousFeed;
import com.deliciousdroid.listadapter.TagListAdapter;
import com.deliciousdroid.platform.TagManager;
import com.deliciousdroid.providers.BookmarkContentProvider;
import com.deliciousdroid.providers.TagContent.Tag;

import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.view.*;

public class BrowseTags extends AppBaseActivity {
		
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browse_tags);
		Intent intent = getIntent();
		String action = intent.getAction();
		
		ArrayList<Tag> tagList = new ArrayList<Tag>();
		
		
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
    		
    		tagList = TagManager.SearchTags(query, mAccount.name, this);
    		
    		setListAdapter(new TagListAdapter(this, R.layout.tag_view, tagList));	
    		
    	} else if(mAccount.name.equals(username)){
			try{
				if(Intent.ACTION_VIEW.equals(action)) {
					setTitle("My Tags");
				} else if(Intent.ACTION_PICK.equals(action)) {
					setTitle("Choose A Tag For The Folder");
				}

				String[] projection = new String[] {Tag.Name, Tag.Count};
							
				Uri tags = Tag.CONTENT_URI;
				
				Cursor c = managedQuery(tags, projection, null, null, null);				
				
				if(c.moveToFirst()){
					
					int nameColumn = c.getColumnIndex(Tag.Name);
					int countColumn = c.getColumnIndex(Tag.Count);

					do {	
						Tag t = new Tag(c.getString(nameColumn), c.getInt(countColumn));

						tagList.add(t);
					} while(c.moveToNext());	
				}

				setListAdapter(new TagListAdapter(this, R.layout.tag_view, tagList));	

			} catch(Exception e) {
				
			}
			
		} else {
			try{
				setTitle("Tags For " + username);
				
				tagList = DeliciousFeed.fetchFriendTags(username);
				
				setListAdapter(new TagListAdapter(this, R.layout.tag_view, tagList));	
			}
			catch(Exception e){}
		}

		ListView lv = getListView();
		lv.setTextFilterEnabled(true);
	
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
