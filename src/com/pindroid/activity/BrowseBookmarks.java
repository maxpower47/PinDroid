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

import java.io.IOException;
import java.text.ParseException;

import com.pindroid.R;
import com.pindroid.Constants;
import com.pindroid.action.BookmarkTaskArgs;
import com.pindroid.action.DeleteBookmarkTask;
import com.pindroid.action.IntentHelper;
import com.pindroid.action.MarkReadBookmarkTask;
import com.pindroid.client.PinboardFeed;
import com.pindroid.listadapter.BookmarkViewBinder;
import com.pindroid.platform.BookmarkManager;
import com.pindroid.providers.BookmarkContentProvider;
import com.pindroid.providers.BookmarkContent.Bookmark;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleCursorAdapter;

public class BrowseBookmarks extends AppBaseListActivity {
	
	private ListView lv;
	
	private String sortfield = Bookmark.Time + " DESC";

	private String tagname = null;
	private boolean unread = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browse_bookmarks);
		
		if(mAccount != null) {
			Intent intent = getIntent();
				
			Uri data = intent.getData();
			String path = null;
			
			if(data != null) {
				if(data.getUserInfo() != "") {
					username = data.getUserInfo();
				} else username = mAccount.name;
				
				path = data.getPath();
				tagname = data.getQueryParameter("tagname");
				unread = data.getQueryParameter("unread") != null;
			}
			
	    	if(Intent.ACTION_SEARCH.equals(intent.getAction())) {
	    		Bundle searchData = intent.getBundleExtra(SearchManager.APP_DATA);
	
	    		if(searchData != null) {
	    			tagname = searchData.getString("tagname");
	    			username = searchData.getString("username");
	    			unread = searchData.getBoolean("unread");
	    		}
	    		
	    		String query = intent.getStringExtra(SearchManager.QUERY);
	    		
	    		if(intent.hasExtra("username")) {
	    			username = intent.getStringExtra("username");
	    		}
	    		
	    		if(unread) {
	    			setTitle(getString(R.string.unread_search_results_title, query));
	    		} else setTitle(getString(R.string.bookmark_search_results_title, query));
	    		
	    		if(isMyself()) {
	    			Cursor c = BookmarkManager.SearchBookmarks(query, tagname, unread, username, this);
	    			startManagingCursor(c);
	    			
	    			SimpleCursorAdapter a = new SimpleCursorAdapter(mContext, R.layout.bookmark_view, c, 
	    					new String[]{Bookmark.Description, Bookmark.Tags, Bookmark.ToRead, Bookmark.Shared}, 
	    					new int[]{R.id.bookmark_description, R.id.bookmark_tags, R.id.bookmark_unread, R.id.bookmark_private});
	    			a.setViewBinder(new BookmarkViewBinder());
	    			
	    			setListAdapter(a);
	    		} else {
	    			setTitle(getString(R.string.search_results_global_tag_title, query));
	    			new LoadBookmarkFeedTask().execute("global", query);
	    		}
	    		
	    	} else if(!data.getScheme().equals("content")) {
	    		
	    		openBookmarkInBrowser(new Bookmark(data.toString()));
	    		finish();
	    		
	    	} else if(path.equals("/bookmarks") && isMyself()) {

	    		String title = "";
	    		
	    		if(unread && tagname != null && tagname != "") {
	    			title = getString(R.string.browse_my_unread_bookmarks_tagged_title, tagname);
	    		} else if(unread && (tagname == null || tagname.equals(""))) {
	    			title = getString(R.string.browse_my_unread_bookmarks_title);
	    		} else if(tagname != null && tagname != "") {
	    			title = getString(R.string.browse_my_bookmarks_tagged_title, tagname);
	    		} else {
	    			title = getString(R.string.browse_my_bookmarks_title);
	    		}
	    		
				setTitle(title);
				

				loadBookmarkList();


			}  else if(username.equals("recent")){
				try{
					setTitle(getString(R.string.browse_recent_bookmarks_title));

					new LoadBookmarkFeedTask().execute("recent");
				}
				catch(Exception e){}
			} else if(path.contains("bookmarks") && TextUtils.isDigitsOnly(data.getLastPathSegment())) {
				viewBookmark(Integer.parseInt(data.getLastPathSegment()));
				finish();
			} else {
				try{
					String title = "";
					
					if(tagname != null && tagname != "") {
						title = getString(R.string.browse_user_bookmarks_tagged_title, username, tagname);
					} else {
						title = getString(R.string.browse_user_bookmarks_title, username);
					}
					setTitle(title);

					new LoadBookmarkFeedTask().execute(username, tagname);
				}
				catch(Exception e){}
			}
			
			lv = getListView();
			lv.setTextFilterEnabled(true);
			lv.setFastScrollEnabled(true);
		
			lv.setOnItemClickListener(new OnItemClickListener() {
			    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					final Cursor c = (Cursor)lv.getItemAtPosition(position);
					Bookmark b = BookmarkManager.CursorToBookmark(c);
	
			    	if(defaultAction.equals("view")) {
			    		viewBookmark(b);
			    	} else if(defaultAction.equals("read")) {
			    		readBookmark(b);
			    	} else {
			    		openBookmarkInBrowser(b);
			    	}   	
			    }
			});
			
			/* Add Context-Menu listener to the ListView. */
			lv.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
				public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
					menu.setHeaderTitle("Actions");
					MenuInflater inflater = getMenuInflater();
					
					if(isMyself()){
						inflater.inflate(R.menu.browse_bookmark_context_menu_self, menu);
					} else {
						inflater.inflate(R.menu.browse_bookmark_context_menu_other, menu);
					}
				}
			});
		}
	}
	
	@Override
	public void onResume(){
		super.onResume();
		
		Uri data = getIntent().getData();
		if(data != null && data.getUserInfo() != null && data.getUserInfo() != "") {
			username = data.getUserInfo();
		} else if(getIntent().hasExtra("username")){
			username = getIntent().getStringExtra("username");
		} else username = mAccount.name;
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem aItem) {
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) aItem.getMenuInfo();
		final Cursor c = (Cursor)lv.getItemAtPosition(menuInfo.position);
		Bookmark b = BookmarkManager.CursorToBookmark(c);
		
		switch (aItem.getItemId()) {
			case R.id.menu_bookmark_context_open:
				openBookmarkInBrowser(b);
				return true;
			case R.id.menu_bookmark_context_view:				
				viewBookmark(b);
				return true;
			case R.id.menu_bookmark_context_edit:
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
			
			case R.id.menu_bookmark_context_delete:
				BookmarkTaskArgs args = new BookmarkTaskArgs(b, mAccount, mContext);	
				new DeleteBookmarkTask().execute(args);

				return true;
				
			case R.id.menu_bookmark_context_add:				
				startActivity(IntentHelper.AddBookmark(b.getUrl(), mAccount.name));
				return true;
				
			case R.id.menu_bookmark_context_share:
		    	Intent sendIntent = IntentHelper.SendBookmark(b.getUrl(), b.getDescription());
		    	startActivity(Intent.createChooser(sendIntent, getString(R.string.share_chooser_title)));
				
				return true;
			case R.id.menu_bookmark_context_read:
				readBookmark(b);
				return true;
		}
		return false;
	}
	
	@Override
	public boolean onSearchRequested() {
		
		if(isMyself()) {
			Bundle contextData = new Bundle();
			contextData.putString("tagname", tagname);
			contextData.putString("username", username);
			contextData.putBoolean("unread", unread);
			startSearch(null, false, contextData, false);
			return true;
		} else {
			startSearch(null, false, Bundle.EMPTY, false);
			return true;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
	    MenuInflater inflater = getMenuInflater();
		
		if(result && isMyself()) {
			inflater.inflate(R.menu.browse_bookmark_menu, menu);
		}
		
	    return result;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		boolean result = false;
		
	    switch (item.getItemId()) {
		    case R.id.menu_bookmark_sort_date_asc:
		    	sortfield = Bookmark.Time + " ASC";
				result = true;
				break;
		    case R.id.menu_bookmark_sort_date_desc:			
		    	sortfield = Bookmark.Time + " DESC";
		    	result = true;
		    	break;
		    case R.id.menu_bookmark_sort_description_asc:			
		    	sortfield = Bookmark.Description + " ASC";
		    	result = true;
		    	break;
		    case R.id.menu_bookmark_sort_description_desc:			
		    	sortfield = Bookmark.Description + " DESC";
		    	result = true;
		    	break;
		    case R.id.menu_bookmark_sort_url_asc:			
		    	sortfield = Bookmark.Url + " ASC";
		    	result = true;
		    	break;
		    case R.id.menu_bookmark_sort_url_desc:			
		    	sortfield = Bookmark.Url + " DESC";
		    	result = true;
		    	break;
	    }
	    
	    if(result) {
	    	loadBookmarkList();
	    } else result = super.onOptionsItemSelected(item);
	    
	    return result;
	}
	
	private void loadBookmarkList() {
		Cursor c = BookmarkManager.GetBookmarks(username, tagname, unread, sortfield, this);
		startManagingCursor(c);
		
		SimpleCursorAdapter a = new SimpleCursorAdapter(mContext, R.layout.bookmark_view, c, 
				new String[]{Bookmark.Description, Bookmark.Tags, Bookmark.ToRead, Bookmark.Shared}, 
				new int[]{R.id.bookmark_description, R.id.bookmark_tags, R.id.bookmark_unread, R.id.bookmark_private});
		a.setViewBinder(new BookmarkViewBinder());
		
		setListAdapter(a);
	}
	
	private void openBookmarkInBrowser(Bookmark b) {
    	String url = b.getUrl();
    	
    	if(!url.startsWith("http")) {
    		url = "http://" + url;
    	}
		
		startActivity(IntentHelper.OpenInBrowser(url));
	}
	
	private void viewBookmark(int id) {
		Bookmark b = new Bookmark(id);
		viewBookmark(b);
	}
	
	private void readBookmark(Bookmark b){
    	if(isMyself() && b.getToRead() && markAsRead) {
    		BookmarkTaskArgs unreadArgs = new BookmarkTaskArgs(b, mAccount, this);
    		new MarkReadBookmarkTask().execute(unreadArgs);
    	}
		startActivity(IntentHelper.ReadBookmark(b.getUrl()));
	}
	
	private void viewBookmark(Bookmark b) {
		startActivity(IntentHelper.ViewBookmark(b, username));
	}
	
    public class LoadBookmarkFeedTask extends AsyncTask<String, Integer, Boolean>{
        private String user;
        private String tag;
        private ProgressDialog progress;
        private Cursor c;
       
        protected void onPreExecute() {
	        progress = new ProgressDialog(mContext);
	        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	        progress.setMessage(getString(R.string.bookmark_feed_task_progress));
	        progress.setCancelable(true);
	        progress.show();
        }
       
        @Override
        protected Boolean doInBackground(String... args) {
	       user = args[0];
	       
	       if(user.equals("global"))
	    	   user = "";
	       
	       if(args.length > 1) {
	    	   tag = args[1];
	       }
	       
	       boolean result = false;
       
		   try {
			   if(user.equals("recent")) {
				   c = PinboardFeed.fetchRecent();
			   } else {
				   c = PinboardFeed.fetchUserRecent(user, tag);
			   }
			   result = true;
		   }catch (ParseException e) {
			   e.printStackTrace();
		   }catch (IOException e) {
			   e.printStackTrace();
		   }

		   return result;
        }
       
        protected void onPostExecute(Boolean result) {
        	progress.dismiss();

        	if(result) {
        		startManagingCursor(c);
        		SimpleCursorAdapter a = new SimpleCursorAdapter(mContext, R.layout.bookmark_view, c, 
        				new String[]{Bookmark.Description, Bookmark.Tags, Bookmark.Source}, 
        				new int[]{R.id.bookmark_description, R.id.bookmark_tags, R.id.bookmark_source});
        		
        		a.setViewBinder(new BookmarkViewBinder());
        		setListAdapter(a);	
        	}
        }
    }
}