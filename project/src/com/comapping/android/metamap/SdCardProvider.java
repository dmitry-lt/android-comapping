package com.comapping.android.metamap;

import java.io.File;
import java.io.FilenameFilter;

import com.comapping.android.metamap.MetaMapListAdapter.MetaMapItem;

public class SdCardProvider extends MetaMapProvider {

	private static final String FOLDER_DESCR = "Folder";
	private static final String MAP_DESCR = "Map";

	final static private FilenameFilter filter = new FilenameFilter() {
		public boolean accept(File dir, String filename) {
			return (new File(dir.getAbsolutePath() + "/" + filename))
					.isDirectory()
					|| filename.toLowerCase().endsWith(".comap");
		}
	};

	static final String root = "/sdcard";

	String currentPath;
	MetaMapItem[] currentLevel;

	public SdCardProvider() {
		currentPath = root;
		updateCurtrentLevel();
	}

	void updateCurtrentLevel() {
		File directory = new File(currentPath);
		File[] fileList = directory.listFiles(filter);
		if (fileList == null)
		{
			currentLevel = new MetaMapItem[0];
			return;
		}
		
		currentLevel = new MetaMapItem[fileList.length];

		for (int i = 0; i < currentLevel.length; i++) {
			currentLevel[i] = new MetaMapItem();
			currentLevel[i].isFolder = fileList[i].isDirectory();
			currentLevel[i].name = fileList[i].getName();
			currentLevel[i].reference = fileList[i].getAbsolutePath();
			if (currentLevel[i].isFolder)
				currentLevel[i].description = FOLDER_DESCR;
			else
				currentLevel[i].description = MAP_DESCR;
		}
	}

	@Override
	public MetaMapItem[] getCurrentLevel() {
		return currentLevel;
	}

	@Override
	public void goHome() {
		currentPath = root;
		updateCurtrentLevel();
	}

	@Override
	public void goUp() {
		// TODO FIX IT
		currentPath = root;
		updateCurtrentLevel();
	}

	@Override
	public void gotoFolder(int index) {
		if (currentLevel[index].isFolder) {
			currentPath += File.separator + currentLevel[index].name;
			updateCurtrentLevel();
		}
	}

	@Override
	public boolean canGoHome() {
		return currentPath != root;
	}

	@Override
	public boolean canGoUp() {
		return currentPath != root;
	}

	@Override
	public boolean canSync() {
		return false;
	}

	@Override
	public boolean sync() {
		return false;
	}
}
