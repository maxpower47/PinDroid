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

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.pindroid.Constants;
import com.pindroid.providers.BookmarkContent.Bookmark;
import com.pindroid.providers.NoteContent.Note;
import com.pindroid.providers.TagContent.Tag;
import com.pindroid.xml.SaxBookmarkParser;
import com.pindroid.xml.SaxNoteListParser;
import com.pindroid.xml.SaxNoteParser;
import com.pindroid.xml.SaxResultParser;
import com.pindroid.xml.SaxTagParser;
import com.pindroid.xml.SaxTokenParser;
import com.pindroid.xml.SaxUpdateParser;

public class PinboardApi {
	
    private static final String TAG = "PinboardApi";

    public static final String AUTH_TOKEN_URI = "v1/user/api_token";
    public static final String FETCH_TAGS_URI = "v1/tags/get";
    public static final String FETCH_SUGGESTED_TAGS_URI = "v1/posts/suggest";
    public static final String FETCH_BOOKMARKS_URI = "v1/posts/all";
    public static final String FETCH_CHANGED_BOOKMARKS_URI = "v1/posts/all";
    public static final String FETCH_BOOKMARK_URI = "v1/posts/get";
    public static final String LAST_UPDATE_URI = "v1/posts/update";
    public static final String DELETE_BOOKMARK_URI = "v1/posts/delete";
    public static final String ADD_BOOKMARKS_URI = "v1/posts/add";
    public static final String FETCH_SECRET_URI = "v1/user/secret";
    public static final String FETCH_NOTE_LIST_URI = "v1/notes/list";
    public static final String FETCH_NOTE_DETAILS_URI = "v1/notes/";
  
    private static final String SCHEME = "https";
    private static final String PINBOARD_AUTHORITY = "api.pinboard.in";
    private static final int PORT = 443;
    
    private static final AuthScope SCOPE = new AuthScope(PINBOARD_AUTHORITY, PORT);

    /**
     * Attempts to authenticate to Pinboard using a legacy Pinboard account.
     * 
     * @param username The user's username.
     * @param password The user's password.
     * @param handler The hander instance from the calling UI thread.
     * @param context The context of the calling Activity.
     * @return The boolean result indicating whether the user was
     *         successfully authenticated.
     * @throws  
     */
    public static String pinboardAuthenticate(String username, String password) {
        final HttpResponse resp;
        
        Uri.Builder builder = new Uri.Builder();
        builder.scheme(SCHEME);
        builder.authority(PINBOARD_AUTHORITY);
        builder.appendEncodedPath(AUTH_TOKEN_URI);
        Uri uri = builder.build();

        HttpGet request = new HttpGet(String.valueOf(uri));

        DefaultHttpClient client = (DefaultHttpClient)HttpClientFactory.getThreadSafeClient();
        
        CredentialsProvider provider = client.getCredentialsProvider();
        Credentials credentials = new UsernamePasswordCredentials(username, password);
        provider.setCredentials(SCOPE, credentials);

        try {
            resp = client.execute(request);
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            	
        		final HttpEntity entity = resp.getEntity();
        		
        		InputStream instream = entity.getContent();		
            	SaxTokenParser parser = new SaxTokenParser(instream);
            	PinboardAuthToken token = parser.parse();
            	instream.close();

                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, "Successful authentication");
                    Log.v(TAG, "AuthToken: " + token.getToken());
                }
                
                return token.getToken();
            } else {
                if (Log.isLoggable(TAG, Log.VERBOSE)) {
                    Log.v(TAG, "Error authenticating" + resp.getStatusLine());
                }
                return null;
            }
        } catch (final IOException e) {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "IOException when getting authtoken", e);
            }
            return null;
        } catch (ParseException e) {
        	if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "ParseException when getting authtoken", e);
            }
            return null;
		} finally {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "getAuthtoken completing");
            }
        }
    }

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
     * @throws ParseException 
     * @throws PinboardException 
     */
    public static Update lastUpdate(Account account, Context context)
    	throws IOException, AuthenticationException, TooManyRequestsException, ParseException, PinboardException {

    	InputStream responseStream = null;
    	TreeMap<String, String> params = new TreeMap<String, String>();
    	
    	responseStream = PinboardApiCall(LAST_UPDATE_URI, params, account, context);
    	SaxUpdateParser parser = new SaxUpdateParser(responseStream);
    	Update update = parser.parse();
    	responseStream.close();

        return update;
    }
    
    /**
     * Sends a request to Pinboard's Add Bookmark api.
     * 
     * @param bookmark The bookmark to be added.
     * @param account The account being synced.
     * @param context The current application context.
     * @return A boolean indicating whether or not the api call was successful.
     * @throws IOException If an IO error was encountered.
     * @throws TooManyRequestsException 
     * @throws AuthenticationException If an authentication error was encountered.
     * @throws PinboardException If a server error is encountered.
     * @throws ParseException 
     * @throws Exception If an unknown error is encountered.
     */
    public static Boolean addBookmark(Bookmark bookmark, Account account, Context context) 
    	throws IOException, AuthenticationException, TooManyRequestsException, PinboardException, ParseException {

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
		InputStream responseStream = null;

    	responseStream = PinboardApiCall(uri, params, account, context);
    	
    	SaxResultParser parser = new SaxResultParser(responseStream);
    	PinboardApiResult result = parser.parse();
    	responseStream.close();

        if (result.getCode().equalsIgnoreCase("done")) {
            return true;
        } else if (result.getCode().equalsIgnoreCase("something went wrong")) {
        	Log.e(TAG, "Pinboard server error in adding bookmark");
        	throw new PinboardException();
        } else {
        	Log.e(TAG, "IO error in adding bookmark");
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
     * @throws ParseException 
     * @throws PinboardException 
     */
    public static Boolean deleteBookmark(Bookmark bookmark, Account account, Context context) 
    	throws IOException, AuthenticationException, TooManyRequestsException, ParseException, PinboardException {

    	TreeMap<String, String> params = new TreeMap<String, String>();
    	InputStream responseStream = null;
    	String url = DELETE_BOOKMARK_URI;

    	params.put("url", bookmark.getUrl());

    	responseStream = PinboardApiCall(url, params, account, context);

    	SaxResultParser parser = new SaxResultParser(responseStream);
    	PinboardApiResult result = parser.parse();
    	responseStream.close();
    	
        if (result.getCode().equalsIgnoreCase("done") || result.getCode().equalsIgnoreCase("item not found")) {
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
     * @throws PinboardException 
     */
    public static ArrayList<Bookmark> getBookmark(ArrayList<String> hashes, Account account,
        Context context) throws IOException, AuthenticationException, TooManyRequestsException, PinboardException {

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
     * @throws PinboardException 
     */
    public static ArrayList<Bookmark> getAllBookmarks(String tagName, Account account, Context context) 
    	throws IOException, AuthenticationException, TooManyRequestsException, PinboardException {

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
     * @throws PinboardException 
     */
    public static ArrayList<Bookmark> getAllBookmarks(String tagName, int start, int count, Account account, Context context) 
	throws IOException, AuthenticationException, TooManyRequestsException, PinboardException {
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
     * @throws PinboardException 
     */
    public static ArrayList<Tag> getSuggestedTags(String suggestUrl, Account account, Context context) 
    	throws IOException, AuthenticationException, TooManyRequestsException, PinboardException {
    	
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
     * @throws PinboardException 
     */
    public static ArrayList<Tag> getTags(Account account, Context context) 
    	throws IOException, AuthenticationException, TooManyRequestsException, PinboardException {
    	
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
     * @throws ParseException 
     * @throws PinboardException 
     */
    public static String getSecretToken(Account account, Context context) 
    	throws IOException, AuthenticationException, TooManyRequestsException, ParseException, PinboardException {

    	InputStream responseStream = null;
    	final TreeMap<String, String> params = new TreeMap<String, String>();
    	  	
    	responseStream = PinboardApiCall(FETCH_SECRET_URI, params, account, context);
    	SaxTokenParser parser = new SaxTokenParser(responseStream);
    	PinboardAuthToken token = parser.parse();
    	responseStream.close();

        return token.getToken();
    }
    
    /**
     * Retrieves a list of all notes for a user from Pinboard.
     * 
     * @param account The account being synced.
     * @param context The current application context.
     * @return A list of the users notes.
     * @throws IOException If a server error was encountered.
     * @throws AuthenticationException If an authentication error was encountered.
     * @throws TooManyRequestsException 
     * @throws PinboardException 
     */
    public static ArrayList<Note> getNoteList(Account account, Context context) 
    	throws IOException, AuthenticationException, TooManyRequestsException, PinboardException {
    	
    	ArrayList<Note> noteList = new ArrayList<Note>();

    	InputStream responseStream = null;
    	final TreeMap<String, String> params = new TreeMap<String, String>();
    	  	
    	responseStream = PinboardApiCall(FETCH_NOTE_LIST_URI, params, account, context);
    	final SaxNoteListParser parser = new SaxNoteListParser(responseStream);
    	
    	try {
			noteList = parser.parse();
		} catch (ParseException e) {
            Log.e(TAG, "Server error in fetching bookmark list");
            throw new IOException();
		}

        responseStream.close();
        return noteList;
    }
    
    /**
     * Retrieves details for a note for a user from Pinboard.
     * 
     * @param account The account being synced.
     * @param context The current application context.
     * @return A note.
     * @throws IOException If a server error was encountered.
     * @throws AuthenticationException If an authentication error was encountered.
     * @throws TooManyRequestsException 
     * @throws PinboardException 
     */
    public static Note getNote(String pid, Account account, Context context) 
    	throws IOException, AuthenticationException, TooManyRequestsException, PinboardException {
    	
    	Note note = new Note();

    	InputStream responseStream = null;
    	final TreeMap<String, String> params = new TreeMap<String, String>();
    	  	
    	responseStream = PinboardApiCall(FETCH_NOTE_DETAILS_URI + pid, params, account, context);
    	final SaxNoteParser parser = new SaxNoteParser(responseStream);
    	
    	try {
			note = parser.parse();
		} catch (ParseException e) {
            Log.e(TAG, "Server error in fetching bookmark list");
            throw new IOException();
		}

        responseStream.close();
        return note;
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
     * @throws PinboardException 
     */
    private static InputStream PinboardApiCall(String url, TreeMap<String, String> params, 
    		Account account, Context context) throws IOException, AuthenticationException, TooManyRequestsException, PinboardException{

    	final AccountManager am = AccountManager.get(context);
    	
		if(account == null)
			throw new AuthenticationException();
		
    	final String username = account.name;
    	String authtoken = "00000000000000000000";  // need to provide a sane default value, since a token that is too short causes a 500 error instead of 401
    	
    	try {
			String tempAuthtoken = am.blockingGetAuthToken(account, Constants.AUTHTOKEN_TYPE, true);
			if(tempAuthtoken != null)
				authtoken = tempAuthtoken;
		} catch (Exception e) {
			e.printStackTrace();
			throw new AuthenticationException("Error getting auth token");	
		}
    	
    	params.put("auth_token", username + ":" + authtoken);
    	
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
    		am.invalidateAuthToken(Constants.AUTHTOKEN_TYPE, authtoken);
    		
        	try {
    			authtoken = am.blockingGetAuthToken(account, Constants.AUTHTOKEN_TYPE, true);
    		} catch (Exception e) {
    			e.printStackTrace();
    			throw new AuthenticationException("Invalid auth token");
    		}
        	
    		throw new AuthenticationException();
    	} else if (statusCode == Constants.HTTP_STATUS_TOO_MANY_REQUESTS) {
    		throw new TooManyRequestsException(300);
    	} else if (statusCode == HttpStatus.SC_REQUEST_URI_TOO_LONG) {
    		throw new PinboardException();
    	} else {
    		throw new IOException();
    	}
    }
}