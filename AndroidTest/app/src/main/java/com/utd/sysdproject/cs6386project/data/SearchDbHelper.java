package com.utd.sysdproject.cs6386project.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.utd.sysdproject.cs6386project.data.SearchContract.MediaEntry;

/**
 * Manages a local database for the media data.
 */
public class SearchDbHelper extends SQLiteOpenHelper {
    private static final String LOG_TAG = SearchDbHelper.class.getSimpleName();

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "media.db";

    public SearchDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d(LOG_TAG, "FUNCTION: onCreate");

        // Create a table to hold locations.  A location consists of the string supplied in the
        // location setting, the city name, and the latitude and longitude
        final String SQL_CREATE_MEDIA_TABLE = "CREATE TABLE " + MediaEntry.TABLE_NAME + " (" +
                MediaEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MediaEntry.COLUMN_MEDIANAME + " TEXT NOT NULL, " +
                MediaEntry.COLUMN_CATEGORY + " TEXT NOT NULL, " +
                MediaEntry.COLUMN_IP + " TEXT NOT NULL, " +
                MediaEntry.COLUMN_PORT + " TEXT NOT NULL " +
                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_MEDIA_TABLE);
    }

    @Override
    public void onOpen(SQLiteDatabase sqLiteDatabase) {
        Log.d(LOG_TAG, "FUNCTION: onOpen");

        // Attempt to drop database if previously created
        final String DROP_SQL = "DROP TABLE IF EXISTS " + MediaEntry.TABLE_NAME + ";";
        sqLiteDatabase.execSQL(DROP_SQL);

        onCreate(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        Log.d(LOG_TAG, "FUNCTION: onUpgrade");

        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MediaEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}