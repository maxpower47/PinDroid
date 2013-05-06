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

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.methods.HttpGet;

import com.pindroid.xml.SaxFeedParser;

import android.database.Cursor;
import android.util.Log;

public class PinboardFeed {
    private static final String TAG = "PinboardFeed";

    public static final String FETCH_RECENT_URI = "http://feeds.pinboard.in/rss/recent/";
    public static final String FETCH_POPULAR_URI = "http://feeds.pinboard.in/rss/popular/";
    public static final String FETCH_RECENT_USER_URI = "http://feeds.pinboard.in/rss";
    public static final String FETCH_NETWORK_URI = "http://feeds.pinboard.in/rss/";
    
    /**
     * Retrieves a list of recent bookmarks for Pinboard.
     * 
     * @return The list of bookmarks received from the server.
     * @throws JSONException If an error was encountered in deserializing the JSON object returned from 
     * the server.
     * @throws IOException If a server error was encountered.
     * @throws AuthenticationException If an authentication error was encountered.
     */
    public static Cursor fetchRecent()
    	throws IOException, ParseException {

        final HttpGet post = new HttpGet(FETCH_RECENT_URI);
        
        Cursor bookmarkList = null;

        final HttpResponse resp = HttpClientFactory.getThreadSafeClient().execute(post);
        InputStream responseStream = resp.getEntity().getContent();

        if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
        	SaxFeedParser parser = new SaxFeedParser(responseStream);

			bookmarkList = parser.parse();

        } else {
        	Log.e(TAG, "Server error in fetching network recent list");
            throw new IOException();
        }

        return bookmarkList;
    }
    
    /**
     * Retrieves a list of popular bookmarks for Pinboard.
     * 
     * @return The list of bookmarks received from the server.
     * @throws JSONException If an error was encountered in deserializing the JSON object returned from 
     * the server.
     * @throws IOException If a server error was encountered.
     * @throws AuthenticationException If an authentication error was encountered.
     */
    public static Cursor fetchPopular()
    	throws IOException, ParseException {

        final HttpGet post = new HttpGet(FETCH_POPULAR_URI);
        
        Cursor bookmarkList = null;

        final HttpResponse resp = HttpClientFactory.getThreadSafeClient().execute(post);
        InputStream responseStream = resp.getEntity().getContent();

        if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
        	SaxFeedParser parser = new SaxFeedParser(responseStream);

			bookmarkList = parser.parse();

        } else {
        	Log.e(TAG, "Server error in fetching network popular list");
            throw new IOException();
        }

        return bookmarkList;
    }
    
    /**
     * Retrieves a list of recent bookmarks for a Pinboard user.
     * 
     * @return The list of bookmarks received from the server.
     * @throws JSONException If an error was encountered in deserializing the JSON object returned from 
     * the server.
     * @throws IOException If a server error was encountered.
     * @throws AuthenticationException If an authentication error was encountered.
     */
    public static Cursor fetchUserRecent(String username, String tagname)
    	throws IOException, ParseException {
    	
    	String url = FETCH_RECENT_USER_URI;
    	
    	if(username != null && username != ""){
    		url += "/u:" + username;
    	}
    	if(tagname != null && tagname != "") {
    		for(String s : tagname.split(" ")) {
    			url += "/t:" + s;
    		}	
    	}

        final HttpGet post = new HttpGet(url.trim());
        
        Cursor bookmarkList = null;

        final HttpResponse resp = HttpClientFactory.getThreadSafeClient().execute(post);
        InputStream responseStream = resp.getEntity().getContent();

        if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
        	SaxFeedParser parser = new SaxFeedParser(responseStream);

			bookmarkList = parser.parse();

        } else {
        	Log.e(TAG, "Server error in fetching network recent list");
            throw new IOException();
        }

        return bookmarkList;
    }
    
    /**
     * Retrieves a list of recent bookmarks for a Pinboard users network.
     * 
     * @return The list of bookmarks received from the server.
     * @throws IOException If a server error was encountered.
     * @throws AuthenticationException If an authentication error was encountered.
     */
    public static Cursor fetchNetworkRecent(String username, String secretToken)
    	throws IOException, ParseException {
    	
    	String url = FETCH_RECENT_USER_URI;
    	
    	if(secretToken != null && secretToken != ""){
    		url += "/secret:" + secretToken;
    	}
    	
    	if(username != null && username != ""){
    		url += "/u:" + username;
    	}
    	
    	url += "/network/";
    	
    	Log.d("network", url);

        final HttpGet post = new HttpGet(url);
        
        Cursor bookmarkList = null;

        final HttpResponse resp = HttpClientFactory.getThreadSafeClient().execute(post);
        InputStream responseStream = resp.getEntity().getContent();

        if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
        	SaxFeedParser parser = new SaxFeedParser(responseStream);

			bookmarkList = parser.parse();

        } else {
        	Log.e(TAG, "Server error in fetching network recent list");
            throw new IOException();
        }

        return bookmarkList;
    }
}