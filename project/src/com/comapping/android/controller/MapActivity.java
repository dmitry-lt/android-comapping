package com.comapping.android.controller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.comapping.android.Cache;
import com.comapping.android.Log;
import com.comapping.android.Options;
import com.comapping.android.ViewType;
import com.comapping.android.communication.Client;
import com.comapping.android.communication.ConnectionException;
import com.comapping.android.communication.LoginInterruptedException;
import com.comapping.android.model.Map;
import com.comapping.android.model.MapBuilder;
import com.comapping.android.model.MapParsingException;
import com.comapping.android.model.StringToXMLConvertionException;
import com.comapping.android.view.ComappingRender;
import com.comapping.android.view.ExplorerRender;
import com.comapping.android.view.MainMapView;
import com.comapping.android.view.Render;

public class MapActivity extends Activity {
	public static final String MAP_ACTIVITY_INTENT = "com.comapping.android.intent.MAP";

	public static final String EXT_VIEW_TYPE = "viewType";
	public static final String EXT_MAP_ID = "mapId";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();

		final ViewType viewType = ViewType.getViewTypeFromString(extras.getString(EXT_VIEW_TYPE));
		final String mapId = extras.getString(EXT_MAP_ID);

		Map map = (Map) Cache.get(mapId);
		final Activity current = this;

		if (map == null) {
			setContentView(R.layout.splash);
			
			new Thread() {
				public void run() {
					String result = "";
					try {
						result = MetaMapActivity.client.getComap(mapId, current);

						final Map buildedMap = MapBuilder.buildMap(result);

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

	public void loadMap(Map map, ViewType viewType) {
		View view = null;
		Render r = null;
		switch (viewType) {
		case EXPLORER_VIEW:
			r = new ExplorerRender(this, map);
			break;
		case TREE_VIEW:
			r = new ComappingRender(this, map.getRoot());
			break;
		}
		view = new MainMapView(this, r);
		if (view != null) {
			this.setContentView(view);
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
}