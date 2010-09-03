package com.android.droidlicious.activity;

import java.util.ArrayList;

import com.android.droidlicious.Constants;
import com.android.droidlicious.R;
import com.android.droidlicious.client.NetworkUtilities;
import com.android.droidlicious.client.User;
import com.android.droidlicious.listadapter.BookmarkListAdapter;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ListActivity;
import android.os.Bundle;

public class BrowseBookmarks extends ListActivity {

	AccountManager mAccountManager;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browse_bookmarks);
		
		ArrayList<User.Bookmark> bookmarkList = new ArrayList<User.Bookmark>();
		String authtoken = null;
		
		mAccountManager = AccountManager.get(this);
		Account[] al = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
		
		if(this.getIntent().hasExtra("username") && this.getIntent().hasExtra("tagname")){
			
			String username = getIntent().getStringExtra("username");
			String tagname = getIntent().getStringExtra("tagname");
			
			try{	
				authtoken = mAccountManager.blockingGetAuthToken(al[0], Constants.AUTHTOKEN_TYPE, true);
			
				bookmarkList = NetworkUtilities.fetchBookmarks(username, tagname, al[0], authtoken);
				
				setListAdapter(new BookmarkListAdapter(this, R.layout.bookmark_view, bookmarkList));	
			}
			catch(Exception e){}
	
		}
	
	}
}
