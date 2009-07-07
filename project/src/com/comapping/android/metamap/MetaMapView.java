package com.comapping.android.metamap;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.comapping.android.Options;
import com.comapping.android.ViewType;
import com.comapping.android.controller.R;
import com.comapping.android.model.map.Map;
import com.comapping.android.model.map.Topic;
import com.comapping.android.storage.PreferencesStorage;

public class MetaMapView {
	protected static final String MAP_DESCRIPTION = "Map";
	protected static final String FOLDER_DESCRIPTION = "Folder";

	protected MetaMapActivity metaMapActivity;

	private Topic currentTopic;

	public MetaMapProvider provider;

	public MetaMapView(MetaMapProvider _provider) {

		provider = _provider;
	}

	public void updateMetaMap() {

		// title
		// metaMapActivity.setTitle("Comapping: " + currentTopic.getText());

		// list view
		ListView listView = (ListView) metaMapActivity
				.findViewById(R.id.listView);

		metaMapActivity.registerForContextMenu(listView);

		MetaMapListAdapter.MetaMapItem[] items = provider.getCurrentLevel();
		listView.setAdapter(new MetaMapListAdapter(metaMapActivity, items));

		// listView.setAdapter(new TopicAdapter(metaMapActivity, childTopics));

		listView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> apapterView, View view,
					int position, long arg3) {
				// get viewType

				if (provider.getCurrentLevel()[position].isFolder) {
					provider.gotoFolder(position);
					updateMetaMap();
				} else {

					String viewType = PreferencesStorage.get(
							PreferencesStorage.VIEW_TYPE_KEY,
							Options.DEFAULT_VIEW_TYPE);

					metaMapActivity.loadMap(
							provider.getCurrentLevel()[position].reference,
							ViewType.getViewTypeFromString(viewType), false);
				}
			}
		});

		// if (topic.isRoot()) {
		// setButtonsDisabled();
		// } else {
		// metaMapActivity.findViewById(R.id.upLevelButton)
		// .setOnClickListener(new OnClickListener() {
		//
		// public void onClick(View v) {
		// metaMapActivity.loadMetaMapTopic(topic.getParent());
		// }
		// });
		//
		// setButtonsEnabled();
		// }
	}

	public Integer getOptionsMenu() {
		return null;
	}

	protected void drawMetaMapMessage(String message) {
		ListView mapList = (ListView) metaMapActivity
				.findViewById(R.id.listView);
		mapList.setVisibility(ListView.GONE);

		TextView textViewMessage = (TextView) metaMapActivity
				.findViewById(R.id.textViewMessage);
		textViewMessage.setText(message);
		textViewMessage.setVisibility(TextView.VISIBLE);
	}

	protected void drawMetaMap() {
		ListView mapList = (ListView) metaMapActivity
				.findViewById(R.id.listView);
		mapList.setVisibility(ListView.VISIBLE);

		TextView textViewMessage = (TextView) metaMapActivity
				.findViewById(R.id.textViewMessage);
		textViewMessage.setVisibility(TextView.GONE);
	}

	protected void enableImageButton(ImageButton button, int resource) {
		button.setEnabled(true);
		button.setFocusable(true);

		button.setImageResource(resource);
	}

	protected void disableImageButton(ImageButton button, int resource) {
		button.setEnabled(false);
		button.setFocusable(false);

		button.setImageResource(resource);
	}

	private void bindHomeButton() {
		ImageButton homeButton = (ImageButton) metaMapActivity
				.findViewById(R.id.homeButton);

		homeButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				provider.goHome();
				updateMetaMap();
			}
		});

		ImageButton backButton = (ImageButton) metaMapActivity
				.findViewById(R.id.upLevelButton);

		backButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				provider.goUp();
				updateMetaMap();
			}
		});
	}

	private void setButtonsEnabled() {
		ImageButton upLevelButton = (ImageButton) metaMapActivity
				.findViewById(R.id.upLevelButton);
		ImageButton homeButton = (ImageButton) metaMapActivity
				.findViewById(R.id.homeButton);

		enableImageButton(upLevelButton, R.drawable.metamap_up);
		enableImageButton(homeButton, R.drawable.metamap_home);
	}

	private void setButtonsDisabled() {
		ImageButton upLevelButton = (ImageButton) metaMapActivity
				.findViewById(R.id.upLevelButton);
		ImageButton homeButton = (ImageButton) metaMapActivity
				.findViewById(R.id.homeButton);

		disableImageButton(upLevelButton, R.drawable.metamap_up_grey);
		disableImageButton(homeButton, R.drawable.metamap_home_grey);
	}

	private void bindSwitchViewButton() {
		ImageButton switchButton = (ImageButton) metaMapActivity
				.findViewById(R.id.viewSwitcher);

		switchButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				metaMapActivity.switchView();
			}
		});
	}

	private static void bindSynchronizeButton(final MetaMapActivity activity) {
		ImageButton synchronizeButton = (ImageButton) activity
				.findViewById(R.id.synchronizeButton);

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

		setButtonsEnabled();

		// metaMapActivity.loadMetaMapTopic(currentTopic);
		drawMetaMap();
		updateMetaMap();
	}

	public static void loadLayout(MetaMapActivity activity) {
		activity.setContentView(R.layout.metamap);

		// bing synchronize button
		bindSynchronizeButton(activity);
	}

	public String getMapDescription(Topic topic) {
		return MAP_DESCRIPTION;
	}

	public String getFolderDescription(Topic topic) {
		return FOLDER_DESCRIPTION;
	}
}