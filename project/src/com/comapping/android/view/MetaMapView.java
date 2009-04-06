package com.comapping.android.view;

import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.comapping.android.ViewType;
import com.comapping.android.controller.MetaMapActivity;
import com.comapping.android.controller.R;
import com.comapping.android.model.Map;
import com.comapping.android.model.Topic;

public class MetaMapView {
	private MetaMapActivity metaMapActivity;

	public MetaMapView(MetaMapActivity metaMapActivity) {
		this.metaMapActivity = metaMapActivity;
	}

	public void splash() {
		metaMapActivity.setContentView(R.layout.splash);
	}

	public void loadMetaMapTopic(final Map metaMap, final Topic topic) {
		TextView currentFolder = (TextView) metaMapActivity.findViewById(R.id.currentFolder);
		currentFolder.setText("Current folder: " + topic.getText());

		ListView listView = (ListView) metaMapActivity.findViewById(R.id.listView);

		List<String> topicNames = new ArrayList<String>();
		List<Topic> topics = new ArrayList<Topic>();

		// first - home folder
		final boolean isHomeRowExist = !topic.isRoot();
		if (isHomeRowExist) {
			topicNames.add("Go to home folder");
			topics.add(metaMap.getRoot());
		}

		// second - up
		final boolean isUpRowExist = (!topic.isRoot()) && (!topic.getParent().isRoot());
		if (isUpRowExist) {
			topicNames.add("Go up");
			topics.add(topic.getParent());
		}

		// third - folders
		for (Topic child : topic.getChildTopics()) {
			if (child.isFolder()) {
				topicNames.add("[Folder] " + child.getText());
				topics.add(child);
			}
		}

		// fourth - maps
		for (Topic child : topic.getChildTopics()) {
			if (!child.isFolder()) {
				topicNames.add(child.getText());
				topics.add(child);
			}
		}

		listView.setAdapter(new ArrayAdapter<String>(metaMapActivity, R.layout.row, R.id.label, topicNames));

		final List<Topic> finalTopics = topics;

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				// get viewType
				ViewType viewType = null;

				if (((RadioButton) metaMapActivity.findViewById(R.id.explorerViewRadioButton)).isChecked()) {
					viewType = ViewType.EXPLORER_VIEW;
				}

				if (((RadioButton) metaMapActivity.findViewById(R.id.treeViewRadioButton)).isChecked()) {
					viewType = ViewType.TREE_VIEW;
				}

				Topic current = finalTopics.get(position);

				if (current.isFolder()) {
					loadMetaMapTopic(metaMap, current);
				} else {
					metaMapActivity.loadMap(current.getMapRef(), viewType);
				}
			}
		});
	}

	public void load(Map metaMap) {
		metaMapActivity.setContentView(R.layout.metamap);

		loadMetaMapTopic(metaMap, metaMap.getRoot());

		Button logout = (Button) metaMapActivity.findViewById(R.id.logout);
		logout.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				metaMapActivity.logout();
			}
		});
	}
}