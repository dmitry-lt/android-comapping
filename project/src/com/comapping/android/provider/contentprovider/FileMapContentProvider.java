package com.comapping.android.provider.contentprovider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import android.database.Cursor;
import android.net.Uri;

import com.comapping.android.Log;
import com.comapping.android.map.model.map.Topic;
import com.comapping.android.metamap.MetaMapItem;

public class FileMapContentProvider extends MapContentProvider {

	public static final MapContentProviderInfo INFO = new MapContentProviderInfo(
			"sdcard", "", false, false);
	public static final Uri CONTENT_URI = Uri.parse("content://" + INFO.root);
	private static final String EMPTY_FOLDER_MESSAGE = "Folder is empty";

	private static final String MAP_DESCRIPTION = "Map";
	private static final String FOLDER_DESCRIPTION = "Folder";

	private enum QueryType {
		MAP, META_MAP, LOGOUT, SYNC
	}

	public static final int PREFIX_LENGTH = 1;

	String currentPath = "/sdcard";
	MetaMapItem[] currentLevel;
	public static boolean ignoreCache = true;

	@Override
	public boolean onCreate() {
		return false;
	}

	private String URiToId(Uri uri) {
		Log.e("******UriToId-input", uri.toString());
		Log.e("******UriToId-output", uri.getPath().substring(PREFIX_LENGTH));
		return "sdcard/" + uri.getPath().substring(PREFIX_LENGTH);

	}

	final static private FilenameFilter filter = new FilenameFilter() {
		public boolean accept(File dir, String filename) {
			return (new File(dir.getAbsolutePath() + "/" + filename))
					.isDirectory()
					|| filename.toLowerCase().endsWith(".comap");
		}
	};

	void updateCurrentLevel() {
		File directory = new File(currentPath);
		File[] fileList = directory.listFiles(filter);
		if (fileList == null) {

			currentLevel = new MetaMapItem[0];
			return;
		}

		currentLevel = new MetaMapItem[fileList.length];

		for (int i = 0; i < currentLevel.length; i++) {
			currentLevel[i] = new MetaMapItem();
			currentLevel[i].isFolder = fileList[i].isDirectory();
			currentLevel[i].name = fileList[i].getName();
			currentLevel[i].reference = "content:/"
					+ fileList[i].getAbsolutePath();
			if (currentLevel[i].isFolder)
				currentLevel[i].description = FOLDER_DESCRIPTION;
			else
				currentLevel[i].description = MAP_DESCRIPTION;
		}

	}

	public MetaMapItem[] getCurrentLevel() {
		return currentLevel;
	}

	public void goHome() {
		currentPath = INFO.root;
		updateCurrentLevel();
	}

	public void goUp() {
		currentPath = new File(currentPath).getParent();
		updateCurrentLevel();
	}

	public void gotoFolder(int index) {
		if (currentLevel[index].isFolder) {
			currentPath += File.separator + currentLevel[index].name;
			updateCurrentLevel();
		}
	}

	public boolean canGoHome() {
		return currentPath.compareTo(INFO.root) != 0;
	}

	public boolean canGoUp() {
		return currentPath.compareTo(INFO.root) != 0;
	}

	public boolean canSync() {
		return false;
	}

	public boolean sync() {
		return false;
	}

	public boolean canLogout() {
		return false;
	}

	public void logout() {
	}

	public String getEmptyListText() {
		return EMPTY_FOLDER_MESSAGE;
	}

	private QueryType detectQueryType(Uri uri) {
		String uriString = uri.toString();
		Log.e("detectQueryType", "" + uri);
		// TODO realize and implement detecting using patterns
		/*
		 * if (Pattern.matches("content://" + INFO.logout, uriString)) { return
		 * QueryType.LOGOUT; } else if (Pattern.matches("content://" +
		 * INFO.sync, uriString)) { return QueryType.SYNC; } else if
		 * (Pattern.matches( "content://" + INFO.root + "werrt.comap",
		 * uriString)) { return QueryType.MAP; } else { return
		 * QueryType.META_MAP; }
		 */
		if (uriString.endsWith(".comap")) {
			return QueryType.MAP;
		} else {
			return QueryType.META_MAP;
		}

	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		QueryType queryType = detectQueryType(uri);

		switch (queryType) {
		case META_MAP:
			Log.e("******", "metamap");

			List<String> pathSegments = uri.getPathSegments();

			if (!INFO.relRoot.equals("")) {
				pathSegments.remove(0);
			}

			updateCurrentLevel();
			FileMetamapCursor mmc = new FileMetamapCursor(currentLevel);
			return mmc;

		case MAP:
			Log.e("******", "!!!MAP!!!");
			return new FileMapCursor(URiToId(uri));
		case LOGOUT:
			Log.e("******", "LOGOUT");

			return null;
		case SYNC:
			Log.e("******", "SYNC");

			return null;
		default:
			Log.e("******", "ERROR while parsing URI");
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}

	}

	// //////////////////////////////////////////////////////////////////////////////
	// _____________________________CURSORS________________________________________//
	// //////////////////////////////////////////////////////////////////////////////

	private class FileMapCursor extends MapCursor {

		protected String getTextFromInputStream(InputStream input)
				throws IOException {
			StringBuffer content = new StringBuffer();

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					input), 8 * 1024);

			String line = null;
			boolean first = true;

			while ((line = reader.readLine()) != null) {
				if (!first) {
					content.append(System.getProperty("line.separator"));
				} else {
					first = false;
				}

				content.append(line);
			}

			reader.close();

			return content.toString();
		}

		public FileMapCursor(String mapId) {
			this.id = mapId;

			String response = null;

			try {
				Log.e(Log.CONNECTION_TAG, mapId);
				response = getTextFromInputStream(new FileInputStream(mapId));
			} catch (FileNotFoundException e) {
				Log.e(Log.CONNECTION_TAG, "map file not found");
			} catch (IOException e) {
				Log.e(Log.CONNECTION_TAG, "map file IO exception");
			}

			Log.d(Log.CONNECTION_TAG, "file comap provider response: "
					+ response);

			this.text = response;

		}
	}

	@SuppressWarnings("unused")
	private class FileMetamapCursor extends MetamapCursor {
		private static final String LAST_SYNCHRONIZATION = "Last synchronization";

		private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

		private static final String MAP_DESCRIPTION = "Map";
		private static final String FOLDER_DESCRIPTION = "Folder";

		public FileMetamapCursor(Topic topic) {
			currentLevel = getItems(topic.getChildTopics());
		}

		public FileMetamapCursor(MetaMapItem[] mmi) {
			currentLevel = mmi;
		}

		private MetaMapItem[] getItems(Topic[] topics) {
			MetaMapItem[] res = new MetaMapItem[topics.length];

			for (int i = 0; i < topics.length; i++) {
				res[i] = new MetaMapItem();
				res[i].name = topics[i].getText();

				res[i].isFolder = topics[i].isFolder();

				if (res[i].isFolder) {
					res[i].description = getFolderDescription(topics[i]);
				} else {
					res[i].description = getMapDescription(topics[i]);
				}

				res[i].reference = "content://" + INFO.root + INFO.separator
						+ topics[i].getMapRef();
			}

			return res;
		}

		private String getMapDescription(Topic topic) {
			return MAP_DESCRIPTION;

		}

		private String getFolderDescription(Topic topic) {
			return FOLDER_DESCRIPTION;
		}
	}

}
