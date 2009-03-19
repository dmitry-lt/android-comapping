package com.comapping.android.view;

import android.widget.TextView;

import com.comapping.android.controller.Main;
import com.comapping.android.controller.R;

public class MetaMap {
	public void load(String metaMap) {
		Main.instance.setContentView(R.layout.metamap);

		TextView metaMapText = (TextView) Main.instance
				.findViewById(R.id.metaMapText);

		metaMapText.setText("(" + metaMap + ")");
	}
}