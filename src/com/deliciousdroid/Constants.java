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

package com.deliciousdroid;

import android.net.Uri;

public class Constants {

    /**
     * Account type string.
     */
    public static final String ACCOUNT_TYPE = "com.deliciousdroid";
    
    public static final Uri CONTENT_URI_BASE = Uri.parse("content://com.deliciousdroid");
    
    public static final String CONTENT_SCHEME = "content";

    /**
     * Authtoken type string.
     */
    public static final String AUTHTOKEN_TYPE = "com.deliciousdroid";
    
    public static final String AUTH_PREFS_NAME = "com.deliciousdroid.auth";
    
    public static final String PREFS_LAST_SYNC = "last_sync";
    
    public static final String PREFS_AUTH_TYPE = "authentication_type";
    public static final String AUTH_TYPE_OAUTH = "oauth";
    public static final String AUTH_TYPE_DELICIOUS = "delicious";
    
    public static final String PREFS_INITIAL_SYNC = "initial_sync";
    
    public static final String OAUTH_CALLBACK_PROPERTY = "oauth_callback";
    public static final String OAUTH_CALLBACK = "http://deliciousdroid.com/oauth.htm";
    
    public static final String OAUTH_SESSION_HANDLE_PROPERTY = "oauth_session_handle";
    
    public static final String OAUTH_LANG_PREF_PROPERTY = "xoauth_lang_pref";
    public static final String OAUTH_LANG_PREF = "en-us";
    
    public static final String OAUTH_EXPIRES_IN_PROPERTY = "oauth_expires_in";
    
    public static final String OAUTH_REQUEST_AUTH_URL_PROPERTY = "xoauth_request_auth_url";
    
    public static final String OAUTH_CALLBACK_CONFIRMED_PROPERTY = "oauth_callback_confirmed";
    
    public static final String OAUTH_VERIFIER_PROPERTY = "oauth_verifier";
    
    public static final String OAUTH_YAHOO_GUID_PROPERTY = "xoauth_yahoo_guid";
    
    public static final String OAUTH_AUTHORIZATION_EXPIRES_IN_PROPERTY = "oauth_authorization_expires_in";
    
    public static final String OAUTH_VERSION_PROPERTY = "oauth_version";
    public static final String OAUTH_VERSION = "1.0";
    
    public static final String OAUTH_TOKEN_PROPERTY = "oauth_token";
    
    public static final String OAUTH_TOKEN_SECRET_PROPERTY = "oauth_token_secret";
    
    public static final String OAUTH_TIMESTAMP_PROPERTY = "oauth_timestamp";
    
    public static final String OAUTH_NONCE_PROPERTY = "oauth_nonce";
    
    public static final String OAUTH_SIGNATURE_PROPERTY = "oauth_signature";
    
    public static final String OAUTH_SIGNATURE_METHOD_PROPERTY = "oauth_signature_method";
    public static final String OAUTH_SIGNATURE_METHOD_PLAINTEXT = "PLAINTEXT";
    public static final String OAUTH_SIGNATURE_METHOD_HMAC = "HMAC-SHA1";
    
    public static final String OAUTH_COMSUMER_KEY_PROPERTY = "oauth_consumer_key";    
    public static final String OAUTH_CONSUMER_KEY = "dj0yJmk9Mmsxd2c3b3V4UEFEJmQ9WVdrOVJFWlNSbEZYTkdFbWNHbzlNQS0tJnM9Y29uc3VtZXJzZWNyZXQmeD01Mg--";
    
    public static final String OAUTH_SHARED_SECRET = "1985c8fea4058d3853256664cec83ed2a47143e2";
    public static final String OAUTH_APPLICATION_ID = "DFRFQW4a";
}