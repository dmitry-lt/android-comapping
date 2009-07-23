package com.comapping.android.map.model.map.builder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import com.comapping.android.Log;
import com.comapping.android.map.model.exceptions.DocumentBuilderCreatingError;
import com.comapping.android.map.model.exceptions.MapParsingException;
import com.comapping.android.map.model.exceptions.StringToXMLConvertionException;
import com.comapping.android.map.model.map.Map;

public class SaxMapBuilder extends MapBuilder {

	SaxHandler handler;

	public Map buildMap(InputStream xmlDocument) throws StringToXMLConvertionException, MapParsingException
	{
		try {
			SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
			SAXParser parser = saxParserFactory.newSAXParser();

			handler = new SaxHandler();
			
			parser.parse(xmlDocument, handler);			
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
	
	public Map buildMap(String xmlDocument) throws StringToXMLConvertionException, MapParsingException {
		InputStream stream;
		try {
			stream = new ByteArrayInputStream(xmlDocument.getBytes("UTF-8"));
			return buildMap(stream);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new StringToXMLConvertionException();
		}
	}
}