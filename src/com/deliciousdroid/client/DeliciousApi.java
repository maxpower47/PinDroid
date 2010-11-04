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

package com.deliciousdroid.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.deliciousdroid.Constants;
import com.deliciousdroid.authenticator.AuthToken;
import com.deliciousdroid.authenticator.OauthUtilities;
import com.deliciousdroid.providers.BookmarkContent.Bookmark;
import com.deliciousdroid.providers.TagContent.Tag;

public class DeliciousApi {
	
    private static final String TAG = "DeliciousApi";

    public static final String USER_AGENT = "AuthenticationService/1.0";
    public static final int REGISTRATION_TIMEOUT = 30 * 1000; // ms

    public static final String FETCH_TAGS_URI = "tags/get";
    public static final String FETCH_BOOKMARKS_URI = "posts/all";
    public static final String FETCH_CHANGED_BOOKMARKS_URI = "posts/all";
    public static final String FETCH_BOOKMARK_URI = "posts/get";
    public static final String LAST_UPDATE_URI = "posts/update";
    public static final String DELETE_BOOKMARK_URI = "posts/delete";
    public static final String ADD_BOOKMARKS_URI = "posts/add";
    private static DefaultHttpClient mHttpClient;
    
    private static final String SCHEME = "https";
    private static final String SCHEME_HTTP = "http";
    private static final String DELICIOUS_AUTHORITY = "api.del.icio.us";
    private static final int PORT = 443;
 
    private static final AuthScope SCOPE = new AuthScope(DELICIOUS_AUTHORITY, PORT);
        
    /**
     * Configures the httpClient to connect to the URL provided.
     */
    public static void maybeCreateHttpClient() {
        if (mHttpClient == null) {
            mHttpClient = new DefaultHttpClient();
            final HttpParams params = mHttpClient.getParams();
            HttpConnectionParams.setConnectionTimeout(params, REGISTRATION_TIMEOUT);
            HttpConnectionParams.setSoTimeout(params, REGISTRATION_TIMEOUT);
            ConnManagerParams.setTimeout(params, REGISTRATION_TIMEOUT);
        }
    }
    
    /**
     * Fetches users bookmarks
     * 
     * @param account The account being synced.
     * @param authtoken The authtoken stored in the AccountManager for the
     *        account
     * @return list The list of bookmarks received from the server.
     * @throws AuthenticationException 
     */
    public static Update lastUpdate(Account account, Context context)
    	throws IOException, AuthenticationException {

    	String response = null;
    	TreeMap<String, String> params = new TreeMap<String, String>();
    	Update update = null;
    	String url = LAST_UPDATE_URI;
    	
    	response = DeliciousApiCall(url, params, account, context);
    	
        if (response.contains("<?xml")) {
        	update = Update.valueOf(response);
        } else {
            Log.e(TAG, "Server error in fetching bookmark list");
            throw new IOException();
        }
        return update;
    }
    
    public static Boolean addBookmark(Bookmark bookmark, Account account, Context context) 
    	throws Exception {

    	TreeMap<String, String> params = new TreeMap<String, String>();
    	  	
		params.put("description", bookmark.getDescription());
		params.put("extended", bookmark.getNotes());
		params.put("tags", bookmark.getTags());
		params.put("url", bookmark.getUrl());
		
		if(bookmark.getPrivate()){
			params.put("shared", "no");
		}
		
		String url = ADD_BOOKMARKS_URI;
		String response = null;

    	response = DeliciousApiCall(url, params, account, context);

        if (response.contains("<result code=\"done\" />")) {
            return true;
        } else {
        	if(response.contains("<result code=\"something went wrong\" />")){
                Log.e(TAG, "Server error in adding bookmark");
                throw new IOException();
            } else if(response.contains("token_expired")){
            	Log.d(TAG, "Token Expired");
            	throw new TokenRejectedException();
            } else{
            	Log.e(TAG, "Unknown error in adding bookmark");
            	throw new Exception();
            }
        }
    }
    
    /**
     * Delete a Bookmark
     * 
     * @param account The account being synced.
     * @param authtoken The authtoken stored in the AccountManager for the
     *        account
     * @return list The list of bookmarks received from the server.
     * @throws AuthenticationException 
     */
    public static Boolean deleteBookmark(Bookmark bookmark, Account account, Context context) 
    	throws IOException, AuthenticationException {

    	TreeMap<String, String> params = new TreeMap<String, String>();
    	String response = null;
    	String url = DELETE_BOOKMARK_URI;

    	params.put("url", bookmark.getUrl());

    	response = DeliciousApiCall(url, params, account, context);
    	
        if (response.contains("<result code=\"done\"")) {
            return true;
        } else {
            Log.e(TAG, "Server error in fetching bookmark list");
            throw new IOException();
        }
    }
    
    /**
     * Fetches users bookmarks
     * 
     * @param account The account being synced.
     * @param authtoken The authtoken stored in the AccountManager for the
     *        account
     * @return list The list of bookmarks received from the server.
     * @throws AuthenticationException 
     */
    public static ArrayList<Bookmark> getBookmark(ArrayList<String> hashes, Account account,
        Context context) throws IOException, AuthenticationException {

    	ArrayList<Bookmark> bookmarkList = new ArrayList<Bookmark>();
    	TreeMap<String, String> params = new TreeMap<String, String>();
    	String hashString = "";
    	String response = null;
    	String url = FETCH_BOOKMARK_URI;

    	for(String h : hashes){
    		if(hashes.get(0) != h){
    			hashString += "+";
    		}
    		hashString += h;
    	}
    	params.put("meta", "yes");
    	params.put("hashes", hashString);

    	response = DeliciousApiCall(url, params, account, context);
    	
        if (response.contains("<?xml")) {
            bookmarkList = Bookmark.valueOf(response);
        } else {
            Log.e(TAG, "Server error in fetching bookmark list");
            throw new IOException();
        }
        return bookmarkList;
    }
    
    /**
     * Fetches users bookmarks
     * 
     * @param account The account being synced.
     * @param authtoken The authtoken stored in the AccountManager for the
     *        account
     * @return list The list of bookmarks received from the server.
     * @throws AuthenticationException 
     */
    public static ArrayList<Bookmark> getAllBookmarks(String tagName, Account account, Context context) 
    	throws IOException, AuthenticationException {
    	
    	ArrayList<Bookmark> bookmarkList = new ArrayList<Bookmark>();
    	String response = null;
    	TreeMap<String, String> params = new TreeMap<String, String>();
    	String url = FETCH_BOOKMARKS_URI;

    	if(tagName != null && tagName != ""){
    		params.put("tag", tagName);
    	}
    	
    	params.put("meta", "yes");

    	response = DeliciousApiCall(url, params, account, context);
    	
        if (response.contains("<?xml")) {

        	bookmarkList = Bookmark.valueOf(response);
         
        } else {
            Log.e(TAG, "Server error in fetching bookmark list");
            throw new IOException();
        }
        return bookmarkList;
    }
    
    /**
     * Fetches users bookmarks
     * 
     * @param account The account being synced.
     * @param authtoken The authtoken stored in the AccountManager for the
     *        account
     * @return list The list of bookmarks received from the server.
     * @throws AuthenticationException 
     */
    public static ArrayList<Bookmark> getChangedBookmarks(Account account, Context context) 
    	throws IOException, AuthenticationException {
    	
    	ArrayList<Bookmark> bookmarkList = new ArrayList<Bookmark>();
    	String response = null;
    	TreeMap<String, String> params = new TreeMap<String, String>();
    	String url = FETCH_CHANGED_BOOKMARKS_URI;

    	params.put("hashes", "yes");

    	response = DeliciousApiCall(url, params, account, context);

        if (response.contains("<?xml")) {

        	bookmarkList = Bookmark.valueOf(response);
         
        } else {
            Log.e(TAG, "Server error in fetching bookmark list");
            throw new IOException();
        }
        return bookmarkList;
    }
    
    /**
     * Fetches status messages for the user's friends from the server
     * 
     * @param account The account being synced.
     * @param authtoken The authtoken stored in the AccountManager for the
     *        account
     * @return list The list of status messages received from the server.
     * @throws AuthenticationException 
     */
    public static ArrayList<Tag> getTags(Account account, Context context) 
    	throws IOException, AuthenticationException {
    	
    	ArrayList<Tag> tagList = new ArrayList<Tag>();
    	String response = null;
    	TreeMap<String, String> params = new TreeMap<String, String>();
    	String url = FETCH_TAGS_URI;
    	  	
    	response = DeliciousApiCall(url, params, account, context);
    	Log.d("loadTagResponse", response);
    	
        if (response.contains("<?xml")) {
        	tagList = Tag.valueOf(response);
        } else {
            Log.e(TAG, "Server error in fetching bookmark list");
            throw new IOException();
        }
        return tagList;
    }
    
    private static String DeliciousApiCall(String url, TreeMap<String, String> params, 
    		Account account, Context context) throws IOException, AuthenticationException{

    	final AccountManager am = AccountManager.get(context);
    	String authtype = am.getUserData(account, Constants.PREFS_AUTH_TYPE);
    	
    	String username = account.name;
    	String authtoken = null;
    	String path = null;
    	String scheme = null;
    	
    	AuthToken at = new AuthToken(context, account);
    	authtoken = at.getAuthToken();
    	
    	if(authtype.equals(Constants.AUTH_TYPE_OAUTH)) {
    		path = "v2/" + url;
    		scheme = SCHEME_HTTP;
    	} else {
    		path = "v1/" + url;
    		scheme = SCHEME;
    	}
    	
    	HttpResponse resp = null;
    	HttpGet post = null;
    	
		Uri.Builder builder = new Uri.Builder();
		builder.scheme(scheme);
		builder.authority(DELICIOUS_AUTHORITY);
		builder.appendEncodedPath(path);
		for(String key : params.keySet()){
			builder.appendQueryParameter(key, params.get(key));
		}
		
		Log.d("apiCallUrl", builder.build().toString().replace("%3A", ":").replace("%2F", "/").replace("%2B", "+"));
		post = new HttpGet(builder.build().toString().replace("%3A", ":").replace("%2F", "/").replace("%2B", "+"));
		HttpHost host = new HttpHost(DELICIOUS_AUTHORITY);
		maybeCreateHttpClient();
		post.setHeader("User-Agent", "DeliciousDroid");
    	

    	if(authtype.equals(Constants.AUTH_TYPE_OAUTH)) {
    		Log.d("apiCall", "oauth");
    		String tokenSecret = am.getUserData(account, Constants.OAUTH_TOKEN_SECRET_PROPERTY);

			OauthUtilities.signRequest(post, params, authtoken, tokenSecret);

			Log.d("header", post.getHeaders("Authorization")[0].getValue());
	        
	        resp = mHttpClient.execute(host, post);

    	} else{ 
	        CredentialsProvider provider = mHttpClient.getCredentialsProvider();
	        Credentials credentials = new UsernamePasswordCredentials(username, authtoken);
	        provider.setCredentials(SCOPE, credentials);
	        
	        resp = mHttpClient.execute(post);
    	}
    	if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
    		return EntityUtils.toString(resp.getEntity());
    	} else if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
    		throw new AuthenticationException();
    	} else {
    		throw new IOException();
    	}

    }
}