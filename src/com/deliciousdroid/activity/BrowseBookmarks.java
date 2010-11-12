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
import com.deliciousdroid.action.BookmarkTaskArgs;
import com.deliciousdroid.action.DeleteBookmarkTask;
import com.deliciousdroid.client.DeliciousFeed;
import com.deliciousdroid.listadapter.BookmarkListAdapter;
import com.deliciousdroid.providers.BookmarkContentProvider;
import com.deliciousdroid.providers.BookmarkContent.Bookmark;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class BrowseBookmarks extends AppBaseActivity {
	
	private AccountManager mAccountManager;
	private Account mAccount;
	private ListView lv;
	private Context mContext;
	private Boolean myself;
	
	private String bookmarkLimit;
	private String defaultAction;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browse_bookmarks);
		
		mAccountManager = AccountManager.get(this);
		mAccount = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE)[0];
		mContext = this;
		
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
    	bookmarkLimit = settings.getString("pref_contact_bookmark_results", "50");
    	defaultAction = settings.getString("pref_view_bookmark_default_action", "browser");
		
		Log.d("browse bookmarks", getIntent().getDataString());
		Uri data = getIntent().getData();
		String scheme = data.getScheme();
		String path = data.getPath();
		Log.d("path", path);
		String username = data.getUserInfo();
		String tagname = data.getQueryParameter("tagname");
		String recent = data.getQueryParameter("recent");
		
		myself = mAccount.name.equals(username);
		
		ArrayList<Bookmark> bookmarkList = new ArrayList<Bookmark>();
		
		if(scheme.equals("content") && path.equals("/bookmarks") && myself){
			
			try{	
				if(tagname != null && tagname != "") {
					setTitle("My Bookmarks Tagged With " + tagname);
				} else {
					setTitle("My Bookmarks");
				}
				
				String[] projection = new String[] {Bookmark._ID, Bookmark.Url, Bookmark.Description, Bookmark.Meta, Bookmark.Tags};
				String selection = null;
				String sortorder = null;
				
				if(tagname != null && tagname != "") {
					selection = "(" + Bookmark.Tags + " LIKE '% " + tagname + " %' OR " +
						Bookmark.Tags + " LIKE '% " + tagname + "' OR " +
						Bookmark.Tags + " LIKE '" + tagname + " %' OR " +
						Bookmark.Tags + " = '" + tagname + "') AND " +
						Bookmark.Account + " = '" + username + "'";
				}
				
				if(recent != null && recent.equals("1")){
					sortorder = Bookmark.Time + " DESC";
				}
				
				Uri bookmarks = Bookmark.CONTENT_URI;
				
				Cursor c = managedQuery(bookmarks, projection, selection, null, sortorder);				
				
				if(c.moveToFirst()){
					int idColumn = c.getColumnIndex(Bookmark._ID);
					int urlColumn = c.getColumnIndex(Bookmark.Url);
					int descriptionColumn = c.getColumnIndex(Bookmark.Description);
					int tagsColumn = c.getColumnIndex(Bookmark.Tags);
					int metaColumn = c.getColumnIndex(Bookmark.Meta);
					
					do {
						
						Bookmark b = new Bookmark(c.getInt(idColumn), c.getString(urlColumn), 
								c.getString(descriptionColumn), "", c.getString(tagsColumn), "", 
								c.getString(metaColumn), 0);
						
						bookmarkList.add(b);
						
					} while(c.moveToNext());
						
				}

				setListAdapter(new BookmarkListAdapter(this, R.layout.bookmark_view, bookmarkList));	
			}
			catch(Exception e){}
			
		} else if(scheme.equals("content") && path.equals("/bookmarks")) {
			try{
				if(tagname != null && tagname != "") {
					setTitle("Bookmarks For " + username + " Tagged With " + tagname);
				} else {
					setTitle("Bookmarks For " + username);
				}
		    	
				bookmarkList = DeliciousFeed.fetchFriendBookmarks(username, tagname, Integer.parseInt(bookmarkLimit));

				setListAdapter(new BookmarkListAdapter(this, R.layout.bookmark_view, bookmarkList));	
			}
			catch(Exception e){}
		} else if(scheme.equals("content") && path.equals("/network")){
			try{
				setTitle("My Network's Recent Bookmarks");
				
				bookmarkList = DeliciousFeed.fetchNetworkRecent(username);

				setListAdapter(new BookmarkListAdapter(this, R.layout.bookmark_view, bookmarkList));	
			}
			catch(Exception e){}
		} else if(scheme.equals("http") || scheme.equals("https")) {
			String url = data.toString();
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(url));
			startActivity(i);
			finish();
		}
		
		lv = getListView();
		lv.setTextFilterEnabled(true);
	
		lv.setOnItemClickListener(new OnItemClickListener() {
		    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		    	Bookmark b = (Bookmark)lv.getItemAtPosition(position);

		    	if(defaultAction.equals("view")) {
		    		viewBookmark(b);
		    	} else {
		    		openBookmarkInBrowser(b);
		    	}
		    }
		});
		
		/* Add Context-Menu listener to the ListView. */
		lv.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				menu.setHeaderTitle("Actions");
				if(myself){
					menu.add(Menu.NONE, 0, Menu.NONE, "Open in browser");
					menu.add(Menu.NONE, 1, Menu.NONE, "View Details");
					menu.add(Menu.NONE, 2, Menu.NONE, "Delete");
				} else {
					menu.add(Menu.NONE, 3, Menu.NONE, "Add");
				}
			}
		});
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem aItem) {
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) aItem.getMenuInfo();
		final Bookmark b = (Bookmark)lv.getItemAtPosition(menuInfo.position);
		
		switch (aItem.getItemId()) {
			case 0:
				openBookmarkInBrowser(b);
				return true;
			case 1:				
				viewBookmark(b);
				return true;
			
			case 2:
				BookmarkTaskArgs args = new BookmarkTaskArgs(b, mAccount, mContext);	
				new DeleteBookmarkTask().execute(args);
				
				BookmarkListAdapter bla = (BookmarkListAdapter) lv.getAdapter();
				bla.remove(b);
				return true;
				
			case 3:				
				Intent addBookmark = new Intent(this, AddBookmark.class);
				addBookmark.setAction(Intent.ACTION_SEND);
				addBookmark.putExtra(Intent.EXTRA_TEXT, b.getUrl());
				startActivity(addBookmark);
				return true;
		}
		return false;
	}
	
	private void openBookmarkInBrowser(Bookmark b) {
    	String url = b.getUrl();
    	Uri link = Uri.parse(url);
		Intent i = new Intent(Intent.ACTION_VIEW, link);
		
		startActivity(i);
	}
	
	private void viewBookmark(Bookmark b) {
		Intent viewBookmark = new Intent(this, ViewBookmark.class);
		Uri.Builder data = new Uri.Builder();
		data.scheme(Constants.CONTENT_SCHEME);
		data.encodedAuthority(mAccount.name + "@" + BookmarkContentProvider.AUTHORITY);
		data.appendEncodedPath("bookmarks");
		data.appendEncodedPath(Integer.toString(b.getId()));
		viewBookmark.setData(data.build());
		
		Log.d("View Bookmark Uri", data.build().toString());
		startActivity(viewBookmark);
	}
}


