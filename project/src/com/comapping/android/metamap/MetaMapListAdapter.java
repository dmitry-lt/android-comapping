package com.comapping.android.metamap;

import com.comapping.android.controller.R;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

class MetaMapListAdapter extends ArrayAdapter<MetaMapItem> {
	

	MetaMapListAdapter(Activity context, MetaMapItem[] topics) {
		super(context, R.layout.metamap_row, topics);
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		
		// row view
		View row = convertView;
		if (row == null)
		{
			LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
			row = inflater.inflate(R.layout.metamap_row, null);
		}
		
		// map name
		TextView mapName = (TextView) row.findViewById(R.id.name);
		mapName.setText(getItem(position).name);

		// icon
		ImageView icon = (ImageView) row.findViewById(R.id.icon);
		if (getItem(position).isFolder) {
			icon.setImageResource(R.drawable.metamap_folder);
		} else {
			icon.setImageResource(R.drawable.metamap_map);
		}

		// description
		TextView description = (TextView) row.findViewById(R.id.description);
		description.setText(getItem(position).description);

		return row;
	}
}