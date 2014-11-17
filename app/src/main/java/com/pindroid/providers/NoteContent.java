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

import android.net.Uri;
import android.provider.BaseColumns;

public class NoteContent {

	public static class Note implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.parse("content://" + BookmarkContentProvider.AUTHORITY + "/note");
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.PinDroid.notes";
		
		public static final int NOTE_ID_PATH_POSITION = 1;
		
		public static final String Title = "TITLE";
		public static final String Text = "TEXT";
		public static final String Account = "ACCOUNT";
		public static final String Added = "ADDED";
		public static final String Updated = "UPDATED";
		public static final String Hash = "HASH";
		public static final String Pid = "PID";
		
        private String mTitle;
        private String mText;
        private String mHash;
        private String mPid;
        private int mId = 0;
        private String mAccount = null;
        private long mAdded = 0;
        private long mUpdated = 0;

        public int getId(){
        	return mId;
        }
        
        public void setId(int id){
        	mId = id;
        }
        
        public String getTitle() {
            return mTitle;
        }

        public void setTitle(String title) {
        	mTitle = title;
        }
        
        public String getText() {
            return mText;
        }

        public void setText(String text) {
        	mText = text;
        }
        
        public String getHash() {
            return mHash;
        }

        public void setHash(String hash) {
        	mHash = hash;
        }
        
        public String getPid() {
            return mPid;
        }

        public void setPid(String pid) {
        	mPid = pid;
        }
        
        public String getAccount(){
        	return mAccount;
        }
        
        public void setAccount(String account) {
        	mAccount = account;
        }
        
        public long getAdded(){
        	return mAdded;
        }
        
        public void setAdded(long added) {
        	mAdded = added;
        }
        
        public long getUpdated(){
        	return mUpdated;
        }
        
        public void setUpdated(long updated) {
        	mUpdated = updated;
        }
        
        public Note() {
        }
        
        public Note(int id, String title, String text, String account, String hash, String pid, long added, long updated) {
        	mId = id;
            mTitle = title;
            mText = text;
            mAccount = account;
            mHash = hash;
            mPid = pid;
            mAdded = added;
            mUpdated = updated;
        }
        
        public Note copy() {
        	Note n = new Note();
        	n.mTitle = this.mTitle;
        	n.mId = this.mId;
        	n.mText = this.mText;
        	n.mHash = this.mHash;
        	n.mPid = this.mPid;
        	n.mAdded = this.mAdded;
        	n.mUpdated = this.mUpdated;
        	return n;
        }
        
        public void clear() {
        	mTitle = null;
        	mId = 0;
        	mText = null;
        	mHash = null;
        	mPid = null;
        	mAdded = 0;
        	mUpdated = 0;
        }
	}
}
