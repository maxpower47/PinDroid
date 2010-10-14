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

package com.android.droidlicious;

import android.net.Uri;

public class Constants {

    /**
     * Account type string.
     */
    public static final String ACCOUNT_TYPE = "com.android.droidlicious";
    
    public static final Uri BOOKMARK_CONENT_URI = Uri.parse("content://com.android.droidlicious.bookmark");
    
    public static final Uri CONTENT_URI_BASE = Uri.parse("content://com.android.droidlicious.providers.BookmarkContentProvider");

    /**
     * Authtoken type string.
     */
    public static final String AUTHTOKEN_TYPE = "com.android.droidlicious";
    
    public static final String AUTH_PREFS_NAME = "com.android.droidlicious.auth";
    
    public static final String PREFS_LAST_SYNC = "last_sync";
    
    public static final String PREFS_AUTH_TYPE = "authentication_type";
    public static final String AUTH_TYPE_OAUTH = "oauth";
    public static final String AUTH_TYPE_DELICIOUS = "delicious";
    
    public static final String PREFS_INITIAL_SYNC = "initial_sync";
    
    public static final String OAUTH_CALLBACK_PROPERTY = "oauth_callback";
    public static final String OAUTH_CALLBACK = "oob";
    
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
    public static final String OAUTH_CONSUMER_KEY = "dj0yJmk9OFMyMU03NlVOYlJNJmQ9WVdrOVN6Qk5TR0ZhTjJrbWNHbzlNQS0tJnM9Y29uc3VtZXJzZWNyZXQmeD03NA--";
    
    public static final String OAUTH_SHARED_SECRET = "ba0a2f0d1ecadb6d3f79eb0e875689ea6890af27";
    public static final String OAUTH_APPLICATION_ID = "K0MHaZ7i";

}
