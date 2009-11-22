package com.comapping.android.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;
import com.comapping.android.AbstractNotificationGenerator;
import com.comapping.android.Notification;
import com.comapping.android.provider.exceptions.NotImplementedException;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Eugene Bakisov
 * Date: 21.11.2009
 * Time: 21:18:20
 * To change this template use File | Settings | File Templates.
 */
public class NotificationProvider extends ContentProvider {
    private static final String LOG_TAG = "NotificationProvider";

    public static final String AUTHORITY = "com.comapping.android.provider.NotificationProvider";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/notification");

    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/notification";
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/notification";

    public static class Column {
        public static final String _ID = "_id";
        public static final String TITLE = "title";
        public static final String LINK = "link";
        public static final String DESCRIPTION = "description";
        public static final String CATEGORY = "category";
        public static final String DATE = "date";
    }

    private static final UriMatcher uriMatcher;

    private static class UriID {
        public static final int NOTIFICATION_SET_URI_INDICATOR = 1;
        public static final int NOTIFICATION_SET_AFTER_DATE_URI_INDICATOR = 2;
        public static final int SINGLE_NOTIFICATION_URI_INDICATOR = 3;
    }

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "notification", UriID.NOTIFICATION_SET_URI_INDICATOR);
        uriMatcher.addURI(AUTHORITY, "notification/#", UriID.NOTIFICATION_SET_AFTER_DATE_URI_INDICATOR);
        uriMatcher.addURI(AUTHORITY, "notification/#/#", UriID.SINGLE_NOTIFICATION_URI_INDICATOR);
    }

    public static Uri getNotificationsUri(Date date) {
        return Uri.parse(CONTENT_URI + "/" + date.getTime()); // cause new Date(String) was deprecated
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
            case UriID.NOTIFICATION_SET_URI_INDICATOR:
                notifications = getNotifications();
                break;
            case UriID.NOTIFICATION_SET_AFTER_DATE_URI_INDICATOR:
                notifications = getNotifications(segment.get(2));
                break;
            case UriID.SINGLE_NOTIFICATION_URI_INDICATOR:
                notifications = getNotification(segment.get(2), segment.get(3));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: <" + uri + ">");
        }

                                        
        if (projection == null) {
            projection = new String[Column.class.getFields().length];
            for (int i = projection.length; i > 0; i--) {
                try {
                    Field columnName = Column.class.getFields()[i - 1];
                    // Field.get(null) - cause all fields in Column class are "static"
                    projection[i - 1] = (String) columnName.get(null);
                } catch (IllegalAccessException e) {
                    Log.d(LOG_TAG, "O_O", e);
                }
            }
        }
        cursor = new MatrixCursor(projection);

        for (int i = 0; i < notifications.size(); i++) {
            Notification cur = notifications.get(i);
            Object[] columnValues = new Object[projection.length];

            // fill columnValues as it described in projection:
            for (int j = 0; j < columnValues.length; j++) {
                // cause we haven't got ID field in Notification class:
                if (projection[j].equals(Column._ID)) {
                    columnValues[j] = i;
                } else {
                    try {
                        Field column = Notification.class.getField(projection[j]);
                        columnValues[j] = column.get(cur);
                    } catch (NoSuchFieldException e) {
                        Log.e(LOG_TAG, "Wrong query's projection: [" + projection[j] + "]");
                        throw new IllegalArgumentException("Wrong query's projection: [" + projection[j] + "]");
                    } catch (IllegalAccessException e) {
                        Log.e(LOG_TAG, "O_O", e);
                    }
                }
            }
            
            cursor.addRow(columnValues);
        }

        return cursor;
    }

    private List<Notification> getNotification(String date, String _id) {
        ArrayList<Notification> result = new ArrayList<Notification>();
        result.add(AbstractNotificationGenerator.generateNotification(new Date(Long.valueOf(date))));
        return result;
    }

    private List<Notification> getNotifications() {
        return AbstractNotificationGenerator.generateNotificationList(new Date());
    }

    private List<Notification> getNotifications(String date) {
        long day = 1000 * 60 * 60 * 24;
        Date from = new Date(Long.valueOf(date));
        return AbstractNotificationGenerator.generateNotificationList(from, new Date(), day / 4, 0.5);
    }


    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case UriID.NOTIFICATION_SET_URI_INDICATOR:
                return CONTENT_TYPE;
            case UriID.NOTIFICATION_SET_AFTER_DATE_URI_INDICATOR:
                return CONTENT_TYPE;
            case UriID.SINGLE_NOTIFICATION_URI_INDICATOR:
                return CONTENT_ITEM_TYPE;
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
}
