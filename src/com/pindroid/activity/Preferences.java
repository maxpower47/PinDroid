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
import com.pindroid.Constants;
import com.pindroid.providers.BookmarkContentProvider;
import com.pindroid.util.SyncUtils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.widget.Toast;

public class Preferences extends PreferenceActivity {
	
	private Context mContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        mContext = this;
        
        Preference synctimePref = (Preference) findPreference("pref_synctime");
        synctimePref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference, Object value) {
				long time = Long.parseLong((String)value);
				
				SyncUtils.removePeriodicSync(BookmarkContentProvider.AUTHORITY, Bundle.EMPTY, mContext);
				
				if(time != 0) {
					SyncUtils.addPeriodicSync(BookmarkContentProvider.AUTHORITY, Bundle.EMPTY, time, mContext);
				}
				
				return true;
			}
        });
        
        Preference syncPref = (Preference) findPreference("pref_forcesync");
        syncPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        	public boolean onPreferenceClick(Preference preference) {
            	
        		Toast.makeText(mContext, "Syncing...", Toast.LENGTH_LONG).show();
        		ContentResolver.requestSync(null, BookmarkContentProvider.AUTHORITY, Bundle.EMPTY);
        		
            	return true;
            }
        });
        
        Preference licensePref = (Preference) findPreference("pref_license");
        licensePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        	public boolean onPreferenceClick(Preference preference) {
            	Uri link = Uri.parse(Constants.GPL_URL);
        		Intent i = new Intent(Intent.ACTION_VIEW, link);
        		
        		startActivity(i);
            	return true;
            }
        });
        
        Preference helpPref = (Preference) findPreference("pref_help");
        helpPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        	public boolean onPreferenceClick(Preference preference) {
            	Uri link = Uri.parse(Constants.MANUAL_URL);
        		Intent i = new Intent(Intent.ACTION_VIEW, link);
        		
        		startActivity(i);
            	return true;
            }
        });
        
        Preference aboutPref = (Preference) findPreference("pref_about");
        aboutPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        	public boolean onPreferenceClick(Preference preference) {
        		Intent i = new Intent(mContext, AboutActivity.class);
        		
        		startActivity(i);
            	return true;
            }
        });
        
        Preference donatePref = (Preference) findPreference("pref_donate");
        donatePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        	public boolean onPreferenceClick(Preference preference) {
            	Uri link = Uri.parse(Constants.DONATION_URL);
        		Intent i = new Intent(Intent.ACTION_VIEW, link);
        		
        		startActivity(i);
            	return true;
            }
        });
        
        setTitle("PinDroid Settings");
    }
}