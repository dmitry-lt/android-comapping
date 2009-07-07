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

import com.comapping.android.Constants;
import com.comapping.android.Log;
import com.comapping.android.ViewType;
import com.comapping.android.communication.CachingClient;
import com.comapping.android.communication.Client;
import com.comapping.android.communication.FileMapProvider;
import com.comapping.android.communication.MapProvider;
import com.comapping.android.communication.exceptions.ConnectionException;
import com.comapping.android.controller.MapActivity;
import com.comapping.android.controller.PreferencesActivity;
import com.comapping.android.controller.R;
import com.comapping.android.metamap.provider.ComappingProvider;
import com.comapping.android.metamap.provider.SdCardProvider;
import com.comapping.android.model.map.builder.MapBuilder;
import com.comapping.android.model.map.builder.SaxMapBuilder;
import com.comapping.android.storage.PreferencesStorage;

public class MetaMapActivity extends Activity {

	public static MetaMapActivity instance = null;

	// constants
	public static final int MAP_REQUEST = 5523;

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

	// activity methods

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		instance = this;

		MetaMapView.loadLayout(this);

		if (internetView == null)
			internetView = new MetaMapView(new ComappingProvider(this));

		if (currentView == internetView) {
			switchView(internetView);
		} else {
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

	// public boolean onOptionsItemSelected(MenuItem item) {
	// switch (item.getItemId()) {
	// case R.id.preferences:
	// preferences();
	// return true;
	// case R.id.logout:
	// logout();
	// return true;
	// }
	//
	// return false;
	// }

	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();

		int itemPos = info.position;

		MetaMapItem itm = currentView.provider.getCurrentLevel()[itemPos];
		if (!itm.isFolder) {

			switch (item.getItemId()) {
			case R.id.openWithComappingView:
				loadMap(itm.reference, ViewType.COMAPPING_VIEW, false, this);
				break;
			case R.id.openWithExplorerView:
				loadMap(itm.reference, ViewType.EXPLORER_VIEW, false, this);
				break;
			}
		} else {
			currentView.provider.gotoFolder(itemPos);
			currentView.updateMetaMap();
		}

		return true;
	}

	protected void onDestroy() {

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

	public static void loadMap(final String mapId, final ViewType viewType,
			boolean ignoreCache, Activity parent) {
		Intent intent = new Intent(MapActivity.MAP_ACTIVITY_INTENT);

		intent.putExtra(MapActivity.EXT_MAP_ID, mapId);
		intent.putExtra(MapActivity.EXT_VIEW_TYPE, viewType.toString());
		intent.putExtra(MapActivity.EXT_IS_IGNORE_CACHE, ignoreCache);

		if (currentView == internetView) {
			intent.putExtra(MapActivity.EXT_DATA_SOURCE,
					Constants.DATA_SOURCE_COMAPPING);
		} else {
			intent.putExtra(MapActivity.EXT_DATA_SOURCE,
					Constants.DATA_SOURCE_SD);
		}

		parent.startActivityForResult(intent, MAP_REQUEST);
	}

	// public void logout() {
	// PreferencesStorage.set("key", "");
	//
	// try {
	// CachingClient client = Client.getClient(this);
	// client.logout(this);
	// } catch (ConnectionException e) {
	// Log
	// .e(Log.META_MAP_CONTROLLER_TAG,
	// "connection exception in logout");
	// }
	// }
	//
	// public void preferences() {
	// startActivity(new Intent(
	// PreferencesActivity.PREFERENCES_ACTIVITY_INTENT));
	// }
}