/*
 * MetaMapView Controller
 * Android Comapping, 2009
 * Last change: Abishev Timur
 * 
 * Class implements MetaMapView controller
 */

package com.comapping.android.metamap;

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
import com.comapping.android.controller.MapActivity;
import com.comapping.android.controller.PreferencesActivity;
import com.comapping.android.controller.R;
import com.comapping.android.metamap.MetaMapListAdapter.MetaMapItem;
import com.comapping.android.model.exceptions.MapParsingException;
import com.comapping.android.model.exceptions.StringToXMLConvertionException;
import com.comapping.android.model.map.Map;
import com.comapping.android.model.map.Topic;
import com.comapping.android.model.map.builder.MapBuilder;
import com.comapping.android.model.map.builder.SaxMapBuilder;
import com.comapping.android.storage.SqliteMapCache;
import com.comapping.android.storage.PreferencesStorage;

public class MetaMapActivity extends Activity {
	// constants
	public static final int MAP_REQUEST = 5523;
	private static final String LOADING_MESSAGE = "Loading map list";

	// public variables
	// public static CachingClient client = null;
	public static FileMapProvider fileMapProvider = new FileMapProvider();

	public static MapBuilder mapBuilder = new SaxMapBuilder();

	// private variables
	// views
	private static MetaMapView currentView = null;

	private static MetaMapView internetView = null;
	private static MetaMapView sdcardView = new MetaMapView(
			new SdCardProvider());

	//
	private static MetaMapActivity instance;
	private ProgressDialog splash = null;

	// activity methods

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		instance = this;

		MetaMapView.loadLayout(this);

		// init internetView if needed
		if (internetView == null) {
			metaMapRefresh(false);
		} else {
			if (currentView == internetView) {
				switchView(internetView);
			}
		}

		// activate sdcardView if needed
		if (currentView == sdcardView) {
			switchView(sdcardView);
		}
	}

	/* Creates the menu items */

	// public boolean onPrepareOptionsMenu(Menu menu) {
	// Integer currentMenu = currentView.getOptionsMenu();
	//		
	// Log.d(Log.META_MAP_CONTROLLER_TAG,
	// "On create options menu. Current menu: "+currentMenu);
	//		
	// if (currentMenu != null) {
	// menu.clear();
	//			
	// MenuInflater inflater = getMenuInflater();
	// inflater.inflate(currentMenu, menu);
	// return true;
	// } else {
	// return false;
	// }
	// }
	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenuInfo menuInfo) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;

		MenuInflater inflater = getMenuInflater();
		super.onCreateContextMenu(menu, view, menuInfo);

		int toInflate = R.menu.metamap_map_context; // default value

		if (!currentView.provider.getCurrentLevel()[info.position].isFolder) {
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
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();

		int itemId = info.position;

		MetaMapItem itm = currentView.provider.getCurrentLevel()[itemId];
		if (!itm.isFolder) {
			// String mapId = itm.name;
			//
			switch (item.getItemId()) {
			case R.id.openWithComappingView:
				loadMap(itm.reference, ViewType.COMAPPING_VIEW, false);
				break;
			case R.id.openWithExplorerView:
				loadMap(itm.reference, ViewType.EXPLORER_VIEW, false);
				break;
			}
		} else {
			currentView.provider.gotoFolder(itemId);
			currentView.updateMetaMap();
			// loadMetaMapTopic(currentTopicChildren[itemId]);
		}

		return true;
	}

	protected void onDestroy() {
		splashDeactivate();

		try {
			CachingClient client = Client.getClient(this);
			client.applicationClose(this);
		} catch (ConnectionException e) {
			Log
					.e(Log.META_MAP_CONTROLLER_TAG,
							"Connection exception in logout");
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
		return (currentView == internetView) ? Client.getClient(null)
				: fileMapProvider;
	}

	public static MetaMapActivity getInstance() {
		return instance;
	}

	public void switchView(MetaMapView view) {
		currentView = view;

		currentView.activate(this);
	}

	public void switchView() {
		if (currentView != sdcardView) {

			switchView(sdcardView);

		} else {

			switchView(internetView);
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

	public static final String PLEASE_SYNCHRONIZE_MESSAGE = "Please synchronize your map list or open sdcard view";
	public static final String PROBLEMS_WHILE_RETRIEVING_MESSAGE = "There are some problem while map list retrieving.";
	public static final String PROBLEMS_WITH_MAP_MESSAGE = "There are some problem while map list parsing.";

	private void metaMapRefresh(final boolean ignoreCache) {
		final MetaMapActivity context = this;

		new Thread() {
			public void run() {
				String result = "";

				splashActivate(LOADING_MESSAGE);
				String error = null;

				try {
					CachingClient client = Client.getClient(context);
					result = client.getComap("meta", context, ignoreCache,
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

				Map metaMap = null;
				if (error == null) {
					// retrieving was successful
					try {
						if (result != null) {
							metaMap = mapBuilder.buildMap(result);
						}
					} catch (StringToXMLConvertionException e) {
						Log.e(Log.META_MAP_CONTROLLER_TAG,
								"xml convertion exception");
						error = PROBLEMS_WITH_MAP_MESSAGE;
					} catch (MapParsingException e) {
						Log.e(Log.META_MAP_CONTROLLER_TAG,
								"map parsing exception");
						error = PROBLEMS_WITH_MAP_MESSAGE;
					}
				}

				splashDeactivate();

				internetView = new MetaMapView(new ComappingProvider(context,
						metaMap));
				// if (error != null) {
				// internetView.setError(error);
				// }

				runOnUiThread(new Runnable() {

					public void run() {
						switchView(internetView);
					}
				});
			}
		}.start();
	}

	public void loadMap(final String mapId, final ViewType viewType,
			boolean ignoreCache) {
		Intent intent = new Intent(MapActivity.MAP_ACTIVITY_INTENT);

		intent.putExtra(MapActivity.EXT_MAP_ID, mapId);
		intent.putExtra(MapActivity.EXT_VIEW_TYPE, viewType.toString());
		intent.putExtra(MapActivity.EXT_IS_IGNORE_CACHE, ignoreCache);

		startActivityForResult(intent, MAP_REQUEST);
	}

	public void logout() {
		PreferencesStorage.set("key", "");

		try {
			CachingClient client = Client.getClient(this);
			client.logout(this);
		} catch (ConnectionException e) {
			Log
					.e(Log.META_MAP_CONTROLLER_TAG,
							"connection exception in logout");
		}

		// metaMapRefresh(false);
	}

	public void preferences() {
		startActivity(new Intent(
				PreferencesActivity.PREFERENCES_ACTIVITY_INTENT));
	}
}