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
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.pindroid.R;
import com.pindroid.platform.TagManager;
import com.pindroid.providers.TagContent.Tag;

public class BrowseTagsFragment extends ListFragment
	implements LoaderManager.LoaderCallbacks<Cursor>, PindroidFragment  {

	private String sortfield = Tag.Name + " ASC";
	private SimpleCursorAdapter mAdapter;
	
	private String username = null;
	private String query = null;
	
	private OnTagSelectedListener tagSelectedListener;
	private OnItemClickListener clickListener;
	
	public interface OnTagSelectedListener {
		public void onTagSelected(String tag);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		clickListener = viewListener;
		setRetainInstance(true);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);

		mAdapter = new SimpleCursorAdapter(this.getActivity(), 
				R.layout.tag_view, null, 
				new String[] {Tag.Name, Tag.Count}, new int[] {R.id.tag_name, R.id.tag_count}, 0);
		
		setListAdapter(mAdapter);	
		
		getLoaderManager().initLoader(0, null, this);
		
		ListView lv = getListView();
		lv.setTextFilterEnabled(true);
		lv.setFastScrollEnabled(true);
		lv.setOnItemClickListener(clickListener);

		lv.setItemsCanFocus(false);
		lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getAccount(){
		return username;
	}
	
	public void setQuery(String query) {
		this.query = query;
	}
	
	public void refresh(){
		try{
			getLoaderManager().restartLoader(0, null, this);
		} catch(Exception e){}
	}
	
	private OnItemClickListener viewListener = new OnItemClickListener() {
	    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	    	String tagName = ((TextView)view.findViewById(R.id.tag_name)).getText().toString();
	    	
	    	tagSelectedListener.onTagSelected(tagName);
	    }
	};
	
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if(username != null) {
			if(query != null) {
				return TagManager.SearchTags(query, username, this.getActivity());
			} else {
				return TagManager.GetTags(username, sortfield, this.getActivity());
			}
		}
		else return null;
	}
	
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
	    mAdapter.swapCursor(data);
	}
	
	public void onLoaderReset(Loader<Cursor> loader) {
	    mAdapter.swapCursor(null);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			tagSelectedListener = (OnTagSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnTagSelectedListener");
		}
	}
}