package com.comapping.android.provider;

import android.content.*;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;
import com.comapping.android.Notification;
import com.comapping.android.provider.exceptions.NotImplementedException;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Eugene Bakisov
 * Date: 23.11.2009
 * Time: 10:56:56
 */
public class LocalHistoryProvider extends ContentProvider {
    public static final String DATABASE_NAME = "comappingLocalHistory.db";
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "localHistory";
    public static final String DEFAULT_SORT_ORDER = "_ID DESC";

    public static final String AUTHORITY = "com.comapping.android.provider.LocalHistoryProvider";
    public final static Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/history");
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/notification";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/notification";

    public static class Columns implements BaseColumns {
        public static final String TITLE = "title";
        public static final String LINK = "link";
        public static final String DESCRIPTION = "description";
        public static final String CATEGORY = "category";
        public static final String DATE = "date";
        //TODO add column "READ"
    }

    public static Uri getNotificationUri(int id) {
        return Uri.parse(CONTENT_URI + "/" + id);
    }

    public static Date getModifiedDate() {
        return modifiedDate;
    }

    @Override
    public boolean onCreate() {
        openHelper = new DatabaseHelper(this.getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (uriMatcher.match(uri)) {
            case UriID.NOTIFICATIONS_SET_URI_INDICATOR:
                qb.setTables(TABLE_NAME);
                break;
            case UriID.SINGLE_NOTIFICATION_URI_INDICATOR:
                qb.setTables(TABLE_NAME);
                qb.appendWhere(Columns._ID + "=" + uri.getPathSegments().get(1));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: <" + uri + ">");
        }

        String order;
        if (TextUtils.isEmpty(sortOrder)) {
            order = DEFAULT_SORT_ORDER;
        } else {
            order = sortOrder;
        }

        if (projection == null) {
            projection = getDefaultProjection();
        }

        SQLiteDatabase db = openHelper.getReadableDatabase();
        Cursor cursor = qb.query(db, projection, selection, selectionArgs, null, null, order);
        cursor.setNotificationUri(this.getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case UriID.NOTIFICATIONS_SET_URI_INDICATOR:
                return CONTENT_TYPE;
            case UriID.SINGLE_NOTIFICATION_URI_INDICATOR:
                return CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI: <" + uri + ">");
        }
    }

    // TODO rewrite insert method
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        if (uriMatcher.match(uri) != UriID.NOTIFICATIONS_SET_URI_INDICATOR) {
            throw new IllegalArgumentException("Unknown URI <" + uri + ">");
        }

        // contentValues validation:
        String[] columns = getDefaultProjection();
        for (int i = 1; i < columns.length; i++) {
            if (!contentValues.containsKey(columns[i])) {
                throw new SQLException("Failed to insert row into [" + uri
                        + "]: haven't got value of column \"" + columns[i] + "\"");
            }
        }

        if (!(contentValues.get(Columns.DATE) instanceof Long)) {
            throw new SQLException("Failed to insert row into ["
                    + uri + "]: field \"date\" must contain long value");
        }

        try {
            Notification.Category.valueOf((String) contentValues.get(Columns.CATEGORY));
        } catch (IllegalArgumentException e) {
            throw new SQLException("Failed to insert row into ["
                    + uri + "]: field \"category\" must contain valid category name");
        }
        /*
        if (!contentValues.containsKey(Columns.TITLE)) {
            throw new SQLException("Failed to insert row into ["
                    + uri + "] because field \"TITLE\" is needed");
        }
        if (!contentValues.containsKey(Columns.LINK)) {
            throw new SQLException("Failed to insert row into ["
                    + uri + "] because field \"LINK\" is needed");
        }
        if (!contentValues.containsKey(Columns.DESCRIPTION)) {
            throw new SQLException("Failed to insert row into ["
                    + uri + "] because field \"DESCRIPTION\" is needed");
        }
        if (!contentValues.containsKey(Columns.CATEGORY)) {
            throw new SQLException("Failed to insert row into ["
                    + uri + "] because field \"CATEGORY\" is needed");
        } else {
            try {
                Notification.Category.valueOf((String) contentValues.get(Columns.CATEGORY));
            } catch (IllegalArgumentException e) {
                throw new SQLException("Failed to insert row into ["
                        + uri + "] because field \"CATEGORY\" must contain category name");
            }
        }
        if (!contentValues.containsKey(Columns.DATE)) {
            throw new SQLException("Failed to insert row into ["
                    + uri + "] because field \"DATE\" is needed");
        } else {
            if (!(contentValues.get(Columns.DATE) instanceof Long)) {
                throw new SQLException("Failed to insert row into ["
                        + uri + "] because field \"DATE\" must contain long value");
            }
        }
        */
        SQLiteDatabase db = openHelper.getReadableDatabase();
        long rowId = db.insert(TABLE_NAME, Columns.TITLE, contentValues);
        if (rowId > 0) {
            modifiedDate.setTime(System.currentTimeMillis());
            Uri insertedNotificationUri = ContentUris.withAppendedId(CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(insertedNotificationUri, null);
            return insertedNotificationUri;
        }

        throw new SQLException("Failed to insert row into [" + uri + "]");
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = openHelper.getWritableDatabase();
        int count;
        switch (uriMatcher.match(uri)) {
            case UriID.NOTIFICATIONS_SET_URI_INDICATOR:
                count = db.delete(TABLE_NAME, where, whereArgs);
                break;
            case UriID.SINGLE_NOTIFICATION_URI_INDICATOR:
                String rowId = uri.getPathSegments().get(1);
                count = db.delete(TABLE_NAME
                        , Columns._ID + "=" + rowId
                                + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : "")
                        , whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String where,
                      String[] whereArgs) {
        SQLiteDatabase db = openHelper.getWritableDatabase();
        int count;
        switch (uriMatcher.match(uri)) {
            case UriID.NOTIFICATIONS_SET_URI_INDICATOR:
                count = db.update(TABLE_NAME,
                        contentValues, where, whereArgs);
                break;
            case UriID.SINGLE_NOTIFICATION_URI_INDICATOR:
                String rowId = uri.getPathSegments().get(1);
                count = db.update(TABLE_NAME
                        , contentValues
                        , Columns._ID + "=" + rowId
                                + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : "")
                        , whereArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }


    private final static String LOG_TAG = "LocalHistoryProvider";
    // TODO add reading modifiedDate from somewhere
    private static Date modifiedDate = new Date();

    private static String[] getDefaultProjection() {
        return new String[]{
                Columns._ID, Columns.TITLE, Columns.LINK,
                Columns.DESCRIPTION, Columns.CATEGORY, Columns.DATE
        };
    }

    private class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context) {
            super(context, TABLE_NAME, null,
                    DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase database) {
            database.execSQL("CREATE TABLE " + TABLE_NAME + " ("
                    + Columns._ID + " INTEGER PRIMARY KEY,"
                    + Columns.TITLE + " TEXT,"
                    + Columns.LINK + " TEXT,"
                    + Columns.DESCRIPTION + " TEXT,"
                    + Columns.CATEGORY + " TEXT,"
                    + Columns.DATE + " INTEGER"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
            Log.w(LOG_TAG, "Updating database from version " + oldVersion
                    + " to " + newVersion + ", which will destroy all old data");
            database.execSQL("DROP TABLE IF EXISTS " + LocalHistoryProvider.TABLE_NAME);
            onCreate(database);
        }
    }

    private DatabaseHelper openHelper;

    private static UriMatcher uriMatcher;

    private static class UriID {
        public static final int NOTIFICATIONS_SET_URI_INDICATOR = 1;
        public static final int SINGLE_NOTIFICATION_URI_INDICATOR = 3;
    }

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "history", UriID.NOTIFICATIONS_SET_URI_INDICATOR);
        uriMatcher.addURI(AUTHORITY, "history/#", UriID.SINGLE_NOTIFICATION_URI_INDICATOR);
    }

}
