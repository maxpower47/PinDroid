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
import com.pindroid.action.IntentHelper;
import com.pindroid.authenticator.AuthenticatorActivity;
import com.pindroid.providers.BookmarkContentProvider;

import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class FragmentBaseActivity extends FragmentActivity {
	
	protected AccountManager mAccountManager;
	public Account mAccount;
	public Context mContext;
	public String username = null;
	protected SharedPreferences settings;
	
	protected long lastUpdate;
	public boolean privateDefault;
	public boolean toreadDefault;
	public String defaultAction;
	public boolean markAsRead;
	public String secretToken;
	
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

		Intent intent = getIntent();
		
		if(Intent.ACTION_SEARCH.equals(intent.getAction()) && !intent.hasExtra("MainSearchResults")){
			if(intent.hasExtra(SearchManager.QUERY)){
				Intent i = new Intent(this, MainSearchResults.class);
				i.putExtras(intent.getExtras());
				startActivity(i);
				finish();
			} else {
				onSearchRequested();
			}
		} else if(Intent.ACTION_VIEW.equals(intent.getAction())) {
			
			Uri data = intent.getData();
			String path = null;
			String tagname = null;
			
			if(data != null) {
				path = data.getPath();
				tagname = data.getQueryParameter("tagname");
			}
			
			if(data.getScheme() == null || !data.getScheme().equals("content")){
				Intent i = new Intent(Intent.ACTION_VIEW, data);
				startActivity(i);
				finish();				
			} else if(path.contains("bookmarks") && TextUtils.isDigitsOnly(data.getLastPathSegment()) && intent.hasExtra(SearchManager.USER_QUERY)) {
				Intent viewBookmark = new Intent(this, ViewBookmark.class);
				viewBookmark.setAction(Intent.ACTION_VIEW);
				viewBookmark.setData(data);
				viewBookmark.removeExtra(SearchManager.USER_QUERY);
				Log.d("View Bookmark Uri", data.toString());
				startActivity(viewBookmark);
				finish();
			} else if(tagname != null) {
				Intent viewTags = new Intent(this, BrowseBookmarks.class);
				viewTags.setData(data);
				
				Log.d("View Tags Uri", data.toString());
				startActivity(viewTags);
				finish();
			}
		}
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
		
		username = mAccount.name;
	}
	
	private void loadSettings(){
    	lastUpdate = settings.getLong(Constants.PREFS_LAST_SYNC, 0);
    	privateDefault = settings.getBoolean("pref_save_private_default", false);
    	toreadDefault = settings.getBoolean("pref_save_toread_default", false);
    	defaultAction = settings.getString("pref_view_bookmark_default_action", "browser");
    	markAsRead = settings.getBoolean("pref_markasread", false);
    	secretToken = settings.getString(Constants.PREFS_SECRET_TOKEN, "");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_menu, menu);
	    
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	            Intent intent = new Intent(this, Main.class);
	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
	            return true;
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
	
	public boolean isMyself() {
		if(mAccount != null && username != null)
			return mAccount.name.equals(username);
		else return false;
	}
	
	@Override
	public void setTitle(CharSequence title){
		super.setTitle(title);

		if(this.findViewById(R.id.action_bar_title) != null) {
			((TextView)this.findViewById(R.id.action_bar_title)).setText(title);
		}
	}
}