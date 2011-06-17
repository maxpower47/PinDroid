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

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.pindroid.Constants;
import com.pindroid.R;
import com.pindroid.activity.BrowseBookmarks;
import com.pindroid.activity.FragmentBaseActivity;
import com.pindroid.platform.TagManager;
import com.pindroid.providers.BookmarkContentProvider;
import com.pindroid.providers.TagContent.Tag;

public class BrowseTagsFragment extends ListFragment
	implements LoaderManager.LoaderCallbacks<Cursor> {

	private String sortfield = Tag.Name + " ASC";
	private SimpleCursorAdapter mAdapter;
	private FragmentBaseActivity base;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		
		base = (FragmentBaseActivity)getActivity();
		
		setHasOptionsMenu(true);
		
		mAdapter = new SimpleCursorAdapter(base, 
				R.layout.tag_view, null, 
				new String[] {Tag.Name, Tag.Count}, new int[] {R.id.tag_name, R.id.tag_count}, 0);
		
		setListAdapter(mAdapter);	
		
		getLoaderManager().initLoader(0, null, this);
		
		Intent intent = base.getIntent();
		String action = intent.getAction();
		
		Uri data = base.getIntent().getData();

		if(data != null)
			base.username = data.getUserInfo();
		else base.username = base.mAccount.name;

		if(Intent.ACTION_VIEW.equals(action) && data.getLastPathSegment().equals("bookmarks")) {
			Intent i = new Intent(base, BrowseBookmarks.class);
			i.setAction(Intent.ACTION_VIEW);
			i.addCategory(Intent.CATEGORY_DEFAULT);
			i.setData(data);
			
			startActivity(i);
			base.finish();			
		}
		
		if(Intent.ACTION_VIEW.equals(action)) {
			base.setTitle(getString(R.string.browse_my_tags_title));
		} else if(Intent.ACTION_PICK.equals(action)) {
			base.setTitle(getString(R.string.tag_live_folder_chooser_title));
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
			    	
			    	base.setResult(Activity.RESULT_OK, i);
			    	base.finish();
			    }
			});
			
		} else {
			lv.setOnItemClickListener(new OnItemClickListener() {
			    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			    	String tagName = ((TextView)view.findViewById(R.id.tag_name)).getText().toString();
			    	
					Intent i = new Intent(base, BrowseBookmarks.class);
					i.setAction(Intent.ACTION_VIEW);
					i.addCategory(Intent.CATEGORY_DEFAULT);
	
					Uri.Builder dataBuilder = new Uri.Builder();
					dataBuilder.scheme(Constants.CONTENT_SCHEME);
					dataBuilder.encodedAuthority(base.username + "@" + BookmarkContentProvider.AUTHORITY);
					dataBuilder.appendEncodedPath("bookmarks");
					dataBuilder.appendQueryParameter("tagname", tagName);
					i.setData(dataBuilder.build());
					
					startActivity(i);
			    }
			});
		}
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.browse_tag_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	
		boolean result = false;

	    switch (item.getItemId()) {
		    case R.id.menu_tag_sort_name_asc:
		    	sortfield = Tag.Name + " ASC";
				result = true;
				break;
		    case R.id.menu_tag_sort_name_desc:			
		    	sortfield = Tag.Name + " DESC";
		    	result = true;
		    	break;
		    case R.id.menu_tag_sort_count_asc:			
		    	sortfield = Tag.Count + " ASC";
		    	result = true;
		    	break;
		    case R.id.menu_tag_sort_count_desc:			
		    	sortfield = Tag.Count + " DESC";
		    	result = true;
		    	break;
	    }
	    
	    if(result) {
	    	getLoaderManager().restartLoader(0, null, this);
	    } else result = super.onOptionsItemSelected(item);
	    
	    return result;
	}
	
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {

		if(Intent.ACTION_SEARCH.equals(getActivity().getIntent().getAction())) { 		
			String query = getActivity().getIntent().getStringExtra(SearchManager.QUERY);
			base.setTitle(getString(R.string.tag_search_results_title, query));
			return TagManager.SearchTags(query, base.username, this.getActivity());
	
		} else if(base.mAccount.name.equals(base.username)){
			return TagManager.GetTags(base.username, sortfield, this.getActivity());
		}
		return new CursorLoader(base);
	}
	
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
	    mAdapter.swapCursor(data);
	}
	
	public void onLoaderReset(Loader<Cursor> loader) {
	    mAdapter.swapCursor(null);
	}
}