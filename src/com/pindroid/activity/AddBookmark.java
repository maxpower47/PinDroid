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

import com.pindroid.Constants;
import com.pindroid.R;
import com.pindroid.fragment.AddBookmarkFragment;
import com.pindroid.fragment.AddBookmarkFragment.OnBookmarkSaveListener;
import com.pindroid.platform.BookmarkManager;
import com.pindroid.providers.ContentNotFoundException;
import com.pindroid.providers.BookmarkContent.Bookmark;
import com.pindroid.util.SettingsHelper;
import com.pindroid.util.StringUtils;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.view.View;
import android.widget.Toast;

public class AddBookmark extends FragmentBaseActivity implements OnBookmarkSaveListener {

	private AddBookmarkFragment frag;
	private Bookmark bookmark = null;
	private Bookmark oldBookmark = null;
	private Boolean update = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_bookmark);
		
		Intent intent = getIntent();
		
		if(Intent.ACTION_SEND.equals(intent.getAction()) && intent.hasExtra(Intent.EXTRA_TEXT)){
			bookmark = new Bookmark();
			
			ShareCompat.IntentReader reader = ShareCompat.IntentReader.from(this);
			
			String url = StringUtils.getUrl(reader.getText().toString());
			bookmark.setUrl(url);
			
			if(reader.getSubject() != null)
				bookmark.setDescription(reader.getSubject());
			
			if(url.equals("")) {
				Toast.makeText(this, R.string.add_bookmark_invalid_url, Toast.LENGTH_LONG).show();
			}
			
			if(intent.hasExtra(Constants.EXTRA_DESCRIPTION)){
				bookmark.setDescription(intent.getStringExtra(Constants.EXTRA_DESCRIPTION));
			}
			
			bookmark.setNotes(intent.getStringExtra(Constants.EXTRA_NOTES));
			bookmark.setTagString(intent.getStringExtra(Constants.EXTRA_TAGS));
			bookmark.setShared(!intent.getBooleanExtra(Constants.EXTRA_PRIVATE, SettingsHelper.getPrivateDefault(this)));
			bookmark.setToRead(intent.getBooleanExtra(Constants.EXTRA_TOREAD, SettingsHelper.getToReadDefault(this)));

			try{
				Bookmark old = BookmarkManager.GetByUrl(bookmark.getUrl(), this);
				bookmark = old.copy();
			} catch(Exception e) {
				
			}
			
		} else if(Intent.ACTION_EDIT.equals(intent.getAction())){
			int id = Integer.parseInt(intent.getData().getLastPathSegment());
			try {
				bookmark = BookmarkManager.GetById(id, this);
				oldBookmark = bookmark.copy();
				
				update = true;
			} catch (ContentNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		if(update)
			setTitle(getString(R.string.add_bookmark_edit_title));
		else setTitle(getString(R.string.add_bookmark_add_title));
		
		frag = (AddBookmarkFragment) getSupportFragmentManager().findFragmentById(R.id.add_bookmark_fragment);
		frag.loadBookmark(bookmark, oldBookmark);
		frag.setUsername(app.getUsername());
	}
	
	public void saveHandler(View v) {
		frag.saveHandler(v);
	}
	
	public void cancelHandler(View v) {
		frag.cancelHandler(v);
	}

	public void onBookmarkSave(Bookmark b) {
		finish();
	}

	public void onBookmarkCancel(Bookmark b) {
		finish();
	}
}