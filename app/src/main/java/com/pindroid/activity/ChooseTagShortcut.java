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
import com.pindroid.action.IntentHelper;
import com.pindroid.fragment.BrowseTagsFragment;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;

public class ChooseTagShortcut extends ActionBarActivity implements BrowseTagsFragment.OnTagSelectedListener {

	private String username = "";
	BrowseTagsFragment frag;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.browse_tags);
        getSupportActionBar().setTitle(R.string.shortcut_activity_title);

		Intent i = AccountManager.newChooseAccountIntent(null, null, new String[]{Constants.ACCOUNT_TYPE}, false, null, null, null, null);
		startActivityForResult(i, Constants.REQUEST_CODE_ACCOUNT_CHANGE);
          
		frag = (BrowseTagsFragment) getSupportFragmentManager().findFragmentById(R.id.listcontent);
        frag.setUsername(username);
    }
    
	public void onTagSelected(String tag) {		
		final Intent shortcutIntent = IntentHelper.ViewBookmarks(tag, username, null, this);
        final ShortcutIconResource iconResource = Intent.ShortcutIconResource.fromContext(this, R.drawable.ic_shortcut);
        final Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, tag);
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
        setResult(RESULT_OK, intent);
        finish();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){	
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode == Constants.REQUEST_CODE_ACCOUNT_CHANGE){
			username = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
			frag.setUsername(username);
			frag.refresh();
		}
	}
}