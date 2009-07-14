package com.comapping.android.metamap.provider;

import java.sql.Timestamp;
import java.util.Arrays;

import android.app.Activity;
import android.app.ProgressDialog;

import com.comapping.android.Log;
import com.comapping.android.provider.communication.CachingClient;
import com.comapping.android.provider.communication.Client;
import com.comapping.android.provider.communication.exceptions.ConnectionException;
import com.comapping.android.provider.communication.exceptions.InvalidCredentialsException;
import com.comapping.android.provider.communication.exceptions.LoginInterruptedException;
import com.comapping.android.map.model.exceptions.MapParsingException;
import com.comapping.android.map.model.exceptions.StringToXMLConvertionException;
import com.comapping.android.map.model.map.Map;
import com.comapping.android.map.model.map.Topic;
import com.comapping.android.map.model.map.builder.MapBuilder;
import com.comapping.android.map.model.map.builder.SaxMapBuilder;
import com.comapping.android.metamap.MetaMapItem;

public class ComappingProvider extends MetaMapProvider {

	private static final String LAST_SYNCHRONIZATION = "Last synchronization";

	private static final String MAP_DESCRIPTION = "Map";
	private static final String FOLDER_DESCRIPTION = "Folder";
	
	private static final String EMPTY_FOLDER_MESSAGE = "Folder is empty";
	private static final String NEED_RESYNC_MESSAGE = "Please, click resync button for downloading map list";

	Map metamap;
	Topic currentLevel;
	Activity activity;
	MetaMapItem[] cachedLevel;

	public ComappingProvider(Activity _activity) {
		activity = _activity;
		metamap = null;

		update(false);
		updateCache();

		if (metamap != null)
			currentLevel = metamap.getRoot();
	}

	void updateCache() {
		if (metamap == null)
			cachedLevel = new MetaMapItem[0];
		else
			cachedLevel = getItems(currentLevel.getChildTopics());
		
		Arrays.sort(cachedLevel, new MetaMapProvider.MetaMapItemComparator());
	}
	
	// =========================================
	// Topic conversion
	// =========================================
	MetaMapItem[] getItems(Topic[] topics) {
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

			res[i].reference = topics[i].getMapRef();
		}

		return res;
	}

	private String getLastSynchronization(Timestamp date) {
		long time = (System.currentTimeMillis() - date.getTime()) / 1000;
		if (time < 5 * 60) {
			return "just now";
		}
		time /= 60;
		if (time < 60) {
			return time + " minutes ago";
		}
		time /= 60;
		if (time < 24) {
			return time + " hours ago";
		}
		time /= 24;
		return time + " days ago";
	}
	
	private String getSize(int size) {
		if (size < 1024) {
			return size + " bytes";
		}
		size /= 1024;
		return size + " Kbytes";

	}
	
	public String getMapDescription(Topic topic) {
		CachingClient client = Client.getClient(activity);
		Timestamp lastSynchronizationDate = client
				.getLastSynchronizationDate(topic.getMapRef());

		if (lastSynchronizationDate == null) {
			return MAP_DESCRIPTION;
		} else {
			String result = LAST_SYNCHRONIZATION + ": "
			+ getLastSynchronization(lastSynchronizationDate) + "\nSize: ";
			try {
				result = result 
						+ getSize(client.getComap(topic.getMapRef()).length());
			} catch (Exception e) {
				
			}
			return result;
		}
	}

	public String getFolderDescription(Topic topic) {
		return FOLDER_DESCRIPTION;
	}
	
	
	@Override
	public MetaMapItem[] getCurrentLevel() {
		return cachedLevel;
	}

	@Override
	public void goHome() {
		if (metamap == null)
			return;

		currentLevel = metamap.getRoot();
		updateCache();
	}

	@Override
	public void goUp() {

		if (metamap == null)
			return;

		currentLevel = currentLevel.getParent();
		updateCache();
	}

	@Override
	public void gotoFolder(int index) {

		if (metamap == null)
			return;

		if (cachedLevel[index].isFolder) {
			//cachedLevel[index].name;
			Topic[] topics = currentLevel.getChildTopics();
			for(int i = 0; i< topics.length; i++)
			{
				if (topics[i].getText().equals(cachedLevel[index].name))
				{
					currentLevel = topics[i];
					break;
				}
			}
			//currentLevel = currentLevel.getChildByIndex(index);
		}
		updateCache();
	}

	@Override
	public boolean canGoHome() {

		if (metamap == null)
			return false;

		return currentLevel != metamap.getRoot();
	}

	@Override
	public boolean canGoUp() {

		if (metamap == null)
			return false;

		return currentLevel != metamap.getRoot();
	}

	@Override
	public boolean canSync() {

		return true;
	}

	public static final String PLEASE_SYNCHRONIZE_MESSAGE = "Please synchronize your map list or open sdcard view";
	public static final String PROBLEMS_WHILE_RETRIEVING_MESSAGE = "There are some problem while map list retrieving.";
	public static final String PROBLEMS_WITH_MAP_MESSAGE = "There are some problem while map list parsing.";

	private static final String LOADING_MESSAGE = "Loading map list";

	private ProgressDialog splash = null;

	public void splashActivate(final String message) {
		final Activity context = activity;

		activity.runOnUiThread(new Runnable() {
			public void run() {
				if (splash == null) {
					splash = ProgressDialog.show(context, "Comapping", message);
				} else {
					splash.setMessage(message);
				}
			}
		});
	}

	public void splashDeactivate() {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				if (splash != null) {
					splash.dismiss();
					splash = null;
				}
			}
		});
	}

	private void update(boolean ignoreCache) {
		CachingClient client = Client.getClient(activity);

		String result = "";

		splashActivate(LOADING_MESSAGE);
		String error = null;

		try {
			result = client.getComap("meta", ignoreCache,
					!ignoreCache);
		} catch (ConnectionException e) {
			error = PROBLEMS_WHILE_RETRIEVING_MESSAGE; // TODO:
			// different
			// messages
			Log.e(Log.META_MAP_CONTROLLER_TAG,
					"connection error in metamap retrieving");
		} catch (LoginInterruptedException e) {
			error = PROBLEMS_WHILE_RETRIEVING_MESSAGE; // TODO:
			// different
			// messages
			Log.e(Log.META_MAP_CONTROLLER_TAG,
					"login interrupted in metamap retrieving");
		} catch (InvalidCredentialsException e) {
			error = PROBLEMS_WHILE_RETRIEVING_MESSAGE; // TODO:
			// different
			// messages
			Log.e(Log.META_MAP_CONTROLLER_TAG,
					"invalid credentails while getting comap oO");
		}

		// Map metaMap = null;
		if (error == null) {
			// retrieving was successful
			try {
				if (result != null) {
					MapBuilder mapBuilder = new SaxMapBuilder();
					metamap = mapBuilder.buildMap(result);
				}
			} catch (StringToXMLConvertionException e) {
				Log.e(Log.META_MAP_CONTROLLER_TAG, "xml convertion exception");
				error = PROBLEMS_WITH_MAP_MESSAGE;
			} catch (MapParsingException e) {
				Log.e(Log.META_MAP_CONTROLLER_TAG, "map parsing exception");
				error = PROBLEMS_WITH_MAP_MESSAGE;
			}
		}

		if (metamap != null)
			currentLevel = metamap.getRoot();

		splashDeactivate();

	}

	@Override
	public boolean sync() {
		update(true);
		updateCache();
		return false;
	}

	@Override
	public boolean canLogout() {
		CachingClient client = Client.getClient(activity);
		return client.isLoggedIn();
	}

	@Override
	public void logout() {
		CachingClient client = Client.getClient(activity);
		try {
			client.logout();
		} catch (ConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public String getEmptyListText() {
		if (metamap == null)
			return NEED_RESYNC_MESSAGE;
		else
			return EMPTY_FOLDER_MESSAGE;
	}
}
