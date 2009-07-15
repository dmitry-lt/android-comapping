package com.comapping.android.map.model.map.builder;

import com.comapping.android.map.model.map.Arrow;
import com.comapping.android.map.model.map.Flag;
import com.comapping.android.map.model.map.Map;
import com.comapping.android.map.model.map.Smiley;
import com.comapping.android.map.model.map.Star;
import com.comapping.android.map.model.map.TaskCompletion;
import com.comapping.android.map.model.map.Topic;

public class XmlBuilder {

	static final String TAB_STRING = "   ";

	private StringBuilder result;

	public String buildXml(Map map) {
		result = new StringBuilder();
		writeTopic(map.getRoot(), 0);
		return result.toString();
	}

	private void writeAttribute(String attribute, String value) {
		if (value == null) {
			return;
		}
		result.append(" " + attribute + "=\""
				+ value + "\"");
	}

	private void writeTopic(Topic topic, int tabs) {
		for (int i = 0; i < tabs; i++) {
			result.append(TAB_STRING);
		}

		result.append("<" + MapBuilder.TOPIC_TAG);
		writeAttribute(MapBuilder.TOPIC_ARROW_ATTR, Arrow.write(topic.getArrow()));
		writeAttribute(MapBuilder.TOPIC_FLAG_ATTR, Flag.write(topic.getFlag()));
		writeAttribute(MapBuilder.TOPIC_ID_ATTR, topic.getId() + "");
		writeAttribute(MapBuilder.TOPIC_BGCOLOR_ATTR, topic.getBgColor() + "");
		if (topic.getPriority() != 0) {
			writeAttribute(MapBuilder.TOPIC_PRIORITY_ATTR, topic.getPriority() + "");
		}
		writeAttribute(MapBuilder.TOPIC_SMILEY_ATTR, Smiley.write(topic.getSmiley()));
		writeAttribute(MapBuilder.TOPIC_STAR_ATTR, Star.write(topic.getStar()));
		writeAttribute(MapBuilder.TOPIC_TASK_COMPLETION_ATTR, TaskCompletion.write(topic.getTaskCompletion()));
		result.append(">\n");

		for (int i = 0; i < topic.getChildrenCount(); i++) {
			writeTopic(topic.getChildByIndex(i), tabs + 1);
		}

		for (int i = 0; i < tabs; i++) {
			result.append(TAB_STRING);
		}
		result.append("</" + MapBuilder.TOPIC_TAG + ">\n");
	}

}
