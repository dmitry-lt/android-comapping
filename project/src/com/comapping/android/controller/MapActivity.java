package com.comapping.android.controller;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
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
import android.widget.Button;
import android.widget.EditText;
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
import com.comapping.android.view.ExplorerRender;
import com.comapping.android.view.MainMapView;
import com.comapping.android.view.MapRender;

public class MapActivity extends Activity {
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
							mapProcessingThread.setPriority(Thread.MIN_PRIORITY);
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
				Dialog dialog = (new AlertDialog.Builder(activity).setTitle("Error").setMessage(message)
						.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();

		final ViewType viewType = ViewType.getViewTypeFromString(extras.getString(EXT_VIEW_TYPE));
		final String mapId = extras.getString(EXT_MAP_ID);
		final boolean ignoreCache = extras.getBoolean(EXT_IS_IGNORE_CACHE);

		currentMapId = mapId;
		currentViewType = viewType;

		final Activity current = this;
		final Context context = this;

		mapProcessingThread = new Thread() {
			public void run() {
				try {
					final Map map;
					if (!MemoryCache.has(mapId) || (ignoreCache)) {
						splashActivate("Downloading map", false);
						String result = "";
						try {
							if (MetaMapActivity.getCurrentMapProvider() instanceof CachingClient) {
								result = ((CachingClient) MetaMapActivity.getCurrentMapProvider()).getComap(mapId,
										current, ignoreCache);
							} else {
								result = MetaMapActivity.getCurrentMapProvider().getComap(mapId, current);
							}
						} catch (InvalidCredentialsException e) {
							Log.e(Log.mapControllerTag, "invalid credentials while map getting");
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

							layout = (LinearLayout) findViewById(R.id.FindView);
							cancel = (ImageButton) findViewById(R.id.cancelButton);
							next = (ImageButton) findViewById(R.id.nextButton);
							previous = (ImageButton) findViewById(R.id.previousButton);
							text = (AutoCompleteTextView) findViewById(R.id.nameEditText);

							Topic topic = map.getRoot();
							allTopicsTexts(topic);
							allTopics(topic);

							text.setAdapter(new ArrayAdapter<String>(context,
									android.R.layout.simple_dropdown_item_1line, texts));

							view = (MainMapView) findViewById(R.id.MapView);
							view.setRender(mapRender);
							view.setZoom(zoom);
							hideZoom();
							zoom.setIsZoomInEnabled(false);
							zoom.setOnZoomInClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									view.setScale(view.getScale() + 0.1f);
									lastZoomPress = System.currentTimeMillis();
									view.refresh();
								}
							});
							zoom.setOnZoomOutClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									view.setScale(view.getScale() - 0.1f);
									lastZoomPress = System.currentTimeMillis();
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
					Log.e(Log.mapControllerTag, "login interrupted");
					onError("login interrupted", current);
				} catch (ConnectionException e) {
					Log.e(Log.mapControllerTag, "connection exception");
					onError("Connection error", current);
				} catch (StringToXMLConvertionException e) {
					Log.e(Log.mapControllerTag, e.toString());
					onError("Wrong file format", current);
				} catch (MapParsingException e) {
					Log.e(Log.mapControllerTag, e.toString());
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

	LinearLayout layout;
	ImageButton cancel;
	ImageButton next;
	ImageButton previous;
	AutoCompleteTextView text;

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
			view.setlayout(layout, cancel, next, previous, text, topics);
			return true;
		case R.id.mapSynchronizeButton:
			finish();
			MetaMapActivity.getInstance().loadMap(currentMapId, currentViewType, true);
			return true;
		}
		return false;
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
			Log.i(Log.mapControllerTag, "finish");
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