package com.comapping.android.metamap.provider;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

import com.comapping.android.metamap.MetaMapItem;

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
		updateCurrentLevel();
	}

	void updateCurrentLevel() {
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
		
		Arrays.sort(currentLevel, new MetaMapProvider.MetaMapItemComparator());
	}

	@Override
	public MetaMapItem[] getCurrentLevel() {
		return currentLevel;
	}

	@Override
	public void goHome() {
		currentPath = root;
		updateCurrentLevel();
	}

	@Override
	public void goUp() {
		currentPath = new File(currentPath).getParent();
		updateCurrentLevel();
	}

	@Override
	public void gotoFolder(int index) {
		if (currentLevel[index].isFolder) {
			currentPath += File.separator + currentLevel[index].name;
			updateCurrentLevel();
		}
	}

	@Override
	public boolean canGoHome() {
		return currentPath.compareTo(root) != 0;
	}

	@Override
	public boolean canGoUp() {
		return currentPath.compareTo(root) != 0;
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
