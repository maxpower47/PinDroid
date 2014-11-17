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
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

public class BookmarkContent {

	public static class Bookmark implements BaseColumns, Parcelable {

		public static final Uri CONTENT_URI = Uri.parse("content://" + BookmarkContentProvider.AUTHORITY + "/bookmark");
		public static final Uri UNREAD_CONTENT_URI = Uri.parse("content://" + BookmarkContentProvider.AUTHORITY + "/unreadcount");
		
		public static final  String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.pindroid.bookmarks";
		
		public static final int BOOKMARK_ID_PATH_POSITION = 1;
		
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
		public static final String Synced = "SYNCED";
		public static final String Deleted = "DELETED";
		
		private int mId = 0;
		private String mAccount = null;
        private String mUrl = null;
        private String mDescription = null;
        private String mNotes = null;
        private String mTags = null;
        private String mHash = null;
        private String mMeta = null;
        private boolean mShared = true;
        private boolean mRead = false;
        private long mTime = 0;
        private int mSynced = 0;
        private boolean mDeleted = false;

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
        	return mNotes == null ? "" : mNotes;
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
			
			if(mTags != null){
				for(String s : mTags.split(" ")) {
					if(!s.equals(""))
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
        
        public int getSynced(){
        	return mSynced;
        }
        
        public void setSynced(int synced){
        	mSynced = synced;
        }
        
        public boolean getDeleted(){
        	return mDeleted;
        }
        
        public void setDeleted(boolean deleted){
        	mDeleted = deleted;
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
        
        public Bookmark(int id, String account, String url, String description, String notes, String tags, String hash, String meta, long time, boolean read, boolean share, int synced, boolean deleted) {
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
            mSynced = synced;
            mDeleted = deleted;
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
        	b.mSynced = this.mSynced;
        	b.mDeleted = this.mDeleted;
        	return b;
        }
        
        public Bookmark copyForSharing() {
        	Bookmark b = new Bookmark();
        	b.mDescription = this.mDescription;
        	b.mUrl = this.mUrl;
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
        	this.mSynced = 0;
        	this.mDeleted = false;
        }

		public int describeContents() {
			return 0;
		}

		public void writeToParcel(Parcel dest, int flags) {
			dest.writeInt(mId);
			dest.writeString(mAccount);
			dest.writeString(mUrl);
			dest.writeString(mDescription);
			dest.writeString(mNotes);
			dest.writeString(mTags);
			dest.writeString(mHash);
			dest.writeString(mMeta);
			dest.writeLong(mTime);
			dest.writeInt(mSynced);
			dest.writeByte((byte) (mShared ? 1 : 0));
			dest.writeByte((byte) (mRead ? 1 : 0));
			dest.writeByte((byte) (mDeleted ? 1 : 0)); 
		}
		
		public static final Parcelable.Creator<Bookmark> CREATOR
			= new Parcelable.Creator<Bookmark>() {
				public Bookmark createFromParcel(Parcel in) {
	    	 		return new Bookmark(in);
	    	 	}
	    	 
	    	 	public Bookmark[] newArray(int size) {
	    	 		return new Bookmark[size];
	    	 	}
		};
 
		private Bookmark(Parcel in) {
			mId = in.readInt();
			mAccount = in.readString();
			mUrl = in.readString();
			mDescription = in.readString();
			mNotes = in.readString();
			mTags = in.readString();
			mHash = in.readString();
			mMeta = in.readString();
			mTime = in.readLong();
			mSynced = in.readInt();
			mShared = in.readByte() == 1;
			mRead = in.readByte() == 1;
			mDeleted = in.readByte() == 1;
		 }

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((mHash == null) ? 0 : mHash.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Bookmark other = (Bookmark) obj;
			if (mHash == null) {
				if (other.mHash != null)
					return false;
			} else if (!mHash.equals(other.mHash))
				return false;
			return true;
		}
	}
}