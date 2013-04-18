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


public class SettingsHelper {
	
    public static boolean getPrivateDefault(Context context) {
    	return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_save_private_default", false);
    }
    
    public static boolean getToReadDefault(Context context) {
    	return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_save_toread_default", false);
    }
	
    public static String getDefaultAction(Context context) {
    	return PreferenceManager.getDefaultSharedPreferences(context).getString("pref_view_bookmark_default_action", "browser");
    }
    
    public static boolean getMarkAsRead(Context context) {
    	return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_markasread", false);
    }
    
    public static String getReadingMargins(Context context) {
    	return PreferenceManager.getDefaultSharedPreferences(context).getString("pref_reading_margins", "20");
    }
    
    public static String getReadingBackground(Context context) {
    	return PreferenceManager.getDefaultSharedPreferences(context).getString("pref_reading_background", "-1");
    }
    
    public static String getReadingFont(Context context) {
    	return PreferenceManager.getDefaultSharedPreferences(context).getString("pref_reading_font", "Roboto-Regular");
    }
    
    public static String getReadingFontSize(Context context) {
    	return PreferenceManager.getDefaultSharedPreferences(context).getString("pref_reading_fontsize", "16");
    }
    
    public static String getReadingLineSpace(Context context) {
    	return PreferenceManager.getDefaultSharedPreferences(context).getString("pref_reading_linespace", "5");
    }
    
    public static boolean getUseBrowser(Context context) {
    	return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_usebrowser", false);
    }
}
