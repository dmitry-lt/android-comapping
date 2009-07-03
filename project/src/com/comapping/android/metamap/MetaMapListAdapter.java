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

class MetaMapListAdapter extends ArrayAdapter<String> {
	
	public class MetaMapItem
	{
		public String name;
		public String description;
		public boolean isFolder;
	}
	
	private MetaMapItem[] topics;

	private static List<String> getTopicsNames(MetaMapItem[] topics) {
		List<String> names = new ArrayList<String>();

		for (MetaMapItem topic : topics) {
			names.add(topic.name);
		}

		return names;
	}

	MetaMapListAdapter(Activity context, MetaMapItem[] topics) {
		super(context, R.layout.row, getTopicsNames(topics));

		this.topics = topics;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		
		// row view
		View row = convertView;
		if (row == null)
		{
			LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
			row = inflater.inflate(R.layout.row, null);
		}
		
		// map name
		TextView mapName = (TextView) row.findViewById(R.id.name);
		mapName.setText(topics[position].name);

		// icon
		ImageView icon = (ImageView) row.findViewById(R.id.icon);
		if (topics[position].isFolder) {
			icon.setImageResource(R.drawable.folder_icon);
		} else {
			icon.setImageResource(R.drawable.map_icon);
		}

		// description
		TextView description = (TextView) row.findViewById(R.id.description);
		description.setText(topics[position].description);

		return row;
	}
}