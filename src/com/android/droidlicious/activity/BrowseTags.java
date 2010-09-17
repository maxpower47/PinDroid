package com.android.droidlicious.activity;

import java.util.ArrayList;
import java.util.List;

import com.android.droidlicious.R;
import com.android.droidlicious.Constants;
import com.android.droidlicious.client.NetworkUtilities;
import com.android.droidlicious.client.User;
import com.android.droidlicious.listadapter.TagListAdapter;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import android.view.*;

public class BrowseTags extends DroidliciousBaseActivity {

	WebView mWebView;
	AccountManager mAccountManager;
	String username = null;
		
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browse_tags);
		
		ArrayList<User.Tag> tagList = new ArrayList<User.Tag>();
		String authtoken = null;
		
		mAccountManager = AccountManager.get(this);
		Account[] al = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
		
		if(al.length > 0){
			if(this.getIntent().hasExtra("username"))
				username = getIntent().getStringExtra("username");
			else username = al[0].name;
			
			try{	
				authtoken = mAccountManager.blockingGetAuthToken(al[0], Constants.AUTHTOKEN_TYPE, true);
			
				tagList = NetworkUtilities.fetchTags(username, al[0], authtoken);
				
				setListAdapter(new TagListAdapter(this, R.layout.tag_view, tagList));	
			}
			catch(Exception e){}
	
			ListView lv = getListView();
			lv.setTextFilterEnabled(true);
		
			lv.setOnItemClickListener(new OnItemClickListener() {
			    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			    	String tagName = ((TextView)view.findViewById(R.id.tag_name)).getText().toString();
			    	
			    	
					Intent i = new Intent(parent.getContext(), BrowseBookmarks.class);
					i.putExtra("tagname", tagName);
					i.putExtra("username", username);
					
					startActivity(i);
			    }
			});

		}
		else{
			Toast.makeText(getApplicationContext(), "blah", Toast.LENGTH_SHORT).show();
			
		}
	}
	

	
}
