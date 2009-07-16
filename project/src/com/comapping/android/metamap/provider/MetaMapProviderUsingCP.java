package com.comapping.android.metamap.provider;

import java.util.Arrays;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.comapping.android.metamap.MetaMapItem;
import com.comapping.android.provider.contentprovider.MapContentProvider.MapContentProviderInfo;
import com.comapping.android.Log;

public class MetaMapProviderUsingCP extends MetaMapProvider {
	private MapContentProviderInfo info;
	private String nullListMessage;
	private String emptyListMessage;
	private Context context;

	private String emptyListCurrentMessage;
	private String currentPath;
	private MetaMapItem[] currentLevel;

	public MetaMapProviderUsingCP(MapContentProviderInfo info, String nullListMessage, String emptyListMessage, Context context) {
		this.info = info;
		this.nullListMessage = nullListMessage;
		this.emptyListMessage = emptyListMessage;
		this.context = context;

		this.currentPath = info.root;
	}

	@Override
	public boolean canGoHome() {
		return !isInRoot();
	}

	@Override
	public boolean canGoUp() {
		return !isInRoot();
	}

	@Override
	public boolean canLogout() {
		return info.canLogout;
	}

	@Override
	public boolean canSync() {
		return info.canSync;
	}

	@Override
	public MetaMapItem[] getCurrentLevel() {
		updateCurrentLevelFromCache();
		return currentLevel;
	}

	@Override
	public String getEmptyListText() {
		return emptyListCurrentMessage;
	}

	@Override
	public void goHome() {
		currentPath = info.root;
	}

	@Override
	public void goUp() {
		// TODO write it more accurate
		currentPath = currentPath.substring(0, currentPath.substring(0, currentPath.length() - 1).lastIndexOf(info.separator) + 1);
	}

	@Override
	public void gotoFolder(int index) {
		if (currentLevel[index].isFolder) {
			currentPath += currentLevel[index].name + info.separator;
		}
	}

	@Override
	public void logout() {
		query(info.logout);
	}

	@Override
	public boolean sync() {
		query(info.sync);
		return false;
	}

	private boolean isInRoot() {
		return currentPath.equals(info.root);
	}

	private Cursor query(String uriString) {
		Uri uri = Uri.parse("content://" + uriString);
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
				item.reference = cursor.getString(cursor.getColumnIndex(MetaMapItem.COLUMN_REFERENCE));
			}

			isCurrentPresented = cursor.moveToNext();
		}

		Arrays.sort(currentLevel, new MetaMapProvider.MetaMapItemComparator());
	}

	@Override
	public void finishWork() {
		query(info.finishWork);		
	}
}
