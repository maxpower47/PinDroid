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
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.pindroid.R;
import com.pindroid.platform.NoteManager;
import com.pindroid.providers.NoteContent.Note;

public class BrowseNotesFragment extends ListFragment
	implements LoaderManager.LoaderCallbacks<Cursor>, PindroidFragment {

	private String sortfield = Note.Title + " ASC";
	private SimpleCursorAdapter mAdapter;
	
	private String username = null;
	private String query = null;
	
	private OnNoteSelectedListener noteSelectedListener;
	
	public interface OnNoteSelectedListener {
		public void onNoteView(Note n);
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
				R.layout.note_view, null, 
				new String[] {Note.Title}, new int[] {R.id.note_title}, 0);
		
		setListAdapter(mAdapter);	
		
		getLoaderManager().initLoader(0, null, this);
		
		final ListView lv = getListView();
		lv.setTextFilterEnabled(true);
		lv.setFastScrollEnabled(true);
		
		lv.setItemsCanFocus(false);
		lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		lv.setOnItemClickListener(new OnItemClickListener() {
		    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final Cursor c = (Cursor)lv.getItemAtPosition(position);
				Note n = NoteManager.CursorToNote(c);
				
		    	viewNote(n);
		    }
		});
		
		getActivity().setTitle(getString(R.string.browse_my_notes_title));
	}
	
	private void viewNote(Note n) {
		noteSelectedListener.onNoteView(n);
	}
	
	public void setUsername(String username) {
		this.username = username;
	}
	
	public void setQuery(String query) {
		this.query = query;
	}
	
	public void refresh(){
		try{
			getLoaderManager().restartLoader(0, null, this);
		} catch(Exception e){}
	}
	
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if(username != null && !username.equals("")) {
			if(query != null) {
				return NoteManager.SearchNotes(query, username, this.getActivity());
			} else {
				return NoteManager.GetNotes(username, sortfield, this.getActivity());
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
			noteSelectedListener = (OnNoteSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnNoteSelectedListener");
		}
	}
}