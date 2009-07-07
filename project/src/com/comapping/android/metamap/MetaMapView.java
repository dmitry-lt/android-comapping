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
import com.comapping.android.metamap.provider.MetaMapProvider;
import com.comapping.android.metamap.provider.SdCardProvider;
import com.comapping.android.storage.PreferencesStorage;

public class MetaMapView {

	// Button id's

	private static final int UP_LEVEL = 1;
	private static final int HOME = 2;
	private static final int SYNC = 3;

	protected static final String MAP_DESCRIPTION = "Map";
	protected static final String FOLDER_DESCRIPTION = "Folder";

	protected MetaMapActivity metaMapActivity;

	public MetaMapProvider provider;

	private MetaMapProvider sdCardProvider = new SdCardProvider();

	public MetaMapView(MetaMapProvider _provider) {

		provider = _provider;
	}

	public void updateMetaMap() {

		// list view
		ListView listView = (ListView) metaMapActivity
				.findViewById(R.id.listView);

		MetaMapItem[] items = provider.getCurrentLevel();
		listView.setAdapter(new MetaMapListAdapter(metaMapActivity, items));

		// Buttons

		if (provider.canGoHome())
			enableButton(HOME);
		else
			disableButton(HOME);

		if (provider.canGoUp())
			enableButton(UP_LEVEL);
		else
			disableButton(UP_LEVEL);

		if (provider.canSync())
			enableButton(SYNC);
		else
			disableButton(SYNC);

	}

	void enableButton(int id) {
		int resource = 0;
		ImageButton button = null;

		switch (id) {
		case UP_LEVEL:
			button = (ImageButton) metaMapActivity
					.findViewById(R.id.upLevelButton);
			resource = R.drawable.metamap_up;
			break;
		case HOME:
			button = (ImageButton) metaMapActivity
					.findViewById(R.id.homeButton);
			resource = R.drawable.metamap_home;
			break;
		case SYNC:
			button = (ImageButton) metaMapActivity
					.findViewById(R.id.synchronizeButton);
			resource = R.drawable.menu_reload;
			break;

		default:
			return;
		}

		button.setEnabled(true);
		button.setFocusable(true);

		button.setImageResource(resource);
	}

	void disableButton(int id) {
		int resource = 0;
		ImageButton button = null;

		switch (id) {
		case UP_LEVEL:
			button = (ImageButton) metaMapActivity
					.findViewById(R.id.upLevelButton);
			resource = R.drawable.metamap_up_grey;
			break;
		case HOME:
			button = (ImageButton) metaMapActivity
					.findViewById(R.id.homeButton);
			resource = R.drawable.metamap_home_grey;
			break;
		case SYNC:
			button = (ImageButton) metaMapActivity
					.findViewById(R.id.synchronizeButton);
			resource = R.drawable.menu_reload_grey;
			break;

		default:
			return;
		}

		button.setEnabled(false);
		button.setFocusable(false);

		button.setImageResource(resource);
	}

	void initButtons(MetaMapActivity activity) {

		// Sync

		ImageButton synchronizeButton = (ImageButton) activity
				.findViewById(R.id.synchronizeButton);

		final MetaMapActivity act = activity;
		synchronizeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				act.synchronize();
			}
		});

		// Switch view

		ImageButton switchButton = (ImageButton) activity
				.findViewById(R.id.viewSwitcher);

		switchButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				metaMapActivity.switchView();
			}
		});

		// Home

		ImageButton homeButton = (ImageButton) metaMapActivity
				.findViewById(R.id.homeButton);

		homeButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				provider.goHome();
				updateMetaMap();
			}
		});

		// Up level

		ImageButton upLevelButton = (ImageButton) metaMapActivity
				.findViewById(R.id.upLevelButton);

		upLevelButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				provider.goUp();
				updateMetaMap();
			}
		});

		// Sync

		ImageButton syncButton = (ImageButton) metaMapActivity
				.findViewById(R.id.synchronizeButton);

		syncButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				provider.sync();
				updateMetaMap();
			}
		});
	}

	void initListView(MetaMapActivity activity) {
		ListView listView = (ListView) activity.findViewById(R.id.listView);
		activity.registerForContextMenu(listView);
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

		TextView text = new TextView(activity);
		text.setText("Test text");
		listView.setEmptyView(text);
	}

	public void activate(MetaMapActivity _metaMapActivity) {
		metaMapActivity = _metaMapActivity;

		initButtons(metaMapActivity);
		initListView(metaMapActivity);

		updateMetaMap();
	}

	public static void loadLayout(MetaMapActivity activity) {
		activity.setContentView(R.layout.metamap);
	}
}