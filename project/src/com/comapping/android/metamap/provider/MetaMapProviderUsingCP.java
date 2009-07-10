package com.comapping.android.metamap.provider;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.comapping.android.metamap.MetaMapItem;

public class MetaMapProviderUsingCP extends MetaMapProvider {
	private static final String SEPARATOR = "/";
	
	private final boolean canLogout;
	private final boolean canSync;
	private final String root;
	private final String logout;
	private final String sync;
	private String currentPath;
	private String emptyListText;
		
	private Context context;
	
	private MetaMapItem[] currentLevel;
	
	public MetaMapProviderUsingCP(String authorities, String root, boolean canLogout, boolean canSync, String emptyListText, Context context) {
		this.root = authorities + SEPARATOR + root + SEPARATOR;
		this.logout = authorities + SEPARATOR + "logout";
		this.sync = authorities + SEPARATOR + "sync";
		this.canLogout = canLogout;
		this.canSync = canSync;
		this.emptyListText = emptyListText;
		
		this.context = context;
	}
	
	private boolean isInRoot() {
		return currentPath.equals(root);
	}
	
	private Cursor query(String uriString) {
		Uri uri = Uri.parse("content://" + uriString);
		return context.getContentResolver().query(uri, null, null, null, null);
	}
	
	private void updateCurrentLevel() {
		
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
		query(logout);
	}

	@Override
	public boolean sync() {
		query(sync);
		return false;
	}
	
}
