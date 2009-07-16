/*
 * MetaMapView Controller
 * Android Comapping, 2009
 * Korshakov Stepan
 * 
 * Class implements MetaMapActivity
 */

package com.comapping.android.metamap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

import com.comapping.android.Constants;
import com.comapping.android.preferences.PreferencesActivity;
import com.comapping.android.preferences.PreferencesStorage;
import com.comapping.android.provider.contentprovider.ComappingMapContentProvider;
import com.comapping.android.provider.contentprovider.FileMapContentProvider;
import com.comapping.android.controller.R;
import com.comapping.android.map.MapActivity;
import com.comapping.android.map.model.map.builder.MapBuilder;
import com.comapping.android.map.model.map.builder.SaxMapBuilder;
import com.comapping.android.metamap.provider.MetaMapProvider;
import com.comapping.android.metamap.provider.MetaMapProviderUsingCP;

public class MetaMapActivity extends Activity {

	// Button id's

	private static final int UP_LEVEL = R.id.upLevelButton;
	private static final int HOME = R.id.homeButton;
	private static final int SYNC = R.id.synchronizeButton;
	private static final int SWITCHER = R.id.viewSwitcher;

	protected static final String DEFAULT_MAP_DESCRIPTION = "Map";
	protected static final String DEFAULT_FOLDER_DESCRIPTION = "Folder";

	private static final String PLEASE_SYNCHRONIZE_MESSAGE = "Please synchronize your map list or open sdcard view";
	private static final String EMPTY_FOLDER_MESSAGE = "Folder is empty";

	// public variables
	public static MapBuilder mapBuilder = new SaxMapBuilder();

	private static MetaMapProviderUsingCP sdCardProvider = null;
	private static MetaMapProviderUsingCP comappingProvider = null;
	private static MetaMapProvider currentProvider = null;

	// ====================================================
	// Live Cycle
	// ====================================================

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.metamap);

		initControls();

		// Init providers

		if (comappingProvider == null) {
			comappingProvider = new MetaMapProviderUsingCP(
					ComappingMapContentProvider.INFO,
					PLEASE_SYNCHRONIZE_MESSAGE, EMPTY_FOLDER_MESSAGE, this);
		}

		if (sdCardProvider == null) {
			sdCardProvider = new MetaMapProviderUsingCP(
					FileMapContentProvider.INFO, EMPTY_FOLDER_MESSAGE,
					EMPTY_FOLDER_MESSAGE, this);
		}

		// set provider

		if (currentProvider == null)
			enableProvider(comappingProvider);
		else
			enableProvider(currentProvider);
	}

	protected void onDestroy() {
		currentProvider.finishWork();
		
		super.onDestroy();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) & (currentProvider.canGoUp())) {
			currentProvider.goUp();
			updateMetaMap();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}
	
	// Refresh list after map opening
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Constants.ACTION_MAP_REQUEST) {
			initControls();
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	// ====================================================
	// Menu Functions
	// ====================================================

	public void preferences() {
		startActivity(new Intent(
				PreferencesActivity.PREFERENCES_ACTIVITY_INTENT));
	}

	void openMap(final String mapRef, final String viewType, boolean ignoreCache) {
		MapActivity.openMap(mapRef, viewType, ignoreCache, this);
	}

	public void logout() {
		currentProvider.logout();
		updateMetaMap();
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

		if (currentProvider != sdCardProvider) {
			((ImageButton) findViewById(SWITCHER))
					.setImageResource(R.drawable.metamap_sdcard);
			if (!isSdPresent())
				disableButton(SWITCHER);
		} else {
			((ImageButton) findViewById(SWITCHER))
					.setImageResource(R.drawable.metamap_internet);
		}

		TextView emptyListText = (TextView) findViewById(R.id.emptyListText);
		emptyListText.setText(currentProvider.getEmptyListText());
	}

	public void switchProvider() {
		if (currentProvider != sdCardProvider) {
			if (isSdPresent())
				enableProvider(sdCardProvider);
		} else {
			enableProvider(comappingProvider);
		}
	}

	private boolean isSdPresent() {
		return android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
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
		case SWITCHER:
			resource = R.drawable.metamap_sdcard;
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

		// Switch view

		ImageButton switchButton = (ImageButton) findViewById(SWITCHER);

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

				sync();
			}
		});
	}

	void initListView() {
		ListView listView = (ListView) findViewById(R.id.listView);
		registerForContextMenu(listView);

		final Context context = this;

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

					String viewType = PreferencesStorage
							.get(PreferencesStorage.VIEW_TYPE_KEY,
									PreferencesStorage.VIEW_TYPE_DEFAULT_VALUE,
									context);

					openMap(
							currentProvider.getCurrentLevel()[position].reference,
							viewType, false);

				}
			}
		});

		listView.setEmptyView(findViewById(R.id.emptyListText));
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
				openMap(itm.reference, Constants.VIEW_TYPE_COMAPPING, false);
				break;
			case R.id.openWithExplorerView:
				openMap(itm.reference, Constants.VIEW_TYPE_EXPLORER, false);
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
		inflater.inflate(R.menu.metamap, menu);

		MenuItem logoutItem = menu.findItem(R.id.logout);
		logoutItem.setEnabled(currentProvider.canLogout());

		if (currentProvider.canLogout()) {
			logoutItem.setIcon(R.drawable.metamap_logout);
		} else {
			logoutItem.setIcon(R.drawable.metamap_logout_grey);
		}

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