/*
 * MetaMapView Controller
 * Android Comapping, 2009
 * Last change: Abishev Timur
 * 
 * Class implements MetaMapView controller
 */

package com.comapping.android.controller;

import java.io.File;
import java.util.Arrays;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.comapping.android.Log;
import com.comapping.android.MetaMapViewType;
import com.comapping.android.ViewType;
import com.comapping.android.communication.CachingClient;
import com.comapping.android.communication.Client;
import com.comapping.android.communication.FileMapProvider;
import com.comapping.android.communication.MapProvider;
import com.comapping.android.communication.exceptions.ConnectionException;
import com.comapping.android.communication.exceptions.InvalidCredentialsException;
import com.comapping.android.communication.exceptions.LoginInterruptedException;
import com.comapping.android.model.Map;
import com.comapping.android.model.MapBuilder;
import com.comapping.android.model.SaxMapBuilder;
import com.comapping.android.model.Topic;
import com.comapping.android.model.TopicComparator;
import com.comapping.android.model.exceptions.MapParsingException;
import com.comapping.android.model.exceptions.StringToXMLConvertionException;
import com.comapping.android.storage.MemoryCache;
import com.comapping.android.storage.SqliteMapCache;
import com.comapping.android.storage.Storage;
import com.comapping.android.view.MetaMapView;

public class MetaMapActivity extends Activity {
	// constants
	public static final int MAP_REQUEST = 5523;

	public static final String MAP_DESCRIPTION = "Map";
	public static final String FOLDER_DESCRIPTION = "Folder";

	// public variables
	public static CachingClient client = null;
	public static FileMapProvider fileMapProvider = new FileMapProvider();

	public static MapBuilder mapBuilder = new SaxMapBuilder();

	// private variables
	// views
	private static MetaMapViewType currentView = MetaMapViewType.INTERNET_VIEW;

	private static MetaMapView internetView = null;
	private static MetaMapView sdcardView = null;

	//
	private static MetaMapActivity instance;
	private ProgressDialog splash = null;
	private Topic[] currentTopicChildren;

	// activity methods
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		instance = this;

		client = new CachingClient(new Client(), new SqliteMapCache(this));

		MetaMapView.loadLayout(this);

		// init sdcardView if needed
		if (sdcardView == null) {
			initSdcardView();
		}

		// init internetView if needed
		if (internetView == null) {
			metaMapRefresh();
		} else {
			if (currentView == MetaMapViewType.INTERNET_VIEW) {
				internetView.activate(this);
			}
		}

		// activate sdcardView if needed
		if (currentView == MetaMapViewType.SDCARD_VIEW) {
			sdcardView.activate(this);
		}
	}

	/* Creates the menu items */
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.metamap_options, menu);
		return true;
	}

	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;

		MenuInflater inflater = getMenuInflater();
		super.onCreateContextMenu(menu, view, menuInfo);

		int toInflate = R.menu.metamap_map_context; // default value

		if (!currentTopicChildren[info.position].isFolder()) {
			toInflate = R.menu.metamap_map_context;
		} else {
			toInflate = R.menu.metamap_folder_context;
		}

		inflater.inflate(toInflate, menu);
	}

	/* Handles item selections */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.preferences:
			preferences();
			return true;
		case R.id.logout:
			logout();
			return true;
		case R.id.reloadMetamap:
			metaMapRefresh();
			return true;
		case R.id.clearCache:
			MemoryCache.clear();
			client.clearCache();

			return true;
		}

		return false;
	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

		int itemId = (int) info.id;

		if (!currentTopicChildren[itemId].isFolder()) {
			String mapId = currentTopicChildren[itemId].getMapRef();

			switch (item.getItemId()) {
			case R.id.openWithTreeView:
				loadMap(mapId, ViewType.TREE_VIEW);
				break;
			case R.id.openWithExplorerView:
				loadMap(mapId, ViewType.EXPLORER_VIEW);
				break;
			}
		} else {
			loadMetaMapTopic(currentTopicChildren[itemId]);
		}

		return true;
	}

	@Override
	protected void onDestroy() {
		splashDeactivate();
		super.onDestroy();
	}

	// MetaMap methods

	public void initSdcardView() {
		Map sdMap = new Map(0);
		Topic root = new Topic(0, null);

		try {
			root.setText("sdcard");
		} catch (StringToXMLConvertionException e) {
			// unreachable code
			Log.e(Log.metaMapControllerTag, "error while parsing 'sdcard'");
		}

		root.setNote("/sdcard");

		sdMap.setRoot(root);

		sdcardView = new MetaMapView(sdMap);
	}

	public static MapProvider getCurrentMapProvider() {
		return (currentView == MetaMapViewType.INTERNET_VIEW) ? client : fileMapProvider;
	}

	public static MetaMapActivity getInstance() {
		return instance;
	}

	public MetaMapViewType getCurrentView() {
		return currentView;
	}

	public void switchView(MetaMapViewType viewType) {
		currentView = viewType;

		if (viewType == MetaMapViewType.INTERNET_VIEW) {
			internetView.activate(this);
		} else {
			sdcardView.activate(this);
		}
	}

	public void splashActivate(final String message) {
		final Activity context = this;

		runOnUiThread(new Runnable() {
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
		runOnUiThread(new Runnable() {
			public void run() {
				if (splash != null) {
					splash.dismiss();
					splash = null;
				}
			}
		});
	}

	private void metaMapRefresh() {
		final MetaMapActivity context = this;

		new Thread() {
			public void run() {
				String result = "";

				splashActivate("Downloading map list");

				try {
					result = client.getComap("meta", context);
				} catch (ConnectionException e) {
					Log.e(Log.metaMapControllerTag, "connection error in metamap retrieving");
				} catch (LoginInterruptedException e) {
					Log.e(Log.metaMapControllerTag, "login interrupted in metamap retrieving");
				} catch (InvalidCredentialsException e) {
					Log.e(Log.metaMapControllerTag, "invalid credentails while getting comap oO");
				}

				splashActivate("Loading map list");

				Map metaMap = null;
				try {
					metaMap = mapBuilder.buildMap(result);
				} catch (StringToXMLConvertionException e) {
					Log.e(Log.metaMapControllerTag, "xml convertion exception");
				} catch (MapParsingException e) {
					Log.e(Log.metaMapControllerTag, "map parsing exception");
				}

				splashDeactivate();

				internetView = new MetaMapView(metaMap);

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						switchView(MetaMapViewType.INTERNET_VIEW);
					}
				});
			}
		}.start();
	}

	private void prepareSdcardTopic(final Topic topic) {
		topic.removeAllChildTopics();

		File directory = new File(topic.getNote());
		for (File file : directory.listFiles()) {
			Topic newTopic = new Topic(0, topic);
			try {
				newTopic.setText(file.getName());
			} catch (StringToXMLConvertionException e) {
				Log.e(Log.metaMapControllerTag, "error while parsing file name");
			}

			newTopic.setNote(file.getAbsolutePath());

			if (!file.isDirectory()) {
				newTopic.setMapRef(file.getAbsolutePath());
			}

			topic.addChild(newTopic);
		}
	}

	public void loadMetaMapTopic(final Topic topic) {
		// prepare the topic
		if (currentView == MetaMapViewType.SDCARD_VIEW) {
			prepareSdcardTopic(topic);
		}
		// end prepare

		currentTopicChildren = topic.getChildTopics();

		Arrays.sort(currentTopicChildren, new TopicComparator());

		if (currentView == MetaMapViewType.INTERNET_VIEW) {
			internetView.drawMetaMapTopic(topic, currentTopicChildren);
		} else {
			sdcardView.drawMetaMapTopic(topic, currentTopicChildren);
		}
	}

	public void loadMap(final String mapId, final ViewType viewType) {
		Intent intent = new Intent(MapActivity.MAP_ACTIVITY_INTENT);
		intent.putExtra(MapActivity.EXT_MAP_ID, mapId);
		intent.putExtra(MapActivity.EXT_VIEW_TYPE, viewType.toString());

		startActivityForResult(intent, MAP_REQUEST);
	}

	public void logout() {
		Storage.getInstance().set("key", "");

		try {
			client.logout(this);
		} catch (ConnectionException e) {
			Log.e(Log.metaMapControllerTag, "connection exception in logout");
		}

		metaMapRefresh();
	}

	public void preferences() {
		startActivity(new Intent(PreferencesActivity.PREFERENCES_ACTIVITY_INTENT));
	}
}