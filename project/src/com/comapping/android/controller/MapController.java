package com.comapping.android.controller;

import com.comapping.android.model.Map;
import com.comapping.android.view.MapView;
import com.comapping.android.view.TestMapView;

public class MapController {
	private MapController() {
	};

	private static MapController instance = new MapController();

	public static MapController getInstance() {
		return instance;
	}

	public void loadMap(Map map) {
		MainController.getInstance().setContentView(
				new MapView(MainController.getInstance()
						.getApplicationContext(), map));
	}
}
