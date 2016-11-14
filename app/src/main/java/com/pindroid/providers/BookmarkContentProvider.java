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
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import android.accounts.AccountManager;
import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import com.pindroid.Constants;
import com.pindroid.R;
import com.pindroid.application.PindroidApplication;
import com.pindroid.providers.BookmarkContent.Bookmark;
import com.pindroid.providers.NoteContent.Note;
import com.pindroid.providers.TagContent.Tag;
import com.pindroid.util.SyncUtils;

public class BookmarkContentProvider extends ContentProvider {
	
	private static PindroidApplication app;
	
	private SQLiteDatabase db;
	private DatabaseHelper dbHelper;
	private static final String DATABASE_NAME = "PinboardBookmarks.db";
	private static final int DATABASE_VERSION = 27;
	private static final String BOOKMARK_TABLE_NAME = "bookmark";
	private static final String TAG_TABLE_NAME = "tag";
	private static final String NOTE_TABLE_NAME = "note";
	
	private static final int Bookmarks = 1;
	private static final int SearchSuggest = 2;
	private static final int Tags = 3;
	private static final int TagSearchSuggest = 4;
	private static final int BookmarkSearchSuggest = 5;
	private static final int Notes = 6;
	private static final int NoteSearchSuggest = 7;
	private static final int GlobalSearchSuggest = 8;
	private static final int UnreadCount = 9;
	private static final int NoteId = 10;
	private static final int BookmarkId = 11;
	
	
	private static final String SuggestionLimit = "10";
	
	private static final UriMatcher sURIMatcher = buildUriMatcher();
	
	public static final String AUTHORITY = "com.pindroid.providers.BookmarkContentProvider";
	
	public static class DatabaseHelper extends SQLiteOpenHelper {
		
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			app = (PindroidApplication)context;
		}

		@Override
		public void onCreate(SQLiteDatabase sqlDb) {

			sqlDb.execSQL("Create table " + BOOKMARK_TABLE_NAME + 
					" (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
					"ACCOUNT TEXT, " +
					"DESCRIPTION TEXT COLLATE NOCASE, " +
					"URL TEXT COLLATE NOCASE, " +
					"NOTES TEXT, " +
					"TAGS TEXT, " +
					"HASH TEXT, " +
					"META TEXT, " +
					"TIME INTEGER, " +
					"TOREAD INTEGER, " +
					"SHARED INTEGER, " +
					"DELETED INTEGER, " +
					"SYNCED INTEGER);");
			
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
					"NAME TEXT COLLATE NOCASE, " +
					"COUNT INTEGER);");
			
			sqlDb.execSQL("CREATE INDEX " + TAG_TABLE_NAME + 
					"_ACCOUNT ON " + TAG_TABLE_NAME + " " +
					"(ACCOUNT)");
			
			sqlDb.execSQL("Create table " + NOTE_TABLE_NAME + 
					" (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
					"ACCOUNT TEXT, " +
					"TITLE TEXT COLLATE NOCASE, " +
					"TEXT TEXT, " +
					"ADDED INTEGER, " +
					"UPDATED INTEGER, " +
					"HASH TEXT, " +
					"PID TEXT);");
			
			sqlDb.execSQL("CREATE INDEX " + NOTE_TABLE_NAME + 
					"_ACCOUNT ON " + NOTE_TABLE_NAME + " " +
					"(ACCOUNT)");
			
		}

		@Override
		public void onUpgrade(SQLiteDatabase sqlDb, int oldVersion, int newVersion) {
			sqlDb.execSQL("DROP INDEX IF EXISTS " + BOOKMARK_TABLE_NAME + "_ACCOUNT");
			sqlDb.execSQL("DROP INDEX IF EXISTS " + BOOKMARK_TABLE_NAME + "_TAGS");
			sqlDb.execSQL("DROP INDEX IF EXISTS " + BOOKMARK_TABLE_NAME + "_HASH");
			sqlDb.execSQL("DROP INDEX IF EXISTS " + TAG_TABLE_NAME + "_ACCOUNT");
			sqlDb.execSQL("DROP INDEX IF EXISTS " + NOTE_TABLE_NAME + "_ACCOUNT");
			sqlDb.execSQL("DROP TABLE IF EXISTS " + BOOKMARK_TABLE_NAME);
			sqlDb.execSQL("DROP TABLE IF EXISTS " + TAG_TABLE_NAME);
			sqlDb.execSQL("DROP TABLE IF EXISTS " + NOTE_TABLE_NAME);	
			onCreate(sqlDb);
			
			SyncUtils.clearSyncMarkers(app);
		}
	}
	
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int count;
		switch (sURIMatcher.match(uri)) {
			case Bookmarks:
				count = db.delete(BOOKMARK_TABLE_NAME, where, whereArgs);
				getContext().getContentResolver().notifyChange(uri, null, false);
				break;
			case Tags:
				count = db.delete(TAG_TABLE_NAME, where, whereArgs);
				getContext().getContentResolver().notifyChange(uri, null, false);
				break;
			case Notes:
				count = db.delete(NOTE_TABLE_NAME, where, whereArgs);
				getContext().getContentResolver().notifyChange(uri, null, false);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch(sURIMatcher.match(uri)){
			case Bookmarks:
			case BookmarkId:
				return Bookmark.CONTENT_TYPE;
			case SearchSuggest:
				return SearchManager.SUGGEST_MIME_TYPE;
			case Tags:
				return Tag.CONTENT_TYPE;
			case NoteId:
			case Notes:
				return Note.CONTENT_TYPE;
			case UnreadCount:
				return Bookmark.CONTENT_TYPE;
			default:
				throw new IllegalArgumentException("Unknown Uri " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		
		switch(sURIMatcher.match(uri)) {
			case Bookmarks:
				return insertBookmark(uri, values);
			case Tags:
				return insertTag(uri, values);
			case Notes:
				return insertNote(uri, values);
			default:
				throw new IllegalArgumentException("Unknown Uri: " + uri);
		}
	}
	
	private Uri insertBookmark(Uri uri, ContentValues values){
		db = dbHelper.getWritableDatabase();
		long rowId = db.insert(BOOKMARK_TABLE_NAME, "", values);
		if(rowId > 0) {
			Uri rowUri = ContentUris.appendId(BookmarkContent.Bookmark.CONTENT_URI.buildUpon(), rowId).build();
			getContext().getContentResolver().notifyChange(rowUri, null, true);
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
	
	private Uri insertNote(Uri uri, ContentValues values){
		db = dbHelper.getWritableDatabase();
		long rowId = db.insert(NOTE_TABLE_NAME, "", values);
		if(rowId > 0) {
			Uri rowUri = ContentUris.appendId(NoteContent.Note.CONTENT_URI.buildUpon(), rowId).build();
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
		Log.d("BookmarkContentProvider Query", uri.toString());
		
		switch(sURIMatcher.match(uri)) {
			case Bookmarks:
				return getBookmarks(uri, projection, selection, selectionArgs, sortOrder);
			case BookmarkId:
				return getBookmark(uri, projection, selection, selectionArgs, sortOrder);
			case GlobalSearchSuggest:
				String globalQquery = uri.getLastPathSegment().toLowerCase(Locale.ENGLISH);
				return getSearchSuggestions(globalQquery, false);
			case SearchSuggest:
				String query = uri.getLastPathSegment().toLowerCase(Locale.ENGLISH);
				return getSearchSuggestions(query, true);
			case Tags:
				return getTags(uri, projection, selection, selectionArgs, sortOrder);
			case TagSearchSuggest:
				String tagQuery = uri.getLastPathSegment().toLowerCase(Locale.ENGLISH);
				return getSearchCursor(getTagSearchSuggestions(tagQuery, true));
			case BookmarkSearchSuggest:
				String bookmarkQuery = uri.getLastPathSegment().toLowerCase(Locale.ENGLISH);
				return getSearchCursor(getBookmarkSearchSuggestions(bookmarkQuery, true));
			case Notes:
				return getNotes(uri, projection, selection, selectionArgs, sortOrder);
			case NoteId:
				return getNote(uri, projection, selection, selectionArgs, sortOrder);
			case NoteSearchSuggest:
				String noteQuery = uri.getLastPathSegment().toLowerCase(Locale.ENGLISH);
				return getSearchCursor(getNoteSearchSuggestions(noteQuery, true));
			case UnreadCount:
				SQLiteDatabase rdb = dbHelper.getReadableDatabase();
				return rdb.rawQuery("select count(*) as Count, ACCOUNT as Account from " + BOOKMARK_TABLE_NAME + " where " + Bookmark.ToRead + "=1 group by " + Bookmark.Account, null);
			default:
				throw new IllegalArgumentException("Unknown Uri: " + uri);
		}
	}
	
	private int getAccountCount() {
		return AccountManager.get(app).getAccountsByType(Constants.ACCOUNT_TYPE).length;
	}
	
	private Cursor getBookmark(Uri uri, String[] projection, String selection,	String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase rdb = dbHelper.getReadableDatabase();
		qb.setTables(BOOKMARK_TABLE_NAME);
		qb.appendWhere(Bookmark._ID + "=" + uri.getPathSegments().get(Bookmark.BOOKMARK_ID_PATH_POSITION));
		Cursor c = qb.query(rdb, projection, selection, selectionArgs, null, null, sortOrder, null);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
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
	
	private Cursor getNote(Uri uri, String[] projection, String selection,	String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase rdb = dbHelper.getReadableDatabase();
		qb.setTables(NOTE_TABLE_NAME);
		qb.appendWhere(Note._ID + "=" + uri.getPathSegments().get(Note.NOTE_ID_PATH_POSITION));
		Cursor c = qb.query(rdb, projection, selection, selectionArgs, null, null, sortOrder, null);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}
	
	private Cursor getNotes(Uri uri, String[] projection, String selection,	String[] selectionArgs, String sortOrder) {
		return getNotes(uri, projection, selection, selectionArgs, sortOrder, null);
	}
	
	private Cursor getNotes(Uri uri, String[] projection, String selection,	String[] selectionArgs, String sortOrder, String limit) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		SQLiteDatabase rdb = dbHelper.getReadableDatabase();
		qb.setTables(NOTE_TABLE_NAME);
		Cursor c = qb.query(rdb, projection, selection, selectionArgs, null, null, sortOrder, limit);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}
	
	private Cursor getSearchSuggestions(String query, boolean accountSpecific) {
		Log.d("getSearchSuggestions", query);
		
		Map<String, SearchSuggestionContent> tagSuggestions = new TreeMap<String, SearchSuggestionContent>();
		Map<String, SearchSuggestionContent> bookmarkSuggestions = new TreeMap<String, SearchSuggestionContent>();
		Map<String, SearchSuggestionContent> noteSuggestions = new TreeMap<String, SearchSuggestionContent>();
			
		tagSuggestions = getTagSearchSuggestions(query, accountSpecific);
		bookmarkSuggestions = getBookmarkSearchSuggestions(query, accountSpecific);
		noteSuggestions = getNoteSearchSuggestions(query, accountSpecific);
	
		SortedMap<String, SearchSuggestionContent> s = new TreeMap<String, SearchSuggestionContent>();
		s.putAll(tagSuggestions);
		s.putAll(bookmarkSuggestions);
		s.putAll(noteSuggestions);
		
		return getSearchCursor(s);
	}
	
	private Map<String, SearchSuggestionContent> getBookmarkSearchSuggestions(String query, boolean accountSpecific) {
		Log.d("getBookmarkSearchSuggestions", query);
		
		String[] bookmarks = query.split(" ");
		
		Map<String, SearchSuggestionContent> suggestions = new TreeMap<String, SearchSuggestionContent>();
				
		// Title/description/notes search suggestions
		SQLiteQueryBuilder bookmarkqb = new SQLiteQueryBuilder();	
		bookmarkqb.setTables(BOOKMARK_TABLE_NAME);
		
		ArrayList<String> bookmarkList = new ArrayList<String>();
		final ArrayList<String> selectionlist = new ArrayList<String>();
		
		for(String s : bookmarks) {
			bookmarkList.add("(" + Bookmark.Description + " LIKE ? OR " + 
					Bookmark.Notes + " LIKE ?)");
			
			selectionlist.add("%" + s + "%");
			selectionlist.add("%" + s + "%");
			
			if(accountSpecific){
				bookmarkList.add(Bookmark.Account + "=?");
				selectionlist.add(app.getUsername());
			}
		}
		
		String selection = TextUtils.join(" AND ", bookmarkList);
		
		String[] projection = new String[] {BaseColumns._ID, Bookmark.Description, Bookmark.Url, Bookmark.Account};

		Cursor c = getBookmarks(Bookmark.CONTENT_URI, projection, selection, selectionlist.toArray(new String[]{}), null, SuggestionLimit);
		
		if(c.moveToFirst()){
			int descColumn = c.getColumnIndex(Bookmark.Description);
			int idColumn = c.getColumnIndex(BaseColumns._ID);
			int urlColumn = c.getColumnIndex(Bookmark.Url);
			int accountColumn = c.getColumnIndex(Bookmark.Account);
			
			int accountCount = getAccountCount();

			do {
				String account = c.getString(accountColumn);
		    	
				Uri data;
		    	Uri.Builder builder = new Uri.Builder();
		    	
		    	String action = Constants.ACTION_SEARCH_SUGGESTION_VIEW;
		    	

	    		builder.scheme(Constants.CONTENT_SCHEME);
	    		builder.encodedAuthority(account + "@" + Constants.INTENT_URI);
	    		builder.appendEncodedPath("bookmarks");
	    		builder.appendEncodedPath(c.getString(idColumn));
	    		data = builder.build();
				
				String title = c.getString(descColumn);
				String line2 = c.getString(urlColumn);
				String url = line2;
				
				if(!accountSpecific && accountCount > 1) {
					line2 = account;
					url = null;
				}
				
				suggestions.put(title + "_bookmark_" + account, new SearchSuggestionContent(title, 
					line2, url, R.drawable.ic_bookmark_blue_24dp,	data.toString(), action));
				
			} while(c.moveToNext());	
		}
		c.close();

		return suggestions;
	}
	
	private Map<String, SearchSuggestionContent> getTagSearchSuggestions(String query, boolean accountSpecific) {
		Log.d("getTagSearchSuggestions", query);
		
		Resources res = getContext().getResources();
		
		String[] tags = query.split(" ");
		
		Map<String, SearchSuggestionContent> suggestions = new TreeMap<String, SearchSuggestionContent>();
		
		// Tag search suggestions
		SQLiteQueryBuilder tagqb = new SQLiteQueryBuilder();	
		tagqb.setTables(TAG_TABLE_NAME);
		
		ArrayList<String> tagList = new ArrayList<String>();
		final ArrayList<String> selectionlist = new ArrayList<String>();
		
		for(String s : tags){
			tagList.add(Tag.Name + " LIKE ?");
			selectionlist.add("%" + s + "%");
			
			if(accountSpecific){
				tagList.add(Bookmark.Account + "=?");
				selectionlist.add(app.getUsername());
			}
		}
		
		String selection = TextUtils.join(" AND ", tagList);

		String[] projection = new String[] {BaseColumns._ID, Tag.Name, Tag.Count, Tag.Account};

		Cursor c = getTags(Tag.CONTENT_URI, projection, selection, selectionlist.toArray(new String[]{}), null, SuggestionLimit);
		
		if(c.moveToFirst()){
			int nameColumn = c.getColumnIndex(Tag.Name);
			int countColumn = c.getColumnIndex(Tag.Count);
			int accountColumn = c.getColumnIndex(Tag.Account);
			
			int accountCount = getAccountCount();

			do {
				String account = c.getString(accountColumn);
				int count = c.getInt(countColumn);
				String name = c.getString(nameColumn);
				
				Uri.Builder data = new Uri.Builder();
				data.scheme(Constants.CONTENT_SCHEME);
				data.encodedAuthority(account + "@" + Constants.INTENT_URI);
				data.appendEncodedPath("bookmarks");
				data.appendQueryParameter("tagname", name);
				
				String tagCount = Integer.toString(count) + " " + res.getString(R.string.bookmark_count);
				
				if(!accountSpecific && accountCount > 1)
					tagCount = account;
				
				suggestions.put(name + "_tag_" + account, new SearchSuggestionContent(name, 
					tagCount, R.drawable.ic_label_gray_24dp, data.build().toString(), Constants.ACTION_SEARCH_SUGGESTION_VIEW));
				
			} while(c.moveToNext());	
		}
		c.close();

		return suggestions;
	}
	
	private Map<String, SearchSuggestionContent> getNoteSearchSuggestions(String query, boolean accountSpecific) {
		Log.d("getNoteSearchSuggestions", query);
		
		String[] notes = query.split(" ");
		
		Map<String, SearchSuggestionContent> suggestions = new TreeMap<String, SearchSuggestionContent>();
		
		// Tag search suggestions
		SQLiteQueryBuilder noteqb = new SQLiteQueryBuilder();	
		noteqb.setTables(NOTE_TABLE_NAME);
		
		ArrayList<String> noteList = new ArrayList<String>();
		final ArrayList<String> selectionlist = new ArrayList<String>();
		
		for(String s : notes) {
			noteList.add("(" + Note.Title + " LIKE ? OR " + 
					Note.Text + " LIKE ?)");
			selectionlist.add("%" + s + "%");
			selectionlist.add("%" + s + "%");
			
			if(accountSpecific){
				noteList.add(Bookmark.Account + "=?");
				selectionlist.add(app.getUsername());
			}
		}
		
		String selection = TextUtils.join(" AND ", noteList);

		String[] projection = new String[] {BaseColumns._ID, Note.Title, Note.Text, Note.Account};

		Cursor c = getNotes(Tag.CONTENT_URI, projection, selection, selectionlist.toArray(new String[]{}), null, SuggestionLimit);
		
		if(c.moveToFirst()){
			int titleColumn = c.getColumnIndex(Note.Title);
			int textColumn = c.getColumnIndex(Note.Text);
			int idColumn = c.getColumnIndex(BaseColumns._ID);
			int accountColumn = c.getColumnIndex(Tag.Account);
			
			int accountCount = getAccountCount();

			do {
				String title = c.getString(titleColumn);
				String text = c.getString(textColumn);
				String account = c.getString(accountColumn);
				
				Uri data;
				Uri.Builder builder = new Uri.Builder();
				builder.scheme(Constants.CONTENT_SCHEME);
				builder.encodedAuthority(account + "@" + Constants.INTENT_URI);
				builder.appendEncodedPath("notes");
				builder.appendEncodedPath(c.getString(idColumn));
	    		data = builder.build();
				
				if(!accountSpecific && accountCount > 1)
					text = account;
				
				suggestions.put(title + "_note_" + account, new SearchSuggestionContent(title, 
					text, R.drawable.ic_note_brown_24dp, data.toString(), Constants.ACTION_SEARCH_SUGGESTION_VIEW));
				
			} while(c.moveToNext());	
		}
		c.close();

		return suggestions;
	}
	
	private Cursor getSearchCursor(Map<String, SearchSuggestionContent> list) {
    	SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getContext());
    	Boolean icons = settings.getBoolean("pref_searchicons", true);

    	MatrixCursor mc;
    	
    	if(icons) {
			mc = new MatrixCursor(new String[] {BaseColumns._ID, 
					SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2, SearchManager.SUGGEST_COLUMN_TEXT_2_URL,
					SearchManager.SUGGEST_COLUMN_INTENT_DATA, SearchManager.SUGGEST_COLUMN_INTENT_ACTION,
					SearchManager.SUGGEST_COLUMN_ICON_2});
	
			int i = 0;
			
			for(SearchSuggestionContent s : list.values()) {
				mc.addRow(new Object[]{ i++, s.getText1(), s.getText2(), s.getText2Url(), s.getIntentData(), s.getIntentAction(),
					s.getIcon2() });
			}
    	} else {
			mc = new MatrixCursor(new String[] {BaseColumns._ID, 
					SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2, SearchManager.SUGGEST_COLUMN_TEXT_2_URL,
					SearchManager.SUGGEST_COLUMN_INTENT_DATA, SearchManager.SUGGEST_COLUMN_INTENT_ACTION});
	
			int i = 0;
			
			for(SearchSuggestionContent s : list.values()) {
				mc.addRow(new Object[]{ i++, s.getText1(), s.getText2(), s.getText2Url(), s.getIntentData(), s.getIntentAction() });
			}
    	}
		
		return mc;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		int count;
		switch (sURIMatcher.match(uri)) {
			case Bookmarks:
			case BookmarkId:
				count = db.update(BOOKMARK_TABLE_NAME, values, selection, selectionArgs);
				break;
			case Tags:
				count = db.update(TAG_TABLE_NAME, values, selection, selectionArgs);
				break;
			case Notes:
				count = db.update(NOTE_TABLE_NAME, values, selection, selectionArgs);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		boolean syncOnly = values.size() == 1 && values.containsKey(Bookmark.Synced) && values.getAsInteger(Bookmark.Synced) == 1;
		
		getContext().getContentResolver().notifyChange(uri, null, !syncOnly);
		return count;
	}
	
	public DatabaseHelper getDatabaseHelper(){
		return dbHelper;
	}
	
	@Override
	public int bulkInsert(Uri uri, ContentValues[] values){
		
		int result = 0;
		
		switch(sURIMatcher.match(uri)) {
			case Bookmarks:
				result = bulkLoad(BOOKMARK_TABLE_NAME, values);
				break;
			case Tags:
				result = bulkLoad(TAG_TABLE_NAME, values);
				break;
			case Notes:
				result = bulkLoad(NOTE_TABLE_NAME, values);
				break;
			default:
				throw new IllegalArgumentException("Unknown Uri: " + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null, false);
		
		return result;
	}
	
	private int bulkLoad(String table, ContentValues[] values){
		db = dbHelper.getWritableDatabase();
		int inserted = 0;
		
		db.beginTransaction();
		
		try{
			for(ContentValues v : values) {
				db.insert(table, null, v);
			}
			
			db.setTransactionSuccessful();
			inserted = values.length;
		}
		finally{
			db.endTransaction();
		}

		return inserted;
	}
	
    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher =  new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(AUTHORITY, "bookmark", Bookmarks);
        matcher.addURI(AUTHORITY, "bookmark/#", BookmarkId);
        matcher.addURI(AUTHORITY, "tag", Tags);
        matcher.addURI(AUTHORITY, "note", Notes);
        matcher.addURI(AUTHORITY, "note/#", NoteId);
        matcher.addURI(AUTHORITY, "unreadcount", UnreadCount);
        matcher.addURI(AUTHORITY, "global/" + SearchManager.SUGGEST_URI_PATH_QUERY, GlobalSearchSuggest);
        matcher.addURI(AUTHORITY, "global/" + SearchManager.SUGGEST_URI_PATH_QUERY + "/*", GlobalSearchSuggest);
        matcher.addURI(AUTHORITY, "main/" + SearchManager.SUGGEST_URI_PATH_QUERY, SearchSuggest);
        matcher.addURI(AUTHORITY, "main/" + SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SearchSuggest);
        matcher.addURI(AUTHORITY, "tag/" + SearchManager.SUGGEST_URI_PATH_QUERY, TagSearchSuggest);
        matcher.addURI(AUTHORITY, "tag/" + SearchManager.SUGGEST_URI_PATH_QUERY + "/*", TagSearchSuggest);
        matcher.addURI(AUTHORITY, "bookmark/" + SearchManager.SUGGEST_URI_PATH_QUERY, BookmarkSearchSuggest);
        matcher.addURI(AUTHORITY, "bookmark/" + SearchManager.SUGGEST_URI_PATH_QUERY + "/*", BookmarkSearchSuggest);
        matcher.addURI(AUTHORITY, "note/" + SearchManager.SUGGEST_URI_PATH_QUERY, NoteSearchSuggest);
        matcher.addURI(AUTHORITY, "note/" + SearchManager.SUGGEST_URI_PATH_QUERY + "/*", NoteSearchSuggest);
        return matcher;
    }

}