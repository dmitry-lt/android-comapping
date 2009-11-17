// Written by Valery Petrov

/*
 * To-do: write to sql list of notification (while from AbstractGenerator)
 */

package com.comapping.android;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.app.Notification;
import android.app.PendingIntent;

public class BackgroundService extends Service {
	private NotificationManager notificationMgr;

	@Override
	public void onCreate() {
		super.onCreate();
		notificationMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		displayNotificationMessage("creating Background Service");
		
		// new thread for ServiceWorker
		Thread thr = new Thread(null, new ServiceWorker(), "BackgroundService");
		thr.start();
	}

	class ServiceWorker implements Runnable {
		public void run() {
			// do background processing here...
			displayNotificationMessage("running... unbind me to stop!");
			while (true) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
				SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(
						"/data/data/com.comapping.android/databases/notifications.db", null);
				//get last date of updatе
				//check for new notification
				//add new notification in db
				//add new date of update in db
			}
		}
	}

	@Override
	public void onDestroy() {
		displayNotificationMessage("stopping Background Service");
		super.onDestroy();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		displayNotificationMessage("starting Background Service");
		super.onStart(intent, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		displayNotificationMessage("binding Background Service");
		return null;
	}

	// Display notification message using system notifications
	private void displayNotificationMessage(String message) {
		Notification notification = new Notification(R.drawable.note, message,
				System.currentTimeMillis());
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, MainActivity.class), 0);
		notification.setLatestEventInfo(this, "Background Service", message,
				contentIntent);
		notificationMgr.notify(R.string.app_notification_id, notification);
	}
}
