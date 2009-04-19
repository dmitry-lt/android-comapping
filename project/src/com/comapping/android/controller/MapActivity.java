package com.comapping.android.controller;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
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

	public void splashActivate(final String message) {
		final Activity context = this;

		runOnUiThread(new Runnable() {
			public void run() {
				if (splash == null) {
					splash = ProgressDialog.show(context, "Comapping", message);
				} else {
					splash.setMessage(message);
				}
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();

		final ViewType viewType = ViewType.getViewTypeFromString(extras.getString(EXT_VIEW_TYPE));
		final String mapId = extras.getString(EXT_MAP_ID);

		Map map = (Map) Cache.get(mapId);
		final Activity current = this;

		if (map == null) {
			new Thread() {
				public void run() {
					String result = "";
					try {
						splashActivate("Downloading map");

						try {
							result = MetaMapActivity.getCurrentMapProvider().getComap(mapId, current);
						} catch (InvalidCredentialsException e) {
							Log.e(Log.mapControllerTag, "invalid credentials while map getting");
							// TODO: ???
						}

						splashActivate("Loading map");

						final Map buildedMap = MetaMapActivity.mapBuilder.buildMap(result);

						splashDeactivate();

						// add to chache
						Cache.set(mapId, buildedMap);

						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								loadMap(buildedMap, viewType);
							};
						});
					} catch (LoginInterruptedException e) {
						Log.e(Log.mapControllerTag, "login interrupted");
					} catch (ConnectionException e) {
						Log.e(Log.mapControllerTag, "connection exception");
					} catch (StringToXMLConvertionException e) {
						Log.e(Log.mapControllerTag, e.toString());
					} catch (MapParsingException e) {
						Log.e(Log.mapControllerTag, e.toString());
					}
				}
			}.start();
		} else {
			loadMap(map, viewType);
		}
	}

	ZoomControls zoom;
	MainMapView view;

	public void loadMap(Map map, ViewType viewType) {
		MapRender r = null;
		switch (viewType) {
		case EXPLORER_VIEW:
			r = new ExplorerRender(this, map);
			break;
		case TREE_VIEW:
			r = new ComappingRender(this, map.getRoot());
			break;
		}

		this.setContentView(R.layout.map);

		zoom = (ZoomControls) findViewById(R.id.Zoom);

		view = (MainMapView) findViewById(R.id.MapView);
		view.setRender(r);
		view.setZoom(zoom);

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