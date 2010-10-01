package com.android.droidlicious.activity;

import com.android.droidlicious.Constants;
import com.android.droidlicious.R;
import com.android.droidlicious.authenticator.AuthToken;
import com.android.droidlicious.client.NetworkUtilities;
import com.android.droidlicious.client.TokenRejectedException;
import com.android.droidlicious.client.User;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddBookmark extends Activity implements View.OnClickListener{

	private EditText mEditUrl;
	private EditText mEditDescription;
	private EditText mEditNotes;
	private Button mButtonSave;
	private AccountManager mAccountManager;
	private String authtoken;
	private Account account;
	private User.Bookmark bookmark;
	private Context context;
	Thread background;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.add_bookmark);
		mEditUrl = (EditText) findViewById(R.id.add_edit_url);
		mEditDescription = (EditText) findViewById(R.id.add_edit_description);
		mEditNotes = (EditText) findViewById(R.id.add_edit_notes);
		mButtonSave = (Button) findViewById(R.id.add_button_save);
		context = this;

		mButtonSave.setOnClickListener(this);	
	}
	
    public void save() {
    	
		mAccountManager = AccountManager.get(this);
		Account[] al = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
		account = al[0];
		
		
		bookmark = new User.Bookmark(mEditUrl.getText().toString(), 
		mEditDescription.getText().toString(), mEditNotes.getText().toString());
		

		AuthToken at = new AuthToken(context, account);
		at.getAuthTokenAsync(messageHandler);

    }

    // Instantiating the Handler associated with the main thread.
    private Handler messageHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {  
        	Boolean success = false;
        	authtoken = (String)msg.obj;
    		try {
    			success = NetworkUtilities.addBookmarks(bookmark, account, authtoken, context);
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    		
    		if(success){
    			Toast.makeText(getApplicationContext(), "Bookmark Added Successfully", Toast.LENGTH_SHORT).show();
    		} else {
    			Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
    		}
    		
    		finish();
        }

    };
	
    /**
     * {@inheritDoc}
     */
    public void onClick(View v) {
        if (v == mButtonSave) {
            save();
        }
    }

}
