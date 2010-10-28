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

package com.android.droidlicious.client;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.android.droidlicious.Constants;
import com.android.droidlicious.authenticator.AuthenticatorActivity;
import com.android.droidlicious.authenticator.OauthUtilities;

import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.auth.AuthScope;
import android.net.Uri;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Date;
import java.util.Random;
import java.util.TreeMap;

/**
 * Provides utility methods for communicating with the server.
 */
public class NetworkUtilities {
    private static final String TAG = "NetworkUtilities";
    public static final String PARAM_USERNAME = "username";
    public static final String PARAM_PASSWORD = "password";
    public static final String PARAM_UPDATED = "timestamp";
    public static final String USER_AGENT = "AuthenticationService/1.0";
    public static final int REGISTRATION_TIMEOUT = 30 * 1000; // ms

    public static final String FETCH_FRIEND_UPDATES_URI = "http://feeds.delicious.com/v2/json/networkmembers/";
    public static final String FETCH_FRIEND_BOOKMARKS_URI = "http://feeds.delicious.com/v2/json/";
    public static final String FETCH_NETWORK_RECENT_BOOKMARKS_URI = "http://feeds.delicious.com/v2/json/network/";
    public static final String FETCH_STATUS_URI = "http://feeds.delicious.com/v2/json/network/";
    public static final String FETCH_TAGS_URI = "http://feeds.delicious.com/v2/json/tags/";
    private static DefaultHttpClient mHttpClient;
    
    private static final String SCHEME = "https";
    private static final String SCHEME_HTTP = "http";
    private static final String DELICIOUS_AUTHORITY = "api.del.icio.us";
    private static final int PORT = 443;
 
    private static final AuthScope SCOPE = new AuthScope(DELICIOUS_AUTHORITY, PORT);
    
    private static final String OAUTH_AUTHORITY = "api.login.yahoo.com";

    private static final String OAUTH_REQUEST_TOKEN_URI = "oauth/v2/get_request_token";
    private static final String OAUTH_GET_TOKEN_URI = "oauth/v2/get_token";
    private static final String OAUTH_GET_USERNAME_URI = "v2/posts/get";

    /**
     * Configures the httpClient to connect to the URL provided.
     */
    public static void maybeCreateHttpClient() {
        if (mHttpClient == null) {
            mHttpClient = new DefaultHttpClient();
            final HttpParams params = mHttpClient.getParams();
            HttpConnectionParams.setConnectionTimeout(params,
                REGISTRATION_TIMEOUT);
            HttpConnectionParams.setSoTimeout(params, REGISTRATION_TIMEOUT);
            ConnManagerParams.setTimeout(params, REGISTRATION_TIMEOUT);
        }
    }

    /**
     * Executes the network requests on a separate thread.
     * 
     * @param runnable The runnable instance containing network mOperations to
     *        be executed.
     */
    public static Thread performOnBackgroundThread(final Runnable runnable) {
        final Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    runnable.run();
                } finally {
                }
            }
        };
        t.start();
        return t;
    }

    /**
     * Connects to the Voiper server, authenticates the provided username and
     * password.
     * 
     * @param username The user's username
     * @param password The user's password
     * @param handler The hander instance from the calling UI thread.
     * @param context The context of the calling Activity.
     * @return boolean The boolean result indicating whether the user was
     *         successfully authenticated.
     */
    public static boolean deliciousAuthenticate(String username, String password,
        Handler handler, final Context context) {
        final HttpResponse resp;
        
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME);
        builder.authority(DELICIOUS_AUTHORITY);
        builder.appendEncodedPath("v1/tags/get");
        Uri uri = builder.build();

        HttpGet request = new HttpGet(String.valueOf(uri));
        maybeCreateHttpClient();
        
        CredentialsProvider provider = mHttpClient.getCredentialsProvider();
        Credentials credentials = new UsernamePasswordCredentials(username, password);
        provider.setCredentials(SCOPE, credentials);

        try {
            resp = mHttpClient.execute(request);
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, "Successful authentication");
                }
                sendResult(new LoginResult(true), handler, context);
                return true;
            } else {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, "Error authenticating" + resp.getStatusLine());
                }
                sendResult(new LoginResult(false), handler, context);
                return false;
            }
        } catch (final IOException e) {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "IOException when getting authtoken", e);
            }
            sendResult(new LoginResult(false), handler, context);
            return false;
        } finally {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "getAuthtoken completing");
            }
        }
    }
    
    /**
     * Yahoo OAuth Authentication Step 1
     * 
     * @param username The user's username
     * @param password The user's password
     * @param handler The hander instance from the calling UI thread.
     * @param context The context of the calling Activity.
     * @return boolean The boolean result indicating whether the user was
     *         successfully authenticated.
     */
    public static boolean oauthAuthenticate(Handler handler, final Context context) {
        final HttpResponse resp;
        
        Random r = new Random();
        String token = Long.toString(Math.abs(r.nextLong()), 36);
        
        Date d = new Date();
        
		Uri.Builder builder = new Uri.Builder();
		builder.scheme(SCHEME);
		builder.authority(OAUTH_AUTHORITY);
		builder.appendEncodedPath(OAUTH_REQUEST_TOKEN_URI);
		builder.appendQueryParameter(Constants.OAUTH_NONCE_PROPERTY, token);
		builder.appendQueryParameter(Constants.OAUTH_TIMESTAMP_PROPERTY, Long.toString(d.getTime()));
		builder.appendQueryParameter(Constants.OAUTH_COMSUMER_KEY_PROPERTY, Constants.OAUTH_CONSUMER_KEY);
		builder.appendQueryParameter(Constants.OAUTH_SIGNATURE_METHOD_PROPERTY, Constants.OAUTH_SIGNATURE_METHOD_PLAINTEXT);
		builder.appendQueryParameter(Constants.OAUTH_SIGNATURE_PROPERTY, Constants.OAUTH_SHARED_SECRET + "&");
		builder.appendQueryParameter(Constants.OAUTH_VERSION_PROPERTY, Constants.OAUTH_VERSION);
		builder.appendQueryParameter(Constants.OAUTH_LANG_PREF_PROPERTY, Constants.OAUTH_LANG_PREF);
		builder.appendQueryParameter(Constants.OAUTH_CALLBACK_PROPERTY, Constants.OAUTH_CALLBACK);
	
		Uri u = builder.build();

		Log.d(TAG, String.valueOf(u));
        
        HttpGet request = new HttpGet(String.valueOf(u));
        maybeCreateHttpClient();
        

        try {
            resp = mHttpClient.execute(request);
            final String response = EntityUtils.toString(resp.getEntity());
            
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

		    	LoginResult lr = new LoginResult(true, response);
		    	
		    	sendResult(lr, handler, context);
		    	
            } else {
            	sendResult(new LoginResult(false), handler, context);
            	return false;
            }
        } catch (final IOException e) {
        	Log.d("error", e.getStackTrace().toString());
        	sendResult(new LoginResult(false), handler, context);
        	return false;
        } finally {

        }
        return true;
    }
    
    /**
     * Yahoo OAuth Authentication Step 2
     * 
     * @param username The user's username
     * @param password The user's password
     * @param handler The hander instance from the calling UI thread.
     * @param context The context of the calling Activity.
     * @return boolean The boolean result indicating whether the user was
     *         successfully authenticated.
     */
    public static boolean getOauthRequestToken(String token, String tokenSecret, String verifier, 
    		Handler handler, final Context context) {
        final HttpResponse resp;
        
        Random r = new Random();
        String nonceToken = Long.toString(Math.abs(r.nextLong()), 36);
        
        Date d = new Date();
        
		Uri.Builder builder = new Uri.Builder();
		builder.scheme(SCHEME);
		builder.authority(OAUTH_AUTHORITY);
		builder.appendEncodedPath(OAUTH_GET_TOKEN_URI);
		builder.appendQueryParameter(Constants.OAUTH_NONCE_PROPERTY, nonceToken);
		builder.appendQueryParameter(Constants.OAUTH_TIMESTAMP_PROPERTY, Long.toString(d.getTime()));
		builder.appendQueryParameter(Constants.OAUTH_COMSUMER_KEY_PROPERTY, Constants.OAUTH_CONSUMER_KEY);
		builder.appendQueryParameter(Constants.OAUTH_SIGNATURE_METHOD_PROPERTY, Constants.OAUTH_SIGNATURE_METHOD_PLAINTEXT);
		builder.appendQueryParameter(Constants.OAUTH_SIGNATURE_PROPERTY, Constants.OAUTH_SHARED_SECRET + "&" + tokenSecret);
		builder.appendQueryParameter(Constants.OAUTH_VERSION_PROPERTY, Constants.OAUTH_VERSION);
		builder.appendQueryParameter(Constants.OAUTH_LANG_PREF_PROPERTY, Constants.OAUTH_LANG_PREF);
		builder.appendQueryParameter(Constants.OAUTH_CALLBACK_PROPERTY, Constants.OAUTH_CALLBACK);
		builder.appendQueryParameter(Constants.OAUTH_VERIFIER_PROPERTY, verifier);
		builder.appendQueryParameter(Constants.OAUTH_TOKEN_PROPERTY, token);
	
		Uri u = builder.build();

		Log.d(TAG, String.valueOf(u));
        
        HttpGet request = new HttpGet(String.valueOf(u));
        maybeCreateHttpClient();
        

        try {
            resp = mHttpClient.execute(request);
            final String response = EntityUtils.toString(resp.getEntity());
            
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

            	LoginResult lr = new LoginResult(true, response);
		    	
		    	sendResult(lr, handler, context);
		    	
            } else {
            	sendResult(new LoginResult(false), handler, context);
            	return false;
            }
        } catch (final IOException e) {
        	sendResult(new LoginResult(false), handler, context);
        	return false;
        } finally {

        }
        return true;
    }

    /**
     * Refresh an OAuth access token
     * 
     * @param username The user's username
     * @param password The user's password
     * @param handler The hander instance from the calling UI thread.
     * @param context The context of the calling Activity.
     * @return boolean The boolean result indicating whether the user was
     *         successfully authenticated.
     */
    public static LoginResult refreshOauthRequestToken(Account account, String token, final Context context) {
        final HttpResponse resp;
        
        final AccountManager am = AccountManager.get(context);
        String tokenSecret  = am.getUserData(account, Constants.OAUTH_TOKEN_SECRET_PROPERTY);
        String sessionHandle = am.getUserData(account, Constants.OAUTH_SESSION_HANDLE_PROPERTY);
        
        Random r = new Random();
        String nonceToken = Long.toString(Math.abs(r.nextLong()), 36);
        
        Log.d("old token", token);
        
        Date d = new Date();
        
		Uri.Builder builder = new Uri.Builder();
		builder.scheme(SCHEME);
		builder.authority(OAUTH_AUTHORITY);
		builder.appendEncodedPath(OAUTH_GET_TOKEN_URI);
		builder.appendQueryParameter(Constants.OAUTH_NONCE_PROPERTY, nonceToken);
		builder.appendQueryParameter(Constants.OAUTH_TIMESTAMP_PROPERTY, Long.toString(d.getTime()));
		builder.appendQueryParameter(Constants.OAUTH_COMSUMER_KEY_PROPERTY, Constants.OAUTH_CONSUMER_KEY);
		builder.appendQueryParameter(Constants.OAUTH_SIGNATURE_METHOD_PROPERTY, Constants.OAUTH_SIGNATURE_METHOD_PLAINTEXT);
		builder.appendQueryParameter(Constants.OAUTH_SIGNATURE_PROPERTY, Constants.OAUTH_SHARED_SECRET + "&" + tokenSecret);
		builder.appendQueryParameter(Constants.OAUTH_VERSION_PROPERTY, Constants.OAUTH_VERSION);
		builder.appendQueryParameter(Constants.OAUTH_SESSION_HANDLE_PROPERTY, sessionHandle);
		builder.appendQueryParameter(Constants.OAUTH_TOKEN_PROPERTY, token);
	
		Uri u = builder.build();

		Log.d("Refresh Token", String.valueOf(u));
        
        HttpGet request = new HttpGet(String.valueOf(u));
        maybeCreateHttpClient();
        
        LoginResult result = null;
        
        try {
            resp = mHttpClient.execute(request);
            final String response = EntityUtils.toString(resp.getEntity());
            
            Log.d("Refresh Token Response", response);
            
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {      
            	
        		Log.d("new token", token);

        		result = new LoginResult(true, response);

            } else {

            }
        } catch (final IOException e) {

        } finally {

        }
        return result;
    }
    
    /**
     * Fetches users bookmarks
     * 
     * @param account The account being synced.
     * @param authtoken The authtoken stored in the AccountManager for the
     *        account
     * @return list The list of bookmarks received from the server.
     */
    public static String getOauthUserName(String authtoken, String tokensecret, Context context) 
    	throws IOException {

    	TreeMap<String, String> params = new TreeMap<String, String>();
    	String url = OAUTH_GET_USERNAME_URI;

    	params.put("count", "1");

    	HttpResponse resp = null;
    	HttpGet post = null;
    	
		Uri.Builder builder = new Uri.Builder();
		builder.scheme(SCHEME_HTTP);
		builder.authority(DELICIOUS_AUTHORITY);
		builder.appendEncodedPath(url);
		for(String key : params.keySet()){
			builder.appendQueryParameter(key, params.get(key));
		}
		
		Log.d("getUsername", builder.build().toString().replace("%3A", ":").replace("%2F", "/").replace("%2B", "+"));
		post = new HttpGet(builder.build().toString().replace("%3A", ":").replace("%2F", "/").replace("%2B", "+"));
		HttpHost host = new HttpHost(DELICIOUS_AUTHORITY);
		maybeCreateHttpClient();
		post.setHeader("User-Agent", "Droidlicious");
    	
		try{
    		Log.d("apiCall", "oauth");

			OauthUtilities.signRequest(post, params, authtoken, tokensecret);

			Log.d("header", post.getHeaders("Authorization")[0].getValue());
	        
	        resp = mHttpClient.execute(host, post);

	    	if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
	    		String response = EntityUtils.toString(resp.getEntity());
	    		Log.d("response", response);
	    		int start = response.indexOf("user=\"") + 6;
	    		int end = response.indexOf("\"", start + 1);
	    		String username = response.substring(start, end);
	    		Log.d("username", username);
	    		return username;
	    	} else {
	    		throw new IOException();
	    	}
		} catch(Exception e){
			Log.e("DeliciousApiCall Error", Integer.toString(resp.getStatusLine().getStatusCode()));
		}
		throw new IOException();
    }
    
    /**
     * Sends the authentication response from server back to the caller main UI
     * thread through its handler.
     * 
     * @param result The boolean holding authentication result
     * @param handler The main UI thread's handler instance.
     * @param context The caller Activity's context.
     */
    private static void sendResult(final LoginResult result, final Handler handler,
        final Context context) {
        if (handler == null || context == null) {
            return;
        }
        
        handler.post(new Runnable() {
            public void run() {
                ((AuthenticatorActivity) context).onAuthenticationResult(result);
            }
        });
    }

    /**
     * Attempts to authenticate the user credentials on the server.
     * 
     * @param username The user's username
     * @param password The user's password to be authenticated
     * @param handler The main UI thread's handler instance.
     * @param context The caller Activity's context
     * @return Thread The thread on which the network mOperations are executed.
     */
    public static Thread attemptAuth(final String username,
        final String password, final int authType, final Handler handler, final Context context) {
        final Runnable runnable = new Runnable() {
            public void run() {
            	if(authType == 0){
            		deliciousAuthenticate(username, password, handler, context);
            	}
            	else{
            		oauthAuthenticate(handler, context);
            	}
            }
        };
        // run on background thread.
        return NetworkUtilities.performOnBackgroundThread(runnable);
    }
}