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
import android.widget.TextView;

import com.pindroid.R;
import com.pindroid.platform.NoteManager;
import com.pindroid.providers.ContentNotFoundException;
import com.pindroid.providers.NoteContent.Note;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

@EFragment(R.layout.view_note_fragment)
public class ViewNoteFragment extends Fragment {
	
	@ViewById(R.id.view_note_title) TextView mTitle;
    @ViewById(R.id.view_note_text) TextView mText;
    @ViewById(R.id.view_note_account) TextView mUsername;

	private Note note;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setRetainInstance(false);
	}

	public void setNote(Note n) {
		note = n;
	}

    @Override
    public void onStart(){
    	super.onStart();
    	
    	loadNote();
    }
    
    public void refresh() {
    	loadNote();
    }

    public boolean useMainToolbar() {
        return true;
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