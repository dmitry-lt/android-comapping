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
import com.comapping.android.provider.communication.CachingClient;
import com.comapping.android.provider.communication.Client;
import com.comapping.android.provider.communication.exceptions.ConnectionException;
import com.comapping.android.provider.communication.exceptions.InvalidCredentialsException;
import com.comapping.android.provider.communication.exceptions.LoginInterruptedException;
import com.comapping.android.controller.R;
import com.comapping.android.controller.R.id;
import com.comapping.android.controller.R.layout;
import com.comapping.android.controller.R.menu;
import com.comapping.android.metamap.MetaMapActivity;
import com.comapping.android.model.exceptions.MapParsingException;
import com.comapping.android.model.exceptions.StringToXMLConvertionException;
import com.comapping.android.model.map.Map;
import com.comapping.android.model.map.Topic;
import com.comapping.android.provider.IMapProvider;
import com.comapping.android.provider.InternetMapProvider;
import com.comapping.android.provider.contentprovider.FileMapContentProvider;
import com.comapping.android.provider.contentprovider.ComappingMapContentProvider;
import com.comapping.android.provider.contentprovider.MapContentProvider;
import com.comapping.android.storage.MemoryCache;
import com.comapping.android.view.comapping.ComappingRender;
import com.comapping.android.view.explorer.ExplorerRender;
import com.comapping.android.view.MapRender;

public class MapActivity extends Activity {

	private static MapActivity currentActivity = null;

	public static final MapActivity getCurrentActivity() {
		return currentActivity;
	}

	public static void openMap(final String mapId, final String viewType,
			boolean ignoreCache, Activity parent, String dataSource) {
		Intent intent = new Intent(Constants.MAP_ACTIVITY_INTENT);

		intent.putExtra(MapActivity.EXT_MAP_ID, mapId);
		intent.putExtra(MapActivity.EXT_VIEW_TYPE, viewType.toString());
		intent.putExtra(MapActivity.EXT_IS_IGNORE_CACHE, ignoreCache);
		intent.putExtra(MapActivity.EXT_DATA_SOURCE, dataSource);

		parent.startActivityForResult(intent, Constants.ACTION_MAP_REQUEST);
	}

	public static final String EXT_VIEW_TYPE = "viewType";
	public static final String EXT_MAP_ID = "mapId";
	public static final String EXT_IS_IGNORE_CACHE = "ignoreCache";
	public static final String EXT_DATA_SOURCE = "dataSource";

	private static final long TIME_TO_HIDE = 2000;

	private ProgressDialog splash = null;
	private Thread mapProcessingThread;

	private String currentMapId = null;
	private String currentViewType = null;

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

	private void onError(final String message, final Activity activity) {
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

	private long lastZoomPress = -100000;
	private boolean zoomVisible = true;

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

	static private String viewType;
	static private String mapId;
	static private boolean ignoreCache;
	static private String dataSource;

	MapRender mapRender;

	private boolean canDraw = true;

	public boolean canDraw() {
		return canDraw;
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

		if (Intent.ACTION_SEARCH.equals(getIntent().getAction())) {
			String query = getIntent().getStringExtra(SearchManager.QUERY);
			currentActivity.setQuery(query);
			finish();
			return;
		}

		currentActivity = this;

		Bundle extras = getIntent().getExtras();

		try {
			viewType = extras.getString(EXT_VIEW_TYPE);
			mapId = extras.getString(EXT_MAP_ID);
			ignoreCache = extras.getBoolean(EXT_IS_IGNORE_CACHE);
			dataSource = extras.getString(EXT_DATA_SOURCE);
		} catch (Exception e) {

		}

		currentMapId = mapId;
		currentViewType = viewType;

		final Activity current = this;
		final MapActivity activity = this;

		mapProcessingThread = new Thread() {
			public void run() {
				try {
					final Map map;
					if (!MemoryCache.has(mapId) || (ignoreCache)) {
						splashActivate("Downloading map", false);
						String result = "";
						// try {
						// if (dataSource == Constants.DATA_SOURCE_COMAPPING)
						// {
						// result = Client.getClient(current).getComap(mapId,
						// current, ignoreCache, false);
						result = MapContentProvider
								.getComap(mapId,
										ComappingMapContentProvider.CONTENT_URI,
										current);
						// } else
						// {
						// result = new
						// InternetMapProvider(current).getComap(mapId, false,
						// current);
						// }
						// } catch (InvalidCredentialsException e) {
						// Log.e(Log.MAP_CONTROLLER_TAG,
						// "invalid credentials while map getting");
						// // TODO: ???
						// }

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

					if (interrupted()) {
						return;
					}

					splashActivate("Loading map", true);
					mapRender = initMapRender(map, viewType);

					runOnUiThread(new Runnable() {

						public void run() {

							setContentView(R.layout.map);

							zoom = (ZoomControls) findViewById(R.id.zoom);
							prev = (ImageButton) findViewById(R.id.previousButton);
							next = (ImageButton) findViewById(R.id.nextButton);
							cancel = (ImageButton) findViewById(R.id.cancelButton);
							queryTextView = (TextView) findViewById(R.id.query);

							LinearLayout findLayout = (LinearLayout) findViewById(R.id.findView);

							Topic topic = map.getRoot();
							allTopicsTexts(topic);
							allTopics(topic);

							view = (MapView) findViewById(R.id.mapView);
							view.setSearchUI(findLayout, cancel, next, prev,
									queryTextView);
							view.setRender(mapRender);
							view.setZoom(zoom);
							view.setActivity(activity);
							hideZoom();
							zoom.setIsZoomInEnabled(false);
							zoom
									.setOnZoomInClickListener(new OnClickListener() {

										public void onClick(View v) {
											view
													.setScale(view.getScale() + 0.1f);
											lastZoomPress = System
													.currentTimeMillis();
											view.refresh();
										}
									});
							zoom
									.setOnZoomOutClickListener(new OnClickListener() {

										public void onClick(View v) {
											view
													.setScale(view.getScale() - 0.1f);
											lastZoomPress = System
													.currentTimeMillis();
											view.refresh();
										}
									});
						};
					});

					while (view == null || !view.isInitialized()) {
						if (interrupted()) {
							current.finish();
							return;
						}
						sleep(100);
					}

					splashDeactivate();

					while (true) {
						if (System.currentTimeMillis() - lastZoomPress > TIME_TO_HIDE) {
							hideZoom();
						}
						sleep(100);
					}
					// } catch (LoginInterruptedException e) {
					// Log.e(Log.MAP_CONTROLLER_TAG, "login interrupted");
					// onError("login interrupted", current);
					// } catch (ConnectionException e) {
					// Log.e(Log.MAP_CONTROLLER_TAG, "connection exception");
					// onError("Connection error", current);
				} catch (StringToXMLConvertionException e) {
					Log.e(Log.MAP_CONTROLLER_TAG, e.toString());
					onError("Wrong file format", current);
				} catch (MapParsingException e) {
					Log.e(Log.MAP_CONTROLLER_TAG, e.toString());
					onError("Wrong file format", current);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		mapProcessingThread.start();
	}

	ZoomControls zoom;
	MapView view;
	ImageButton prev;
	ImageButton next;
	ImageButton cancel;
	TextView queryTextView;
	String searchQuery = "";

	public void setQuery(String s) {
		searchQuery = s.toLowerCase();

		ArrayList<Topic> searchResult = new ArrayList<Topic>();
		for (Topic i : topics) {
			if (i.getText().toLowerCase().contains(searchQuery)) {
				searchResult.add(i);
			}
		}
		view.onSearch(searchResult, s);
	}

	public MapRender initMapRender(Map map, String viewType) {
		if (viewType.equals(Constants.VIEW_TYPE_EXPLORER))
			return new ExplorerRender(this, map);
		else if (viewType.equals(Constants.VIEW_TYPE_COMAPPING))
			return new ComappingRender(this, map);
		else
			return null;
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.map_options, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.zoom:
			showZoom();
			return true;
		case R.id.find:
			// view.showSearchButtins();
			onSearchRequested();
			// view.setlayout(layout, cancel, next, previous, text, topics);
			return true;
		case R.id.mapSynchronizeButton:
			finish();
			openMap(currentMapId, currentViewType, true, this, dataSource);
			return true;
		}
		return false;
	}

	// 
	// public boolean onSearchRequested() {
	// // If your application absolutely must disable search, do it here.
	// // if (mMenuMode.getSelectedItemPosition() == MENUMODE_DISABLED) {
	// // return false;
	// // }
	//
	// // It's possible to prefill the query string before launching the search
	// // UI. For this demo, we simply copy it from the user input field.
	// // For most applications, you can simply pass null to startSearch() to
	// // open the UI with an empty query string.
	// // final String queryPrefill = mQueryPrefill.getText().toString();
	//
	// // Next, set up a bundle to send context-specific search data (if any)
	// // The bundle can contain any number of elements, using any number of
	// // keys;
	// // For this Api Demo we copy a string from the user input field, and
	// // store
	// // it in the bundle as a string with the key "demo_key".
	// // For most applications, you can simply pass null to startSearch().
	// Bundle appDataBundle = null;
	// // final String queryAppDataString = mQueryAppData.getText().toString();
	// // if (queryAppDataString != null) {
	// // appDataBundle = new Bundle();
	// // appDataBundle.putString("demo_key", queryAppDataString);
	// // }
	//
	// // Now call the Activity member function that invokes the Search Manager
	// // UI.
	// startSearch(null, false, null, false);
	//
	// // Returning true indicates that we did launch the search, instead of
	// // blocking it.
	// return true;
	// }

	ArrayList<String> texts = new ArrayList<String>();
	ArrayList<Topic> topics = new ArrayList<Topic>();

	public void allTopics(Topic parent) {
		topics.add(parent);
		for (int i = 0; i < parent.getChildrenCount(); i++) {
			allTopics(parent.getChildByIndex(i));
		}
	}

	public void allTopicsTexts(Topic parent) {
		if (!texts.contains(parent.getText()))
			texts.add(parent.getText());
		for (int i = 0; i < parent.getChildrenCount(); i++) {
			allTopicsTexts(parent.getChildByIndex(i));
		}
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

	protected void onDestroy() {
		splashDeactivate();
		super.onDestroy();
	}
}