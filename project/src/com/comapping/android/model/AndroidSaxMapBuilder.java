package com.comapping.android.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.FactoryConfigurationError;

import android.util.Xml;

import org.xml.sax.SAXException;

import com.comapping.android.Log;
import com.comapping.android.model.exceptions.DocumentBuilderCreatingError;
import com.comapping.android.model.exceptions.MapParsingException;
import com.comapping.android.model.exceptions.StringToXMLConvertionException;

public class AndroidSaxMapBuilder extends MapBuilder {
	
	AndroidSaxParser handler;
	
	public Map buildMap(String xmlDocument) throws StringToXMLConvertionException, MapParsingException {
		try {
			
			InputStream stream = new ByteArrayInputStream(xmlDocument.getBytes());
			handler = new AndroidSaxParser();

			Log.d(Log.modelTag, "parsing xml document: \n" + xmlDocument);
			
			Xml.parse(stream, Xml.Encoding.UTF_8,handler.newContentHandler());			
		} catch (FactoryConfigurationError e) {
			Log.e("AndroidSAX Parser", e.toString());
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

