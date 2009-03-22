package com.comapping.android.view;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.comapping.android.ViewType;
import com.comapping.android.controller.MainController;
import com.comapping.android.controller.MetaMapController;
import com.comapping.android.controller.R;

public class MetaMapView {
	public void setMetaMapText(final String text) {
		
		final TextView metaMapText = (TextView) MainController.getInstance()
				.findViewById(R.id.metaMapText);

		MainController.getInstance().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				metaMapText.setText(text);	
			}			
		});
	}

	public void load() {
		MainController.getInstance().setContentView(R.layout.metamap);

		Button loadTreeView = (Button) MainController.getInstance().findViewById(R.id.loadTreeView);
		final EditText mapName = (EditText) MainController.getInstance()
				.findViewById(R.id.EditText01);

		loadTreeView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MetaMapController.getInstance()
						.loadMap(mapName.getText().toString(), ViewType.TREE_VIEW);
			}
		});

		Button loadExplorerView = (Button) MainController.getInstance().findViewById(R.id.loadExplorerView);
		
		loadExplorerView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MetaMapController.getInstance()
						.loadMap(mapName.getText().toString(), ViewType.EXPLORER_VIEW);
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