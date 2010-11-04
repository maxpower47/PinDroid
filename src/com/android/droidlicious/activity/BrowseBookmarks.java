package com.android.droidlicious.activity;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.auth.AuthenticationException;

import com.android.droidlicious.Constants;
import com.android.droidlicious.R;
import com.android.droidlicious.client.DeliciousApi;
import com.android.droidlicious.client.DeliciousFeed;
import com.android.droidlicious.listadapter.BookmarkListAdapter;
import com.android.droidlicious.platform.BookmarkManager;
import com.android.droidlicious.providers.BookmarkContent.Bookmark;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class BrowseBookmarks extends DroidliciousBaseActivity {
	
	private AccountManager mAccountManager;
	private Account mAccount;
	private ListView lv;
	private Context mContext;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browse_bookmarks);
		
		mAccountManager = AccountManager.get(this);
		mAccount = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE)[0];
		mContext = this;
		
		Log.d("browse bookmarks", getIntent().getDataString());
		Uri data = getIntent().getData();
		String scheme = data.getScheme();
		String path = data.getPath();
		Log.d("path", path);
		String username = data.getQueryParameter("username");
		String tagname = data.getQueryParameter("tagname");
		String recent = data.getQueryParameter("recent");
		
		ArrayList<Bookmark> bookmarkList = new ArrayList<Bookmark>();
		
		if(scheme.equals("content") && path.equals("/bookmarks") && mAccount.name.equals(username)){
			
			try{	
				
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
				bookmarkList = DeliciousFeed.fetchFriendBookmarks(username, tagname);

				setListAdapter(new BookmarkListAdapter(this, R.layout.bookmark_view, bookmarkList));	
			}
			catch(Exception e){}
		} else if(scheme.equals("content") && path.equals("/network")){
			try{	
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
		    	
		    	String url = b.getUrl();
		    	Uri link = Uri.parse(url);
				Intent i = new Intent(Intent.ACTION_VIEW, link);
				
				startActivity(i);
		    }
		});
		
		/* Add Context-Menu listener to the ListView. */
		lv.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				menu.setHeaderTitle("ContextMenu");
				menu.add(0, 0, 0, "Delete");
				
			}
		});
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem aItem) {
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) aItem.getMenuInfo();
		switch (aItem.getItemId()) {
			case 0:
				final Bookmark b = (Bookmark)lv.getItemAtPosition(menuInfo.position);
				
				BookmarkTaskArgs args = new BookmarkTaskArgs(b, mAccount, mContext);
				
				new DeleteBookmarkTask().execute(args);
				
				return true;
		}
		return false;
	}
	
	private class DeleteBookmarkTask extends AsyncTask<BookmarkTaskArgs, Integer, Boolean>{
		private Context context;
		private Bookmark bookmark;
		
		@Override
		protected Boolean doInBackground(BookmarkTaskArgs... args) {
			context = args[0].getContext();
			bookmark = args[0].getBookmark();
			
			try {
				Boolean success =  DeliciousApi.deleteBookmark(bookmark, args[0].getAccount(), context);
				if(success){
					BookmarkManager.DeleteBookmark(args[0].getBookmark(), context);
					return true;
				} else return false;
					
			} catch (IOException e) {
				return false;
			} catch (AuthenticationException e) {
				return false;
			}
		}

	    protected void onPostExecute(Boolean result) {
			if(result){
				BookmarkListAdapter bla = (BookmarkListAdapter) lv.getAdapter();
				bla.remove(bookmark);
				
				Toast.makeText(context, "Bookmark Deleted Successfully", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
			}
			
	    }
	}

	private class BookmarkTaskArgs{
		private Bookmark bookmark;
		private Account account;
		private Context context;
		
		public Bookmark getBookmark(){
			return bookmark;
		}
		
		public Account getAccount(){
			return account;
		}
		
		public Context getContext(){
			return context;
		}
		
		public BookmarkTaskArgs(Bookmark b, Account a, Context c){
			bookmark = b;
			account = a;
			context = c;
		}
	}
}


