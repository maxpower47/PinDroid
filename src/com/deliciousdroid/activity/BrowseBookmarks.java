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

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.ParseException;
import org.apache.http.auth.AuthenticationException;
import org.json.JSONException;

import com.deliciousdroid.R;
import com.deliciousdroid.Constants;
import com.deliciousdroid.action.BookmarkTaskArgs;
import com.deliciousdroid.action.DeleteBookmarkTask;
import com.deliciousdroid.client.DeliciousFeed;
import com.deliciousdroid.listadapter.BookmarkListAdapter;
import com.deliciousdroid.platform.BookmarkManager;
import com.deliciousdroid.providers.BookmarkContentProvider;
import com.deliciousdroid.providers.BookmarkContent.Bookmark;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
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
	
	private String bookmarkLimit;
	private String defaultAction;
	
	private String username;
	private String tagname = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browse_bookmarks);
		
		Intent intent = getIntent();
		
		mAccountManager = AccountManager.get(this);
		mAccount = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE)[0];
		mContext = this;
		
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
    	bookmarkLimit = settings.getString("pref_contact_bookmark_results", "50");
    	defaultAction = settings.getString("pref_view_bookmark_default_action", "browser");
		
    	ArrayList<Bookmark> bookmarkList = new ArrayList<Bookmark>();


		Uri data = intent.getData();
		String path = null;
		
		if(data != null) {
			path = data.getPath();
			username = data.getUserInfo();
			tagname = data.getQueryParameter("tagname");
		}
		
    	if(Intent.ACTION_SEARCH.equals(intent.getAction())) {
    		Bundle searchData = intent.getBundleExtra(SearchManager.APP_DATA);
    		String tag = null;
    		if(searchData != null) {
    			tag = searchData.getString("tagname");
    			username = searchData.getString("username");
    		}
    		
    		String query = intent.getStringExtra(SearchManager.QUERY);
    		
    		setTitle("Bookmark Search Results For \"" + query + "\"");
    		
    		if(isMyself()) {
    			bookmarkList = BookmarkManager.SearchBookmarks(query, tag, username, this);
    		
    			setListAdapter(new BookmarkListAdapter(this, R.layout.bookmark_view, bookmarkList));
    		}
    		
    	} else if(path.equals("/bookmarks") && isMyself()) {
    		
			if(tagname != null && tagname != "") {
				setTitle("My Bookmarks Tagged With " + tagname);
			} else {
				setTitle("My Bookmarks");
			}
			
			bookmarkList = BookmarkManager.GetBookmarks(username, tagname, this);

			setListAdapter(new BookmarkListAdapter(this, R.layout.bookmark_view, bookmarkList));	

			
		} else if(path.equals("/bookmarks")) {
			try{
				if(tagname != null && tagname != "") {
					setTitle("Bookmarks For " + username + " Tagged With " + tagname);
				} else {
					setTitle("Bookmarks For " + username);
				}
		    	
				new LoadBookmarkFeedTask().execute(username, tagname);
			}
			catch(Exception e){}
		} else if(username.equals("network")){
			try{
				setTitle("My Network's Recent Bookmarks");
				
				new LoadBookmarkFeedTask().execute("network");
			}
			catch(Exception e){}
		} else if(path.contains("bookmarks") && TextUtils.isDigitsOnly(data.getLastPathSegment())) {
			viewBookmark(Integer.parseInt(data.getLastPathSegment()));
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
				if(isMyself()){
					menu.add(Menu.NONE, 0, Menu.NONE, "Open in browser");
					menu.add(Menu.NONE, 1, Menu.NONE, "View Details");
					menu.add(Menu.NONE, 2, Menu.NONE, "Edit");
					menu.add(Menu.NONE, 3, Menu.NONE, "Delete");
				} else {
					menu.add(Menu.NONE, 0, Menu.NONE, "Open in browser");
					menu.add(Menu.NONE, 1, Menu.NONE, "View Details");
					menu.add(Menu.NONE, 4, Menu.NONE, "Add");
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
				Intent editBookmark = new Intent(this, AddBookmark.class);
				editBookmark.setAction(Intent.ACTION_EDIT);
				
				Uri.Builder data = new Uri.Builder();
				data.scheme(Constants.CONTENT_SCHEME);
				data.encodedAuthority(mAccount.name + "@" + BookmarkContentProvider.AUTHORITY);
				data.appendEncodedPath("bookmarks");
				data.appendEncodedPath(Integer.toString(b.getId()));
				editBookmark.setData(data.build());

				startActivity(editBookmark);
				return true;
			
			case 3:
				BookmarkTaskArgs args = new BookmarkTaskArgs(b, mAccount, mContext);	
				new DeleteBookmarkTask().execute(args);
				
				BookmarkListAdapter bla = (BookmarkListAdapter) lv.getAdapter();
				bla.remove(b);
				return true;
				
			case 4:				
				Intent addBookmark = new Intent(this, AddBookmark.class);
				addBookmark.setAction(Intent.ACTION_SEND);
				addBookmark.putExtra(Intent.EXTRA_TEXT, b.getUrl());
				startActivity(addBookmark);
				return true;
		}
		return false;
	}
	
	@Override
	public boolean onSearchRequested() {
		Bundle contextData = new Bundle();
		contextData.putString("tagname", tagname);
		contextData.putString("username", username);
		startSearch(null, false, contextData, false);
		return true;
	}
	
	private void openBookmarkInBrowser(Bookmark b) {
    	String url = b.getUrl();
    	Uri link = Uri.parse(url);
		Intent i = new Intent(Intent.ACTION_VIEW, link);
		
		startActivity(i);
	}
	
	private void viewBookmark(int id) {
		Bookmark b = new Bookmark(id);
		viewBookmark(b);
	}
	
	private void viewBookmark(Bookmark b) {
		Intent viewBookmark = new Intent(this, ViewBookmark.class);
		Uri.Builder data = new Uri.Builder();
		data.scheme(Constants.CONTENT_SCHEME);
		data.encodedAuthority(username + "@" + BookmarkContentProvider.AUTHORITY);
		data.appendEncodedPath("bookmarks");
		
		if(isMyself()) {
			data.appendEncodedPath(Integer.toString(b.getId()));
		} else {
			data.appendQueryParameter("url", b.getUrl());
			data.appendQueryParameter("title", b.getDescription());
			data.appendQueryParameter("notes", b.getNotes());
			data.appendQueryParameter("tags", b.getTags());
			data.appendQueryParameter("time", Long.toString(b.getTime()));
			data.appendQueryParameter("account", b.getAccount());
		}
		viewBookmark.setData(data.build());
		
		Log.d("View Bookmark Uri", data.build().toString());
		startActivity(viewBookmark);
	}
	
	private boolean isMyself() {
		return mAccount.name.equals(username);
	}
	
    public class LoadBookmarkFeedTask extends AsyncTask<String, Integer, Boolean>{
    	private String user;
    	private String tag = null;
    	private ArrayList<Bookmark> bookmarkList;
    	private ProgressDialog progress;
    	
    	protected void onPreExecute() {
    		progress = new ProgressDialog(mContext);
    		progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    		progress.setMessage("Loading. Please wait...");
    		progress.setCancelable(true);
    		progress.show();
    	}
    	
    	@Override
    	protected Boolean doInBackground(String... args) {
    		user = args[0];
    		
    		if(args.length > 1)
    			tag = args[1];
    		
    		bookmarkList = new ArrayList<Bookmark>();
    		boolean result = false;
    		
			try {
				if(user.equals("network")) {
					bookmarkList = DeliciousFeed.fetchNetworkRecent(mAccount.name, Integer.parseInt(bookmarkLimit));
				} else {
					bookmarkList = DeliciousFeed.fetchFriendBookmarks(user, tag, Integer.parseInt(bookmarkLimit));
				}
				result = true;
			} catch (AuthenticationException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	
    		return result;
    	}
    	
        protected void onPostExecute(Boolean result) {
        	progress.dismiss();
        	
        	if(result) {
        		setListAdapter(new BookmarkListAdapter(mContext, R.layout.bookmark_view, bookmarkList));
        	}
        }
    }
}