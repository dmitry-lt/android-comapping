package com.comapping.android.controller;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ZoomControls;

import com.comapping.android.Log;
import com.comapping.android.Options;
import com.comapping.android.ViewType;
import com.comapping.android.communication.CachingClient;
import com.comapping.android.communication.Client;
import com.comapping.android.communication.exceptions.ConnectionException;
import com.comapping.android.communication.exceptions.InvalidCredentialsException;
import com.comapping.android.communication.exceptions.LoginInterruptedException;
import com.comapping.android.model.Map;
import com.comapping.android.model.Topic;
import com.comapping.android.model.exceptions.MapParsingException;
import com.comapping.android.model.exceptions.StringToXMLConvertionException;
import com.comapping.android.storage.MemoryCache;
import com.comapping.android.view.ComappingRender;
import com.comapping.android.view.explorer.ExplorerRender;
import com.comapping.android.view.MainMapView;
import com.comapping.android.view.MapRender;

public class MapActivity extends Activity {
	
	private static MapActivity currentActivity = null;
	public static final MapActivity getCurrentActivity()
	{
		return currentActivity;
	}
	
	public static final String MAP_ACTIVITY_INTENT = "com.comapping.android.intent.MAP";

	public static final String EXT_VIEW_TYPE = "viewType";
	public static final String EXT_MAP_ID = "mapId";
	public static final String EXT_IS_IGNORE_CACHE = "ignoreCache";
	private static final long TIME_TO_HIDE = 2000;

	private ProgressDialog splash = null;
	private Thread mapProcessingThread;

	private String currentMapId = null;
	private ViewType currentViewType = null;

	public void splashActivate(final String message, final boolean cancelable) {
		final Activity context = this;

		runOnUiThread(new Runnable() {
			public void run() {
				if (splash == null) {
					splash = ProgressDialog.show(context, "Comapping", message);
					splash.setOnCancelListener(new OnCancelListener() {
						@Override
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
							@Override
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
			zoom.show();
		}
	}

	public void hideZoom() {
		if (zoomVisible) {
			zoomVisible = false;
			zoom.hide();
		}
	}

	static private ViewType viewType;
	static private String mapId;
	static private boolean ignoreCache;

	@Override
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
			viewType = ViewType.getViewTypeFromString(extras
					.getString(EXT_VIEW_TYPE));
			mapId = extras.getString(EXT_MAP_ID);
			ignoreCache = extras.getBoolean(EXT_IS_IGNORE_CACHE);
		} catch (Exception e) {

		}

		currentMapId = mapId;
		currentViewType = viewType;

		final Activity current = this;

		mapProcessingThread = new Thread() {
			public void run() {
				try {
					final Map map;
					if (!MemoryCache.has(mapId) || (ignoreCache)) {
						splashActivate("Downloading map", false);
						String result = "";
						try {
							if (MetaMapActivity.getCurrentMapProvider() instanceof CachingClient) {
								result = ((CachingClient) MetaMapActivity
										.getCurrentMapProvider()).getComap(
										mapId, current, ignoreCache, false);
							} else {
								result = MetaMapActivity
										.getCurrentMapProvider().getComap(
												mapId, current);
							}
						} catch (InvalidCredentialsException e) {
							Log.e(Log.MAP_CONTROLLER_TAG,
									"invalid credentials while map getting");
							// TODO: ???
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
					final MapRender mapRender = initMapRender(map, viewType);

					runOnUiThread(new Runnable() {
						@Override
						public void run() {

							setContentView(R.layout.map);

							zoom = (ZoomControls) findViewById(R.id.Zoom);
							prev = (ImageButton) findViewById(R.id.previousButton);
							next = (ImageButton) findViewById(R.id.nextButton);
							cancel = (ImageButton) findViewById(R.id.cancelButton);
							Topic topic = map.getRoot();
							allTopicsTexts(topic);
							allTopics(topic);

							view = (MainMapView) findViewById(R.id.MapView);
							view.setSearchUI(cancel, next, prev);
							view.setRender(mapRender);
							view.setZoom(zoom);
							hideZoom();
							zoom.setIsZoomInEnabled(false);
							zoom
									.setOnZoomInClickListener(new OnClickListener() {
										@Override
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
										@Override
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
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									hideZoom();
								}
							});
						}
						sleep(100);
					}
				} catch (LoginInterruptedException e) {
					Log.e(Log.MAP_CONTROLLER_TAG, "login interrupted");
					onError("login interrupted", current);
				} catch (ConnectionException e) {
					Log.e(Log.MAP_CONTROLLER_TAG, "connection exception");
					onError("Connection error", current);
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
	MainMapView view;
	ImageButton prev;
	ImageButton next;
	ImageButton cancel;
	String searchQuery = "";

	public void setQuery(String s)
	{
		searchQuery = s;
		view.onSearch(s);
	}
	
	public MapRender initMapRender(Map map, ViewType viewType) {
		switch (viewType) {
		case EXPLORER_VIEW:
			return new ExplorerRender(this, map);
		case COMAPPING_VIEW:
			return new ComappingRender(this, map);
		default:
			return null;
		}
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.map_options, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.zoom:
			// view.isVisible(View.INVISIBLE);
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					showZoom();
				}
			});
			return true;
		case R.id.find:
			//view.showSearchButtins();
			onSearchRequested();
			// view.setlayout(layout, cancel, next, previous, text, topics);
			return true;
		case R.id.mapSynchronizeButton:
			finish();
			MetaMapActivity.getInstance().loadMap(currentMapId,
					currentViewType, true);
			return true;
		}
		return false;
	}

	@Override
	public boolean onSearchRequested() {
		// If your application absolutely must disable search, do it here.
		// if (mMenuMode.getSelectedItemPosition() == MENUMODE_DISABLED) {
		// return false;
		// }

		// It's possible to prefill the query string before launching the search
		// UI. For this demo, we simply copy it from the user input field.
		// For most applications, you can simply pass null to startSearch() to
		// open the UI with an empty query string.
		// final String queryPrefill = mQueryPrefill.getText().toString();

		// Next, set up a bundle to send context-specific search data (if any)
		// The bundle can contain any number of elements, using any number of
		// keys;
		// For this Api Demo we copy a string from the user input field, and
		// store
		// it in the bundle as a string with the key "demo_key".
		// For most applications, you can simply pass null to startSearch().
		Bundle appDataBundle = null;
		// final String queryAppDataString = mQueryAppData.getText().toString();
		// if (queryAppDataString != null) {
		// appDataBundle = new Bundle();
		// appDataBundle.putString("demo_key", queryAppDataString);
		// }

		// Now call the Activity member function that invokes the Search Manager
		// UI.
		startSearch(null, false, null, false);

		// Returning true indicates that we did launch the search, instead of
		// blocking it.
		return true;
	}

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
	protected void onDestroy() {
		splashDeactivate();
		super.onDestroy();
	}
}