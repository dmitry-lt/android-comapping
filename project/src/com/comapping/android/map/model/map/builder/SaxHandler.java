/**This class provides method to build Map with SAX Parser. 
 * 
 * @author Dmitry Manayev
 * 
 */
package com.comapping.android.map.model.map.builder;

import java.util.Date;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.comapping.android.Log;
import com.comapping.android.map.model.exceptions.EnumParsingException;
import com.comapping.android.map.model.exceptions.MapParsingException;
import com.comapping.android.map.model.exceptions.StringToXMLConvertionException;
import com.comapping.android.map.model.map.Attachment;
import com.comapping.android.map.model.map.Flag;
import com.comapping.android.map.model.map.Icon;
import com.comapping.android.map.model.map.Map;
import com.comapping.android.map.model.map.Smiley;
import com.comapping.android.map.model.map.Arrow;
import com.comapping.android.map.model.map.Star;
import com.comapping.android.map.model.map.Task;
import com.comapping.android.map.model.map.TaskCompletion;
import com.comapping.android.map.model.map.Topic;
import com.comapping.android.map.model.map.User;

class SaxHandler extends DefaultHandler {
	private static int base = 1000;

	private static int TOPIC_ID_TAG_HASHCODE = mod(MapBuilder.TOPIC_ID_ATTR
			.hashCode());
	private static int TOPIC_BGCOLOR_TAG_HASHCODE = mod(MapBuilder.TOPIC_BGCOLOR_ATTR
			.hashCode());
	private static int TOPIC_FLAG_TAG_HASHCODE = mod(MapBuilder.TOPIC_FLAG_ATTR
			.hashCode());
	private static int TOPIC_PRIORITY_TAG_HASHCODE = mod(MapBuilder.TOPIC_PRIORITY_ATTR
			.hashCode());
	private static int TOPIC_SMILEY_TAG_HASHCODE = mod(MapBuilder.TOPIC_SMILEY_ATTR
			.hashCode());
	private static int TOPIC_ARROW_TAG_HASHCODE = mod(MapBuilder.TOPIC_ARROW_ATTR
			.hashCode());
	private static int TOPIC_STAR_TAG_HASHCODE = mod(MapBuilder.TOPIC_STAR_ATTR
			.hashCode());
	private static int TOPIC_TASK_COMPLETION_TAG_HASHCODE = mod(MapBuilder.TOPIC_TASK_COMPLETION_ATTR
			.hashCode());
	private static int TOPIC_MAP_REF_TAG_HASHCODE = mod(MapBuilder.TOPIC_MAP_REF_TAG
			.hashCode());

	private String[] attributesMap = new String[base]; // make it global for
	// memory and time
	// saving

	private boolean isMapIDTag = false;
	private boolean isMapNameTag = false;
	private boolean isOwnerTag = false;
	private boolean isOwnerIDTag = false;
	private boolean isOwnerNameTag = false;
	private boolean isOwnerEmailTag = false;
	private boolean isTextTag = false;
	private boolean isNoteTag = false;

	private boolean hasMeta = false;

	private int mapId = 0;
	private String mapName = null;

	private int ownerId;
	private String ownerName;
	private String ownerEmail;
	private User owner;
	private Map map;
	private String topicText;
	private String noteText;
	private Topic currentTopic = null;
	private long startTime;

	/**
	 * Method for getting module on base, with always positive result
	 * 
	 * @param value
	 * @return value mod base
	 */
	private static int mod(int value) {
		int tmp = value % base;

		return (tmp >= 0) ? tmp : tmp + base;
	}

	public void startDocument() {
		startTime = System.currentTimeMillis();

		// Log.i(Log.modelTag, "SAX parsing started... \n");
	}

	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		try {
			if (localName.equals(MapBuilder.TOPIC_TEXT_TAG)) {
				isTextTag = true;
			} else if (localName.equals(MapBuilder.TOPIC_TAG)) {
				currentTopic = new Topic(currentTopic);
				getTopicAttributes(attributes);
				if (currentTopic.isRoot()) {
					map.setRoot(currentTopic);
				} else {
					currentTopic.getParent().addChild(currentTopic);
				}
			} else if (localName.equals(MapBuilder.TOPIC_ICON_TAG)) {

				String iconName = attributes
						.getValue(MapBuilder.ICON_NAME_ATTR);
				Icon icon = Icon.parse(iconName);
				if (icon != null) {
					currentTopic.addIcon(Icon.parse(iconName));
				}

			} else if (localName.equals(MapBuilder.TOPIC_NOTE_TAG)) {
				String note = attributes.getValue(MapBuilder.NOTE_TEXT_ATTR);
				if (note != null) {
					currentTopic.setNote(note);
					noteText = null;
				} else {
					isNoteTag = true;
					noteText = "";
				}

			} else if (localName.equals(MapBuilder.TOPIC_TASK_TAG)) {
				String start = attributes.getValue(MapBuilder.TASK_START_ATTR);
				String deadline = attributes
						.getValue(MapBuilder.TASK_DEADLINE_ATTR);
				String responsible = attributes
						.getValue(MapBuilder.TASK_RESPONSIBLE_ATTR);
				String estimate = attributes
						.getValue(MapBuilder.TASK_ESTIMATE_ATTR);
				Task task = new Task(start, deadline, responsible, estimate);
				currentTopic.setTask(task);
			} else if (localName.equals(MapBuilder.TOPIC_ATTACHMENT_TAG)) {
				Date date = new Date((long) Float.parseFloat(attributes
						.getValue(MapBuilder.ATTACHMENT_DATE_ATTR)));
				String filename = attributes
						.getValue(MapBuilder.ATTACHMENT_FILENAME_ATTR);
				String key = attributes
						.getValue(MapBuilder.ATTACHMENT_KEY_ATTR);
				int size = Integer.parseInt(attributes
						.getValue(MapBuilder.ATTACHMENT_SIZE_ATTR));
				Attachment attachment = new Attachment(date, filename, key,
						size);
				currentTopic.setAttachment(attachment);
			} else if (!hasMeta) {
				if (localName.equals(MapBuilder.METADATA_TAG)) {
				} else if (localName.equals(MapBuilder.MAP_ID_TAG)
						&& mapId == 0) {
					isMapIDTag = true;
				} else if (localName.equals(MapBuilder.MAP_NAME_TAG)
						&& mapName == null) {
					isMapNameTag = true;
				} else if (localName.equals(MapBuilder.MAP_OWNER_TAG)) {
					isOwnerTag = true;
				} else if (localName.equals(MapBuilder.OWNER_ID_TAG)
						&& isOwnerTag) {
					isOwnerIDTag = true;
				} else if (localName.equals(MapBuilder.OWNER_NAME_TAG)
						&& isOwnerTag) {
					isOwnerNameTag = true;
				} else if (localName.equals(MapBuilder.OWNER_EMAIL_TAG)
						&& isOwnerTag) {
					isOwnerEmailTag = true;
				}
			}

		} catch (NullPointerException e) {
			e.printStackTrace();
			Log.e(Log.MODEL_TAG, e.toString());
			throw new SAXException();
		} catch (NumberFormatException e) {
			e.printStackTrace();
			Log.e(Log.MODEL_TAG, e.toString());
			throw new SAXException();
		}
	}

	private void getTopicAttributes(Attributes attributes) throws SAXException {
		int length = attributes.getLength();
		for (int i = 0; i < length; i++) {
			attributesMap[mod(attributes.getLocalName(i).hashCode())] = attributes
					.getValue(i);
		}

		if (attributesMap[TOPIC_ID_TAG_HASHCODE] != null) {
			currentTopic.setId(Integer
					.parseInt(attributesMap[TOPIC_ID_TAG_HASHCODE]));
			attributesMap[TOPIC_ID_TAG_HASHCODE] = null;
		}

		if (attributesMap[TOPIC_BGCOLOR_TAG_HASHCODE] != null) {
			currentTopic.setBgColor(Integer
					.parseInt(attributesMap[TOPIC_BGCOLOR_TAG_HASHCODE]));
			attributesMap[TOPIC_BGCOLOR_TAG_HASHCODE] = null;
		}

		if (attributesMap[TOPIC_FLAG_TAG_HASHCODE] != null) {
			currentTopic.setFlag(Flag
					.parse(attributesMap[TOPIC_FLAG_TAG_HASHCODE]));
			attributesMap[TOPIC_FLAG_TAG_HASHCODE] = null;
		}

		if (attributesMap[TOPIC_PRIORITY_TAG_HASHCODE] != null) {
			currentTopic.setPriority(Integer
					.parseInt(attributesMap[TOPIC_PRIORITY_TAG_HASHCODE]));
			attributesMap[TOPIC_PRIORITY_TAG_HASHCODE] = null;
		}

		if (attributesMap[TOPIC_SMILEY_TAG_HASHCODE] != null) {
			currentTopic.setSmiley(Smiley
					.parse(attributesMap[TOPIC_SMILEY_TAG_HASHCODE]));
			attributesMap[TOPIC_SMILEY_TAG_HASHCODE] = null;
		}

		if (attributesMap[TOPIC_ARROW_TAG_HASHCODE] != null) {
			currentTopic.setArrow(Arrow
					.parse(attributesMap[TOPIC_ARROW_TAG_HASHCODE]));
			attributesMap[TOPIC_ARROW_TAG_HASHCODE] = null;
		}

		if (attributesMap[TOPIC_STAR_TAG_HASHCODE] != null) {
			currentTopic.setStar(Star
					.parse(attributesMap[TOPIC_STAR_TAG_HASHCODE]));
			attributesMap[TOPIC_STAR_TAG_HASHCODE] = null;
		}

		if (attributesMap[TOPIC_TASK_COMPLETION_TAG_HASHCODE] != null) {
			currentTopic.setTaskCompletion(TaskCompletion
					.parse(attributesMap[TOPIC_TASK_COMPLETION_TAG_HASHCODE]));
			attributesMap[TOPIC_TASK_COMPLETION_TAG_HASHCODE] = null;
		}

		if (attributesMap[TOPIC_MAP_REF_TAG_HASHCODE] != null) {
			currentTopic.setMapRef(attributesMap[TOPIC_MAP_REF_TAG_HASHCODE]);
			attributesMap[TOPIC_MAP_REF_TAG_HASHCODE] = null;
		}

	}

	public void characters(char[] ch, int start, int len) throws SAXException {
		String string = new String(ch, start, len);
		if (isTextTag) {
			topicText = string;
			isTextTag = false;
		} else if (isNoteTag) {
			noteText += string;
		} else if (!hasMeta) {
			if (isMapIDTag) {
				mapId = Integer.parseInt(string);
				isMapIDTag = false;
			} else if (isMapNameTag) {
				mapName = string;
				isMapNameTag = false;
			} else if (isOwnerIDTag) {
				ownerId = Integer.parseInt(string);
				isOwnerIDTag = false;
			} else if (isOwnerNameTag) {
				ownerName = string;
				isOwnerNameTag = false;
			} else if (isOwnerEmailTag) {
				ownerEmail = string;
				isOwnerEmailTag = false;
			}
		}
	}

	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		// try {
		if (localName.equals(MapBuilder.TOPIC_TEXT_TAG)) {
			try {
				currentTopic.setHtmlText(topicText);
			} catch (StringToXMLConvertionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (localName.equals(MapBuilder.TOPIC_TAG)) {
			if (currentTopic.getParent() != null) {
				currentTopic = currentTopic.getParent();
			}
		} else if (localName.equals(MapBuilder.TOPIC_NOTE_TAG)) {
			isNoteTag = false;
			if (noteText != null) {
				currentTopic.setNote(noteText);
			}
		} else if (!hasMeta) {
			if (localName.equals(MapBuilder.MAP_OWNER_TAG)) {
				owner = new User(ownerId, ownerName, ownerEmail);
				isOwnerTag = false;
			} else if (localName.equals(MapBuilder.METADATA_TAG)) {
				map = new Map(mapId);
				map.setName(mapName);
				map.setOwner(owner);

				hasMeta = true;
			}
		}
		// } catch (StringToXMLConvertionException e) {
		// e.printStackTrace();
		// Log.e(Log.MODEL_TAG, e.toString());
		// throw new SAXException();
		// }
	}

	public Map getMap() throws MapParsingException {
		if (map.getRoot() == null)
			throw new MapParsingException();
		return map;
	}

	public void endDocument() {
		long parsingTime = System.currentTimeMillis() - startTime;
		Log.i(Log.MODEL_TAG,
				"map was built with SAX successfully, parsing time: "
						+ parsingTime);
	}
}