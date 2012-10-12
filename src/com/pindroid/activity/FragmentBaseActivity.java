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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;

import com.pindroid.Constants;
import com.pindroid.R;
import com.pindroid.action.IntentHelper;
import com.pindroid.authenticator.AuthenticatorActivity;

public abstract class FragmentBaseActivity extends FragmentActivity {
	
	protected AccountManager mAccountManager;
	public Account mAccount;
	public Context mContext;
	public String username = null;
	protected SharedPreferences settings;
	
	public boolean privateDefault;
	public boolean toreadDefault;
	public String defaultAction;
	public boolean markAsRead;
	public String readingBackground;
	public String readingFont;
	public String readingMargins;
	public String readingFontSize;
	public String readingLineSpace;
	
	private boolean first = true;
	
	Bundle savedState;
	
	@Override
	@TargetApi(14)
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		mContext = this;
		mAccountManager = AccountManager.get(this);
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		
		loadSettings();
		init();
		
		if(android.os.Build.VERSION.SDK_INT >= 14) {
			if(getActionBar() != null) {
				getActionBar().setHomeButtonEnabled(true);
			}
		}
		
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
		} else if(Constants.ACTION_SEARCH_SUGGESTION_VIEW.equals(intent.getAction())) {
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
			} else if(path.contains("notes") && TextUtils.isDigitsOnly(data.getLastPathSegment()) && intent.hasExtra(SearchManager.USER_QUERY)){
				Intent viewNote = new Intent(this, ViewNote.class);
				viewNote.setAction(Intent.ACTION_VIEW);
				viewNote.setData(data);
				viewNote.removeExtra(SearchManager.USER_QUERY);
				Log.d("View Note Uri", data.toString());
				startActivity(viewNote);
				finish();
			}
		} else if(Constants.ACTION_SEARCH_SUGGESTION_EDIT.equals(intent.getAction())){
			Uri data = intent.getData();

			Intent editBookmark = new Intent(this, AddBookmark.class);
			editBookmark.setAction(Intent.ACTION_EDIT);
			editBookmark.setData(data);
			editBookmark.removeExtra(SearchManager.USER_QUERY);
			Log.d("Edit Bookmark Uri", data.toString());
			startActivity(editBookmark);
			finish();
		}
	}
	
	private void init(){
		
		if(mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE).length < 1) {		
			Intent i = new Intent(this, AuthenticatorActivity.class);
			startActivityForResult(i, 0);
			
			return;
		} else {
			if(mAccount == null || username == null)
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
	
	@TargetApi(11)
	public void setupSearch(Menu menu){
	    if(android.os.Build.VERSION.SDK_INT >= 11) {
	    	SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
	    	SearchView searchView = (SearchView)menu.findItem(R.id.menu_search).getActionView();
	    	searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
	    	searchView.setSubmitButtonEnabled(false);
	    }
	}
	
	private void loadSettings(){
    	privateDefault = settings.getBoolean("pref_save_private_default", false);
    	toreadDefault = settings.getBoolean("pref_save_toread_default", false);
    	defaultAction = settings.getString("pref_view_bookmark_default_action", "browser");
    	markAsRead = settings.getBoolean("pref_markasread", false);
    	readingBackground = settings.getString("pref_reading_background", "-1");
    	readingFont = settings.getString("pref_reading_font", "Roboto-Regular");
    	readingMargins = settings.getString("pref_reading_margins", "20");
    	readingFontSize = settings.getString("pref_reading_fontsize", "16");
    	readingLineSpace = settings.getString("pref_reading_linespace", "5");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.base_menu, menu);
	    
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
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data){	
		if(resultCode == Activity.RESULT_CANCELED){
			finish();
		}
	}
}