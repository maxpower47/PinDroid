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

package com.pindroid.fragment;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.widget.Toast;

import com.pindroid.BuildConfig;
import com.pindroid.R;
import com.pindroid.providers.BookmarkContentProvider;
import com.pindroid.util.SyncUtils;
import com.tasomaniac.android.widget.IntegrationPreference;

import org.androidannotations.annotations.AfterPreferences;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.PreferenceByKey;
import org.androidannotations.annotations.PreferenceChange;
import org.androidannotations.annotations.PreferenceClick;
import org.androidannotations.annotations.PreferenceScreen;
import org.androidannotations.annotations.res.StringRes;

@EFragment
@PreferenceScreen(R.xml.preferences)
public class SettingsFragment extends PreferenceFragment {
    @StringRes(R.string.syncing_toast) String syncingToast;

    @PreferenceByKey(R.string.pref_cat_version_key) PreferenceCategory versionPrefCat;
    @PreferenceByKey(R.string.pref_dashclock_key) IntegrationPreference dashclockPref;

    @AfterPreferences
    public void init() {
        try {
            String versionName = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;

            Preference versionPref = new Preference(getActivity());
            versionPref.setTitle("Version " + versionName + (BuildConfig.DEBUG ? " - DEBUG" : ""));
            versionPrefCat.addPreference(versionPref);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @PreferenceClick(R.string.pref_forcesync_key)
    void forceSync() {
        Toast.makeText(getActivity(), syncingToast, Toast.LENGTH_LONG).show();
        SyncUtils.clearSyncMarkers(getActivity());

        Bundle extras = new Bundle();
        extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);

        ContentResolver.requestSync(null, BookmarkContentProvider.AUTHORITY, extras);
    }

    @PreferenceClick(R.string.pref_accountsettings_key)
    void openAccountSettings() {
        Intent i = new Intent(Settings.ACTION_SYNC_SETTINGS);
        i.putExtra(Settings.EXTRA_AUTHORITIES, new String[] {BookmarkContentProvider.AUTHORITY});

        startActivity(i);
    }

    @PreferenceChange(R.string.pref_synctime_key)
    void synctimeChange(Object value) {
        long time = Long.parseLong((String) value);

        SyncUtils.removePeriodicSync(BookmarkContentProvider.AUTHORITY, Bundle.EMPTY, getActivity());

        if (time != 0) {
            SyncUtils.addPeriodicSync(BookmarkContentProvider.AUTHORITY, Bundle.EMPTY, time, getActivity());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        dashclockPref.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        dashclockPref.pause();
    }
}
