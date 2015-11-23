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

import com.pindroid.providers.BookmarkContentProvider;

import java.util.Date;

public class Tag implements BaseColumns {
    public static final Uri CONTENT_URI = Uri.parse("content://" +
            BookmarkContentProvider.AUTHORITY + "/tag");

    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.PinDroid.tags";

    public static final String Name = "NAME";
    public static final String Count = "COUNT";
    public static final String Account = "ACCOUNT";

    private String mTagName;
    private int mCount = 0;
    private int mId = 0;
    private String mType = null;
    private String mAccount;

    public int getId() {
        return mId;
    }

    public String getTagName() {
        return mTagName;
    }

    public int getCount() {
        return mCount;
    }

    public String getType() {
        return mType;
    }

    public String getAccount() {
        return mAccount;
    }

    public void setType(String type) {
        mType = type;
    }

    public void setCount(int count) {
        mCount = count;
    }

    public Tag() {
        mTagName = "";
    }

    public Tag(String tagName) {
        mTagName = tagName;
    }

    public Tag(String tagName, String account) {
        mTagName = tagName;
        mAccount = account;
    }

    public Tag(String tagName, int count) {
        mTagName = tagName;
        mCount = count;
    }

    public Tag(Cursor c) {
        mId = c.getInt(c.getColumnIndex(_ID));

        if(c.getColumnIndex(Name) != -1)
            mTagName = c.getString(c.getColumnIndex(Name));

        if(c.getColumnIndex(Count) != -1)
            mCount = c.getInt(c.getColumnIndex(Count));

        if(c.getColumnIndex(Account) != -1)
            mAccount = c.getString(c.getColumnIndex(Account));
    }

    public Tag copy() {
        Tag t = new Tag();
        t.mCount = this.mCount;
        t.mId = this.mId;
        t.mTagName = this.mTagName;
        t.mType = this.mType;
        return t;
    }

    public ContentValues toContentValues() {
        final ContentValues values = new ContentValues();

        values.put(Tag.Name, mTagName);
        values.put(Tag.Count, mCount);
        values.put(Tag.Account, mAccount);

        return  values;
    }
}
