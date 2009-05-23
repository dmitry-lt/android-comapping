package com.comapping.android.view.metamap;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import android.widget.ImageButton;

import com.comapping.android.controller.MetaMapActivity;
import com.comapping.android.controller.R;
import com.comapping.android.model.map.Map;
import com.comapping.android.model.map.Topic;

public class InternetView extends MetaMapView {
	public static final String PLEASE_SYNCHRONIZE_MESSAGE = "Please synchronize your map list or open sdcard view";
	public static final String PROBLEMS_WHILE_RETRIEVING_MESSAGE = "There are some problem while map list retrieving.";
	public static final String PROBLEMS_WITH_MAP_MESSAGE = "There are some problem while map list parsing.";
	
	private static final String LAST_SYNCHRONIZATION = "Last synchronization";
	
	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	private String error = PLEASE_SYNCHRONIZE_MESSAGE;
	
	public InternetView(Map _map) {
		super(_map);
	}
	
	public void activate(MetaMapActivity _metaMapActivity) {
		super.activate(_metaMapActivity);
		
		// if metamap not synchronize
		if (map == null) {
			drawMetaMapMessage(error);
		} else {
			drawMetaMap();
		}

		// change button to switch
		ImageButton switchButton = (ImageButton) metaMapActivity.findViewById(R.id.viewSwitcher);
		switchButton.setImageResource(R.drawable.sdcard_icon);
		
		// activate synchronize button
		ImageButton synchronizeButton = (ImageButton) metaMapActivity.findViewById(R.id.synchronizeButton);
		enableImageButton(synchronizeButton, R.drawable.reload_metamap_menu_icon);
	}
	
	public String getMapDescription(Topic topic) {
		Timestamp lastSynchronizationDate = MetaMapActivity.client.getLastSynchronizationDate(topic.getMapRef());
		
		if (lastSynchronizationDate == null) {
			return MAP_DESCRIPTION;
		} else {
			SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
			
			return LAST_SYNCHRONIZATION+": "+dateFormat.format(lastSynchronizationDate);
		}
	}
	
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
