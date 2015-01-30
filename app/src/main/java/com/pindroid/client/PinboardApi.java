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

import android.net.Uri;
import android.util.Log;

import com.pindroid.xml.SaxTokenParser;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

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
}