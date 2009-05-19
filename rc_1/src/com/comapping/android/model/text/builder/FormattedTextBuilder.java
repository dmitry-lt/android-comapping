package com.comapping.android.model.text.builder;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.graphics.Color;

import com.comapping.android.Log;
import com.comapping.android.model.DocumentBuilder;
import com.comapping.android.model.exceptions.StringToXMLConvertionException;
import com.comapping.android.model.text.FormattedText;
import com.comapping.android.model.text.TextBlock;
import com.comapping.android.model.text.TextFormat;
import com.comapping.android.model.text.TextParagraph;

public class FormattedTextBuilder {
	private static final String PARAGRAPH_TAG = "P";

	private static final String FONT_TAG = "FONT";
	private static final String FONT_ATTR_SIZE_TAG = "SIZE";
	private static final String FONT_ATTR_COLOR_TAG = "COLOR";
	private static final String HYPER_REF_TAG = "A";
	private static final String HYPER_REF_ATTR_HREF_TAG = "HREF";
	private static final String UNDERLINED_TAG = "U";

	private static final TextFormat defFormat = getDefFormat();

	public static FormattedText buildFormattedText(String xmlString) throws StringToXMLConvertionException {
		if (xmlString.startsWith("<P")) {
			xmlString = "<TEXT>" + xmlString + "</TEXT>";
		} else if (xmlString.startsWith("<FONT")) {
			xmlString = "<TEXT><P>" + xmlString + "</P></TEXT>";
		} else {
			xmlString = "<TEXT><P><FONT>" + xmlString + "</FONT></P></TEXT>";
		}

		Log.d(Log.MODEL_TAG, "parsing text: " + xmlString);

		Document document = DocumentBuilder.buildDocument(xmlString);

		NodeList paragraphNodes = document.getElementsByTagName(PARAGRAPH_TAG);
		List<TextParagraph> textParagraphs = new ArrayList<TextParagraph>();
		for (int i = 0; i < paragraphNodes.getLength(); i++) {
			List<TextBlock> textBlocks = buildTextBlocks(paragraphNodes.item(i), defFormat);
			textParagraphs.add(new TextParagraph(textBlocks));
		}

		return new FormattedText(textParagraphs);
	}

	private static List<TextBlock> buildTextBlocks(Node node, TextFormat curFormat) {
		List<TextBlock> result = new ArrayList<TextBlock>();

		if (node.getNodeType() == Node.TEXT_NODE) {
			String text = node.getNodeValue();
			result.add(new TextBlock(text, curFormat));

		} else {
			if (node.hasAttributes()) {
				NamedNodeMap attributes = node.getAttributes();
				for (int i = 0; i < attributes.getLength(); i++) {
					Node curAttribute = attributes.item(i);
					changeFormat(curFormat, node.getNodeName(), curAttribute.getNodeName(), curAttribute.getNodeValue());
				}
			} else {
				changeFormat(curFormat, node.getNodeName(), "", "");
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
