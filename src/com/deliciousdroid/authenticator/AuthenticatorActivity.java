/*
 * DeliciousDroid - http://code.google.com/p/DeliciousDroid/
 *
 * Copyright (C) 2010 Matt Schmidt
 *
 * DeliciousDroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
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

package com.deliciousdroid.authenticator;

import java.io.IOException;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.deliciousdroid.R;
import com.deliciousdroid.Constants;
import com.deliciousdroid.activity.OauthLogin;
import com.deliciousdroid.client.LoginResult;
import com.deliciousdroid.client.NetworkUtilities;

/**
 * Activity which displays login screen to the user.
 */
public class AuthenticatorActivity extends AccountAuthenticatorActivity {
    public static final String PARAM_CONFIRMCREDENTIALS = "confirmCredentials";
    public static final String PARAM_PASSWORD = "password";
    public static final String PARAM_USERNAME = "username";
    public static final String PARAM_AUTHTOKEN_TYPE = "authtokenType";

    private static final String TAG = "AuthenticatorActivity";

    private AccountManager mAccountManager;
    private Thread mAuthThread;
    private String mAuthtoken;
    private String mAuthtokenType;

    /**
     * If set we are just checking that the user knows their credentials; this
     * doesn't cause the user's password to be changed on the device.
     */
    private Boolean mConfirmCredentials = false;

    /** for posting authentication attempts back to UI thread */
    private final Handler mHandler = new Handler();
    private TextView mMessage;
    private String mPassword;
    private EditText mPasswordEdit;

    /** Was the original caller asking for an entirely new account? */
    protected boolean mRequestNewAccount = false;

    private String mUsername;
    private EditText mUsernameEdit;
    
    private RadioButton mDeliciousAuth;
    private RadioButton mYahooAuth;
    
    private String oauthVerifier;
    private String oauthToken;
    private String oauthTokenSecret;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle icicle) {
        Log.i(TAG, "onCreate(" + icicle + ")");
        super.onCreate(icicle);
        mAccountManager = AccountManager.get(this);
        Log.i(TAG, "loading data from Intent");
        final Intent intent = getIntent();
        mUsername = intent.getStringExtra(PARAM_USERNAME);
        mAuthtokenType = intent.getStringExtra(PARAM_AUTHTOKEN_TYPE);
        mRequestNewAccount = mUsername == null;
        mConfirmCredentials = intent.getBooleanExtra(PARAM_CONFIRMCREDENTIALS, false);

        Log.i(TAG, "    request new: " + mRequestNewAccount);
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.login_authtype);
        getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, android.R.drawable.ic_dialog_alert);

        mMessage = (TextView) findViewById(R.id.message);
      
        mDeliciousAuth = (RadioButton) findViewById(R.id.auth_type_delicious);
        mYahooAuth = (RadioButton) findViewById(R.id.auth_type_yahoo);
        mMessage.setText(R.string.login_activity_authtype_text);
    }

    /*
     * {@inheritDoc}
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getText(R.string.ui_activity_authenticating));
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                Log.i(TAG, "dialog cancel has been invoked");
                if (mAuthThread != null) {
                    mAuthThread.interrupt();
                    finish();
                }
            }
        });
        return dialog;
    }
    
    /**
     * Handles onClick event on the Submit button. Sends username/password to
     * the server for authentication.
     * 
     * @param view The Submit button for which this method is invoked
     */
    public void handleAuthtype(View view) {      

    	if(mYahooAuth.isChecked()){
            mAuthThread = NetworkUtilities.attemptAuth(mUsername, mPassword, 1, mHandler,
                    AuthenticatorActivity.this);
    	} else {
    		setContentView(R.layout.login_activity);
    		
            mUsernameEdit = (EditText) findViewById(R.id.username_edit);
            mPasswordEdit = (EditText) findViewById(R.id.password_edit);
            mMessage = (TextView) findViewById(R.id.message);

            mUsernameEdit.setText(mUsername);
            mMessage.setText(getMessage());
    	}
    }

    /**
     * Handles onClick event on the Submit button. Sends username/password to
     * the server for authentication.
     * 
     * @param view The Submit button for which this method is invoked
     */
    public void handleLogin(View view) {
        if (mRequestNewAccount) {
            mUsername = mUsernameEdit.getText().toString();
        }
        mPassword = mPasswordEdit.getText().toString();
        
        int authType = mYahooAuth.isChecked() ? 1 : 0;
        if (TextUtils.isEmpty(mUsername) || TextUtils.isEmpty(mPassword)) {
            mMessage.setText(getMessage());
        } else {
            showProgress();
            // Start authenticating...
            mAuthThread = NetworkUtilities.attemptAuth(mUsername, mPassword, authType, mHandler,
                    AuthenticatorActivity.this);
        }
    }

    /**
     * Called when response is received from the server for confirm credentials
     * request. See onAuthenticationResult(). Sets the
     * AccountAuthenticatorResult which is sent back to the caller.
     * 
     * @param the confirmCredentials result.
     */
    protected void finishConfirmCredentials(boolean result) {
        Log.i(TAG, "finishConfirmCredentials()");
        final Account account = new Account(mUsername, Constants.ACCOUNT_TYPE);
        mAccountManager.setPassword(account, mPassword);
        final Intent intent = new Intent();
        intent.putExtra(AccountManager.KEY_BOOLEAN_RESULT, result);
        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * 
     * Called when response is received from the server for authentication
     * request. See onAuthenticationResult(). Sets the
     * AccountAuthenticatorResult which is sent back to the caller. Also sets
     * the authToken in AccountManager for this account.
     * 
     * @param the confirmCredentials result.
     */
    protected void finishLogin(String authToken) {
        Log.i(TAG, "finishLogin()");
        
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        final String authtype = settings.getString(Constants.PREFS_AUTH_TYPE, Constants.AUTH_TYPE_DELICIOUS);
        final String token = settings.getString(Constants.OAUTH_TOKEN_PROPERTY, "");
        final String tokensecret = settings.getString(Constants.OAUTH_TOKEN_SECRET_PROPERTY, "");
        final String sessionhandle = settings.getString(Constants.OAUTH_SESSION_HANDLE_PROPERTY, "");
        
        if(authToken != null && authToken != ""){
        	try {
				mUsername = NetworkUtilities.getOauthUserName(authToken, tokensecret, this);
			} catch (IOException e) {
				e.printStackTrace();
			}
        	mPassword = authToken;
        }
        
        final Account account = new Account(mUsername, Constants.ACCOUNT_TYPE);

        if (mRequestNewAccount) {
            mAccountManager.addAccountExplicitly(account, mPassword, null);
            // Set contacts sync for this account.
            ContentResolver.setSyncAutomatically(account, ContactsContract.AUTHORITY, true);
        } else {
            mAccountManager.setPassword(account, mPassword);
        }
  
        mAccountManager.setUserData(account, Constants.PREFS_AUTH_TYPE, authtype);
        mAccountManager.setUserData(account, Constants.OAUTH_TOKEN_PROPERTY, token);
        mAccountManager.setUserData(account, Constants.OAUTH_TOKEN_SECRET_PROPERTY, tokensecret);
        mAccountManager.setUserData(account, Constants.OAUTH_SESSION_HANDLE_PROPERTY, sessionhandle);
        
        final Intent intent = new Intent();
        
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, mUsername);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
        if (mAuthtokenType != null && mAuthtokenType.equals(Constants.AUTHTOKEN_TYPE)) {
            intent.putExtra(AccountManager.KEY_AUTHTOKEN, mAuthtoken);
        }
        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }
    
    protected void getOauthAccessToken() {
        Log.i(TAG, "getOauthAccessToken()");
        
        NetworkUtilities.getOauthRequestToken(oauthToken, oauthTokenSecret, oauthVerifier, mHandler, 
        		AuthenticatorActivity.this);  
    }

    /**
     * Hides the progress UI for a lengthy operation.
     */
    protected void hideProgress() {
    	try{
    		dismissDialog(0);
    	}
    	catch(IllegalArgumentException e){
    		
    	}
    }

    /**
     * Called when the authentication process completes (see attemptLogin()).
     */
    public void onAuthenticationResult(LoginResult result) {
        Log.i(TAG, "onAuthenticationResult(" + result + ")");
        // Hide the progress dialog
        hideProgress();
        if (result.getResult() && result.getToken() == null && result.getSessionHandle() == null) {
            if (!mConfirmCredentials) {
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = settings.edit();
            	editor.putString(Constants.PREFS_AUTH_TYPE, Constants.AUTH_TYPE_DELICIOUS);
            	editor.commit();
            	
                finishLogin(null);
            } else {
                finishConfirmCredentials(true);
            }
        } else if(result.getResult() && result.getToken() != null && result.getSessionHandle() == null){
        	oauthToken = result.getToken();
        	oauthTokenSecret = result.getTokenSecret();
        	
        	Intent i = new Intent(getApplicationContext(), OauthLogin.class);
        	i.putExtra("oauth_url", result.getRequestUrl());
        	startActivityForResult(i, 0);

        } else if(result.getResult() && result.getSessionHandle() != null){
        	Log.d(TAG, result.getToken());
        	
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = settings.edit();
            
            editor.putString(Constants.OAUTH_TOKEN_PROPERTY, result.getToken());
            editor.putString(Constants.OAUTH_TOKEN_SECRET_PROPERTY, result.getTokenSecret());
            editor.putString(Constants.OAUTH_SESSION_HANDLE_PROPERTY, result.getSessionHandle());
            editor.putString(Constants.PREFS_AUTH_TYPE, Constants.AUTH_TYPE_OAUTH);
            editor.commit();

        	finishLogin(result.getToken());

        }else {
            Log.e(TAG, "onAuthenticationResult: failed to authenticate");
            if (mRequestNewAccount) {
                // "Please enter a valid username/password.
                mMessage.setText(getText(R.string.login_activity_loginfail_text_both));
            } else {
                // "Please enter a valid password." (Used when the
                // account is already in the database but the password
                // doesn't work.)
                mMessage.setText(getText(R.string.login_activity_loginfail_text_pwonly));
            }
        }
    }

    /**
     * Returns the message to be displayed at the top of the login dialog box.
     */
    private CharSequence getMessage() {
        if (TextUtils.isEmpty(mUsername)) {
            // If no username, then we ask the user to log in using an
            // appropriate service.
            return getText(R.string.login_activity_newaccount_text);
        }
        if (TextUtils.isEmpty(mPassword)) {
            // We have an account but no password
            return getText(R.string.login_activity_loginfail_text_pwmissing);
        }
        return null;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);
        Bundle extras = intent.getExtras();
        oauthVerifier = extras.getString(Constants.OAUTH_VERIFIER_PROPERTY);
        Log.d("oauth_verifier", oauthVerifier);
        
        getOauthAccessToken();
    }

    /**
     * Shows the progress UI for a lengthy operation.
     */
    protected void showProgress() {
        showDialog(0);
    }
}
