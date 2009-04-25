package com.comapping.android.view.metamap;

import android.widget.ImageButton;

import com.comapping.android.controller.MetaMapActivity;
import com.comapping.android.controller.R;
import com.comapping.android.model.Map;

public class SdcardView extends MetaMapView {
	public SdcardView(Map _map) {
		super(_map);
	}
	
	public void activate(MetaMapActivity _metaMapActivity) {
		super.activate(_metaMapActivity);
		
		// change button to switch
		ImageButton switchButton = (ImageButton) metaMapActivity.findViewById(R.id.viewSwitcher);
		switchButton.setImageResource(R.drawable.internet_icon);
	}
}
