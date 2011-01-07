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

    /**
     * Authtoken type string.
     */
    public static final String AUTHTOKEN_TYPE = "com.pindroid";
    
    public static final String PREFS_LAST_SYNC = "last_sync";
    
    public static final String PREFS_AUTH_TYPE = "authentication_type";
    public static final String AUTH_TYPE_PINBOARD = "pinboard";
    
    public static final String GPL_URL = "http://www.gnu.org/licenses/gpl-3.0.txt";
    public static final String MANUAL_URL = "http://code.google.com/p/pindroid/wiki/Manual";
    public static final String DONATION_URL = "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=EUS2Z3WVWK6ZU";
}