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
import android.database.Cursor;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import com.comapping.android.provider.NotificationProvider;

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

        // must read from sql base List of Notifications
        // update:   from SQL base via ContentProvider
        //List<Notification> notifications = AbstractNotificationGenerator.
        //		generateNotificationList(cur, future, day / 2, 0.5);


        // get number of current day and week:
        Calendar calendar = new GregorianCalendar();
        int curDayNumber = calendar.get(Calendar.DAY_OF_YEAR);
        int curWeekNumber = calendar.get(Calendar.WEEK_OF_YEAR);

        // fill tag's title lists:
        Cursor cursor = this.getContentResolver().query(NotificationProvider.
                getNotificationsUri(new Date()), null, null, null, null);
        int dateColumnIndex = cursor.getColumnIndex(NotificationProvider.Column.DATE);
        int titleColumnIndex = cursor.getColumnIndex(NotificationProvider.Column.TITLE);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            Date date = new Date(cursor.getLong(dateColumnIndex));
            // get number of notification's publishing day and week:
            calendar.setTime(date);
            int dayNumber = calendar.get(Calendar.DAY_OF_YEAR);
            int weekNumber = calendar.get(Calendar.WEEK_OF_YEAR);

            String title = cursor.getString(titleColumnIndex);
            Tag.All.titles.add(title);

            if (dayNumber == curDayNumber) {
                Tag.Day.titles.add(title);
            }
            if (weekNumber == curWeekNumber) {
                Tag.Week.titles.add(title);
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

        for (Tag t : Tag.values()) {
            tabHost.addTab(tabHost.newTabSpec(t.name()).setIndicator(t.name())
                    .setContent(this));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(MainActivity.this, BackgroundService.class));
    }

    public View createTabContent(String tag) {
        ListView lv = new ListView(this);
        lv.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                Tag.valueOf(tag).titles));
        return lv;
    }
}
