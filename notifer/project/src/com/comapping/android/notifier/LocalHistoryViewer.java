/*
 * Written by Valery Petrov
 * Changed by Fedor Burdun (10.11.2009)
 * 							(01.12.2009)
 * Changed by Eugene Bakisov (18.11.2009)   
 */
package com.comapping.android.notifier;

import android.app.TabActivity;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TabHost;
import com.comapping.android.notifier.provider.LocalHistoryProvider;
import com.comapping.android.notifier.provider.NotificationProvider;

import java.util.Date;
import java.util.List;

public class LocalHistoryViewer extends TabActivity implements
		TabHost.TabContentFactory {

	private static final String LOG_TAG = "LocalHistoryViewer";
	private Cursor cursor;
	private TabHost tabHost;

	// enumeration of our activity tags:
	public enum Tab {
		Day, Week, All;

		// list of notification's titles for each tag
		public List<String> titles = null;
		// list of id's for each tag
		public List<Long> id = null;
		//public TabHost.TabSpec spec = null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		cursor = this.getContentResolver().query(
				LocalHistoryProvider.CONTENT_URI, null, null, null, null
		);
		cursor.setNotificationUri(this.getContentResolver(), LocalHistoryProvider.CONTENT_URI);

		cursor.registerContentObserver(new ContentObserver(new Handler()) {
			@Override
			public void onChange(boolean selfChange) {
				super.onChange(selfChange);
				Log.d(LOG_TAG, "Yay! Changes! oO");
				cursor.requery();
			}
		});

		this.startManagingCursor(cursor);

		tabHost = this.getTabHost();
		for (Tab tab : Tab.values()) {
			TabHost.TabSpec tabSpec = tabHost.newTabSpec(tab.name());
			tabSpec.setIndicator(tab.name(),
					this.getResources().getDrawable(R.drawable.icon));
			tabSpec.setContent(this);
			tabHost.addTab(tabSpec);
		}
		/*
		// get number of current day and week:
		Calendar calendar = new GregorianCalendar();
		int curDayNumber = calendar.get(Calendar.DAY_OF_YEAR);
		int curWeekNumber = calendar.get(Calendar.WEEK_OF_YEAR);

		// fill tag's title lists:
		for (Tab tag : Tab.values()) {
			tag.titles = new ArrayList<String>();
			tag.id = new ArrayList<Long>();
		}

		//Cursor
		cursor = this.getContentResolver().query(
				LocalHistoryProvider.CONTENT_URI, null, null, null, null);
		cursor.setNotificationUri(this.getContentResolver(), LocalHistoryProvider.CONTENT_URI);
		cursor.registerContentObserver(new ContentObserver(new Handler()) {
			@Override
			public void onChange(boolean selfChange) {
				super.onChange(selfChange);
				Log.d(LOG_TAG, "Yay! Changes is coming! oO");
				if (selfChange) {
					Log.d(LOG_TAG, "Some self change");
				}
				//LocalHistoryViewer.this.refresh();
			}
		});

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
			if (cursor.getInt(cursor.getColumnIndex(LocalHistoryProvider.Columns.READ)) != 1) {
				title = "!: " + title;
			}
			Long id = cursor.getLong(idColumnIndex);
			Tab.All.titles.add(title);
			Tab.All.id.add(id);

			if (dayNumber == curDayNumber) {
				Tab.Day.titles.add(title);
				Tab.Day.id.add(id);
			}
			if (weekNumber == curWeekNumber) {
				Tab.Week.titles.add(title);
				Tab.Week.id.add(id);
			}

		}

		TabHost tabHost = getTabHost();

		for (Tab tab : Tab.values()) {
			/// Old version
						   tabHost.addTab(tabHost.newTabSpec(tab.name()).setIndicator(tab.name())
								.setContent(this));
						 //

			//testing with image
			ImageView iv = new ImageView(this);
			iv.setImageResource(R.drawable.note);

			tabHost.addTab(tabHost.newTabSpec(tab.name()).setIndicator(iv)
					.setContent(this));
		}
		*/

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public View createTabContent(String tag) {

		ListView tabListView = new ListView(this);
		String[] from = new String[]{LocalHistoryProvider.Columns.TITLE};
		int[] to = new int[]{R.id.tab_list_element_text};

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(
				this, R.layout.tab_list_element, cursor, from, to);
		/*
		List<ListView.FixedViewInfo> a = new ArrayList<ListView.FixedViewInfo>();
		a.add(tabListView.get)
		List<ListView.FixedViewInfo> b = new ArrayList<ListView.FixedViewInfo>();
		HeaderViewListAdapter wrapedAdapter = new HeaderViewListAdapter(a, b, adapter);
		*/
		tabListView.setAdapter(adapter);
		tabListView.setClickable(true);
		//tabListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

		//return tabListView;
		/*
		ListView lv = new ListView(this);

		lv.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, Tab.valueOf(tag).titles));
		//*/
		final String tagf = tag;
		/*
		lv
		*/
		tabListView.setTextFilterEnabled(true);
		tabListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
									long arg3) {
				Log.i("ListView onItemClick", Integer.toString(arg2));

				long id = Tab.valueOf(tagf).id.get(arg2);
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

					/* Unused code!
					String message = title + "\n" + category + "\n"
							+ date.toString() + link + "\n" + description;

					//Create and initialize SingleNotificationActivity
					SingleNotificationViewer notificationViewer = new SingleNotificationViewer();
					*/
					Bundle extras = new Bundle();
					extras.putLong("position", id);
					Notification.Category categoryValue = Notification.Category.valueOf(category);

					int iconId;
					switch (categoryValue) {
						case Invitation:
							iconId = R.drawable.ic_other;
							break;
						case MapChanges:
							iconId = R.drawable.ic_rewrite;
							break;
						case Tasks:
							iconId = R.drawable.ic_add;
							break;
						case Subscription:
							iconId = R.drawable.ic_other;
							break;
						case Update:
							iconId = R.drawable.ic_rewrite;
							break;
						default:
							throw new IllegalArgumentException();
					}
					extras.putInt("image", iconId);
					//*/
					/*
					if (categoryValue.equals(Notification.Category.Update)) {
						extras.putInt("image", R.drawable.ic_rewrite);
					} else if (categoryValue.equals(Notification.Category.Tasks)) {
						extras.putInt("image", R.drawable.ic_add);
					} else if (categoryValue.equals(Notification.Category.Subscription)) {
						extras.putInt("image", R.drawable.ic_other);
					} else if (categoryValue.equals(Notification.Category.MapChanges)) {
						extras.putInt("image", R.drawable.ic_rewrite);
					} else if (categoryValue.equals(Notification.Category.Invitation)) {
						extras.putInt("image", R.drawable.ic_other);
					} else {
						extras.putInt("image", R.drawable.ic_other);
					}
					*/ //

					extras.putString("description", description);
					extras.putString("date", date.toString());
					extras.putString("link", link);
					extras.putString("category", category);

					Intent intent = new Intent(LocalHistoryViewer.this, SingleNotificationViewer.class);
					intent.putExtras(extras);
					startActivity(intent);

					/* // Creating Dialog
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
										*/

				} else {
					Log.i("LocalHistoryView.ListView.onClick",
							"Can't find notification");
				}
			}
		});

		//return lv;
		return tabListView;
	}
}
