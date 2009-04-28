/*
 * MetaMapView Controller
 * Android Comapping, 2009
 * Last change: Abishev Timur
 * 
 * Class implements MetaMapView controller
 */

package com.comapping.android.controller;

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
import com.comapping.android.ViewType;
import com.comapping.android.communication.CachingClient;
import com.comapping.android.communication.Client;
import com.comapping.android.communication.FileMapProvider;
import com.comapping.android.communication.MapProvider;
import com.comapping.android.communication.exceptions.ConnectionException;
import com.comapping.android.communication.exceptions.InvalidCredentialsException;
import com.comapping.android.communication.exceptions.LoginInterruptedException;
import com.comapping.android.model.DomMapBuilder;
import com.comapping.android.model.Map;
import com.comapping.android.model.MapBuilder;
import com.comapping.android.model.SaxMapBuilder;
import com.comapping.android.model.Topic;
import com.comapping.android.model.TopicComparator;
import com.comapping.android.model.exceptions.MapParsingException;
import com.comapping.android.model.exceptions.StringToXMLConvertionException;
import com.comapping.android.storage.SqliteMapCache;
import com.comapping.android.storage.Storage;
import com.comapping.android.view.metamap.InternetView;
import com.comapping.android.view.metamap.MetaMapView;
import com.comapping.android.view.metamap.SdcardView;

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
	private static MetaMapView currentView = null;

	private static InternetView internetView = null;
	private static SdcardView sdcardView = new SdcardView();

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

		// init internetView if needed
		if (internetView == null) {
			metaMapRefresh(false);
		} else {
			if (currentView instanceof InternetView) {
				switchView(internetView);
			}
		}

		// activate sdcardView if needed
		if ((currentView != null) && (currentView instanceof SdcardView)) {
			switchView(sdcardView);
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
		}

		return false;
	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

		int itemId = (int) info.id;

		if (!currentTopicChildren[itemId].isFolder()) {
			String mapId = currentTopicChildren[itemId].getMapRef();

			switch (item.getItemId()) {
			case R.id.openWithComappingView:
				loadMap(mapId, ViewType.COMAPPING_VIEW, false);
				break;
			case R.id.openWithExplorerView:
				loadMap(mapId, ViewType.EXPLORER_VIEW, false);
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

		try {
			client.applicationClose(this);
		} catch (ConnectionException e) {
			Log.e(Log.metaMapControllerTag, "Connection exception in logout");
		}

		super.onDestroy();
	}

	// MetaMap methods
	public static MapProvider getCurrentMapProvider() {
		return (currentView instanceof InternetView) ? client : fileMapProvider;
	}

	public static MetaMapActivity getInstance() {
		return instance;
	}

	public void switchView(MetaMapView view) {
		currentView = view;

		currentView.activate(this);
	}

	public void switchView() {
		if (currentView instanceof SdcardView) {
			switchView(internetView);
		} else {
			switchView(sdcardView);
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

	public void synchronize() {
		metaMapRefresh(true);
	}

	private void metaMapRefresh(final boolean ignoreCache) {
		final MetaMapActivity context = this;

		new Thread() {
			public void run() {
				String result = "";

				splashActivate("Downloading map list");

				try {
					result = client.getComap("meta", context, ignoreCache);
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

				internetView = new InternetView(metaMap);

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						switchView(internetView);
					}
				});
			}
		}.start();
	}

	public void loadMetaMapTopic(final Topic topic) {
		currentView.prepareTopic(topic);

		currentTopicChildren = topic.getChildTopics();

		Arrays.sort(currentTopicChildren, new TopicComparator());

		currentView.drawMetaMapTopic(topic, currentTopicChildren);
	}

	public void loadMap(final String mapId, final ViewType viewType, boolean ignoreCache) {
		Intent intent = new Intent(MapActivity.MAP_ACTIVITY_INTENT);

		intent.putExtra(MapActivity.EXT_MAP_ID, mapId);
		intent.putExtra(MapActivity.EXT_VIEW_TYPE, viewType.toString());
		intent.putExtra(MapActivity.EXT_IS_IGNORE_CACHE, ignoreCache);

		startActivityForResult(intent, MAP_REQUEST);
	}

	public void logout() {
		Storage.getInstance().set("key", "");

		try {
			client.logout(this);
		} catch (ConnectionException e) {
			Log.e(Log.metaMapControllerTag, "connection exception in logout");
		}

		metaMapRefresh(false);
	}

	public void preferences() {
		startActivity(new Intent(PreferencesActivity.PREFERENCES_ACTIVITY_INTENT));
	}
}