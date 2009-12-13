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
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
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
		public static final String AUTHOR = "author";
		public static final String GUID = "date";
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
		List<Notification> notifications;
		List<String> segment = uri.getPathSegments();

		switch (uriMatcher.match(uri)) {
			case UriID.NOTIFICATIONS_SET_URI_INDICATOR:
				notifications = getNotifications(null);
				break;
			case UriID.NOTIFICATIONS_SET_AFTER_DATE_URI_INDICATOR:
				notifications = getNotifications(segment.get(1));
				break;
			default:
				throw new IllegalArgumentException("Unknown URI: <" + uri + ">");
		}

		MatrixCursor cursor = new MatrixCursor(defaultProjection);

		int i = 0;
		for (Notification notification : notifications) {
			cursor.addRow(new Object[]{
					i++, // _ID
					notification.getTitle(),
					notification.getLink(),
					notification.getDescription(),
					notification.getCategory(),
					notification.getDate().getTime(),
					notification.getAuthor(),
					notification.getGuid()
			});
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

	private List<Notification> getNotifications(String date) {
		//TODO rewrite work with exceptions!
		if (clientId == null) {
			setupClientId();
		}

		InputStream streamToParse = getRssStream(date);
		try {
			return RssParser.parse(streamToParse);
		} catch (SAXException e) {
			// wrong answer from server.
			// try to get new clientId and repeat:
			setupClientId();
			streamToParse = getRssStream(date);
			try {
				return RssParser.parse(streamToParse);
			} catch (Exception e1) {
				Log.e(LOG_TAG, "Exception: ", e1);
				//TODO show up alert dialog
				throw new RuntimeException(e1);
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, "Exception: ", e);
			//TODO show up alert dialog
			throw new RuntimeException(e);
		}
	}

	private void setupClientId() {
		Cursor cursor = this.getContext().getContentResolver().query(
				Uri.parse("content://www.comapping.com/login"),
				null, null, null, null
		);
		cursor.moveToFirst();
		clientId = cursor.getString(cursor.getColumnIndex("clientId"));
		cursor.close();
	}

	private InputStream getRssStream(String date) {
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

		postParameters.add(new BasicNameValuePair("clientID", clientId));

		try {
			UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(postParameters);
			request.setEntity(formEntity);
			HttpResponse response = client.execute(request);
			return response.getEntity().getContent();
		} catch (IOException e) {
			Log.e(LOG_TAG, "IOException: ", e);
			// TODO show up alert dialog
			throw new RuntimeException(e);
		}
	}

	private static String[] defaultProjection = new String[]{
			Columns._ID, Columns.TITLE, Columns.LINK, Columns.DESCRIPTION,
			Columns.CATEGORY, Columns.DATE, Columns.AUTHOR, Columns.GUID
	};

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