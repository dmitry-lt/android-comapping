/*
 * MetaMapView Controller
 * Android Comapping, 2009
 * Korshakov Stepan
 * 
 * Class implements MetaMapActivity
 */

package com.comapping.android.metamap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

import com.comapping.android.Constants;
import com.comapping.android.Log;
import com.comapping.android.Options;
import com.comapping.android.ViewType;
import com.comapping.android.communication.CachingClient;
import com.comapping.android.communication.Client;
import com.comapping.android.communication.exceptions.ConnectionException;
import com.comapping.android.controller.MapActivity;
import com.comapping.android.controller.PreferencesActivity;
import com.comapping.android.controller.R;
import com.comapping.android.metamap.provider.ComappingProvider;
import com.comapping.android.metamap.provider.MetaMapProvider;
import com.comapping.android.metamap.provider.SdCardProvider;
import com.comapping.android.model.map.builder.MapBuilder;
import com.comapping.android.model.map.builder.SaxMapBuilder;
import com.comapping.android.storage.PreferencesStorage;

public class MetaMapActivity extends Activity {

	// Button id's

	private static final int UP_LEVEL = R.id.upLevelButton;
	private static final int HOME = R.id.homeButton;
	private static final int SYNC = R.id.synchronizeButton;

	protected static final String DEFAULT_MAP_DESCRIPTION = "Map";
	protected static final String DEFAULT_FOLDER_DESCRIPTION = "Folder";

	public static MetaMapActivity instance = null;

	// public variables
	public static MapBuilder mapBuilder = new SaxMapBuilder();

	private static SdCardProvider sdCardProvider = null;
	private static ComappingProvider comappingProvider = null;
	private static MetaMapProvider currentProvider = null;

	
	// ====================================================
	// Live cycle
	// ====================================================

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		instance = this;

		setContentView(R.layout.metamap);

		initControls();

		// Init providers

		if (comappingProvider == null)
			comappingProvider = new ComappingProvider(this);

		if (sdCardProvider == null)
			sdCardProvider = new SdCardProvider();

		// set provider

		if (currentProvider == null)
			enableProvider(comappingProvider);
		else
			enableProvider(currentProvider);
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

	// WTF?
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Constants.ACTION_MAP_REQUEST) {
			initControls();
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	public void preferences() {
		startActivity(new Intent(
				PreferencesActivity.PREFERENCES_ACTIVITY_INTENT));
	}

	void openMap(final String mapId, final ViewType viewType,
			boolean ignoreCache) {
		if (currentProvider == comappingProvider)
			MapActivity.openMap(mapId, viewType, ignoreCache, this,
					Constants.DATA_SOURCE_COMAPPING);
		else
			MapActivity.openMap(mapId, viewType, ignoreCache, this,
					Constants.DATA_SOURCE_SD);
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
	}

	public void sync() {
		new Thread() {
			public void run() {
				currentProvider.sync();

				runOnUiThread(new Runnable() {
					public void run() {
						updateMetaMap();
					}
				});
			}
		}.start();
	}

	// ====================================================
	// View Logic
	// ====================================================
	
	public void updateMetaMap() {

		if (currentProvider == null)
			return;

		// list view
		ListView listView = (ListView) findViewById(R.id.listView);

		MetaMapItem[] items = currentProvider.getCurrentLevel();
		listView.setAdapter(new MetaMapListAdapter(this, items));

		// Buttons

		if (currentProvider.canGoHome())
			enableButton(HOME);
		else
			disableButton(HOME);

		if (currentProvider.canGoUp())
			enableButton(UP_LEVEL);
		else
			disableButton(UP_LEVEL);

		if (currentProvider.canSync())
			enableButton(SYNC);
		else
			disableButton(SYNC);

		if (currentProvider == comappingProvider) {
			((ImageButton) findViewById(R.id.viewSwitcher))
					.setImageResource(R.drawable.metamap_sdcard);
		} else {
			((ImageButton) findViewById(R.id.viewSwitcher))
					.setImageResource(R.drawable.metamap_internet);
		}

	}

	public void switchProvider() {
		if (currentProvider != sdCardProvider) {
			enableProvider(sdCardProvider);
		} else {
			enableProvider(comappingProvider);
		}
	}

	// ====================================================
	// View Controls Manipulation 
	// ====================================================

	void enableButton(int id) {
		int resource = 0;
		ImageButton button = (ImageButton) findViewById(id);

		switch (id) {
		case UP_LEVEL:
			resource = R.drawable.metamap_up;
			break;
		case HOME:
			resource = R.drawable.metamap_home;
			break;
		case SYNC:
			resource = R.drawable.menu_reload;
			break;

		default:
			return;
		}

		button.setEnabled(true);
		button.setFocusable(true);

		button.setImageResource(resource);
	}

	void disableButton(int id) {
		int resource = 0;
		ImageButton button = (ImageButton) findViewById(id);

		switch (id) {
		case UP_LEVEL:
			resource = R.drawable.metamap_up_grey;
			break;
		case HOME:
			resource = R.drawable.metamap_home_grey;
			break;
		case SYNC:
			resource = R.drawable.menu_reload_grey;
			break;

		default:
			return;
		}

		button.setEnabled(false);
		button.setFocusable(false);

		button.setImageResource(resource);
	}

	void enableProvider(MetaMapProvider prov) {
		currentProvider = prov;

		updateMetaMap();
	}

	// ====================================================
	// View Controls Init
	// ====================================================
	
	void initControls() {
		initButtons();
		initListView();

		updateMetaMap();
	}

	void initButtons() {

		// Sync

		ImageButton synchronizeButton = (ImageButton) findViewById(R.id.synchronizeButton);

		synchronizeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {

				if (currentProvider == null)
					return;

				currentProvider.sync();
			}
		});

		// Switch view

		ImageButton switchButton = (ImageButton) findViewById(R.id.viewSwitcher);

		switchButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				switchProvider();
			}
		});

		// Home

		ImageButton homeButton = (ImageButton) findViewById(R.id.homeButton);

		homeButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				if (currentProvider == null)
					return;

				currentProvider.goHome();
				updateMetaMap();
			}
		});

		// Up level

		ImageButton upLevelButton = (ImageButton) findViewById(R.id.upLevelButton);

		upLevelButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				if (currentProvider == null)
					return;

				currentProvider.goUp();
				updateMetaMap();
			}
		});

		// Sync

		ImageButton syncButton = (ImageButton) findViewById(R.id.synchronizeButton);

		syncButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				if (currentProvider == null)
					return;

				currentProvider.sync();
				updateMetaMap();
			}
		});
	}

	void initListView() {
		ListView listView = (ListView) findViewById(R.id.listView);
		registerForContextMenu(listView);

		listView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> apapterView, View view,
					int position, long arg3) {

				if (currentProvider == null)
					return;
				// get viewType

				if (currentProvider.getCurrentLevel()[position].isFolder) {
					currentProvider.gotoFolder(position);
					updateMetaMap();
				} else {

					String viewType = PreferencesStorage.get(
							PreferencesStorage.VIEW_TYPE_KEY,
							Options.DEFAULT_VIEW_TYPE);

					openMap(
							currentProvider.getCurrentLevel()[position].reference,
							ViewType.getViewTypeFromString(viewType), false);

				}
			}
		});
	}

	// ====================================================
	// Context Menu
	// ====================================================

	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, view, menuInfo);

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		MenuInflater inflater = getMenuInflater();

		int toInflate;

		if (!currentProvider.getCurrentLevel()[info.position].isFolder) {
			toInflate = R.menu.metamap_map_context;
		} else {
			toInflate = R.menu.metamap_folder_context;
		}

		inflater.inflate(toInflate, menu);
	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();

		int itemPos = info.position;

		MetaMapItem itm = currentProvider.getCurrentLevel()[itemPos];
		if (!itm.isFolder) {

			switch (item.getItemId()) {
			case R.id.openWithComappingView:
				openMap(itm.reference, ViewType.COMAPPING_VIEW, false);
				break;
			case R.id.openWithExplorerView:
				openMap(itm.reference, ViewType.EXPLORER_VIEW, false);
				break;
			}
		} else {
			currentProvider.gotoFolder(itemPos);
			updateMetaMap();
		}

		return true;
	}

	// ====================================================
	// Options Menu
	// ====================================================

	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.metamap_with_logout_options, menu);
		return true;
	}

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
}