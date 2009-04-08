package com.comapping.android.view;

import android.app.ProgressDialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.comapping.android.ViewType;
import com.comapping.android.controller.MetaMapActivity;
import com.comapping.android.controller.R;
import com.comapping.android.model.Map;
import com.comapping.android.model.Topic;
import com.comapping.android.storage.Storage;

public class MetaMapView {
	private MetaMapActivity metaMapActivity;
	private ProgressDialog splash;

	public MetaMapView(MetaMapActivity metaMapActivity) {
		this.metaMapActivity = metaMapActivity;

		metaMapActivity.setContentView(R.layout.metamap);
		metaMapActivity.setTitle("Comapping: My Maps");
	}

	public void splashActivate(final String message) {
		metaMapActivity.runOnUiThread(new Runnable() {
			public void run() {
				if (splash == null) {
					splash = ProgressDialog.show(metaMapActivity, "Comapping", message);
				} else {
					splash.setMessage(message);
				}
			}
		});
	}

	public void splashDeactivate() {
		metaMapActivity.runOnUiThread(new Runnable() {
			public void run() {
				if (splash != null) {
					splash.dismiss();
					splash = null;
				}
			}
		});
	}

	public void drawMetaMapTopic(final Topic topic, final Topic[] children) {
		metaMapActivity.setTitle("Comapping: " + topic.getText());

		// list view
		ListView listView = (ListView) metaMapActivity.findViewById(R.id.listView);
		metaMapActivity.registerForContextMenu(listView);

		listView.setAdapter(new TopicAdapter(metaMapActivity, children));

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> apapterView, View view, int position, long arg3) {
				// get viewType
				String viewType = Storage.getInstance().get(Storage.VIEW_TYPE_KEY);

				if (children[position].isFolder()) {
					metaMapActivity.loadMetaMapTopic(children[position]);
				} else {
					metaMapActivity.loadMap(children[position].getMapRef(), ViewType.getViewTypeFromString(viewType));
				}
			}
		});

		// up button
		ImageButton upLevelButton = (ImageButton) metaMapActivity.findViewById(R.id.upLevelButton);
		ImageButton homeButton = (ImageButton) metaMapActivity.findViewById(R.id.homeButton);

		if (topic.isRoot()) {
			// deactivate buttons
			upLevelButton.setEnabled(false);
			homeButton.setEnabled(false);
		} else {
			// activate buttons
			upLevelButton.setEnabled(true);
			homeButton.setEnabled(true);

			upLevelButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					metaMapActivity.loadMetaMapTopic(topic.getParent());
				}
			});
		}
	}

	public void load(final Map metaMap) {
		metaMapActivity.loadMetaMapTopic(metaMap.getRoot());

		ImageButton homeButton = (ImageButton) metaMapActivity.findViewById(R.id.homeButton);
		homeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				metaMapActivity.loadMetaMapTopic(metaMap.getRoot());
			}

		});
	}
}