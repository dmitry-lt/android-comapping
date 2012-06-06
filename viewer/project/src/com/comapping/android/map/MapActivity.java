package com.comapping.android.map;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ZoomControls;

import com.comapping.android.Constants;
import com.comapping.android.Log;
import com.comapping.android.Options;
import com.comapping.android.preferences.PreferencesStorage;
import com.comapping.android.provider.communication.Client;
import com.comapping.android.R;
import com.comapping.android.map.model.exceptions.MapParsingException;
import com.comapping.android.map.model.exceptions.StringToXMLConvertionException;
import com.comapping.android.map.model.map.Map;
import com.comapping.android.map.model.map.Topic;
import com.comapping.android.map.model.map.builder.XmlBuilder;
import com.comapping.android.map.render.MapRender;
import com.comapping.android.map.render.comapping.ComappingRender;
import com.comapping.android.map.render.explorer.ExplorerRender;
import com.comapping.android.metamap.MetaMapActivity;
import com.comapping.android.provider.contentprovider.MapContentProvider;
import com.comapping.android.provider.contentprovider.exceptions.ConnectionRuntimeException;
import com.comapping.android.provider.contentprovider.exceptions.LoginInterruptedRuntimeException;
import com.comapping.android.provider.contentprovider.exceptions.MapNotFoundException;
import com.comapping.android.storage.MemoryCache;

public class MapActivity extends Activity {

	// ===========================================================
	// Identifiers for our menu items.
	// ===========================================================

	private final static int MENU_ZOOM = Menu.FIRST;
	private final static int MENU_SEARCH = Menu.FIRST + 1;
	private final static int MENU_SWITCH_VIEW = Menu.FIRST + 2;
	private final static int MENU_SYNCHRONIZE = Menu.FIRST + 3;
	private final static int MENU_SAVE_AS = Menu.FIRST + 4;

	// ===========================================================
	// Current MapActivity
	// ===========================================================

	private static MapActivity currentActivity = null;

	public static final MapActivity getCurrentActivity() {
		return currentActivity;
	}

	// ===========================================================
	// Opening map by starting new MapActivity
	// ===========================================================

	public static void openMap(final String mapRef, final String viewType,
			boolean ignoreCache, Activity parent) {
		Intent intent = new Intent(Constants.MAP_ACTIVITY_INTENT);

		intent.putExtra(MapActivity.EXT_MAP_REF, mapRef);
		intent.putExtra(MapActivity.EXT_VIEW_TYPE, viewType);
		intent.putExtra(MapActivity.EXT_IS_IGNORE_CACHE, ignoreCache);

		parent.startActivityForResult(intent, Constants.ACTION_MAP_REQUEST);
	}

	// ===========================================================
	// Intent parameters
	// ===========================================================

	public static final String EXT_VIEW_TYPE = "viewType";
	public static final String EXT_MAP_REF = "mapRef";
	public static final String EXT_IS_IGNORE_CACHE = "ignoreCache";
	public static final String SAVE_FOLDER = "sdcard\\comapping";

	// ===========================================================
	// Saved parameters for view
	// ===========================================================

	static private String viewType;
	static private String mapRef;
	static private String mapPath;
	static private boolean ignoreCache;

	// ===========================================================
	// Map variables
	// ===========================================================

	private Map map;
	private MapRender mapRender;

	// ===========================================================
	// Misc
	// ===========================================================

	private ProgressDialog splash = null;
	private Thread mapProcessingThread;

	private boolean canDraw = true;

	// ===========================================================
	// Zoom controls variables
	// ===========================================================

	// ===========================================================
	// Controls of view
	// ===========================================================

	private ZoomControls zoom;
	private MapView view;
	private ImageButton prev;
	private ImageButton next;
	private ImageButton cancel;
	private TextView queryTextView;
	private String searchQuery = "";

	// ===========================================================
	// Splash control
	// ===========================================================

	public void splashActivate(final String message, final boolean cancelable) {
		final Activity context = this;

		runOnUiThread(new Runnable() {
			public void run() {
				if (splash == null) {
					splash = ProgressDialog.show(context, getString(R.string.MapActivitySplashTitle), message);
					splash.setOnCancelListener(new OnCancelListener() {

						public void onCancel(DialogInterface dialog) {
							mapProcessingThread.interrupt();
							mapProcessingThread
									.setPriority(Thread.MIN_PRIORITY);
							finish();
						}
					});
				} else {
					splash.setMessage(message);
				}
				splash.setCancelable(cancelable);
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

	private void showError(final String message, final boolean finish) {
		final Activity activity = this;

		activity.runOnUiThread(new Runnable() {
			public void run() {
				Dialog dialog = (new AlertDialog.Builder(activity).setTitle(
						getString(R.string.MapActivityErrorDialogTitle)).setMessage(message).setNeutralButton(getString(R.string.NeutralButtonText),
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								if (finish) {
									activity.finish();
								}
							}
						})).create();
				dialog.show();
			}
		});
	}

	// ===========================================================
	// Misc
	// ===========================================================

	public boolean canDraw() {
		return canDraw;
	}

	// ===========================================================
	// Map Loading
	// ===========================================================

	Map loadMap() throws StringToXMLConvertionException, MapParsingException {
		final Map map;
		if (!MemoryCache.has(mapRef) || (ignoreCache)) {
			splashActivate(getString(R.string.DownloadingMap), false);
			String result = "";

			try {
				result = MapContentProvider.getComap(mapRef, ignoreCache,
						false, this);
			} catch (MapNotFoundException e) {
				Log.w(Log.MAP_CONTROLLER_TAG, e.toString());
				showError(getString(R.string.MapNotFoundOnServer), false);
				result = MapContentProvider.getComap(mapRef, false, true, this);
			}

			if (result == null) {
				result = "";
			}

			// Log.e(Log.CONNECTION_TAG, result);

			splashActivate(getString(R.string.ParsingMap), true);
			map = MetaMapActivity.mapBuilder.buildMap(result);

			// add to cache
			MemoryCache.set(mapRef, map);
		} else {
			map = (Map) MemoryCache.get(mapRef);
		}
		return map;
	}

	// ===========================================================
	// Init
	// ===========================================================

	void initLayout() {
		runOnUiThread(new Runnable() {

			public void run() {

				setContentView(R.layout.map);
				saveConstrols();
				initConstrols();
			};
		});
	}

	void saveConstrols() {
		zoom = (ZoomControls) findViewById(R.id.zoom);
		prev = (ImageButton) findViewById(R.id.previousButton);
		next = (ImageButton) findViewById(R.id.nextButton);
		cancel = (ImageButton) findViewById(R.id.cancelButton);
		queryTextView = (TextView) findViewById(R.id.query);
		view = (MapView) findViewById(R.id.mapView);
	}

	void initConstrols() {
		// View

		LinearLayout findLayout = (LinearLayout) findViewById(R.id.findView);
		view.setSearchUI(findLayout, cancel, next, prev, queryTextView);
		view.setRender(mapRender);
		view.setZoom(zoom);
		view.setActivity(this);

		// Zoom

		view.hideZoom();
		zoom.setIsZoomInEnabled(false);
		zoom.setOnZoomInClickListener(new OnClickListener() {

			public void onClick(View v) {
				view.setScale(view.getScale() + 0.1f);
				view.refresh();
			}
		});
		zoom.setOnZoomOutClickListener(new OnClickListener() {

			public void onClick(View v) {
				view.setScale(view.getScale() - 0.1f);
				view.refresh();
			}
		});
	}

	void parseIntentParameters() {
		Bundle extras = getIntent().getExtras();

		try {
			viewType = extras.getString(EXT_VIEW_TYPE);
			mapRef = extras.getString(EXT_MAP_REF);
			mapPath = mapRef.substring(10);
			if (!mapPath.startsWith("sdcard")) {
				mapPath = "";
			}
			ignoreCache = extras.getBoolean(EXT_IS_IGNORE_CACHE);
		} catch (Exception e) {
			// TODO Empty catch
		}
	}

	public MapRender initMapRender(Map map, String viewType) {
		if (viewType.equals(Constants.VIEW_TYPE_EXPLORER))
			return new ExplorerRender(this, map);
		else if (viewType.equals(Constants.VIEW_TYPE_COMAPPING))
			return new ComappingRender(this, map);
		else
			return null;
	}

	// ===========================================================
	// Life cycle
	// ===========================================================

	public boolean onSearchProcess() {
		if (Intent.ACTION_SEARCH.equals(getIntent().getAction())) {
			String query = getIntent().getStringExtra(SearchManager.QUERY);
			currentActivity.setQuery(query);
			finish();
			return true;
		} else
			return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (((resultCode == RESULT_CANCELED) && (requestCode == Client.LOGIN_REQUEST_CODE))
				|| (resultCode == Options.RESULT_CHAIN_CLOSE)) {
			setResult(Options.RESULT_CHAIN_CLOSE);
			Log.i(Log.MAP_CONTROLLER_TAG, "finish");
			finish();
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		new Thread() {
			@Override
			public void run() {
				splashActivate(getString(R.string.LoadingMap), false);
				canDraw = false;
				// Log.d(Log.MAP_CONTROLLER_TAG, "onConfigurationChanged");
				while (!mapRender.canRotate()) {
					try {
						sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				splashDeactivate();
				// Log.d(Log.MAP_CONTROLLER_TAG,
				// "onConfigurationChanged finish");
				canDraw = true;
				view.onRotate();
			}
		}.start();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (onSearchProcess())
			return;
		
		splashActivate(getString(R.string.LoadingMap), true);

		parseIntentParameters();

		currentActivity = this;

		final Activity current = this;

		mapProcessingThread = new Thread() {
			@Override			
			public void run() {
				try {
					map = loadMap();

					// Canceled
					if (interrupted()) {
						return;
					}

					splashActivate(getString(R.string.LoadingMap), true);

					mapRender = initMapRender(map, viewType);

					initLayout();

					// Canceled
					while (view == null || !view.isInitialized()) {
						if (interrupted()) {
							current.finish();
							return;
						}
						sleep(100);
					}

					splashDeactivate();

				} catch (LoginInterruptedRuntimeException e) {
					Log.e(Log.MAP_CONTROLLER_TAG, e.toString());
					finish();
				} catch (ConnectionRuntimeException e) {
					Log.e(Log.MAP_CONTROLLER_TAG, e.toString());
					showError(getString(R.string.ConnectionError), true);
				} catch (StringToXMLConvertionException e) {
					Log.e(Log.MAP_CONTROLLER_TAG, e.toString());
					showError(getString(R.string.WrongFileFormat), true);
				} catch (MapParsingException e) {
					Log.e(Log.MAP_CONTROLLER_TAG, e.toString());
					showError(getString(R.string.WrongFileFormat), true);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};

		mapProcessingThread.start();
	}

	@Override
	protected void onResume() {
		super.onResume();
		PreferencesStorage.updateFullScreenStatus(this);
	}

	private void saveMapAs() {
		AlertDialog dialog = (new AlertDialog.Builder(this).setIcon(
				android.R.drawable.ic_dialog_info).setTitle(getString(R.string.SaveMapDialogTitle))
				.setMessage(
						String.format(getString(R.string.SaveMapDialogMessageFormat), map.getName(), SAVE_FOLDER))
				.setPositiveButton(getString(R.string.PositiveButtonText),
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
												int which) {
								if (Environment
										.getExternalStorageState()
										.equals(
												Environment.MEDIA_MOUNTED)) {
									String path = SAVE_FOLDER + "\\"
											+ map.getName() + ".comap";

									File file = new File(SAVE_FOLDER);
									file.mkdirs();

									file = new File(path);
									try {
										file.createNewFile();
										FileOutputStream output = new FileOutputStream(
												file);
										output.write(new XmlBuilder().buildXml(
												map).getBytes());
										output.close();
									} catch (IOException e) {

									}
								} else {
									(new AlertDialog.Builder(
											getCurrentActivity()).setIcon(
											android.R.drawable.ic_dialog_alert)
											.setTitle(getString(R.string.MapActivityAlertDialogTitle))
											.setMessage(getString(R.string.CannotDownloadSdNotInstalled)))
											.show();
								}
							}
						}).setNegativeButton(getString(R.string.NegativeButtonText),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {

					}
				})).create();

		dialog.show();
	}
	
	@Override
	protected void onDestroy() {
		splashDeactivate();
		super.onDestroy();
	}

	// ===========================================================
	// Search
	// ===========================================================

	public void setQuery(String s) {
		searchQuery = s.toLowerCase();

		ArrayList<Topic> searchResult = new ArrayList<Topic>();

		search(searchQuery, map.getRoot(), searchResult);

		view.onSearch(searchResult, s);
	}

	private void search(String query, Topic root, ArrayList<Topic> resultList) {
		if (root == null)
			return;

		if (root.getText().toLowerCase().contains(query)) {
			resultList.add(root);
		}

		for (Topic i : root.getChildTopics()) {
			search(query, i, resultList);
		}
	}

	// ===========================================================
	// Options Menu
	// ===========================================================
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(0, MENU_SEARCH, 0, getString(R.string.MenuSearch)).setIcon(
				android.R.drawable.ic_menu_search);

		menu.add(0, MENU_ZOOM, 0, getString(R.string.MenuZoom)).setIcon(
				android.R.drawable.ic_menu_zoom);

		menu.add(0, MENU_SWITCH_VIEW, 0, getString(R.string.MenuSwitchView)).setIcon(
				android.R.drawable.ic_menu_mapmode);

		menu.add(0, MENU_SYNCHRONIZE, 0, getString(R.string.MenuSynchronize)).setIcon(
				R.drawable.menu_sync);

		menu.add(0, MENU_SAVE_AS, 0, getString(R.string.MenuSaveAs)).setIcon(
				android.R.drawable.ic_menu_save);

		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_SEARCH:
			onSearchRequested();
			return true;
		case MENU_ZOOM:
			view.showZoom();
			return true;
		case MENU_SYNCHRONIZE:
			finish();
			openMap(mapRef, viewType, true, this);
			return true;
		case MENU_SAVE_AS:
			saveMapAs();
			return true;
		case MENU_SWITCH_VIEW:
			finish();
			if (viewType.equals(Constants.VIEW_TYPE_COMAPPING)) {
				viewType = Constants.VIEW_TYPE_EXPLORER;
			} else {
				viewType = Constants.VIEW_TYPE_COMAPPING;
			}
			openMap(mapRef, viewType, false, this);
			return true;
		}

		return false;
	}

}