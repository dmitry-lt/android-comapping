package com.comapping.android;

import java.util.List;

import com.comapping.android.map.model.map.Icon;
import com.comapping.android.map.model.map.Map;
import com.comapping.android.map.model.map.Topic;

public class TestHelper {
	
	public static boolean objectEquals(Object o1, Object o2) {
		if (o1 == null && o2 == null) {
			return true;
		}
		if (o1 == null || o2 == null) {
			return false;
		}
		return o1.equals(o2);
	}
	
	public static boolean topicSimpleEquals(Topic topic1, Topic topic2) {
		if (topic1.getBgColor() != topic2.getBgColor()) {
			return false;
		}
		if (!objectEquals(topic1.getFlag(), topic2.getFlag())) {
			return false;
		}
		if (topic1.getPriority() != topic2.getPriority()) {
			return false;
		}
		if (!objectEquals(topic1.getSmiley(), topic2.getSmiley())) {
			return false;
		}
		if (!objectEquals(topic1.getTaskCompletion(), topic2.getTaskCompletion())) {
			return false;
		}
		if (!objectEquals(topic1.getText(), topic2.getText())) {
			return false;
		}
		if (topic1.getIconCount() != topic2.getIconCount()) {
			return false;
		}
		List <Icon> icons = topic1.getIcons();
		for (Icon icon : icons) {
			if (!topic2.hasIcon(icon)) {
				return false;
			}
		}
		if (!objectEquals(topic1.getNote(), topic2.getNote())) {
			return false;
		}
		
		return true;
	}

	private static boolean treeEquals(Topic topic1, Topic topic2) {
		if (topic1 == null && topic2 == null) {
			return true;
		}
		if (topic1 == null || topic2 == null) {
			return false;
		}
		if (topic1.getChildrenCount() != topic2.getChildrenCount()) {
			return false;
		}
		if (!topicSimpleEquals(topic1, topic2)) {
			return false;
		}
		for (int i = 0; i < topic1.getChildrenCount(); i++) {
			if (!treeEquals(topic1.getChildByIndex(i), topic2.getChildByIndex(i))) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean mapSimpleEquals(Map map1, Map map2) {
		return treeEquals(map1.getRoot(), map2.getRoot());
	}	
	
}