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
import com.comapping.android.map.model.text.FormattedText;
import com.comapping.android.map.model.text.TextBlock;
import com.comapping.android.map.model.text.TextFormat;
import com.comapping.android.map.model.text.TextParagraph;
import com.comapping.android.map.model.text.builder.FormattedTextSaxBuilder;

public class XmlBuilder {

	static final String TAB_STRING = "  ";

	private StringBuilder result;

	public String buildXml(Map map) {
		result = new StringBuilder();
		int tabs = 0;
		offset(tabs);
		result.append("<mindmap escape=\"false\">" + "\n");
		tabs++;
		
		offset(tabs);
		result.append("<" + MapBuilder.METADATA_TAG + ">" + "\n");
		tabs++;
		offset(tabs);
		result.append("<" + MapBuilder.MAP_ID_TAG + ">");
		result.append(cData(map.getId() + ""));
		result.append("</" + MapBuilder.MAP_ID_TAG + ">" + "\n");
		offset(tabs);
		result.append("<" + MapBuilder.MAP_NAME_TAG + ">");
		result.append(cData(map.getName() + ""));
		result.append("</" + MapBuilder.MAP_NAME_TAG + ">" + "\n");
		offset(tabs);
		result.append("<" + MapBuilder.MAP_OWNER_TAG + ">" + "\n");
		tabs++;
		
		offset(tabs);
		result.append("<" + MapBuilder.OWNER_ID_TAG + ">");
		result.append(cData(map.getOwner().getId() + ""));
		result.append("</" + MapBuilder.OWNER_ID_TAG + ">" + "\n");
		offset(tabs);
		result.append("<" + MapBuilder.OWNER_NAME_TAG + ">");
		result.append(cData(map.getOwner().getName() + ""));
		result.append("</" + MapBuilder.OWNER_NAME_TAG + ">" + "\n");
		offset(tabs);
		result.append("<" + MapBuilder.OWNER_EMAIL_TAG + ">");
		result.append(cData(map.getOwner().getEmail() + ""));
		result.append("</" + MapBuilder.OWNER_EMAIL_TAG + ">" + "\n");
		
		tabs--;
		offset(tabs);
		result.append("</" + MapBuilder.MAP_OWNER_TAG + ">" + "\n");
		tabs--;
		offset(tabs);
		result.append("</" + MapBuilder.METADATA_TAG + ">" + "\n");
		writeTopic(map.getRoot(), tabs);
		
		tabs--;
		offset(tabs);
		result.append("</mindmap>" + "\n");
		tabs++;
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
	
	private void writeAttribute(StringBuilder result, String attribute, String value) {
		if (value == null) {
			return;
		}
		result.append(" " + attribute + "=\""
				+ value + "\"");
	}

	private String writeText(FormattedText formattedText) {
		StringBuilder result = new StringBuilder();
		for (TextParagraph paragraph : formattedText.getTextParagraphs()) {
			result.append("<" + FormattedTextSaxBuilder.PARAGRAPH_TAG + ">");
			for (TextBlock block : paragraph.getTextBlocks()) {
				String text = block.getText();
				TextFormat format = block.getFormat();
				result.append("<" + FormattedTextSaxBuilder.FONT_TAG);
//				writeAttribute(result, FormattedTextSaxBuilder.FONT_ATTR_COLOR_TAG, format.getFontColor() + "");
				writeAttribute(result, FormattedTextSaxBuilder.FONT_ATTR_SIZE_TAG, format.getFontSize() + "");
				result.append(">");
				if (format.isUnderlined()) {
					result.append("<" + FormattedTextSaxBuilder.UNDERLINED_TAG + ">");
				}
				result.append(text);
				if (format.isUnderlined()) {
					result.append("</" + FormattedTextSaxBuilder.UNDERLINED_TAG + ">");
				}
				result.append("</" + FormattedTextSaxBuilder.FONT_TAG + ">");
			}
			result.append("</" + FormattedTextSaxBuilder.PARAGRAPH_TAG + ">");
		}
		return result.toString();
	}

	private void writeTopic(Topic topic, int tabs) {
		offset(tabs);

		result.append("<" + MapBuilder.TOPIC_TAG);
		writeAttribute(result, MapBuilder.TOPIC_ARROW_ATTR, Arrow.write(topic.getArrow()));
		writeAttribute(result, MapBuilder.TOPIC_FLAG_ATTR, Flag.write(topic.getFlag()));
		writeAttribute(result, MapBuilder.TOPIC_ID_ATTR, topic.getId() + "");
		if (topic.getBgColor() != Color.WHITE) {
			writeAttribute(result, MapBuilder.TOPIC_BGCOLOR_ATTR, topic.getBgColor() + "");
		}
		if (topic.getPriority() != 0) {
			writeAttribute(result, MapBuilder.TOPIC_PRIORITY_ATTR, topic.getPriority() + "");
		}
		writeAttribute(result, MapBuilder.TOPIC_SMILEY_ATTR, Smiley.write(topic.getSmiley()));
		writeAttribute(result, MapBuilder.TOPIC_STAR_ATTR, Star.write(topic.getStar()));
		writeAttribute(result, MapBuilder.TOPIC_TASK_COMPLETION_ATTR, TaskCompletion.write(topic.getTaskCompletion()));
		result.append(">\n");
		
		tabs++;
		List <Icon> icons = topic.getIcons();
		for (Icon icon : icons) {
			offset(tabs);
			result.append("<" + MapBuilder.TOPIC_ICON_TAG);
			writeAttribute(result, MapBuilder.ICON_NAME_ATTR, Icon.write(icon));
			result.append("/>\n");
		}
		
		Task task = topic.getTask();
		if (task != null) {
			offset(tabs);
			result.append("<" + MapBuilder.TOPIC_TASK_TAG);
			writeAttribute(result, MapBuilder.TASK_DEADLINE_ATTR, task.getDeadline());
			writeAttribute(result, MapBuilder.TASK_ESTIMATE_ATTR, task.getEstimate());
			writeAttribute(result, MapBuilder.TASK_RESPONSIBLE_ATTR, task.getResponsible());
			writeAttribute(result, MapBuilder.TASK_START_ATTR, task.getStart());
			result.append("/>\n");
		}
		
		Attachment attachment = topic.getAttachment();
		if (attachment != null) {
			offset(tabs);
			result.append("<" + MapBuilder.TOPIC_ATTACHMENT_TAG);
			writeAttribute(result, MapBuilder.ATTACHMENT_DATE_ATTR, (float) attachment.getDate().getTime() + "");
			writeAttribute(result, MapBuilder.ATTACHMENT_FILENAME_ATTR, attachment.getFilename());
			writeAttribute(result, MapBuilder.ATTACHMENT_KEY_ATTR, attachment.getKey());
			writeAttribute(result, MapBuilder.ATTACHMENT_SIZE_ATTR, attachment.getSize() + "");
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
		//result.append(cData(writeText(topic.getFormattedText())));
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
