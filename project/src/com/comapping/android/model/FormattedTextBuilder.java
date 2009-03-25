package com.comapping.android.model;

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
		
		TextBlock first;
		if (document.getDocumentElement() == null) {
			first = new TextBlock(xmlString, defFormat);
		} else {
			first = buildTextBlock(document.getDocumentElement(), defFormat);
		}

		return new FormattedText(first);
	}

	private static TextBlock buildTextBlock(Node node, TextFormat curFormat) {
		Log.d(Log.modelTag, "nodeName: " + node.getNodeName());
		if (node.getNodeType() == Node.TEXT_NODE) {
			Log.d(Log.modelTag, "nodeValue: " + node.getNodeValue());
			return new TextBlock(node.getNodeValue(), curFormat);

		} else {
			NamedNodeMap attributes = node.getAttributes();
			if (attributes.getLength() == 0)
				changeFormat(curFormat, node.getNodeName(), "", "");
			for (int i = 0; i < attributes.getLength(); i++) {
				Node curAttribute = attributes.item(i);
				changeFormat(curFormat, node.getNodeName(), curAttribute.getNodeName(), curAttribute.getNodeValue());
			}

			NodeList childNodes = node.getChildNodes();
			if (childNodes.getLength() != 0) {
				TextBlock first = buildTextBlock(childNodes.item(0), curFormat.clone());
				TextBlock cur = first;
				for (int i = 1; i < childNodes.getLength(); i++) {
					cur.setNext(buildTextBlock(childNodes.item(i), curFormat.clone()));
					cur = cur.getNext();
				}

				return first;
			} else {
				return new TextBlock("", curFormat);
			}
		}
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
