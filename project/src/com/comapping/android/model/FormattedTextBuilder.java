package com.comapping.android.model;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.graphics.Color;

import com.comapping.android.Log;

public class FormattedTextBuilder {
	private static final String FONT_TAG = "FONT";
	private static final String FONT_ATTR_SIZE_TAG = "SIZE";
	private static final String FONT_ATTR_COLOR_TAG = "COLOR";
	private static final String HYPER_REF_TAG = "A";
	private static final String HYPER_REF_ATTR_HREF_TAG = "HREF";
	private static final String UNDERLINED_TAG = "U";

	private static final TextFormat defFormat = getDefFormat();

	public static FormattedText buildFormattedText(String xmlString) throws StringToXMLConvertionException {
		xmlString = xmlString.replace('&', '#');
		xmlString = "<P><FONT>" + xmlString + "</FONT></P>";
		Log.d(Log.modelTag, "parsing text: " + xmlString);
		
		Document document = DocumentBuilder.buildDocument(xmlString);
		
		List<TextBlock> textBlocks = buildTextBlocks(document.getDocumentElement(), defFormat);		

		return new FormattedText(textBlocks);
	}

	private static List<TextBlock> buildTextBlocks(Node node, TextFormat curFormat) {
		List<TextBlock> result = new ArrayList<TextBlock>();
		
		Log.d(Log.modelTag, "nodeName: " + node.getNodeName());
		if (node.getNodeType() == Node.TEXT_NODE) {
			Log.d(Log.modelTag, "nodeValue: " + node.getNodeValue());
			result.add(new TextBlock(node.getNodeValue(), curFormat));

		} else {
			NamedNodeMap attributes = node.getAttributes();
			if (attributes.getLength() == 0)
				changeFormat(curFormat, node.getNodeName(), "", "");
			for (int i = 0; i < attributes.getLength(); i++) {
				Node curAttribute = attributes.item(i);
				changeFormat(curFormat, node.getNodeName(), curAttribute.getNodeName(), curAttribute.getNodeValue());
			}

			NodeList childNodes = node.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				result.addAll(buildTextBlocks(childNodes.item(i), curFormat.clone()));					
			}									
		}
		
		return result;
	}

	private static void changeFormat(TextFormat format, String tag, String attr, String value) {
		if (tag.equals(FONT_TAG)) {
			if (attr.equals(FONT_ATTR_SIZE_TAG)) {
				format.setFontSize(Integer.parseInt(value));
			} else if (attr.equals(FONT_ATTR_COLOR_TAG)) {
				format.setFontColor(Color.parseColor(value));
			}
		} else if (tag.equals(HYPER_REF_TAG)) {
			if (attr.equals(HYPER_REF_ATTR_HREF_TAG)) {
				format.setHRef(value);
			}
		} else if (tag.equals(UNDERLINED_TAG)) {
			format.setUnderlined(true);
		}
	}

	private static TextFormat getDefFormat() {
		TextFormat format = new TextFormat();
		format.setFontSize(16);
		format.setFontColor(Color.BLACK);
		format.setHRef("");
		format.setUnderlined(false);
		return format;
	}
}
