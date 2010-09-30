package com.android.droidlicious.client;

public class LoginResult {

	private final Boolean result;
	private final String oauth_token_secret;
	private final String oauth_expires_in;
	private final String xoauth_request_auth_url;
	private final String oauth_token;
	private final String oauth_callback_confirmed;
	private final String oauth_access_token;
	private final String oauth_session_handle;
	private final String oauth_authorization_expires_in;
	private final String xoauth_yahoo_guid;
	
	public Boolean getResult(){
		return result;
	}
	
	public String getTokenSecret(){
		return oauth_token_secret;
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
		oauth_token_secret = "";
		oauth_expires_in = "";
		xoauth_request_auth_url = "";
		oauth_token = "";
		oauth_callback_confirmed = "";
		oauth_access_token = "";
		oauth_session_handle = "";
		oauth_authorization_expires_in = "";
		xoauth_yahoo_guid = "";
	}
	
	LoginResult(Boolean r, String token, String tokenSecret){
		result = r;
		oauth_token_secret = tokenSecret;
		oauth_expires_in = "";
		xoauth_request_auth_url = "";
		oauth_token = token;
		oauth_callback_confirmed = "";
		oauth_access_token = "";
		oauth_session_handle = "";
		oauth_authorization_expires_in = "";
		xoauth_yahoo_guid = "";
	}
	
	LoginResult(Boolean r, String accessToken, String tokenSecret, String session, String expiration, 
			String sessionExpires, String uid){
		result = r;
		oauth_access_token = accessToken;
		oauth_token_secret = tokenSecret;
		oauth_expires_in = expiration;
		xoauth_request_auth_url = "";
		oauth_token = "";
		oauth_callback_confirmed = "";
		oauth_session_handle = session;
		oauth_authorization_expires_in = sessionExpires;
		xoauth_yahoo_guid = uid;
	}
	
	LoginResult(Boolean r, String ots, String oei, String orau, String ot, String occ){
		result = r;
		oauth_token_secret = ots;
		oauth_expires_in = oei;
		xoauth_request_auth_url = orau;
		oauth_token = ot;
		oauth_callback_confirmed = occ;
		oauth_access_token = "";
		oauth_session_handle = "";
		oauth_authorization_expires_in = "";
		xoauth_yahoo_guid = "";
	}
	
	
}
