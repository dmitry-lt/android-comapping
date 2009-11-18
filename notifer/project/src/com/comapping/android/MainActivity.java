/*
 * Written by Valery Petrov
 * Changed by Fedor Burdun (10.11.2009)
 * Changed by Eugene Bakisov (18.11.2009)   
 */
package com.comapping.android;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

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
	
	// enumeration of our activity tags:
	public enum Tag {
		Day,
		Week,
		All;
		
		// list of notification's titles for each tag
		public List<String> titles = new ArrayList<String>();
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		long day = 1000 * 60 * 60 * 24; // one day in milliseconds
		Date cur = new Date();
		Date future = new Date();
		future.setTime(cur.getTime() + day * 10);

		// must read from sql base List of Notifications
		// update:   from SQL base via ContentProvider
		List<Notification> notifications = AbstractNotificationGenerator.
				generateNotificationList(cur, future, day / 2, 0.5);
		
		Calendar calendar = new GregorianCalendar();
		int curDayNumber = calendar.get(Calendar.DAY_OF_YEAR);
		int curWeekNumber = calendar.get(Calendar.WEEK_OF_YEAR);
		
		// fill tag's title lists:
		for (Notification n: notifications) {
			calendar.setTime(n.date);
			int nDayNumber = calendar.get(Calendar.DAY_OF_YEAR);
			int nWeekNumber = calendar.get(Calendar.WEEK_OF_YEAR);
			
			Tag.All.titles.add(n.title);
			
			if (nDayNumber == curDayNumber) {
				Tag.Day.titles.add(n.title);
			}
			
			if (nWeekNumber == curWeekNumber) {
				Tag.Week.titles.add(n.title);
			}
		}
		
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
		
		for (Tag t: Tag.values()) {
			tabHost.addTab(tabHost.newTabSpec(t.name()).setIndicator(t.name())
					.setContent(this));	
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopService(new Intent(MainActivity.this, BackgroundService.class));
	}

	@Override
	public View createTabContent(String tag) {
		ListView lv = new ListView(this);
		lv.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, 
				Tag.valueOf(tag).titles));
		return lv;
	}
}
