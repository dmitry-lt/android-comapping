package com.comapping.android.view.metamap;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.comapping.android.ViewType;
import com.comapping.android.controller.MetaMapActivity;
import com.comapping.android.controller.R;
import com.comapping.android.model.Map;
import com.comapping.android.model.Topic;
import com.comapping.android.storage.Storage;

public class MetaMapView {
	protected static final String MAP_DESCRIPTION = "Map";
	protected static final String FOLDER_DESCRIPTION = "Folder";
	
	protected MetaMapActivity metaMapActivity;

	protected Map map;
	private Topic currentTopic;

	public MetaMapView(Map _map) {
		map = _map;
		if (map != null) {
			currentTopic = map.getRoot();
		} else {
			currentTopic = null;
		}
	}

	public void drawMetaMapTopic(final Topic topic, final Topic[] childTopics) {
		currentTopic = topic;

		// title
		metaMapActivity.setTitle("Comapping: " + currentTopic.getText());

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
					metaMapActivity.loadMap(childTopics[position].getMapRef(),
							ViewType.getViewTypeFromString(viewType), false);
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

	protected void drawMetaMapMessage(String message) {
		ListView mapList = (ListView) metaMapActivity.findViewById(R.id.listView);
		mapList.setVisibility(ListView.GONE);

		TextView textViewMessage = (TextView) metaMapActivity.findViewById(R.id.textViewMessage);
		textViewMessage.setText(message);
		textViewMessage.setVisibility(TextView.VISIBLE);
	}

	protected void drawMetaMap() {
		ListView mapList = (ListView) metaMapActivity.findViewById(R.id.listView);
		mapList.setVisibility(ListView.VISIBLE);

		TextView textViewMessage = (TextView) metaMapActivity.findViewById(R.id.textViewMessage);
		textViewMessage.setVisibility(TextView.GONE);
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

		switchButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				metaMapActivity.switchView();
			}
		});
	}

	private static void bindSynchronizeButton(final MetaMapActivity activity) {
		ImageButton synchronizeButton = (ImageButton) activity.findViewById(R.id.synchronizeButton);

		synchronizeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				activity.synchronize();
			}
		});
	}

	public void activate(MetaMapActivity _metaMapActivity) {
		metaMapActivity = _metaMapActivity;

		if (currentTopic != null) {
			metaMapActivity.setTitle("Comapping: " + currentTopic.getText());
		} else {
			metaMapActivity.setTitle("Comapping");
		}
		
		if ((currentTopic == null) || (currentTopic.isRoot())) {
			setButtonsDisabled();
		} else {
			setButtonsEnabled();
		}

		bindHomeButton();
		bindSwitchViewButton();

		metaMapActivity.loadMetaMapTopic(currentTopic);
	}

	public static void loadLayout(MetaMapActivity activity) {
		activity.setContentView(R.layout.metamap);

		// bing synchronize button
		bindSynchronizeButton(activity);
	}

	public void prepareTopic(Topic topic) {
	}
	
	public String getMapDescription(Topic topic) {
		return MAP_DESCRIPTION;
	}
	
	public String getFolderDescription(Topic topic) {
		return FOLDER_DESCRIPTION;
	}
}