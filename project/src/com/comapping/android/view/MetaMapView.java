package com.comapping.android.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.comapping.android.ViewType;
import com.comapping.android.controller.MetaMapActivity;
import com.comapping.android.controller.R;
import com.comapping.android.model.Map;
import com.comapping.android.model.Topic;
import com.comapping.android.model.TopicComparator;
import com.comapping.android.storage.Storage;

public class MetaMapView {
	private MetaMapActivity metaMapActivity;

	public MetaMapView(MetaMapActivity metaMapActivity) {
		this.metaMapActivity = metaMapActivity;
	}

	public void splash() {
		metaMapActivity.setContentView(R.layout.splash);
	}

	public void loadMetaMapTopic(final Topic topic) {
		// current folder
		TextView currentFolder = (TextView) metaMapActivity.findViewById(R.id.currentFolder);
		currentFolder.setText("Current folder: " + topic.getText());

		// list view
		ListView listView = (ListView) metaMapActivity.findViewById(R.id.listView);

		final Topic[] children = topic.getChildTopics();

		Arrays.sort(children, new TopicComparator());

		List<String> topicNames = new ArrayList<String>();

		for (Topic child : children) {
			String name = child.getText();

			if (child.isFolder()) {
				name = "[Folder] " + name;
			}

			topicNames.add(name);
		}

		listView.setAdapter(new ArrayAdapter<String>(metaMapActivity, R.layout.row, R.id.label, topicNames));

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				// get viewType
				String viewType = Storage.getInstance().get(Storage.VIEW_TYPE_KEY);

				if (children[position].isFolder()) {
					loadMetaMapTopic(children[position]);
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
					loadMetaMapTopic(topic.getParent());
				}

			});
		}
	}

	public void load(final Map metaMap) {
		metaMapActivity.setContentView(R.layout.metamap);

		loadMetaMapTopic(metaMap.getRoot());

		ImageButton homeButton = (ImageButton) metaMapActivity.findViewById(R.id.homeButton);
		homeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				loadMetaMapTopic(metaMap.getRoot());
			}

		});
	}
}