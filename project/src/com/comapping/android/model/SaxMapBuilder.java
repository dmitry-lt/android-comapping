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
import com.comapping.android.model.exceptions.MapParsingException;
import com.comapping.android.model.exceptions.StringToXMLConvertionException;

public class SaxMapBuilder extends MapBuilder {

	SaxHandler handler;

	@Override
	public Map buildMap(String xmlDocument) throws StringToXMLConvertionException, MapParsingException {
		try {
			SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
			SAXParser parser = saxParserFactory.newSAXParser();

			InputStream stream = new ByteArrayInputStream(xmlDocument.getBytes("UTF-8"));
			handler = new SaxHandler();

			Log.d(Log.modelTag, "parsing xml document: \n" + xmlDocument);
			
			parser.parse(stream, handler);			
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