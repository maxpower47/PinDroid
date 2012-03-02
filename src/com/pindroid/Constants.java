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

package com.pindroid;

import android.net.Uri;

public class Constants {

    /**
     * Account type string.
     */
    public static final String ACCOUNT_TYPE = "com.pindroid";
    
    public static final Uri CONTENT_URI_BASE = Uri.parse("content://com.pindroid");
    
    public static final String CONTENT_SCHEME = "content";
    
    public static final String EXTRA_DESCRIPTION = "com.pindroid.bookmark.description";
    public static final String EXTRA_NOTES = "com.pindroid.bookmark.notes";
    public static final String EXTRA_TAGS = "com.pindroid.bookmark.tags";
    public static final String EXTRA_PRIVATE = "com.pindroid.bookmark.private";
    public static final String EXTRA_TOREAD = "com.pindroid.bookmark.toread";
    public static final String EXTRA_ERROR = "com.pindroid.bookmark.error";
    public static final String EXTRA_TIME = "com.pindroid.bookmark.time";
    public static final String EXTRA_UPDATE = "com.pindroid.bookmark.update";
    
    public static final String SYNC_MARKER_KEY = "com.pindroid.BookmarkSyncAdapter.marker";
    
    public static final int HTTP_STATUS_TOO_MANY_REQUESTS = 429;

    public static final int BOOKMARK_PAGE_SIZE = 500;
    
    /**
     * Authtoken type string.
     */
    public static final String AUTHTOKEN_TYPE = "com.pindroid";
    
    //public static final String PREFS_LAST_SYNC = "last_sync";
    
    public static final String PREFS_AUTH_TYPE = "authentication_type";
    public static final String AUTH_TYPE_PINBOARD = "pinboard";
    
    public static final String PREFS_SECRET_TOKEN = "secret_token";
    
    public static final String INSTAPAPER_URL = "http://www.instapaper.com/text?u=";
    
    public static final String GPL_URL = "http://www.gnu.org/licenses/gpl-3.0.txt";
    public static final String MANUAL_URL = "http://code.google.com/p/pindroid/wiki/Manual";
    public static final String DONATION_URL = "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=EUS2Z3WVWK6ZU";
    
    public static enum BookmarkViewType {VIEW, READ, WEB};
}