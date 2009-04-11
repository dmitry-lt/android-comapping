/**This class provides method to build Map with SAX Parser. 
 * 
 * @author Dmitry Manayev
 * 
 */
package com.comapping.android.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.comapping.android.Log;

class SaxHandler extends DefaultHandler {

	private boolean isMetaDataTag = false;
	private boolean isMapIDTag = false;
	private boolean isMapNameTag = false;
	private boolean isOwnerTag = false;
	private boolean isOwnerIDTag = false;
	private boolean isOwnerNameTag = false;
	private boolean isOwnerEmailTag = false;
	private boolean isTopicTag = false;
	private boolean isTextTag = false;

	private int MapId = 0;
	private String MapName = null;
	private int ownerId;
	private String ownerName;
	private String ownerEmail;
	private User owner;
	private Map map;
	private String topicText;
	private Topic currentTopic = null;
	private Topic parent = null;
	private long startTime;

	public void startDocument() {
		startTime = System.currentTimeMillis();
		Log.i(Log.modelTag, "SAX parsing started... \n");
	}

	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		try {
			int length = attributes.getLength();
			if (localName.equals(MapBuilder.METADATA_TAG)) {
				isMetaDataTag = true;
			} else if (localName.equals(MapBuilder.MAP_ID_TAG) && MapId == 0) {
				isMapIDTag = true;
			} else if (localName.equals(MapBuilder.MAP_NAME_TAG)
					&& MapName == null) {
				isMapNameTag = true;
			} else if (localName.equals(MapBuilder.MAP_OWNER_TAG)) {
				isOwnerTag = true;
			} else if (localName.equals(MapBuilder.OWNER_ID_TAG) && isOwnerTag) {
				isOwnerIDTag = true;
			} else if (localName.equals(MapBuilder.OWNER_NAME_TAG)
					&& isOwnerTag) {
				isOwnerNameTag = true;
			} else if (localName.equals(MapBuilder.OWNER_EMAIL_TAG)
					&& isOwnerTag) {
				isOwnerEmailTag = true;
			} else if (localName.equals(MapBuilder.TOPIC_TAG)) {
				isTopicTag = true;
				currentTopic = new Topic(parent);
				parent = currentTopic.getParent();
				getTopicAttributes(attributes);
				if (parent == null) {
					parent = currentTopic;
					map.setRoot(currentTopic);
				} else {
					currentTopic.getParent().addChild(currentTopic);
				}
			} else if (localName.equals(MapBuilder.TOPIC_TEXT_TAG)) {
				isTextTag = true;
			} else if (localName.equals(MapBuilder.TOPIC_ICON_TAG)) {
				String iconName = attributes.getValue(MapBuilder.ICON_NAME_TAG);
				currentTopic.addIcon(Icon.parse(iconName));
			} else if (localName.equals(MapBuilder.TOPIC_NOTE_TAG)) {
				String note = attributes.getValue(MapBuilder.NOTE_TEXT_TAG);
				currentTopic.setNote(note);

			} else if (localName.equals(MapBuilder.TOPIC_TASK_TAG)) {
				String start = attributes.getValue(MapBuilder.TASK_START_TAG);
				String deadline = attributes
						.getValue(MapBuilder.TASK_DEADLINE_TAG);
				String responsible = attributes
						.getValue(MapBuilder.TASK_RESPONSIBLE_TAG);
				Task task = new Task(start, deadline, responsible);
			} else if (qName.equals(MapBuilder.TOPIC_TASK_TAG)) {
				String start = attributes.getValue(MapBuilder.TASK_START_TAG);
				String deadline = attributes
						.getValue(MapBuilder.TASK_DEADLINE_TAG);
				String responsible = attributes
						.getValue(MapBuilder.TASK_RESPONSIBLE_TAG);
				Task task = new Task(start, deadline, responsible);
				currentTopic.setTask(task);
			} else if (localName.equals(MapBuilder.TOPIC_ATTACHMENT_TAG)) {
				float fDate = Float.parseFloat(attributes
						.getValue(MapBuilder.ATTACHMENT_DATE_TAG));
				String filename = attributes
						.getValue(MapBuilder.ATTACHMENT_FILENAME_TAG);
				String key = attributes.getValue(MapBuilder.ATTACHMENT_KEY_TAG);
				int size = Integer.parseInt(attributes
						.getValue(MapBuilder.ATTACHMENT_SIZE_TAG));
				Attachment attachment = new Attachment(new Date(), filename,
						key, size);
				currentTopic.setAttachment(attachment);
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
			Log.e(Log.modelTag, e.toString());
			throw new SAXException();
		} catch (NumberFormatException e) {
			e.printStackTrace();
			Log.e(Log.modelTag, e.toString());
			throw new SAXException();
		} catch (EnumParsingException e) {
			e.printStackTrace();
			Log.e(Log.modelTag, e.toString());
			throw new SAXException();
		}
	}

	public void getTopicAttributes(Attributes attributes) throws SAXException {
		try {
			int length = attributes.getLength();
			for (int i = 0; i < length; i++) {
				String qName = attributes.getQName(i);
				if (qName.equals(MapBuilder.TOPIC_ID_TAG)) {
					currentTopic
							.setId(Integer.parseInt(attributes.getValue(i)));
					int id = Integer.parseInt(attributes.getValue(i));
					Log.d("Map Builder", "parsing node with id=" + id);
				} else if (qName
						.equals(MapBuilder.TOPIC_LAST_MODIFICATION_DATE_TAG)) {
					String strDate = attributes.getValue(i);
					SimpleDateFormat dateFormat = new SimpleDateFormat();
					dateFormat.applyPattern("yyyy-MM-dd HH:mm:ss");
					Date date = new Date();
					try {
						date = dateFormat.parse(strDate);
					} catch (ParseException e) {
						throw new DateParsingException();
					}
					currentTopic.setLastModificationDate(date);
				} else if (qName.equals(MapBuilder.TOPIC_BGCOLOR_TAG)) {
					currentTopic.setBgColor(Integer.parseInt(attributes
							.getValue(i)));
				} else if (qName.equals(MapBuilder.TOPIC_FLAG_TAG)) {
					String strFlag = attributes.getValue(i);
					currentTopic.setFlag(Flag.parse(strFlag));
				} else if (qName.equals(MapBuilder.TOPIC_PRIORITY_TAG)) {
					currentTopic.setPriority(Integer.parseInt(attributes
							.getValue(i)));
				} else if (qName.equals(MapBuilder.TOPIC_SMILEY_TAG)) {
					String strSmiley = attributes.getValue(i);
					currentTopic.setSmiley(Smiley.parse(strSmiley));
				} else if (qName.equals(MapBuilder.TOPIC_TASK_COMPLETION_TAG)) {
					String strTaskCompletion = attributes.getValue(i);
					currentTopic.setTaskCompletion(TaskCompletion
							.parse(strTaskCompletion));
				} else if (qName.equals(MapBuilder.TOPIC_MAP_REF_TAG)) {
					String mapRef = attributes.getValue(i);
					currentTopic.setMapRef(mapRef);
				}
			}
		} catch (DateParsingException e) {
			e.printStackTrace();
			Log.e(Log.modelTag, e.toString());
			throw new SAXException();
		} catch (EnumParsingException e) {
			e.printStackTrace();
			Log.e(Log.modelTag, e.toString());
			throw new SAXException();
		}

	}

	public void characters(char[] ch, int start, int len) throws SAXException {
		String string = new String(ch, start, len);
		if (isMapIDTag) {
			MapId = Integer.parseInt(string);
			isMapIDTag = false;
		} else if (isMapNameTag) {
			MapName = string;
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
		} else if (isTopicTag) {

		} else if (isTextTag) {
			topicText = string;
			isTextTag = false;
		}
	}

	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		try {
			if (localName.equals(MapBuilder.MAP_OWNER_TAG)) {
				owner = new User(ownerId, ownerName, ownerEmail);
				isOwnerTag = false;
			} else if (localName.equals(MapBuilder.METADATA_TAG)) {
				map = new Map(MapId);
				map.setName(MapName);
				map.setOwner(owner);
				isMetaDataTag = false;
			} else if (localName.equals(MapBuilder.TOPIC_TAG)) {
				if (currentTopic.getParent() != null) {
					currentTopic = currentTopic.getParent();
				}
			} else if (localName.equals(MapBuilder.TOPIC_TEXT_TAG)) {
				currentTopic.setText(topicText);
			}
		} catch (StringToXMLConvertionException e) {
			e.printStackTrace();
			Log.e(Log.modelTag, e.toString());
			throw new SAXException();
		}
	}

	public void setMap(Map newmap) {
		map = newmap;
	}

	public Map getMap() {
		return map;
	}

	public void endDocument() {
		setMap(map);
		long parsingTime = System.currentTimeMillis() - startTime;
		Log.w(Log.modelTag, "map was built successfully, parsing time: "
				+ parsingTime);
	}
}