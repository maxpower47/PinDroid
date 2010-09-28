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
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.android.droidlicious.authenticator.AuthenticatorActivity;

import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.auth.AuthScope;
import android.net.Uri;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

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
    public static final String FETCH_STATUS_URI = "http://feeds.delicious.com/v2/json/network/";
    public static final String FETCH_TAGS_URI = "http://feeds.delicious.com/v2/json/tags/";
    public static final String FETCH_BOOKMARKS_URI = "http://feeds.delicious.com/v2/json/";
    public static final String ADD_BOOKMARKS_URI = "v1/posts/add";
    public static final String OAUTH_ADD_BOOKMARKS_URI = "v2/posts/add";
    private static DefaultHttpClient mHttpClient;
    
    private static final String SCHEME = "https";
    private static final String SCHEME_HTTP = "http";
    private static final String DELICIOUS_AUTHORITY = "api.del.icio.us";
    private static final int PORT = 443;
 
    private static final AuthScope SCOPE = new AuthScope(DELICIOUS_AUTHORITY, PORT);
    
    private static final String OAUTH_AUTHORITY = "api.login.yahoo.com";
    private static final String OAUTH_CONSUMER_KEY = "dj0yJmk9OFMyMU03NlVOYlJNJmQ9WVdrOVN6Qk5TR0ZhTjJrbWNHbzlNQS0tJnM9Y29uc3VtZXJzZWNyZXQmeD03NA--";
    private static final String OAUTH_SHARED_SECRET = "ba0a2f0d1ecadb6d3f79eb0e875689ea6890af27";
    private static final String OAUTH_APPLICATION_ID = "K0MHaZ7i";
    private static final String OAUTH_REQUEST_TOKEN_URI = "oauth/v2/get_request_token";
    private static final String OAUTH_GET_TOKEN_URI = "oauth/v2/get_token";
    private static final String OAUTH_VERSION = "1.0";
    private static final String OAUTH_LANG = "en-us";
    private static final String OAUTH_CALLBACK = "oob";
    private static final String OAUTH_SIGNATURE_METHOD = "PLAINTEXT";

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
    public static boolean oauthAuthenticate(String username, String password,
        Handler handler, final Context context) {
        final HttpResponse resp;
        
        Random r = new Random();
        String token = Long.toString(Math.abs(r.nextLong()), 36);
        
        Date d = new Date();
        
		Uri.Builder builder = new Uri.Builder();
		builder.scheme(SCHEME);
		builder.authority(OAUTH_AUTHORITY);
		builder.appendEncodedPath(OAUTH_REQUEST_TOKEN_URI);
		builder.appendQueryParameter("oauth_nonce", token);
		builder.appendQueryParameter("oauth_timestamp", Long.toString(d.getTime()));
		builder.appendQueryParameter("oauth_consumer_key", OAUTH_CONSUMER_KEY);
		builder.appendQueryParameter("oauth_signature_method", OAUTH_SIGNATURE_METHOD);
		builder.appendQueryParameter("oauth_signature", OAUTH_SHARED_SECRET + "&");
		builder.appendQueryParameter("oauth_version", OAUTH_VERSION);
		builder.appendQueryParameter("xoauth_lang_pref", OAUTH_LANG);
		builder.appendQueryParameter("oauth_callback", OAUTH_CALLBACK);
	
		Uri u = builder.build();

		Log.d(TAG, String.valueOf(u));
        
        HttpGet request = new HttpGet(String.valueOf(u));
        maybeCreateHttpClient();
        

        try {
            resp = mHttpClient.execute(request);
            final String response = EntityUtils.toString(resp.getEntity());
            
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            	String oauth_token_secret = "";
            	String oauth_expires_in = "";
            	String xoauth_request_auth_url = "";
            	String oauth_token = "";
            	String oauth_callback_confirmed = "";
            	
            	String[] responseParams = response.split("&");
            	for(String s : responseParams){
            		if(s.contains("oauth_token_secret=")){
            			oauth_token_secret = s.split("=")[1];
            		}
            		if(s.contains("oauth_expires_in=")){
            			oauth_expires_in = s.split("=")[1];
            		}
            		if(s.contains("xoauth_request_auth_url=")){
            			xoauth_request_auth_url = s.split("=")[1];
            		}
            		if(s.contains("oauth_token=")){
            			oauth_token = s.split("=")[1];
            		}
            		if(s.contains("oauth_callback_confirmed=")){
            			oauth_callback_confirmed = s.split("=")[1];
            		}
            	}
            	
        		Log.d("oauth_token_secret", oauth_token_secret);
        		Log.d("oauth_expires_in", oauth_expires_in);
        		Log.d("xoauth_request_auth_url", xoauth_request_auth_url);
        		Log.d("oauth_token", oauth_token);
        		Log.d("oauth_callback_confirmed", oauth_callback_confirmed);
            	
		    	String link = URLDecoder.decode(xoauth_request_auth_url);

		    	LoginResult lr = new LoginResult(true, oauth_token_secret, oauth_expires_in,
		    			link, oauth_token, oauth_callback_confirmed);
		    	
		    	sendResult(lr, handler, context);
		    	
            } else {

            }
        } catch (final IOException e) {

        } finally {

        }
        return true;
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
		builder.appendQueryParameter("oauth_nonce", nonceToken);
		builder.appendQueryParameter("oauth_timestamp", Long.toString(d.getTime()));
		builder.appendQueryParameter("oauth_consumer_key", OAUTH_CONSUMER_KEY);
		builder.appendQueryParameter("oauth_signature_method", OAUTH_SIGNATURE_METHOD);
		builder.appendQueryParameter("oauth_signature", OAUTH_SHARED_SECRET + "&" + tokenSecret);
		builder.appendQueryParameter("oauth_version", OAUTH_VERSION);
		builder.appendQueryParameter("xoauth_lang_pref", OAUTH_LANG);
		builder.appendQueryParameter("oauth_callback", OAUTH_CALLBACK);
		builder.appendQueryParameter("oauth_verifier", verifier);
		builder.appendQueryParameter("oauth_token", token);
	
		Uri u = builder.build();

		Log.d(TAG, String.valueOf(u));
        
        HttpGet request = new HttpGet(String.valueOf(u));
        maybeCreateHttpClient();
        

        try {
            resp = mHttpClient.execute(request);
            final String response = EntityUtils.toString(resp.getEntity());
            
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            	String oauth_token_secret = "";
            	String oauth_expires_in = "";
            	String xoauth_yahoo_guid = "";
            	String oauth_token = "";
            	String oauth_authorization_expires_in = "";
            	String oauth_session_handle = "";
            	
            	String[] responseParams = response.split("&");
            	for(String s : responseParams){
            		if(s.contains("oauth_token_secret=")){
            			oauth_token_secret = s.split("=")[1];
            		}
            		if(s.contains("oauth_expires_in=")){
            			oauth_expires_in = s.split("=")[1];
            		}
            		if(s.contains("xoauth_yahoo_guid=")){
            			xoauth_yahoo_guid = s.split("=")[1];
            		}
            		if(s.contains("oauth_token=")){
            			oauth_token = s.split("=")[1];
            		}
            		if(s.contains("oauth_authorization_expires_in=")){
            			oauth_authorization_expires_in = s.split("=")[1];
            		}
            		if(s.contains("oauth_session_handle=")){
            			oauth_session_handle = s.split("=")[1];
            		}
            	}
            	
        		Log.d("oauth_token_secret", oauth_token_secret);
        		Log.d("oauth_expires_in", oauth_expires_in);
        		Log.d("xoauth_yahoo_guid", xoauth_yahoo_guid);
        		Log.d("oauth_token", oauth_token);
        		Log.d("oauth_authorization_expires_in", oauth_authorization_expires_in);
        		Log.d("oauth_session_handle", oauth_session_handle);
        		
		    	LoginResult lr = new LoginResult(true, oauth_token, oauth_token_secret,
		    			oauth_session_handle, oauth_expires_in, oauth_authorization_expires_in,
		    			xoauth_yahoo_guid);
		    	
		    	sendResult(lr, handler, context);
		    	
            } else {

            }
        } catch (final IOException e) {

        } finally {

        }
        return true;
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
            		oauthAuthenticate(username, password, handler, context);
            	}
            }
        };
        // run on background thread.
        return NetworkUtilities.performOnBackgroundThread(runnable);
    }

    /**
     * Fetches the list of friend data updates from the server
     * 
     * @param account The account being synced.
     * @param authtoken The authtoken stored in AccountManager for this account
     * @param lastUpdated The last time that sync was performed
     * @return list The list of updates received from the server.
     */
    public static List<User> fetchFriendUpdates(Account account,
        String authtoken, Date lastUpdated) throws JSONException,
        ParseException, IOException, AuthenticationException {
        final ArrayList<User> friendList = new ArrayList<User>();


        final HttpGet post = new HttpGet(FETCH_FRIEND_UPDATES_URI + account.name);
        maybeCreateHttpClient();

        final HttpResponse resp = mHttpClient.execute(post);
        final String response = EntityUtils.toString(resp.getEntity());

        if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            // Succesfully connected to the samplesyncadapter server and
            // authenticated.
            // Extract friends data in json format.
            final JSONArray friends = new JSONArray(response);
            Log.d(TAG, response);
            for (int i = 0; i < friends.length(); i++) {
                friendList.add(User.valueOf(friends.getJSONObject(i)));
            }
        } else {
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                Log.e(TAG,
                    "Authentication exception in fetching remote contacts");
                throw new AuthenticationException();
            } else {
                Log.e(TAG, "Server error in fetching remote contacts: "
                    + resp.getStatusLine());
                throw new IOException();
            }
        }
        return friendList;
    }

    /**
     * Fetches status messages for the user's friends from the server
     * 
     * @param account The account being synced.
     * @param authtoken The authtoken stored in the AccountManager for the
     *        account
     * @return list The list of status messages received from the server.
     */
    public static List<User.Status> fetchFriendStatuses(Account account,
        String authtoken) throws JSONException, ParseException, IOException,
        AuthenticationException {
        final ArrayList<User.Status> statusList = new ArrayList<User.Status>();

        final HttpGet post = new HttpGet(FETCH_STATUS_URI + account.name + "?count=15");
        maybeCreateHttpClient();

        final HttpResponse resp = mHttpClient.execute(post);
        final String response = EntityUtils.toString(resp.getEntity());

        if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            // Succesfully connected to the samplesyncadapter server and
            // authenticated.
            // Extract friends data in json format.
            final JSONArray statuses = new JSONArray(response);
            Log.d(TAG, response);
            for (int i = 0; i < statuses.length(); i++) {
                statusList.add(User.Status.valueOf(statuses.getJSONObject(i)));
            }
        } else {
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                Log.e(TAG,
                    "Authentication exception in fetching friend status list");
                throw new AuthenticationException();
            } else {
                Log.e(TAG, "Server error in fetching friend status list");
                throw new IOException();
            }
        }
        return statusList;
    }
    
    /**
     * Fetches status messages for the user's friends from the server
     * 
     * @param account The account being synced.
     * @param authtoken The authtoken stored in the AccountManager for the
     *        account
     * @return list The list of status messages received from the server.
     */
    public static ArrayList<User.Tag> fetchTags(String userName, Account account,
        String authtoken) throws JSONException, ParseException, IOException,
        AuthenticationException {

        final HttpGet post = new HttpGet(FETCH_TAGS_URI + userName + "?count=100");
        maybeCreateHttpClient();
        
        final ArrayList<User.Tag> tagList = new ArrayList<User.Tag>();

        final HttpResponse resp = mHttpClient.execute(post);
        final String response = EntityUtils.toString(resp.getEntity());

        if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            // Succesfully connected to the samplesyncadapter server and
            // authenticated.
            // Extract friends data in json format.
            final JSONObject tags = new JSONObject(response);
            Iterator<?> i = tags.keys();
            while(i.hasNext()){
            	Object e = i.next();
            	Log.d("tag", e.toString());
            	tagList.add(new User.Tag(e.toString(), tags.getInt(e.toString())));
            }
            
            Log.d(TAG, response);

        } else {
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                Log.e(TAG,
                    "Authentication exception in fetching friend status list");
                throw new AuthenticationException();
            } else {
                Log.e(TAG, "Server error in fetching friend status list");
                throw new IOException();
            }
        }
        return tagList;
    }
    
    /**
     * Fetches status messages for the user's friends from the server
     * 
     * @param account The account being synced.
     * @param authtoken The authtoken stored in the AccountManager for the
     *        account
     * @return list The list of bookmarks received from the server.
     */
    public static ArrayList<User.Bookmark> fetchBookmarks(String userName, String tagName, Account account,
        String authtoken) throws JSONException, ParseException, IOException,
        AuthenticationException {

        final HttpGet post = new HttpGet(FETCH_BOOKMARKS_URI + userName + "/" + tagName + "?count=100");
        maybeCreateHttpClient();
        
        final ArrayList<User.Bookmark> bookmarkList = new ArrayList<User.Bookmark>();

        final HttpResponse resp = mHttpClient.execute(post);
        final String response = EntityUtils.toString(resp.getEntity());

        if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            // Succesfully connected to the samplesyncadapter server and
            // authenticated.
            // Extract friends data in json format.
            final JSONArray bookmarks = new JSONArray(response);
            Log.d(TAG, response);
            
            for (int i = 0; i < bookmarks.length(); i++) {
                bookmarkList.add(User.Bookmark.valueOf(bookmarks.getJSONObject(i)));
            }            
        } else {
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                Log.e(TAG,
                    "Authentication exception in fetching friend status list");
                throw new AuthenticationException();
            } else {
                Log.e(TAG, "Server error in fetching friend status list");
                throw new IOException();
            }
        }
        return bookmarkList;
    }
    
    public static Boolean addBookmarks(User.Bookmark bookmark, Account account,
        String authtoken) throws Exception {
    	
    	String username = account.name;
    	String password =  authtoken;
    	String response = null;
    	
    	if(password.startsWith("oauth:")) {
    	
            Random r = new Random();
            String token = Long.toString(Math.abs(r.nextLong()), 36);
            
            Date d = new Date();
            String timestamp = Long.toString(d.getTime() / 1000);
    		
    		StringBuilder sb = new StringBuilder();
    		sb.append("GET");
    		sb.append("&http%3A%2F%2Fapi.del.icio.us%2Fv2%2Fposts%2Fadd");
    		sb.append("&description%3D");
    		sb.append(URLEncoder.encode(bookmark.getDescription()));
    		//sb.append("%26extended%3D");
    		//sb.append(URLEncoder.encode(bookmark.getNotes()));
    		sb.append("%26oauth_consumer_key%3D");
    		sb.append(URLEncoder.encode(OAUTH_CONSUMER_KEY));
    		sb.append("%26oauth_nonce%3D");
    		sb.append(URLEncoder.encode(token));
    		sb.append("%26oauth_signature_method%3D");
    		sb.append(URLEncoder.encode("HMAC-SHA1"));
    		sb.append("%26oauth_timestamp%3D");
    		sb.append(URLEncoder.encode(timestamp));
    		sb.append("%26oauth_token%3D");
    		sb.append(URLEncoder.encode(password.split(":")[1]));
    		sb.append("%26oauth_version%3D");
    		sb.append(URLEncoder.encode(OAUTH_VERSION));
    		sb.append("%26url%3D");
    		sb.append(URLEncoder.encode("www.yahoo.com"));
    		
    		Log.d("base string", sb.toString());
    		
    		String keystring = OAUTH_SHARED_SECRET + "&" + password.split(":")[2];
    	
    		Log.d("key string", keystring);
    		
    		SecretKeySpec sha1key = new SecretKeySpec(keystring.getBytes(), "HmacSHA1");
    		Mac mac = Mac.getInstance("HmacSHA1");
    		mac.init(sha1key);
    		
    		byte[] sigBytes = mac.doFinal(sb.toString().getBytes());
    		String signature = Base64.encodeToString(sigBytes, Base64.NO_WRAP);
    		
    		Log.d("signature", signature);
    		

    		
			Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_HTTP);
			builder.authority(DELICIOUS_AUTHORITY);
			builder.appendEncodedPath(OAUTH_ADD_BOOKMARKS_URI);
			builder.appendQueryParameter("description", bookmark.getDescription());
			//builder.appendQueryParameter("extended", bookmark.getNotes());
			builder.appendQueryParameter("url", URLEncoder.encode("www.yahoo.com"));
		
			Uri u = builder.build();
			Log.d("URI", u.toString());
			HttpGet post = new HttpGet(u.toString());
			HttpHost host = new HttpHost(DELICIOUS_AUTHORITY);
			
    		StringBuilder sb2 = new StringBuilder();
    		sb2.append("OAuth ");
    		sb2.append("realm=");
    		sb2.append("\"yahooapis.com\"");
    		sb2.append(",oauth_consumer_key=");
    		sb2.append("\"" + OAUTH_CONSUMER_KEY + "\"");
    		sb2.append(",oauth_nonce=");
    		sb2.append("\"" + token + "\"");
    		sb2.append(",oauth_signature=");
    		sb2.append("\"" + signature + "\"");
    		sb2.append(",oauth_signature_method=");
    		sb2.append("\"" + "HMAC-SHA1" + "\"");
    		sb2.append(",oauth_timestamp=");
    		sb2.append("\"" + timestamp + "\"");
    		sb2.append(",oauth_token=");
    		sb2.append("\"" + password.split(":")[1] + "\"");
    		sb2.append(",oauth_version=");
    		sb2.append("\"" + OAUTH_VERSION + "\"");


			
			post.setHeader("Authorization", sb2.toString());
			//post.setHeader("Content-type", "application/x-www-form-urlencoded");
			Log.d("header", sb2.toString());
    		
	        maybeCreateHttpClient();
	        
	        final HttpResponse resp = mHttpClient.execute(host, post);
	        response = EntityUtils.toString(resp.getEntity());
	        
    		
    	} else{
			Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME);
			builder.authority(DELICIOUS_AUTHORITY);
			builder.appendEncodedPath(ADD_BOOKMARKS_URI);
			builder.appendQueryParameter("url", bookmark.getUrl());
			builder.appendQueryParameter("description", bookmark.getDescription());
			builder.appendQueryParameter("extended", bookmark.getNotes());
		
			Uri u = builder.build();
			
			HttpGet post = new HttpGet(u.toString());
			
	        maybeCreateHttpClient();
	        
	        CredentialsProvider provider = mHttpClient.getCredentialsProvider();
	        Credentials credentials = new UsernamePasswordCredentials(username, password);
	        provider.setCredentials(SCOPE, credentials);
	        
	        final HttpResponse resp = mHttpClient.execute(post);
	        response = EntityUtils.toString(resp.getEntity());
    	}
		
        
        



        
        Log.d(TAG, response);

        if (response.contains("<result code=\"done\" />")) {

            Log.d("success", "success");
         
        } else {

        	if(response.contains("<result code=\"something went wrong\" />")){
                Log.e(TAG, "Server error in adding bookmark");
                throw new IOException();
            } else{
            	Log.e(TAG, "Unknown error in adding bookmark");
            	throw new Exception();
            }
        }
        return true;
    }

}
