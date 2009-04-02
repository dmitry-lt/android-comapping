package com.comapping.android.view;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.comapping.android.ViewType;
import com.comapping.android.controller.MetaMapActivity;
import com.comapping.android.controller.R;

public class MetaMapView {
	private MetaMapActivity metaMapActivity;

	public MetaMapView(MetaMapActivity metaMapActivity) {
		this.metaMapActivity = metaMapActivity;
	}

	public void setMetaMapText(final String text) {

		final TextView metaMapText = (TextView) metaMapActivity.findViewById(R.id.metaMapText);

		metaMapActivity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				metaMapText.setText(text);
			}
		});
	}

	public void load() {
		metaMapActivity.setContentView(R.layout.metamap);

		Button loadTreeView = (Button) metaMapActivity.findViewById(R.id.loadTreeView);
		final EditText mapName = (EditText) metaMapActivity.findViewById(R.id.EditText01);

		loadTreeView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				metaMapActivity.loadMap(mapName.getText().toString(), ViewType.TREE_VIEW);
			}
		});

		Button loadExplorerView = (Button) metaMapActivity.findViewById(R.id.loadExplorerView);

		loadExplorerView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				metaMapActivity.loadMap(mapName.getText().toString(), ViewType.EXPLORER_VIEW);
			}
		});

		Button logout = (Button) metaMapActivity.findViewById(R.id.logout);
		logout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				metaMapActivity.logout();
			}
		});
	}
}