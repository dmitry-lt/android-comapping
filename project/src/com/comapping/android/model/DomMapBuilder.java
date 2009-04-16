/**This class provides method to build Map from comap XML file. 
 * 
 * @author Passichenko Victor
 * @author Yuri Zemlyanskiy
 * 
 */
package com.comapping.android.model;

import java.text.ParseException;
import java.util.Date;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.comapping.android.Log;
import com.comapping.android.model.exceptions.DateParsingException;
import com.comapping.android.model.exceptions.EnumParsingException;
import com.comapping.android.model.exceptions.MapParsingException;
import com.comapping.android.model.exceptions.StringToXMLConvertionException;

public class DomMapBuilder extends MapBuilder {
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
	public Map buildMap(String xmlDocument) throws StringToXMLConvertionException, MapParsingException {
		Log.d(Log.modelTag, "parsing xml document: \n" + xmlDocument);

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
				map.setRoot(buildTopic(nodes.item(0), null));
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
		} catch (DateParsingException e) {
			e.printStackTrace();
			Log.e(Log.modelTag, e.toString());
			throw new MapParsingException();
		}

		long parsingTime = System.currentTimeMillis() - startTime;
		Log.i(Log.modelTag, "map was built with DOM successfully, parsing time: " + parsingTime);

		return map;
	}

	/**
	 * Method that builds Topic and it's children from document's node
	 * 
	 * @param node
	 * @param parent
	 * @return built topic
	 * @throws MapParsingException
	 * @throws ParseException
	 * @throws EnumParsingException
	 * @throws StringToXMLConvertionException
	 * @throws DateParsingException
	 */
	private Topic buildTopic(Node node, Topic parent) throws MapParsingException, ParseException, EnumParsingException,
			StringToXMLConvertionException, DateParsingException {
		NamedNodeMap attributes = node.getAttributes();

		// Integer.parseInt(attributes.getNamedItem("id").getNodeValue());
		// Log.d("Map Builder", "parsing node with id=" + id);

		// parsing attributes
		Topic topic = new Topic(parent);
		boolean hasId = false;

		for (int i = 0; i < attributes.getLength(); i++) {
			Node curAttr = attributes.item(i);

			if (curAttr.getNodeName().equals(TOPIC_ID_TAG)) {
				topic.setId(Integer.parseInt(curAttr.getNodeValue()));
				hasId = true;

			} else if (curAttr.getNodeName().equals(TOPIC_LAST_MODIFICATION_DATE_TAG)) {
				String stringDate = curAttr.getNodeValue();
				Date date = MapBuilder.parseDate(stringDate);
				topic.setLastModificationDate(date);

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
			} else if (curAttr.getNodeName().equals(TOPIC_MAP_REF_TAG)) {
				String mapRef = curAttr.getNodeValue();
				topic.setMapRef(mapRef);
			}
		}

		if (!hasId) {
			throw new MapParsingException();
		}

		NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);

			if (childNode.getNodeName().equals(TOPIC_TEXT_TAG)) {
				topic.setText(getStringValue(childNode));

			} else if (childNode.getNodeName().equals(TOPIC_TAG)) {
				topic.addChild(buildTopic(childNode, topic));

			} else if (childNode.getNodeName().equals(TOPIC_ICON_TAG)) {
				String iconName = childNode.getAttributes().getNamedItem(ICON_NAME_TAG).getNodeValue();
				topic.addIcon(Icon.parse(iconName));

			} else if (childNode.getNodeName().equals(TOPIC_NOTE_TAG)) {
				String note = childNode.getAttributes().getNamedItem(NOTE_TEXT_TAG).getNodeValue();
				topic.setNote(note);

			} else if (childNode.getNodeName().equals(TOPIC_TASK_TAG)) {
				String start = childNode.getAttributes().getNamedItem(TASK_START_TAG).getNodeValue();
				String deadline = childNode.getAttributes().getNamedItem(TASK_DEADLINE_TAG).getNodeValue();
				String responsible = childNode.getAttributes().getNamedItem(TASK_RESPONSIBLE_TAG).getNodeValue();
				Task task = new Task(start, deadline, responsible);
				topic.setTask(task);

			} else if (childNode.getNodeName().equals(TOPIC_ATTACHMENT_TAG)) {
				// float fDate = Float.parseFloat(childNode.getAttributes().getNamedItem(ATTACHMENT_DATE_TAG).getNodeValue());
				String filename = childNode.getAttributes().getNamedItem(ATTACHMENT_FILENAME_TAG).getNodeValue();
				String key = childNode.getAttributes().getNamedItem(ATTACHMENT_KEY_TAG).getNodeValue();
				int size = Integer.parseInt(childNode.getAttributes().getNamedItem(ATTACHMENT_SIZE_TAG).getNodeValue());
				Attachment attachment = new Attachment(new Date(), filename, key, size);
				topic.setAttachment(attachment);
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
	private Node findChildNodeByName(Node node, String name) {
		NodeList childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);
			if (childNode.getNodeName().equals(name)) {
				return childNode;
			}
		}

		// nothing found
		return null;
	}

	private String getStringValue(Node node) {
		return node.getFirstChild().getNodeValue();
	}

	private int getIntValue(Node node) {
		return Integer.parseInt(getStringValue(node));
	}
}
