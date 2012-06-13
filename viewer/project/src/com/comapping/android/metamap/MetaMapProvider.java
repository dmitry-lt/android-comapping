package com.comapping.android.metamap;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Comparator;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.comapping.android.provider.contentprovider.MapContentProvider.MapContentProviderInfo;
import com.comapping.android.Log;

public class MetaMapProvider {
	private MapContentProviderInfo info;
	private String nullListMessage;
	private String emptyListMessage;
	private Context context;

	private String emptyListCurrentMessage;
	private String currentPath;
	private MetaMapItem[] currentLevel;

	public MetaMapProvider(MapContentProviderInfo info, String nullListMessage, String emptyListMessage, Context context) {
		this.info = info;
		this.nullListMessage = nullListMessage;
		this.emptyListMessage = emptyListMessage;
		this.context = context;

		this.currentPath = info.root;
	}

	public boolean canGoHome() {
		return !isInRoot();
	}

	public boolean canGoUp() {
		return !isInRoot();
	}

	public boolean canLogout() {
		return info.canLogout;
	}

	public boolean canSync() {
		return info.canSync;
	}

	public MetaMapItem[] getCurrentLevel() {
		updateCurrentLevelFromCache();
		return currentLevel;
	}

	public String getEmptyListText() {
		return emptyListCurrentMessage;
	}

	public void goHome() {
		currentPath = info.root;
	}

	public void goUp() {
		// TODO write it more accurate
		currentPath = currentPath.substring(0, currentPath.substring(0, currentPath.length() - 1).lastIndexOf(
				info.separator) + 1);
	}

	public void gotoFolder(int index) {
		if (currentLevel[index].isFolder) {
			currentPath += currentLevel[index].name + info.separator;
		}
	}

	/**
	 * @return clientId
	 */
	public String login() {
		try {
			Cursor cursor = query(info.login);
			String clientId = cursor.getString(cursor.getColumnIndex("clientId"));
			Log.d(Log.META_MAP_CONTROLLER_TAG, "clientId=" + clientId);
			return clientId;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void logout() {
		query(info.logout);
		info.setLogout(false);
	}

	public boolean sync() {
		query(info.sync);
		// TODO: it doesn't mean that can logout (LoginInterruptedException for example)
		info.setLogout(true);
		return false;
	}

	public boolean isInRoot() {
		return currentPath.equals(info.root);
	}

	private Cursor query(String uriString) {
		return queryWithoutPrefix("content://" + uriString);
	}
	
	private Cursor queryWithoutPrefix(String uriString) {
		Uri uri = Uri.parse(uriString);
		return context.getContentResolver().query(uri, null, null, null, null);
	}

	private void updateCurrentLevelFromCache() {
		Cursor cursor = query(info.setIgnoreInternet(currentPath, true));

		if (cursor == null) {
			emptyListCurrentMessage = nullListMessage;
			currentLevel = new MetaMapItem[0];
			return;
		} else {
			emptyListCurrentMessage = emptyListMessage;
		}

		currentLevel = new MetaMapItem[cursor.getCount()];

		boolean isCurrentPresented = cursor.moveToFirst();
		for (int i = 0; i < currentLevel.length; i++) {
			currentLevel[i] = new MetaMapItem();
			MetaMapItem item = currentLevel[i];

			if (!isCurrentPresented) {
				Log.w(Log.META_MAP_CONTROLLER_TAG,
						"MetaMapProviderUsingCP.updateCurrentLevel(): cursor's element not found");
			} else {
				item.name = cursor.getString(cursor.getColumnIndex(MetaMapItem.COLUMN_NAME));
				item.description = cursor.getString(cursor.getColumnIndex(MetaMapItem.COLUMN_DESCRIPTION));
				item.isFolder = Boolean.parseBoolean(cursor.getString(cursor
						.getColumnIndex(MetaMapItem.COLUMN_IS_FOLDER)));

				if (!item.isFolder) {
					item.reference = cursor.getString(cursor.getColumnIndex(MetaMapItem.COLUMN_REFERENCE));

					String dateString = cursor.getString(cursor
							.getColumnIndex(MetaMapItem.COLUMN_LAST_SYNCHRONIZATION_DATE));
					if (dateString != null) {
						try {
							item.lastSynchronizationDate = Timestamp.valueOf(dateString);
						} catch (Exception e) {
							Log.w(Log.META_MAP_CONTROLLER_TAG, "Cannot parse date in MetaMapItem, name=" + item.name);
						}
					}

					item.sizeInBytes = cursor.getInt(cursor.getColumnIndex(MetaMapItem.COLUMN_SIZE_IN_BYTES));
				}
			}

			isCurrentPresented = cursor.moveToNext();
		}

		Arrays.sort(currentLevel, new MetaMapItemComparator());
	}

	public void finishWork() {
		query(info.finishWork);
	}
	
	public int getMapSizeInBytes(MetaMapItem item) {
		Cursor cursor = queryWithoutPrefix(info.setAction(item.reference, info.getMapSizeInBytesAction));
		int size = cursor.getInt(cursor.getColumnIndex(MetaMapItem.COLUMN_SIZE_IN_BYTES));
		return size;
	}

	protected class MetaMapItemComparator implements Comparator<MetaMapItem> {

		public int compare(MetaMapItem topic1, MetaMapItem topic2) {
			if ((topic1.isFolder && topic2.isFolder) || (!topic1.isFolder && !topic2.isFolder)) {
				// if both folder or both maps we compare texts
				return topic1.name.compareToIgnoreCase(topic2.name);
			} else {
				if (topic1.isFolder) {
					return -1;
				} else {
					return 1;
				}
			}
		}
	}
}
