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

package com.pindroid.test.providers;

import java.util.Date;

import com.pindroid.Constants;
import com.pindroid.providers.BookmarkContent;
import com.pindroid.providers.BookmarkContentProvider;
import com.pindroid.providers.BookmarkContent.Bookmark;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;

public class BookmarkContentProviderTest extends ProviderTestCase2<BookmarkContentProvider> {

	
    // A URI that the provider does not offer, for testing error handling.
    private static final Uri INVALID_URI = Uri.withAppendedPath(Constants.CONTENT_URI_BASE, "invalid");

    private MockContentResolver mMockResolver;

    private SQLiteDatabase mDb;
	
    
    public BookmarkContentProviderTest(){
    	super(BookmarkContentProvider.class, BookmarkContentProvider.AUTHORITY);
    }
	
	public BookmarkContentProviderTest(Class<BookmarkContentProvider> providerClass, String providerAuthority) {
		super(providerClass, providerAuthority);	
	}

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mMockResolver = getMockContentResolver();
        mDb = getProvider().getDatabaseHelper().getWritableDatabase();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /*
     *  Tests inserts into the data model.
     */
    public void testInserts() {
    	Date d = new Date();
    	
		ContentValues values = new ContentValues();
		values.put(Bookmark.Description, "Test Description");
		values.put(Bookmark.Url, "Test Url");
		values.put(Bookmark.Notes, "Test Note");
		values.put(Bookmark.Tags, "Test Tags");
		values.put(Bookmark.Hash, "testhash");
		values.put(Bookmark.Meta, "testmeta");
		values.put(Bookmark.Time, d.getTime());
		values.put(Bookmark.Account, "testaccount");
		values.put(Bookmark.ToRead, 1);
		values.put(Bookmark.Shared, 0);
		values.put(Bookmark.Synced, 1);
		values.put(Bookmark.Deleted, 0);

        // Insert subtest 1.
        // Inserts a row using the new note instance.
        // No assertion will be done. The insert() method either works or throws an Exception
        Uri rowUri = mMockResolver.insert(BookmarkContent.Bookmark.CONTENT_URI, values);

        // Parses the returned URI to get the note ID of the new note. The ID is used in subtest 2.
        long noteId = ContentUris.parseId(rowUri);

        // Does a full query on the table. Since insertData() hasn't yet been called, the
        // table should only contain the record just inserted.
        Cursor cursor = mMockResolver.query(BookmarkContent.Bookmark.CONTENT_URI, null, null, null, null);

        // Asserts that there should be only 1 record.
        assertEquals(1, cursor.getCount());

        // Moves to the first (and only) record in the cursor and asserts that this worked.
        assertTrue(cursor.moveToFirst());

        // Since no projection was used, get the column indexes of the returned columns
        int descriptionIndex = cursor.getColumnIndex(BookmarkContent.Bookmark.Description);
        int urlIndex = cursor.getColumnIndex(BookmarkContent.Bookmark.Url);
        int noteIndex = cursor.getColumnIndex(BookmarkContent.Bookmark.Notes);
        int tagsIndex = cursor.getColumnIndex(BookmarkContent.Bookmark.Tags);
        int hashIndex = cursor.getColumnIndex(BookmarkContent.Bookmark.Hash);
        int metaIndex = cursor.getColumnIndex(BookmarkContent.Bookmark.Meta);
        int timeIndex = cursor.getColumnIndex(BookmarkContent.Bookmark.Time);
        int accountIndex = cursor.getColumnIndex(BookmarkContent.Bookmark.Account);
        int toreadIndex = cursor.getColumnIndex(BookmarkContent.Bookmark.ToRead);
        int sharedIndex = cursor.getColumnIndex(BookmarkContent.Bookmark.Shared);
        int syncedIndex = cursor.getColumnIndex(BookmarkContent.Bookmark.Synced);
        int deletedIndex = cursor.getColumnIndex(BookmarkContent.Bookmark.Deleted);

        // Tests each column in the returned cursor against the data that was inserted, comparing
        // the field in the NoteInfo object to the data at the column index in the cursor.
        assertEquals("Test Description", cursor.getString(descriptionIndex));
        assertEquals("Test Url", cursor.getString(urlIndex));
        assertEquals("Test Note", cursor.getString(noteIndex));
        assertEquals("Test Tags", cursor.getString(tagsIndex));
        assertEquals("testhash", cursor.getString(hashIndex));
        assertEquals("testmeta", cursor.getString(metaIndex));
        assertEquals(d.getTime(), cursor.getLong(timeIndex));
        assertEquals("testaccount", cursor.getString(accountIndex));
        assertEquals(1, cursor.getInt(toreadIndex));
        assertEquals(0, cursor.getInt(sharedIndex));
        assertEquals(1, cursor.getInt(syncedIndex));
        assertEquals(0, cursor.getInt(deletedIndex));

        // Insert subtest 2.
        // Tests that we can't insert a record whose id value already exists.

        // Defines a ContentValues object so that the test can add a note ID to it.
        ContentValues values2 = values;

        // Adds the note ID retrieved in subtest 1 to the ContentValues object.
        values.put(BookmarkContent.Bookmark._ID, (int) noteId);

        // Tries to insert this record into the table. This should fail and drop into the
        // catch block. If it succeeds, issue a failure message.
        try {
            rowUri = mMockResolver.insert(BookmarkContent.Bookmark.CONTENT_URI, values);
            fail("Expected insert failure for existing record but insert succeeded.");
        } catch (Exception e) {
          // succeeded, so do nothing.
        }
    }
}
