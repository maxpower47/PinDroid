package com.android.droidlicious.activity;

import java.util.ArrayList;

import com.android.droidlicious.Constants;
import com.android.droidlicious.R;
import com.android.droidlicious.client.NetworkUtilities;
import com.android.droidlicious.listadapter.BookmarkListAdapter;
import com.android.droidlicious.providers.BookmarkContent.Bookmark;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class BrowseBookmarks extends DroidliciousBaseActivity {
	
	private AccountManager mAccountManager;
	private Account mAccount;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browse_bookmarks);
		
		mAccountManager = AccountManager.get(this);
		mAccount = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE)[0];
		
		Log.d("browse bookmarks", getIntent().getDataString());
		Uri data = getIntent().getData();
		String username = data.getQueryParameter("username");
		String tagname = data.getQueryParameter("tagname");
		
		ArrayList<Bookmark> bookmarkList = new ArrayList<Bookmark>();
		
		if(mAccount.name.equals(username)){
			
			try{	
				
				String[] projection = new String[] {Bookmark.Url, Bookmark.Description, Bookmark.Meta, Bookmark.Tags};
				String selection = Bookmark.Tags + " LIKE '% " + tagname + " %' OR " +
					Bookmark.Tags + " LIKE '% " + tagname + "' OR " +
					Bookmark.Tags + " LIKE '" + tagname + " %' OR " +
					Bookmark.Tags + " = '" + tagname + "'";
				
				Log.d("selection", selection);
				
				Uri bookmarks = Bookmark.CONTENT_URI;
				
				Cursor c = managedQuery(bookmarks, projection, selection, null, null);				
				
				if(c.moveToFirst()){
					
					int urlColumn = c.getColumnIndex(Bookmark.Url);
					int descriptionColumn = c.getColumnIndex(Bookmark.Description);
					int tagsColumn = c.getColumnIndex(Bookmark.Tags);
					int metaColumn = c.getColumnIndex(Bookmark.Meta);
					
					do {
						
						Bookmark b = new Bookmark(c.getString(urlColumn), 
								c.getString(descriptionColumn), "", c.getString(tagsColumn), "", 
								c.getString(metaColumn));
						
						
						bookmarkList.add(b);
						
					} while(c.moveToNext());
					
					
				}

				setListAdapter(new BookmarkListAdapter(this, R.layout.bookmark_view, bookmarkList));	
			}
			catch(Exception e){}
			
			ListView lv = getListView();
			lv.setTextFilterEnabled(true);
		
			lv.setOnItemClickListener(new OnItemClickListener() {
			    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			    	String url = ((TextView)view.findViewById(R.id.bookmark_url)).getText().toString();
			    	Uri link = Uri.parse(url);
			    	
					Intent i = new Intent(Intent.ACTION_VIEW, link);
					
					startActivity(i);
			    }
			});
		} else {

			try{	
				
				 bookmarkList = NetworkUtilities.fetchFriendBookmarks(username, tagname);

				setListAdapter(new BookmarkListAdapter(this, R.layout.bookmark_view, bookmarkList));	
			}
			catch(Exception e){}
			
			ListView lv = getListView();
			lv.setTextFilterEnabled(true);
		
			lv.setOnItemClickListener(new OnItemClickListener() {
			    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			    	String url = ((TextView)view.findViewById(R.id.bookmark_url)).getText().toString();
			    	Uri link = Uri.parse(url);
			    	
					Intent i = new Intent(Intent.ACTION_VIEW, link);
					
					startActivity(i);
			    }
			});	
		}
	}
}
