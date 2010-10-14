package com.android.droidlicious.activity;

import java.util.ArrayList;

import com.android.droidlicious.R;
import com.android.droidlicious.Constants;
import com.android.droidlicious.client.NetworkUtilities;
import com.android.droidlicious.listadapter.TagListAdapter;
import com.android.droidlicious.providers.TagContent.Tag;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.view.*;

public class BrowseTags extends DroidliciousBaseActivity {

	WebView mWebView;
	AccountManager mAccountManager;
	String username = null;
	Account mAccount = null;
		
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browse_tags);
		
		ArrayList<Tag> tagList = new ArrayList<Tag>();
		
		mAccountManager = AccountManager.get(this);
		mAccount = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE)[0];
		
		Uri data = getIntent().getData();
		username = data.getPathSegments().get(0);
		username = data.getQueryParameter("username");
		
		if(mAccount.name.equals(username)){
			try{

				String[] projection = new String[] {Tag.Name, Tag.Count};
							
				Uri tags = Tag.CONTENT_URI;
				
				Cursor c = managedQuery(tags, projection, null, null, null);				
				
				if(c.moveToFirst()){
					
					int nameColumn = c.getColumnIndex(Tag.Name);
					int countColumn = c.getColumnIndex(Tag.Count);

					do {	
						Tag t = new Tag(c.getString(nameColumn), c.getInt(countColumn));

						tagList.add(t);
					} while(c.moveToNext());	
				}

				setListAdapter(new TagListAdapter(this, R.layout.tag_view, tagList));	

			} catch(Exception e) {
				
			}
			
		} else {
			try{	
				tagList = NetworkUtilities.fetchFriendTags(username, mAccount, "");
				
				setListAdapter(new TagListAdapter(this, R.layout.tag_view, tagList));	
			}
			catch(Exception e){}
		}

		ListView lv = getListView();
		lv.setTextFilterEnabled(true);
	
		lv.setOnItemClickListener(new OnItemClickListener() {
		    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		    	String tagName = ((TextView)view.findViewById(R.id.tag_name)).getText().toString();
		    	
				Intent i = new Intent();

				Uri.Builder dataBuilder = Constants.CONTENT_URI_BASE.buildUpon();
				dataBuilder.appendEncodedPath("bookmarks");
				dataBuilder.appendQueryParameter("username", username);
				dataBuilder.appendQueryParameter("tagname", tagName);
				i.setData(dataBuilder.build());
				
				startActivity(i);
		    }
		});
	}
}
