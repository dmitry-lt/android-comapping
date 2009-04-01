package com.comapping.android.controller;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.comapping.android.ViewType;
import com.comapping.android.model.Map;
import com.comapping.android.view.ComappingRender;
import com.comapping.android.view.ExplorerRender;
import com.comapping.android.view.MainMapView;
import com.comapping.android.view.Render;

public class MapActivity extends Activity {
	public static final String EXT_VIEW_TYPE = "viewType";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();

		ViewType viewType = ViewType.getViewTypeFromString(extras.getString(EXT_VIEW_TYPE));

		loadMap(MetaMapActivity.getInstance().currentMap, viewType);
	}

	public void loadMap(Map map, ViewType viewType) {
		View view = null;
		Render r = null;
		switch (viewType) {
		case EXPLORER_VIEW:
			r = new ExplorerRender(this, map);
			break;
		case TREE_VIEW:
			r = new ComappingRender(this, map.getRoot());
			break;
		}
		view = new MainMapView(this, r);
		if (view != null) {
			this.setContentView(view);
		}
	}
}