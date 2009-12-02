/*
 * Written by Valery Petrov
 * Changed by Fedor Burdun (10.11.2009)
 * 							(01.12.2009)
 * Changed by Eugene Bakisov (18.11.2009)   
 */
package com.comapping.android;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TabHost;
import com.comapping.android.provider.LocalHistoryProvider;
import com.comapping.android.provider.NotificationProvider;

import java.util.*;

public class LocalHistoryViewer extends TabActivity implements
        TabHost.TabContentFactory {

    private static final String LOG_TAG = "LocalHistoryViewer";

    // enumeration of our activity tags:
    public enum Tag {
        Day, Week, All;

        // list of notification's titles for each tag
        public List<String> titles = null;
        // list of id's for each tag
        public List<Long> id = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get number of current day and week:
        Calendar calendar = new GregorianCalendar();
        int curDayNumber = calendar.get(Calendar.DAY_OF_YEAR);
        int curWeekNumber = calendar.get(Calendar.WEEK_OF_YEAR);

        // fill tag's title lists:
        for (Tag tag : Tag.values()) {
            tag.titles = new ArrayList<String>();
            tag.id = new ArrayList<Long>();
        }

        // delete all in database
        // this.getContentResolver().delete(LocalHistoryProvider.CONTENT_URI,
        // null, null);

        // insert some notification in database
        /*
           * Cursor cursor = this.getContentResolver().query(NotificationProvider.
           * getNotificationsUri(new Date()), null, null, null, null);
           *
           * for (cursor.moveToFirst(); !cursor.isAfterLast();
           * cursor.moveToNext()) { ContentValues contentValues = new
           * ContentValues(); contentValues.put(LocalHistoryProvider.Columns.DATE,
           * cursor.getLong(dateColumnIndex));
           *
           * String[] columns = {LocalHistoryProvider.Columns.TITLE,
           * LocalHistoryProvider.Columns.LINK,
           * LocalHistoryProvider.Columns.DESCRIPTION,
           * LocalHistoryProvider.Columns.CATEGORY};
           *
           * for (String column : columns) { contentValues.put(column,
           * cursor.getString(cursor.getColumnIndex(column))); }
           *
           * this.getContentResolver().insert( LocalHistoryProvider.CONTENT_URI,
           * contentValues); }
           */
        Cursor cursor = this.getContentResolver().query(
                LocalHistoryProvider.CONTENT_URI, null, null, null, null);

        int dateColumnIndex = cursor
                .getColumnIndex(NotificationProvider.Columns.DATE);
        int titleColumnIndex = cursor
                .getColumnIndex(NotificationProvider.Columns.TITLE);
        int idColumnIndex = cursor
                .getColumnIndex(NotificationProvider.Columns._ID);

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            Date date = new Date(cursor.getLong(dateColumnIndex));
            // get number of notification's publishing day and week:
            calendar.setTime(date);
            int dayNumber = calendar.get(Calendar.DAY_OF_YEAR);
            int weekNumber = calendar.get(Calendar.WEEK_OF_YEAR);

            String title = cursor.getString(titleColumnIndex);
            Long id = cursor.getLong(idColumnIndex);
            Tag.All.titles.add(title);
            Tag.All.id.add(id);

            if (dayNumber == curDayNumber) {
                Tag.Day.titles.add(title);
                Tag.Day.id.add(id);
            }
            if (weekNumber == curWeekNumber) {
                Tag.Week.titles.add(title);
                Tag.Week.id.add(id);
            }
        }

        /*
           * (setContentView(R.layout.main); Log.d(TAG, "starting service");
           * Button bindBtn = (Button) findViewById(R.id.bindBtn);
           * bindBtn.setOnClickListener(new OnClickListener() {
           *
           * Intent(LocalHistoryViewer.this, BackgroundService.class)); } });
           * Button unbindBtn = (Button) findViewById(R.id.unbindBtn);
           * unbindBtn.setOnClickListener(new OnClickListener() {
           *
           * @Override public void onClick(View arg0) { stopService(new
           * Intent(LocalHistoryViewer.this, BackgroundService.class)); } });
           */

        /*
           * Do it in MainActivity.java Log.d(TAG, "starting service");
           * startService(new Intent(LocalHistoryViewer.this,
           * BackgroundService.class));
           */
        TabHost tabHost = getTabHost();

        for (Tag t : Tag.values()) {
            tabHost.addTab(tabHost.newTabSpec(t.name()).setIndicator(t.name())
                    .setContent(this));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public View createTabContent(String tag) {
        ListView lv = new ListView(this);
        lv.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, Tag.valueOf(tag).titles));

        final String tagf = tag;
        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                Log.i("ListView onItemClick", Integer.toString(arg2));

                long id = Tag.valueOf(tagf).id.get(arg2);
                Cursor cursor = LocalHistoryViewer.this.getContentResolver()
                        .query(LocalHistoryProvider.getNotificationUri(id),
                                null, null, null, null);

                if (cursor.moveToFirst()) {

                    int categoryColumnIndex = cursor
                            .getColumnIndex(NotificationProvider.Columns.CATEGORY);
                    int dateColumnIndex = cursor
                            .getColumnIndex(NotificationProvider.Columns.DATE);
                    int linkColumnIndex = cursor
                            .getColumnIndex(NotificationProvider.Columns.LINK);
                    int titleColumnIndex = cursor
                            .getColumnIndex(NotificationProvider.Columns.TITLE);
                    int descriptionColumnIndex = cursor
                            .getColumnIndex(NotificationProvider.Columns.DESCRIPTION);

                    Date date = new Date(cursor.getLong(dateColumnIndex));
                    String description = cursor
                            .getString(descriptionColumnIndex);
                    String title = cursor.getString(titleColumnIndex);
                    String category = cursor.getString(categoryColumnIndex);
                    String link = cursor.getString(linkColumnIndex);

                    String message = title + "\n" + category + "\n"
                            + date.toString() + link + "\n" + description;

                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            LocalHistoryViewer.this);
                    builder.setMessage(message).setCancelable(false)
                            .setPositiveButton("Ok",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    Log.i("LocalHistoryView.ListView.onClick",
                            "Can't find notification");
                }
            }
        });

        return lv;
    }
}
