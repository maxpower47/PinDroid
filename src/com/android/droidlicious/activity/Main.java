package com.android.droidlicious.activity;

import com.android.droidlicious.Constants;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

public class Main extends Activity {

	WebView mWebView;
	private AccountManager mAccountManager;
	private Account mAccount;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		mAccountManager = AccountManager.get(this);
		mAccount = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE)[0];
		
		Intent i = new Intent();
		Uri.Builder data = Constants.CONTENT_URI_BASE.buildUpon();
		data.appendEncodedPath("tags");
		data.appendQueryParameter("username", mAccount.name);
		i.setData(data.build());
		
		Log.d("uri", data.build().toString());
		
		startActivity(i);
		
		finish();
	}

}
