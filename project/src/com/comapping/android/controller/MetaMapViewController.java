/*
 * MetaMapView Controller
 * Android Comapping, 2009
 * Last change: Abishev Timur
 * 
 * Class implements MetaMapView controller
 */

package com.comapping.android.controller;

import com.comapping.android.communication.NotLoggedInException;

import android.util.Log;
import android.widget.TextView;

public class MetaMapViewController {
	// Singleton
	private MetaMapViewController() {
	};

	public static MetaMapViewController instance = new MetaMapViewController();

	public static MetaMapViewController getInstance() {
		return instance;
	}

	public void activate() {
		String metaMapData = "";
		try {
			metaMapData = MainController.instance.client.getComap("25276");
		} catch (NotLoggedInException e) {
			Log.e("MetaMap View", "User not logged in");
		}

		Log.i("MetaMap View", "MetaMap text: " + metaMapData);

		MainController.instance.setContentView(R.layout.metamap);

		TextView metaMapText = (TextView) MainController.instance
				.findViewById(R.id.metaMapText);

		metaMapText.setText("(" + metaMapData + ")");
	}
}