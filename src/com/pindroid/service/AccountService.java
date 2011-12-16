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

package com.pindroid.service;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.auth.AuthenticationException;

import com.pindroid.Constants;
import com.pindroid.client.PinboardApi;
import com.pindroid.client.TooManyRequestsException;
import com.pindroid.platform.BookmarkManager;
import com.pindroid.platform.TagManager;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class AccountService extends IntentService {

	private AccountManager mAccountManager;
	
	public AccountService() {
		super("AccountService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		mAccountManager = AccountManager.get(this);
		
		Account[] a = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
		ArrayList<String> accounts = new ArrayList<String>();
		for (int i = 0; i < a.length; i++) {	
			accounts.add(a[i].name);
		}
		
		BookmarkManager.TruncateBookmarks(accounts, this, true);
		TagManager.TruncateOldTags(accounts, this);
		
		if(a.length > 0) {
			Log.d("AS Handle", "Getting Token for " + a[0].name);
			
			try {
				String token = PinboardApi.getSecretToken(a[0], this);

		        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		        SharedPreferences.Editor editor = settings.edit();
		    	editor.putString(Constants.PREFS_SECRET_TOKEN, token);
		    	editor.commit();
			} catch (AuthenticationException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (TooManyRequestsException e){
				e.printStackTrace();
			}
		}
	}
}