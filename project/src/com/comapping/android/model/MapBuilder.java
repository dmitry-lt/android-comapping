package com.comapping.android.model;

import java.io.*;
import java.sql.Time;
import java.text.ParseException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.comapping.android.Log;

/**
 * @author Passichenko Victor and Yuri Zemlyanskiy
 * 
 */
public class MapBuilder {
	public static Map buildMap(String xmlDocument) throws StringToXMLConvertionException, MapParsingException {
		Log.i(Log.modelTag, "parsing xml document: \n" + xmlDocument);

		long startTime = System.currentTimeMillis();

		Document document = DocumentBuilder.buildDocument(xmlDocument);

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
			String ownerName = getTextValue(findChildNodeByName(ownerNode, "name"));
			String ownerEmail = getTextValue(findChildNodeByName(ownerNode, "email"));
			User owner = new User(ownerId, ownerName, ownerEmail);
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
		} catch (ParseException e) {
			e.printStackTrace();
			Log.e(Log.modelTag, e.toString());
			throw new MapParsingException();
		} catch (EnumParsingException e) {
			e.printStackTrace();
			Log.e(Log.modelTag, e.toString());
			throw new MapParsingException();
		} catch (StringToXMLConvertionException e) {
			e.printStackTrace();
			Log.e(Log.modelTag, e.toString());
			throw new MapParsingException();
		}

		long parsingTime = System.currentTimeMillis() - startTime;
		Log.w(Log.modelTag, "map was built successfully, parsing time: " + parsingTime);

		return map;
	}

	private static Topic buildTopic(Node node) throws MapParsingException, ParseException, EnumParsingException,
			StringToXMLConvertionException {
		NamedNodeMap attributes = node.getAttributes();

		// int id =
		// Integer.parseInt(attributes.getNamedItem("id").getNodeValue());
		// Log.d("Map Builder", "parsing node with id=" + id);

		// parsing attributes
		Topic topic = new Topic();
		boolean hasId = false;

		for (int i = 0; i < attributes.getLength(); i++) {
			Node curAttr = attributes.item(i);

			if (curAttr.getNodeName().equals("id")) {
				topic.setId(Integer.parseInt(curAttr.getNodeValue()));
				hasId = true;

			} else if (curAttr.getNodeName().equals("LastModificationData")) {
				String strDate = curAttr.getNodeValue();
				topic.setLastModificationDate(strDate);

			} else if (curAttr.getNodeName().equals("bgColor")) {
				topic.setBgColor(Integer.parseInt(curAttr.getNodeValue()));

			} else if (curAttr.getNodeName().equals("flag")) {
				String strFlag = curAttr.getNodeValue();
				topic.setFlag(Flag.parse(strFlag));

			} else if (curAttr.getNodeName().equals("priority")) {
				topic.setPriority(Integer.parseInt(curAttr.getNodeValue()));

			} else if (curAttr.getNodeName().equals("smiley")) {
				String strSmiley = curAttr.getNodeValue();
				topic.setSmiley(Smiley.parse(strSmiley));

			} else if (curAttr.getNodeName().equals("taskCompletion")) {
				String strTaskCompletion = curAttr.getNodeValue();
				topic.setTaskCompletion(TaskCompletion.parse(strTaskCompletion));
			}
		}

		if (!hasId)
			throw new MapParsingException();

		NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);

			if (childNode.getNodeName().equals("text")) {
				topic.setText(getTextValue(childNode));
			} else if (childNode.getNodeName().equals("node")) {
				topic.addChild(buildTopic(childNode));
			} else if (childNode.getNodeName().equals("icon")) {
				String iconName = childNode.getAttributes().getNamedItem("name").getNodeValue();
				topic.addIcon(Icon.parse(iconName));
			} else if (childNode.getNodeName().equals("note")) {
				String note = childNode.getAttributes().getNamedItem("text").getNodeValue();
				topic.setNote(note);
			} else if (childNode.getNodeName().equals("task")) {
				String deadline = childNode.getAttributes().getNamedItem("deadline").getNodeValue();
				String responsible = childNode.getAttributes().getNamedItem("responsible").getNodeValue();
				Task task = new Task(deadline, responsible);
				topic.setTask(task);
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
