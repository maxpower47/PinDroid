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
import android.util.SparseBooleanArray;
import android.widget.ListView;

import com.pindroid.R;
import com.pindroid.event.AccountChangedEvent;
import com.pindroid.platform.TagManager;
import com.pindroid.providers.TagContent.Tag;
import com.pindroid.util.SettingsHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;

import java.util.HashSet;
import java.util.Set;

import de.greenrobot.event.EventBus;

@EFragment
@OptionsMenu(R.menu.select_tags_menu)
public class SelectTagsFragment extends ListFragment
	implements LoaderManager.LoaderCallbacks<Cursor>  {

	private final String sortfield = Tag.Name + " ASC";
	private SimpleCursorAdapter mAdapter;
	
	private String username = null;
	
	private OnTagsSelectedListener tagsSelectedListener;
	
	public interface OnTagsSelectedListener {
		void onTagsSelected(Set<String> tags);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().registerSticky(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
	
	@AfterViews
	public void init(){
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

    public void onEvent(AccountChangedEvent event) {
        this.username = event.getNewAccount();
        try{
            getLoaderManager().restartLoader(0, null, this);
        } catch(Exception e){}
    }

    @OptionsItem(R.id.menu_selecttags_ok)
    void menuOk() {
        Set<String> tags = new HashSet<>();

        SparseBooleanArray checked = getListView().getCheckedItemPositions();

        for(int i = 0; i < getListAdapter().getCount(); i++) {
            if(checked.get(i)) {
                Cursor c = (Cursor) getListAdapter().getItem(i);
                String n = c.getString(c.getColumnIndex(Tag.Name));

                tags.add(n);
            }
        }

        tagsSelectedListener.onTagsSelected(tags);
    }
	
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return TagManager.GetTags(username, sortfield, this.getActivity());
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