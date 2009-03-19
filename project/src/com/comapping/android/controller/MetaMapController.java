/*
 * MetaMapView Controller
 * Android Comapping, 2009
 * Last change: Abishev Timur
 * 
 * Class implements MetaMapView controller
 */

package com.comapping.android.controller;

import android.util.Log;

import com.comapping.android.communication.ConnectionException;
import com.comapping.android.communication.NotLoggedInException;
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

	public void loadMap(final String mapId) {
		metaMapView.setMetaMapText("Loading #" + mapId + " map ... ");

		new Thread() {
			public void run() {
				String result = "";

				try {
					result = MainController.getInstance().client.getComap(mapId); 
				} catch (NotLoggedInException e) {
					result = "You not logged in!";
				} catch (ConnectionException e) {
					Log.e("Comapping", "MetaMap: connection exception");
				}

				final String finalResult = result;

				MainController.getInstance().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						metaMapView.setMetaMapText(finalResult);
					}
				});
			}
		}.start();
	}

	public void activate() {
		metaMapView.load();
	}
}