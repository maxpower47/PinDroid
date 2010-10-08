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
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.*;

public class BrowseTags extends DroidliciousBaseActivity {

	WebView mWebView;
	AccountManager mAccountManager;
	String username = null;
	Account account = null;
		
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.browse_tags);
		
		ArrayList<User.Tag> tagList = new ArrayList<User.Tag>();
		
		mAccountManager = AccountManager.get(this);
		Account[] al = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
		account = al[0];
		
		if(al.length > 0){
			if(this.getIntent().hasExtra("username"))
				username = getIntent().getStringExtra("username");
			else username = account.name;
			
			try{	
				tagList = NetworkUtilities.fetchTags(username, al[0], "");
				
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
					if(username != account.name){
						i.putExtra("username", username);
					}
					
					startActivity(i);
			    }
			});
		}
		else{
			Toast.makeText(getApplicationContext(), "blah", Toast.LENGTH_SHORT).show();
		}
	}
}
