package com.comapping.android.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;
import com.comapping.android.AbstractNotificationGenerator;
import com.comapping.android.Notification;
import com.comapping.android.provider.exceptions.NotImplementedException;


import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Eugene Bakisov
 * Date: 21.11.2009
 * Time: 21:18:20
 */
public class NotificationProvider extends ContentProvider {
    public static final String AUTHORITY = "com.comapping.android.provider.NotificationProvider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/notification");
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/notification";

    public static class Columns implements BaseColumns {
        public static final String TITLE = "title";
        public static final String LINK = "link";
        public static final String DESCRIPTION = "description";
        public static final String CATEGORY = "category";
        public static final String DATE = "date";

    }

    public static Uri getNotificationsUri(Date date) {
        return Uri.parse(CONTENT_URI + "/" + date.getTime());
    }


    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectArgs, String sortOrder) {
        MatrixCursor cursor;
        List<Notification> notifications;
        List<String> segment = uri.getPathSegments();

        switch (uriMatcher.match(uri)) {
            case UriID.NOTIFICATIONS_SET_URI_INDICATOR:
                notifications = getNotifications();
                break;
            case UriID.NOTIFICATIONS_SET_AFTER_DATE_URI_INDICATOR:
                notifications = getNotifications(segment.get(1));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: <" + uri + ">");
        }

        if (projection == null) {
            projection = getDefaultProjection();
        }

        cursor = new MatrixCursor(projection);

        for (int i = 0; i < notifications.size(); i++) {
            Notification cur = notifications.get(i);
            Object[] columnValues = new Object[projection.length];

            // fill columnValues as it described in projection:
            for (int j = 0; j < columnValues.length; j++) {
                if (projection[j].equals(Columns._ID)) {
                    columnValues[j] = i;
                } else if (projection[j].equals(Columns._COUNT)) {
                    // TODO "_COUNT" - ???
                    columnValues[j] = 0;
                } else if (projection[j].equals(Columns.DATE)) {
                    columnValues[j] = cur.date.getTime();
                } else {
                    try {
                        Field column = Notification.class.getField(projection[j]);
                        columnValues[j] = column.get(cur);
                    } catch (NoSuchFieldException e) {
                        Log.e(LOG_TAG, "Wrong query's projection: [" + projection[j] + "]");
                        throw new IllegalArgumentException("Wrong query's projection: [" + projection[j] + "]");
                    } catch (IllegalAccessException e) {
                        Log.e(LOG_TAG, "o_O", e);
                    }
                }
            }

            cursor.addRow(columnValues);
        }

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case UriID.NOTIFICATIONS_SET_URI_INDICATOR:
                return CONTENT_TYPE;
            case UriID.NOTIFICATIONS_SET_AFTER_DATE_URI_INDICATOR:
                return CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI: <" + uri + ">");
        }
    }

    // some kind of "read-only" content:
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        throw new NotImplementedException();
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        throw new NotImplementedException();
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        throw new NotImplementedException();
    }


    private List<Notification> getNotifications() {
        //TODO realize getting notifications list from server
        // here some imitation of work with server now
        return AbstractNotificationGenerator.generateNotificationList(new Date());
    }

    private List<Notification> getNotifications(String date) {
        //TODO realize getting notifications list from server
        // here some imitation of work with server now
        long day = 1000 * 60 * 60 * 24;
        Date future = new Date(Long.valueOf(date));
        Date from = new Date(future.getTime() - day / 2);
        return AbstractNotificationGenerator.generateNotificationList(from, future, day / 8, 0.5);
    }


    private static String[] getDefaultProjection() {
        return new String[]{
                Columns._ID, Columns.TITLE, Columns.LINK,
                Columns.DESCRIPTION, Columns.CATEGORY, Columns.DATE
        };
    }

    private static final String LOG_TAG = "NotificationProvider";

    private static final UriMatcher uriMatcher;

    private static class UriID {
        public static final int NOTIFICATIONS_SET_URI_INDICATOR = 1;
        public static final int NOTIFICATIONS_SET_AFTER_DATE_URI_INDICATOR = 2;
    }

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "notification", UriID.NOTIFICATIONS_SET_URI_INDICATOR);
        uriMatcher.addURI(AUTHORITY, "notification/#", UriID.NOTIFICATIONS_SET_AFTER_DATE_URI_INDICATOR);
    }
}