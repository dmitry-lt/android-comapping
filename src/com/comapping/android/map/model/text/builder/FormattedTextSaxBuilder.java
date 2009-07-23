package com.comapping.android.map.model.text.builder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import com.comapping.android.Log;
import com.comapping.android.map.model.exceptions.DocumentBuilderCreatingError;
import com.comapping.android.map.model.exceptions.StringToXMLConvertionException;
import com.comapping.android.map.model.text.FormattedText;
import com.comapping.android.map.model.text.TextFormat;

public class FormattedTextSaxBuilder {

	public static final String PARAGRAPH_TAG = "P";
	public static final String FONT_TAG = "FONT";
	public static final String FONT_ATTR_SIZE_TAG = "SIZE";
	public static final String FONT_ATTR_COLOR_TAG = "COLOR";
	public static final String HYPER_REF_TAG = "A";
	public static final String HYPER_REF_ATTR_HREF_TAG = "HREF";
	public static final String UNDERLINED_TAG = "U";

	private static final String ERROR_TEXT = "#ERROR#";

	private static FormattedTextSaxHandler handler;
	private static SAXParserFactory saxParserFactory;
	private static SAXParser parser;

	private static void init() {
		if (saxParserFactory == null)
			saxParserFactory = SAXParserFactory.newInstance();

		try {
			if (parser == null)
				parser = saxParserFactory.newSAXParser();
			else
				parser.reset();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		handler = new FormattedTextSaxHandler();
	}

	static TextFormat getDefFormat() {
		return new TextFormat();
	}

	//@SuppressWarnings("deprecation")
	public static FormattedText buildFormattedText(String xmlString)
			throws StringToXMLConvertionException {
		
		if (!xmlString.contains("<"))
		{
			xmlString = xmlString.replace("&gt;", ">");
			xmlString = xmlString.replace("&lt;", "<");
			xmlString = xmlString.replace("&apos;", "\"");
			xmlString = xmlString.replace("&quot;", "'");
			xmlString = xmlString.replace("&amp;", "&");
			return new FormattedText(xmlString, getDefFormat());
		}
		
		// Ugly "&" Fix
		xmlString = xmlString.replace('&', FormattedTextSaxHandler.AndReplacer);

		if (xmlString.startsWith("<P")) {
			xmlString = "<TEXT>" + xmlString + "</TEXT>";
		} else if (xmlString.startsWith("<FONT")) {
			xmlString = "<TEXT><P>" + xmlString + "</P></TEXT>";
		} else {
			xmlString = "<TEXT><P><FONT>" + xmlString + "</FONT></P></TEXT>";
		}

		FormattedText resultText;
		init();
		try {

			InputStream stream = new ByteArrayInputStream(xmlString.getBytes("UTF-8"));

			parser.parse(stream, handler);
			resultText = handler.getFormattedText();
		} catch (FactoryConfigurationError e) {
			Log.e("SAX Text Parser", e.toString());
			throw new DocumentBuilderCreatingError();
		} catch (SAXException e) {
			Log.e(Log.MODEL_TAG, "cannot convert string to xml:" + e.toString());
			Log.e(Log.MODEL_TAG, "error while parsing text: " + xmlString);
			resultText = new FormattedText(ERROR_TEXT, getDefFormat());
		} catch (IOException e) {
			Log.e(Log.MODEL_TAG, "cannot convert string to xml:" + e.toString());
			throw new StringToXMLConvertionException();
		}
		return resultText;
	}
}