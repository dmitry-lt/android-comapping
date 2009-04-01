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
import com.comapping.android.model.MapBuilder;
import com.comapping.android.model.MapParsingException;
import com.comapping.android.model.StringToXMLConvertionException;
import com.comapping.android.view.MetaMapView;

public class MetaMapActivity extends Activity {
	private MetaMapView metaMapView;
	private static MetaMapActivity instance;

	public static MetaMapActivity getInstance() {
		return instance;
	}

	public static Client client = new Client();
	public Map currentMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		metaMapView = new MetaMapView(this);
		client.clientSideLogout();
		metaMapView.load();

		instance = this;

		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i("Main Controller", "finish code: " + resultCode);

		if (((resultCode == RESULT_CANCELED) && (requestCode == Client.LOGIN_REQUEST_CODE))
				|| (resultCode == Options.RESULT_CHAIN_CLOSE)) {
			setResult(Options.RESULT_CHAIN_CLOSE);
			Log.i("Meta Map activity", "finish");
			finish();
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	public void loadMap(final String mapId, final ViewType viewType) {
		metaMapView.setMetaMapText("Loading #" + mapId + " map ... ");

		final Activity current = this;

		new Thread() {
			public void run() {
				String result = "";
				try {
					result = client.getComap(mapId, current);

					currentMap = MapBuilder.buildMap(result);

					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Intent intent = new Intent("com.comapping.android.intent.MAP");
							intent.putExtra(MapActivity.EXT_VIEW_TYPE, viewType.toString());

							startActivity(intent);
						};
					});
				} catch (LoginInterruptedException e) {
					metaMapView.setMetaMapText("Login interrupted!");
				} catch (ConnectionException e) {
					Log.e(Log.metaMapControllerTag, "connection exception");
				} catch (StringToXMLConvertionException e) {
					Log.e(Log.metaMapControllerTag, e.toString());
					metaMapView.setMetaMapText("Wrong format");
				} catch (MapParsingException e) {
					Log.e(Log.metaMapControllerTag, e.toString());
					metaMapView.setMetaMapText("Wrong format");
				}
			}
		}.start();
	}
}