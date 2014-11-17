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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;

import com.pindroid.Constants;

public class SyncUtils {

	public static void addPeriodicSync(String authority, Bundle extras, long frequency, Context context) {
		AccountManager am = AccountManager.get(context);
		Account[] accounts = am.getAccountsByType(Constants.ACCOUNT_TYPE);
		
		for(Account a : accounts) {
			
			ContentResolver.addPeriodicSync(a, authority, extras, frequency * 60);
		}
	}
	
    public static void removePeriodicSync(String authority, Bundle extras, Context context) {	
		AccountManager am = AccountManager.get(context);
		Account[] accounts = am.getAccountsByType(Constants.ACCOUNT_TYPE);
		
		for(Account a : accounts) {
			ContentResolver.removePeriodicSync(a, authority, extras);
		}
    }
    
    public static void clearSyncMarkers(Context context){
		Account[] accounts = AccountManager.get(context).getAccountsByType(Constants.ACCOUNT_TYPE);
		for(Account a : accounts){
			AccountManager.get(context).setUserData(a, Constants.SYNC_MARKER_KEY, "0");
		}
    }
}