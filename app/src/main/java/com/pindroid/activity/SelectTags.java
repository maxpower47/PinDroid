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

import android.accounts.AccountManager;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;

import com.pindroid.Constants;
import com.pindroid.R;
import com.pindroid.action.IntentHelper;
import com.pindroid.application.PindroidApplication;
import com.pindroid.fragment.BrowseTagsFragment;
import com.pindroid.fragment.SelectTagsFragment;

import java.util.List;
import java.util.Set;

public class SelectTags extends ActionBarActivity implements SelectTagsFragment.OnTagsSelectedListener {

	SelectTagsFragment frag;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.select_tags);
        getSupportActionBar().setTitle(R.string.select_tags_activity_title);

		frag = (SelectTagsFragment) getSupportFragmentManager().findFragmentById(R.id.listcontent);
        frag.setUsername(((PindroidApplication)getApplication()).getUsername());
    }

	public void onTagsSelected(Set<String> tags) {

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putStringSet(getResources().getString(R.string.pref_drawertags_key), tags);
        editor.commit();

        finish();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}
}