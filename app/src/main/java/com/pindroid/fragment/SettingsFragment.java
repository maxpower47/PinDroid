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
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;

import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.widget.Toast;

import com.pindroid.Constants;
import com.pindroid.R;
import com.pindroid.providers.BookmarkContentProvider;
import com.pindroid.util.SyncUtils;

public class SettingsFragment extends PreferenceFragment {

    private Context mContext;
    private Resources res;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mContext = getActivity();
        res = getResources();

        Preference synctimePref = findPreference("pref_synctime");
        synctimePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object value) {
                long time = Long.parseLong((String)value);

                SyncUtils.removePeriodicSync(BookmarkContentProvider.AUTHORITY, Bundle.EMPTY, mContext);

                if(time != 0) {
                    SyncUtils.addPeriodicSync(BookmarkContentProvider.AUTHORITY, Bundle.EMPTY, time, mContext);
                }

                return true;
            }
        });

        Preference syncPref = findPreference("pref_forcesync");
        syncPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Toast.makeText(mContext, res.getString(R.string.syncing_toast), Toast.LENGTH_LONG).show();
                SyncUtils.clearSyncMarkers(mContext);

                Bundle extras = new Bundle();
                extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);

                ContentResolver.requestSync(null, BookmarkContentProvider.AUTHORITY, extras);

                return true;
            }
        });

        Preference accountPref = findPreference("pref_accountsettings");
        accountPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent i = new Intent(Settings.ACTION_SYNC_SETTINGS);
                i.putExtra(Settings.EXTRA_AUTHORITIES, new String[] {BookmarkContentProvider.AUTHORITY});

                mContext.startActivity(i);
                return true;
            }
        });

        Preference licensePref = findPreference("pref_license");
        licensePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Uri link = Uri.parse(Constants.GPL_URL);
                Intent i = new Intent(Intent.ACTION_VIEW, link);

                startActivity(i);
                return true;
            }
        });

        Preference helpPref = findPreference("pref_help");
        helpPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Uri link = Uri.parse(Constants.MANUAL_URL);
                Intent i = new Intent(Intent.ACTION_VIEW, link);

                startActivity(i);
                return true;
            }
        });

        Preference aboutPref = findPreference("pref_about");
        aboutPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Uri link = Uri.parse(Constants.ABOUT_URL);
                Intent i = new Intent(Intent.ACTION_VIEW, link);

                startActivity(i);
                return true;
            }
        });

        Preference donatePref = findPreference("pref_donate");
        donatePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Uri link = Uri.parse(Constants.DONATION_URL);
                Intent i = new Intent(Intent.ACTION_VIEW, link);

                startActivity(i);
                return true;
            }
        });
    }
}
