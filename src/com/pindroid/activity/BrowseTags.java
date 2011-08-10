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


import com.pindroid.R;
import com.pindroid.action.IntentHelper;
import com.pindroid.fragment.BrowseTagsFragment;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class BrowseTags extends FragmentBaseActivity implements BrowseTagsFragment.OnTagSelectedListener {
	
    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browse_tags);

        Intent intent = getIntent();
        
        Uri data = intent.getData();
        String action = intent.getAction();

		if(data != null)
			username = data.getUserInfo();
		else username = mAccount.name;
        
		BrowseTagsFragment frag = (BrowseTagsFragment) getSupportFragmentManager().findFragmentById(R.id.listcontent);
        frag.setAccount(username);
		
		if(Intent.ACTION_VIEW.equals(action)) {
			setTitle(getString(R.string.browse_my_tags_title));
		} else if(Intent.ACTION_PICK.equals(action)) {
			setTitle(getString(R.string.tag_live_folder_chooser_title));
		} else if(Intent.ACTION_SEARCH.equals(action)) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			setTitle(getString(R.string.tag_search_results_title, query));
		}
		
		if(action != null && action.equals(Intent.ACTION_PICK)) {
			frag.setAction("pick");
		} else {
			frag.setAction("notpick");
		}
    }

	public void onTagSelected(String tag) {		
		startActivity(IntentHelper.ViewBookmarks(tag, username, this));
	}
}