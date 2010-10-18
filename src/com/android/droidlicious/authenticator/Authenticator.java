/*
 * Copyright (C) 2010 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.android.droidlicious.authenticator;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.android.droidlicious.Constants;
import com.android.droidlicious.client.LoginResult;
import com.android.droidlicious.client.NetworkUtilities;
import com.android.droidlicious.R;

/**
 * This class is an implementation of AbstractAccountAuthenticator for
 * authenticating accounts in the com.android.droidlicious domain.
 */
class Authenticator extends AbstractAccountAuthenticator {
    // Authentication Service context
    private final Context mContext;

    public Authenticator(Context context) {
        super(context);
        mContext = context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response,
        String accountType, String authTokenType, String[] requiredFeatures,
        Bundle options) {
        final Intent intent = new Intent(mContext, AuthenticatorActivity.class);
        intent.putExtra(AuthenticatorActivity.PARAM_AUTHTOKEN_TYPE, authTokenType);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        
		SharedPreferences settings = mContext.getSharedPreferences(Constants.AUTH_PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("first_time", true);
        editor.commit();
        
        return bundle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response,
        Account account, Bundle options) {
        if (options != null && options.containsKey(AccountManager.KEY_PASSWORD)) {
            final String password =
                options.getString(AccountManager.KEY_PASSWORD);
            final boolean verified =
                onlineConfirmPassword(account.name, password);
            final Bundle result = new Bundle();
            result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, verified);
            return result;
        }
        // Launch AuthenticatorActivity to confirm credentials
        final Intent intent = new Intent(mContext, AuthenticatorActivity.class);
        intent.putExtra(AuthenticatorActivity.PARAM_USERNAME, account.name);
        intent.putExtra(AuthenticatorActivity.PARAM_CONFIRMCREDENTIALS, true);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE,
            response);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response,
        String accountType) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response,
        Account account, String authTokenType, Bundle loginOptions) {
    	Log.d("getAuthToken", "blah");
    	SharedPreferences settings = mContext.getSharedPreferences(Constants.AUTH_PREFS_NAME, 0);
    	
        if (!authTokenType.equals(Constants.AUTHTOKEN_TYPE)) {
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ERROR_MESSAGE, "invalid authTokenType");
            return result;
        }
        final AccountManager am = AccountManager.get(mContext);
        final String password = am.getPassword(account);
        if(settings.getString(Constants.PREFS_AUTH_TYPE, Constants.AUTH_TYPE_DELICIOUS) == Constants.AUTH_TYPE_DELICIOUS){
        	Log.d("getAuthToken", "notoauth");
        	
	        if (password != null) {
	            final boolean verified =
	                onlineConfirmPassword(account.name, password);
	            if (verified) {
	                final Bundle result = new Bundle();
	                result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
	                result.putString(AccountManager.KEY_ACCOUNT_TYPE,
	                    Constants.ACCOUNT_TYPE);
	                result.putString(AccountManager.KEY_AUTHTOKEN, password);
	                return result;
	            }
	        }
	        // the password was missing or incorrect, return an Intent to an
	        // Activity that will prompt the user for the password.
	        final Intent intent = new Intent(mContext, AuthenticatorActivity.class);
	        intent.putExtra(AuthenticatorActivity.PARAM_USERNAME, account.name);
	        intent.putExtra(AuthenticatorActivity.PARAM_AUTHTOKEN_TYPE,
	            authTokenType);
	        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE,
	            response);
	        final Bundle bundle = new Bundle();
	        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
	        return bundle;
        } else {
        	Log.d("getAuthToken", "oauth");
    			
        	String token;
        	if(!settings.getBoolean("first_time", true)) {
        		
        		String oldauthtoken = settings.getString(Constants.OAUTH_TOKEN_PROPERTY, "");
        		
        		LoginResult lresult = NetworkUtilities.refreshOauthRequestToken(oldauthtoken, mContext);
        		
            	SharedPreferences.Editor editor = settings.edit();
                editor.putString(Constants.OAUTH_TOKEN_SECRET_PROPERTY, lresult.getTokenSecret());
                editor.putString(Constants.OAUTH_TOKEN_PROPERTY, lresult.getToken());
                editor.commit();
            	
            	Log.d("loginresult token", lresult.getToken());
            	Log.d("loginresult token secret", lresult.getTokenSecret());
            	
            	token = lresult.getToken();
            	
            	am.setAuthToken(account, authTokenType, token);

        	} else {
        		SharedPreferences.Editor editor = settings.edit();
        		editor.putBoolean("first_time", false);
                editor.commit();
                token = password;
        	}
        	
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE,
                Constants.ACCOUNT_TYPE);
            result.putString(AccountManager.KEY_AUTHTOKEN, token);
            return result;
        	   	
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAuthTokenLabel(String authTokenType) {
        if (authTokenType.equals(Constants.AUTHTOKEN_TYPE)) {
            return mContext.getString(R.string.label);
        }
        return null;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response,
        Account account, String[] features) {
        final Bundle result = new Bundle();
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
        return result;
    }

    /**
     * Validates user's password on the server
     */
    private boolean onlineConfirmPassword(String username, String password) {
    	SharedPreferences settings = mContext.getSharedPreferences(Constants.AUTH_PREFS_NAME, 0);
    	
    	if(settings.getString(Constants.PREFS_AUTH_TYPE, Constants.AUTH_TYPE_DELICIOUS) == Constants.AUTH_TYPE_DELICIOUS){
    		return NetworkUtilities.deliciousAuthenticate(username, password, null, null);
    	} else {
    		return true;
    	}
    	
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response,
        Account account, String authTokenType, Bundle loginOptions) {
        final Intent intent = new Intent(mContext, AuthenticatorActivity.class);
        intent.putExtra(AuthenticatorActivity.PARAM_USERNAME, account.name);
        intent.putExtra(AuthenticatorActivity.PARAM_AUTHTOKEN_TYPE, authTokenType);
        intent.putExtra(AuthenticatorActivity.PARAM_CONFIRMCREDENTIALS, false);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

}
