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

package com.pindroid.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.pindroid.Constants;
import com.pindroid.providers.BookmarkContent.Bookmark;
import com.pindroid.providers.TagContent.Tag;
import com.pindroid.xml.SaxBookmarkParser;
import com.pindroid.xml.SaxTagParser;

public class PinboardApi {
	
    private static final String TAG = "PinboardApi";

    public static final String FETCH_TAGS_URI = "v1/tags/get";
    public static final String FETCH_SUGGESTED_TAGS_URI = "v1/posts/suggest";
    public static final String FETCH_BOOKMARKS_URI = "v1/posts/all";
    public static final String FETCH_CHANGED_BOOKMARKS_URI = "v1/posts/all";
    public static final String FETCH_BOOKMARK_URI = "v1/posts/get";
    public static final String LAST_UPDATE_URI = "v1/posts/update";
    public static final String DELETE_BOOKMARK_URI = "v1/posts/delete";
    public static final String ADD_BOOKMARKS_URI = "v1/posts/add";
    public static final String FETCH_SECRET_URI = "v1/user/secret";
  
    private static final String SCHEME = "https";
    private static final String PINBOARD_AUTHORITY = "api.pinboard.in";
    private static final int PORT = 443;
 
    private static final AuthScope SCOPE = new AuthScope(PINBOARD_AUTHORITY, PORT);

    /**
     * Gets timestamp of last update to data on Pinboard servers.
     * 
     * @param account The account being synced.
     * @param context The current application context.
     * @return An Update object containing the timestamp and the number of new bookmarks in the
     * inbox.
     * @throws IOException If a server error was encountered.
     * @throws AuthenticationException If an authentication error was encountered.
     * @throws TooManyRequestsException 
     */
    public static Update lastUpdate(Account account, Context context)
    	throws IOException, AuthenticationException, TooManyRequestsException {

    	String response = null;
    	InputStream responseStream = null;
    	TreeMap<String, String> params = new TreeMap<String, String>();
    	Update update = null;
    	
    	responseStream = PinboardApiCall(LAST_UPDATE_URI, params, account, context);
    	response = convertStreamToString(responseStream);
    	responseStream.close();
    	
    	try{
	        if (response.contains("<?xml")) {
	        	update = Update.valueOf(response);
	        } else {
	            Log.e(TAG, "Server error in fetching bookmark list");
	            throw new IOException();
	        }
    	}
    	catch(Exception e) {
    		Log.e(TAG, "Server error in fetching bookmark list");
    		throw new IOException();
    	}
        return update;
    }
    
    /**
     * Sends a request to Pinboard's Add Bookmark api.
     * 
     * @param bookmark The bookmark to be added.
     * @param account The account being synced.
     * @param context The current application context.
     * @return A boolean indicating whether or not the api call was successful.
     * @throws IOException If a server error was encountered.
     * @throws TooManyRequestsException 
     * @throws AuthenticationException If an authentication error was encountered.
     * @throws Exception If an unknown error is encountered.
     */
    public static Boolean addBookmark(Bookmark bookmark, Account account, Context context) 
    	throws IOException, AuthenticationException, TooManyRequestsException {

    	String url = bookmark.getUrl();
    	if(url.endsWith("/")) {
    		url = url.substring(0, url.lastIndexOf('/'));
    	}
    	
    	TreeMap<String, String> params = new TreeMap<String, String>();
    	  	
		params.put("description", bookmark.getDescription());
		params.put("extended", bookmark.getNotes());
		params.put("tags", bookmark.getTagString());
		params.put("url", bookmark.getUrl());
		
		if(bookmark.getShared()){
			params.put("shared", "yes");
		} else params.put("shared", "no");
		
		if(bookmark.getToRead()){
			params.put("toread", "yes");
		}
		
		String uri = ADD_BOOKMARKS_URI;
		String response = null;
		InputStream responseStream = null;

    	responseStream = PinboardApiCall(uri, params, account, context);
    	response = convertStreamToString(responseStream);
    	responseStream.close();

        if (response.contains("<result code=\"done\" />")) {
            return true;
        } else {
        	Log.e(TAG, "Server error in adding bookmark");
            throw new IOException();
        }
    }
    
    /**
     * Sends a request to Pinboard's Delete Bookmark api.
     * 
     * @param bookmark The bookmark to be deleted.
     * @param account The account being synced.
     * @param context The current application context.
     * @return A boolean indicating whether or not the api call was successful.
     * @throws IOException If a server error was encountered.
     * @throws AuthenticationException If an authentication error was encountered.
     * @throws TooManyRequestsException 
     */
    public static Boolean deleteBookmark(Bookmark bookmark, Account account, Context context) 
    	throws IOException, AuthenticationException, TooManyRequestsException {

    	TreeMap<String, String> params = new TreeMap<String, String>();
    	String response = null;
    	InputStream responseStream = null;
    	String url = DELETE_BOOKMARK_URI;

    	params.put("url", bookmark.getUrl());

    	responseStream = PinboardApiCall(url, params, account, context);
    	response = convertStreamToString(responseStream);
    	responseStream.close();
    	
        if (response.contains("<result code=\"done\"")) {
            return true;
        } else {
            Log.e(TAG, "Server error in fetching bookmark list");
            throw new IOException();
        }
    }
    
    /**
     * Retrieves a specific list of bookmarks from Pinboard.
     * 
     * @param hashes A list of bookmark hashes to be retrieved.  
     * 	The hashes are MD5 hashes of the URL of the bookmark.
     * 
     * @param account The account being synced.
     * @param context The current application context.
     * @return A list of bookmarks received from the server.
     * @throws IOException If a server error was encountered.
     * @throws AuthenticationException If an authentication error was encountered.
     * @throws TooManyRequestsException 
     */
    public static ArrayList<Bookmark> getBookmark(ArrayList<String> hashes, Account account,
        Context context) throws IOException, AuthenticationException, TooManyRequestsException {

    	ArrayList<Bookmark> bookmarkList = new ArrayList<Bookmark>();
    	TreeMap<String, String> params = new TreeMap<String, String>();
    	String hashString = "";
    	InputStream responseStream = null;
    	String url = FETCH_BOOKMARK_URI;

    	for(String h : hashes){
    		if(hashes.get(0) != h){
    			hashString += "+";
    		}
    		hashString += h;
    	}
    	params.put("meta", "yes");
    	params.put("hashes", hashString);

    	responseStream = PinboardApiCall(url, params, account, context);
    	SaxBookmarkParser parser = new SaxBookmarkParser(responseStream);
    	
    	try {
			bookmarkList = parser.parse();
		} catch (ParseException e) {
            Log.e(TAG, "Server error in fetching bookmark list");
            throw new IOException();
		}

        responseStream.close();
        return bookmarkList;
    }
    
    /**
     * Retrieves the entire list of bookmarks for a user from Pinboard.
     * 
     * @param tagname If specified, will only retrieve bookmarks with a specific tag.
     * @param account The account being synced.
     * @param context The current application context.
     * @return A list of bookmarks received from the server.
     * @throws IOException If a server error was encountered.
     * @throws AuthenticationException If an authentication error was encountered.
     * @throws TooManyRequestsException 
     */
    public static ArrayList<Bookmark> getAllBookmarks(String tagName, Account account, Context context) 
    	throws IOException, AuthenticationException, TooManyRequestsException {

        return getAllBookmarks(tagName, 0, 0, account, context);
    }
    
    /**
     * Retrieves the entire list of bookmarks for a user from Pinboard.
     * 
     * @param tagname If specified, will only retrieve bookmarks with a specific tag.
     * @param start Bookmark number to start from.
     * @param count Number of results to retrieve.
     * @param account The account being synced.
     * @param context The current application context.
     * @return A list of bookmarks received from the server.
     * @throws IOException If a server error was encountered.
     * @throws AuthenticationException If an authentication error was encountered.
     * @throws TooManyRequestsException 
     */
    public static ArrayList<Bookmark> getAllBookmarks(String tagName, int start, int count, Account account, Context context) 
	throws IOException, AuthenticationException, TooManyRequestsException {
    	ArrayList<Bookmark> bookmarkList = new ArrayList<Bookmark>();

    	InputStream responseStream = null;
    	TreeMap<String, String> params = new TreeMap<String, String>();
    	String url = FETCH_BOOKMARKS_URI;

    	if(tagName != null && tagName != ""){
    		params.put("tag", tagName);
    	}
    	
    	if(start != 0){
    		params.put("start", Integer.toString(start));
    	}
    	
    	if(count != 0){
    		params.put("results", Integer.toString(count));
    	}
    	
    	params.put("meta", "yes");

    	responseStream = PinboardApiCall(url, params, account, context);
    	SaxBookmarkParser parser = new SaxBookmarkParser(responseStream);
    	
    	try {
			bookmarkList = parser.parse();
		} catch (ParseException e) {
            Log.e(TAG, "Server error in fetching bookmark list");
            throw new IOException();
		}

        responseStream.close();
        
        return bookmarkList;
    }
    
    /**
     * Retrieves a list of suggested tags for a URL.
     * 
     * @param suggestUrl The URL to get suggested tags for.
     * @param account The account being synced.
     * @param context The current application context.
     * @return A list of tags suggested for the provided url.
     * @throws IOException If a server error was encountered.
     * @throws AuthenticationException If an authentication error was encountered.
     * @throws TooManyRequestsException 
     */
    public static ArrayList<Tag> getSuggestedTags(String suggestUrl, Account account, Context context) 
    	throws IOException, AuthenticationException, TooManyRequestsException {
    	
    	ArrayList<Tag> tagList = new ArrayList<Tag>();
    	
		if(!suggestUrl.startsWith("http")){
			suggestUrl = "http://" + suggestUrl;
		}

    	InputStream responseStream = null;
    	TreeMap<String, String> params = new TreeMap<String, String>();
    	params.put("url", suggestUrl);
    	
    	String url = FETCH_SUGGESTED_TAGS_URI;
    	  	
    	responseStream = PinboardApiCall(url, params, account, context);
    	SaxTagParser parser = new SaxTagParser(responseStream);
    	
    	try {
			tagList = parser.parseSuggested();
		} catch (ParseException e) {
            Log.e(TAG, "Server error in fetching bookmark list");
            throw new IOException();
		}

        responseStream.close();
        return tagList;
    }
    
    /**
     * Retrieves a list of all tags for a user from Pinboard.
     * 
     * @param account The account being synced.
     * @param context The current application context.
     * @return A list of the users tags.
     * @throws IOException If a server error was encountered.
     * @throws AuthenticationException If an authentication error was encountered.
     * @throws TooManyRequestsException 
     */
    public static ArrayList<Tag> getTags(Account account, Context context) 
    	throws IOException, AuthenticationException, TooManyRequestsException {
    	
    	ArrayList<Tag> tagList = new ArrayList<Tag>();

    	InputStream responseStream = null;
    	final TreeMap<String, String> params = new TreeMap<String, String>();
    	  	
    	responseStream = PinboardApiCall(FETCH_TAGS_URI, params, account, context);
    	final SaxTagParser parser = new SaxTagParser(responseStream);
    	
    	try {
			tagList = parser.parse();
		} catch (ParseException e) {
            Log.e(TAG, "Server error in fetching bookmark list");
            throw new IOException();
		}

        responseStream.close();
        return tagList;
    }
    
    /**
     * Gets the users secret rss token.
     * 
     * @param account The account being synced.
     * @param context The current application context.
     * @return The secret rss token.
     * @throws IOException If a server error was encountered.
     * @throws AuthenticationException If an authentication error was encountered.
     * @throws TooManyRequestsException 
     */
    public static String getSecretToken(Account account, Context context) 
    	throws IOException, AuthenticationException, TooManyRequestsException {

    	InputStream responseStream = null;
    	final TreeMap<String, String> params = new TreeMap<String, String>();
    	String response = null;
    	String token = null;
    	  	
    	responseStream = PinboardApiCall(FETCH_SECRET_URI, params, account, context);
    	response = convertStreamToString(responseStream);
    	responseStream.close();

        if (response.contains("<?xml")) {
        	
        	int start = response.indexOf("<result>");
        	int end = response.indexOf("</result>", start);
        	token = response.substring(start + 8, end);
        } else {
            Log.e(TAG, "Server error in fetching bookmark list");
            throw new IOException();
        }
        return token;
    }
    
    /**
     * Performs an api call to Pinboard's http based api methods.
     * 
     * @param url URL of the api method to call.
     * @param params Extra parameters included in the api call, as specified by different methods.
     * @param account The account being synced.
     * @param context The current application context.
     * @return A String containing the response from the server.
     * @throws IOException If a server error was encountered.
     * @throws AuthenticationException If an authentication error was encountered.
     * @throws TooManyRequestsException 
     */
    private static InputStream PinboardApiCall(String url, TreeMap<String, String> params, 
    		Account account, Context context) throws IOException, AuthenticationException, TooManyRequestsException{

    	final AccountManager am = AccountManager.get(context);
    	
		if(account == null)
			throw new AuthenticationException();
		
    	final String username = account.name;
    	String authtoken = null;
    	
    	try {
			authtoken = am.blockingGetAuthToken(account, Constants.AUTHTOKEN_TYPE, false);
		} catch (OperationCanceledException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AuthenticatorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
		final Uri.Builder builder = new Uri.Builder();
		builder.scheme(SCHEME);
		builder.authority(PINBOARD_AUTHORITY);
		builder.appendEncodedPath(url);
		for(String key : params.keySet()){
			builder.appendQueryParameter(key, params.get(key));
		}
		
		String apiCallUrl = builder.build().toString();
		
		Log.d("apiCallUrl", apiCallUrl);
		final HttpGet post = new HttpGet(apiCallUrl);

		post.setHeader("User-Agent", "PinDroid");
		post.setHeader("Accept-Encoding", "gzip");

		final DefaultHttpClient client = (DefaultHttpClient)HttpClientFactory.getThreadSafeClient();
        final CredentialsProvider provider = client.getCredentialsProvider();
        final Credentials credentials = new UsernamePasswordCredentials(username, authtoken);
        provider.setCredentials(SCOPE, credentials);
        
		// Get auth/login cookies
		final HttpPost auth = new HttpPost("http://pinboard.in/auth/");
		java.util.List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
		nameValuePairs.add(new BasicNameValuePair("username", username));
		nameValuePairs.add(new BasicNameValuePair("password", authtoken));
		nameValuePairs.add(new BasicNameValuePair("submit", "log in"));
		auth.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		client.execute(auth);
        
        final HttpResponse resp = client.execute(post);
        
        final int statusCode = resp.getStatusLine().getStatusCode();

    	if (statusCode == HttpStatus.SC_OK) {
    		
    		final HttpEntity entity = resp.getEntity();
    		
    		InputStream instream = entity.getContent();
    		
    		final Header encoding = entity.getContentEncoding();
    		
    		if(encoding != null && encoding.getValue().equalsIgnoreCase("gzip")) {
    			instream = new GZIPInputStream(instream);
    		}
    		
    		return instream;
    	} else if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
    		throw new AuthenticationException();
    	} else if (statusCode == Constants.HTTP_STATUS_TOO_MANY_REQUESTS) {
    		throw new TooManyRequestsException(300);
    	} else {
    		throw new IOException();
    	}
    }
    
    /**
     * Converts an InputStream to a string.
     * 
     * @param is The InputStream to convert.
     * @return The String retrieved from the InputStream.
     */
    private static String convertStreamToString(InputStream is) {
        /*
         * To convert the InputStream to String we use the BufferedReader.readLine()
         * method. We iterate until the BufferedReader return null which means
         * there's no more data to read. Each line will appended to a StringBuilder
         * and returned as String.
         */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
 
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}