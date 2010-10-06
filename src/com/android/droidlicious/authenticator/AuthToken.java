package com.android.droidlicious.authenticator;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.android.droidlicious.Constants;

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
			mToken = mAccountManager.blockingGetAuthToken(mAccount, Constants.AUTHTOKEN_TYPE, false);
			mAccountManager.invalidateAuthToken(Constants.ACCOUNT_TYPE, mToken);
			mToken = mAccountManager.blockingGetAuthToken(mAccount, Constants.AUTHTOKEN_TYPE, false);
		} catch (Exception e) {
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
