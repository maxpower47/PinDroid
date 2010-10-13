package com.android.droidlicious.activity;

import java.util.ArrayList;

import com.android.droidlicious.R;
import com.android.droidlicious.Constants;
import com.android.droidlicious.client.NetworkUtilities;
import com.android.droidlicious.client.User;
import com.android.droidlicious.listadapter.TagListAdapter;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.util.Log;
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
		
		ArrayList<User.Tag> tagList = new ArrayList<User.Tag>();
		
		mAccountManager = AccountManager.get(this);
		mAccount = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE)[0];
		
		Uri data = getIntent().getData();
		Log.d("blah", data.toString());
		username = data.getPathSegments().get(0);
		username = data.getQueryParameter("username");
		
		try{	
			tagList = NetworkUtilities.fetchTags(username, mAccount, "");
			
			setListAdapter(new TagListAdapter(this, R.layout.tag_view, tagList));	
		}
		catch(Exception e){}

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
