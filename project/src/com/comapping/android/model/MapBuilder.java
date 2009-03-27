/**This class provides method to build Map from comap XML file. 
 * 
 * @author Passichenko Victor
 * @author Yuri Zemlyanskiy
 * 
 */
package com.comapping.android.model;

import java.text.ParseException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.comapping.android.Log;

public class MapBuilder {
	private static final String METADATA_TAG = "metadata";
	private static final String MAP_ID_TAG = "id";
	private static final String MAP_NAME_TAG = "name";
	private static final String MAP_OWNER_TAG = "owner";
	private static final String OWNER_ID_TAG = "id";
	private static final String OWNER_NAME_TAG = "name";
	private static final String OWNER_EMAIL_TAG = "email";

	private static final String TOPIC_TAG = "node";
	private static final String TOPIC_ID_TAG = "id";
	private static final String TOPIC_LAST_MODIFICATION_DATE_TAG = "LastModificationData";
	private static final String TOPIC_BGCOLOR_TAG = "bgColor";
	private static final String TOPIC_FLAG_TAG = "flag";
	private static final String TOPIC_PRIORITY_TAG = "priority";
	private static final String TOPIC_SMILEY_TAG = "smiley";
	private static final String TOPIC_TASK_COMPLETION_TAG = "taskCompletion";
	private static final String TOPIC_TEXT_TAG = "text";
	private static final String TOPIC_ICON_TAG = "icon";
	private static final String ICON_NAME_TAG = "name";
	private static final String TOPIC_NOTE_TAG = "note";
	private static final String NOTE_TEXT_TAG = "text";
	private static final String TOPIC_TASK_TAG = "task";
	private static final String TASK_DEADLINE_TAG = "deadline";
	private static final String TASK_RESPONSIBLE_TAG = "responsible";

	/**
	 * Method that builds Map from comap XML file.
	 * 
	 * @param xmlDocument
	 *            text from comap XML file
	 * @return built map
	 * @throws StringToXMLConvertionException
	 *             when cannot convert given string to XML
	 * @throws MapParsingException
	 *             when given XML document has wrong format
	 */
	public static Map buildMap(String xmlDocument) throws StringToXMLConvertionException, MapParsingException {
		Log.i(Log.modelTag, "parsing xml document: \n" + xmlDocument);

		long startTime = System.currentTimeMillis();

		Document document = DocumentBuilder.buildDocument(xmlDocument);

		Map map;
		try {
			// parsing metadata
			Node metadata = document.getElementsByTagName(METADATA_TAG).item(0);

			int id = getIntValue(findChildNodeByName(metadata, MAP_ID_TAG));
			map = new Map(id);

			String name = getStringValue(findChildNodeByName(metadata, MAP_NAME_TAG));
			map.setName(name);

			Node ownerNode = findChildNodeByName(metadata, MAP_OWNER_TAG);
			int ownerId = getIntValue(findChildNodeByName(ownerNode, OWNER_ID_TAG));
			String ownerName = getStringValue(findChildNodeByName(ownerNode, OWNER_NAME_TAG));
			String ownerEmail = getStringValue(findChildNodeByName(ownerNode, OWNER_EMAIL_TAG));
			User owner = new User(ownerId, ownerName, ownerEmail);
			map.setOwner(owner);

			// parsing topics
			NodeList nodes = document.getElementsByTagName(TOPIC_TAG);
			if (nodes.getLength() > 0) {
				// first node with TOPIC_TAG must be root topic
				map.setRoot(buildTopic(nodes.item(0)));
			} else {
				// there is no topic in this map
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

	/**
	 * Method that builds Topic and it's children from document's node
	 * 
	 * @param node
	 * @return built topic
	 * @throws MapParsingException
	 * @throws ParseException
	 * @throws EnumParsingException
	 * @throws StringToXMLConvertionException
	 */
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

			if (curAttr.getNodeName().equals(TOPIC_ID_TAG)) {
				topic.setId(Integer.parseInt(curAttr.getNodeValue()));
				hasId = true;

			} else if (curAttr.getNodeName().equals(TOPIC_LAST_MODIFICATION_DATE_TAG)) {
				String strDate = curAttr.getNodeValue();
				topic.setLastModificationDate(strDate);

			} else if (curAttr.getNodeName().equals(TOPIC_BGCOLOR_TAG)) {
				topic.setBgColor(Integer.parseInt(curAttr.getNodeValue()));

			} else if (curAttr.getNodeName().equals(TOPIC_FLAG_TAG)) {
				String strFlag = curAttr.getNodeValue();
				topic.setFlag(Flag.parse(strFlag));

			} else if (curAttr.getNodeName().equals(TOPIC_PRIORITY_TAG)) {
				topic.setPriority(Integer.parseInt(curAttr.getNodeValue()));

			} else if (curAttr.getNodeName().equals(TOPIC_SMILEY_TAG)) {
				String strSmiley = curAttr.getNodeValue();
				topic.setSmiley(Smiley.parse(strSmiley));

			} else if (curAttr.getNodeName().equals(TOPIC_TASK_COMPLETION_TAG)) {
				String strTaskCompletion = curAttr.getNodeValue();
				topic.setTaskCompletion(TaskCompletion.parse(strTaskCompletion));
			}
		}

		if (!hasId)
			throw new MapParsingException();

		NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);

			if (childNode.getNodeName().equals(TOPIC_TEXT_TAG)) {
				topic.setText(getStringValue(childNode));

			} else if (childNode.getNodeName().equals(TOPIC_TAG)) {
				topic.addChild(buildTopic(childNode));

			} else if (childNode.getNodeName().equals(TOPIC_ICON_TAG)) {
				String iconName = childNode.getAttributes().getNamedItem(ICON_NAME_TAG).getNodeValue();
				topic.addIcon(Icon.parse(iconName));

			} else if (childNode.getNodeName().equals(TOPIC_NOTE_TAG)) {
				String note = childNode.getAttributes().getNamedItem(NOTE_TEXT_TAG).getNodeValue();
				topic.setNote(note);

			} else if (childNode.getNodeName().equals(TOPIC_TASK_TAG)) {
				String deadline = childNode.getAttributes().getNamedItem(TASK_DEADLINE_TAG).getNodeValue();
				String responsible = childNode.getAttributes().getNamedItem(TASK_RESPONSIBLE_TAG).getNodeValue();
				Task task = new Task(deadline, responsible);
				topic.setTask(task);
			}
		}

		return topic;
	}

	/**
	 * Method that returns first child of given node with specified name or null
	 * if there is no such child
	 * 
	 * @param node
	 * @param name
	 * @return found child with specified name or null if there is no such child
	 */
	private static Node findChildNodeByName(Node node, String name) {
		NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);
			if (childNode.getNodeName().equals(name)) {
				return childNode;
			}
		}

		//nothing found
		return null;
	}

	private static String getStringValue(Node node) {
		return node.getFirstChild().getNodeValue();
	}

	private static int getIntValue(Node node) {
		return Integer.parseInt(getStringValue(node));
	}
}
