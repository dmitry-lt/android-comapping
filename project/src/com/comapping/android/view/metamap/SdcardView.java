package com.comapping.android.view.metamap;

import java.io.File;
import java.io.FilenameFilter;

import android.widget.ImageButton;

import com.comapping.android.controller.MetaMapActivity;
import com.comapping.android.controller.R;
import com.comapping.android.model.Map;
import com.comapping.android.model.Topic;

public class SdcardView extends MetaMapView {
	final static private FilenameFilter filter = new FilenameFilter() {
		public boolean accept(File dir, String filename) {
			return (new File(dir.getAbsolutePath() + "/" + filename)).isDirectory()
					|| filename.toLowerCase().endsWith(".comap");
		}
	};

	private static Map getSdMap() {
		Map sdMap = new Map(0);
		Topic root = new Topic(null);
		root.setText("sdcard");
		root.setNote("/sdcard");
		sdMap.setRoot(root);
		return sdMap;
	}

	public SdcardView() {
		super(getSdMap());
	}

	public void prepareTopic(Topic topic) {
		topic.removeAllChildTopics();
		File directory = new File(topic.getNote());
		for (File file : directory.listFiles(filter)) {
			Topic newTopic = new Topic(topic);
			newTopic.setText(file.getName());
			newTopic.setNote(file.getAbsolutePath());
			if (!file.isDirectory()) {
				newTopic.setMapRef(file.getAbsolutePath());
			}
			topic.addChild(newTopic);
		}
	}

	public void activate(MetaMapActivity _metaMapActivity) {
		super.activate(_metaMapActivity);

		drawMetaMap();
		// change button to switch
		ImageButton switchButton = (ImageButton) metaMapActivity.findViewById(R.id.viewSwitcher);
		switchButton.setImageResource(R.drawable.internet_icon);

		// deactivate synchronize button
		ImageButton synchronizeButton = (ImageButton) metaMapActivity.findViewById(R.id.synchronizeButton);
		synchronizeButton.setEnabled(false);
	}
}
