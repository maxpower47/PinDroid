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

package com.android.droidlicious.client;

import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import android.util.Log;

import org.json.JSONObject;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

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
     * @return user The new instance of Delicious user created from the JSON data.
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
                
                DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                Date timestamp = new Date();
                timestamp = (Date)formatter.parse(date);
                
                Log.d("status_timestamp", timestamp.toString());
                return new User.Status(userName, status, timestamp);
            } catch (final Exception ex) {
                Log.i("User.Status", "Error parsing JSON user object");
                Log.d("User.Status", ex.toString());
            }
            return null;
        }
    }
    
    /**
     * Represents the User's tags
     * 
     */
    public static class Tag {
        private final String mTagName;
        private final int mCount;

        public String getTagName() {
            return mTagName;
        }

        public int getCount() {
            return mCount;
        }

        public Tag(String tagName, int count) {
            mTagName = tagName;
            mCount = count;
        }
    }
    
    /**
     * Represents the User's tags
     * 
     */
    public static class Bookmark {
        private String mUrl;
        private String mDescription;
        private String mNotes;
        private String mTags;
        private String mHash;
        private String mMeta;

        public String getUrl() {
            return mUrl;
        }

        public String getDescription() {
            return mDescription;
        }
        
        public String getNotes(){
        	return mNotes;
        }
        
        public String getTags(){
        	return mTags;
        }
        
        public String getHash(){
        	return mHash;
        }

        public String getMeta(){
        	return mMeta;
        }
        
        public Bookmark() {
            mUrl = "";
            mDescription = "";
            mNotes = "";
            mTags = "";
            mHash = "";
            mMeta = "";
        }
        
        public Bookmark(String url, String description) {
            mUrl = url;
            mDescription = description;
            mNotes = "";
            mTags = "";
            mHash = "";
            mMeta = "";
        }
        
        public Bookmark(String url, String description, String notes) {
            mUrl = url;
            mDescription = description;
            mNotes = notes;
            mTags = "";
            mHash = "";
            mMeta = "";
        }
        
        public Bookmark(String url, String description, String notes, String tags, String hash, String meta) {
            mUrl = url;
            mDescription = description;
            mNotes = notes;
            mTags = tags;
            mHash = hash;
            mMeta = meta;
        }
        
        public static ArrayList<User.Bookmark> valueOf(String userBookmark){
            XPath xpath = XPathFactory.newInstance().newXPath();
            String expression = "/posts/post";
            ArrayList<User.Bookmark> list = new ArrayList<User.Bookmark>();
            
            InputSource inputSource = new InputSource(new StringReader(userBookmark));
            try {
            	
				NodeList nodes = (NodeList)xpath.evaluate(expression, inputSource, XPathConstants.NODESET);
				
				for(int i = 0; i < nodes.getLength(); i++){
					Node href = nodes.item(i).getAttributes().getNamedItem("href");
					Node title = nodes.item(i).getAttributes().getNamedItem("description");
					Node notes = nodes.item(i).getAttributes().getNamedItem("extended");
					Node tags = nodes.item(i).getAttributes().getNamedItem("tag");
					Node hash = nodes.item(i).getAttributes().getNamedItem("hash");
					Node meta = nodes.item(i).getAttributes().getNamedItem("meta");
					Node url = nodes.item(i).getAttributes().getNamedItem("url");
					String shref = "";
					String stitle = "";
					String snotes = "";
					String stags = "";
					String shash = "";
					String smeta = "";

					if(href != null)
						shref = href.getTextContent();
					if(title != null)
						stitle = title.getTextContent();
					if(notes != null)
						snotes = notes.getTextContent();
					if(tags != null)
						stags = tags.getTextContent();
					if(hash != null)
						shash = hash.getTextContent();
					if(url != null)
						shash = url.getTextContent();
					if(meta != null)
						smeta = meta.getTextContent();
					
					list.add(new User.Bookmark(shref, stitle, snotes, stags, shash, smeta));

				}
				
			} catch (XPathExpressionException e) {
				e.printStackTrace();
			}
			return list;
        }
        
        public static User.Bookmark valueOf(JSONObject userBookmark) {
            try {
                final String url = userBookmark.getString("u");
                final String description = userBookmark.getString("d");
                Log.d("bookmarkurl", url);
                Log.d("bookmarkdescription", description);

                return new User.Bookmark(url, description);
            } catch (final Exception ex) {
                Log.i("User.Bookmark", "Error parsing JSON user object");
            }
            return null;
        }

    }

}
