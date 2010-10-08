package com.android.droidlicious.providers;

import com.android.droidlicious.Constants;
import com.android.droidlicious.providers.BookmarkContent.Bookmark;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class BookmarkContentProvider extends ContentProvider {
	
	private SQLiteDatabase db;
	private DatabaseHelper dbHelper;
	private static final String DATABASE_NAME = "DeliciousBookmarks.db";
	private static final int DATABASE_VERSION = 8;
	private static final String BOOKMARK_TABLE_NAME = "bookmark";
	
	public static final String AUTHORITY = "com.android.droidlicious.providers.BookmarkContentProvider";
	
	private static class DatabaseHelper extends SQLiteOpenHelper {
		
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase sqlDb) {

			sqlDb.execSQL("Create table " + BOOKMARK_TABLE_NAME + 
					" (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
					"DESCRIPTION TEXT, " +
					"URL TEXT, " +
					"NOTES TEXT, " +
					"TAGS TEXT, " +
					"HASH TEXT, " +
					"META TEXT);");
			
		}

		@Override
		public void onUpgrade(SQLiteDatabase sqlDb, int oldVersion, int newVersion) {
			sqlDb.execSQL("DROP TABLE IF EXISTS " + BOOKMARK_TABLE_NAME);
			onCreate(sqlDb);	
		}
	}

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		return Bookmark.CONTENT_TYPE;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		
		db = dbHelper.getWritableDatabase();
		long rowId = db.insert(BOOKMARK_TABLE_NAME, "", values);
		if(rowId > 0) {
			Uri rowUri = ContentUris.appendId(BookmarkContent.Bookmark.CONTENT_URI.buildUpon(), rowId).build();
			getContext().getContentResolver().notifyChange(rowUri, null);
			return rowUri;
		}
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public boolean onCreate() {

		dbHelper = new DatabaseHelper(getContext());
		return !(dbHelper == null);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,	String[] selectionArgs, String sortOrder) {
		
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase rdb = dbHelper.getReadableDatabase();
		qb.setTables(BOOKMARK_TABLE_NAME);
		Cursor c = qb.query(rdb, projection, selection, selectionArgs, null, null, sortOrder);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		return 0;
	}

}
