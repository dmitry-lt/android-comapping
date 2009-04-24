package com.comapping.android.view;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.comapping.android.MetaMapViewType;
import com.comapping.android.ViewType;
import com.comapping.android.controller.MetaMapActivity;
import com.comapping.android.controller.R;
import com.comapping.android.model.Map;
import com.comapping.android.model.Topic;
import com.comapping.android.storage.Storage;

public class MetaMapView {
	private MetaMapActivity metaMapActivity;
	
	private Map map;
	private Topic currentTopic;
	
	public MetaMapView(Map _map) {
		map = _map;
		currentTopic = map.getRoot();
	}
	
	public void drawMetaMapTopic(final Topic topic, final Topic[] childTopics) {
		currentTopic = topic;
		
		// title
		metaMapActivity.setTitle("Comapping: "+currentTopic.getText());
		
		// list view
		ListView listView = (ListView) metaMapActivity.findViewById(R.id.listView);
		metaMapActivity.registerForContextMenu(listView);

		listView.setAdapter(new TopicAdapter(metaMapActivity, childTopics));

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> apapterView, View view, int position, long arg3) {
				// get viewType
				String viewType = Storage.getInstance().get(Storage.VIEW_TYPE_KEY);

				if (childTopics[position].isFolder()) {
					metaMapActivity.loadMetaMapTopic(childTopics[position]);
				} else {
					metaMapActivity.loadMap(childTopics[position].getMapRef(), ViewType.getViewTypeFromString(viewType));
				}
			}
		});

		
		if (topic.isRoot()) {
			setButtonsDisabled();
		} else {
			metaMapActivity.findViewById(R.id.upLevelButton).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					metaMapActivity.loadMetaMapTopic(topic.getParent());
				}
			});
			
			setButtonsEnabled();
		}
	}

	private void bindHomeButton() {
		ImageButton homeButton = (ImageButton) metaMapActivity.findViewById(R.id.homeButton);
		homeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				metaMapActivity.loadMetaMapTopic(map.getRoot());
			}

		});
	}
	
	private void setButtonsEnabled() {
		ImageButton upLevelButton = (ImageButton) metaMapActivity.findViewById(R.id.upLevelButton);
		ImageButton homeButton = (ImageButton) metaMapActivity.findViewById(R.id.homeButton);

		upLevelButton.setEnabled(true);
		homeButton.setEnabled(true);

		upLevelButton.setImageResource(R.drawable.up_level_button);
		homeButton.setImageResource(R.drawable.home_button);
	}
	
	private void setButtonsDisabled() {
		ImageButton upLevelButton = (ImageButton) metaMapActivity.findViewById(R.id.upLevelButton);
		ImageButton homeButton = (ImageButton) metaMapActivity.findViewById(R.id.homeButton);

		upLevelButton.setEnabled(false);
		homeButton.setEnabled(false);

		upLevelButton.setImageResource(R.drawable.up_level_button_grey);
		homeButton.setImageResource(R.drawable.home_button_grey);
	}
	
	private void bindSwitchViewButton() {
		ImageButton switchButton = (ImageButton) metaMapActivity.findViewById(R.id.viewSwitcher);
		
		MetaMapViewType oppositeViewType = null;
		
		if (metaMapActivity.getCurrentView() == MetaMapViewType.SDCARD_VIEW) {
			switchButton.setImageResource(R.drawable.internet_icon);
			oppositeViewType = MetaMapViewType.INTERNET_VIEW;
		} else {
			oppositeViewType = MetaMapViewType.SDCARD_VIEW;
		}
		
		final MetaMapViewType finalOppositeViewType = oppositeViewType;
		
		switchButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {	
				metaMapActivity.switchView(finalOppositeViewType);
			}
		});
	}
	
	public void activate(MetaMapActivity _metaMapActivity) {
		metaMapActivity = _metaMapActivity;
		
		metaMapActivity.setTitle("Comapping: "+currentTopic.getText());
		
		if (currentTopic.isRoot()) {
			setButtonsDisabled();
		} else {
			setButtonsEnabled();
		}
		
		bindHomeButton();
		bindSwitchViewButton();
		
		metaMapActivity.loadMetaMapTopic(currentTopic);
	}
	
	public static void loadLayout(Activity activity) {
		activity.setContentView(R.layout.metamap);
	}
}