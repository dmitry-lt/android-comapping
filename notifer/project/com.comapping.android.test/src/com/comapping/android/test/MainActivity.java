// Written by Valery Petrov
// Changed by Fedor Burdun (10.11.2009)

package com.comapping.android.test;

import android.app.TabActivity;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TabHost;

public class MainActivity  extends TabActivity implements TabHost.TabContentFactory {
	private static final String TAG = "MainActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*
		(setContentView(R.layout.main);
		Log.d(TAG, "starting service");
		Button bindBtn = (Button) findViewById(R.id.bindBtn);
		bindBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				startService(new Intent(MainActivity.this,
						BackgroundService.class));
			}
		});
		Button unbindBtn = (Button) findViewById(R.id.unbindBtn);
		unbindBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				stopService(new Intent(MainActivity.this,
						BackgroundService.class));
			}
		});
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
		
		if (tag.equals("Day"))
		lv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
				new AbstractNotificationGenerator().getListNotification(5)));
		else if (tag.equals("Week"))
				lv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
						new AbstractNotificationGenerator().getListNotification(10)));
		else if (tag.equals("All"))
			lv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
					new AbstractNotificationGenerator().getListNotification(20)));

		return lv;
	}

}