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

package com.pindroid.dashclock;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;
import com.pindroid.Constants;
import com.pindroid.R;
import com.pindroid.action.IntentHelper;
import com.pindroid.activity.Main;
import com.pindroid.providers.BookmarkContent.Bookmark;
import com.pindroid.util.AccountHelper;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

public class PinDroidExtension extends DashClockExtension {

    @Override
    protected void onInitialize(boolean isReconnect) {
    	addWatchContentUris(new String[] {Bookmark.CONTENT_URI.toString()});
    }
    
    @Override
    protected void onUpdateData(int reason) {
    	String body = "";
    	int total = 0;
        boolean visible = true;
        int accounts = 0;
        String mainAccount = null;
    	
    	try {
	        Map<String, Integer> counts = GetUnreadCount();
	        accounts = AccountHelper.getAccountCount(this);
	        
	        for(Entry<String, Integer> e : counts.entrySet()) {
	        	body += e.getKey() + " (" + e.getValue() + ")\n";
	        	total += e.getValue();

                if(mainAccount == null) {
                    mainAccount = e.getKey();
                }
	        }
	        
	        body = body.substring(0, body.length() - 1);
    	} catch (Exception e) {
    		visible = false;
    	}

        // Publish the extension data update.
        publishUpdate(new ExtensionData()
                .visible(visible && total > 0)
                .icon(R.drawable.ic_pindroid_dashclock)
                .status(getString(R.string.dashclock_update_status, total))
                .expandedTitle(getString(R.string.dashclock_update_expanded_title, total))
                .expandedBody(accounts > 1 ? body : null)
                .clickIntent(IntentHelper.ViewUnread(mainAccount, this.getBaseContext())));
    }
	
	public Map<String, Integer> GetUnreadCount(){		
		Map<String, Integer> result = new HashMap<String, Integer>();

		final String[] projection = new String[] {"Count", "Account"};
		final Cursor c = this.getContentResolver().query(Bookmark.UNREAD_CONTENT_URI, projection, null, null, null);	
		
		if(c.moveToFirst()){
			do {			
				int count = c.getInt(c.getColumnIndex("Count"));
				String account = c.getString(c.getColumnIndex("Account"));
				
				result.put(account, count);
				
			} while(c.moveToNext());
		}
			
		c.close();
		
		return result;
	}
}