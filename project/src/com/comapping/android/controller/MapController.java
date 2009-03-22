package com.comapping.android.controller;

import android.view.View;

import com.comapping.android.ViewType;
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

	public void loadMap(Map map, ViewType viewType) {
		View view = null;
		switch (viewType) {
			case EXPLORER_VIEW:
				view = new MapView(MainController.getInstance()
						.getApplicationContext(), map);
				break;
			case TREE_VIEW:
				view = new TestMapView(MainController.getInstance()
						.getApplicationContext(), map);
				break;				
		}
		if (view != null) MainController.getInstance().setContentView(view);
	}
}
