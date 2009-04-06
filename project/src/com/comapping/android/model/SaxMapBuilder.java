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

public class SaxMapBuilder {

	private static SaxHandler handler;
	
	public static Map saxMapBuilder(String xmlText) throws StringToXMLConvertionException {
		try {
			
			SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		    SAXParser parser = saxParserFactory.newSAXParser();
			

			InputStream stream = new ByteArrayInputStream(xmlText.getBytes("UTF-8"));
			handler = new SaxHandler();
			
			parser.parse(stream,handler);
			Log.i(Log.modelTag, "parsing xml document: \n" + xmlText);
		} catch (FactoryConfigurationError e) {
			Log.e("SAX Parser", e.toString());
			throw new DocumentBuilderCreatingError();
		} catch (ParserConfigurationException e) {
			Log.e("SAX Parser", e.toString());
			throw new DocumentBuilderCreatingError();
		} catch (SAXException e) {
			Log.e(Log.modelTag, "cannot convert string to xml:" + e.toString());
			throw new StringToXMLConvertionException();
		} catch (IOException e) {
			Log.e(Log.modelTag, "cannot convert string to xml:" + e.toString());
			throw new StringToXMLConvertionException();
		}
	return handler.getMap();
	}
}

