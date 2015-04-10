package com.infmme.githubtracker.app.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

/**
 * infm created it with love on 4/10/15. Enjoy ;)
 */
public class NotificationsContentProvider extends ContentProvider {
    public static final String AUTHORITY = "com.infmme.githubtracker.app.provider";
    public static final String PATH = NotificationContract.NotificationEntry.TABLE_NAME;

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH);
    public static final int URI_NOTIFICATION = 1;
    public static final int URI_NOTIFICATION_ID = 2;
    static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd."
            + AUTHORITY + "." + PATH;
    static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd."
            + AUTHORITY + "." + PATH;

    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, PATH, URI_NOTIFICATION);
        uriMatcher.addURI(AUTHORITY, PATH + "/#", URI_NOTIFICATION_ID);
    }

    private NotificationDbHelper dbHelper;
    private SQLiteDatabase db;

    @Override
    public boolean onCreate() {
        dbHelper = new NotificationDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        switch (uriMatcher.match(uri)) {
            case URI_NOTIFICATION:
                break;
            case URI_NOTIFICATION_ID:
                String rowId = uri.getLastPathSegment();
                selection = updateSingleSelection(selection, rowId);
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }

        db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(NotificationContract.NotificationEntry.TABLE_NAME,
                                 projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case URI_NOTIFICATION:
                return CONTENT_TYPE;
            case URI_NOTIFICATION_ID:
                return CONTENT_ITEM_TYPE;
        }
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (uriMatcher.match(uri) != URI_NOTIFICATION) {
            throw new IllegalArgumentException("Wrong URI: " + uri);
        }

        db = dbHelper.getWritableDatabase();
        long rowId = db.insertWithOnConflict(NotificationContract.NotificationEntry.TABLE_NAME,
                                             null, values, SQLiteDatabase.CONFLICT_REPLACE);
        Uri resultUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
        getContext().getContentResolver().notifyChange(resultUri, null);
        return resultUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
            case URI_NOTIFICATION:
                break;
            case URI_NOTIFICATION_ID:
                String rowId = uri.getLastPathSegment();
                selection = updateSingleSelection(selection, rowId);
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        db = dbHelper.getWritableDatabase();
        int count = db.delete(NotificationContract.NotificationEntry.TABLE_NAME,
                              selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
            case URI_NOTIFICATION:
                break;
            case URI_NOTIFICATION_ID:
                String rowId = uri.getLastPathSegment();
                selection = updateSingleSelection(selection, rowId);
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }

        db = dbHelper.getWritableDatabase();
        int count = db.update(NotificationContract.NotificationEntry.TABLE_NAME,
                              values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    private String updateSingleSelection(String selection, String rowId) {
        if (TextUtils.isEmpty(selection)) {
            selection = NotificationContract.NotificationEntry._ID + " = " + rowId;
        } else {
            selection += " AND " + NotificationContract.NotificationEntry._ID + " = " + rowId;
        }
        return selection;
    }
}
