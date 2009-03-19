package com.comapping.android.view;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.comapping.android.controller.Main;
import com.comapping.android.controller.R;

public class MetaMap {
	public void setMetaMapText(String text) {
		TextView metaMapText = (TextView) Main.getInstance()
				.findViewById(R.id.metaMapText);

		metaMapText.setText(text);
	}

	public void load() {
		Main.getInstance().setContentView(R.layout.metamap);

		Button go = (Button) Main.getInstance().findViewById(R.id.go);
		final EditText mapName = (EditText) Main.getInstance()
				.findViewById(R.id.EditText01);

		go.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				com.comapping.android.controller.MetaMap.getInstance()
						.loadMap(mapName.getText().toString());
			}
		});

		Button logout = (Button) Main.getInstance().findViewById(R.id.logout);
		logout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Main.getInstance().logout();
			}
		});
	}
}