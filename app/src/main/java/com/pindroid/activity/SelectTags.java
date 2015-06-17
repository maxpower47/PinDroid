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

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.pindroid.R;
import com.pindroid.application.PindroidApplication;
import com.pindroid.event.DrawerTagsChangedEvent;
import com.pindroid.fragment.SelectTagsFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.FragmentById;

import java.util.Set;

import de.greenrobot.event.EventBus;

@EActivity(R.layout.select_tags)
public class SelectTags extends AppCompatActivity implements SelectTagsFragment.OnTagsSelectedListener {

	public void onTagsSelected(Set<String> tags) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putStringSet(getResources().getString(R.string.pref_drawertags_key), tags);
        editor.commit();

        EventBus.getDefault().post(new DrawerTagsChangedEvent());

        finish();
	}
}