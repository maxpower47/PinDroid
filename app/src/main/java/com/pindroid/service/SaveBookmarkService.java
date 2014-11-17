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

package com.pindroid.service;

import com.pindroid.Constants;
import com.pindroid.R;
import com.pindroid.client.NetworkUtilities;
import com.pindroid.platform.BookmarkManager;
import com.pindroid.providers.BookmarkContent.Bookmark;

import android.app.IntentService;
import android.content.Intent;

public class SaveBookmarkService extends IntentService {
	
	public SaveBookmarkService() {
		super("SaveBookmarkService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bookmark bookmark = intent.getParcelableExtra(Constants.EXTRA_BOOKMARK);
		
		if(bookmark.getDescription() == null || bookmark.getDescription().equals("")) {
    		bookmark.setDescription(NetworkUtilities.getWebpageTitle(bookmark.getUrl()));
		}
		
		if(bookmark.getDescription() == null || bookmark.getDescription().equals("")) {
			bookmark.setDescription(getResources().getString(R.string.add_bookmark_default_title));
		}
		
		BookmarkManager.AddBookmark(bookmark, bookmark.getAccount(), this);
	}
}