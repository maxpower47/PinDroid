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

package com.deliciousdroid.client;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.deliciousdroid.Constants;
import com.deliciousdroid.authenticator.AuthenticatorActivity;
import com.deliciousdroid.authenticator.OauthUtilities;

import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.auth.AuthScope;
import android.net.Uri;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
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

    public static final String FETCH_FRIEND_UPDATES_URI = "http://feeds.delicious.com/v2/json/networkmembers/";
    public static final String FETCH_FRIEND_BOOKMARKS_URI = "http://feeds.delicious.com/v2/json/";
    public static final String FETCH_NETWORK_RECENT_BOOKMARKS_URI = "http://feeds.delicious.com/v2/json/network/";
    public static final String FETCH_STATUS_URI = "http://feeds.delicious.com/v2/json/network/";
    public static final String FETCH_TAGS_URI = "http://feeds.delicious.com/v2/json/tags/";

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
     * Attempts to authenticate to Delicious using a legacy Delicious account.
     * 
     * @param username The user's username.
     * @param password The user's password.
     * @param handler The hander instance from the calling UI thread.
     * @param context The context of the calling Activity.
     * @return The boolean result indicating whether the user was
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

        DefaultHttpClient client = HttpClientFactory.getThreadSafeClient();
        
        CredentialsProvider provider = client.getCredentialsProvider();
        Credentials credentials = new UsernamePasswordCredentials(username, password);
        provider.setCredentials(SCOPE, credentials);

        try {
            resp = client.execute(request);
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
     * Attempts to authenticate to Delicious using Yahoo OAuth authentication.  
     * This is the first step of the three party handshake.
     * 
     * @param username The user's username
     * @param password The user's password
     * @param handler The hander instance from the calling UI thread.
     * @param context The context of the calling Activity.
     * @return The boolean result indicating whether the user was
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

        try {
            resp = HttpClientFactory.getThreadSafeClient().execute(request);
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
     * Get a request token as part of the Yahoo OAuth authentication.  This is step 2
     * of the three party handshake.
     * 
     * @param token Request token returned by the initial authentication step.
     * @param tokenSecret Request token secret returned by the initial authentication step.
     * @param verifier Verification code received after user is prompted to log in to Yahoo.
     * @param handler The hander instance from the calling UI thread.
     * @param context The context of the calling Activity.
     * @return The boolean result indicating whether the user was
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

        try {
            resp = HttpClientFactory.getThreadSafeClient().execute(request);
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
     * Refresh an OAuth access token.  A fresh (unexpired) access token is required for every 
     * request.
     * 
     * @param account The account to retrieve a new access token for.
     * @param token The old acces token
     * @param context The context of the calling Activity.
     * @return The result of the api call.
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
   
        LoginResult result = null;
        
        try {
            resp = HttpClientFactory.getThreadSafeClient().execute(request);
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
     * Retrieves the Delicious username for an account authenticated with Yahoo OAuth.
     * 
     * @param authtoken The authentication token of the account.
     * @param tokensecret The token secret for the authentication token.
     * @param context The context of the calling Activity.
     * @return A String containing the Delicious username for the user.
     * @throws IOException If a server error was encountered.
     * @throws AuthenticationException If an authentication error was encountered.
     */
    public static String getOauthUserName(String authtoken, String tokensecret, Context context) 
    	throws IOException, AuthenticationException {

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

		post.setHeader("User-Agent", "DeliciousDroid");
    	

		Log.d("apiCall", "oauth");

		OauthUtilities.signRequest(post, params, authtoken, tokensecret);

		Log.d("header", post.getHeaders("Authorization")[0].getValue());
        
        resp = HttpClientFactory.getThreadSafeClient().execute(host, post);

    	if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
    		String response = EntityUtils.toString(resp.getEntity());
    		Log.d("response", response);
    		int start = response.indexOf("user=\"") + 6;
    		int end = response.indexOf("\"", start + 1);
    		String username = response.substring(start, end);
    		Log.d("username", username);
    		return username;
    	} else if(resp.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED){
    		throw new AuthenticationException();
    	} else throw new IOException();
    }
    
    /**
     * Gets the title of a web page.
     * 
     * @param url The URL of the web page.
     * @return A String containing the title of the web page.
     */
    public static String getWebpageTitle(String url) {
   	
    	if(url != null && !url.equals("")) {
    		
    		if(!url.startsWith("http")){
    			url = "http://" + url;
    		}
	
	    	HttpResponse resp = null;
	    	HttpGet post = null;
	    		
			post = new HttpGet(url);

			post.setHeader("User-Agent", "Mozilla/5.0");
	
	        try {
				resp = HttpClientFactory.getThreadSafeClient().execute(post);

		    	if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
		    		String response;

					response = EntityUtils.toString(resp.getEntity());
		    		Log.d("response", response);
		    		int start = response.indexOf("<title>") + 7;
		    		int end = response.indexOf("</title>", start + 1);
		    		String title = response.substring(start, end);
		    		Log.d("username", title);
		    		return title;
		    	} else return "";
			} catch (Exception e) {
				return "";
			}
    	} else return "";
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