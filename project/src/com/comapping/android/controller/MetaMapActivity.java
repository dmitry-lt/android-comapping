/*
 * MetaMapView Controller
 * Android Comapping, 2009
 * Last change: Abishev Timur
 * 
 * Class implements MetaMapView controller
 */

package com.comapping.android.controller;

import java.util.Arrays;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.comapping.android.Cache;
import com.comapping.android.Log;
import com.comapping.android.Options;
import com.comapping.android.ViewType;
import com.comapping.android.communication.Client;
import com.comapping.android.communication.exceptions.ConnectionException;
import com.comapping.android.communication.exceptions.LoginInterruptedException;
import com.comapping.android.model.Map;
import com.comapping.android.model.MapBuilder;
import com.comapping.android.model.SaxMapBuilder;
import com.comapping.android.model.Topic;
import com.comapping.android.model.TopicComparator;
import com.comapping.android.model.exceptions.MapParsingException;
import com.comapping.android.model.exceptions.StringToXMLConvertionException;
import com.comapping.android.storage.Storage;
import com.comapping.android.view.MetaMapView;

public class MetaMapActivity extends Activity {
	public static final int MAP_REQUEST = 5523;

	public static final String MAP_DESCRIPTION = "Map";
	public static final String FOLDER_DESCRIPTION = "Folder";

	private MetaMapView metaMapView;
	private static MetaMapActivity instance;

	private Topic[] currentTopicChildren;

	public static MetaMapActivity getInstance() {
		return instance;
	}

	public static Client client = new Client();
	public static MapBuilder mapBuilder = new SaxMapBuilder();

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
		inflater.inflate(R.menu.metamap_options, menu);
		return true;
	}

	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;

		MenuInflater inflater = getMenuInflater();
		super.onCreateContextMenu(menu, view, menuInfo);

		int toInflate = R.menu.metamap_map_context; // default value

		if (!currentTopicChildren[info.position].isFolder()) {
			toInflate = R.menu.metamap_map_context;
		} else {
			toInflate = R.menu.metamap_folder_context;
		}

		inflater.inflate(toInflate, menu);
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
		case R.id.reloadMetamap:
			// TODO: clear metamap in cache
			metaMapRefresh();
			return true;
		case R.id.clearCache:
			Cache.clear();
			return true;
		}

		return false;
	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

		int itemId = (int) info.id;

		if (!currentTopicChildren[itemId].isFolder()) {
			String mapId = currentTopicChildren[itemId].getMapRef();

			switch (item.getItemId()) {
			case R.id.openWithTreeView:
				loadMap(mapId, ViewType.TREE_VIEW);
				break;
			case R.id.openWithExplorerView:
				loadMap(mapId, ViewType.EXPLORER_VIEW);
				break;
			}
		} else {
			loadMetaMapTopic(currentTopicChildren[itemId]);
		}

		return true;
	}

	private void metaMapRefresh() {
		metaMapView = new MetaMapView(this);

		final Activity context = this;

		new Thread() {
			public void run() {
				String result = "";

				metaMapView.splashActivate("Downloading map list");

				try {
					result = client.getComap("meta", context);
				} catch (ConnectionException e) {
					Log.e(Log.metaMapControllerTag, "connection error in metamap retrieving");
				} catch (LoginInterruptedException e) {
					Log.e(Log.metaMapControllerTag, "login interrupted in metamap retrieving");
				}

				metaMapView.splashActivate("Loading map list");

				Map metaMap = null;
				try {
					metaMap = mapBuilder.buildMap(result);
				} catch (StringToXMLConvertionException e) {
					Log.e(Log.metaMapControllerTag, "xml convertion exception");
				} catch (MapParsingException e) {
					Log.e(Log.metaMapControllerTag, "map parsing exception");
				}
				
				metaMapView.splashDeactivate();
				
				//Simple fix
				if (metaMap == null)
					return;

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

	public void loadMetaMapTopic(final Topic topic) {
		currentTopicChildren = topic.getChildTopics();

		Arrays.sort(currentTopicChildren, new TopicComparator());

		metaMapView.drawMetaMapTopic(topic, currentTopicChildren);
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

	@Override
	protected void onDestroy() {
		metaMapView.splashDeactivate();
		super.onDestroy();
	}
}