package com.pindroid.providers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.pindroid.application.PindroidApplication;
import com.pindroid.util.SyncUtils;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static PindroidApplication app;

    private static final String DATABASE_NAME = "PinboardBookmarks.db";
    private static final int DATABASE_VERSION = 27;
    private static final String BOOKMARK_TABLE_NAME = "bookmark";
    private static final String TAG_TABLE_NAME = "tag";
    private static final String NOTE_TABLE_NAME = "note";

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
