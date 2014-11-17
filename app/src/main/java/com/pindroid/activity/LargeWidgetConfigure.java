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

import com.pindroid.Constants;
import com.pindroid.R;
import com.pindroid.widget.SearchWidgetProvider;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.ListActivity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

public class LargeWidgetConfigure extends ListActivity {
	
	int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	
    private static final String PREFS_NAME = "com.pindroid.widget.SearchWidgetProvider";
    private static final String PREF_PREFIX_KEY_ACCOUNT = "account_";
    
    private String username = "";
	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED);

        // Set the view layout resource to use.
        setTitle(R.string.small_widget_configuration_description);
        



        // Find the widget id from the intent. 
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If they gave us an intent without the widget id, just bail.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
        
		if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH){
			Intent i = AccountManager.newChooseAccountIntent(null, null, new String[]{Constants.ACCOUNT_TYPE}, false, null, null, null, null);
			startActivityForResult(i, Constants.REQUEST_CODE_ACCOUNT_CHANGE);
		} else {
			if(AccountManager.get(this).getAccountsByType(Constants.ACCOUNT_TYPE).length > 0) {	
				Account account = AccountManager.get(this).getAccountsByType(Constants.ACCOUNT_TYPE)[0];
				
				username = account.name;
			}
		}
    }
	
    // Write the prefix to the SharedPreferences object for this widget
    static void saveAccountPref(Context context, int appWidgetId, String username) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY_ACCOUNT + appWidgetId, username);
        prefs.commit();
    }
    
    public static String loadAccountPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String account = prefs.getString(PREF_PREFIX_KEY_ACCOUNT + appWidgetId, null);
        return account;

    }
    
    public static void deleteAccountPref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY_ACCOUNT + appWidgetId);
        prefs.commit();
    }
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){	
		if(requestCode == Constants.REQUEST_CODE_ACCOUNT_CHANGE){
			username = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
			saveAccountPref(this, mAppWidgetId, username);
			
			// Push widget update to surface with newly set prefix
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            SearchWidgetProvider.updateAppWidget(this, appWidgetManager, mAppWidgetId);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
			
			
			finish();
		}
	}
}