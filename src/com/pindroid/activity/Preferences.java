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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        
        Preference licensePref = (Preference) findPreference("pref_license");
        licensePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        	public boolean onPreferenceClick(Preference preference) {
            	Uri link = Uri.parse("http://www.gnu.org/licenses/gpl-3.0.txt");
        		Intent i = new Intent(Intent.ACTION_VIEW, link);
        		
        		startActivity(i);
            	return true;
            }
        });
        
        Preference helpPref = (Preference) findPreference("pref_help");
        helpPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        	public boolean onPreferenceClick(Preference preference) {
            	Uri link = Uri.parse("http://code.google.com/p/PinDroid/wiki/Manual");
        		Intent i = new Intent(Intent.ACTION_VIEW, link);
        		
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