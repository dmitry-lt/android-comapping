package com.comapping.android.widget;

import com.comapping.android.service.NotificationsChecker;
import com.comapping.android.LocalHistoryViewer;
import com.comapping.android.R;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/*
 * author Yan Lobkarev
 * It is a extension of AppWidgetProvider
 * that start a service
 */
public class NotifierWidgetProvider extends AppWidgetProvider {

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

		// add the Activity pending intent on the widget's background
		onClickHandler(context);

		// start the service
		context.startService(new Intent(context, NotificationsChecker.class));
	}

	public void onClickHandler(Context context) {
		RemoteViews updateViews = null;
		updateViews = new RemoteViews(context.getPackageName(),
				R.layout.notifier_widget_layout);

		Intent launchIntent = new Intent(context, LocalHistoryViewer.class);
		PendingIntent backgroundIntent = PendingIntent.getActivity(context, 0,
				launchIntent, 0);
		updateViews.setOnClickPendingIntent(R.id.widget, backgroundIntent);

		// Push update for this widget to the home screen
		ComponentName thisWidget = new ComponentName(context,
				NotifierWidgetProvider.class);
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		manager.updateAppWidget(thisWidget, updateViews);
	}

}
