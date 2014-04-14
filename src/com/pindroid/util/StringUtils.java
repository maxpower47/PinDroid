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

package com.pindroid.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    public static String getUrl(String s) {
    	String result = "";
    	
    	Pattern pattern = Pattern.compile("\\b(https?|ftp|file)://[-a-zA-Z0-9+&@'#/%?=~_|!:,.;()]*[-a-zA-Z0-9+&@'#/%=~_|()]");
    	
    	try{
	    	Matcher matcher = pattern.matcher(s);
	    	
	    	if(matcher.find()) {
	    		result = s.substring(matcher.start(), matcher.end());    		
	    	}
    	}
    	catch(Exception e){
    		result = "";
    	}
    	
    	return result;
    }
}
