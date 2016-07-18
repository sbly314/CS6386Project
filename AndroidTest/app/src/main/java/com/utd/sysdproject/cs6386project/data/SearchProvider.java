package com.utd.sysdproject.cs6386project.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by Stephen on 7/17/2016.
 */
public class SearchProvider extends ContentProvider {
    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private SearchDbHelper mOpenHelper;

    // Unique integer constant for each type of query
    static final int SEARCH = 100;
    static final int SEARCHWITHMEDIANAME = 101;

    private static final SQLiteQueryBuilder sSearchQueryBuilder = new SQLiteQueryBuilder();

    // select based on medianame
    private static final String sMovieNameSelection =
            SearchContract.MediaEntry.TABLE_NAME +
                    "." + SearchContract.MediaEntry.COLUMN_MEDIANAME + " = ?";

    /* Match each URI to the integer constants defined above */
    static UriMatcher buildUriMatcher() {
        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        final String authority = SearchContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, SearchContract.PATH, SEARCH);
        matcher.addURI(authority, SearchContract.PATH + "/*", SEARCHWITHMEDIANAME);

        return matcher;
    }

    // Create a new SearchDbHelper for later use
    @Override
    public boolean onCreate() {
        mOpenHelper = new SearchDbHelper(getContext());
        return true;
    }

    // getType function that uses UriMatcher
    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // Student: Uncomment and fill out these two cases
            case SEARCH:
                return SearchContract.MediaEntry.CONTENT_TYPE;
            case SEARCHWITHMEDIANAME:
                return SearchContract.MediaEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "search"
            case SEARCH:
            {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        SearchContract.MediaEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "search/*"
            case SEARCHWITHMEDIANAME: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        SearchContract.MediaEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    // Ability to insert
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Uri returnUri;

        long _id = db.insert(SearchContract.MediaEntry.TABLE_NAME, null, values);
        if ( _id > 0 )
            returnUri = SearchContract.MediaEntry.buildSearchUri(_id);
        else
            throw new android.database.SQLException("Failed to insert row into " + uri);

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    // Ability to delete
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";

        rowsDeleted = db.delete(SearchContract.MediaEntry.TABLE_NAME, selection, selectionArgs);

        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    // Ability to update
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsUpdated;

        rowsUpdated = db.update(SearchContract.MediaEntry.TABLE_NAME, values, selection,
                selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    // Ability to bulk insert
    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        db.beginTransaction();
        int returnCount = 0;
        try {
            for (ContentValues value : values) {
                long _id = db.insert(SearchContract.MediaEntry.TABLE_NAME, null, value);
                if (_id != -1) {
                    returnCount++;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnCount;
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
