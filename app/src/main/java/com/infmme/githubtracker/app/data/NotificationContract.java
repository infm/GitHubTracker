package com.infmme.githubtracker.app.data;

import android.provider.BaseColumns;

/**
 * infm created it with love on 4/10/15. Enjoy ;)
 */
public class NotificationContract {
    public static final class UserEntry implements BaseColumns {
        public static final String TABLE_NAME = "userpics";

    }

    public static final class NotificationEntry implements BaseColumns {
        public static final String TABLE_NAME = "notifications";

        public static final String COLUMN_TYPE = "type";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_TIME = "time";
        public static final String COLUMN_USER_ID = "user_id";
        public static final String COLUMN_INFO = "info";
    }
}
