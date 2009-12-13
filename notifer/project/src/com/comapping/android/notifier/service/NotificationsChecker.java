package com.comapping.android.notifier.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.util.Log;
import com.comapping.android.notifier.LocalHistoryViewer;
import com.comapping.android.notifier.R;
import com.comapping.android.notifier.provider.LocalHistoryProvider;
import com.comapping.android.notifier.provider.NotificationProvider;

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
		// TODO use normal "sleep time"
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
			Cursor unread = owner.getContentResolver().query(
					LocalHistoryProvider.CONTENT_URI,
					new String[]{LocalHistoryProvider.Columns._ID},
					LocalHistoryProvider.Columns.READ + "=0",
					null, null);
			Cursor syncDateCursor = owner.getContentResolver().query(
					LocalHistoryProvider.LAST_SYNC_URI,
					null, null, null, null
			);
			ContentValues contentValues = new ContentValues();
			while (true) {
				Log.d(LOG_TAG, "Worker wake up.");
				// setup date after sleeping
				date.setTime(System.currentTimeMillis());
				// getting notifications from server
				// TODO use NotificationProvider.getNotificationUri(date)
				/*Cursor = owner.getContentResolver().query(
						LocalHistoryProvider.LAST_SYNC_URI,
						null, null, null, null
				);*/
				// get time of last synchronization:
				syncDateCursor.requery();
				int lastSyncTimeColumnIndex = syncDateCursor.getColumnIndex(
						LocalHistoryProvider.LAST_SYNC_DATE);
				syncDateCursor.moveToFirst();
				long lastSyncTime = syncDateCursor.getLong(lastSyncTimeColumnIndex);
				// get notifications from server from this time:
				Log.d(LOG_TAG, "Last synchronization time: " + new Date(lastSyncTime));
				Cursor cursor = owner.getContentResolver().query(
						//NotificationProvider.CONTENT_URI,
						NotificationProvider.getNotificationsUri(lastSyncTime),
						null, null, null, null);
				// update time of last synchronization:
				contentValues.put(LocalHistoryProvider.LAST_SYNC_DATE, (new Date()).getTime());
				owner.getContentResolver().update(
						LocalHistoryProvider.LAST_SYNC_URI,
						contentValues, null, null);
				contentValues.clear();


				Log.d(LOG_TAG, "Worker received " + cursor.getCount() + " new notifications");
				// checking if there is some notifications:
				if (cursor.getCount() != 0) {
					// add all notification what we have into database:
					for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
						// fill inserting contentValues:
						int dateColumnIndex = cursor.getColumnIndex(NotificationProvider.Columns.DATE);
						int guidColumnIndex = cursor.getColumnIndex(NotificationProvider.Columns.GUID);

						contentValues.put(LocalHistoryProvider.Columns.DATE,
								cursor.getLong(dateColumnIndex));

						contentValues.put(LocalHistoryProvider.Columns.GUID,
								cursor.getLong(guidColumnIndex));

						// all others columns have "String" type
						String[] columns = {
								LocalHistoryProvider.Columns.TITLE,
								LocalHistoryProvider.Columns.LINK,
								LocalHistoryProvider.Columns.DESCRIPTION,
								LocalHistoryProvider.Columns.CATEGORY,
								LocalHistoryProvider.Columns.AUTHOR
						};

						for (String column : columns) {
							contentValues.put(column,
									cursor.getString(cursor.getColumnIndex(column)));
						}
						// insert contentValues into database
						owner.getContentResolver().insert(
								LocalHistoryProvider.CONTENT_URI, contentValues);
						contentValues.clear();
					}
					// after addition new notifications into database notify user:
					displayNotificationMessage("You have new " + cursor.getCount()
							+ " notification" + ((cursor.getCount() == 1) ? "" : "s") + "!");

					// get count of unread notifications in database:
					unread.requery();
					displayNotificationMessage("You have " + unread.getCount()
							+ " unwatched notification"
							+ ((unread.getCount() == 1) ? "" : "s"));
					Log.d(LOG_TAG, "Worker notified user");
				}
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
