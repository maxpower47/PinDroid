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

public class PinboardApi {
	
    private static final String TAG = "PinboardApi";

    public static final String AUTH_TOKEN_URI = "v1/user/api_token";
  
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