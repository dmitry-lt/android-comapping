/*
 * MetaMapView Controller
 * Android Comapping, 2009
 * Last change: Abishev Timur
 * 
 * Class implements MetaMapView controller
 */

package com.comapping.android.controller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.comapping.android.Log;
import com.comapping.android.Options;
import com.comapping.android.ViewType;
import com.comapping.android.communication.Client;
import com.comapping.android.communication.ConnectionException;
import com.comapping.android.communication.LoginInterruptedException;
import com.comapping.android.model.Map;
import com.comapping.android.model.DomMapBuilder;
import com.comapping.android.model.MapBuilder;
import com.comapping.android.model.MapParsingException;
import com.comapping.android.model.StringToXMLConvertionException;
import com.comapping.android.storage.Storage;
import com.comapping.android.view.MetaMapView;

public class MetaMapActivity extends Activity {
	public static final int MAP_REQUEST = 5523;

	private MetaMapView metaMapView;
	private static MetaMapActivity instance;

	public static MetaMapActivity getInstance() {
		return instance;
	}

	public static Client client = new Client();
	public static MapBuilder mapBuilder = new DomMapBuilder();
	
	public Map currentMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		instance = this;
		metaMapRefresh();
	}

	private void metaMapRefresh() {
		metaMapView = new MetaMapView(this);

		metaMapView.splash();

		final Activity context = this;

		new Thread() {
			public void run() {
				String result = "";

				try {
					result = client.getComap("meta", context);
				} catch (ConnectionException e) {
					Log.e(Log.metaMapControllerTag, "connection error in metamap retrieving");
				} catch (LoginInterruptedException e) {
					Log.e(Log.metaMapControllerTag, "login interrupted in metamap retrieving");
				}

				Map metaMap = null;
				try {
					metaMap = mapBuilder.buildMap(result);
				} catch (StringToXMLConvertionException e) {
					Log.e(Log.metaMapControllerTag, "xml convertion exception");
				} catch (MapParsingException e) {
					Log.e(Log.metaMapControllerTag, "map parsing exception");
				}

				final Map finalMetaMap = metaMap;

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						metaMapView.load(finalMetaMap);
					}
				});
			}
		}.start();
	}

	public void logout() {
		Storage.getInstance().set("key", "");
		try {
			client.logout(this);
		} catch (ConnectionException e) {
			Log.e(Log.metaMapControllerTag, "connection exception in logout");
		}

		metaMapRefresh();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (((resultCode == RESULT_CANCELED) && (requestCode == Client.LOGIN_REQUEST_CODE))
				|| (resultCode == Options.RESULT_CHAIN_CLOSE)) {
			setResult(Options.RESULT_CHAIN_CLOSE);
			Log.i(Log.metaMapControllerTag, "finish");
			finish();
		}
	}

	public void loadMap(final String mapId, final ViewType viewType) {
		Intent intent = new Intent(MapActivity.MAP_ACTIVITY_INTENT);
		intent.putExtra(MapActivity.EXT_MAP_ID, mapId);
		intent.putExtra(MapActivity.EXT_VIEW_TYPE, viewType.toString());

		startActivityForResult(intent, MAP_REQUEST);
	}
}