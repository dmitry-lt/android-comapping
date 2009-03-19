/*
 * MetaMapView Controller
 * Android Comapping, 2009
 * Last change: Abishev Timur
 * 
 * Class implements MetaMapView controller
 */

package com.comapping.android.controller;

import com.comapping.android.communication.NotLoggedInException;

public class MetaMap {
	// Singleton
	private MetaMap() {
	};

	public static MetaMap instance = new MetaMap();
	private com.comapping.android.view.MetaMap metaMapView = new com.comapping.android.view.MetaMap();

	public static MetaMap getInstance() {
		return instance;
	}

	public void loadMap(final String mapId) {
		metaMapView.setMetaMapText("Loading #"+mapId+" map ... ");
		
		new Thread() {
			public void run() {
				String result;
				
				try {
					result = Main.instance.client.getComap(mapId);
				} catch (NotLoggedInException e) {
					result = "You not logged in!";
				}
				
				final String finalResult = result;
				
				Main.instance.runOnUiThread(new Runnable() {
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