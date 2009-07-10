package com.comapping.android.map;

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
import android.view.Menu;
import android.view.MenuInflater;
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
import com.comapping.android.provider.communication.Client;
import com.comapping.android.controller.R;
import com.comapping.android.map.model.exceptions.MapParsingException;
import com.comapping.android.map.model.exceptions.StringToXMLConvertionException;
import com.comapping.android.map.model.map.Map;
import com.comapping.android.map.model.map.Topic;
import com.comapping.android.map.render.MapRender;
import com.comapping.android.map.render.comapping.ComappingRender;
import com.comapping.android.map.render.explorer.ExplorerRender;
import com.comapping.android.metamap.MetaMapActivity;
import com.comapping.android.provider.contentprovider.ComappingMapContentProvider;
import com.comapping.android.provider.contentprovider.MapContentProvider;
import com.comapping.android.storage.MemoryCache;

public class MapActivity extends Activity {
	
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
	
	public static void openMap(final String mapId, final String viewType,
			boolean ignoreCache, Activity parent, String dataSource) {
		Intent intent = new Intent(Constants.MAP_ACTIVITY_INTENT);

		intent.putExtra(MapActivity.EXT_MAP_ID, mapId);
		intent.putExtra(MapActivity.EXT_VIEW_TYPE, viewType.toString());
		intent.putExtra(MapActivity.EXT_IS_IGNORE_CACHE, ignoreCache);
		intent.putExtra(MapActivity.EXT_DATA_SOURCE, dataSource);

		parent.startActivityForResult(intent, Constants.ACTION_MAP_REQUEST);
	}

	// ===========================================================
	// Intent parameters
	// ===========================================================
	
	public static final String EXT_VIEW_TYPE = "viewType";
	public static final String EXT_MAP_ID = "mapId";
	public static final String EXT_IS_IGNORE_CACHE = "ignoreCache";
	public static final String EXT_DATA_SOURCE = "dataSource";

	private static final long ZOOM_CONTROLS_TIME_TO_HIDE = 2000;

	// ===========================================================
	// Saved parameters for view
	// ===========================================================
	
	static private String viewType;
	static private String mapId;
	static private boolean ignoreCache;
	static private String dataSource;
	
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

	private long lastZoomPress = -100000;
	private boolean zoomVisible = true;

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
					splash = ProgressDialog.show(context, "Comapping", message);
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

	private void showError(final String message) {
		final Activity activity = this;

		activity.runOnUiThread(new Runnable() {
			public void run() {
				Dialog dialog = (new AlertDialog.Builder(activity).setTitle(
						"Error").setMessage(message).setNeutralButton("Ok",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								activity.finish();
							}
						})).create();
				dialog.show();
			}
		});
	}

	// ===========================================================
	// Zoom control
	// ===========================================================

	public void showZoom() {
		lastZoomPress = System.currentTimeMillis();
		if (!zoomVisible) {
			zoomVisible = true;
			runOnUiThread(new Runnable() {

				public void run() {
					zoom.show();
				}
			});
		}
	}

	public void hideZoom() {
		if (zoomVisible) {
			zoomVisible = false;
			runOnUiThread(new Runnable() {

				public void run() {
					zoom.hide();
				}
			});
		}
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
		if (!MemoryCache.has(mapId) || (ignoreCache)) {
			splashActivate("Downloading map", false);
			String result = "";
			result = MapContentProvider.getComap(mapId,
					ComappingMapContentProvider.CONTENT_URI, this);

			if (result == null) {
				result = "";
			}

			splashActivate("Parsing map", true);
			map = MetaMapActivity.mapBuilder.buildMap(result);

			// add to cache
			MemoryCache.set(mapId, map);
		} else {
			map = (Map) MemoryCache.get(mapId);
		}
		return map;
	}

	// ===========================================================
	// Init
	// ===========================================================
	
	void initLayout()
	{
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

		hideZoom();
		zoom.setIsZoomInEnabled(false);
		zoom.setOnZoomInClickListener(new OnClickListener() {

			public void onClick(View v) {
				view.setScale(view.getScale() + 0.1f);
				lastZoomPress = System.currentTimeMillis();
				view.refresh();
			}
		});
		zoom.setOnZoomOutClickListener(new OnClickListener() {

			public void onClick(View v) {
				view.setScale(view.getScale() - 0.1f);
				lastZoomPress = System.currentTimeMillis();
				view.refresh();
			}
		});
	}

	void parseIntentParameters() {
		Bundle extras = getIntent().getExtras();

		try {
			viewType = extras.getString(EXT_VIEW_TYPE);
			mapId = extras.getString(EXT_MAP_ID);
			ignoreCache = extras.getBoolean(EXT_IS_IGNORE_CACHE);
			dataSource = extras.getString(EXT_DATA_SOURCE);
		} catch (Exception e) {

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
	// Live cycle
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

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (((resultCode == RESULT_CANCELED) && (requestCode == Client.LOGIN_REQUEST_CODE))
				|| (resultCode == Options.RESULT_CHAIN_CLOSE)) {
			setResult(Options.RESULT_CHAIN_CLOSE);
			Log.i(Log.MAP_CONTROLLER_TAG, "finish");
			finish();
		}

		super.onActivityResult(requestCode, resultCode, data);
	}
	
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		new Thread() {

			public void run() {
				splashActivate("Loading map", false);
				canDraw = false;
				Log.d(Log.MAP_CONTROLLER_TAG, "onConfigurationChanged");
				while (!mapRender.canRotate()) {
					try {
						sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				splashDeactivate();
				Log.d(Log.MAP_CONTROLLER_TAG, "onConfigurationChanged finish");
				view.onRotate();
				canDraw = true;
			}
		}.start();
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (onSearchProcess())
			return;

		parseIntentParameters();

		currentActivity = this;

		final Activity current = this;

		mapProcessingThread = new Thread() {
			public void run() {
				try {
					map = loadMap();

					// Canceled
					if (interrupted()) {
						return;
					}

					splashActivate("Loading map", true);

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

					// Zoom code
					while (true) {
						if (System.currentTimeMillis() - lastZoomPress > ZOOM_CONTROLS_TIME_TO_HIDE) {
							hideZoom();
						}
						sleep(100);
					}

				} catch (StringToXMLConvertionException e) {
					Log.e(Log.MAP_CONTROLLER_TAG, e.toString());
					showError("Wrong file format");
				} catch (MapParsingException e) {
					Log.e(Log.MAP_CONTROLLER_TAG, e.toString());
					showError("Wrong file format");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};

		mapProcessingThread.start();
	}

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

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.map, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.zoom:
			showZoom();
			return true;
		case R.id.find:
			onSearchRequested();
			return true;
		case R.id.mapSynchronizeButton:
			finish();
			openMap(mapId, viewType, true, this, dataSource);
			return true;
		}
		return false;
	}

}