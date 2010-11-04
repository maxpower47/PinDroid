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

package com.deliciousdroid.activity;

import com.deliciousdroid.R;
import com.deliciousdroid.Constants;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.view.View;

public class Main extends AppBaseActivity {

	WebView mWebView;
	private AccountManager mAccountManager;
	private Account mAccount;
	private Context mContext;
	
	static final String[] MENU_ITEMS = new String[] {"View My Recent", "View My Tags", "View Network Recent"};
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setListAdapter(new ArrayAdapter<String>(this, R.layout.main_view, MENU_ITEMS));
		mContext = this;
		
		ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		lv.setOnItemClickListener(new OnItemClickListener() {
		    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		    	if(position == 0){
		    		mAccountManager = AccountManager.get(mContext);
		    		mAccount = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE)[0];
		    		
		    		Intent i = new Intent();
		    		Uri.Builder data = Constants.CONTENT_URI_BASE.buildUpon();
		    		data.appendEncodedPath("bookmarks");
		    		data.appendQueryParameter("username", mAccount.name);
		    		data.appendQueryParameter("recent", "1");
		    		i.setData(data.build());
		    		
		    		Log.d("uri", data.build().toString());
		    		
		    		startActivity(i);
		    	} else if(position == 1){
		    		mAccountManager = AccountManager.get(mContext);
		    		mAccount = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE)[0];
		    		
		    		Intent i = new Intent();
		    		Uri.Builder data = Constants.CONTENT_URI_BASE.buildUpon();
		    		data.appendEncodedPath("tags");
		    		data.appendQueryParameter("username", mAccount.name);
		    		i.setData(data.build());
		    		
		    		Log.d("uri", data.build().toString());
		    		
		    		startActivity(i);
		    	} else if(position == 2){
		    		mAccountManager = AccountManager.get(mContext);
		    		mAccount = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE)[0];
		    		
		    		Intent i = new Intent();
		    		Uri.Builder data = Constants.CONTENT_URI_BASE.buildUpon();
		    		data.appendEncodedPath("network");
		    		data.appendQueryParameter("username", mAccount.name);
		    		i.setData(data.build());
		    		
		    		Log.d("uri", data.build().toString());
		    		
		    		startActivity(i);
		    	}
		    	
		    }
		});

		
		

	}

}
