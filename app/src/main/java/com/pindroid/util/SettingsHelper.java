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

package com.pindroid.util;

import android.content.Context;
import android.preference.PreferenceManager;

import com.pindroid.R;

import java.util.HashSet;
import java.util.Set;


public class SettingsHelper {
	
    public static boolean getPrivateDefault(Context context) {
    	return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getResources().getString(R.string.pref_save_private_default_key), false);
    }
    
    public static boolean getToReadDefault(Context context) {
    	return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getResources().getString(R.string.pref_save_toread_default_key), false);
    }
	
    public static String getDefaultAction(Context context) {
    	return PreferenceManager.getDefaultSharedPreferences(context).getString(context.getResources().getString(R.string.pref_view_bookmark_default_action_key), "browser");
    }
    
    public static boolean getMarkAsRead(Context context) {
    	return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getResources().getString(R.string.pref_markasread_key), false);
    }
    
    public static String getReadingMargins(Context context) {
    	return PreferenceManager.getDefaultSharedPreferences(context).getString(context.getResources().getString(R.string.pref_reading_margins_key), "medwidthmode");
    }
    
    public static String getReadingBackground(Context context) {
    	return PreferenceManager.getDefaultSharedPreferences(context).getString(context.getResources().getString(R.string.pref_reading_background_key), "-1");
    }
    
    public static String getReadingFont(Context context) {
    	return PreferenceManager.getDefaultSharedPreferences(context).getString(context.getResources().getString(R.string.pref_reading_font_key), "Roboto-Regular");
    }
    
    public static String getReadingFontSize(Context context) {
    	return PreferenceManager.getDefaultSharedPreferences(context).getString(context.getResources().getString(R.string.pref_reading_fontsize_key), "16");
    }
    
    public static boolean getUseBrowser(Context context) {
    	return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getResources().getString(R.string.pref_usebrowser_key), false);
    }

    public static Set<String> getDrawerTags(Context context) {
        Set<String> tags = new HashSet<String>();
        tags = PreferenceManager.getDefaultSharedPreferences(context).getStringSet(context.getResources().getString(R.string.pref_drawertags_key), tags);
        return tags;
    }
}
