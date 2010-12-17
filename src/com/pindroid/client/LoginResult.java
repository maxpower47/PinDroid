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

package com.pindroid.client;

import java.net.URLDecoder;

import com.pindroid.Constants;

public class LoginResult {

	private final Boolean result;
	private String oauth_token_secret = null;
	private String oauth_expires_in = null;
	private String xoauth_request_auth_url = null;
	private String oauth_token = null;
	private String oauth_access_token = null;
	private String oauth_session_handle = null;
	private String oauth_authorization_expires_in = null;
	private String xoauth_yahoo_guid = null;
	private String oauth_callback_confirmed = null;
	
	public Boolean getResult(){
		return result;
	}
	
	public String getTokenSecret(){
		return oauth_token_secret;
	}
	
	public String getCallbackConfirmed(){
		return oauth_callback_confirmed;
	}
	
	public String getExpiration(){
		return oauth_expires_in;
	}
	
	public String getRequestUrl(){
		return xoauth_request_auth_url;
	}
	
	public String getAccessToken(){
		return oauth_access_token;
	}
	
	public String getToken(){
		return oauth_token;
	}
	
	public String getSessionHandle(){
		return oauth_session_handle;
	}
	
	public String getSessionExpiration(){
		return oauth_authorization_expires_in;
	}
	
	public String getYahooGuid(){
		return xoauth_yahoo_guid;
	}
	
	LoginResult(Boolean r){
		result = r;
	}
	
	LoginResult(Boolean r, String response){
		result = r;
		
    	String[] responseParams = response.split("&");
    	for(String s : responseParams){
    		if(s.contains(Constants.OAUTH_TOKEN_SECRET_PROPERTY + "=")){
    			oauth_token_secret = s.split("=")[1];
    		}
    		if(s.contains(Constants.OAUTH_EXPIRES_IN_PROPERTY + "=")){
    			oauth_expires_in = s.split("=")[1];
    		}
    		if(s.contains(Constants.OAUTH_REQUEST_AUTH_URL_PROPERTY + "=")){
    			xoauth_request_auth_url = URLDecoder.decode(s.split("=")[1]);
    		}
    		if(s.contains(Constants.OAUTH_TOKEN_PROPERTY + "=")){
    			oauth_token = s.split("=")[1];
    		}
    		if(s.contains(Constants.OAUTH_CALLBACK_CONFIRMED_PROPERTY + "=")){
    			oauth_callback_confirmed = s.split("=")[1];
    		}
    		if(s.contains(Constants.OAUTH_YAHOO_GUID_PROPERTY + "=")){
    			xoauth_yahoo_guid = s.split("=")[1];
    		}
    		if(s.contains(Constants.OAUTH_AUTHORIZATION_EXPIRES_IN_PROPERTY + "=")){
    			oauth_authorization_expires_in = s.split("=")[1];
    		}
    		if(s.contains(Constants.OAUTH_SESSION_HANDLE_PROPERTY + "=")){
    			oauth_session_handle = s.split("=")[1];
    		}
    	}
	}
	
	
}
