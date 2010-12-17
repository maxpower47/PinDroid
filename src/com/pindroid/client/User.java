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

import java.util.Date;

import android.util.Log;

import org.json.JSONObject;

import com.pindroid.util.DateParser;

/**
 * Represents a sample SyncAdapter user
 */
public class User {

    private final String mUserName;

    public String getUserName() {
        return mUserName;
    }

    public User(String name) {
        mUserName = name;
    }

    /**
     * Creates and returns an instance of the user from the provided JSON data.
     * 
     * @param user The JSONObject containing user data
     * @return user The new instance of Pinboard user created from the JSON data.
     */
    public static User valueOf(JSONObject user) {
        try {
            final String userName = user.getString("user");
            return new User(userName);
        } catch (final Exception ex) {
            Log.i("User", "Error parsing JSON user object" + ex.toString());

        }
        return null;
    }

    /**
     * Represents the User's status messages
     * 
     */
    public static class Status {
        private final String mUserName;
        private final String mStatus;
        private final Date mTimestamp;

        public String getUserName() {
            return mUserName;
        }

        public String getStatus() {
            return mStatus;
        }
        
        public Date getTimeStamp() {
            return mTimestamp;
        }

        public Status(String userName, String status, Date timestamp) {
            mUserName = userName;
            mStatus = status;
            mTimestamp = timestamp;
        }

        public static User.Status valueOf(JSONObject userStatus) {
            try {
                final String userName = userStatus.getString("a");
                final String status = userStatus.getString("d");
                final String date = userStatus.getString("dt");
                
                Date timestamp = DateParser.parse(date);
                
                return new User.Status(userName, status, timestamp);
            } catch (final Exception ex) {
                Log.i("User.Status", "Error parsing JSON user object");
                Log.d("User.Status", ex.toString());
            }
            return null;
        }
    }
}