package com.comapping.android.map.model.map.builder;

import com.comapping.android.map.model.map.Map;
import com.comapping.android.map.model.map.Topic;

public class XmlBuilder {
	
	static final String TAB_STRING = "   ";
	
	public String buildXml(Map map) {
		String result = "";
		writeTopic(map.getRoot(), result, 0);
		return result;
	}
	
	private void writeTopic(Topic topic, String result, int tabs) {
		for (int i = 0; i < tabs; i++) {
			result = result + TAB_STRING;
		}
		result = result + "<" + MapBuilder.TOPIC_TAG;
		result = result + ">\n";
		for (int i = 0; i < tabs; i++) {
			result = result + TAB_STRING;
		}
		result = result + "</" + MapBuilder.TOPIC_TAG + ">\n";
	}
	
}
