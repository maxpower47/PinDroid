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

package com.pindroid.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import com.google.gson.annotations.SerializedName;
import com.pindroid.providers.BookmarkContentProvider;

import java.util.Date;

public class Note implements BaseColumns {
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

    private int mId = 0;
    private String mAccount = null;
    @SerializedName("title") private String mTitle;
    @SerializedName("text") private String mText;
    @SerializedName("hash") private String mHash;
    @SerializedName("id") private String mPid;
    @SerializedName("created_at") private Date mAdded;
    @SerializedName("updated_at") private Date mUpdated;

    public Note() {
    }

    public Note(int id, String title, String text, String account, String hash, String pid, Date added, Date updated) {
        mId = id;
        mTitle = title;
        mText = text;
        mAccount = account;
        mHash = hash;
        mPid = pid;
        mAdded = added;
        mUpdated = updated;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
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

    public String getPid() {
        return mPid;
    }

    public String getAccount() {
        return mAccount;
    }

    public void setAccount(String account) {
        mAccount = account;
    }

    public Date getAdded() {
        return mAdded;
    }

    public Date getUpdated() {
        return mUpdated;
    }

    public Note(Cursor c) {
        mId = c.getInt(c.getColumnIndex(_ID));

        if(c.getColumnIndex(Title) != -1)
            mTitle = c.getString(c.getColumnIndex(Title));

        if(c.getColumnIndex(Text) != -1)
            mText = c.getString(c.getColumnIndex(Text));

        if(c.getColumnIndex(Account) != -1)
            mAccount = c.getString(c.getColumnIndex(Account));

        if(c.getColumnIndex(Added) != -1)
            mAdded = new Date(c.getLong(c.getColumnIndex(Added)));

        if(c.getColumnIndex(Updated) != -1)
            mUpdated = new Date(c.getLong(c.getColumnIndex(Updated)));

        if(c.getColumnIndex(Hash) != -1)
            mHash = c.getString(c.getColumnIndex(Hash));

        if(c.getColumnIndex(Pid) != -1)
            mPid = c.getString(c.getColumnIndex(Pid));
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

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();

        values.put(Note.Title, mTitle);
        values.put(Note.Text, mText);
        values.put(Note.Account, mAccount);
        values.put(Note.Hash, mHash);
        values.put(Note.Pid, mPid);
        values.put(Note.Added, mAdded.getTime());
        values.put(Note.Updated, mUpdated.getTime());

        return values;
    }
}
