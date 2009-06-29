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
import com.comapping.android.model.TopicComparator;
import com.comapping.android.model.exceptions.MapParsingException;
import com.comapping.android.model.exceptions.StringToXMLConvertionException;
import com.comapping.android.model.map.Map;
import com.comapping.android.model.map.Topic;
import com.comapping.android.model.map.builder.MapBuilder;
import com.comapping.android.model.map.builder.SaxMapBuilder;
import com.comapping.android.storage.SqliteMapCache;
import com.comapping.android.storage.Storage;
import com.comapping.android.view.metamap.InternetView;
import com.comapping.android.view.metamap.MetaMapView;
import com.comapping.android.view.metamap.SdcardView;

public class MetaMapActivity extends Activity {
	// constants
	public static final int MAP_REQUEST = 5523;
	private static final String LOADING_MESSAGE = "Loading map list";
	
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
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		instance = this;

		if (client == null) {
			client = new CachingClient(new Client(), new SqliteMapCache(this));
		}

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
	
	public boolean onPrepareOptionsMenu(Menu menu) {
		Integer currentMenu = currentView.getOptionsMenu();
		Log.d(Log.META_MAP_CONTROLLER_TAG, "On create options menu. Current menu: "+currentMenu);
		
		if (currentMenu != null) {
			menu.clear();
			
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(currentMenu, menu);
			return true;
		} else {
			return false;
		}
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

	
	protected void onDestroy() {
		splashDeactivate();

		try {
			client.applicationClose(this);
		} catch (ConnectionException e) {
			Log.e(Log.META_MAP_CONTROLLER_TAG, "Connection exception in logout");
		}

		super.onDestroy();
	}

	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == MAP_REQUEST) {
			currentView.activate(this);
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	// MetaMap methods
	public static MapProvider getCurrentMapProvider() {
		return (currentView instanceof InternetView) ? client : fileMapProvider;
	}

	public static MetaMapActivity getInstance() {
		return instance;
	}

	public String getFolderDescription(Topic topic) {
		return currentView.getFolderDescription(topic);
	}
	
	public String getMapDescription(Topic topic) {
		return currentView.getMapDescription(topic);
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

				splashActivate(LOADING_MESSAGE);
				String error = null;
				
				try {
					result = client.getComap("meta", context, ignoreCache, !ignoreCache);
				} catch (ConnectionException e) {
					error = InternetView.PROBLEMS_WHILE_RETRIEVING_MESSAGE; // TODO: different messages
					Log.e(Log.META_MAP_CONTROLLER_TAG, "connection error in metamap retrieving");
				} catch (LoginInterruptedException e) {
					error = InternetView.PROBLEMS_WHILE_RETRIEVING_MESSAGE; // TODO: different messages
					Log.e(Log.META_MAP_CONTROLLER_TAG, "login interrupted in metamap retrieving");
				} catch (InvalidCredentialsException e) {
					error = InternetView.PROBLEMS_WHILE_RETRIEVING_MESSAGE; // TODO: different messages
					Log.e(Log.META_MAP_CONTROLLER_TAG, "invalid credentails while getting comap oO");
				}

				Map metaMap = null;
				if (error == null) {
					// retrieving was successful
					try {
						if (result != null) {
							metaMap = mapBuilder.buildMap(result);
						}
					} catch (StringToXMLConvertionException e) {
						Log.e(Log.META_MAP_CONTROLLER_TAG, "xml convertion exception");
						error = InternetView.PROBLEMS_WITH_MAP_MESSAGE;
					} catch (MapParsingException e) {
						Log.e(Log.META_MAP_CONTROLLER_TAG, "map parsing exception");
						error = InternetView.PROBLEMS_WITH_MAP_MESSAGE;
					}
					}
				
				splashDeactivate();

				internetView = new InternetView(metaMap);
				if (error != null) {
					internetView.setError(error);
				}
				
				runOnUiThread(new Runnable() {
					
					public void run() {
						switchView(internetView);
					}
				});
			}
		}.start();
	}

	public void loadMetaMapTopic(final Topic topic) {
		if (topic == null) {
			return;
		}
		
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
			Log.e(Log.META_MAP_CONTROLLER_TAG, "connection exception in logout");
		}

		metaMapRefresh(false);
	}

	public void preferences() {
		startActivity(new Intent(PreferencesActivity.PREFERENCES_ACTIVITY_INTENT));
	}
}