package com.comapping.android.map.model.map.builder;

import java.util.List;

import android.graphics.Color;

import com.comapping.android.map.model.map.Arrow;
import com.comapping.android.map.model.map.Attachment;
import com.comapping.android.map.model.map.Flag;
import com.comapping.android.map.model.map.Icon;
import com.comapping.android.map.model.map.Map;
import com.comapping.android.map.model.map.Smiley;
import com.comapping.android.map.model.map.Star;
import com.comapping.android.map.model.map.Task;
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

	private void offset(int tabs) {
		for (int i = 0; i < tabs; i++) {
			result.append(TAB_STRING);
		}
	}
	
	private String cData(String s) {
		return "<![CDATA[" + s + "]]>";
	}
	
	private void writeAttribute(String attribute, String value) {
		if (value == null) {
			return;
		}
		result.append(" " + attribute + "=\""
				+ value + "\"");
	}

	private void writeTopic(Topic topic, int tabs) {
		offset(tabs);

		result.append("<" + MapBuilder.TOPIC_TAG);
		writeAttribute(MapBuilder.TOPIC_ARROW_ATTR, Arrow.write(topic.getArrow()));
		writeAttribute(MapBuilder.TOPIC_FLAG_ATTR, Flag.write(topic.getFlag()));
		writeAttribute(MapBuilder.TOPIC_ID_ATTR, topic.getId() + "");
		if (topic.getBgColor() != Color.WHITE) {
			writeAttribute(MapBuilder.TOPIC_BGCOLOR_ATTR, topic.getBgColor() + "");
		}
		if (topic.getPriority() != 0) {
			writeAttribute(MapBuilder.TOPIC_PRIORITY_ATTR, topic.getPriority() + "");
		}
		writeAttribute(MapBuilder.TOPIC_SMILEY_ATTR, Smiley.write(topic.getSmiley()));
		writeAttribute(MapBuilder.TOPIC_STAR_ATTR, Star.write(topic.getStar()));
		writeAttribute(MapBuilder.TOPIC_TASK_COMPLETION_ATTR, TaskCompletion.write(topic.getTaskCompletion()));
		result.append(">\n");
		
		tabs++;
		List <Icon> icons = topic.getIcons();
		for (Icon icon : icons) {
			offset(tabs);
			result.append("<" + MapBuilder.TOPIC_ICON_TAG);
			writeAttribute(MapBuilder.ICON_NAME_ATTR, Icon.write(icon));
			result.append("/>\n");
		}
		
		Task task = topic.getTask();
		if (task != null) {
			offset(tabs);
			result.append("<" + MapBuilder.TOPIC_TASK_TAG);
			writeAttribute(MapBuilder.TASK_DEADLINE_ATTR, task.getDeadline());
			writeAttribute(MapBuilder.TASK_ESTIMATE_ATTR, task.getEstimate());
			writeAttribute(MapBuilder.TASK_RESPONSIBLE_ATTR, task.getResponsible());
			writeAttribute(MapBuilder.TASK_START_ATTR, task.getStart());
			result.append("/>\n");
		}
		
		Attachment attachment = topic.getAttachment();
		if (attachment != null) {
			offset(tabs);
			result.append("<" + MapBuilder.TOPIC_ATTACHMENT_TAG);
			writeAttribute(MapBuilder.ATTACHMENT_DATE_ATTR, (float) attachment.getDate().getTime() + "");
			writeAttribute(MapBuilder.ATTACHMENT_FILENAME_ATTR, attachment.getFilename());
			writeAttribute(MapBuilder.ATTACHMENT_KEY_ATTR, attachment.getKey());
			writeAttribute(MapBuilder.ATTACHMENT_SIZE_ATTR, attachment.getSize() + "");
			result.append("/>\n");
		}
		
		String note = topic.getNote();
		if (note != null) {
			offset(tabs);
			result.append("<" + MapBuilder.TOPIC_NOTE_TAG + ">");
			result.append(cData(note));
			result.append("</" + MapBuilder.TOPIC_NOTE_TAG + ">" + "\n");
		}
		
		offset(tabs);
		result.append("<" + MapBuilder.TOPIC_TEXT_TAG + ">");
		result.append(cData(topic.getText()));
		result.append("</" + MapBuilder.TOPIC_TEXT_TAG + ">" + "\n");
		
		for (int i = 0; i < topic.getChildrenCount(); i++) {
			writeTopic(topic.getChildByIndex(i), tabs);
		}

		tabs--;
		offset(tabs);
		result.append("</" + MapBuilder.TOPIC_TAG + ">\n");
	}

}
