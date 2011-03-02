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

package com.pindroid.providers;

import java.util.ArrayList;

import com.pindroid.providers.TagContent.Tag;

import android.net.Uri;
import android.provider.BaseColumns;

public class BookmarkContent {

	public static class Bookmark implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.parse("content://" + 
				BookmarkContentProvider.AUTHORITY + "/bookmark");
		
		public static final  String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.pindroid.bookmarks";
		
		public static final String Account = "ACCOUNT";
		public static final String Description = "DESCRIPTION";
		public static final String Url = "URL";
		public static final String Notes = "NOTES";
		public static final String Tags = "TAGS";
		public static final String Hash = "HASH";
		public static final String Meta = "META";
		public static final String Time = "TIME";
		public static final String ToRead = "TOREAD";
		public static final String Shared = "SHARED";
		public static final String Source = "SOURCE";
		
		private int mId = 0;
		private String mAccount = null;
        private String mUrl = null;
        private String mDescription = null;
        private String mNotes = null;
        private String mTags = null;
        private String mHash = null;
        private String mMeta = null;
        private Boolean mShared = true;
        private Boolean mRead = false;
        private long mTime = 0;
        private String mSource = null;

        public int getId(){
        	return mId;
        }
        
        public void setId(int id){
        	mId = id;
        }
        
        public String getUrl() {
            return mUrl;
        }
        
        public void setUrl(String url) {
        	mUrl = url;
        }

        public String getDescription() {
            return mDescription;
        }
        
        public void setDescription(String desc) {
        	mDescription = desc;
        }
        
        public String getNotes(){
        	return mNotes;
        }
        
        public void setNotes(String notes) {
        	mNotes = notes;
        }
        
        public String getTagString(){
        	return mTags;
        }
        
        public void setTagString(String tags){
        	mTags = tags;
        }
        
        public ArrayList<Tag> getTags(){
			ArrayList<Tag> result = new ArrayList<Tag>();
			
			if(this.getTagString() != null){
				for(String s : this.getTagString().split(" ")) {
					result.add(new Tag(s));
				}
			}
			
			return result;
        }
        
        public String getHash(){
        	return mHash;
        }
        
        public void setHash(String hash) {
        	mHash = hash;
        }

        public String getMeta(){
        	return mMeta;
        }
        
        public void setMeta(String meta) {
        	mMeta = meta;
        }
        
        public long getTime(){
        	return mTime;
        }
        
        public void setTime(long time) {
        	mTime = time;
        }
        
        public boolean getShared(){
        	return mShared;
        }
        
        public void setShared(boolean shared) {
        	mShared = shared;
        }
        
        public boolean getToRead(){
        	return mRead;
        }
        
        public void setToRead(boolean toread) {
        	mRead = toread;
        }
        
        public String getAccount(){
        	return mAccount;
        }
        
        public void setAccount(String account) {
        	mAccount = account;
        }
        
        public String getSource(){
        	return mSource;
        }
        
        public void setSource(String source){
        	mSource = source;
        }
        
        public Bookmark() {
        }
        
        public Bookmark(int id) {
        	mId = id;
        }
        
        public Bookmark(String url) {
            mUrl = url;
        }
        
        public Bookmark(String url, String description, String notes, String tags, boolean priv, boolean toread, long time) {
            mUrl = url;
            mDescription = description;
            mNotes = notes;
            mTags = tags;
            mShared = priv;
            mRead = toread;
            mTime = time;
        }
        
        public Bookmark(int id, String account, String url, String description, String notes, String tags, String hash, String meta, long time, boolean read, boolean share) {
            mId = id;
        	mUrl = url;
            mDescription = description;
            mNotes = notes;
            mTags = tags;
            mHash = hash;
            mMeta = meta;
            mTime = time;
            mAccount = account;
            mRead = read;
            mShared = share;
        }
        
        public Bookmark copy() {
        	Bookmark b = new Bookmark();
        	b.mAccount = this.mAccount;
        	b.mDescription = this.mDescription;
        	b.mHash = this.mHash;
        	b.mId = this.mId;
        	b.mMeta = this.mMeta;
        	b.mNotes = this.mNotes;
        	b.mRead = this.mRead;
        	b.mShared = this.mShared;
        	b.mTags = this.mTags;
        	b.mTime = this.mTime;
        	b.mUrl = this.mUrl;
        	b.mSource = this.mSource;
        	return b;
        }
        
        public void clear() {
        	this.mAccount = null;
        	this.mDescription = null;
        	this.mHash = null;
        	this.mId = 0;
        	this.mMeta = null;
        	this.mNotes = null;
        	this.mRead = false;
        	this.mShared = true;
        	this.mTags = null;
        	this.mTime = 0;
        	this.mUrl = null;
        	this.mSource = null;
        }
	}
}