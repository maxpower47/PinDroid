/*
 * DeliciousDroid - http://code.google.com/p/DeliciousDroid/
 *
 * Copyright (C) 2010 Matt Schmidt
 *
 * DeliciousDroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * DeliciousDroid is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DeliciousDroid; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */

package com.deliciousdroid.authenticator;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.deliciousdroid.Constants;

public class AuthToken {

	private String mToken;
	private Context mContext;
	private AccountManager mAccountManager;
	private Account mAccount;
	private Handler mHandler;
	
	
	public AuthToken(Context context, Account account){
		mToken = "";
		mContext = context;
		mAccountManager = AccountManager.get(mContext);
		mAccount = account;
	}
	
	public String getAuthToken(){
		try {
	    	String authtype = mAccountManager.getUserData(mAccount, Constants.PREFS_AUTH_TYPE);
			
	    	if(authtype.equals(Constants.AUTH_TYPE_OAUTH)) {
	    		mToken = mAccountManager.blockingGetAuthToken(mAccount, Constants.AUTHTOKEN_TYPE, false);
				mAccountManager.invalidateAuthToken(Constants.ACCOUNT_TYPE, mToken);
	    	}
	    	
			mToken = mAccountManager.blockingGetAuthToken(mAccount, Constants.AUTHTOKEN_TYPE, false);
		} catch (Exception e) {
			Log.d("AuthToken Error", "blah");
			e.printStackTrace();
		}
		return mToken;
	}
	
	public void getAuthTokenAsync(Handler handler){	
		Thread background = null;

		mHandler = handler;
		
		try {
			background = new Thread(new Runnable() {
				public void run(){
					
					try{			
						mToken = mAccountManager.blockingGetAuthToken(mAccount, Constants.AUTHTOKEN_TYPE, false);
						mAccountManager.invalidateAuthToken(Constants.ACCOUNT_TYPE, mToken);
						mToken = mAccountManager.blockingGetAuthToken(mAccount, Constants.AUTHTOKEN_TYPE, false);
						
						mHandler.sendMessage(mHandler.obtainMessage(1, mToken));
					}
					catch(Exception e){
						Log.d("authtokene", "blah");
					}
					return;
				}	
			});

			background.start();		
	
		} catch (Exception e) {
			Log.d("blah", "blah2");
		}

	}
}
