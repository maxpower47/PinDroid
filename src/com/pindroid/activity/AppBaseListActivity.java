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

import java.util.ArrayList;

import com.pindroid.R;
import com.pindroid.Constants;
import com.pindroid.action.GetSecretTask;
import com.pindroid.action.IntentHelper;
import com.pindroid.action.TaskArgs;
import com.pindroid.authenticator.AuthenticatorActivity;
import com.pindroid.platform.BookmarkManager;
import com.pindroid.platform.TagManager;
import com.pindroid.providers.BookmarkContentProvider;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class AppBaseListActivity extends ListActivity {
	
	protected AccountManager mAccountManager;
	protected Account mAccount;
	protected Context mContext;
	protected String username = null;
	protected SharedPreferences settings;
	
	protected long lastUpdate;
	protected boolean privateDefault;
	protected boolean toreadDefault;
	protected String defaultAction;
	protected boolean markAsRead;
	protected String secretToken;
	
	private boolean first = true;
	
	Bundle savedState;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		savedState = savedInstanceState;
		super.onCreate(savedState);
		
		mContext = this;
		mAccountManager = AccountManager.get(this);
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		
		loadSettings();
		init();
		loadSecret();
	}
	
	private void init(){
		
		if(mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE).length < 1) {		
			Intent i = new Intent(this, AuthenticatorActivity.class);
			startActivity(i);
			
			return;
		} else if(lastUpdate == 0) {

			if(mAccount == null || username == null)
				loadAccounts();
			
			if(!ContentResolver.isSyncActive(mAccount, BookmarkContentProvider.AUTHORITY) &&
				!ContentResolver.isSyncPending(mAccount, BookmarkContentProvider.AUTHORITY)) {
				
				Toast.makeText(this, getString(R.string.syncing_toast), Toast.LENGTH_LONG).show();
				
				ContentResolver.requestSync(mAccount, BookmarkContentProvider.AUTHORITY, Bundle.EMPTY);
			}
		} else {
			loadAccounts();
		}
	}
	

	public void searchHandler(View v) {
		onSearchRequested();
	}

	
	@Override
	public void onResume(){
		super.onResume();
		
		if(!first) {
			loadSettings();
			init();
		}
		first = false;
		
	}
	
	private void loadAccounts(){
		if(mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE).length > 0) {	
			mAccount = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE)[0];
		}
		
		ArrayList<String> accounts = new ArrayList<String>();
		
		for(Account a : mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE)) {
			accounts.add(a.name);
		}
		
		BookmarkManager.TruncateBookmarks(accounts, this, true);
		TagManager.TruncateOldTags(accounts, this);
		
		username = mAccount.name;
	}
	
	private void loadSettings(){
    	lastUpdate = settings.getLong(Constants.PREFS_LAST_SYNC, 0);
    	privateDefault = settings.getBoolean("pref_save_private_default", false);
    	toreadDefault = settings.getBoolean("pref_save_toread_default", false);
    	defaultAction = settings.getString("pref_view_bookmark_default_action", "browser");
    	markAsRead = settings.getBoolean("pref_markasread", false);
	}
	
	private void loadSecret(){
        secretToken = settings.getString(Constants.PREFS_SECRET_TOKEN, "");
		
        if(secretToken.equals("") && mAccount != null){
			new GetSecretTask().execute(new TaskArgs(mAccount, this));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_menu, menu);
	    
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.menu_addbookmark:
			startActivity(IntentHelper.AddBookmark(null, mAccount.name, this));
			return true;
	    case R.id.menu_search:
	    	onSearchRequested();
	    	return true;
	    case R.id.menu_settings:
			Intent prefs = new Intent(this, Preferences.class);
			startActivity(prefs);
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	protected boolean isMyself() {
		if(mAccount != null && username != null)
			return mAccount.name.equals(username);
		else return false;
	}
	
	@Override
	public void setTitle(CharSequence title){
		super.setTitle(title);
		Log.d("got", "here");
		if(this.findViewById(R.id.action_bar_title) != null) {
			((TextView)this.findViewById(R.id.action_bar_title)).setText(title);
		}
	}
}