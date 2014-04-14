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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pindroid.R;
import com.pindroid.platform.NoteManager;
import com.pindroid.providers.ContentNotFoundException;
import com.pindroid.providers.NoteContent.Note;

public class ViewNoteFragment extends Fragment {
	
	private TextView mTitle;
	private TextView mText;
	private TextView mUsername;
	private Note note;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setRetainInstance(false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		
		mTitle = (TextView) getView().findViewById(R.id.view_note_title);
		mText = (TextView) getView().findViewById(R.id.view_note_text);
		mUsername = (TextView) getView().findViewById(R.id.view_note_account);
		
		setHasOptionsMenu(true);
	}
    
	public void setNote(Note n) {
		note = n;
	}
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.view_note_fragment, container, false);
    }

    @Override
    public void onStart(){
    	super.onStart();
    	
    	loadNote();
    }
    
    public void refresh() {
    	loadNote();
    }

    public void loadNote(){
    	if(note != null){
			try{		
				int id = note.getId();
				note = NoteManager.GetById(id, getActivity());
			} catch(ContentNotFoundException e){}
			
			mTitle.setText(note.getTitle());
			mText.setText(note.getText());
			mUsername.setText(note.getAccount());
    	}
    }
}