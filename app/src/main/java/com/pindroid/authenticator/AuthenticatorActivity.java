/*
 * PinDroid - http://code.google.com/p/PinDroid/
 *
 * Copyright (C) 2010 Matt Schmidt
 *
 * PinDroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * PinDroid is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PinDroid; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */

package com.pindroid.authenticator;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.pindroid.R;
import com.pindroid.Constants;
import com.pindroid.client.PinboardApi;
import com.pindroid.providers.BookmarkContentProvider;
import com.pindroid.util.SyncUtils;

/**
 * Activity which displays login screen to the user.
 */
public class AuthenticatorActivity extends ActionBarActivity {
    public static final String PARAM_CONFIRMCREDENTIALS = "confirmCredentials";
    public static final String PARAM_PASSWORD = "password";
    public static final String PARAM_USERNAME = "username";
    public static final String PARAM_AUTHTOKEN_TYPE = "authtokenType";

    private static final String TAG = "AuthenticatorActivity";

    private AccountManager mAccountManager;
    private UserLoginTask mAuthTask = null;
    private DialogFragment mProgressDialog = null;
    
    private AccountAuthenticatorResponse mAccountAuthenticatorResponse = null;
    private Bundle mResultBundle = null;

    /**
     * If set we are just checking that the user knows their credentials; this
     * doesn't cause the user's password to be changed on the device.
     */
    private Boolean mConfirmCredentials = false;

    private TextView mMessage;
    private String mPassword;
    private EditText mPasswordEdit;

    /** Was the original caller asking for an entirely new account? */
    protected boolean mRequestNewAccount = false;

    private String mUsername;
    private EditText mUsernameEdit;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
        mAccountAuthenticatorResponse = getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);

        if (mAccountAuthenticatorResponse != null) {
            mAccountAuthenticatorResponse.onRequestContinued();
        }
        
        mAccountManager = AccountManager.get(this);
        final Intent intent = getIntent();
        mUsername = intent.getStringExtra(PARAM_USERNAME);
        mRequestNewAccount = mUsername == null;
        mConfirmCredentials = intent.getBooleanExtra(PARAM_CONFIRMCREDENTIALS, false);

        setContentView(R.layout.login_activity);
      
        mUsernameEdit = (EditText) findViewById(R.id.username_edit);
        mPasswordEdit = (EditText) findViewById(R.id.password_edit);
        mMessage = (TextView) findViewById(R.id.message);

        if (!TextUtils.isEmpty(mUsername)){
        	mUsernameEdit.setText(mUsername);
        	mPasswordEdit.requestFocus();
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
            mUsername = mUsernameEdit.getText().toString().trim();
        }
        mPassword = mPasswordEdit.getText().toString();
        
        if (TextUtils.isEmpty(mUsername) || TextUtils.isEmpty(mPassword)) {
            mMessage.setText(getMessage());
        } else {
            showProgress();
            // Start authenticating...
            mAuthTask = new UserLoginTask();
            mAuthTask.execute();
        }
    }

    /**
     * Called when response is received from the server for confirm credentials
     * request. See onAuthenticationResult(). Sets the
     * AccountAuthenticatorResult which is sent back to the caller.
     * 
     * @param the confirmCredentials result.
     */
    protected void finishConfirmCredentials(String authToken) {
        Log.i(TAG, "finishConfirmCredentials()");
        final Account account = new Account(mUsername, Constants.ACCOUNT_TYPE);
        mAccountManager.setAuthToken(account, Constants.AUTHTOKEN_TYPE, authToken);
        final Intent intent = new Intent();
        intent.putExtra(AccountManager.KEY_BOOLEAN_RESULT, authToken != null);
        intent.putExtra(AccountManager.KEY_AUTHTOKEN, authToken);
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
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        final int synctime = Integer.parseInt(settings.getString("pref_synctime", "0"));
        
        final Account account = new Account(mUsername, Constants.ACCOUNT_TYPE);

        if (mRequestNewAccount) {
            mAccountManager.addAccountExplicitly(account, null, null);

            ContentResolver.setSyncAutomatically(account, BookmarkContentProvider.AUTHORITY, true);
            if(synctime != 0) {
            	SyncUtils.addPeriodicSync(BookmarkContentProvider.AUTHORITY, Bundle.EMPTY, synctime, this);
            }
        }
        
        mAccountManager.setAuthToken(account, Constants.AUTHTOKEN_TYPE, authToken);
        
        final Intent intent = new Intent();
        
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, mUsername);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
        intent.putExtra(AccountManager.KEY_AUTHTOKEN, authToken);
        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * Called when the authentication process completes (see attemptLogin()).
     */
    public void onAuthenticationResult(String result) {
    	mAuthTask = null;
        hideProgress();
        if (result != null) {
            if (!mConfirmCredentials) {            	
                finishLogin(result);
            } else {
                finishConfirmCredentials(result);
            }
        } else {
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
        if (TextUtils.isEmpty(mPassword)) {
            // We have an account but no password
            return getText(R.string.login_activity_loginfail_text_pwmissing);
        }
        return null;
    }

    /**
     * Shows the progress UI for a lengthy operation.
     */
    protected void showProgress() {
    	mProgressDialog = ProgressDialogFragment.newInstance();
    	mProgressDialog.show(getSupportFragmentManager(), "dialog");
    }
    
    /**
     * Hides the progress UI for a lengthy operation.
     */
    private void hideProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }
    
    public void cancel(){
    	if (mAuthTask != null) {
            mAuthTask.cancel(true);
            finish();
        }
    }
    
    public void onAuthenticationCancel() {
        mAuthTask = null;
        hideProgress();
    }
    
    /**
     * Set the result that is to be sent as the result of the request that caused this
     * Activity to be launched. If result is null or this method is never called then
     * the request will be canceled.
     * @param result this is returned as the result of the AbstractAccountAuthenticator request
     */
    public final void setAccountAuthenticatorResult(Bundle result) {
        mResultBundle = result;
    }
    
    /**
     * Sends the result or a Constants.ERROR_CODE_CANCELED error if a result isn't present.
     */
    public void finish() {
        if (mAccountAuthenticatorResponse != null) {
            // send the result bundle back if set, otherwise send an error.
            if (mResultBundle != null) {
                mAccountAuthenticatorResponse.onResult(mResultBundle);
            } else {
                mAccountAuthenticatorResponse.onError(AccountManager.ERROR_CODE_CANCELED, "canceled");
            }
            mAccountAuthenticatorResponse = null;
        }
        super.finish();
    }
    
    /**
     * Represents an asynchronous task used to authenticate a user against the
     * SampleSync Service
     */
    public class UserLoginTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            // We do the actual work of authenticating the user
            // in the NetworkUtilities class.
            try {
                return PinboardApi.pinboardAuthenticate(mUsername, mPassword);
            } catch (Exception ex) {
                Log.e(TAG, "UserLoginTask.doInBackground: failed to authenticate");
                Log.e(TAG, "", ex);
                return null;
            }
        }

        @Override
        protected void onPostExecute(final String authToken) {
            // On a successful authentication, call back into the Activity to
            // communicate the authToken (or null for an error).
            onAuthenticationResult(authToken);
        }

        @Override
        protected void onCancelled() {
            // If the action was canceled (by the user clicking the cancel
            // button in the progress dialog), then call back into the
            // activity to let it know.
            onAuthenticationCancel();
        }
    }
    
    public static class ProgressDialogFragment extends DialogFragment {

    	public static ProgressDialogFragment newInstance() {
    		ProgressDialogFragment frag = new ProgressDialogFragment ();
    		return frag;
    	}

    	@Override
    	public Dialog onCreateDialog(Bundle savedInstanceState) {

    		final ProgressDialog dialog = new ProgressDialog(getActivity());
    		dialog.setMessage(getString(R.string.ui_activity_authenticating));
    		dialog.setIndeterminate(true);
    		dialog.setCancelable(true);
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    Log.i(TAG, "dialog cancel has been invoked");
                    ((AuthenticatorActivity)getActivity()).cancel();
                }
            });
    		return dialog;
    	}
    }
}