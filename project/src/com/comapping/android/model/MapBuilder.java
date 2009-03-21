package com.comapping.android.model;

import java.io.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.comapping.android.Log;

/**
 * @author Passichenko Victor and Yuri Zemlyanskiy
 * 
 */
public class MapBuilder {
	public static Map buildMap(String xmlDocument)
			throws StringToXMLConvertionException, MapParsingException {
		Log.i(Log.modelTag, "parsing xml document: \n" + xmlDocument);		
		// creating document
		Document document;
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory
					.newDocumentBuilder();

			// TODO choose best method to convert String to InputStream
			// maybe it's better:
			// InputStream stream = new
			// ByteArrayInputStream(xmlDocument.getBytes("UTF-8"));
			InputStream stream = new StringBufferInputStream(xmlDocument);

			document = documentBuilder.parse(stream);
		} catch (FactoryConfigurationError e) {
			Log.e("Map Builder", e.toString());
			throw new DocumentBuilderCreatingError();
		} catch (ParserConfigurationException e) {
			Log.e("Map Builder", e.toString());
			throw new DocumentBuilderCreatingError();
		} catch (SAXException e) {
			Log
					.e(Log.modelTag, "cannot convert string to xml:"
							+ e.toString());
			throw new StringToXMLConvertionException();
		} catch (IOException e) {
			Log
					.e(Log.modelTag, "cannot convert string to xml:"
							+ e.toString());
			throw new StringToXMLConvertionException();
		}

		Map map;
		try {
			// parsing metadata
			Node metadata = document.getElementsByTagName("metadata").item(0);

			int id = getIntValue(findChildNodeByName(metadata, "id"));
			map = new Map(id);

			String name = getTextValue(findChildNodeByName(metadata, "name"));
			map.setName(name);

			Node ownerNode = findChildNodeByName(metadata, "owner");
			int ownerId = getIntValue(findChildNodeByName(ownerNode, "id"));
			String ownerName = getTextValue(findChildNodeByName(ownerNode,
					"name"));
			String ownerEmail = getTextValue(findChildNodeByName(ownerNode,
					"email"));
			Map.User owner = new Map.User(ownerId, ownerName, ownerEmail);
			map.setOwner(owner);

			// parsing topics
			NodeList nodes = document.getElementsByTagName("node");
			if (nodes.getLength() > 0) {
				map.setRoot(buildTopic(nodes.item(0)));
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
			Log.e(Log.modelTag, e.toString());
			throw new MapParsingException();
		} catch (NumberFormatException e) {
			e.printStackTrace();
			Log.e(Log.modelTag, e.toString());
			throw new MapParsingException();
		} catch (DOMException e) {
			e.printStackTrace();
			Log.e(Log.modelTag, e.toString());
			throw new MapParsingException();
		}

		return map;
	}

	private static Topic buildTopic(Node node) {
		NamedNodeMap attribures = node.getAttributes();
		int id = Integer.parseInt(attribures.getNamedItem("id").getNodeValue());

		// Log.d("Map Builder", "parsing node with id=" + id);

		Topic topic = new Topic(id);

		NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);

			if (childNode.getNodeName().equals("text")) {
				topic.setText(getTextValue(childNode));
			} else if (childNode.getNodeName().equals("node")) {
				topic.addChild(buildTopic(childNode));
			}
		}

		return topic;
	}

	private static Node findChildNodeByName(Node node, String name) {
		NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);
			if (childNode.getNodeName().equals(name))
				return childNode;
		}

		return null;
	}

	private static String getTextValue(Node node) {
		return node.getFirstChild().getNodeValue();
	}

	private static int getIntValue(Node node) {
		return Integer.parseInt(getTextValue(node));
	}
}
