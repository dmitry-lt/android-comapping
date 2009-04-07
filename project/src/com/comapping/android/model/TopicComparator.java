package com.comapping.android.model;

import java.util.Comparator;

public class TopicComparator implements Comparator<Topic> {

	@Override
	public int compare(Topic topic1, Topic topic2) {
		if ((topic1.isFolder() && topic2.isFolder()) || 
			(!topic1.isFolder() && !topic2.isFolder())) {
			// if both folder or both maps we compare texts
			return topic1.getText().compareTo(topic2.getText());
		} else {
			if (topic1.isFolder()) {
				return -1;
			} else {
				return 1;
			}
		}
	}

}