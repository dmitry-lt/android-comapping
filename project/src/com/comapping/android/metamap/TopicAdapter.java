package com.comapping.android.metamap;

import java.util.ArrayList;
import java.util.List;

import com.comapping.android.controller.R;
import com.comapping.android.model.map.Topic;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

class TopicAdapter extends ArrayAdapter<String> {
	private Activity context;

	private Topic[] topics;

	private static List<String> getTopicsNames(Topic[] topics) {
		List<String> names = new ArrayList<String>();

		for (Topic topic : topics) {
			names.add(topic.getText());
		}

		return names;
	}

	TopicAdapter(Activity context, Topic[] topics) {
		super(context, R.layout.row, getTopicsNames(topics));

		this.topics = topics;
		this.context = context;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = context.getLayoutInflater();
		View row = inflater.inflate(R.layout.row, null);
		TextView mapName = (TextView) row.findViewById(R.id.name);

		mapName.setText(topics[position].getText());

		// set up icon and description
		ImageView icon = (ImageView) row.findViewById(R.id.icon);
		TextView description = (TextView) row.findViewById(R.id.description);

		if (topics[position].isFolder()) {
			icon.setImageResource(R.drawable.folder_icon);
			description.setText(MetaMapActivity.getInstance().getFolderDescription(topics[position]));
		} else {
			icon.setImageResource(R.drawable.map_icon);
			description.setText(MetaMapActivity.getInstance().getMapDescription(topics[position]));
		}

		return row;
	}
}