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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import com.pindroid.application.PindroidApplication;
import com.pindroid.authenticator.AuthenticatorActivity;
import com.pindroid.providers.BookmarkContentProvider;

public abstract class FragmentBaseActivity extends FragmentActivity {
	
	protected AccountManager mAccountManager;

	Bundle savedState;
	
	public PindroidApplication app;
	
	@Override
	@TargetApi(14)
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		app = (PindroidApplication)getApplicationContext();
		
		mAccountManager = AccountManager.get(this);

		init();
		
		if(android.os.Build.VERSION.SDK_INT >= 14) {
			if(getActionBar() != null) {
				getActionBar().setHomeButtonEnabled(true);
			}
		}
		
		Intent intent = getIntent();
		
		if(Intent.ACTION_SEARCH.equals(intent.getAction()) && !intent.hasExtra("MainSearchResults")){
			if(intent.hasExtra("username"))
				app.setUsername(intent.getStringExtra("username"));
			
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
	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void init(){
		
		if(mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE).length < 1) {		
			Intent i = new Intent(this, AuthenticatorActivity.class);
			startActivityForResult(i, 0);
			
			return;
		} else {			
			if(getIntent().getData() != null && getIntent().getData().getUserInfo() != null){
				app.setUsername(getIntent().getData().getUserInfo());
			} else if(app.getUsername() == null || app.getUsername().equals("")){
				
				if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH){
					Intent i = AccountManager.newChooseAccountIntent(null, null, new String[]{Constants.ACCOUNT_TYPE}, false, null, null, null, null);
					startActivityForResult(i, Constants.REQUEST_CODE_ACCOUNT_CHANGE);
				} else {
					if(mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE).length > 0) {	
						Account account = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE)[0];
						
						app.setUsername(account.name);
					}
				}
			}
		}
	}
	
	public void searchHandler(View v) {
		onSearchRequested();
	}
	
	@TargetApi(11)
	public void setupSearch(Menu menu){
	    if(android.os.Build.VERSION.SDK_INT >= 11) {
	    	SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
	    	SearchView searchView = (SearchView)menu.findItem(R.id.menu_search).getActionView();
	    	searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
	    	searchView.setSubmitButtonEnabled(false);
	    	
	    	searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

	    	    public boolean onQueryTextSubmit(String query) {
	    	    	startSearch(query);
	    	    	return true;
	    	    }

	    	    public boolean onQueryTextChange(final String s) {
	    	    	return false;
	    	    }
	    	});
	    }
	}
	
	// ******************************************
	// ******************************************
	// ******************************************
	//TODO test searching on < 3.0 devices
	//TODO test on tablet
	// ******************************************
	// ******************************************
	// ******************************************

	private void startSearch(final String query) {
		Intent i = new Intent(this, MainSearchResults.class);
		i.putExtra("query", query);
		Uri.Builder data = new Uri.Builder();
		data.scheme(Constants.CONTENT_SCHEME);
		data.encodedAuthority(app.getUsername() + "@" + BookmarkContentProvider.AUTHORITY);
		i.setData(data.build());
		startActivity(i);
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.base_menu, menu);
	    
	    return true;
	}
	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	            Intent intent = new Intent(this, Main.class);
	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
	            return true;
		    case R.id.menu_addbookmark:
				startActivity(IntentHelper.AddBookmark(null, app.getUsername(), this));
				return true;
		    case R.id.menu_search:
		    	onSearchRequested();
		    	return true;
		    case R.id.menu_settings:
				Intent prefs = new Intent(this, Preferences.class);
				startActivity(prefs);
		        return true;
		    case R.id.menu_choose_account:
				Intent i = AccountManager.newChooseAccountIntent(null, null, new String[]{Constants.ACCOUNT_TYPE}, true, null, null, null, null);
				startActivityForResult(i, Constants.REQUEST_CODE_ACCOUNT_CHANGE);
		    	return true;
		    default:
		        return super.onOptionsItemSelected(item);
	    }
	}
	
	public boolean isMyself() {
		for(Account account : mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE)){
			if(app.getUsername().equals(account.name))
				return true;
		}
		
		return false;
	}
	
    
    public Account getAccount(){
    	for(Account account : AccountManager.get(this).getAccountsByType(Constants.ACCOUNT_TYPE))
			   if (account.name.equals(app.getUsername()))
				   return account;
    	
    	return null;		   
    }
	
	@Override
	public void setTitle(CharSequence title){
		super.setTitle(title);

		if(this.findViewById(R.id.action_bar_title) != null) {
			((TextView)this.findViewById(R.id.action_bar_title)).setText(title);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){	
		if(resultCode == Activity.RESULT_CANCELED){
			finish();
		} else if(requestCode == Constants.REQUEST_CODE_ACCOUNT_CHANGE){
			app.setUsername(data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
		}
	}
}