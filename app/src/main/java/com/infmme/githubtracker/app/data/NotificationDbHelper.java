package com.infmme.githubtracker.app.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.infmme.githubtracker.app.data.NotificationContract.*;

/**
 * infm created it with love on 4/10/15. Enjoy ;)
 */
public class NotificationDbHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "notifications.db";

    public NotificationDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String create_statement =
                "CREATE TABLE " + NotificationEntry.TABLE_NAME + " (" +
                NotificationEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                NotificationEntry.COLUMN_TYPE + " TEXT NOT NULL, " +
                NotificationEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                NotificationEntry.COLUMN_TIME + " TEXT NOT NULL," +
                NotificationEntry.COLUMN_USER_ID + " INTEGER NOT NULL, " +
                NotificationEntry.COLUMN_INFO + " TEXT NOT NULL," +

                // Set up the location column as a foreign key to location table.
                " FOREIGN KEY (" + NotificationEntry.COLUMN_USER_ID + ") REFERENCES " +
                UserEntry.TABLE_NAME + " (" + UserEntry._ID + "));";

        db.execSQL(create_statement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + NotificationEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + UserEntry.TABLE_NAME);
        onCreate(db);
    }
}
