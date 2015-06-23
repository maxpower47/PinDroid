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
import com.pindroid.fragment.BrowseTagsFragment_;

import android.accounts.AccountManager;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;

@EActivity(R.layout.browse_tags)
public class ChooseTagShortcut extends AppCompatActivity implements BrowseTagsFragment.OnTagSelectedListener {

	private String username = "";

	@AfterViews
	public void init() {
        setTitle(R.string.shortcut_activity_title);

		Intent i = AccountManager.newChooseAccountIntent(null, null, new String[]{Constants.ACCOUNT_TYPE}, false, null, null, null, null);
		startActivityForResult(i, Constants.REQUEST_CODE_ACCOUNT_CHANGE);
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

	@OnActivityResult(Constants.REQUEST_CODE_ACCOUNT_CHANGE)
	protected void onChooseAccount(Intent data, @OnActivityResult.Extra(value = AccountManager.KEY_ACCOUNT_NAME) String username){
        this.username = username;
        BrowseTagsFragment frag = BrowseTagsFragment_.builder()
                .username(username)
                .build();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction t = fragmentManager.beginTransaction();
        t.replace(R.id.browse_tags_content, frag, "browse_tags_content");
        t.commitAllowingStateLoss();
	}
}