// Written by Valery Petrov

package com.comapping.android.test;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
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