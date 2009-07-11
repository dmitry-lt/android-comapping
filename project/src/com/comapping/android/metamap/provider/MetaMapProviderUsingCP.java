package com.comapping.android.metamap.provider;

import java.util.Arrays;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.comapping.android.metamap.MetaMapItem;
import com.comapping.android.Log;

public class MetaMapProviderUsingCP extends MetaMapProvider {
	private static final String SEPARATOR = "/";
	
	private final boolean canLogout;
	private final boolean canSync;
	private final String root;
	private final String logoutUri;
	private final String syncUri;
	private String currentPath;
	private String emptyListText;
		
	private Context context;
	
	private MetaMapItem[] currentLevel;
	
	public MetaMapProviderUsingCP(String authorities, String relativeRoot, boolean canLogout, boolean canSync, String emptyListText, Context context) {
		this.root = authorities + SEPARATOR + relativeRoot + SEPARATOR;
		this.logoutUri = authorities + SEPARATOR + "logout";
		this.syncUri = authorities + SEPARATOR + "sync";
		this.canLogout = canLogout;
		this.canSync = canSync;
		this.emptyListText = emptyListText;
		
		this.context = context;
		
		this.currentPath = root;
	}
	
	private boolean isInRoot() {
		return currentPath.equals(root);
	}
	
	private Cursor query(String uriString) {
		Uri uri = Uri.parse("content://" + uriString);
		return context.getContentResolver().query(uri, null, null, null, null);
	}
	
	private void updateCurrentLevel() {
		Cursor cursor = query(currentPath);
		
		currentLevel = new MetaMapItem[cursor.getCount()];
		
		boolean isCurrentPresented = cursor.moveToFirst();
		for (MetaMapItem item : currentLevel) {
			item = new MetaMapItem();
			if (!isCurrentPresented) {
				Log.w(Log.META_MAP_CONTROLLER_TAG, "MetaMapProviderUsingCP.updateCurrentLevel(): cursor's element not found");				
			} else {
				item.name = cursor.getString(cursor.getColumnIndex(MetaMapItem.COLUMN_NAME));
				item.description = cursor.getString(cursor.getColumnIndex(MetaMapItem.COLUMN_DESCRIPTION));
				item.isFolder = Boolean.parseBoolean(cursor.getString(cursor.getColumnIndex(MetaMapItem.COLUMN_IS_FOLDER)));
				item.reference = cursor.getString(cursor.getColumnIndex(MetaMapItem.COLUMN_REFERENCE));
			}
			
			isCurrentPresented = cursor.moveToNext();
		}
		
		Arrays.sort(currentLevel, new MetaMapProvider.MetaMapItemComparator());
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
		return canLogout;
	}

	@Override
	public boolean canSync() {
		return canSync;
	}

	@Override
	public MetaMapItem[] getCurrentLevel() {
		return currentLevel;
	}

	@Override
	public String getEmptyListText() {
		return emptyListText;
	}

	@Override
	public void goHome() {
		currentPath = root;
		updateCurrentLevel();
	}

	@Override
	public void goUp() {
		currentPath = currentPath.substring(0, currentPath.lastIndexOf(SEPARATOR));
		updateCurrentLevel();
	}

	@Override
	public void gotoFolder(int index) {
		if (currentLevel[index].isFolder) {
			currentPath += currentLevel[index].name + SEPARATOR;
			updateCurrentLevel();
		}
	}

	@Override
	public void logout() {		
		query(logoutUri);
	}

	@Override
	public boolean sync() {
		query(syncUri);
		return false;
	}
	
}
