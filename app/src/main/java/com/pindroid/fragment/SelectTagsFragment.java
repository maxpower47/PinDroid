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
import android.preference.PreferenceManager;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.pindroid.R;
import com.pindroid.platform.TagManager;
import com.pindroid.providers.TagContent.Tag;
import com.pindroid.util.SettingsHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SelectTagsFragment extends ListFragment
	implements LoaderManager.LoaderCallbacks<Cursor>, PindroidFragment  {

	private String sortfield = Tag.Name + " ASC";
	private SimpleCursorAdapter mAdapter;
	
	private String username = null;
	
	private OnTagsSelectedListener tagsSelectedListener;
	private OnItemClickListener clickListener;
	
	public interface OnTagsSelectedListener {
		public void onTagsSelected(Set<String> tags);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);

        setHasOptionsMenu(true);

		mAdapter = new SimpleCursorAdapter(this.getActivity(),
                android.R.layout.simple_list_item_multiple_choice, null,
				new String[] {Tag.Name}, new int[] {android.R.id.text1}, 0);

		setListAdapter(mAdapter);

		getLoaderManager().initLoader(0, null, this);

		ListView lv = getListView();
		lv.setTextFilterEnabled(true);
		lv.setFastScrollEnabled(true);

		lv.setItemsCanFocus(false);
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);


	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getAccount(){
		return username;
	}
	
	public void refresh(){
		try{
			getLoaderManager().restartLoader(0, null, this);
		} catch(Exception e){}
	}

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.select_tags_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_selecttags_ok:

                Set<String> tags = new HashSet<String>();

                SparseBooleanArray checked = getListView().getCheckedItemPositions();

                for(int i = 0; i < getListAdapter().getCount(); i++) {
                    if(checked.get(i)) {
                        Cursor c = (Cursor) getListAdapter().getItem(i);
                        String n = c.getString(c.getColumnIndex(Tag.Name));

                        tags.add(n);
                    }
                }

                tagsSelectedListener.onTagsSelected(tags);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
	
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if(username != null) {
			return TagManager.GetTags(username, sortfield, this.getActivity());
		}
		else return null;
	}
	
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
	    mAdapter.swapCursor(data);

        Set<String> tags = SettingsHelper.getDrawerTags(getActivity());

        for(int i = 0; i < getListAdapter().getCount(); i++) {
            Cursor c = (Cursor) getListAdapter().getItem(i);
            String n = c.getString(c.getColumnIndex(Tag.Name));

            if(tags.contains(n)) {
                getListView().setItemChecked(i, true);
            }
        }
	}
	
	public void onLoaderReset(Loader<Cursor> loader) {
	    mAdapter.swapCursor(null);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			tagsSelectedListener = (OnTagsSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnTagSelectedListener");
		}
	}
}