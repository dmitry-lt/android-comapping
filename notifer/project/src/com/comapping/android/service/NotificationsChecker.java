package com.comapping.android.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.util.Log;
import com.comapping.android.LocalHistoryViewer;
import com.comapping.android.R;
import com.comapping.android.provider.LocalHistoryProvider;
import com.comapping.android.provider.NotificationProvider;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Eugene Bakisov
 * Date: 26.11.2009
 * Time: 14:05:38
 */
public class NotificationsChecker extends Service {
	private NotificationManager notificationManager;
	private CheckerWorker worker;
	private static final String LOG_TAG = "NotificationChecker";

	@Override
	public void onCreate() {
		super.onCreate();
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		worker = new CheckerWorker(this, CheckerWorker.DEFAULT_SLEEP_TIME / 10);
		displayNotificationMessage("Background service created.");
	}

	@Override
	public void onDestroy() {
		displayNotificationMessage("Destroying background service...");
		worker.stop();
		super.onDestroy();
		displayNotificationMessage("Background service destroyed");
	}

	@Override
	public void onStart(Intent intent, int startId) {
		displayNotificationMessage("Starting background service...");
		super.onStart(intent, startId);
		worker.start();
		displayNotificationMessage("Background service started.");
	}

	@Override
	public IBinder onBind(Intent intent) {
		displayNotificationMessage("Binding background service.");
		return null;
	}

	//TODO Rewrite CheckerWorker class using some another work_with_threads_methods
	private class CheckerWorker implements Runnable {
		public static final long DEFAULT_SLEEP_TIME = 5 * 60 * 1000; // 5 minutes in milliseconds

		private Service owner;
		private Thread workThread;
		private long sleepTime;

		public CheckerWorker(Service owner, long sleepTime) {
			this.owner = owner;
			this.workThread = null;
			if (sleepTime > 0) {
				this.sleepTime = sleepTime;
			} else {
				throw new IllegalArgumentException("Sleep time must be positive");
			}
		}

		public CheckerWorker(Service owner) {
			this(owner, DEFAULT_SLEEP_TIME);
		}

		public void start() {
			workThread = new Thread(this);
			workThread.start();
			Log.d(LOG_TAG, "Start work thread");
		}

		public void stop() {
			if (workThread != null) {
				// TODO add checking that Worker isn't busy right now
				workThread.interrupt();
				workThread = null;
				Log.d(LOG_TAG, "Stop work thread");
			}
		}

		@Override
		public void run() {
			Date date = new Date();
			while (true) {
				Log.d(LOG_TAG, "Worker wake up.");
				// setup date after sleeping
				date.setTime(System.currentTimeMillis());
				// getting notifications from server
				Cursor cursor = owner.getContentResolver().query(
						NotificationProvider.getNotificationsUri(date),
						null, null, null, null);
				Log.d(LOG_TAG, "Worker received " + cursor.getCount() + " new notifications");
				// checking if there is some notifications:
				if (cursor.getCount() == 0)
					continue;

				int dateColumnIndex = cursor.getColumnIndex(LocalHistoryProvider.Columns.DATE);

				// add all notification what we have into database:
				for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
					// fill inserting contentValues:
					ContentValues contentValues = new ContentValues();

					// DATE column have "Long" type
					contentValues.put(LocalHistoryProvider.Columns.DATE,
							cursor.getLong(dateColumnIndex));

					// all others columns have "String" type
					String[] columns = {
							LocalHistoryProvider.Columns.TITLE,
							LocalHistoryProvider.Columns.LINK,
							LocalHistoryProvider.Columns.DESCRIPTION,
							LocalHistoryProvider.Columns.CATEGORY
					};

					for (String column : columns) {
						contentValues.put(column,
								cursor.getString(cursor.getColumnIndex(column)));
					}
					// insert contentValues into database
					owner.getContentResolver().insert(
							LocalHistoryProvider.CONTENT_URI, contentValues);
				}
				// after addition new notifications into database notify user:
				displayNotificationMessage("You have new " + cursor.getCount()
						+ " notification" + ((cursor.getCount() == 1) ? "" : "s") + "!");

				// get count of unread notifications in database:
				cursor = owner.getContentResolver().query(
						LocalHistoryProvider.CONTENT_URI,
						new String[]{LocalHistoryProvider.Columns._ID},
						LocalHistoryProvider.Columns.READ + "=0",
						null,
						null);
				displayNotificationMessage("You have " + cursor.getCount()
						+ " unwatched notification"
						+ ((cursor.getCount() == 1) ? "" : "s"));

				Log.d(LOG_TAG, "Worker notified user");

				try {
					Log.d(LOG_TAG, "Worker is going to sleep. Zzzz...");
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					Log.d(LOG_TAG, "Worker was interrupted.");
					return;
				}
			}
		}
	}

	private void displayNotificationMessage(String message) {
		Notification notification = new Notification(R.drawable.note, message,
				System.currentTimeMillis());
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, LocalHistoryViewer.class), 0);
		notification.setLatestEventInfo(this, "Background Service", message,
				contentIntent);
		notificationManager.notify(R.string.app_notification_id, notification);
	}
}
