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

import java.util.ArrayList;

import com.deliciousdroid.Constants;
import com.deliciousdroid.R;
import com.deliciousdroid.platform.BookmarkManager;
import com.deliciousdroid.platform.TagManager;
import com.deliciousdroid.providers.BookmarkContentProvider;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class AppBaseActivity extends Activity {
	
	protected AccountManager mAccountManager;
	protected Account mAccount;
	protected Context mContext;
	protected String username = null;
	
	Bundle savedState;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		savedState = savedInstanceState;
		super.onCreate(savedState);
		
		mContext = this;
		mAccountManager = AccountManager.get(this);

    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
    	long lastUpdate = settings.getLong(Constants.PREFS_LAST_SYNC, 0);
		
		if(mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE).length < 1) {		
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.dialog_no_account_text)
			       .setCancelable(true)
			       .setTitle(R.string.dialog_no_account_title)
			       .setPositiveButton("Go", new DialogInterface.OnClickListener() {
			    	   public void onClick(DialogInterface dialog, int id) {
			    		   Intent i = new Intent(android.provider.Settings.ACTION_SYNC_SETTINGS);
			    		   startActivity(i);
			    		   finish();
			           }
			       })
			       .setOnCancelListener( new DialogInterface.OnCancelListener() {
						public void onCancel(DialogInterface dialog) {
							finish();
							
						}
				});
			
			AlertDialog alert = builder.create();
			alert.setIcon(android.R.drawable.ic_dialog_alert);
			alert.show();
			
			return;
		} else if(lastUpdate == 0) {
	
			Toast.makeText(this, "Syncing...", Toast.LENGTH_LONG).show();
			
			ContentResolver.requestSync(mAccount, BookmarkContentProvider.AUTHORITY, Bundle.EMPTY);
		} else {
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
		if(mAccount != null && username != null)
			return mAccount.name.equals(username);
		else return false;
	}
}