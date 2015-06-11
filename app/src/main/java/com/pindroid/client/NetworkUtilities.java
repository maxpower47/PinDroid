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

import com.pindroid.Constants;

import org.apache.commons.io.IOUtils;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Provides utility methods for communicating with the server.
 */
public class NetworkUtilities {

    /**
     * Gets the title of a web page.
     * 
     * @param urlString The URL of the web page.
     * @return A String containing the title of the web page.
     */
    public static String getWebpageTitle(String urlString) {
   	
    	if(urlString != null && !urlString.equals("")) {
    		
    		if(!urlString.startsWith("http")){
                urlString = "http://" + urlString;
    		}

            try {
                URL url = new URL(urlString);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                conn.connect();

                if (conn.getResponseCode() == Constants.HTTP_STATUS_OK) {
                    String response = IOUtils.toString(conn.getInputStream());
                    int start = response.indexOf("<title>") + 7;
                    int end = response.indexOf("</title>", start + 1);
                    return response.substring(start, end);
                }
            } catch (Exception e) {

            }
    	}
        return "";
    }
}