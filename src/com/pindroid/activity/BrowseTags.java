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
import com.pindroid.providers.BookmarkContentProvider;
import com.pindroid.fragment.BrowseTagsFragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class BrowseTags extends FragmentBaseActivity implements BrowseTagsFragment.OnTagSelectedListener {
	
    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browse_tags);
    }

	public void onTagSelected(String tag) {
		Intent i = new Intent(this, BrowseBookmarks.class);
		i.setAction(Intent.ACTION_VIEW);
		i.addCategory(Intent.CATEGORY_DEFAULT);

		Uri.Builder dataBuilder = new Uri.Builder();
		dataBuilder.scheme(Constants.CONTENT_SCHEME);
		dataBuilder.encodedAuthority(username + "@" + BookmarkContentProvider.AUTHORITY);
		dataBuilder.appendEncodedPath("bookmarks");
		dataBuilder.appendQueryParameter("tagname", tag);
		i.setData(dataBuilder.build());
		
		startActivity(i);	
	}
}