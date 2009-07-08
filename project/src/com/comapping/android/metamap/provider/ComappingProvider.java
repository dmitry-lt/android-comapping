package com.comapping.android.metamap.provider;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import android.app.Activity;
import android.app.ProgressDialog;

import com.comapping.android.Log;
import com.comapping.android.communication.CachingClient;
import com.comapping.android.communication.Client;
import com.comapping.android.communication.exceptions.ConnectionException;
import com.comapping.android.communication.exceptions.InvalidCredentialsException;
import com.comapping.android.communication.exceptions.LoginInterruptedException;
import com.comapping.android.metamap.MetaMapItem;
import com.comapping.android.model.exceptions.MapParsingException;
import com.comapping.android.model.exceptions.StringToXMLConvertionException;
import com.comapping.android.model.map.Map;
import com.comapping.android.model.map.Topic;
import com.comapping.android.model.map.builder.MapBuilder;
import com.comapping.android.model.map.builder.SaxMapBuilder;
import com.comapping.android.storage.SqliteMapCache;

public class ComappingProvider extends MetaMapProvider {

	private static final String LAST_SYNCHRONIZATION = "Last synchronization";

	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	private static final String MAP_DESCRIPTION = "Map";
	private static final String FOLDER_DESCRIPTION = "Folder";

	Map metamap;
	Topic currentLevel;
	Activity activity;

	public ComappingProvider(Activity _activity) {
		activity = _activity;
		metamap = null;

		update(false);

		if (metamap == null)
			return;
		currentLevel = metamap.getRoot();
	}

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

	public String getMapDescription(Topic topic) {
		CachingClient client = Client.getClient(activity);
		Timestamp lastSynchronizationDate = client
				.getLastSynchronizationDate(topic.getMapRef());

		if (lastSynchronizationDate == null) {
			return MAP_DESCRIPTION;
		} else {
			SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

			return LAST_SYNCHRONIZATION + ": "
					+ dateFormat.format(lastSynchronizationDate);
		}
	}

	public String getFolderDescription(Topic topic) {
		return FOLDER_DESCRIPTION;
	}

	@Override
	public MetaMapItem[] getCurrentLevel() {

		if (metamap == null)
			return new MetaMapItem[0];

		return getItems(currentLevel.getChildTopics());
	}

	@Override
	public void goHome() {
		if (metamap == null)
			return;

		currentLevel = metamap.getRoot();
	}

	@Override
	public void goUp() {

		if (metamap == null)
			return;

		currentLevel = currentLevel.getParent();
	}

	@Override
	public void gotoFolder(int index) {

		if (metamap == null)
			return;

		if (currentLevel.getChildByIndex(index).isFolder()) {
			currentLevel = currentLevel.getChildByIndex(index);
		}
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
			result = client.getComap("meta", activity, ignoreCache,
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
		return false;
	}
}
