/*
 * Droidlicious - http://code.google.com/p/droidlicious/
 *
 * Copyright (C) 2010 Matt Schmidt
 *
 * Droidlicious is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * Droidlicious is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Droidlicious; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */

package com.android.droidlicious.client;

import com.android.droidlicious.util.DateParser;

public class Update {
	private long lastUpdate;
	private int inboxNew;
	
	public long getLastUpdate(){
		return lastUpdate;
	}
	
	public int getInboxNew(){
		return inboxNew;
	}
	
	public Update(long update, int inbox){
		lastUpdate = update;
		inboxNew = inbox;
	}
	
	public static Update valueOf(String updateResponse){
        try {
        	int start = updateResponse.indexOf("<update");
        	int end = updateResponse.indexOf("/>", start);
        	String updateElement = updateResponse.substring(start, end);
        	int timestart = updateElement.indexOf("time=");
        	int timeend = updateElement.indexOf("\"", timestart + 7);
        	String time = updateElement.substring(timestart + 6, timeend);

			long updateTime = DateParser.parse(time).getTime();
			
			int inboxstart = updateElement.indexOf("inboxnew");
			int inboxend = updateElement.indexOf("\"", inboxstart + 10);
			int inbox = Integer.parseInt(updateElement.substring(inboxstart + 10, inboxend));
			
			return new Update(updateTime, inbox);
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
