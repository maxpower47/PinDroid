package com.android.droidlicious.activity;

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
		
		Intent tagBrowseIntent = new Intent(this, BrowseTags.class);
		tagBrowseIntent.putExtra("username", userName);
		
		startActivity(tagBrowseIntent);
		
		finish();
		
	}
}
