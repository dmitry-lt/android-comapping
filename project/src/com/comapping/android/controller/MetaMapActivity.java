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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

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

	/* Creates the menu items */
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.metamap, menu);
	    return true;
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

	/* Handles item selections */
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.preferences:
	    	preferences();
	    	return true;
	    case R.id.logout:
	        logout();
	        return true;
	    }
	    
	    return false;
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

	public void loadMap(final String mapId, final ViewType viewType) {
		Intent intent = new Intent(MapActivity.MAP_ACTIVITY_INTENT);
		intent.putExtra(MapActivity.EXT_MAP_ID, mapId);
		intent.putExtra(MapActivity.EXT_VIEW_TYPE, viewType.toString());

		startActivityForResult(intent, MAP_REQUEST);
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
	
	public void preferences() {
		startActivity(new Intent(PreferencesActivity.PREFERENCES_ACTIVITY_INTENT));
	}
}