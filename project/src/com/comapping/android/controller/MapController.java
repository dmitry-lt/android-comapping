package com.comapping.android.controller;

import android.view.View;

import com.comapping.android.ViewType;
import com.comapping.android.model.Map;
import com.comapping.android.model.Topic;
import com.comapping.android.view.ComappingRender;
import com.comapping.android.view.ExplorerRender;
import com.comapping.android.view.MainMapView;
import com.comapping.android.view.Render;

public class MapController {
	private MapController() {
	};

	private static MapController instance = new MapController();

	public static MapController getInstance() {
		return instance;
	}

	public void loadMap(Map map, ViewType viewType) {
		View view = null;
		Render r = null;
		switch (viewType) {
			case EXPLORER_VIEW:
				r = new ExplorerRender(MainController.getInstance(), map);
				break;
			case TREE_VIEW:
				r = new ComappingRender(MainController.getInstance(), map.getRoot());				
				break;
		}
		view = new MainMapView(MainController.getInstance(), r);
		if (view != null) MainController.getInstance().setContentView(view);
	}
}
