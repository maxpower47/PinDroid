package com.android.droidlicious.providers;


import com.android.droidlicious.Constants;
import com.android.droidlicious.providers.BookmarkContent.Bookmark;
import com.android.droidlicious.providers.TagContent.Tag;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class BookmarkContentProvider extends ContentProvider {
	
	private AccountManager mAccountManager;
	private Account mAccount;
	
	private SQLiteDatabase db;
	private DatabaseHelper dbHelper;
	private static final String DATABASE_NAME = "DeliciousBookmarks.db";
	private static final int DATABASE_VERSION = 9;
	private static final String BOOKMARK_TABLE_NAME = "bookmark";
	private static final String TAG_TABLE_NAME = "tag";
	
	private static final UriMatcher sURIMatcher = buildUriMatcher();
	
	public static final String AUTHORITY = "com.android.droidlicious";
	
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
			
			sqlDb.execSQL("Create table " + TAG_TABLE_NAME + 
					" (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
					"NAME TEXT, " +
					"COUNT INTEGER);");
			
		}

		@Override
		public void onUpgrade(SQLiteDatabase sqlDb, int oldVersion, int newVersion) {
			sqlDb.execSQL("DROP TABLE IF EXISTS " + BOOKMARK_TABLE_NAME);
			sqlDb.execSQL("DROP TABLE IF EXISTS " + TAG_TABLE_NAME);
			onCreate(sqlDb);	
		}
	}

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		switch(sURIMatcher.match(uri)){
			case 1:
				return Bookmark.CONTENT_TYPE;
			case 2:
				return SearchManager.SUGGEST_MIME_TYPE;
			case 3:
				return Tag.CONTENT_TYPE;
			default:
				throw new IllegalArgumentException("Unknown URL " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		switch(sURIMatcher.match(uri)) {
			case 1:
				return insertBookmark(uri, values);
			case 3:
				return insertTag(uri, values);
			default:
				throw new IllegalArgumentException("Unknown Uri: " + uri);
		}
	}
	
	private Uri insertBookmark(Uri uri, ContentValues values){
		db = dbHelper.getWritableDatabase();
		long rowId = db.insert(BOOKMARK_TABLE_NAME, "", values);
		if(rowId > 0) {
			Uri rowUri = ContentUris.appendId(BookmarkContent.Bookmark.CONTENT_URI.buildUpon(), rowId).build();
			getContext().getContentResolver().notifyChange(rowUri, null);
			return rowUri;
		}
		throw new SQLException("Failed to insert row into " + uri);
	}

	private Uri insertTag(Uri uri, ContentValues values){
		db = dbHelper.getWritableDatabase();
		long rowId = db.insert(TAG_TABLE_NAME, "", values);
		if(rowId > 0) {
			Uri rowUri = ContentUris.appendId(TagContent.Tag.CONTENT_URI.buildUpon(), rowId).build();
			getContext().getContentResolver().notifyChange(rowUri, null);
			return rowUri;
		}
		throw new SQLException("Failed to insert row into " + uri);
	}
	
	@Override
	public boolean onCreate() {

		dbHelper = new DatabaseHelper(getContext());
		mAccountManager = AccountManager.get(getContext());
		mAccount = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE)[0];
		return !(dbHelper == null);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,	String[] selectionArgs, String sortOrder) {
		switch(sURIMatcher.match(uri)) {
			case 1:
				return getBookmarks(projection, selection, selectionArgs, sortOrder);
			case 2:
				String query = uri.getLastPathSegment().toLowerCase();
				return getSearchSuggestions(query);
			case 3:
				return getTags(projection, selection, selectionArgs, sortOrder);
			default:
				throw new IllegalArgumentException("Unknown Uri: " + uri);
		}
	}
	
	private Cursor getBookmarks(String[] projection, String selection,	String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase rdb = dbHelper.getReadableDatabase();
		qb.setTables(BOOKMARK_TABLE_NAME);
		Cursor c = qb.query(rdb, projection, selection, selectionArgs, null, null, sortOrder);
		//c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}
	
	private Cursor getTags(String[] projection, String selection,	String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase rdb = dbHelper.getReadableDatabase();
		qb.setTables(TAG_TABLE_NAME);
		Cursor c = qb.query(rdb, projection, selection, selectionArgs, null, null, sortOrder);
		//c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}
	
	private Cursor getSearchSuggestions(String query) {
		Log.d("getSearchSuggestions", query);
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase rdb = dbHelper.getReadableDatabase();
		qb.setTables(TAG_TABLE_NAME);
		
		String selection = Tag.Name + " LIKE '%" + query + "%'";
		
		String[] projection = new String[] {BaseColumns._ID, Tag.Name, Tag.Count};

		Cursor c = qb.query(rdb, projection, selection, null, null, null, null);
		
		MatrixCursor mc = new MatrixCursor(new String[] {BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_INTENT_DATA});

		int i = 0;
		
		if(c.moveToFirst()){
			int nameColumn = c.getColumnIndex(Tag.Name);

			do {
				Uri.Builder data = Constants.CONTENT_URI_BASE.buildUpon();
				data.appendEncodedPath("bookmarks");
				data.appendQueryParameter("username", mAccount.name);
				data.appendQueryParameter("tagname", c.getString(nameColumn));
				
				mc.addRow(new Object[] {i++, c.getString(nameColumn), data.build().toString()});
				
			} while(c.moveToNext());	
		}
		c.close();
		
		return mc;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		return 0;
	}
	
    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher =  new UriMatcher(UriMatcher.NO_MATCH);
        // to get definitions...
        matcher.addURI(AUTHORITY, "bookmark", 1);
        matcher.addURI(AUTHORITY, "tag", 3);
        // to get suggestions...
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, 2);
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", 2);
        return matcher;
    }


}
