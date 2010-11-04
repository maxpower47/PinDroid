/*
 * Droidlicious - http://code.google.com/p/droidlicious/
 *
 * Copyright (C) 2010 Matt Schmidt
 *
 * Droidlicious is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * Droidlicious is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Droidlicious; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */

package com.android.droidlicious.activity;

import com.android.droidlicious.Constants;
import com.android.droidlicious.R;
import com.android.droidlicious.client.DeliciousApi;
import com.android.droidlicious.platform.BookmarkManager;
import com.android.droidlicious.providers.BookmarkContent.Bookmark;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class AddBookmark extends Activity implements View.OnClickListener{

	private EditText mEditUrl;
	private EditText mEditDescription;
	private EditText mEditNotes;
	private EditText mEditTags;
	private CheckBox mPrivate;
	private Button mButtonSave;
	private AccountManager mAccountManager;
	private Account account;
	private Bookmark bookmark;
	private Context context;
	Thread background;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.add_bookmark);
		mEditUrl = (EditText) findViewById(R.id.add_edit_url);
		mEditDescription = (EditText) findViewById(R.id.add_edit_description);
		mEditNotes = (EditText) findViewById(R.id.add_edit_notes);
		mEditTags = (EditText) findViewById(R.id.add_edit_tags);
		mPrivate = (CheckBox) findViewById(R.id.add_edit_private);
		mButtonSave = (Button) findViewById(R.id.add_button_save);
		context = this;
		
		if(savedInstanceState ==  null){
			Intent intent = getIntent();
			
			if(Intent.ACTION_SEND.equals(intent.getAction())){
				mEditUrl.setText(intent.getStringExtra(Intent.EXTRA_TEXT));
			}
		}

		mButtonSave.setOnClickListener(this);	
	}
	
    public void save() {
    	
		mAccountManager = AccountManager.get(this);
		Account[] al = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
		account = al[0];
		
		String url = mEditUrl.getText().toString();
		
		if(!url.startsWith("http://")){
			url = "http://" + url;
		}
		
		Log.d("private", Boolean.toString(mPrivate.isChecked()));
		
		bookmark = new Bookmark(url, mEditDescription.getText().toString(), 
			mEditNotes.getText().toString(), mEditTags.getText().toString(),
			mPrivate.isChecked());
		
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
    				BookmarkManager.AddBookmark(bookmark, account.name, context);
    				return true;
    			} else return false;
    		} catch (Exception e) {
    			return false;
    		}
    	}

        protected void onPostExecute(Boolean result) {
    		if(result){  			
    			Toast.makeText(context, "Bookmark Added Successfully", Toast.LENGTH_SHORT).show();
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