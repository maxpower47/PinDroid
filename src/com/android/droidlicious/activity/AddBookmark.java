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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddBookmark extends Activity implements View.OnClickListener{

	private EditText mEditUrl;
	private EditText mEditDescription;
	private EditText mEditNotes;
	private EditText mEditTags;
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
		
		
		bookmark = new Bookmark(url, mEditDescription.getText().toString(), 
			mEditNotes.getText().toString(), mEditTags.getText().toString());
		
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
    	
    	@Override
    	protected Boolean doInBackground(BookmarkTaskArgs... args) {
    		context = args[0].getContext();
    		bookmark = args[0].getBookmark();
    		
    		try {
    			Boolean success = DeliciousApi.addBookmark(args[0].getBookmark(), args[0].getAccount(), args[0].getContext());
    			if(success){
    				BookmarkManager.AddBookmark(bookmark, context);
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