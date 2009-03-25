package com.comapping.android.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.comapping.android.Log;

public class DocumentBuilder {
	public static Document buildDocument(String xmlText) throws StringToXMLConvertionException {
		Document document;
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			javax.xml.parsers.DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

			// TODO choose best method to convert String to InputStream
			// maybe it's better:
			// InputStream stream = new
			// ByteArrayInputStream(xmlDocument.getBytes("UTF-8"));
			InputStream stream = new StringBufferInputStream(xmlText);

			document = documentBuilder.parse(stream);
		} catch (FactoryConfigurationError e) {
			Log.e("Document Builder", e.toString());
			throw new DocumentBuilderCreatingError();
		} catch (ParserConfigurationException e) {
			Log.e("Document Builder", e.toString());
			throw new DocumentBuilderCreatingError();
		} catch (SAXException e) {
			Log.e(Log.modelTag, "cannot convert string to xml:" + e.toString());
			throw new StringToXMLConvertionException();
		} catch (IOException e) {
			Log.e(Log.modelTag, "cannot convert string to xml:" + e.toString());
			throw new StringToXMLConvertionException();
		}

		return document;
	}
}
