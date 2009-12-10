package com.comapping.android.notifier.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.DateFormat;
import android.util.Log;
import com.comapping.android.notifier.Notification;
import com.comapping.android.notifier.provider.exceptions.NotImplementedException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Eugene Bakisov
 * Date: 21.11.2009
 * Time: 21:18:20
 */
public class NotificationProvider extends ContentProvider {
	public static final String AUTHORITY = "com.comapping.android.notifier.provider.NotificationProvider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/notification");
	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/notification";

	private static String clientId = null;

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
		/*
		if (notifications == null) {
			return cursor;
		}
         */
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
		return getNotifications(null);
	}

	private List<Notification> getNotifications(String date) {
		try {
			HttpClient client = new DefaultHttpClient();

			HttpPost request = new HttpPost("http://go.comapping.com/cgi-bin/comapping.n");

			List<NameValuePair> postParameters = new ArrayList<NameValuePair>();

			postParameters.add(new BasicNameValuePair("action", "get_latest_changes"));

			if (date != null) {
				String fromDate = DateFormat.format(
						"yyyy-MM-dd HH:mm:ss",
						new Long(date)
				).toString();
				postParameters.add(new BasicNameValuePair("fromDate", fromDate));
			}

			if (clientId == null) {
				Cursor cursor = this.getContext().getContentResolver().query(
						Uri.parse("content://www.comapping.com/login"),
						null, null, null, null
				);
				cursor.moveToFirst();
				clientId = cursor.getString(cursor.getColumnIndex("clientId"));
				Log.d(LOG_TAG, "clientId = " + clientId);
			}
			postParameters.add(new BasicNameValuePair("clientID", clientId));

			UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(postParameters);
			request.setEntity(formEntity);

			HttpResponse response = client.execute(request);
			InputStream content = response.getEntity().getContent();

			return RssParser.parse(content);
		} catch (UnsupportedEncodingException e) {
			Log.e(LOG_TAG, "Exception :", e);
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			Log.e(LOG_TAG, "Exception :", e);
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(LOG_TAG, "Exception :", e);
			e.printStackTrace();
		}
		return null;
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