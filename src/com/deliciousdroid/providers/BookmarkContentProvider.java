/*
 * DeliciousDroid - http://code.google.com/p/DeliciousDroid/
 *
 * Copyright (C) 2010 Matt Schmidt
 *
 * DeliciousDroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 3 of the License,
 * or (at your option) any later version.
 *
 * DeliciousDroid is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DeliciousDroid; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */

package com.deliciousdroid.providers;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.deliciousdroid.Constants;
import com.deliciousdroid.R;
import com.deliciousdroid.providers.BookmarkContent.Bookmark;
import com.deliciousdroid.providers.TagContent.Tag;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.LiveFolders;
import android.util.Log;

public class BookmarkContentProvider extends ContentProvider {
	
	private AccountManager mAccountManager = null;
	private Account mAccount = null;
	
	private SQLiteDatabase db;
	private DatabaseHelper dbHelper;
	private static final String DATABASE_NAME = "DeliciousBookmarks.db";
	private static final int DATABASE_VERSION = 19;
	private static final String BOOKMARK_TABLE_NAME = "bookmark";
	private static final String TAG_TABLE_NAME = "tag";
	
	private static final int Bookmarks = 1;
	private static final int SearchSuggest = 2;
	private static final int Tags = 3;
	private static final int TagSearchSuggest = 4;
	private static final int BookmarkSearchSuggest = 5;
	private static final int TagLiveFolder = 6;
	private static final int BookmarkLiveFolder = 7;
	
	private static final String SuggestionLimit = "10";
	
	private static final UriMatcher sURIMatcher = buildUriMatcher();
	
	public static final String AUTHORITY = "com.deliciousdroid.providers.BookmarkContentProvider";
	
	private static class DatabaseHelper extends SQLiteOpenHelper {
		
		private Context mContext;
		
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			mContext = context;
		}

		@Override
		public void onCreate(SQLiteDatabase sqlDb) {

			sqlDb.execSQL("Create table " + BOOKMARK_TABLE_NAME + 
					" (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
					"ACCOUNT TEXT, " +
					"DESCRIPTION TEXT, " +
					"URL TEXT, " +
					"NOTES TEXT, " +
					"TAGS TEXT, " +
					"HASH TEXT, " +
					"META TEXT, " +
					"TIME INTEGER, " +
					"LASTUPDATE INTEGER);");
			
			sqlDb.execSQL("CREATE INDEX " + BOOKMARK_TABLE_NAME + 
					"_ACCOUNT ON " + BOOKMARK_TABLE_NAME + " " +
					"(ACCOUNT)");
			
			sqlDb.execSQL("CREATE INDEX " + BOOKMARK_TABLE_NAME + 
					"_TAGS ON " + BOOKMARK_TABLE_NAME + " " +
					"(TAGS)");
			
			sqlDb.execSQL("CREATE INDEX " + BOOKMARK_TABLE_NAME + 
					"_HASH ON " + BOOKMARK_TABLE_NAME + " " +
					"(HASH)");
			
			sqlDb.execSQL("Create table " + TAG_TABLE_NAME + 
					" (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
					"ACCOUNT TEXT, " +
					"NAME TEXT, " +
					"COUNT INTEGER);");
			
			sqlDb.execSQL("CREATE INDEX " + TAG_TABLE_NAME + 
					"_ACCOUNT ON " + TAG_TABLE_NAME + " " +
					"(ACCOUNT)");
			
		}

		@Override
		public void onUpgrade(SQLiteDatabase sqlDb, int oldVersion, int newVersion) {
			sqlDb.execSQL("DROP INDEX IF EXISTS " + BOOKMARK_TABLE_NAME + "_ACCOUNT");
			sqlDb.execSQL("DROP INDEX IF EXISTS " + BOOKMARK_TABLE_NAME + "_TAGS");
			sqlDb.execSQL("DROP INDEX IF EXISTS " + BOOKMARK_TABLE_NAME + "_HASH");
			sqlDb.execSQL("DROP INDEX IF EXISTS " + TAG_TABLE_NAME + "_ACCOUNT");
			sqlDb.execSQL("DROP TABLE IF EXISTS " + BOOKMARK_TABLE_NAME);
			sqlDb.execSQL("DROP TABLE IF EXISTS " + TAG_TABLE_NAME);
			
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
    		SharedPreferences.Editor editor = settings.edit();
    		editor.putLong(Constants.PREFS_LAST_SYNC, 0);
            editor.commit();
			
			onCreate(sqlDb);	
		}
	}
	
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int count;
		switch (sURIMatcher.match(uri)) {
			case Bookmarks:
				count = db.delete(BOOKMARK_TABLE_NAME, where, whereArgs);
				break;
			case Tags:
				count = db.delete(TAG_TABLE_NAME, where, whereArgs);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch(sURIMatcher.match(uri)){
			case Bookmarks:
				return Bookmark.CONTENT_TYPE;
			case SearchSuggest:
				return SearchManager.SUGGEST_MIME_TYPE;
			case Tags:
				return Tag.CONTENT_TYPE;
			default:
				throw new IllegalArgumentException("Unknown URL " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		
		switch(sURIMatcher.match(uri)) {
			case Bookmarks:
				return insertBookmark(uri, values);
			case Tags:
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
		
		return !(dbHelper == null);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,	String[] selectionArgs, String sortOrder) {
		switch(sURIMatcher.match(uri)) {
			case Bookmarks:
				return getBookmarks(uri, projection, selection, selectionArgs, sortOrder);
			case SearchSuggest:
				String query = uri.getLastPathSegment().toLowerCase();
				return getSearchSuggestions(query);
			case Tags:
				return getTags(uri, projection, selection, selectionArgs, sortOrder);
			case TagSearchSuggest:
				String tagQuery = uri.getLastPathSegment().toLowerCase();
				return getSearchCursor(getTagSearchSuggestions(tagQuery));
			case BookmarkSearchSuggest:
				String bookmarkQuery = uri.getLastPathSegment().toLowerCase();
				return getSearchCursor(getBookmarkSearchSuggestions(bookmarkQuery));
			case TagLiveFolder:
				return getTagLiveFolderResults(uri);
			case BookmarkLiveFolder:
				return getBookmarkLiveFolderResults(uri);
			default:
				throw new IllegalArgumentException("Unknown Uri: " + uri);
		}
	}
	
	private Cursor getBookmarks(Uri uri, String[] projection, String selection,	String[] selectionArgs, String sortOrder) {
		return getBookmarks(uri, projection, selection, selectionArgs, sortOrder, null);
	}
	
	private Cursor getBookmarks(Uri uri, String[] projection, String selection,	String[] selectionArgs, String sortOrder, String limit) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase rdb = dbHelper.getReadableDatabase();
		qb.setTables(BOOKMARK_TABLE_NAME);
		Cursor c = qb.query(rdb, projection, selection, selectionArgs, null, null, sortOrder, limit);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}
	
	private Cursor getTags(Uri uri, String[] projection, String selection,	String[] selectionArgs, String sortOrder) {
		return getTags(uri, projection, selection, selectionArgs, sortOrder, null);
	}
	
	private Cursor getTags(Uri uri, String[] projection, String selection,	String[] selectionArgs, String sortOrder, String limit) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase rdb = dbHelper.getReadableDatabase();
		qb.setTables(TAG_TABLE_NAME);
		Cursor c = qb.query(rdb, projection, selection, selectionArgs, null, null, sortOrder, limit);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}
	
	private Cursor getSearchSuggestions(String query) {
		Log.d("getSearchSuggestions", query);
		
		mAccountManager = AccountManager.get(getContext());
		mAccount = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE)[0];
		
		Map<String, SearchSuggestionContent> tagSuggestions = new TreeMap<String, SearchSuggestionContent>();
		Map<String, SearchSuggestionContent> bookmarkSuggestions = new TreeMap<String, SearchSuggestionContent>();
			
		tagSuggestions = getTagSearchSuggestions(query);
		bookmarkSuggestions = getBookmarkSearchSuggestions(query);
	
		SortedMap<String, SearchSuggestionContent> s = new TreeMap<String, SearchSuggestionContent>();
		s.putAll(tagSuggestions);
		s.putAll(bookmarkSuggestions);
		
		return getSearchCursor(s);
	}
	
	private Map<String, SearchSuggestionContent> getBookmarkSearchSuggestions(String query) {
		Log.d("getBookmarkSearchSuggestions", query);
		
		mAccountManager = AccountManager.get(getContext());
		mAccount = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE)[0];
		
		Map<String, SearchSuggestionContent> suggestions = new TreeMap<String, SearchSuggestionContent>();
				
		// Title/description/notes search suggestions
		SQLiteQueryBuilder bookmarkqb = new SQLiteQueryBuilder();	
		bookmarkqb.setTables(BOOKMARK_TABLE_NAME);
		
		String selection = Bookmark.Description + " LIKE '%" + query + "%' OR " + 
			Bookmark.Notes + " LIKE '%" + query + "%'";
		
		String[] projection = new String[] {BaseColumns._ID, Bookmark.Description, Bookmark.Url};

		Cursor c = getBookmarks(Bookmark.CONTENT_URI, projection, selection, null, null, SuggestionLimit);
		
		if(c.moveToFirst()){
			int descColumn = c.getColumnIndex(Bookmark.Description);
			int idColumn = c.getColumnIndex(BaseColumns._ID);
			int urlColumn = c.getColumnIndex(Bookmark.Url);

			do {			
				Uri.Builder data = new Uri.Builder();
				data.scheme(Constants.CONTENT_SCHEME);
				data.encodedAuthority(mAccount.name + "@" + BookmarkContentProvider.AUTHORITY);
				data.appendEncodedPath("bookmarks");
				data.appendEncodedPath(c.getString(idColumn));
				
				String title = c.getString(descColumn);
				
				suggestions.put(title, new SearchSuggestionContent(title, 
					c.getString(urlColumn), R.drawable.ic_main, R.drawable.ic_bookmark, 
					data.build().toString()));
				
			} while(c.moveToNext());	
		}
		c.close();

		return suggestions;
	}
	
	private Map<String, SearchSuggestionContent> getTagSearchSuggestions(String query) {
		Log.d("getTagSearchSuggestions", query);
		
		mAccountManager = AccountManager.get(getContext());
		mAccount = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE)[0];
		
		Map<String, SearchSuggestionContent> suggestions = new TreeMap<String, SearchSuggestionContent>();
		
		// Tag search suggestions
		SQLiteQueryBuilder tagqb = new SQLiteQueryBuilder();	
		tagqb.setTables(TAG_TABLE_NAME);
		
		String selection = Tag.Name + " LIKE '%" + query + "%'";
		
		String[] projection = new String[] {BaseColumns._ID, Tag.Name, Tag.Count};

		Cursor c = getTags(Tag.CONTENT_URI, projection, selection, null, null, SuggestionLimit);
		
		if(c.moveToFirst()){
			int nameColumn = c.getColumnIndex(Tag.Name);
			int countColumn = c.getColumnIndex(Tag.Count);

			do {
				Uri.Builder data = new Uri.Builder();
				data.scheme(Constants.CONTENT_SCHEME);
				data.encodedAuthority(mAccount.name + "@" + BookmarkContentProvider.AUTHORITY);
				data.appendEncodedPath("bookmarks");
				data.appendQueryParameter("tagname", c.getString(nameColumn));
				
				int count = c.getInt(countColumn);
				String name = c.getString(nameColumn);
				
				suggestions.put(name, new SearchSuggestionContent(name, 
					Integer.toString(count) + " bookmark" + (count > 1 ? "s" : ""),
					R.drawable.ic_main, R.drawable.ic_tag, data.build().toString()));
				
			} while(c.moveToNext());	
		}
		c.close();

		return suggestions;
	}
	
	private Cursor getSearchCursor(Map<String, SearchSuggestionContent> list) {
		MatrixCursor mc = new MatrixCursor(new String[] {BaseColumns._ID, 
				SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2, 
				SearchManager.SUGGEST_COLUMN_INTENT_DATA, 
				SearchManager.SUGGEST_COLUMN_ICON_1, SearchManager.SUGGEST_COLUMN_ICON_2});

		int i = 0;
		
		for(SearchSuggestionContent s : list.values()) {
			mc.addRow(new Object[]{ i++, s.getText1(), s.getText2(), s.getIntentData(), 
				s.getIcon1(), s.getIcon2() });
		}
		
		return mc;
	}
	
	private Cursor getTagLiveFolderResults(Uri uri) {
		
		mAccountManager = AccountManager.get(getContext());
		mAccount = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE)[0];
		
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase rdb = dbHelper.getReadableDatabase();
		qb.setTables(TAG_TABLE_NAME);
		
		String[] projection = new String[]{Tag.Name, Tag.Count};
		String selection = Tag.Account + "=?";
		String[] selectionArgs = new String[]{mAccount.name};
		String sortOrder = Tag.Name + " ASC";
		
		Cursor c = qb.query(rdb, projection, selection, selectionArgs, null, null, sortOrder);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		
		MatrixCursor mc = new MatrixCursor(new String[] { BaseColumns._ID, LiveFolders.NAME, 
				LiveFolders.DESCRIPTION, LiveFolders.INTENT});
		
		if(c.moveToFirst()){
			int nameColumn = c.getColumnIndex(Tag.Name);
			int countColumn = c.getColumnIndex(Tag.Count);

			int i = 0;
			
			do {
				String name = c.getString(nameColumn);
				
				Uri.Builder data = new Uri.Builder();
				data.scheme(Constants.CONTENT_SCHEME);
				data.encodedAuthority(mAccount.name + "@" + BookmarkContentProvider.AUTHORITY);
				data.appendEncodedPath("bookmarks");
				data.appendQueryParameter("tagname", name);
				
				mc.addRow(new Object[] {i++, name, 
					Integer.toString(c.getInt(countColumn)) + " bookmark(s)", data.build()});
				
			} while(c.moveToNext());	
		}
		c.close();
		
		
		return mc;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int count;
		switch (sURIMatcher.match(uri)) {
			case Bookmarks:
				count = db.update(BOOKMARK_TABLE_NAME, values, selection, selectionArgs);
				break;
			case Tags:
				count = db.update(TAG_TABLE_NAME, values, selection, selectionArgs);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
	
	private Cursor getBookmarkLiveFolderResults(Uri uri) {
		
		mAccountManager = AccountManager.get(getContext());
		mAccount = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE)[0];
		
		String tagname = uri.getQueryParameter("tagname");
		
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase rdb = dbHelper.getReadableDatabase();
		qb.setTables(BOOKMARK_TABLE_NAME);
		
		String[] projection = new String[]{Bookmark.Description, Bookmark.Url, BaseColumns._ID};
		String selection = "(" + Bookmark.Tags + " LIKE '% " + tagname + " %' OR " +
			Bookmark.Tags + " LIKE '% " + tagname + "' OR " +
			Bookmark.Tags + " LIKE '" + tagname + " %' OR " +
			Bookmark.Tags + " = '" + tagname + "') AND " +
			Bookmark.Account + "=?";
		String[] selectionArgs = new String[]{mAccount.name};
		String sortOrder = Bookmark.Description + " ASC";
		
		Cursor c = qb.query(rdb, projection, selection, selectionArgs, null, null, sortOrder);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		
		MatrixCursor mc = new MatrixCursor(new String[] { BaseColumns._ID, LiveFolders.NAME, 
				LiveFolders.DESCRIPTION, LiveFolders.INTENT});
		
		if(c.moveToFirst()){
			int descColumn = c.getColumnIndex(Bookmark.Description);
			int urlColumn = c.getColumnIndex(Bookmark.Url);
			int idColumn = c.getColumnIndex(BaseColumns._ID);

			int i = 0;
			
			do {
				
				Uri.Builder data = new Uri.Builder();
				data.scheme(Constants.CONTENT_SCHEME);
				data.encodedAuthority(mAccount.name + "@" + BookmarkContentProvider.AUTHORITY);
				data.appendEncodedPath("bookmarks");
				data.appendEncodedPath(Integer.toString(c.getInt(idColumn)));
				
				mc.addRow(new Object[] {i++, c.getString(descColumn), 
					c.getString(urlColumn), data.build()});
				
			} while(c.moveToNext());	
		}
		c.close();
		
		
		return mc;
	}
	
    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher =  new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(AUTHORITY, "bookmark", Bookmarks);
        matcher.addURI(AUTHORITY, "tag", Tags);
        matcher.addURI(AUTHORITY, "main/" + SearchManager.SUGGEST_URI_PATH_QUERY, SearchSuggest);
        matcher.addURI(AUTHORITY, "main/" + SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SearchSuggest);
        matcher.addURI(AUTHORITY, "tag/" + SearchManager.SUGGEST_URI_PATH_QUERY, TagSearchSuggest);
        matcher.addURI(AUTHORITY, "tag/" + SearchManager.SUGGEST_URI_PATH_QUERY + "/*", TagSearchSuggest);
        matcher.addURI(AUTHORITY, "bookmark/" + SearchManager.SUGGEST_URI_PATH_QUERY, BookmarkSearchSuggest);
        matcher.addURI(AUTHORITY, "bookmark/" + SearchManager.SUGGEST_URI_PATH_QUERY + "/*", BookmarkSearchSuggest);
        matcher.addURI(AUTHORITY, "tag/livefolder", TagLiveFolder);
        matcher.addURI(AUTHORITY, "bookmark/livefolder", BookmarkLiveFolder);
        return matcher;
    }
}