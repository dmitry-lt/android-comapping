package com.comapping.android.model.map.builder;

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
import com.comapping.android.model.exceptions.MapParsingException;
import com.comapping.android.model.exceptions.StringToXMLConvertionException;
import com.comapping.android.model.map.Map;

public class SaxMapBuilder extends MapBuilder {

	SaxHandler handler;

	
	public Map buildMap(String xmlDocument) throws StringToXMLConvertionException, MapParsingException {
		try {
			SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
			SAXParser parser = saxParserFactory.newSAXParser();

			InputStream stream = new ByteArrayInputStream(xmlDocument.getBytes("UTF-8"));
			handler = new SaxHandler();

			Log.d(Log.MODEL_TAG, "parsing xml document: \n" + xmlDocument);
			
			parser.parse(stream, handler);			
		} catch (FactoryConfigurationError e) {
			Log.e("SAX Parser", e.toString());
			throw new DocumentBuilderCreatingError();
		} catch (ParserConfigurationException e) {
			Log.e("SAX Parser", e.toString());
			throw new DocumentBuilderCreatingError();
		} catch (SAXException e) {
			Log.w(Log.MODEL_TAG, "cannot convert string to xml:" + e.toString());
			throw new StringToXMLConvertionException();
		} catch (IOException e) {
			Log.w(Log.MODEL_TAG, "cannot convert string to xml:" + e.toString());
			throw new StringToXMLConvertionException();
		} catch (NumberFormatException e) {			
			Log.w(Log.MODEL_TAG, "wrong map format " + e.toString());
			throw new MapParsingException();
		}

		return handler.getMap();
	}
}