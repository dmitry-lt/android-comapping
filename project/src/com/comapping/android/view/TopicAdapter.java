package com.comapping.android.view;

import java.util.List;

import com.comapping.android.controller.R;
import com.comapping.android.model.Topic;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

class TopicAdapter extends ArrayAdapter<String> {
	Activity context;

	Topic[] topics;
	List<String> names;

	TopicAdapter(Activity context, Topic[] topics, List<String> names) {
		super(context, R.layout.row, names);

		this.topics = topics;
		this.names = names;

		this.context = context;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = context.getLayoutInflater();
		View row = inflater.inflate(R.layout.row, null);
		TextView mapName = (TextView) row.findViewById(R.id.name);

		mapName.setText(names.get(position));

		// set up icon and description
		ImageView icon = (ImageView) row.findViewById(R.id.icon);
		TextView description = (TextView) row.findViewById(R.id.description);

		if (topics[position].isFolder()) {
			icon.setImageResource(R.drawable.folder_icon);
			description.setText("Folder");
		} else {
			icon.setImageResource(R.drawable.map_icon);
			description.setText("Map");
		}

		return row;
	}

}