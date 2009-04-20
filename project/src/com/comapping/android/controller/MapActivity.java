package com.comapping.android.controller;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ZoomControls;

import com.comapping.android.Cache;
import com.comapping.android.Log;
import com.comapping.android.Options;
import com.comapping.android.ViewType;
import com.comapping.android.communication.Client;
import com.comapping.android.communication.exceptions.ConnectionException;
import com.comapping.android.communication.exceptions.InvalidCredentialsException;
import com.comapping.android.communication.exceptions.LoginInterruptedException;
import com.comapping.android.model.Map;
import com.comapping.android.model.exceptions.MapParsingException;
import com.comapping.android.model.exceptions.StringToXMLConvertionException;
import com.comapping.android.view.ComappingRender;
import com.comapping.android.view.ExplorerRender;
import com.comapping.android.view.MainMapView;
import com.comapping.android.view.MapRender;

public class MapActivity extends Activity {
	public static final String MAP_ACTIVITY_INTENT = "com.comapping.android.intent.MAP";

	public static final String EXT_VIEW_TYPE = "viewType";
	public static final String EXT_MAP_ID = "mapId";

	private ProgressDialog splash = null;
	private Thread mapProcessingThread;

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();

		final ViewType viewType = ViewType.getViewTypeFromString(extras.getString(EXT_VIEW_TYPE));
		final String mapId = extras.getString(EXT_MAP_ID);

		final Activity current = this;

		mapProcessingThread = new Thread() {
			public void run() {
				try {
					final Map map;
					if (!Cache.has(mapId)) {
						splashActivate("Downloading map", false);						
						String result = "";
						try {
							result = MetaMapActivity.getCurrentMapProvider().getComap(mapId, current);
						} catch (InvalidCredentialsException e) {
							Log.e(Log.mapControllerTag, "invalid credentials while map getting");
							// TODO: ???
						}

						splashActivate("Parsing map", true);
						map = MetaMapActivity.mapBuilder.buildMap(result);

						// add to cache
						Cache.set(mapId, map);
					} else {
						map = (Map) Cache.get(mapId);
					}
					
					if (interrupted()) {
						return;
					}
					
					splashActivate("Loading map", true);
					final MapRender mapRender = initMapRender(map, viewType);

					if (interrupted()) {
						return;
					}
					
					splashDeactivate();

					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							setContentView(R.layout.map);

							zoom = (ZoomControls) findViewById(R.id.Zoom);

							view = (MainMapView) findViewById(R.id.MapView);
							view.setRender(mapRender);
							view.setZoom(zoom);
						};
					});
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
				}
			}
		};
		mapProcessingThread.start();
	}

	ZoomControls zoom;
	MainMapView view;

	public MapRender initMapRender(Map map, ViewType viewType) {
		switch (viewType) {
		case EXPLORER_VIEW:
			return new ExplorerRender(this, map);
		case TREE_VIEW:
			return new ComappingRender(this, map.getRoot());
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
			zoom.show();
			view.setVisible();
			return true;
		}

		return false;
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