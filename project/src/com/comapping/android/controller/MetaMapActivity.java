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
import com.comapping.android.model.Map;
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
	public Map currentMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		instance = this;
		metaMapRefresh();

		super.onCreate(savedInstanceState);
	}

	private void metaMapRefresh() {
		metaMapView = new MetaMapView(this);
		metaMapView.load();
	}
	
	public void logout() {
		Storage.getInstance().set("key", "");
		try {
			client.logout(this);
		} catch (ConnectionException e) {
			Log.e(Log.metaMapControllerTag, "connection exception in logout");
		}
		
		metaMapRefresh();
		
		metaMapView.setMetaMapText("Logout successfull");
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (((resultCode == RESULT_CANCELED) && (requestCode == Client.LOGIN_REQUEST_CODE))
				|| (resultCode == Options.RESULT_CHAIN_CLOSE)) {
			setResult(Options.RESULT_CHAIN_CLOSE);
			Log.i(Log.metaMapControllerTag, "finish");
			finish();
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	public void loadMap(final String mapId, final ViewType viewType) {
		metaMapView.setMetaMapText("Loading #" + mapId + " map ... ");

		Intent intent = new Intent(MapActivity.MAP_ACTIVITY_INTENT);
		intent.putExtra(MapActivity.EXT_MAP_ID, mapId);
		intent.putExtra(MapActivity.EXT_VIEW_TYPE, viewType.toString());

		startActivityForResult(intent, MAP_REQUEST);
	}
}