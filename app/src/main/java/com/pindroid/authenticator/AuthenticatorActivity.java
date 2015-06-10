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
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.dd.CircularProgressButton;
import com.pindroid.Constants;
import com.pindroid.R;
import com.pindroid.client.PinboardApi;
import com.pindroid.providers.BookmarkContentProvider;
import com.pindroid.util.SyncUtils;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.EditorAction;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.TextChange;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

/**
 * Activity which displays login screen to the user.
 */
@EActivity(R.layout.login_activity)
public class AuthenticatorActivity extends AppCompatActivity {
    public static final String PARAM_CONFIRMCREDENTIALS = "confirmCredentials";
    public static final String PARAM_USERNAME = "username";
    public static final String PARAM_AUTHTOKEN_TYPE = "authtokenType";

    private static final String TAG = "AuthenticatorActivity";

    private AccountManager mAccountManager;
    
    private AccountAuthenticatorResponse mAccountAuthenticatorResponse = null;
    private Bundle mResultBundle = null;

    @SystemService InputMethodManager inputMethodManager;

    /**
     * If set we are just checking that the user knows their credentials; this
     * doesn't cause the user's password to be changed on the device.
     */
    private Boolean mConfirmCredentials = false;

	@ViewById(R.id.username_edit) EditText mUsernameEdit;
	@ViewById(R.id.password_edit) EditText mPasswordEdit;
    @ViewById(R.id.message) TextView mMessage;
    @ViewById(R.id.login_button) CircularProgressButton mLoginButton;

    /** Was the original caller asking for an entirely new account? */
    protected boolean mRequestNewAccount = false;

    private String mUsername;
	private String mPassword;

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
    }

	@AfterViews
	public void init() {
		if (!TextUtils.isEmpty(mUsername)){
			mUsernameEdit.setText(mUsername);
			mPasswordEdit.requestFocus();
		}
        mLoginButton.setIndeterminateProgressMode(true);
	}

    /**
     * Handles onClick event on the Submit button. Sends username/password to
     * the server for authentication.
     */
    @Click(R.id.login_button)
    public void handleLogin() {
        if (mRequestNewAccount) {
            mUsername = mUsernameEdit.getText().toString().trim();
        }
        mPassword = mPasswordEdit.getText().toString();

        if(inputMethodManager.isAcceptingText()) {
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }

        mLoginButton.setProgress(50);
        authenticate();
    }

    @EditorAction(R.id.password_edit)
    void onEditorActionPassword(int actionId) {
        if (mLoginButton.isEnabled() && actionId == EditorInfo.IME_ACTION_DONE) {
            handleLogin();
        }
    }

    @TextChange({R.id.username_edit, R.id.password_edit})
    void textChanged() {
        mLoginButton.setEnabled(!TextUtils.isEmpty(mUsernameEdit.getText()) && !TextUtils.isEmpty(mPasswordEdit.getText()));
    }

    /**
     * Called when response is received from the server for confirm credentials
     * request. See onAuthenticationResult(). Sets the
     * AccountAuthenticatorResult which is sent back to the caller.
     * 
     * @param authToken the confirmCredentials result.
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
     * @param authToken the confirmCredentials result.
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
    @UiThread
    public void onAuthenticationResult(String result) {
        if (result != null) {
            if (!mConfirmCredentials) {
                finishLogin(result);
            } else {
                finishConfirmCredentials(result);
            }
        } else {
            Log.e(TAG, "onAuthenticationResult: failed to authenticate");
            mLoginButton.setProgress(0);
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

    @Background
    void authenticate() {
        onAuthenticationResult(PinboardApi.pinboardAuthenticate(mUsername, mPassword));
    }
}