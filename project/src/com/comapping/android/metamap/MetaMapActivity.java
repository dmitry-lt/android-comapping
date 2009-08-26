/*
 * MetaMapView Controller
 * Android Comapping, 2009
 * Korshakov Stepan
 * 
 * Class implements MetaMapActivity
 */

package com.comapping.android.metamap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
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
import com.comapping.android.provider.contentprovider.exceptions.LoginInterruptedRuntimeException;
import com.comapping.android.R;
import com.comapping.android.map.MapActivity;
import com.comapping.android.map.model.map.builder.MapBuilder;
import com.comapping.android.map.model.map.builder.SaxMapBuilder;

public class MetaMapActivity extends Activity {

	// Button id's

	private static final int UP_LEVEL = R.id.upLevelButton;
	private static final int HOME = R.id.homeButton;
	private static final int SYNC = R.id.synchronizeButton;
	private static final int SWITCHER = R.id.viewSwitcher;

	// Identifiers for our menu items.
	private static final int MENU_LOGOUT = Menu.FIRST;
	private static final int MENU_PREFERENCES = Menu.FIRST + 1;
	private static final int MENU_ABOUT = Menu.FIRST + 2;

	protected static final String DEFAULT_MAP_DESCRIPTION = "Map";
	protected static final String DEFAULT_FOLDER_DESCRIPTION = "Folder";

	private static final String PLEASE_SYNCHRONIZE_MESSAGE = "Please synchronize your map list or open SD card view";
	private static final String PLEASE_SYNCHRONIZE_NOSDMESSAGE = "Please synchronize your map list.";
	private static final String EMPTY_FOLDER_MESSAGE = "Folder is empty";

	private static final int MAX_MAP_SIZE_IN_BYTES = 200 * 1024;

	// public variables
	public static MapBuilder mapBuilder = new SaxMapBuilder();

	private static MetaMapProvider sdCardProvider = null;
	private static MetaMapProvider comappingProvider = null;
	private static MetaMapProvider currentProvider = null;

	private MetaMapItem[] metaMapItems;
	private AboutDialog aboutDialog;

	// ====================================================
	// Live Cycle
	// ====================================================

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.metamap);

		initControls();

		// Init providers

		if (comappingProvider == null) {
			if (isSdPresent())
				comappingProvider = new MetaMapProvider(
						ComappingMapContentProvider.INFO,
						PLEASE_SYNCHRONIZE_MESSAGE, EMPTY_FOLDER_MESSAGE, this);
			else
				comappingProvider = new MetaMapProvider(
						ComappingMapContentProvider.INFO,
						PLEASE_SYNCHRONIZE_NOSDMESSAGE, EMPTY_FOLDER_MESSAGE,
						this);
		}

		if (sdCardProvider == null) {
			sdCardProvider = new MetaMapProvider(FileMapContentProvider.INFO,
					EMPTY_FOLDER_MESSAGE, EMPTY_FOLDER_MESSAGE, this);
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

	private String getSize(int size) {
		if (size == -1)
			return "-";

		if (size < 1024) {
			return size + " bytes";
		}
		size /= 1024;
		return size + " Kbytes";
	}

	void openMap(final MetaMapItem item, final String viewType,
			final boolean ignoreCache) {
		final Activity currentActivity = this;
		new Thread() {
			public void run() {
				try {
					item.sizeInBytes = currentProvider.getMapSizeInBytes(item);
				} catch (LoginInterruptedRuntimeException e) {
					return;
				}

				runOnUiThread(new Runnable() {
					public void run() {
						if (item.sizeInBytes > MAX_MAP_SIZE_IN_BYTES) {
							new AlertDialog.Builder(currentActivity)
									.setMessage(
											"       Map is too big. \nMax map size supported is:\n"
													+ getSize(MAX_MAP_SIZE_IN_BYTES)
													+ "\nCurrent map:\n"
													+ item.name + ", "
													+ getSize(item.sizeInBytes))
									.create().show();
						} else {
							MapActivity.openMap(item.reference, viewType,
									ignoreCache, currentActivity);
						}
					}
				});
			}
		}.start();
	}

	public void logout() {
		currentProvider.logout();
		updateMetaMap();
	}

	private ProgressDialog splash = null;

	// ====================================================
	// Splash Functions
	// ====================================================

	public void splashActivate(final String message, final boolean cancelable) {
		final Activity context = this;

		runOnUiThread(new Runnable() {
			public void run() {
				if (splash == null) {
					splash = ProgressDialog.show(context, "Comapping", message);
					// splash.setOnCancelListener(new OnCancelListener() {
					//
					// public void onCancel(DialogInterface dialog) {
					// mapProcessingThread.interrupt();
					// mapProcessingThread
					// .setPriority(Thread.MIN_PRIORITY);
					// finish();
					// }
					// });
				} else {
					splash.setMessage(message);
				}
				splash.setCancelable(cancelable);
			}
		});
	}

	public void sync() {
		final ProgressDialog splash = ProgressDialog.show(this, "Comapping",
				"Loading Map List...");
		final Thread workThread = new Thread() {
			public void run() {
				currentProvider.sync();

				runOnUiThread(new Runnable() {
					public void run() {
						updateMetaMap();
						splash.dismiss();
					}
				});
			}
		};

		splash.setCancelable(true);

		splash.setOnCancelListener(new OnCancelListener() {
			public void onCancel(DialogInterface dialog) {
				splash.dismiss();
				workThread.interrupt();
				workThread.setPriority(Thread.MIN_PRIORITY);

			}
		});

		workThread.start();
	}

	// ====================================================
	// View Logic
	// ====================================================

	public void updateMetaMap() {

		if (currentProvider == null)
			return;

		// list view
		ListView listView = (ListView) findViewById(R.id.listView);

		int position = listView.getFirstVisiblePosition();

		metaMapItems = currentProvider.getCurrentLevel();
		listView.setAdapter(new MetaMapListAdapter(this, metaMapItems));

		position = Math.min(position, metaMapItems.length - 1);
		listView.setSelection(position);

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
			if (isSdPresent())
				((ImageButton) findViewById(SWITCHER))
						.setImageResource(R.drawable.metamap_sdcard);
			else {
				disableButton(SWITCHER);
				((ImageButton) findViewById(SWITCHER))
						.setImageResource(R.drawable.metamap_sdcard_grey);

			}
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
			resource = R.drawable.metamap_sync;
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
			resource = R.drawable.metamap_sync_grey;
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
		
		// Welcome map

		Button welcomeButton = (Button) findViewById(R.id.welcome);
		final Context context = this;
		welcomeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				 new AlertDialog.Builder(context).setMessage("Welcome!").show();
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

				if (metaMapItems[position].isFolder) {
					currentProvider.gotoFolder(position);
					updateMetaMap();
				} else {

					String viewType = PreferencesStorage
							.get(PreferencesStorage.VIEW_TYPE_KEY,
									PreferencesStorage.VIEW_TYPE_DEFAULT_VALUE,
									context);

					openMap(metaMapItems[position], viewType, false);

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

		if (!metaMapItems[info.position].isFolder) {
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

		MetaMapItem itm = metaMapItems[itemPos];
		if (!itm.isFolder) {

			switch (item.getItemId()) {
			case R.id.open:
				String viewType = PreferencesStorage.get(
						PreferencesStorage.VIEW_TYPE_KEY,
						PreferencesStorage.VIEW_TYPE_DEFAULT_VALUE, this);

				openMap(itm, viewType, false);

				break;
			case R.id.openComapping:
				openMap(itm, Constants.VIEW_TYPE_COMAPPING, false);
				break;
			case R.id.openExplorer:
				openMap(itm, Constants.VIEW_TYPE_EXPLORER, false);
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

		menu.add(0, MENU_LOGOUT, 0, "Logout").setIcon(R.drawable.menu_logout)
				.setEnabled(currentProvider.canLogout());

		menu.add(0, MENU_PREFERENCES, 0, "Preferences").setIcon(
				android.R.drawable.ic_menu_preferences);

		menu.add(0, MENU_ABOUT, 0, "About").setIcon(
				android.R.drawable.ic_menu_info_details);

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_PREFERENCES:
			preferences();
			return true;
		case MENU_LOGOUT:
			logout();
			return true;
		case MENU_ABOUT:
			Log.e("" + Log.ERROR,getBaseContext().toString());
			if(aboutDialog==null){
				aboutDialog = new AboutDialog(this);
			}
			aboutDialog.show();
			
			return true;
		}

		return false;
	}
}