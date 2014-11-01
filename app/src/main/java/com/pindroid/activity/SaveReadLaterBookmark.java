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

import java.util.Date;

import com.pindroid.Constants;
import com.pindroid.R;
import com.pindroid.providers.BookmarkContent.Bookmark;
import com.pindroid.service.SaveBookmarkService;
import com.pindroid.util.SettingsHelper;
import com.pindroid.util.StringUtils;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.widget.Toast;

public class SaveReadLaterBookmark extends FragmentBaseActivity {

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH){
			requestAccount();
		} else saveBookmark();
	}
	
	@Override
	protected void changeAccount(){
		saveBookmark();
	}
	
	private void saveBookmark(){
		if(app.getUsername() != null){
			Intent intent = getIntent();
	
			if((Intent.ACTION_SEND.equals(intent.getAction()) || Constants.ACTION_READLATER.equals(intent.getAction())) && intent.hasExtra(Intent.EXTRA_TEXT)){
				ShareCompat.IntentReader reader = ShareCompat.IntentReader.from(this);
				String url = StringUtils.getUrl(reader.getText().toString());

				if ("".equals(url)) {
					Toast.makeText(this, R.string.add_bookmark_invalid_url, Toast.LENGTH_LONG).show();
					finish();
					return;
				}

				Bookmark bookmark = new Bookmark();
				bookmark.setUrl(url);
				bookmark.setDescription(reader.getSubject());
				bookmark.setShared(!intent.getBooleanExtra(Constants.EXTRA_PRIVATE, SettingsHelper.getPrivateDefault(this)));
				bookmark.setToRead(true);
				bookmark.setTime(new Date().getTime());
				bookmark.setTagString("");
				bookmark.setAccount(app.getUsername());
				
				Intent i = new Intent(this, SaveBookmarkService.class);
				i.putExtra(Constants.EXTRA_BOOKMARK, bookmark);
				
				startService(i);
				
				Toast.makeText(this, R.string.save_later_saved, Toast.LENGTH_SHORT).show();
				
				finish();
			}
		} else {
			Toast.makeText(this, R.string.login_no_account, Toast.LENGTH_SHORT).show();
			finish();
		}	
	}

	@Override
	protected void startSearch(String query) {
		// TODO Auto-generated method stub
		
	}
}