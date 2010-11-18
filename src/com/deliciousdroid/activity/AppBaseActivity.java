/*
 * DeliciousDroid - http://code.google.com/p/DeliciousDroid/
 *
 * Copyright (C) 2010 Matt Schmidt
 *
 * DeliciousDroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
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

import com.deliciousdroid.Constants;
import com.deliciousdroid.R;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class AppBaseActivity extends ListActivity {
	
	protected AccountManager mAccountManager;
	protected Account mAccount;
	protected Context mContext;
	protected String username = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		mAccountManager = AccountManager.get(this);
		if(mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE).length > 0) {	
			mAccount = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE)[0];
		}
		mContext = this;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_menu, menu);
	    
	    if(!isMyself()) {
	    	menu.findItem(R.id.menu_search).setEnabled(false);
	    }
	    
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.menu_addbookmark:
			Intent addBookmark = new Intent(this, AddBookmark.class);
			startActivity(addBookmark);
			return true;
	    case R.id.menu_search:			
			this.onSearchRequested();
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
		return mAccount.name.equals(username);
	}
}