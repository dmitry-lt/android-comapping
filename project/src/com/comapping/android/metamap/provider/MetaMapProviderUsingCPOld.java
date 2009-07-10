package com.comapping.android.metamap.provider;

import java.io.File;

import android.content.Context;

import com.comapping.android.metamap.MetaMapItem;

public class MetaMapProviderUsingCPOld extends MetaMapProvider {
	private static final String SEPARATOR = "/";
	
	private final Provider comappingProvider = new Provider("comapping.com", "maps", true, true);
	private final Provider sdCardProvider = new Provider("sdcard", "", false, false);
	
	private Provider currentProvider;
	
	private MetaMapItem[] currentLevel;
	
	private Context context;
	
	public MetaMapProviderUsingCPOld(Context context) {
		this.context = context;		
		setComappingProvider();
	}
	
	public boolean setComappingProvider() {
		currentProvider = comappingProvider;
		updateCurrentLevel();
		return true;
	}
	
	public boolean setSdCardProvider() {
		if (hasSDCard()) {
			currentProvider = sdCardProvider;
			updateCurrentLevel();
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean canGoHome() {		
		return !currentProvider.isInRoot();
	}

	@Override
	public boolean canGoUp() {		
		return !currentProvider.isInRoot();
	}

	@Override
	public boolean canLogout() {		
		return currentProvider.canLogout;
	}

	@Override
	public boolean canSync() {		
		return currentProvider.canSync;
	}

	@Override
	public MetaMapItem[] getCurrentLevel() {		
		return currentLevel;
	}

	@Override
	public void goHome() {
		currentProvider.currentPath = currentProvider.root;
		updateCurrentLevel();
	}

	@Override
	public void goUp() {
		currentProvider.currentPath = new File(currentProvider.currentPath).getParent();
		updateCurrentLevel();
	}

	@Override
	public void gotoFolder(int index) {
		if (currentLevel[index].isFolder) {
			currentProvider.currentPath += currentLevel[index].name + SEPARATOR;
			updateCurrentLevel();
		}
	}

	@Override
	public void logout() {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean sync() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getEmptyListText() {
		// TODO Auto-generated method stub
		return null;
	}

	private boolean hasSDCard() {
		final String sdCardPath = "/sdcard";
		File directory = new File(sdCardPath);
		if (directory.exists())
			return true;
		return false;
	}
	
	private void updateCurrentLevel() {
		
	}
	
	private static class Provider {
		public final String authorities;
		public final boolean canLogout;
		public final boolean canSync;
		public final String root;
		public final String logout;
		public final String sync;
		public String currentPath;
		
		public Provider(String authorities, String root, boolean canLogout, boolean canSync) {
			this.authorities = authorities;
			this.root = authorities + SEPARATOR + root + SEPARATOR;
			this.logout = authorities + SEPARATOR + "logout";
			this.sync = authorities + SEPARATOR + "sync";
			this.canLogout = canLogout;
			this.canSync = canSync;
		}
		
		public boolean isInRoot() {
			return currentPath.equals(root);
		}
	}
}
