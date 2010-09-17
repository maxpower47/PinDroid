package com.android.droidlicious.activity;

import java.io.IOException;

import org.apache.http.ParseException;
import org.apache.http.auth.AuthenticationException;
import org.json.JSONException;

import com.android.droidlicious.Constants;
import com.android.droidlicious.R;
import com.android.droidlicious.client.NetworkUtilities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddBookmark extends Activity implements View.OnClickListener{

	private EditText mEditUrl;
	private EditText mEditDescription;
	private Button mButtonSave;
	private AccountManager mAccountManager;

	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.add_bookmark);
		mEditUrl = (EditText) findViewById(R.id.add_edit_url);
		mEditDescription = (EditText) findViewById(R.id.add_edit_description);
		mButtonSave = (Button) findViewById(R.id.add_button_save);

		mButtonSave.setOnClickListener(this);

		
	}
	
    public void save() {
    	
		mAccountManager = AccountManager.get(this);
		Account[] al = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
		String authtoken = null;
		Boolean success = false;
		
		try {
			authtoken = mAccountManager.blockingGetAuthToken(al[0], Constants.AUTHTOKEN_TYPE, true);
		} catch (Exception e1) {

		}
    	
    	try {
			success = NetworkUtilities.addBookmarks(mEditUrl.getText().toString(), mEditDescription.getText().toString(), al[0], authtoken);
		} catch (Exception e) {

		}
		
		if(success){
			Toast.makeText(getApplicationContext(), "Bookmark Added Successfully", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
		}
		
		finish();
        
    }

	
    /**
     * {@inheritDoc}
     */
    public void onClick(View v) {
        if (v == mButtonSave) {
            save();
        }
    }

}
