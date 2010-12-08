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

import com.deliciousdroid.R;
import com.deliciousdroid.Constants;
import com.deliciousdroid.platform.BookmarkManager;
import com.deliciousdroid.platform.TagManager;
import com.deliciousdroid.providers.BookmarkContentProvider;

import android.accounts.Account;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.view.View;

public class Main extends AppBaseActivity {
	
	static final String[] MENU_ITEMS = new String[] {"View My Recent", "View My Tags", 
		"View Network Recent", "View Hotlist", "View Popular"};
	
	Bundle savedState;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		savedState = savedInstanceState;
		super.onCreate(savedState);
		init();
	}
	
	private void init(){
		setListAdapter(new ArrayAdapter<String>(this, R.layout.main_view, MENU_ITEMS));

		Intent intent = getIntent();

    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
    	long lastUpdate = settings.getLong(Constants.PREFS_LAST_SYNC, 0);
		
		if(mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE).length < 1) {		
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.dialog_no_account_text)
			       .setCancelable(false)
			       .setTitle(R.string.dialog_no_account_title)
			       .setPositiveButton("Go", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			   			Intent i = new Intent(android.provider.Settings.ACTION_SYNC_SETTINGS);
						startActivityForResult(i, 0);
						
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
			username = mAccount.name;
		}
		
		ArrayList<String> accounts = new ArrayList<String>();
		
		for(Account a : mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE)) {
			accounts.add(a.name);
		}
		
		BookmarkManager.TruncateBookmarks(accounts, this, true);
		TagManager.TruncateOldTags(accounts, this);

		if(Intent.ACTION_SEARCH.equals(intent.getAction())){
			Intent i = new Intent(mContext, MainSearchResults.class);
			i.putExtras(getIntent().getExtras());
			startActivity(i);
			finish();
		} else if(Intent.ACTION_VIEW.equals(intent.getAction())) {
			
			Uri data = intent.getData();
			String path = null;
			String tagname = null;
			
			if(data != null) {
				path = data.getPath();
				tagname = data.getQueryParameter("tagname");
			}
			
			if(path.contains("bookmarks") && TextUtils.isDigitsOnly(data.getLastPathSegment())) {
				Intent viewBookmark = new Intent(this, ViewBookmark.class);
				viewBookmark.setData(data);
				
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
		
		ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		lv.setOnItemClickListener(new OnItemClickListener() {
		    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		    	if(position == 0){
		    		
		    		Intent i = new Intent();
		    		i.setAction(Intent.ACTION_VIEW);
		    		i.addCategory(Intent.CATEGORY_DEFAULT);
		    		Uri.Builder data = new Uri.Builder();
		    		data.scheme(Constants.CONTENT_SCHEME);
		    		data.encodedAuthority(mAccount.name + "@" + BookmarkContentProvider.AUTHORITY);
		    		data.appendEncodedPath("bookmarks");
		    		i.setData(data.build());
		    		
		    		Log.d("uri", data.build().toString());
		    		
		    		startActivity(i);
		    	} else if(position == 1){
		    		
		    		Intent i = new Intent();
		    		i.setAction(Intent.ACTION_VIEW);
		    		i.addCategory(Intent.CATEGORY_DEFAULT);
		    		Uri.Builder data = new Uri.Builder();
		    		data.scheme(Constants.CONTENT_SCHEME);
		    		data.encodedAuthority(mAccount.name + "@" + BookmarkContentProvider.AUTHORITY);
		    		data.appendEncodedPath("tags");
		    		i.setData(data.build());
		    		
		    		Log.d("uri", data.build().toString());
		    		
		    		startActivity(i);
		    	} else if(position == 2){
		    		
		    		Intent i = new Intent();
		    		i.setAction(Intent.ACTION_VIEW);
		    		i.addCategory(Intent.CATEGORY_DEFAULT);
		    		Uri.Builder data = new Uri.Builder();
		    		data.scheme(Constants.CONTENT_SCHEME);
		    		data.encodedAuthority("network@" + BookmarkContentProvider.AUTHORITY);
		    		data.appendEncodedPath("bookmarks");
		    		i.setData(data.build());
		    		
		    		Log.d("uri", data.build().toString());
		    		
		    		startActivity(i);
		    	} else if(position == 3){
		    		
		    		Intent i = new Intent();
		    		i.setAction(Intent.ACTION_VIEW);
		    		i.addCategory(Intent.CATEGORY_DEFAULT);
		    		Uri.Builder data = new Uri.Builder();
		    		data.scheme(Constants.CONTENT_SCHEME);
		    		data.encodedAuthority("hotlist@" + BookmarkContentProvider.AUTHORITY);
		    		data.appendEncodedPath("bookmarks");
		    		i.setData(data.build());
		    		
		    		Log.d("uri", data.build().toString());
		    		
		    		startActivity(i);
		    	} else if(position == 4){
		    		
		    		Intent i = new Intent();
		    		i.setAction(Intent.ACTION_VIEW);
		    		i.addCategory(Intent.CATEGORY_DEFAULT);
		    		Uri.Builder data = new Uri.Builder();
		    		data.scheme(Constants.CONTENT_SCHEME);
		    		data.encodedAuthority("popular@" + BookmarkContentProvider.AUTHORITY);
		    		data.appendEncodedPath("bookmarks");
		    		i.setData(data.build());
		    		
		    		Log.d("uri", data.build().toString());
		    		
		    		startActivity(i);
		    	}
		    }
		});
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		this.onCreate(savedState);
	}
}