package com.comapping.android.metamap;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import com.comapping.android.metamap.MetaMapListAdapter.MetaMapItem;
import com.comapping.android.model.map.Map;
import com.comapping.android.model.map.Topic;

public class ComappingProvider extends MetaMapProvider {
	
	private static final String LAST_SYNCHRONIZATION = "Last synchronization";
	
	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	protected static final String MAP_DESCRIPTION = "Map";
	protected static final String FOLDER_DESCRIPTION = "Folder";
	
	Map metamap;
	Topic currentLevel;
	
	public ComappingProvider(Map _metamap)
	{
		metamap = _metamap;
		currentLevel = metamap.getRoot();
	}
	
	MetaMapListAdapter.MetaMapItem[] getItems(Topic[] topics) {
		MetaMapListAdapter.MetaMapItem[] res = new MetaMapListAdapter.MetaMapItem[topics.length];

		for (int i = 0; i < topics.length; i++) {
			res[i] = new MetaMapListAdapter.MetaMapItem();
			res[i].name = topics[i].getText();

			res[i].isFolder = topics[i].isFolder();
			
			if (res[i].isFolder)
			{
				res[i].description = getFolderDescription(topics[i]);
			}
			else
			{
				res[i].description = getMapDescription(topics[i]);
			}
			
			res[i].reference = topics[i].getMapRef();
		}

		return res;
	}
	
	public String getMapDescription(Topic topic) {
		Timestamp lastSynchronizationDate = MetaMapActivity.client.getLastSynchronizationDate(topic.getMapRef());
		
		if (lastSynchronizationDate == null) {
			return MAP_DESCRIPTION;
		} else {
			SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
			
			return LAST_SYNCHRONIZATION+": "+dateFormat.format(lastSynchronizationDate);
		}
	}
	
	public String getFolderDescription(Topic topic) {
		return FOLDER_DESCRIPTION;
	}
	
	@Override
	public MetaMapItem[] getCurrentLevel() {
		return getItems(currentLevel.getChildTopics());
	}

	@Override
	public void goHome() {
		currentLevel = metamap.getRoot();
	}

	@Override
	public void goUp() {
		
		// TODO Fix it now i'm too lazy
		
		currentLevel = metamap.getRoot();
	}

	@Override
	public void gotoFolder(int index) {
		if (currentLevel.getChildByIndex(index).isFolder())
		{
			currentLevel = currentLevel.getChildByIndex(index);
		}
	}
}
