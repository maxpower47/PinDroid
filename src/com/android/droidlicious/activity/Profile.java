/*
 * Droidlicious - http://code.google.com/p/droidlicious/
 *
 * Copyright (C) 2010 Matt Schmidt
 *
 * Droidlicious is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * Droidlicious is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Droidlicious; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */

package com.android.droidlicious.activity;

import com.android.droidlicious.Constants;

import android.app.ActivityGroup;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.webkit.WebView;


public class Profile extends ActivityGroup {
	
	WebView mWebView;

	@Override
	public void onCreate(Bundle icicle){
		
		super.onCreate(icicle);
			
		Intent i = getIntent();
		Uri u = Uri.parse(i.getDataString());
		
		Cursor c = managedQuery(u, null, null, null, null);
		
		int userNameCol = c.getColumnIndex(ContactsContract.Data.DATA1);
		String userName = "";
        try {
            if (c.moveToFirst()) {
                userName = c.getString(userNameCol);
                Log.d("username", userName);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
		
		Intent tagBrowseIntent = new Intent();
		
		Uri.Builder data = Constants.CONTENT_URI_BASE.buildUpon();
		data.appendEncodedPath("tags");
		data.appendQueryParameter("username", userName);
		tagBrowseIntent.setData(data.build());
		
		Log.d("uri", data.build().toString());
		
		startActivity(tagBrowseIntent);
		
		finish();
		
	}
}
