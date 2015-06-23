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

import android.accounts.AccountManager;
import android.app.ListActivity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.pindroid.Constants;
import com.pindroid.R;
import com.pindroid.widget.SearchWidgetProvider;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;

@EActivity
public class LargeWidgetConfigure extends ListActivity {

    private static final String PREFS_NAME = "com.pindroid.widget.SearchWidgetProvider";
    private static final String PREF_PREFIX_KEY_ACCOUNT = "account_";

    @Extra(AppWidgetManager.EXTRA_APPWIDGET_ID) int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

	@AfterInject
    public void init() {
        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED);

        // Set the view layout resource to use.
        setTitle(R.string.small_widget_configuration_description);

        // If they gave us an intent without the widget id, just bail.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        Intent i = AccountManager.newChooseAccountIntent(null, null, new String[]{Constants.ACCOUNT_TYPE}, false, null, null, null, null);
        startActivityForResult(i, Constants.REQUEST_CODE_ACCOUNT_CHANGE);
    }
	
    // Write the prefix to the SharedPreferences object for this widget
    static void saveAccountPref(Context context, int appWidgetId, String username) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY_ACCOUNT + appWidgetId, username);
        prefs.commit();
    }
    
    public static String loadAccountPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getString(PREF_PREFIX_KEY_ACCOUNT + appWidgetId, null);
    }
    
    public static void deleteAccountPref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY_ACCOUNT + appWidgetId);
        prefs.commit();
    }

    @OnActivityResult(Constants.REQUEST_CODE_ACCOUNT_CHANGE)
    void onResult(@OnActivityResult.Extra(value = AccountManager.KEY_ACCOUNT_NAME) String username) {
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