package com.comapping.android.metamap;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import android.widget.ImageButton;

import com.comapping.android.controller.R;
import com.comapping.android.model.map.Map;
import com.comapping.android.model.map.Topic;

public class InternetView extends MetaMapView {
	public static final String PLEASE_SYNCHRONIZE_MESSAGE = "Please synchronize your map list or open sdcard view";
	public static final String PROBLEMS_WHILE_RETRIEVING_MESSAGE = "There are some problem while map list retrieving.";
	public static final String PROBLEMS_WITH_MAP_MESSAGE = "There are some problem while map list parsing.";
	
	private String error = PLEASE_SYNCHRONIZE_MESSAGE;
	
	Map map;
	public InternetView(Map _map) {
		super(new ComappingProvider(_map));
		map = _map;
	}
	
//	public void activate(MetaMapActivity _metaMapActivity) {
//		super.activate(_metaMapActivity);
//		
//		drawMetaMap();
//		
//		// if metamap not synchronize
//		if (map == null) {
//			drawMetaMapMessage(error);
//		} else {
//			drawMetaMap();
//		}
//
//		// change button to switch
//		ImageButton switchButton = (ImageButton) metaMapActivity.findViewById(R.id.viewSwitcher);
//		switchButton.setImageResource(R.drawable.metamap_sdcard);
//		
//		// activate synchronize button
//		ImageButton synchronizeButton = (ImageButton) metaMapActivity.findViewById(R.id.synchronizeButton);
//		enableImageButton(synchronizeButton, R.drawable.menu_reload);
//	}
	
	public void setError(String _error) {
		error = _error;
	}
	
	public Integer getOptionsMenu() {
		if (MetaMapActivity.client.isLoggedIn()) {
			return R.menu.metamap_with_logout_options;
		} else {
			return R.menu.metamap_without_logout_options;
		}
	}
}
