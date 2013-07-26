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
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.pindroid.Constants;
import com.pindroid.R;
import com.pindroid.application.PindroidApplication;
import com.pindroid.authenticator.AuthenticatorActivity;
import com.pindroid.util.AccountHelper;

public abstract class FragmentBaseActivity extends ActionBarActivity {
	
	protected AccountManager mAccountManager;

	Bundle savedState;
	
	public PindroidApplication app;
	
	static final String STATE_USERNAME = "username";
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		app = (PindroidApplication)getApplicationContext();
		
		mAccountManager = AccountManager.get(this);
		
		if(getSupportActionBar() != null) {
			getSupportActionBar().setHomeButtonEnabled(true);
		}
		
		Intent intent = getIntent();
		
		if(Intent.ACTION_SEARCH.equals(intent.getAction()) && !intent.hasExtra("MainSearchResults")){
			if(intent.hasExtra("username"))
				app.setUsername(intent.getStringExtra("username"));
			
			if(intent.hasExtra(SearchManager.QUERY)){
				//Intent i = new Intent(this, MainSearchResults.class);
				//i.putExtras(intent.getExtras());
				//startActivity(i);
				//finish();
			} else {
				onSearchRequested();
			}
		}
		
		init();
	}
	
	private void init(){
		if(getAccountCount() < 1) {		
			Intent i = new Intent(this, AuthenticatorActivity.class);
			startActivityForResult(i, 0);
			
			return;
		} else {			
			/*if(getIntent().getData() != null && getIntent().getData().getUserInfo() != null){
				app.setUsername(getIntent().getData().getUserInfo());
				Builder b = getIntent().getData().buildUpon();
				b.encodedAuthority(Constants.INTENT_URI);
				getIntent().setData(b.build());
				changeAccount();
			} else if(app.getUsername() == null || app.getUsername().equals("")){
				//requestAccount();
			}*/
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		init();
		
		if(app.getUsername() != null && getAccountCount() > 1){
			setSubtitle(app.getUsername());
		}
	}
	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	protected void requestAccount() {
		if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH){
			Intent i = AccountManager.newChooseAccountIntent(null, null, new String[]{Constants.ACCOUNT_TYPE}, false, null, null, null, null);
			i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivityForResult(i, Constants.REQUEST_CODE_ACCOUNT_CHANGE);
		} else {
			if(getAccountCount()  > 0) {	
				Account account = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE)[0];
				
				app.setUsername(account.name);
			}
		}
	}
	
	public void searchHandler(View v) {
		onSearchRequested();
	}
	
	public void setupSearch(Menu menu){
    	SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
    	MenuItem searchItem = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
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
	
	// ******************************************
	// ******************************************
	// ******************************************
	//TODO test searching on < 3.0 devices
	//TODO test on tablet
	// ******************************************
	// ******************************************
	// ******************************************

	protected abstract void startSearch(final String query);
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {	
		savedInstanceState.putString(STATE_USERNAME, app.getUsername());

	    super.onSaveInstanceState(savedInstanceState);
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
	    super.onRestoreInstanceState(savedInstanceState);

	    app.setUsername(savedInstanceState.getString(STATE_USERNAME));
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
    
    public int getAccountCount(){
    	return AccountHelper.getAccountCount(this);
    }
	
	private void setSubtitle(String subtitle){
		getSupportActionBar().setSubtitle(subtitle);
	}
	
	// signal to derived activity that the account may have changed
	protected abstract void changeAccount();
	
	protected void setAccount(String username){
		Log.d("setAccount", username);
		
		app.setUsername(username);
		
		if(getAccountCount() > 1)
			setSubtitle(app.getUsername());
		
		changeAccount();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){	
		if(resultCode == Activity.RESULT_CANCELED && requestCode != Constants.REQUEST_CODE_ACCOUNT_CHANGE){
			finish();
		} else if(resultCode == Activity.RESULT_OK && requestCode == Constants.REQUEST_CODE_ACCOUNT_CHANGE){
			setAccount(data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));	
		}
	}
}