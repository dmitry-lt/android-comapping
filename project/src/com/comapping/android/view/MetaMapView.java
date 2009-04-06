package com.comapping.android.view;

import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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

		List<String> topics = new ArrayList<String>();

		// first - home folder
		final boolean isHomeRowExist = !topic.isRoot();
		if (isHomeRowExist) {
			topics.add("Go to home folder");
		}

		// second - up
		final boolean isUpRowExist = (!topic.isRoot()) && (!topic.getParent().isRoot());
		if (isUpRowExist) {
			topics.add("Go up");
		}

		for (Topic child : topic.getChildTopics()) {
			String childText = "";

			if (child.isFolder()) {
				childText = "[Folder]";
			}

			childText += " " + child.getText();

			topics.add(childText);
		}

		listView.setAdapter(new ArrayAdapter<String>(metaMapActivity, R.layout.row, R.id.label, topics));

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				// get new topic
				Topic child = null;

				// if home folder
				if (isHomeRowExist) {
					if (position == 0)
						child = metaMap.getRoot();
					position--;
				}

				// if up folder
				if (isUpRowExist) {
					if (position == 0)
						child = topic.getParent();
					position--;
				}

				if (position >= 0) {
					child = topic.getChildByIndex(position);
				}

				if (child.isFolder()) {
					loadMetaMapTopic(metaMap, child);
				} else {
					metaMapActivity.loadMap(child.getMapRef(), ViewType.EXPLORER_VIEW);
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