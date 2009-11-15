// Written by Valery Petrov
// Changed by Fedor Burdun (10.11.2009)

package com.comapping.android;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Locale;

import android.app.TabActivity;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TabHost;

public class MainActivity extends TabActivity implements
		TabHost.TabContentFactory {
	private static final String TAG = "MainActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*
		 * (setContentView(R.layout.main); Log.d(TAG, "starting service");
		 * Button bindBtn = (Button) findViewById(R.id.bindBtn);
		 * bindBtn.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View arg0) { startService(new
		 * Intent(MainActivity.this, BackgroundService.class)); } }); Button
		 * unbindBtn = (Button) findViewById(R.id.unbindBtn);
		 * unbindBtn.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View arg0) { stopService(new
		 * Intent(MainActivity.this, BackgroundService.class)); } });
		 */
		Log.d(TAG, "starting service");
		startService(new Intent(MainActivity.this, BackgroundService.class));

		TabHost tabHost = getTabHost();

		tabHost.addTab(tabHost.newTabSpec("Day").setIndicator("Day")
				.setContent(this));
		tabHost.addTab(tabHost.newTabSpec("Week").setIndicator("Week")
				.setContent(this));
		tabHost.addTab(tabHost.newTabSpec("All").setIndicator("All")
				.setContent(this));

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		stopService(new Intent(MainActivity.this, BackgroundService.class));
	}

	@Override
	public View createTabContent(String tag) {
		// TODO Auto-generated method stub
		ListView lv = new ListView(this);
		String myDate = "19.10.10";

		// must read from sql base List of Notifications
		
		ArrayList<Notification> myList = new ArrayList<Notification>();
		ArrayList<String> myStrList = new ArrayList<String>();

		AbstractNotificationGenerator myANG = new AbstractNotificationGenerator();
		myList = myANG.getListNotification(myDate);
		Locale loc = new Locale("ru", "RU");
		NumberFormat nf = NumberFormat.getNumberInstance(loc);
		int curDay = 1;
		try {
			curDay = nf.parse(myDate.substring(0, 2)).intValue();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (tag.equals("Day")) {
			for (Notification notification : myList) {
				if (notification.date.equals(myDate)) {
					myStrList.add(notification.text);
				}
			}
			lv.setAdapter(new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, myStrList));
		} else if (tag.equals("Week")) {
			for (Notification notification : myList) {
				if (notification.date.substring(3).equals("10.10")) {
					int notificationDay = 1;
					try {
						notificationDay = nf.parse(
								notification.date.substring(0, 2)).intValue();
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (notificationDay >= curDay - 7) {
						myStrList.add(notification.text);
					}
				}
			}

			lv.setAdapter(new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, myStrList));
		} else if (tag.equals("All")) {
			for (Notification notification : myList) {
				myStrList.add(notification.text);
			}
			lv.setAdapter(new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, myStrList));
		}

		return lv;
	}
}
