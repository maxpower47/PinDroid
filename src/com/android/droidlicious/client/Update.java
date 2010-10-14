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
