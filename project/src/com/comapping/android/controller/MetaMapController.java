/*
 * MetaMapView Controller
 * Android Comapping, 2009
 * Last change: Abishev Timur
 * 
 * Class implements MetaMapView controller
 */

package com.comapping.android.controller;

import com.comapping.android.Log;
import com.comapping.android.ViewType;
import com.comapping.android.communication.ConnectionException;
import com.comapping.android.communication.NotLoggedInException;
import com.comapping.android.model.Map;
import com.comapping.android.model.MapBuilder;
import com.comapping.android.model.MapParsingException;
import com.comapping.android.model.StringToXMLConvertionException;
import com.comapping.android.view.MetaMapView;

public class MetaMapController {
	// Singleton
	private MetaMapController() {
	};

	private static MetaMapController instance = new MetaMapController();

	public static MetaMapController getInstance() {
		return instance;
	}

	private MetaMapView metaMapView = new MetaMapView();

	public void loadMap(final String mapId, final ViewType viewType) {
		metaMapView.setMetaMapText("Loading #" + mapId + " map ... ");

		new Thread() {
			public void run() {
				String result = "";

				try {
					result = MainController.getInstance().client
							.getComap(mapId);

					final Map map = MapBuilder.buildMap(result);
					MainController.getInstance().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							MapController.getInstance().loadMap(map, viewType);
						};
					});
				} catch (NotLoggedInException e) {
					metaMapView.setMetaMapText("You not logged in!");
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

	public void activate() {
		metaMapView.load();
	}
}