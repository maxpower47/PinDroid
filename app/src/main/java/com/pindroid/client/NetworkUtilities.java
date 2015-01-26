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

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

/**
 * Provides utility methods for communicating with the server.
 */
public class NetworkUtilities {

    /**
     * Gets the title of a web page.
     * 
     * @param url The URL of the web page.
     * @return A String containing the title of the web page.
     */
    public static String getWebpageTitle(String url) {
   	
    	if(url != null && !url.equals("")) {
    		
    		if(!url.startsWith("http")){
    			url = "http://" + url;
    		}
	
	    	HttpResponse resp = null;
	    	HttpGet post = null;
	    	
	    	try {
				post = new HttpGet(url.replace("|", "%7C"));
	
				post.setHeader("User-Agent", "Mozilla/5.0");
	
				resp = HttpClientFactory.getThreadSafeClient().execute(post);

		    	if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
		    		String response = EntityUtils.toString(resp.getEntity(), HTTP.UTF_8);
		    		int start = response.indexOf("<title>") + 7;
		    		int end = response.indexOf("</title>", start + 1);
		    		String title = response.substring(start, end);
		    		return title;
		    	} else return "";
			} catch (Exception e) {
				return "";
			}
    	} else return "";
    }
}