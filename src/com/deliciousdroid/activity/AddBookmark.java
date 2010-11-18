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
import java.util.Date;

import org.apache.http.auth.AuthenticationException;

import com.deliciousdroid.R;
import com.deliciousdroid.Constants;
import com.deliciousdroid.action.BookmarkTaskArgs;
import com.deliciousdroid.client.DeliciousApi;
import com.deliciousdroid.client.NetworkUtilities;
import com.deliciousdroid.platform.BookmarkManager;
import com.deliciousdroid.platform.TagManager;
import com.deliciousdroid.providers.BookmarkContent.Bookmark;
import com.deliciousdroid.providers.ContentNotFoundException;
import com.deliciousdroid.providers.TagContent.Tag;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AddBookmark extends Activity implements View.OnClickListener{

	private EditText mEditUrl;
	private EditText mEditDescription;
	private EditText mEditNotes;
	private EditText mEditTags;
	private TextView mSuggestTags;
	private CheckBox mPrivate;
	private Button mButtonSave;
	private AccountManager mAccountManager;
	private Account account;
	private Bookmark bookmark;
	private Context context;
	Thread background;
	private Boolean update = false;
	private Resources res;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.add_bookmark);
		mEditUrl = (EditText) findViewById(R.id.add_edit_url);
		mEditDescription = (EditText) findViewById(R.id.add_edit_description);
		mEditNotes = (EditText) findViewById(R.id.add_edit_notes);
		mEditTags = (EditText) findViewById(R.id.add_edit_tags);
		mSuggestTags = (TextView) findViewById(R.id.add_suggest_tags);
		mPrivate = (CheckBox) findViewById(R.id.add_edit_private);
		mButtonSave = (Button) findViewById(R.id.add_button_save);
		context = this;
		
		res = getResources();
		
		mAccountManager = AccountManager.get(this);
		Account[] al = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
		account = al[0];

		if(savedInstanceState ==  null){
			Intent intent = getIntent();
			
			if(Intent.ACTION_SEND.equals(intent.getAction())){
				mEditUrl.setText(intent.getStringExtra(Intent.EXTRA_TEXT));
				
				new GetWebpageTitleTask().execute(intent.getStringExtra(Intent.EXTRA_TEXT));
			} else if(Intent.ACTION_EDIT.equals(intent.getAction())){
				int id = Integer.parseInt(intent.getData().getLastPathSegment());
				try {
					Bookmark b = BookmarkManager.GetById(id, context);
					
					mEditUrl.setText(b.getUrl());
					mEditDescription.setText(b.getDescription());
					mEditNotes.setText(b.getNotes());
					mEditTags.setText(b.getTags());
					update = true;
				} catch (ContentNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		if(update)
			setTitle("Edit Bookmark");
		else setTitle("Add Bookmark");
		
		mEditUrl.setOnFocusChangeListener(new OnFocusChangeListener(){
			public void onFocusChange(View v, boolean hasFocus) {
				if(!hasFocus){
					String url = mEditUrl.getText().toString();
					
					new GetWebpageTitleTask().execute(url);
					new GetTagSuggestionsTask().execute(url);
				}
			}
		});

		mButtonSave.setOnClickListener(this);	
	}
	
    public void save() {

		String url = mEditUrl.getText().toString();
		
		if(!url.startsWith("http")){
			url = "http://" + url;
		}
		
		Date d = new Date();
		long time = d.getTime();
		
		bookmark = new Bookmark(url, mEditDescription.getText().toString(), 
			mEditNotes.getText().toString(), mEditTags.getText().toString(),
			mPrivate.isChecked(), time);
		
		BookmarkTaskArgs args = new BookmarkTaskArgs(bookmark, account, context);
		
		new AddBookmarkTask().execute(args);
    }

    /**
     * {@inheritDoc}
     */
    public void onClick(View v) {
        if (v == mButtonSave) {
            save();
        }
    }
    
    private class AddBookmarkTask extends AsyncTask<BookmarkTaskArgs, Integer, Boolean>{
    	private Context context;
    	private Bookmark bookmark;
    	private Account account;
    	
    	@Override
    	protected Boolean doInBackground(BookmarkTaskArgs... args) {
    		context = args[0].getContext();
    		bookmark = args[0].getBookmark();
    		account = args[0].getAccount();
    		
    		try {
    			Boolean success = DeliciousApi.addBookmark(bookmark, account, context);
    			if(success){
    				if(update){
    					BookmarkManager.UpdateBookmark(bookmark, account.name, context);
    				} else {
    					BookmarkManager.AddBookmark(bookmark, account.name, context);
    				}
    				return true;
    			} else return false;
    		} catch (Exception e) {
    			Log.d("addBookmark error", e.toString());
    			return false;
    		}
    	}

        protected void onPostExecute(Boolean result) {
    		if(result){
    			String[] tags = bookmark.getTags().split(" ");
    			for(String s:tags){
    				Tag t = new Tag(s, 1);    				
    				TagManager.UpsertTag(t, account.name, context);
    			}
    			
    			String msg = null;
    			if(update)
    				msg = res.getString(R.string.edit_bookmark_success_msg);
    			else msg = res.getString(R.string.add_bookmark_success_msg);
    			
    			Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    		} else {
    			Toast.makeText(context, res.getString(R.string.add_bookmark_error_msg), Toast.LENGTH_SHORT).show();
    		}
    		
    		finish();
        }
    }
    
    public class GetWebpageTitleTask extends AsyncTask<String, Integer, String>{
    	private String url;
    	
    	@Override
    	protected String doInBackground(String... args) {
    		
    		if(args.length > 0) {
	    		url = args[0];
		
	    		return NetworkUtilities.getWebpageTitle(url);
    		} else return "";
    		
    	}
    	
        protected void onPostExecute(String result) {
        	mEditDescription.setText(result);
        }
    }
    
    public class GetTagSuggestionsTask extends AsyncTask<String, Integer, ArrayList<Tag>>{
    	private String url;
    	
    	@Override
    	protected ArrayList<Tag> doInBackground(String... args) {
    		url = args[0];
	
    		try {
				return DeliciousApi.getSuggestedTags(url, account, context);
			} catch (AuthenticationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
    	}
    	
        protected void onPostExecute(ArrayList<Tag> result) {
        	
        	StringBuilder sb = new StringBuilder();
        	
        	if(result != null) {
        		for(Tag t : result) {
        			sb.append(t.getTagName() + " ");
        		}
        		
        		mSuggestTags.setText(sb.toString());
        	} 	
        }
    }
}