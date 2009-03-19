package com.comapping.android.view;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.comapping.android.controller.MainController;
import com.comapping.android.controller.MetaMapController;
import com.comapping.android.controller.R;

public class MetaMapView {
	public void setMetaMapText(String text) {
		TextView metaMapText = (TextView) MainController.getInstance()
				.findViewById(R.id.metaMapText);

		metaMapText.setText(text);
	}

	public void load() {
		MainController.getInstance().setContentView(R.layout.metamap);

		Button go = (Button) MainController.getInstance().findViewById(R.id.go);
		final EditText mapName = (EditText) MainController.getInstance()
				.findViewById(R.id.EditText01);

		go.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MetaMapController.getInstance()
						.loadMap(mapName.getText().toString());
			}
		});

		Button logout = (Button) MainController.getInstance().findViewById(R.id.logout);
		logout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MainController.getInstance().logout();
			}
		});
	}
}