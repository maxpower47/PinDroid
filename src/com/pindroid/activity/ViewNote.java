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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;

import com.pindroid.R;
import com.pindroid.fragment.ViewNoteFragment;
import com.pindroid.providers.NoteContent.Note;

public class ViewNote extends FragmentBaseActivity {
	
	private String path;
	private Note note = null;

	@Override
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.view_note);
        
        setTitle(R.string.view_note_title);
        
        Intent intent = getIntent();
        
        if(Intent.ACTION_VIEW.equals(intent.getAction())) {
					
			Uri data = intent.getData();
			
			if(data != null) {
				path = data.getPath();				
			}
			
			note = new Note();
			
			if(path.contains("/notes")){
				int id = Integer.parseInt(data.getLastPathSegment());
				note.setId(id);
			}
			
			ViewNoteFragment frag = (ViewNoteFragment) getSupportFragmentManager().findFragmentById(R.id.view_note_fragment);
	        frag.setNote(note);
		}
	}
	
	@Override
	protected void changeAccount(){}
}