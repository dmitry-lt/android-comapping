package com.comapping.android.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import com.comapping.android.Log;
import com.comapping.android.model.exceptions.DocumentBuilderCreatingError;
import com.comapping.android.model.exceptions.StringToXMLConvertionException;

public class FormattedTextSaxBuilder {

	static final String PARAGRAPH_TAG = "P";
	static final String FONT_TAG = "FONT";
	static final String FONT_ATTR_SIZE_TAG = "SIZE";
	static final String FONT_ATTR_COLOR_TAG = "COLOR";
	static final String HYPER_REF_TAG = "A";
	static final String HYPER_REF_ATTR_HREF_TAG = "HREF";
	static final String UNDERLINED_TAG = "U";	
	
	private static FormattedTextSaxHandler handler;

	public static FormattedText buildFormattedText(String xmlString) throws StringToXMLConvertionException {
		
		long startTime = System.currentTimeMillis();
		
		if (xmlString.startsWith("<P")) {
			xmlString = "<TEXT>" + xmlString + "</TEXT>";
		} else if (xmlString.startsWith("<FONT")) {
			xmlString = "<TEXT><P>" + xmlString + "</P></TEXT>";
		} else {
			xmlString = "<TEXT><P><FONT>" + xmlString + "</FONT></P></TEXT>";
		}

		try {
			SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
			SAXParser parser = saxParserFactory.newSAXParser();

			InputStream stream = new ByteArrayInputStream(xmlString.getBytes("UTF-8"));
			handler = new FormattedTextSaxHandler();

			parser.parse(stream, handler);
			Log.d(Log.modelTag, "Text SAX parsing: " + xmlString);
			
		} catch (FactoryConfigurationError e) {
			Log.e("SAX Text Parser", e.toString());
			throw new DocumentBuilderCreatingError();
		} catch (ParserConfigurationException e) {
			Log.e("SAX Text Parser", e.toString());
			throw new DocumentBuilderCreatingError();
		} catch (SAXException e) {
			Log.e(Log.modelTag, "cannot convert string to xml:" + e.toString());
			throw new StringToXMLConvertionException();
		} catch (IOException e) {
			Log.e(Log.modelTag, "cannot convert string to xml:" + e.toString());
			throw new StringToXMLConvertionException();
		}

		long parsingTime = System.currentTimeMillis() - startTime;
		
		Log.w(Log.modelTag, "Formatted Text was built with SAX successfully, parsing time: " + parsingTime);
		
		return handler.getFormattedText();
	}
}